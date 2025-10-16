package de.cyberwarfare.terminals;

import de.cyberwarfare.CyberWarfarePlugin;

/**
 * Manages hacking terminals in the world
 */
public class TerminalManager {
    
    private final CyberWarfarePlugin plugin;
    
    public TerminalManager(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("Terminal Manager initialized");
    }
    
    public void shutdown() {
        plugin.getLogger().info("Terminal Manager shutdown");
    }
}