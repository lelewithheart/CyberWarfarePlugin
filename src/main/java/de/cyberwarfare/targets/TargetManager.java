package de.cyberwarfare.targets;

import de.cyberwarfare.CyberWarfarePlugin;

/**
 * Manages hackable targets and their effects
 */
public class TargetManager {
    
    private final CyberWarfarePlugin plugin;
    
    public TargetManager(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("Target Manager initialized");
    }
    
    public void shutdown() {
        plugin.getLogger().info("Target Manager shutdown");
    }
}