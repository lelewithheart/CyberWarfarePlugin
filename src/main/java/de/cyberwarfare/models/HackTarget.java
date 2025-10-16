package de.cyberwarfare.models;

import de.cyberwarfare.managers.TargetManager.TargetType;
import org.bukkit.Location;

import java.sql.Timestamp;

/**
 * Represents a hackable target in the world
 */
public class HackTarget {
    
    private final int id;
    private final Location location;
    private final TargetType targetType;
    private final int difficulty;
    private boolean isCompromised;
    private final int value;
    private final String createdBy;
    private final Timestamp createdAt;
    private Timestamp lastHacked;
    
    public HackTarget(int id, Location location, TargetType targetType, int difficulty, 
                     boolean isCompromised, int value, String createdBy, 
                     Timestamp createdAt, Timestamp lastHacked) {
        this.id = id;
        this.location = location;
        this.targetType = targetType;
        this.difficulty = difficulty;
        this.isCompromised = isCompromised;
        this.value = value;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.lastHacked = lastHacked;
    }
    
    public int getId() {
        return id;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public TargetType getTargetType() {
        return targetType;
    }
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public boolean isCompromised() {
        return isCompromised;
    }
    
    public void setCompromised(boolean compromised) {
        isCompromised = compromised;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public Timestamp getLastHacked() {
        return lastHacked;
    }
    
    public void setLastHacked(Timestamp lastHacked) {
        this.lastHacked = lastHacked;
    }
    
    /**
     * Gets the effective difficulty including base difficulty
     */
    public int getEffectiveDifficulty() {
        return targetType.getBaseDifficulty() + difficulty;
    }
    
    /**
     * Gets the hack reward based on difficulty and type
     */
    public int getHackReward() {
        double multiplier = isCompromised ? 0.3 : 1.0; // Reduced reward for already compromised targets
        return (int) (value * multiplier);
    }
    
    /**
     * Gets the trace penalty for failed hacks
     */
    public int getTracePenalty() {
        return getEffectiveDifficulty() * 5;
    }
    
    /**
     * Checks if the target can be hacked again
     */
    public boolean canBeHacked() {
        if (!isCompromised) {
            return true;
        }
        
        // Compromised targets can be hacked again after some time
        if (lastHacked == null) {
            return true;
        }
        
        long timeSinceLastHack = System.currentTimeMillis() - lastHacked.getTime();
        long cooldownTime = 30 * 60 * 1000; // 30 minutes in milliseconds
        
        return timeSinceLastHack > cooldownTime;
    }
    
    @Override
    public String toString() {
        return String.format("HackTarget{id=%d, type=%s, location=%s, difficulty=%d, compromised=%b}", 
            id, targetType.name(), location.toString(), difficulty, isCompromised);
    }
}