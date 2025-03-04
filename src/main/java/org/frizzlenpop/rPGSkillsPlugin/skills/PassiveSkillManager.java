package org.frizzlenpop.rPGSkillsPlugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.jetbrains.annotations.Nullable;

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
            passives.add("doubleOreDrop");
            doubleOreDropPlayers.add(playerId);
        }
        if (miningLevel >= 10) {
            passives.add("autoSmelt");
            autoSmeltPlayers.add(playerId);
        }
        if (miningLevel >= 15) {
            passives.add("fortuneBoost");
            fortuneBoostPlayers.add(playerId);
        }
        if (miningLevel >= 20) {
            passives.add("autoSmeltUpgrade");
            autoSmeltUpgradePlayers.add(playerId);
        }

        // Logging passives
        if (loggingLevel >= 5) {
            passives.add("doubleWoodDrop");
            doubleWoodDropPlayers.add(playerId);
        }
        if (loggingLevel >= 10) {
            passives.add("treeGrowthBoost");
            treeGrowthBoostPlayers.add(playerId);
        }
        if (loggingLevel >= 15) {
            passives.add("tripleLogDrop");
            tripleLogDropPlayers.add(playerId);
        }

        // Farming passives
        if (farmingLevel >= 5) {
            passives.add("doubleCropYield");
            doubleCropYieldPlayers.add(playerId);
        }
        if (farmingLevel >= 10) {
            passives.add("autoReplant");
            autoReplantPlayers.add(playerId);
        }
        if (farmingLevel >= 15) {
            passives.add("instantGrowth");
            instantGrowthPlayers.add(playerId);
        }

        // Fighting passives
        if (fightingLevel >= 5) {
            passives.add("lifesteal");
            lifestealPlayers.add(playerId);
        }
        if (fightingLevel >= 10) {
            passives.add("damageReduction");
            damageReductionPlayers.add(playerId);
        }
        if (fightingLevel >= 15) {
            passives.add("healOnKill");
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

    public void updatePassiveEffects(Player player, String skill, int level) {
        // Configure passive unlocks based on skill levels
        switch (skill.toLowerCase()) {
            case "mining":
                if (level >= 10) {
                    unlockPassive(player, skill, "doubleOreDrop");
                }
                if (level >= 20) {
                    unlockPassive(player, skill, "autoSmelt");
                }
                if (level >= 30) {
                    unlockPassive(player, skill, "fortuneBoost");
                }
                break;

            case "woodcutting":
                if (level >= 10) {
                    unlockPassive(player, skill, "doubleWoodDrop");
                }
                if (level >= 20) {
                    unlockPassive(player, skill, "treeGrowthBoost");
                }
                if (level >= 30) {
                    unlockPassive(player, skill, "tripleLogDrop");
                }
                break;

            case "farming":
                if (level >= 10) {
                    unlockPassive(player, skill, "doubleCropYield");
                }
                if (level >= 20) {
                    unlockPassive(player, skill, "autoReplant");
                }
                if (level >= 30) {
                    unlockPassive(player, skill, "instantGrowth");
                }
                break;

            case "combat":
                if (level >= 10) {
                    unlockPassive(player, skill, "healOnKill");
                }
                if (level >= 20) {
                    unlockPassive(player, skill, "lifesteal");
                }
                if (level >= 30) {
                    unlockPassive(player, skill, "damageReduction");
                }
                break;
        }

        // Notify player of new passive unlocks
        if (level == 10 || level == 20 || level == 30) {
            player.sendMessage(ChatColor.GREEN + "✨ You've unlocked new passive abilities for " + skill + "!");
        }
    }

    public double getXPMultiplier(Player player, String skill) {
        UUID playerId = player.getUniqueId();
        double multiplier = 1.0; // Base multiplier

        // Get the player's level for the skill
        int level = xpManager.getPlayerLevel(player, skill);

        // Add skill-specific XP bonuses based on unlocked passives
        switch (skill.toLowerCase()) {
            case "mining":
                if (hasPassive(player, skill, "doubleOreDrop")) {
                    multiplier += 0.1; // +10% XP
                }
                if (hasPassive(player, skill, "autoSmelt")) {
                    multiplier += 0.15; // +15% XP
                }
                if (hasPassive(player, skill, "fortuneBoost")) {
                    multiplier += 0.25; // +25% XP
                }
                break;

            case "woodcutting":
                if (hasPassive(player, skill, "doubleWoodDrop")) {
                    multiplier += 0.1;
                }
                if (hasPassive(player, skill, "treeGrowthBoost")) {
                    multiplier += 0.15;
                }
                if (hasPassive(player, skill, "tripleLogDrop")) {
                    multiplier += 0.25;
                }
                break;

            case "farming":
                if (hasPassive(player, skill, "doubleCropYield")) {
                    multiplier += 0.1;
                }
                if (hasPassive(player, skill, "autoReplant")) {
                    multiplier += 0.15;
                }
                if (hasPassive(player, skill, "instantGrowth")) {
                    multiplier += 0.25;
                }
                break;

            case "combat":
                if (hasPassive(player, skill, "healOnKill")) {
                    multiplier += 0.1;
                }
                if (hasPassive(player, skill, "lifesteal")) {
                    multiplier += 0.15;
                }
                if (hasPassive(player, skill, "damageReduction")) {
                    multiplier += 0.25;
                }
                break;
        }

        // Add level-based XP bonus
        // Every 10 levels adds 5% bonus XP (up to 25% at level 50)
        double levelBonus = Math.min((level / 10) * 0.05, 0.25);
        multiplier += levelBonus;

        return multiplier;
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

    private ItemStack getCropDrops(Material cropType, int amount) {
        // Returns the default drop for a given crop type.
        return switch (cropType) {
            case WHEAT -> new ItemStack(Material.WHEAT_SEEDS, amount);
            case CARROTS -> new ItemStack(Material.CARROT, amount);
            case POTATOES -> new ItemStack(Material.POTATO, amount);
            case BEETROOTS -> new ItemStack(Material.BEETROOT_SEEDS, amount);
            case NETHER_WART -> new ItemStack(Material.NETHER_WART, amount);
            case PUMPKIN -> new ItemStack(Material.PUMPKIN_SEEDS, amount);
            case MELON -> new ItemStack(Material.MELON_SEEDS, amount);
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
        return switch (material) {
            case WHEAT, CARROTS, POTATOES, BEETROOTS, NETHER_WART -> true;
            default -> false;
        };
    }


    private boolean isOre(Material material) {
        // Consider any material containing "_ORE" in its name or Ancient Debris as an ore.
        return material.name().contains("_ORE") || material == Material.ANCIENT_DEBRIS;
    }


    // Determines the appropriate drop material based on the mined block type.
    private Material getOreDrop(Material material, boolean isSmelted) {
        return switch (material) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> isSmelted ? Material.IRON_INGOT : Material.RAW_IRON;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> isSmelted ? Material.GOLD_INGOT : Material.RAW_GOLD;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> isSmelted ? Material.COPPER_INGOT : Material.RAW_COPPER;
            case ANCIENT_DEBRIS -> isSmelted ? Material.NETHERITE_SCRAP : Material.ANCIENT_DEBRIS;

            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> Material.DIAMOND;
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> Material.EMERALD;
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> Material.LAPIS_LAZULI;
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> Material.REDSTONE;
            case COAL_ORE, DEEPSLATE_COAL_ORE -> Material.COAL;
            case NETHER_QUARTZ_ORE -> Material.QUARTZ;
            case NETHER_GOLD_ORE -> Material.GOLD_NUGGET;
            default -> null;
        };
    }

    // handles the block break event for ores
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        UUID playerId = player.getUniqueId();
        if (!isOre(block.getType())) {
            return;
        }

        event.setDropItems(false);

        int amount = 1;
        if (hasPassive(playerId, "doubleOreDrop")) {
            if (Math.random() < 0.5) { // 50% chance to double drop
                amount = 2;
                player.sendActionBar("§6You got lucky and the ore dropped twice as much!");
            }
        }

        boolean isSmelted = hasPassive(playerId, "autoSmelt");
        Material dropMaterial = getOreDrop(block.getType(), isSmelted);

        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropMaterial, amount));

    }

    @EventHandler
    public void onCropBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        UUID playerId = player.getUniqueId();
        if (!isCrop(block.getType())) {
            return;
        }

        if (!isMatureCrop(block)) {
            return;
        }

        boolean autoReplant = hasPassive(playerId, "autoReplant");

        if (autoReplant) {
            event.setCancelled(true);
            Ageable crop = (Ageable) block.getBlockData();
            crop.setAge(0);
            block.setBlockData(crop);
        }

        int amount = 1;
        if (hasPassive(playerId, "doubleCropYield")) {
            if (Math.random() < 0.5) { // 50% chance to double drop
                amount = 2;
                player.sendMessage("§6You got lucky and the crop dropped twice as much!");
            }
        }

        block.getWorld().dropItemNaturally(block.getLocation(), getCropDrops(block.getType(), amount));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
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
