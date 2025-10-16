package de.cyberwarfare;

import de.cyberwarfare.config.ConfigManager;
import de.cyberwarfare.database.DatabaseManager;
import de.cyberwarfare.terminals.TerminalManager;
import de.cyberwarfare.minigames.MinigameManager;
import de.cyberwarfare.targets.TargetManager;
import de.cyberwarfare.commands.CyberWarfareCommand;
import de.cyberwarfare.listeners.PlayerListener;
import de.cyberwarfare.listeners.TerminalListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main plugin class for CyberWarfare
 * Coordinates all subsystems and manages plugin lifecycle
 */
public class CyberWarfarePlugin extends JavaPlugin {
    
    private static CyberWarfarePlugin instance;
    private static final Logger logger = LoggerFactory.getLogger(CyberWarfarePlugin.class);
    
    // Core managers
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private TerminalManager terminalManager;
    private MinigameManager minigameManager;
    private TargetManager targetManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        logger.info("Starting CyberWarfare Plugin v{}", getDescription().getVersion());
        
        try {
            // Initialize core systems in order
            initializeManagers();
            registerCommands();
            registerListeners();
            
            logger.info("CyberWarfare Plugin enabled successfully!");
            
        } catch (Exception e) {
            logger.error("Failed to enable CyberWarfare Plugin", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        logger.info("Shutting down CyberWarfare Plugin...");
        
        try {
            // Shutdown systems in reverse order
            if (minigameManager != null) {
                minigameManager.shutdown();
            }
            
            if (terminalManager != null) {
                terminalManager.shutdown();
            }
            
            if (databaseManager != null) {
                databaseManager.shutdown();
            }
            
            logger.info("CyberWarfare Plugin disabled successfully!");
            
        } catch (Exception e) {
            logger.error("Error during plugin shutdown", e);
        }
        
        instance = null;
    }
    
    private void initializeManagers() {
        logger.info("Initializing core managers...");
        
        // Configuration first
        this.configManager = new ConfigManager(this);
        
        // Database second
        this.databaseManager = new DatabaseManager(this);
        
        // Game systems third
        this.terminalManager = new TerminalManager(this);
        this.minigameManager = new MinigameManager(this);
        this.targetManager = new TargetManager(this);
        
        logger.info("All managers initialized successfully");
    }
    
    private void registerCommands() {
        logger.debug("Registering commands...");
        
        CyberWarfareCommand commandHandler = new CyberWarfareCommand(this);
        getCommand("cyberwarfare").setExecutor(commandHandler);
        getCommand("cyberwarfare").setTabCompleter(commandHandler);
    }
    
    private void registerListeners() {
        logger.debug("Registering event listeners...");
        
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new TerminalListener(this), this);
    }
    
    // Static access
    public static CyberWarfarePlugin getInstance() {
        return instance;
    }
    
    // Manager getters
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public TerminalManager getTerminalManager() {
        return terminalManager;
    }
    
    public MinigameManager getMinigameManager() {
        return minigameManager;
    }
    
    public TargetManager getTargetManager() {
        return targetManager;
    }
}