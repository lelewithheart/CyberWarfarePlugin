package de.cyberwarfare.minigames;

import de.cyberwarfare.CyberWarfarePlugin;

/**
 * Manages all active minigame sessions
 */
public class MinigameManager {
    
    private final CyberWarfarePlugin plugin;
    
    public MinigameManager(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("Minigame Manager initialized");
    }
    
    public void shutdown() {
        plugin.getLogger().info("Minigame Manager shutdown");
    }
}