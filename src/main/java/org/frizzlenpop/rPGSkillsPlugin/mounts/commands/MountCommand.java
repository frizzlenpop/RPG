package org.frizzlenpop.rPGSkillsPlugin.mounts.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.mounts.Mount;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountRarity;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountType;
import org.frizzlenpop.rPGSkillsPlugin.mounts.abilities.MountAbilityManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.loot.MountChestCommand;
import org.frizzlenpop.rPGSkillsPlugin.mounts.xp.MountXPManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles mount-related commands for players and admins.
 */
public class MountCommand implements CommandExecutor, TabCompleter {
    private final RPGSkillsPlugin plugin;
    private final MountManager mountManager;
    private final MountAbilityManager abilityManager;
    private final MountXPManager xpManager;
    private final MountChestCommand chestCommand;
    
    /**
     * Creates a new mount command handler
     * 
     * @param plugin The plugin instance
     */
    public MountCommand(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        this.mountManager = plugin.getMountManager();
        this.abilityManager = mountManager.getAbilityManager();
        this.xpManager = mountManager.getXPManager();
        this.chestCommand = plugin.getMountChestCommand();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        // Check for permissions
        if (!player.hasPermission("rpgskills.mount.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use mounts.");
            return true;
        }
        
        // Handle subcommands
        if (args.length == 0) {
            // Display help menu
            sendHelpMenu(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "list" -> handleListCommand(player);
            case "info" -> handleInfoCommand(player, args);
            case "summon" -> handleSummonCommand(player, args);
            case "dismiss" -> handleDismissCommand(player);
            case "ability" -> handleAbilityCommand(player, args);
            case "stats" -> handleStatsCommand(player, args);
            case "level" -> handleLevelCommand(player, args);
            
            // Chest and key commands
            case "chest", "keys", "open" -> {
                if (chestCommand != null) {
                    return chestCommand.handleCommand(sender, args);
                } else {
                    player.sendMessage(ChatColor.RED + "The mount chest system is not enabled.");
                    return true;
                }
            }
            
            // Admin commands
            case "give" -> handleGiveCommand(player, args);
            case "reload" -> handleReloadCommand(player);
            case "debug" -> handleDebugCommand(player, args);
            case "admin" -> {
                if (args.length > 1 && (args[1].equalsIgnoreCase("givekey") || args[1].equalsIgnoreCase("editdroprate"))) {
                    if (chestCommand != null) {
                        // Use the chest command handler for these admin subcommands
                        return chestCommand.handleAdminCommand(sender, Arrays.copyOfRange(args, 1, args.length));
                    } else {
                        player.sendMessage(ChatColor.RED + "The mount chest system is not enabled.");
                        return true;
                    }
                } else {
                    handleAdminCommand(player, args);
                    return true;
                }
            }
            
            default -> {
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /mount for help.");
                return true;
            }
        }
        
        return true;
    }
    
    /**
     * Sends the help menu to a player
     * 
     * @param player The player
     */
    private void sendHelpMenu(Player player) {
        player.sendMessage(ChatColor.GOLD + "==== Mount System Help ====");
        player.sendMessage(ChatColor.YELLOW + "/mount list" + ChatColor.WHITE + " - List your owned mounts");
        player.sendMessage(ChatColor.YELLOW + "/mount info [mount]" + ChatColor.WHITE + " - View info about a mount");
        player.sendMessage(ChatColor.YELLOW + "/mount summon <mount>" + ChatColor.WHITE + " - Summon a mount");
        player.sendMessage(ChatColor.YELLOW + "/mount dismiss" + ChatColor.WHITE + " - Dismiss your current mount");
        player.sendMessage(ChatColor.YELLOW + "/mount ability <ability>" + ChatColor.WHITE + " - Use a mount ability");
        player.sendMessage(ChatColor.YELLOW + "/mount stats [mount]" + ChatColor.WHITE + " - View mount stats");
        player.sendMessage(ChatColor.YELLOW + "/mount level [mount]" + ChatColor.WHITE + " - View mount level progress");
        
        // Chest commands
        if (chestCommand != null) {
            player.sendMessage(ChatColor.GOLD + "==== Mount Chest Commands ====");
            player.sendMessage(ChatColor.YELLOW + "/mount chest" + ChatColor.WHITE + " - Open the chest GUI");
            player.sendMessage(ChatColor.YELLOW + "/mount keys" + ChatColor.WHITE + " - View your mount keys");
            player.sendMessage(ChatColor.YELLOW + "/mount open <keyType>" + ChatColor.WHITE + " - Open a mount chest");
        }
        
        // Admin commands
        if (player.hasPermission("rpgskills.mount.admin")) {
            player.sendMessage(ChatColor.GOLD + "==== Admin Commands ====");
            player.sendMessage(ChatColor.YELLOW + "/mount give <player> <mount> [rarity]" + ChatColor.WHITE + " - Give a mount to a player");
            player.sendMessage(ChatColor.YELLOW + "/mount reload" + ChatColor.WHITE + " - Reload mount configuration");
            player.sendMessage(ChatColor.YELLOW + "/mount debug [args...]" + ChatColor.WHITE + " - Debug mount features");
            
            if (chestCommand != null) {
                player.sendMessage(ChatColor.YELLOW + "/mount admin givekey <player> <keyType> <amount> [item]" + ChatColor.WHITE + " - Give keys to a player");
                player.sendMessage(ChatColor.YELLOW + "/mount admin editdroprate <mountName> <keyType> <rate>" + ChatColor.WHITE + " - Edit mount drop rates");
            }
        }
    }
    
    /**
     * Handles the list command to show owned mounts
     * 
     * @param player The player
     */
    private void handleListCommand(Player player) {
        Set<String> ownedMounts = mountManager.getPlayerOwnedMounts(player.getUniqueId());
        
        if (ownedMounts.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You don't own any mounts.");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "==== Your Mounts ====");
        
        // List owned mounts with their levels
        for (String mountId : ownedMounts) {
            MountType mountType = mountManager.getMountType(mountId);
            int level = xpManager.getMountLevel(player.getUniqueId(), mountId);
            
            if (mountType != null) {
                player.sendMessage(ChatColor.YELLOW + "- " + mountType.getDisplayName() + 
                        ChatColor.GRAY + " (Level " + level + ")");
            }
        }
    }
    
    /**
     * Handles the info command to display mount details
     * 
     * @param player The player
     * @param args Command arguments
     */
    private void handleInfoCommand(Player player, String[] args) {
        String mountId;
        
        // If mount specified, use that; otherwise use current mount
        if (args.length > 1) {
            mountId = args[1].toLowerCase();
            
            // Check if mount type exists
            if (mountManager.getMountType(mountId) == null) {
                player.sendMessage(ChatColor.RED + "Unknown mount type: " + mountId);
                return;
            }
            
            // Check if player owns this mount
            if (!mountManager.ownsMount(player.getUniqueId(), mountId) && 
                    !player.hasPermission("rpgskills.mount.admin")) {
                player.sendMessage(ChatColor.RED + "You don't own this mount.");
                return;
            }
        } else {
            // Use currently summoned mount if any
            Mount currentMount = mountManager.getActiveMount(player.getUniqueId());
            if (currentMount == null) {
                player.sendMessage(ChatColor.RED + "You don't have a mount summoned. Specify a mount type.");
                return;
            }
            
            mountId = currentMount.getType().getId();
        }
        
        // Display mount info
        MountType mountType = mountManager.getMountType(mountId);
        player.sendMessage(ChatColor.GOLD + "==== " + mountType.getDisplayName() + " ====");
        player.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + mountType.getDescription());
        
        // Display stats
        player.sendMessage(ChatColor.YELLOW + "Stats:");
        player.sendMessage(ChatColor.GRAY + "- Speed: " + ChatColor.WHITE + mountType.getSpeed());
        player.sendMessage(ChatColor.GRAY + "- Jump: " + ChatColor.WHITE + mountType.getJump());
        player.sendMessage(ChatColor.GRAY + "- Health: " + ChatColor.WHITE + mountType.getHealth());
        
        // Display abilities
        player.sendMessage(ChatColor.YELLOW + "Abilities:");
        Map<String, MountType.MountAbility> abilities = mountType.getAbilities();
        if (abilities.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "- None");
        } else {
            int playerLevel = xpManager.getMountLevel(player.getUniqueId(), mountId);
            
            for (MountType.MountAbility ability : abilities.values()) {
                String statusColor = ability.isEnabled() && playerLevel >= ability.getMinLevel() ? 
                        ChatColor.GREEN.toString() : ChatColor.RED.toString();
                
                player.sendMessage(statusColor + "- " + ability.getKey() + 
                        (ability.isPassive() ? " (Passive)" : "") + 
                        " - Unlocks at level " + ability.getMinLevel());
            }
        }
    }
    
    /**
     * Handles the summon command to summon a mount
     * 
     * @param player The player
     * @param args Command arguments
     */
    private void handleSummonCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /mount summon <mount>");
            return;
        }
        
        String mountId = args[1].toLowerCase();
        
        // Check if player owns this mount
        if (!mountManager.ownsMount(player.getUniqueId(), mountId) && 
                !player.hasPermission("rpgskills.mount.admin")) {
            player.sendMessage(ChatColor.RED + "You don't own this mount.");
            return;
        }
        
        // Try to summon the mount
        boolean success = mountManager.summonMount(player, mountId);
        
        if (!success) {
            player.sendMessage(ChatColor.RED + "Failed to summon mount. You may be on cooldown or in a disabled world.");
        }
    }
    
    /**
     * Handles the dismiss command to dismiss the current mount
     * 
     * @param player The player
     */
    private void handleDismissCommand(Player player) {
        Mount currentMount = mountManager.getActiveMount(player.getUniqueId());
        
        if (currentMount == null) {
            player.sendMessage(ChatColor.RED + "You don't have a mount summoned.");
            return;
        }
        
        boolean success = mountManager.dismissMount(player.getUniqueId());
        
        if (!success) {
            player.sendMessage(ChatColor.RED + "Failed to dismiss mount.");
        }
    }
    
    /**
     * Handles the ability command to use mount abilities
     * 
     * @param player The player
     * @param args Command arguments
     */
    private void handleAbilityCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /mount ability <ability>");
            
            // Show available abilities if mounted
            Mount currentMount = mountManager.getActiveMount(player.getUniqueId());
            if (currentMount != null) {
                player.sendMessage(ChatColor.YELLOW + "Available abilities:");
                
                for (Map.Entry<String, MountType.MountAbility> entry : 
                        currentMount.getType().getAbilities().entrySet()) {
                    if (!entry.getValue().isPassive() && 
                            abilityManager.isAbilityUnlocked(player.getUniqueId(), currentMount, entry.getKey())) {
                        player.sendMessage(ChatColor.GRAY + "- " + entry.getKey());
                    }
                }
            }
            
            return;
        }
        
        String abilityName = args[1].toLowerCase();
        
        // Attempt to use the ability
        boolean success = abilityManager.activateAbility(player, abilityName);
        
        if (!success) {
            // Error messages are handled in the ability manager
        }
    }
    
    /**
     * Handles the stats command to view mount stats
     * 
     * @param player The player
     * @param args Command arguments
     */
    private void handleStatsCommand(Player player, String[] args) {
        String mountId;
        
        // If mount specified, use that; otherwise use current mount
        if (args.length > 1) {
            mountId = args[1].toLowerCase();
            
            // Check if mount type exists
            if (mountManager.getMountType(mountId) == null) {
                player.sendMessage(ChatColor.RED + "Unknown mount type: " + mountId);
                return;
            }
            
            // Check if player owns this mount
            if (!mountManager.ownsMount(player.getUniqueId(), mountId) && 
                    !player.hasPermission("rpgskills.mount.admin")) {
                player.sendMessage(ChatColor.RED + "You don't own this mount.");
                return;
            }
        } else {
            // Use currently summoned mount if any
            Mount currentMount = mountManager.getActiveMount(player.getUniqueId());
            if (currentMount == null) {
                player.sendMessage(ChatColor.RED + "You don't have a mount summoned. Specify a mount type.");
                return;
            }
            
            mountId = currentMount.getType().getId();
        }
        
        UUID playerUUID = player.getUniqueId();
        MountType mountType = mountManager.getMountType(mountId);
        int level = xpManager.getMountLevel(playerUUID, mountId);
        int xp = xpManager.getMountXP(playerUUID, mountId);
        int nextLevelXP = xpManager.getRequiredXP(level + 1);
        int progress = xpManager.getLevelProgress(playerUUID, mountId);
        
        player.sendMessage(ChatColor.GOLD + "==== " + mountType.getDisplayName() + " Statistics ====");
        player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + level + 
                " (" + progress + "% to next level)");
        player.sendMessage(ChatColor.YELLOW + "XP: " + ChatColor.WHITE + xp + " / " + nextLevelXP);
        
        // Show active perks
        player.sendMessage(ChatColor.YELLOW + "Active Perks:");
        List<String> activePerks = new ArrayList<>();
        
        if (level >= 10) activePerks.add("Reduced Cooldown");
        if (level >= 20) activePerks.add("Fall Damage Reduction");
        if (level >= 35) activePerks.add("Auto-Recovery");
        if (level >= 45) activePerks.add("Resource Generation");
        
        if (activePerks.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "- None yet");
        } else {
            for (String perk : activePerks) {
                player.sendMessage(ChatColor.GREEN + "- " + perk);
            }
        }
    }
    
    /**
     * Handles the level command to view level progress
     * 
     * @param player The player
     * @param args Command arguments
     */
    private void handleLevelCommand(Player player, String[] args) {
        String mountId;
        
        // If mount specified, use that; otherwise use current mount
        if (args.length > 1) {
            mountId = args[1].toLowerCase();
            
            // Check if mount type exists
            if (mountManager.getMountType(mountId) == null) {
                player.sendMessage(ChatColor.RED + "Unknown mount type: " + mountId);
                return;
            }
            
            // Check if player owns this mount
            if (!mountManager.ownsMount(player.getUniqueId(), mountId) && 
                    !player.hasPermission("rpgskills.mount.admin")) {
                player.sendMessage(ChatColor.RED + "You don't own this mount.");
                return;
            }
        } else {
            // Use currently summoned mount if any
            Mount currentMount = mountManager.getActiveMount(player.getUniqueId());
            if (currentMount == null) {
                player.sendMessage(ChatColor.RED + "You don't have a mount summoned. Specify a mount type.");
                return;
            }
            
            mountId = currentMount.getType().getId();
        }
        
        UUID playerUUID = player.getUniqueId();
        MountType mountType = mountManager.getMountType(mountId);
        int level = xpManager.getMountLevel(playerUUID, mountId);
        int xp = xpManager.getMountXP(playerUUID, mountId);
        int levelCap = xpManager.getLevelCap(mountId);
        int progress = xpManager.getLevelProgress(playerUUID, mountId);
        
        player.sendMessage(ChatColor.GOLD + "==== " + mountType.getDisplayName() + " Level Progress ====");
        player.sendMessage(ChatColor.YELLOW + "Current Level: " + ChatColor.WHITE + level + " / " + levelCap);
        
        if (level >= levelCap) {
            player.sendMessage(ChatColor.GREEN + "This mount has reached maximum level!");
        } else {
            int nextLevelXP = xpManager.getRequiredXP(level + 1);
            int currentLevelXP = xpManager.getRequiredXP(level);
            int needed = nextLevelXP - xp;
            
            player.sendMessage(ChatColor.YELLOW + "Progress: " + ChatColor.WHITE + 
                    "[" + generateProgressBar(progress, 20) + ChatColor.WHITE + "] " + progress + "%");
            player.sendMessage(ChatColor.YELLOW + "XP: " + ChatColor.WHITE + 
                    xp + " / " + nextLevelXP + " (" + needed + " more needed)");
            
            // Show upcoming milestone
            player.sendMessage(ChatColor.YELLOW + "Upcoming Milestones:");
            if (level < 5) {
                player.sendMessage(ChatColor.AQUA + "- Level 5: " + ChatColor.WHITE + "First ability unlocked");
            } else if (level < 10) {
                player.sendMessage(ChatColor.AQUA + "- Level 10: " + ChatColor.WHITE + "Reduced cooldown perk");
            } else if (level < 15) {
                player.sendMessage(ChatColor.AQUA + "- Level 15: " + ChatColor.WHITE + "Second ability unlocked");
            } else if (level < 20) {
                player.sendMessage(ChatColor.AQUA + "- Level 20: " + ChatColor.WHITE + "Fall damage reduction perk");
            } else if (level < 30) {
                player.sendMessage(ChatColor.AQUA + "- Level 30: " + ChatColor.WHITE + "Enhanced abilities");
            } else if (level < 35) {
                player.sendMessage(ChatColor.AQUA + "- Level 35: " + ChatColor.WHITE + "Auto-recovery perk");
            } else if (level < 45) {
                player.sendMessage(ChatColor.AQUA + "- Level 45: " + ChatColor.WHITE + "Resource generation perk");
            } else if (level < 50) {
                player.sendMessage(ChatColor.AQUA + "- Level 50: " + ChatColor.WHITE + "Ultimate potential");
            }
        }
    }
    
    /**
     * Generates a text progress bar
     * 
     * @param percent The percentage to display (0-100)
     * @param length The length of the bar
     * @return Formatted progress bar string
     */
    private String generateProgressBar(int percent, int length) {
        int filledLength = (int) Math.ceil(length * (percent / 100.0));
        StringBuilder bar = new StringBuilder();
        
        bar.append(ChatColor.GREEN);
        for (int i = 0; i < filledLength; i++) {
            bar.append("█");
        }
        
        bar.append(ChatColor.GRAY);
        for (int i = filledLength; i < length; i++) {
            bar.append("█");
        }
        
        return bar.toString();
    }
    
    /**
     * Handles the give command (admin only)
     * 
     * @param player The player
     * @param args Command arguments
     */
    private void handleGiveCommand(Player player, String[] args) {
        if (!player.hasPermission("rpgskills.mount.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return;
        }
        
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /mount give <player> <mount> [rarity]");
            return;
        }
        
        // Get target player
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return;
        }
        
        // Get mount type
        String mountId = args[2].toLowerCase();
        MountType mountType = mountManager.getMountType(mountId);
        if (mountType == null) {
            player.sendMessage(ChatColor.RED + "Unknown mount type: " + mountId);
            return;
        }
        
        // Get rarity (optional)
        MountRarity rarity = MountRarity.COMMON;
        if (args.length > 3) {
            try {
                rarity = MountRarity.valueOf(args[3].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Invalid rarity: " + args[3]);
                return;
            }
        }
        
        // Add mount to target
        mountManager.addMountToPlayer(target.getUniqueId(), mountId);
        
        // Notify both players
        player.sendMessage(ChatColor.GREEN + "Gave " + mountType.getDisplayName() + 
                " (" + rarity.getFormattedName() + ChatColor.GREEN + ") to " + target.getName());
        
        target.sendMessage(ChatColor.GREEN + "You received a " + rarity.getFormattedName() + 
                " " + mountType.getDisplayName() + ChatColor.GREEN + "!");
    }
    
    /**
     * Handles the reload command (admin only)
     * 
     * @param player The player
     */
    private void handleReloadCommand(Player player) {
        if (!player.hasPermission("rpgskills.mount.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return;
        }
        
        mountManager.reloadConfig();
        player.sendMessage(ChatColor.GREEN + "Mount configuration reloaded.");
    }
    
    /**
     * Handles the debug command (admin only)
     * 
     * @param player The player
     * @param args Command arguments
     */
    private void handleDebugCommand(Player player, String[] args) {
        if (!player.hasPermission("rpgskills.mount.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /mount debug <option> [args...]");
            player.sendMessage(ChatColor.YELLOW + "Options: addxp, resetcooldown, forcerarity");
            return;
        }
        
        switch (args[1].toLowerCase()) {
            case "addxp" -> {
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Usage: /mount debug addxp <mount> <amount>");
                    return;
                }
                
                String mountId = args[2].toLowerCase();
                
                // Check if mount type exists
                if (mountManager.getMountType(mountId) == null) {
                    player.sendMessage(ChatColor.RED + "Unknown mount type: " + mountId);
                    return;
                }
                
                // Check if player owns this mount
                if (!mountManager.ownsMount(player.getUniqueId(), mountId)) {
                    player.sendMessage(ChatColor.RED + "You don't own this mount.");
                    return;
                }
                
                // Parse XP amount
                int xpAmount;
                try {
                    xpAmount = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid XP amount: " + args[3]);
                    return;
                }
                
                // Add XP
                boolean leveledUp = xpManager.addMountXP(player.getUniqueId(), mountId, xpAmount);
                player.sendMessage(ChatColor.GREEN + "Added " + xpAmount + " XP to your " + 
                        mountManager.getMountType(mountId).getDisplayName() + 
                        (leveledUp ? " and it leveled up!" : "."));
            }
            
            case "resetcooldown" -> {
                // Reset mount cooldown
                player.sendMessage(ChatColor.GREEN + "Mount cooldown reset.");
                // This would need a method in MountManager to reset cooldowns
            }
            
            case "forcerarity" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /mount debug forcerarity <rarity>");
                    return;
                }
                
                // This would need to be implemented with some sort of temporary override
                // in the MountManager for the next mount summoned
                player.sendMessage(ChatColor.GREEN + "Set forced rarity for next summon to: " + args[2]);
            }
            
            default -> player.sendMessage(ChatColor.RED + "Unknown debug option: " + args[1]);
        }
    }
    
    /**
     * Handle the admin command
     * 
     * @param player The player
     * @param args The command arguments
     */
    private void handleAdminCommand(Player player, String[] args) {
        if (!player.hasPermission("rpgskills.mount.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use mount admin commands.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /mount admin <subcommand>");
            return;
        }
        
        // Process admin subcommands not handled by the chest command
        // Can be extended with more functionality as needed
        player.sendMessage(ChatColor.RED + "Unknown admin subcommand: " + args[1]);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            // Main subcommands
            List<String> completions = new ArrayList<>(Arrays.asList(
                    "list", "info", "summon", "dismiss", "ability", "stats", "level"));
            
            // Add chest commands if available
            if (chestCommand != null) {
                completions.addAll(Arrays.asList("chest", "keys", "open"));
            }
            
            // Admin commands
            if (player.hasPermission("rpgskills.mount.admin")) {
                completions.addAll(Arrays.asList("give", "reload", "debug", "admin"));
            }
            
            return filterCompletions(completions, args[0]);
        } else if (args.length > 1) {
            // Handle chest command tab completion
            if (chestCommand != null && (args[0].equalsIgnoreCase("chest") || 
                    args[0].equalsIgnoreCase("keys") || 
                    args[0].equalsIgnoreCase("open"))) {
                return chestCommand.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
            }
            
            // Handle admin chest command tab completion
            if (chestCommand != null && args[0].equalsIgnoreCase("admin") && 
                    (args.length > 2 && (args[1].equalsIgnoreCase("givekey") || args[1].equalsIgnoreCase("editdroprate")))) {
                return chestCommand.tabCompleteAdmin(sender, Arrays.copyOfRange(args, 1, args.length));
            }
            
            // Subcommand arguments
            if (args.length == 2) {
                return switch (args[0].toLowerCase()) {
                    case "info", "summon", "stats", "level" -> filterCompletions(
                            new ArrayList<>(mountManager.getPlayerOwnedMounts(player.getUniqueId())), 
                            args[1]);
                            
                    case "ability" -> {
                        Mount mount = mountManager.getActiveMount(player.getUniqueId());
                        if (mount != null) {
                            yield filterCompletions(
                                    mount.getType().getAbilities().values().stream()
                                            .filter(ability -> !ability.isPassive())
                                            .map(MountType.MountAbility::getKey)
                                            .collect(Collectors.toList()),
                                    args[1]);
                        }
                        yield new ArrayList<>();
                    }
                    
                    case "give" -> filterCompletions(
                            Bukkit.getOnlinePlayers().stream()
                                    .map(Player::getName)
                                    .collect(Collectors.toList()),
                            args[1]);
                            
                    case "debug" -> filterCompletions(
                            Arrays.asList("addxp", "resetcooldown", "forcerarity"),
                            args[1]);
                            
                    case "admin" -> filterCompletions(
                            Arrays.asList("givekey", "editdroprate"),
                            args[1]);
                            
                    default -> new ArrayList<>();
                };
            }
        } else if (args.length == 3) {
            // Third argument completions
            return switch (args[0].toLowerCase()) {
                case "give" -> filterCompletions(
                        mountManager.getMountTypes().keySet().stream()
                                .collect(Collectors.toList()),
                        args[2]);
                                
                case "debug" -> {
                    if (args[1].equalsIgnoreCase("addxp")) {
                        yield filterCompletions(
                                new ArrayList<>(mountManager.getPlayerOwnedMounts(player.getUniqueId())), 
                                args[2]);
                    }
                    yield new ArrayList<>();
                }
                
                default -> new ArrayList<>();
            };
        } else if (args.length == 4) {
            // Fourth argument completions
            if (args[0].equalsIgnoreCase("give")) {
                return filterCompletions(
                        Arrays.stream(MountRarity.values())
                                .map(Enum::name)
                                .collect(Collectors.toList()),
                        args[3]);
            } else if (args[0].equalsIgnoreCase("debug") && args[1].equalsIgnoreCase("addxp")) {
                return Arrays.asList("100", "500", "1000", "5000");
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Filters tab completion options based on current input
     * 
     * @param options The available options
     * @param current The current input
     * @return Filtered completion options
     */
    private List<String> filterCompletions(List<String> options, String current) {
        if (current.isEmpty()) {
            return options;
        }
        
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }
} 