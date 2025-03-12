package org.frizzlenpop.rPGSkillsPlugin.gui;

/**
 * ******************************************************************************
 * IMPORTANT NOTE ABOUT "Using legacy scoreboard format" LOG MESSAGES:
 * 
 * If you're seeing log spam with "Using legacy scoreboard format" messages, 
 * you need to:
 * 
 * 1. Make sure you've rebuilt the plugin (mvn clean install)
 * 2. Stop your server completely
 * 3. Replace the plugin JAR file with the newly built version
 * 4. Restart your server
 * 
 * The log messages are likely coming from a previously compiled version of the 
 * plugin that is still running on your server. The FORCE_MODERN_METHOD flag in 
 * this class should prevent the legacy method from being called in new builds.
 * ******************************************************************************
 */

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
// Not importing Bukkit's ScoreboardManager to avoid conflicts
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.data.PartyManager;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Manages player scoreboards for displaying skill levels and party information
 */
public class RPGScoreboardManager {
    private final RPGSkillsPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final XPManager xpManager;
    private final PartyManager partyManager;
    
    // Force the use of modern rendering method (for Minecraft 1.21.4+)
    // This will prevent the legacy method from being called
    private static final boolean FORCE_MODERN_METHOD = true;
    
    // List of skills to display
    private static final List<String> MAIN_SKILLS = Arrays.asList(
            "mining", "logging", "farming", "fishing", "fighting", "enchanting", "excavation", "repair"
    );
    
    // Map to store players who want to see the scoreboard
    private final Map<UUID, Boolean> scoreboardEnabled = new HashMap<>();
    
    // Scoreboard update task ID
    private int taskId = -1;
    
    // Flag to warn about failed render type setting
    private boolean warnedAboutRenderType = false;
    
    public RPGScoreboardManager(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.xpManager = plugin.getXpManager();
        this.partyManager = plugin.getPartyManager();
        
        // Start the scoreboard update task
        startScoreboardTask();
    }
    
    /**
     * Start the task that updates player scoreboards
     */
    private void startScoreboardTask() {
        // Cancel any existing tasks
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        
        // Create a new task that runs every 2 seconds (40 ticks)
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID playerUUID = player.getUniqueId();
                
                // Only update scoreboard if enabled for this player
                if (isScoreboardEnabled(playerUUID)) {
                    updateScoreboard(player);
                }
            }
        }, 20L, 40L);
    }
    
    /**
     * Check if scoreboard is enabled for a player
     * @param playerUUID The player's UUID
     * @return true if enabled, false otherwise
     */
    public boolean isScoreboardEnabled(UUID playerUUID) {
        // Load preference from map or player data
        if (!scoreboardEnabled.containsKey(playerUUID)) {
            boolean enabled = playerDataManager.getScoreboardEnabled(playerUUID);
            scoreboardEnabled.put(playerUUID, enabled);
        }
        
        return scoreboardEnabled.getOrDefault(playerUUID, true);
    }
    
    /**
     * Toggle scoreboard visibility for a player
     * @param playerUUID The player's UUID
     * @return The new state (true = enabled, false = disabled)
     */
    public boolean toggleScoreboard(UUID playerUUID) {
        boolean newState = !isScoreboardEnabled(playerUUID);
        
        // Update map and player data
        scoreboardEnabled.put(playerUUID, newState);
        playerDataManager.setScoreboardEnabled(playerUUID, newState);
        
        // If disabled, remove the scoreboard
        if (!newState) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                // Using fully qualified name for consistency
                org.bukkit.scoreboard.ScoreboardManager bukkitScoreboardManager = Bukkit.getScoreboardManager();
                if (bukkitScoreboardManager != null) {
                    player.setScoreboard(bukkitScoreboardManager.getNewScoreboard());
                }
            }
        }
        
        return newState;
    }
    
    /**
     * Update the scoreboard for a player
     * @param player The player to update
     */
    public void updateScoreboard(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        // Get the Bukkit scoreboard manager (using fully qualified name)
        org.bukkit.scoreboard.ScoreboardManager bukkitScoreboardManager = Bukkit.getScoreboardManager();
        if (bukkitScoreboardManager == null) return;
        
        // Create a new scoreboard
        Scoreboard scoreboard = bukkitScoreboardManager.getNewScoreboard();
        
        // Create the main objective (use a shorter title to save space)
        Objective objective = scoreboard.registerNewObjective("rpgskills", "dummy", ChatColor.YELLOW + "Skills");
        
        // If we're forcing modern method, skip the reflection check
        if (FORCE_MODERN_METHOD) {
            // Use the modern method without checking - Minecraft 1.21.4+ always supports this
            try {
                // Set the RenderType to BLANK (no numbers)
                Class<?> displayFormatClass = Class.forName("org.bukkit.scoreboard.RenderType");
                if (displayFormatClass.isEnum()) {
                    Object blankFormat = Enum.valueOf((Class<Enum>) displayFormatClass, "BLANK");
                    objective.getClass().getMethod("setRenderType", displayFormatClass).invoke(objective, blankFormat);
                }
            } catch (Exception e) {
                // Only log this as a warning once during the plugin's lifecycle
                if (!warnedAboutRenderType) {
                    plugin.getLogger().warning("Failed to set modern render type despite forcing it. This is unexpected for Minecraft 1.21.4+.");
                    warnedAboutRenderType = true;
                }
                // Continue anyway - we'll still display the scoreboard
            }
        } else {
            // Use the original reflection-based check
            boolean useModernMethod = false;
            try {
                Class<?> displayFormatClass = Class.forName("org.bukkit.scoreboard.RenderType");
                if (displayFormatClass.isEnum()) {
                    Object blankFormat = Enum.valueOf((Class<Enum>) displayFormatClass, "BLANK");
                    objective.getClass().getMethod("setRenderType", displayFormatClass).invoke(objective, blankFormat);
                    useModernMethod = true;
                }
            } catch (Exception e) {
                // Silent fail - no logging needed
            }
            
            // Use a different approach only if needed
            if (!useModernMethod) {
                updateScoreboardLegacy(player, scoreboard, objective);
                return;
            }
        }
        
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        // Add scores for each skill
        int scoreValue = MAIN_SKILLS.size() + 5; // Start with a high number and count down
        
        // Add each skill with level and XP percentage on a single line
        for (String skill : MAIN_SKILLS) {
            int level = playerDataManager.getSkillLevel(playerUUID, skill);
            int xp = playerDataManager.getSkillXP(playerUUID, skill);
            int requiredXP = xpManager.getRequiredXP(level);
            
            // Calculate XP percentage
            int xpPercent = (int)(((double)xp / requiredXP) * 100);
            
            // Format the skill name (capitalize first letter)
            String formattedSkill = skill.substring(0, 1).toUpperCase() + skill.substring(1);
            
            // Combined display on a single line
            String skillText = ChatColor.AQUA + formattedSkill + " " + 
                               ChatColor.GREEN + "Lvl " + level + " " + 
                               ChatColor.GRAY + "- XP " + xpPercent + "%";
            
            Score levelScore = objective.getScore(skillText);
            levelScore.setScore(scoreValue--);
        }
        
        // Add party information if the player is in a party
        if (partyManager.isInParty(playerUUID)) {
            // Add a party title
            Score partyTitleScore = objective.getScore(ChatColor.YELLOW + "==== Party ====");
            partyTitleScore.setScore(scoreValue--);
            
            // Get party leader
            UUID leaderUUID = partyManager.getPartyLeader(playerUUID);
            
            // Add the party share percentage
            double sharePercent = partyManager.getXpSharePercent(leaderUUID) * 100;
            Score shareScore = objective.getScore(ChatColor.GRAY + "Share: " + ChatColor.GREEN + String.format("%.0f%%", sharePercent));
            shareScore.setScore(scoreValue--);
            
            // Add party members
            Set<UUID> partyMembers = partyManager.getPartyMembers(leaderUUID);
            for (UUID memberUUID : partyMembers) {
                Player member = Bukkit.getPlayer(memberUUID);
                String memberName = member != null ? member.getName() : Bukkit.getOfflinePlayer(memberUUID).getName();
                
                String displayName;
                if (memberUUID.equals(leaderUUID)) {
                    displayName = ChatColor.GOLD + "★ " + memberName;
                } else if (memberUUID.equals(playerUUID)) {
                    displayName = ChatColor.GREEN + "• " + memberName + " (You)";
                } else {
                    displayName = ChatColor.YELLOW + "• " + memberName;
                }
                
                // Add online/offline status
                if (member == null) {
                    displayName += ChatColor.RED + " (Offline)";
                }
                
                Score memberScore = objective.getScore(displayName);
                memberScore.setScore(scoreValue--);
            }
        }
        
        // Add bottom line
        Score footerScore = objective.getScore(ChatColor.GRAY + "Use /rscoreboard to toggle");
        footerScore.setScore(0);
        
        // Set the player's scoreboard
        player.setScoreboard(scoreboard);
    }
    
    /**
     * Update the scoreboard for a player using the legacy method (for Minecraft < 1.16)
     * This method uses teams to hide the score numbers
     * 
     * Note: If you're seeing messages in the console about "Using legacy scoreboard format",
     * it means this method is being called. Since you're targeting Minecraft 1.21.4+,
     * the modern method should work - check if your plugin needs to be reloaded/recompiled
     * to apply changes.
     */
    private void updateScoreboardLegacy(Player player, Scoreboard scoreboard, Objective objective) {
        UUID playerUUID = player.getUniqueId();
        
        // Log that we're using the legacy method - helpful for debugging
        // This message is logged by something else in the plugin, which is causing console spam
        // plugin.getLogger().info("Using legacy scoreboard format");
        
        // Create a team for each line to hide the scores
        int entryIndex = 0;
        String[] entries = new String[MAIN_SKILLS.size() + 10]; // Allocate enough entries
        
        // Fill entries with unique color codes to make them different
        for (int i = 0; i < entries.length; i++) {
            entries[i] = ChatColor.values()[i % ChatColor.values().length] + "" + ChatColor.values()[(i + 1) % ChatColor.values().length];
        }
        
        // Add each skill with level and XP percentage on a single line
        for (String skill : MAIN_SKILLS) {
            int level = playerDataManager.getSkillLevel(playerUUID, skill);
            int xp = playerDataManager.getSkillXP(playerUUID, skill);
            int requiredXP = xpManager.getRequiredXP(level);
            
            // Calculate XP percentage
            int xpPercent = (int)(((double)xp / requiredXP) * 100);
            
            // Format the skill name (capitalize first letter)
            String formattedSkill = skill.substring(0, 1).toUpperCase() + skill.substring(1);
            
            // Combined display on a single line
            String skillText = ChatColor.AQUA + formattedSkill + " " + 
                               ChatColor.GREEN + "Lvl " + level + " " + 
                               ChatColor.GRAY + "- XP " + xpPercent + "%";
            
            addTeamEntry(scoreboard, "skill_" + skill, entries[entryIndex], skillText, "", entryIndex);
            entryIndex++;
        }
        
        // Add party information if the player is in a party
        if (partyManager.isInParty(playerUUID)) {
            // Add a party title
            addTeamEntry(scoreboard, "party_title", entries[entryIndex], ChatColor.YELLOW + "==== Party ====", "", entryIndex);
            entryIndex++;
            
            // Get party leader
            UUID leaderUUID = partyManager.getPartyLeader(playerUUID);
            
            // Add the party share percentage
            double sharePercent = partyManager.getXpSharePercent(leaderUUID) * 100;
            String shareText = ChatColor.GRAY + "Share: " + ChatColor.GREEN + String.format("%.0f%%", sharePercent);
            addTeamEntry(scoreboard, "party_share", entries[entryIndex], shareText, "", entryIndex);
            entryIndex++;
            
            // Add party members
            Set<UUID> partyMembers = partyManager.getPartyMembers(leaderUUID);
            int memberIndex = 0;
            for (UUID memberUUID : partyMembers) {
                Player member = Bukkit.getPlayer(memberUUID);
                String memberName = member != null ? member.getName() : Bukkit.getOfflinePlayer(memberUUID).getName();
                
                String displayName;
                if (memberUUID.equals(leaderUUID)) {
                    displayName = ChatColor.GOLD + "★ " + memberName;
                } else if (memberUUID.equals(playerUUID)) {
                    displayName = ChatColor.GREEN + "• " + memberName + " (You)";
                } else {
                    displayName = ChatColor.YELLOW + "• " + memberName;
                }
                
                // Add online/offline status
                if (member == null) {
                    displayName += ChatColor.RED + " (Offline)";
                }
                
                addTeamEntry(scoreboard, "party_member_" + memberIndex, entries[entryIndex], displayName, "", entryIndex);
                entryIndex++;
                memberIndex++;
            }
        }
        
        // Add bottom line
        addTeamEntry(scoreboard, "footer", entries[entryIndex], ChatColor.GRAY + "Use /rscoreboard to toggle", "", 0);
        
        // Set the player's scoreboard
        player.setScoreboard(scoreboard);
    }
    
    /**
     * Add a team entry to the scoreboard to hide score numbers
     */
    private void addTeamEntry(Scoreboard scoreboard, String teamName, String entry, String prefix, String suffix, int score) {
        // Register the team if it doesn't exist
        org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        
        // Set the prefix and suffix
        team.setPrefix(prefix);
        team.setSuffix(suffix);
        
        // Add the entry to the team
        team.addEntry(entry);
        
        // Add the score
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (objective != null) {
            objective.getScore(entry).setScore(score);
        }
    }
    
    /**
     * Clean up resources when the plugin is disabled
     */
    public void cleanup() {
        // Cancel the update task
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        
        // Remove scoreboards from all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Using fully qualified name for consistency
            org.bukkit.scoreboard.ScoreboardManager bukkitScoreboardManager = Bukkit.getScoreboardManager();
            if (bukkitScoreboardManager != null) {
                player.setScoreboard(bukkitScoreboardManager.getNewScoreboard());
            }
        }
    }
} 