package org.frizzlenpop.rPGSkillsPlugin.skilltree;

import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player levels based on total XP from all skills
 */
public class PlayerLevel {
    private final PlayerDataManager dataManager;
    private final XPManager xpManager;
    private static final String[] SKILL_TYPES = {"mining", "logging", "farming", "fighting", "fishing", "enchanting"};
    
    // Base XP required for each level
    private static final int BASE_XP_REQUIRED = 100;
    private static final double XP_SCALING_FACTOR = 1.15;
    
    // Store the total XP and highest level reached for each player
    private final Map<UUID, Integer> playerTotalXP = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerHighestLevel = new ConcurrentHashMap<>();
    
    public PlayerLevel(PlayerDataManager dataManager, XPManager xpManager) {
        this.dataManager = dataManager;
        this.xpManager = xpManager;
    }
    
    /**
     * Calculate total XP across all skills for a player
     */
    public int getTotalXP(Player player) {
        UUID playerUUID = player.getUniqueId();
        int totalXP = 0;
        
        for (String skill : SKILL_TYPES) {
            // Use total_earned field instead of current XP
            totalXP += dataManager.getTotalSkillXPEarned(playerUUID, skill);
        }
        
        // No need to store in a map anymore since we're using persistent storage
        return totalXP;
    }
    
    /**
     * Get player's overall level based on total XP
     */
    public int getPlayerLevel(Player player) {
        UUID playerUUID = player.getUniqueId();
        int totalXP = getTotalXP(player);
        
        // Start at level 1
        int calculatedLevel = 1;
        
        // Calculate how much total XP is needed for each level until we find the player's level
        int cumulativeXP = 0;
        
        // While the cumulative XP for the next level is less than or equal to the player's total XP
        while (true) {
            int xpForNextLevel = getRequiredXPForLevel(calculatedLevel);
            if (cumulativeXP + xpForNextLevel > totalXP) {
                // Not enough XP for the next level
                break;
            }
            
            // Add the XP required for this level to the cumulative total
            cumulativeXP += xpForNextLevel;
            
            // Increase level
            calculatedLevel++;
        }
        
        // Return the calculated level - no need to persist highest level since that's 
        // now handled by the data manager for individual skills
        return calculatedLevel;
    }
    
    /**
     * Get the XP required for a specific level
     */
    public int getRequiredXPForLevel(int level) {
        return (int)(BASE_XP_REQUIRED * Math.pow(XP_SCALING_FACTOR, level - 1));
    }
    
    /**
     * Get the total XP required to reach a specific level from level 1
     */
    public int getTotalXPForLevel(int targetLevel) {
        int totalXP = 0;
        for (int level = 1; level < targetLevel; level++) {
            totalXP += getRequiredXPForLevel(level);
        }
        return totalXP;
    }
    
    /**
     * Get total skill points available to a player
     * 1 point per level + 2 bonus points every 10 levels (total 3 at milestones)
     * Players always start with at least 1 point regardless of level
     */
    public int getTotalSkillPoints(Player player) {
        int level = getPlayerLevel(player);
        int milestones = level / 10;
        int points = level + (milestones * 2);
        
        // Ensure players always have at least 1 point to start with
        return Math.max(1, points);
    }
    
    /**
     * Get the player's progress to the next level as a percentage
     */
    public double getLevelProgress(Player player) {
        int totalXP = getTotalXP(player);
        int currentLevel = getPlayerLevel(player);
        
        // Calculate the XP needed for previous levels (from level 1 to current level)
        int xpForCurrentLevel = getTotalXPForLevel(currentLevel);
        
        // Calculate XP needed for the next level
        int xpRequiredForNextLevel = getRequiredXPForLevel(currentLevel);
        
        // Calculate how much XP player has in the current level
        int xpInCurrentLevel = totalXP - xpForCurrentLevel;
        
        // Calculate progress as a percentage
        return (double) xpInCurrentLevel / xpRequiredForNextLevel;
    }
    
    /**
     * Get XP remaining until next level
     */
    public int getXPUntilNextLevel(Player player) {
        int totalXP = getTotalXP(player);
        int currentLevel = getPlayerLevel(player);
        
        // Calculate the XP needed for previous levels (from level 1 to current level)
        int xpForCurrentLevel = getTotalXPForLevel(currentLevel);
        
        // Calculate XP needed for the next level
        int xpRequiredForNextLevel = getRequiredXPForLevel(currentLevel);
        
        // Calculate how much XP player has in the current level
        int xpInCurrentLevel = totalXP - xpForCurrentLevel;
        
        // Calculate remaining XP needed
        return xpRequiredForNextLevel - xpInCurrentLevel;
    }
} 