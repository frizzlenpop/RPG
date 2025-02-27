package org.frizzlenpop.rPGSkillsPlugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.frizzlenpop.rPGSkillsPlugin.skills.PassiveSkillManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.SkillAbilityManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkillsGUI implements Listener {

    private final PlayerDataManager dataManager;
    private final XPManager xpManager;
    private final SkillAbilityManager abilityManager;
    private final PassiveSkillManager passiveManager; // New field

    public SkillsGUI(PlayerDataManager dataManager, XPManager xpManager,
                     SkillAbilityManager abilityManager, PassiveSkillManager passiveManager) {
        this.dataManager = dataManager;
        this.xpManager = xpManager;
        this.abilityManager = abilityManager;
        this.passiveManager = passiveManager;
    }

    // Main Skills Menu (unchanged)
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

    // Original method used for the main menu icons (unchanged)
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

    // New detail menu that combines original skill details and passive skills
    private void openSkillDetailMenu(Player player, String skillName, String skillKey, Material icon) {
        // Create a 54-slot inventory with a title including "Details"
        Inventory menu = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + skillName + " Details");
        UUID playerId = player.getUniqueId();
        // Assume dataManager now provides a method to directly get the skill level
        int skillLevel = dataManager.getSkillLevel(playerId, skillKey);

        // Add skill information at the top using a detailed icon (includes milestones and active skill info)
        ItemStack skillInfo = createSkillDetailIcon(icon, skillName, skillKey, playerId);
        menu.setItem(4, skillInfo);

        // Add passive skills section
        addPassiveSkills(menu, skillKey, skillLevel, playerId);

        // Add active skill activation button if available
        if (skillLevel >= getActiveSkillUnlockLevel(skillKey)) {
            ItemStack activeSkillButton = createActiveSkillButton(skillKey);
            menu.setItem(49, activeSkillButton);
        }

        // Add exit/back button
        menu.setItem(53, createExitButton());

        player.openInventory(menu);
    }

    // Creates a detailed skill icon for the detail menu (incorporates original milestone and XP info)
    private ItemStack createSkillDetailIcon(Material material, String skillName, String skillKey, UUID playerId) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6" + skillName);
            List<String> lore = new ArrayList<>();
            FileConfiguration config = dataManager.getPlayerData(playerId);
            int level = config.getInt("skills." + skillKey + ".level", 1);
            int xp = config.getInt("skills." + skillKey + ".xp", 0);
            int xpRequired = (int) Math.pow(level, 1.5) * 100;
            lore.add("§7Current Level: §a" + level);
            lore.add("§7Current XP: §e" + xp + "§7 / §b" + xpRequired);
            lore.add("§fProgress: §e" + (xp * 100 / xpRequired) + "%");
            lore.add("");
            lore.add("§eUnlocked Perks:");
            int[] milestones = {5, 10, 15, 25, 50, 100};
            for (int milestone : milestones) {
                String milestoneText = getSkillMilestone(skillKey, milestone);
                if (level >= milestone) {
                    lore.add("§a✓ Level " + milestone + ": " + milestoneText);
                } else {
                    lore.add("§c✗ Level " + milestone + ": " + milestoneText);
                }
            }
            lore.add("");
            lore.add("§bActive Skill: " + getActiveSkillName(skillKey));
            lore.add("§7Unlocks at Level " + getActiveSkillUnlockLevel(skillKey));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    // New methods for the passive skills system
    private void addPassiveSkills(Inventory menu, String skillKey, int skillLevel, UUID playerId) {
        List<PassiveSkillInfo> passives = getPassiveSkillsForSkill(skillKey);
        int slot = 19; // Starting slot for passive skills

        for (PassiveSkillInfo passive : passives) {
            ItemStack passiveIcon = createPassiveSkillIcon(passive, skillLevel, playerId);
            menu.setItem(slot++, passiveIcon);
        }
    }

    private ItemStack createPassiveSkillIcon(PassiveSkillInfo passive, int skillLevel, UUID playerId) {
        ItemStack icon = new ItemStack(passive.getIcon());
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            boolean isUnlocked = skillLevel >= passive.getRequiredLevel();
            String name = (isUnlocked ? ChatColor.GREEN : ChatColor.GRAY) + passive.getName();
            meta.setDisplayName(name);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.YELLOW + "Required Level: " + passive.getRequiredLevel());
            lore.add("");

            if (isUnlocked) {
                lore.add(ChatColor.GREEN + "✓ Unlocked");
                lore.add("");
                lore.add(ChatColor.WHITE + passive.getDescription());
            } else {
                lore.add(ChatColor.RED + "✗ Locked");
                lore.add("");
                lore.add(ChatColor.GRAY + passive.getDescription());
            }
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    private List<PassiveSkillInfo> getPassiveSkillsForSkill(String skillKey) {
        List<PassiveSkillInfo> passives = new ArrayList<>();

        switch (skillKey.toLowerCase()) {
            case "mining":
                passives.add(new PassiveSkillInfo("Double Ore Drop", 5, Material.DIAMOND_ORE,
                        "Chance to receive double drops from mining ores"));
                passives.add(new PassiveSkillInfo("Auto Smelt", 10, Material.FURNACE,
                        "Automatically smelt ores while mining"));
                passives.add(new PassiveSkillInfo("Fortune Boost", 15, Material.DIAMOND,
                        "Increased fortune effect while mining"));
                passives.add(new PassiveSkillInfo("Auto Smelt Upgrade", 20, Material.BLAST_FURNACE,
                        "Improved auto-smelting efficiency"));
                break;

            case "logging":
                passives.add(new PassiveSkillInfo("Double Wood Drop", 5, Material.OAK_LOG,
                        "Chance to receive double drops from chopping wood"));
                passives.add(new PassiveSkillInfo("Tree Growth Boost", 10, Material.BONE_MEAL,
                        "Nearby trees grow faster"));
                passives.add(new PassiveSkillInfo("Triple Log Drop", 15, Material.SPRUCE_LOG,
                        "Chance to receive triple drops from chopping wood"));
                break;

            case "fighting":
                passives.add(new PassiveSkillInfo("Lifesteal", 5, Material.REDSTONE,
                        "Chance to heal when dealing damage"));
                passives.add(new PassiveSkillInfo("Damage Reduction", 10, Material.SHIELD,
                        "Take less damage from attacks"));
                passives.add(new PassiveSkillInfo("Heal on Kill", 15, Material.GOLDEN_APPLE,
                        "Heal when killing enemies"));
                break;
            case "fishing":
                passives.add(new PassiveSkillInfo("XP Boost", 5, Material.EXPERIENCE_BOTTLE,
                        "+10% XP Boost when fishing"));
                passives.add(new PassiveSkillInfo("Treasure Hunter", 10, Material.CHEST,
                        "15% increased treasure catch rate"));
                passives.add(new PassiveSkillInfo("Rare Fish Master", 15, Material.PUFFERFISH,
                        "Unlocks rare fish catches"));
                passives.add(new PassiveSkillInfo("Quick Hook", 20, Material.FISHING_ROD,
                        "10% faster hook time"));
                break;
            case "farming":
                passives.add(new PassiveSkillInfo("Farming XP Boost", 5, Material.EXPERIENCE_BOTTLE,
                        "+10% XP Boost when harvesting crops"));
                passives.add(new PassiveSkillInfo("Auto Replant", 10, Material.WHEAT_SEEDS,
                        "Automatically replants harvested crops"));
                passives.add(new PassiveSkillInfo("Double Harvest", 15, Material.WHEAT,
                        "Chance for double crop yields"));
                passives.add(new PassiveSkillInfo("Growth Boost", 20, Material.BONE_MEAL,
                        "Crops grow 10% faster"));
                break;
            case "enchanting":
                passives.add(new PassiveSkillInfo("Research Master", 5, Material.BOOK,
                        "25% more XP from research"));
                passives.add(new PassiveSkillInfo("Book Upgrade", 10, Material.ENCHANTED_BOOK,
                        "Chance to upgrade enchanted books"));
                passives.add(new PassiveSkillInfo("Custom Enchants", 15, Material.EXPERIENCE_BOTTLE,
                        "Unlocks exclusive enchantments"));
                passives.add(new PassiveSkillInfo("Rare Enchant Boost", 20, Material.DIAMOND,
                        "Higher chance of rare enchantments"));
                break;
        }

        return passives;
    }

    // Helper class to store passive skill information
    private static class PassiveSkillInfo {
        private final String name;
        private final int requiredLevel;
        private final Material icon;
        private final String description;

        public PassiveSkillInfo(String name, int requiredLevel, Material icon, String description) {
            this.name = name;
            this.requiredLevel = requiredLevel;
            this.icon = icon;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public int getRequiredLevel() {
            return requiredLevel;
        }

        public Material getIcon() {
            return icon;
        }

        public String getDescription() {
            return description;
        }
    }

    // Inventory click handler (updated for new detail menu layout)
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();

        if (title.equals("§aYour Skills")) {
            event.setCancelled(true);
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
        } else if (title.contains("Details")) {
            event.setCancelled(true);
            // For the detail menu, use the new slot numbers:
            // Exit button is in slot 53 and active skill button (if present) is in slot 49.
            if (event.getSlot() == 53) {
                openSkillsMenu(player);
            } else if (event.getSlot() == 49) {
                // Activate active skill when clicking the ability button.
                // Derive the skill key from the title by removing color codes and extra text.
                String skillKey = title.replace(ChatColor.DARK_PURPLE.toString(), "").replace(" Details", "").toLowerCase();
                activateSkill(player, skillKey);
            }
        }
    }

    // Activates the active ability for a given skill
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
        if (meta != null) {
            meta.setDisplayName("§cBack to Skills Menu");
            item.setItemMeta(meta);
        }
        return item;
    }
}
