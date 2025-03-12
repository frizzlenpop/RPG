package org.frizzlenpop.rPGSkillsPlugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
        
        // Start the tree growth booster task
        startTreeGrowthBooster();
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
        List<String> passives = new ArrayList<>(getPlayerPassives(player.getUniqueId()));
        
        // Add skill-specific passives based on player level - to match SkillsGUI
        UUID playerId = player.getUniqueId();
        
        // Get skill levels
        int miningLevel = xpManager.getPlayerLevel(player, "mining");
        int loggingLevel = xpManager.getPlayerLevel(player, "logging");
        int farmingLevel = xpManager.getPlayerLevel(player, "farming");
        int fightingLevel = xpManager.getPlayerLevel(player, "fighting");
        int fishingLevel = xpManager.getPlayerLevel(player, "fishing");
        int enchantingLevel = xpManager.getPlayerLevel(player, "enchanting");
        
        // Add Mining passives
        if (miningLevel >= 5) passives.add("Double Ore Drop");
        if (miningLevel >= 10) passives.add("Auto Smelt");
        if (miningLevel >= 15) passives.add("Fortune Boost");
        if (miningLevel >= 20) passives.add("Auto Smelt Upgrade");
        
        // Add Logging passives
        if (loggingLevel >= 5) passives.add("Double Wood Drop");
        if (loggingLevel >= 10) passives.add("Tree Growth Boost");
        if (loggingLevel >= 15) passives.add("Triple Log Drop");
        
        // Add Farming passives
        if (farmingLevel >= 5) passives.add("Farming XP Boost");
        if (farmingLevel >= 10) passives.add("Auto Replant");
        if (farmingLevel >= 15) passives.add("Double Harvest");
        if (farmingLevel >= 20) passives.add("Growth Boost");
        
        // Add Fighting passives
        if (fightingLevel >= 5) passives.add("Lifesteal");
        if (fightingLevel >= 10) passives.add("Damage Reduction");
        if (fightingLevel >= 15) passives.add("Heal on Kill");
        
        // Add Fishing passives
        if (fishingLevel >= 5) passives.add("XP Boost");
        if (fishingLevel >= 10) passives.add("Treasure Hunter");
        if (fishingLevel >= 15) passives.add("Rare Fish Master");
        if (fishingLevel >= 20) passives.add("Quick Hook");
        
        // Add Enchanting passives
        if (enchantingLevel >= 5) passives.add("Research Master");
        if (enchantingLevel >= 10) passives.add("Book Upgrade");
        if (enchantingLevel >= 15) passives.add("Custom Enchants");
        if (enchantingLevel >= 20) passives.add("Rare Enchant Boost");
        
        return passives;
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
        UUID playerId = player.getUniqueId();
        int level = xpManager.getPlayerLevel(player, "fishing");

        // XP Boost (Level 5) implementation
        if (hasPassive(player, "fishing", "XP Boost")) {
            // The XP boost is handled in the XPManager via getXPMultiplier
            // Let the player know their boost is active
            if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
                player.sendActionBar("§a+10% Fishing XP Boost Applied!");
            }
        }

        // Treasure Hunter (Level 10)
        if (hasPassive(player, "fishing", "Treasure Hunter") && event.getCaught() instanceof Item) {
            if (Math.random() < 0.15) { // 15% increased treasure rate
                Item item = (Item) event.getCaught();
                ItemStack currentItem = item.getItemStack();
                
                // Replace with a better item depending on what was caught
                if (isFish(currentItem.getType())) {
                    // Instead of fish, give treasure
                    ItemStack treasureItem = getTreasureItem();
                    item.setItemStack(treasureItem);
                    player.sendMessage("§6Your Treasure Hunter passive found something special!");
                }
            }
        }

        // Rare Fish (Level 15)
        if (hasPassive(player, "fishing", "Rare Fish Master") && event.getCaught() instanceof Item) {
            if (Math.random() < 0.1) { // 10% chance for rare fish
                Item item = (Item) event.getCaught();
                item.setItemStack(new ItemStack(Material.PUFFERFISH));
                player.sendMessage("§6You caught a rare fish with your Rare Fish Master passive!");
            }
        }

        // Quick Hook (Level 20)
        if (hasPassive(player, "fishing", "Quick Hook")) {
            // This passive reduces the time to catch fish
            if (event.getState() == PlayerFishEvent.State.FISHING) {
                // Schedule a task to potentially reduce fishing time
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Check that player is still fishing and the hook is still there
                    if (player.getInventory().getItemInMainHand().getType() == Material.FISHING_ROD &&
                        event.getHook() != null && !event.getHook().isDead()) {
                        
                        // 20% chance to trigger quick hook
                        if (Math.random() < 0.20) {
                            player.sendActionBar("§b⚡ Quick Hook is helping you catch fish faster!");
                            
                            // Modify the hook properties to catch fish faster
                            // This approach is safer than trying to force a bite directly
                            
                            // Instead of trying to manipulate the hook directly (which can be risky),
                            // we'll drop a fish near the player to simulate a catch
                            ItemStack fishItem = new ItemStack(Material.COD);
                            
                            // Drop the item near the player, which is a safe approach
                            player.getWorld().dropItemNaturally(player.getLocation(), fishItem);
                            
                            // Try to cancel the current fishing
                            event.getHook().remove();
                        }
                    }
                }, 40L); // Check after 2 seconds
            }
        }
    }
    
    // Helper methods for fishing passives
    private boolean isFish(Material material) {
        return material == Material.COD || 
               material == Material.SALMON || 
               material == Material.TROPICAL_FISH || 
               material == Material.PUFFERFISH;
    }
    
    private ItemStack getTreasureItem() {
        // Array of possible treasure items
        Material[] treasures = {
            Material.NAME_TAG,
            Material.SADDLE,
            Material.NAUTILUS_SHELL,
            Material.BOOK,
            Material.BOW,
            Material.ENCHANTED_BOOK,
            Material.FISHING_ROD
        };
        
        // Random treasure selection
        Material treasure = treasures[new Random().nextInt(treasures.length)];
        ItemStack item = new ItemStack(treasure);
        
        // Add random enchantment to enchantable items
        if (treasure == Material.BOW || 
            treasure == Material.FISHING_ROD ||
            treasure == Material.ENCHANTED_BOOK) {
            
            // Get random enchantment
            Enchantment[] enchantments = Enchantment.values();
            Enchantment randomEnchant = enchantments[new Random().nextInt(enchantments.length)];
            
            // Random level between 1 and max for that enchantment
            int level = new Random().nextInt(randomEnchant.getMaxLevel()) + 1;
            
            // Add the enchantment
            item.addUnsafeEnchantment(randomEnchant, level);
        }
        
        return item;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        int level = xpManager.getPlayerLevel(player, "enchanting");

        // Research Master (Level 5) – XP boost for enchanting
        if (hasPassive(player, "enchanting", "Research Master")) {
            // Add 25% more XP from enchanting
            int baseXP = event.getExpLevelCost() * 5; // Base XP from enchanting
            int bonusXP = (int)(baseXP * 0.25);
            
            if (bonusXP > 0) {
                // Award the bonus XP directly
                xpManager.addXP(player, "enchanting", bonusXP);
                player.sendActionBar("§a+25% Enchanting XP from Research Master!");
            }
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
            // 10% chance to add a custom enchantment lore
            if (Math.random() < 0.10) {
                // Get the item being enchanted
                ItemStack item = event.getItem();
                ItemMeta meta = item.getItemMeta();
                
                if (meta != null) {
                    // Add a custom lore "enchantment" based on the item type
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                    
                    // Add a different custom enchant based on item type
                    String customEnchant = getCustomEnchantForItem(item.getType());
                    if (customEnchant != null) {
                        lore.add("§5" + customEnchant);
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        
                        // Notify the player
                        player.sendMessage("§d✨ Your Custom Enchants passive added: " + customEnchant);
                    }
                }
            }
        }

        // Rare Enchant Boost (Level 20)
        if (hasPassive(player, "enchanting", "Rare Enchant Boost")) {
            // 20% chance to add a rare enchantment
            if (Math.random() < 0.20) {
                ItemStack item = event.getItem();
                
                // Get a rare enchantment appropriate for this item
                Enchantment rareEnchant = getRareEnchantment(item.getType());
                if (rareEnchant != null && !item.containsEnchantment(rareEnchant)) {
                    // Add the rare enchantment to the existing enchants
                    Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();
                    enchants.put(rareEnchant, 1); // Start with level 1
                    
                    player.sendMessage("§d✨ Your Rare Enchant Boost added a rare enchantment!");
                }
            }
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
        if (hasPassive(playerId, "doubleOreDrop") && plugin.isPassiveEnabled("mining", "doubleOreChance")) {
            if (Math.random() < plugin.getPassiveValue("mining", "doubleOreChance")) {
                amount = 2;
                player.sendActionBar("§6You got lucky and the ore dropped twice as much!");
            }
        }

        // Fortune Boost implementation
        if (hasPassive(playerId, "fortuneBoost")) {
            ItemStack tool = player.getInventory().getItemInMainHand();
            if (tool.containsEnchantment(Enchantment.FORTUNE)) {
                int fortuneLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);
                // Increase fortune level by 1 with the passive
                int bonusDrops = calculateFortuneDrops(fortuneLevel + 1);
                amount += bonusDrops;
                player.sendActionBar("§6Fortune Boost gave you extra drops!");
            }
        }

        boolean isSmelted = hasPassive(playerId, "autoSmelt");
        boolean isUpgraded = hasPassive(playerId, "autoSmeltUpgrade");
        Material dropMaterial = getOreDrop(block.getType(), isSmelted);

        // Auto Smelt Upgrade implementation
        if (isSmelted && isUpgraded) {
            // 20% chance for double smelted output
            if (Math.random() < 0.20) {
                amount *= 2;
                player.sendActionBar("§6Auto Smelt Upgrade doubled your smelting output!");
            }
        }

        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropMaterial, amount));
    }

    // Helper method for Fortune Boost passive
    private int calculateFortuneDrops(int fortuneLevel) {
        // Standard Minecraft fortune algorithm with our enhancement
        double chance = (fortuneLevel) / 2.0;
        int bonusDrops = 0;
        
        if (Math.random() < chance) {
            bonusDrops = new Random().nextInt(fortuneLevel + 2) - 1;
            if (bonusDrops < 0) bonusDrops = 0;
        }
        
        return bonusDrops;
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
        if (hasPassive(playerId, "doubleCropYield") && plugin.isPassiveEnabled("farming", "doubleCropChance")) {
            if (Math.random() < plugin.getPassiveValue("farming", "doubleCropChance")) {
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

    // Helper method to check if a material is a log
    private boolean isLog(Material material) {
        return material.name().contains("_LOG");
    }

    // Handles the block break event for logs
    @EventHandler
    public void onLogBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        UUID playerId = player.getUniqueId();
        
        if (!isLog(block.getType())) {
            return;
        }

        // Don't modify drops if the player doesn't have any logging passives
        if (!hasPassive(playerId, "doubleWoodDrop") && 
            !hasPassive(playerId, "tripleLogDrop")) {
            return;
        }

        event.setDropItems(false);
        int amount = 1;

        // Check for Double Wood Drop passive (Level 5)
        if (hasPassive(playerId, "doubleWoodDrop")) {
            if (Math.random() < 0.25) { // 25% chance for double drops
                amount = 2;
                player.sendActionBar("§6You got lucky and the log dropped twice as much!");
            }
        }
        
        // Check for Triple Log Drop passive (Level 15)
        if (hasPassive(playerId, "tripleLogDrop")) {
            if (Math.random() < 0.15) { // 15% chance for triple drops
                amount = 3;
                player.sendActionBar("§6You got extremely lucky and the log dropped triple drops!");
            }
        }

        // Create and drop the item
        ItemStack drops = new ItemStack(block.getType(), amount);
        block.getWorld().dropItemNaturally(block.getLocation(), drops);
    }

    // Implement Tree Growth Boost (runs on a timer)
    public void startTreeGrowthBooster() {
        // Schedule a task that runs every 5 minutes (6000 ticks)
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            // For every online player with the Tree Growth Boost passive
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (hasPassive(player.getUniqueId(), "treeGrowthBoost")) {
                    boostNearbySaplings(player);
                }
            }
        }, 6000L, 6000L);
    }

    private void boostNearbySaplings(Player player) {
        int radius = 10; // 10 block radius around player
        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location blockLoc = playerLoc.clone().add(x, y, z);
                    Block block = world.getBlockAt(blockLoc);
                    
                    // Check if the block is a sapling
                    if (block.getType().name().contains("_SAPLING")) {
                        // 30% chance to apply bonemeal effect
                        if (Math.random() < 0.3) {
                            // Apply bonemeal effect (simulate growth)
                            block.applyBoneMeal(BlockFace.UP);
                            player.sendActionBar("§6Your Tree Growth Boost passive is working!");
                        }
                    }
                }
            }
        }
    }

    // Lifesteal passive implementation
    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent event) {
        // Only applies if a player is dealing damage
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getDamager();
        UUID playerId = player.getUniqueId();
        
        // Check for Lifesteal passive
        if (hasPassive(playerId, "lifesteal")) {
            // Calculate lifesteal amount (10% of damage dealt)
            double damage = event.getFinalDamage();
            double healAmount = damage * 0.10;
            
            // Only apply lifesteal if player needs healing and there's a reasonable amount to heal
            if (player.getHealth() < player.getMaxHealth() && healAmount > 0.5) {
                // 25% chance to trigger lifesteal
                if (Math.random() < 0.25) {
                    double newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
                    player.setHealth(newHealth);
                    player.sendActionBar("§c♥ Lifesteal restored " + String.format("%.1f", healAmount) + " health");
                }
            }
        }
    }
    
    // Damage Reduction passive implementation
    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        // Only applies to players getting damaged
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        UUID playerId = player.getUniqueId();
        
        // Check for Damage Reduction passive
        if (hasPassive(playerId, "damageReduction")) {
            // Reduce damage by 10%
            double originalDamage = event.getDamage();
            double reducedDamage = originalDamage * 0.9; // 10% reduction
            event.setDamage(reducedDamage);
            
            // Only show message if reduction is significant
            if (originalDamage - reducedDamage > 0.5) {
                player.sendActionBar("§9🛡 Damage Reduction absorbed " + 
                    String.format("%.1f", (originalDamage - reducedDamage)) + " damage");
            }
        }
    }

    // Helper method to get a custom enchant based on item type
    private String getCustomEnchantForItem(Material material) {
        // Weapons
        if (material.name().contains("SWORD")) {
            return "Vampiric Touch";
        } else if (material.name().contains("AXE")) {
            return "Lumberjack";
        } else if (material.name().contains("BOW")) {
            return "Multi-Shot";
        // Tools
        } else if (material.name().contains("PICKAXE")) {
            return "Ore Seeker";
        } else if (material.name().contains("SHOVEL")) {
            return "Excavator";
        } else if (material.name().contains("HOE")) {
            return "Harvester";
        // Armor
        } else if (material.name().contains("HELMET")) {
            return "Eagle Eye";
        } else if (material.name().contains("CHESTPLATE")) {
            return "Thorns Aura";
        } else if (material.name().contains("LEGGINGS")) {
            return "Swift Step";
        } else if (material.name().contains("BOOTS")) {
            return "Cushioned Fall";
        // Fishing
        } else if (material == Material.FISHING_ROD) {
            return "Deep Sea Fisher";
        }
        
        return "Enchanted Item";
    }

    // Helper method to get a rare enchantment for the item
    private Enchantment getRareEnchantment(Material material) {
        // Define rare enchantments for different item types
        if (material.name().contains("SWORD")) {
            return Enchantment.FIRE_ASPECT;
        } else if (material.name().contains("AXE") || material.name().contains("PICKAXE")) {
            return Enchantment.SILK_TOUCH;
        } else if (material.name().contains("BOW")) {
            return Enchantment.INFINITY;
        } else if (material.name().contains("HELMET") || material.name().contains("CHESTPLATE") 
                || material.name().contains("LEGGINGS") || material.name().contains("BOOTS")) {
            return Enchantment.THORNS;
        } else if (material == Material.FISHING_ROD) {
            return Enchantment.LUCK_OF_THE_SEA;
        } else if (material == Material.TRIDENT) {
            return Enchantment.CHANNELING;
        }
        
        // Default for other items
        return null;
    }
}
