package org.frizzlenpop.rPGSkillsPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.data.PartyManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Command handler for the /rparty command
 * Allows players to create and manage parties for XP sharing
 */
public class PartyCommand implements CommandExecutor, TabCompleter {
    private final RPGSkillsPlugin plugin;
    private final PartyManager partyManager;
    
    // List of subcommands
    private static final List<String> SUBCOMMANDS = Arrays.asList(
        "create", "invite", "accept", "leave", "disband", "kick", "list", "info", "share"
    );
    
    public PartyCommand(RPGSkillsPlugin plugin, PartyManager partyManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                handleCreate(player);
                break;
                
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /rparty invite <player>");
                    return true;
                }
                handleInvite(player, args[1]);
                break;
                
            case "accept":
                handleAccept(player);
                break;
                
            case "leave":
                handleLeave(player);
                break;
                
            case "disband":
                handleDisband(player);
                break;
                
            case "kick":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /rparty kick <player>");
                    return true;
                }
                handleKick(player, args[1]);
                break;
                
            case "list":
            case "info":
                handleList(player);
                break;
                
            case "share":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /rparty share <percentage>");
                    return true;
                }
                handleShare(player, args[1]);
                break;
                
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /rparty for help.");
                break;
        }
        
        return true;
    }
    
    /**
     * Handle the 'create' subcommand
     */
    private void handleCreate(Player player) {
        if (partyManager.isInParty(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already in a party. Leave your current party first.");
            return;
        }
        
        if (partyManager.createParty(player)) {
            player.sendMessage(ChatColor.GREEN + "Party created! You are now the party leader.");
            player.sendMessage(ChatColor.YELLOW + "Invite players with /rparty invite <player>");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to create party. You might already be in a party.");
        }
    }
    
    /**
     * Handle the 'invite' subcommand
     */
    private void handleInvite(Player player, String targetName) {
        // Check if player is a party leader
        if (!partyManager.isPartyLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only party leaders can invite players.");
            return;
        }
        
        // Find target player
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found or not online.");
            return;
        }
        
        // Don't allow inviting yourself
        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot invite yourself.");
            return;
        }
        
        // Check if target is already in a party
        if (partyManager.isInParty(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + target.getName() + " is already in a party.");
            return;
        }
        
        // Send invitation
        if (partyManager.invitePlayer(player, target)) {
            player.sendMessage(ChatColor.GREEN + "Invitation sent to " + target.getName() + ".");
            
            // Notify target
            target.sendMessage(ChatColor.GREEN + "You have been invited to join " + player.getName() + "'s party.");
            target.sendMessage(ChatColor.YELLOW + "Type /rparty accept to join the party or wait for it to expire.");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to invite player. Your party might be full.");
        }
    }
    
    /**
     * Handle the 'accept' subcommand
     */
    private void handleAccept(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        if (partyManager.isInParty(playerUUID)) {
            player.sendMessage(ChatColor.RED + "You are already in a party.");
            return;
        }
        
        if (!partyManager.hasInvitation(playerUUID)) {
            player.sendMessage(ChatColor.RED + "You don't have any pending party invitations.");
            return;
        }
        
        UUID inviterUUID = partyManager.getInviter(playerUUID);
        Player inviter = Bukkit.getPlayer(inviterUUID);
        String inviterName = inviter != null ? inviter.getName() : "the party leader";
        
        if (partyManager.acceptInvitation(player)) {
            player.sendMessage(ChatColor.GREEN + "You joined " + inviterName + "'s party!");
            
            // Notify party members
            UUID leaderUUID = partyManager.getPartyLeader(playerUUID);
            for (UUID memberUUID : partyManager.getPartyMembers(leaderUUID)) {
                if (!memberUUID.equals(playerUUID)) {
                    Player member = Bukkit.getPlayer(memberUUID);
                    if (member != null) {
                        member.sendMessage(ChatColor.GREEN + player.getName() + " joined the party!");
                    }
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to join party. The party might be full or no longer exists.");
        }
    }
    
    /**
     * Handle the 'leave' subcommand
     */
    private void handleLeave(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        if (!partyManager.isInParty(playerUUID)) {
            player.sendMessage(ChatColor.RED + "You are not in a party.");
            return;
        }
        
        UUID leaderUUID = partyManager.getPartyLeader(playerUUID);
        boolean isLeader = playerUUID.equals(leaderUUID);
        
        if (partyManager.leaveParty(player)) {
            if (isLeader) {
                player.sendMessage(ChatColor.YELLOW + "As the leader, you disbanded the party.");
                
                // Notify former party members
                for (UUID memberUUID : partyManager.getPartyMembers(leaderUUID)) {
                    if (!memberUUID.equals(playerUUID)) {
                        Player member = Bukkit.getPlayer(memberUUID);
                        if (member != null) {
                            member.sendMessage(ChatColor.YELLOW + "The party has been disbanded by the leader.");
                        }
                    }
                }
            } else {
                player.sendMessage(ChatColor.YELLOW + "You left the party.");
                
                // Notify remaining party members
                for (UUID memberUUID : partyManager.getPartyMembers(leaderUUID)) {
                    Player member = Bukkit.getPlayer(memberUUID);
                    if (member != null) {
                        member.sendMessage(ChatColor.YELLOW + player.getName() + " left the party.");
                    }
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to leave party.");
        }
    }
    
    /**
     * Handle the 'disband' subcommand
     */
    private void handleDisband(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        if (!partyManager.isPartyLeader(playerUUID)) {
            player.sendMessage(ChatColor.RED + "Only the party leader can disband the party.");
            return;
        }
        
        // Get all members before disbanding
        List<Player> members = partyManager.getOnlinePartyMembers(playerUUID);
        
        if (partyManager.disbandParty(player)) {
            player.sendMessage(ChatColor.YELLOW + "You disbanded the party.");
            
            // Notify former party members
            for (Player member : members) {
                if (!member.equals(player)) {
                    member.sendMessage(ChatColor.YELLOW + "The party has been disbanded by the leader.");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to disband party.");
        }
    }
    
    /**
     * Handle the 'kick' subcommand
     */
    private void handleKick(Player player, String targetName) {
        UUID playerUUID = player.getUniqueId();
        
        if (!partyManager.isPartyLeader(playerUUID)) {
            player.sendMessage(ChatColor.RED + "Only the party leader can kick members.");
            return;
        }
        
        // Find target player
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found or not online.");
            return;
        }
        
        // Don't allow kicking yourself
        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot kick yourself. Use /rparty disband to close the party.");
            return;
        }
        
        if (partyManager.kickPlayer(player, target)) {
            player.sendMessage(ChatColor.YELLOW + "You kicked " + target.getName() + " from the party.");
            target.sendMessage(ChatColor.RED + "You were kicked from the party by " + player.getName() + ".");
            
            // Notify remaining party members
            for (UUID memberUUID : partyManager.getPartyMembers(playerUUID)) {
                if (!memberUUID.equals(playerUUID) && !memberUUID.equals(target.getUniqueId())) {
                    Player member = Bukkit.getPlayer(memberUUID);
                    if (member != null) {
                        member.sendMessage(ChatColor.YELLOW + target.getName() + " was kicked from the party.");
                    }
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to kick player. They might not be in your party.");
        }
    }
    
    /**
     * Handle the 'list' or 'info' subcommand
     */
    private void handleList(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        if (!partyManager.isInParty(playerUUID)) {
            player.sendMessage(ChatColor.RED + "You are not in a party.");
            return;
        }
        
        UUID leaderUUID = partyManager.getPartyLeader(playerUUID);
        String partyList = partyManager.getFormattedPartyList(leaderUUID);
        
        player.sendMessage(partyList);
    }
    
    /**
     * Handle the 'share' subcommand
     */
    private void handleShare(Player player, String percentStr) {
        UUID playerUUID = player.getUniqueId();
        
        if (!partyManager.isPartyLeader(playerUUID)) {
            player.sendMessage(ChatColor.RED + "Only the party leader can change the XP sharing percentage.");
            return;
        }
        
        try {
            // Parse percentage
            double percent = Double.parseDouble(percentStr);
            
            // Convert to decimal (0-1 range)
            if (percent > 1 && percent <= 100) {
                percent = percent / 100.0;
            }
            
            // Validate range
            if (percent < 0 || percent > 1) {
                player.sendMessage(ChatColor.RED + "Percentage must be between 0 and 100.");
                return;
            }
            
            if (partyManager.setXpSharePercent(player, percent)) {
                int displayPercent = (int)(percent * 100);
                player.sendMessage(ChatColor.GREEN + "XP sharing set to " + displayPercent + "%.");
                
                // Notify party members
                for (UUID memberUUID : partyManager.getPartyMembers(playerUUID)) {
                    if (!memberUUID.equals(playerUUID)) {
                        Player member = Bukkit.getPlayer(memberUUID);
                        if (member != null) {
                            member.sendMessage(ChatColor.GREEN + "Party XP sharing has been set to " 
                                    + displayPercent + "% by the leader.");
                        }
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + "Failed to set XP sharing percentage.");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid percentage format. Use a number between 0 and 100.");
        }
    }
    
    /**
     * Send help message to player
     */
    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "===== RPG Skills Party Commands =====");
        player.sendMessage(ChatColor.YELLOW + "/rparty create " + ChatColor.WHITE + "- Create a new party");
        player.sendMessage(ChatColor.YELLOW + "/rparty invite <player> " + ChatColor.WHITE + "- Invite a player to your party");
        player.sendMessage(ChatColor.YELLOW + "/rparty accept " + ChatColor.WHITE + "- Accept a party invitation");
        player.sendMessage(ChatColor.YELLOW + "/rparty leave " + ChatColor.WHITE + "- Leave your current party");
        player.sendMessage(ChatColor.YELLOW + "/rparty disband " + ChatColor.WHITE + "- Disband your party (leader only)");
        player.sendMessage(ChatColor.YELLOW + "/rparty kick <player> " + ChatColor.WHITE + "- Kick a player from your party (leader only)");
        player.sendMessage(ChatColor.YELLOW + "/rparty list " + ChatColor.WHITE + "- List all members in your party");
        player.sendMessage(ChatColor.YELLOW + "/rparty share <percent> " + ChatColor.WHITE + "- Set XP sharing percentage (leader only)");
        player.sendMessage(ChatColor.GRAY + "When in a party, a percentage of your earned XP is");
        player.sendMessage(ChatColor.GRAY + "deducted and distributed among party members.");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Tab complete subcommands
            String partialSubcommand = args[0].toLowerCase();
            for (String subcommand : SUBCOMMANDS) {
                if (subcommand.startsWith(partialSubcommand)) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            // Tab complete player names for invite and kick
            String subcommand = args[0].toLowerCase();
            if (subcommand.equals("invite") || subcommand.equals("kick")) {
                String partialPlayerName = args[1].toLowerCase();
                List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(partialPlayerName))
                        .collect(Collectors.toList());
                completions.addAll(playerNames);
            } else if (subcommand.equals("share")) {
                // Suggest common share percentages
                String partialPercent = args[1].toLowerCase();
                List<String> percentages = Arrays.asList("10", "20", "30", "40", "50");
                for (String percent : percentages) {
                    if (percent.startsWith(partialPercent)) {
                        completions.add(percent);
                    }
                }
            }
        }
        
        return completions;
    }
} 