package de.cyberwarfare.commands;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.models.HackerPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main command handler for CyberWarfare
 */
public class CyberWarfareCommand implements CommandExecutor, TabCompleter {
    
    private final CyberWarfarePlugin plugin;
    
    public CyberWarfareCommand(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "help":
                sendHelpMessage(sender);
                return true;
                
            case "reload":
                return handleReload(sender);
                
            case "stats":
                return handleStats(sender, args);
                
            case "terminal":
                return handleTerminal(sender, args);
                
            case "target":
                return handleTarget(sender, args);
                
            case "info":
                return handleInfo(sender);
                
            case "version":
                return handleVersion(sender);
                
            case "mobile":
                return handleMobile(sender, args);
                
            case "ipgrabber":
            case "ip-grabber":
                return handleIPGrabber(sender, args);
                
            default:
                sender.sendMessage(Component.text("Unbekannter Befehl. Nutze ", NamedTextColor.RED)
                    .append(Component.text("/cyber help", NamedTextColor.YELLOW))
                    .append(Component.text(" für Hilfe.", NamedTextColor.RED)));
                return true;
        }
    }
    
    private void sendHelpMessage(CommandSender sender) {
        Component header = Component.text("═══════════════════════════════════════", NamedTextColor.DARK_AQUA);
        Component title = Component.text("CyberWarfare Commands", NamedTextColor.AQUA, TextDecoration.BOLD);
        
        sender.sendMessage(header);
        sender.sendMessage(title);
        sender.sendMessage(header);
        
        // Basis Commands
        sender.sendMessage(Component.text("/cyber help", NamedTextColor.YELLOW)
            .append(Component.text(" - Zeigt diese Hilfe", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/cyber info", NamedTextColor.YELLOW)
            .append(Component.text(" - Plugin Informationen", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/cyber stats", NamedTextColor.YELLOW)
            .append(Component.text(" - Deine Hacker-Statistiken", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/cyber version", NamedTextColor.YELLOW)
            .append(Component.text(" - Plugin Version", NamedTextColor.GRAY)));
        
        // Player Commands
        if (sender.hasPermission("cyberwarfare.hack")) {
            sender.sendMessage(Component.empty());
            sender.sendMessage(Component.text("Hacker Commands:", NamedTextColor.GREEN));
            sender.sendMessage(Component.text("/cyber stats [player]", NamedTextColor.YELLOW)
                .append(Component.text(" - Statistiken anzeigen", NamedTextColor.GRAY)));
        }
        
        // Mobile Terminal Commands
        if (sender.hasPermission("cyberwarfare.mobile")) {
            sender.sendMessage(Component.empty());
            sender.sendMessage(Component.text("Mobile Terminal:", NamedTextColor.AQUA));
            sender.sendMessage(Component.text("/cyber mobile give", NamedTextColor.YELLOW)
                .append(Component.text(" - Mobile Terminal erhalten", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/cyber mobile open", NamedTextColor.YELLOW)
                .append(Component.text(" - Mobile Terminal GUI öffnen", NamedTextColor.GRAY)));
        }
        
        // IP-Grabber Commands
        if (sender.hasPermission("cyberwarfare.ipgrabber")) {
            sender.sendMessage(Component.empty());
            sender.sendMessage(Component.text("IP-Grabber:", NamedTextColor.GOLD));
            sender.sendMessage(Component.text("/cyber ipgrabber give", NamedTextColor.YELLOW)
                .append(Component.text(" - IP-Grabber Item erhalten", NamedTextColor.GRAY)));
        }
        
        // Equipment Commands
        if (sender.hasPermission("cyberwarfare.mobile")) {
            sender.sendMessage(Component.empty());
            sender.sendMessage(Component.text("Equipment Commands:", NamedTextColor.AQUA));
            sender.sendMessage(Component.text("/cyber mobile give", NamedTextColor.YELLOW)
                .append(Component.text(" - Mobile Terminal erhalten", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/cyber mobile open", NamedTextColor.YELLOW)
                .append(Component.text(" - Mobile Terminal öffnen", NamedTextColor.GRAY)));
        }
        
        if (sender.hasPermission("cyberwarfare.ipgrabber")) {
            if (!sender.hasPermission("cyberwarfare.mobile")) {
                sender.sendMessage(Component.empty());
                sender.sendMessage(Component.text("Equipment Commands:", NamedTextColor.AQUA));
            }
            sender.sendMessage(Component.text("/cyber ipgrabber give", NamedTextColor.YELLOW)
                .append(Component.text(" - IP-Grabber erhalten", NamedTextColor.GRAY)));
        }
        
        // Admin Commands
        if (sender.hasPermission("cyberwarfare.admin")) {
            sender.sendMessage(Component.empty());
            sender.sendMessage(Component.text("Admin Commands:", NamedTextColor.RED));
            sender.sendMessage(Component.text("/cyber reload", NamedTextColor.YELLOW)
                .append(Component.text(" - Konfiguration neu laden", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/cyber terminal create", NamedTextColor.YELLOW)
                .append(Component.text(" - Terminal erstellen", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/cyber terminal remove", NamedTextColor.YELLOW)
                .append(Component.text(" - Terminal entfernen", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/cyber target create <type>", NamedTextColor.YELLOW)
                .append(Component.text(" - Ziel erstellen", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/cyber target remove", NamedTextColor.YELLOW)
                .append(Component.text(" - Ziel entfernen", NamedTextColor.GRAY)));
        }
        
        sender.sendMessage(header);
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("cyberwarfare.admin")) {
            sender.sendMessage(Component.text("Keine Berechtigung!", NamedTextColor.RED));
            return true;
        }
        
        try {
            plugin.getConfigManager().reload();
            sender.sendMessage(Component.text("Konfiguration erfolgreich neu geladen!", NamedTextColor.GREEN));
        } catch (Exception e) {
            sender.sendMessage(Component.text("Fehler beim Neuladen der Konfiguration!", NamedTextColor.RED));
            plugin.getLogger().severe("Config reload error: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleStats(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern verwendet werden!", NamedTextColor.RED));
            return true;
        }
        
        // Eigene Stats oder andere Spieler (mit Permission)
        final String targetName;
        if (args.length > 1) {
            if (!sender.hasPermission("cyberwarfare.admin")) {
                sender.sendMessage(Component.text("Keine Berechtigung um andere Spieler-Stats zu sehen!", NamedTextColor.RED));
                return true;
            }
            targetName = args[1];
        } else {
            targetName = player.getName();
        }
        
        // Stats asynchron laden
        Player targetPlayer = plugin.getServer().getPlayer(targetName);
        if (targetPlayer == null && args.length > 1) {
            sender.sendMessage(Component.text("Spieler nicht gefunden!", NamedTextColor.RED));
            return true;
        }
        
        plugin.getDatabaseManager().getHackerPlayer(targetPlayer != null ? targetPlayer.getUniqueId() : player.getUniqueId())
            .thenAccept(hackerPlayer -> showPlayerStats(sender, targetName, hackerPlayer));
        
        return true;
    }
    
    private void showPlayerStats(CommandSender sender, String playerName, HackerPlayer hackerPlayer) {
        Component header = Component.text("═══════════════════════════════════════", NamedTextColor.DARK_GREEN);
        Component title = Component.text("Hacker Profil: " + playerName, NamedTextColor.GREEN, TextDecoration.BOLD);
        
        sender.sendMessage(header);
        sender.sendMessage(title);
        sender.sendMessage(header);
        
        // Basis Stats
        sender.sendMessage(Component.text("Skill Level: ", NamedTextColor.YELLOW)
            .append(Component.text(hackerPlayer.getSkillLevel(), NamedTextColor.WHITE))
            .append(Component.text(" (", NamedTextColor.GRAY))
            .append(Component.text(hackerPlayer.getSkillRank(), NamedTextColor.AQUA))
            .append(Component.text(")", NamedTextColor.GRAY)));
        
        sender.sendMessage(Component.text("Erfolgreiche Hacks: ", NamedTextColor.YELLOW)
            .append(Component.text(hackerPlayer.getSuccessfulHacks(), NamedTextColor.GREEN)));
        
        sender.sendMessage(Component.text("Fehlgeschlagene Hacks: ", NamedTextColor.YELLOW)
            .append(Component.text(hackerPlayer.getFailedHacks(), NamedTextColor.RED)));
        
        sender.sendMessage(Component.text("Gesamt Hacks: ", NamedTextColor.YELLOW)
            .append(Component.text(hackerPlayer.getTotalHacks(), NamedTextColor.WHITE)));
        
        if (hackerPlayer.getTotalHacks() > 0) {
            double successRate = hackerPlayer.getSuccessRate() * 100;
            NamedTextColor rateColor = successRate >= 70 ? NamedTextColor.GREEN : 
                                     successRate >= 40 ? NamedTextColor.YELLOW : NamedTextColor.RED;
            
            sender.sendMessage(Component.text("Erfolgsrate: ", NamedTextColor.YELLOW)
                .append(Component.text(String.format("%.1f%%", successRate), rateColor)));
        }
        
        sender.sendMessage(Component.text("Trace Punkte: ", NamedTextColor.YELLOW)
            .append(Component.text(hackerPlayer.getTotalTraceScore(), 
                hackerPlayer.getTotalTraceScore() > 50 ? NamedTextColor.RED : NamedTextColor.GREEN)));
        
        sender.sendMessage(header);
    }
    
    private boolean handleTerminal(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cyberwarfare.admin")) {
            sender.sendMessage(Component.text("Keine Berechtigung!", NamedTextColor.RED));
            return true;
        }
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern verwendet werden!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("Verwendung: /cyber terminal <create|remove>", NamedTextColor.YELLOW));
            return true;
        }
        
        switch (args[1].toLowerCase()) {
            case "create":
                int securityLevel = 1;
                if (args.length > 2) {
                    try {
                        securityLevel = Integer.parseInt(args[2]);
                        securityLevel = Math.max(1, Math.min(10, securityLevel));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Component.text("Ungültiger Security Level! Verwende 1-10", NamedTextColor.RED));
                        return true;
                    }
                }
                
                final int finalSecurityLevel = securityLevel;
                plugin.getTerminalManager().createTerminal(player.getLocation(), player, finalSecurityLevel)
                    .thenAccept(success -> {
                        if (success) {
                            sender.sendMessage(Component.text("✅ Terminal erfolgreich erstellt! (Security Level: " + finalSecurityLevel + ")", NamedTextColor.GREEN));
                        } else {
                            sender.sendMessage(Component.text("❌ Terminal konnte nicht erstellt werden! (Bereits vorhanden?)", NamedTextColor.RED));
                        }
                    });
                break;
                
            case "remove":
                plugin.getTerminalManager().removeTerminal(player.getLocation())
                    .thenAccept(success -> {
                        if (success) {
                            sender.sendMessage(Component.text("✅ Terminal erfolgreich entfernt!", NamedTextColor.GREEN));
                        } else {
                            sender.sendMessage(Component.text("❌ Kein Terminal an dieser Position gefunden!", NamedTextColor.RED));
                        }
                    });
                break;
                
            case "give":
                // Give mobile terminal
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Verwendung: /cyber terminal give <securityLevel>", NamedTextColor.YELLOW));
                    return true;
                }
                
                try {
                    int mobileSecurityLevel = Integer.parseInt(args[2]);
                    mobileSecurityLevel = Math.max(1, Math.min(10, mobileSecurityLevel));
                    
                    final int finalMobileLevel = mobileSecurityLevel;
                    plugin.getMobileTerminalManager().createMobileTerminal(player, "Mobile Terminal", finalMobileLevel)
                        .thenAccept(mobileItem -> {
                            if (mobileItem != null) {
                                player.getInventory().addItem(mobileItem);
                                sender.sendMessage(Component.text("✅ Mobile Terminal erhalten! (Security Level: " + finalMobileLevel + ")", NamedTextColor.GREEN));
                            } else {
                                sender.sendMessage(Component.text("❌ Mobile Terminal konnte nicht erstellt werden!", NamedTextColor.RED));
                            }
                        });
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Ungültiger Security Level! Verwende 1-10", NamedTextColor.RED));
                }
                break;
                
            default:
                sender.sendMessage(Component.text("Verwendung: /cyber terminal <create|remove|give> [args]", NamedTextColor.YELLOW));
        }
        
        return true;
    }
    
    private boolean handleTarget(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cyberwarfare.admin")) {
            sender.sendMessage(Component.text("Keine Berechtigung!", NamedTextColor.RED));
            return true;
        }
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern verwendet werden!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("Verwendung: /cyber target <create|remove> [type]", NamedTextColor.YELLOW));
            return true;
        }
        
        switch (args[1].toLowerCase()) {
            case "create":
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Verfügbare Typen: SERVER, CAMERA, ALARM, DOOR, ATM, DATABASE", NamedTextColor.YELLOW));
                    return true;
                }
                
                try {
                    de.cyberwarfare.managers.TargetManager.TargetType targetType = 
                        de.cyberwarfare.managers.TargetManager.TargetType.valueOf(args[2].toUpperCase());
                    
                    int difficulty = 1;
                    if (args.length > 3) {
                        try {
                            difficulty = Integer.parseInt(args[3]);
                            difficulty = Math.max(1, Math.min(10, difficulty));
                        } catch (NumberFormatException e) {
                            // Use default difficulty
                        }
                    }
                    
                    final int finalDifficulty = difficulty;
                    plugin.getTargetManager().createTarget(player.getLocation(), targetType, player, finalDifficulty)
                        .thenAccept(success -> {
                            if (success) {
                                sender.sendMessage(Component.text("✅ " + targetType.getDisplayName() + " erfolgreich erstellt! (Schwierigkeit: " + finalDifficulty + ")", NamedTextColor.GREEN));
                            } else {
                                sender.sendMessage(Component.text("❌ Ziel konnte nicht erstellt werden! (Bereits vorhanden?)", NamedTextColor.RED));
                            }
                        });
                        
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(Component.text("❌ Unbekannter Zieltyp! Verfügbare Typen: SERVER, CAMERA, ALARM, DOOR, ATM, DATABASE", NamedTextColor.RED));
                }
                break;
                
            case "remove":
                plugin.getTargetManager().removeTarget(player.getLocation())
                    .thenAccept(success -> {
                        if (success) {
                            sender.sendMessage(Component.text("✅ Ziel erfolgreich entfernt!", NamedTextColor.GREEN));
                        } else {
                            sender.sendMessage(Component.text("❌ Kein Ziel an dieser Position gefunden!", NamedTextColor.RED));
                        }
                    });
                break;
                
            default:
                sender.sendMessage(Component.text("Verwendung: /cyber target <create|remove> [type] [difficulty]", NamedTextColor.YELLOW));
        }
        
        return true;
    }
    
    private boolean handleInfo(CommandSender sender) {
        Component header = Component.text("═══════════════════════════════════════", NamedTextColor.DARK_BLUE);
        Component title = Component.text("CyberWarfare Plugin Info", NamedTextColor.BLUE, TextDecoration.BOLD);
        
        sender.sendMessage(header);
        sender.sendMessage(title);
        sender.sendMessage(header);
        
        sender.sendMessage(Component.text("Version: ", NamedTextColor.YELLOW)
            .append(Component.text("1.0.0-SNAPSHOT", NamedTextColor.WHITE)));
        
        sender.sendMessage(Component.text("Author: ", NamedTextColor.YELLOW)
            .append(Component.text("CyberWarfare Team", NamedTextColor.WHITE)));
        
        sender.sendMessage(Component.text("Description: ", NamedTextColor.YELLOW)
            .append(Component.text("Advanced Minecraft Hacking System", NamedTextColor.WHITE)));
        
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Features:", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("• ", NamedTextColor.GRAY)
            .append(Component.text("Interaktive Hacking Terminals", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("• ", NamedTextColor.GRAY)
            .append(Component.text("Minigame System", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("• ", NamedTextColor.GRAY)
            .append(Component.text("Spieler Progression", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("• ", NamedTextColor.GRAY)
            .append(Component.text("Database Integration", NamedTextColor.WHITE)));
        
        sender.sendMessage(header);
        return true;
    }
    
    private boolean handleVersion(CommandSender sender) {
        sender.sendMessage(Component.text("CyberWarfare Plugin ", NamedTextColor.AQUA)
            .append(Component.text("v1.0.0-SNAPSHOT", NamedTextColor.YELLOW)));
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Haupt-Subcommands
            completions.addAll(Arrays.asList("help", "info", "stats", "version"));
            
            if (sender.hasPermission("cyberwarfare.admin")) {
                completions.addAll(Arrays.asList("reload", "terminal", "target"));
            }
            
            if (sender.hasPermission("cyberwarfare.mobile")) {
                completions.add("mobile");
            }
            
            if (sender.hasPermission("cyberwarfare.ipgrabber")) {
                completions.addAll(Arrays.asList("ipgrabber", "ip-grabber"));
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "terminal":
                    if (sender.hasPermission("cyberwarfare.admin")) {
                        completions.addAll(Arrays.asList("create", "remove", "give"));
                    }
                    break;
                case "target":
                    if (sender.hasPermission("cyberwarfare.admin")) {
                        completions.addAll(Arrays.asList("create", "remove"));
                    }
                    break;
                case "mobile":
                    if (sender.hasPermission("cyberwarfare.mobile")) {
                        completions.addAll(Arrays.asList("give", "open"));
                    }
                    break;
                case "ipgrabber":
                case "ip-grabber":
                    if (sender.hasPermission("cyberwarfare.ipgrabber")) {
                        completions.add("give");
                    }
                    break;
                case "stats":
                    if (sender.hasPermission("cyberwarfare.admin")) {
                        // Online-Spieler für Stats
                        plugin.getServer().getOnlinePlayers().forEach(p -> 
                            completions.add(p.getName()));
                    }
                    break;
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("target") && 
                   args[1].equalsIgnoreCase("create")) {
            if (sender.hasPermission("cyberwarfare.admin")) {
                completions.addAll(Arrays.asList("SERVER", "CAMERA", "ALARM", "DOOR", "ATM", "DATABASE"));
            }
        }
        
        // Filter results based on what the player has typed
        String partial = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(partial));
        
        return completions;
    }
    
    /**
     * Handles mobile terminal commands
     */
    private boolean handleMobile(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern ausgeführt werden!", NamedTextColor.RED));
            return true;
        }
        
        if (!sender.hasPermission("cyberwarfare.mobile")) {
            sender.sendMessage(Component.text("Du hast keine Berechtigung für diesen Befehl!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage(Component.text("Verwendung: /cyber mobile <give|open>", NamedTextColor.YELLOW));
            return true;
        }
        
        switch (args[1].toLowerCase()) {
            case "give":
                plugin.getMobileTerminalManager().giveMobileTerminal(player).thenAccept(success -> {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        if (success) {
                            player.sendMessage(Component.text("📱 Mobile Terminal erhalten!")
                                .color(NamedTextColor.GREEN));
                        } else {
                            player.sendMessage(Component.text("❌ Fehler beim Erstellen des Mobile Terminals!")
                                .color(NamedTextColor.RED));
                        }
                    });
                });
                break;
                
            case "open":
                new de.cyberwarfare.gui.MobileTerminalGUI(plugin).openMobileTerminalGUI(player);
                break;
                
            default:
                player.sendMessage(Component.text("Verwendung: /cyber mobile <give|open>", NamedTextColor.YELLOW));
        }
        
        return true;
    }
    
    /**
     * Handles IP-Grabber commands  
     */
    private boolean handleIPGrabber(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern ausgeführt werden!", NamedTextColor.RED));
            return true;
        }
        
        if (!sender.hasPermission("cyberwarfare.ipgrabber")) {
            sender.sendMessage(Component.text("Du hast keine Berechtigung für diesen Befehl!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage(Component.text("Verwendung: /cyber ipgrabber <give>", NamedTextColor.YELLOW));
            return true;
        }
        
        switch (args[1].toLowerCase()) {
            case "give":
                de.cyberwarfare.items.IPGrabber ipGrabber = new de.cyberwarfare.items.IPGrabber(plugin);
                var item = ipGrabber.createIPGrabber();
                
                if (player.getInventory().addItem(item).isEmpty()) {
                    player.sendMessage(Component.text("📡 IP-Grabber erhalten!")
                        .color(NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("❌ Inventar voll!")
                        .color(NamedTextColor.RED));
                }
                break;
                
            default:
                player.sendMessage(Component.text("Verwendung: /cyber ipgrabber <give>", NamedTextColor.YELLOW));
        }
        
        return true;
    }
}