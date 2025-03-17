package org.frizzlenpop.rPGSkillsPlugin.data;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.gui.PartyPerksGUI;
import org.frizzlenpop.rPGSkillsPlugin.api.events.PartyCreateEvent;
import org.frizzlenpop.rPGSkillsPlugin.api.events.PartyJoinEvent;
import org.frizzlenpop.rPGSkillsPlugin.api.events.PartyLeaveEvent;
import org.frizzlenpop.rPGSkillsPlugin.api.events.PartyLevelUpEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages player parties for XP sharing functionality
 */
public class PartyManager {
    private final RPGSkillsPlugin plugin;
    
    // Map of party leader UUID to list of member UUIDs (including the leader)
    private final Map<UUID, Set<UUID>> parties = new ConcurrentHashMap<>();
    
    // Map of player UUIDs to their party leader's UUID
    private final Map<UUID, UUID> playerParties = new ConcurrentHashMap<>();
    
    // Pending invitations: invitee UUID -> inviter UUID
    private final Map<UUID, UUID> pendingInvites = new ConcurrentHashMap<>();
    
    // Party XP sharing settings - key is party leader UUID
    private final Map<UUID, Double> partyXpSharePercent = new ConcurrentHashMap<>();
    
    // Party total shared XP - key is party leader UUID
    private final Map<UUID, Long> partyTotalSharedXp = new ConcurrentHashMap<>();
    
    // Party levels - key is party leader UUID
    private final Map<UUID, Integer> partyLevels = new ConcurrentHashMap<>();
    
    // Party configuration file
    private File partyConfigFile;
    private FileConfiguration partyConfig;
    
    // Default XP share percentage (30%)
    private static final double DEFAULT_XP_SHARE_PERCENT = 0.30;
    
    // Maximum party size
    private static final int MAX_PARTY_SIZE = 6;
    
    // XP required for party level up - base value
    private static final int BASE_LEVEL_XP = 5000;
    
    // XP multiplier per level
    private static final double LEVEL_XP_MULTIPLIER = 1.5;
    
    // Maximum party level
    private static final int MAX_PARTY_LEVEL = 50;
    
    // Party bonus XP per level (1%)
    private static final double PARTY_BONUS_PER_LEVEL = 0.01;
    
    private PartyPerksGUI partyPerksGUI;
    
    public PartyManager(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        loadPartyData();
    }
    
    /**
     * Load party data from configuration file
     */
    private void loadPartyData() {
        // Clear existing data
        parties.clear();
        playerParties.clear();
        partyXpSharePercent.clear();
        partyTotalSharedXp.clear();
        partyLevels.clear();
        
        partyConfigFile = new File(plugin.getDataFolder(), "party_data.yml");
        
        if (!partyConfigFile.exists()) {
            try {
                partyConfigFile.createNewFile();
                partyConfig = YamlConfiguration.loadConfiguration(partyConfigFile);
                partyConfig.createSection("parties");
                partyConfig.save(partyConfigFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create party data file: " + e.getMessage());
                return;
            }
        } else {
            partyConfig = YamlConfiguration.loadConfiguration(partyConfigFile);
        }
        
        ConfigurationSection partiesSection = partyConfig.getConfigurationSection("parties");
        
        if (partiesSection == null) {
            return;
        }
        
        for (String leaderUUIDString : partiesSection.getKeys(false)) {
            try {
                UUID leaderUUID = UUID.fromString(leaderUUIDString);
                ConfigurationSection partySection = partiesSection.getConfigurationSection(leaderUUIDString);
                
                if (partySection == null) {
                    continue;
                }
                
                // Load party members
                List<String> memberUUIDStrings = partySection.getStringList("members");
                Set<UUID> memberUUIDs = new HashSet<>();
                
                for (String memberUUIDString : memberUUIDStrings) {
                    try {
                        UUID memberUUID = UUID.fromString(memberUUIDString);
                        memberUUIDs.add(memberUUID);
                        playerParties.put(memberUUID, leaderUUID);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in party members: " + memberUUIDString);
                    }
                }
                
                // Add the party to the parties map
                parties.put(leaderUUID, memberUUIDs);
                
                // Load XP sharing percentage
                double xpSharePercent = partySection.getDouble("xp_share_percent", DEFAULT_XP_SHARE_PERCENT);
                partyXpSharePercent.put(leaderUUID, xpSharePercent);
                
                // Load party level
                int level = partySection.getInt("level", 1);
                partyLevels.put(leaderUUID, level);
                
                // Load total shared XP
                long totalSharedXp = partySection.getLong("total_shared_xp", 0L);
                partyTotalSharedXp.put(leaderUUID, totalSharedXp);
                
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid party leader UUID: " + leaderUUIDString);
            }
        }
        
        plugin.getLogger().info("Loaded " + parties.size() + " parties from storage.");
    }
    
    /**
     * Saves party data to configuration file
     */
    public void savePartyData() {
        partyConfig.set("parties", null);
        
        ConfigurationSection partiesSection = partyConfig.createSection("parties");
        
        for (Map.Entry<UUID, Set<UUID>> entry : parties.entrySet()) {
            UUID leaderUUID = entry.getKey();
            Set<UUID> members = entry.getValue();
            
            String leaderKey = leaderUUID.toString();
            ConfigurationSection partySection = partiesSection.createSection(leaderKey);
            
            // Save party members
            List<String> memberUUIDs = members.stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
            partySection.set("members", memberUUIDs);
            
            // Save XP sharing percentage
            partySection.set("xp_share_percent", 
                    partyXpSharePercent.getOrDefault(leaderUUID, DEFAULT_XP_SHARE_PERCENT));
            
            // Save party level
            partySection.set("level", partyLevels.getOrDefault(leaderUUID, 1));
            
            // Save total shared XP
            partySection.set("total_shared_xp", partyTotalSharedXp.getOrDefault(leaderUUID, 0L));
        }
        
        try {
            partyConfig.save(partyConfigFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save party data: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new party with the specified player as the leader
     * @param leader The player who will be the party leader
     * @return true if party was created, false if player is already in a party
     */
    public boolean createParty(Player leader) {
        UUID leaderUUID = leader.getUniqueId();
        
        // Check if player is already in a party
        if (isInParty(leaderUUID)) {
            leader.sendMessage(ChatColor.RED + "You are already in a party!");
            return false;
        }
        
        // Fire the PartyCreateEvent
        PartyCreateEvent event = new PartyCreateEvent(leader);
        Bukkit.getPluginManager().callEvent(event);
        
        // Check if the event was cancelled
        if (event.isCancelled()) {
            return false;
        }
        
        // Create a new party with the leader as the only member
        Set<UUID> members = new HashSet<>();
        members.add(leaderUUID);
        parties.put(leaderUUID, members);
        
        // Add the leader to the player parties map
        playerParties.put(leaderUUID, leaderUUID);
        
        // Set default XP share percentage
        partyXpSharePercent.put(leaderUUID, DEFAULT_XP_SHARE_PERCENT);
        
        // Initialize total shared XP
        partyTotalSharedXp.put(leaderUUID, 0L);
        
        // Initialize party level
        partyLevels.put(leaderUUID, 1);
        
        // Save party data
        savePartyData();
        
        leader.sendMessage(ChatColor.GREEN + "You have created a new party!");
        
        return true;
    }
    
    /**
     * Get the maximum party size for a specific party, considering perks
     * @param leaderUUID The UUID of the party leader
     * @return The maximum party size
     */
    public int getMaxPartySize(UUID leaderUUID) {
        int baseSize = MAX_PARTY_SIZE;
        
        // Apply party size perk if available
        if (partyPerksGUI != null) {
            baseSize += partyPerksGUI.getExtraPartySize(leaderUUID);
        }
        
        return baseSize;
    }
    
    /**
     * Invites a player to join a party
     * @param inviter The player sending the invitation
     * @param invitee The player being invited
     * @return true if invitation was sent, false otherwise
     */
    public boolean invitePlayer(Player inviter, Player invitee) {
        UUID inviterUUID = inviter.getUniqueId();
        UUID inviteeUUID = invitee.getUniqueId();
        
        // Check if inviter is a party leader
        if (!isPartyLeader(inviterUUID)) {
            return false;
        }
        
        // Check if invitee is already in a party
        if (isInParty(inviteeUUID)) {
            return false;
        }
        
        // Check if party is full
        if (getPartyMembers(inviterUUID).size() >= getMaxPartySize(inviterUUID)) {
            inviter.sendMessage(ChatColor.RED + "Your party is full. Consider upgrading your party size perk.");
            return false;
        }
        
        // Send invitation
        pendingInvites.put(inviteeUUID, inviterUUID);
        
        // Schedule invitation expiration after 60 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Remove invitation if it wasn't accepted
            pendingInvites.remove(inviteeUUID, inviterUUID);
        }, 20 * 60); // 60 seconds
        
        return true;
    }
    
    /**
     * Accept a party invitation
     * @param player The player accepting the invitation
     * @return true if accepted successfully, false otherwise
     */
    public boolean acceptInvitation(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        // Check if player has an invitation
        if (!pendingInvites.containsKey(playerUUID)) {
            player.sendMessage(ChatColor.RED + "You don't have any pending party invitations!");
            return false;
        }
        
        // Get the inviter's UUID
        UUID inviterUUID = pendingInvites.get(playerUUID);
        
        // Check if the inviter is still a party leader
        if (!parties.containsKey(inviterUUID)) {
            player.sendMessage(ChatColor.RED + "The party no longer exists!");
            pendingInvites.remove(playerUUID);
            return false;
        }
        
        // Check if player is already in a party
        if (isInParty(playerUUID)) {
            player.sendMessage(ChatColor.RED + "You are already in a party! Leave your current party first.");
            pendingInvites.remove(playerUUID);
            return false;
        }
        
        // Check if the party is full
        if (parties.get(inviterUUID).size() >= getMaxPartySize(inviterUUID)) {
            player.sendMessage(ChatColor.RED + "The party is full!");
            pendingInvites.remove(playerUUID);
            return false;
        }
        
        // Fire the PartyJoinEvent
        PartyJoinEvent event = new PartyJoinEvent(player, inviterUUID);
        Bukkit.getPluginManager().callEvent(event);
        
        // Check if the event was cancelled
        if (event.isCancelled()) {
            return false;
        }
        
        // Add player to the party
        parties.get(inviterUUID).add(playerUUID);
        
        // Add player to the player parties map
        playerParties.put(playerUUID, inviterUUID);
        
        // Remove the invitation
        pendingInvites.remove(playerUUID);
        
        // Save party data
        savePartyData();
        
        // Get the inviter's name
        String inviterName = Bukkit.getOfflinePlayer(inviterUUID).getName();
        if (inviterName == null) {
            inviterName = "Unknown";
        }
        
        // Notify the player
        player.sendMessage(ChatColor.GREEN + "You have joined " + inviterName + "'s party!");
        
        // Notify all online party members
        for (Player member : getOnlinePartyMembers(inviterUUID)) {
            if (!member.equals(player)) {
                member.sendMessage(ChatColor.GREEN + player.getName() + " has joined the party!");
            }
        }
        
        return true;
    }
    
    /**
     * Makes a player leave their party
     * @param playerUUID The UUID of the player to leave the party
     */
    public void leaveParty(UUID playerUUID) {
        // Check if player is in a party
        if (!isInParty(playerUUID)) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                player.sendMessage(ChatColor.RED + "You are not in a party!");
            }
            return;
        }
        
        // Get the party leader's UUID
        UUID leaderUUID = getPartyLeader(playerUUID);
        
        // Check if the player is the party leader
        boolean isLeader = playerUUID.equals(leaderUUID);
        
        // Fire the PartyLeaveEvent
        PartyLeaveEvent event = new PartyLeaveEvent(playerUUID, leaderUUID, false, false);
        Bukkit.getPluginManager().callEvent(event);
        
        if (isLeader) {
            // If the leader is leaving, find a new leader or disband the party
            Set<UUID> members = new HashSet<>(parties.get(leaderUUID));
            members.remove(playerUUID);
            
            if (members.isEmpty()) {
                // If there are no other members, disband the party
                disbandParty(Bukkit.getPlayer(playerUUID));
            } else {
                // Find a new leader
                UUID newLeaderUUID = findNewLeader(members);
                
                // Transfer leadership
                transferPartyLeadership(leaderUUID, newLeaderUUID);
                
                // Remove the old leader from the party
                parties.get(newLeaderUUID).remove(playerUUID);
                playerParties.remove(playerUUID);
                
                // Save party data
                savePartyData();
                
                // Notify the player
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null) {
                    player.sendMessage(ChatColor.GREEN + "You have left the party!");
                }
                
                // Notify all online party members
                String playerName = Bukkit.getOfflinePlayer(playerUUID).getName();
                if (playerName == null) {
                    playerName = "Unknown";
                }
                
                for (Player member : getOnlinePartyMembers(newLeaderUUID)) {
                    member.sendMessage(ChatColor.YELLOW + playerName + " has left the party!");
                    
                    if (member.getUniqueId().equals(newLeaderUUID)) {
                        member.sendMessage(ChatColor.GREEN + "You are now the party leader!");
                    }
                }
            }
        } else {
            // If a regular member is leaving, just remove them from the party
            parties.get(leaderUUID).remove(playerUUID);
            playerParties.remove(playerUUID);
            
            // Save party data
            savePartyData();
            
            // Notify the player
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                player.sendMessage(ChatColor.GREEN + "You have left the party!");
            }
            
            // Notify all online party members
            String playerName = Bukkit.getOfflinePlayer(playerUUID).getName();
            if (playerName == null) {
                playerName = "Unknown";
            }
            
            for (Player member : getOnlinePartyMembers(leaderUUID)) {
                member.sendMessage(ChatColor.YELLOW + playerName + " has left the party!");
            }
        }
    }
    
    /**
     * Find a suitable new leader from the party members
     * @param members The set of party members
     * @return The UUID of the new leader
     */
    private UUID findNewLeader(Set<UUID> members) {
        // First, try to find an online player
        for (UUID memberUUID : members) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline()) {
                return memberUUID;
            }
        }
        
        // If no online players, just pick the first member
        return members.iterator().next();
    }
    
    /**
     * Transfer party leadership to another player
     * @param oldLeaderUUID The current leader's UUID
     * @param newLeaderUUID The new leader's UUID
     */
    private void transferPartyLeadership(UUID oldLeaderUUID, UUID newLeaderUUID) {
        // Get current party data
        Set<UUID> members = parties.get(oldLeaderUUID);
        double xpSharePercent = partyXpSharePercent.getOrDefault(oldLeaderUUID, DEFAULT_XP_SHARE_PERCENT);
        long totalSharedXp = partyTotalSharedXp.getOrDefault(oldLeaderUUID, 0L);
        int partyLevel = partyLevels.getOrDefault(oldLeaderUUID, 1);
        
        // Create new party entry for the new leader
        parties.put(newLeaderUUID, members);
        partyXpSharePercent.put(newLeaderUUID, xpSharePercent);
        partyTotalSharedXp.put(newLeaderUUID, totalSharedXp);
        partyLevels.put(newLeaderUUID, partyLevel);
        
        // Update all members to point to the new leader
        for (UUID memberUUID : members) {
            playerParties.put(memberUUID, newLeaderUUID);
        }
        
        // Remove old leader's party data
        parties.remove(oldLeaderUUID);
        partyXpSharePercent.remove(oldLeaderUUID);
        partyTotalSharedXp.remove(oldLeaderUUID);
        partyLevels.remove(oldLeaderUUID);
    }
    
    /**
     * Disband a party (can only be done by the leader)
     * @param leader The party leader
     * @return true if disbanded successfully, false otherwise
     */
    public boolean disbandParty(Player leader) {
        UUID leaderUUID = leader.getUniqueId();
        
        // Check if player is a party leader
        if (!isPartyLeader(leaderUUID)) {
            leader.sendMessage(ChatColor.RED + "You are not a party leader!");
            return false;
        }
        
        // Get all party members
        Set<UUID> members = new HashSet<>(parties.get(leaderUUID));
        
        // Fire the PartyLeaveEvent for each member
        for (UUID memberUUID : members) {
            PartyLeaveEvent event = new PartyLeaveEvent(memberUUID, leaderUUID, false, true);
            Bukkit.getPluginManager().callEvent(event);
        }
        
        // Remove the party
        parties.remove(leaderUUID);
        
        // Remove all members from the player parties map
        for (UUID memberUUID : members) {
            playerParties.remove(memberUUID);
        }
        
        // Remove party settings
        partyXpSharePercent.remove(leaderUUID);
        partyTotalSharedXp.remove(leaderUUID);
        partyLevels.remove(leaderUUID);
        
        // Save party data
        savePartyData();
        
        // Notify the leader
        leader.sendMessage(ChatColor.GREEN + "You have disbanded the party!");
        
        // Notify all online party members
        for (UUID memberUUID : members) {
            if (!memberUUID.equals(leaderUUID)) {
                Player member = Bukkit.getPlayer(memberUUID);
                if (member != null) {
                    member.sendMessage(ChatColor.RED + "The party has been disbanded by the leader!");
                }
            }
        }
        
        return true;
    }
    
    /**
     * Kick a player from a party
     * @param leader The party leader
     * @param target The player to kick
     * @return true if kicked successfully, false otherwise
     */
    public boolean kickPlayer(Player leader, Player target) {
        UUID leaderUUID = leader.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        
        // Check if player is a party leader
        if (!isPartyLeader(leaderUUID)) {
            leader.sendMessage(ChatColor.RED + "You are not a party leader!");
            return false;
        }
        
        // Check if target is in the leader's party
        if (!parties.get(leaderUUID).contains(targetUUID)) {
            leader.sendMessage(ChatColor.RED + target.getName() + " is not in your party!");
            return false;
        }
        
        // Check if target is the leader (can't kick yourself)
        if (targetUUID.equals(leaderUUID)) {
            leader.sendMessage(ChatColor.RED + "You cannot kick yourself from the party!");
            return false;
        }
        
        // Fire the PartyLeaveEvent
        PartyLeaveEvent event = new PartyLeaveEvent(targetUUID, leaderUUID, true, false);
        Bukkit.getPluginManager().callEvent(event);
        
        // Remove target from the party
        parties.get(leaderUUID).remove(targetUUID);
        playerParties.remove(targetUUID);
        
        // Save party data
        savePartyData();
        
        // Notify the leader
        leader.sendMessage(ChatColor.GREEN + "You have kicked " + target.getName() + " from the party!");
        
        // Notify the target
        target.sendMessage(ChatColor.RED + "You have been kicked from the party by " + leader.getName() + "!");
        
        // Notify all online party members
        for (Player member : getOnlinePartyMembers(leaderUUID)) {
            if (!member.equals(leader)) {
                member.sendMessage(ChatColor.YELLOW + target.getName() + " has been kicked from the party by " + leader.getName() + "!");
            }
        }
        
        return true;
    }
    
    /**
     * Set the XP share percentage for a party
     * @param leader The party leader
     * @param percent The percentage to share (between 0 and 1)
     * @return true if set successfully, false otherwise
     */
    public boolean setXpSharePercent(Player leader, double percent) {
        UUID leaderUUID = leader.getUniqueId();
        
        // Check if player is a party leader
        if (!isPartyLeader(leaderUUID)) {
            return false;
        }
        
        // Validate percentage (between 0 and 1)
        if (percent < 0 || percent > 1) {
            return false;
        }
        
        // Set XP share percentage
        partyXpSharePercent.put(leaderUUID, percent);
        
        // Save party data
        savePartyData();
        
        return true;
    }
    
    /**
     * Get the XP share percentage for a party
     * @param partyLeaderUUID The party leader's UUID
     * @return The XP share percentage (between 0 and 1)
     */
    public double getXpSharePercent(UUID partyLeaderUUID) {
        return partyXpSharePercent.getOrDefault(partyLeaderUUID, DEFAULT_XP_SHARE_PERCENT);
    }
    
    /**
     * Add shared XP to the party's total
     * @param partyLeaderUUID The party leader's UUID
     * @param xpAmount The amount of XP to add
     * @return true if added successfully, false otherwise
     */
    public boolean addSharedXp(UUID partyLeaderUUID, int xpAmount) {
        if (!parties.containsKey(partyLeaderUUID)) {
            return false;
        }
        
        // Add XP to total
        long currentXp = partyTotalSharedXp.getOrDefault(partyLeaderUUID, 0L);
        long newXp = currentXp + xpAmount;
        partyTotalSharedXp.put(partyLeaderUUID, newXp);
        
        // Check for level up
        checkPartyLevelUp(partyLeaderUUID, currentXp, newXp);
        
        // Save party data
        savePartyData();
        
        return true;
    }
    
    /**
     * Check if the party has leveled up and handle level up if necessary
     * @param partyLeaderUUID The party leader's UUID
     * @param oldXp The old XP total
     * @param newXp The new XP total
     */
    private void checkPartyLevelUp(UUID partyLeaderUUID, long oldXp, long newXp) {
        int currentLevel = partyLevels.getOrDefault(partyLeaderUUID, 1);
        
        // Check if the party is already at max level
        if (currentLevel >= MAX_PARTY_LEVEL) {
            return;
        }
        
        // Calculate XP required for next level
        long xpForNextLevel = getXpForLevel(currentLevel + 1);
        
        // Check if the party has enough XP to level up
        if (newXp >= xpForNextLevel && oldXp < xpForNextLevel) {
            // Level up the party
            int oldLevel = currentLevel;
            int newLevel = currentLevel + 1;
            
            // Update party level
            partyLevels.put(partyLeaderUUID, newLevel);
            
            // Fire the PartyLevelUpEvent
            PartyLevelUpEvent event = new PartyLevelUpEvent(partyLeaderUUID, oldLevel, newLevel);
            Bukkit.getPluginManager().callEvent(event);
            
            // Save party data
            savePartyData();
            
            // Notify party members
            notifyPartyLevelUp(partyLeaderUUID, newLevel);
            
            // Check for additional level ups
            checkPartyLevelUp(partyLeaderUUID, newXp, newXp);
        }
    }
    
    /**
     * Notify party members about a level up
     * @param partyLeaderUUID The party leader's UUID
     * @param newLevel The new party level
     */
    private void notifyPartyLevelUp(UUID partyLeaderUUID, int newLevel) {
        Set<UUID> members = getPartyMembers(partyLeaderUUID);
        
        for (UUID memberUUID : members) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                member.sendMessage(ChatColor.GOLD + "★ " + ChatColor.GREEN + "Your party has reached level " + 
                                  newLevel + "! " + ChatColor.YELLOW + "The party now gets a " + 
                                  String.format("%.0f%%", newLevel * PARTY_BONUS_PER_LEVEL * 100) + 
                                  " XP bonus.");
            }
        }
    }
    
    /**
     * Calculate the XP required for a specific party level
     * @param level The party level
     * @return The XP required for that level
     */
    private long getXpForLevel(int level) {
        // Level 1 is 0 XP
        if (level <= 1) return 0;
        
        // Use a formula that increases XP requirements for each level
        return (long)(BASE_LEVEL_XP * Math.pow(LEVEL_XP_MULTIPLIER, level - 2));
    }
    
    /**
     * Get the party level
     * @param partyLeaderUUID The party leader's UUID
     * @return The party level
     */
    public int getPartyLevel(UUID partyLeaderUUID) {
        return partyLevels.getOrDefault(partyLeaderUUID, 1);
    }
    
    /**
     * Get the party's total accumulated shared XP
     * @param partyLeaderUUID The party leader's UUID
     * @return The total shared XP
     */
    public long getPartyTotalSharedXp(UUID partyLeaderUUID) {
        return partyTotalSharedXp.getOrDefault(partyLeaderUUID, 0L);
    }
    
    /**
     * Get the XP required for the next party level
     * @param partyLeaderUUID The party leader's UUID
     * @return The XP required for the next level, or -1 if at max level
     */
    public long getXpForNextLevel(UUID partyLeaderUUID) {
        int currentLevel = getPartyLevel(partyLeaderUUID);
        
        if (currentLevel >= MAX_PARTY_LEVEL) {
            return -1; // Max level reached
        }
        
        return getXpForLevel(currentLevel + 1);
    }
    
    /**
     * Get the party's bonus XP percentage based on level
     * @param partyLeaderUUID The party leader's UUID
     * @return The bonus XP percentage (0-1)
     */
    public double getPartyBonusPercent(UUID partyLeaderUUID) {
        int level = getPartyLevel(partyLeaderUUID);
        return level * PARTY_BONUS_PER_LEVEL;
    }
    
    /**
     * Check if player is in a party
     * @param playerUUID The player's UUID
     * @return true if in a party, false otherwise
     */
    public boolean isInParty(UUID playerUUID) {
        return playerParties.containsKey(playerUUID);
    }
    
    /**
     * Check if player is a party leader
     * @param playerUUID The player's UUID
     * @return true if party leader, false otherwise
     */
    public boolean isPartyLeader(UUID playerUUID) {
        return parties.containsKey(playerUUID);
    }
    
    /**
     * Get all members of a party
     * @param partyLeaderUUID The party leader's UUID
     * @return Set of member UUIDs, or empty set if party doesn't exist
     */
    public Set<UUID> getPartyMembers(UUID partyLeaderUUID) {
        return parties.getOrDefault(partyLeaderUUID, new HashSet<>());
    }
    
    /**
     * Get the party leader for a player
     * @param playerUUID The player's UUID
     * @return The party leader's UUID, or null if not in a party
     */
    public UUID getPartyLeader(UUID playerUUID) {
        return playerParties.get(playerUUID);
    }
    
    /**
     * Get party members as Player objects
     * @param partyLeaderUUID The party leader's UUID
     * @return List of online party members
     */
    public List<Player> getOnlinePartyMembers(UUID partyLeaderUUID) {
        return getPartyMembers(partyLeaderUUID).stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate the XP to distribute to each party member
     * @param sourcePlayerUUID The player who earned the XP
     * @param xpToShare The total XP to share (already calculated with percentage)
     * @return Map of player UUIDs to XP amounts to distribute
     */
    public Map<UUID, Integer> calculateSharedXp(UUID sourcePlayerUUID, int xpToShare) {
        Map<UUID, Integer> sharedXp = new HashMap<>();
        
        // Check if player is in a party
        if (!isInParty(sourcePlayerUUID)) {
            return sharedXp;
        }
        
        // Get party leader
        UUID leaderUUID = getPartyLeader(sourcePlayerUUID);
        
        // Get all party members
        Set<UUID> members = getPartyMembers(leaderUUID);
        
        // Apply party level bonus to the XP to share
        double bonusPercent = getPartyBonusPercent(leaderUUID);
        
        // Apply XP bonus from party perks if available
        if (partyPerksGUI != null) {
            bonusPercent += partyPerksGUI.getXpBoostPercent(leaderUUID);
        }
        
        int bonusXp = (int) Math.ceil(xpToShare * bonusPercent);
        int totalXpToShare = xpToShare + bonusXp;
        
        // Get party members (excluding the source player)
        List<UUID> otherMembers = new ArrayList<>();
        
        boolean includeOffline = partyPerksGUI != null && partyPerksGUI.hasPerk(leaderUUID, "offline_xp");
        
        for (UUID memberUUID : members) {
            if (!memberUUID.equals(sourcePlayerUUID)) {
                Player member = Bukkit.getPlayer(memberUUID);
                
                // Add the member if they're online, or if the offline_xp perk is active
                if (member != null || includeOffline) {
                    otherMembers.add(memberUUID);
                }
            }
        }
        
        // If no eligible members, return empty map
        if (otherMembers.isEmpty()) {
            return sharedXp;
        }
        
        // Distribute XP to each member - each gets the full share amount
        for (UUID memberUUID : otherMembers) {
            sharedXp.put(memberUUID, totalXpToShare);
        }
        
        // Track the total XP shared for party levels
        addSharedXp(leaderUUID, totalXpToShare * otherMembers.size());
        
        return sharedXp;
    }
    
    /**
     * Get a formatted list of party members
     * @param partyLeaderUUID The party leader's UUID
     * @return Formatted string of party members
     */
    public String getFormattedPartyList(UUID partyLeaderUUID) {
        Set<UUID> members = getPartyMembers(partyLeaderUUID);
        StringBuilder partyList = new StringBuilder();
        
        int partyLevel = getPartyLevel(partyLeaderUUID);
        long totalXp = getPartyTotalSharedXp(partyLeaderUUID);
        long nextLevelXp = getXpForNextLevel(partyLeaderUUID);
        double bonusPercent = getPartyBonusPercent(partyLeaderUUID);
        
        partyList.append(ChatColor.GOLD).append("Party Level: ").append(ChatColor.GREEN).append(partyLevel)
                .append(ChatColor.GOLD).append(" (").append(ChatColor.YELLOW).append("+")
                .append(String.format("%.0f%%", bonusPercent * 100)).append(" XP Bonus").append(ChatColor.GOLD).append(")");
        
        if (nextLevelXp > 0) {
            double progress = (double) totalXp / nextLevelXp;
            partyList.append("\n").append(ChatColor.GRAY).append("Progress: ").append(ChatColor.YELLOW)
                    .append(totalXp).append("/").append(nextLevelXp).append(" XP ")
                    .append(ChatColor.GOLD).append("(").append(String.format("%.1f%%", progress * 100)).append(")");
        } else {
            partyList.append("\n").append(ChatColor.GRAY).append("Max Level Reached! Total XP: ")
                    .append(ChatColor.YELLOW).append(totalXp);
        }
        
        partyList.append("\n").append(ChatColor.GOLD).append("Party Members (").append(members.size())
                .append("/").append(getMaxPartySize(partyLeaderUUID)).append("):\n");
        
        for (UUID memberUUID : members) {
            Player member = Bukkit.getPlayer(memberUUID);
            String name = member != null ? member.getName() : Bukkit.getOfflinePlayer(memberUUID).getName();
            
            if (memberUUID.equals(partyLeaderUUID)) {
                partyList.append(ChatColor.GOLD).append("★ ").append(name).append(" (Leader)");
            } else {
                partyList.append(ChatColor.YELLOW).append("• ").append(name);
            }
            
            if (member == null) {
                partyList.append(" ").append(ChatColor.RED).append("(Offline)");
            }
            
            partyList.append("\n");
        }
        
        double sharePercent = getXpSharePercent(partyLeaderUUID) * 100;
        partyList.append(ChatColor.GRAY).append("XP Sharing: ").append(ChatColor.GREEN).append(String.format("%.0f%%", sharePercent));
        partyList.append("\n").append(ChatColor.GRAY).append("Each member receives ").append(ChatColor.GREEN)
                .append(String.format("%.0f%%", sharePercent)).append(ChatColor.GRAY).append(" of XP earned by other members");
        
        return partyList.toString();
    }
    
    /**
     * Check if a player has a pending invitation
     * @param playerUUID The player's UUID
     * @return true if has invitation, false otherwise
     */
    public boolean hasInvitation(UUID playerUUID) {
        return pendingInvites.containsKey(playerUUID);
    }
    
    /**
     * Get the inviter for a pending invitation
     * @param inviteeUUID The invitee's UUID
     * @return The inviter's UUID, or null if no invitation
     */
    public UUID getInviter(UUID inviteeUUID) {
        return pendingInvites.get(inviteeUUID);
    }
    
    /**
     * Sets the party perks GUI
     * 
     * @param partyPerksGUI The party perks GUI
     */
    public void setPartyPerksGUI(PartyPerksGUI partyPerksGUI) {
        this.partyPerksGUI = partyPerksGUI;
    }
    
    /**
     * Get party information as a formatted string
     * @param player The player to get information for
     * @return Party information string
     */
    public String getPartyInfo(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        // Check if player is in a party
        if (!isInParty(playerUUID)) {
            return ChatColor.RED + "You are not in a party.";
        }
        
        UUID leaderUUID = getPartyLeader(playerUUID);
        Set<UUID> members = getPartyMembers(leaderUUID);
        Player leader = Bukkit.getPlayer(leaderUUID);
        
        StringBuilder info = new StringBuilder();
        info.append(ChatColor.GOLD).append("===== Party Information =====\n")
            .append(ChatColor.YELLOW).append("Leader: ").append(ChatColor.WHITE)
            .append(leader != null ? leader.getName() : "Offline Player").append("\n")
            .append(ChatColor.YELLOW).append("Members (").append(members.size())
            .append("/").append(getMaxPartySize(leaderUUID)).append("):\n");
        
        // ... rest of existing code ...
        
        return info.toString();
    }
} 