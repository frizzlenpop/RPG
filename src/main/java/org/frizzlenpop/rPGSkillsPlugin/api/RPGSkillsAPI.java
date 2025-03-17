package org.frizzlenpop.rPGSkillsPlugin.api;

import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.data.PartyManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.PassiveSkillManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillTreeManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * API for interacting with the RPG Skills Plugin.
 * This API provides methods to access and modify player skills, XP, and passive abilities.
 */
public class RPGSkillsAPI {
    private static RPGSkillsAPI instance;
    private final RPGSkillsPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final XPManager xpManager;
    private final PassiveSkillManager passiveSkillManager;
    private final SkillTreeManager skillTreeManager;
    private final PartyManager partyManager;

    /**
     * Private constructor to enforce singleton pattern.
     * 
     * @param plugin The RPG Skills Plugin instance
     */
    private RPGSkillsAPI(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.xpManager = plugin.getXpManager();
        this.passiveSkillManager = plugin.getPassiveSkillManager();
        this.skillTreeManager = plugin.getSkillTreeManager();
        this.partyManager = plugin.getPartyManager();
    }

    /**
     * Gets the API instance.
     * 
     * @param plugin The RPG Skills Plugin instance
     * @return The API instance
     */
    public static RPGSkillsAPI getInstance(RPGSkillsPlugin plugin) {
        if (instance == null) {
            instance = new RPGSkillsAPI(plugin);
        }
        return instance;
    }

    /**
     * Gets the skill level for a player.
     * 
     * @param player The player
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @return The skill level
     */
    public int getSkillLevel(Player player, String skill) {
        return playerDataManager.getSkillLevel(player.getUniqueId(), skill);
    }

    /**
     * Gets the skill level for a player by UUID.
     * 
     * @param playerUUID The player's UUID
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @return The skill level
     */
    public int getSkillLevel(UUID playerUUID, String skill) {
        return playerDataManager.getSkillLevel(playerUUID, skill);
    }

    /**
     * Sets the skill level for a player.
     * 
     * @param player The player
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @param level The new skill level
     */
    public void setSkillLevel(Player player, String skill, int level) {
        playerDataManager.setSkillLevel(player.getUniqueId(), skill, level);
    }

    /**
     * Sets the skill level for a player by UUID.
     * 
     * @param playerUUID The player's UUID
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @param level The new skill level
     */
    public void setSkillLevel(UUID playerUUID, String skill, int level) {
        playerDataManager.setSkillLevel(playerUUID, skill, level);
    }

    /**
     * Gets the skill XP for a player.
     * 
     * @param player The player
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @return The skill XP
     */
    public int getSkillXP(Player player, String skill) {
        return playerDataManager.getSkillXP(player.getUniqueId(), skill);
    }

    /**
     * Gets the skill XP for a player by UUID.
     * 
     * @param playerUUID The player's UUID
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @return The skill XP
     */
    public int getSkillXP(UUID playerUUID, String skill) {
        return playerDataManager.getSkillXP(playerUUID, skill);
    }

    /**
     * Sets the skill XP for a player.
     * 
     * @param player The player
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @param xp The new skill XP
     */
    public void setSkillXP(Player player, String skill, int xp) {
        playerDataManager.setSkillXP(player.getUniqueId(), skill, xp);
    }

    /**
     * Sets the skill XP for a player by UUID.
     * 
     * @param playerUUID The player's UUID
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @param xp The new skill XP
     */
    public void setSkillXP(UUID playerUUID, String skill, int xp) {
        playerDataManager.setSkillXP(playerUUID, skill, xp);
    }

    /**
     * Adds XP to a player's skill.
     * This method handles level ups and passive ability unlocks.
     * 
     * @param player The player
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @param xp The amount of XP to add
     */
    public void addXP(Player player, String skill, int xp) {
        xpManager.addXP(player, skill, xp);
    }

    /**
     * Gets the total XP earned for a skill.
     * 
     * @param player The player
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @return The total XP earned
     */
    public int getTotalSkillXPEarned(Player player, String skill) {
        return playerDataManager.getTotalSkillXPEarned(player.getUniqueId(), skill);
    }

    /**
     * Gets the total XP earned for a skill by UUID.
     * 
     * @param playerUUID The player's UUID
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @return The total XP earned
     */
    public int getTotalSkillXPEarned(UUID playerUUID, String skill) {
        return playerDataManager.getTotalSkillXPEarned(playerUUID, skill);
    }

    /**
     * Gets the highest level achieved for a skill.
     * 
     * @param player The player
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @return The highest level achieved
     */
    public int getHighestSkillLevel(Player player, String skill) {
        return playerDataManager.getHighestSkillLevel(player.getUniqueId(), skill);
    }

    /**
     * Gets the highest level achieved for a skill by UUID.
     * 
     * @param playerUUID The player's UUID
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @return The highest level achieved
     */
    public int getHighestSkillLevel(UUID playerUUID, String skill) {
        return playerDataManager.getHighestSkillLevel(playerUUID, skill);
    }

    /**
     * Checks if a player has a specific passive ability.
     * 
     * @param player The player
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @param passive The passive ability name
     * @return True if the player has the passive ability, false otherwise
     */
    public boolean hasPassive(Player player, String skill, String passive) {
        return passiveSkillManager.hasPassive(player.getUniqueId(), skill, passive);
    }

    /**
     * Checks if a player has a specific passive ability by UUID.
     * 
     * @param playerUUID The player's UUID
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @param passive The passive ability name
     * @return True if the player has the passive ability, false otherwise
     */
    public boolean hasPassive(UUID playerUUID, String skill, String passive) {
        return passiveSkillManager.hasPassive(playerUUID, skill, passive);
    }

    /**
     * Gets all passive abilities for a player and skill.
     * 
     * @param player The player
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @return A set of passive ability names
     */
    public Set<String> getPassives(Player player, String skill) {
        return passiveSkillManager.getPassives(player.getUniqueId(), skill);
    }

    /**
     * Gets all passive abilities for a player and skill by UUID.
     * 
     * @param playerUUID The player's UUID
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @return A set of passive ability names
     */
    public Set<String> getPassives(UUID playerUUID, String skill) {
        return passiveSkillManager.getPassives(playerUUID, skill);
    }

    /**
     * Gets all passive abilities for a player.
     * 
     * @param player The player
     * @return A map of skill names to sets of passive ability names
     */
    public Map<String, Set<String>> getAllPassives(Player player) {
        return passiveSkillManager.getAllPassives(player.getUniqueId());
    }

    /**
     * Gets all passive abilities for a player by UUID.
     * 
     * @param playerUUID The player's UUID
     * @return A map of skill names to sets of passive ability names
     */
    public Map<String, Set<String>> getAllPassives(UUID playerUUID) {
        return passiveSkillManager.getAllPassives(playerUUID);
    }

    /**
     * Adds a passive ability to a player.
     * 
     * @param player The player
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @param passive The passive ability name
     */
    public void addPassive(Player player, String skill, String passive) {
        passiveSkillManager.addPassive(player.getUniqueId(), skill, passive);
    }

    /**
     * Adds a passive ability to a player by UUID.
     * 
     * @param playerUUID The player's UUID
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @param passive The passive ability name
     */
    public void addPassive(UUID playerUUID, String skill, String passive) {
        passiveSkillManager.addPassive(playerUUID, skill, passive);
    }

    /**
     * Removes a passive ability from a player.
     * 
     * @param player The player
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @param passive The passive ability name
     */
    public void removePassive(Player player, String skill, String passive) {
        passiveSkillManager.removePassive(player.getUniqueId(), skill, passive);
    }

    /**
     * Removes a passive ability from a player by UUID.
     * 
     * @param playerUUID The player's UUID
     * @param skill The skill name (mining, logging, farming, fighting, fishing, enchanting, excavation, repair)
     * @param passive The passive ability name
     */
    public void removePassive(UUID playerUUID, String skill, String passive) {
        passiveSkillManager.removePassive(playerUUID, skill, passive);
    }

    /**
     * Gets the XP required for a specific level.
     * 
     * @param level The level
     * @return The XP required
     */
    public int getRequiredXP(int level) {
        return xpManager.getRequiredXP(level);
    }

    /**
     * Gets the RPG Skills Plugin instance.
     * 
     * @return The plugin instance
     */
    public RPGSkillsPlugin getPlugin() {
        return plugin;
    }

    // Party System API Methods
    
    /**
     * Checks if a player is in a party.
     * 
     * @param player The player
     * @return True if the player is in a party, false otherwise
     */
    public boolean isInParty(Player player) {
        return partyManager.isInParty(player.getUniqueId());
    }
    
    /**
     * Checks if a player is in a party by UUID.
     * 
     * @param playerUUID The player's UUID
     * @return True if the player is in a party, false otherwise
     */
    public boolean isInParty(UUID playerUUID) {
        return partyManager.isInParty(playerUUID);
    }
    
    /**
     * Checks if a player is the leader of their party.
     * 
     * @param player The player
     * @return True if the player is the party leader, false otherwise
     */
    public boolean isPartyLeader(Player player) {
        return partyManager.isPartyLeader(player.getUniqueId());
    }
    
    /**
     * Checks if a player is the leader of their party by UUID.
     * 
     * @param playerUUID The player's UUID
     * @return True if the player is the party leader, false otherwise
     */
    public boolean isPartyLeader(UUID playerUUID) {
        return partyManager.isPartyLeader(playerUUID);
    }
    
    /**
     * Gets the party leader's UUID for a player.
     * 
     * @param player The player
     * @return The party leader's UUID, or null if the player is not in a party
     */
    public UUID getPartyLeader(Player player) {
        if (!partyManager.isInParty(player.getUniqueId())) {
            return null;
        }
        return partyManager.getPartyLeader(player.getUniqueId());
    }
    
    /**
     * Gets the party leader's UUID for a player by UUID.
     * 
     * @param playerUUID The player's UUID
     * @return The party leader's UUID, or null if the player is not in a party
     */
    public UUID getPartyLeader(UUID playerUUID) {
        if (!partyManager.isInParty(playerUUID)) {
            return null;
        }
        return partyManager.getPartyLeader(playerUUID);
    }
    
    /**
     * Gets all members of a party.
     * 
     * @param partyLeaderUUID The party leader's UUID
     * @return A set of UUIDs of all party members, or an empty set if the party doesn't exist
     */
    public Set<UUID> getPartyMembers(UUID partyLeaderUUID) {
        return partyManager.getPartyMembers(partyLeaderUUID);
    }
    
    /**
     * Gets all online members of a party.
     * 
     * @param partyLeaderUUID The party leader's UUID
     * @return A list of online players in the party, or an empty list if the party doesn't exist or no members are online
     */
    public List<Player> getOnlinePartyMembers(UUID partyLeaderUUID) {
        return partyManager.getOnlinePartyMembers(partyLeaderUUID);
    }
    
    /**
     * Gets the party level.
     * 
     * @param partyLeaderUUID The party leader's UUID
     * @return The party level, or 0 if the party doesn't exist
     */
    public int getPartyLevel(UUID partyLeaderUUID) {
        return partyManager.getPartyLevel(partyLeaderUUID);
    }
    
    /**
     * Gets the party's total shared XP.
     * 
     * @param partyLeaderUUID The party leader's UUID
     * @return The total shared XP, or 0 if the party doesn't exist
     */
    public long getPartyTotalSharedXp(UUID partyLeaderUUID) {
        return partyManager.getPartyTotalSharedXp(partyLeaderUUID);
    }
    
    /**
     * Gets the XP required for the party to reach the next level.
     * 
     * @param partyLeaderUUID The party leader's UUID
     * @return The XP required for the next level, or 0 if the party doesn't exist or is at max level
     */
    public long getXpForNextLevel(UUID partyLeaderUUID) {
        return partyManager.getXpForNextLevel(partyLeaderUUID);
    }
    
    /**
     * Gets the party's XP sharing percentage.
     * 
     * @param partyLeaderUUID The party leader's UUID
     * @return The XP sharing percentage (0.0 to 1.0), or 0 if the party doesn't exist
     */
    public double getXpSharePercent(UUID partyLeaderUUID) {
        return partyManager.getXpSharePercent(partyLeaderUUID);
    }
    
    /**
     * Gets the party's bonus XP percentage based on party level.
     * 
     * @param partyLeaderUUID The party leader's UUID
     * @return The bonus XP percentage (0.0 to 1.0), or 0 if the party doesn't exist
     */
    public double getPartyBonusPercent(UUID partyLeaderUUID) {
        return partyManager.getPartyBonusPercent(partyLeaderUUID);
    }
    
    /**
     * Creates a new party with the specified player as the leader.
     * 
     * @param leader The player who will be the party leader
     * @return True if the party was created successfully, false otherwise (e.g., if the player is already in a party)
     */
    public boolean createParty(Player leader) {
        return partyManager.createParty(leader);
    }
    
    /**
     * Invites a player to join a party.
     * 
     * @param inviter The player sending the invitation (must be the party leader)
     * @param invitee The player being invited
     * @return True if the invitation was sent successfully, false otherwise
     */
    public boolean invitePlayer(Player inviter, Player invitee) {
        return partyManager.invitePlayer(inviter, invitee);
    }
    
    /**
     * Accepts a party invitation.
     * 
     * @param player The player accepting the invitation
     * @return True if the invitation was accepted successfully, false otherwise
     */
    public boolean acceptInvitation(Player player) {
        return partyManager.acceptInvitation(player);
    }
    
    /**
     * Checks if a player has a pending party invitation.
     * 
     * @param player The player
     * @return True if the player has a pending invitation, false otherwise
     */
    public boolean hasInvitation(Player player) {
        return partyManager.hasInvitation(player.getUniqueId());
    }
    
    /**
     * Gets the UUID of the player who invited another player to a party.
     * 
     * @param invitee The player who was invited
     * @return The UUID of the inviter, or null if there is no pending invitation
     */
    public UUID getInviter(Player invitee) {
        return partyManager.getInviter(invitee.getUniqueId());
    }
    
    /**
     * Makes a player leave their current party.
     * 
     * @param player The player
     */
    public void leaveParty(Player player) {
        partyManager.leaveParty(player.getUniqueId());
    }
    
    /**
     * Disbands a party.
     * 
     * @param leader The party leader
     * @return True if the party was disbanded successfully, false otherwise
     */
    public boolean disbandParty(Player leader) {
        return partyManager.disbandParty(leader);
    }
    
    /**
     * Kicks a player from a party.
     * 
     * @param leader The party leader
     * @param target The player to kick
     * @return True if the player was kicked successfully, false otherwise
     */
    public boolean kickPlayer(Player leader, Player target) {
        return partyManager.kickPlayer(leader, target);
    }
    
    /**
     * Sets the XP sharing percentage for a party.
     * 
     * @param leader The party leader
     * @param percent The XP sharing percentage (0.0 to 1.0)
     * @return True if the percentage was set successfully, false otherwise
     */
    public boolean setXpSharePercent(Player leader, double percent) {
        return partyManager.setXpSharePercent(leader, percent);
    }
    
    /**
     * Gets the maximum party size.
     * 
     * @param partyLeaderUUID The party leader's UUID
     * @return The maximum party size
     */
    public int getMaxPartySize(UUID partyLeaderUUID) {
        return partyManager.getMaxPartySize(partyLeaderUUID);
    }
    
    /**
     * Gets formatted information about a party.
     * 
     * @param player A player in the party
     * @return A string with formatted party information, or null if the player is not in a party
     */
    public String getPartyInfo(Player player) {
        if (!partyManager.isInParty(player.getUniqueId())) {
            return null;
        }
        return partyManager.getPartyInfo(player);
    }
} 