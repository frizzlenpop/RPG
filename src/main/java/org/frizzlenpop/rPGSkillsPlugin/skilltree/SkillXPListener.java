package org.frizzlenpop.rPGSkillsPlugin.skilltree;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener for skill XP changes to update the player's overall level
 */
public class SkillXPListener implements Listener {
    private final RPGSkillsPlugin plugin;
    private final SkillTreeManager skillTreeManager;
    
    // Store last level for each player to detect level changes
    private final Map<UUID, Integer> lastKnownLevel = new HashMap<>();
    
    /**
     * Constructor for the skill XP listener
     */
    public SkillXPListener(RPGSkillsPlugin plugin, SkillTreeManager skillTreeManager) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;
        
        // Register this listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // Schedule a task to check for level changes periodically
        // This is needed because we don't have direct events for our custom skill XP
        new BukkitRunnable() {
            @Override
            public void run() {
                checkPlayerLevels();
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }
    
    /**
     * Check if any online players have leveled up
     */
    private void checkPlayerLevels() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerUUID = player.getUniqueId();
            int currentLevel = skillTreeManager.getPlayerLevel(player);
            
            // If we have a record of the player's last level
            if (lastKnownLevel.containsKey(playerUUID)) {
                int lastLevel = lastKnownLevel.get(playerUUID);
                
                // Check if player leveled up
                if (currentLevel > lastLevel) {
                    int levelsGained = currentLevel - lastLevel;
                    playerLevelUp(player, lastLevel, currentLevel, levelsGained);
                }
            }
            
            // Update the last known level
            lastKnownLevel.put(playerUUID, currentLevel);
        }
    }
    
    /**
     * Handle a player leveling up
     */
    private void playerLevelUp(Player player, int oldLevel, int newLevel, int levelsGained) {
        // Calculate skill points gained
        int pointsGained = levelsGained; // 1 point per level
        
        // Add bonus points for reaching milestone levels
        for (int level = oldLevel + 1; level <= newLevel; level++) {
            if (level % 10 == 0) {
                // Milestone level (10, 20, 30, etc.) - adds 2 bonus points
                pointsGained += 2;
            }
        }
        
        // Send level up message
        player.sendMessage(ChatColor.GOLD + "✨ " + ChatColor.GREEN + "LEVEL UP! " + 
                          ChatColor.GOLD + "✨ You are now level " + ChatColor.GREEN + newLevel + 
                          ChatColor.GOLD + "!");
        
        player.sendMessage(ChatColor.YELLOW + "You gained " + ChatColor.GREEN + pointsGained + 
                          ChatColor.YELLOW + " skill points!");
        
        // Check for milestone levels
        for (int level = oldLevel + 1; level <= newLevel; level++) {
            if (level % 10 == 0) {
                // Milestone level message
                player.sendMessage(ChatColor.GOLD + "🏆 " + ChatColor.GREEN + "MILESTONE REACHED! " + 
                                  ChatColor.GOLD + "🏆 Level " + ChatColor.GREEN + level + 
                                  ChatColor.GOLD + " reached!");
                player.sendMessage(ChatColor.YELLOW + "You received " + ChatColor.GREEN + "2 bonus skill points" + 
                                  ChatColor.YELLOW + " for reaching this milestone!");
            }
        }
        
        // Remind the player how to use skill points
        player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.GREEN + "/skilltree" + 
                          ChatColor.YELLOW + " to spend your skill points!");
        
        // Play level up sound
        player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
    }
} 