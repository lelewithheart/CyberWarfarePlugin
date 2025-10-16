package de.cyberwarfare.managers;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.models.HackTarget;
import de.cyberwarfare.models.MobileTerminal;
import de.cyberwarfare.models.Terminal;
import de.cyberwarfare.gui.HackingGUI;
import de.cyberwarfare.gui.minigames.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Manages hacking sessions and minigames
 */
public class HackingManager {
    
    private final CyberWarfarePlugin plugin;
    private final Map<UUID, HackingSession> activeSessions;
    
    public HackingManager(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
        this.activeSessions = new HashMap<>();
    }
    
    /**
     * Starts a hacking session with a stationary terminal
     */
    public void startHackingSession(Player player, Terminal terminal) {
        if (hasActiveSession(player)) {
            player.sendMessage(Component.text("Du hackst bereits etwas anderes!", NamedTextColor.RED));
            return;
        }
        
        // Find nearby targets
        findNearbyTargets(player, terminal.getLocation()).thenAccept(targets -> {
            if (targets.isEmpty()) {
                player.sendMessage(Component.text("Keine hackbaren Ziele in der Nähe!", NamedTextColor.YELLOW));
                return;
            }
            
            // Create hacking session
            HackingSession session = new HackingSession(player, terminal, targets);
            activeSessions.put(player.getUniqueId(), session);
            
            // Open target selection GUI
            new HackingGUI(plugin, session).openTargetSelection();
        });
    }
    
    /**
     * Starts a direct hacking session with a specific target (mobile)
     */
    public void startMobileHackingSession(Player player, HackTarget target) {
        if (hasActiveSession(player)) {
            player.sendMessage(Component.text("Du hackst bereits etwas anderes!", NamedTextColor.RED));
            return;
        }
        
        // Create direct hacking session with one target using a dummy mobile terminal
        List<HackTarget> targets = new ArrayList<>();
        targets.add(target);
        
        // Create temporary mobile terminal for this session
        MobileTerminal dummyMobile = new MobileTerminal(0, player.getUniqueId(), "Mobile Scanner", 1, 100, true, null);
        
        HackingSession session = new HackingSession(player, dummyMobile, targets);
        activeSessions.put(player.getUniqueId(), session);
        
        // Start minigame directly
        startMinigame(player, target);
    }
    
    /**
     * Starts a mobile hacking session
     */
    public void startMobileHackingSession(Player player, MobileTerminal mobileTerminal) {
        if (hasActiveSession(player)) {
            player.sendMessage(Component.text("Du hackst bereits etwas anderes!", NamedTextColor.RED));
            return;
        }
        
        // Check battery
        if (!mobileTerminal.canPerformHack()) {
            player.sendMessage(Component.text("🪫 Nicht genug Akku für Hacking!", NamedTextColor.RED));
            return;
        }
        
        // Find nearby targets
        findNearbyTargets(player, player.getLocation()).thenAccept(targets -> {
            if (targets.isEmpty()) {
                player.sendMessage(Component.text("Keine hackbaren Ziele in der Nähe!", NamedTextColor.YELLOW));
                return;
            }
            
            // Create mobile hacking session
            HackingSession session = new HackingSession(player, mobileTerminal, targets);
            activeSessions.put(player.getUniqueId(), session);
            
            // Open target selection GUI
            new HackingGUI(plugin, session).openTargetSelection();
        });
    }
    
    /**
     * Finds hackable targets near a location
     */
    private CompletableFuture<java.util.List<HackTarget>> findNearbyTargets(Player player, org.bukkit.Location location) {
        return plugin.getTargetManager().getAllTargets().thenApply(allTargets -> {
            return allTargets.stream()
                .filter(target -> target.getLocation().getWorld().equals(location.getWorld()))
                .filter(target -> target.getLocation().distance(location) <= 10.0) // 10 block radius
                .filter(target -> target.canBeHacked())
                .collect(java.util.stream.Collectors.toList());
        });
    }
    
    /**
     * Starts a specific minigame for a target
     */
    public void startMinigame(Player player, HackTarget target) {
        HackingSession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            player.sendMessage(Component.text("Keine aktive Hacking-Session!", NamedTextColor.RED));
            return;
        }
        
        // Select appropriate minigame based on target type
        BaseMinigame minigame = selectMinigame(target, session);
        
        if (minigame == null) {
            player.sendMessage(Component.text("Unbekannter Zieltyp!", NamedTextColor.RED));
            return;
        }
        
        // Consume battery for mobile terminals
        if (session.isMobileSession()) {
            session.getMobileTerminal().consumeBatteryForHack();
            // Update item battery display
            updateMobileTerminalBattery(player, session.getMobileTerminal());
        }
        
        // Start the minigame
        minigame.start();
    }
    
    /**
     * Selects appropriate minigame based on target type
     */
    private BaseMinigame selectMinigame(HackTarget target, HackingSession session) {
        Player player = session.getPlayer();
        
        switch (target.getTargetType()) {
            case SERVER:
            case DATABASE:
                return new PasswordCrackingMinigame(plugin, player, target, session);
            
            case CAMERA:
            case ALARM:
                return new FirewallBypassMinigame(plugin, player, target, session);
            
            case DOOR:
                return new CodeBreakingMinigame(plugin, player, target, session);
            
            case ATM:
                return new NetworkIntrusionMinigame(plugin, player, target, session);
            
            default:
                return new PasswordCrackingMinigame(plugin, player, target, session);
        }
    }
    
    /**
     * Handles successful hack completion
     */
    public void onHackSuccess(Player player, HackTarget target, int reward) {
        // Update player stats
        plugin.getDatabaseManager().getHackerPlayer(player.getUniqueId()).thenAccept(hackerPlayer -> {
            hackerPlayer.addSuccessfulHack();
            plugin.getDatabaseManager().saveHackerPlayer(hackerPlayer);
        });
        
        // Mark target as compromised
        plugin.getTargetManager().compromiseTarget(target);
        
        // Give rewards
        player.sendMessage(Component.text("✅ Hack erfolgreich! Belohnung: ", NamedTextColor.GREEN)
            .append(Component.text(reward + " Credits", NamedTextColor.GOLD)));
        
        // End session
        endSession(player);
    }
    
    /**
     * Handles failed hack attempt
     */
    public void onHackFailure(Player player, HackTarget target, int tracePenalty) {
        // Update player stats
        plugin.getDatabaseManager().getHackerPlayer(player.getUniqueId()).thenAccept(hackerPlayer -> {
            hackerPlayer.addFailedHack();
            hackerPlayer.addTraceScore(tracePenalty);
            plugin.getDatabaseManager().saveHackerPlayer(hackerPlayer);
        });
        
        player.sendMessage(Component.text("❌ Hack fehlgeschlagen! Trace +", NamedTextColor.RED)
            .append(Component.text(tracePenalty, NamedTextColor.DARK_RED)));
        
        // End session
        endSession(player);
    }
    
    /**
     * Updates mobile terminal battery in player inventory
     */
    private void updateMobileTerminalBattery(Player player, MobileTerminal terminal) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            org.bukkit.inventory.ItemStack item = player.getInventory().getItem(i);
            if (plugin.getMobileTerminalManager().isMobileTerminal(item)) {
                final int finalSlot = i;
                final org.bukkit.inventory.ItemStack finalItem = item;
                plugin.getMobileTerminalManager().getMobileTerminalFromItem(item).thenAccept(itemTerminal -> {
                    if (itemTerminal != null && itemTerminal.getId() == terminal.getId()) {
                        org.bukkit.inventory.ItemStack updatedItem = plugin.getMobileTerminalManager()
                            .updateItemBattery(finalItem, terminal.getBatteryLevel());
                        player.getInventory().setItem(finalSlot, updatedItem);
                    }
                });
                break;
            }
        }
        
        // Update database
        plugin.getMobileTerminalManager().updateBatteryLevel(terminal.getId(), terminal.getBatteryLevel());
    }
    
    /**
     * Ends a hacking session
     */
    public void endSession(Player player) {
        activeSessions.remove(player.getUniqueId());
        player.closeInventory();
    }
    
    /**
     * Checks if player has an active hacking session
     */
    public boolean hasActiveSession(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }
    
    /**
     * Gets active session for player
     */
    public HackingSession getActiveSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }
    
    /**
     * Represents an active hacking session
     */
    public static class HackingSession {
        private final Player player;
        private final Terminal terminal;
        private final MobileTerminal mobileTerminal;
        private final java.util.List<HackTarget> availableTargets;
        
        // Constructor for stationary terminal session
        public HackingSession(Player player, Terminal terminal, java.util.List<HackTarget> availableTargets) {
            this.player = player;
            this.terminal = terminal;
            this.mobileTerminal = null;
            this.availableTargets = availableTargets;
        }
        
        // Constructor for mobile terminal session
        public HackingSession(Player player, MobileTerminal mobileTerminal, java.util.List<HackTarget> availableTargets) {
            this.player = player;
            this.terminal = null;
            this.mobileTerminal = mobileTerminal;
            this.availableTargets = availableTargets;
        }
        
        public Player getPlayer() { return player; }
        public Terminal getTerminal() { return terminal; }
        public MobileTerminal getMobileTerminal() { return mobileTerminal; }
        public java.util.List<HackTarget> getAvailableTargets() { return availableTargets; }
        
        public boolean isMobileSession() { return mobileTerminal != null; }
        public boolean isStationarySession() { return terminal != null; }
        
        public int getSecurityLevel() {
            return isMobileSession() ? mobileTerminal.getSecurityLevel() : terminal.getSecurityLevel();
        }
    }
}