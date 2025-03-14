package org.frizzlenpop.rPGSkillsPlugin.skilltree;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages the skill tree system, including skill tree nodes, player skill points,
 * and unlocked nodes.
 */
public class SkillTreeManager implements Listener {
    private final RPGSkillsPlugin plugin;
    private final PlayerDataManager dataManager;
    private final XPManager xpManager;
    private final PlayerLevel playerLevel;
    
    private final Map<String, SkillTreeNode> nodes;
    private final Map<UUID, Set<String>> unlockedNodes;
    private final Map<UUID, Integer> spentPoints;
    
    private static final String CONFIG_NODES_PATH = "skill_tree.nodes";
    private static final String PLAYER_DATA_NODES_PATH = "skill_tree.unlocked_nodes";
    private static final String PLAYER_DATA_POINTS_SPENT_PATH = "skill_tree.points_spent";
    
    // Added for skill tree directory and files
    private static final String SKILL_TREE_FOLDER = "skilltree";
    private static final String[] SKILL_CATEGORIES = {"warrior", "mining", "logging", "farming", "fighting", "fishing", "excavation", "enchanting"};

    /**
     * Constructor for the skill tree manager
     */
    public SkillTreeManager(RPGSkillsPlugin plugin, PlayerDataManager dataManager, XPManager xpManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.xpManager = xpManager;
        this.playerLevel = new PlayerLevel(dataManager, xpManager);
        
        this.nodes = new HashMap<>();
        this.unlockedNodes = new HashMap<>();
        this.spentPoints = new HashMap<>();
        
        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // Create skill tree directory if it doesn't exist
        createSkillTreeDirectory();
        
        // Load skill tree configuration
        loadSkillTreeConfig();
        
        // Apply effects to online players (in case of reload)
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player);
            applyAllUnlockedEffects(player);
        }
        
        // Register commands
        registerCommands();
    }
    
    /**
     * Register commands related to skill tree
     */
    private void registerCommands() {
        plugin.getCommand("skilltree").setExecutor((sender, command, label, args) -> {
            if (args.length > 0 && args[0].equalsIgnoreCase("admin") && sender.hasPermission("skilltree.admin")) {
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("resetplayer") && args.length > 2) {
                        String playerName = args[2];
                        Player targetPlayer = Bukkit.getPlayer(playerName);
                        
                        if (targetPlayer != null) {
                            resetPlayerSkills(targetPlayer);
                            sender.sendMessage("§aRemoved all skills from player " + targetPlayer.getName());
                            return true;
                        } else {
                            sender.sendMessage("§cPlayer not found: " + playerName);
                        }
                    } else if (args[1].equalsIgnoreCase("reload")) {
                        reloadSkillTreeConfig();
                        sender.sendMessage("§aSkill tree configuration reloaded.");
                        return true;
                    }
                }
                
                sender.sendMessage("§cUsage: /skilltree admin [reload|resetplayer <player>]");
                return true;
            }
            return false;
        });
    }
    
    /**
     * Reset all skills for a player, removing all effects
     */
    public void resetPlayerSkills(Player player) {
        UUID playerUUID = player.getUniqueId();
        Set<String> playerNodes = unlockedNodes.getOrDefault(playerUUID, new HashSet<>());
        
        // Remove all effects from the player
        for (String nodeId : playerNodes) {
            SkillTreeNode node = nodes.get(nodeId);
            if (node != null) {
                node.removeEffects(player);
            }
        }
        
        // Clear unlocked nodes and reset spent points
        playerNodes.clear();
        unlockedNodes.put(playerUUID, playerNodes);
        spentPoints.put(playerUUID, 0);
        
        // Save player data
        savePlayerData(player);
        
        player.sendMessage("§c⚠ All your skill tree perks have been reset!");
    }
    
    /**
     * Reset all skills for a player, removing all unlocked nodes and refunding points
     * @param player The player to reset
     * @return The number of nodes that were reset
     */
    public int resetAllSkills(Player player) {
        UUID playerUUID = player.getUniqueId();
        Set<String> playerNodes = unlockedNodes.getOrDefault(playerUUID, new HashSet<>());
        
        if (playerNodes.isEmpty()) {
            return 0;
        }
        
        // First, remove all effects
        for (String nodeId : playerNodes) {
            SkillTreeNode node = nodes.get(nodeId);
            if (node != null) {
                node.removeEffects(player);
            }
        }
        
        // Get the number of nodes being reset
        int nodeCount = playerNodes.size();
        
        // Reset spent points
        int pointsSpent = spentPoints.getOrDefault(playerUUID, 0);
        spentPoints.put(playerUUID, 0);
        
        // Clear unlocked nodes
        unlockedNodes.put(playerUUID, new HashSet<>());
        
        // Save player data
        savePlayerData(player);
        
        plugin.getLogger().info("Reset all skills for " + player.getName() + 
                ", removed " + nodeCount + " nodes and refunded " + pointsSpent + " points");
        
        return nodeCount;
    }
    
    /**
     * Create the skill tree directory and default files if they don't exist
     */
    private void createSkillTreeDirectory() {
        File skillTreeDir = new File(plugin.getDataFolder(), SKILL_TREE_FOLDER);
        if (!skillTreeDir.exists()) {
            skillTreeDir.mkdirs();
        }
        
        // Create default category files if they don't exist
        for (String category : SKILL_CATEGORIES) {
            File categoryFile = new File(skillTreeDir, category + ".yml");
            if (!categoryFile.exists()) {
                YamlConfiguration config = new YamlConfiguration();
                
                try {
                    config.save(categoryFile);
                    plugin.getLogger().info("Created empty skill tree file: " + category + ".yml");
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not create skill tree file: " + category + ".yml");
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Load skill tree configuration from files
     */
    private void loadSkillTreeConfig() {
        // Clear existing nodes
        nodes.clear();
        
        File skillTreeDir = new File(plugin.getDataFolder(), SKILL_TREE_FOLDER);
        if (!skillTreeDir.exists() || !skillTreeDir.isDirectory()) {
            plugin.getLogger().warning("Skill tree directory not found, using default configuration.");
            migrateFromDefaultConfig();
            return;
        }
        
        // Check if directory is empty - if so, migrate from config.yml
        File[] files = skillTreeDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            migrateFromDefaultConfig();
            return;
        }
        
        // Load each category file
        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                
                // Load nodes from this file
                for (String nodeId : config.getKeys(false)) {
                    ConfigurationSection nodeSection = config.getConfigurationSection(nodeId);
                    if (nodeSection != null) {
                        loadNodeFromConfig(nodeId, nodeSection);
                    }
                }
                
                plugin.getLogger().info("Loaded skill tree nodes from: " + file.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("Error loading skill tree file: " + file.getName());
                e.printStackTrace();
            }
        }
        
        if (nodes.isEmpty()) {
            plugin.getLogger().warning("No skill tree nodes loaded, using default configuration.");
            migrateFromDefaultConfig();
        }
    }
    
    /**
     * Load a specific node from a configuration section
     */
    private void loadNodeFromConfig(String nodeId, ConfigurationSection nodeSection) {
        try {
            String name = nodeSection.getString("name", "Unknown Node");
            String description = nodeSection.getString("description", "");
            int pointCost = nodeSection.getInt("point_cost", 1);
            List<String> prerequisites = nodeSection.getStringList("prerequisites");
            String iconName = nodeSection.getString("icon", "BOOK");
            Material icon = Material.getMaterial(iconName) != null ? 
                            Material.getMaterial(iconName) : Material.BOOK;
            String typeName = nodeSection.getString("type", "PASSIVE");
            SkillTreeNode.NodeType type = SkillTreeNode.NodeType.valueOf(typeName);
            
            SkillTreeNode node = new SkillTreeNode(nodeId, name, description, pointCost, 
                                             prerequisites, icon, type);
            
            // Load effects
            ConfigurationSection effectsSection = nodeSection.getConfigurationSection("effects");
            if (effectsSection != null) {
                for (String effectKey : effectsSection.getKeys(false)) {
                    ConfigurationSection effectSection = effectsSection.getConfigurationSection(effectKey);
                    if (effectSection != null) {
                        String effectTypeName = effectSection.getString("type");
                        String target = effectSection.getString("target", "");
                        double value = effectSection.getDouble("value", 0.0);
                        
                        SkillTreeNode.EffectType effectType = SkillTreeNode.EffectType.valueOf(effectTypeName);
                        node.addEffect(new SkillTreeNode.NodeEffect(effectType, target, value));
                    }
                }
            }
            
            nodes.put(nodeId, node);
            plugin.getLogger().info("Loaded skill tree node: " + nodeId);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load skill tree node " + nodeId + ": " + e.getMessage());
        }
    }
    
    /**
     * Migrate nodes from config.yml to separate files
     */
    private void migrateFromDefaultConfig() {
        plugin.getLogger().info("Migrating skill tree configuration to separate files...");
        
        // First, check if there are any nodes in the config.yml
        ConfigurationSection nodesSection = plugin.getConfig().getConfigurationSection(CONFIG_NODES_PATH);
        if (nodesSection == null || nodesSection.getKeys(false).isEmpty()) {
            // No nodes in config.yml, create default nodes directly
            createDefaultNodes();
            return;
        }
        
        // Map to organize nodes by category
        Map<String, YamlConfiguration> categoryConfigs = new HashMap<>();
        
        // Create configurations for each category
        for (String category : SKILL_CATEGORIES) {
            categoryConfigs.put(category, new YamlConfiguration());
        }
        
        // Process each node from config.yml
        for (String nodeId : nodesSection.getKeys(false)) {
            ConfigurationSection nodeSection = nodesSection.getConfigurationSection(nodeId);
            if (nodeSection == null) continue;
            
            // Determine which category this node belongs to
            String category = determineNodeCategory(nodeId);
            YamlConfiguration categoryConfig = categoryConfigs.get(category);
            
            // Copy all node properties to the category configuration
            for (String key : nodeSection.getKeys(true)) {
                if (nodeSection.isConfigurationSection(key)) continue;
                categoryConfig.set(nodeId + "." + key, nodeSection.get(key));
            }
            
            // Load the node into memory
            loadNodeFromConfig(nodeId, nodeSection);
        }
        
        // Save each category to its respective file
        for (String category : categoryConfigs.keySet()) {
            YamlConfiguration config = categoryConfigs.get(category);
            File categoryFile = new File(new File(plugin.getDataFolder(), SKILL_TREE_FOLDER), category + ".yml");
            
            try {
                config.save(categoryFile);
                plugin.getLogger().info("Saved skill tree nodes for category: " + category);
            } catch (IOException e) {
                plugin.getLogger().severe("Error saving skill tree category: " + category);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Determine the category for a node ID
     */
    private String determineNodeCategory(String nodeId) {
        for (String category : SKILL_CATEGORIES) {
            if (nodeId.startsWith(category + "_")) {
                return category;
            }
        }
        
        // Default to warrior if we can't determine category
        return "warrior";
    }
    
    /**
     * Create default nodes if no configuration exists
     */
    private void createDefaultNodes() {
        plugin.getLogger().info("Creating default skill tree nodes...");
        
        // Create default nodes directly in separate files
        for (String category : SKILL_CATEGORIES) {
            createDefaultNodesForCategory(category);
        }
    }
    
    /**
     * Create default nodes for a specific category
     */
    private void createDefaultNodesForCategory(String category) {
        YamlConfiguration config = new YamlConfiguration();
        File categoryFile = new File(new File(plugin.getDataFolder(), SKILL_TREE_FOLDER), category + ".yml");
        
        switch (category) {
            case "warrior":
                // Warrior skill tree - Combat focused abilities
                createWarriorSkillTree(config);
                break;
            case "mining":
                // Mining skill tree - Mining efficiency and special drops
                createMiningSkillTree(config);
                break;
            case "logging":
                // Logging skill tree - Woodcutting and tree-related abilities
                createLoggingSkillTree(config);
                break;
            case "farming":
                // Farming skill tree - Crop growth and harvesting abilities
                createFarmingSkillTree(config);
                break;
            case "fighting":
                // Fighting skill tree - PvP focused abilities 
                createFightingSkillTree(config);
                break;
            case "fishing":
                // Fishing skill tree - Fishing efficiency and special catches
                createFishingSkillTree(config);
                break;
            case "excavation":
                // Excavation skill tree - Digging abilities
                createExcavationSkillTree(config);
                break;
            case "enchanting":
                // Enchanting skill tree - Enchanting and magical abilities
                createEnchantingSkillTree(config);
                break;
        }
        
        try {
            config.save(categoryFile);
            plugin.getLogger().info("Created default skill tree for category: " + category);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving default skill tree for category: " + category);
            e.printStackTrace();
        }
    }
    
    /**
     * Create default warrior skill tree
     */
    private void createWarriorSkillTree(YamlConfiguration config) {
        // Tier 1 - Base abilities
        createSkillNode(config, "warrior_strength", "Strength", "Increases melee damage by 10%", 1, 
                new ArrayList<>(), "IRON_SWORD", "PASSIVE", 
                new NodeEffectData("DAMAGE_MULTIPLIER", "melee", 0.1));
        
        createSkillNode(config, "warrior_toughness", "Toughness", "Reduces damage taken by 5%", 1, 
                new ArrayList<>(), "IRON_CHESTPLATE", "PASSIVE", 
                new NodeEffectData("DAMAGE_REDUCTION", "all", 0.05));
        
        createSkillNode(config, "warrior_health", "Vitality", "Increases max health by 2 hearts", 1, 
                new ArrayList<>(), "APPLE", "PASSIVE", 
                new NodeEffectData("ATTRIBUTE", "GENERIC_MAX_HEALTH", 4.0));
        
        // Tier 2 - Advanced abilities (require tier 1)
        List<String> strengthPrereq = Collections.singletonList("warrior_strength");
        createSkillNode(config, "warrior_power_strike", "Power Strike", "15% chance to deal double damage", 2, 
                strengthPrereq, "DIAMOND_SWORD", "PASSIVE", 
                new NodeEffectData("CRITICAL_CHANCE", "melee", 0.15));
        
        List<String> toughnessPrereq = Collections.singletonList("warrior_toughness");
        createSkillNode(config, "warrior_resilience", "Resilience", "20% chance to ignore knockback", 2, 
                toughnessPrereq, "SHIELD", "PASSIVE", 
                new NodeEffectData("KNOCKBACK_RESISTANCE", "all", 0.2));
        
        List<String> healthPrereq = Collections.singletonList("warrior_health");
        createSkillNode(config, "warrior_regeneration", "Regeneration", "Regenerate health 10% faster", 2, 
                healthPrereq, "GOLDEN_APPLE", "PASSIVE", 
                new NodeEffectData("REGENERATION_RATE", "health", 0.1));
        
        // Tier 3 - Specialized abilities (require tier 2)
        List<String> powerStrikePrereq = Collections.singletonList("warrior_power_strike");
        createSkillNode(config, "warrior_cleave", "Cleave", "Attacks deal 25% damage to nearby enemies", 3, 
                powerStrikePrereq, "NETHERITE_AXE", "PASSIVE", 
                new NodeEffectData("AOE_DAMAGE", "melee", 0.25));
        
        List<String> resiliencePrereq = Collections.singletonList("warrior_resilience");
        createSkillNode(config, "warrior_deflection", "Deflection", "10% chance to reflect projectiles", 3, 
                resiliencePrereq, "ARROW", "PASSIVE", 
                new NodeEffectData("PROJECTILE_REFLECTION", "all", 0.1));
        
        List<String> regenPrereq = Collections.singletonList("warrior_regeneration");
        createSkillNode(config, "warrior_second_wind", "Second Wind", "Gain Regeneration II for 5s when below 4 hearts", 3, 
                regenPrereq, "TOTEM_OF_UNDYING", "PASSIVE", 
                new NodeEffectData("LOW_HEALTH_EFFECT", "regeneration", 2.0));
        
        // Tier 4 - Master abilities (require multiple tier 3 prerequisites)
        List<String> masterPrereq = Arrays.asList("warrior_cleave", "warrior_deflection", "warrior_second_wind");
        createSkillNode(config, "warrior_battle_master", "Battle Master", "Gain Strength I for 5s after killing an enemy", 5, 
                masterPrereq, "BEACON", "PASSIVE", 
                new NodeEffectData("ON_KILL_EFFECT", "strength", 1.0));
    }
    
    /**
     * Create default mining skill tree
     */
    private void createMiningSkillTree(YamlConfiguration config) {
        // Tier 1 - Base abilities
        createSkillNode(config, "mining_efficiency", "Efficiency", "Mine blocks 10% faster", 1, 
                new ArrayList<>(), "IRON_PICKAXE", "PASSIVE", 
                new NodeEffectData("MINING_SPEED", "all", 0.1));
        
        createSkillNode(config, "mining_fortune", "Fortune", "5% chance to get double drops", 1, 
                new ArrayList<>(), "GOLD_NUGGET", "PASSIVE", 
                new NodeEffectData("DOUBLE_DROP_CHANCE", "mining", 0.05));
        
        createSkillNode(config, "mining_smelting", "Auto-Smelt", "5% chance to auto-smelt ores", 1, 
                new ArrayList<>(), "FURNACE", "PASSIVE", 
                new NodeEffectData("AUTO_SMELT_CHANCE", "ores", 0.05));
        
        // Tier 2 abilities
        List<String> efficiencyPrereq = Collections.singletonList("mining_efficiency");
        createSkillNode(config, "mining_haste", "Haste", "Mine blocks 20% faster", 2, 
                efficiencyPrereq, "DIAMOND_PICKAXE", "PASSIVE", 
                new NodeEffectData("MINING_SPEED", "all", 0.2));
        
        List<String> fortunePrereq = Collections.singletonList("mining_fortune");
        createSkillNode(config, "mining_treasure", "Treasure Hunter", "1% chance to find gems in stone", 2, 
                fortunePrereq, "EMERALD", "PASSIVE", 
                new NodeEffectData("RARE_DROP_CHANCE", "stone", 0.01));
        
        List<String> smeltingPrereq = Collections.singletonList("mining_smelting");
        createSkillNode(config, "mining_expert_smelter", "Expert Smelter", "15% chance to auto-smelt ores", 2, 
                smeltingPrereq, "BLAST_FURNACE", "PASSIVE", 
                new NodeEffectData("AUTO_SMELT_CHANCE", "ores", 0.15));
    }

    // Create nodes for the remaining skill trees in similar fashion
    private void createLoggingSkillTree(YamlConfiguration config) {
        // Base tier
        createSkillNode(config, "logging_efficiency", "Lumber Efficiency", "Cut wood 10% faster", 1, 
                new ArrayList<>(), "IRON_AXE", "PASSIVE", 
                new NodeEffectData("WOODCUTTING_SPEED", "all", 0.1));
                
        createSkillNode(config, "logging_harvest", "Better Harvest", "10% chance to get extra logs", 1, 
                new ArrayList<>(), "OAK_LOG", "PASSIVE", 
                new NodeEffectData("EXTRA_DROPS", "logs", 0.1));
                
        // Advanced tier
        List<String> efficiencyPrereq = Collections.singletonList("logging_efficiency");
        createSkillNode(config, "logging_timber", "Timber", "5% chance to instantly cut down a tree", 3, 
                efficiencyPrereq, "DIAMOND_AXE", "PASSIVE", 
                new NodeEffectData("TREE_FELLER_CHANCE", "all", 0.05));
    }

    private void createFarmingSkillTree(YamlConfiguration config) {
        // Base tier
        createSkillNode(config, "farming_growth", "Green Thumb", "Crops grow 10% faster", 1, 
                new ArrayList<>(), "WHEAT", "PASSIVE", 
                new NodeEffectData("GROWTH_RATE", "crops", 0.1));
                
        createSkillNode(config, "farming_harvest", "Bountiful Harvest", "15% chance for extra crop drops", 1, 
                new ArrayList<>(), "WHEAT_SEEDS", "PASSIVE", 
                new NodeEffectData("EXTRA_DROPS", "crops", 0.15));
                
        // Advanced tier
        List<String> growthPrereq = Collections.singletonList("farming_growth");
        createSkillNode(config, "farming_fertilizer", "Natural Fertilizer", "Right-click crops to boost growth", 3, 
                growthPrereq, "BONE_MEAL", "ACTIVE", 
                new NodeEffectData("GROWTH_BOOST", "crops", 1.0));
    }

    private void createFightingSkillTree(YamlConfiguration config) {
        // Base tier
        createSkillNode(config, "fighting_damage", "Combat Training", "Deal 5% more damage to players", 1, 
                new ArrayList<>(), "IRON_SWORD", "PASSIVE", 
                new NodeEffectData("PVP_DAMAGE", "all", 0.05));
                
        createSkillNode(config, "fighting_defense", "Combat Defense", "Take 5% less damage from players", 1, 
                new ArrayList<>(), "SHIELD", "PASSIVE", 
                new NodeEffectData("PVP_DEFENSE", "all", 0.05));
                
        // Advanced tier
        List<String> damagePrereq = Collections.singletonList("fighting_damage");
        createSkillNode(config, "fighting_combo", "Combo Strikes", "Consecutive hits deal 2% more damage (stacking)", 3, 
                damagePrereq, "DIAMOND_SWORD", "PASSIVE", 
                new NodeEffectData("COMBO_DAMAGE", "melee", 0.02));
    }

    private void createFishingSkillTree(YamlConfiguration config) {
        // Base tier
        createSkillNode(config, "fishing_luck", "Fishing Luck", "10% better chance for good loot", 1, 
                new ArrayList<>(), "FISHING_ROD", "PASSIVE", 
                new NodeEffectData("FISHING_LUCK", "all", 0.1));
                
        createSkillNode(config, "fishing_speed", "Quick Catch", "Fish bite 15% faster", 1, 
                new ArrayList<>(), "COD", "PASSIVE", 
                new NodeEffectData("FISHING_SPEED", "all", 0.15));
                
        // Advanced tier
        List<String> luckPrereq = Collections.singletonList("fishing_luck");
        createSkillNode(config, "fishing_treasure", "Treasure Hunter", "5% chance to catch treasure items", 3, 
                luckPrereq, "GOLDEN_APPLE", "PASSIVE", 
                new NodeEffectData("TREASURE_CHANCE", "fishing", 0.05));
    }

    private void createExcavationSkillTree(YamlConfiguration config) {
        // Base tier
        createSkillNode(config, "excavation_efficiency", "Efficient Digging", "Dig 10% faster", 1, 
                new ArrayList<>(), "IRON_SHOVEL", "PASSIVE", 
                new NodeEffectData("DIGGING_SPEED", "all", 0.1));
                
        createSkillNode(config, "excavation_treasure", "Treasure Seeker", "1% chance to find items while digging", 1, 
                new ArrayList<>(), "GOLD_NUGGET", "PASSIVE", 
                new NodeEffectData("EXCAVATION_TREASURE", "all", 0.01));
                
        // Advanced tier
        List<String> efficiencyPrereq = Collections.singletonList("excavation_efficiency");
        createSkillNode(config, "excavation_archeologist", "Archeologist", "5% chance to find rare items in certain biomes", 3, 
                efficiencyPrereq, "DIAMOND", "PASSIVE", 
                new NodeEffectData("BIOME_TREASURE", "special", 0.05));
    }

    private void createEnchantingSkillTree(YamlConfiguration config) {
        // Base tier
        createSkillNode(config, "enchanting_experience", "Arcane Knowledge", "Gain 10% more enchanting XP", 1, 
                new ArrayList<>(), "ENCHANTING_TABLE", "PASSIVE", 
                new NodeEffectData("ENCHANTING_XP", "all", 0.1));
                
        createSkillNode(config, "enchanting_cost", "Efficient Enchanter", "Enchanting costs 10% less levels", 1, 
                new ArrayList<>(), "EXPERIENCE_BOTTLE", "PASSIVE", 
                new NodeEffectData("ENCHANTING_COST", "all", -0.1));
                
        // Advanced tier
        List<String> experiencePrereq = Collections.singletonList("enchanting_experience");
        createSkillNode(config, "enchanting_quality", "Quality Enchanter", "5% chance for higher level enchantments", 3, 
                experiencePrereq, "BOOKSHELF", "PASSIVE", 
                new NodeEffectData("ENCHANTMENT_QUALITY", "all", 0.05));
    }
    
    /**
     * Helper class to store node effect data
     */
    private static class NodeEffectData {
        final String type;
        final String target;
        final double value;
        
        NodeEffectData(String type, String target, double value) {
            this.type = type;
            this.target = target;
            this.value = value;
        }
    }
    
    /**
     * Helper method to create a skill node in the configuration
     */
    private void createSkillNode(YamlConfiguration config, String nodeId, String name, String description, 
                              int pointCost, List<String> prerequisites, String icon, String type, 
                              NodeEffectData... effects) {
        config.set(nodeId + ".name", name);
        config.set(nodeId + ".description", description);
        config.set(nodeId + ".point_cost", pointCost);
        config.set(nodeId + ".prerequisites", prerequisites);
        config.set(nodeId + ".icon", icon);
        config.set(nodeId + ".type", type);
        
        // Add effects
        for (int i = 0; i < effects.length; i++) {
            NodeEffectData effect = effects[i];
            config.set(nodeId + ".effects.effect" + i + ".type", effect.type);
            config.set(nodeId + ".effects.effect" + i + ".target", effect.target);
            config.set(nodeId + ".effects.effect" + i + ".value", effect.value);
        }
    }
    
    /**
     * Load player's unlocked nodes and spent points
     */
    public void loadPlayerData(Player player) {
        UUID playerUUID = player.getUniqueId();
        FileConfiguration playerData = dataManager.getPlayerData(playerUUID);
        
        // Load unlocked nodes
        List<String> nodesList = playerData.getStringList(PLAYER_DATA_NODES_PATH);
        Set<String> nodesSet = new HashSet<>(nodesList);
        unlockedNodes.put(playerUUID, nodesSet);
        
        // Load spent points
        int points = playerData.getInt(PLAYER_DATA_POINTS_SPENT_PATH, 0);
        spentPoints.put(playerUUID, points);
    }
    
    /**
     * Save player's unlocked nodes and spent points
     */
    public void savePlayerData(Player player) {
        UUID playerUUID = player.getUniqueId();
        FileConfiguration playerData = dataManager.getPlayerData(playerUUID);
        
        // Save unlocked nodes
        Set<String> nodesSet = unlockedNodes.getOrDefault(playerUUID, new HashSet<>());
        List<String> nodesList = new ArrayList<>(nodesSet);
        playerData.set(PLAYER_DATA_NODES_PATH, nodesList);
        
        // Save spent points
        int points = spentPoints.getOrDefault(playerUUID, 0);
        playerData.set(PLAYER_DATA_POINTS_SPENT_PATH, points);
        
        dataManager.savePlayerData(playerUUID, playerData);
    }
    
    /**
     * Apply all unlocked node effects to a player
     */
    public void applyAllUnlockedEffects(Player player) {
        UUID playerUUID = player.getUniqueId();
        Set<String> playerNodes = unlockedNodes.getOrDefault(playerUUID, new HashSet<>());
        
        // First, remove all effects for each node the player has unlocked
        // This ensures we don't stack effects when relogging or reapplying
        for (String nodeId : playerNodes) {
            SkillTreeNode node = nodes.get(nodeId);
            if (node != null) {
                node.removeEffects(player);
            }
        }
        
        // Now apply the effects
        for (String nodeId : playerNodes) {
            SkillTreeNode node = nodes.get(nodeId);
            if (node != null) {
                node.applyEffects(player);
            }
        }
    }
    
    /**
     * Check if a player has unlocked a specific node
     */
    public boolean hasUnlockedNode(Player player, String nodeId) {
        UUID playerUUID = player.getUniqueId();
        Set<String> playerNodes = unlockedNodes.getOrDefault(playerUUID, Collections.emptySet());
        return playerNodes.contains(nodeId);
    }
    
    /**
     * Check if a player can unlock a specific node
     */
    public boolean canUnlockNode(Player player, String nodeId) {
        UUID playerUUID = player.getUniqueId();
        SkillTreeNode node = nodes.get(nodeId);
        
        if (node == null) {
            return false;
        }
        
        // Check if already unlocked
        if (hasUnlockedNode(player, nodeId)) {
            return false;
        }
        
        // Special case: If this is a base node (no prerequisites) and the player has no unlocked nodes in 
        // this category, allow unlocking it even without points to ensure new players can start
        String category = getCategoryFromNodeId(nodeId);
        boolean isBaseNode = node.getPrerequisites().isEmpty();
        boolean hasNodesInThisCategory = false;
        
        // Check if player has any nodes in this category
        for (String unlockedNodeId : unlockedNodes.getOrDefault(playerUUID, Collections.emptySet())) {
            if (unlockedNodeId.startsWith(category + "_")) {
                hasNodesInThisCategory = true;
                break;
            }
        }
        
        // First node in each category is free for new players
        if (isBaseNode && !hasNodesInThisCategory) {
            // Still check prerequisites (should be none for base nodes, but just in case)
            for (String prerequisite : node.getPrerequisites()) {
                if (!hasUnlockedNode(player, prerequisite)) {
                    return false;
                }
            }
            return true;
        }
        
        // Check available points (for non-base nodes)
        int availablePoints = getAvailableSkillPoints(player);
        if (availablePoints < node.getPointCost()) {
            return false;
        }
        
        // Check prerequisites
        for (String prerequisite : node.getPrerequisites()) {
            if (!hasUnlockedNode(player, prerequisite)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Extract category from node ID (e.g., "warrior_strength" -> "warrior")
     */
    private String getCategoryFromNodeId(String nodeId) {
        if (nodeId.contains("_")) {
            return nodeId.substring(0, nodeId.indexOf("_"));
        }
        return "unknown";
    }
    
    /**
     * Unlock a node for a player
     * @return true if successful, false if failed
     */
    public boolean unlockNode(Player player, String nodeId) {
        if (!canUnlockNode(player, nodeId)) {
            return false;
        }
        
        UUID playerUUID = player.getUniqueId();
        SkillTreeNode node = nodes.get(nodeId);
        
        // First remove any existing effects (in case we're reapplying)
        node.removeEffects(player);
        
        // Add to unlocked nodes
        Set<String> playerNodes = unlockedNodes.getOrDefault(playerUUID, new HashSet<>());
        playerNodes.add(nodeId);
        unlockedNodes.put(playerUUID, playerNodes);
        
        // Check if this is a free base node (first in category)
        String category = getCategoryFromNodeId(nodeId);
        boolean isBaseNode = node.getPrerequisites().isEmpty();
        boolean hasOtherNodesInCategory = false;
        
        // Count nodes in this category (excluding the one we just added)
        for (String unlockedNodeId : playerNodes) {
            if (unlockedNodeId.equals(nodeId)) continue; // Skip the node we just added
            if (unlockedNodeId.startsWith(category + "_")) {
                hasOtherNodesInCategory = true;
                break;
            }
        }
        
        // Deduct points, but not for free base nodes
        if (!(isBaseNode && !hasOtherNodesInCategory)) {
            // Deduct points for non-free nodes
            int spent = spentPoints.getOrDefault(playerUUID, 0);
            spent += node.getPointCost();
            spentPoints.put(playerUUID, spent);
        }
        
        // Apply effects
        node.applyEffects(player);
        
        // Save player data
        savePlayerData(player);
        
        // Notify player
        player.sendMessage("§a✨ You've unlocked " + node.getName() + "!");
        
        return true;
    }
    
    /**
     * Get the total skill points a player has earned
     */
    public int getTotalSkillPoints(Player player) {
        return playerLevel.getTotalSkillPoints(player);
    }
    
    /**
     * Get the number of skill points a player has spent
     */
    public int getSpentSkillPoints(Player player) {
        UUID playerUUID = player.getUniqueId();
        return spentPoints.getOrDefault(playerUUID, 0);
    }
    
    /**
     * Get the number of skill points a player has available to spend
     */
    public int getAvailableSkillPoints(Player player) {
        return getTotalSkillPoints(player) - getSpentSkillPoints(player);
    }
    
    /**
     * Get the player's current level
     */
    public int getPlayerLevel(Player player) {
        return playerLevel.getPlayerLevel(player);
    }
    
    /**
     * Get the player's progress to the next level as a percentage
     */
    public double getLevelProgress(Player player) {
        return playerLevel.getLevelProgress(player);
    }
    
    /**
     * Get all available skill tree nodes
     */
    public Map<String, SkillTreeNode> getAllNodes() {
        return nodes;
    }
    
    /**
     * Get all nodes unlocked by a player
     */
    public Set<String> getPlayerUnlockedNodes(Player player) {
        UUID playerUUID = player.getUniqueId();
        return unlockedNodes.getOrDefault(playerUUID, new HashSet<>());
    }
    
    /**
     * Get a list of nodes that the player can unlock now
     */
    public List<String> getAvailableNodes(Player player) {
        List<String> available = new ArrayList<>();
        for (String nodeId : nodes.keySet()) {
            if (canUnlockNode(player, nodeId)) {
                available.add(nodeId);
            }
        }
        return available;
    }
    
    /**
     * Handler for when a player joins the server
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadPlayerData(player);
        
        // Clear and reapply effects for consistency
        applyAllUnlockedEffects(player);
    }
    
    /**
     * Handler for when a player leaves the server
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        savePlayerData(player);
        UUID playerUUID = player.getUniqueId();
        unlockedNodes.remove(playerUUID);
        spentPoints.remove(playerUUID);
    }
    
    /**
     * Handler for when a player dies
     * This ensures attributes and effects are properly removed before respawn
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerUUID = player.getUniqueId();
        Set<String> playerNodes = unlockedNodes.getOrDefault(playerUUID, new HashSet<>());
        
        // Remove all effects to prevent them from persisting through death
        for (String nodeId : playerNodes) {
            SkillTreeNode node = nodes.get(nodeId);
            if (node != null) {
                node.removeEffects(player);
            }
        }
        
        plugin.getLogger().info("Removed skill tree effects for " + player.getName() + " on death");
    }
    
    /**
     * Handler for when a player respawns after death
     * This ensures attributes and effects are properly reapplied
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Schedule task to run 1 tick after respawn to ensure all vanilla attributes are reset
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = event.getPlayer();
            
            // Clear and reapply all effects
            UUID playerUUID = player.getUniqueId();
            Set<String> playerNodes = unlockedNodes.getOrDefault(playerUUID, new HashSet<>());
            
            for (String nodeId : playerNodes) {
                SkillTreeNode node = nodes.get(nodeId);
                if (node != null) {
                    // Make sure to first remove any lingering effects
                    node.removeEffects(player);
                }
            }
            
            // Now apply all effects freshly
            for (String nodeId : playerNodes) {
                SkillTreeNode node = nodes.get(nodeId);
                if (node != null) {
                    node.applyEffects(player);
                }
            }
            
            // Debug message to help track effect application
            plugin.getLogger().info("Reapplied skill tree effects for " + player.getName() + " after respawn");
        }, 1L); // 1 tick delay
    }
    
    /**
     * Reload skill tree configuration from config
     */
    public void reloadSkillTreeConfig() {
        // First, remove all effects from online players to prevent duplicates
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerUUID = player.getUniqueId();
            Set<String> playerNodes = unlockedNodes.getOrDefault(playerUUID, new HashSet<>());
            
            for (String nodeId : playerNodes) {
                SkillTreeNode node = nodes.get(nodeId);
                if (node != null) {
                    node.removeEffects(player);
                }
            }
        }
        
        // Clear existing nodes
        nodes.clear();
        
        // Reload nodes from config
        loadSkillTreeConfig();
        
        // Reapply effects to online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyAllUnlockedEffects(player);
        }
        
        plugin.getLogger().info("Skill tree configuration reloaded.");
    }
    
    /**
     * Get the PlayerLevel helper class
     */
    public PlayerLevel getPlayerLevel() {
        return playerLevel;
    }

    /**
     * Get the set of unlocked nodes for a player
     */
    public Set<String> getUnlockedNodes(UUID playerUUID) {
        return unlockedNodes.getOrDefault(playerUUID, Collections.emptySet());
    }

    /**
     * Get a list of node IDs that a player can unlock
     * @param player The player to check
     * @return A list of node IDs that the player can unlock
     */
    public List<String> getUnlockableNodes(Player player) {
        List<String> unlockableNodes = new ArrayList<>();
        
        for (String nodeId : nodes.keySet()) {
            if (canUnlockNode(player, nodeId)) {
                unlockableNodes.add(nodeId);
            }
        }
        
        return unlockableNodes;
    }
} 