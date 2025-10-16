package de.cyberwarfare.managers;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.models.MobileTerminal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages mobile terminals (tablet items)
 */
public class MobileTerminalManager {
    
    private final CyberWarfarePlugin plugin;
    private final NamespacedKey terminalIdKey;
    private final NamespacedKey batteryKey;
    
    public MobileTerminalManager(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
        this.terminalIdKey = new NamespacedKey(plugin, "terminal_id");
        this.batteryKey = new NamespacedKey(plugin, "battery_level");
    }
    
    /**
     * Creates a new mobile terminal for a player
     */
    public CompletableFuture<ItemStack> createMobileTerminal(Player player, String terminalName, int securityLevel) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                
                // Insert into database
                String sql = """
                    INSERT INTO mobile_terminals (owner_uuid, terminal_name, security_level, battery_level)
                    VALUES (?, ?, ?, ?)
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, player.getUniqueId().toString());
                    stmt.setString(2, terminalName);
                    stmt.setInt(3, securityLevel);
                    stmt.setInt(4, 100); // Full battery
                    
                    int affected = stmt.executeUpdate();
                    if (affected == 0) {
                        return null;
                    }
                    
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            int terminalId = keys.getInt(1);
                            return createMobileTerminalItem(terminalId, terminalName, securityLevel, 100);
                        }
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error creating mobile terminal: " + e.getMessage());
            }
            
            return null;
        });
    }
    
    /**
     * Creates the physical item stack for a mobile terminal
     */
    private ItemStack createMobileTerminalItem(int terminalId, String terminalName, int securityLevel, int batteryLevel) {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name with formatting
        Component displayName = Component.text("🖥 ", NamedTextColor.AQUA)
            .append(Component.text(terminalName, NamedTextColor.WHITE, TextDecoration.BOLD));
        meta.displayName(displayName);
        
        // Set lore with terminal info
        List<Component> lore = List.of(
            Component.empty(),
            Component.text("Security Level: ", NamedTextColor.GRAY)
                .append(Component.text(securityLevel, NamedTextColor.YELLOW)),
            Component.text("Battery: ", NamedTextColor.GRAY)
                .append(getBatteryComponent(batteryLevel)),
            Component.empty(),
            Component.text("Rechtsklick zum Verwenden", NamedTextColor.GREEN, TextDecoration.ITALIC),
            Component.text("Shift+Rechtsklick für Einstellungen", NamedTextColor.GRAY, TextDecoration.ITALIC),
            Component.empty(),
            Component.text("Mobile Hacking Terminal", NamedTextColor.DARK_AQUA)
        );
        meta.lore(lore);
        
        // Store terminal data in NBT
        meta.getPersistentDataContainer().set(terminalIdKey, PersistentDataType.INTEGER, terminalId);
        meta.getPersistentDataContainer().set(batteryKey, PersistentDataType.INTEGER, batteryLevel);
        
        // Set custom model data for resource pack compatibility
        meta.setCustomModelData(1001);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Gets battery level component with color coding
     */
    private Component getBatteryComponent(int batteryLevel) {
        NamedTextColor color;
        String icon;
        
        if (batteryLevel > 75) {
            color = NamedTextColor.GREEN;
            icon = "🔋";
        } else if (batteryLevel > 50) {
            color = NamedTextColor.YELLOW;
            icon = "🔋";
        } else if (batteryLevel > 25) {
            color = NamedTextColor.GOLD;
            icon = "🪫";
        } else {
            color = NamedTextColor.RED;
            icon = "🪫";
        }
        
        return Component.text(icon + " " + batteryLevel + "%", color);
    }
    
    /**
     * Checks if an item is a mobile terminal
     */
    public boolean isMobileTerminal(ItemStack item) {
        if (item == null || item.getType() != Material.RECOVERY_COMPASS) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        return meta.getPersistentDataContainer().has(terminalIdKey, PersistentDataType.INTEGER);
    }
    
    /**
     * Gets mobile terminal data from item
     */
    public CompletableFuture<MobileTerminal> getMobileTerminalFromItem(ItemStack item) {
        if (!isMobileTerminal(item)) {
            return CompletableFuture.completedFuture(null);
        }
        
        ItemMeta meta = item.getItemMeta();
        Integer terminalId = meta.getPersistentDataContainer().get(terminalIdKey, PersistentDataType.INTEGER);
        
        if (terminalId == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        return getMobileTerminalById(terminalId);
    }
    
    /**
     * Gets mobile terminal by ID from database
     */
    public CompletableFuture<MobileTerminal> getMobileTerminalById(int terminalId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                
                String sql = """
                    SELECT owner_uuid, terminal_name, security_level, battery_level, is_active, created_at
                    FROM mobile_terminals 
                    WHERE id = ?
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, terminalId);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return new MobileTerminal(
                                terminalId,
                                UUID.fromString(rs.getString("owner_uuid")),
                                rs.getString("terminal_name"),
                                rs.getInt("security_level"),
                                rs.getInt("battery_level"),
                                rs.getBoolean("is_active"),
                                rs.getTimestamp("created_at")
                            );
                        }
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting mobile terminal: " + e.getMessage());
            }
            
            return null;
        });
    }
    
    /**
     * Updates battery level of a mobile terminal
     */
    public CompletableFuture<Boolean> updateBatteryLevel(int terminalId, int newBatteryLevel) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                
                String sql = """
                    UPDATE mobile_terminals 
                    SET battery_level = ?
                    WHERE id = ?
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, Math.max(0, Math.min(100, newBatteryLevel)));
                    stmt.setInt(2, terminalId);
                    
                    return stmt.executeUpdate() > 0;
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error updating battery level: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Updates the item's battery display
     */
    public ItemStack updateItemBattery(ItemStack item, int newBatteryLevel) {
        if (!isMobileTerminal(item)) {
            return item;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        
        // Update battery in NBT
        meta.getPersistentDataContainer().set(batteryKey, PersistentDataType.INTEGER, newBatteryLevel);
        
        // Update lore
        List<Component> currentLore = meta.lore();
        if (currentLore != null && currentLore.size() > 2) {
            List<Component> newLore = new java.util.ArrayList<>(currentLore);
            // Replace battery line (index 2)
            newLore.set(2, Component.text("Battery: ", NamedTextColor.GRAY)
                .append(getBatteryComponent(newBatteryLevel)));
            meta.lore(newLore);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Handles mobile terminal usage
     */
    public void handleMobileTerminalUse(Player player, ItemStack item, boolean isShiftClick) {
        getMobileTerminalFromItem(item).thenAccept(mobileTerminal -> {
            if (mobileTerminal == null) {
                player.sendMessage(Component.text("Ungültiges Mobile Terminal!", NamedTextColor.RED));
                return;
            }
            
            // Check battery
            if (mobileTerminal.getBatteryLevel() <= 0) {
                player.sendMessage(Component.text("🪫 Terminal Akku leer!", NamedTextColor.RED));
                return;
            }
            
            // Check permissions
            if (!player.hasPermission("cyberwarfare.mobile")) {
                player.sendMessage(Component.text("Du hast keine Berechtigung Mobile Terminals zu nutzen!", NamedTextColor.RED));
                return;
            }
            
            if (isShiftClick) {
                // Open terminal settings
                openMobileTerminalSettings(player, mobileTerminal);
            } else {
                // Open hacking interface
                plugin.getHackingManager().startMobileHackingSession(player, mobileTerminal);
            }
        });
    }
    
    /**
     * Opens mobile terminal settings GUI
     */
    private void openMobileTerminalSettings(Player player, MobileTerminal terminal) {
        // TODO: Implement settings GUI
        player.sendMessage(Component.text("Mobile Terminal Einstellungen (coming soon)", NamedTextColor.YELLOW));
    }
}