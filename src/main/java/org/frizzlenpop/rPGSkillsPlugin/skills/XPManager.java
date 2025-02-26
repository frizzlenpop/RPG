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

        // Assign XP values to different skills
        miningXPValues.put(Material.STONE, 5);
        miningXPValues.put(Material.COAL_ORE, 10);
        miningXPValues.put(Material.IRON_ORE, 15);
        miningXPValues.put(Material.GOLD_ORE, 20);
        miningXPValues.put(Material.DIAMOND_ORE, 50);
        miningXPValues.put(Material.EMERALD_ORE, 75);
        miningXPValues.put(Material.NETHERITE_BLOCK, 100);

        loggingXPValues.put(Material.OAK_LOG, 10);
        loggingXPValues.put(Material.BIRCH_LOG, 10);
        loggingXPValues.put(Material.SPRUCE_LOG, 15);
        loggingXPValues.put(Material.JUNGLE_LOG, 20);
        loggingXPValues.put(Material.ACACIA_LOG, 20);
        loggingXPValues.put(Material.DARK_OAK_LOG, 25);

        farmingXPValues.put(Material.WHEAT, 10);
        farmingXPValues.put(Material.CARROTS, 12);
        farmingXPValues.put(Material.POTATOES, 12);
        farmingXPValues.put(Material.BEETROOTS, 15);
        farmingXPValues.put(Material.MELON, 18);
        farmingXPValues.put(Material.PUMPKIN, 18);
        farmingXPValues.put(Material.SWEET_BERRY_BUSH, 20);
        farmingXPValues.put(Material.NETHER_WART, 25);

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

        fishingXPValues.put("COD", 10);
        fishingXPValues.put("SALMON", 12);
        fishingXPValues.put("TROPICAL_FISH", 15);
        fishingXPValues.put("PUFFERFISH", 20);

        enchantingXPValues.put(Material.ENCHANTED_BOOK, 50);
        enchantingXPValues.put(Material.LAPIS_LAZULI, 10);
        enchantingXPValues.put(Material.NETHER_STAR, 200);
        enchantingXPValues.put(Material.DRAGON_BREATH, 250);
        enchantingXPValues.put(Material.AMETHYST_SHARD, 25);
    }

    public void addXP(Player player, String skill, int xpGained) {
        UUID playerUUID = player.getUniqueId();
        FileConfiguration config = dataManager.getPlayerData(playerUUID);

        int currentXP = config.getInt("skills." + skill + ".xp", 0);
        int currentLevel = config.getInt("skills." + skill + ".level", 1);

        int requiredXP = getRequiredXP(currentLevel);

        currentXP += xpGained;

        if (currentXP >= requiredXP) {
            currentXP -= requiredXP;
            currentLevel++;

            player.sendMessage("§a[Skills] You leveled up your " + skill + " skill to Level " + currentLevel + "!");
            player.playSound(player.getLocation(), "minecraft:entity.player.levelup", 1.0f, 1.0f);

            handleSkillRewards(player, skill, currentLevel);
        }

        config.set("skills." + skill + ".xp", currentXP);
        config.set("skills." + skill + ".level", currentLevel);
        dataManager.savePlayerData(playerUUID, config);
    }

    public int getPlayerLevel(Player player, String skill) {
        UUID playerUUID = player.getUniqueId();
        FileConfiguration config = dataManager.getPlayerData(playerUUID);
        return config.getInt("skills." + skill + ".level", 1);
    }

    public int getPlayerXP(Player player, String skill) {
        UUID playerUUID = player.getUniqueId();
        FileConfiguration config = dataManager.getPlayerData(playerUUID);
        return config.getInt("skills." + skill + ".xp", 0);
    }

    public int getRequiredXP(int level) {
        return (int) Math.pow(level, 1.5) * 100;
    }

    private void handleSkillRewards(Player player, String skill, int level) {
        switch (skill) {
            case "mining":
                if (level == 5) passiveSkillManager.applyPassiveEffect(player, "mining_xp_boost");
                if (level == 10) passiveSkillManager.applyPassiveEffect(player, "auto_smelt");
                if (level == 15) passiveSkillManager.applyPassiveEffect(player, "double_ore_drops");
                break;
            case "logging":
                if (level == 5) passiveSkillManager.applyPassiveEffect(player, "logging_xp_boost");
                if (level == 10) passiveSkillManager.applyPassiveEffect(player, "fast_chop");
                if (level == 15) passiveSkillManager.applyPassiveEffect(player, "double_wood_drops");
                break;
            case "farming":
                if (level == 5) passiveSkillManager.applyPassiveEffect(player, "farming_xp_boost");
                if (level == 10) passiveSkillManager.applyPassiveEffect(player, "auto_replant");
                if (level == 15) passiveSkillManager.applyPassiveEffect(player, "double_crop_yield");
                break;
            case "fighting":
                if (level == 5) passiveSkillManager.applyPassiveEffect(player, "fighting_damage_boost");
                if (level == 10) passiveSkillManager.applyPassiveEffect(player, "heal_on_kill");
                if (level == 15) passiveSkillManager.applyPassiveEffect(player, "crit_boost");
                break;
            case "fishing":
                if (level == 5) passiveSkillManager.applyPassiveEffect(player, "fishing_xp_boost");
                if (level == 10) passiveSkillManager.applyPassiveEffect(player, "treasure_boost");
                if (level == 15) passiveSkillManager.applyPassiveEffect(player, "rare_fish_boost");
                break;
            case "enchanting":
                if (level == 5) passiveSkillManager.applyPassiveEffect(player, "enchanting_research_boost");
                if (level == 10) passiveSkillManager.applyPassiveEffect(player, "auto_upgrade_books");
                if (level == 15) passiveSkillManager.applyPassiveEffect(player, "custom_enchantments");
                break;
        }
    }
}
