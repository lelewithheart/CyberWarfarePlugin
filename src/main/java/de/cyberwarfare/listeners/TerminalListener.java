package de.cyberwarfare.listeners;

import de.cyberwarfare.CyberWarfarePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles terminal-related events
 */
public class TerminalListener implements Listener {
    
    private final CyberWarfarePlugin plugin;
    
    public TerminalListener(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Handle terminal interactions
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Prevent breaking terminal blocks
    }
}