package de.cyberwarfare.gui.minigames;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.managers.HackingManager.HackingSession;
import de.cyberwarfare.models.HackTarget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Base class for all hacking minigames
 */
public abstract class BaseMinigame implements Listener {
    
    protected final CyberWarfarePlugin plugin;
    protected final Player player;
    protected final HackTarget target;
    protected final HackingSession session;
    protected Inventory gui;
    protected BukkitRunnable timer;
    protected int timeLeft;
    protected boolean isActive;
    
    protected static final int GAME_TIME = 30; // 30 seconds
    
    public BaseMinigame(CyberWarfarePlugin plugin, Player player, HackTarget target, HackingSession session) {
        this.plugin = plugin;
        this.player = player;
        this.target = target;
        this.session = session;
        this.timeLeft = GAME_TIME;
        this.isActive = false;
        
        // Register event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Starts the minigame
     */
    public void start() {
        if (isActive) return;
        
        isActive = true;
        createGUI();
        player.openInventory(gui);
        startTimer();
        onGameStart();
    }
    
    /**
     * Ends the minigame
     */
    public void end(boolean success) {
        if (!isActive) return;
        
        isActive = false;
        
        // Stop timer
        if (timer != null) {
            timer.cancel();
        }
        
        // Unregister events
        HandlerList.unregisterAll(this);
        
        // Handle success/failure
        if (success) {
            int reward = target.getHackReward();
            plugin.getHackingManager().onHackSuccess(player, target, reward);
        } else {
            int penalty = target.getTracePenalty();
            plugin.getHackingManager().onHackFailure(player, target, penalty);
        }
        
        onGameEnd(success);
    }
    
    /**
     * Creates the GUI for this minigame
     */
    protected abstract void createGUI();
    
    /**
     * Called when the game starts
     */
    protected abstract void onGameStart();
    
    /**
     * Called when the game ends
     */
    protected abstract void onGameEnd(boolean success);
    
    /**
     * Called every second during the game
     */
    protected abstract void onTimerTick();
    
    /**
     * Handles GUI clicks - override in subclasses
     */
    protected abstract void handleClick(InventoryClickEvent event);
    
    /**
     * Starts the countdown timer
     */
    private void startTimer() {
        timer = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) {
                    cancel();
                    return;
                }
                
                timeLeft--;
                updateTimerDisplay();
                onTimerTick();
                
                if (timeLeft <= 0) {
                    end(false); // Time's up = failure
                }
            }
        };
        
        timer.runTaskTimer(plugin, 20L, 20L); // Run every second
    }
    
    /**
     * Updates the timer display in GUI
     */
    protected void updateTimerDisplay() {
        if (gui == null) return;
        
        ItemStack timerItem = createTimerItem();
        gui.setItem(4, timerItem); // Timer in top center
    }
    
    /**
     * Creates timer display item
     */
    protected ItemStack createTimerItem() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        
        NamedTextColor timeColor = timeLeft > 10 ? NamedTextColor.GREEN : 
                                 timeLeft > 5 ? NamedTextColor.YELLOW : NamedTextColor.RED;
        
        meta.displayName(Component.text("⏰ Zeit: " + timeLeft + "s", timeColor, TextDecoration.BOLD));
        
        List<Component> lore = List.of(
            Component.empty(),
            Component.text("Verbleibende Zeit bis Timeout", NamedTextColor.GRAY)
        );
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates target info item
     */
    protected ItemStack createTargetInfoItem() {
        ItemStack item = new ItemStack(target.getTargetType().getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("🎯 " + target.getTargetType().getDisplayName(), NamedTextColor.AQUA, TextDecoration.BOLD));
        
        List<Component> lore = List.of(
            Component.empty(),
            Component.text("Schwierigkeit: ", NamedTextColor.GRAY)
                .append(Component.text("★".repeat(target.getEffectiveDifficulty()), NamedTextColor.YELLOW)),
            Component.text("Belohnung: ", NamedTextColor.GRAY)
                .append(Component.text(target.getHackReward() + " Credits", NamedTextColor.GOLD)),
            Component.empty(),
            Component.text("Hacke dieses Ziel!", NamedTextColor.GREEN, TextDecoration.ITALIC)
        );
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates progress bar item
     */
    protected ItemStack createProgressItem(int progress, int max, String label) {
        Material material = progress >= max ? Material.LIME_STAINED_GLASS_PANE : Material.YELLOW_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        double percentage = (double) progress / max * 100;
        NamedTextColor color = progress >= max ? NamedTextColor.GREEN : NamedTextColor.YELLOW;
        
        meta.displayName(Component.text(label, color, TextDecoration.BOLD));
        
        List<Component> lore = List.of(
            Component.empty(),
            Component.text("Fortschritt: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%.0f%%", percentage), color)),
            Component.text(progress + "/" + max, NamedTextColor.WHITE)
        );
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates filler glass pane
     */
    protected ItemStack createFillerItem() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Fills empty slots with filler items
     */
    protected void fillEmptySlots() {
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, createFillerItem());
            }
        }
    }
    
    /**
     * Handles inventory click events
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clickedPlayer)) {
            return;
        }
        
        if (!clickedPlayer.equals(player) || event.getInventory() != gui) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!isActive) {
            return;
        }
        
        handleClick(event);
    }
}