package de.cyberwarfare.listeners;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.models.Terminal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles interactions with terminals and mobile terminals
 */
public class TerminalInteractionListener implements Listener {
    
    private final CyberWarfarePlugin plugin;
    
    public TerminalInteractionListener(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        
        // Handle right-click interactions
        if (action == Action.RIGHT_CLICK_BLOCK) {
            handleBlockInteraction(event, player);
        } else if (action == Action.RIGHT_CLICK_AIR) {
            handleItemInteraction(event, player);
        }
    }
    
    /**
     * Handles interaction with blocks (stationary terminals)
     */
    private void handleBlockInteraction(PlayerInteractEvent event, Player player) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        // Check if it's a terminal block
        if (block.getType() == Material.OBSERVER) { // Using OBSERVER as terminal block
            plugin.getTerminalManager().getTerminalAt(block.getLocation()).thenAccept(terminal -> {
                if (terminal != null) {
                    event.setCancelled(true);
                    handleTerminalInteraction(player, terminal);
                }
            });
        }
    }
    
    /**
     * Handles interaction with items (mobile terminals)
     */
    private void handleItemInteraction(PlayerInteractEvent event, Player player) {
        ItemStack item = event.getItem();
        if (item == null) return;
        
        // Check if it's a mobile terminal
        if (plugin.getMobileTerminalManager().isMobileTerminal(item)) {
            event.setCancelled(true);
            
            boolean isShiftClick = player.isSneaking();
            plugin.getMobileTerminalManager().handleMobileTerminalUse(player, item, isShiftClick);
        }
    }
    
    /**
     * Handles terminal interaction logic
     */
    private void handleTerminalInteraction(Player player, Terminal terminal) {
        // Check permissions
        if (!player.hasPermission("cyberwarfare.hack")) {
            player.sendMessage(Component.text("Du hast keine Berechtigung um Terminals zu nutzen!", NamedTextColor.RED));
            return;
        }
        
        // Check if player already has an active session
        if (plugin.getHackingManager().hasActiveSession(player)) {
            player.sendMessage(Component.text("Du hackst bereits etwas anderes!", NamedTextColor.RED));
            return;
        }
        
        // Start hacking session
        plugin.getHackingManager().startHackingSession(player, terminal);
    }
}