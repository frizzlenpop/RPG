package org.frizzlenpop.rPGSkillsPlugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.data.PartyManager;
import org.frizzlenpop.rPGSkillsPlugin.data.XPBoosterManager;
import org.frizzlenpop.rPGSkillsPlugin.api.events.SkillLevelUpEvent;
import org.frizzlenpop.rPGSkillsPlugin.api.events.SkillXPGainEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class XPManager {
    private final PlayerDataManager dataManager;
    private PassiveSkillManager passiveSkillManager; // Removed final modifier
    private PartyManager partyManager; // Added for XP sharing
    private final Map<Material, Integer> miningXPValues;
    private final Map<Material, Integer> loggingXPValues;
    private final Map<Material, Integer> farmingXPValues;
    private final Map<String, Integer> fightingXPValues;
    private final Map<String, Integer> fishingXPValues;
    private final Map<Material, Integer> excavationXPValues; // Added for excavation
    private final RPGSkillsPlugin plugin;
    
    // Track total XP earned for each player
    private final Map<UUID, Map<String, Integer>> totalSkillXPEarned = new ConcurrentHashMap<>();
    // Track highest level reached for each skill
    private final Map<UUID, Map<String, Integer>> highestSkillLevels = new ConcurrentHashMap<>();

    // Updated constructor to include plugin
    public XPManager(PlayerDataManager dataManager, RPGSkillsPlugin plugin) {
        this.dataManager = dataManager;
        this.plugin = plugin;
        this.miningXPValues = initializeMiningXP();
        this.loggingXPValues = initializeLoggingXP();
        this.farmingXPValues = initializeFarmingXP();
        this.fightingXPValues = initializeFightingXP();
        this.fishingXPValues = initializeFishingXP();
        this.excavationXPValues = initializeExcavationXP();
    }

    public void setPassiveSkillManager(PassiveSkillManager passiveSkillManager) {
        if (this.passiveSkillManager == null) {
            this.passiveSkillManager = passiveSkillManager;
        }
    }

    public void setPartyManager(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    private void handleSkillRewards(Player player, String skill, int level) {
        // Send level-up notification
        player.sendMessage("§a✨ Your " + skill + " skill is now level " + level + "!");

        // Check for ability unlocks at level 15
        if (level == 15) {
            player.sendMessage("§6⚔ You've unlocked the active ability for " + skill + "!");
        }

        // Apply passive effects if PassiveSkillManager is available
        if (passiveSkillManager != null) {
            passiveSkillManager.updatePassiveEffects(player, skill, level);
        }
    }

    public void addXP(Player player, String skill, int xpGained) {
        UUID playerUUID = player.getUniqueId();
        int currentXP = dataManager.getSkillXP(playerUUID, skill);
        int currentLevel = dataManager.getSkillLevel(playerUUID, skill);

        // Store the base XP before multipliers
        int baseXP = xpGained;
        int bonusXP = 0;
        
        // Check for XP booster on the player's held item
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        double itemBoosterMultiplier = 1.0;
        
        // Only apply item booster if the plugin has a booster manager
        try {
            XPBoosterManager boosterManager = plugin.getXPBoosterManager();
            if (boosterManager != null) {
                itemBoosterMultiplier = boosterManager.getMultiplier(heldItem, skill);
            }
        } catch (Exception e) {
            // If there's any error getting the booster manager, ignore it
            // This prevents issues if the method doesn't exist
        }
        
        // Apply XP multipliers if PassiveSkillManager is available
        double multiplier = 1.0;
        if (passiveSkillManager != null) {
            multiplier = passiveSkillManager.getXPMultiplier(player, skill);
        }
        
        // Combine with item booster multiplier
        double totalMultiplier = multiplier * itemBoosterMultiplier;
        
        // If we have any multiplier bonus, calculate bonus XP
        if (totalMultiplier > 1.0) {
            // Calculate bonus XP correctly - multiply base XP by the percentage boost
            bonusXP = (int)Math.round(baseXP * (totalMultiplier - 1.0));
            xpGained = baseXP + bonusXP;
        }
        
        // Fire the SkillXPGainEvent
        SkillXPGainEvent xpEvent = new SkillXPGainEvent(player, skill, xpGained);
        Bukkit.getPluginManager().callEvent(xpEvent);
        
        // Check if the event was cancelled
        if (xpEvent.isCancelled()) {
            return;
        }
        
        // Get the potentially modified XP amount
        xpGained = xpEvent.getXPGained();
        
        // Calculate amount to share with party members
        int sharedAmount = 0;
        Map<UUID, Integer> sharedXp = null;
        
        if (partyManager != null && partyManager.isInParty(playerUUID)) {
            // Get the party leader UUID
            UUID leaderUUID = partyManager.getPartyLeader(playerUUID);
            
            // Get share percentage from party settings
            double sharePercent = partyManager.getXpSharePercent(leaderUUID);
            
            // Get online party members (excluding the player)
            List<UUID> onlineMembers = partyManager.getPartyMembers(leaderUUID).stream()
                    .filter(uuid -> !uuid.equals(playerUUID))
                    .filter(uuid -> player.getServer().getPlayer(uuid) != null)
                    .collect(Collectors.toList());
            
            // Only calculate shared XP if there are online members to share with
            if (!onlineMembers.isEmpty()) {
                // Calculate total amount to share with all members
                sharedAmount = (int)(xpGained * sharePercent);
                
                // Only proceed if there's something to share
                if (sharedAmount > 0) {
                    // Reduce player's XP by the shared amount
                    xpGained -= sharedAmount;
                    
                    // Calculate shared XP for distribution to party members
                    sharedXp = partyManager.calculateSharedXp(playerUUID, sharedAmount);
                }
            }
        }
        
        // Display XP gain popup message if there's XP to add
        if (xpGained > 0) {
            // If we're sharing XP, include that in the message
            if (sharedAmount > 0) {
                showXPGainMessageWithSharing(player, skill, baseXP, bonusXP, sharedAmount);
            } else {
                showXPGainMessage(player, skill, baseXP, bonusXP);
            }
        }

        // Add remaining XP to the player
        addXPToPlayer(player, skill, xpGained, currentXP, currentLevel);
        
        // Distribute the shared XP to party members
        if (sharedXp != null && !sharedXp.isEmpty()) {
            distributeSharedXP(player, skill, sharedXp);
        }
    }
    
    /**
     * Add XP to a player and handle level ups
     */
    private void addXPToPlayer(Player player, String skill, int xpGained, int currentXP, int currentLevel) {
        UUID playerUUID = player.getUniqueId();
        
        // Get the highest level achieved for this skill
        int highestLevel = dataManager.getHighestSkillLevel(playerUUID, skill);
        
        // Make sure current level isn't below highest level
        if (currentLevel < highestLevel) {
            currentLevel = highestLevel;
        }
        
        int newXP = currentXP + xpGained;
        int requiredXP = getRequiredXP(currentLevel);

        while (newXP >= requiredXP) {
            // Store the old level before leveling up
            int oldLevel = currentLevel;
            
            // Level up
            currentLevel++;
            newXP -= requiredXP;
            requiredXP = getRequiredXP(currentLevel);

            // Update highest level if needed
            if (currentLevel > highestLevel) {
                highestLevel = currentLevel;
                dataManager.setHighestSkillLevel(playerUUID, skill, highestLevel);
            }

            // Update level in data manager
            dataManager.setSkillLevel(playerUUID, skill, currentLevel);

            // Handle rewards
            handleSkillRewards(player, skill, currentLevel);
            
            // Fire the SkillLevelUpEvent
            SkillLevelUpEvent levelEvent = new SkillLevelUpEvent(player, skill, oldLevel, currentLevel);
            Bukkit.getPluginManager().callEvent(levelEvent);
        }

        // Update XP in data manager
        dataManager.setSkillXP(playerUUID, skill, newXP);
        
        // Final check to ensure level is never below highest level
        if (currentLevel < highestLevel) {
            dataManager.setSkillLevel(playerUUID, skill, highestLevel);
        }
    }
    
    /**
     * Displays a popup message to the player when they gain XP and share some with the party
     */
    private void showXPGainMessageWithSharing(Player player, String skill, int baseXP, int bonusXP, int sharedAmount) {
        // Format the skill name nicely (capitalize first letter)
        String formattedSkill = skill.substring(0, 1).toUpperCase() + skill.substring(1).toLowerCase();
        
        // Get a color for the skill
        String color = getSkillColor(skill);
        
        // Calculate actual XP received (base + bonus - shared)
        int actualXP = baseXP + bonusXP - sharedAmount;
        
        // Show action bar message
        String message;
        if (bonusXP > 0) {
            message = String.format("%s+%d %s XP %s(+%d Bonus, -%d Shared)", 
                color, actualXP, formattedSkill, "§6", bonusXP, sharedAmount);
        } else {
            message = String.format("%s+%d %s XP %s(-%d Shared)", 
                color, actualXP, formattedSkill, "§7", sharedAmount);
        }
        
        player.sendActionBar(message);
    }
    
    /**
     * Distribute shared XP to party members
     */
    private void distributeSharedXP(Player source, String skill, Map<UUID, Integer> sharedXp) {
        UUID sourcePlayerUUID = source.getUniqueId();
        
        // Get party level info if available
        double bonusPercent = 0;
        if (partyManager != null && partyManager.isInParty(sourcePlayerUUID)) {
            UUID leaderUUID = partyManager.getPartyLeader(sourcePlayerUUID);
            bonusPercent = partyManager.getPartyBonusPercent(leaderUUID);
        }
        
        for (Map.Entry<UUID, Integer> entry : sharedXp.entrySet()) {
            UUID memberUUID = entry.getKey();
            int xpAmount = entry.getValue();
            
            // Get the party member
            Player member = source.getServer().getPlayer(memberUUID);
            if (member != null && !member.equals(source)) {
                // Get current XP and level
                int currentXP = dataManager.getSkillXP(memberUUID, skill);
                int currentLevel = dataManager.getSkillLevel(memberUUID, skill);
                
                // Add the shared XP (without triggering further sharing)
                addXPToPlayer(member, skill, xpAmount, currentXP, currentLevel);
                
                // Format skill name nicely
                String formattedSkill = skill.substring(0, 1).toUpperCase() + skill.substring(1).toLowerCase();
                
                // Show party XP notification
                if (bonusPercent > 0) {
                    member.sendActionBar(
                        "§d+" + xpAmount + " " + formattedSkill + 
                        " XP §7(from " + source.getName() + ", includes §a+" +
                        String.format("%.0f%%", bonusPercent * 100) + "§7 party bonus)"
                    );
                } else {
                    member.sendActionBar(
                        "§d+" + xpAmount + " " + formattedSkill + 
                        " XP §7(from " + source.getName() + ")"
                    );
                }
            }
        }
    }

    /**
     * Displays a popup message to the player when they gain XP
     * 
     * @param player The player who gained XP
     * @param skill The skill for which XP was gained
     * @param baseXP The base amount of XP gained before multipliers
     * @param bonusXP The bonus XP from multipliers
     */
    private void showXPGainMessage(Player player, String skill, int baseXP, int bonusXP) {
        // Format the skill name nicely (capitalize first letter)
        String formattedSkill = skill.substring(0, 1).toUpperCase() + skill.substring(1).toLowerCase();
        
        // Get a color for the skill
        String color = getSkillColor(skill);
        
        // Show action bar message
        String message;
        if (bonusXP > 0) {
            message = String.format("%s+%d %s XP %s(+%d Bonus XP)", 
                color, baseXP, formattedSkill, "§6", bonusXP);
        } else {
            message = String.format("%s+%d %s XP", color, baseXP, formattedSkill);
        }
        
        player.sendActionBar(message);
    }
    
    /**
     * Returns the color code for a given skill
     * 
     * @param skill The skill name
     * @return The color code for the skill
     */
    private String getSkillColor(String skill) {
        switch (skill.toLowerCase()) {
            case "mining":
                return "§b"; // Aqua
            case "logging":
            case "woodcutting":
                return "§2"; // Dark Green
            case "farming":
                return "§a"; // Green
            case "fighting":
            case "combat":
                return "§c"; // Red
            case "fishing":
                return "§9"; // Blue
            case "enchanting":
                return "§d"; // Light Purple
            default:
                return "§e"; // Yellow
        }
    }

    public int getPlayerXP(Player player, String skill) {
        return dataManager.getSkillXP(player.getUniqueId(), skill);
    }

    public int getRequiredXP(int level) {
        return 100 * level;
    }

    public int getPlayerLevel(Player player, String skill) {
        return dataManager.getSkillLevel(player.getUniqueId(), skill);
    }

    public int getXPForMaterial(Material material) {
        return miningXPValues.getOrDefault(material, 0);
    }

    public int getXPForLog(Material material) {
        return loggingXPValues.getOrDefault(material, 0);
    }

    public int getXPForCrop(Material material) {
        return farmingXPValues.getOrDefault(material, 0);
    }

    public int getXPForMob(String mobName) {
        // Log the mob type for debugging
        System.out.println("[RPGSkills] Looking up XP for mob type: " + mobName);
        
        // Try to get the XP value using the exact mobName
        int xpValue = fightingXPValues.getOrDefault(mobName, 0);
        
        // If not found, try looking up using just the base mob name 
        // (in case it's a special variant or has other attributes)
        if (xpValue == 0) {
            for (String key : fightingXPValues.keySet()) {
                if (mobName.contains(key)) {
                    xpValue = fightingXPValues.get(key);
                    System.out.println("[RPGSkills] Found partial match: " + key + " for " + mobName);
                    break;
                }
            }
        }
        
        System.out.println("[RPGSkills] XP value found for " + mobName + ": " + xpValue);
        return xpValue;
    }

    public int getXPForFish(String fishType) {
        // Log the fish type for debugging
        System.out.println("Looking up XP for fish type: " + fishType);
        
        // Try to get the XP value using the exact fishType
        int xpValue = fishingXPValues.getOrDefault(fishType, 0);
        
        // If not found, try looking up using just the base item name 
        // (in case it's a special variant or has other attributes)
        if (xpValue == 0) {
            for (String key : fishingXPValues.keySet()) {
                if (fishType.contains(key)) {
                    xpValue = fishingXPValues.get(key);
                    break;
                }
            }
        }
        
        System.out.println("XP value found: " + xpValue);
        return xpValue;
    }

    public int getXPForExcavationMaterial(Material material) {
        return excavationXPValues.getOrDefault(material, 0);
    }

    // Initialize XP value maps
    private Map<Material, Integer> initializeMiningXP() {
        Map<Material, Integer> xpValues = new HashMap<>();
        // Stone variants
        xpValues.put(Material.STONE, 1);
        xpValues.put(Material.DEEPSLATE, 2);
        xpValues.put(Material.COBBLED_DEEPSLATE, 2);
        xpValues.put(Material.TUFF, 1);
        xpValues.put(Material.GRANITE, 1);
        xpValues.put(Material.DIORITE, 1);
        xpValues.put(Material.ANDESITE, 1);
        xpValues.put(Material.CALCITE, 1);
        
        // Regular ores
        xpValues.put(Material.COAL_ORE, 5);
        xpValues.put(Material.IRON_ORE, 10);
        xpValues.put(Material.COPPER_ORE, 8);
        xpValues.put(Material.GOLD_ORE, 15);
        xpValues.put(Material.REDSTONE_ORE, 12);
        xpValues.put(Material.LAPIS_ORE, 12);
        xpValues.put(Material.DIAMOND_ORE, 30);
        xpValues.put(Material.EMERALD_ORE, 35);
        
        // Deepslate ores (more XP than regular variants)
        xpValues.put(Material.DEEPSLATE_COAL_ORE, 7);
        xpValues.put(Material.DEEPSLATE_IRON_ORE, 12);
        xpValues.put(Material.DEEPSLATE_COPPER_ORE, 10);
        xpValues.put(Material.DEEPSLATE_GOLD_ORE, 18);
        xpValues.put(Material.DEEPSLATE_REDSTONE_ORE, 15);
        xpValues.put(Material.DEEPSLATE_LAPIS_ORE, 15);
        xpValues.put(Material.DEEPSLATE_DIAMOND_ORE, 35);
        xpValues.put(Material.DEEPSLATE_EMERALD_ORE, 40);
        
        // Nether ores
        xpValues.put(Material.NETHER_GOLD_ORE, 15);
        xpValues.put(Material.NETHER_QUARTZ_ORE, 10);
        xpValues.put(Material.ANCIENT_DEBRIS, 50);
        
        // Additional Nether blocks
        xpValues.put(Material.NETHERRACK, 1);
        xpValues.put(Material.NETHER_BRICKS, 2);
        xpValues.put(Material.RED_NETHER_BRICKS, 2);
        xpValues.put(Material.BLACKSTONE, 3);
        xpValues.put(Material.GILDED_BLACKSTONE, 15);
        xpValues.put(Material.POLISHED_BLACKSTONE, 3);
        xpValues.put(Material.POLISHED_BLACKSTONE_BRICKS, 4);
        xpValues.put(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS, 4);
        xpValues.put(Material.CHISELED_POLISHED_BLACKSTONE, 5);
        xpValues.put(Material.BASALT, 2);
        xpValues.put(Material.POLISHED_BASALT, 3);
        xpValues.put(Material.CRYING_OBSIDIAN, 20);
        xpValues.put(Material.GLOWSTONE, 8);
        xpValues.put(Material.MAGMA_BLOCK, 5);
        
        return xpValues;
    }

    private Map<Material, Integer> initializeLoggingXP() {
        Map<Material, Integer> xpValues = new HashMap<>();
        xpValues.put(Material.OAK_LOG, 5);
        xpValues.put(Material.BIRCH_LOG, 5);
        xpValues.put(Material.SPRUCE_LOG, 5);
        xpValues.put(Material.JUNGLE_LOG, 5);
        xpValues.put(Material.ACACIA_LOG, 5);
        xpValues.put(Material.DARK_OAK_LOG, 5);
        return xpValues;
    }

    private Map<Material, Integer> initializeFarmingXP() {
        Map<Material, Integer> xpValues = new HashMap<>();
        xpValues.put(Material.WHEAT, 5);
        xpValues.put(Material.CARROTS, 5);
        xpValues.put(Material.POTATOES, 5);
        xpValues.put(Material.BEETROOTS, 5);
        return xpValues;
    }

    private Map<String, Integer> initializeFightingXP() {
        Map<String, Integer> xpValues = new HashMap<>();
        // Common hostiles
        xpValues.put("ZOMBIE", 10);
        xpValues.put("SKELETON", 15);
        xpValues.put("SPIDER", 12);
        xpValues.put("CREEPER", 20);
        
        // Other hostiles
        xpValues.put("ENDERMAN", 25);
        xpValues.put("WITCH", 25);
        xpValues.put("SLIME", 8);
        xpValues.put("MAGMA_CUBE", 12);
        xpValues.put("BLAZE", 20);
        xpValues.put("GHAST", 25);
        xpValues.put("WITHER_SKELETON", 20);
        xpValues.put("STRAY", 15);
        xpValues.put("HUSK", 12);
        xpValues.put("DROWNED", 12);
        xpValues.put("GUARDIAN", 20);
        xpValues.put("ELDER_GUARDIAN", 50);
        xpValues.put("SHULKER", 25);
        xpValues.put("VEX", 18);
        xpValues.put("VINDICATOR", 25);
        xpValues.put("EVOKER", 30);
        xpValues.put("RAVAGER", 35);
        xpValues.put("PHANTOM", 20);
        
        // Neutrals/Passives (less XP)
        xpValues.put("ZOMBIE_VILLAGER", 12);
        xpValues.put("PIG_ZOMBIE", 15); // Legacy name
        xpValues.put("ZOMBIFIED_PIGLIN", 15); // Modern name
        xpValues.put("WOLF", 10);
        xpValues.put("IRON_GOLEM", 30);
        xpValues.put("POLAR_BEAR", 15);
        xpValues.put("PANDA", 15);
        xpValues.put("LLAMA", 12);
        xpValues.put("TRADER_LLAMA", 12);
        
        // Passive mobs (least XP)
        xpValues.put("PIG", 5);
        xpValues.put("COW", 5);
        xpValues.put("SHEEP", 5);
        xpValues.put("CHICKEN", 5);
        xpValues.put("RABBIT", 5);
        xpValues.put("SQUID", 8);
        xpValues.put("BAT", 3);
        xpValues.put("HORSE", 8);
        xpValues.put("DONKEY", 8);
        xpValues.put("MULE", 8);
        
        // End game bosses
        xpValues.put("ENDER_DRAGON", 500);
        xpValues.put("WITHER", 350);
        
        return xpValues;
    }

    private Map<String, Integer> initializeFishingXP() {
        Map<String, Integer> xpValues = new HashMap<>();
        // Increased fishing XP by 300% (multiplied by 4)
        xpValues.put("COD", 20); // Was 5
        xpValues.put("SALMON", 28); // Was 7
        xpValues.put("PUFFERFISH", 40); // Was 10
        xpValues.put("TROPICAL_FISH", 60); // Was 15
        // Add more modern item types (also increased by 300%)
        xpValues.put("FISHING_ROD", 8); // Was 2
        xpValues.put("BOW", 20); // Was 5
        xpValues.put("ENCHANTED_BOOK", 80); // Was 20
        xpValues.put("NAME_TAG", 100); // Was 25
        xpValues.put("NAUTILUS_SHELL", 120); // Was 30
        xpValues.put("SADDLE", 60); // Was 15
        return xpValues;
    }

    private Map<Material, Integer> initializeExcavationXP() {
        Map<Material, Integer> excavationXP = new HashMap<>();
        
        // Reduced excavation XP by 75% (multiplied by 0.25)
        // Basic dirt blocks
        excavationXP.put(Material.DIRT, 2); // Was 10
        excavationXP.put(Material.COARSE_DIRT, 2); // Was 10
        excavationXP.put(Material.FARMLAND, 2); // Was 10
        excavationXP.put(Material.GRASS_BLOCK, 2); // Was 10
        excavationXP.put(Material.DIRT_PATH, 2); // Was 10
        
        // Sand materials
        excavationXP.put(Material.SAND, 3); // Was 15
        excavationXP.put(Material.RED_SAND, 3); // Was 15
        
        // Gravel
        excavationXP.put(Material.GRAVEL, 5); // Was 20
        
        // Clay
        excavationXP.put(Material.CLAY, 6); // Was 25
        
        // Soul materials
        excavationXP.put(Material.SOUL_SAND, 7); // Was 30
        excavationXP.put(Material.SOUL_SOIL, 7); // Was 30
        
        // Special dirt types
        excavationXP.put(Material.MYCELIUM, 10); // Was 40
        excavationXP.put(Material.PODZOL, 10); // Was 40
        
        // Snow
        excavationXP.put(Material.SNOW_BLOCK, 3); // Was 15
        excavationXP.put(Material.SNOW, 3); // Was 15
        
        return excavationXP;
    }

    /**
     * Get total XP earned for a player in a specific skill
     * This includes all XP earned, even after level ups
     */
    public int getTotalXPEarned(UUID playerUUID, String skill) {
        Map<String, Integer> playerTotalXP = totalSkillXPEarned.get(playerUUID);
        if (playerTotalXP == null) {
            return 0;
        }
        return playerTotalXP.getOrDefault(skill, 0);
    }
    
    /**
     * Get total XP earned across all skills
     */
    public int getTotalXPEarnedAllSkills(UUID playerUUID) {
        Map<String, Integer> playerTotalXP = totalSkillXPEarned.get(playerUUID);
        if (playerTotalXP == null) {
            return 0;
        }
        return playerTotalXP.values().stream().mapToInt(Integer::intValue).sum();
    }
}