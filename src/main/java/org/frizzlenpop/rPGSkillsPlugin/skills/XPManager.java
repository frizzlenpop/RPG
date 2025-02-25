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



    public XPManager(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
        this.miningXPValues = new HashMap<>();
        this.loggingXPValues = new HashMap<>();
        this.farmingXPValues = new HashMap<>();

        // Assign XP values to different ores
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

            // Play level-up sound effect
            player.playSound(player.getLocation(), "minecraft:entity.player.levelup", 1.0f, 1.0f);
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


}
