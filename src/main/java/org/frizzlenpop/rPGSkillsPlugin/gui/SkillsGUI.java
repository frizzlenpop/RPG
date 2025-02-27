package org.frizzlenpop.rPGSkillsPlugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.SkillAbilityManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkillsGUI implements Listener {

    private final PlayerDataManager dataManager;
    private final XPManager xpManager;
    private final SkillAbilityManager abilityManager;

    public SkillsGUI(PlayerDataManager dataManager, XPManager xpManager, SkillAbilityManager abilityManager) {
        this.dataManager = dataManager;
        this.xpManager = xpManager;
        this.abilityManager = abilityManager;
    }

    public void openSkillsMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§aYour Skills");

        UUID playerUUID = player.getUniqueId();
        FileConfiguration config = dataManager.getPlayerData(playerUUID);

        // Add each skill to the menu
        gui.setItem(10, createSkillIcon(Material.IRON_PICKAXE, "Mining", "mining", config));
        gui.setItem(11, createSkillIcon(Material.IRON_AXE, "Logging", "logging", config));
        gui.setItem(12, createSkillIcon(Material.WHEAT, "Farming", "farming", config));
        gui.setItem(13, createSkillIcon(Material.IRON_SWORD, "Fighting", "fighting", config));
        gui.setItem(14, createSkillIcon(Material.FISHING_ROD, "Fishing", "fishing", config));
        gui.setItem(15, createSkillIcon(Material.ENCHANTING_TABLE, "Enchanting", "enchanting", config));

        // Add exit button
        gui.setItem(18, createExitButton());

        player.openInventory(gui);
    }

    private ItemStack createSkillIcon(Material material, String skillName, String skillKey, FileConfiguration config) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6" + skillName);
            List<String> lore = new ArrayList<>();

            int level = config.getInt("skills." + skillKey + ".level", 1);
            int xp = config.getInt("skills." + skillKey + ".xp", 0);
            int xpRequired = (int) Math.pow(level, 1.5) * 100;

            lore.add("§7Level: §a" + level);
            lore.add("§7XP: §e" + xp + "§7 / §b" + xpRequired);
            lore.add("§fProgress: §e" + (xp * 100 / xpRequired) + "%");
            lore.add("");
            lore.add("§eClick to view details!");

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void openSkillDetailMenu(Player player, String skillName, String skillKey, Material icon) {
        Inventory detailGUI = Bukkit.createInventory(null, 27, "§a" + skillName + " Details");

        UUID playerUUID = player.getUniqueId();
        FileConfiguration config = dataManager.getPlayerData(playerUUID);

        int level = config.getInt("skills." + skillKey + ".level", 1);
        int xp = config.getInt("skills." + skillKey + ".xp", 0);
        int xpRequired = (int) Math.pow(level, 1.5) * 100;

        ItemStack skillInfo = new ItemStack(icon);
        ItemMeta skillMeta = skillInfo.getItemMeta();
        if (skillMeta != null) {
            skillMeta.setDisplayName("§6" + skillName);
            List<String> lore = new ArrayList<>();
            lore.add("§7Current Level: §a" + level);
            lore.add("§7Current XP: §e" + xp + "§7 / §b" + xpRequired);
            lore.add("");
            lore.add("§eUnlocked Perks:");

            // Display all possible milestones with unlock status
            int[] milestones = {5, 10, 15, 25, 50, 100};
            for (int milestone : milestones) {
                String milestone_text = getSkillMilestone(skillKey, milestone);
                if (level >= milestone) {
                    lore.add("§a✓ Level " + milestone + ": " + milestone_text);
                } else {
                    lore.add("§c✗ Level " + milestone + ": " + milestone_text);
                }
            }

            // Show Active Skill Unlock Level
            lore.add("");
            lore.add("§bActive Skill: " + getActiveSkillName(skillKey));
            lore.add("§7Unlocks at Level " + getActiveSkillUnlockLevel(skillKey));

            skillMeta.setLore(lore);
            skillInfo.setItemMeta(skillMeta);
        }
        detailGUI.setItem(13, skillInfo);

        // Add active ability activation button (if unlocked)
        if (level >= getActiveSkillUnlockLevel(skillKey)) {
            detailGUI.setItem(15, createActiveSkillButton(skillKey));
        }

        // Add back button
        detailGUI.setItem(18, createExitButton());

        player.openInventory(detailGUI);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§aYour Skills")) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            switch (event.getSlot()) {
                case 10:
                    openSkillDetailMenu(player, "Mining", "mining", Material.IRON_PICKAXE);
                    break;
                case 11:
                    openSkillDetailMenu(player, "Logging", "logging", Material.IRON_AXE);
                    break;
                case 12:
                    openSkillDetailMenu(player, "Farming", "farming", Material.WHEAT);
                    break;
                case 13:
                    openSkillDetailMenu(player, "Fighting", "fighting", Material.IRON_SWORD);
                    break;
                case 14:
                    openSkillDetailMenu(player, "Fishing", "fishing", Material.FISHING_ROD);
                    break;
                case 15:
                    openSkillDetailMenu(player, "Enchanting", "enchanting", Material.ENCHANTING_TABLE);
                    break;
            }
        } else if (event.getView().getTitle().contains("Details")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            if (event.getSlot() == 18) {
                openSkillsMenu(player);
            } else if (event.getSlot() == 15) {
                // Activate skill when clicking the ability button
                activateSkill(player, event.getView().getTitle().split(" ")[0].toLowerCase());
            }
        }
    }

    private void activateSkill(Player player, String skill) {
        switch (skill) {
            case "mining":
                abilityManager.activateMiningBurst(player);
                break;
            case "logging":
                abilityManager.activateTimberChop(player);
                break;
            case "fighting":
                abilityManager.activateBerserkerRage(player);
                break;
            default:
                player.sendMessage("§cNo active skill available for this category.");
        }
    }

    private ItemStack createActiveSkillButton(String skill) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bActivate " + getActiveSkillName(skill));
            meta.setLore(List.of("§7Click to activate this skill!"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private String getSkillMilestone(String skill, int level) {
        return switch (skill.toLowerCase()) {
            case "mining" -> switch (level) {
                case 5 -> "§a+10% Mining XP Gain";
                case 10 -> "§aAuto-Smelt Ores";
                case 15 -> "§aDouble Ore Drops";
                case 25 -> "§6Fortune Boost";
                case 50 -> "§6Advanced Auto-Smelt";
                case 100 -> "§6Master Miner Status";
                default -> "§7None";
            };
            case "logging" -> switch (level) {
                case 5 -> "§a+10% Logging XP Gain";
                case 10 -> "§aFast Tree Chopping";
                case 15 -> "§aDouble Wood Drops";
                case 25 -> "§6Tree Growth Boost";
                case 50 -> "§6Triple Log Drops";
                case 100 -> "§6Master Logger Status";
                default -> "§7None";
            };
            case "farming" -> switch (level) {
                case 5 -> "§a+10% Farming XP Gain";
                case 10 -> "§aAuto-Replant Crops";
                case 15 -> "§aDouble Crop Yield";
                case 25 -> "§6Instant Growth Chance";
                case 50 -> "§6Auto-Harvest";
                case 100 -> "§6Master Farmer Status";
                default -> "§7None";
            };
            case "fighting" -> switch (level) {
                case 5 -> "§a+10% Damage";
                case 10 -> "§aHeal on Kill";
                case 15 -> "§aCritical Hit Bonus";
                case 25 -> "§6Lifesteal";
                case 50 -> "§6Damage Reduction";
                case 100 -> "§6Master Warrior Status";
                default -> "§7None";
            };
            default -> "§7None";
        };
    }


    private String getActiveSkillName(String skill) {
        switch (skill) {
            case "mining": return "Mining Burst";
            case "logging": return "Timber Chop";
            case "fighting": return "Berserker Rage";
            default: return "None";
        }
    }

    private int getActiveSkillUnlockLevel(String skill) {
        return 15;
    }

    private ItemStack createExitButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) meta.setDisplayName("§cBack to Skills Menu");
        item.setItemMeta(meta);
        return item;
    }
}
