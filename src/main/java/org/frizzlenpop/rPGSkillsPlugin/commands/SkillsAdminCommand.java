package org.frizzlenpop.rPGSkillsPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SkillsAdminCommand implements CommandExecutor, TabCompleter {
    private final PlayerDataManager playerDataManager;
    private final XPManager xpManager;
    
    // List of valid skills
    private static final List<String> VALID_SKILLS = Arrays.asList(
        "mining", "logging", "farming", "fishing", "fighting", "enchanting", "excavation", "repair"
    );

    public SkillsAdminCommand(PlayerDataManager playerDataManager, XPManager xpManager) {
        this.playerDataManager = playerDataManager;
        this.xpManager = xpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rpgskills.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin <player> <skill> <set/add> <level/xp> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        String skill = args[1].toLowerCase();
        // Validate skill name
        if (!VALID_SKILLS.contains(skill)) {
            sender.sendMessage(ChatColor.RED + "Invalid skill: " + skill);
            sender.sendMessage(ChatColor.YELLOW + "Valid skills: " + String.join(", ", VALID_SKILLS));
            return true;
        }
        
        String action = args[2].toLowerCase();
        String type = args[3].toLowerCase();

        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "Please specify an amount!");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[4]);
            
            switch (action) {
                case "set":
                    if (type.equals("level")) {
                        playerDataManager.setSkillLevel(target.getUniqueId(), skill, amount);
                        sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s " + skill + " level to " + amount);
                    } else if (type.equals("xp")) {
                        playerDataManager.setSkillXP(target.getUniqueId(), skill, amount);
                        sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s " + skill + " XP to " + amount);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid type! Use 'level' or 'xp'");
                    }
                    break;
                    
                case "add":
                    if (type.equals("xp")) {
                        xpManager.addXP(target, skill, amount);
                        sender.sendMessage(ChatColor.GREEN + "Added " + amount + " XP to " + target.getName() + "'s " + skill);
                    } else if (type.equals("level")) {
                        // Get current level
                        int currentLevel = playerDataManager.getSkillLevel(target.getUniqueId(), skill);
                        // Add the specified number of levels
                        int newLevel = currentLevel + amount;
                        // Set the new level
                        playerDataManager.setSkillLevel(target.getUniqueId(), skill, newLevel);
                        sender.sendMessage(ChatColor.GREEN + "Added " + amount + " levels to " + target.getName() + "'s " + skill + 
                                           " (now level " + newLevel + ")");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid type! Use 'level' or 'xp'");
                    }
                    break;
                    
                default:
                    sender.sendMessage(ChatColor.RED + "Invalid action! Use 'set' or 'add'");
                    return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Please enter a valid number!");
            return true;
        }

        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("rpgskills.admin")) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - player name
            return null; // Let Bukkit handle player name completion
        } else if (args.length == 2) {
            // Second argument - skill name
            completions.addAll(VALID_SKILLS);
        } else if (args.length == 3) {
            // Third argument - action
            completions.addAll(Arrays.asList("set", "add"));
        } else if (args.length == 4) {
            // Fourth argument - type
            completions.addAll(Arrays.asList("level", "xp"));
        } else if (args.length == 5) {
            // Fifth argument - amount (suggest some common values)
            String type = args[3].toLowerCase();
            if (type.equals("level")) {
                completions.addAll(Arrays.asList("1", "5", "10", "25", "50"));
            } else if (type.equals("xp")) {
                completions.addAll(Arrays.asList("100", "500", "1000", "5000", "10000"));
            }
        }
        
        // Filter completions based on current input
        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }
}