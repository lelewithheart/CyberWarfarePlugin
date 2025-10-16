package de.cyberwarfare.managers;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.models.HackTarget;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages hackable targets in the world
 */
public class TargetManager {
    
    private final CyberWarfarePlugin plugin;
    
    public enum TargetType {
        SERVER("Server", Material.IRON_BLOCK, 3, 200),
        CAMERA("Kamera", Material.OBSERVER, 2, 100),
        ALARM("Alarm", Material.REDSTONE_LAMP, 2, 80),
        DOOR("Tür", Material.IRON_DOOR, 1, 50),
        ATM("Geldautomat", Material.DISPENSER, 4, 500),
        DATABASE("Datenbank", Material.BOOKSHELF, 5, 1000);
        
        private final String displayName;
        private final Material material;
        private final int baseDifficulty;
        private final int baseValue;
        
        TargetType(String displayName, Material material, int baseDifficulty, int baseValue) {
            this.displayName = displayName;
            this.material = material;
            this.baseDifficulty = baseDifficulty;
            this.baseValue = baseValue;
        }
        
        public String getDisplayName() { return displayName; }
        public Material getMaterial() { return material; }
        public int getBaseDifficulty() { return baseDifficulty; }
        public int getBaseValue() { return baseValue; }
    }
    
    public TargetManager(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Creates a new hackable target
     */
    public CompletableFuture<Boolean> createTarget(Location location, TargetType type, Player creator, int difficulty) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                
                // Check if target already exists at this location
                if (getTargetAt(location).join() != null) {
                    return false;
                }
                
                // Calculate value based on difficulty
                int value = type.getBaseValue() * difficulty;
                
                // Save to database first
                String sql = """
                    INSERT INTO hack_targets (world_name, x, y, z, target_type, difficulty, value, created_by)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, location.getWorld().getName());
                    stmt.setInt(2, location.getBlockX());
                    stmt.setInt(3, location.getBlockY());
                    stmt.setInt(4, location.getBlockZ());
                    stmt.setString(5, type.name());
                    stmt.setInt(6, difficulty);
                    stmt.setInt(7, value);
                    stmt.setString(8, creator.getUniqueId().toString());
                    
                    boolean success = stmt.executeUpdate() > 0;
                    
                    if (success) {
                        // Place block on main thread
                        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                            location.getBlock().setType(type.getMaterial());
                        });
                    }
                    
                    return success;
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error creating target: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Removes a target at the given location
     */
    public CompletableFuture<Boolean> removeTarget(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                
                String sql = """
                    DELETE FROM hack_targets 
                    WHERE world_name = ? AND x = ? AND y = ? AND z = ?
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, location.getWorld().getName());
                    stmt.setInt(2, location.getBlockX());
                    stmt.setInt(3, location.getBlockY());
                    stmt.setInt(4, location.getBlockZ());
                    
                    boolean removed = stmt.executeUpdate() > 0;
                    
                    if (removed) {
                        // Remove the block on main thread
                        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                            location.getBlock().setType(Material.AIR);
                        });
                    }
                    
                    return removed;
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error removing target: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Gets a target at the specified location
     */
    public CompletableFuture<HackTarget> getTargetAt(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                
                String sql = """
                    SELECT id, target_type, difficulty, is_compromised, value, created_by, created_at, last_hacked
                    FROM hack_targets 
                    WHERE world_name = ? AND x = ? AND y = ? AND z = ?
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, location.getWorld().getName());
                    stmt.setInt(2, location.getBlockX());
                    stmt.setInt(3, location.getBlockY());
                    stmt.setInt(4, location.getBlockZ());
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return new HackTarget(
                                rs.getInt("id"),
                                location,
                                TargetType.valueOf(rs.getString("target_type")),
                                rs.getInt("difficulty"),
                                rs.getBoolean("is_compromised"),
                                rs.getInt("value"),
                                rs.getString("created_by"),
                                rs.getTimestamp("created_at"),
                                rs.getTimestamp("last_hacked")
                            );
                        }
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting target: " + e.getMessage());
            }
            
            return null;
        });
    }
    
    /**
     * Gets all targets
     */
    public CompletableFuture<List<HackTarget>> getAllTargets() {
        return CompletableFuture.supplyAsync(() -> {
            List<HackTarget> targets = new ArrayList<>();
            
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                
                String sql = """
                    SELECT id, world_name, x, y, z, target_type, difficulty, is_compromised, 
                           value, created_by, created_at, last_hacked
                    FROM hack_targets 
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
                            
                            targets.add(new HackTarget(
                                rs.getInt("id"),
                                location,
                                TargetType.valueOf(rs.getString("target_type")),
                                rs.getInt("difficulty"),
                                rs.getBoolean("is_compromised"),
                                rs.getInt("value"),
                                rs.getString("created_by"),
                                rs.getTimestamp("created_at"),
                                rs.getTimestamp("last_hacked")
                            ));
                        }
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting targets: " + e.getMessage());
            }
            
            return targets;
        });
    }
    
    /**
     * Marks a target as compromised
     */
    public CompletableFuture<Boolean> compromiseTarget(HackTarget target) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                
                String sql = """
                    UPDATE hack_targets 
                    SET is_compromised = TRUE, last_hacked = CURRENT_TIMESTAMP
                    WHERE id = ?
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, target.getId());
                    return stmt.executeUpdate() > 0;
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error compromising target: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Checks if a block is a hackable target
     */
    public boolean isTargetBlock(Location location) {
        HackTarget target = getTargetAt(location).join();
        return target != null;
    }
    
    /**
     * Gets all targets in radius around a location
     */
    public List<HackTarget> getTargetsInRadius(Location center, double radius) {
        return CompletableFuture.supplyAsync(() -> {
            List<HackTarget> targets = new ArrayList<>();
            
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String sql = "SELECT * FROM hack_targets WHERE world_name = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, center.getWorld().getName());
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            HackTarget target = createTargetFromResultSet(rs);
                            
                            // Check distance
                            if (target.getLocation().distance(center) <= radius) {
                                targets.add(target);
                            }
                        }
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting targets in radius: " + e.getMessage());
            }
            
            return targets;
        }).join();
    }
    
    /**
     * Gets only hackable (not compromised) targets in radius
     */
    public List<HackTarget> getHackableTargetsInRadius(Location center, double radius) {
        return getTargetsInRadius(center, radius).stream()
            .filter(target -> !target.isCompromised())
            .toList();
    }
    
    /**
     * Gets a target by ID
     */
    public CompletableFuture<java.util.Optional<HackTarget>> getTargetById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String sql = "SELECT * FROM hack_targets WHERE id = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, id);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return java.util.Optional.of(createTargetFromResultSet(rs));
                        }
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting target by ID: " + e.getMessage());
            }
            
            return java.util.Optional.empty();
        });
    }
    
    /**
     * Gets all targets owned by a specific terminal
     */
    public List<HackTarget> getTargetsOwnedByTerminal(int terminalId) {
        return CompletableFuture.supplyAsync(() -> {
            List<HackTarget> targets = new ArrayList<>();
            
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String sql = "SELECT * FROM hack_targets WHERE owned_by_terminal = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, terminalId);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            targets.add(createTargetFromResultSet(rs));
                        }
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting targets owned by terminal: " + e.getMessage());
            }
            
            return targets;
        }).join();
    }
    
    /**
     * Finds a target by IP address
     */
    public CompletableFuture<java.util.Optional<HackTarget>> findTargetByIP(String ipAddress) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String sql = "SELECT * FROM hack_targets WHERE ip_address = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, ipAddress);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return java.util.Optional.of(createTargetFromResultSet(rs));
                        }
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error finding target by IP: " + e.getMessage());
            }
            
            return java.util.Optional.empty();
        });
    }
    
    /**
     * Helper method to create HackTarget from ResultSet
     */
    private HackTarget createTargetFromResultSet(ResultSet rs) throws SQLException {
        Location location = new Location(
            plugin.getServer().getWorld(rs.getString("world_name")),
            rs.getInt("x"),
            rs.getInt("y"),
            rs.getInt("z")
        );
        
        return new HackTarget(
            rs.getInt("id"),
            location,
            TargetType.valueOf(rs.getString("target_type")),
            rs.getInt("difficulty"),
            rs.getBoolean("is_compromised"),
            rs.getInt("value"),
            rs.getString("created_by"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("last_hacked")
        );
    }
}