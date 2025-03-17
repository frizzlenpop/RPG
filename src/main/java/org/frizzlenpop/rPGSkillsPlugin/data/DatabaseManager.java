package org.frizzlenpop.rPGSkillsPlugin.data;

import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Manages database connections and operations for the RPG Skills Plugin.
 * Supports both MySQL and SQLite databases.
 */
public class DatabaseManager {
    private final RPGSkillsPlugin plugin;
    private Connection connection;
    private String host, database, username, password;
    private int port;
    private boolean useMySQL;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    /**
     * Creates a new DatabaseManager
     * 
     * @param plugin The plugin instance
     */
    public DatabaseManager(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
        
        if (useMySQL) {
            connectToMySQL();
        } else {
            connectToSQLite();
        }
        
        createTables();
    }
    
    /**
     * Loads database configuration from config.yml
     */
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        useMySQL = config.getBoolean("database.mysql.enabled", false);
        
        if (useMySQL) {
            host = config.getString("database.mysql.host", "localhost");
            port = config.getInt("database.mysql.port", 3306);
            database = config.getString("database.mysql.database", "rpgskills");
            username = config.getString("database.mysql.username", "root");
            password = config.getString("database.mysql.password", "");
        }
    }
    
    /**
     * Connects to a MySQL database
     */
    private void connectToMySQL() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + database + 
                "?useSSL=false&autoReconnect=true", 
                username, password);
            plugin.getLogger().info("Connected to MySQL database!");
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to connect to MySQL database: " + e.getMessage());
            plugin.getLogger().info("Falling back to SQLite...");
            useMySQL = false;
            connectToSQLite();
        }
    }
    
    /**
     * Connects to an SQLite database
     */
    private void connectToSQLite() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(
                "jdbc:sqlite:" + new File(plugin.getDataFolder(), "database.db").getAbsolutePath());
            plugin.getLogger().info("Connected to SQLite database!");
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to connect to SQLite database: " + e.getMessage());
        }
    }
    
    /**
     * Creates necessary database tables if they don't exist
     */
    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            // Player skills table
            statement.execute("CREATE TABLE IF NOT EXISTS player_skills (" +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "skill_name VARCHAR(32) NOT NULL, " +
                "level INT NOT NULL DEFAULT 1, " +
                "xp INT NOT NULL DEFAULT 0, " +
                "PRIMARY KEY (player_uuid, skill_name)" +
                ")");
            
            // Player passives table
            statement.execute("CREATE TABLE IF NOT EXISTS player_passives (" +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "skill_name VARCHAR(32) NOT NULL, " +
                "passive_name VARCHAR(64) NOT NULL, " +
                "PRIMARY KEY (player_uuid, skill_name, passive_name)" +
                ")");
            
            // Player placed blocks table
            statement.execute("CREATE TABLE IF NOT EXISTS player_placed_blocks (" +
                "world VARCHAR(64) NOT NULL, " +
                "x INT NOT NULL, " +
                "y INT NOT NULL, " +
                "z INT NOT NULL, " +
                "placed_time BIGINT NOT NULL, " +
                "PRIMARY KEY (world, x, y, z)" +
                ")");
            
            // Player settings table
            statement.execute("CREATE TABLE IF NOT EXISTS player_settings (" +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "setting_name VARCHAR(32) NOT NULL, " +
                "setting_value VARCHAR(255) NOT NULL, " +
                "PRIMARY KEY (player_uuid, setting_name)" +
                ")");
            
            // Skill tree progress table
            statement.execute("CREATE TABLE IF NOT EXISTS skill_tree_progress (" +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "tree_id VARCHAR(32) NOT NULL, " +
                "node_id VARCHAR(32) NOT NULL, " +
                "unlocked BOOLEAN NOT NULL DEFAULT 0, " +
                "PRIMARY KEY (player_uuid, tree_id, node_id)" +
                ")");
            
            plugin.getLogger().info("Database tables created successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database tables: " + e.getMessage());
        }
    }
    
    /**
     * Gets a connection to the database
     * 
     * @return The database connection
     * @throws SQLException If the connection is closed or invalid
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (useMySQL) {
                connectToMySQL();
            } else {
                connectToSQLite();
            }
        }
        return connection;
    }
    
    /**
     * Executes a database operation asynchronously
     * 
     * @param operation The operation to execute
     * @param <T> The return type of the operation
     * @return A CompletableFuture that will be completed with the result of the operation
     */
    public <T> CompletableFuture<T> executeAsync(Function<Connection, T> operation) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection()) {
                return operation.apply(conn);
            } catch (SQLException e) {
                plugin.getLogger().severe("Database operation failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    /**
     * Executes a database operation asynchronously with no return value
     * 
     * @param operation The operation to execute
     * @return A CompletableFuture that will be completed when the operation is done
     */
    public CompletableFuture<Void> executeAsyncVoid(Consumer<Connection> operation) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                operation.accept(conn);
            } catch (SQLException e) {
                plugin.getLogger().severe("Database operation failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    /**
     * Closes the database connection and executor service
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            executor.shutdown();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing database connection: " + e.getMessage());
        }
    }
    
    /**
     * Migrates data from YAML files to the database
     */
    public void migrateFromYAML() {
        plugin.getLogger().info("Starting data migration from YAML to database...");
        
        // Get all player data files
        File playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!playerDataFolder.exists() || !playerDataFolder.isDirectory()) {
            plugin.getLogger().info("No player data to migrate.");
            return;
        }
        
        File[] playerFiles = playerDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (playerFiles == null || playerFiles.length == 0) {
            plugin.getLogger().info("No player data files found.");
            return;
        }
        
        plugin.getLogger().info("Found " + playerFiles.length + " player data files to migrate.");
        
        // Schedule migration to run asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int migratedPlayers = 0;
            
            for (File file : playerFiles) {
                String fileName = file.getName();
                String uuidStr = fileName.substring(0, fileName.length() - 4); // Remove .yml
                
                try {
                    UUID playerUUID = UUID.fromString(uuidStr);
                    
                    // Load player data from file
                    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                    
                    // Migrate skill data
                    boolean success = migratePlayerData(playerUUID, config);
                    
                    if (success) {
                        migratedPlayers++;
                        
                        // Rename the file to indicate it's been migrated
                        File backupFile = new File(playerDataFolder, fileName + ".migrated");
                        if (!file.renameTo(backupFile)) {
                            plugin.getLogger().warning("Could not rename migrated file: " + fileName);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid player data file name: " + fileName);
                }
            }
            
            plugin.getLogger().info("Migration complete! Migrated " + migratedPlayers + " player data files.");
        });
    }
    
    /**
     * Migrates a single player's data from a YAML configuration to the database
     * 
     * @param playerUUID The player's UUID
     * @param config The player's configuration
     * @return True if migration was successful, false otherwise
     */
    private boolean migratePlayerData(UUID playerUUID, FileConfiguration config) {
        try {
            // Migrate skill data
            for (String skill : new String[]{"mining", "logging", "farming", "fighting", "fishing", "enchanting", "excavation", "repair"}) {
                int level = config.getInt("skills." + skill + ".level", 1);
                int xp = config.getInt("skills." + skill + ".xp", 0);
                int totalEarned = config.getInt("skills." + skill + ".total_earned", 0);
                int highestLevel = config.getInt("skills." + skill + ".highest_level", level);
                
                // Insert into database
                final String skillName = skill;
                final int finalLevel = level;
                final int finalXp = xp;
                
                executeAsyncVoid(conn -> {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO player_skills (player_uuid, skill_name, level, xp) VALUES (?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE level = ?, xp = ?")) {
                        stmt.setString(1, playerUUID.toString());
                        stmt.setString(2, skillName);
                        stmt.setInt(3, finalLevel);
                        stmt.setInt(4, finalXp);
                        stmt.setInt(5, finalLevel);
                        stmt.setInt(6, finalXp);
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Failed to migrate skill data: " + e.getMessage());
                    }
                }).get(); // Wait for completion
            }
            
            // Migrate preferences
            boolean scoreboardEnabled = config.getBoolean("preferences.scoreboard", true);
            boolean showRpgHubOnLogin = config.getBoolean("preferences.show_rpg_hub_on_login", true);
            
            executeAsyncVoid(conn -> {
                try {
                    // Scoreboard setting
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO player_settings (player_uuid, setting_name, setting_value) VALUES (?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE setting_value = ?")) {
                        stmt.setString(1, playerUUID.toString());
                        stmt.setString(2, "scoreboard");
                        stmt.setString(3, String.valueOf(scoreboardEnabled));
                        stmt.setString(4, String.valueOf(scoreboardEnabled));
                        stmt.executeUpdate();
                    }
                    
                    // RPG hub setting
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO player_settings (player_uuid, setting_name, setting_value) VALUES (?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE setting_value = ?")) {
                        stmt.setString(1, playerUUID.toString());
                        stmt.setString(2, "show_rpg_hub_on_login");
                        stmt.setString(3, String.valueOf(showRpgHubOnLogin));
                        stmt.setString(4, String.valueOf(showRpgHubOnLogin));
                        stmt.executeUpdate();
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Failed to migrate player settings: " + e.getMessage());
                }
            }).get(); // Wait for completion
            
            // Migrate passive abilities
            if (config.contains("passiveAbilities")) {
                for (String skill : config.getConfigurationSection("passiveAbilities").getKeys(false)) {
                    for (String passive : config.getStringList("passiveAbilities." + skill)) {
                        final String skillName = skill;
                        final String passiveName = passive;
                        
                        executeAsyncVoid(conn -> {
                            try (PreparedStatement stmt = conn.prepareStatement(
                                    "INSERT INTO player_passives (player_uuid, skill_name, passive_name) VALUES (?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE passive_name = VALUES(passive_name)")) {
                                stmt.setString(1, playerUUID.toString());
                                stmt.setString(2, skillName);
                                stmt.setString(3, passiveName);
                                stmt.executeUpdate();
                            } catch (SQLException e) {
                                plugin.getLogger().severe("Failed to migrate passive ability: " + e.getMessage());
                            }
                        }).get(); // Wait for completion
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to migrate player data: " + e.getMessage());
            return false;
        }
    }
} 