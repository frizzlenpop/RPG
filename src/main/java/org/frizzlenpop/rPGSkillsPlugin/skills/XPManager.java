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
    private final Map<Material, Integer> enchantingXPValues;

    // Updated constructor to not require PassiveSkillManager
    public XPManager(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
        this.miningXPValues = initializeMiningXP();
        this.loggingXPValues = initializeLoggingXP();
        this.farmingXPValues = initializeFarmingXP();
        this.fightingXPValues = initializeFightingXP();
        this.fishingXPValues = initializeFishingXP();
        this.enchantingXPValues = initializeEnchantingXP();
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

        // Apply XP multipliers if PassiveSkillManager is available
        if (passiveSkillManager != null) {
            xpGained = (int)(xpGained * passiveSkillManager.getXPMultiplier(player, skill));
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
        return fightingXPValues.getOrDefault(mobName, 0);
    }

    public int getXPForFish(String fishType) {
        return fishingXPValues.getOrDefault(fishType, 0);
    }

    public int getXPForEnchanting(Material material) {
        return enchantingXPValues.getOrDefault(material, 0);
    }

    // Initialize XP value maps
    private Map<Material, Integer> initializeMiningXP() {
        Map<Material, Integer> xpValues = new HashMap<>();
        xpValues.put(Material.STONE, 1);
        xpValues.put(Material.COAL_ORE, 5);
        xpValues.put(Material.IRON_ORE, 10);
        xpValues.put(Material.GOLD_ORE, 15);
        xpValues.put(Material.DIAMOND_ORE, 30);
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
        xpValues.put("ZOMBIE", 10);
        xpValues.put("SKELETON", 15);
        xpValues.put("SPIDER", 12);
        xpValues.put("CREEPER", 20);
        return xpValues;
    }

    private Map<String, Integer> initializeFishingXP() {
        Map<String, Integer> xpValues = new HashMap<>();
        xpValues.put("RAW_FISH", 5);
        xpValues.put("RAW_SALMON", 7);
        xpValues.put("PUFFERFISH", 10);
        xpValues.put("TROPICAL_FISH", 15);
        return xpValues;
    }

    private Map<Material, Integer> initializeEnchantingXP() {
        Map<Material, Integer> xpValues = new HashMap<>();
        xpValues.put(Material.WOODEN_SWORD, 5);
        xpValues.put(Material.STONE_SWORD, 7);
        xpValues.put(Material.IRON_SWORD, 10);
        xpValues.put(Material.DIAMOND_SWORD, 15);
        xpValues.put(Material.NETHERITE_SWORD, 20);
        return xpValues;
    }
}