package de.cyberwarfare.listeners;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.gui.MobileTerminalGUI;
import de.cyberwarfare.gui.TerminalGUI;
import de.cyberwarfare.items.IPGrabber;
import de.cyberwarfare.managers.MobileTerminalManager;
import de.cyberwarfare.managers.TargetManager;
import de.cyberwarfare.managers.TerminalManager;
import de.cyberwarfare.models.HackTarget;
import de.cyberwarfare.models.Terminal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles GUI interactions and IP-Grabber usage
 */
public class GUIInteractionListener implements Listener {
    
    private final CyberWarfarePlugin plugin;
    private final TerminalManager terminalManager;
    private final TargetManager targetManager;
    private final MobileTerminalManager mobileTerminalManager;
    private final IPGrabber ipGrabber;
    private final TerminalGUI terminalGUI;
    private final MobileTerminalGUI mobileTerminalGUI;
    
    public GUIInteractionListener(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
        this.terminalManager = plugin.getTerminalManager();
        this.targetManager = plugin.getTargetManager();
        this.mobileTerminalManager = plugin.getMobileTerminalManager();
        this.ipGrabber = new IPGrabber(plugin);
        this.terminalGUI = new TerminalGUI(plugin);
        this.mobileTerminalGUI = new MobileTerminalGUI(plugin);
    }
    
    /**
     * Handles right-click interactions with terminals, targets and mobile terminals
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        
        var player = event.getPlayer();
        var block = event.getClickedBlock();
        var itemInHand = player.getInventory().getItemInMainHand();
        
        // Check if player is using IP-Grabber
        if (ipGrabber.isIPGrabber(itemInHand)) {
            handleIPGrabberUsage(event, player, block, itemInHand);
            return;
        }
        
        // Check if block is a terminal
        if (terminalManager.isTerminalBlock(block)) {
            event.setCancelled(true);
            
            terminalManager.getTerminalAt(block.getLocation()).thenAccept(terminal -> {
                if (terminal != null) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        terminalGUI.openTerminalGUI(player, terminal);
                    });
                }
            });
            return;
        }
        
        // Check if block is a target
        if (targetManager.isTargetBlock(block.getLocation())) {
            event.setCancelled(true);
            
            targetManager.getTargetAt(block.getLocation()).thenAccept(target -> {
                if (target != null) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        player.sendMessage(Component.text("Verwende ein Terminal oder Mobile Terminal zum Hacken!")
                            .color(NamedTextColor.YELLOW));
                    });
                }
            });
            return;
        }
    }
    
    /**
     * Handles mobile terminal item usage
     */
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        var player = event.getPlayer();
        var newItem = player.getInventory().getItem(event.getNewSlot());
        
        if (newItem != null && mobileTerminalManager.isMobileTerminal(newItem)) {
            // Player selected mobile terminal - could show info or auto-open GUI
            player.sendMessage(Component.text("📱 Mobile Terminal ausgewählt - Rechtsklick zum Öffnen")
                .color(NamedTextColor.AQUA));
        }
    }
    
    /**
     * Handles right-click with mobile terminal
     */
    @EventHandler
    public void onMobileTerminalUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        var player = event.getPlayer();
        var itemInHand = player.getInventory().getItemInMainHand();
        
        if (mobileTerminalManager.isMobileTerminal(itemInHand)) {
            event.setCancelled(true);
            mobileTerminalGUI.openMobileTerminalGUI(player);
        }
    }
    
    /**
     * Handles GUI click events
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        var title = event.getView().title();
        var titleText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
            .serialize(title);
        
        // Terminal GUI clicks
        if (titleText.contains("Terminal #") || titleText.contains("IP-Adresse eingeben") || titleText.contains("Eigene Targets")) {
            // Für Terminal GUIs verwenden wir einen anderen Ansatz
            // Da wir das Terminal nicht einfach finden können, erstellen wir ein Dummy-Terminal
            if (event.getWhoClicked() instanceof org.bukkit.entity.Player bukkitPlayer) {
                event.setCancelled(true);
                
                // Vereinfachter Ansatz: Behandle Terminal-GUI Klicks direkt
                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || clicked.getType() == org.bukkit.Material.AIR) return;
                
                handleSimpleTerminalClick(bukkitPlayer, clicked, titleText);
            }
            return;
        }
        
        // Mobile Terminal GUI clicks
        if (titleText.contains("Mobile Terminal") || titleText.contains("Geräte-Scanner") || 
            titleText.contains("Hackbare Geräte")) {
            mobileTerminalGUI.handleClick(event);
            return;
        }
    }
    
    /**
     * Handles IP-Grabber usage on terminals and targets
     */
    private void handleIPGrabberUsage(PlayerInteractEvent event, org.bukkit.entity.Player player, 
                                     org.bukkit.block.Block block, ItemStack ipGrabberItem) {
        event.setCancelled(true);
        
        // Check if it's a terminal
        if (terminalManager.isTerminalBlock(block)) {
            terminalManager.getTerminalAt(block.getLocation()).thenAccept(terminal -> {
                if (terminal != null) {
                    String ip = ipGrabber.generateIPForTerminal(terminal);
                    
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        ipGrabber.addIP(ipGrabberItem, ip);
                        player.sendMessage(Component.text("📡 IP-Adresse gesammelt: " + ip)
                            .color(NamedTextColor.GREEN));
                    });
                }
            });
            return;
        }
        
        // Check if it's a target
        if (targetManager.isTargetBlock(block.getLocation())) {
            targetManager.getTargetAt(block.getLocation()).thenAccept(target -> {
                if (target != null) {
                    String ip = ipGrabber.generateIPForTarget(target);
                    
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        ipGrabber.addIP(ipGrabberItem, ip);
                        player.sendMessage(Component.text("📡 IP-Adresse gesammelt: " + ip)
                            .color(NamedTextColor.GREEN));
                    });
                }
            });
            return;
        }
        
        // Not a hackable device
        player.sendMessage(Component.text("❌ Kein hackbares Gerät!")
            .color(NamedTextColor.RED));
    }
    
    /**
     * Simplified terminal GUI click handler
     */
    private void handleSimpleTerminalClick(org.bukkit.entity.Player player, ItemStack clicked, String title) {
        org.bukkit.Material type = clicked.getType();
        
        if (title.contains("Terminal #")) {
            // Main terminal GUI
            switch (type) {
                case COMPASS -> {
                    // IP-Verbindung - öffne IP-Eingabe GUI
                    terminalGUI.openIPInputGUI(player, null); // null terminal für vereinfachten Ansatz
                }
                case CHEST -> {
                    // Eigene Targets - zeige Info
                    player.sendMessage(Component.text("Owned Targets Feature - Implementierung folgt")
                        .color(NamedTextColor.YELLOW));
                }
                case OBSERVER -> {
                    // Scanner nicht verfügbar
                    player.sendMessage(Component.text("Scanner wird nicht unterstützt - verwende Mobile Terminal")
                        .color(NamedTextColor.RED));
                    player.closeInventory();
                }
            }
        } else if (title.contains("IP-Adresse eingeben")) {
            // IP-Eingabe GUI
            if (type == org.bukkit.Material.PAPER) {
                // IP ausgewählt - starte Hacking
                var itemMeta = clicked.getItemMeta();
                if (itemMeta != null && itemMeta.displayName() != null) {
                    String ip = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                        .serialize(itemMeta.displayName());
                    
                    connectToIPSimple(player, ip);
                }
            } else if (type == org.bukkit.Material.ARROW) {
                // Zurück - öffne Haupt-Terminal GUI
                terminalGUI.openTerminalGUI(player, null); // null terminal für vereinfachten Ansatz
            }
        } else if (title.contains("Eigene Targets")) {
            // Owned Targets GUI
            if (type == org.bukkit.Material.ARROW) {
                // Zurück
                terminalGUI.openTerminalGUI(player, null);
            }
        }
    }
    
    /**
     * Simplified IP connection
     */
    private void connectToIPSimple(org.bukkit.entity.Player player, String ip) {
        targetManager.findTargetByIP(ip).thenAccept(targetOpt -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (targetOpt.isPresent()) {
                    var target = targetOpt.get();
                    player.closeInventory();
                    
                    // Starte Hacking direkt mit dem Target
                    plugin.getHackingManager().startMobileHackingSession(player, target);
                } else {
                    player.sendMessage(Component.text("Verbindung zu " + ip + " fehlgeschlagen!")
                        .color(NamedTextColor.RED));
                }
            });
        });
    }
}