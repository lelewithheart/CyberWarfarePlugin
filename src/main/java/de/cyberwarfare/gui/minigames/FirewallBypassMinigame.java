package de.cyberwarfare.gui.minigames;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.managers.HackingManager.HackingSession;
import de.cyberwarfare.models.HackTarget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;

/**
 * Firewall bypass minigame - click sequences in the right order
 */
public class FirewallBypassMinigame extends BaseMinigame {
    
    private int[] sequence;
    private int currentStep;
    private final int sequenceLength;
    private final Random random;
    
    public FirewallBypassMinigame(CyberWarfarePlugin plugin, org.bukkit.entity.Player player, HackTarget target, HackingSession session) {
        super(plugin, player, target, session);
        this.random = new Random();
        this.sequenceLength = Math.max(3, Math.min(7, target.getEffectiveDifficulty()));
        this.currentStep = 0;
        generateSequence();
    }
    
    @Override
    protected void createGUI() {
        gui = Bukkit.createInventory(null, 54, Component.text("🛡 Firewall Bypass"));
        
        // Header
        gui.setItem(4, createTimerItem());
        gui.setItem(13, createTargetInfoItem());
        gui.setItem(22, createSequenceDisplayItem());
        
        // Bypass buttons (3x3 grid in center)
        for (int i = 0; i < 9; i++) {
            int slot = 28 + (i % 3) + ((i / 3) * 9);
            gui.setItem(slot, createBypassButton(i));
        }
        
        fillEmptySlots();
    }
    
    private void generateSequence() {
        sequence = new int[sequenceLength];
        for (int i = 0; i < sequenceLength; i++) {
            sequence[i] = random.nextInt(9);
        }
    }
    
    private ItemStack createSequenceDisplayItem() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("🛡 Firewall Sequenz", NamedTextColor.AQUA, TextDecoration.BOLD));
        
        StringBuilder sequenceStr = new StringBuilder();
        for (int i = 0; i < sequenceLength; i++) {
            if (i < currentStep) {
                sequenceStr.append("✓ ");
            } else if (i == currentStep) {
                sequenceStr.append("→ ");
            } else {
                sequenceStr.append("? ");
            }
        }
        
        meta.lore(List.of(
            Component.empty(),
            Component.text("Fortschritt: ", NamedTextColor.GRAY)
                .append(Component.text(sequenceStr.toString(), NamedTextColor.WHITE)),
            Component.text("Schritt: ", NamedTextColor.GRAY)
                .append(Component.text((currentStep + 1) + "/" + sequenceLength, NamedTextColor.YELLOW)),
            Component.empty(),
            Component.text("Klicke die Knöpfe in der richtigen Reihenfolge!", NamedTextColor.GREEN, TextDecoration.ITALIC)
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createBypassButton(int buttonId) {
        ItemStack item = new ItemStack(Material.STONE_BUTTON);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Knopf " + (buttonId + 1), NamedTextColor.WHITE, TextDecoration.BOLD));
        
        // Highlight current target button
        if (currentStep < sequenceLength && sequence[currentStep] == buttonId) {
            item.setType(Material.LIME_CONCRETE);
            meta.displayName(Component.text("→ Knopf " + (buttonId + 1) + " ←", NamedTextColor.GREEN, TextDecoration.BOLD));
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    @Override
    protected void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        
        // Check if it's a bypass button
        if ((slot >= 28 && slot <= 30) || (slot >= 37 && slot <= 39) || (slot >= 46 && slot <= 48)) {
            int buttonId = getButtonIdFromSlot(slot);
            
            if (currentStep < sequenceLength && sequence[currentStep] == buttonId) {
                // Correct button!
                currentStep++;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f + (currentStep * 0.1f));
                
                if (currentStep >= sequenceLength) {
                    // Sequence complete!
                    player.sendMessage(Component.text("✅ Firewall erfolgreich umgangen!", NamedTextColor.GREEN));
                    end(true);
                } else {
                    updateDisplay();
                }
            } else {
                // Wrong button!
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(Component.text("❌ Falsche Sequenz! Neustart...", NamedTextColor.RED));
                currentStep = 0;
                updateDisplay();
            }
        }
    }
    
    private int getButtonIdFromSlot(int slot) {
        if (slot >= 28 && slot <= 30) return slot - 28;
        if (slot >= 37 && slot <= 39) return (slot - 37) + 3;
        if (slot >= 46 && slot <= 48) return (slot - 46) + 6;
        return -1;
    }
    
    private void updateDisplay() {
        gui.setItem(22, createSequenceDisplayItem());
        
        // Update buttons
        for (int i = 0; i < 9; i++) {
            int slot = 28 + (i % 3) + ((i / 3) * 9);
            gui.setItem(slot, createBypassButton(i));
        }
    }
    
    @Override
    protected void onGameStart() {
        player.sendMessage(Component.text("🛡 Firewall Bypass gestartet!", NamedTextColor.AQUA));
        player.sendMessage(Component.text("Folge der Sequenz von " + sequenceLength + " Schritten!", NamedTextColor.YELLOW));
    }
    
    @Override
    protected void onGameEnd(boolean success) {
        if (!success && timeLeft <= 0) {
            player.sendMessage(Component.text("⏰ Firewall-Timeout erreicht!", NamedTextColor.RED));
        }
    }
    
    @Override
    protected void onTimerTick() {
        updateTimerDisplay();
    }
}