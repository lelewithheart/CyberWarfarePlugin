package de.cyberwarfare.gui.minigames;

import de.cyberwarfare.CyberWarfarePlugin;
import de.cyberwarfare.managers.HackingManager.HackingSession;
import de.cyberwarfare.models.HackTarget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Password cracking minigame - match sequences of characters
 */
public class PasswordCrackingMinigame extends BaseMinigame {
    
    private String targetPassword;
    private StringBuilder currentInput;
    private List<Character> availableChars;
    private int correctCharacters;
    private final int passwordLength;
    private final Random random;
    
    public PasswordCrackingMinigame(CyberWarfarePlugin plugin, org.bukkit.entity.Player player, HackTarget target, HackingSession session) {
        super(plugin, player, target, session);
        this.random = new Random();
        this.passwordLength = Math.max(4, Math.min(8, target.getEffectiveDifficulty()));
        this.currentInput = new StringBuilder();
        this.correctCharacters = 0;
        generatePassword();
    }
    
    @Override
    protected void createGUI() {
        gui = Bukkit.createInventory(null, 54, Component.text("🔐 Password Cracking"));
        
        // Header items
        gui.setItem(4, createTimerItem());
        gui.setItem(13, createTargetInfoItem());
        gui.setItem(22, createPasswordDisplayItem());
        
        // Character selection area (bottom 3 rows)
        updateCharacterButtons();
        
        // Progress bar
        updateProgressBar();
        
        // Control buttons
        gui.setItem(48, createClearButton());
        gui.setItem(50, createSubmitButton());
        
        fillEmptySlots();
    }
    
    /**
     * Generates a random password to crack
     */
    private void generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        targetPassword = "";
        
        for (int i = 0; i < passwordLength; i++) {
            targetPassword += chars.charAt(random.nextInt(chars.length()));
        }
        
        // Generate available characters (password chars + some extras)
        availableChars = new ArrayList<>();
        for (char c : targetPassword.toCharArray()) {
            if (!availableChars.contains(c)) {
                availableChars.add(c);
            }
        }
        
        // Add some decoy characters
        int decoyCount = Math.max(4, 12 - availableChars.size());
        while (availableChars.size() < passwordLength + decoyCount) {
            char randomChar = chars.charAt(random.nextInt(chars.length()));
            if (!availableChars.contains(randomChar)) {
                availableChars.add(randomChar);
            }
        }
        
        Collections.shuffle(availableChars);
    }
    
    /**
     * Creates password display item
     */
    private ItemStack createPasswordDisplayItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("🔐 Password eingeben", NamedTextColor.AQUA, TextDecoration.BOLD));
        
        String displayPassword = currentInput.toString();
        while (displayPassword.length() < passwordLength) {
            displayPassword += "_";
        }
        
        List<Component> lore = List.of(
            Component.empty(),
            Component.text("Zielpasswort: ", NamedTextColor.GRAY)
                .append(Component.text(displayPassword, NamedTextColor.WHITE)),
            Component.text("Länge: ", NamedTextColor.GRAY)
                .append(Component.text(passwordLength + " Zeichen", NamedTextColor.YELLOW)),
            Component.empty(),
            Component.text("Tipp: ", NamedTextColor.YELLOW)
                .append(Component.text("Finde die richtigen Zeichen!", NamedTextColor.GRAY))
            
        );
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Updates character selection buttons
     */
    private void updateCharacterButtons() {
        // Clear character area
        for (int i = 27; i < 45; i++) {
            gui.setItem(i, null);
        }
        
        // Place character buttons
        for (int i = 0; i < Math.min(availableChars.size(), 18); i++) {
            char character = availableChars.get(i);
            gui.setItem(27 + i, createCharacterButton(character));
        }
    }
    
    /**
     * Creates a character selection button
     */
    private ItemStack createCharacterButton(char character) {
        ItemStack item = new ItemStack(Material.STONE_BUTTON);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text(String.valueOf(character), NamedTextColor.WHITE, TextDecoration.BOLD));
        
        List<Component> lore = List.of(
            Component.empty(),
            Component.text("Klicken um hinzuzufügen", NamedTextColor.GREEN, TextDecoration.ITALIC)
        );
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates clear button
     */
    private ItemStack createClearButton() {
        ItemStack item = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("🗑 Löschen", NamedTextColor.RED, TextDecoration.BOLD));
        meta.lore(List.of(
            Component.empty(),
            Component.text("Eingabe zurücksetzen", NamedTextColor.GRAY)
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates submit button
     */
    private ItemStack createSubmitButton() {
        ItemStack item = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("✓ Password senden", NamedTextColor.GREEN, TextDecoration.BOLD));
        
        boolean canSubmit = currentInput.length() == passwordLength;
        NamedTextColor color = canSubmit ? NamedTextColor.GREEN : NamedTextColor.GRAY;
        
        meta.lore(List.of(
            Component.empty(),
            Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(canSubmit ? "Bereit" : "Unvollständig", color)),
            Component.empty(),
            Component.text("Hack ausführen", NamedTextColor.WHITE, TextDecoration.ITALIC)
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Updates progress bar
     */
    private void updateProgressBar() {
        // Progress bar in slots 10-16
        for (int i = 10; i <= 16; i++) {
            int progressSlot = i - 10;
            boolean filled = progressSlot < correctCharacters;
            
            Material material = filled ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            
            if (filled) {
                meta.displayName(Component.text("✓", NamedTextColor.GREEN, TextDecoration.BOLD));
            } else {
                meta.displayName(Component.empty());
            }
            
            item.setItemMeta(meta);
            gui.setItem(i, item);
        }
    }
    
    @Override
    protected void handleClick(InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        
        int slot = event.getSlot();
        
        // Character buttons (slots 27-44)
        if (slot >= 27 && slot <= 44 && clicked.getType() == Material.STONE_BUTTON) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.displayName() != null) {
                String charText = ((net.kyori.adventure.text.TextComponent) meta.displayName()).content();
                if (charText.length() == 1 && currentInput.length() < passwordLength) {
                    currentInput.append(charText);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
                    updateDisplay();
                }
            }
        }
        
        // Clear button (slot 48)
        else if (slot == 48 && clicked.getType() == Material.RED_CONCRETE) {
            currentInput.setLength(0);
            correctCharacters = 0;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
            updateDisplay();
        }
        
        // Submit button (slot 50)
        else if (slot == 50 && clicked.getType() == Material.GREEN_CONCRETE) {
            if (currentInput.length() == passwordLength) {
                checkPassword();
            }
        }
    }
    
    /**
     * Checks if the entered password is correct
     */
    private void checkPassword() {
        String enteredPassword = currentInput.toString();
        
        if (enteredPassword.equals(targetPassword)) {
            // Success!
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendMessage(Component.text("✅ Password erfolgreich geknackt!", NamedTextColor.GREEN));
            end(true);
        } else {
            // Check for partial matches
            correctCharacters = 0;
            for (int i = 0; i < Math.min(enteredPassword.length(), targetPassword.length()); i++) {
                if (enteredPassword.charAt(i) == targetPassword.charAt(i)) {
                    correctCharacters++;
                }
            }
            
            if (correctCharacters > 0) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
                player.sendMessage(Component.text("🔍 " + correctCharacters + " Zeichen korrekt!", NamedTextColor.YELLOW));
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(Component.text("❌ Komplett falsch!", NamedTextColor.RED));
            }
            
            // Reset for next attempt
            currentInput.setLength(0);
            updateDisplay();
        }
    }
    
    /**
     * Updates the GUI display
     */
    private void updateDisplay() {
        gui.setItem(22, createPasswordDisplayItem());
        gui.setItem(50, createSubmitButton());
        updateProgressBar();
    }
    
    @Override
    protected void onGameStart() {
        player.sendMessage(Component.text("🔐 Password Cracking gestartet!", NamedTextColor.AQUA));
        player.sendMessage(Component.text("Finde das " + passwordLength + "-stellige Password!", NamedTextColor.YELLOW));
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
    }
    
    @Override
    protected void onGameEnd(boolean success) {
        if (!success && timeLeft <= 0) {
            player.sendMessage(Component.text("⏰ Zeit abgelaufen! Password nicht geknackt.", NamedTextColor.RED));
            player.sendMessage(Component.text("Das Password war: " + targetPassword, NamedTextColor.GRAY));
        }
        player.playSound(player.getLocation(), success ? Sound.ENTITY_PLAYER_LEVELUP : Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }
    
    @Override
    protected void onTimerTick() {
        // Update timer display
        updateTimerDisplay();
        
        // Warning sounds
        if (timeLeft == 10 || timeLeft == 5) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        }
    }
}