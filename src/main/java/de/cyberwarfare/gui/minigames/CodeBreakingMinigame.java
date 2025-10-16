package de.cyberwarfare.gui.minigames;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.managers.HackingManager.HackingSession;
import de.cyberwarfare.models.HackTarget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/**
 * Code breaking minigame - simplified for doors
 */
public class CodeBreakingMinigame extends PasswordCrackingMinigame {
    
    public CodeBreakingMinigame(CyberWarfarePlugin plugin, Player player, HackTarget target, HackingSession session) {
        super(plugin, player, target, session);
    }
    
    @Override
    protected void onGameStart() {
        player.sendMessage(Component.text("🔐 Code Breaking gestartet!", NamedTextColor.AQUA));
        player.sendMessage(Component.text("Knacke den Zugangscode!", NamedTextColor.YELLOW));
    }
}