package org.frizzlenpop.rPGSkillsPlugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkillsGUI implements Listener {
    private final PlayerDataManager playerDataManager;
    private final XPManager xpManager;

    public SkillsGUI(PlayerDataManager playerDataManager, XPManager xpManager) {
        this.playerDataManager = playerDataManager;
        this.xpManager = xpManager;
    }

    public void openSkillsMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "RPG Skills");

        // Define skill list
        String[] skills = {"mining", "logging", "farming", "fighting", "fishing", "enchanting"};
        Material[] icons = {
                Material.DIAMOND_PICKAXE, Material.OAK_LOG, Material.WHEAT,
                Material.IRON_SWORD, Material.FISHING_ROD, Material.ENCHANTING_TABLE
        };

        for (int i = 0; i < skills.length; i++) {
            String skill = skills[i];
            Material icon = icons[i];

            int level = xpManager.getPlayerLevel(player, skill);
            int currentXP = xpManager.getPlayerXP(player, skill);
            int requiredXP = xpManager.getRequiredXP(level);

            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();

            meta.setDisplayName("§a" + skill.substring(0, 1).toUpperCase() + skill.substring(1));
            lore.add("§7Level: §e" + level);
            lore.add("§7XP: §b" + currentXP + " / " + requiredXP);
            lore.add("§7Progress: " + getProgressBar(currentXP, requiredXP));

            lore.add("§eClick to view details!");
            meta.setLore(lore);
            item.setItemMeta(meta);

            gui.setItem(i, item);
        }

        player.openInventory(gui);
    }

    private String getProgressBar(int current, int max) {
        int totalBars = 10;
        int filledBars = (int) ((double) current / max * totalBars);
        return "§a" + "█".repeat(filledBars) + "§7" + "█".repeat(totalBars - filledBars);
    }
}
