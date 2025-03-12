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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PassiveSkillManager implements Listener {
    // New system: tracking passives unlocked via config
    private final Map<UUID, Map<String, Set<String>>> playerPassives = new HashMap<>();

    // Old system: active passive effects loaded from player data
    private final Map<UUID, Set<String>> activePassives;

    // Sets for various passive effect implementations
    // Mining passives
    private final Set<UUID> miningBasicsPlayers = new HashSet<>();
    private final Set<UUID> stoneEfficiencyPlayers = new HashSet<>();
    private final Set<UUID> autoSmeltPlayers = new HashSet<>();
    private final Set<UUID> doubleOreDropPlayers = new HashSet<>();
    private final Set<UUID> fortuneBoostPlayers = new HashSet<>();
    private final Set<UUID> autoSmeltUpgradePlayers = new HashSet<>();
    private final Set<UUID> minersHastePlayers = new HashSet<>();
    private final Set<UUID> minersNightVisionPlayers = new HashSet<>();
    private final Set<UUID> tripleOreDropPlayers = new HashSet<>();
    private final Set<UUID> miningXpBoostPlayers = new HashSet<>();
    private final Set<UUID> advancedFortunePlayers = new HashSet<>();
    private final Set<UUID> treasureHunterPlayers = new HashSet<>();
    private final Set<UUID> minerEfficiencyPlayers = new HashSet<>();
    private final Set<UUID> unbreakableToolsPlayers = new HashSet<>();
    private final Set<UUID> masterSmelterPlayers = new HashSet<>();
    private final Set<UUID> oreVeinSensorPlayers = new HashSet<>();
    private final Set<UUID> quadrupleOreDropPlayers = new HashSet<>();
    private final Set<UUID> excavationPlayers = new HashSet<>();
    private final Set<UUID> netherMiningPlayers = new HashSet<>();
    private final Set<UUID> advancedHastePlayers = new HashSet<>();
    private final Set<UUID> masterFortunePlayers = new HashSet<>();
    private final Set<UUID> obsidianSpecialistPlayers = new HashSet<>();
    private final Set<UUID> deepslateExpertPlayers = new HashSet<>();
    private final Set<UUID> legendaryFortunePlayers = new HashSet<>();
    private final Set<UUID> ultimateSmelterPlayers = new HashSet<>();
    private final Set<UUID> excavationMasterPlayers = new HashSet<>();
    private final Set<UUID> masterMinerPlayers = new HashSet<>();
    
    // Ore specialization passives
    private final Set<UUID> coalSpecializationPlayers = new HashSet<>();
    private final Set<UUID> ironSpecializationPlayers = new HashSet<>();
    private final Set<UUID> goldSpecializationPlayers = new HashSet<>();
    private final Set<UUID> redstoneSpecializationPlayers = new HashSet<>();
    private final Set<UUID> lapisSpecializationPlayers = new HashSet<>();
    private final Set<UUID> copperSpecializationPlayers = new HashSet<>();
    private final Set<UUID> diamondSpecializationPlayers = new HashSet<>();
    private final Set<UUID> emeraldSpecializationPlayers = new HashSet<>();
    
    // Farming passives
    private final Set<UUID> autoReplantPlayers = new HashSet<>();
    private final Set<UUID> doubleCropYieldPlayers = new HashSet<>();
    private final Set<UUID> instantGrowthPlayers = new HashSet<>();

    // Logging passives
    private final Set<UUID> doubleWoodDropPlayers = new HashSet<>();
    private final Set<UUID> treeGrowthBoostPlayers = new HashSet<>();
    private final Set<UUID> tripleLogDropPlayers = new HashSet<>();
    
    // Fighting passives
    private final Set<UUID> healOnKillPlayers = new HashSet<>();
    private final Set<UUID> lifestealPlayers = new HashSet<>();
    private final Set<UUID> damageReductionPlayers = new HashSet<>();

    // Fishing passives
    private final Set<UUID> fishingBasicsPlayers = new HashSet<>();
    private final Set<UUID> baitSaverPlayers = new HashSet<>();
    private final Set<UUID> fishingXpBoostIPlayers = new HashSet<>();
    private final Set<UUID> fishingXpBoostIIPlayers = new HashSet<>();
    private final Set<UUID> fishingXpBoostIIIPlayers = new HashSet<>();
    private final Set<UUID> fishingXpBoostIVPlayers = new HashSet<>();
    private final Set<UUID> fishingXpBoostVPlayers = new HashSet<>();
    private final Set<UUID> fishingXpBoostVIPlayers = new HashSet<>();
    private final Set<UUID> fishFinderPlayers = new HashSet<>();
    private final Set<UUID> treasureHunterIPlayers = new HashSet<>();
    private final Set<UUID> treasureHunterIIPlayers = new HashSet<>();
    private final Set<UUID> treasureHunterIIIPlayers = new HashSet<>();
    private final Set<UUID> treasureHunterIVPlayers = new HashSet<>();
    private final Set<UUID> treasureHunterVPlayers = new HashSet<>();
    private final Set<UUID> salmonSpecialistPlayers = new HashSet<>();
    private final Set<UUID> rareFishMasterIPlayers = new HashSet<>();
    private final Set<UUID> rareFishMasterIIPlayers = new HashSet<>();
    private final Set<UUID> tropicalFishSpecialistPlayers = new HashSet<>();
    private final Set<UUID> quickHookIPlayers = new HashSet<>();
    private final Set<UUID> quickHookIIPlayers = new HashSet<>();
    private final Set<UUID> quickHookIIIPlayers = new HashSet<>();
    private final Set<UUID> quickHookIVPlayers = new HashSet<>();
    private final Set<UUID> junkReducerIPlayers = new HashSet<>();
    private final Set<UUID> junkReducerIIPlayers = new HashSet<>();
    private final Set<UUID> junkReducerIIIPlayers = new HashSet<>();
    private final Set<UUID> junkReducerIVPlayers = new HashSet<>();
    private final Set<UUID> waterBreathingPlayers = new HashSet<>();
    private final Set<UUID> doubleCatchIPlayers = new HashSet<>();
    private final Set<UUID> doubleCatchIIPlayers = new HashSet<>();
    private final Set<UUID> doubleCatchIIIPlayers = new HashSet<>();
    private final Set<UUID> enchantedBookFisherIPlayers = new HashSet<>();
    private final Set<UUID> enchantedBookFisherIIPlayers = new HashSet<>();
    private final Set<UUID> enchantedBookFisherIIIPlayers = new HashSet<>();
    private final Set<UUID> nightFisherPlayers = new HashSet<>();
    private final Set<UUID> rainFisherPlayers = new HashSet<>();
    private final Set<UUID> masterAnglerPlayers = new HashSet<>();
    private final Set<UUID> oceanExplorerPlayers = new HashSet<>();
    private final Set<UUID> tripleCatchPlayers = new HashSet<>();
    private final Set<UUID> legendaryFisherPlayers = new HashSet<>();
    private final Set<UUID> ancientTreasuresPlayers = new HashSet<>();
    private final Set<UUID> masterFisherPlayers = new HashSet<>();

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

        // Mining passives - add all based on level
        if (miningLevel >= 1) {
            passives.add("miningBasics");
            miningBasicsPlayers.add(playerId);
        }
        if (miningLevel >= 3) {
            passives.add("stoneEfficiency");
            stoneEfficiencyPlayers.add(playerId);
        }
        if (miningLevel >= 5) {
            passives.add("doubleOreDrop");
            doubleOreDropPlayers.add(playerId);
        }
        if (miningLevel >= 7) {
            passives.add("miningXpBoost");
            miningXpBoostPlayers.add(playerId);
        }
        if (miningLevel >= 10) {
            passives.add("autoSmelt");
            autoSmeltPlayers.add(playerId);
        }
        if (miningLevel >= 12) {
            passives.add("coalSpecialization");
            coalSpecializationPlayers.add(playerId);
        }
        if (miningLevel >= 14) {
            passives.add("ironSpecialization");
            ironSpecializationPlayers.add(playerId);
        }
        if (miningLevel >= 15) {
            passives.add("fortuneBoost");
            fortuneBoostPlayers.add(playerId);
        }
        if (miningLevel >= 17) {
            passives.add("minersHaste");
            minersHastePlayers.add(playerId);
        }
        if (miningLevel >= 20) {
            passives.add("autoSmeltUpgrade");
            autoSmeltUpgradePlayers.add(playerId);
        }
        if (miningLevel >= 22) {
            passives.add("goldSpecialization");
            goldSpecializationPlayers.add(playerId);
        }
        if (miningLevel >= 25) {
            passives.add("minersNightVision");
            minersNightVisionPlayers.add(playerId);
        }
        if (miningLevel >= 30) {
            passives.add("tripleOreDrop");
            tripleOreDropPlayers.add(playerId);
        }
        if (miningLevel >= 32) {
            passives.add("redstoneSpecialization");
            redstoneSpecializationPlayers.add(playerId);
        }
        if (miningLevel >= 34) {
            passives.add("lapisSpecialization");
            lapisSpecializationPlayers.add(playerId);
        }
        if (miningLevel >= 35) {
            passives.add("advancedFortune");
            advancedFortunePlayers.add(playerId);
        }
        if (miningLevel >= 37) {
            passives.add("treasureHunter");
            treasureHunterPlayers.add(playerId);
        }
        if (miningLevel >= 40) {
            passives.add("minerEfficiency");
            minerEfficiencyPlayers.add(playerId);
        }
        if (miningLevel >= 42) {
            passives.add("copperSpecialization");
            copperSpecializationPlayers.add(playerId);
        }
        if (miningLevel >= 44) {
            passives.add("diamondSpecialization");
            diamondSpecializationPlayers.add(playerId);
        }
        if (miningLevel >= 47) {
            passives.add("unbreakableTools");
            unbreakableToolsPlayers.add(playerId);
        }
        if (miningLevel >= 50) {
            passives.add("masterSmelter");
            masterSmelterPlayers.add(playerId);
        }
        if (miningLevel >= 52) {
            passives.add("emeraldSpecialization");
            emeraldSpecializationPlayers.add(playerId);
        }
        if (miningLevel >= 54) {
            passives.add("oreVeinSensor");
            oreVeinSensorPlayers.add(playerId);
        }
        if (miningLevel >= 55) {
            passives.add("quadrupleOreDrop");
            quadrupleOreDropPlayers.add(playerId);
        }
        if (miningLevel >= 57) {
            passives.add("excavation");
            excavationPlayers.add(playerId);
        }
        if (miningLevel >= 60) {
            passives.add("netherMining");
            netherMiningPlayers.add(playerId);
        }
        if (miningLevel >= 62) {
            passives.add("advancedHaste");
            advancedHastePlayers.add(playerId);
        }
        if (miningLevel >= 65) {
            passives.add("masterFortune");
            masterFortunePlayers.add(playerId);
        }
        if (miningLevel >= 74) {
            passives.add("obsidianSpecialist");
            obsidianSpecialistPlayers.add(playerId);
        }
        if (miningLevel >= 75) {
            passives.add("deepslateExpert");
            deepslateExpertPlayers.add(playerId);
        }
        if (miningLevel >= 94) {
            passives.add("legendaryFortune");
            legendaryFortunePlayers.add(playerId);
        }
        if (miningLevel >= 95) {
            passives.add("ultimateSmelter");
            ultimateSmelterPlayers.add(playerId);
        }
        if (miningLevel >= 97) {
            passives.add("excavationMaster");
            excavationMasterPlayers.add(playerId);
        }
        if (miningLevel >= 100) {
            passives.add("masterMiner");
            masterMinerPlayers.add(playerId);
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
                
            case "fishing":
                if (level >= 1) {
                    unlockPassive(player, skill, "Fishing Basics");
                }
                if (level >= 3) {
                    unlockPassive(player, skill, "Bait Saver");
                }
                if (level >= 5) {
                    unlockPassive(player, skill, "XP Boost I");
                }
                if (level >= 7) {
                    unlockPassive(player, skill, "Fish Finder");
                }
                if (level >= 10) {
                    unlockPassive(player, skill, "Treasure Hunter I");
                }
                if (level >= 12) {
                    unlockPassive(player, skill, "Salmon Specialist");
                }
                if (level >= 15) {
                    unlockPassive(player, skill, "Rare Fish Master I");
                }
                if (level >= 17) {
                    unlockPassive(player, skill, "Tropical Fish Specialist");
                }
                if (level >= 20) {
                    unlockPassive(player, skill, "Quick Hook I");
                }
                if (level >= 22) {
                    unlockPassive(player, skill, "XP Boost II");
                }
                if (level >= 25) {
                    unlockPassive(player, skill, "Junk Reducer I");
                }
                if (level >= 27) {
                    unlockPassive(player, skill, "Treasure Hunter II");
                }
                if (level >= 30) {
                    unlockPassive(player, skill, "Water Breathing");
                }
                if (level >= 32) {
                    unlockPassive(player, skill, "Double Catch I");
                }
                if (level >= 35) {
                    unlockPassive(player, skill, "Enchanted Book Fisher I");
                }
                if (level >= 37) {
                    unlockPassive(player, skill, "Quick Hook II");
                }
                if (level >= 40) {
                    unlockPassive(player, skill, "Rare Fish Master II");
                }
                if (level >= 42) {
                    unlockPassive(player, skill, "XP Boost III");
                }
                if (level >= 45) {
                    unlockPassive(player, skill, "Junk Reducer II");
                }
                if (level >= 47) {
                    unlockPassive(player, skill, "Treasure Hunter III");
                }
                if (level >= 50) {
                    unlockPassive(player, skill, "Night Fisher");
                }
                if (level >= 52) {
                    unlockPassive(player, skill, "Double Catch II");
                }
                if (level >= 55) {
                    unlockPassive(player, skill, "Enchanted Book Fisher II");
                }
                if (level >= 57) {
                    unlockPassive(player, skill, "Quick Hook III");
                }
                if (level >= 60) {
                    unlockPassive(player, skill, "XP Boost IV");
                }
                if (level >= 62) {
                    unlockPassive(player, skill, "Junk Reducer III");
                }
                if (level >= 65) {
                    unlockPassive(player, skill, "Treasure Hunter IV");
                }
                if (level >= 67) {
                    unlockPassive(player, skill, "Rain Fisher");
                }
                if (level >= 70) {
                    unlockPassive(player, skill, "Master Angler");
                }
                if (level >= 72) {
                    unlockPassive(player, skill, "XP Boost V");
                }
                if (level >= 75) {
                    unlockPassive(player, skill, "Double Catch III");
                }
                if (level >= 77) {
                    unlockPassive(player, skill, "Enchanted Book Fisher III");
                }
                if (level >= 80) {
                    unlockPassive(player, skill, "Ocean Explorer");
                }
                if (level >= 82) {
                    unlockPassive(player, skill, "Junk Reducer IV");
                }
                if (level >= 85) {
                    unlockPassive(player, skill, "Treasure Hunter V");
                }
                if (level >= 87) {
                    unlockPassive(player, skill, "Quick Hook IV");
                }
                if (level >= 90) {
                    unlockPassive(player, skill, "Triple Catch");
                }
                if (level >= 92) {
                    unlockPassive(player, skill, "XP Boost VI");
                }
                if (level >= 95) {
                    unlockPassive(player, skill, "Legendary Fisher");
                }
                if (level >= 97) {
                    unlockPassive(player, skill, "Ancient Treasures");
                }
                if (level >= 100) {
                    unlockPassive(player, skill, "Master Fisher");
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
                
            case "fishing":
                // XP Boost I - Level 5 (+10% XP)
                if (hasPassive(player, skill, "XP Boost I")) {
                    multiplier += 0.1;
                }
                // XP Boost II - Level 22 (+15% XP)
                else if (hasPassive(player, skill, "XP Boost II")) {
                    multiplier += 0.15;
                }
                // XP Boost III - Level 42 (+20% XP)
                else if (hasPassive(player, skill, "XP Boost III")) {
                    multiplier += 0.2;
                }
                // XP Boost IV - Level 60 (+25% XP)
                else if (hasPassive(player, skill, "XP Boost IV")) {
                    multiplier += 0.25;
                }
                // XP Boost V - Level 72 (+30% XP)
                else if (hasPassive(player, skill, "XP Boost V")) {
                    multiplier += 0.3;
                }
                // XP Boost VI - Level 92 (+40% XP)
                else if (hasPassive(player, skill, "XP Boost VI")) {
                    multiplier += 0.4;
                }
                
                // Master Angler - Level 70 (All fishing stats improved by 10%)
                if (hasPassive(player, skill, "Master Angler")) {
                    multiplier += 0.1;
                }
                
                // Legendary Fisher - Level 95 (All fishing yields increased by 25%)
                if (hasPassive(player, skill, "Legendary Fisher")) {
                    multiplier += 0.25;
                }
                
                // Master Fisher - Level 100 (Ultimate fishing mastery)
                if (hasPassive(player, skill, "Master Fisher")) {
                    multiplier += 0.5; // +50% XP as part of ultimate mastery
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
        // Get the internal camelCase passive names
        Set<String> internalPassives = getPlayerPassives(player.getUniqueId());
        
        // Create a filtered list to avoid duplicates
        List<String> passives = new ArrayList<>();
        
        // Track which passives we've already added to avoid duplicates
        Set<String> addedPassiveTypes = new HashSet<>();
        
        // First add the internal passives, converting to a more readable format
        for (String passive : internalPassives) {
            // Add the internal passive name
            passives.add(passive);
            
            // Track which passive types we've already added
            // Map the internal name to a type to avoid duplicates
            if (passive.equalsIgnoreCase("doubleOreDrop") || passive.equalsIgnoreCase("Double Ore Drop")) {
                addedPassiveTypes.add("DOUBLE_ORE_DROP");
            } else if (passive.equalsIgnoreCase("autoSmelt") || passive.equalsIgnoreCase("Auto Smelt")) {
                addedPassiveTypes.add("AUTO_SMELT");
            } else if (passive.equalsIgnoreCase("fortuneBoost") || passive.equalsIgnoreCase("Fortune Boost")) {
                addedPassiveTypes.add("FORTUNE_BOOST");
            } else if (passive.equalsIgnoreCase("autoSmeltUpgrade") || passive.equalsIgnoreCase("Auto Smelt Upgrade")) {
                addedPassiveTypes.add("AUTO_SMELT_UPGRADE");
            } else if (passive.equalsIgnoreCase("doubleWoodDrop") || passive.equalsIgnoreCase("Double Wood Drop")) {
                addedPassiveTypes.add("DOUBLE_WOOD_DROP");
            } else if (passive.equalsIgnoreCase("treeGrowthBoost") || passive.equalsIgnoreCase("Tree Growth Boost")) {
                addedPassiveTypes.add("TREE_GROWTH_BOOST");
            } else if (passive.equalsIgnoreCase("tripleLogDrop") || passive.equalsIgnoreCase("Triple Log Drop")) {
                addedPassiveTypes.add("TRIPLE_LOG_DROP");
            } else if (passive.equalsIgnoreCase("farmingXpBoost") || passive.equalsIgnoreCase("Farming XP Boost")) {
                addedPassiveTypes.add("FARMING_XP_BOOST");
            } else if (passive.equalsIgnoreCase("autoReplant") || passive.equalsIgnoreCase("Auto Replant")) {
                addedPassiveTypes.add("AUTO_REPLANT");
            } else if (passive.equalsIgnoreCase("doubleHarvest") || passive.equalsIgnoreCase("Double Harvest")) {
                addedPassiveTypes.add("DOUBLE_HARVEST");
            } else if (passive.equalsIgnoreCase("growthBoost") || passive.equalsIgnoreCase("Growth Boost")) {
                addedPassiveTypes.add("GROWTH_BOOST");
            } else if (passive.equalsIgnoreCase("lifesteal") || passive.equalsIgnoreCase("Lifesteal")) {
                addedPassiveTypes.add("LIFESTEAL");
            } else if (passive.equalsIgnoreCase("damageReduction") || passive.equalsIgnoreCase("Damage Reduction")) {
                addedPassiveTypes.add("DAMAGE_REDUCTION");
            } else if (passive.equalsIgnoreCase("healOnKill") || passive.equalsIgnoreCase("Heal on Kill")) {
                addedPassiveTypes.add("HEAL_ON_KILL");
            } else if (passive.equalsIgnoreCase("xpBoost") || passive.equalsIgnoreCase("XP Boost")) {
                addedPassiveTypes.add("XP_BOOST");
            } else if (passive.equalsIgnoreCase("treasureHunter") || passive.equalsIgnoreCase("Treasure Hunter")) {
                addedPassiveTypes.add("TREASURE_HUNTER");
            } else if (passive.equalsIgnoreCase("rareFishMaster") || passive.equalsIgnoreCase("Rare Fish Master")) {
                addedPassiveTypes.add("RARE_FISH_MASTER");
            } else if (passive.equalsIgnoreCase("quickHook") || passive.equalsIgnoreCase("Quick Hook")) {
                addedPassiveTypes.add("QUICK_HOOK");
            } else if (passive.equalsIgnoreCase("researchMaster") || passive.equalsIgnoreCase("Research Master")) {
                addedPassiveTypes.add("RESEARCH_MASTER");
            } else if (passive.equalsIgnoreCase("bookUpgrade") || passive.equalsIgnoreCase("Book Upgrade")) {
                addedPassiveTypes.add("BOOK_UPGRADE");
            } else if (passive.equalsIgnoreCase("customEnchants") || passive.equalsIgnoreCase("Custom Enchants")) {
                addedPassiveTypes.add("CUSTOM_ENCHANTS");
            } else if (passive.equalsIgnoreCase("rareEnchantBoost") || passive.equalsIgnoreCase("Rare Enchant Boost")) {
                addedPassiveTypes.add("RARE_ENCHANT_BOOST");
            }
        }
        
        // Get skill levels
        UUID playerId = player.getUniqueId();
        int miningLevel = xpManager.getPlayerLevel(player, "mining");
        int loggingLevel = xpManager.getPlayerLevel(player, "logging");
        int farmingLevel = xpManager.getPlayerLevel(player, "farming");
        int fightingLevel = xpManager.getPlayerLevel(player, "fighting");
        int fishingLevel = xpManager.getPlayerLevel(player, "fishing");
        int enchantingLevel = xpManager.getPlayerLevel(player, "enchanting");
        
        // Add Mining passives only if we haven't already added them
        if (miningLevel >= 5 && !addedPassiveTypes.contains("DOUBLE_ORE_DROP"))
            passives.add("Double Ore Drop");
        if (miningLevel >= 10 && !addedPassiveTypes.contains("AUTO_SMELT"))
            passives.add("Auto Smelt");
        if (miningLevel >= 15 && !addedPassiveTypes.contains("FORTUNE_BOOST"))
            passives.add("Fortune Boost");
        if (miningLevel >= 20 && !addedPassiveTypes.contains("AUTO_SMELT_UPGRADE"))
            passives.add("Auto Smelt Upgrade");
        
        // Add Logging passives
        if (loggingLevel >= 5 && !addedPassiveTypes.contains("DOUBLE_WOOD_DROP"))
            passives.add("Double Wood Drop");
        if (loggingLevel >= 10 && !addedPassiveTypes.contains("TREE_GROWTH_BOOST"))
            passives.add("Tree Growth Boost");
        if (loggingLevel >= 15 && !addedPassiveTypes.contains("TRIPLE_LOG_DROP"))
            passives.add("Triple Log Drop");
        
        // Add Farming passives
        if (farmingLevel >= 5 && !addedPassiveTypes.contains("FARMING_XP_BOOST"))
            passives.add("Farming XP Boost");
        if (farmingLevel >= 10 && !addedPassiveTypes.contains("AUTO_REPLANT"))
            passives.add("Auto Replant");
        if (farmingLevel >= 15 && !addedPassiveTypes.contains("DOUBLE_HARVEST"))
            passives.add("Double Harvest");
        if (farmingLevel >= 20 && !addedPassiveTypes.contains("GROWTH_BOOST"))
            passives.add("Growth Boost");
        
        // Add Fighting passives
        if (fightingLevel >= 5 && !addedPassiveTypes.contains("LIFESTEAL"))
            passives.add("Lifesteal");
        if (fightingLevel >= 10 && !addedPassiveTypes.contains("DAMAGE_REDUCTION"))
            passives.add("Damage Reduction");
        if (fightingLevel >= 15 && !addedPassiveTypes.contains("HEAL_ON_KILL"))
            passives.add("Heal on Kill");
        
        // Add Fishing passives
        if (fishingLevel >= 5 && !addedPassiveTypes.contains("XP_BOOST"))
            passives.add("XP Boost");
        if (fishingLevel >= 10 && !addedPassiveTypes.contains("TREASURE_HUNTER"))
            passives.add("Treasure Hunter");
        if (fishingLevel >= 15 && !addedPassiveTypes.contains("RARE_FISH_MASTER"))
            passives.add("Rare Fish Master");
        if (fishingLevel >= 20 && !addedPassiveTypes.contains("QUICK_HOOK"))
            passives.add("Quick Hook");
        
        // Add Enchanting passives
        if (enchantingLevel >= 5 && !addedPassiveTypes.contains("RESEARCH_MASTER"))
            passives.add("Research Master");
        if (enchantingLevel >= 10 && !addedPassiveTypes.contains("BOOK_UPGRADE"))
            passives.add("Book Upgrade");
        if (enchantingLevel >= 15 && !addedPassiveTypes.contains("CUSTOM_ENCHANTS"))
            passives.add("Custom Enchants");
        if (enchantingLevel >= 20 && !addedPassiveTypes.contains("RARE_ENCHANT_BOOST"))
            passives.add("Rare Enchant Boost");
        
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

        // Fishing Basics (Level 1) - just a starting passive, no effect

        // Bait Saver (Level 3) - 10% chance to not consume bait when fishing
        if (hasPassive(player, "fishing", "Bait Saver") && event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (Math.random() < 0.10) {
                // We can't directly prevent bait consumption, but we can give them an extra bait item
                Material baitType = Material.STRING; // Default bait
                ItemStack bait = new ItemStack(baitType);
                player.getInventory().addItem(bait);
                player.sendActionBar("§a⚓ Bait Saver: Bait preserved!");
            }
        }

        // XP Boost passives (Levels 5, 22, 42, 60, 72, 92)
        // These are handled in getXPMultiplier method
        if (hasPassive(player, "fishing", "XP Boost I") && event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            player.sendActionBar("§a+10% Fishing XP Boost Applied!");
        } else if (hasPassive(player, "fishing", "XP Boost II") && event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            player.sendActionBar("§a+15% Fishing XP Boost Applied!");
        } else if (hasPassive(player, "fishing", "XP Boost III") && event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            player.sendActionBar("§a+20% Fishing XP Boost Applied!");
        } else if (hasPassive(player, "fishing", "XP Boost IV") && event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            player.sendActionBar("§a+25% Fishing XP Boost Applied!");
        } else if (hasPassive(player, "fishing", "XP Boost V") && event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            player.sendActionBar("§a+30% Fishing XP Boost Applied!");
        } else if (hasPassive(player, "fishing", "XP Boost VI") && event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            player.sendActionBar("§a+40% Fishing XP Boost Applied!");
        }

        // Fish Finder (Level 7) - 5% increased fish catch rate
        if (hasPassive(player, "fishing", "Fish Finder") && event.getCaught() instanceof Item) {
            Item item = (Item) event.getCaught();
            ItemStack itemStack = item.getItemStack();
            
            // If not already a fish, there's a chance to convert to fish
            if (!isFish(itemStack.getType()) && Math.random() < 0.05) {
                itemStack.setType(Material.COD);
                player.sendActionBar("§b🐟 Fish Finder found you a fish!");
            }
        }

        // Treasure Hunter I-V (Levels 10, 27, 47, 65, 85)
        if (event.getCaught() instanceof Item) {
            double treasureChance = 0.0;
            String message = "";
            
            if (hasPassive(player, "fishing", "Treasure Hunter I")) {
                treasureChance = 0.10;
                message = "§6Your Treasure Hunter I passive found something special!";
            } else if (hasPassive(player, "fishing", "Treasure Hunter II")) {
                treasureChance = 0.15;
                message = "§6Your Treasure Hunter II passive found something special!";
            } else if (hasPassive(player, "fishing", "Treasure Hunter III")) {
                treasureChance = 0.20;
                message = "§6Your Treasure Hunter III passive found something special!";
            } else if (hasPassive(player, "fishing", "Treasure Hunter IV")) {
                treasureChance = 0.25;
                message = "§6Your Treasure Hunter IV passive found something special!";
            } else if (hasPassive(player, "fishing", "Treasure Hunter V")) {
                treasureChance = 0.30;
                message = "§6Your Treasure Hunter V passive found something special!";
            }
            
            if (treasureChance > 0 && Math.random() < treasureChance) {
                Item item = (Item) event.getCaught();
                ItemStack currentItem = item.getItemStack();
                
                // Replace with a better item if it's a fish or junk
                if (isFish(currentItem.getType()) || !isValuedItem(currentItem.getType())) {
                    // Instead of fish, give treasure
                    ItemStack treasureItem = getTreasureItem();
                    item.setItemStack(treasureItem);
                    player.sendMessage(message);
                }
            }
        }

        // Salmon Specialist (Level 12) - Implemented in FishingListener.handleFishTypeSpecialization

        // Rare Fish Master I & II (Levels 15, 40) - Implemented in FishingListener.handleFishTypeSpecialization 

        // Tropical Fish Specialist (Level 17) - Implemented in FishingListener.handleFishTypeSpecialization

        // Quick Hook I-IV (Levels 20, 37, 57, 87) - Implemented in FishingListener

        // Junk Reducer I-IV (Levels 25, 45, 62, 82) - Implemented in FishingListener.handleJunkReducer

        // Water Breathing (Level 30) - Implemented in FishingListener.checkWaterBreathingPassive

        // Double Catch I-III (Levels 32, 52, 75) - Implemented in FishingListener.handleExtraCatchChance

        // Enchanted Book Fisher I-III (Levels 35, 55, 77)
        if (event.getCaught() instanceof Item) {
            double enchantedBookChance = 0.0;
            String message = "";
            
            if (hasPassive(player, "fishing", "Enchanted Book Fisher I")) {
                enchantedBookChance = 0.05;
                message = "§dYour Enchanted Book Fisher I passive found a special book!";
            } else if (hasPassive(player, "fishing", "Enchanted Book Fisher II")) {
                enchantedBookChance = 0.10;
                message = "§dYour Enchanted Book Fisher II passive found a special book!";
            } else if (hasPassive(player, "fishing", "Enchanted Book Fisher III")) {
                enchantedBookChance = 0.15;
                message = "§dYour Enchanted Book Fisher III passive found a special book!";
            }
            
            if (enchantedBookChance > 0 && Math.random() < enchantedBookChance) {
                Item item = (Item) event.getCaught();
                
                // Create enchanted book
                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                
                // Get random enchantment
                Enchantment[] enchantments = Enchantment.values();
                Enchantment randomEnchant = enchantments[new Random().nextInt(enchantments.length)];
                
                // Higher levels have a chance for higher enchantment levels
                int maxLevel = 1;
                if (hasPassive(player, "fishing", "Enchanted Book Fisher II")) {
                    maxLevel = 2;
                } else if (hasPassive(player, "fishing", "Enchanted Book Fisher III")) {
                    maxLevel = 3;
                }
                
                // Random level between 1 and max
                int enchantLevel = new Random().nextInt(maxLevel) + 1;
                
                // Add the enchantment
                book.addUnsafeEnchantment(randomEnchant, enchantLevel);
                
                // Replace caught item
                item.setItemStack(book);
                player.sendMessage(message);
            }
        }

        // Night Fisher (Level 50) - Implemented in FishingListener.checkNightFisherPassive

        // Rain Fisher (Level 67) - Implemented in FishingListener.checkRainFisherPassive

        // Master Angler (Level 70) - All fishing stats improved by 10%
        // This is applied in various places including XP multiplier

        // Ocean Explorer (Level 80) - Chance to find rare ocean treasures
        if (hasPassive(player, "fishing", "Ocean Explorer") && event.getCaught() instanceof Item) {
            if (Math.random() < 0.05) { // 5% chance
                Item item = (Item) event.getCaught();
                
                // List of rare ocean items
                Material[] oceanTreasures = {
                    Material.HEART_OF_THE_SEA,
                    Material.NAUTILUS_SHELL,
                    Material.PRISMARINE_CRYSTALS,
                    Material.PRISMARINE_SHARD,
                    Material.SEA_LANTERN,
                    Material.TURTLE_EGG, // Changed from SCUTE to TURTLE_EGG
                    Material.TURTLE_HELMET
                };
                
                // Select random treasure
                Material treasure = oceanTreasures[new Random().nextInt(oceanTreasures.length)];
                ItemStack treasureItem = new ItemStack(treasure);
                
                // Replace the caught item
                item.setItemStack(treasureItem);
                player.sendMessage("§b✧ Ocean Explorer: You discovered a rare ocean treasure!");
            }
        }

        // Triple Catch (Level 90) - Implemented in FishingListener.handleExtraCatchChance

        // Legendary Fisher (Level 95) - All fishing yields increased by 25%
        // This is applied in various places including XP multiplier
        
        // Ancient Treasures (Level 97) - Chance to find ancient artifacts
        if (hasPassive(player, "fishing", "Ancient Treasures") && event.getCaught() instanceof Item) {
            if (Math.random() < 0.03) { // 3% chance
                Item item = (Item) event.getCaught();
                
                // List of ancient artifact items
                Material[] ancientArtifacts = {
                    Material.GOLDEN_APPLE,
                    Material.ENCHANTED_GOLDEN_APPLE,
                    Material.TOTEM_OF_UNDYING,
                    Material.TRIDENT,
                    Material.MUSIC_DISC_WAIT, // Random music disc
                    Material.NETHER_STAR,
                    Material.DRAGON_EGG,
                    Material.ELYTRA
                };
                
                // Select random artifact (weighted toward the less powerful ones)
                int index = 0;
                double roll = Math.random();
                if (roll < 0.6) {
                    // 60% chance for common artifacts (indices 0-3)
                    index = new Random().nextInt(4);
                } else if (roll < 0.9) {
                    // 30% chance for uncommon artifacts (indices 4-6)
                    index = 4 + new Random().nextInt(3);
                } else {
                    // 10% chance for very rare artifact (index 7)
                    index = 7;
                }
                
                Material artifact = ancientArtifacts[index];
                ItemStack artifactItem = new ItemStack(artifact);
                
                // Replace the caught item
                item.setItemStack(artifactItem);
                player.sendMessage("§5✦ Ancient Treasures: You discovered a legendary artifact!");
            }
        }

        // Master Fisher (Level 100) - Ultimate fishing mastery
        if (hasPassive(player, "fishing", "Master Fisher") && event.getCaught() instanceof Item) {
            Item item = (Item) event.getCaught();
            ItemStack currentItem = item.getItemStack();
            
            // 25% chance to upgrade any catch
            if (Math.random() < 0.25) {
                // If it's a fish, increase amount and quality
                if (isFish(currentItem.getType())) {
                    // Double the amount
                    currentItem.setAmount(currentItem.getAmount() * 2);
                    
                    // Chance to upgrade to better fish
                    if (currentItem.getType() == Material.COD && Math.random() < 0.5) {
                        currentItem.setType(Material.SALMON);
                    } else if (currentItem.getType() == Material.SALMON && Math.random() < 0.3) {
                        currentItem.setType(Material.TROPICAL_FISH);
                    }
                    
                    player.sendMessage("§b★ Master Fisher: Enhanced your fish catch!");
                } 
                // If it's treasure, enhance it
                else if (isValuedItem(currentItem.getType())) {
                    // If it's already enchanted, increase enchantment level
                    if (currentItem.hasItemMeta() && currentItem.getItemMeta().hasEnchants()) {
                        for (Enchantment enchant : currentItem.getEnchantments().keySet()) {
                            int currentLevel = currentItem.getEnchantmentLevel(enchant);
                            if (currentLevel < enchant.getMaxLevel()) {
                                currentItem.addUnsafeEnchantment(enchant, currentLevel + 1);
                            }
                        }
                    } 
                    // Otherwise add a random enchantment
                    else if (currentItem.getType() != Material.NAUTILUS_SHELL && 
                             currentItem.getType() != Material.HEART_OF_THE_SEA) {
                        Enchantment[] enchantments = Enchantment.values();
                        Enchantment randomEnchant = enchantments[new Random().nextInt(enchantments.length)];
                        currentItem.addUnsafeEnchantment(randomEnchant, 1);
                    }
                    
                    player.sendMessage("§b★ Master Fisher: Enhanced your treasure!");
                }
                // For junk, convert to fish
                else {
                    Material[] fishTypes = {Material.COD, Material.SALMON, Material.PUFFERFISH, Material.TROPICAL_FISH};
                    Material randomFish = fishTypes[new Random().nextInt(fishTypes.length)];
                    currentItem.setType(randomFish);
                    currentItem.setAmount(2); // Give 2 fish
                    player.sendMessage("§b★ Master Fisher: Converted junk to fish!");
                }
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
    
    private boolean isValuedItem(Material material) {
        return material == Material.NAME_TAG || 
               material == Material.SADDLE || 
               material == Material.NAUTILUS_SHELL || 
               material == Material.ENCHANTED_BOOK || 
               material == Material.BOW || 
               material == Material.FISHING_ROD || 
               material == Material.HEART_OF_THE_SEA || 
               material == Material.TRIDENT;
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
        ItemStack tool = player.getInventory().getItemInMainHand();
        boolean hasSilkTouch = tool.containsEnchantment(Enchantment.SILK_TOUCH);
        
        // Handle Stone Efficiency
        if (hasPassive(playerId, "stoneEfficiency") && isStone(block.getType())) {
            // Cancel default drops since we're custom handling them
            event.setDropItems(false);
            
            // Determine what to drop based on block type and enchantments
            Material dropType;
            if (block.getType() == Material.STONE) {
                // Drop stone with silk touch, otherwise cobblestone
                dropType = hasSilkTouch ? Material.STONE : Material.COBBLESTONE;
            } else {
                // For other stone types, drop themselves
                dropType = block.getType();
            }
            
            // Calculate fortune bonus if applicable
            int amount = 1;
            if (tool.containsEnchantment(Enchantment.FORTUNE) && !hasSilkTouch) {
                int fortuneLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);
                int fortuneBonus = 0;
                
                // Apply fortune boost passives
                if (hasPassive(playerId, "fortuneBoost")) {
                    fortuneBonus += 1;
                }
                if (hasPassive(playerId, "advancedFortune")) {
                    fortuneBonus += 1; // +2 total with fortuneBoost
                }
                if (hasPassive(playerId, "masterFortune")) {
                    fortuneBonus += 1; // +3 total with previous boosts
                }
                if (hasPassive(playerId, "legendaryFortune")) {
                    fortuneBonus += 1; // +4 total with all boosts
                }
                
                // Apply the fortune effect with our enhancement (only 25% chance for stone blocks)
                if (Math.random() < 0.25) {
                    int bonusDrops = calculateFortuneDrops(fortuneLevel + fortuneBonus);
                    amount += bonusDrops;
                    if (bonusDrops > 0) {
                        player.sendActionBar("§6Fortune " + (fortuneLevel + fortuneBonus) + " gave you extra drops!");
                    }
                }
            }
            
            // Drop the items
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropType, amount));
            
            // Apply XP boost for mining stone
            if (hasPassive(playerId, "miningXpBoost")) {
                givePlayerMiningXP(player, 2); // Give a small amount of XP
            } else {
                givePlayerMiningXP(player, 1); // Give a smaller amount of XP
            }
            
            // Check for the Treasure Hunter passive
            if (hasPassive(playerId, "treasureHunter") && Math.random() < 0.03) { // 3% chance
                dropTreasureItem(block.getLocation());
                player.sendActionBar("§6You found a hidden treasure!");
            }
            
            return;
        }
        
        // If the block is an obsidian, handle obsidian specialist
        if (block.getType() == Material.OBSIDIAN && hasPassive(playerId, "obsidianSpecialist")) {
            // We don't want to change the drops, just make it break faster
            // This is handled elsewhere with mining speed adjustments
        }
        
        // If the block is deepslate, handle deepslate expert
        if (isDeepslate(block.getType()) && hasPassive(playerId, "deepslateExpert")) {
            // We don't want to change the drops, just make it break faster
            // This is handled elsewhere with mining speed adjustments
        }
        
        // Only process ore blocks
        if (!isOre(block.getType())) {
            return;
        }

        // Cancel the default event since we'll handle drops manually
        event.setDropItems(false);

        int amount = 1;
        Material oreType = block.getType();
        
        // Check for silk touch on ores
        if (hasSilkTouch) {
            // With silk touch, just drop the ore block itself
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(oreType, 1));
            
            // Apply XP boost for mining
            if (hasPassive(playerId, "miningXpBoost")) {
                givePlayerMiningXP(player, calculateOreXP(block.getType()));
            } else {
                givePlayerMiningXP(player, calculateOreXP(block.getType()) * 0.5); // Less XP for silk touch
            }
            
            return;
        }
        
        // Apply ore specialization bonuses
        boolean hasSpecialization = false;
        if (oreType == Material.COAL_ORE || oreType == Material.DEEPSLATE_COAL_ORE) {
            if (hasPassive(playerId, "coalSpecialization")) {
                amount += 1; // +1 coal (20% more)
                hasSpecialization = true;
            }
        } else if (oreType == Material.IRON_ORE || oreType == Material.DEEPSLATE_IRON_ORE) {
            if (hasPassive(playerId, "ironSpecialization")) {
                amount += 1; // +1 iron (20% more)
                hasSpecialization = true;
            }
        } else if (oreType == Material.GOLD_ORE || oreType == Material.DEEPSLATE_GOLD_ORE) {
            if (hasPassive(playerId, "goldSpecialization")) {
                amount += 1; // +1 gold (20% more)
                hasSpecialization = true;
            }
        } else if (oreType == Material.REDSTONE_ORE || oreType == Material.DEEPSLATE_REDSTONE_ORE) {
            if (hasPassive(playerId, "redstoneSpecialization")) {
                amount += 1; // +1 redstone (20% more)
                hasSpecialization = true;
            }
        } else if (oreType == Material.LAPIS_ORE || oreType == Material.DEEPSLATE_LAPIS_ORE) {
            if (hasPassive(playerId, "lapisSpecialization")) {
                amount += 1; // +1 lapis (20% more)
                hasSpecialization = true;
            }
        } else if (oreType == Material.COPPER_ORE || oreType == Material.DEEPSLATE_COPPER_ORE) {
            if (hasPassive(playerId, "copperSpecialization")) {
                amount += 1; // +1 copper (20% more)
                hasSpecialization = true;
            }
        } else if (oreType == Material.DIAMOND_ORE || oreType == Material.DEEPSLATE_DIAMOND_ORE) {
            if (hasPassive(playerId, "diamondSpecialization")) {
                amount += 1; // +1 diamond (20% more)
                hasSpecialization = true;
            }
        } else if (oreType == Material.EMERALD_ORE || oreType == Material.DEEPSLATE_EMERALD_ORE) {
            if (hasPassive(playerId, "emeraldSpecialization")) {
                amount += 1; // +1 emerald (20% more)
                hasSpecialization = true;
            }
        }
        
        if (hasSpecialization) {
            player.sendActionBar("§6Your specialization gives you bonus resources!");
        }

        // Apply luck-based passive effects
        if (hasPassive(playerId, "doubleOreDrop") && Math.random() < 0.25) { // 25% chance
            amount *= 2;
            player.sendActionBar("§6You got lucky and the ore dropped twice as much!");
        } else if (hasPassive(playerId, "tripleOreDrop") && Math.random() < 0.15) { // 15% chance
            amount *= 3;
            player.sendActionBar("§6You got very lucky and the ore dropped triple the amount!");
        } else if (hasPassive(playerId, "quadrupleOreDrop") && Math.random() < 0.05) { // 5% chance
            amount *= 4;
            player.sendActionBar("§6Incredible luck! The ore dropped quadruple the amount!");
        }

        // Fortune effects implementation
        if (tool.containsEnchantment(Enchantment.FORTUNE)) {
            int fortuneLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);
            int fortuneBonus = 0;
            
            // Apply fortune boost passives
            if (hasPassive(playerId, "fortuneBoost")) {
                fortuneBonus += 1;
            }
            if (hasPassive(playerId, "advancedFortune")) {
                fortuneBonus += 1; // +2 total with fortuneBoost
            }
            if (hasPassive(playerId, "masterFortune")) {
                fortuneBonus += 1; // +3 total with previous boosts
            }
            if (hasPassive(playerId, "legendaryFortune")) {
                fortuneBonus += 1; // +4 total with all boosts
            }
            
            // Apply the fortune effect with our enhancement
            int bonusDrops = calculateFortuneDrops(fortuneLevel + fortuneBonus);
            if (bonusDrops > 0) {
                amount += bonusDrops;
                player.sendActionBar("§6Fortune " + (fortuneLevel + fortuneBonus) + " gave you extra drops!");
            }
        }

        // Smelting logic
        boolean isSmelted = hasPassive(playerId, "autoSmelt");
        Material dropMaterial = getOreDrop(block.getType(), isSmelted);
        
        // Auto Smelt Upgrade implementation
        if (isSmelted) {
            if (hasPassive(playerId, "masterSmelter") && Math.random() < 0.35) { // 35% chance for master smelter
                amount *= 2;
                player.sendActionBar("§6Master Smelter doubled your smelting output!");
            } else if (hasPassive(playerId, "autoSmeltUpgrade") && Math.random() < 0.20) { // 20% chance for regular upgrade
                amount *= 2;
                player.sendActionBar("§6Auto Smelt Upgrade doubled your smelting output!");
            }
            
            // Ultimate Smelter has a chance to triple output
            if (hasPassive(playerId, "ultimateSmelter") && Math.random() < 0.10) { // 10% chance
                amount *= 3;
                player.sendActionBar("§6Ultimate Smelter tripled your smelting output!");
            }
        }
        
        // Apply Unbreakable Tools passive - 10% chance tools don't lose durability
        if (hasPassive(playerId, "unbreakableTools") && Math.random() < 0.10) {
            // Get the tool being used
            if (!tool.getType().isAir() && tool.getType().getMaxDurability() > 0) {
                // If about to take damage, prevent it by canceling the event
                // This is a simple way to prevent durability loss
                player.sendActionBar("§6Your tool didn't lose any durability!");
            }
        }
        
        // Apply Miner's Haste passive after mining ore
        if (hasPassive(playerId, "minersHaste")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 5 * 20, 0)); // Haste I for 5 seconds
            player.sendActionBar("§6Miner's Haste activated!");
        }
        
        // Apply Advanced Haste for rare ores
        if (hasPassive(playerId, "advancedHaste") && isRareOre(block.getType())) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 10 * 20, 1)); // Haste II for 10 seconds
            player.sendActionBar("§6Advanced Haste activated from rare ore!");
        }
        
        // Apply Mining XP Boost passive
        if (hasPassive(playerId, "miningXpBoost")) {
            // Give bonus XP for mining based on ore type
            givePlayerMiningXP(player, calculateOreXP(block.getType()) * 1.1); // 10% more XP
        } else {
            // Standard XP
            givePlayerMiningXP(player, calculateOreXP(block.getType()));
        }
        
        // Apply Miner's Night Vision when below Y=30
        if (hasPassive(playerId, "minersNightVision") && block.getY() < 30) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 60 * 20, 0)); // Night Vision for 60 seconds
        }
        
        // Apply excavation passives for adjacent blocks
        if ((hasPassive(playerId, "excavation") || hasPassive(playerId, "excavationMaster")) && isStone(block.getType())) {
            int radius = hasPassive(playerId, "excavationMaster") ? 2 : 1; // 5x5 for master, 3x3 for regular
            excavateArea(player, block, radius);
        }
        
        // Apply Nether Mining bonus for nether ores
        if (hasPassive(playerId, "netherMining") && isNetherOre(block.getType())) {
            amount = (int)(amount * 1.15); // 15% more yield
            player.sendActionBar("§6Nether Mining bonus applied!");
        }

        // Drop the final item
        if (dropMaterial != null) {
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropMaterial, amount));
        }
    }

    // Helper method for the Treasure Hunter passive
    private void dropTreasureItem(Location location) {
        // Random treasure selection
        double rand = Math.random();
        ItemStack treasure;
        
        if (rand < 0.60) { // 60% common treasures
            Material[] commonTreasures = {
                Material.IRON_INGOT, Material.GOLD_INGOT, Material.REDSTONE, 
                Material.COAL, Material.LAPIS_LAZULI
            };
            treasure = new ItemStack(commonTreasures[new Random().nextInt(commonTreasures.length)], 1 + new Random().nextInt(3));
        } else if (rand < 0.90) { // 30% uncommon treasures
            Material[] uncommonTreasures = {
                Material.DIAMOND, Material.EMERALD, Material.AMETHYST_SHARD,
                Material.GOLD_NUGGET, Material.QUARTZ
            };
            treasure = new ItemStack(uncommonTreasures[new Random().nextInt(uncommonTreasures.length)], 1 + new Random().nextInt(2));
        } else { // 10% rare treasures
            Material[] rareTreasures = {
                Material.ANCIENT_DEBRIS, Material.EMERALD, Material.DIAMOND,
                Material.NAUTILUS_SHELL, Material.HEART_OF_THE_SEA
            };
            treasure = new ItemStack(rareTreasures[new Random().nextInt(rareTreasures.length)], 1);
        }
        
        location.getWorld().dropItemNaturally(location, treasure);
    }

    // Helper method to excavate an area around a block
    private void excavateArea(Player player, Block centerBlock, int radius) {
        UUID playerId = player.getUniqueId();
        Material targetType = centerBlock.getType();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Skip the center block as it's already broken
                    if (x == 0 && y == 0 && z == 0) continue;
                    
                    Block relativeBlock = centerBlock.getRelative(x, y, z);
                    
                    // Only break blocks of similar type (stone variants)
                    if (isStone(relativeBlock.getType())) {
                        // Break the block and drop its item
                        relativeBlock.breakNaturally(player.getInventory().getItemInMainHand());
                        
                        // Apply XP boost if the player has it
                        if (hasPassive(playerId, "miningXpBoost")) {
                            givePlayerMiningXP(player, 1); // Small amount of XP per block
                        }
                    }
                }
            }
        }
    }

    // Check if a material is stone or a stone variant
    private boolean isStone(Material material) {
        return material == Material.STONE || 
               material == Material.COBBLESTONE || 
               material == Material.GRANITE || 
               material == Material.DIORITE || 
               material == Material.ANDESITE ||
               material == Material.CALCITE ||
               material == Material.TUFF;
    }

    // Check if a material is deepslate or a deepslate variant
    private boolean isDeepslate(Material material) {
        return material == Material.DEEPSLATE || 
               material == Material.COBBLED_DEEPSLATE || 
               material.name().contains("DEEPSLATE");
    }

    // Check if a material is a rare ore
    private boolean isRareOre(Material material) {
        return material == Material.DIAMOND_ORE || 
               material == Material.DEEPSLATE_DIAMOND_ORE || 
               material == Material.EMERALD_ORE || 
               material == Material.DEEPSLATE_EMERALD_ORE ||
               material == Material.ANCIENT_DEBRIS;
    }

    // Check if a material is a nether ore
    private boolean isNetherOre(Material material) {
        return material == Material.NETHER_GOLD_ORE || 
               material == Material.NETHER_QUARTZ_ORE || 
               material == Material.ANCIENT_DEBRIS;
    }

    // Calculate XP value for different ore types
    private int calculateOreXP(Material material) {
        if (material == Material.COAL_ORE || material == Material.DEEPSLATE_COAL_ORE) {
            return 1;
        } else if (material == Material.IRON_ORE || material == Material.DEEPSLATE_IRON_ORE) {
            return 2;
        } else if (material == Material.COPPER_ORE || material == Material.DEEPSLATE_COPPER_ORE) {
            return 2;
        } else if (material == Material.GOLD_ORE || material == Material.DEEPSLATE_GOLD_ORE || material == Material.NETHER_GOLD_ORE) {
            return 3;
        } else if (material == Material.REDSTONE_ORE || material == Material.DEEPSLATE_REDSTONE_ORE) {
            return 3;
        } else if (material == Material.LAPIS_ORE || material == Material.DEEPSLATE_LAPIS_ORE) {
            return 3;
        } else if (material == Material.DIAMOND_ORE || material == Material.DEEPSLATE_DIAMOND_ORE) {
            return 5;
        } else if (material == Material.EMERALD_ORE || material == Material.DEEPSLATE_EMERALD_ORE) {
            return 7;
        } else if (material == Material.NETHER_QUARTZ_ORE) {
            return 2;
        } else if (material == Material.ANCIENT_DEBRIS) {
            return 10;
        } else {
            return 1;
        }
    }

    // Give mining XP to the player
    private void givePlayerMiningXP(Player player, double amount) {
        // Calculate any XP multipliers from other passives
        double multiplier = 1.0;
        
        // Apply Master Miner bonus if applicable
        if (hasPassive(player.getUniqueId(), "masterMiner")) {
            multiplier *= 1.25; // 25% bonus to all mining XP
        }
        
        // Apply mining XP boost levels
        if (hasPassive(player.getUniqueId(), "miningXpBoost")) {
            UUID playerId = player.getUniqueId();
            int miningLevel = plugin.getPlayerDataManager().getSkillLevel(playerId, "mining");
            
            if (miningLevel >= 82) { // XP Boost V
                multiplier *= 1.3; // +30%
            } else if (miningLevel >= 64) { // XP Boost IV
                multiplier *= 1.25; // +25%
            } else if (miningLevel >= 45) { // XP Boost III
                multiplier *= 1.2; // +20%
            } else if (miningLevel >= 24) { // XP Boost II
                multiplier *= 1.15; // +15%
            } else if (miningLevel >= 7) { // XP Boost I
                multiplier *= 1.1; // +10%
            }
        }
        
        // Calculate final XP amount
        int finalXp = (int)(amount * multiplier);
        
        // Call XP manager to add XP
        xpManager.addXP(player, "mining", finalXp);
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
}
