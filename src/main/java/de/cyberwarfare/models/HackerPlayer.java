package de.cyberwarfare.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a player's hacking statistics and progression
 */
public class HackerPlayer {
    
    private final UUID playerId;
    private int successfulHacks;
    private int failedHacks;
    private int totalTraceScore;
    private int skillLevel;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public HackerPlayer(UUID playerId, int successfulHacks, int failedHacks, 
                       int totalTraceScore, int skillLevel, 
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.playerId = playerId;
        this.successfulHacks = successfulHacks;
        this.failedHacks = failedHacks;
        this.totalTraceScore = totalTraceScore;
        this.skillLevel = skillLevel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters
    public UUID getPlayerId() { return playerId; }
    public int getSuccessfulHacks() { return successfulHacks; }
    public int getFailedHacks() { return failedHacks; }
    public int getTotalHacks() { return successfulHacks + failedHacks; }
    public int getTotalTraceScore() { return totalTraceScore; }
    public int getSkillLevel() { return skillLevel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Calculated properties
    public double getSuccessRate() {
        if (getTotalHacks() == 0) return 0.0;
        return (double) successfulHacks / getTotalHacks();
    }
    
    public String getSkillRank() {
        return switch (skillLevel) {
            case 1 -> "Novice";
            case 2, 3 -> "Amateur";
            case 4, 5 -> "Intermediate";
            case 6, 7, 8 -> "Advanced";
            case 9, 10 -> "Expert";
            case 11, 12, 13 -> "Master";
            case 14, 15 -> "Elite";
            default -> skillLevel >= 16 ? "Legendary" : "Unknown";
        };
    }
    
    // Modifiers
    public void addSuccessfulHack() {
        this.successfulHacks++;
        checkSkillLevelUp();
        updateTimestamp();
    }
    
    public void addFailedHack() {
        this.failedHacks++;
        updateTimestamp();
    }
    
    public void addTraceScore(int score) {
        this.totalTraceScore += score;
        updateTimestamp();
    }
    
    public void setSkillLevel(int skillLevel) {
        this.skillLevel = skillLevel;
        updateTimestamp();
    }
    
    private void checkSkillLevelUp() {
        int requiredHacks = getRequiredHacksForLevel(skillLevel + 1);
        if (successfulHacks >= requiredHacks) {
            skillLevel++;
            checkSkillLevelUp(); // Check for multiple level ups
        }
    }
    
    public static int getRequiredHacksForLevel(int level) {
        return (int) Math.pow(level - 1, 2) * 5 + (level - 1) * 5;
    }
    
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Create HackerPlayer from database ResultSet
     */
    public static HackerPlayer fromResultSet(ResultSet rs) throws SQLException {
        UUID playerId = UUID.fromString(rs.getString("player_uuid"));
        int successfulHacks = rs.getInt("successful_hacks");
        int failedHacks = rs.getInt("failed_hacks");
        int totalTraceScore = rs.getInt("total_trace_score");
        int skillLevel = rs.getInt("skill_level");
        
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        LocalDateTime updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();
        
        return new HackerPlayer(playerId, successfulHacks, failedHacks, 
                              totalTraceScore, skillLevel, createdAt, updatedAt);
    }
    
    @Override
    public String toString() {
        return "HackerPlayer{" +
                "playerId=" + playerId +
                ", successfulHacks=" + successfulHacks +
                ", failedHacks=" + failedHacks +
                ", skillLevel=" + skillLevel +
                ", skillRank='" + getSkillRank() + '\'' +
                ", successRate=" + String.format("%.1f%%", getSuccessRate() * 100) +
                '}';
    }
}