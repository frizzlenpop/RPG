package org.frizzlenpop.rPGSkillsPlugin.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerDataManager {

    private final File playerDataFolder;

    public PlayerDataManager() {
        File pluginFolder = Bukkit.getServer().getPluginManager().getPlugin("RPGSkillsPlugin").getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs(); // Ensure the base plugin folder exists
        }

        this.playerDataFolder = new File(pluginFolder, "playerdata");
        if (!playerDataFolder.exists()) {
            boolean created = playerDataFolder.mkdirs();
            if (created) {
                Bukkit.getLogger().info("✅ Player data folder created: " + playerDataFolder.getAbsolutePath());
            } else {
                Bukkit.getLogger().severe("❌ Failed to create player data folder!");
            }
        }
    }

    public File getPlayerDataFolder() {
        return playerDataFolder;
    }

    // Load or create player data file
    public FileConfiguration getPlayerData(UUID playerUUID) {
        File playerFile = new File(playerDataFolder, playerUUID + ".yml");
        if (!playerFile.exists()) {
            createNewPlayerFile(playerUUID);
        }
        return YamlConfiguration.loadConfiguration(playerFile);
    }

    // Save player data file
    public void savePlayerData(UUID playerUUID, FileConfiguration config) {
        File playerFile = new File(playerDataFolder, playerUUID + ".yml");
        try {
            config.save(playerFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save data for " + playerUUID);
        }
    }

    // Create new player data file
    private void createNewPlayerFile(UUID playerUUID) {
        File playerFile = new File(playerDataFolder, playerUUID + ".yml");
        try {
            if (playerFile.createNewFile()) {
                Bukkit.getLogger().info("✅ Created new player data file: " + playerFile.getAbsolutePath());
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

            // Initialize all skills to level 1 with 0 XP
            config.set("skills.mining.level", 1);
            config.set("skills.mining.xp", 0);
            config.set("skills.logging.level", 1);
            config.set("skills.logging.xp", 0);
            config.set("skills.farming.level", 1);
            config.set("skills.farming.xp", 0);
            config.set("skills.fighting.level", 1);
            config.set("skills.fighting.xp", 0);
            config.set("skills.fishing.level", 1);
            config.set("skills.fishing.xp", 0);
            config.set("skills.enchanting.level", 1);
            config.set("skills.enchanting.xp", 0);

            // Milestone tracking
            config.createSection("milestones");

            config.save(playerFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to create new data file for " + playerUUID);
        }
    }
}
