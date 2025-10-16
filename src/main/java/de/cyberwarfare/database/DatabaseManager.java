package de.cyberwarfare.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.models.HackerPlayer;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handles all database operations with connection pooling
 */
public class DatabaseManager {
    
    private final CyberWarfarePlugin plugin;
    private HikariDataSource dataSource;
    
    public DatabaseManager(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        String type = plugin.getConfigManager().getDatabaseType();
        
        HikariConfig config = new HikariConfig();
        
        if (type.equals("mysql")) {
            setupMySQL(config);
        } else {
            setupSQLite(config);
        }
        
        // Common HikariCP settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setLeakDetectionThreshold(30000);
        
        dataSource = new HikariDataSource(config);
        
        // Initialize database schema
        initializeSchema();
    }
    
    private void setupMySQL(HikariConfig config) {
        String host = plugin.getConfigManager().getDatabaseHost();
        int port = plugin.getConfigManager().getDatabasePort();
        String database = plugin.getConfigManager().getDatabaseName();
        String username = plugin.getConfigManager().getDatabaseUsername();
        String password = plugin.getConfigManager().getDatabasePassword();
        
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", host, port, database));
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        // MySQL specific settings
        config.addDataSourceProperty("useSSL", "false");
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("characterEncoding", "utf8");
    }
    
    private void setupSQLite(HikariConfig config) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File dbFile = new File(dataFolder, "cyberwarfare.db");
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        
        // SQLite specific settings
        config.addDataSourceProperty("foreign_keys", "true");
    }
    
    private void initializeSchema() {
        try (Connection conn = dataSource.getConnection()) {
            
            plugin.getLogger().info("Initializing database schema...");
            
            // Create players table
            conn.createStatement().executeUpdate("""
                CREATE TABLE IF NOT EXISTS hacker_players (
                    player_uuid VARCHAR(36) PRIMARY KEY,
                    successful_hacks INTEGER DEFAULT 0,
                    failed_hacks INTEGER DEFAULT 0,
                    total_trace_score INTEGER DEFAULT 0,
                    skill_level INTEGER DEFAULT 1,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            // Create terminals table
            conn.createStatement().executeUpdate("""
                CREATE TABLE IF NOT EXISTS terminals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    world_name VARCHAR(50) NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    security_level INTEGER DEFAULT 1,
                    created_by VARCHAR(36),
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            // Create hack targets table
            conn.createStatement().executeUpdate("""
                CREATE TABLE IF NOT EXISTS hack_targets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    world_name VARCHAR(50) NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    target_type VARCHAR(20) NOT NULL,
                    difficulty INTEGER DEFAULT 1,
                    is_compromised BOOLEAN DEFAULT FALSE,
                    value INTEGER DEFAULT 100,
                    created_by VARCHAR(36),
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    last_hacked DATETIME NULL
                )
            """);
            
            // Create mobile terminals table (without foreign key for now)
            conn.createStatement().executeUpdate("""
                CREATE TABLE IF NOT EXISTS mobile_terminals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner_uuid VARCHAR(36) NOT NULL,
                    terminal_name VARCHAR(50) DEFAULT 'Mobile Terminal',
                    security_level INTEGER DEFAULT 1,
                    battery_level INTEGER DEFAULT 100,
                    is_active BOOLEAN DEFAULT TRUE,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            // Migration: Fix existing tables with wrong column names
            migrateOldTables(conn);
            
            plugin.getLogger().info("Database schema initialized successfully");

            // Create hack sessions table
            conn.createStatement().executeUpdate("""
                CREATE TABLE IF NOT EXISTS hack_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid VARCHAR(36) NOT NULL,
                    terminal_id INTEGER,
                    start_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                    end_time DATETIME,
                    success BOOLEAN DEFAULT FALSE,
                    minigame_type VARCHAR(20)
                )
            """);
            
            // Create hack targets table
            conn.createStatement().executeUpdate("""
                CREATE TABLE IF NOT EXISTS hack_targets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(50) NOT NULL UNIQUE,
                    description TEXT,
                    target_type VARCHAR(20) NOT NULL,
                    difficulty INTEGER DEFAULT 1,
                    cooldown_seconds INTEGER DEFAULT 300,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database schema: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Migrates old tables to new schema
     */
    private void migrateOldTables(Connection conn) throws SQLException {
        plugin.getLogger().info("Checking for database migrations...");
        
        // Check if old terminals table exists with wrong column names
        try {
            conn.createStatement().executeQuery("SELECT world_name FROM terminals LIMIT 1");
            plugin.getLogger().info("Terminals table schema is correct");
        } catch (SQLException e) {
            plugin.getLogger().info("Migrating terminals table...");
            // Drop and recreate terminals table
            try {
                conn.createStatement().executeUpdate("DROP TABLE IF EXISTS terminals");
                conn.createStatement().executeUpdate("""
                    CREATE TABLE terminals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        world_name VARCHAR(50) NOT NULL,
                        x INTEGER NOT NULL,
                        y INTEGER NOT NULL,
                        z INTEGER NOT NULL,
                        security_level INTEGER DEFAULT 1,
                        ip_address VARCHAR(15) UNIQUE,
                        created_by VARCHAR(36),
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                """);
                plugin.getLogger().info("Terminals table migrated successfully");
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to migrate terminals table: " + ex.getMessage());
            }
        }
        
        // Check if old hack_targets table exists with wrong column names  
        try {
            conn.createStatement().executeQuery("SELECT world_name, value, is_compromised FROM hack_targets LIMIT 1");
            plugin.getLogger().info("Hack_targets table schema is correct");
        } catch (SQLException e) {
            plugin.getLogger().info("Migrating hack_targets table...");
            // Drop and recreate hack_targets table
            try {
                conn.createStatement().executeUpdate("DROP TABLE IF EXISTS hack_targets");
                conn.createStatement().executeUpdate("""
                    CREATE TABLE hack_targets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        world_name VARCHAR(50) NOT NULL,
                        x INTEGER NOT NULL,
                        y INTEGER NOT NULL,
                        z INTEGER NOT NULL,
                        target_type VARCHAR(20) NOT NULL,
                        difficulty INTEGER DEFAULT 1,
                        is_compromised BOOLEAN DEFAULT FALSE,
                        value INTEGER DEFAULT 100,
                        ip_address VARCHAR(15) UNIQUE,
                        owned_by_terminal INTEGER NULL,
                        created_by VARCHAR(36),
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        last_hacked DATETIME NULL
                    )
                """);
                plugin.getLogger().info("Hack_targets table migrated successfully");
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to migrate hack_targets table: " + ex.getMessage());
            }
        }
        
        // Check if mobile_terminals table has foreign key issues
        try {
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS mobile_terminals");
            conn.createStatement().executeUpdate("""
                CREATE TABLE mobile_terminals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner_uuid VARCHAR(36) NOT NULL,
                    terminal_name VARCHAR(50) DEFAULT 'Mobile Terminal',
                    security_level INTEGER DEFAULT 1,
                    battery_level INTEGER DEFAULT 100,
                    is_active BOOLEAN DEFAULT TRUE,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);
            plugin.getLogger().info("Mobile_terminals table recreated without foreign keys");
        } catch (SQLException ex) {
            plugin.getLogger().warning("Failed to recreate mobile_terminals table: " + ex.getMessage());
        }
    }
    
    /**
     * Get a HackerPlayer from database or create new one
     */
    public CompletableFuture<HackerPlayer> getHackerPlayer(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM hacker_players WHERE player_uuid = ?";
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, playerId.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return HackerPlayer.fromResultSet(rs);
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error loading hacker player: " + e.getMessage());
            }
            
            // Create new player if not found
            LocalDateTime now = LocalDateTime.now();
            return new HackerPlayer(playerId, 0, 0, 0, 1, now, now);
        });
    }
    
    /**
     * Save or update a HackerPlayer
     */
    public CompletableFuture<Void> saveHackerPlayer(HackerPlayer player) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO hacker_players (player_uuid, successful_hacks, failed_hacks, 
                                          total_trace_score, skill_level, created_at, updated_at) 
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET
                    successful_hacks = excluded.successful_hacks,
                    failed_hacks = excluded.failed_hacks,
                    total_trace_score = excluded.total_trace_score,
                    skill_level = excluded.skill_level,
                    updated_at = excluded.updated_at
                """;
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, player.getPlayerId().toString());
                stmt.setInt(2, player.getSuccessfulHacks());
                stmt.setInt(3, player.getFailedHacks());
                stmt.setInt(4, player.getTotalTraceScore());
                stmt.setInt(5, player.getSkillLevel());
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(player.getCreatedAt()));
                stmt.setTimestamp(7, java.sql.Timestamp.valueOf(player.getUpdatedAt()));
                
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Error saving hacker player: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get database connection for advanced operations
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    /**
     * Shutdown the database connection pool
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed");
        }
    }
}