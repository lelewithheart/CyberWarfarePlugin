package de.cyberwarfare.gui.minigames;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.managers.HackingManager.HackingSession;
import de.cyberwarfare.models.HackTarget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/**
 * Network intrusion minigame - enhanced version for ATMs
 */
public class NetworkIntrusionMinigame extends FirewallBypassMinigame {
    
    public NetworkIntrusionMinigame(CyberWarfarePlugin plugin, Player player, HackTarget target, HackingSession session) {
        super(plugin, player, target, session);
    }
    
    @Override
    protected void onGameStart() {
        player.sendMessage(Component.text("🌐 Network Intrusion gestartet!", NamedTextColor.AQUA));
        player.sendMessage(Component.text("Infiltriere das Netzwerk!", NamedTextColor.YELLOW));
    }
}