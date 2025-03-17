package org.frizzlenpop.rPGSkillsPlugin.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerDataManager {
    private final File playerDataFolder;
    private final RPGSkillsPlugin plugin;
    private DatabaseManager databaseManager;
    private boolean useDatabase = false;
    
    // Cache for player skill data to reduce database queries
    private final Map<UUID, Map<String, PlayerSkillData>> skillCache = new ConcurrentHashMap<>();
    
    // Class to hold skill data in memory
    private static class PlayerSkillData {
        int level;
        int xp;
        int totalEarned;
        int highestLevel;
        
        PlayerSkillData(int level, int xp, int totalEarned, int highestLevel) {
            this.level = level;
            this.xp = xp;
            this.totalEarned = totalEarned;
            this.highestLevel = highestLevel;
        }
    }

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
    
    /**
     * Sets the database manager and enables database storage
     * 
     * @param databaseManager The database manager to use
     */
    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.useDatabase = true;
    }
    
    /**
     * Checks if database storage is enabled
     * 
     * @return True if database storage is enabled, false otherwise
     */
    public boolean isDatabaseEnabled() {
        return useDatabase && databaseManager != null;
    }

    public File getPlayerDataFolder() {
        return playerDataFolder;
    }
    
    /**
     * Checks if a player has existing data
     * 
     * @param playerUUID The player's UUID
     * @return True if the player has data, false otherwise
     */
    public boolean hasPlayerData(UUID playerUUID) {
        if (isDatabaseEnabled()) {
            try {
                CompletableFuture<Boolean> future = databaseManager.executeAsync(conn -> {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "SELECT COUNT(*) FROM player_skills WHERE player_uuid = ?")) {
                        stmt.setString(1, playerUUID.toString());
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                return rs.getInt(1) > 0;
                            }
                            return false;
                        } catch (SQLException e) {
                            plugin.getLogger().severe("Error executing query: " + e.getMessage());
                            return false;
                        }
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Error preparing statement: " + e.getMessage());
                        return false;
                    }
                });
                return future.get();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to check player data in database: " + e.getMessage());
                // Fall back to file check
                File playerFile = new File(playerDataFolder, playerUUID + ".yml");
                return playerFile.exists();
            }
        } else {
            File playerFile = new File(playerDataFolder, playerUUID + ".yml");
            return playerFile.exists();
        }
    }
    
    /**
     * Creates default player data for a new player
     * 
     * @param playerUUID The player's UUID
     */
    public void createDefaultPlayerData(UUID playerUUID) {
        if (isDatabaseEnabled()) {
            String[] skills = {"mining", "logging", "farming", "fighting", "fishing", "enchanting", "excavation", "repair"};
            
            databaseManager.executeAsyncVoid(conn -> {
                try {
                    // Insert default skill data
                    for (String skill : skills) {
                        try (PreparedStatement stmt = conn.prepareStatement(
                                "INSERT INTO player_skills (player_uuid, skill_name, level, xp) VALUES (?, ?, 1, 0) " +
                                "ON DUPLICATE KEY UPDATE level = 1, xp = 0")) {
                            try {
                                stmt.setString(1, playerUUID.toString());
                                stmt.setString(2, skill);
                                stmt.executeUpdate();
                            } catch (SQLException e) {
                                plugin.getLogger().severe("Error inserting default skill data: " + e.getMessage());
                            }
                        } catch (SQLException e) {
                            plugin.getLogger().severe("Error preparing statement for default skill data: " + e.getMessage());
                        }
                    }
                    
                    // Insert default settings
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO player_settings (player_uuid, setting_name, setting_value) VALUES (?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)")) {
                        try {
                            // Scoreboard enabled by default
                            stmt.setString(1, playerUUID.toString());
                            stmt.setString(2, "scoreboard");
                            stmt.setString(3, "true");
                            stmt.executeUpdate();
                            
                            // Show RPG hub on login enabled by default
                            stmt.setString(1, playerUUID.toString());
                            stmt.setString(2, "show_rpg_hub_on_login");
                            stmt.setString(3, "true");
                            stmt.executeUpdate();
                        } catch (SQLException e) {
                            plugin.getLogger().severe("Error inserting default settings: " + e.getMessage());
                        }
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Error preparing statement for default settings: " + e.getMessage());
                    }
                    
                    // Update cache
                    Map<String, PlayerSkillData> playerSkills = new HashMap<>();
                    for (String skill : skills) {
                        playerSkills.put(skill, new PlayerSkillData(1, 0, 0, 1));
                    }
                    skillCache.put(playerUUID, playerSkills);
                    
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to create default player data in database: " + e.getMessage());
                    // Fall back to file-based storage
                    createNewPlayerFile(playerUUID);
                }
            });
        } else {
            createNewPlayerFile(playerUUID);
        }
    }

    public FileConfiguration getPlayerData(UUID playerUUID) {
        File playerFile = new File(playerDataFolder, playerUUID + ".yml");
        if (!playerFile.exists()) {
            createNewPlayerFile(playerUUID);
        }
        return YamlConfiguration.loadConfiguration(playerFile);
    }

    /**
     * Saves player data to file
     * 
     * @param playerUUID The player's UUID
     */
    public void savePlayerData(UUID playerUUID) {
        if (!isDatabaseEnabled()) {
            FileConfiguration config = getPlayerData(playerUUID);
            savePlayerData(playerUUID, config);
        }
        // When using database, data is saved immediately on each operation
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
            config.set("preferences.show_rpg_hub_on_login", true);

            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create new data file for " + playerUUID);
        }
    }
    
    /**
     * Loads player skill data into cache
     * 
     * @param playerUUID The player's UUID
     */
    private void loadPlayerSkillData(UUID playerUUID) {
        if (!isDatabaseEnabled() || skillCache.containsKey(playerUUID)) {
            return;
        }
        
        try {
            Map<String, PlayerSkillData> playerSkills = databaseManager.executeAsync(conn -> {
                Map<String, PlayerSkillData> skills = new HashMap<>();
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT skill_name, level, xp FROM player_skills WHERE player_uuid = ?")) {
                    stmt.setString(1, playerUUID.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            String skillName = rs.getString("skill_name");
                            int level = rs.getInt("level");
                            int xp = rs.getInt("xp");
                            // Default values for total_earned and highest_level if not in database
                            int totalEarned = xp;
                            int highestLevel = level;
                            
                            skills.put(skillName, new PlayerSkillData(level, xp, totalEarned, highestLevel));
                        }
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Error executing query: " + e.getMessage());
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Error preparing statement: " + e.getMessage());
                }
                return skills;
            }).get();
            
            skillCache.put(playerUUID, playerSkills);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load player skill data from database: " + e.getMessage());
        }
    }

    public int getSkillLevel(UUID playerUUID, String skill) {
        if (isDatabaseEnabled()) {
            // Check cache first
            if (skillCache.containsKey(playerUUID) && skillCache.get(playerUUID).containsKey(skill)) {
                return skillCache.get(playerUUID).get(skill).level;
            }
            
            // Load from database if not in cache
            loadPlayerSkillData(playerUUID);
            
            // Check cache again
            if (skillCache.containsKey(playerUUID) && skillCache.get(playerUUID).containsKey(skill)) {
                return skillCache.get(playerUUID).get(skill).level;
            }
            
            // If still not found, return default
            return 1;
        } else {
            FileConfiguration config = getPlayerData(playerUUID);
            return config.getInt("skills." + skill + ".level", 1);
        }
    }

    public void setSkillLevel(UUID playerUUID, String skill, int level) {
        if (isDatabaseEnabled()) {
            // Update database
            databaseManager.executeAsyncVoid(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO player_skills (player_uuid, skill_name, level) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE level = ?")) {
                    try {
                        stmt.setString(1, playerUUID.toString());
                        stmt.setString(2, skill);
                        stmt.setInt(3, level);
                        stmt.setInt(4, level);
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Error setting skill level: " + e.getMessage());
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Error preparing statement: " + e.getMessage());
                }
            });
            
            // Update cache
            skillCache.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>())
                     .computeIfAbsent(skill, k -> new PlayerSkillData(1, 0, 0, 1))
                     .level = level;
            
            // Update highest level if needed
            int currentHighest = getHighestSkillLevel(playerUUID, skill);
            if (level > currentHighest) {
                setHighestSkillLevel(playerUUID, skill, level);
            }
        } else {
            FileConfiguration config = getPlayerData(playerUUID);
            config.set("skills." + skill + ".level", level);
            savePlayerData(playerUUID, config);
        }
    }

    public int getSkillXP(UUID playerUUID, String skill) {
        if (isDatabaseEnabled()) {
            // Check cache first
            if (skillCache.containsKey(playerUUID) && skillCache.get(playerUUID).containsKey(skill)) {
                return skillCache.get(playerUUID).get(skill).xp;
            }
            
            // Load from database if not in cache
            loadPlayerSkillData(playerUUID);
            
            // Check cache again
            if (skillCache.containsKey(playerUUID) && skillCache.get(playerUUID).containsKey(skill)) {
                return skillCache.get(playerUUID).get(skill).xp;
            }
            
            // If still not found, return default
            return 0;
        } else {
            FileConfiguration config = getPlayerData(playerUUID);
            return config.getInt("skills." + skill + ".xp", 0);
        }
    }

    public void setSkillXP(UUID playerUUID, String skill, int xp) {
        if (isDatabaseEnabled()) {
            // Get current XP for calculating total earned
            int currentXP = getSkillXP(playerUUID, skill);
            int totalEarned = getTotalSkillXPEarned(playerUUID, skill);
            
            // Only add positive XP gains to total earned
            if (xp > currentXP) {
                totalEarned += (xp - currentXP);
            }
            
            // Update database
            final int finalTotalEarned = totalEarned;
            databaseManager.executeAsyncVoid(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO player_skills (player_uuid, skill_name, xp) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE xp = ?")) {
                    try {
                        stmt.setString(1, playerUUID.toString());
                        stmt.setString(2, skill);
                        stmt.setInt(3, xp);
                        stmt.setInt(4, xp);
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Error setting skill XP: " + e.getMessage());
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Error preparing statement: " + e.getMessage());
                }
            });
            
            // Update cache
            skillCache.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>())
                     .computeIfAbsent(skill, k -> new PlayerSkillData(1, 0, 0, 1))
                     .xp = xp;
            
            // Update total earned in cache
            skillCache.get(playerUUID).get(skill).totalEarned = finalTotalEarned;
        } else {
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
    }
    
    /**
     * Gets the total XP earned for a skill (lifetime)
     */
    public int getTotalSkillXPEarned(UUID playerUUID, String skill) {
        if (isDatabaseEnabled()) {
            // Check cache first
            if (skillCache.containsKey(playerUUID) && skillCache.get(playerUUID).containsKey(skill)) {
                return skillCache.get(playerUUID).get(skill).totalEarned;
            }
            
            // Load from database if not in cache
            loadPlayerSkillData(playerUUID);
            
            // Check cache again
            if (skillCache.containsKey(playerUUID) && skillCache.get(playerUUID).containsKey(skill)) {
                return skillCache.get(playerUUID).get(skill).totalEarned;
            }
            
            // If still not found, return default
            return 0;
        } else {
            FileConfiguration config = getPlayerData(playerUUID);
            return config.getInt("skills." + skill + ".total_earned", 0);
        }
    }
    
    /**
     * Sets the total XP earned for a skill (lifetime)
     */
    public void setTotalSkillXPEarned(UUID playerUUID, String skill, int totalEarned) {
        if (isDatabaseEnabled()) {
            databaseManager.executeAsyncVoid(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE player_skills SET total_earned = ? WHERE player_uuid = ? AND skill_name = ?")) {
                    try {
                        stmt.setInt(1, totalEarned);
                        stmt.setString(2, playerUUID.toString());
                        stmt.setString(3, skill);
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Error setting total skill XP earned: " + e.getMessage());
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Error preparing statement: " + e.getMessage());
                }
            });
            
            // Update cache
            if (skillCache.containsKey(playerUUID) && skillCache.get(playerUUID).containsKey(skill)) {
                skillCache.get(playerUUID).get(skill).totalEarned = totalEarned;
            }
        } else {
            FileConfiguration config = getPlayerData(playerUUID);
            config.set("skills." + skill + ".total_earned", totalEarned);
            savePlayerData(playerUUID, config);
        }
    }
    
    /**
     * Sets the highest level achieved for a skill
     */
    public void setHighestSkillLevel(UUID playerUUID, String skill, int level) {
        if (isDatabaseEnabled()) {
            // Update database
            databaseManager.executeAsyncVoid(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE player_skills SET highest_level = ? WHERE player_uuid = ? AND skill_name = ?")) {
                    try {
                        stmt.setInt(1, level);
                        stmt.setString(2, playerUUID.toString());
                        stmt.setString(3, skill);
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Error setting highest skill level: " + e.getMessage());
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Error preparing statement: " + e.getMessage());
                }
            });
            
            // Update cache
            if (skillCache.containsKey(playerUUID) && skillCache.get(playerUUID).containsKey(skill)) {
                skillCache.get(playerUUID).get(skill).highestLevel = level;
            }
        } else {
            FileConfiguration config = getPlayerData(playerUUID);
            config.set("skills." + skill + ".highest_level", level);
            savePlayerData(playerUUID, config);
        }
    }
    
    /**
     * Gets the highest level achieved for a skill
     */
    public int getHighestSkillLevel(UUID playerUUID, String skill) {
        if (isDatabaseEnabled()) {
            // Check cache first
            if (skillCache.containsKey(playerUUID) && skillCache.get(playerUUID).containsKey(skill)) {
                return skillCache.get(playerUUID).get(skill).highestLevel;
            }
            
            // Load from database if not in cache
            loadPlayerSkillData(playerUUID);
            
            // Check cache again
            if (skillCache.containsKey(playerUUID) && skillCache.get(playerUUID).containsKey(skill)) {
                return skillCache.get(playerUUID).get(skill).highestLevel;
            }
            
            // If still not found, return default
            return 1;
        } else {
            FileConfiguration config = getPlayerData(playerUUID);
            return config.getInt("skills." + skill + ".highest_level", 1);
        }
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
        if (isDatabaseEnabled()) {
            try {
                return databaseManager.executeAsync(conn -> {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "SELECT setting_value FROM player_settings WHERE player_uuid = ? AND setting_name = ?")) {
                        try {
                            stmt.setString(1, playerUUID.toString());
                            stmt.setString(2, "scoreboard");
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    return Boolean.parseBoolean(rs.getString("setting_value"));
                                }
                                return true; // Default to enabled
                            } catch (SQLException e) {
                                plugin.getLogger().severe("Error executing query for scoreboard setting: " + e.getMessage());
                                return true; // Default to enabled on error
                            }
                        } catch (SQLException e) {
                            plugin.getLogger().severe("Error setting parameters for scoreboard query: " + e.getMessage());
                            return true; // Default to enabled on error
                        }
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Error preparing statement for scoreboard query: " + e.getMessage());
                        return true; // Default to enabled on error
                    }
                }).get();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to get scoreboard setting from database: " + e.getMessage());
                // Fall back to file
                FileConfiguration config = getPlayerData(playerUUID);
                return config.getBoolean("preferences.scoreboard", true);
            }
        } else {
            FileConfiguration config = getPlayerData(playerUUID);
            return config.getBoolean("preferences.scoreboard", true); // Default to enabled
        }
    }

    /**
     * Set whether the scoreboard is enabled for a player
     * @param playerUUID The player's UUID
     * @param enabled Whether the scoreboard should be enabled
     */
    public void setScoreboardEnabled(UUID playerUUID, boolean enabled) {
        if (isDatabaseEnabled()) {
            databaseManager.executeAsyncVoid(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO player_settings (player_uuid, setting_name, setting_value) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE setting_value = ?")) {
                    try {
                        stmt.setString(1, playerUUID.toString());
                        stmt.setString(2, "scoreboard");
                        stmt.setString(3, String.valueOf(enabled));
                        stmt.setString(4, String.valueOf(enabled));
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Error setting scoreboard setting: " + e.getMessage());
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Error preparing statement for scoreboard setting: " + e.getMessage());
                }
            });
        } else {
            FileConfiguration config = getPlayerData(playerUUID);
            config.set("preferences.scoreboard", enabled);
            savePlayerData(playerUUID, config);
        }
    }
    
    /**
     * Clears the skill cache for a player
     * 
     * @param playerUUID The player's UUID
     */
    public void clearCache(UUID playerUUID) {
        skillCache.remove(playerUUID);
    }
    
    /**
     * Clears the entire skill cache
     */
    public void clearAllCaches() {
        skillCache.clear();
    }
    
    /**
     * Migrates player data from a YAML file to the database
     * 
     * @param playerUUID The player's UUID
     * @param file The YAML file to migrate from
     * @return True if migration was successful, false otherwise
     */
    public boolean migratePlayerData(UUID playerUUID) {
        if (!isDatabaseEnabled()) {
            plugin.getLogger().warning("Cannot migrate player data: Database is not enabled");
            return false;
        }
        
        File playerFile = new File(plugin.getDataFolder(), "playerdata/" + playerUUID.toString() + ".yml");
        if (!playerFile.exists()) {
            plugin.getLogger().warning("Cannot migrate player data: Player file does not exist");
            return false;
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            
            // Migrate skills
            if (config.contains("skills")) {
                for (String skill : config.getConfigurationSection("skills").getKeys(false)) {
                    int level = config.getInt("skills." + skill + ".level", 1);
                    int xp = config.getInt("skills." + skill + ".xp", 0);
                    int totalEarned = config.getInt("skills." + skill + ".total_earned", xp);
                    int highestLevel = config.getInt("skills." + skill + ".highest_level", level);
                    
                    final String skillName = skill;
                    final int finalLevel = level;
                    final int finalXp = xp;
                    final int finalTotalEarned = totalEarned;
                    final int finalHighestLevel = highestLevel;
                    
                    databaseManager.executeAsyncVoid(conn -> {
                        try (PreparedStatement stmt = conn.prepareStatement(
                                "INSERT INTO player_skills (player_uuid, skill_name, level, xp, total_earned, highest_level) VALUES (?, ?, ?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE level = ?, xp = ?, total_earned = ?, highest_level = ?")) {
                            try {
                                stmt.setString(1, playerUUID.toString());
                                stmt.setString(2, skillName);
                                stmt.setInt(3, finalLevel);
                                stmt.setInt(4, finalXp);
                                stmt.setInt(5, finalTotalEarned);
                                stmt.setInt(6, finalHighestLevel);
                                stmt.setInt(7, finalLevel);
                                stmt.setInt(8, finalXp);
                                stmt.setInt(9, finalTotalEarned);
                                stmt.setInt(10, finalHighestLevel);
                                stmt.executeUpdate();
                            } catch (SQLException e) {
                                plugin.getLogger().severe("Error migrating player skill data: " + e.getMessage());
                            }
                        } catch (SQLException e) {
                            plugin.getLogger().severe("Error preparing statement for skill migration: " + e.getMessage());
                        }
                    }).get(); // Wait for completion
                }
                
                // Migrate preferences
                boolean scoreboardEnabled = config.getBoolean("preferences.scoreboard", true);
                boolean showRpgHubOnLogin = config.getBoolean("preferences.show_rpg_hub_on_login", true);
                
                databaseManager.executeAsyncVoid(conn -> {
                    try {
                        // Scoreboard setting
                        try (PreparedStatement stmt = conn.prepareStatement(
                                "INSERT INTO player_settings (player_uuid, setting_name, setting_value) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE setting_value = ?")) {
                            try {
                                stmt.setString(1, playerUUID.toString());
                                stmt.setString(2, "scoreboard");
                                stmt.setString(3, String.valueOf(scoreboardEnabled));
                                stmt.setString(4, String.valueOf(scoreboardEnabled));
                                stmt.executeUpdate();
                            } catch (SQLException e) {
                                plugin.getLogger().severe("Error migrating scoreboard setting: " + e.getMessage());
                            }
                        } catch (SQLException e) {
                            plugin.getLogger().severe("Error preparing statement for scoreboard setting: " + e.getMessage());
                        }
                        
                        // RPG hub setting
                        try (PreparedStatement stmt = conn.prepareStatement(
                                "INSERT INTO player_settings (player_uuid, setting_name, setting_value) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE setting_value = ?")) {
                            try {
                                stmt.setString(1, playerUUID.toString());
                                stmt.setString(2, "show_rpg_hub_on_login");
                                stmt.setString(3, String.valueOf(showRpgHubOnLogin));
                                stmt.setString(4, String.valueOf(showRpgHubOnLogin));
                                stmt.executeUpdate();
                            } catch (SQLException e) {
                                plugin.getLogger().severe("Error migrating RPG hub setting: " + e.getMessage());
                            }
                        } catch (SQLException e) {
                            plugin.getLogger().severe("Error preparing statement for RPG hub setting: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        plugin.getLogger().severe("Failed to migrate player settings: " + e.getMessage());
                    }
                }).get(); // Wait for completion
                
                // Migrate passive abilities
                if (config.contains("passiveAbilities")) {
                    for (String skill : config.getConfigurationSection("passiveAbilities").getKeys(false)) {
                        for (String passive : config.getStringList("passiveAbilities." + skill)) {
                            final String skillName = skill;
                            final String passiveName = passive;
                            
                            databaseManager.executeAsyncVoid(conn -> {
                                try (PreparedStatement stmt = conn.prepareStatement(
                                        "INSERT INTO player_passives (player_uuid, skill_name, passive_name) VALUES (?, ?, ?) " +
                                        "ON DUPLICATE KEY UPDATE passive_name = VALUES(passive_name)")) {
                                    try {
                                        stmt.setString(1, playerUUID.toString());
                                        stmt.setString(2, skillName);
                                        stmt.setString(3, passiveName);
                                        stmt.executeUpdate();
                                    } catch (SQLException e) {
                                        plugin.getLogger().severe("Error migrating passive ability: " + e.getMessage());
                                    }
                                } catch (SQLException e) {
                                    plugin.getLogger().severe("Error preparing statement for passive ability: " + e.getMessage());
                                }
                            }).get(); // Wait for completion
                        }
                    }
                }
                
                return true;
            }
            
            return false;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to migrate player data: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get whether to show the RPG hub on login for a player
     * @param playerUUID The player's UUID
     * @return true if the RPG hub should be shown on login, false otherwise
     */
    public boolean getShowRpgHubOnLogin(UUID playerUUID) {
        if (isDatabaseEnabled()) {
            try {
                return databaseManager.executeAsync(conn -> {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "SELECT setting_value FROM player_settings WHERE player_uuid = ? AND setting_name = ?")) {
                        try {
                            stmt.setString(1, playerUUID.toString());
                            stmt.setString(2, "show_rpg_hub_on_login");
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    return Boolean.parseBoolean(rs.getString("setting_value"));
                                }
                                return true; // Default to enabled
                            } catch (SQLException e) {
                                plugin.getLogger().severe("Error executing query for RPG hub setting: " + e.getMessage());
                                return true; // Default to enabled on error
                            }
                        } catch (SQLException e) {
                            plugin.getLogger().severe("Error setting parameters for RPG hub query: " + e.getMessage());
                            return true; // Default to enabled on error
                        }
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Error preparing statement for RPG hub query: " + e.getMessage());
                        return true; // Default to enabled on error
                    }
                }).get();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to get RPG hub setting from database: " + e.getMessage());
                // Fall back to file
                FileConfiguration config = getPlayerData(playerUUID);
                return config.getBoolean("preferences.show_rpg_hub_on_login", true);
            }
        } else {
            FileConfiguration config = getPlayerData(playerUUID);
            return config.getBoolean("preferences.show_rpg_hub_on_login", true); // Default to enabled
        }
    }

    /**
     * Set whether to show the RPG hub on login for a player
     * @param playerUUID The player's UUID
     * @param enabled Whether the RPG hub should be shown on login
     */
    public void setShowRpgHubOnLogin(UUID playerUUID, boolean enabled) {
        if (isDatabaseEnabled()) {
            databaseManager.executeAsyncVoid(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO player_settings (player_uuid, setting_name, setting_value) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE setting_value = ?")) {
                    try {
                        stmt.setString(1, playerUUID.toString());
                        stmt.setString(2, "show_rpg_hub_on_login");
                        stmt.setString(3, String.valueOf(enabled));
                        stmt.setString(4, String.valueOf(enabled));
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Error setting RPG hub setting: " + e.getMessage());
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Error preparing statement for RPG hub setting: " + e.getMessage());
                }
            });
        } else {
            FileConfiguration config = getPlayerData(playerUUID);
            config.set("preferences.show_rpg_hub_on_login", enabled);
            savePlayerData(playerUUID, config);
        }
    }

    public RPGSkillsPlugin getPlugin() {
        return plugin;
    }
}