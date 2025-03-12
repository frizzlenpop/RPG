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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SkillsGUI implements Listener {

    private final PlayerDataManager dataManager;
    private final XPManager xpManager;
    private final SkillAbilityManager abilityManager;
    private final PassiveSkillManager passiveManager; // New field

    // Map to store which page of passives each player is viewing
    private final Map<UUID, Integer> playerPassivePages = new HashMap<>();

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
        
        // Get skill level
        int skillLevel = dataManager.getSkillLevel(playerId, skillKey);

        // Add skill information at the top using a detailed icon
        ItemStack skillInfo = createSkillDetailIcon(icon, skillName, skillKey, playerId);
        menu.setItem(4, skillInfo);

        // Get the current page (default to first page)
        int currentPage = playerPassivePages.getOrDefault(playerId, 0);
        
        // Add passive skills section with pagination
        addPassiveSkillsWithPagination(menu, skillKey, skillLevel, playerId, currentPage);

        // Add navigation buttons
        addNavigationButtons(menu, skillKey, playerId, currentPage);

        // Add active skill activation button if available
        if (skillLevel >= getActiveSkillUnlockLevel(skillKey)) {
            ItemStack activeSkillButton = createActiveSkillButton(skillKey);
            menu.setItem(49, activeSkillButton);
        }

        // Add exit/back button
        menu.setItem(53, createExitButton());

        player.openInventory(menu);
    }

    // Creates a detailed skill icon for the detail menu (focuses on XP info and active skills)
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
            
            // Add skill level and XP information
            lore.add("§7Current Level: §a" + level);
            lore.add("§7Current XP: §e" + xp + "§7 / §b" + xpRequired);
            lore.add("§fProgress: §e" + (xp * 100 / xpRequired) + "%");
            lore.add("");
            
            // Add a note about passive skills
            lore.add("§ePassive Skills:");
            lore.add("§7View the passive skill items below");
            lore.add("§7to see your unlocked abilities.");
            lore.add("§7Unlock up to 40 passive skills");
            lore.add("§7as you increase your level.");
            lore.add("");
            
            // Add active skill info
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
                // Level 1-10
                passives.add(new PassiveSkillInfo("Mining Basics", 1, Material.WOODEN_PICKAXE,
                        "Basic mining techniques"));
                passives.add(new PassiveSkillInfo("Stone Efficiency", 3, Material.STONE_PICKAXE,
                        "Mine stone 10% faster"));
                passives.add(new PassiveSkillInfo("Double Ore Drop", 5, Material.DIAMOND_ORE,
                        "25% chance to receive double drops from mining ores"));
                passives.add(new PassiveSkillInfo("XP Boost I", 7, Material.EXPERIENCE_BOTTLE,
                        "+10% Mining XP Gain"));
                passives.add(new PassiveSkillInfo("Auto Smelt", 10, Material.FURNACE,
                        "Automatically smelt iron, gold, and copper ores"));
                
                // Level 11-20
                passives.add(new PassiveSkillInfo("Coal Specialization", 12, Material.COAL_ORE,
                        "Gain 20% more coal from coal ore"));
                passives.add(new PassiveSkillInfo("Iron Specialization", 14, Material.IRON_ORE,
                        "Gain 20% more iron from iron ore"));
                passives.add(new PassiveSkillInfo("Fortune Boost", 15, Material.DIAMOND,
                        "Increase fortune effect by 1 level"));
                passives.add(new PassiveSkillInfo("Miner's Haste", 17, Material.GOLDEN_PICKAXE,
                        "Gain Haste I for 5 seconds after mining an ore"));
                passives.add(new PassiveSkillInfo("Auto Smelt Upgrade", 20, Material.BLAST_FURNACE,
                        "20% chance to double smelted output"));
                
                // Level 21-30
                passives.add(new PassiveSkillInfo("Gold Specialization", 22, Material.GOLD_ORE,
                        "Gain 20% more gold from gold ore"));
                passives.add(new PassiveSkillInfo("XP Boost II", 24, Material.EXPERIENCE_BOTTLE,
                        "+15% Mining XP Gain"));
                passives.add(new PassiveSkillInfo("Night Vision", 25, Material.GOLDEN_CARROT,
                        "Gain Night Vision when mining below Y=30"));
                passives.add(new PassiveSkillInfo("Resource Finder", 27, Material.COMPASS,
                        "Compass occasionally points to nearby ores"));
                passives.add(new PassiveSkillInfo("Triple Ore Drop", 30, Material.EMERALD_ORE,
                        "10% chance to receive triple drops from mining ores"));
                
                // Level 31-40
                passives.add(new PassiveSkillInfo("Redstone Specialization", 32, Material.REDSTONE_ORE,
                        "Gain 20% more redstone from redstone ore"));
                passives.add(new PassiveSkillInfo("Lapis Specialization", 34, Material.LAPIS_ORE,
                        "Gain 20% more lapis from lapis ore"));
                passives.add(new PassiveSkillInfo("Advanced Fortune", 35, Material.DIAMOND,
                        "Fortune effect increased by 2 levels"));
                passives.add(new PassiveSkillInfo("Treasure Hunter", 37, Material.CHEST,
                        "Chance to find treasure when mining stone"));
                passives.add(new PassiveSkillInfo("Miner's Efficiency", 40, Material.POTION,
                        "Gain Efficiency effect when below Y=30"));
                
                // Level 41-50
                passives.add(new PassiveSkillInfo("Copper Specialization", 42, Material.COPPER_ORE,
                        "Gain 25% more copper from copper ore"));
                passives.add(new PassiveSkillInfo("Diamond Specialization", 44, Material.DIAMOND_ORE,
                        "Gain 15% more diamonds from diamond ore"));
                passives.add(new PassiveSkillInfo("XP Boost III", 45, Material.EXPERIENCE_BOTTLE,
                        "+20% Mining XP Gain"));
                passives.add(new PassiveSkillInfo("Unbreakable Tools", 47, Material.NETHERITE_PICKAXE,
                        "10% chance pickaxes don't lose durability"));
                passives.add(new PassiveSkillInfo("Master Smelter", 50, Material.BLAST_FURNACE,
                        "35% chance to double smelted output"));
                
                // Level 51-60
                passives.add(new PassiveSkillInfo("Emerald Specialization", 52, Material.EMERALD_ORE,
                        "Gain 20% more emeralds from emerald ore"));
                passives.add(new PassiveSkillInfo("Ore Vein Sensor", 54, Material.DAYLIGHT_DETECTOR,
                        "Detect nearby ore veins while mining"));
                passives.add(new PassiveSkillInfo("Quadruple Ore Drop", 55, Material.DIAMOND_BLOCK,
                        "5% chance to receive quadruple drops from mining ores"));
                passives.add(new PassiveSkillInfo("Excavation", 57, Material.IRON_SHOVEL,
                        "Mine a 3x3 area when mining stone"));
                passives.add(new PassiveSkillInfo("Nether Mining", 60, Material.ANCIENT_DEBRIS,
                        "15% increased ancient debris and nether ore drops"));
                
                // Level 61-70
                passives.add(new PassiveSkillInfo("Advanced Haste", 62, Material.CLOCK,
                        "Gain Haste II for 10 seconds after mining rare ores"));
                passives.add(new PassiveSkillInfo("XP Boost IV", 64, Material.EXPERIENCE_BOTTLE,
                        "+25% Mining XP Gain"));
                passives.add(new PassiveSkillInfo("Master Fortune", 65, Material.NETHERITE_BLOCK,
                        "Fortune effect increased by 3 levels"));
                passives.add(new PassiveSkillInfo("Ancient Debris Finder", 67, Material.NETHERITE_SCRAP,
                        "Occasionally highlights nearby ancient debris"));
                passives.add(new PassiveSkillInfo("Supernatural Luck", 70, Material.RABBIT_FOOT,
                        "5% chance to find rare items when mining"));
                
                // Level 71-80
                passives.add(new PassiveSkillInfo("Speed Mining", 72, Material.SUGAR,
                        "Gain Speed effect while mining below Y=20"));
                passives.add(new PassiveSkillInfo("Obsidian Specialist", 74, Material.OBSIDIAN,
                        "Mine obsidian 50% faster"));
                passives.add(new PassiveSkillInfo("Deepslate Expert", 75, Material.DEEPSLATE,
                        "Mine deepslate and related blocks 50% faster"));
                passives.add(new PassiveSkillInfo("Crystal Formation", 77, Material.AMETHYST_SHARD,
                        "Chance to create amethyst crystals when mining stone"));
                passives.add(new PassiveSkillInfo("Geode Finder", 80, Material.AMETHYST_CLUSTER,
                        "Detect nearby amethyst geodes while mining"));
                
                // Level 81-90
                passives.add(new PassiveSkillInfo("XP Boost V", 82, Material.EXPERIENCE_BOTTLE,
                        "+30% Mining XP Gain"));
                passives.add(new PassiveSkillInfo("Blacksmith", 84, Material.ANVIL,
                        "20% chance to repair tools slightly while mining"));
                passives.add(new PassiveSkillInfo("Tool Enchanter", 85, Material.ENCHANTED_BOOK,
                        "Occasionally enchant your pickaxe with random enchantments"));
                passives.add(new PassiveSkillInfo("Diamond Touch", 87, Material.DIAMOND_PICKAXE,
                        "2% chance to turn stone into diamond ore"));
                passives.add(new PassiveSkillInfo("Cave Master", 90, Material.CAVE_VINES,
                        "Breathe longer underwater and see better in darkness"));
                
                // Level 91-100
                passives.add(new PassiveSkillInfo("Dwarven Resilience", 92, Material.GOLDEN_APPLE,
                        "Gain Resistance effect while mining below Y=0"));
                passives.add(new PassiveSkillInfo("Legendary Fortune", 94, Material.DRAGON_EGG,
                        "Fortune effect increased by 4 levels"));
                passives.add(new PassiveSkillInfo("Ultimate Smelter", 95, Material.NETHERITE_INGOT,
                        "50% chance to double smelted output and 10% chance to triple"));
                passives.add(new PassiveSkillInfo("Excavation Master", 97, Material.BEACON,
                        "Occasionally mine in a 5x5 area"));
                passives.add(new PassiveSkillInfo("Master Miner", 100, Material.BEDROCK,
                        "Ultimate mining mastery with multiple bonuses"));
                break;

            case "logging":
                // Level 1-10
                passives.add(new PassiveSkillInfo("Logging Basics", 1, Material.WOODEN_AXE,
                        "Basic wood chopping techniques"));
                passives.add(new PassiveSkillInfo("Wood Efficiency", 3, Material.STONE_AXE,
                        "Chop wood 10% faster"));
                passives.add(new PassiveSkillInfo("Double Wood Drop", 5, Material.OAK_LOG,
                        "25% chance to receive double drops from chopping wood"));
                passives.add(new PassiveSkillInfo("XP Boost I", 7, Material.EXPERIENCE_BOTTLE,
                        "+10% Logging XP Gain"));
                passives.add(new PassiveSkillInfo("Tree Growth Boost", 10, Material.BONE_MEAL,
                        "Nearby trees grow 30% faster"));
                
                // Level 11-20
                passives.add(new PassiveSkillInfo("Oak Specialization", 12, Material.OAK_LOG,
                        "Gain 20% more oak logs from oak trees"));
                passives.add(new PassiveSkillInfo("Birch Specialization", 14, Material.BIRCH_LOG,
                        "Gain 20% more birch logs from birch trees"));
                passives.add(new PassiveSkillInfo("Triple Log Drop", 15, Material.SPRUCE_LOG,
                        "15% chance to receive triple drops from chopping wood"));
                passives.add(new PassiveSkillInfo("Logger's Haste", 17, Material.GOLDEN_AXE,
                        "Gain Haste I for 5 seconds after chopping wood"));
                passives.add(new PassiveSkillInfo("Sapling Collector", 20, Material.OAK_SAPLING,
                        "Increased sapling drop rate from leaves"));
                
                // Level 21-30
                passives.add(new PassiveSkillInfo("Spruce Specialization", 22, Material.SPRUCE_LOG,
                        "Gain 20% more spruce logs from spruce trees"));
                passives.add(new PassiveSkillInfo("XP Boost II", 24, Material.EXPERIENCE_BOTTLE,
                        "+15% Logging XP Gain"));
                passives.add(new PassiveSkillInfo("Timber!", 25, Material.DIAMOND_AXE,
                        "Chance to fell entire tree at once"));
                passives.add(new PassiveSkillInfo("Tree Finder", 27, Material.COMPASS,
                        "Compass occasionally points to rare tree types"));
                passives.add(new PassiveSkillInfo("Apple Harvester", 30, Material.APPLE,
                        "Increased chance of apples dropping from oak leaves"));
                
                // Level 31-40
                passives.add(new PassiveSkillInfo("Jungle Specialization", 32, Material.JUNGLE_LOG,
                        "Gain 20% more jungle logs from jungle trees"));
                passives.add(new PassiveSkillInfo("Acacia Specialization", 34, Material.ACACIA_LOG,
                        "Gain 20% more acacia logs from acacia trees"));
                passives.add(new PassiveSkillInfo("Quadruple Log Drop", 35, Material.DARK_OAK_LOG,
                        "5% chance to receive quadruple drops from chopping wood"));
                passives.add(new PassiveSkillInfo("Tree Planter", 37, Material.FLOWERING_AZALEA,
                        "Chance to automatically plant new saplings when chopping trees"));
                passives.add(new PassiveSkillInfo("Logger's Efficiency", 40, Material.POTION,
                        "Gain Efficiency effect in forests"));
                
                // Level 41-50
                passives.add(new PassiveSkillInfo("Dark Oak Specialization", 42, Material.DARK_OAK_LOG,
                        "Gain 20% more dark oak logs from dark oak trees"));
                passives.add(new PassiveSkillInfo("Mangrove Specialization", 44, Material.MANGROVE_LOG,
                        "Gain 20% more mangrove logs from mangrove trees"));
                passives.add(new PassiveSkillInfo("XP Boost III", 45, Material.EXPERIENCE_BOTTLE,
                        "+20% Logging XP Gain"));
                passives.add(new PassiveSkillInfo("Unbreakable Tools", 47, Material.NETHERITE_AXE,
                        "10% chance axes don't lose durability"));
                passives.add(new PassiveSkillInfo("Master Timber", 50, Material.STRIPPED_OAK_LOG,
                        "Improved chance to fell entire tree at once"));
                
                // Level 51-60
                passives.add(new PassiveSkillInfo("Stripped Log Expert", 52, Material.STRIPPED_SPRUCE_LOG,
                        "Gain extra items when stripping logs"));
                passives.add(new PassiveSkillInfo("Forest Sense", 54, Material.DAYLIGHT_DETECTOR,
                        "Detect nearby rare tree types"));
                passives.add(new PassiveSkillInfo("Cherry Specialization", 55, Material.CHERRY_LOG,
                        "Gain 20% more cherry logs from cherry trees"));
                passives.add(new PassiveSkillInfo("Mass Harvester", 57, Material.IRON_AXE,
                        "Chance to chop multiple trees at once"));
                passives.add(new PassiveSkillInfo("Azalea Expert", 60, Material.AZALEA,
                        "Gain special items when chopping azalea trees"));
                
                // Level 61-70
                passives.add(new PassiveSkillInfo("Advanced Haste", 62, Material.CLOCK,
                        "Gain Haste II for 10 seconds after chopping rare woods"));
                passives.add(new PassiveSkillInfo("XP Boost IV", 64, Material.EXPERIENCE_BOTTLE,
                        "+25% Logging XP Gain"));
                passives.add(new PassiveSkillInfo("Exotic Timber", 65, Material.CRIMSON_STEM,
                        "Ability to harvest nether stems more efficiently"));
                passives.add(new PassiveSkillInfo("Warped Specialization", 67, Material.WARPED_STEM,
                        "Gain 20% more warped stems"));
                passives.add(new PassiveSkillInfo("Hollow Tree Finder", 70, Material.BEE_NEST,
                        "Chance to find bee nests and special items inside trees"));
                
                // Level 71-80
                passives.add(new PassiveSkillInfo("Forest Speed", 72, Material.SUGAR,
                        "Gain Speed effect while in forests"));
                passives.add(new PassiveSkillInfo("Leaf Expert", 74, Material.OAK_LEAVES,
                        "Break leaves 50% faster"));
                passives.add(new PassiveSkillInfo("Crimson Specialization", 75, Material.CRIMSON_STEM,
                        "Gain 20% more crimson stems"));
                passives.add(new PassiveSkillInfo("Sap Collection", 77, Material.HONEY_BOTTLE,
                        "Chance to collect sap from trees"));
                passives.add(new PassiveSkillInfo("Ancient Tree Finder", 80, Material.LARGE_FERN,
                        "Detect giant trees and special forests"));
                
                // Level 81-90
                passives.add(new PassiveSkillInfo("XP Boost V", 82, Material.EXPERIENCE_BOTTLE,
                        "+30% Logging XP Gain"));
                passives.add(new PassiveSkillInfo("Tool Smith", 84, Material.ANVIL,
                        "20% chance to repair axes slightly while chopping"));
                passives.add(new PassiveSkillInfo("Axe Enchanter", 85, Material.ENCHANTED_BOOK,
                        "Occasionally enchant your axe with random enchantments"));
                passives.add(new PassiveSkillInfo("Golden Touch", 87, Material.GOLDEN_APPLE,
                        "2% chance to find golden apples in oak trees"));
                passives.add(new PassiveSkillInfo("Forest Master", 90, Material.AZALEA_LEAVES,
                        "Increased movement speed and jump height in forests"));
                
                // Level 91-100
                passives.add(new PassiveSkillInfo("Dryad's Blessing", 92, Material.TOTEM_OF_UNDYING,
                        "Gain Regeneration effect in forests"));
                passives.add(new PassiveSkillInfo("Legendary Harvester", 94, Material.BEACON,
                        "Chance to receive legendary wood items"));
                passives.add(new PassiveSkillInfo("Ultimate Timber", 95, Material.NETHERITE_AXE,
                        "Always chop entire trees at once"));
                passives.add(new PassiveSkillInfo("Forest Guardian", 97, Material.SPORE_BLOSSOM,
                        "Animals and monsters won't attack you in forests"));
                passives.add(new PassiveSkillInfo("Master Logger", 100, Material.JUNGLE_LOG,
                        "Ultimate logging mastery with multiple bonuses"));
                break;

            case "fighting":
                // Level 1-10
                passives.add(new PassiveSkillInfo("Combat Basics", 1, Material.WOODEN_SWORD,
                        "Basic combat knowledge"));
                passives.add(new PassiveSkillInfo("First Aid", 3, Material.APPLE,
                        "5% faster health regeneration"));
                passives.add(new PassiveSkillInfo("Lifesteal I", 5, Material.REDSTONE,
                        "3% chance to heal when dealing damage"));
                passives.add(new PassiveSkillInfo("Combat XP Boost I", 7, Material.EXPERIENCE_BOTTLE,
                        "+10% Combat XP Gain"));
                passives.add(new PassiveSkillInfo("Damage Reduction I", 10, Material.SHIELD,
                        "5% less damage from attacks"));
                
                // Level 11-20
                passives.add(new PassiveSkillInfo("Sword Specialist I", 12, Material.STONE_SWORD,
                        "5% increased sword damage"));
                passives.add(new PassiveSkillInfo("Heal on Kill I", 15, Material.GOLDEN_APPLE,
                        "Heal 1 heart when killing enemies"));
                passives.add(new PassiveSkillInfo("Axe Specialist I", 17, Material.STONE_AXE,
                        "5% increased axe damage"));
                passives.add(new PassiveSkillInfo("Critical Strike I", 20, Material.DIAMOND,
                        "5% increased critical hit chance"));
                
                // Level 21-30
                passives.add(new PassiveSkillInfo("Combat XP Boost II", 22, Material.EXPERIENCE_BOTTLE,
                        "+15% Combat XP Gain"));
                passives.add(new PassiveSkillInfo("Bow Specialist I", 25, Material.BOW,
                        "5% increased bow damage"));
                passives.add(new PassiveSkillInfo("Lifesteal II", 27, Material.REDSTONE,
                        "5% chance to heal when dealing damage"));
                passives.add(new PassiveSkillInfo("Damage Reduction II", 30, Material.SHIELD,
                        "10% less damage from attacks"));
                
                // Level 31-40
                passives.add(new PassiveSkillInfo("Sword Specialist II", 32, Material.IRON_SWORD,
                        "10% increased sword damage"));
                passives.add(new PassiveSkillInfo("Heal on Kill II", 35, Material.GOLDEN_APPLE,
                        "Heal 2 hearts when killing enemies"));
                passives.add(new PassiveSkillInfo("Axe Specialist II", 37, Material.IRON_AXE,
                        "10% increased axe damage"));
                passives.add(new PassiveSkillInfo("Critical Strike II", 40, Material.DIAMOND,
                        "10% increased critical hit chance"));
                
                // Level 41-50
                passives.add(new PassiveSkillInfo("Combat XP Boost III", 42, Material.EXPERIENCE_BOTTLE,
                        "+20% Combat XP Gain"));
                passives.add(new PassiveSkillInfo("Bow Specialist II", 45, Material.BOW,
                        "10% increased bow damage"));
                passives.add(new PassiveSkillInfo("Lifesteal III", 47, Material.REDSTONE,
                        "7% chance to heal when dealing damage"));
                passives.add(new PassiveSkillInfo("Knockback Resistance", 50, Material.NETHERITE_INGOT,
                        "15% knockback resistance"));
                
                // Level 51-60
                passives.add(new PassiveSkillInfo("Sword Specialist III", 52, Material.DIAMOND_SWORD,
                        "15% increased sword damage"));
                passives.add(new PassiveSkillInfo("Heal on Kill III", 55, Material.ENCHANTED_GOLDEN_APPLE,
                        "Heal 3 hearts when killing enemies"));
                passives.add(new PassiveSkillInfo("Axe Specialist III", 57, Material.DIAMOND_AXE,
                        "15% increased axe damage"));
                passives.add(new PassiveSkillInfo("Damage Reduction III", 60, Material.SHIELD,
                        "15% less damage from attacks"));
                
                // Level 61-70
                passives.add(new PassiveSkillInfo("Combat XP Boost IV", 62, Material.EXPERIENCE_BOTTLE,
                        "+25% Combat XP Gain"));
                passives.add(new PassiveSkillInfo("Bow Specialist III", 65, Material.BOW,
                        "15% increased bow damage"));
                passives.add(new PassiveSkillInfo("Critical Strike III", 67, Material.DIAMOND,
                        "15% increased critical hit chance"));
                passives.add(new PassiveSkillInfo("Battle Rage", 70, Material.BLAZE_POWDER,
                        "Gain Strength I for 5 seconds after killing an enemy"));
                
                // Level 71-80
                passives.add(new PassiveSkillInfo("Sword Specialist IV", 72, Material.NETHERITE_SWORD,
                        "20% increased sword damage"));
                passives.add(new PassiveSkillInfo("Lifesteal IV", 75, Material.REDSTONE,
                        "10% chance to heal when dealing damage"));
                passives.add(new PassiveSkillInfo("Axe Specialist IV", 77, Material.NETHERITE_AXE,
                        "20% increased axe damage"));
                passives.add(new PassiveSkillInfo("Damage Reduction IV", 80, Material.SHIELD,
                        "20% less damage from attacks"));
                
                // Level 81-90
                passives.add(new PassiveSkillInfo("Combat XP Boost V", 82, Material.EXPERIENCE_BOTTLE,
                        "+30% Combat XP Gain"));
                passives.add(new PassiveSkillInfo("Bow Specialist IV", 85, Material.BOW,
                        "20% increased bow damage"));
                passives.add(new PassiveSkillInfo("Heal on Kill IV", 87, Material.ENCHANTED_GOLDEN_APPLE,
                        "Heal 4 hearts when killing enemies"));
                passives.add(new PassiveSkillInfo("Critical Strike IV", 90, Material.DIAMOND,
                        "20% increased critical hit chance"));
                
                // Level 91-100
                passives.add(new PassiveSkillInfo("Combat Master", 92, Material.BEACON,
                        "All combat stats improved by 10%"));
                passives.add(new PassiveSkillInfo("Legendary Warrior", 95, Material.NETHERITE_SWORD,
                        "25% increased damage with all weapons"));
                passives.add(new PassiveSkillInfo("Berserker", 97, Material.TOTEM_OF_UNDYING,
                        "Gain Strength II and Speed I for 10 seconds when below 3 hearts"));
                passives.add(new PassiveSkillInfo("Master Fighter", 100, Material.DRAGON_HEAD,
                        "Ultimate combat mastery with multiple bonuses"));
                break;

            case "fishing":
                // Level 1-10
                passives.add(new PassiveSkillInfo("Fishing Basics", 1, Material.FISHING_ROD,
                        "Basic fishing knowledge"));
                passives.add(new PassiveSkillInfo("Bait Saver", 3, Material.STRING,
                        "10% chance to not consume bait when fishing"));
                passives.add(new PassiveSkillInfo("XP Boost I", 5, Material.EXPERIENCE_BOTTLE,
                        "+10% XP Boost when fishing"));
                passives.add(new PassiveSkillInfo("Fish Finder", 7, Material.COD,
                        "5% increased fish catch rate"));
                passives.add(new PassiveSkillInfo("Treasure Hunter I", 10, Material.CHEST,
                        "10% increased treasure catch rate"));
                
                // Level 11-20
                passives.add(new PassiveSkillInfo("Salmon Specialist", 12, Material.SALMON,
                        "15% increased salmon catch rate"));
                passives.add(new PassiveSkillInfo("Rare Fish Master I", 15, Material.PUFFERFISH,
                        "Unlocks rare fish catches"));
                passives.add(new PassiveSkillInfo("Tropical Fish Specialist", 17, Material.TROPICAL_FISH,
                        "15% increased tropical fish catch rate"));
                passives.add(new PassiveSkillInfo("Quick Hook I", 20, Material.FISHING_ROD,
                        "10% faster hook time"));
                
                // Level 21-30
                passives.add(new PassiveSkillInfo("XP Boost II", 22, Material.EXPERIENCE_BOTTLE,
                        "+15% XP Boost when fishing"));
                passives.add(new PassiveSkillInfo("Junk Reducer I", 25, Material.LILY_PAD,
                        "15% less junk items when fishing"));
                passives.add(new PassiveSkillInfo("Treasure Hunter II", 27, Material.CHEST,
                        "15% increased treasure catch rate"));
                passives.add(new PassiveSkillInfo("Water Breathing", 30, Material.WATER_BUCKET,
                        "Gain Water Breathing effect while fishing"));
                
                // Level 31-40
                passives.add(new PassiveSkillInfo("Double Catch I", 32, Material.COD,
                        "10% chance to catch two fish at once"));
                passives.add(new PassiveSkillInfo("Enchanted Book Fisher I", 35, Material.ENCHANTED_BOOK,
                        "5% increased chance to catch enchanted books"));
                passives.add(new PassiveSkillInfo("Quick Hook II", 37, Material.FISHING_ROD,
                        "15% faster hook time"));
                passives.add(new PassiveSkillInfo("Rare Fish Master II", 40, Material.PUFFERFISH,
                        "Increased chance for rare fish varieties"));
                
                // Level 41-50
                passives.add(new PassiveSkillInfo("XP Boost III", 42, Material.EXPERIENCE_BOTTLE,
                        "+20% XP Boost when fishing"));
                passives.add(new PassiveSkillInfo("Junk Reducer II", 45, Material.LILY_PAD,
                        "25% less junk items when fishing"));
                passives.add(new PassiveSkillInfo("Treasure Hunter III", 47, Material.CHEST,
                        "20% increased treasure catch rate"));
                passives.add(new PassiveSkillInfo("Night Fisher", 50, Material.CLOCK,
                        "25% increased catch rate at night"));
                
                // Level 51-60
                passives.add(new PassiveSkillInfo("Double Catch II", 52, Material.COD,
                        "15% chance to catch two fish at once"));
                passives.add(new PassiveSkillInfo("Enchanted Book Fisher II", 55, Material.ENCHANTED_BOOK,
                        "10% increased chance to catch enchanted books"));
                passives.add(new PassiveSkillInfo("Quick Hook III", 57, Material.FISHING_ROD,
                        "20% faster hook time"));
                passives.add(new PassiveSkillInfo("XP Boost IV", 60, Material.EXPERIENCE_BOTTLE,
                        "+25% XP Boost when fishing"));
                
                // Level 61-70
                passives.add(new PassiveSkillInfo("Junk Reducer III", 62, Material.LILY_PAD,
                        "35% less junk items when fishing"));
                passives.add(new PassiveSkillInfo("Treasure Hunter IV", 65, Material.CHEST,
                        "25% increased treasure catch rate"));
                passives.add(new PassiveSkillInfo("Rain Fisher", 67, Material.WATER_BUCKET,
                        "25% increased catch rate during rain"));
                passives.add(new PassiveSkillInfo("Master Angler", 70, Material.FISHING_ROD,
                        "All fishing stats improved by 10%"));
                
                // Level 71-80
                passives.add(new PassiveSkillInfo("XP Boost V", 72, Material.EXPERIENCE_BOTTLE,
                        "+30% XP Boost when fishing"));
                passives.add(new PassiveSkillInfo("Double Catch III", 75, Material.COD,
                        "20% chance to catch two fish at once"));
                passives.add(new PassiveSkillInfo("Enchanted Book Fisher III", 77, Material.ENCHANTED_BOOK,
                        "15% increased chance to catch enchanted books"));
                passives.add(new PassiveSkillInfo("Ocean Explorer", 80, Material.HEART_OF_THE_SEA,
                        "Chance to find rare ocean treasures"));
                
                // Level 81-90
                passives.add(new PassiveSkillInfo("Junk Reducer IV", 82, Material.LILY_PAD,
                        "50% less junk items when fishing"));
                passives.add(new PassiveSkillInfo("Treasure Hunter V", 85, Material.CHEST,
                        "30% increased treasure catch rate"));
                passives.add(new PassiveSkillInfo("Quick Hook IV", 87, Material.FISHING_ROD,
                        "25% faster hook time"));
                passives.add(new PassiveSkillInfo("Triple Catch", 90, Material.COD,
                        "5% chance to catch three fish at once"));
                
                // Level 91-100
                passives.add(new PassiveSkillInfo("XP Boost VI", 92, Material.EXPERIENCE_BOTTLE,
                        "+40% XP Boost when fishing"));
                passives.add(new PassiveSkillInfo("Legendary Fisher", 95, Material.NETHERITE_INGOT,
                        "All fishing yields increased by 25%"));
                passives.add(new PassiveSkillInfo("Ancient Treasures", 97, Material.NAUTILUS_SHELL,
                        "Chance to find ancient artifacts while fishing"));
                passives.add(new PassiveSkillInfo("Master Fisher", 100, Material.TRIDENT,
                        "Ultimate fishing mastery with multiple bonuses"));
                break;
            case "enchanting":
                // Level 1-10
                passives.add(new PassiveSkillInfo("Enchanting Basics", 1, Material.BOOK,
                        "Basic enchanting knowledge"));
                passives.add(new PassiveSkillInfo("Lapis Saver I", 3, Material.LAPIS_LAZULI,
                        "10% chance to not consume lapis when enchanting"));
                passives.add(new PassiveSkillInfo("Research Master I", 5, Material.BOOK,
                        "15% more XP from research"));
                passives.add(new PassiveSkillInfo("Enchanting XP Boost I", 7, Material.EXPERIENCE_BOTTLE,
                        "+10% Enchanting XP Gain"));
                passives.add(new PassiveSkillInfo("Book Upgrade I", 10, Material.ENCHANTED_BOOK,
                        "5% chance to upgrade enchanted books"));
                
                // Level 11-20
                passives.add(new PassiveSkillInfo("Efficiency Specialist", 12, Material.DIAMOND_PICKAXE,
                        "10% increased chance for Efficiency enchantments"));
                passives.add(new PassiveSkillInfo("Custom Enchants I", 15, Material.EXPERIENCE_BOTTLE,
                        "Unlocks basic exclusive enchantments"));
                passives.add(new PassiveSkillInfo("Protection Specialist", 17, Material.DIAMOND_CHESTPLATE,
                        "10% increased chance for Protection enchantments"));
                passives.add(new PassiveSkillInfo("Rare Enchant Boost I", 20, Material.DIAMOND,
                        "5% higher chance of rare enchantments"));
                
                // Level 21-30
                passives.add(new PassiveSkillInfo("Enchanting XP Boost II", 22, Material.EXPERIENCE_BOTTLE,
                        "+15% Enchanting XP Gain"));
                passives.add(new PassiveSkillInfo("Lapis Saver II", 25, Material.LAPIS_LAZULI,
                        "15% chance to not consume lapis when enchanting"));
                passives.add(new PassiveSkillInfo("Research Master II", 27, Material.BOOK,
                        "25% more XP from research"));
                passives.add(new PassiveSkillInfo("Sharpness Specialist", 30, Material.DIAMOND_SWORD,
                        "10% increased chance for Sharpness enchantments"));
                
                // Level 31-40
                passives.add(new PassiveSkillInfo("Book Upgrade II", 32, Material.ENCHANTED_BOOK,
                        "10% chance to upgrade enchanted books"));
                passives.add(new PassiveSkillInfo("Custom Enchants II", 35, Material.EXPERIENCE_BOTTLE,
                        "Unlocks intermediate exclusive enchantments"));
                passives.add(new PassiveSkillInfo("Fortune Specialist", 37, Material.DIAMOND_PICKAXE,
                        "10% increased chance for Fortune enchantments"));
                passives.add(new PassiveSkillInfo("Rare Enchant Boost II", 40, Material.DIAMOND,
                        "10% higher chance of rare enchantments"));
                
                // Level 41-50
                passives.add(new PassiveSkillInfo("Enchanting XP Boost III", 42, Material.EXPERIENCE_BOTTLE,
                        "+20% Enchanting XP Gain"));
                passives.add(new PassiveSkillInfo("Lapis Saver III", 45, Material.LAPIS_LAZULI,
                        "20% chance to not consume lapis when enchanting"));
                passives.add(new PassiveSkillInfo("Research Master III", 47, Material.BOOK,
                        "35% more XP from research"));
                passives.add(new PassiveSkillInfo("Looting Specialist", 50, Material.DIAMOND_SWORD,
                        "10% increased chance for Looting enchantments"));
                
                // Level 51-60
                passives.add(new PassiveSkillInfo("Book Upgrade III", 52, Material.ENCHANTED_BOOK,
                        "15% chance to upgrade enchanted books"));
                passives.add(new PassiveSkillInfo("Custom Enchants III", 55, Material.EXPERIENCE_BOTTLE,
                        "Unlocks advanced exclusive enchantments"));
                passives.add(new PassiveSkillInfo("Silk Touch Specialist", 57, Material.DIAMOND_PICKAXE,
                        "15% increased chance for Silk Touch enchantments"));
                passives.add(new PassiveSkillInfo("Rare Enchant Boost III", 60, Material.DIAMOND,
                        "15% higher chance of rare enchantments"));
                
                // Level 61-70
                passives.add(new PassiveSkillInfo("Enchanting XP Boost IV", 62, Material.EXPERIENCE_BOTTLE,
                        "+25% Enchanting XP Gain"));
                passives.add(new PassiveSkillInfo("Lapis Saver IV", 65, Material.LAPIS_LAZULI,
                        "25% chance to not consume lapis when enchanting"));
                passives.add(new PassiveSkillInfo("Research Master IV", 67, Material.BOOK,
                        "45% more XP from research"));
                passives.add(new PassiveSkillInfo("Power Specialist", 70, Material.BOW,
                        "10% increased chance for Power enchantments"));
                
                // Level 71-80
                passives.add(new PassiveSkillInfo("Book Upgrade IV", 72, Material.ENCHANTED_BOOK,
                        "20% chance to upgrade enchanted books"));
                passives.add(new PassiveSkillInfo("Custom Enchants IV", 75, Material.EXPERIENCE_BOTTLE,
                        "Unlocks expert exclusive enchantments"));
                passives.add(new PassiveSkillInfo("Mending Specialist", 77, Material.EXPERIENCE_BOTTLE,
                        "15% increased chance for Mending enchantments"));
                passives.add(new PassiveSkillInfo("Rare Enchant Boost IV", 80, Material.DIAMOND,
                        "20% higher chance of rare enchantments"));
                
                // Level 81-90
                passives.add(new PassiveSkillInfo("Enchanting XP Boost V", 82, Material.EXPERIENCE_BOTTLE,
                        "+30% Enchanting XP Gain"));
                passives.add(new PassiveSkillInfo("Lapis Saver V", 85, Material.LAPIS_LAZULI,
                        "30% chance to not consume lapis when enchanting"));
                passives.add(new PassiveSkillInfo("Research Master V", 87, Material.BOOK,
                        "55% more XP from research"));
                passives.add(new PassiveSkillInfo("Unbreaking Specialist", 90, Material.ANVIL,
                        "15% increased chance for Unbreaking enchantments"));
                
                // Level 91-100
                passives.add(new PassiveSkillInfo("Book Upgrade V", 92, Material.ENCHANTED_BOOK,
                        "25% chance to upgrade enchanted books"));
                passives.add(new PassiveSkillInfo("Legendary Enchanter", 95, Material.NETHERITE_INGOT,
                        "All enchantments are 1 level higher"));
                passives.add(new PassiveSkillInfo("Custom Enchants V", 97, Material.NETHER_STAR,
                        "Unlocks legendary exclusive enchantments"));
                passives.add(new PassiveSkillInfo("Master Enchanter", 100, Material.END_CRYSTAL,
                        "Ultimate enchanting mastery with multiple bonuses"));
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
                    // Reset page view when opening a skill
                    playerPassivePages.put(player.getUniqueId(), 0);
                    openSkillDetailMenu(player, "Mining", "mining", Material.IRON_PICKAXE);
                    break;
                case 11:
                    playerPassivePages.put(player.getUniqueId(), 0);
                    openSkillDetailMenu(player, "Logging", "logging", Material.IRON_AXE);
                    break;
                case 12:
                    playerPassivePages.put(player.getUniqueId(), 0);
                    openSkillDetailMenu(player, "Farming", "farming", Material.WHEAT);
                    break;
                case 13:
                    playerPassivePages.put(player.getUniqueId(), 0);
                    openSkillDetailMenu(player, "Fighting", "fighting", Material.IRON_SWORD);
                    break;
                case 14:
                    playerPassivePages.put(player.getUniqueId(), 0);
                    openSkillDetailMenu(player, "Fishing", "fishing", Material.FISHING_ROD);
                    break;
                case 15:
                    playerPassivePages.put(player.getUniqueId(), 0);
                    openSkillDetailMenu(player, "Enchanting", "enchanting", Material.ENCHANTING_TABLE);
                    break;
            }
        } else if (title.contains("Details")) {
            event.setCancelled(true);
            UUID playerId = player.getUniqueId();
            int currentPage = playerPassivePages.getOrDefault(playerId, 0);
            String skillKey = title.replace(ChatColor.DARK_PURPLE.toString(), "").replace(" Details", "").toLowerCase();
            
            // Handle navigation buttons
            switch (event.getSlot()) {
                case 48: // Previous page
                    if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.ARROW) {
                        if (currentPage > 0) {
                            playerPassivePages.put(playerId, currentPage - 1);
                            openSkillDetailMenu(player, toTitleCase(skillKey), skillKey, getSkillMaterial(skillKey));
                        }
                    }
                    break;
                case 50: // Next page
                    if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.ARROW) {
                        List<PassiveSkillInfo> allPassives = getPassiveSkillsForSkill(skillKey);
                        int totalPages = (int) Math.ceil(allPassives.size() / 21.0);
                        if (currentPage < totalPages - 1) {
                            playerPassivePages.put(playerId, currentPage + 1);
                            openSkillDetailMenu(player, toTitleCase(skillKey), skillKey, getSkillMaterial(skillKey));
                        }
                    }
                    break;
                case 53: // Exit button
                    openSkillsMenu(player);
                    break;
                case 49: // Active skill button
                    activateSkill(player, skillKey);
                    break;
            }
        }
    }

    // Helper method to get skill material
    private Material getSkillMaterial(String skillKey) {
        return switch (skillKey.toLowerCase()) {
            case "mining" -> Material.IRON_PICKAXE;
            case "logging" -> Material.IRON_AXE;
            case "farming" -> Material.WHEAT;
            case "fighting" -> Material.IRON_SWORD;
            case "fishing" -> Material.FISHING_ROD;
            case "enchanting" -> Material.ENCHANTING_TABLE;
            default -> Material.BOOK;
        };
    }
    
    // Helper method to convert skill key to title case
    private String toTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
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

    // Add the pagination buttons
    private void addNavigationButtons(Inventory menu, String skillKey, UUID playerId, int currentPage) {
        List<PassiveSkillInfo> allPassives = getPassiveSkillsForSkill(skillKey);
        int totalPages = (int) Math.ceil(allPassives.size() / 21.0); // 21 slots per page
        
        // Previous page button (if not on first page)
        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(ChatColor.YELLOW + "Previous Page");
                prevButton.setItemMeta(prevMeta);
            }
            menu.setItem(48, prevButton);
        }
        
        // Next page button (if not on last page)
        if (currentPage < totalPages - 1) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(ChatColor.YELLOW + "Next Page");
                nextButton.setItemMeta(nextMeta);
            }
            menu.setItem(50, nextButton);
        }
        
        // Current page indicator
        ItemStack pageIndicator = new ItemStack(Material.PAPER);
        ItemMeta pageMeta = pageIndicator.getItemMeta();
        if (pageMeta != null) {
            pageMeta.setDisplayName(ChatColor.GOLD + "Page " + (currentPage + 1) + " of " + totalPages);
            pageIndicator.setItemMeta(pageMeta);
        }
        menu.setItem(45, pageIndicator);
    }

    // Adds passive skills with pagination support
    private void addPassiveSkillsWithPagination(Inventory menu, String skillKey, int skillLevel, UUID playerId, int page) {
        List<PassiveSkillInfo> passives = getPassiveSkillsForSkill(skillKey);
        
        // Calculate pagination
        int passivesPerPage = 21; // 3 rows of 7 slots each
        int startIndex = page * passivesPerPage;
        int endIndex = Math.min(startIndex + passivesPerPage, passives.size());
        
        // Calculate which passives to show on this page
        List<PassiveSkillInfo> pagePassives = passives.subList(startIndex, endIndex);
        
        // Slots for passives: use a grid in the middle of the inventory (rows 1-3, cols 1-7)
        int[] slots = {
            10, 11, 12, 13, 14, 15, 16,   // Row 1
            19, 20, 21, 22, 23, 24, 25,   // Row 2
            28, 29, 30, 31, 32, 33, 34    // Row 3
        };
        
        // Add passives to slots
        for (int i = 0; i < pagePassives.size(); i++) {
            PassiveSkillInfo passive = pagePassives.get(i);
            ItemStack passiveIcon = createPassiveSkillIcon(passive, skillLevel, playerId);
            menu.setItem(slots[i], passiveIcon);
        }
    }
}
