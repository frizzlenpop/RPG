package org.frizzlenpop.rPGSkillsPlugin.mounts.xp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.Mount;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountSummonedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages mount XP, leveling, and progression.
 */
public class MountXPManager implements Listener {
    private final RPGSkillsPlugin plugin;
    private final MountManager mountManager;
    private final PlayerDataManager playerDataManager;
    private final Map<UUID, Map<String, Long>> lastPassiveXPGain = new ConcurrentHashMap<>();
    private final Map<String, Integer> levelCapCache = new HashMap<>();
    
    /**
     * Creates a new Mount XP Manager
     * 
     * @param plugin The plugin instance
     * @param mountManager The mount manager
     */
    public MountXPManager(RPGSkillsPlugin plugin, MountManager mountManager) {
        this.plugin = plugin;
        this.mountManager = mountManager;
        this.playerDataManager = plugin.getPlayerDataManager();
    }
    
    /**
     * Processes passive XP gain for a mount
     * 
     * @param player The player riding the mount
     * @param mount The mount
     */
    public void processPassiveXPGain(Player player, Mount mount) {
        UUID playerUUID = player.getUniqueId();
        String mountId = mount.getType().getId();
        
        // Check if cooldown has elapsed
        long currentTime = System.currentTimeMillis();
        Map<String, Long> playerLastGain = lastPassiveXPGain.computeIfAbsent(playerUUID, k -> new HashMap<>());
        long lastGain = playerLastGain.getOrDefault(mountId, 0L);
        
        // Only gain passive XP once per minute
        if (currentTime - lastGain < 60000) {
            return;
        }
        
        // Get passive gain rate
        int xpGain = getPassiveXPRate();
        
        // Apply passive XP gain
        addMountXP(playerUUID, mountId, xpGain);
        
        // Update last gain time
        playerLastGain.put(mountId, currentTime);
    }
    
    /**
     * Gets the passive XP gain rate from config
     * 
     * @return XP gained per minute
     */
    private int getPassiveXPRate() {
        return mountManager.getMountConfig().getInt("mount_leveling.xp_sources.passive_gain.rate", 10);
    }
    
    /**
     * Adds XP to a player's mount
     * 
     * @param playerUUID The player's UUID
     * @param mountId The mount's ID
     * @param xp The amount of XP to add
     * @return true if the mount leveled up
     */
    public boolean addMountXP(UUID playerUUID, String mountId, int xp) {
        FileConfiguration playerData = playerDataManager.getPlayerData(playerUUID);
        if (playerData == null) return false;
        
        // Get current XP and level
        int currentXP = playerData.getInt("mounts.data." + mountId + ".xp", 0);
        int currentLevel = playerData.getInt("mounts.data." + mountId + ".level", 1);
        int newXP = currentXP + xp;
        
        // Check for level up
        boolean leveledUp = false;
        int levelCap = getLevelCap(mountId);
        
        // Only process level ups if below cap
        if (currentLevel < levelCap) {
            int requiredXP = getRequiredXP(currentLevel + 1);
            
            // Level up if XP is sufficient
            while (newXP >= requiredXP && currentLevel < levelCap) {
                currentLevel++;
                leveledUp = true;
                
                // Notify player if online
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    // Get mount name
                    String mountName = mountManager.getMountType(mountId).getDisplayName();
                    player.sendMessage(ChatColor.GREEN + "Your " + mountName + " has reached level " + currentLevel + "!");
                    
                    // Check for milestone levels and notify of new abilities/perks
                    checkMilestoneLevelRewards(player, mountId, currentLevel);
                }
                
                // Check if we need another level
                if (currentLevel < levelCap) {
                    requiredXP = getRequiredXP(currentLevel + 1);
                }
            }
        }
        
        // Save new values
        playerData.set("mounts.data." + mountId + ".xp", newXP);
        playerData.set("mounts.data." + mountId + ".level", currentLevel);
        playerDataManager.savePlayerData(playerUUID, playerData);
        
        return leveledUp;
    }
    
    /**
     * Gets the amount of XP required for a level
     * 
     * @param level The level to check
     * @return The required XP amount
     */
    public int getRequiredXP(int level) {
        if (level <= 1) return 0;
        
        // Get base formula values
        int baseRequirement = mountManager.getMountConfig().getInt(
                "mount_leveling.level_scaling.base_requirement", 100);
        double scalingFactor = mountManager.getMountConfig().getDouble(
                "mount_leveling.level_scaling.scaling_factor", 1.2);
        
        // Calculate required XP
        return (int) (baseRequirement * Math.pow(scalingFactor, level - 2));
    }
    
    /**
     * Gets the level cap for mounts
     * 
     * @param mountId The mount ID
     * @return The level cap
     */
    public int getLevelCap(String mountId) {
        // Check cache first
        if (levelCapCache.containsKey(mountId)) {
            return levelCapCache.get(mountId);
        }
        
        // Get from config or use default
        int cap = mountManager.getMountConfig().getInt("mount_leveling.level_cap", 50);
        
        // Cache and return
        levelCapCache.put(mountId, cap);
        return cap;
    }
    
    /**
     * Gets a mount's level
     * 
     * @param playerUUID The player's UUID
     * @param mountId The mount's ID
     * @return The mount's level
     */
    public int getMountLevel(UUID playerUUID, String mountId) {
        FileConfiguration playerData = playerDataManager.getPlayerData(playerUUID);
        return playerData != null ? playerData.getInt("mounts.data." + mountId + ".level", 1) : 1;
    }
    
    /**
     * Gets a mount's XP
     * 
     * @param playerUUID The player's UUID
     * @param mountId The mount's ID
     * @return The mount's XP
     */
    public int getMountXP(UUID playerUUID, String mountId) {
        FileConfiguration playerData = playerDataManager.getPlayerData(playerUUID);
        return playerData != null ? playerData.getInt("mounts.data." + mountId + ".xp", 0) : 0;
    }
    
    /**
     * Gets the progress to the next level as a percentage
     * 
     * @param playerUUID The player's UUID
     * @param mountId The mount's ID
     * @return Percentage to next level (0-100)
     */
    public int getLevelProgress(UUID playerUUID, String mountId) {
        int currentLevel = getMountLevel(playerUUID, mountId);
        if (currentLevel >= getLevelCap(mountId)) {
            return 100;
        }
        
        int currentXP = getMountXP(playerUUID, mountId);
        int requiredXP = getRequiredXP(currentLevel + 1);
        int previousLevelXP = getRequiredXP(currentLevel);
        
        if (requiredXP == previousLevelXP) return 0;
        
        int levelXP = currentXP - previousLevelXP;
        int levelRangeXP = requiredXP - previousLevelXP;
        
        return (int) (((double) levelXP / levelRangeXP) * 100);
    }
    
    /**
     * Checks for milestone level rewards and handles them
     * 
     * @param player The player
     * @param mountId The mount's ID
     * @param level The new level
     */
    private void checkMilestoneLevelRewards(Player player, String mountId, int level) {
        String mountName = mountManager.getMountType(mountId).getDisplayName();
        
        // Process mount abilities unlocked by this level
        switch (mountId) {
            case "phoenix_blaze" -> {
                switch (level) {
                    case 5 -> player.sendMessage(ChatColor.GOLD + "Your " + mountName + " can now use Flame Trail!");
                    case 15 -> player.sendMessage(ChatColor.GOLD + "Your " + mountName + " can now use Thermal Updraft!");
                    case 30 -> player.sendMessage(ChatColor.GOLD + "Your " + mountName + "'s Flame Trail ability has been enhanced!");
                    case 50 -> player.sendMessage(ChatColor.GOLD + "Your " + mountName + " has reached its ultimate potential!");
                }
            }
            case "shadow_steed" -> {
                switch (level) {
                    case 5 -> player.sendMessage(ChatColor.DARK_PURPLE + "Your " + mountName + " can now use Shadow Dash!");
                    case 15 -> player.sendMessage(ChatColor.DARK_PURPLE + "Your " + mountName + " can now use Shadow Cloak!");
                    case 30 -> player.sendMessage(ChatColor.DARK_PURPLE + "Your " + mountName + "'s Shadow Dash ability has been enhanced!");
                    case 50 -> player.sendMessage(ChatColor.DARK_PURPLE + "Your " + mountName + " has reached its ultimate potential!");
                }
            }
            case "crystal_drake" -> {
                switch (level) {
                    case 5 -> player.sendMessage(ChatColor.AQUA + "Your " + mountName + " can now use Crystal Shield!");
                    case 15 -> player.sendMessage(ChatColor.AQUA + "Your " + mountName + " can now use Crystal Teleport!");
                    case 30 -> player.sendMessage(ChatColor.AQUA + "Your " + mountName + " can now use Prismatic Beam!");
                    case 50 -> player.sendMessage(ChatColor.AQUA + "Your " + mountName + " has reached its ultimate potential!");
                }
            }
            case "storm_charger" -> {
                switch (level) {
                    case 5 -> player.sendMessage(ChatColor.BLUE + "Your " + mountName + " can now use Lightning Dash!");
                    case 15 -> player.sendMessage(ChatColor.BLUE + "Your " + mountName + " can now use Thunder Step!");
                    case 30 -> player.sendMessage(ChatColor.BLUE + "Your " + mountName + " can now use Lightning Strike!");
                    case 50 -> player.sendMessage(ChatColor.BLUE + "Your " + mountName + " has reached its ultimate potential!");
                }
            }
            case "ancient_golem" -> {
                switch (level) {
                    case 5 -> player.sendMessage(ChatColor.GRAY + "Your " + mountName + " can now use Ground Pound!");
                    case 15 -> player.sendMessage(ChatColor.GRAY + "Your " + mountName + " can now use Mountain Leap!");
                    case 30 -> player.sendMessage(ChatColor.GRAY + "Your " + mountName + "'s Stone Shield has been enhanced!");
                    case 50 -> player.sendMessage(ChatColor.GRAY + "Your " + mountName + " has reached its ultimate potential!");
                }
            }
        }
        
        // Check for passive perk unlocks
        if (level == 10) {
            player.sendMessage(ChatColor.GREEN + "Your " + mountName + " now has a reduced summon cooldown!");
        } else if (level == 20) {
            player.sendMessage(ChatColor.GREEN + "Your " + mountName + " now reduces fall damage!");
        } else if (level == 35) {
            player.sendMessage(ChatColor.GREEN + "Your " + mountName + " now has auto-recovery when dismounted!");
        } else if (level == 45) {
            player.sendMessage(ChatColor.GREEN + "Your " + mountName + " now generates resources periodically!");
        }
    }
    
    /**
     * Combines two mounts of the same type, adding their XP together
     * 
     * @param playerUUID The player's UUID
     * @param sourceMountId Mount being consumed (will be removed)
     * @param targetMountId Mount to gain the XP (will be kept)
     * @param catalystBonus Extra XP % from catalyst (0-100)
     * @return true if combination was successful
     */
    public boolean combineMounts(UUID playerUUID, String sourceMountId, String targetMountId, int catalystBonus) {
        FileConfiguration playerData = playerDataManager.getPlayerData(playerUUID);
        if (playerData == null) return false;
        
        // Ensure both mounts are the same type
        if (!sourceMountId.equals(targetMountId)) {
            return false;
        }
        
        // Get source mount data
        int sourceXP = playerData.getInt("mounts.data." + sourceMountId + ".xp", 0);
        
        // Get target mount data
        int targetXP = playerData.getInt("mounts.data." + targetMountId + ".xp", 0);
        int targetLevel = playerData.getInt("mounts.data." + targetMountId + ".level", 1);
        
        // Calculate bonus XP from catalyst
        int bonusXP = (int) (sourceXP * (catalystBonus / 100.0));
        int totalXP = targetXP + sourceXP + bonusXP;
        
        // Update target mount with new XP
        boolean leveledUp = false;
        int levelCap = getLevelCap(targetMountId);
        
        if (targetLevel < levelCap) {
            int newLevel = targetLevel;
            
            // Check for level ups
            while (newLevel < levelCap) {
                int requiredXP = getRequiredXP(newLevel + 1);
                if (totalXP >= requiredXP) {
                    newLevel++;
                    leveledUp = true;
                } else {
                    break;
                }
            }
            
            // Save new level if changed
            if (newLevel != targetLevel) {
                playerData.set("mounts.data." + targetMountId + ".level", newLevel);
            }
        }
        
        // Save new XP
        playerData.set("mounts.data." + targetMountId + ".xp", totalXP);
        
        // Remove source mount
        playerData.set("mounts.data." + sourceMountId, null);
        
        // Remove from owned mounts if this was the last instance
        boolean stillOwnsMount = false;
        ConfigurationSection mountDataSection = playerData.getConfigurationSection("mounts.data");
        if (mountDataSection != null) {
            for (String key : mountDataSection.getKeys(false)) {
                if (key.equals(sourceMountId)) {
                    stillOwnsMount = true;
                    break;
                }
            }
        }
        
        if (!stillOwnsMount) {
            mountManager.removeMountFromPlayer(playerUUID, sourceMountId);
        }
        
        // Save changes
        playerDataManager.savePlayerData(playerUUID, playerData);
        
        return leveledUp;
    }
    
    /**
     * Checks if a mount has a specific passive perk
     * 
     * @param playerUUID The player's UUID
     * @param mountId The mount's ID
     * @param perkName The perk name
     * @return true if the mount has the perk
     */
    public boolean hasPassivePerk(UUID playerUUID, String mountId, String perkName) {
        int mountLevel = getMountLevel(playerUUID, mountId);
        
        return switch (perkName) {
            case "reduced-cooldown" -> mountLevel >= 10;
            case "fall-damage-reduction" -> mountLevel >= 20;
            case "auto-recovery" -> mountLevel >= 35;
            case "resource-generation" -> mountLevel >= 45;
            default -> false;
        };
    }
    
    /**
     * Handles XP gain from combat for mounted players
     * 
     * @param playerUUID The player's UUID
     * @param mountId The mount's ID
     * @param isBoss Whether the killed entity was a boss
     */
    public void processCombatXPGain(UUID playerUUID, String mountId, boolean isBoss) {
        // Get XP rates from config
        int mobXP = mountManager.getMountConfig().getInt("mount_leveling.xp_sources.mob_kills", 5);
        int bossXP = mountManager.getMountConfig().getInt("mount_leveling.xp_sources.boss_kills", 100);
        
        // Award XP based on entity type
        int xpGain = isBoss ? bossXP : mobXP;
        
        // Add XP to mount
        if (xpGain > 0) {
            addMountXP(playerUUID, mountId, xpGain);
            
            // Notify player
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.GREEN + "Your mount gained " + xpGain + " XP from combat!");
            }
        }
    }
    
    /**
     * Event handler for entity death to award mount XP
     * 
     * @param event The entity death event
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Check if killed by a player
        if (event.getEntity().getKiller() == null) return;
        
        Player player = event.getEntity().getKiller();
        Mount mount = mountManager.getActiveMount(player.getUniqueId());
        
        // Only process if player is on a mount
        if (mount == null) return;
        
        // Check if entity is a boss
        boolean isBoss = isBossEntity(event.getEntity());
        
        // Award XP
        processCombatXPGain(player.getUniqueId(), mount.getType().getId(), isBoss);
    }
    
    /**
     * Checks if an entity is considered a boss
     * 
     * @param entity The entity to check
     * @return true if the entity is a boss
     */
    private boolean isBossEntity(org.bukkit.entity.LivingEntity entity) {
        return entity.getType() == org.bukkit.entity.EntityType.ENDER_DRAGON ||
                entity.getType() == org.bukkit.entity.EntityType.WITHER ||
                entity.getType() == org.bukkit.entity.EntityType.ELDER_GUARDIAN;
    }
    
    /**
     * Event handler for when a mount is summoned
     * 
     * @param event The mount summoned event
     */
    @EventHandler
    public void onMountSummoned(MountSummonedEvent event) {
        Player player = event.getPlayer();
        Mount mount = event.getMount();
        
        // Initialize mount data if not exist
        FileConfiguration playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData != null) {
            // Ensure mount has level and XP data
            if (!playerData.contains("mounts.data." + mount.getType().getId() + ".level")) {
                playerData.set("mounts.data." + mount.getType().getId() + ".level", 1);
            }
            
            if (!playerData.contains("mounts.data." + mount.getType().getId() + ".xp")) {
                playerData.set("mounts.data." + mount.getType().getId() + ".xp", 0);
            }
            
            // Save changes
            playerDataManager.savePlayerData(player.getUniqueId(), playerData);
        }
    }
} 