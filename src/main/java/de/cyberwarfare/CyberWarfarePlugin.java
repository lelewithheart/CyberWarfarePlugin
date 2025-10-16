package de.cyberwarfare;

import de.cyberwarfare.config.ConfigManager;
import de.cyberwarfare.database.DatabaseManager;
import de.cyberwarfare.managers.TerminalManager;
import de.cyberwarfare.managers.TargetManager;
import de.cyberwarfare.managers.MobileTerminalManager;
import de.cyberwarfare.managers.HackingManager;
import de.cyberwarfare.commands.CyberWarfareCommand;
import de.cyberwarfare.listeners.TerminalInteractionListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for CyberWarfare
 * Coordinates all subsystems and manages plugin lifecycle
 */
public class CyberWarfarePlugin extends JavaPlugin {
    
    private static CyberWarfarePlugin instance;
    
    // Core managers
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private TerminalManager terminalManager;
    private TargetManager targetManager;
    private MobileTerminalManager mobileTerminalManager;
    private HackingManager hackingManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("Starting CyberWarfare Plugin v" + getPluginMeta().getVersion());
        
        try {
            // Initialize core systems in order
            initializeManagers();
            registerCommands();
            registerListeners();
            
            getLogger().info("CyberWarfare Plugin enabled successfully!");
            
        } catch (Exception e) {
            getLogger().severe("Failed to enable CyberWarfare Plugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Shutting down CyberWarfare Plugin...");
        
        try {
            // Shutdown database connection
            if (databaseManager != null) {
                databaseManager.shutdown();
            }
            
            getLogger().info("CyberWarfare Plugin disabled successfully!");
            
        } catch (Exception e) {
            getLogger().severe("Error during plugin shutdown: " + e.getMessage());
            e.printStackTrace();
        }
        
        instance = null;
    }
    
    /**
     * Initializes all manager classes
     */
    private void initializeManagers() throws Exception {
        getLogger().info("Initializing managers...");
        
        // Core configuration first
        this.configManager = new ConfigManager(this);
        
        // Database connection
        this.databaseManager = new DatabaseManager(this);
        
        // Game systems
        this.terminalManager = new TerminalManager(this);
        this.targetManager = new TargetManager(this);
        this.mobileTerminalManager = new MobileTerminalManager(this);
        this.hackingManager = new HackingManager(this);
        
        getLogger().info("All managers initialized successfully");
    }
    
    /**
     * Registers command handlers
     */
    private void registerCommands() {
        getLogger().info("Registering commands...");
        
        CyberWarfareCommand commandHandler = new CyberWarfareCommand(this);
        getCommand("cyber").setExecutor(commandHandler);
        getCommand("cyber").setTabCompleter(commandHandler);
    }
    
    /**
     * Registers event listeners
     */
    private void registerListeners() {
        getLogger().info("Registering event listeners...");
        
        getServer().getPluginManager().registerEvents(new TerminalInteractionListener(this), this);
        getServer().getPluginManager().registerEvents(new de.cyberwarfare.listeners.GUIInteractionListener(this), this);
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
    
    public TargetManager getTargetManager() {
        return targetManager;
    }
    
    public MobileTerminalManager getMobileTerminalManager() {
        return mobileTerminalManager;
    }
    
    public HackingManager getHackingManager() {
        return hackingManager;
    }
}