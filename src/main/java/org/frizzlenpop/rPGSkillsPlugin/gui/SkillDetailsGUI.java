package org.frizzlenpop.rPGSkillsPlugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.rPGSkillsPlugin.skills.PassiveSkillManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.SkillAbilityManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkillDetailsGUI {
    private final XPManager xpManager;
    private final PassiveSkillManager passiveSkillManager;
    private final SkillAbilityManager skillAbilityManager;

    public SkillDetailsGUI(XPManager xpManager, PassiveSkillManager passiveSkillManager, SkillAbilityManager skillAbilityManager) {
        this.xpManager = xpManager;
        this.passiveSkillManager = passiveSkillManager;
        this.skillAbilityManager = skillAbilityManager;
    }

    public void openSkillDetails(Player player, String skill) {
        Inventory gui = Bukkit.createInventory(null, 27, "Skill Details: " + skill);

        int level = xpManager.getPlayerLevel(player, skill);
        int currentXP = xpManager.getPlayerXP(player, skill);
        int requiredXP = xpManager.getRequiredXP(level);
        List<String> activePassives = passiveSkillManager.getActivePassives(player);
        List<String> skillAbilities = skillAbilityManager.getAbilitiesForSkill(skill);

        // XP Progress
        ItemStack xpItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta xpMeta = xpItem.getItemMeta();
        xpMeta.setDisplayName("§aXP Progress");
        List<String> xpLore = new ArrayList<>();
        xpLore.add("§7Level: §e" + level);
        xpLore.add("§7XP: §b" + currentXP + " / " + requiredXP);
        xpLore.add("§7Progress: " + getProgressBar(currentXP, requiredXP));
        xpMeta.setLore(xpLore);
        xpItem.setItemMeta(xpMeta);
        gui.setItem(10, xpItem);

        // Passive Abilities
        ItemStack passiveItem = new ItemStack(Material.BOOK);
        ItemMeta passiveMeta = passiveItem.getItemMeta();
        passiveMeta.setDisplayName("§aPassive Abilities");
        List<String> passiveLore = new ArrayList<>();
        passiveLore.add("§7Unlocked:");
        for (String passive : activePassives) {
            passiveLore.add("§e✔ " + passive);
        }
        passiveLore.add("§cLocked:");
        passiveLore.add("§7(Level 5, 10, 15 unlocks)");
        passiveMeta.setLore(passiveLore);
        passiveItem.setItemMeta(passiveMeta);
        gui.setItem(12, passiveItem);

        // Active Abilities (Clickable)
        ItemStack activeItem = new ItemStack(Material.ENDER_EYE);
        ItemMeta activeMeta = activeItem.getItemMeta();
        activeMeta.setDisplayName("§aActive Abilities");
        List<String> activeLore = new ArrayList<>();
        for (String ability : skillAbilities) {
            activeLore.add("§eClick to activate: " + ability);
        }
        activeMeta.setLore(activeLore);
        activeItem.setItemMeta(activeMeta);
        gui.setItem(14, activeItem);

        player.openInventory(gui);
    }

    private String getProgressBar(int current, int max) {
        int totalBars = 10;
        int filledBars = (int) ((double) current / max * totalBars);
        return "§a" + "█".repeat(filledBars) + "§7" + "█".repeat(totalBars - filledBars);
    }
}
