package de.cyberwarfare.items;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.models.Terminal;
import de.cyberwarfare.models.HackTarget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * IP-Grabber Item zum Sammeln von IP-Adressen von Terminals und Targets
 */
public class IPGrabber {
    
    private final CyberWarfarePlugin plugin;
    private final NamespacedKey ipListKey;
    private final NamespacedKey itemTypeKey;
    
    public IPGrabber(CyberWarfarePlugin plugin) {
        this.plugin = plugin;
        this.ipListKey = new NamespacedKey(plugin, "ip_list");
        this.itemTypeKey = new NamespacedKey(plugin, "item_type");
    }
    
    /**
     * Erstellt einen neuen IP-Grabber
     */
    public ItemStack createIPGrabber() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        
        // Name und Lore
        meta.displayName(Component.text("IP-Grabber")
            .color(NamedTextColor.AQUA)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Rechtsklick auf Terminal oder Target")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("um die IP-Adresse zu sammeln")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Gespeicherte IPs: 0")
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        
        // Persistent Data
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, "ip_grabber");
        meta.getPersistentDataContainer().set(ipListKey, PersistentDataType.STRING, "");
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Prüft ob ein Item ein IP-Grabber ist
     */
    public boolean isIPGrabber(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        
        return "ip_grabber".equals(item.getItemMeta()
            .getPersistentDataContainer()
            .get(itemTypeKey, PersistentDataType.STRING));
    }
    
    /**
     * Fügt eine IP-Adresse zum Grabber hinzu
     */
    public void addIP(ItemStack item, String ipAddress) {
        if (!isIPGrabber(item)) return;
        
        ItemMeta meta = item.getItemMeta();
        String currentIPs = meta.getPersistentDataContainer()
            .get(ipListKey, PersistentDataType.STRING);
        
        if (currentIPs == null) currentIPs = "";
        
        // Prüfen ob IP bereits vorhanden
        List<String> ipList = getStoredIPs(item);
        if (ipList.contains(ipAddress)) {
            return; // IP bereits vorhanden
        }
        
        // IP hinzufügen
        if (!currentIPs.isEmpty()) {
            currentIPs += "," + ipAddress;
        } else {
            currentIPs = ipAddress;
        }
        
        meta.getPersistentDataContainer().set(ipListKey, PersistentDataType.STRING, currentIPs);
        
        // Lore aktualisieren
        updateLore(meta, getStoredIPs(currentIPs));
        item.setItemMeta(meta);
    }
    
    /**
     * Holt alle gespeicherten IP-Adressen
     */
    public List<String> getStoredIPs(ItemStack item) {
        if (!isIPGrabber(item)) return new ArrayList<>();
        
        String ipString = item.getItemMeta()
            .getPersistentDataContainer()
            .get(ipListKey, PersistentDataType.STRING);
        
        return getStoredIPs(ipString);
    }
    
    private List<String> getStoredIPs(String ipString) {
        List<String> ips = new ArrayList<>();
        if (ipString != null && !ipString.isEmpty()) {
            String[] parts = ipString.split(",");
            for (String ip : parts) {
                ips.add(ip.trim());
            }
        }
        return ips;
    }
    
    /**
     * Aktualisiert die Lore des Items
     */
    private void updateLore(ItemMeta meta, List<String> ips) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Rechtsklick auf Terminal oder Target")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("um die IP-Adresse zu sammeln")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Gespeicherte IPs: " + ips.size())
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        
        if (!ips.isEmpty()) {
            lore.add(Component.empty());
            lore.add(Component.text("IP-Adressen:")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
            
            for (String ip : ips) {
                lore.add(Component.text("• " + ip)
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            }
        }
        
        meta.lore(lore);
    }
    
    /**
     * Generiert eine IP-Adresse für ein Terminal
     */
    public String generateIPForTerminal(Terminal terminal) {
        // Einfache IP-Generierung basierend auf Terminal-Position
        int x = Math.abs(terminal.getLocation().getBlockX()) % 255;
        int y = Math.abs(terminal.getLocation().getBlockY()) % 255;
        int z = Math.abs(terminal.getLocation().getBlockZ()) % 255;
        int id = Math.abs(terminal.getId()) % 255;
        
        return String.format("192.%d.%d.%d", x, y, (z + id) % 255);
    }
    
    /**
     * Generiert eine IP-Adresse für ein Target
     */
    public String generateIPForTarget(HackTarget target) {
        // Einfache IP-Generierung basierend auf Target-Position
        int x = Math.abs(target.getLocation().getBlockX()) % 255;
        int y = Math.abs(target.getLocation().getBlockY()) % 255;
        int z = Math.abs(target.getLocation().getBlockZ()) % 255;
        int id = Math.abs(target.getId()) % 255;
        
        return String.format("10.%d.%d.%d", x, y, (z + id) % 255);
    }
}