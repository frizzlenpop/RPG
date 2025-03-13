package org.frizzlenpop.rPGSkillsPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.data.XPBoosterManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for XP boosters
 */
public class XPBoosterCommand implements CommandExecutor, TabCompleter {
    private final RPGSkillsPlugin plugin;
    private final XPBoosterManager boosterManager;
    
    // List of valid skills
    private static final List<String> VALID_SKILLS = Arrays.asList(
        "mining", "logging", "farming", "fishing", "fighting", "enchanting", "excavation", "repair"
    );
    
    /**
     * Creates a new XP booster command handler
     * 
     * @param plugin The plugin instance
     * @param boosterManager The XP booster manager
     */
    public XPBoosterCommand(RPGSkillsPlugin plugin, XPBoosterManager boosterManager) {
        this.plugin = plugin;
        this.boosterManager = boosterManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has permission
        if (!sender.hasPermission("rpgskills.admin.xpbooster")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "apply":
                handleApply(sender, args);
                break;
            case "remove":
                handleRemove(sender, args);
                break;
            case "check":
                handleCheck(sender, args);
                break;
            case "help":
                sendHelpMessage(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                sendHelpMessage(sender);
                break;
        }
        
        return true;
    }
    
    /**
     * Handles the 'apply' subcommand
     * 
     * @param sender The command sender
     * @param args The command arguments
     */
    private void handleApply(CommandSender sender, String[] args) {
        // Syntax: /rpgbooster apply <player> <skill> <multiplier> [duration]
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /rpgbooster apply <player> <skill> <multiplier> [duration]");
            return;
        }
        
        // Get player
        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return;
        }
        
        // Get skill
        String skill = args[2].toLowerCase();
        if (!VALID_SKILLS.contains(skill)) {
            sender.sendMessage(ChatColor.RED + "Invalid skill: " + skill);
            sender.sendMessage(ChatColor.YELLOW + "Valid skills: " + String.join(", ", VALID_SKILLS));
            return;
        }
        
        // Get multiplier
        double multiplier;
        try {
            multiplier = Double.parseDouble(args[3]);
            
            // Convert from percentage to multiplier if needed
            if (multiplier > 0 && multiplier <= 100) {
                multiplier = 1.0 + (multiplier / 100.0);
            }
            
            // Validate range
            if (multiplier < 1.0 || multiplier > 10.0) {
                sender.sendMessage(ChatColor.RED + "Multiplier must be between 1.0 and 10.0 (or 0-900%)");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid multiplier: " + args[3]);
            return;
        }
        
        // Get duration (optional)
        long duration = 0; // Default: permanent
        if (args.length > 4) {
            try {
                duration = parseDuration(args[4]);
                if (duration < 0) {
                    sender.sendMessage(ChatColor.RED + "Duration must be positive!");
                    return;
                }
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Invalid duration format: " + args[4]);
                sender.sendMessage(ChatColor.YELLOW + "Examples: 30s, 5m, 2h, 7d");
                return;
            }
        }
        
        // Apply booster
        boolean success = boosterManager.applyBooster(player, skill, multiplier, duration);
        
        if (success) {
            String durationText = (duration > 0) ? formatDuration(duration) : "permanent";
            String percentBoost = formatMultiplier(multiplier);
            
            sender.sendMessage(ChatColor.GREEN + "Successfully applied " + ChatColor.GOLD + "+" + 
                    percentBoost + " " + capitalizeSkill(skill) + " XP" + ChatColor.GREEN + 
                    " booster to " + player.getName() + "'s item! (" + durationText + ")");
        }
    }
    
    /**
     * Handles the 'remove' subcommand
     * 
     * @param sender The command sender
     * @param args The command arguments
     */
    private void handleRemove(CommandSender sender, String[] args) {
        // Syntax: /rpgbooster remove <player>
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /rpgbooster remove <player>");
            return;
        }
        
        // Get player
        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return;
        }
        
        // Remove booster
        boolean success = boosterManager.removeBooster(player);
        
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Successfully removed XP booster from " + 
                    player.getName() + "'s item!");
        }
    }
    
    /**
     * Handles the 'check' subcommand
     * 
     * @param sender The command sender
     * @param args The command arguments
     */
    private void handleCheck(CommandSender sender, String[] args) {
        // Must be a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return;
        }
        
        Player player = (Player) sender;
        
        // Check booster on held item
        if (!boosterManager.hasBooster(player.getInventory().getItemInMainHand())) {
            player.sendMessage(ChatColor.RED + "The item you're holding doesn't have an XP booster!");
            return;
        }
        
        // Note: Getting details would require additional methods in XPBoosterManager
        // For now, we just tell them to look at the item lore
        player.sendMessage(ChatColor.GREEN + "This item has an XP booster! Check the item lore for details.");
    }
    
    /**
     * Sends the help message to a command sender
     * 
     * @param sender The command sender
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== RPG Skills XP Booster Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/rpgbooster apply <player> <skill> <multiplier> [duration]");
        sender.sendMessage(ChatColor.GRAY + "  Apply an XP booster to a player's held item");
        sender.sendMessage(ChatColor.GRAY + "  - multiplier: 1.5 = 50% more XP");
        sender.sendMessage(ChatColor.GRAY + "  - duration: 30s, 5m, 2h, 7d (default: permanent)");
        sender.sendMessage(ChatColor.YELLOW + "/rpgbooster remove <player>");
        sender.sendMessage(ChatColor.GRAY + "  Remove an XP booster from a player's held item");
        sender.sendMessage(ChatColor.YELLOW + "/rpgbooster check");
        sender.sendMessage(ChatColor.GRAY + "  Check if your held item has an XP booster");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("rpgskills.admin.xpbooster")) {
            return Collections.emptyList();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - subcommands
            completions.addAll(Arrays.asList("apply", "remove", "check", "help"));
        } else if (args.length == 2) {
            // Second argument - player name for apply/remove
            if (args[0].equalsIgnoreCase("apply") || args[0].equalsIgnoreCase("remove")) {
                return null; // Return null to get player names from Bukkit
            }
        } else if (args.length == 3) {
            // Third argument - skill name for apply
            if (args[0].equalsIgnoreCase("apply")) {
                completions.addAll(VALID_SKILLS);
            }
        } else if (args.length == 4) {
            // Fourth argument - multiplier suggestions for apply
            if (args[0].equalsIgnoreCase("apply")) {
                completions.addAll(Arrays.asList("1.5", "2.0", "3.0", "10", "25", "50"));
            }
        } else if (args.length == 5) {
            // Fifth argument - duration suggestions for apply
            if (args[0].equalsIgnoreCase("apply")) {
                completions.addAll(Arrays.asList("30m", "1h", "12h", "1d", "7d", "30d"));
            }
        }
        
        // Filter completions based on current input
        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }
    
    /**
     * Parses a duration string into seconds
     * 
     * @param duration Duration string (e.g., 30s, 5m, 2h, 7d)
     * @return Duration in seconds
     * @throws IllegalArgumentException If the duration format is invalid
     */
    private long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) {
            throw new IllegalArgumentException("Duration cannot be empty");
        }
        
        // Try to parse as raw seconds
        try {
            return Long.parseLong(duration);
        } catch (NumberFormatException e) {
            // Not a simple number, try parsing with units
        }
        
        // Parse with units (s, m, h, d)
        String number = duration.replaceAll("[^0-9]", "");
        String unit = duration.replaceAll("[0-9]", "").toLowerCase();
        
        if (number.isEmpty() || unit.isEmpty()) {
            throw new IllegalArgumentException("Invalid duration format");
        }
        
        long value = Long.parseLong(number);
        
        switch (unit) {
            case "s":
                return value;
            case "m":
                return value * 60;
            case "h":
                return value * 60 * 60;
            case "d":
                return value * 60 * 60 * 24;
            default:
                throw new IllegalArgumentException("Unknown time unit: " + unit);
        }
    }
    
    /**
     * Formats a duration in seconds to a readable string
     * 
     * @param seconds The duration in seconds
     * @return A formatted string (e.g., "2h 30m")
     */
    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        }
        
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " minutes";
        }
        
        long hours = minutes / 60;
        minutes %= 60;
        if (hours < 24) {
            return hours + " hours" + (minutes > 0 ? " " + minutes + " minutes" : "");
        }
        
        long days = hours / 24;
        hours %= 24;
        return days + " days" + (hours > 0 ? " " + hours + " hours" : "");
    }
    
    /**
     * Formats a multiplier as a percentage
     * 
     * @param multiplier The XP multiplier
     * @return A formatted percentage string
     */
    private String formatMultiplier(double multiplier) {
        int percentage = (int) Math.round((multiplier - 1.0) * 100);
        return percentage + "%";
    }
    
    /**
     * Capitalizes a skill name
     * 
     * @param skill The skill name
     * @return The capitalized skill name
     */
    private String capitalizeSkill(String skill) {
        return skill.substring(0, 1).toUpperCase() + skill.substring(1).toLowerCase();
    }
} 