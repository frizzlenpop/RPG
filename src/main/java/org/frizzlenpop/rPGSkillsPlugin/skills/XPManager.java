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
    private final Map<Material, Integer> miningXPValues;
    private final Map<Material, Integer> loggingXPValues;
    private final Map<Material, Integer> farmingXPValues;
    private final Map<String, Integer> fightingXPValues;
    private final Map<String, Integer> fishingXPValues;
    private final Map<Material, Integer> enchantingXPValues;

    public XPManager(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
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

        int currentXP = config.getInt("skills." + skill + ".xp", 0);
        int currentLevel = config.getInt("skills." + skill + ".level", 1);

        // XP Scaling Formula
        int requiredXP = (int) Math.pow(currentLevel, 1.5) * 100;

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
        switch (skill) {
            case "mining":
                if (level == 5) player.sendMessage("§e[Mining] You now earn 10% more XP from ores!");
                if (level == 10) player.sendMessage("§e[Mining] You unlocked auto-smelting for rare ores!");
                if (level == 15) player.sendMessage("§e[Mining] You have a chance to double ore drops!");
                break;

            case "logging":
                if (level == 5) player.sendMessage("§e[Logging] You now earn 10% more XP from chopping trees!");
                if (level == 10) player.sendMessage("§e[Logging] You chop trees faster!");
                if (level == 15) player.sendMessage("§e[Logging] You have a chance to double log drops!");
                break;

            case "farming":
                if (level == 5) player.sendMessage("§e[Farming] You now earn 10% more XP from harvesting crops!");
                if (level == 10) player.sendMessage("§e[Farming] You unlocked auto-replanting for crops!");
                if (level == 15) player.sendMessage("§e[Farming] Your crops have a chance to double harvest!");
                break;

            case "fighting":
                if (level == 5) player.sendMessage("§e[Fighting] You deal 5% more damage!");
                if (level == 10) player.sendMessage("§e[Fighting] You heal slightly when killing mobs!");
                if (level == 15) player.sendMessage("§e[Fighting] Your critical hit chance is increased!");
                break;

            case "fishing":
                if (level == 5) player.sendMessage("§e[Fishing] You now earn 10% more XP from fishing!");
                if (level == 10) player.sendMessage("§e[Fishing] You have a chance to catch treasure!");
                if (level == 15) player.sendMessage("§e[Fishing] You catch rare fish more often!");
                break;

            case "enchanting":
                if (level == 5) player.sendMessage("§e[Enchanting] Your research success rate increased by 5%!");
                if (level == 10) player.sendMessage("§e[Enchanting] You have a chance to upgrade books automatically!");
                if (level == 15) player.sendMessage("§e[Enchanting] You unlocked rare enchantment discoveries!");
                break;
        }
    }
}
