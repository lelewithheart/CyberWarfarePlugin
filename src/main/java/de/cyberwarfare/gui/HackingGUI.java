package de.cyberwarfare.gui;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.managers.HackingManager.HackingSession;
import de.cyberwarfare.managers.TargetManager.TargetType;
import de.cyberwarfare.models.HackTarget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Main GUI for hacking interface
 */
public class HackingGUI implements Listener {
    
    private final CyberWarfarePlugin plugin;
    private final HackingSession session;
    
    public HackingGUI(CyberWarfarePlugin plugin, HackingSession session) {
        this.plugin = plugin;
        this.session = session;
        
        // Register event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Opens target selection interface
     */
    public void openTargetSelection() {
        Player player = session.getPlayer();
        
        String title = session.isMobileSession() ? "📱 Mobile Terminal" : "🖥 Hacking Terminal";
        Inventory gui = Bukkit.createInventory(null, 27, Component.text(title));
        
        // Header
        gui.setItem(4, createHeaderItem());
        
        // Available targets
        List<HackTarget> targets = session.getAvailableTargets();
        for (int i = 0; i < Math.min(targets.size(), 18); i++) {
            HackTarget target = targets.get(i);
            gui.setItem(9 + i, createTargetItem(target));
        }
        
        // Info item
        gui.setItem(22, createInfoItem());
        
        // Close button
        gui.setItem(26, createCloseItem());
        
        player.openInventory(gui);
    }
    
    /**
     * Creates header item showing terminal info
     */
    private ItemStack createHeaderItem() {
        ItemStack item = new ItemStack(session.isMobileSession() ? Material.RECOVERY_COMPASS : Material.COMPUTER);
        ItemMeta meta = item.getItemMeta();
        
        Component name = Component.text("Hacking Terminal", NamedTextColor.AQUA, TextDecoration.BOLD);
        meta.displayName(name);
        
        List<Component> lore = List.of(
            Component.empty(),
            Component.text("Security Level: ", NamedTextColor.GRAY)
                .append(Component.text(session.getSecurityLevel(), NamedTextColor.YELLOW)),
            Component.empty(),
            Component.text("Wähle ein Ziel zum Hacken", NamedTextColor.GREEN, TextDecoration.ITALIC)
        );
        
        if (session.isMobileSession()) {
            lore = new java.util.ArrayList<>(lore);
            lore.add(2, Component.text("Battery: ", NamedTextColor.GRAY)
                .append(Component.text(session.getMobileTerminal().getBatteryLevel() + "%", 
                    session.getMobileTerminal().getBatteryLevel() > 25 ? NamedTextColor.GREEN : NamedTextColor.RED)));
        }
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates item representing a hackable target
     */
    private ItemStack createTargetItem(HackTarget target) {
        ItemStack item = new ItemStack(target.getTargetType().getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        // Name with status indicator
        String statusIcon = target.isCompromised() ? "🔓" : "🔒";
        Component name = Component.text(statusIcon + " " + target.getTargetType().getDisplayName(), 
            target.isCompromised() ? NamedTextColor.YELLOW : NamedTextColor.WHITE, TextDecoration.BOLD);
        meta.displayName(name);
        
        // Lore with target info
        List<Component> lore = List.of(
            Component.empty(),
            Component.text("Typ: ", NamedTextColor.GRAY)
                .append(Component.text(target.getTargetType().getDisplayName(), NamedTextColor.WHITE)),
            Component.text("Schwierigkeit: ", NamedTextColor.GRAY)
                .append(getDifficultyComponent(target.getEffectiveDifficulty())),
            Component.text("Belohnung: ", NamedTextColor.GRAY)
                .append(Component.text(target.getHackReward() + " Credits", NamedTextColor.GOLD)),
            Component.empty()
        );
        
        if (target.isCompromised()) {
            lore = new java.util.ArrayList<>(lore);
            lore.add(Component.text("⚠ Bereits kompromittiert", NamedTextColor.YELLOW, TextDecoration.ITALIC));
            lore.add(Component.text("Reduzierte Belohnung", NamedTextColor.GRAY, TextDecoration.ITALIC));
        }
        
        lore = new java.util.ArrayList<>(lore);
        lore.add(Component.text("Klicken zum Hacken", NamedTextColor.GREEN, TextDecoration.ITALIC));
        
        meta.lore(lore);
        item.setItemMeta(meta);
        
        // Store target data in NBT
        meta.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(plugin, "target_id"), 
            org.bukkit.persistence.PersistentDataType.INTEGER, 
            target.getId()
        );
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Gets difficulty component with color coding
     */
    private Component getDifficultyComponent(int difficulty) {
        String stars = "★".repeat(Math.min(difficulty, 5));
        NamedTextColor color;
        
        if (difficulty <= 2) {
            color = NamedTextColor.GREEN;
        } else if (difficulty <= 4) {
            color = NamedTextColor.YELLOW;
        } else if (difficulty <= 6) {
            color = NamedTextColor.GOLD;
        } else {
            color = NamedTextColor.RED;
        }
        
        return Component.text(stars + " (" + difficulty + ")", color);
    }
    
    /**
     * Creates info item
     */
    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("ℹ Hacking Info", NamedTextColor.AQUA, TextDecoration.BOLD));
        
        List<Component> lore = List.of(
            Component.empty(),
            Component.text("Hacking Grundlagen:", NamedTextColor.YELLOW),
            Component.text("• Höhere Schwierigkeit = Mehr Belohnung", NamedTextColor.GRAY),
            Component.text("• Fehlschläge erhöhen Trace Score", NamedTextColor.GRAY),
            Component.text("• Kompromittierte Ziele geben weniger", NamedTextColor.GRAY),
            Component.empty(),
            Component.text("Minigame-Typen:", NamedTextColor.YELLOW),
            Component.text("• Server/DB: Password Cracking", NamedTextColor.GRAY),
            Component.text("• Kamera/Alarm: Firewall Bypass", NamedTextColor.GRAY),
            Component.text("• Türen: Code Breaking", NamedTextColor.GRAY),
            Component.text("• ATM: Network Intrusion", NamedTextColor.GRAY)
        );
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates close button item
     */
    private ItemStack createCloseItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("❌ Schließen", NamedTextColor.RED, TextDecoration.BOLD));
        meta.lore(List.of(
            Component.empty(),
            Component.text("Terminal-Session beenden", NamedTextColor.GRAY)
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Handles GUI clicks
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        // Check if this is our GUI
        if (!isOurGUI(event.getView().title())) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        // Handle different click actions
        if (clicked.getType() == Material.BARRIER) {
            // Close button
            plugin.getHackingManager().endSession(player);
            return;
        }
        
        // Check if it's a target item
        ItemMeta meta = clicked.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(
            new org.bukkit.NamespacedKey(plugin, "target_id"), 
            org.bukkit.persistence.PersistentDataType.INTEGER)) {
            
            Integer targetId = meta.getPersistentDataContainer().get(
                new org.bukkit.NamespacedKey(plugin, "target_id"), 
                org.bukkit.persistence.PersistentDataType.INTEGER);
            
            if (targetId != null) {
                // Find the target and start hacking
                HackTarget target = session.getAvailableTargets().stream()
                    .filter(t -> t.getId() == targetId)
                    .findFirst()
                    .orElse(null);
                
                if (target != null) {
                    plugin.getHackingManager().startMinigame(player, target);
                }
            }
        }
    }
    
    /**
     * Checks if the inventory belongs to our hacking GUI
     */
    private boolean isOurGUI(Component title) {
        if (title == null) return false;
        String titleText = ((net.kyori.adventure.text.TextComponent) title).content();
        return titleText.contains("Terminal");
    }
}