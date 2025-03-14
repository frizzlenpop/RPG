package org.frizzlenpop.rPGSkillsPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillTreeGUI;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillTreeManager;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillTreeNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Command for interacting with the skill tree system
 */
public class SkillTreeCommand implements CommandExecutor, TabCompleter {
    private final RPGSkillsPlugin plugin;
    private final SkillTreeGUI skillTreeGUI;
    private final SkillTreeManager skillTreeManager;
    
    /**
     * Constructor for the skill tree command
     */
    public SkillTreeCommand(RPGSkillsPlugin plugin, SkillTreeGUI skillTreeGUI, SkillTreeManager skillTreeManager) {
        this.plugin = plugin;
        this.skillTreeGUI = skillTreeGUI;
        this.skillTreeManager = skillTreeManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open skill tree GUI
            skillTreeGUI.openSkillTree(player);
            return true;
        }
        
        if (args.length >= 1) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "info":
                    // Show skill tree information
                    showSkillTreeInfo(player);
                    return true;
                case "unlock":
                    // Unlock a skill tree node
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /skilltree unlock <nodeId>");
                        return true;
                    }
                    String nodeId = args[1];
                    unlockNode(player, nodeId);
                    return true;
                case "reset":
                    // Admin command to reset a player's skill tree
                    if (!player.hasPermission("rpgskills.admin")) {
                        player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                        return true;
                    }
                    
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /skilltree reset <player|all>");
                        return true;
                    }
                    
                    String targetArg = args[1].toLowerCase();
                    if (targetArg.equals("all")) {
                        // Reset all players (only if explicitly specified)
                        if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
                            player.sendMessage(ChatColor.RED + "⚠ WARNING: This will reset ALL players' skill trees!");
                            player.sendMessage(ChatColor.RED + "To confirm, use: /skilltree reset all confirm");
                            return true;
                        }
                        
                        int totalReset = 0;
                        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                            int nodesReset = skillTreeManager.resetAllSkills(onlinePlayer);
                            if (nodesReset > 0) {
                                onlinePlayer.sendMessage(ChatColor.RED + "⚠ Your skill tree has been reset by an admin!");
                                totalReset++;
                            }
                        }
                        
                        player.sendMessage(ChatColor.GREEN + "Reset skill trees for " + totalReset + " online players.");
                        return true;
                    } else {
                        // Reset a specific player
                        Player targetPlayer = plugin.getServer().getPlayer(targetArg);
                        if (targetPlayer == null) {
                            player.sendMessage(ChatColor.RED + "Player not found: " + targetArg);
                            return true;
                        }
                        
                        int nodesReset = skillTreeManager.resetAllSkills(targetPlayer);
                        if (nodesReset > 0) {
                            player.sendMessage(ChatColor.GREEN + "Reset " + nodesReset + " skill nodes for " + targetPlayer.getName() + ".");
                            targetPlayer.sendMessage(ChatColor.RED + "⚠ Your skill tree has been reset by an admin!");
                        } else {
                            player.sendMessage(ChatColor.YELLOW + targetPlayer.getName() + " has no skill nodes to reset.");
                        }
                        return true;
                    }
                case "level":
                    // Show player level information
                    showLevelInfo(player);
                    return true;
                case "debug":
                    // Debug command to list all available nodes
                    if (!player.hasPermission("rpgskills.admin")) {
                        player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                        return true;
                    }
                    debugSkillTree(player);
                    return true;
                case "reload":
                    // Reload command to refresh configuration
                    if (!player.hasPermission("rpgskills.admin")) {
                        player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                        return true;
                    }
                    plugin.reloadConfig();
                    player.sendMessage(ChatColor.GREEN + "Configuration reloaded. You may need to restart the server for changes to take effect.");
                    return true;
                default:
                    player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /skilltree for the skill tree GUI.");
                    return true;
            }
        }
        
        return true;
    }
    
    /**
     * Show skill tree information to a player
     */
    private void showSkillTreeInfo(Player player) {
        int playerLevel = skillTreeManager.getPlayerLevel(player);
        int availablePoints = skillTreeManager.getAvailableSkillPoints(player);
        int spentPoints = skillTreeManager.getSpentSkillPoints(player);
        int totalPoints = skillTreeManager.getTotalSkillPoints(player);
        double progress = skillTreeManager.getLevelProgress(player);
        
        player.sendMessage(ChatColor.GOLD + "=== Skill Tree Information ===");
        player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + playerLevel);
        player.sendMessage(ChatColor.YELLOW + "Progress to next level: " + 
                           ChatColor.WHITE + String.format("%.1f%%", progress * 100));
        player.sendMessage(ChatColor.YELLOW + "Available Points: " + ChatColor.WHITE + availablePoints);
        player.sendMessage(ChatColor.YELLOW + "Spent Points: " + ChatColor.WHITE + spentPoints);
        player.sendMessage(ChatColor.YELLOW + "Total Points: " + ChatColor.WHITE + totalPoints);
        player.sendMessage(ChatColor.YELLOW + "Unlocked Nodes: " + ChatColor.WHITE + 
                          skillTreeManager.getPlayerUnlockedNodes(player).size());
        
        player.sendMessage(ChatColor.GOLD + "Use " + ChatColor.GREEN + "/skilltree" + 
                          ChatColor.GOLD + " to open the skill tree GUI.");
    }
    
    /**
     * Show level information to a player
     */
    private void showLevelInfo(Player player) {
        int playerLevel = skillTreeManager.getPlayerLevel(player);
        int xpUntilNext = skillTreeManager.getPlayerLevel().getXPUntilNextLevel(player);
        double progress = skillTreeManager.getLevelProgress(player);
        
        player.sendMessage(ChatColor.GOLD + "=== Player Level Information ===");
        player.sendMessage(ChatColor.YELLOW + "Current Level: " + ChatColor.WHITE + playerLevel);
        player.sendMessage(ChatColor.YELLOW + "XP until next level: " + ChatColor.WHITE + xpUntilNext);
        player.sendMessage(ChatColor.YELLOW + "Progress: " + ChatColor.WHITE + 
                          String.format("%.1f%%", progress * 100));
        
        // Calculate next milestone
        int nextMilestone = (playerLevel / 10 + 1) * 10;
        int levelsUntilMilestone = nextMilestone - playerLevel;
        
        player.sendMessage(ChatColor.YELLOW + "Next milestone (level " + nextMilestone + 
                          "): " + ChatColor.WHITE + levelsUntilMilestone + " levels away");
        player.sendMessage(ChatColor.YELLOW + "Milestone reward: " + 
                          ChatColor.GREEN + "3 skill points");
    }
    
    /**
     * Unlock a node for a player
     */
    private void unlockNode(Player player, String nodeId) {
        if (!skillTreeManager.getAllNodes().containsKey(nodeId)) {
            player.sendMessage(ChatColor.RED + "Node not found: " + nodeId);
            return;
        }
        
        if (skillTreeManager.hasUnlockedNode(player, nodeId)) {
            player.sendMessage(ChatColor.YELLOW + "You have already unlocked this node!");
            return;
        }
        
        if (!skillTreeManager.canUnlockNode(player, nodeId)) {
            player.sendMessage(ChatColor.RED + "You cannot unlock this node yet. Check the requirements!");
            return;
        }
        
        if (skillTreeManager.unlockNode(player, nodeId)) {
            player.sendMessage(ChatColor.GREEN + "Successfully unlocked node: " + 
                              skillTreeManager.getAllNodes().get(nodeId).getName());
        } else {
            player.sendMessage(ChatColor.RED + "Failed to unlock node. Please try again.");
        }
    }
    
    /**
     * Debug command to list all available nodes and diagnose issues
     */
    private void debugSkillTree(Player player) {
        Map<String, SkillTreeNode> allNodes = skillTreeManager.getAllNodes();
        
        if (allNodes.isEmpty()) {
            player.sendMessage(ChatColor.RED + "ERROR: No skill tree nodes found in configuration!");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== Skill Tree Debug ===");
        player.sendMessage(ChatColor.YELLOW + "Total nodes in configuration: " + ChatColor.WHITE + allNodes.size());
        
        // List all node IDs
        player.sendMessage(ChatColor.YELLOW + "All node IDs:");
        StringBuilder nodeList = new StringBuilder();
        for (String nodeId : allNodes.keySet()) {
            nodeList.append(nodeId).append(", ");
        }
        if (nodeList.length() > 2) {
            nodeList.setLength(nodeList.length() - 2); // Remove the trailing comma and space
        }
        player.sendMessage(ChatColor.WHITE + nodeList.toString());
        
        // Check if warrior_strength exists (since it's the base node)
        if (allNodes.containsKey("warrior_strength")) {
            player.sendMessage(ChatColor.GREEN + "Base node 'warrior_strength' exists");
        } else {
            player.sendMessage(ChatColor.RED + "ERROR: Base node 'warrior_strength' not found!");
        }
        
        // Check GUI layout
        player.sendMessage(ChatColor.YELLOW + "Checking GUI layout...");
        for (int pageNum = 0; pageNum < 6; pageNum++) {
            Map<Integer, String> pageLayout = skillTreeGUI.getPageLayout(pageNum);
            if (pageLayout == null) {
                player.sendMessage(ChatColor.RED + "  Page " + pageNum + ": Layout not found!");
                continue;
            }
            
            int validNodes = 0;
            int missingNodes = 0;
            
            for (String nodeId : pageLayout.values()) {
                if (allNodes.containsKey(nodeId)) {
                    validNodes++;
                } else {
                    missingNodes++;
                    player.sendMessage(ChatColor.RED + "  Missing node on page " + pageNum + ": " + nodeId);
                }
            }
            
            player.sendMessage(ChatColor.YELLOW + "  Page " + pageNum + ": " + 
                              ChatColor.GREEN + validNodes + " valid nodes, " +
                              (missingNodes > 0 ? ChatColor.RED : ChatColor.GREEN) + missingNodes + " missing nodes");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = new ArrayList<>(Arrays.asList("info", "level"));
            
            // Add admin commands if player has permission
            if (player.hasPermission("rpgskills.admin")) {
                subCommands.addAll(Arrays.asList("debug", "reload", "reset"));
            }
            
            // Add unlock command if player has unlockable nodes
            if (!skillTreeManager.getUnlockableNodes(player).isEmpty()) {
                subCommands.add("unlock");
            }
            
            return subCommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Second argument - depends on first argument
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("unlock")) {
                // Return list of unlockable node IDs
                return skillTreeManager.getUnlockableNodes(player).stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("reset") && player.hasPermission("rpgskills.admin")) {
                // Return list of online players and "all" option
                List<String> options = new ArrayList<>();
                options.add("all");
                
                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    options.add(onlinePlayer.getName());
                }
                
                return options.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            // Third argument - depends on first and second arguments
            String subCommand = args[0].toLowerCase();
            String secondArg = args[1].toLowerCase();
            
            if (subCommand.equals("reset") && secondArg.equals("all") && player.hasPermission("rpgskills.admin")) {
                // Return "confirm" option
                List<String> options = new ArrayList<>();
                options.add("confirm");
                
                return options.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
} 