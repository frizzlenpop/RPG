package org.frizzlenpop.rPGSkillsPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for database operations
 */
public class DatabaseCommand implements CommandExecutor, TabCompleter {
    private final RPGSkillsPlugin plugin;
    
    public DatabaseCommand(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rpgskills.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "status":
                showDatabaseStatus(sender);
                break;
            case "migrate":
                migrateData(sender);
                break;
            case "help":
                sendHelpMessage(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /rpgdb help for help.");
                break;
        }
        
        return true;
    }
    
    /**
     * Shows the current database status
     * 
     * @param sender The command sender
     */
    private void showDatabaseStatus(CommandSender sender) {
        if (!plugin.isDatabaseEnabled()) {
            sender.sendMessage(ChatColor.RED + "Database storage is currently disabled.");
            sender.sendMessage(ChatColor.YELLOW + "Enable it in config.yml by setting database.mysql.enabled or database.sqlite.enabled to true.");
            return;
        }
        
        sender.sendMessage(ChatColor.GREEN + "Database storage is currently enabled.");
        sender.sendMessage(ChatColor.YELLOW + "Type: " + 
                (plugin.getConfig().getBoolean("database.mysql.enabled") ? "MySQL" : "SQLite"));
        
        // Show player data stats
        int playersInDatabase = countPlayersInDatabase();
        sender.sendMessage(ChatColor.YELLOW + "Players in database: " + ChatColor.GREEN + playersInDatabase);
    }
    
    /**
     * Counts the number of players in the database
     * 
     * @return The number of players
     */
    private int countPlayersInDatabase() {
        if (!plugin.isDatabaseEnabled()) {
            return 0;
        }
        
        try {
            return plugin.getDatabaseManager().executeAsync(conn -> {
                try (var stmt = conn.prepareStatement("SELECT COUNT(DISTINCT player_uuid) FROM player_skills");
                     var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                } catch (SQLException e) {
                    plugin.getLogger().severe("Error counting players: " + e.getMessage());
                    return 0;
                }
            }).get();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to count players in database: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Migrates data from YAML to database
     * 
     * @param sender The command sender
     */
    private void migrateData(CommandSender sender) {
        if (!plugin.isDatabaseEnabled()) {
            sender.sendMessage(ChatColor.RED + "Database storage is currently disabled. Cannot migrate data.");
            return;
        }
        
        sender.sendMessage(ChatColor.YELLOW + "Starting data migration from YAML to database...");
        sender.sendMessage(ChatColor.YELLOW + "This may take a while depending on the amount of data.");
        
        // Start migration asynchronously
        plugin.getDatabaseManager().migrateFromYAML();
        
        sender.sendMessage(ChatColor.GREEN + "Migration process started. Check console for progress.");
    }
    
    /**
     * Sends the help message
     * 
     * @param sender The command sender
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== RPG Skills Database Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/rpgdb status " + ChatColor.WHITE + "- Shows the current database status");
        sender.sendMessage(ChatColor.YELLOW + "/rpgdb migrate " + ChatColor.WHITE + "- Migrates data from YAML to database");
        sender.sendMessage(ChatColor.YELLOW + "/rpgdb help " + ChatColor.WHITE + "- Shows this help message");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("rpgskills.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("status", "migrate", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 