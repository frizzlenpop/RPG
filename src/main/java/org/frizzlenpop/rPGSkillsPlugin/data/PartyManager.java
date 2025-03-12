package org.frizzlenpop.rPGSkillsPlugin.data;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

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
    
    // Default XP share percentage (30%)
    private static final double DEFAULT_XP_SHARE_PERCENT = 0.30;
    
    // Maximum party size
    private static final int MAX_PARTY_SIZE = 6;
    
    public PartyManager(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
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
            return false;
        }
        
        // Create new party with leader as the only member
        Set<UUID> members = new HashSet<>();
        members.add(leaderUUID);
        parties.put(leaderUUID, members);
        
        // Add leader to playerParties map
        playerParties.put(leaderUUID, leaderUUID);
        
        // Set default XP share percentage
        partyXpSharePercent.put(leaderUUID, DEFAULT_XP_SHARE_PERCENT);
        
        return true;
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
        if (getPartyMembers(inviterUUID).size() >= MAX_PARTY_SIZE) {
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
        
        // Check if player has pending invitation
        if (!pendingInvites.containsKey(playerUUID)) {
            return false;
        }
        
        UUID leaderUUID = pendingInvites.get(playerUUID);
        
        // Check if party still exists
        if (!parties.containsKey(leaderUUID)) {
            pendingInvites.remove(playerUUID);
            return false;
        }
        
        // Check if party is full
        if (getPartyMembers(leaderUUID).size() >= MAX_PARTY_SIZE) {
            pendingInvites.remove(playerUUID);
            return false;
        }
        
        // Add player to party
        parties.get(leaderUUID).add(playerUUID);
        playerParties.put(playerUUID, leaderUUID);
        
        // Remove invitation
        pendingInvites.remove(playerUUID);
        
        return true;
    }
    
    /**
     * Leave the current party
     * @param player The player leaving the party
     * @return true if left successfully, false if player isn't in a party
     */
    public boolean leaveParty(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        // Check if player is in a party
        if (!isInParty(playerUUID)) {
            return false;
        }
        
        UUID leaderUUID = playerParties.get(playerUUID);
        
        // If player is the leader, disband the party
        if (playerUUID.equals(leaderUUID)) {
            return disbandParty(player);
        }
        
        // Remove player from party
        parties.get(leaderUUID).remove(playerUUID);
        playerParties.remove(playerUUID);
        
        return true;
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
            return false;
        }
        
        // Get all party members
        Set<UUID> members = new HashSet<>(parties.get(leaderUUID));
        
        // Remove all members from playerParties map
        for (UUID memberUUID : members) {
            playerParties.remove(memberUUID);
        }
        
        // Remove party
        parties.remove(leaderUUID);
        partyXpSharePercent.remove(leaderUUID);
        
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
            return false;
        }
        
        // Check if target is in leader's party
        if (!playerParties.getOrDefault(targetUUID, UUID.randomUUID()).equals(leaderUUID)) {
            return false;
        }
        
        // Remove target from party
        parties.get(leaderUUID).remove(targetUUID);
        playerParties.remove(targetUUID);
        
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
        
        // Get online party members (excluding the source player)
        List<UUID> onlineMembers = members.stream()
                .filter(uuid -> !uuid.equals(sourcePlayerUUID))
                .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                .collect(Collectors.toList());
        
        // If no other online members, return empty map
        if (onlineMembers.isEmpty()) {
            return sharedXp;
        }
        
        // Calculate XP per member
        int xpPerMember = xpToShare / onlineMembers.size();
        
        // If XP per member is too small, don't share
        if (xpPerMember <= 0) {
            return sharedXp;
        }
        
        // Distribute XP to each member
        for (UUID memberUUID : onlineMembers) {
            sharedXp.put(memberUUID, xpPerMember);
        }
        
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
        
        partyList.append(ChatColor.GOLD).append("Party Members (").append(members.size()).append("/").append(MAX_PARTY_SIZE).append("):\n");
        
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
        partyList.append("\n").append(ChatColor.GRAY).append("(Each member receives a share of XP deducted from what you earn)");
        
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
} 