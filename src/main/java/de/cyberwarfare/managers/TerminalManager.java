package de.cyberwarfare.managers;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.models.Terminal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages hacking terminals in the world
 */
public class TerminalManager {
    
    private final CyberWarfarePlugin plugin;
    
    public TerminalManager(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Creates a new terminal at the given location
     */
    public CompletableFuture<Boolean> createTerminal(Location location, Player creator, int securityLevel) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                
                // Check if terminal already exists at this location
                if (getTerminalAt(location).join() != null) {
                    return false;
                }
                
                // Place terminal block
                Block block = location.getBlock();
                block.setType(Material.OBSERVER);
                
                // Save to database
                String sql = """
                    INSERT INTO terminals (world_name, x, y, z, security_level, created_by)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, location.getWorld().getName());
                    stmt.setInt(2, location.getBlockX());
                    stmt.setInt(3, location.getBlockY());
                    stmt.setInt(4, location.getBlockZ());
                    stmt.setInt(5, securityLevel);
                    stmt.setString(6, creator.getUniqueId().toString());
                    
                    return stmt.executeUpdate() > 0;
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error creating terminal: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Removes a terminal at the given location
     */
    public CompletableFuture<Boolean> removeTerminal(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                
                String sql = """
                    DELETE FROM terminals 
                    WHERE world_name = ? AND x = ? AND y = ? AND z = ?
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, location.getWorld().getName());
                    stmt.setInt(2, location.getBlockX());
                    stmt.setInt(3, location.getBlockY());
                    stmt.setInt(4, location.getBlockZ());
                    
                    boolean removed = stmt.executeUpdate() > 0;
                    
                    if (removed) {
                        // Remove the block
                        location.getBlock().setType(Material.AIR);
                    }
                    
                    return removed;
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error removing terminal: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Gets a terminal at the specified location
     */
    public CompletableFuture<Terminal> getTerminalAt(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                
                String sql = """
                    SELECT id, security_level, created_by, created_at
                    FROM terminals 
                    WHERE world_name = ? AND x = ? AND y = ? AND z = ?
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, location.getWorld().getName());
                    stmt.setInt(2, location.getBlockX());
                    stmt.setInt(3, location.getBlockY());
                    stmt.setInt(4, location.getBlockZ());
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return new Terminal(
                                rs.getInt("id"),
                                location,
                                rs.getInt("security_level"),
                                rs.getString("created_by"),
                                rs.getTimestamp("created_at")
                            );
                        }
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting terminal: " + e.getMessage());
            }
            
            return null;
        });
    }
    
    /**
     * Gets all terminals in the world
     */
    public CompletableFuture<List<Terminal>> getAllTerminals() {
        return CompletableFuture.supplyAsync(() -> {
            List<Terminal> terminals = new ArrayList<>();
            
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                
                String sql = """
                    SELECT id, world_name, x, y, z, security_level, created_by, created_at
                    FROM terminals 
                    ORDER BY created_at DESC
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Location location = new Location(
                                plugin.getServer().getWorld(rs.getString("world_name")),
                                rs.getInt("x"),
                                rs.getInt("y"),
                                rs.getInt("z")
                            );
                            
                            terminals.add(new Terminal(
                                rs.getInt("id"),
                                location,
                                rs.getInt("security_level"),
                                rs.getString("created_by"),
                                rs.getTimestamp("created_at")
                            ));
                        }
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting terminals: " + e.getMessage());
            }
            
            return terminals;
        });
    }
    
    /**
     * Handles terminal interaction
     */
    public void handleTerminalInteraction(Player player, Terminal terminal) {
        // Check permissions
        if (!player.hasPermission("cyberwarfare.hack")) {
            player.sendMessage(Component.text("Du hast keine Berechtigung um Terminals zu nutzen!", NamedTextColor.RED));
            return;
        }
        
        // Open hacking interface
        plugin.getHackingManager().startHackingSession(player, terminal);
    }
    
    /**
     * Checks if a block is a terminal
     */
    public boolean isTerminalBlock(Block block) {
        if (block.getType() != Material.OBSERVER) {
            return false;
        }
        
        Terminal terminal = getTerminalAt(block.getLocation()).join();
        return terminal != null;
    }
}