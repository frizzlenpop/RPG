package org.frizzlenpop.rPGSkillsPlugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Item;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;


public class PassiveSkillManager implements Listener {
    private final RPGSkillsPlugin plugin;

    // Existing Sets
    private final Set<UUID> autoSmeltPlayers = new HashSet<>();
    private final Set<UUID> autoReplantPlayers = new HashSet<>();
    private final Set<UUID> doubleOreDropPlayers = new HashSet<>();
    private final Set<UUID> doubleWoodDropPlayers = new HashSet<>();
    private final Set<UUID> doubleCropYieldPlayers = new HashSet<>();
    private final Set<UUID> healOnKillPlayers = new HashSet<>();

    // New Sets for Higher Level Passives
    private final Set<UUID> fortuneBoostPlayers = new HashSet<>();
    private final Set<UUID> autoSmeltUpgradePlayers = new HashSet<>();
    private final Set<UUID> treeGrowthBoostPlayers = new HashSet<>();
    private final Set<UUID> tripleLogDropPlayers = new HashSet<>();
    private final Set<UUID> instantGrowthPlayers = new HashSet<>();
    private final Set<UUID> autoHarvestPlayers = new HashSet<>();
    private final Set<UUID> lifestealPlayers = new HashSet<>();
    private final Set<UUID> damageReductionPlayers = new HashSet<>();

    public PassiveSkillManager(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        UUID playerId = player.getUniqueId();

        // Enhanced ore processing
        if (isOre(block.getType())) {
            // Legacy double ore drop check
            if (doubleOreDropPlayers.contains(playerId)) {
                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation(),
                        new ItemStack(block.getType(), 2));
            }

            // Fortune boost passive (higher level)
            if (fortuneBoostPlayers.contains(playerId)) {
                double chance = 0.25; // 25% chance for fortune boost
                int bonusAmount = 2; // Default bonus amount

                if (Math.random() < chance) {
                    event.setDropItems(false);
                    block.getWorld().dropItemNaturally(block.getLocation(),
                            new ItemStack(getOreDrop(block.getType()), bonusAmount));
                }
            }

            // Auto-smelt and upgrade handling
            if (autoSmeltPlayers.contains(playerId) || autoSmeltUpgradePlayers.contains(playerId)) {
                event.setDropItems(false);
                Material smelted = getSmeltedOre(block.getType());
                if (smelted != null) {
                    int amount = 1;
                    // Check for upgraded auto-smelt
                    if (autoSmeltUpgradePlayers.contains(playerId)) {
                        double bonusChance = 0.5; // 50% chance for bonus from upgrade
                        if (Math.random() < bonusChance) {
                            amount = 2;
                        }
                    }
                    block.getWorld().dropItemNaturally(block.getLocation(),
                            new ItemStack(smelted, amount));
                }
            }
        }

        // Wood processing
        if (block.getType().name().endsWith("_LOG") || block.getType().name().endsWith("_WOOD")) {
            if (doubleWoodDropPlayers.contains(playerId)) {
                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation(),
                        new ItemStack(block.getType(), 2));
            }

            if (tripleLogDropPlayers.contains(playerId)) {
                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation(),
                        new ItemStack(block.getType(), 3));
            }
        }

        // Crop handling
        if (isCrop(block.getType())) {
            if (doubleCropYieldPlayers.contains(playerId) && isMatureCrop(block)) {
                event.setDropItems(false);
                Material cropType = block.getType();
                ItemStack drops = getCropDrops(cropType);
                if (drops != null) {
                    drops.setAmount(drops.getAmount() * 2);
                    block.getWorld().dropItemNaturally(block.getLocation(), drops);
                }
            }

            // Auto replant system
            if (autoReplantPlayers.contains(playerId) && isMatureCrop(block)) {
                Material cropType = block.getType();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    block.setType(cropType);
                }, 2L);
            }
        }
    }


    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            if (healOnKillPlayers.contains(killer.getUniqueId())) {
                double currentHealth = killer.getHealth();
                double maxHealth = killer.getMaxHealth();
                double healAmount = maxHealth * 0.15; // Heal 15% of max health
                killer.setHealth(Math.min(currentHealth + healAmount, maxHealth));
            }
        }
    }

    private boolean isCrop(Material material) {
        return material == Material.WHEAT ||
                material == Material.CARROTS ||
                material == Material.POTATOES ||
                material == Material.BEETROOTS ||
                material == Material.NETHER_WART;
    }

    private ItemStack getCropDrops(Material cropType) {
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
        if (!isCrop(block.getType())) return false;

        org.bukkit.block.data.Ageable ageable = (org.bukkit.block.data.Ageable) block.getBlockData();
        return ageable.getAge() == ageable.getMaximumAge();
    }

    private boolean isOre(Material material) {
        return material.name().contains("_ORE") ||
                material == Material.ANCIENT_DEBRIS;
    }

    private Material getOreDrop(Material oreMaterial) {
        return switch (oreMaterial) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.RAW_IRON;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.RAW_GOLD;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.RAW_COPPER;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            default -> oreMaterial;
        };
    }

    private Material getSmeltedOre(Material oreMaterial) {
        return switch (oreMaterial) {
            case IRON_ORE, DEEPSLATE_IRON_ORE, RAW_IRON -> Material.IRON_INGOT;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, RAW_GOLD -> Material.GOLD_INGOT;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE, RAW_COPPER -> Material.COPPER_INGOT;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            default -> null;
        };
    }

    public void applyPassiveEffect(Player player, String effect) {
        UUID playerId = player.getUniqueId();
        switch (effect.toLowerCase()) {
            case "autosmelt" -> autoSmeltPlayers.add(playerId);
            case "autoreplant" -> autoReplantPlayers.add(playerId);
            case "doubleore" -> doubleOreDropPlayers.add(playerId);
            case "doublewood" -> doubleWoodDropPlayers.add(playerId);
            case "doublecrop" -> doubleCropYieldPlayers.add(playerId);
            case "healOnKill" -> healOnKillPlayers.add(playerId);
            case "fortuneboost" -> fortuneBoostPlayers.add(playerId);
            case "autosmeltupgrade" -> autoSmeltUpgradePlayers.add(playerId);
            case "treegrowthboost" -> treeGrowthBoostPlayers.add(playerId);
            case "triplelog" -> tripleLogDropPlayers.add(playerId);
            case "instantgrowth" -> instantGrowthPlayers.add(playerId);
            case "autoharvest" -> autoHarvestPlayers.add(playerId);
            case "lifesteal" -> lifestealPlayers.add(playerId);
            case "damagereduction" -> damageReductionPlayers.add(playerId);
        }
    }

    public List<String> getActivePassives(Player player) {
        UUID playerId = player.getUniqueId();
        List<String> activePassives = new ArrayList<>();

        if (autoSmeltPlayers.contains(playerId)) activePassives.add("Auto Smelt");
        if (autoReplantPlayers.contains(playerId)) activePassives.add("Auto Replant");
        if (doubleOreDropPlayers.contains(playerId)) activePassives.add("Double Ore");
        if (doubleWoodDropPlayers.contains(playerId)) activePassives.add("Double Wood");
        if (doubleCropYieldPlayers.contains(playerId)) activePassives.add("Double Crop");
        if (healOnKillPlayers.contains(playerId)) activePassives.add("Heal on Kill");
        if (fortuneBoostPlayers.contains(playerId)) activePassives.add("Fortune Boost");
        if (autoSmeltUpgradePlayers.contains(playerId)) activePassives.add("Auto Smelt Upgrade");
        if (treeGrowthBoostPlayers.contains(playerId)) activePassives.add("Tree Growth Boost");
        if (tripleLogDropPlayers.contains(playerId)) activePassives.add("Triple Log");
        if (instantGrowthPlayers.contains(playerId)) activePassives.add("Instant Growth");
        if (autoHarvestPlayers.contains(playerId)) activePassives.add("Auto Harvest");
        if (lifestealPlayers.contains(playerId)) activePassives.add("Lifesteal");
        if (damageReductionPlayers.contains(playerId)) activePassives.add("Damage Reduction");

        return activePassives;
    }
}
