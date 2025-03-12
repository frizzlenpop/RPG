package org.frizzlenpop.rPGSkillsPlugin.skilltree;

import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

import java.util.Arrays;
import java.util.UUID;

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
            totalXP += dataManager.getSkillXP(playerUUID, skill);
        }
        
        return totalXP;
    }
    
    /**
     * Get player's overall level based on total XP
     */
    public int getPlayerLevel(Player player) {
        int totalXP = getTotalXP(player);
        int level = 1;
        int requiredXP = getRequiredXPForLevel(level);
        
        while (totalXP >= requiredXP) {
            totalXP -= requiredXP;
            level++;
            requiredXP = getRequiredXPForLevel(level);
        }
        
        return level;
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
     */
    public int getTotalSkillPoints(Player player) {
        int level = getPlayerLevel(player);
        int milestones = level / 10;
        return level + (milestones * 2);
    }
    
    /**
     * Get the player's progress to the next level as a percentage
     */
    public double getLevelProgress(Player player) {
        int totalXP = getTotalXP(player);
        int currentLevel = getPlayerLevel(player);
        int xpForCurrentLevel = getTotalXPForLevel(currentLevel);
        int xpForNextLevel = getTotalXPForLevel(currentLevel + 1);
        
        int xpInCurrentLevel = totalXP - xpForCurrentLevel;
        int xpRequiredForNextLevel = xpForNextLevel - xpForCurrentLevel;
        
        return (double) xpInCurrentLevel / xpRequiredForNextLevel;
    }
    
    /**
     * Get XP remaining until next level
     */
    public int getXPUntilNextLevel(Player player) {
        int totalXP = getTotalXP(player);
        int currentLevel = getPlayerLevel(player);
        int xpForNextLevel = getTotalXPForLevel(currentLevel + 1);
        
        return xpForNextLevel - totalXP;
    }
} 