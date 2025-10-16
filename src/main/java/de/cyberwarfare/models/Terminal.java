package de.cyberwarfare.models;

import org.bukkit.Location;

import java.sql.Timestamp;

/**
 * Represents a hacking terminal in the world
 */
public class Terminal {
    
    private final int id;
    private final Location location;
    private final int securityLevel;
    private final String createdBy;
    private final Timestamp createdAt;
    
    public Terminal(int id, Location location, int securityLevel, String createdBy, Timestamp createdAt) {
        this.id = id;
        this.location = location;
        this.securityLevel = securityLevel;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }
    
    public int getId() {
        return id;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public int getSecurityLevel() {
        return securityLevel;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Gets the terminal difficulty based on security level
     */
    public int getDifficulty() {
        return Math.max(1, Math.min(10, securityLevel));
    }
    
    /**
     * Gets the hacking reward based on security level
     */
    public int getHackReward() {
        return securityLevel * 50;
    }
    
    /**
     * Gets the trace penalty for failed hacks
     */
    public int getTracePenalty() {
        return securityLevel * 10;
    }
    
    @Override
    public String toString() {
        return String.format("Terminal{id=%d, location=%s, security=%d}", 
            id, location.toString(), securityLevel);
    }
}