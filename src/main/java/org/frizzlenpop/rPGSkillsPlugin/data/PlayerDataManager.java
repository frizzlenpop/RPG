package org.frizzlenpop.rPGSkillsPlugin.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerDataManager {
    private final File playerDataFolder;
    private final RPGSkillsPlugin plugin;

    public PlayerDataManager(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        File pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        this.playerDataFolder = new File(pluginFolder, "playerdata");
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
    }

    public File getPlayerDataFolder() {
        return playerDataFolder;
    }

    public FileConfiguration getPlayerData(UUID playerUUID) {
        File playerFile = new File(playerDataFolder, playerUUID + ".yml");
        if (!playerFile.exists()) {
            createNewPlayerFile(playerUUID);
        }
        return YamlConfiguration.loadConfiguration(playerFile);
    }

    public void savePlayerData(UUID playerUUID, FileConfiguration config) {
        File playerFile = new File(playerDataFolder, playerUUID + ".yml");
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data for " + playerUUID);
        }
    }

    private void createNewPlayerFile(UUID playerUUID) {
        File playerFile = new File(playerDataFolder, playerUUID + ".yml");
        try {
            if (playerFile.createNewFile()) {
                plugin.getLogger().info("✅ Created new player data file: " + playerFile.getAbsolutePath());
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

            // Initialize all skills to level 1 with 0 XP
            String[] skills = {"mining", "logging", "farming", "fighting", "fishing", "enchanting", "excavation", "repair"};
            for (String skill : skills) {
                config.set("skills." + skill + ".level", 1);
                config.set("skills." + skill + ".xp", 0);
                config.set("skills." + skill + ".total_earned", 0);
                config.set("skills." + skill + ".highest_level", 1);
            }

            // Create milestones section
            config.createSection("milestones");

            // Create an empty section for passive abilities
            config.createSection("passiveAbilities");
            
            // Set default preferences
            config.set("preferences.scoreboard", true);

            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create new data file for " + playerUUID);
        }
    }

    public int getSkillLevel(UUID playerUUID, String skill) {
        FileConfiguration config = getPlayerData(playerUUID);
        return config.getInt("skills." + skill + ".level", 1);
    }

    public void setSkillLevel(UUID playerUUID, String skill, int level) {
        FileConfiguration config = getPlayerData(playerUUID);
        config.set("skills." + skill + ".level", level);
        savePlayerData(playerUUID, config);
    }

    public int getSkillXP(UUID playerUUID, String skill) {
        FileConfiguration config = getPlayerData(playerUUID);
        return config.getInt("skills." + skill + ".xp", 0);
    }

    public void setSkillXP(UUID playerUUID, String skill, int xp) {
        FileConfiguration config = getPlayerData(playerUUID);
        // Store current XP to calculate total earned
        int currentXP = config.getInt("skills." + skill + ".xp", 0);
        
        // Set new XP
        config.set("skills." + skill + ".xp", xp);
        
        // Update total XP earned for tracking purposes
        int totalEarned = config.getInt("skills." + skill + ".total_earned", 0);
        // Only add positive XP gains to total earned
        if (xp > currentXP) {
            totalEarned += (xp - currentXP);
            config.set("skills." + skill + ".total_earned", totalEarned);
        }
        
        savePlayerData(playerUUID, config);
    }
    
    /**
     * Gets the total XP earned for a skill (lifetime)
     */
    public int getTotalSkillXPEarned(UUID playerUUID, String skill) {
        FileConfiguration config = getPlayerData(playerUUID);
        return config.getInt("skills." + skill + ".total_earned", 0);
    }
    
    /**
     * Sets the highest level achieved for a skill
     */
    public void setHighestSkillLevel(UUID playerUUID, String skill, int level) {
        FileConfiguration config = getPlayerData(playerUUID);
        int currentHighest = config.getInt("skills." + skill + ".highest_level", 1);
        
        // Only update if the new level is higher
        if (level > currentHighest) {
            config.set("skills." + skill + ".highest_level", level);
            savePlayerData(playerUUID, config);
        }
    }
    
    /**
     * Gets the highest level achieved for a skill
     */
    public int getHighestSkillLevel(UUID playerUUID, String skill) {
        FileConfiguration config = getPlayerData(playerUUID);
        return config.getInt("skills." + skill + ".highest_level", 1);
    }

    public boolean hasUnlockedActiveSkill(UUID playerUUID, String skill) {
        int level = getSkillLevel(playerUUID, skill);
        return level >= 15;
    }

    /**
     * Get whether the scoreboard is enabled for a player
     * @param playerUUID The player's UUID
     * @return true if the scoreboard is enabled, false otherwise
     */
    public boolean getScoreboardEnabled(UUID playerUUID) {
        FileConfiguration config = getPlayerData(playerUUID);
        return config.getBoolean("preferences.scoreboard", true); // Default to enabled
    }

    /**
     * Set whether the scoreboard is enabled for a player
     * @param playerUUID The player's UUID
     * @param enabled Whether the scoreboard should be enabled
     */
    public void setScoreboardEnabled(UUID playerUUID, boolean enabled) {
        FileConfiguration config = getPlayerData(playerUUID);
        config.set("preferences.scoreboard", enabled);
        savePlayerData(playerUUID, config);
    }

    public RPGSkillsPlugin getPlugin() {
        return plugin;
    }
}