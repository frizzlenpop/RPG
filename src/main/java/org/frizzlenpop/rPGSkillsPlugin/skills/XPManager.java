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
    private final PassiveSkillManager passiveSkillManager;
    private final Map<Material, Integer> miningXPValues;
    private final Map<Material, Integer> loggingXPValues;
    private final Map<Material, Integer> farmingXPValues;
    private final Map<String, Integer> fightingXPValues;
    private final Map<String, Integer> fishingXPValues;
    private final Map<Material, Integer> enchantingXPValues;

    public XPManager(PlayerDataManager dataManager, PassiveSkillManager passiveSkillManager) {
        this.dataManager = dataManager;
        this.passiveSkillManager = passiveSkillManager;
        this.miningXPValues = new HashMap<>();
        this.loggingXPValues = new HashMap<>();
        this.farmingXPValues = new HashMap<>();
        this.fightingXPValues = new HashMap<>();
        this.fishingXPValues = new HashMap<>();
        this.enchantingXPValues = new HashMap<>();

        // Assign XP values to different ores
        miningXPValues.put(Material.STONE, 5);
        miningXPValues.put(Material.COAL_ORE, 10);
        miningXPValues.put(Material.IRON_ORE, 15);
        miningXPValues.put(Material.GOLD_ORE, 20);
        miningXPValues.put(Material.DIAMOND_ORE, 50);
        miningXPValues.put(Material.EMERALD_ORE, 75);
        miningXPValues.put(Material.NETHERITE_BLOCK, 100);

        // Assign XP values to different trees
        loggingXPValues.put(Material.OAK_LOG, 10);
        loggingXPValues.put(Material.BIRCH_LOG, 10);
        loggingXPValues.put(Material.SPRUCE_LOG, 15);
        loggingXPValues.put(Material.JUNGLE_LOG, 20);
        loggingXPValues.put(Material.ACACIA_LOG, 20);
        loggingXPValues.put(Material.DARK_OAK_LOG, 25);

        // Assign XP values to different crops
        farmingXPValues.put(Material.WHEAT, 10);
        farmingXPValues.put(Material.CARROTS, 12);
        farmingXPValues.put(Material.POTATOES, 12);
        farmingXPValues.put(Material.BEETROOTS, 15);
        farmingXPValues.put(Material.MELON, 18);
        farmingXPValues.put(Material.PUMPKIN, 18);
        farmingXPValues.put(Material.SWEET_BERRY_BUSH, 20);
        farmingXPValues.put(Material.NETHER_WART, 25);

        // Assign XP Values to Different Mobs
        fightingXPValues.put("ZOMBIE", 10);
        fightingXPValues.put("SKELETON", 12);
        fightingXPValues.put("SPIDER", 15);
        fightingXPValues.put("CREEPER", 20);
        fightingXPValues.put("ENDERMAN", 30);
        fightingXPValues.put("WITHER_SKELETON", 40);
        fightingXPValues.put("BLAZE", 35);
        fightingXPValues.put("ELDER_GUARDIAN", 75);
        fightingXPValues.put("WITHER", 250);
        fightingXPValues.put("ENDER_DRAGON", 500);

        // Assign XP values to different fishing
        fishingXPValues.put("COD", 10);
        fishingXPValues.put("SALMON", 12);
        fishingXPValues.put("TROPICAL_FISH", 15);
        fishingXPValues.put("PUFFERFISH", 20);

        // Assign XP values to different enchantments
        enchantingXPValues.put(Material.ENCHANTED_BOOK, 50);
        enchantingXPValues.put(Material.LAPIS_LAZULI, 10);
        enchantingXPValues.put(Material.NETHER_STAR, 200);
        enchantingXPValues.put(Material.DRAGON_BREATH, 250);
        enchantingXPValues.put(Material.AMETHYST_SHARD, 25);
    }

    public void addXP(Player player, String skill, int xpGained) {
        UUID playerUUID = player.getUniqueId();
        FileConfiguration config = dataManager.getPlayerData(playerUUID);

        int currentXP = getPlayerXP(player, skill);
        int currentLevel = getPlayerLevel(player, skill);

        // XP Scaling Formula
        int requiredXP = getRequiredXP(currentLevel);

        currentXP += xpGained;

        // Check for level up
        if (currentXP >= requiredXP) {
            currentXP -= requiredXP;
            currentLevel++;

            // Notify player of level up
            player.sendMessage("§a[Skills] You leveled up your " + skill + " skill to Level " + currentLevel + "!");
            player.playSound(player.getLocation(), "minecraft:entity.player.levelup", 1.0f, 1.0f);

            // Handle milestone rewards
            handleSkillRewards(player, skill, currentLevel);
        }

        // Save updated values
        config.set("skills." + skill + ".xp", currentXP);
        config.set("skills." + skill + ".level", currentLevel);
        dataManager.savePlayerData(playerUUID, config);
    }

    // ✅ NEW: Method to retrieve player's XP
    public int getPlayerXP(Player player, String skill) {
        UUID playerUUID = player.getUniqueId();
        FileConfiguration config = dataManager.getPlayerData(playerUUID);
        return config.getInt("skills." + skill + ".xp", 0);
    }

    // ✅ NEW: Method to retrieve XP required for leveling up
    public int getRequiredXP(int level) {
        return (int) Math.pow(level, 1.5) * 100;
    }

    // ✅ Method to retrieve player's skill level
    public int getPlayerLevel(Player player, String skill) {
        UUID playerUUID = player.getUniqueId();
        FileConfiguration config = dataManager.getPlayerData(playerUUID);
        return config.getInt("skills." + skill + ".level", 1);
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
        return fightingXPValues.getOrDefault(mobName.toUpperCase(), 0);
    }

    public int getXPForFish(String fishType) {
        return fishingXPValues.getOrDefault(fishType.toUpperCase(), 0);
    }

    public int getXPForEnchanting(Material material) {
        return enchantingXPValues.getOrDefault(material, 0);
    }

    private void handleSkillRewards(Player player, String skill, int level) {
        switch (skill.toLowerCase()) {
            case "mining" -> {
                if (level == 5) passiveSkillManager.applyPassiveEffect(player, "mining_xp_boost");
                if (level == 10) passiveSkillManager.applyPassiveEffect(player, "auto_smelt");
                if (level == 15) passiveSkillManager.applyPassiveEffect(player, "double_ore_drop");
                if (level == 25) passiveSkillManager.applyPassiveEffect(player, "fortune_boost");
                if (level == 50) passiveSkillManager.applyPassiveEffect(player, "auto_smelt_upgrade");
            }
            case "logging" -> {
                if (level == 5) passiveSkillManager.applyPassiveEffect(player, "logging_xp_boost");
                if (level == 10) passiveSkillManager.applyPassiveEffect(player, "fast_chop");
                if (level == 15) passiveSkillManager.applyPassiveEffect(player, "double_wood_drop");
                if (level == 25) passiveSkillManager.applyPassiveEffect(player, "tree_growth_boost");
                if (level == 50) passiveSkillManager.applyPassiveEffect(player, "triple_log_drop");
            }
            case "farming" -> {
                if (level == 5) passiveSkillManager.applyPassiveEffect(player, "farming_xp_boost");
                if (level == 10) passiveSkillManager.applyPassiveEffect(player, "auto_replant");
                if (level == 15) passiveSkillManager.applyPassiveEffect(player, "double_crop_yield");
                if (level == 25) passiveSkillManager.applyPassiveEffect(player, "instant_growth");
                if (level == 50) passiveSkillManager.applyPassiveEffect(player, "auto_harvest");
            }
            case "fighting" -> {
                if (level == 5) passiveSkillManager.applyPassiveEffect(player, "damage_boost");
                if (level == 10) passiveSkillManager.applyPassiveEffect(player, "heal_on_kill");
                if (level == 15) passiveSkillManager.applyPassiveEffect(player, "critical_hit");
                if (level == 25) passiveSkillManager.applyPassiveEffect(player, "lifesteal");
                if (level == 50) passiveSkillManager.applyPassiveEffect(player, "damage_reduction");
            }
        }
    }
}
