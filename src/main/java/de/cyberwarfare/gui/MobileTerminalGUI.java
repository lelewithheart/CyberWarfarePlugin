package de.cyberwarfare.gui;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.managers.HackingManager;
import de.cyberwarfare.managers.TargetManager;
import de.cyberwarfare.managers.TerminalManager;
import de.cyberwarfare.models.HackTarget;
import de.cyberwarfare.models.Terminal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
 * GUI für Mobile Terminals mit Nahbereichs-Scanning
 */
public class MobileTerminalGUI {
    
    private final CyberWarfarePlugin plugin;
    private final TerminalManager terminalManager;
    private final TargetManager targetManager;
    private final HackingManager hackingManager;
    private final int SCAN_RADIUS = 50; // Blocks
    
    public MobileTerminalGUI(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
        this.terminalManager = plugin.getTerminalManager();
        this.targetManager = plugin.getTargetManager();
        this.hackingManager = plugin.getHackingManager();
    }
    
    /**
     * Öffnet die Mobile Terminal GUI
     */
    public void openMobileTerminalGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54,
            Component.text("Mobile Terminal")
                .color(NamedTextColor.DARK_AQUA));
        
        // Scanner Button
        gui.setItem(20, createScannerItem());
        
        // Geräte in der Nähe
        gui.setItem(22, createNearbyDevicesItem());
        
        // Terminal Status
        gui.setItem(24, createTerminalStatusItem(player));
        
        fillBorder(gui);
        player.openInventory(gui);
    }
    
    /**
     * Öffnet die Scanner GUI - zeigt alle Geräte in Reichweite
     */
    public void openScannerGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54,
            Component.text("Geräte-Scanner - " + SCAN_RADIUS + " Blöcke")
                .color(NamedTextColor.YELLOW));
        
        Location playerLoc = player.getLocation();
        
        // Async Suche nach Geräten in der Nähe
        CompletableFuture<List<Object>> nearbyDevicesFuture = CompletableFuture.supplyAsync(() -> {
            List<Object> devices = new ArrayList<>();
            
            // Terminals in der Nähe
            List<Terminal> terminals = terminalManager.getTerminalsInRadius(playerLoc, SCAN_RADIUS);
            devices.addAll(terminals);
            
            // Targets in der Nähe
            List<HackTarget> targets = targetManager.getTargetsInRadius(playerLoc, SCAN_RADIUS);
            devices.addAll(targets);
            
            return devices;
        });
        
        nearbyDevicesFuture.thenAccept(devices -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                int slot = 9;
                
                for (Object device : devices) {
                    if (slot >= 45) break;
                    
                    ItemStack deviceItem;
                    if (device instanceof Terminal terminal) {
                        deviceItem = createTerminalDeviceItem(terminal, playerLoc);
                    } else if (device instanceof HackTarget target) {
                        deviceItem = createTargetDeviceItem(target, playerLoc);
                    } else {
                        continue;
                    }
                    
                    gui.setItem(slot++, deviceItem);
                }
                
                if (devices.isEmpty()) {
                    gui.setItem(22, createNoDevicesItem());
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
     * Öffnet die Geräte-Liste GUI
     */
    public void openNearbyDevicesGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54,
            Component.text("Hackbare Geräte in der Nähe")
                .color(NamedTextColor.GREEN));
        
        Location playerLoc = player.getLocation();
        
        // Nur hackbare Targets laden
        CompletableFuture.supplyAsync(() -> {
            return targetManager.getHackableTargetsInRadius(playerLoc, SCAN_RADIUS);
        }).thenAccept(targets -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                int slot = 9;
                
                for (HackTarget target : targets) {
                    if (slot >= 45) break;
                    
                    ItemStack targetItem = createHackableTargetItem(target, playerLoc);
                    gui.setItem(slot++, targetItem);
                }
                
                if (targets.isEmpty()) {
                    gui.setItem(22, createNoHackableDevicesItem());
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
     * Behandelt Klicks in der Mobile Terminal GUI
     */
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String title = event.getView().getTitle();
        
        if (title.contains("Mobile Terminal")) {
            handleMainMobileClick(player, clicked);
        } else if (title.contains("Geräte-Scanner")) {
            handleScannerClick(player, clicked);
        } else if (title.contains("Hackbare Geräte")) {
            handleNearbyDevicesClick(player, clicked);
        }
    }
    
    private void handleMainMobileClick(Player player, ItemStack clicked) {
        Material type = clicked.getType();
        
        switch (type) {
            case OBSERVER -> openScannerGUI(player);
            case REDSTONE_BLOCK -> openNearbyDevicesGUI(player);
            case CLOCK -> {
                // Terminal Status - zeige Batterie Info
                player.sendMessage(Component.text("Batterie: 85% - Funktional")
                    .color(NamedTextColor.GREEN));
            }
            default -> {
                // Ignore other materials
            }
        }
    }
    
    private void handleScannerClick(Player player, ItemStack clicked) {
        if (clicked.getType() == Material.ARROW) {
            // Zurück
            openMobileTerminalGUI(player);
            return;
        }
        
        // Check if it's a device item with stored data
        ItemMeta meta = clicked.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<Component> lore = meta.lore();
            if (lore != null && !lore.isEmpty()) {
                // Get device info from lore and show details
                player.sendMessage(Component.text("Gerät gescannt - siehe Details")
                    .color(NamedTextColor.YELLOW));
            }
        }
    }
    
    private void handleNearbyDevicesClick(Player player, ItemStack clicked) {
        if (clicked.getType() == Material.ARROW) {
            // Zurück
            openMobileTerminalGUI(player);
            return;
        }
        
        // Check if it's a hackable target
        ItemMeta meta = clicked.getItemMeta();
        if (meta != null && meta.hasLore()) {
            // Extract target ID from item and start hacking
            String displayName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(meta.displayName());
            
            if (displayName.contains("#")) {
                try {
                    String idStr = displayName.substring(displayName.lastIndexOf("#") + 1);
                    int targetId = Integer.parseInt(idStr);
                    
                    // Find target and start hacking
                    CompletableFuture<java.util.Optional<HackTarget>> future = targetManager.getTargetById(targetId);
                    future.thenAccept(targetOpt -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (targetOpt.isPresent()) {
                                HackTarget target = targetOpt.get();
                                player.closeInventory();
                                
                                // Start mobile hacking session
                                hackingManager.startMobileHackingSession(player, target);
                            } else {
                                player.sendMessage(Component.text("Target nicht mehr verfügbar!")
                                    .color(NamedTextColor.RED));
                            }
                        });
                    });
                } catch (NumberFormatException e) {
                    player.sendMessage(Component.text("Fehler beim Laden des Targets")
                        .color(NamedTextColor.RED));
                }
            }
        }
    }
    
    // Helper Methods
    private ItemStack createScannerItem() {
        ItemStack item = new ItemStack(Material.OBSERVER);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Geräte-Scanner")
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Scanne alle Geräte in " + SCAN_RADIUS + " Blöcken")
            .color(NamedTextColor.GRAY));
        lore.add(Component.text("Terminals, Targets und mehr")
            .color(NamedTextColor.GRAY));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createNearbyDevicesItem() {
        ItemStack item = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Hackbare Geräte")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Zeige nur hackbare Targets")
            .color(NamedTextColor.GRAY));
        lore.add(Component.text("in der Nähe")
            .color(NamedTextColor.GRAY));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createTerminalStatusItem(Player player) {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Terminal Status")
            .color(NamedTextColor.GREEN)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Batterie: 85%")
            .color(NamedTextColor.GREEN));
        lore.add(Component.text("Signal: Stark")
            .color(NamedTextColor.GREEN));
        lore.add(Component.text("Status: Online")
            .color(NamedTextColor.GREEN));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createTerminalDeviceItem(Terminal terminal, Location playerLoc) {
        ItemStack item = new ItemStack(Material.BEACON);
        ItemMeta meta = item.getItemMeta();
        
        double distance = terminal.getLocation().distance(playerLoc);
        
        meta.displayName(Component.text("Terminal #" + terminal.getId())
            .color(NamedTextColor.BLUE)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Entfernung: " + String.format("%.1f", distance) + " Blöcke")
            .color(NamedTextColor.GRAY));
        lore.add(Component.text("Security Level: " + terminal.getSecurityLevel())
            .color(NamedTextColor.YELLOW));
        lore.add(Component.text("Typ: Terminal")
            .color(NamedTextColor.AQUA));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createTargetDeviceItem(HackTarget target, Location playerLoc) {
        Material material = target.getTargetType().getMaterial();
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        double distance = target.getLocation().distance(playerLoc);
        
        meta.displayName(Component.text(target.getTargetType() + " #" + target.getId())
            .color(target.isCompromised() ? NamedTextColor.GREEN : NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Entfernung: " + String.format("%.1f", distance) + " Blöcke")
            .color(NamedTextColor.GRAY));
        lore.add(Component.text("Status: " + (target.isCompromised() ? "Gehackt" : "Sicher"))
            .color(target.isCompromised() ? NamedTextColor.GREEN : NamedTextColor.RED));
        lore.add(Component.text("Difficulty: " + target.getDifficulty())
            .color(NamedTextColor.YELLOW));
        lore.add(Component.text("Typ: Target")
            .color(NamedTextColor.GOLD));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createHackableTargetItem(HackTarget target, Location playerLoc) {
        ItemStack item = createTargetDeviceItem(target, playerLoc);
        ItemMeta meta = item.getItemMeta();
        
        List<Component> lore = new ArrayList<>(meta.lore());
        lore.add(Component.empty());
        lore.add(Component.text("→ Klicken zum Hacken")
            .color(NamedTextColor.LIGHT_PURPLE));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createNoDevicesItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Keine Geräte gefunden")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Keine Geräte in " + SCAN_RADIUS + " Blöcken")
            .color(NamedTextColor.GRAY));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createNoHackableDevicesItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Keine hackbaren Geräte")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Keine Targets in Reichweite")
            .color(NamedTextColor.GRAY));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createLoadingItem() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Scanne Geräte...")
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        
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