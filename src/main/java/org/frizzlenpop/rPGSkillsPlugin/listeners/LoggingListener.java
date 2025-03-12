package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Particle;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.PassiveSkillManager;

import java.util.Random;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class LoggingListener implements Listener {

    private final XPManager xpManager;
    private final RPGSkillsPlugin plugin;
    private final PassiveSkillManager passiveManager;
    private final Random random = new Random();
    
    // Constants for tree felling
    private static final int MAX_TREE_HEIGHT = 30;
    private static final int MAX_LOGS_PER_TREE = 200;

    public LoggingListener(XPManager xpManager, RPGSkillsPlugin plugin, PassiveSkillManager passiveManager) {
        this.xpManager = xpManager;
        this.plugin = plugin;
        this.passiveManager = passiveManager;
        
        // Start tree growth booster when the listener is created
        startTreeGrowthBooster();
    }

    /**
     * Starts a task that periodically boosts tree growth for players with the Tree Growth Boost passive
     */
    private void startTreeGrowthBooster() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();
                    if (passiveManager.hasPassive(playerId, "treeGrowthBoost")) {
                        boostNearbySaplings(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 400L); // Run every 20 seconds (400 ticks)
    }
    
    /**
     * Boosts growth of nearby saplings for a player with Tree Growth Boost
     */
    private void boostNearbySaplings(Player player) {
        int radius = 15; // 15 block radius around player
        Location playerLoc = player.getLocation();
        World world = player.getWorld();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location checkLoc = playerLoc.clone().add(x, y, z);
                    Block block = checkLoc.getBlock();
                    
                    if (isSapling(block.getType()) && random.nextDouble() < 0.3) { // 30% chance
                        // Apply growth (similar to bone meal)
                        block.applyBoneMeal(BlockFace.UP);
                        world.spawnParticle(Particle.COMPOSTER, checkLoc.add(0.5, 0.5, 0.5), 5);
                    }
                }
            }
        }
    }
    
    /**
     * Checks if a material is a sapling
     */
    private boolean isSapling(Material material) {
        return material == Material.OAK_SAPLING || 
               material == Material.SPRUCE_SAPLING || 
               material == Material.BIRCH_SAPLING || 
               material == Material.JUNGLE_SAPLING || 
               material == Material.ACACIA_SAPLING || 
               material == Material.DARK_OAK_SAPLING ||
               material == Material.MANGROVE_PROPAGULE ||
               material == Material.CHERRY_SAPLING ||
               material == Material.AZALEA || 
               material == Material.FLOWERING_AZALEA;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        UUID playerId = player.getUniqueId();
        
        // Check if it's a log
        if (!isLog(blockType)) {
            return;
        }
        
        // Get XP for the chopped log
        int xpGained = xpManager.getXPForLog(blockType);
        
        // Apply XP boosts if the player has them
        if (passiveManager.hasPassive(playerId, "xpBoost5")) {
            xpGained *= 1.3; // +30% XP
        } else if (passiveManager.hasPassive(playerId, "xpBoost4")) {
            xpGained *= 1.25; // +25% XP
        } else if (passiveManager.hasPassive(playerId, "xpBoost3")) {
            xpGained *= 1.2; // +20% XP
        } else if (passiveManager.hasPassive(playerId, "xpBoost2")) {
            xpGained *= 1.15; // +15% XP
        } else if (passiveManager.hasPassive(playerId, "xpBoost1")) {
            xpGained *= 1.1; // +10% XP
        }
        
        // Apply Logging Basics passive - slight chance for extra XP
        if (passiveManager.hasPassive(playerId, "loggingBasics") && random.nextDouble() < 0.15) {
            xpGained = (int)(xpGained * 1.05); // 5% more XP with 15% chance
            player.sendActionBar("§2Your logging basics gave you bonus XP!");
        }
        
        if (xpGained > 0) {
            xpManager.addXP(player, "logging", xpGained);
        }
        
        // Check for passive abilities and apply them
        ItemStack tool = player.getInventory().getItemInMainHand();
        boolean hasSilkTouch = tool.containsEnchantment(Enchantment.SILK_TOUCH);
        
        // Get base amount (always at least 1 log)
        int amount = 1;
        boolean passiveActivated = false;
        
        // Apply Wood Efficiency - 10% faster wood chopping
        // This is simulated by giving the player a brief speed boost after chopping
        if (passiveManager.hasPassive(playerId, "woodEfficiency")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3 * 20, 0, false, false, true));
            if (random.nextDouble() < 0.1) { // 10% chance for small action bar message
                if (!passiveActivated) {
                    player.sendActionBar("§2Wood Efficiency active!");
                    passiveActivated = true;
                }
            }
        }
        
        // Apply wood type specializations
        if (applyWoodSpecialization(player, blockType)) {
            amount++;
            passiveActivated = true;
            player.sendActionBar("§2Your wood specialization gives you bonus logs!");
        }
        
        // Apply luck-based passive effects (prioritize the highest level one that activates)
        if (passiveManager.hasPassive(playerId, "quadrupleLogDrop") && Math.random() < 0.05) { // 5% chance
            amount *= 4;
            player.sendActionBar("§2Incredible luck! The tree dropped quadruple logs!");
            passiveActivated = true;
        }
        else if (passiveManager.hasPassive(playerId, "tripleLogDrop") && Math.random() < 0.15) { // 15% chance
            amount *= 3;
            player.sendActionBar("§2You got very lucky and the tree dropped triple logs!");
            passiveActivated = true;
        }
        else if (passiveManager.hasPassive(playerId, "doubleWoodDrop") && Math.random() < 0.25) { // 25% chance
            amount *= 2;
            player.sendActionBar("§2You got lucky and the tree dropped twice as much!");
            passiveActivated = true;
        }
        
        // Apply Unbreakable Tools passive - 10% chance tools don't lose durability
        if (passiveManager.hasPassive(playerId, "unbreakableTools") && Math.random() < 0.10) {
            if (!tool.getType().isAir() && tool.getType().getMaxDurability() > 0) {
                // Mark to not reduce durability
                event.setDropItems(false);
                event.getBlock().setType(Material.AIR);
                player.sendActionBar("§2Your tool didn't lose any durability!");
                passiveActivated = true;
            }
        }
        
        // Apply Logger's Haste passive
        if (passiveManager.hasPassive(playerId, "loggerHaste") || passiveManager.hasPassive(playerId, "Logger's Haste")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 5 * 20, 0)); // Haste I for 5 seconds
            if (!passiveActivated) {
                player.sendActionBar("§2Logger's Haste activated!");
                passiveActivated = true;
            }
        }
        
        // Apply Advanced Haste for rare woods
        if (passiveManager.hasPassive(playerId, "advancedHaste") && isRareWood(blockType)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 10 * 20, 1)); // Haste II for 10 seconds
            if (!passiveActivated) {
                player.sendActionBar("§2Advanced Haste activated from rare wood!");
                passiveActivated = true;
            }
        }
        
        // Apply Tree Planter passive - automatically plant sapling where tree was cut
        if (passiveManager.hasPassive(playerId, "treePlanter") && Math.random() < 0.2) { // 20% chance
            Material saplingType = getSaplingTypeForLog(blockType);
            if (saplingType != null) {
                // Create a final copy of passiveActivated for use in the lambda
                final boolean wasActivated = passiveActivated;
                
                // Schedule task to plant sapling after a short delay (let the tree finish breaking)
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    // Check if block is air (tree was fully broken)
                    if (event.getBlock().getType() == Material.AIR) {
                        // Check if block below is dirt, grass, etc. (valid for sapling)
                        Material blockBelow = event.getBlock().getRelative(0, -1, 0).getType();
                        if (blockBelow == Material.DIRT || blockBelow == Material.GRASS_BLOCK || 
                            blockBelow == Material.PODZOL || blockBelow == Material.COARSE_DIRT) {
                            event.getBlock().setType(saplingType);
                            if (!wasActivated) {
                                player.sendActionBar("§2Your Tree Planter passive planted a new sapling!");
                            }
                        }
                    }
                }, 5L); // 5 tick delay (quarter second)
            }
        }
        
        // Apply Apple Harvester passive - increase apple drops from oak
        if (passiveManager.hasPassive(playerId, "appleHarvester") && 
            (blockType == Material.OAK_LOG || blockType == Material.DARK_OAK_LOG) && 
            Math.random() < 0.08) { // 8% chance
            event.getBlock().getWorld().dropItemNaturally(
                event.getBlock().getLocation(), 
                new ItemStack(Material.APPLE, 1)
            );
            if (!passiveActivated) {
                player.sendActionBar("§2Your Apple Harvester passive found an apple!");
                passiveActivated = true;
            }
        }
        
        // Apply Golden Touch passive - chance to find golden apples
        if (passiveManager.hasPassive(playerId, "goldenTouch") && 
            (blockType == Material.OAK_LOG || blockType == Material.DARK_OAK_LOG) && 
            Math.random() < 0.02) { // 2% chance
            event.getBlock().getWorld().dropItemNaturally(
                event.getBlock().getLocation(), 
                new ItemStack(Material.GOLDEN_APPLE, 1)
            );
            player.sendActionBar("§6Your Golden Touch passive found a golden apple!");
            passiveActivated = true;
        }
        
        // Apply Tool Smith passive - repair axes slightly while chopping
        if (passiveManager.hasPassive(playerId, "toolSmith") && Math.random() < 0.2) { // 20% chance
            ItemStack axe = player.getInventory().getItemInMainHand();
            if (isAxe(axe.getType()) && axe.getDurability() > 0) {
                // Repair the axe slightly (5% of max durability)
                int repairAmount = (int)(axe.getType().getMaxDurability() * 0.05);
                int newDurability = Math.max(0, axe.getDurability() - repairAmount);
                axe.setDurability((short)newDurability);
                
                if (!passiveActivated) {
                    player.sendActionBar("§2Your Tool Smith passive repaired your axe!");
                    passiveActivated = true;
                }
            }
        }
        
        // Apply Stripped Log Expert passive - get extra items when stripping logs
        // This should actually be in a separate listener for PlayerInteractEvent when stripping logs
        // But we'll add the logic here for now
        if (passiveManager.hasPassive(playerId, "strippedLogExpert") && blockType.toString().contains("STRIPPED_") && Math.random() < 0.25) {
            Material extraDrop = getExtraDropForStrippedLog(blockType);
            if (extraDrop != null) {
                event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation(),
                    new ItemStack(extraDrop, 1)
                );
                if (!passiveActivated) {
                    player.sendActionBar("§2Your Stripped Log Expert passive gave you extra materials!");
                    passiveActivated = true;
                }
            }
        }
        
        // Apply Leaf Expert passive - break leaves faster
        if (passiveManager.hasPassive(playerId, "leafExpert") && blockType.toString().contains("LEAVES")) {
            // We can't actually modify break speed here, but we can give the player a brief Haste effect
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 3 * 20, 2)); // Haste III for 3 seconds
            if (!passiveActivated && Math.random() < 0.25) { // Only show message occasionally
                player.sendActionBar("§2Your Leaf Expert passive makes breaking leaves easier!");
                passiveActivated = true;
            }
        }
        
        // Apply Sap Collection passive - chance to collect sap (honey bottle) from trees
        if (passiveManager.hasPassive(playerId, "sapCollection") && isLog(blockType) && Math.random() < 0.10) {
            event.getBlock().getWorld().dropItemNaturally(
                event.getBlock().getLocation(),
                new ItemStack(Material.HONEY_BOTTLE, 1)
            );
            if (!passiveActivated) {
                player.sendActionBar("§6You collected sweet sap from the tree!");
                passiveActivated = true;
            }
        }
        
        // Apply Mass Harvester passive - chance to break multiple trees at once
        if (passiveManager.hasPassive(playerId, "massHarvester") && Math.random() < 0.05) { // 5% chance
            // Find nearby logs within 3 blocks
            Location blockLoc = event.getBlock().getLocation();
            List<Block> nearbyLogs = new ArrayList<>();
            
            // Check for logs in a small radius
            for (int x = -2; x <= 2; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -2; z <= 2; z++) {
                        if (x == 0 && y == 0 && z == 0) continue; // Skip the original block
                        
                        Block nearbyBlock = blockLoc.clone().add(x, y, z).getBlock();
                        if (isLog(nearbyBlock.getType()) && nearbyBlock.getType() == blockType) {
                            nearbyLogs.add(nearbyBlock);
                        }
                    }
                }
            }
            
            // If we found nearby logs, break them too
            if (!nearbyLogs.isEmpty()) {
                for (Block log : nearbyLogs) {
                    // Break the block and drop items
                    log.breakNaturally(player.getInventory().getItemInMainHand());
                    
                    // Add XP for the log
                    int logXP = xpManager.getXPForLog(log.getType());
                    if (logXP > 0) {
                        xpManager.addXP(player, "logging", logXP);
                    }
                }
                
                if (!passiveActivated) {
                    player.sendActionBar("§2Your Mass Harvester passive broke nearby logs!");
                    passiveActivated = true;
                }
            }
        }
        
        // Apply Tree Finder / Forest Sense passives
        if ((passiveManager.hasPassive(playerId, "treeFinder") || passiveManager.hasPassive(playerId, "forestSense")) && 
            Math.random() < 0.1) { // 10% chance
            
            boolean foundRareTree = showNearbyRareTrees(player, passiveManager.hasPassive(playerId, "forestSense") ? 50 : 30);
            
            if (foundRareTree && !passiveActivated) {
                if (passiveManager.hasPassive(playerId, "forestSense")) {
                    player.sendActionBar("§2Your Forest Sense detected nearby rare trees!");
                } else {
                    player.sendActionBar("§2Your Tree Finder passive detected a rare tree!");
                }
                passiveActivated = true;
            }
        }
        
        // Apply Ancient Tree Finder - a more advanced version of Tree Finder
        if (passiveManager.hasPassive(playerId, "ancientTreeFinder") && Math.random() < 0.05) {
            showNearbySpecialForests(player);
            if (!passiveActivated && Math.random() < 0.5) {
                player.sendActionBar("§2Your Ancient Tree Finder is guiding you to special forests!");
                passiveActivated = true;
            }
        }
        
        // Apply Legendary Harvester passive
        if (passiveManager.hasPassive(playerId, "legendaryHarvester") && Math.random() < 0.05) {
            ItemStack specialItem = getLegendaryWoodItem();
            if (specialItem != null) {
                event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation(),
                    specialItem
                );
                player.sendActionBar("§6You found a legendary wood item!");
                passiveActivated = true;
            }
        }
        
        // Apply Azalea Expert passive
        if (passiveManager.hasPassive(playerId, "azaleaExpert") && 
            (blockType == Material.AZALEA || blockType == Material.FLOWERING_AZALEA) && 
            Math.random() < 0.25) {
            
            // Special drops for Azalea Expert
            Material[] specialDrops = {
                Material.MOSS_BLOCK,
                Material.SMALL_DRIPLEAF,
                Material.BIG_DRIPLEAF,
                Material.GLOW_BERRIES,
                Material.SPORE_BLOSSOM
            };
            
            Material drop = specialDrops[random.nextInt(specialDrops.length)];
            event.getBlock().getWorld().dropItemNaturally(
                event.getBlock().getLocation(),
                new ItemStack(drop, 1)
            );
            
            if (!passiveActivated) {
                player.sendActionBar("§2Your Azalea Expert passive found special items!");
                passiveActivated = true;
            }
        }
        
        // Hollow Tree Finder passive - chance to find bee nests and special items
        if (passiveManager.hasPassive(playerId, "hollowTreeFinder") && isLog(blockType) && Math.random() < 0.08) {
            // Choose a special item to drop
            ItemStack[] specialItems = {
                new ItemStack(Material.HONEYCOMB, random.nextInt(3) + 1),
                new ItemStack(Material.HONEY_BOTTLE, 1),
                new ItemStack(Material.BEE_NEST, 1)
            };
            
            ItemStack drop = specialItems[random.nextInt(specialItems.length)];
            event.getBlock().getWorld().dropItemNaturally(
                event.getBlock().getLocation(),
                drop
            );
            
            if (!passiveActivated) {
                player.sendActionBar("§6You found a hollow tree with treasures inside!");
                passiveActivated = true;
            }
        }
        
        // Apply Exotic Timber passive - more efficient nether stem harvesting
        if (passiveManager.hasPassive(playerId, "exoticTimber") && 
            (blockType == Material.CRIMSON_STEM || blockType == Material.WARPED_STEM)) {
            
            // Increase amount by 50%
            amount = (int)Math.ceil(amount * 1.5);
            
            if (!passiveActivated) {
                player.sendActionBar("§5Your Exotic Timber passive improved your nether wood harvest!");
                passiveActivated = true;
            }
        }
        
        // Apply Sapling Collector passive - make sapling drop notification
        // Full implementation would be in a leaf break event, but we show the notification here
        if (passiveManager.hasPassive(playerId, "saplingCollector") && Math.random() < 0.1) {
            if (!passiveActivated) {
                player.sendActionBar("§2Your Sapling Collector passive will increase sapling drops from leaves!");
                passiveActivated = true;
            }
        }
        
        // Apply Axe Enchanter passive
        if (passiveManager.hasPassive(playerId, "axeEnchanter") && Math.random() < 0.05) { // 5% chance
            ItemStack axe = player.getInventory().getItemInMainHand();
            if (isAxe(axe.getType())) {
                // Add a random enchantment
                addRandomEnchantment(axe, player);
                if (!passiveActivated) {
                    player.sendActionBar("§5Your Axe Enchanter passive enhanced your axe!");
                    passiveActivated = true;
                }
            }
        }
        
        // Apply Forest passives if in a forest biome
        if (isInForest(player)) {
            // Apply Forest Speed
            if (passiveManager.hasPassive(playerId, "forestSpeed")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 0));
                if (!passiveActivated && Math.random() < 0.1) { // Only show message occasionally
                    player.sendActionBar("§2Your Forest Speed passive activated!");
                    passiveActivated = true;
                }
            }
            
            // Apply Forest Master
            if (passiveManager.hasPassive(playerId, "forestMaster")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 1)); // Speed II
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 10 * 20, 1)); // Jump Boost II
                if (!passiveActivated && Math.random() < 0.1) { // Only show message occasionally
                    player.sendActionBar("§2Your Forest Master passive gives you enhanced movement!");
                    passiveActivated = true;
                }
            }
            
            // Apply Dryad's Blessing
            if (passiveManager.hasPassive(playerId, "dryadsBlessing")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 0)); // Regeneration I for 5 seconds
                if (!passiveActivated && Math.random() < 0.1) { // Only show message occasionally
                    player.sendActionBar("§2The forest's magic heals your wounds!");
                    passiveActivated = true;
                }
            }
            
            // Apply Forest Guardian
            if (passiveManager.hasPassive(playerId, "forestGuardian")) {
                // This effect is passive and will be checked when mobs target the player
                // But we'll give a brief invisibility effect to simulate being protected
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 2 * 20, 0, true, false));
                if (!passiveActivated && Math.random() < 0.05) { // Rarely show message
                    player.sendActionBar("§2The forest's spirits hide you from danger!");
                    passiveActivated = true;
                }
            }
        }
        
        // Apply Logger's Efficiency in forests
        if (passiveManager.hasPassive(playerId, "loggersEfficiency") && isInForest(player)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 15 * 20, 0));
            if (!passiveActivated && Math.random() < 0.15) {
                player.sendActionBar("§2Logger's Efficiency activated in the forest!");
                passiveActivated = true;
            }
        }
        
        // Apply Timber passives
        boolean timberActivated = false;
        if (passiveManager.hasPassive(playerId, "ultimateTimber")) {
            // Ultimate Timber - always activates (level 95)
            timberActivated = true;
            if (!passiveActivated) player.sendActionBar("§2Ultimate Timber activated!");
            passiveActivated = true;
        } else if (passiveManager.hasPassive(playerId, "masterTimber") && random.nextDouble() < 0.60) {
            // Master Timber - 60% chance (level 50)
            timberActivated = true;
            if (!passiveActivated) player.sendActionBar("§2Master Timber activated!");
            passiveActivated = true;
        } else if (passiveManager.hasPassive(playerId, "timber") && random.nextDouble() < 0.30) {
            // Basic Timber - 30% chance (level 25)
            timberActivated = true;
            if (!passiveActivated) player.sendActionBar("§2Timber activated!");
            passiveActivated = true;
        }
        
        // If any Timber passive activated, break the entire tree
        if (timberActivated) {
            // Do this after event completes to prevent concurrent modification
            final int finalAmount = amount;
            final Block startBlock = event.getBlock();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                fellTree(player, startBlock, finalAmount);
            }, 1L);
            
            // Don't do normal drops, as the tree felling will handle it
            event.setDropItems(false);
            return;
        }
        
        // Don't override drops if we didn't modify them
        if (amount > 1 || passiveActivated) {
            // Handle drops ourselves
            event.setDropItems(false);
            event.getBlock().getWorld().dropItemNaturally(
                event.getBlock().getLocation(), 
                new ItemStack(blockType, amount)
            );
        }
    }
    
    /**
     * Adds a random enchantment to an axe
     * @param axe The axe to enchant
     * @param player The player who owns the axe
     */
    private void addRandomEnchantment(ItemStack axe, Player player) {
        Enchantment[] possibleEnchants = {
            Enchantment.UNBREAKING,
            Enchantment.SHARPNESS,
            Enchantment.EFFICIENCY,
            Enchantment.FORTUNE,
            Enchantment.SILK_TOUCH
        };
        
        // Choose a random enchantment
        Enchantment enchant = possibleEnchants[random.nextInt(possibleEnchants.length)];
        
        // Get current level or default to 0
        int currentLevel = axe.getEnchantmentLevel(enchant);
        
        // Add one level, up to a maximum of the enchantment's max level
        int newLevel = Math.min(currentLevel + 1, enchant.getMaxLevel());
        
        // Only apply if it would increase the level
        if (newLevel > currentLevel) {
            axe.addUnsafeEnchantment(enchant, newLevel);
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5f, 1.0f);
        }
    }
    
    /**
     * Checks if an item type is an axe
     * @param material The material to check
     * @return true if the material is an axe
     */
    private boolean isAxe(Material material) {
        return material == Material.WOODEN_AXE ||
               material == Material.STONE_AXE ||
               material == Material.IRON_AXE ||
               material == Material.GOLDEN_AXE ||
               material == Material.DIAMOND_AXE ||
               material == Material.NETHERITE_AXE;
    }
    
    /**
     * Checks if a player is in a forest biome
     * @param player The player to check
     * @return true if the player is in a forest biome
     */
    private boolean isInForest(Player player) {
        org.bukkit.block.Biome biome = player.getLocation().getBlock().getBiome();
        return biome == org.bukkit.block.Biome.FOREST ||
               biome == org.bukkit.block.Biome.FLOWER_FOREST ||
               biome == org.bukkit.block.Biome.BIRCH_FOREST ||
               biome == org.bukkit.block.Biome.DARK_FOREST ||
               biome == org.bukkit.block.Biome.OLD_GROWTH_BIRCH_FOREST ||
               biome == org.bukkit.block.Biome.OLD_GROWTH_PINE_TAIGA ||
               biome == org.bukkit.block.Biome.OLD_GROWTH_SPRUCE_TAIGA ||
               biome == org.bukkit.block.Biome.JUNGLE ||
               biome == org.bukkit.block.Biome.SPARSE_JUNGLE ||
               biome == org.bukkit.block.Biome.BAMBOO_JUNGLE;
    }
    
    /**
     * Recursively fells a tree, breaking all logs connected to it
     * @param player The player felling the tree
     * @param startBlock The first block broken
     * @param dropMultiplier How many logs to drop per block
     */
    private void fellTree(Player player, Block startBlock, int dropMultiplier) {
        Material logType = startBlock.getType();
        if (!isLog(logType)) return;
        
        World world = startBlock.getWorld();
        Set<Block> logsToBreak = new HashSet<>();
        
        // Find all connected logs (BFS)
        findConnectedLogs(startBlock, logsToBreak, logType);
        
        // Limit the size to prevent server lag from huge trees
        if (logsToBreak.size() > MAX_LOGS_PER_TREE) {
            // Truncate the set to the max size
            List<Block> limitedLogs = new ArrayList<>(logsToBreak);
            limitedLogs = limitedLogs.subList(0, MAX_LOGS_PER_TREE);
            logsToBreak = new HashSet<>(limitedLogs);
        }
        
        // Break all logs in the tree with a slight delay between each for visual effect and to reduce server strain
        int delay = 0;
        for (Block log : logsToBreak) {
            final Block finalLog = log;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Drop the items
                world.dropItemNaturally(
                    finalLog.getLocation(),
                    new ItemStack(finalLog.getType(), dropMultiplier)
                );
                
                // Break the block
                finalLog.setType(Material.AIR);
                world.playSound(finalLog.getLocation(), org.bukkit.Sound.BLOCK_WOOD_BREAK, 0.8f, 1.0f);
                
                // Add XP for the log
                int logXP = xpManager.getXPForLog(logType);
                if (logXP > 0) {
                    xpManager.addXP(player, "logging", logXP);
                }
                
            }, delay);
            
            // Increase delay for next log
            delay++;
            
            // Reset delay periodically to prevent excessive delays for large trees
            if (delay > 10) {
                delay = 2;
            }
        }
    }
    
    /**
     * Recursively finds all logs connected to the starting block
     * @param start The starting block
     * @param found Set to store found logs
     * @param logType The type of log we're looking for
     */
    private void findConnectedLogs(Block start, Set<Block> found, Material logType) {
        // If we've already processed this block or reached the limit, return
        if (found.contains(start) || found.size() >= MAX_LOGS_PER_TREE) {
            return;
        }
        
        // If it's not the right type of log, return
        if (start.getType() != logType) {
            return;
        }
        
        // Add this log to found
        found.add(start);
        
        // Check all adjacent blocks (including diagonals for larger trees)
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    // Skip the center block (the current block)
                    if (x == 0 && y == 0 && z == 0) continue;
                    
                    // Get the adjacent block
                    Block adjacent = start.getRelative(x, y, z);
                    
                    // Recursively check this block
                    findConnectedLogs(adjacent, found, logType);
                }
            }
        }
    }
    
    /**
     * Checks if a material is a log
     * @param material The material to check
     * @return true if the material is a log
     */
    private boolean isLog(Material material) {
        return material == Material.OAK_LOG || 
               material == Material.SPRUCE_LOG || 
               material == Material.BIRCH_LOG || 
               material == Material.JUNGLE_LOG || 
               material == Material.ACACIA_LOG || 
               material == Material.DARK_OAK_LOG || 
               material == Material.MANGROVE_LOG || 
               material == Material.CHERRY_LOG ||
               material == Material.CRIMSON_STEM || 
               material == Material.WARPED_STEM;
    }
    
    /**
     * Checks if a material is a rare wood type
     * @param material The material to check
     * @return true if the material is a rare wood type
     */
    private boolean isRareWood(Material material) {
        return material == Material.JUNGLE_LOG || 
               material == Material.ACACIA_LOG || 
               material == Material.DARK_OAK_LOG || 
               material == Material.MANGROVE_LOG || 
               material == Material.CHERRY_LOG ||
               material == Material.CRIMSON_STEM || 
               material == Material.WARPED_STEM;
    }
    
    /**
     * Gets the sapling type for a given log type
     * @param logType The log material
     * @return The corresponding sapling material, or null if not applicable
     */
    private Material getSaplingTypeForLog(Material logType) {
        return switch (logType) {
            case OAK_LOG -> Material.OAK_SAPLING;
            case SPRUCE_LOG -> Material.SPRUCE_SAPLING;
            case BIRCH_LOG -> Material.BIRCH_SAPLING;
            case JUNGLE_LOG -> Material.JUNGLE_SAPLING;
            case ACACIA_LOG -> Material.ACACIA_SAPLING;
            case DARK_OAK_LOG -> Material.DARK_OAK_SAPLING;
            case MANGROVE_LOG -> Material.MANGROVE_PROPAGULE;
            case CHERRY_LOG -> Material.CHERRY_SAPLING;
            // Nether stems don't have saplings
            default -> null;
        };
    }
    
    /**
     * Applies wood specialization passive if the player has it for the specific wood type
     * @param player The player
     * @param blockType The wood type
     * @return true if a specialization was applied
     */
    private boolean applyWoodSpecialization(Player player, Material blockType) {
        UUID playerId = player.getUniqueId();
        
        // Check for the appropriate specialization based on block type
        return switch (blockType) {
            case OAK_LOG -> passiveManager.hasPassive(playerId, "oakSpecialization");
            case BIRCH_LOG -> passiveManager.hasPassive(playerId, "birchSpecialization");
            case SPRUCE_LOG -> passiveManager.hasPassive(playerId, "spruceSpecialization");
            case JUNGLE_LOG -> passiveManager.hasPassive(playerId, "jungleSpecialization");
            case ACACIA_LOG -> passiveManager.hasPassive(playerId, "acaciaSpecialization");
            case DARK_OAK_LOG -> passiveManager.hasPassive(playerId, "darkOakSpecialization");
            case MANGROVE_LOG -> passiveManager.hasPassive(playerId, "mangroveSpecialization");
            case CHERRY_LOG -> passiveManager.hasPassive(playerId, "cherrySpecialization");
            case CRIMSON_STEM -> passiveManager.hasPassive(playerId, "crimsonSpecialization");
            case WARPED_STEM -> passiveManager.hasPassive(playerId, "warpedSpecialization");
            default -> false;
        };
    }
    
    /**
     * Gets an appropriate extra drop for a stripped log type
     * @param strippedLogType The stripped log material
     * @return The material to drop as an extra item
     */
    private Material getExtraDropForStrippedLog(Material strippedLogType) {
        return switch (strippedLogType) {
            case STRIPPED_OAK_LOG -> Material.PAPER;
            case STRIPPED_BIRCH_LOG -> Material.PAPER;
            case STRIPPED_SPRUCE_LOG -> Material.STICK;
            case STRIPPED_JUNGLE_LOG -> Material.VINE;
            case STRIPPED_ACACIA_LOG -> Material.ORANGE_DYE;
            case STRIPPED_DARK_OAK_LOG -> Material.BROWN_DYE;
            case STRIPPED_MANGROVE_LOG -> Material.RED_DYE;
            case STRIPPED_CHERRY_LOG -> Material.PINK_DYE;
            case STRIPPED_CRIMSON_STEM -> Material.CRIMSON_FUNGUS;
            case STRIPPED_WARPED_STEM -> Material.WARPED_FUNGUS;
            default -> null;
        };
    }
    
    /**
     * Shows nearby rare trees to a player with the Tree Finder passive
     * @param player The player to show nearby trees to
     * @param range The range to check for rare trees
     * @return true if any rare trees were found
     */
    private boolean showNearbyRareTrees(Player player, int range) {
        Location playerLoc = player.getLocation();
        World world = player.getWorld();
        boolean found = false;
        
        // Check for rare trees in a radius around the player
        for (int x = -range; x <= range; x += 10) { // Check every 10 blocks to reduce lag
            for (int z = -range; z <= range; z += 10) {
                // Skip if too far
                if (x*x + z*z > range*range) continue;
                
                Location checkLoc = playerLoc.clone().add(x, 0, z);
                // Adjust Y to find the highest block
                checkLoc.setY(world.getHighestBlockYAt(checkLoc));
                
                Block block = checkLoc.getBlock();
                
                // Check if it's a rare tree
                if (isRareWood(block.getType()) || block.getType() == Material.FLOWERING_AZALEA) {
                    // Show particle effect at the location
                    world.spawnParticle(Particle.COMPOSTER, checkLoc.add(0, 1, 0), 30, 0.5, 1.0, 0.5, 0.01);
                    found = true;
                }
            }
        }
        
        return found;
    }
    
    /**
     * Shows nearby special forests to a player with the Ancient Tree Finder passive
     * @param player The player to show special forests to
     */
    private void showNearbySpecialForests(Player player) {
        // Get player's location to calculate direction
        Location playerLoc = player.getLocation();
        
        // Get the direction the player is facing
        float yaw = playerLoc.getYaw();
        
        // Show a trail of particles in the direction of special forests
        for (int i = 10; i <= 50; i += 10) {
            // Calculate position in front of player based on yaw
            double xOffset = -Math.sin(Math.toRadians(yaw)) * i;
            double zOffset = Math.cos(Math.toRadians(yaw)) * i;
            
            // Create location for particles
            Location particleLoc = playerLoc.clone().add(xOffset, 1, zOffset);
            
            // Spawn particles
            player.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 5, 0.5, 0.5, 0.5, 0.01);
        }
    }
    
    /**
     * Gets a random legendary wood item for the Legendary Harvester passive
     * @return A special ItemStack
     */
    private ItemStack getLegendaryWoodItem() {
        ItemStack[] legendaryItems = {
            // Special wooden tools with enchantments
            createEnchantedTool(Material.WOODEN_AXE),
            createEnchantedTool(Material.WOODEN_SWORD),
            createEnchantedTool(Material.WOODEN_PICKAXE),
            
            // Rare wood-related blocks
            new ItemStack(Material.BEE_NEST, 1),
            new ItemStack(Material.BEEHIVE, 1),
            new ItemStack(Material.BOOKSHELF, 2),
            new ItemStack(Material.JUKEBOX, 1),
            new ItemStack(Material.NOTE_BLOCK, 3),
            
            // Special saplings
            new ItemStack(Material.FLOWERING_AZALEA, 1),
            new ItemStack(Material.MANGROVE_PROPAGULE, 3),
            new ItemStack(Material.CHERRY_SAPLING, 2)
        };
        
        return legendaryItems[random.nextInt(legendaryItems.length)];
    }
    
    /**
     * Creates an enchanted wooden tool for the Legendary Harvester passive
     * @param material The tool material
     * @return An enchanted ItemStack
     */
    private ItemStack createEnchantedTool(Material material) {
        ItemStack tool = new ItemStack(material, 1);
        ItemMeta meta = tool.getItemMeta();
        
        meta.setDisplayName("§6Legendary " + formatMaterialName(material));
        
        // Add enchantments based on tool type
        if (isAxe(material)) {
            tool.addUnsafeEnchantment(Enchantment.EFFICIENCY, 5);
            tool.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        } else if (material == Material.WOODEN_SWORD) {
            tool.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
            tool.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        } else if (material == Material.WOODEN_PICKAXE) {
            tool.addUnsafeEnchantment(Enchantment.EFFICIENCY, 5);
            tool.addUnsafeEnchantment(Enchantment.FORTUNE, 3);
        }
        
        tool.setItemMeta(meta);
        return tool;
    }
    
    /**
     * Formats a material name for display
     * @param material The material to format
     * @return A formatted string
     */
    private String formatMaterialName(Material material) {
        String name = material.toString();
        String[] words = name.split("_");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)));
                formatted.append(word.substring(1).toLowerCase());
                formatted.append(" ");
            }
        }
        
        return formatted.toString().trim();
    }
}
