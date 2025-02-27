package org.frizzlenpop.rPGSkillsPlugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

import java.util.*;

public class PassiveSkillManager implements Listener {
    // New system: tracking passives unlocked via config
    private final Map<UUID, Map<String, Set<String>>> playerPassives = new HashMap<>();

    // Old system: active passive effects loaded from player data
    private final Map<UUID, Set<String>> activePassives;

    // Sets for various passive effect implementations
    private final Set<UUID> autoSmeltPlayers = new HashSet<>();
    private final Set<UUID> autoReplantPlayers = new HashSet<>();
    private final Set<UUID> doubleOreDropPlayers = new HashSet<>();
    private final Set<UUID> doubleWoodDropPlayers = new HashSet<>();
    private final Set<UUID> doubleCropYieldPlayers = new HashSet<>();
    private final Set<UUID> healOnKillPlayers = new HashSet<>();

    // Higher-level passives
    private final Set<UUID> fortuneBoostPlayers = new HashSet<>();
    private final Set<UUID> autoSmeltUpgradePlayers = new HashSet<>();
    private final Set<UUID> treeGrowthBoostPlayers = new HashSet<>();
    private final Set<UUID> tripleLogDropPlayers = new HashSet<>();
    private final Set<UUID> instantGrowthPlayers = new HashSet<>();
    private final Set<UUID> autoHarvestPlayers = new HashSet<>();
    private final Set<UUID> lifestealPlayers = new HashSet<>();
    private final Set<UUID> damageReductionPlayers = new HashSet<>();

    private final XPManager xpManager;
    private final RPGSkillsPlugin plugin; // Needed for scheduling and config

    public PassiveSkillManager(XPManager xpManager, RPGSkillsPlugin plugin) {
        this.xpManager = xpManager;
        this.plugin = plugin;
        this.activePassives = new HashMap<>();

        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Load active passives for online players (old system)
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            loadPlayerPassives(player);
        }
    }

    // --- OLD SYSTEM: Loading player passives from PlayerDataManager ---
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayerPassives(event.getPlayer());
    }

    private void loadPlayerPassives(Player player) {
        UUID playerId = player.getUniqueId();
        Set<String> passives = new HashSet<>();

        // Retrieve skill levels from your PlayerDataManager
        int miningLevel = plugin.getPlayerDataManager().getSkillLevel(playerId, "mining");
        int loggingLevel = plugin.getPlayerDataManager().getSkillLevel(playerId, "logging");
        int farmingLevel = plugin.getPlayerDataManager().getSkillLevel(playerId, "farming");
        int fightingLevel = plugin.getPlayerDataManager().getSkillLevel(playerId, "fighting");

        // Mining passives
        if (miningLevel >= 5) {
            passives.add("double_ore_drop");
            doubleOreDropPlayers.add(playerId);
        }
        if (miningLevel >= 10) {
            passives.add("auto_smelt");
            autoSmeltPlayers.add(playerId);
        }
        if (miningLevel >= 15) {
            passives.add("fortune_boost");
            fortuneBoostPlayers.add(playerId);
        }
        if (miningLevel >= 20) {
            passives.add("auto_smelt_upgrade");
            autoSmeltUpgradePlayers.add(playerId);
        }

        // Logging passives
        if (loggingLevel >= 5) {
            passives.add("double_wood_drop");
            doubleWoodDropPlayers.add(playerId);
        }
        if (loggingLevel >= 10) {
            passives.add("tree_growth_boost");
            treeGrowthBoostPlayers.add(playerId);
        }
        if (loggingLevel >= 15) {
            passives.add("triple_log_drop");
            tripleLogDropPlayers.add(playerId);
        }

        // Farming passives
        if (farmingLevel >= 5) {
            passives.add("double_crop_yield");
            doubleCropYieldPlayers.add(playerId);
        }
        if (farmingLevel >= 10) {
            passives.add("auto_replant");
            autoReplantPlayers.add(playerId);
        }
        if (farmingLevel >= 15) {
            passives.add("instant_growth");
            instantGrowthPlayers.add(playerId);
        }

        // Fighting passives
        if (fightingLevel >= 5) {
            passives.add("lifesteal");
            lifestealPlayers.add(playerId);
        }
        if (fightingLevel >= 10) {
            passives.add("damage_reduction");
            damageReductionPlayers.add(playerId);
        }
        if (fightingLevel >= 15) {
            passives.add("heal_on_kill");
            healOnKillPlayers.add(playerId);
        }

        activePassives.put(playerId, passives);
    }

    public boolean hasPassive(UUID playerId, String passive) {
        return activePassives.containsKey(playerId) && activePassives.get(playerId).contains(passive);
    }

    public Set<String> getPlayerPassives(UUID playerId) {
        return activePassives.getOrDefault(playerId, new HashSet<>());
    }

    public void addPassive(UUID playerId, String passive) {
        activePassives.computeIfAbsent(playerId, k -> new HashSet<>()).add(passive);
    }

    public void removePassive(UUID playerId, String passive) {
        if (activePassives.containsKey(playerId)) {
            activePassives.get(playerId).remove(passive);
        }
    }

    public void applyPassiveEffect(Player player, String effect) {
        UUID playerId = player.getUniqueId();
        switch (effect.toLowerCase()) {
            case "auto_smelt":
                autoSmeltPlayers.add(playerId);
                break;
            case "auto_replant":
                autoReplantPlayers.add(playerId);
                break;
            case "double_ore_drop":
                doubleOreDropPlayers.add(playerId);
                break;
            // Add other cases as needed
            default:
                break;
        }
        addPassive(playerId, effect);
    }

    public List<String> getActivePassives(Player player) {
        return new ArrayList<>(getPlayerPassives(player.getUniqueId()));
    }

    // --- NEW SYSTEM: Unlocking & Saving/Loading passives via config ---
    public void unlockPassive(Player player, String skill, String passiveName) {
        UUID playerId = player.getUniqueId();
        playerPassives.computeIfAbsent(playerId, k -> new HashMap<>());
        playerPassives.get(playerId).computeIfAbsent(skill, k -> new HashSet<>());
        playerPassives.get(playerId).get(skill).add(passiveName);
    }

    public boolean hasPassive(Player player, String skill, String passiveName) {
        UUID playerId = player.getUniqueId();
        return playerPassives.containsKey(playerId) &&
                playerPassives.get(playerId).containsKey(skill) &&
                playerPassives.get(playerId).get(skill).contains(passiveName);
    }

    public void savePassives() {
        FileConfiguration config = plugin.getConfig();
        for (Map.Entry<UUID, Map<String, Set<String>>> playerEntry : playerPassives.entrySet()) {
            String playerUUID = playerEntry.getKey().toString();
            Map<String, Set<String>> playerSkills = playerEntry.getValue();
            for (Map.Entry<String, Set<String>> skillEntry : playerSkills.entrySet()) {
                String skill = skillEntry.getKey();
                Set<String> passives = skillEntry.getValue();
                config.set("players." + playerUUID + "." + skill, new ArrayList<>(passives));
            }
        }
        plugin.saveConfig();
    }

    public void loadPassives() {
        FileConfiguration config = plugin.getConfig();
        playerPassives.clear();
        if (config.getConfigurationSection("players") == null) return;
        for (String playerUUID : config.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(playerUUID);
            Map<String, Set<String>> skillMap = new HashMap<>();
            ConfigurationSection playerSection = config.getConfigurationSection("players." + playerUUID);
            for (String skill : playerSection.getKeys(false)) {
                List<String> passives = playerSection.getStringList(skill);
                skillMap.put(skill, new HashSet<>(passives));
            }
            playerPassives.put(uuid, skillMap);
        }
    }

    // --- Event Handlers for Passive Effects ---
    @EventHandler
    public void onFishing(PlayerFishEvent event) {
        Player player = event.getPlayer();
        int level = xpManager.getPlayerLevel(player, "fishing");

        // XP Boost (Level 5) – handled in XPManager if player has "XP Boost"
        if (hasPassive(player, "fishing", "XP Boost")) {
            // XP boost logic here (if needed)
        }

        // Treasure Hunter (Level 10)
        if (hasPassive(player, "fishing", "Treasure Hunter") && event.getCaught() instanceof Item) {
            if (Math.random() < 0.15) { // 15% increased treasure rate
                // Modify loot table for better items (implementation depends on your loot system)
            }
        }

        // Rare Fish (Level 15)
        if (hasPassive(player, "fishing", "Rare Fish Master") && event.getCaught() instanceof Item) {
            if (Math.random() < 0.1) { // 10% chance for rare fish
                Item item = (Item) event.getCaught();
                item.setItemStack(new ItemStack(Material.PUFFERFISH));
            }
        }

        // Quick Hook (Level 20)
        if (hasPassive(player, "fishing", "Quick Hook")) {
            // Reduce fishing time by 10% (implementation depends on modifying fishing mechanics)
        }
    }

    @EventHandler
    public void onCropBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!isCrop(block.getType())) return;

        int level = xpManager.getPlayerLevel(player, "farming");

        // Farming XP Boost (Level 5) – handled in XPManager if player has the passive
        if (hasPassive(player, "farming", "Farming XP Boost")) {
            // XP boost logic here
        }

        // Auto Replant (Level 10)
        if (hasPassive(player, "farming", "Auto Replant")) {
            Material cropType = block.getType();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                block.setType(cropType);
                Ageable crop = (Ageable) block.getBlockData();
                crop.setAge(0);
                block.setBlockData(crop);
            }, 1L);
        }

        // Double Harvest (Level 15)
        if (hasPassive(player, "farming", "Double Harvest")) {
            if (Math.random() < 0.2) { // 20% chance
                for (ItemStack drop : block.getDrops()) {
                    block.getWorld().dropItemNaturally(block.getLocation(), drop.clone());
                }
            }
        }
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        int level = xpManager.getPlayerLevel(player, "enchanting");

        // Research Master (Level 5) – handled in XPManager
        if (hasPassive(player, "enchanting", "Research Master")) {
            // XP boost logic here
        }

        // Book Upgrade (Level 10)
        if (hasPassive(player, "enchanting", "Book Upgrade") && event.getItem().getType() == Material.BOOK) {
            if (Math.random() < 0.15) { // 15% chance to upgrade
                Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();
                enchants.forEach((ench, lvl) -> {
                    if (lvl < ench.getMaxLevel()) {
                        enchants.put(ench, lvl + 1);
                    }
                });
            }
        }

        // Custom Enchants (Level 15)
        if (hasPassive(player, "enchanting", "Custom Enchants")) {
            // Custom enchantment logic here
        }

        // Rare Enchant Boost (Level 20)
        if (hasPassive(player, "enchanting", "Rare Enchant Boost")) {
            // Increase chance of rare enchantments (implementation depends on your system)
        }
    }

    @EventHandler
    public void onCropGrow(BlockGrowEvent event) {
        Collection<Entity> nearbyEntities = event.getBlock().getWorld()
                .getNearbyEntities(event.getBlock().getLocation(), 16, 16, 16);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (hasPassive(player, "farming", "Growth Boost")) {
                    if (Math.random() < 0.1) { // 10% chance
                        Ageable crop = (Ageable) event.getNewState().getBlockData();
                        if (crop.getAge() < crop.getMaximumAge()) {
                            crop.setAge(crop.getAge() + 1);
                            event.getNewState().setBlockData(crop);
                        }
                    }
                }
            }
        }
    }

    // --- Utility Methods (stubs to be replaced with your implementations) ---
    private ItemStack getCropDrops(Material cropType) {
        // Returns the default drop for a given crop type.
        return switch (cropType) {
            case WHEAT -> new ItemStack(Material.WHEAT, 1);
            case CARROTS -> new ItemStack(Material.CARROT, 1);
            case POTATOES -> new ItemStack(Material.POTATO, 1);
            case BEETROOTS -> new ItemStack(Material.BEETROOT, 1);
            case NETHER_WART -> new ItemStack(Material.NETHER_WART, 1);
            default -> null;
        };
    }

    private boolean isMatureCrop(Block block) {
        // Check if a crop block is fully grown by comparing its age to its maximum.
        if (!isCrop(block.getType())) return false;
        Ageable ageable = (Ageable) block.getBlockData();
        return ageable.getAge() == ageable.getMaximumAge();
    }

    private boolean isCrop(Material material) {
        // Define which materials are considered crops.
        return material == Material.WHEAT || material == Material.CARROTS ||
                material == Material.POTATOES || material == Material.BEETROOTS ||
                material == Material.NETHER_WART;
    }


    private boolean isOre(Material material) {
        // Consider any material containing "_ORE" in its name or Ancient Debris as an ore.
        return material.name().contains("_ORE") || material == Material.ANCIENT_DEBRIS;
    }

    private Material getOreDrop(Material oreMaterial) {
        // Determine the drop for a given ore (typically the raw form).
        return switch (oreMaterial) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.RAW_IRON;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.RAW_GOLD;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.RAW_COPPER;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            default -> oreMaterial;  // Fallback: drop the ore itself.
        };
    }

    private Material getSmeltedOre(Material oreMaterial) {
        // Convert the ore (or raw item) to its smelted form.
        return switch (oreMaterial) {
            case IRON_ORE, DEEPSLATE_IRON_ORE, RAW_IRON -> Material.IRON_INGOT;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, RAW_GOLD -> Material.GOLD_INGOT;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE, RAW_COPPER -> Material.COPPER_INGOT;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            default -> null;
        };
    }

    // Stub event handlers from the older version (if needed)
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        UUID playerId = player.getUniqueId();

        // Example: If the block is an ore and the player has the "auto_smelt" passive,
        // prevent the normal drop and drop the smelted item instead.
        if (isOre(block.getType())) {
            if (hasPassive(playerId, "auto_smelt")) {
                event.setDropItems(false);
                Material smelted = getSmeltedOre(block.getType());
                if (smelted != null) {
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smelted, 1));
                }
            }
        }

        // You can add additional logic for crops or other block types here.
    }

    @EventHandler
    public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        // Example: If the killer has a "heal_on_kill" passive, heal them upon a kill.
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            UUID playerId = killer.getUniqueId();
            if (hasPassive(playerId, "heal_on_kill")) {
                double currentHealth = killer.getHealth();
                double maxHealth = killer.getMaxHealth();
                double healAmount = maxHealth * 0.15; // Heal 15% of max health.
                killer.setHealth(Math.min(currentHealth + healAmount, maxHealth));
            }
        }
    }
}
