package de.cyberwarfare.config;

import de.cyberwarfare.CyberWarfarePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages configuration files and localization
 */
public class ConfigManager {
    
    private final CyberWarfarePlugin plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private final Map<String, Component> messageCache = new HashMap<>();
    
    public ConfigManager(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }
    
    private void loadConfigs() {
        // Save default config if it doesn't exist
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        // Load messages
        loadMessages();
    }
    
    private void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        // Create messages.yml if it doesn't exist
        if (!messagesFile.exists()) {
            try (InputStream is = plugin.getResource("messages.yml")) {
                if (is != null) {
                    Files.copy(is, messagesFile.toPath());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Could not create messages.yml: " + e.getMessage());
            }
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        messageCache.clear(); // Clear cache when reloading
    }
    
    /**
     * Get a message component with MiniMessage formatting
     */
    public Component getMessage(String key, Object... args) {
        return messageCache.computeIfAbsent(key + String.join("", java.util.Arrays.toString(args)), 
            k -> {
                String rawMessage = messages.getString(key, "Missing message: " + key);
                
                // Replace placeholders
                if (args.length > 0) {
                    rawMessage = String.format(rawMessage, args);
                }
                
                return MiniMessage.miniMessage().deserialize(rawMessage);
            });
    }
    
    /**
     * Reload all configuration files
     */
    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadMessages();
    }
    
    /**
     * Alias for reloadConfigs()
     */
    public void reload() {
        reloadConfigs();
    }
    
    // Database configuration
    public boolean isDatabaseEnabled() {
        return config.getBoolean("database.enabled", true);
    }
    
    public String getDatabaseType() {
        return config.getString("database.type", "sqlite").toLowerCase();
    }
    
    public String getDatabaseHost() {
        return config.getString("database.host", "localhost");
    }
    
    public int getDatabasePort() {
        return config.getInt("database.port", 3306);
    }
    
    public String getDatabaseName() {
        return config.getString("database.database", "cyberwarfare");
    }
    
    public String getDatabaseUsername() {
        return config.getString("database.username", "root");
    }
    
    public String getDatabasePassword() {
        return config.getString("database.password", "");
    }
    
    // Terminal configuration
    public int getTerminalCooldown() {
        return config.getInt("terminals.cooldown", 30);
    }
    
    public int getMaxTerminals() {
        return config.getInt("terminals.max-per-chunk", 3);
    }
    
    // Minigame configuration
    public int getMinigameTimeout() {
        return config.getInt("minigames.timeout", 60);
    }
    
    public int getMinigameDifficulty() {
        return config.getInt("minigames.difficulty", 5);
    }
    
    // Target configuration
    public int getTargetCooldown() {
        return config.getInt("targets.cooldown", 300);
    }
    
    public double getHackSuccessRate() {
        return config.getDouble("targets.base-success-rate", 0.6);
    }
}