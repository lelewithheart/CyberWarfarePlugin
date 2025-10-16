package de.cyberwarfare.gui;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.items.IPGrabber;
import de.cyberwarfare.managers.HackingManager;
import de.cyberwarfare.managers.TargetManager;
import de.cyberwarfare.managers.TerminalManager;
import de.cyberwarfare.models.Terminal;
import de.cyberwarfare.models.HackTarget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * GUI für normale Terminals mit IP-basiertem Hacking
 */
public class TerminalGUI {
    
    private final CyberWarfarePlugin plugin;
    private final TerminalManager terminalManager;
    private final TargetManager targetManager;
    private final HackingManager hackingManager;
    private final IPGrabber ipGrabber;
    
    public TerminalGUI(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
        this.terminalManager = plugin.getTerminalManager();
        this.targetManager = plugin.getTargetManager();
        this.hackingManager = plugin.getHackingManager();
        this.ipGrabber = new IPGrabber(plugin);
    }
    
    /**
     * Öffnet die Terminal-GUI für einen Spieler
     */
    public void openTerminalGUI(Player player, Terminal terminal) {
        String terminalId = terminal != null ? String.valueOf(terminal.getId()) : "Unknown";
        
        Inventory gui = Bukkit.createInventory(null, 54, 
            Component.text("Terminal #" + terminalId)
                .color(NamedTextColor.DARK_GREEN));
        
        // Terminal Info
        gui.setItem(4, createTerminalInfoItem(terminal));
        
        // IP-Verbindung
        gui.setItem(20, createIPConnectionItem());
        
        // Eigene Targets
        gui.setItem(22, createOwnedTargetsItem(terminal));
        
        // Scanner
        gui.setItem(24, createScannerItem());
        
        // Filler Items
        fillBorder(gui);
        
        player.openInventory(gui);
    }
    
    /**
     * Öffnet die IP-Eingabe GUI
     */
    public void openIPInputGUI(Player player, Terminal terminal) {
        Inventory gui = Bukkit.createInventory(null, 27,
            Component.text("IP-Adresse eingeben")
                .color(NamedTextColor.AQUA));
        
        // IP-Grabber aus Inventar holen und IPs anzeigen
        ItemStack ipGrabberItem = findIPGrabberInInventory(player);
        if (ipGrabberItem != null) {
            List<String> storedIPs = ipGrabber.getStoredIPs(ipGrabberItem);
            
            int slot = 10;
            for (String ip : storedIPs) {
                if (slot >= 17) break; // Max 7 IPs pro Reihe
                
                ItemStack ipItem = new ItemStack(Material.PAPER);
                ItemMeta meta = ipItem.getItemMeta();
                meta.displayName(Component.text(ip)
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
                
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Klicken zum Verbinden")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
                
                ipItem.setItemMeta(meta);
                gui.setItem(slot++, ipItem);
            }
        }
        
        // Zurück Button
        gui.setItem(22, createBackButton());
        
        fillBorder(gui);
        player.openInventory(gui);
    }
    
    /**
     * Öffnet die Liste der eigenen Targets
     */
    public void openOwnedTargetsGUI(Player player, Terminal terminal) {
        Inventory gui = Bukkit.createInventory(null, 54,
            Component.text("Eigene Targets - Terminal #" + terminal.getId())
                .color(NamedTextColor.GREEN));
        
        // Lade eigene Targets
        CompletableFuture.supplyAsync(() -> {
            return targetManager.getTargetsOwnedByTerminal(terminal.getId());
        }).thenAccept(targets -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                int slot = 9;
                for (HackTarget target : targets) {
                    if (slot >= 45) break;
                    
                    ItemStack targetItem = createTargetItem(target);
                    gui.setItem(slot++, targetItem);
                }
                
                // Zurück Button
                gui.setItem(49, createBackButton());
                fillBorder(gui);
                
                // GUI aktualisieren falls noch offen
                if (player.getOpenInventory().getTopInventory().equals(gui)) {
                    player.updateInventory();
                }
            });
        });
        
        // Lade-Item anzeigen
        gui.setItem(22, createLoadingItem());
        gui.setItem(49, createBackButton());
        fillBorder(gui);
        
        player.openInventory(gui);
    }
    
    /**
     * Behandelt Klicks in der Terminal GUI
     */
    public void handleClick(InventoryClickEvent event, Terminal terminal) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
            .serialize(event.getView().title());
        
        if (title.contains("Terminal #")) {
            handleMainTerminalClick(player, clicked, terminal);
        } else if (title.contains("IP-Adresse eingeben")) {
            handleIPInputClick(player, clicked, terminal);
        } else if (title.contains("Eigene Targets")) {
            handleOwnedTargetsClick(player, clicked, terminal);
        }
    }
    
    private void handleMainTerminalClick(Player player, ItemStack clicked, Terminal terminal) {
        Material type = clicked.getType();
        
        switch (type) {
            case COMPASS -> openIPInputGUI(player, terminal);
            case CHEST -> openOwnedTargetsGUI(player, terminal);
            case OBSERVER -> {
                // Scanner - zeigt alle Geräte in der Nähe (für normale Terminals begrenzt)
                player.sendMessage(Component.text("Scanner wird nicht unterstützt - verwende Mobile Terminal")
                    .color(NamedTextColor.RED));
                player.closeInventory();
            }
            default -> {
                // Ignore other materials
            }
        }
    }
    
    private void handleIPInputClick(Player player, ItemStack clicked, Terminal terminal) {
        if (clicked.getType() == Material.PAPER) {
            // IP ausgewählt - versuche zu verbinden
            Component displayName = clicked.getItemMeta().displayName();
            if (displayName != null) {
                String ip = ((net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer) 
                    net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText())
                    .serialize(displayName);
                
                connectToIP(player, terminal, ip);
            }
        } else if (clicked.getType() == Material.ARROW) {
            // Zurück
            openTerminalGUI(player, terminal);
        }
    }
    
    private void handleOwnedTargetsClick(Player player, ItemStack clicked, Terminal terminal) {
        if (clicked.getType() == Material.ARROW) {
            // Zurück
            openTerminalGUI(player, terminal);
        }
        // TODO: Target-spezifische Aktionen hinzufügen
    }
    
    private void connectToIP(Player player, Terminal terminal, String ip) {
        // Finde Target mit dieser IP
        targetManager.findTargetByIP(ip).thenAccept(targetOpt -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (targetOpt.isPresent()) {
                    HackTarget target = targetOpt.get();
                    player.closeInventory();
                    
                    // Starte Hacking direkt mit dem Target
                    hackingManager.startMobileHackingSession(player, target);
                } else {
                    player.sendMessage(Component.text("Verbindung zu " + ip + " fehlgeschlagen!")
                        .color(NamedTextColor.RED));
                }
            });
        });
    }
    
    // Helper Methods
    private ItemStack createTerminalInfoItem(Terminal terminal) {
        ItemStack item = new ItemStack(Material.BEACON);
        ItemMeta meta = item.getItemMeta();
        
        String terminalId = terminal != null ? String.valueOf(terminal.getId()) : "Unknown";
        meta.displayName(Component.text("Terminal #" + terminalId)
            .color(NamedTextColor.DARK_GREEN)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        if (terminal != null) {
            lore.add(Component.text("Security Level: " + terminal.getSecurityLevel())
                .color(NamedTextColor.GRAY));
            lore.add(Component.text("Position: " + terminal.getLocation().getBlockX() + ", " + terminal.getLocation().getBlockY() + ", " + terminal.getLocation().getBlockZ())
                .color(NamedTextColor.GRAY));
        } else {
            lore.add(Component.text("Terminal aktiv")
                .color(NamedTextColor.GREEN));
            lore.add(Component.text("Bereit für Verbindungen")
                .color(NamedTextColor.GRAY));
        }
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createIPConnectionItem() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("IP-Verbindung")
            .color(NamedTextColor.AQUA)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Verbinde dich mit einem Target")
            .color(NamedTextColor.GRAY));
        lore.add(Component.text("über dessen IP-Adresse")
            .color(NamedTextColor.GRAY));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createOwnedTargetsItem(Terminal terminal) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Eigene Targets")
            .color(NamedTextColor.GREEN)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Zeige alle gehackten Targets")
            .color(NamedTextColor.GRAY));
        if (terminal != null) {
            lore.add(Component.text("die diesem Terminal gehören")
                .color(NamedTextColor.GRAY));
        } else {
            lore.add(Component.text("(Feature wird entwickelt)")
                .color(NamedTextColor.YELLOW));
        }
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createScannerItem() {
        ItemStack item = new ItemStack(Material.OBSERVER);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Scanner")
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Nicht verfügbar")
            .color(NamedTextColor.RED));
        lore.add(Component.text("Verwende Mobile Terminal")
            .color(NamedTextColor.GRAY));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createTargetItem(HackTarget target) {
        Material material = target.getTargetType().getMaterial();
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text(target.getTargetType() + " #" + target.getId())
            .color(target.isCompromised() ? NamedTextColor.GREEN : NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Status: " + (target.isCompromised() ? "Gehackt" : "Sicher"))
            .color(target.isCompromised() ? NamedTextColor.GREEN : NamedTextColor.RED));
        lore.add(Component.text("Difficulty: " + target.getDifficulty())
            .color(NamedTextColor.YELLOW));
        lore.add(Component.text("Value: " + target.getValue())
            .color(NamedTextColor.GOLD));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Zurück")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createLoadingItem() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Lade...")
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack findIPGrabberInInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (ipGrabber.isIPGrabber(item)) {
                return item;
            }
        }
        return null;
    }
    
    private void fillBorder(Inventory gui) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.text(" "));
        filler.setItemMeta(meta);
        
        int size = gui.getSize();
        
        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            if (gui.getItem(i) == null) gui.setItem(i, filler);
            if (gui.getItem(size - 9 + i) == null) gui.setItem(size - 9 + i, filler);
        }
        
        // Left and right columns
        for (int row = 1; row < (size / 9) - 1; row++) {
            if (gui.getItem(row * 9) == null) gui.setItem(row * 9, filler);
            if (gui.getItem(row * 9 + 8) == null) gui.setItem(row * 9 + 8, filler);
        }
    }
}