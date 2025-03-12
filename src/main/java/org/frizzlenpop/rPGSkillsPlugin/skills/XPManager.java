package org.frizzlenpop.rPGSkillsPlugin.skills;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class XPManager {
    private final PlayerDataManager dataManager;
    private PassiveSkillManager passiveSkillManager; // Removed final modifier
    private final Map<Material, Integer> miningXPValues;
    private final Map<Material, Integer> loggingXPValues;
    private final Map<Material, Integer> farmingXPValues;
    private final Map<String, Integer> fightingXPValues;
    private final Map<String, Integer> fishingXPValues;

    // Updated constructor to not require PassiveSkillManager
    public XPManager(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
        this.miningXPValues = initializeMiningXP();
        this.loggingXPValues = initializeLoggingXP();
        this.farmingXPValues = initializeFarmingXP();
        this.fightingXPValues = initializeFightingXP();
        this.fishingXPValues = initializeFishingXP();
    }

    public void setPassiveSkillManager(PassiveSkillManager passiveSkillManager) {
        if (this.passiveSkillManager == null) {
            this.passiveSkillManager = passiveSkillManager;
        }
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
        
        // Apply XP multipliers if PassiveSkillManager is available
        if (passiveSkillManager != null) {
            double multiplier = passiveSkillManager.getXPMultiplier(player, skill);
            if (multiplier > 1.0) {
                // Calculate bonus XP correctly - multiply base XP by the percentage boost
                bonusXP = (int)Math.round(baseXP * (multiplier - 1.0));
                xpGained = baseXP + bonusXP;
            }
        }
        
        // Display XP gain popup message if there's XP to add
        if (xpGained > 0) {
            showXPGainMessage(player, skill, baseXP, bonusXP);
        }

        int newXP = currentXP + xpGained;
        int requiredXP = getRequiredXP(currentLevel);

        while (newXP >= requiredXP) {
            // Level up
            currentLevel++;
            newXP -= requiredXP;
            requiredXP = getRequiredXP(currentLevel);

            // Update level in data manager
            dataManager.setSkillLevel(playerUUID, skill, currentLevel);

            // Handle rewards
            handleSkillRewards(player, skill, currentLevel);
        }

        // Update XP in data manager
        dataManager.setSkillXP(playerUUID, skill, newXP);
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
        xpValues.put("COD", 5);
        xpValues.put("SALMON", 7);
        xpValues.put("PUFFERFISH", 10);
        xpValues.put("TROPICAL_FISH", 15);
        // Add more modern item types
        xpValues.put("FISHING_ROD", 2);
        xpValues.put("BOW", 5);
        xpValues.put("ENCHANTED_BOOK", 20);
        xpValues.put("NAME_TAG", 25);
        xpValues.put("NAUTILUS_SHELL", 30);
        xpValues.put("SADDLE", 15);
        return xpValues;
    }
}