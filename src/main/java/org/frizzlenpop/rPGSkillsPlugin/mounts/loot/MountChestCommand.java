package org.frizzlenpop.rPGSkillsPlugin.mounts.loot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.mounts.loot.MountKeyManager.KeyTier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles mount chest-related commands.
 */
public class MountChestCommand implements CommandExecutor, TabCompleter {
    private final RPGSkillsPlugin plugin;
    private final MountKeyManager keyManager;
    private final MountChestGUI chestGUI;
    
    /**
     * Creates a new mount chest command handler
     * 
     * @param plugin The plugin instance
     * @param keyManager The mount key manager
     * @param chestGUI The mount chest GUI
     */
    public MountChestCommand(RPGSkillsPlugin plugin, MountKeyManager keyManager, MountChestGUI chestGUI) {
        this.plugin = plugin;
        this.keyManager = keyManager;
        this.chestGUI = chestGUI;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("mountchest")) {
            if (sender.hasPermission("rpgskills.mount.admin") && args.length > 0 && 
                (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("editrate"))) {
                return handleAdminCommand(sender, args);
            } else {
                return handleCommand(sender, args);
            }
        }
        return false;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("mountchest")) {
            if (sender.hasPermission("rpgskills.mount.admin") && args.length > 0 && 
                (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("editrate"))) {
                return tabCompleteAdmin(sender, args);
            } else {
                return tabComplete(sender, args);
            }
        }
        return new ArrayList<>();
    }
    
    /**
     * Handles mount chest commands
     * 
     * @param sender The command sender
     * @param args Command arguments
     * @return true if the command was handled
     */
    public boolean handleCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        // Check for permissions
        if (!player.hasPermission("rpgskills.mount.chest")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use the mount chest system.");
            return true;
        }
        
        // Handle subcommands
        if (args.length == 0 || args[0].equalsIgnoreCase("chest")) {
            // Open the chest GUI
            chestGUI.openMainGUI(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "keys" -> handleKeysCommand(player);
            case "open" -> handleOpenCommand(player, args);
            default -> {
                player.sendMessage(ChatColor.RED + "Unknown chest subcommand. Use '/mount chest' to open the chest GUI.");
                return true;
            }
        }
        
        return true;
    }
    
    /**
     * Handles the /mount keys command to show keys owned
     * 
     * @param player The player
     */
    private void handleKeysCommand(Player player) {
        keyManager.loadPlayerKeysData(player.getUniqueId());
        
        player.sendMessage(ChatColor.GOLD + "==== Your Mount Keys ====");
        for (KeyTier tier : KeyTier.values()) {
            int count = keyManager.getPlayerKeyCount(player.getUniqueId(), tier);
            player.sendMessage(tier.getColor() + tier.getDisplayName() + ": " + ChatColor.WHITE + count);
        }
    }
    
    /**
     * Handles the /mount open command to open a chest with a key
     * 
     * @param player The player
     * @param args Command arguments
     */
    private void handleOpenCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /mount open <keyType>");
            player.sendMessage(ChatColor.YELLOW + "Available key types: common, uncommon, rare, epic, legendary");
            return;
        }
        
        // Get the key tier
        KeyTier tier = keyManager.getKeyTierByName(args[1]);
        if (tier == null) {
            player.sendMessage(ChatColor.RED + "Invalid key type: " + args[1]);
            player.sendMessage(ChatColor.YELLOW + "Available key types: common, uncommon, rare, epic, legendary");
            return;
        }
        
        // Open the chest with the specified key
        chestGUI.openChest(player, tier);
    }
    
    /**
     * Handles the admin command to give keys to players
     * 
     * @param sender The command sender
     * @param args Command arguments
     * @return true if the command was handled
     */
    public boolean handleAdminCommand(CommandSender sender, String[] args) {
        // Check for admin permission
        if (!sender.hasPermission("rpgskills.mount.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use mount admin commands.");
            return true;
        }
        
        // Handle subcommands
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /mount admin <givekey|editdroprate>");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "givekey" -> handleGiveKeyCommand(sender, args);
            case "editdroprate" -> handleEditDropRateCommand(sender, args);
            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown admin subcommand: " + args[0]);
                return true;
            }
        }
        
        return true;
    }
    
    /**
     * Handles the /mount admin givekey command
     * 
     * @param sender The command sender
     * @param args Command arguments
     */
    private void handleGiveKeyCommand(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /mount admin givekey <player> <keyType> <amount>");
            return;
        }
        
        // Get target player
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return;
        }
        
        // Get key tier
        KeyTier tier = keyManager.getKeyTierByName(args[2]);
        if (tier == null) {
            sender.sendMessage(ChatColor.RED + "Invalid key type: " + args[2]);
            sender.sendMessage(ChatColor.YELLOW + "Available key types: common, uncommon, rare, epic, legendary");
            return;
        }
        
        // Parse amount
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
            if (amount <= 0) {
                sender.sendMessage(ChatColor.RED + "Amount must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount: " + args[3]);
            return;
        }
        
        // Add keys to the player's account
        keyManager.addPlayerKeys(target.getUniqueId(), tier, amount);
        
        // Give physical key items if specified
        if (args.length > 4 && args[4].equalsIgnoreCase("item")) {
            keyManager.giveKeyItem(target, tier, amount);
        }
        
        // Save changes
        keyManager.saveKeyConfig();
        
        // Send messages
        sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " " + tier.getFormattedName() + 
                ChatColor.GREEN + " to " + target.getName());
        
        target.sendMessage(ChatColor.GREEN + "You received " + amount + " " + tier.getFormattedName() + 
                ChatColor.GREEN + " from an admin.");
    }
    
    /**
     * Handles the /mount admin editdroprate command
     * 
     * @param sender The command sender
     * @param args Command arguments
     */
    private void handleEditDropRateCommand(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /mount admin editdroprate <mountName> <keyType> <rate>");
            return;
        }
        
        // Get mount ID
        String mountId = args[1].toLowerCase();
        
        // Get key tier
        KeyTier tier = keyManager.getKeyTierByName(args[2]);
        if (tier == null) {
            sender.sendMessage(ChatColor.RED + "Invalid key type: " + args[2]);
            sender.sendMessage(ChatColor.YELLOW + "Available key types: common, uncommon, rare, epic, legendary");
            return;
        }
        
        // Parse rate
        double rate;
        try {
            rate = Double.parseDouble(args[3]);
            if (rate < 0 || rate > 100) {
                sender.sendMessage(ChatColor.RED + "Rate must be between 0 and 100.");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid rate: " + args[3]);
            return;
        }
        
        // Set drop rate
        keyManager.setMountDropRate(tier, mountId, rate);
        
        // Save changes
        keyManager.saveKeyConfig();
        
        // Send message
        sender.sendMessage(ChatColor.GREEN + "Set drop rate for " + mountId + " from " + 
                tier.getFormattedName() + ChatColor.GREEN + " chests to " + rate + "%");
    }
    
    /**
     * Provides tab completion for mount chest commands
     * 
     * @param sender The command sender
     * @param args Command arguments
     * @return List of tab completions
     */
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return filterCompletions(Arrays.asList("chest", "keys", "open"), args[0]);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("open")) {
            return filterCompletions(
                    Arrays.stream(KeyTier.values())
                            .map(tier -> tier.name().toLowerCase())
                            .collect(Collectors.toList()),
                    args[1]);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Provides tab completion for mount admin chest commands
     * 
     * @param sender The command sender
     * @param args Command arguments
     * @return List of tab completions
     */
    public List<String> tabCompleteAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rpgskills.mount.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return filterCompletions(Arrays.asList("givekey", "editdroprate"), args[0]);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("givekey")) {
                return null; // Return null to get player names
            } else if (args[0].equalsIgnoreCase("editdroprate")) {
                return filterCompletions(Arrays.asList(
                        "phoenix_blaze", "shadow_steed", "crystal_drake", 
                        "storm_charger", "ancient_golem"), args[1]);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("givekey") || args[0].equalsIgnoreCase("editdroprate")) {
                return filterCompletions(
                        Arrays.stream(KeyTier.values())
                                .map(tier -> tier.name().toLowerCase())
                                .collect(Collectors.toList()),
                        args[2]);
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("givekey")) {
                return Arrays.asList("1", "5", "10", "25", "100");
            } else if (args[0].equalsIgnoreCase("editdroprate")) {
                return Arrays.asList("5", "10", "20", "25", "50");
            }
        } else if (args.length == 5 && args[0].equalsIgnoreCase("givekey")) {
            return filterCompletions(Arrays.asList("item"), args[4]);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Filters tab completion options based on current input
     * 
     * @param options Available options
     * @param current Current input
     * @return Filtered list
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