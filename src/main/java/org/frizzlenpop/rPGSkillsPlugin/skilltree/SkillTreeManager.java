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
        
        // Load skill tree configuration
        loadSkillTreeConfig();
        
        // Apply effects to online players (in case of reload)
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player);
            applyAllUnlockedEffects(player);
        }
    }
    
    /**
     * Load skill tree configuration from config
     */
    private void loadSkillTreeConfig() {
        // Ensure default config values
        addDefaultSkillTreeConfig();
        
        // Load nodes from config
        ConfigurationSection nodesSection = plugin.getConfig().getConfigurationSection(CONFIG_NODES_PATH);
        if (nodesSection != null) {
            for (String nodeId : nodesSection.getKeys(false)) {
                ConfigurationSection nodeSection = nodesSection.getConfigurationSection(nodeId);
                if (nodeSection != null) {
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
            }
        }
    }
    
    /**
     * Add default skill tree config values if they don't exist
     */
    private void addDefaultSkillTreeConfig() {
        FileConfiguration config = plugin.getConfig();
        
        // Check for specific node presence rather than just the section
        boolean missingNodes = false;
        if (!config.isConfigurationSection(CONFIG_NODES_PATH + ".warrior_vitality") ||
            !config.isConfigurationSection(CONFIG_NODES_PATH + ".warrior_toughness") ||
            !config.isConfigurationSection(CONFIG_NODES_PATH + ".warrior_agility") ||
            !config.isConfigurationSection(CONFIG_NODES_PATH + ".warrior_power") ||
            !config.isConfigurationSection(CONFIG_NODES_PATH + ".mining_efficiency") ||
            !config.isConfigurationSection(CONFIG_NODES_PATH + ".mining_fortune") ||
            !config.isConfigurationSection(CONFIG_NODES_PATH + ".mining_xp_boost") ||
            !config.isConfigurationSection(CONFIG_NODES_PATH + ".mining_treasure_hunter") ||
            !config.isConfigurationSection(CONFIG_NODES_PATH + ".logging_efficiency") ||
            !config.isConfigurationSection(CONFIG_NODES_PATH + ".fishing_luck")) {
            
            missingNodes = true;
            plugin.getLogger().info("Found missing skill tree nodes. Adding defaults...");
        }
        
        // Add default nodes if any are missing or the section doesn't exist
        if (missingNodes || !config.isConfigurationSection(CONFIG_NODES_PATH)) {
            // ==== WARRIOR TREE ====
            // Warrior's Strength - Base node
            config.set(CONFIG_NODES_PATH + ".warrior_strength.name", "Warrior's Strength");
            config.set(CONFIG_NODES_PATH + ".warrior_strength.description", "Increases your maximum health");
            config.set(CONFIG_NODES_PATH + ".warrior_strength.point_cost", 1);
            config.set(CONFIG_NODES_PATH + ".warrior_strength.icon", "IRON_SWORD");
            config.set(CONFIG_NODES_PATH + ".warrior_strength.type", "STAT_BOOST");
            config.set(CONFIG_NODES_PATH + ".warrior_strength.effects.health.type", "ATTRIBUTE_BOOST");
            config.set(CONFIG_NODES_PATH + ".warrior_strength.effects.health.target", "GENERIC_MAX_HEALTH");
            config.set(CONFIG_NODES_PATH + ".warrior_strength.effects.health.value", 2.0);
            
            // Warrior's Vitality
            config.set(CONFIG_NODES_PATH + ".warrior_vitality.name", "Warrior's Vitality");
            config.set(CONFIG_NODES_PATH + ".warrior_vitality.description", "Increases your health regeneration");
            config.set(CONFIG_NODES_PATH + ".warrior_vitality.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".warrior_vitality.prerequisites", Arrays.asList("warrior_strength"));
            config.set(CONFIG_NODES_PATH + ".warrior_vitality.icon", "GOLDEN_APPLE");
            config.set(CONFIG_NODES_PATH + ".warrior_vitality.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".warrior_vitality.effects.regen.type", "ATTRIBUTE_BOOST");
            config.set(CONFIG_NODES_PATH + ".warrior_vitality.effects.regen.target", "GENERIC_MAX_HEALTH");
            config.set(CONFIG_NODES_PATH + ".warrior_vitality.effects.regen.value", 2.0);
            
            // Warrior's Toughness
            config.set(CONFIG_NODES_PATH + ".warrior_toughness.name", "Warrior's Toughness");
            config.set(CONFIG_NODES_PATH + ".warrior_toughness.description", "Increases your armor and damage resistance");
            config.set(CONFIG_NODES_PATH + ".warrior_toughness.point_cost", 3);
            config.set(CONFIG_NODES_PATH + ".warrior_toughness.prerequisites", Arrays.asList("warrior_vitality"));
            config.set(CONFIG_NODES_PATH + ".warrior_toughness.icon", "DIAMOND_CHESTPLATE");
            config.set(CONFIG_NODES_PATH + ".warrior_toughness.type", "STAT_BOOST");
            config.set(CONFIG_NODES_PATH + ".warrior_toughness.effects.armor.type", "ATTRIBUTE_BOOST");
            config.set(CONFIG_NODES_PATH + ".warrior_toughness.effects.armor.target", "GENERIC_ARMOR");
            config.set(CONFIG_NODES_PATH + ".warrior_toughness.effects.armor.value", 2.0);
            config.set(CONFIG_NODES_PATH + ".warrior_toughness.effects.toughness.type", "ATTRIBUTE_BOOST");
            config.set(CONFIG_NODES_PATH + ".warrior_toughness.effects.toughness.target", "GENERIC_ARMOR_TOUGHNESS");
            config.set(CONFIG_NODES_PATH + ".warrior_toughness.effects.toughness.value", 1.0);
            
            // Warrior's Agility
            config.set(CONFIG_NODES_PATH + ".warrior_agility.name", "Warrior's Agility");
            config.set(CONFIG_NODES_PATH + ".warrior_agility.description", "Increases your movement speed");
            config.set(CONFIG_NODES_PATH + ".warrior_agility.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".warrior_agility.prerequisites", Arrays.asList("warrior_strength"));
            config.set(CONFIG_NODES_PATH + ".warrior_agility.icon", "FEATHER");
            config.set(CONFIG_NODES_PATH + ".warrior_agility.type", "STAT_BOOST");
            config.set(CONFIG_NODES_PATH + ".warrior_agility.effects.speed.type", "ATTRIBUTE_BOOST");
            config.set(CONFIG_NODES_PATH + ".warrior_agility.effects.speed.target", "GENERIC_MOVEMENT_SPEED");
            config.set(CONFIG_NODES_PATH + ".warrior_agility.effects.speed.value", 0.02);
            
            // Warrior's Power
            config.set(CONFIG_NODES_PATH + ".warrior_power.name", "Warrior's Power");
            config.set(CONFIG_NODES_PATH + ".warrior_power.description", "Increases your attack damage");
            config.set(CONFIG_NODES_PATH + ".warrior_power.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".warrior_power.prerequisites", Arrays.asList("warrior_strength"));
            config.set(CONFIG_NODES_PATH + ".warrior_power.icon", "DIAMOND_SWORD");
            config.set(CONFIG_NODES_PATH + ".warrior_power.type", "STAT_BOOST");
            config.set(CONFIG_NODES_PATH + ".warrior_power.effects.damage.type", "ATTRIBUTE_BOOST");
            config.set(CONFIG_NODES_PATH + ".warrior_power.effects.damage.target", "GENERIC_ATTACK_DAMAGE");
            config.set(CONFIG_NODES_PATH + ".warrior_power.effects.damage.value", 1.0);
            
            // ==== MINING TREE ====
            // Mining Efficiency
            config.set(CONFIG_NODES_PATH + ".mining_efficiency.name", "Mining Efficiency");
            config.set(CONFIG_NODES_PATH + ".mining_efficiency.description", "Mine blocks faster");
            config.set(CONFIG_NODES_PATH + ".mining_efficiency.point_cost", 1);
            config.set(CONFIG_NODES_PATH + ".mining_efficiency.icon", "IRON_PICKAXE");
            config.set(CONFIG_NODES_PATH + ".mining_efficiency.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".mining_efficiency.effects.mining_speed.type", "CUSTOM_EFFECT");
            config.set(CONFIG_NODES_PATH + ".mining_efficiency.effects.mining_speed.target", "Increases mining speed by 10%");
            config.set(CONFIG_NODES_PATH + ".mining_efficiency.effects.mining_speed.value", 0.10);
            
            // Mining Fortune
            config.set(CONFIG_NODES_PATH + ".mining_fortune.name", "Mining Fortune");
            config.set(CONFIG_NODES_PATH + ".mining_fortune.description", "Chance to get extra drops from ores");
            config.set(CONFIG_NODES_PATH + ".mining_fortune.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".mining_fortune.prerequisites", Arrays.asList("mining_efficiency"));
            config.set(CONFIG_NODES_PATH + ".mining_fortune.icon", "GOLD_INGOT");
            config.set(CONFIG_NODES_PATH + ".mining_fortune.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".mining_fortune.effects.fortune.type", "RESOURCE_BOOST");
            config.set(CONFIG_NODES_PATH + ".mining_fortune.effects.fortune.target", "ores");
            config.set(CONFIG_NODES_PATH + ".mining_fortune.effects.fortune.value", 0.15);
            
            // Mining XP Boost
            config.set(CONFIG_NODES_PATH + ".mining_xp_boost.name", "Mining XP Boost");
            config.set(CONFIG_NODES_PATH + ".mining_xp_boost.description", "Gain more mining XP");
            config.set(CONFIG_NODES_PATH + ".mining_xp_boost.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".mining_xp_boost.prerequisites", Arrays.asList("mining_efficiency"));
            config.set(CONFIG_NODES_PATH + ".mining_xp_boost.icon", "EXPERIENCE_BOTTLE");
            config.set(CONFIG_NODES_PATH + ".mining_xp_boost.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".mining_xp_boost.effects.xp.type", "SKILL_XP_BOOST");
            config.set(CONFIG_NODES_PATH + ".mining_xp_boost.effects.xp.target", "mining");
            config.set(CONFIG_NODES_PATH + ".mining_xp_boost.effects.xp.value", 0.20);
            
            // Mining Treasure Hunter
            config.set(CONFIG_NODES_PATH + ".mining_treasure_hunter.name", "Mining Treasure Hunter");
            config.set(CONFIG_NODES_PATH + ".mining_treasure_hunter.description", "Chance to find rare items while mining");
            config.set(CONFIG_NODES_PATH + ".mining_treasure_hunter.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".mining_treasure_hunter.prerequisites", Arrays.asList("mining_efficiency"));
            config.set(CONFIG_NODES_PATH + ".mining_treasure_hunter.icon", "CHEST");
            config.set(CONFIG_NODES_PATH + ".mining_treasure_hunter.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".mining_treasure_hunter.effects.treasure.type", "CUSTOM_EFFECT");
            config.set(CONFIG_NODES_PATH + ".mining_treasure_hunter.effects.treasure.target", "5% chance to find rare items when mining");
            config.set(CONFIG_NODES_PATH + ".mining_treasure_hunter.effects.treasure.value", 0.05);
            
            // Mining Mastery
            config.set(CONFIG_NODES_PATH + ".mining_mastery.name", "Mining Mastery");
            config.set(CONFIG_NODES_PATH + ".mining_mastery.description", "Increases mining XP gain and ore drops");
            config.set(CONFIG_NODES_PATH + ".mining_mastery.point_cost", 3);
            config.set(CONFIG_NODES_PATH + ".mining_mastery.prerequisites", Arrays.asList("mining_fortune", "mining_xp_boost"));
            config.set(CONFIG_NODES_PATH + ".mining_mastery.icon", "DIAMOND_PICKAXE");
            config.set(CONFIG_NODES_PATH + ".mining_mastery.type", "MASTERY");
            config.set(CONFIG_NODES_PATH + ".mining_mastery.effects.mining_xp.type", "SKILL_XP_BOOST");
            config.set(CONFIG_NODES_PATH + ".mining_mastery.effects.mining_xp.target", "mining");
            config.set(CONFIG_NODES_PATH + ".mining_mastery.effects.mining_xp.value", 0.15);
            config.set(CONFIG_NODES_PATH + ".mining_mastery.effects.resource.type", "RESOURCE_BOOST");
            config.set(CONFIG_NODES_PATH + ".mining_mastery.effects.resource.target", "ores");
            config.set(CONFIG_NODES_PATH + ".mining_mastery.effects.resource.value", 0.10);
            
            // ==== LOGGING TREE ====
            // Logging Efficiency
            config.set(CONFIG_NODES_PATH + ".logging_efficiency.name", "Logging Efficiency");
            config.set(CONFIG_NODES_PATH + ".logging_efficiency.description", "Chop wood faster");
            config.set(CONFIG_NODES_PATH + ".logging_efficiency.point_cost", 1);
            config.set(CONFIG_NODES_PATH + ".logging_efficiency.icon", "IRON_AXE");
            config.set(CONFIG_NODES_PATH + ".logging_efficiency.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".logging_efficiency.effects.logging_speed.type", "CUSTOM_EFFECT");
            config.set(CONFIG_NODES_PATH + ".logging_efficiency.effects.logging_speed.target", "Increases wood chopping speed by 10%");
            config.set(CONFIG_NODES_PATH + ".logging_efficiency.effects.logging_speed.value", 0.10);
            
            // Logging Harvester
            config.set(CONFIG_NODES_PATH + ".logging_harvester.name", "Logging Harvester");
            config.set(CONFIG_NODES_PATH + ".logging_harvester.description", "Chance to get extra logs");
            config.set(CONFIG_NODES_PATH + ".logging_harvester.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".logging_harvester.prerequisites", Arrays.asList("logging_efficiency"));
            config.set(CONFIG_NODES_PATH + ".logging_harvester.icon", "OAK_LOG");
            config.set(CONFIG_NODES_PATH + ".logging_harvester.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".logging_harvester.effects.logs.type", "RESOURCE_BOOST");
            config.set(CONFIG_NODES_PATH + ".logging_harvester.effects.logs.target", "logs");
            config.set(CONFIG_NODES_PATH + ".logging_harvester.effects.logs.value", 0.15);
            
            // Logging Naturalist
            config.set(CONFIG_NODES_PATH + ".logging_naturalist.name", "Logging Naturalist");
            config.set(CONFIG_NODES_PATH + ".logging_naturalist.description", "Chance to get saplings and apples");
            config.set(CONFIG_NODES_PATH + ".logging_naturalist.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".logging_naturalist.prerequisites", Arrays.asList("logging_efficiency"));
            config.set(CONFIG_NODES_PATH + ".logging_naturalist.icon", "OAK_SAPLING");
            config.set(CONFIG_NODES_PATH + ".logging_naturalist.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".logging_naturalist.effects.saplings.type", "RESOURCE_BOOST");
            config.set(CONFIG_NODES_PATH + ".logging_naturalist.effects.saplings.target", "saplings");
            config.set(CONFIG_NODES_PATH + ".logging_naturalist.effects.saplings.value", 0.20);
            
            // Logging XP Boost
            config.set(CONFIG_NODES_PATH + ".logging_xp_boost.name", "Logging XP Boost");
            config.set(CONFIG_NODES_PATH + ".logging_xp_boost.description", "Gain more logging XP");
            config.set(CONFIG_NODES_PATH + ".logging_xp_boost.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".logging_xp_boost.prerequisites", Arrays.asList("logging_efficiency"));
            config.set(CONFIG_NODES_PATH + ".logging_xp_boost.icon", "EXPERIENCE_BOTTLE");
            config.set(CONFIG_NODES_PATH + ".logging_xp_boost.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".logging_xp_boost.effects.xp.type", "SKILL_XP_BOOST");
            config.set(CONFIG_NODES_PATH + ".logging_xp_boost.effects.xp.target", "logging");
            config.set(CONFIG_NODES_PATH + ".logging_xp_boost.effects.xp.value", 0.20);
            
            // Logging Mastery
            config.set(CONFIG_NODES_PATH + ".logging_mastery.name", "Logging Mastery");
            config.set(CONFIG_NODES_PATH + ".logging_mastery.description", "Master of woodcutting");
            config.set(CONFIG_NODES_PATH + ".logging_mastery.point_cost", 3);
            config.set(CONFIG_NODES_PATH + ".logging_mastery.prerequisites", Arrays.asList("logging_harvester", "logging_xp_boost"));
            config.set(CONFIG_NODES_PATH + ".logging_mastery.icon", "DIAMOND_AXE");
            config.set(CONFIG_NODES_PATH + ".logging_mastery.type", "MASTERY");
            config.set(CONFIG_NODES_PATH + ".logging_mastery.effects.logging_xp.type", "SKILL_XP_BOOST");
            config.set(CONFIG_NODES_PATH + ".logging_mastery.effects.logging_xp.target", "logging");
            config.set(CONFIG_NODES_PATH + ".logging_mastery.effects.logging_xp.value", 0.15);
            config.set(CONFIG_NODES_PATH + ".logging_mastery.effects.timber.type", "ABILITY_UNLOCK");
            config.set(CONFIG_NODES_PATH + ".logging_mastery.effects.timber.target", "Timber Ability");
            config.set(CONFIG_NODES_PATH + ".logging_mastery.effects.timber.value", 1.0);
            
            // ==== FARMING TREE ====
            // Farming Green Thumb
            config.set(CONFIG_NODES_PATH + ".farming_green_thumb.name", "Farming Green Thumb");
            config.set(CONFIG_NODES_PATH + ".farming_green_thumb.description", "Crops grow faster near you");
            config.set(CONFIG_NODES_PATH + ".farming_green_thumb.point_cost", 1);
            config.set(CONFIG_NODES_PATH + ".farming_green_thumb.icon", "WHEAT_SEEDS");
            config.set(CONFIG_NODES_PATH + ".farming_green_thumb.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".farming_green_thumb.effects.growth.type", "CUSTOM_EFFECT");
            config.set(CONFIG_NODES_PATH + ".farming_green_thumb.effects.growth.target", "Crops within 5 blocks grow 10% faster");
            config.set(CONFIG_NODES_PATH + ".farming_green_thumb.effects.growth.value", 0.10);
            
            // Farming Harvester
            config.set(CONFIG_NODES_PATH + ".farming_harvester.name", "Farming Harvester");
            config.set(CONFIG_NODES_PATH + ".farming_harvester.description", "Chance to get extra crops");
            config.set(CONFIG_NODES_PATH + ".farming_harvester.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".farming_harvester.prerequisites", Arrays.asList("farming_green_thumb"));
            config.set(CONFIG_NODES_PATH + ".farming_harvester.icon", "WHEAT");
            config.set(CONFIG_NODES_PATH + ".farming_harvester.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".farming_harvester.effects.crops.type", "RESOURCE_BOOST");
            config.set(CONFIG_NODES_PATH + ".farming_harvester.effects.crops.target", "crops");
            config.set(CONFIG_NODES_PATH + ".farming_harvester.effects.crops.value", 0.20);
            
            // Farming Animal Whisperer
            config.set(CONFIG_NODES_PATH + ".farming_animal_whisperer.name", "Animal Whisperer");
            config.set(CONFIG_NODES_PATH + ".farming_animal_whisperer.description", "Better breeding and animal products");
            config.set(CONFIG_NODES_PATH + ".farming_animal_whisperer.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".farming_animal_whisperer.prerequisites", Arrays.asList("farming_green_thumb"));
            config.set(CONFIG_NODES_PATH + ".farming_animal_whisperer.icon", "EGG");
            config.set(CONFIG_NODES_PATH + ".farming_animal_whisperer.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".farming_animal_whisperer.effects.breeding.type", "CUSTOM_EFFECT");
            config.set(CONFIG_NODES_PATH + ".farming_animal_whisperer.effects.breeding.target", "20% chance for twins when breeding animals");
            config.set(CONFIG_NODES_PATH + ".farming_animal_whisperer.effects.breeding.value", 0.20);
            config.set(CONFIG_NODES_PATH + ".farming_animal_whisperer.effects.products.type", "RESOURCE_BOOST");
            config.set(CONFIG_NODES_PATH + ".farming_animal_whisperer.effects.products.target", "animal products");
            config.set(CONFIG_NODES_PATH + ".farming_animal_whisperer.effects.products.value", 0.15);
            
            // Farming XP Boost
            config.set(CONFIG_NODES_PATH + ".farming_xp_boost.name", "Farming XP Boost");
            config.set(CONFIG_NODES_PATH + ".farming_xp_boost.description", "Gain more farming XP");
            config.set(CONFIG_NODES_PATH + ".farming_xp_boost.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".farming_xp_boost.prerequisites", Arrays.asList("farming_green_thumb"));
            config.set(CONFIG_NODES_PATH + ".farming_xp_boost.icon", "EXPERIENCE_BOTTLE");
            config.set(CONFIG_NODES_PATH + ".farming_xp_boost.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".farming_xp_boost.effects.xp.type", "SKILL_XP_BOOST");
            config.set(CONFIG_NODES_PATH + ".farming_xp_boost.effects.xp.target", "farming");
            config.set(CONFIG_NODES_PATH + ".farming_xp_boost.effects.xp.value", 0.20);
            
            // Farming Mastery
            config.set(CONFIG_NODES_PATH + ".farming_mastery.name", "Farming Mastery");
            config.set(CONFIG_NODES_PATH + ".farming_mastery.description", "Master of agriculture");
            config.set(CONFIG_NODES_PATH + ".farming_mastery.point_cost", 3);
            config.set(CONFIG_NODES_PATH + ".farming_mastery.prerequisites", Arrays.asList("farming_harvester", "farming_xp_boost"));
            config.set(CONFIG_NODES_PATH + ".farming_mastery.icon", "DIAMOND_HOE");
            config.set(CONFIG_NODES_PATH + ".farming_mastery.type", "MASTERY");
            config.set(CONFIG_NODES_PATH + ".farming_mastery.effects.farming_xp.type", "SKILL_XP_BOOST");
            config.set(CONFIG_NODES_PATH + ".farming_mastery.effects.farming_xp.target", "farming");
            config.set(CONFIG_NODES_PATH + ".farming_mastery.effects.farming_xp.value", 0.15);
            config.set(CONFIG_NODES_PATH + ".farming_mastery.effects.harvest.type", "ABILITY_UNLOCK");
            config.set(CONFIG_NODES_PATH + ".farming_mastery.effects.harvest.target", "Super Harvest Ability");
            config.set(CONFIG_NODES_PATH + ".farming_mastery.effects.harvest.value", 1.0);
            
            // ==== FIGHTING TREE ====
            // Fighting Strength
            config.set(CONFIG_NODES_PATH + ".fighting_strength.name", "Fighting Strength");
            config.set(CONFIG_NODES_PATH + ".fighting_strength.description", "Increase melee damage");
            config.set(CONFIG_NODES_PATH + ".fighting_strength.point_cost", 1);
            config.set(CONFIG_NODES_PATH + ".fighting_strength.icon", "IRON_SWORD");
            config.set(CONFIG_NODES_PATH + ".fighting_strength.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".fighting_strength.effects.damage.type", "DAMAGE_BOOST");
            config.set(CONFIG_NODES_PATH + ".fighting_strength.effects.damage.target", "melee");
            config.set(CONFIG_NODES_PATH + ".fighting_strength.effects.damage.value", 0.10);
            
            // Fighting Precision
            config.set(CONFIG_NODES_PATH + ".fighting_precision.name", "Fighting Precision");
            config.set(CONFIG_NODES_PATH + ".fighting_precision.description", "Chance for critical hits");
            config.set(CONFIG_NODES_PATH + ".fighting_precision.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".fighting_precision.prerequisites", Arrays.asList("fighting_strength"));
            config.set(CONFIG_NODES_PATH + ".fighting_precision.icon", "TARGET");
            config.set(CONFIG_NODES_PATH + ".fighting_precision.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".fighting_precision.effects.critical.type", "CUSTOM_EFFECT");
            config.set(CONFIG_NODES_PATH + ".fighting_precision.effects.critical.target", "15% chance for critical hits dealing 50% more damage");
            config.set(CONFIG_NODES_PATH + ".fighting_precision.effects.critical.value", 0.15);
            
            // Fighting Agility
            config.set(CONFIG_NODES_PATH + ".fighting_agility.name", "Fighting Agility");
            config.set(CONFIG_NODES_PATH + ".fighting_agility.description", "Chance to dodge attacks");
            config.set(CONFIG_NODES_PATH + ".fighting_agility.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".fighting_agility.prerequisites", Arrays.asList("fighting_strength"));
            config.set(CONFIG_NODES_PATH + ".fighting_agility.icon", "FEATHER");
            config.set(CONFIG_NODES_PATH + ".fighting_agility.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".fighting_agility.effects.dodge.type", "CUSTOM_EFFECT");
            config.set(CONFIG_NODES_PATH + ".fighting_agility.effects.dodge.target", "10% chance to dodge incoming attacks");
            config.set(CONFIG_NODES_PATH + ".fighting_agility.effects.dodge.value", 0.10);
            
            // Fighting XP Boost
            config.set(CONFIG_NODES_PATH + ".fighting_xp_boost.name", "Fighting XP Boost");
            config.set(CONFIG_NODES_PATH + ".fighting_xp_boost.description", "Gain more fighting XP");
            config.set(CONFIG_NODES_PATH + ".fighting_xp_boost.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".fighting_xp_boost.prerequisites", Arrays.asList("fighting_strength"));
            config.set(CONFIG_NODES_PATH + ".fighting_xp_boost.icon", "EXPERIENCE_BOTTLE");
            config.set(CONFIG_NODES_PATH + ".fighting_xp_boost.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".fighting_xp_boost.effects.xp.type", "SKILL_XP_BOOST");
            config.set(CONFIG_NODES_PATH + ".fighting_xp_boost.effects.xp.target", "fighting");
            config.set(CONFIG_NODES_PATH + ".fighting_xp_boost.effects.xp.value", 0.20);
            
            // Fighting Mastery
            config.set(CONFIG_NODES_PATH + ".fighting_mastery.name", "Fighting Mastery");
            config.set(CONFIG_NODES_PATH + ".fighting_mastery.description", "Master of combat");
            config.set(CONFIG_NODES_PATH + ".fighting_mastery.point_cost", 3);
            config.set(CONFIG_NODES_PATH + ".fighting_mastery.prerequisites", Arrays.asList("fighting_precision", "fighting_xp_boost"));
            config.set(CONFIG_NODES_PATH + ".fighting_mastery.icon", "DIAMOND_SWORD");
            config.set(CONFIG_NODES_PATH + ".fighting_mastery.type", "MASTERY");
            config.set(CONFIG_NODES_PATH + ".fighting_mastery.effects.fighting_xp.type", "SKILL_XP_BOOST");
            config.set(CONFIG_NODES_PATH + ".fighting_mastery.effects.fighting_xp.target", "fighting");
            config.set(CONFIG_NODES_PATH + ".fighting_mastery.effects.fighting_xp.value", 0.15);
            config.set(CONFIG_NODES_PATH + ".fighting_mastery.effects.rage.type", "ABILITY_UNLOCK");
            config.set(CONFIG_NODES_PATH + ".fighting_mastery.effects.rage.target", "Berserker Rage Ability");
            config.set(CONFIG_NODES_PATH + ".fighting_mastery.effects.rage.value", 1.0);
            
            // ==== FISHING TREE ====
            // Fishing Luck
            config.set(CONFIG_NODES_PATH + ".fishing_luck.name", "Fishing Luck");
            config.set(CONFIG_NODES_PATH + ".fishing_luck.description", "Improved fishing luck");
            config.set(CONFIG_NODES_PATH + ".fishing_luck.point_cost", 1);
            config.set(CONFIG_NODES_PATH + ".fishing_luck.icon", "FISHING_ROD");
            config.set(CONFIG_NODES_PATH + ".fishing_luck.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".fishing_luck.effects.luck.type", "ATTRIBUTE_BOOST");
            config.set(CONFIG_NODES_PATH + ".fishing_luck.effects.luck.target", "GENERIC_LUCK");
            config.set(CONFIG_NODES_PATH + ".fishing_luck.effects.luck.value", 1.0);
            
            // Fishing Patience
            config.set(CONFIG_NODES_PATH + ".fishing_patience.name", "Fishing Patience");
            config.set(CONFIG_NODES_PATH + ".fishing_patience.description", "Reduced waiting time for fish to bite");
            config.set(CONFIG_NODES_PATH + ".fishing_patience.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".fishing_patience.prerequisites", Arrays.asList("fishing_luck"));
            config.set(CONFIG_NODES_PATH + ".fishing_patience.icon", "CLOCK");
            config.set(CONFIG_NODES_PATH + ".fishing_patience.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".fishing_patience.effects.bite_time.type", "CUSTOM_EFFECT");
            config.set(CONFIG_NODES_PATH + ".fishing_patience.effects.bite_time.target", "Fish bite 20% faster");
            config.set(CONFIG_NODES_PATH + ".fishing_patience.effects.bite_time.value", 0.20);
            
            // Fishing Treasure Hunter
            config.set(CONFIG_NODES_PATH + ".fishing_treasure_hunter.name", "Fishing Treasure Hunter");
            config.set(CONFIG_NODES_PATH + ".fishing_treasure_hunter.description", "Increased chance for treasure items");
            config.set(CONFIG_NODES_PATH + ".fishing_treasure_hunter.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".fishing_treasure_hunter.prerequisites", Arrays.asList("fishing_luck"));
            config.set(CONFIG_NODES_PATH + ".fishing_treasure_hunter.icon", "CHEST");
            config.set(CONFIG_NODES_PATH + ".fishing_treasure_hunter.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".fishing_treasure_hunter.effects.treasure.type", "CUSTOM_EFFECT");
            config.set(CONFIG_NODES_PATH + ".fishing_treasure_hunter.effects.treasure.target", "25% more treasure items from fishing");
            config.set(CONFIG_NODES_PATH + ".fishing_treasure_hunter.effects.treasure.value", 0.25);
            
            // Fishing XP Boost
            config.set(CONFIG_NODES_PATH + ".fishing_xp_boost.name", "Fishing XP Boost");
            config.set(CONFIG_NODES_PATH + ".fishing_xp_boost.description", "Gain more fishing XP");
            config.set(CONFIG_NODES_PATH + ".fishing_xp_boost.point_cost", 2);
            config.set(CONFIG_NODES_PATH + ".fishing_xp_boost.prerequisites", Arrays.asList("fishing_luck"));
            config.set(CONFIG_NODES_PATH + ".fishing_xp_boost.icon", "EXPERIENCE_BOTTLE");
            config.set(CONFIG_NODES_PATH + ".fishing_xp_boost.type", "PASSIVE");
            config.set(CONFIG_NODES_PATH + ".fishing_xp_boost.effects.xp.type", "SKILL_XP_BOOST");
            config.set(CONFIG_NODES_PATH + ".fishing_xp_boost.effects.xp.target", "fishing");
            config.set(CONFIG_NODES_PATH + ".fishing_xp_boost.effects.xp.value", 0.20);
            
            // Fishing Mastery
            config.set(CONFIG_NODES_PATH + ".fishing_mastery.name", "Fishing Mastery");
            config.set(CONFIG_NODES_PATH + ".fishing_mastery.description", "Master of fishing");
            config.set(CONFIG_NODES_PATH + ".fishing_mastery.point_cost", 3);
            config.set(CONFIG_NODES_PATH + ".fishing_mastery.prerequisites", Arrays.asList("fishing_treasure_hunter", "fishing_xp_boost"));
            config.set(CONFIG_NODES_PATH + ".fishing_mastery.icon", "TROPICAL_FISH");
            config.set(CONFIG_NODES_PATH + ".fishing_mastery.type", "MASTERY");
            config.set(CONFIG_NODES_PATH + ".fishing_mastery.effects.fishing_xp.type", "SKILL_XP_BOOST");
            config.set(CONFIG_NODES_PATH + ".fishing_mastery.effects.fishing_xp.target", "fishing");
            config.set(CONFIG_NODES_PATH + ".fishing_mastery.effects.fishing_xp.value", 0.15);
            config.set(CONFIG_NODES_PATH + ".fishing_mastery.effects.instant.type", "ABILITY_UNLOCK");
            config.set(CONFIG_NODES_PATH + ".fishing_mastery.effects.instant.target", "Instant Catch Ability");
            config.set(CONFIG_NODES_PATH + ".fishing_mastery.effects.instant.value", 1.0);
            
            plugin.saveConfig();
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
        
        // Check available points
        int availablePoints = getAvailableSkillPoints(player);
        if (availablePoints < node.getPointCost()) {
            return false;
        }
        
        // Check prerequisites
        Set<String> playerNodes = unlockedNodes.getOrDefault(playerUUID, Collections.emptySet());
        for (String prerequisite : node.getPrerequisites()) {
            if (!playerNodes.contains(prerequisite)) {
                return false;
            }
        }
        
        return true;
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
        
        // Deduct points
        int spent = spentPoints.getOrDefault(playerUUID, 0);
        spent += node.getPointCost();
        spentPoints.put(playerUUID, spent);
        
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
} 