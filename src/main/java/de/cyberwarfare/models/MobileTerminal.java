package de.cyberwarfare.models;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Represents a mobile terminal (tablet item) owned by a player
 */
public class MobileTerminal {
    
    private final int id;
    private final UUID ownerUuid;
    private final String terminalName;
    private final int securityLevel;
    private int batteryLevel;
    private boolean isActive;
    private final Timestamp createdAt;
    
    public MobileTerminal(int id, UUID ownerUuid, String terminalName, int securityLevel,
                         int batteryLevel, boolean isActive, Timestamp createdAt) {
        this.id = id;
        this.ownerUuid = ownerUuid;
        this.terminalName = terminalName;
        this.securityLevel = securityLevel;
        this.batteryLevel = batteryLevel;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }
    
    public int getId() {
        return id;
    }
    
    public UUID getOwnerUuid() {
        return ownerUuid;
    }
    
    public String getTerminalName() {
        return terminalName;
    }
    
    public int getSecurityLevel() {
        return securityLevel;
    }
    
    public int getBatteryLevel() {
        return batteryLevel;
    }
    
    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = Math.max(0, Math.min(100, batteryLevel));
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Gets the terminal's effective hacking power based on security level
     */
    public int getHackingPower() {
        return securityLevel * 10;
    }
    
    /**
     * Gets battery consumption per hack attempt
     */
    public int getBatteryConsumptionPerHack() {
        return Math.max(1, 10 - securityLevel); // Higher security = more efficient
    }
    
    /**
     * Checks if the terminal has enough battery for a hack
     */
    public boolean canPerformHack() {
        return isActive && batteryLevel >= getBatteryConsumptionPerHack();
    }
    
    /**
     * Consumes battery for a hack attempt
     */
    public void consumeBatteryForHack() {
        setBatteryLevel(batteryLevel - getBatteryConsumptionPerHack());
    }
    
    /**
     * Gets the battery status as a percentage category
     */
    public BatteryStatus getBatteryStatus() {
        if (batteryLevel > 75) {
            return BatteryStatus.FULL;
        } else if (batteryLevel > 50) {
            return BatteryStatus.HIGH;
        } else if (batteryLevel > 25) {
            return BatteryStatus.MEDIUM;
        } else if (batteryLevel > 0) {
            return BatteryStatus.LOW;
        } else {
            return BatteryStatus.EMPTY;
        }
    }
    
    public enum BatteryStatus {
        FULL("Voll", "🔋"),
        HIGH("Hoch", "🔋"),
        MEDIUM("Mittel", "🔋"),
        LOW("Niedrig", "🪫"),
        EMPTY("Leer", "🪫");
        
        private final String displayName;
        private final String icon;
        
        BatteryStatus(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getIcon() {
            return icon;
        }
    }
    
    @Override
    public String toString() {
        return String.format("MobileTerminal{id=%d, name='%s', owner=%s, security=%d, battery=%d%%}", 
            id, terminalName, ownerUuid, securityLevel, batteryLevel);
    }
}