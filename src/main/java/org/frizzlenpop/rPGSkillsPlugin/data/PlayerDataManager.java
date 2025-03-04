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
            String[] skills = {"mining", "logging", "farming", "fighting", "fishing", "enchanting"};
            for (String skill : skills) {
                config.set("skills." + skill + ".level", 1);
                config.set("skills." + skill + ".xp", 0);
            }

            // Create milestones section
            config.createSection("milestones");

            // Create an empty section for passive abilities
            config.createSection("passiveAbilities");

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
        config.set("skills." + skill + ".xp", xp);
        savePlayerData(playerUUID, config);
    }

    public boolean hasUnlockedActiveSkill(UUID playerUUID, String skill) {
        int level = getSkillLevel(playerUUID, skill);
        return level >= 15;
    }

    public RPGSkillsPlugin getPlugin() {
        return plugin;
    }
}