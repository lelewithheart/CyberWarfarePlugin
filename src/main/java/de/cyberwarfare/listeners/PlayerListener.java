package de.cyberwarfare.listeners;

import de.cyberwarfare.CyberWarfarePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player-related events
 */
public class PlayerListener implements Listener {
    
    private final CyberWarfarePlugin plugin;
    
    public PlayerListener(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Initialize player data
        plugin.getDatabaseManager().getHackerPlayer(event.getPlayer().getUniqueId());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Save player data or cleanup if needed
    }
}