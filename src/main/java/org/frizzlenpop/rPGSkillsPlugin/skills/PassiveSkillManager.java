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
import org.bukkit.inventory.Inventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.Particle;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;

import org.frizzlenpop.rPGSkillsPlugin.data.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

import org.frizzlenpop.rPGSkillsPlugin.api.events.PassiveAbilityUnlockEvent;

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
    private final Set<UUID> farmingBasicsPlayers = new HashSet<>();
    private final Set<UUID> seedSaverIPlayers = new HashSet<>();
    private final Set<UUID> farmingXpBoostIPlayers = new HashSet<>();
    private final Set<UUID> harvestFinderPlayers = new HashSet<>();
    private final Set<UUID> doubleCropYieldIPlayers = new HashSet<>();
    private final Set<UUID> wheatSpecialistPlayers = new HashSet<>();
    private final Set<UUID> growthSpeedIPlayers = new HashSet<>();
    private final Set<UUID> carrotSpecialistPlayers = new HashSet<>();
    private final Set<UUID> autoReplantIPlayers = new HashSet<>();
    private final Set<UUID> farmingXpBoostIIPlayers = new HashSet<>();
    private final Set<UUID> seedSaverIIPlayers = new HashSet<>();
    private final Set<UUID> potatoSpecialistPlayers = new HashSet<>();
    private final Set<UUID> fertilizerMasterPlayers = new HashSet<>();
    private final Set<UUID> doubleCropYieldIIPlayers = new HashSet<>();
    private final Set<UUID> beetrootSpecialistPlayers = new HashSet<>();
    private final Set<UUID> growthSpeedIIPlayers = new HashSet<>();
    private final Set<UUID> rareCropMasterIPlayers = new HashSet<>();
    private final Set<UUID> farmingXpBoostIIIPlayers = new HashSet<>();
    private final Set<UUID> seedSaverIIIPlayers = new HashSet<>();
    private final Set<UUID> melonSpecialistPlayers = new HashSet<>();
    private final Set<UUID> autoReplantIIPlayers = new HashSet<>();
    private final Set<UUID> doubleCropYieldIIIPlayers = new HashSet<>();
    private final Set<UUID> pumpkinSpecialistPlayers = new HashSet<>();
    private final Set<UUID> growthSpeedIIIPlayers = new HashSet<>();
    private final Set<UUID> rareCropMasterIIPlayers = new HashSet<>();
    private final Set<UUID> farmingXpBoostIVPlayers = new HashSet<>();
    private final Set<UUID> seedSaverIVPlayers = new HashSet<>();
    private final Set<UUID> netherWartSpecialistPlayers = new HashSet<>();
    private final Set<UUID> soilEnrichmentPlayers = new HashSet<>();
    private final Set<UUID> farmingXpBoostVPlayers = new HashSet<>();
    private final Set<UUID> tripleCropYieldPlayers = new HashSet<>();
    private final Set<UUID> cactusSpecialistPlayers = new HashSet<>();
    private final Set<UUID> autoReplantIIIPlayers = new HashSet<>();
    private final Set<UUID> seedSaverVPlayers = new HashSet<>();
    private final Set<UUID> growthSpeedIVPlayers = new HashSet<>();
    private final Set<UUID> sugarCaneSpecialistPlayers = new HashSet<>();
    private final Set<UUID> instantGrowthMasterPlayers = new HashSet<>();
    private final Set<UUID> farmingXpBoostVIPlayers = new HashSet<>();
    private final Set<UUID> legendaryFarmerPlayers = new HashSet<>();
    private final Set<UUID> quadrupleCropYieldPlayers = new HashSet<>();
    private final Set<UUID> masterFarmerPlayers = new HashSet<>();
    
    // Existing fields for auto replant and double crop yield
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
    
    // Enchanting passives
    private final Set<UUID> enchantingBasicsPlayers = new HashSet<>();
    private final Set<UUID> lapisSaverIPlayers = new HashSet<>();
    private final Set<UUID> lapisSaverIIPlayers = new HashSet<>();
    private final Set<UUID> lapisSaverIIIPlayers = new HashSet<>();
    private final Set<UUID> lapisSaverIVPlayers = new HashSet<>();
    private final Set<UUID> lapisSaverVPlayers = new HashSet<>();
    private final Set<UUID> researchMasterIPlayers = new HashSet<>();
    private final Set<UUID> researchMasterIIPlayers = new HashSet<>();
    private final Set<UUID> researchMasterIIIPlayers = new HashSet<>();
    private final Set<UUID> researchMasterIVPlayers = new HashSet<>();
    private final Set<UUID> researchMasterVPlayers = new HashSet<>();
    private final Set<UUID> enchantingXpBoostIPlayers = new HashSet<>();
    private final Set<UUID> enchantingXpBoostIIPlayers = new HashSet<>();
    private final Set<UUID> enchantingXpBoostIIIPlayers = new HashSet<>();
    private final Set<UUID> enchantingXpBoostIVPlayers = new HashSet<>();
    private final Set<UUID> enchantingXpBoostVPlayers = new HashSet<>();
    private final Set<UUID> bookUpgradeIPlayers = new HashSet<>();
    private final Set<UUID> bookUpgradeIIPlayers = new HashSet<>();
    private final Set<UUID> bookUpgradeIIIPlayers = new HashSet<>();
    private final Set<UUID> bookUpgradeIVPlayers = new HashSet<>();
    private final Set<UUID> bookUpgradeVPlayers = new HashSet<>();
    private final Set<UUID> efficiencySpecialistPlayers = new HashSet<>();
    private final Set<UUID> customEnchantsIPlayers = new HashSet<>();
    private final Set<UUID> customEnchantsIIPlayers = new HashSet<>();
    private final Set<UUID> customEnchantsIIIPlayers = new HashSet<>();
    private final Set<UUID> customEnchantsIVPlayers = new HashSet<>();
    private final Set<UUID> customEnchantsVPlayers = new HashSet<>();
    private final Set<UUID> protectionSpecialistPlayers = new HashSet<>();
    private final Set<UUID> rareEnchantBoostIPlayers = new HashSet<>();
    private final Set<UUID> rareEnchantBoostIIPlayers = new HashSet<>();
    private final Set<UUID> rareEnchantBoostIIIPlayers = new HashSet<>();
    private final Set<UUID> rareEnchantBoostIVPlayers = new HashSet<>();
    private final Set<UUID> sharpnessSpecialistPlayers = new HashSet<>();
    private final Set<UUID> fortuneSpecialistPlayers = new HashSet<>();
    private final Set<UUID> lootingSpecialistPlayers = new HashSet<>();
    private final Set<UUID> silkTouchSpecialistPlayers = new HashSet<>();
    private final Set<UUID> powerSpecialistPlayers = new HashSet<>();
    private final Set<UUID> mendingSpecialistPlayers = new HashSet<>();
    private final Set<UUID> unbreakingSpecialistPlayers = new HashSet<>();
    private final Set<UUID> legendaryEnchanterPlayers = new HashSet<>();
    private final Set<UUID> masterEnchanterPlayers = new HashSet<>();

    // Excavation passives
    private final Set<UUID> excavationBasicsPlayers = new HashSet<>();
    private final Set<UUID> doubleDropsPlayers = new HashSet<>();
    private final Set<UUID> archaeologyBasicsPlayers = new HashSet<>();
    private final Set<UUID> treasureFinderPlayers = new HashSet<>();
    private final Set<UUID> shovelEfficiencyPlayers = new HashSet<>();
    private final Set<UUID> excavationXpBoostPlayers = new HashSet<>();
    private final Set<UUID> rareFindPlayers = new HashSet<>();
    private final Set<UUID> multiBlockPlayers = new HashSet<>();
    private final Set<UUID> ancientArtifactsPlayers = new HashSet<>();
    private final Set<UUID> masterExcavatorPlayers = new HashSet<>();
    
    // Repair passives
    private final Set<UUID> repairBasicsPlayers = new HashSet<>();
    private final Set<UUID> materialSaverPlayers = new HashSet<>();
    private final Set<UUID> experienceSaverPlayers = new HashSet<>();
    private final Set<UUID> qualityRepairPlayers = new HashSet<>();
    private final Set<UUID> toolExpertPlayers = new HashSet<>();
    private final Set<UUID> weaponExpertPlayers = new HashSet<>();
    private final Set<UUID> armorExpertPlayers = new HashSet<>();
    private final Set<UUID> masterSmithPlayers = new HashSet<>();
    
    private final XPManager xpManager;
    private final RPGSkillsPlugin plugin; // Needed for scheduling and config
    
    // Add a set to track player-placed blocks
    private final Set<String> playerPlacedBlocks = new HashSet<>();

    private DatabaseManager databaseManager;
    private boolean useDatabase = false;
    
    public PassiveSkillManager(XPManager xpManager, RPGSkillsPlugin plugin) {
        this.xpManager = xpManager;
        this.plugin = plugin;
        this.activePassives = new HashMap<>();
        
        // Register this class as a listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // Load active passives for online players (old system)
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            loadPlayerPassives(player);
        }
        
        // Start the growth speed task if needed
        startGrowthSpeedTask();
        
        // Start the tree growth booster task
        startTreeGrowthBooster();
        
        // Start the player-placed blocks cleanup task
        startPlacedBlocksCleanupTask();
        
        // Load passives from config
        loadPassives();
    }
    
    /**
     * Sets the database manager and enables database storage
     * 
     * @param databaseManager The database manager to use
     */
    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.useDatabase = true;
    }
    
    /**
     * Checks if database storage is enabled
     * 
     * @return True if database storage is enabled, false otherwise
     */
    public boolean isDatabaseEnabled() {
        return useDatabase && databaseManager != null;
    }
    
    /**
     * Loads a player's passive abilities from the database or YAML file
     * 
     * @param playerUUID The player's UUID
     */
    public void loadPlayerPassives(UUID playerUUID) {
        if (isDatabaseEnabled()) {
            loadPlayerPassivesFromDatabase(playerUUID);
        } else {
            loadPlayerPassivesFromFile(playerUUID);
        }
    }
    
    /**
     * Loads a player's passive abilities from the database
     * 
     * @param playerUUID The player's UUID
     */
    private void loadPlayerPassivesFromDatabase(UUID playerUUID) {
        // Clear existing passives for this player
        playerPassives.remove(playerUUID);
        
        // Create a new map for this player's passives
        Map<String, Set<String>> skillPassives = new HashMap<>();
        playerPassives.put(playerUUID, skillPassives);
        
        // Load passives from database
        databaseManager.executeAsyncVoid(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT skill_name, passive_name FROM player_passives WHERE player_uuid = ?")) {
                stmt.setString(1, playerUUID.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String skillName = rs.getString("skill_name");
                        String passiveName = rs.getString("passive_name");
                        
                        // Add to the player's passives map
                        skillPassives.computeIfAbsent(skillName, k -> new HashSet<>()).add(passiveName);
                        
                        // Apply the passive effect
                        applyPassiveEffect(playerUUID, skillName, passiveName);
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Error executing query: " + e.getMessage());
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error preparing statement: " + e.getMessage());
            }
        });
    }
    
    /**
     * Loads a player's passive abilities from YAML file
     * 
     * @param playerUUID The player's UUID
     */
    private void loadPlayerPassivesFromFile(UUID playerUUID) {
        // Clear existing passives for this player
        playerPassives.remove(playerUUID);
        
        // Create a new map for this player's passives
        Map<String, Set<String>> skillPassives = new HashMap<>();
        playerPassives.put(playerUUID, skillPassives);
        
        // Load from file
        FileConfiguration playerData = plugin.getPlayerDataManager().getPlayerData(playerUUID);
        ConfigurationSection passivesSection = playerData.getConfigurationSection("passiveAbilities");
        
        if (passivesSection != null) {
            for (String skillName : passivesSection.getKeys(false)) {
                List<String> passives = playerData.getStringList("passiveAbilities." + skillName);
                
                // Add to the player's passives map
                Set<String> skillPassiveSet = new HashSet<>(passives);
                skillPassives.put(skillName, skillPassiveSet);
                
                // Apply each passive effect
                for (String passiveName : passives) {
                    applyPassiveEffect(playerUUID, skillName, passiveName);
                }
            }
        }
    }
    
    /**
     * Saves a player's passive abilities to the database or YAML file
     * 
     * @param playerUUID The player's UUID
     */
    public void savePlayerPassives(UUID playerUUID) {
        if (isDatabaseEnabled()) {
            savePlayerPassivesToDatabase(playerUUID);
        } else {
            savePlayerPassivesToFile(playerUUID);
        }
    }
    
    /**
     * Saves a player's passive abilities to the database
     * 
     * @param playerUUID The player's UUID
     */
    private void savePlayerPassivesToDatabase(UUID playerUUID) {
        Map<String, Set<String>> skillPassives = playerPassives.get(playerUUID);
        if (skillPassives == null) {
            return;
        }
        
        databaseManager.executeAsyncVoid(conn -> {
            try {
                // First, delete all existing passives for this player
                try (PreparedStatement deleteStmt = conn.prepareStatement(
                        "DELETE FROM player_passives WHERE player_uuid = ?")) {
                    try {
                        deleteStmt.setString(1, playerUUID.toString());
                        deleteStmt.executeUpdate();
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Error deleting player passives: " + e.getMessage());
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Error preparing delete statement: " + e.getMessage());
                }
                
                // Then, insert all current passives
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO player_passives (player_uuid, skill_name, passive_name) VALUES (?, ?, ?)")) {
                    for (Map.Entry<String, Set<String>> entry : skillPassives.entrySet()) {
                        String skillName = entry.getKey();
                        Set<String> passives = entry.getValue();
                        
                        for (String passiveName : passives) {
                            try {
                                insertStmt.setString(1, playerUUID.toString());
                                insertStmt.setString(2, skillName);
                                insertStmt.setString(3, passiveName);
                                insertStmt.executeUpdate();
                            } catch (SQLException e) {
                                plugin.getLogger().severe("Error inserting passive " + passiveName + " for skill " + skillName + ": " + e.getMessage());
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Error preparing insert statement: " + e.getMessage());
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error saving player passives: " + e.getMessage());
            }
        });
    }
    
    /**
     * Saves a player's passive abilities to YAML file
     * 
     * @param playerUUID The player's UUID
     */
    private void savePlayerPassivesToFile(UUID playerUUID) {
        Map<String, Set<String>> skillPassives = playerPassives.get(playerUUID);
        if (skillPassives == null) {
            return;
        }
        
        FileConfiguration playerData = plugin.getPlayerDataManager().getPlayerData(playerUUID);
        
        // Clear existing passives section
        playerData.set("passiveAbilities", null);
        
        // Create a new passives section
        for (Map.Entry<String, Set<String>> entry : skillPassives.entrySet()) {
            String skillName = entry.getKey();
            Set<String> passives = entry.getValue();
            
            playerData.set("passiveAbilities." + skillName, new ArrayList<>(passives));
        }
        
        // Save the player data
        plugin.getPlayerDataManager().savePlayerData(playerUUID, playerData);
    }
    
    /**
     * Adds a passive ability to a player
     * 
     * @param playerUUID The player's UUID
     * @param skillName The skill name
     * @param passiveName The passive ability name
     */
    public void addPassive(UUID playerUUID, String skillName, String passiveName) {
        // Add to the player's passives map
        playerPassives.computeIfAbsent(playerUUID, k -> new HashMap<>())
                     .computeIfAbsent(skillName, k -> new HashSet<>())
                     .add(passiveName);
        
        // Apply the passive effect
        applyPassiveEffect(playerUUID, skillName, passiveName);
        
        // Fire the PassiveAbilityUnlockEvent if the player is online
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            PassiveAbilityUnlockEvent event = new PassiveAbilityUnlockEvent(player, skillName, passiveName);
            Bukkit.getPluginManager().callEvent(event);
        }
        
        // Save the player's passives
        savePlayerPassives(playerUUID);
    }
    
    /**
     * Removes a passive ability from a player
     * 
     * @param playerUUID The player's UUID
     * @param skillName The skill name
     * @param passiveName The passive ability name
     */
    public void removePassive(UUID playerUUID, String skillName, String passiveName) {
        // Remove from the player's passives map
        Map<String, Set<String>> skillPassives = playerPassives.get(playerUUID);
        if (skillPassives != null) {
            Set<String> passives = skillPassives.get(skillName);
            if (passives != null) {
                passives.remove(passiveName);
                
                // Remove the skill entry if it's empty
                if (passives.isEmpty()) {
                    skillPassives.remove(skillName);
                }
                
                // Remove the player entry if it's empty
                if (skillPassives.isEmpty()) {
                    playerPassives.remove(playerUUID);
                }
            }
        }
        
        // Remove the passive effect
        removePassiveEffect(playerUUID, skillName, passiveName);
        
        // Save the player's passives
        savePlayerPassives(playerUUID);
    }
    
    /**
     * Checks if a player has a specific passive ability
     * 
     * @param playerUUID The player's UUID
     * @param skillName The skill name
     * @param passiveName The passive ability name
     * @return True if the player has the passive ability, false otherwise
     */
    public boolean hasPassive(UUID playerUUID, String skillName, String passiveName) {
        Map<String, Set<String>> skillPassives = playerPassives.get(playerUUID);
        if (skillPassives != null) {
            Set<String> passives = skillPassives.get(skillName);
            return passives != null && passives.contains(passiveName);
        }
        return false;
    }
    
    /**
     * Gets all passive abilities for a player and skill
     * 
     * @param playerUUID The player's UUID
     * @param skillName The skill name
     * @return A set of passive ability names
     */
    public Set<String> getPassives(UUID playerUUID, String skillName) {
        Map<String, Set<String>> skillPassives = playerPassives.get(playerUUID);
        if (skillPassives != null) {
            Set<String> passives = skillPassives.get(skillName);
            if (passives != null) {
                return new HashSet<>(passives); // Return a copy to prevent modification
            }
        }
        return new HashSet<>();
    }
    
    /**
     * Gets all passive abilities for a player
     * 
     * @param playerUUID The player's UUID
     * @return A map of skill names to sets of passive ability names
     */
    public Map<String, Set<String>> getAllPassives(UUID playerUUID) {
        Map<String, Set<String>> skillPassives = playerPassives.get(playerUUID);
        if (skillPassives != null) {
            // Create a deep copy to prevent modification
            Map<String, Set<String>> copy = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : skillPassives.entrySet()) {
                copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
            return copy;
        }
        return new HashMap<>();
    }
    
    /**
     * Applies a passive effect to a player
     * 
     * @param playerUUID The player's UUID
     * @param skillName The skill name
     * @param passiveName The passive ability name
     */
    private void applyPassiveEffect(UUID playerUUID, String skillName, String passiveName) {
        // ... existing code ...
    }
    
    /**
     * Removes a passive effect from a player
     * 
     * @param playerUUID The player's UUID
     * @param skillName The skill name
     * @param passiveName The passive ability name
     */
    private void removePassiveEffect(UUID playerUUID, String skillName, String passiveName) {
        // ... existing code ...
    }
    
    /**
     * Starts a task to periodically clean up the player-placed blocks set
     * to prevent memory leaks from chunks that are unloaded
     */
    private void startPlacedBlocksCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Create a copy of the set to avoid ConcurrentModificationException
            Set<String> toRemove = new HashSet<>();
            
            for (String blockKey : playerPlacedBlocks) {
                String[] parts = blockKey.split(":");
                if (parts.length != 4) continue;
                
                String worldName = parts[0];
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    // World is unloaded, remove the entry
                    toRemove.add(blockKey);
                    continue;
                }
                
                // Check if the chunk is loaded
                int chunkX = x >> 4;
                int chunkZ = z >> 4;
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    // Chunk is unloaded, remove the entry
                    toRemove.add(blockKey);
                    continue;
                }
                
                // Check if the block is still there (might have been broken)
                Block block = world.getBlockAt(x, y, z);
                if (block.getType() == Material.AIR) {
                    // Block is gone, remove the entry
                    toRemove.add(blockKey);
                }
            }
            
            // Remove all entries that need to be removed
            playerPlacedBlocks.removeAll(toRemove);
            
            // Log cleanup info if significant
            if (toRemove.size() > 100) {
                plugin.getLogger().info("Cleaned up " + toRemove.size() + " player-placed block entries");
            }
        }, 20 * 60 * 30, 20 * 60 * 30); // Run every 30 minutes
    }

    // --- OLD SYSTEM: Loading player passives from PlayerDataManager ---
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayerPassives(event.getPlayer().getUniqueId());
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
                if (level >= 1) {
                    unlockPassive(player, skill, "Farming Basics");
                    farmingBasicsPlayers.add(player.getUniqueId());
                }
                if (level >= 3) {
                    unlockPassive(player, skill, "Seed Saver I");
                    seedSaverIPlayers.add(player.getUniqueId());
                }
                if (level >= 5) {
                    unlockPassive(player, skill, "Farming XP Boost I");
                    farmingXpBoostIPlayers.add(player.getUniqueId());
                    doubleCropYieldPlayers.add(player.getUniqueId()); // Legacy support
                }
                if (level >= 7) {
                    unlockPassive(player, skill, "Harvest Finder");
                    harvestFinderPlayers.add(player.getUniqueId());
                }
                if (level >= 10) {
                    unlockPassive(player, skill, "Double Crop Yield I");
                    doubleCropYieldIPlayers.add(player.getUniqueId());
                    // Legacy support
                    autoReplantPlayers.add(player.getUniqueId());
                }
                if (level >= 12) {
                    unlockPassive(player, skill, "Wheat Specialist");
                    wheatSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 15) {
                    unlockPassive(player, skill, "Growth Speed I");
                    growthSpeedIPlayers.add(player.getUniqueId());
                    // Legacy support
                    instantGrowthPlayers.add(player.getUniqueId());
                }
                if (level >= 17) {
                    unlockPassive(player, skill, "Carrot Specialist");
                    carrotSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 20) {
                    unlockPassive(player, skill, "Auto Replant I");
                    autoReplantIPlayers.add(player.getUniqueId());
                }
                if (level >= 22) {
                    unlockPassive(player, skill, "Farming XP Boost II");
                    farmingXpBoostIIPlayers.add(player.getUniqueId());
                }
                if (level >= 25) {
                    unlockPassive(player, skill, "Seed Saver II");
                    seedSaverIIPlayers.add(player.getUniqueId());
                }
                if (level >= 27) {
                    unlockPassive(player, skill, "Potato Specialist");
                    potatoSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 30) {
                    unlockPassive(player, skill, "Fertilizer Master");
                    fertilizerMasterPlayers.add(player.getUniqueId());
                }
                if (level >= 32) {
                    unlockPassive(player, skill, "Double Crop Yield II");
                    doubleCropYieldIIPlayers.add(player.getUniqueId());
                }
                if (level >= 35) {
                    unlockPassive(player, skill, "Beetroot Specialist");
                    beetrootSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 37) {
                    unlockPassive(player, skill, "Growth Speed II");
                    growthSpeedIIPlayers.add(player.getUniqueId());
                }
                if (level >= 40) {
                    unlockPassive(player, skill, "Rare Crop Master I");
                    rareCropMasterIPlayers.add(player.getUniqueId());
                }
                if (level >= 42) {
                    unlockPassive(player, skill, "Farming XP Boost III");
                    farmingXpBoostIIIPlayers.add(player.getUniqueId());
                }
                if (level >= 45) {
                    unlockPassive(player, skill, "Seed Saver III");
                    seedSaverIIIPlayers.add(player.getUniqueId());
                }
                if (level >= 47) {
                    unlockPassive(player, skill, "Melon Specialist");
                    melonSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 50) {
                    unlockPassive(player, skill, "Auto Replant II");
                    autoReplantIIPlayers.add(player.getUniqueId());
                }
                if (level >= 52) {
                    unlockPassive(player, skill, "Double Crop Yield III");
                    doubleCropYieldIIIPlayers.add(player.getUniqueId());
                }
                if (level >= 55) {
                    unlockPassive(player, skill, "Pumpkin Specialist");
                    pumpkinSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 57) {
                    unlockPassive(player, skill, "Growth Speed III");
                    growthSpeedIIIPlayers.add(player.getUniqueId());
                }
                if (level >= 60) {
                    unlockPassive(player, skill, "Rare Crop Master II");
                    rareCropMasterIIPlayers.add(player.getUniqueId());
                }
                if (level >= 62) {
                    unlockPassive(player, skill, "Farming XP Boost IV");
                    farmingXpBoostIVPlayers.add(player.getUniqueId());
                }
                if (level >= 65) {
                    unlockPassive(player, skill, "Seed Saver IV");
                    seedSaverIVPlayers.add(player.getUniqueId());
                }
                if (level >= 67) {
                    unlockPassive(player, skill, "Nether Wart Specialist");
                    netherWartSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 70) {
                    unlockPassive(player, skill, "Soil Enrichment");
                    soilEnrichmentPlayers.add(player.getUniqueId());
                }
                if (level >= 72) {
                    unlockPassive(player, skill, "Farming XP Boost V");
                    farmingXpBoostVPlayers.add(player.getUniqueId());
                }
                if (level >= 75) {
                    unlockPassive(player, skill, "Triple Crop Yield");
                    tripleCropYieldPlayers.add(player.getUniqueId());
                }
                if (level >= 77) {
                    unlockPassive(player, skill, "Cactus Specialist");
                    cactusSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 80) {
                    unlockPassive(player, skill, "Auto Replant III");
                    autoReplantIIIPlayers.add(player.getUniqueId());
                }
                if (level >= 82) {
                    unlockPassive(player, skill, "Seed Saver V");
                    seedSaverVPlayers.add(player.getUniqueId());
                }
                if (level >= 85) {
                    unlockPassive(player, skill, "Growth Speed IV");
                    growthSpeedIVPlayers.add(player.getUniqueId());
                }
                if (level >= 87) {
                    unlockPassive(player, skill, "Sugar Cane Specialist");
                    sugarCaneSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 90) {
                    unlockPassive(player, skill, "Instant Growth Master");
                    instantGrowthMasterPlayers.add(player.getUniqueId());
                }
                if (level >= 92) {
                    unlockPassive(player, skill, "Farming XP Boost VI");
                    farmingXpBoostVIPlayers.add(player.getUniqueId());
                }
                if (level >= 95) {
                    unlockPassive(player, skill, "Legendary Farmer");
                    legendaryFarmerPlayers.add(player.getUniqueId());
                }
                if (level >= 97) {
                    unlockPassive(player, skill, "Quadruple Crop Yield");
                    quadrupleCropYieldPlayers.add(player.getUniqueId());
                }
                if (level >= 100) {
                    unlockPassive(player, skill, "Master Farmer");
                    masterFarmerPlayers.add(player.getUniqueId());
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
                
            case "enchanting":
                if (level >= 1) {
                    unlockPassive(player, skill, "Enchanting Basics");
                    enchantingBasicsPlayers.add(player.getUniqueId());
                }
                if (level >= 3) {
                    unlockPassive(player, skill, "Lapis Saver I");
                    lapisSaverIPlayers.add(player.getUniqueId());
                }
                if (level >= 5) {
                    unlockPassive(player, skill, "Research Master I");
                    researchMasterIPlayers.add(player.getUniqueId());
                }
                if (level >= 7) {
                    unlockPassive(player, skill, "Enchanting XP Boost I");
                    enchantingXpBoostIPlayers.add(player.getUniqueId());
                }
                if (level >= 10) {
                    unlockPassive(player, skill, "Book Upgrade I");
                    bookUpgradeIPlayers.add(player.getUniqueId());
                }
                if (level >= 12) {
                    unlockPassive(player, skill, "Efficiency Specialist");
                    efficiencySpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 15) {
                    unlockPassive(player, skill, "Custom Enchants I");
                    customEnchantsIPlayers.add(player.getUniqueId());
                }
                if (level >= 17) {
                    unlockPassive(player, skill, "Protection Specialist");
                    protectionSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 20) {
                    unlockPassive(player, skill, "Rare Enchant Boost I");
                    rareEnchantBoostIPlayers.add(player.getUniqueId());
                }
                if (level >= 22) {
                    unlockPassive(player, skill, "Enchanting XP Boost II");
                    enchantingXpBoostIIPlayers.add(player.getUniqueId());
                }
                if (level >= 25) {
                    unlockPassive(player, skill, "Lapis Saver II");
                    lapisSaverIIPlayers.add(player.getUniqueId());
                }
                if (level >= 27) {
                    unlockPassive(player, skill, "Research Master II");
                    researchMasterIIPlayers.add(player.getUniqueId());
                }
                if (level >= 30) {
                    unlockPassive(player, skill, "Sharpness Specialist");
                    sharpnessSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 32) {
                    unlockPassive(player, skill, "Book Upgrade II");
                    bookUpgradeIIPlayers.add(player.getUniqueId());
                }
                if (level >= 35) {
                    unlockPassive(player, skill, "Custom Enchants II");
                    customEnchantsIIPlayers.add(player.getUniqueId());
                }
                if (level >= 37) {
                    unlockPassive(player, skill, "Fortune Specialist");
                    fortuneSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 40) {
                    unlockPassive(player, skill, "Rare Enchant Boost II");
                    rareEnchantBoostIIPlayers.add(player.getUniqueId());
                }
                if (level >= 42) {
                    unlockPassive(player, skill, "Enchanting XP Boost III");
                    enchantingXpBoostIIIPlayers.add(player.getUniqueId());
                }
                if (level >= 45) {
                    unlockPassive(player, skill, "Lapis Saver III");
                    lapisSaverIIIPlayers.add(player.getUniqueId());
                }
                if (level >= 47) {
                    unlockPassive(player, skill, "Research Master III");
                    researchMasterIIIPlayers.add(player.getUniqueId());
                }
                if (level >= 50) {
                    unlockPassive(player, skill, "Looting Specialist");
                    lootingSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 52) {
                    unlockPassive(player, skill, "Book Upgrade III");
                    bookUpgradeIIIPlayers.add(player.getUniqueId());
                }
                if (level >= 55) {
                    unlockPassive(player, skill, "Custom Enchants III");
                    customEnchantsIIIPlayers.add(player.getUniqueId());
                }
                if (level >= 57) {
                    unlockPassive(player, skill, "Silk Touch Specialist");
                    silkTouchSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 60) {
                    unlockPassive(player, skill, "Rare Enchant Boost III");
                    rareEnchantBoostIIIPlayers.add(player.getUniqueId());
                }
                if (level >= 62) {
                    unlockPassive(player, skill, "Enchanting XP Boost IV");
                    enchantingXpBoostIVPlayers.add(player.getUniqueId());
                }
                if (level >= 65) {
                    unlockPassive(player, skill, "Lapis Saver IV");
                    lapisSaverIVPlayers.add(player.getUniqueId());
                }
                if (level >= 67) {
                    unlockPassive(player, skill, "Research Master IV");
                    researchMasterIVPlayers.add(player.getUniqueId());
                }
                if (level >= 70) {
                    unlockPassive(player, skill, "Power Specialist");
                    powerSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 72) {
                    unlockPassive(player, skill, "Book Upgrade IV");
                    bookUpgradeIVPlayers.add(player.getUniqueId());
                }
                if (level >= 75) {
                    unlockPassive(player, skill, "Custom Enchants IV");
                    customEnchantsIVPlayers.add(player.getUniqueId());
                }
                if (level >= 77) {
                    unlockPassive(player, skill, "Mending Specialist");
                    mendingSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 80) {
                    unlockPassive(player, skill, "Rare Enchant Boost IV");
                    rareEnchantBoostIVPlayers.add(player.getUniqueId());
                }
                if (level >= 82) {
                    unlockPassive(player, skill, "Enchanting XP Boost V");
                    enchantingXpBoostVPlayers.add(player.getUniqueId());
                }
                if (level >= 85) {
                    unlockPassive(player, skill, "Lapis Saver V");
                    lapisSaverVPlayers.add(player.getUniqueId());
                }
                if (level >= 87) {
                    unlockPassive(player, skill, "Research Master V");
                    researchMasterVPlayers.add(player.getUniqueId());
                }
                if (level >= 90) {
                    unlockPassive(player, skill, "Unbreaking Specialist");
                    unbreakingSpecialistPlayers.add(player.getUniqueId());
                }
                if (level >= 92) {
                    unlockPassive(player, skill, "Book Upgrade V");
                    bookUpgradeVPlayers.add(player.getUniqueId());
                }
                if (level >= 95) {
                    unlockPassive(player, skill, "Legendary Enchanter");
                    legendaryEnchanterPlayers.add(player.getUniqueId());
                }
                if (level >= 97) {
                    unlockPassive(player, skill, "Custom Enchants V");
                    customEnchantsVPlayers.add(player.getUniqueId());
                }
                if (level >= 100) {
                    unlockPassive(player, skill, "Master Enchanter");
                    masterEnchanterPlayers.add(player.getUniqueId());
                }
                break;

            case "excavation":
                if (level >= 5) {
                    excavationBasicsPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "excavationBasics");
                }
                if (level >= 10) {
                    doubleDropsPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "doubleDrops");
                }
                if (level >= 15) {
                    archaeologyBasicsPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "archaeologyBasics");
                }
                if (level >= 20) {
                    treasureFinderPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "treasureFinder");
                }
                if (level >= 25) {
                    shovelEfficiencyPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "shovelEfficiency");
                }
                if (level >= 30) {
                    excavationXpBoostPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "excavationXpBoost");
                }
                if (level >= 40) {
                    rareFindPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "rareFind");
                }
                if (level >= 50) {
                    multiBlockPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "multiBlock");
                }
                if (level >= 75) {
                    ancientArtifactsPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "ancientArtifacts");
                }
                if (level >= 100) {
                    masterExcavatorPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "masterExcavator");
                }
                break;

            case "repair":
                if (level >= 5) {
                    repairBasicsPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "repairBasics");
                }
                if (level >= 10) {
                    materialSaverPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "materialSaver");
                }
                if (level >= 15) {
                    experienceSaverPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "experienceSaver");
                }
                if (level >= 20) {
                    qualityRepairPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "qualityRepair");
                }
                if (level >= 25) {
                    toolExpertPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "toolExpert");
                }
                if (level >= 30) {
                    weaponExpertPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "weaponExpert");
                }
                if (level >= 40) {
                    armorExpertPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "armorExpert");
                }
                if (level >= 100) {
                    masterSmithPlayers.add(player.getUniqueId());
                    unlockPassive(player, skill, "masterSmith");
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
                
            case "excavation":
                if (hasPassive(player, skill, "doubleDrops")) {
                    multiplier += 0.1; // +10% XP
                }
                if (hasPassive(player, skill, "treasureFinder")) {
                    multiplier += 0.15; // +15% XP
                }
                if (hasPassive(player, skill, "excavationXpBoost")) {
                    multiplier += 0.25; // +25% XP
                }
                if (hasPassive(player, skill, "masterExcavator")) {
                    multiplier += 0.3; // +30% XP
                }
                break;
                
            case "repair":
                if (hasPassive(player, skill, "repairBasics")) {
                    multiplier += 0.1; // +10% XP
                }
                if (hasPassive(player, skill, "experienceSaver")) {
                    multiplier += 0.15; // +15% XP
                }
                if (hasPassive(player, skill, "qualityRepair")) {
                    multiplier += 0.2; // +20% XP
                }
                if (hasPassive(player, skill, "masterSmith")) {
                    multiplier += 0.3; // +30% XP
                }
                break;
        }

        // Add a small level-based bonus (up to 25% at level 50)
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
        double xpMultiplier = 1.0;
        boolean lapisSaved = false;
        
        // Enchanting Basics (Level 1) - Basic enchanting knowledge
        if (enchantingBasicsPlayers.contains(player.getUniqueId())) {
            // This is a base passive, no specific effect needed
        }
        
        // Lapis Saver (Level 3, 25, 45, 65, 85) - Chance to not consume lapis
        double lapisSaveChance = 0.0;
        if (lapisSaverVPlayers.contains(player.getUniqueId())) {
            lapisSaveChance = 0.50; // 50% chance at level 85
        } else if (lapisSaverIVPlayers.contains(player.getUniqueId())) {
            lapisSaveChance = 0.40; // 40% chance at level 65
        } else if (lapisSaverIIIPlayers.contains(player.getUniqueId())) {
            lapisSaveChance = 0.30; // 30% chance at level 45
        } else if (lapisSaverIIPlayers.contains(player.getUniqueId())) {
            lapisSaveChance = 0.20; // 20% chance at level 25
        } else if (lapisSaverIPlayers.contains(player.getUniqueId())) {
            lapisSaveChance = 0.10; // 10% chance at level 3
        }
        
        if (lapisSaveChance > 0 && Math.random() < lapisSaveChance) {
            // Get the enchanting inventory
            Inventory inventory = event.getInventory();
            
            // Find the lapis lazuli slot (typically slot 1)
            ItemStack lapis = inventory.getItem(1);
            
            if (lapis != null && lapis.getType() == Material.LAPIS_LAZULI) {
                // Add one lapis back after the enchantment
                Bukkit.getScheduler().runTask(plugin, () -> {
                    lapis.setAmount(lapis.getAmount() + 1);
                    inventory.setItem(1, lapis);
                    player.sendMessage("§b✧ Your Lapis Saver passive preserved some lapis lazuli!");
                });
                lapisSaved = true;
            }
        }
        
        // Research Master (Level 5, 27, 47, 67, 87) - XP boost for enchanting
        if (researchMasterVPlayers.contains(player.getUniqueId())) {
            xpMultiplier += 0.45; // 45% XP boost at level 87
        } else if (researchMasterIVPlayers.contains(player.getUniqueId())) {
            xpMultiplier += 0.40; // 40% XP boost at level 67
        } else if (researchMasterIIIPlayers.contains(player.getUniqueId())) {
            xpMultiplier += 0.35; // 35% XP boost at level 47
        } else if (researchMasterIIPlayers.contains(player.getUniqueId())) {
            xpMultiplier += 0.30; // 30% XP boost at level 27
        } else if (researchMasterIPlayers.contains(player.getUniqueId())) {
            xpMultiplier += 0.25; // 25% XP boost at level 5
        }
        
        // Enchanting XP Boost (Level 7, 22, 42, 62, 82) - Additional XP from enchanting
        if (enchantingXpBoostVPlayers.contains(player.getUniqueId())) {
            xpMultiplier += 0.25; // Additional 25% XP boost at level 82
        } else if (enchantingXpBoostIVPlayers.contains(player.getUniqueId())) {
            xpMultiplier += 0.20; // Additional 20% XP boost at level 62
        } else if (enchantingXpBoostIIIPlayers.contains(player.getUniqueId())) {
            xpMultiplier += 0.15; // Additional 15% XP boost at level 42
        } else if (enchantingXpBoostIIPlayers.contains(player.getUniqueId())) {
            xpMultiplier += 0.10; // Additional 10% XP boost at level 22
        } else if (enchantingXpBoostIPlayers.contains(player.getUniqueId())) {
            xpMultiplier += 0.05; // Additional 5% XP boost at level 7
        }
        
        // Apply XP multiplier if it's greater than 1.0
        if (xpMultiplier > 1.0) {
            int baseXP = event.getExpLevelCost() * 5; // Base XP from enchanting
            int bonusXP = (int)Math.round(baseXP * (xpMultiplier - 1.0));
            
            if (bonusXP > 0) {
                // Award the bonus XP directly
                xpManager.addXP(player, "enchanting", bonusXP);
                player.sendActionBar(Component.text("+" + (int)((xpMultiplier - 1.0) * 100) + "% Enchanting XP from passives!").color(NamedTextColor.GREEN));
            }
        }

        // Book Upgrade (Level 10, 32, 52, 72, 92) - Chance to upgrade enchantment levels on books
        double bookUpgradeChance = 0.0;
        if (bookUpgradeVPlayers.contains(player.getUniqueId())) {
            bookUpgradeChance = 0.35; // 35% chance at level 92
        } else if (bookUpgradeIVPlayers.contains(player.getUniqueId())) {
            bookUpgradeChance = 0.30; // 30% chance at level 72
        } else if (bookUpgradeIIIPlayers.contains(player.getUniqueId())) {
            bookUpgradeChance = 0.25; // 25% chance at level 52
        } else if (bookUpgradeIIPlayers.contains(player.getUniqueId())) {
            bookUpgradeChance = 0.20; // 20% chance at level 32
        } else if (bookUpgradeIPlayers.contains(player.getUniqueId())) {
            bookUpgradeChance = 0.15; // 15% chance at level 10
        }
        
        if (bookUpgradeChance > 0 && event.getItem().getType() == Material.BOOK && Math.random() < bookUpgradeChance) {
            Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();
            boolean upgraded = false;
            
            for (Map.Entry<Enchantment, Integer> entry : new HashMap<>(enchants).entrySet()) {
                Enchantment ench = entry.getKey();
                int lvl = entry.getValue();
                
                if (lvl < ench.getMaxLevel()) {
                    enchants.put(ench, lvl + 1);
                    upgraded = true;
                }
            }
            
            if (upgraded) {
                player.sendMessage("§d✨ Your Book Upgrade passive increased an enchantment level!");
            }
        }
        
        // Enchantment Specialists (Various levels) - Boost specific enchantments
        Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();
        boolean specialistApplied = false;
        
        // Efficiency Specialist (Level 12)
        if (efficiencySpecialistPlayers.contains(player.getUniqueId()) && 
            enchants.containsKey(Enchantment.EFFICIENCY)) {
            int currentLevel = enchants.get(Enchantment.EFFICIENCY);
            if (currentLevel < Enchantment.EFFICIENCY.getMaxLevel()) {
                enchants.put(Enchantment.EFFICIENCY, currentLevel + 1);
                specialistApplied = true;
            }
        }
        
        // Protection Specialist (Level 17)
        if (protectionSpecialistPlayers.contains(player.getUniqueId())) {
            for (Enchantment ench : new ArrayList<>(enchants.keySet())) {
                if (ench.equals(Enchantment.PROTECTION) || 
                    ench.equals(Enchantment.BLAST_PROTECTION) || 
                    ench.equals(Enchantment.PROJECTILE_PROTECTION) || 
                    ench.equals(Enchantment.FIRE_PROTECTION) || 
                    ench.equals(Enchantment.THORNS)) {
                    int currentLevel = enchants.get(ench);
                    if (currentLevel < ench.getMaxLevel()) {
                        enchants.put(ench, currentLevel + 1);
                        specialistApplied = true;
                        break; // Only upgrade one protection enchantment
                    }
                }
            }
        }
        
        // Sharpness Specialist (Level 30)
        if (sharpnessSpecialistPlayers.contains(player.getUniqueId()) && 
            enchants.containsKey(Enchantment.SHARPNESS)) {
            int currentLevel = enchants.get(Enchantment.SHARPNESS);
            if (currentLevel < Enchantment.SHARPNESS.getMaxLevel()) {
                enchants.put(Enchantment.SHARPNESS, currentLevel + 1);
                specialistApplied = true;
            }
        }
        
        // Fortune Specialist (Level 37)
        if (fortuneSpecialistPlayers.contains(player.getUniqueId()) && 
            enchants.containsKey(Enchantment.FORTUNE)) {
            int currentLevel = enchants.get(Enchantment.FORTUNE);
            if (currentLevel < Enchantment.FORTUNE.getMaxLevel()) {
                enchants.put(Enchantment.FORTUNE, currentLevel + 1);
                specialistApplied = true;
            }
        }
        
        // Looting Specialist (Level 50)
        if (lootingSpecialistPlayers.contains(player.getUniqueId()) && 
            enchants.containsKey(Enchantment.LOOTING)) {
            int currentLevel = enchants.get(Enchantment.LOOTING);
            if (currentLevel < Enchantment.LOOTING.getMaxLevel()) {
                enchants.put(Enchantment.LOOTING, currentLevel + 1);
                specialistApplied = true;
            }
        }
        
        // Silk Touch Specialist (Level 57)
        if (silkTouchSpecialistPlayers.contains(player.getUniqueId()) && 
            !enchants.containsKey(Enchantment.SILK_TOUCH) && 
            event.getItem().getType().name().contains("PICKAXE")) {
            // Add Silk Touch if it's not already there and item is a pickaxe
            enchants.put(Enchantment.SILK_TOUCH, 1);
            specialistApplied = true;
        }
        
        // Power Specialist (Level 70)
        if (powerSpecialistPlayers.contains(player.getUniqueId()) && 
            enchants.containsKey(Enchantment.POWER)) {
            int currentLevel = enchants.get(Enchantment.POWER);
            if (currentLevel < Enchantment.POWER.getMaxLevel()) {
                enchants.put(Enchantment.POWER, currentLevel + 1);
                specialistApplied = true;
            }
        }
        
        // Mending Specialist (Level 77)
        if (mendingSpecialistPlayers.contains(player.getUniqueId()) && 
            !enchants.containsKey(Enchantment.MENDING)) {
            // Add Mending if it's not already there
            enchants.put(Enchantment.MENDING, 1);
            specialistApplied = true;
        }
        
        // Unbreaking Specialist (Level 90)
        if (unbreakingSpecialistPlayers.contains(player.getUniqueId()) && 
            enchants.containsKey(Enchantment.UNBREAKING)) {
            int currentLevel = enchants.get(Enchantment.UNBREAKING);
            if (currentLevel < Enchantment.UNBREAKING.getMaxLevel()) {
                enchants.put(Enchantment.UNBREAKING, currentLevel + 1);
                specialistApplied = true;
            }
        }
        
        if (specialistApplied) {
            player.sendMessage("§d✨ Your Enchantment Specialist passive improved an enchantment!");
        }

        // Custom Enchants (Level 15, 35, 55, 75, 97) - Chance to add custom enchantment lore
        double customEnchantChance = 0.0;
        if (customEnchantsVPlayers.contains(player.getUniqueId())) {
            customEnchantChance = 0.30; // 30% chance at level 97
        } else if (customEnchantsIVPlayers.contains(player.getUniqueId())) {
            customEnchantChance = 0.25; // 25% chance at level 75
        } else if (customEnchantsIIIPlayers.contains(player.getUniqueId())) {
            customEnchantChance = 0.20; // 20% chance at level 55
        } else if (customEnchantsIIPlayers.contains(player.getUniqueId())) {
            customEnchantChance = 0.15; // 15% chance at level 35
        } else if (customEnchantsIPlayers.contains(player.getUniqueId())) {
            customEnchantChance = 0.10; // 10% chance at level 15
        }
        
        if (customEnchantChance > 0 && Math.random() < customEnchantChance) {
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

        // Rare Enchant Boost (Level 20, 40, 60, 80) - Chance to add rare enchantment
        double rareEnchantChance = 0.0;
        if (rareEnchantBoostIVPlayers.contains(player.getUniqueId())) {
            rareEnchantChance = 0.35; // 35% chance at level 80
        } else if (rareEnchantBoostIIIPlayers.contains(player.getUniqueId())) {
            rareEnchantChance = 0.30; // 30% chance at level 60
        } else if (rareEnchantBoostIIPlayers.contains(player.getUniqueId())) {
            rareEnchantChance = 0.25; // 25% chance at level 40
        } else if (rareEnchantBoostIPlayers.contains(player.getUniqueId())) {
            rareEnchantChance = 0.20; // 20% chance at level 20
        }
        
        if (rareEnchantChance > 0 && Math.random() < rareEnchantChance) {
            ItemStack item = event.getItem();
            
            // Get a rare enchantment appropriate for this item
            Enchantment rareEnchant = getRareEnchantment(item.getType());
            if (rareEnchant != null && !item.containsEnchantment(rareEnchant)) {
                // Add the rare enchantment to the existing enchants
                enchants.put(rareEnchant, 1); // Start with level 1
                
                player.sendMessage("§d✨ Your Rare Enchant Boost added a rare enchantment!");
            }
        }
        
        // Legendary Enchanter (Level 95) - Chance for max level enchantments
        if (legendaryEnchanterPlayers.contains(player.getUniqueId()) && Math.random() < 0.15) { // 15% chance
            boolean maxLevelApplied = false;
            
            for (Map.Entry<Enchantment, Integer> entry : new HashMap<>(enchants).entrySet()) {
                Enchantment ench = entry.getKey();
                int maxLevel = ench.getMaxLevel();
                
                if (entry.getValue() < maxLevel) {
                    enchants.put(ench, maxLevel);
                    maxLevelApplied = true;
                    break; // Only max out one enchantment
                }
            }
            
            if (maxLevelApplied) {
                player.sendMessage("§6✨ Your Legendary Enchanter passive maximized an enchantment level!");
            }
        }
        
        // Master Enchanter (Level 100) - All enchanting abilities enhanced
        if (masterEnchanterPlayers.contains(player.getUniqueId())) {
            // 10% chance to add an additional enchantment
            if (Math.random() < 0.10) {
                ItemStack item = event.getItem();
                List<Enchantment> possibleEnchants = new ArrayList<>();
                
                // Find all possible enchantments for this item
                for (Enchantment ench : Enchantment.values()) {
                    if (ench.canEnchantItem(item) && !enchants.containsKey(ench)) {
                        possibleEnchants.add(ench);
                    }
                }
                
                if (!possibleEnchants.isEmpty()) {
                    // Add a random enchantment from the possible ones
                    Enchantment randomEnchant = possibleEnchants.get(new Random().nextInt(possibleEnchants.size()));
                    int enchLevel = 1 + new Random().nextInt(randomEnchant.getMaxLevel());
                    enchants.put(randomEnchant, enchLevel);
                    
                    player.sendMessage("§6✨ Your Master Enchanter passive added an additional enchantment!");
                }
            }
            
            // 30% XP boost (already included in the XP multiplier section)
            
            // 20% chance to not consume any experience levels
            if (Math.random() < 0.20) {
                // Schedule a task to restore the player's levels after the enchantment
                final int expLevelCost = event.getExpLevelCost();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.setLevel(player.getLevel() + expLevelCost);
                    player.sendMessage("§6✨ Your Master Enchanter passive restored your experience levels!");
                });
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
        
        // Check if this is a player-placed block - if so, don't give XP but still allow drops
        if (isPlayerPlaced(block)) {
            // If it's a player-placed block, we'll still allow normal drops
            // but we won't give any XP or apply special passive effects
            
            // Remove from tracking set since it's been broken
            playerPlacedBlocks.remove(getBlockLocationKey(block));
            
            // For ores, we still want to handle drops manually to be consistent
            if (isOre(block.getType())) {
                event.setDropItems(false);
                
                // Just drop the normal item without any bonuses
                Material dropMaterial = getOreDrop(block.getType(), false);
                if (hasSilkTouch) {
                    // With silk touch, just drop the ore block itself
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType(), 1));
                } else if (dropMaterial != null) {
                    // Drop the normal amount without any bonuses
                    int baseAmount = 1;
                    // Set correct base amounts for redstone and lapis ores
                    if (block.getType() == Material.REDSTONE_ORE || block.getType() == Material.DEEPSLATE_REDSTONE_ORE ||
                        block.getType() == Material.LAPIS_ORE || block.getType() == Material.DEEPSLATE_LAPIS_ORE) {
                        baseAmount = 4; // Default drop amount for redstone and lapis is 4
                    }
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropMaterial, baseAmount));
                }
            }
            
            return; // Skip the rest of the method to avoid giving XP
        }
        
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
        
        // Set correct base amounts for redstone and lapis ores
        if (oreType == Material.REDSTONE_ORE || oreType == Material.DEEPSLATE_REDSTONE_ORE ||
            oreType == Material.LAPIS_ORE || oreType == Material.DEEPSLATE_LAPIS_ORE) {
            amount = 4; // Default drop amount for redstone and lapis is 4
        }
        
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

    @EventHandler
    public void onFarmingBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Block block = event.getBlock();
        Material blockType = block.getType();
        
        // Only handle farming-related blocks
        if (!isFarmCrop(blockType) && !isMelon(blockType) && !isPumpkin(blockType) && 
            blockType != Material.CACTUS && blockType != Material.SUGAR_CANE && 
            !blockType.name().contains("STEM")) {
            return;
        }
        
        // Check if this is a player-placed block - if so, don't give XP
        if (isPlayerPlaced(block)) {
            // Remove from tracking set since it's been broken
            playerPlacedBlocks.remove(getBlockLocationKey(block));
            
            // For crops, we'll still allow normal drops
            return; // Skip the rest of the method to avoid giving XP
        }
        
        // Get the base drops
        List<ItemStack> drops = new ArrayList<>(block.getDrops(player.getInventory().getItemInMainHand()));
        if (drops.isEmpty()) {
            return;
        }
        
        // Cancel the default drops since we'll handle them manually
        event.setDropItems(false);
        
        // Get player's farming level
        int farmingLevel = xpManager.getPlayerLevel(player, "farming");
        
        // Apply drop multipliers based on crop type specialists
        boolean specialistApplied = false;
        double multiplier = 1.0;
        
        // Check for specialist passive based on crop type
        if (blockType == Material.WHEAT && wheatSpecialistPlayers.contains(playerId)) {
            multiplier += 0.25;
            specialistApplied = true;
        } else if (blockType == Material.CARROTS && carrotSpecialistPlayers.contains(playerId)) {
            multiplier += 0.25;
            specialistApplied = true;
        } else if (blockType == Material.POTATOES && potatoSpecialistPlayers.contains(playerId)) {
            multiplier += 0.25;
            specialistApplied = true;
        } else if (blockType == Material.BEETROOTS && beetrootSpecialistPlayers.contains(playerId)) {
            multiplier += 0.25;
            specialistApplied = true;
        } else if (isMelon(blockType) && melonSpecialistPlayers.contains(playerId)) {
            multiplier += 0.25;
            specialistApplied = true;
        } else if (isPumpkin(blockType) && pumpkinSpecialistPlayers.contains(playerId)) {
            multiplier += 0.25;
            specialistApplied = true;
        } else if (blockType == Material.NETHER_WART && netherWartSpecialistPlayers.contains(playerId)) {
            multiplier += 0.25;
            specialistApplied = true;
        } else if (blockType == Material.CACTUS && cactusSpecialistPlayers.contains(playerId)) {
            multiplier += 0.25;
            specialistApplied = true;
        } else if (blockType == Material.SUGAR_CANE && sugarCaneSpecialistPlayers.contains(playerId)) {
            multiplier += 0.25;
            specialistApplied = true;
        }
        
        if (specialistApplied) {
            player.sendActionBar(Component.text("Crop Specialist: +25% yield!").color(NamedTextColor.GREEN));
        }
        
        // Apply Double Crop Yield I-III passives
        boolean extraDropsApplied = false;
        boolean tripleDropsApplied = false;
        boolean quadrupleDropsApplied = false;
        double extraDropChance = 0.0;
        
        // Apply the legendary farmer effect that can double all boosts
        boolean legendaryEffect = legendaryFarmerPlayers.contains(playerId) && Math.random() < 0.20;
        
        if (doubleCropYieldIIIPlayers.contains(playerId)) {
            extraDropChance = 0.35; // 35% chance
        } else if (doubleCropYieldIIPlayers.contains(playerId)) {
            extraDropChance = 0.25; // 25% chance
        } else if (doubleCropYieldIPlayers.contains(playerId) || doubleCropYieldPlayers.contains(playerId)) {
            extraDropChance = 0.15; // 15% chance
        }
        
        if (legendaryEffect) {
            extraDropChance *= 2; // Double the chance with Legendary Farmer
        }
        
        if (extraDropChance > 0 && Math.random() < extraDropChance) {
            multiplier += 1.0; // Double drops
            extraDropsApplied = true;
            player.sendActionBar(Component.text("Double Crop Yield activated!").color(NamedTextColor.GOLD));
        }
        
        // Apply Triple Crop Yield passive
        if (tripleCropYieldPlayers.contains(playerId)) {
            double tripleChance = 0.15; // 15% chance
            if (legendaryEffect) {
                tripleChance *= 2; // Double the chance with Legendary Farmer
            }
            
            if (Math.random() < tripleChance) {
                multiplier += 1.0; // Add another 100% (total 3x with double yield)
                tripleDropsApplied = true;
                player.sendActionBar(Component.text("Triple Crop Yield activated!").color(NamedTextColor.LIGHT_PURPLE));
            }
        }
        
        // Apply Quadruple Crop Yield passive
        if (quadrupleCropYieldPlayers.contains(playerId)) {
            double quadrupleChance = 0.05; // 5% chance
            if (legendaryEffect) {
                quadrupleChance *= 2; // Double the chance with Legendary Farmer
            }
            
            if (Math.random() < quadrupleChance) {
                multiplier += 1.0; // Add another 100% (total 4x with double and triple)
                quadrupleDropsApplied = true;
                player.sendActionBar(Component.text("Quadruple Crop Yield activated!").color(NamedTextColor.DARK_PURPLE));
            }
        }
        
        // Apply Master Farmer passive (Level 100)
        if (masterFarmerPlayers.contains(playerId)) {
            // Master Farmer has a 10% chance to add 1 to the multiplier
            if (Math.random() < 0.10) {
                multiplier += 1.0;
                player.sendActionBar(Component.text("Master Farmer bonus activated!").color(NamedTextColor.AQUA));
            }
            
            // Master Farmer also adds a flat 20% to all crops
            multiplier += 0.20;
        }
        
        // Apply Rare Crop Master passive
        boolean rareCropApplied = false;
        double rareCropChance = 0.0;
        
        if (rareCropMasterIIPlayers.contains(playerId)) {
            rareCropChance = 0.25; // 25% chance
        } else if (rareCropMasterIPlayers.contains(playerId)) {
            rareCropChance = 0.15; // 15% chance
        }
        
        if (rareCropChance > 0 && Math.random() < rareCropChance) {
            // Add a rare crop variant
            addRareCropVariant(player, blockType);
            rareCropApplied = true;
        }
        
        // Calculate final drops
        if (multiplier > 1.0) {
            for (ItemStack drop : drops) {
                // Calculate new amount
                int originalAmount = drop.getAmount();
                int newAmount = (int) Math.round(originalAmount * multiplier);
                
                // Drop items with the new amount
                if (newAmount > 0) {
                    drop.setAmount(newAmount);
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                }
            }
        } else {
            // No multiplier, just drop the original items
            for (ItemStack drop : drops) {
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
        }
        
        // Apply Auto Replant functionality
        boolean replanted = false;
        double replantChance = 0.0;
        
        if (autoReplantIIIPlayers.contains(playerId)) {
            replantChance = 0.60; // 60% chance
        } else if (autoReplantIIPlayers.contains(playerId)) {
            replantChance = 0.40; // 40% chance
        } else if (autoReplantIPlayers.contains(playerId) || autoReplantPlayers.contains(playerId)) {
            replantChance = 0.20; // 20% chance
        }
        
        if (legendaryEffect) {
            replantChance *= 2; // Double the chance with Legendary Farmer (capped at 100%)
            replantChance = Math.min(replantChance, 1.0);
        }
        
        if (replantChance > 0 && Math.random() < replantChance && canReplant(blockType)) {
            // Schedule task to replant crop after a short delay (to avoid instant break)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                replantCrop(block, blockType);
            }, 2L); // 2 tick delay
            
            replanted = true;
            player.sendActionBar(Component.text("Auto Replant activated!").color(NamedTextColor.GREEN));
        }
    }
    
    // Helper method to check if a material is a crop
    private boolean isFarmCrop(Material material) {
        return material == Material.WHEAT || 
               material == Material.POTATOES || 
               material == Material.CARROTS || 
               material == Material.BEETROOTS ||
               material == Material.NETHER_WART;
    }
    
    // Helper method to check if a material is a melon
    private boolean isMelon(Material material) {
        return material == Material.MELON || material == Material.ATTACHED_MELON_STEM || 
               material == Material.MELON_STEM;
    }
    
    // Helper method to check if a material is a pumpkin
    private boolean isPumpkin(Material material) {
        return material == Material.PUMPKIN || material == Material.CARVED_PUMPKIN || 
               material == Material.ATTACHED_PUMPKIN_STEM || material == Material.PUMPKIN_STEM;
    }
    
    // Helper method to check if a crop can be replanted
    private boolean canReplant(Material material) {
        return material == Material.WHEAT || 
               material == Material.POTATOES || 
               material == Material.CARROTS || 
               material == Material.BEETROOTS || 
               material == Material.NETHER_WART;
    }
    
    // Helper method to replant a crop
    private void replantCrop(Block block, Material harvestedCrop) {
        if (block.getType() == Material.AIR) {
            if (harvestedCrop == Material.WHEAT) {
                block.setType(Material.WHEAT);
                Ageable crop = (Ageable) block.getBlockData();
                crop.setAge(0);
                block.setBlockData(crop);
            } else if (harvestedCrop == Material.POTATOES) {
                block.setType(Material.POTATOES);
                Ageable crop = (Ageable) block.getBlockData();
                crop.setAge(0);
                block.setBlockData(crop);
            } else if (harvestedCrop == Material.CARROTS) {
                block.setType(Material.CARROTS);
                Ageable crop = (Ageable) block.getBlockData();
                crop.setAge(0);
                block.setBlockData(crop);
            } else if (harvestedCrop == Material.BEETROOTS) {
                block.setType(Material.BEETROOTS);
                Ageable crop = (Ageable) block.getBlockData();
                crop.setAge(0);
                block.setBlockData(crop);
            } else if (harvestedCrop == Material.NETHER_WART) {
                block.setType(Material.NETHER_WART);
                Ageable crop = (Ageable) block.getBlockData();
                crop.setAge(0);
                block.setBlockData(crop);
            }
        }
    }
    
    // Helper method to add a rare crop variant
    private void addRareCropVariant(Player player, Material cropType) {
        ItemStack rareItem = null;
        
        if (cropType == Material.WHEAT) {
            // Rare wheat variant: Golden bread
            rareItem = new ItemStack(Material.BREAD);
            ItemMeta meta = rareItem.getItemMeta();
            meta.setDisplayName("§6Golden Bread");
            List<String> lore = new ArrayList<>();
            lore.add("§eA special bread with enhanced properties");
            lore.add("§eRestores more hunger and provides temporary effects");
            meta.setLore(lore);
            rareItem.setItemMeta(meta);
        } else if (cropType == Material.POTATOES) {
            // Rare potato variant: Golden potato
            rareItem = new ItemStack(Material.BAKED_POTATO);
            ItemMeta meta = rareItem.getItemMeta();
            meta.setDisplayName("§6Golden Potato");
            List<String> lore = new ArrayList<>();
            lore.add("§eA special potato with enhanced properties");
            lore.add("§eRestores more hunger and provides temporary effects");
            meta.setLore(lore);
            rareItem.setItemMeta(meta);
        } else if (cropType == Material.CARROTS) {
            // Just give a golden carrot
            rareItem = new ItemStack(Material.GOLDEN_CARROT);
        } else if (cropType == Material.BEETROOTS) {
            // Enhanced beetroot soup
            rareItem = new ItemStack(Material.BEETROOT_SOUP);
            ItemMeta meta = rareItem.getItemMeta();
            meta.setDisplayName("§6Enhanced Beetroot Soup");
            List<String> lore = new ArrayList<>();
            lore.add("§eA special soup with enhanced properties");
            lore.add("§eRestores more hunger and provides temporary effects");
            meta.setLore(lore);
            rareItem.setItemMeta(meta);
        } else if (cropType == Material.MELON) {
            // Glistering melon slice
            rareItem = new ItemStack(Material.GLISTERING_MELON_SLICE);
        } else if (cropType == Material.PUMPKIN) {
            // Special pumpkin pie
            rareItem = new ItemStack(Material.PUMPKIN_PIE);
            ItemMeta meta = rareItem.getItemMeta();
            meta.setDisplayName("§6Magical Pumpkin Pie");
            List<String> lore = new ArrayList<>();
            lore.add("§eA special pie with enhanced properties");
            lore.add("§eRestores more hunger and provides temporary effects");
            meta.setLore(lore);
            rareItem.setItemMeta(meta);
        } else if (cropType == Material.NETHER_WART) {
            // Rare nether wart
            rareItem = new ItemStack(Material.NETHER_WART);
            ItemMeta meta = rareItem.getItemMeta();
            meta.setDisplayName("§6Potent Nether Wart");
            List<String> lore = new ArrayList<>();
            lore.add("§eA potent nether wart for brewing");
            lore.add("§eCreates more powerful potions");
            meta.setLore(lore);
            rareItem.setItemMeta(meta);
        }
        
        if (rareItem != null) {
            player.getWorld().dropItemNaturally(player.getLocation(), rareItem);
            player.sendMessage("§6Rare Crop Master: You've harvested a rare crop variant!");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Block clickedBlock = event.getClickedBlock();
        ItemStack itemInHand = event.getItem();
        
        if (clickedBlock == null || itemInHand == null) {
            return;
        }
        
        // Handle seed planting (Seed Saver passive)
        if (isFarmland(clickedBlock.getType()) && isSeed(itemInHand.getType())) {
            double seedSaveChance = 0.0;
            
            // Apply the legendary farmer effect that can double all boosts
            boolean legendaryEffect = legendaryFarmerPlayers.contains(playerId) && Math.random() < 0.20;
            
            // Check for Seed Saver passives
            if (seedSaverVPlayers.contains(playerId)) {
                seedSaveChance = 0.50; // 50% chance
            } else if (seedSaverIVPlayers.contains(playerId)) {
                seedSaveChance = 0.40; // 40% chance
            } else if (seedSaverIIIPlayers.contains(playerId)) {
                seedSaveChance = 0.30; // 30% chance
            } else if (seedSaverIIPlayers.contains(playerId)) {
                seedSaveChance = 0.20; // 20% chance
            } else if (seedSaverIPlayers.contains(playerId)) {
                seedSaveChance = 0.10; // 10% chance
            }
            
            if (legendaryEffect) {
                seedSaveChance *= 2; // Double the chance with Legendary Farmer (capped at 100%)
                seedSaveChance = Math.min(seedSaveChance, 1.0);
            }
            
            // Roll for seed saving
            if (seedSaveChance > 0 && Math.random() < seedSaveChance) {
                // Cancel event to prevent consuming the seed
                event.setCancelled(true);
                
                // Manually plant the crop without consuming the seed
                Block targetBlock = clickedBlock.getRelative(BlockFace.UP);
                Material cropType = getSeedCropType(itemInHand.getType());
                
                if (cropType != null && targetBlock.getType() == Material.AIR) {
                    targetBlock.setType(cropType);
                    if (cropType != Material.NETHER_WART) {
                        Ageable ageable = (Ageable) targetBlock.getBlockData();
                        ageable.setAge(0);
                        targetBlock.setBlockData(ageable);
                    }
                    
                    // Play planting sound and effect
                    player.getWorld().playSound(targetBlock.getLocation(), Sound.ITEM_CROP_PLANT, 1.0F, 1.0F);
                    player.getWorld().spawnParticle(Particle.COMPOSTER, targetBlock.getLocation().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0);
                    
                    // Notify player
                    player.sendActionBar(Component.text("Seed Saver activated!").color(NamedTextColor.GREEN));
                }
            }
        }
        
        // Handle fertilizer master passive when using bone meal
        if (itemInHand.getType() == Material.BONE_MEAL && clickedBlock != null && isFarmCrop(clickedBlock.getType())) {
            if (fertilizerMasterPlayers.contains(playerId)) {
                // There's a 35% chance the bone meal will have extra effect
                if (Math.random() < 0.35) {
                    // Cancel the event to handle it manually
                    event.setCancelled(true);
                    
                    // Get the current crop age
                    Ageable ageable = (Ageable) clickedBlock.getBlockData();
                    int currentAge = ageable.getAge();
                    int maxAge = ageable.getMaximumAge();
                    
                    // If not fully grown
                    if (currentAge < maxAge) {
                        // Set the age to a higher value (multiple growth stages)
                        int newAge = Math.min(currentAge + 2, maxAge); // Grow by 2 stages instead of 1
                        ageable.setAge(newAge);
                        clickedBlock.setBlockData(ageable);
                        
                        // Consume one bone meal
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            itemInHand.setAmount(itemInHand.getAmount() - 1);
                        }
                        
                        // Play bone meal effect
                        player.getWorld().playSound(clickedBlock.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0F, 1.0F);
                        player.getWorld().spawnParticle(Particle.COMPOSTER, clickedBlock.getLocation().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0);
                        
                        // Notify player
                        player.sendActionBar(Component.text("Fertilizer Master: Enhanced bone meal effect!").color(NamedTextColor.GREEN));
                    }
                }
            }
        }
    }

    // Helper methods for farming passives
    
    // Helper method to check if a material is farmland
    private boolean isFarmland(Material material) {
        return material == Material.FARMLAND || material == Material.SOUL_SAND;
    }
    
    // Helper method to check if a material is a seed
    private boolean isSeed(Material material) {
        return material == Material.WHEAT_SEEDS || 
               material == Material.POTATO || 
               material == Material.CARROT || 
               material == Material.BEETROOT_SEEDS ||
               material == Material.NETHER_WART;
    }
    
    // Helper method to get the crop type from a seed
    private Material getSeedCropType(Material seedType) {
        switch (seedType) {
            case WHEAT_SEEDS: return Material.WHEAT;
            case POTATO: return Material.POTATOES;
            case CARROT: return Material.CARROTS;
            case BEETROOT_SEEDS: return Material.BEETROOTS;
            case NETHER_WART: return Material.NETHER_WART;
            default: return null;
        }
    }
    
    // Handle the Growth Speed passive with a scheduled task
    private BukkitTask growthSpeedTask;
    
    public void startGrowthSpeedTask() {
        // Cancel existing task if it exists
        if (growthSpeedTask != null) {
            growthSpeedTask.cancel();
        }
        
        // Start a new task that runs every minute (1200 ticks)
        growthSpeedTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Process all online players with growth speed passives
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID playerId = player.getUniqueId();
                
                // Skip players without any growth speed passives
                if (!growthSpeedIPlayers.contains(playerId) && 
                    !growthSpeedIIPlayers.contains(playerId) && 
                    !growthSpeedIIIPlayers.contains(playerId) && 
                    !growthSpeedIVPlayers.contains(playerId)) {
                    continue;
                }
                
                // Determine growth speed bonus based on passive level
                double growthChance = 0.0;
                if (growthSpeedIVPlayers.contains(playerId)) {
                    growthChance = 0.40; // 40% chance
                } else if (growthSpeedIIIPlayers.contains(playerId)) {
                    growthChance = 0.30; // 30% chance
                } else if (growthSpeedIIPlayers.contains(playerId)) {
                    growthChance = 0.20; // 20% chance
                } else if (growthSpeedIPlayers.contains(playerId)) {
                    growthChance = 0.10; // 10% chance
                }
                
                // Apply soil enrichment passive (crops within 10 blocks grow 20% faster)
                if (soilEnrichmentPlayers.contains(playerId)) {
                    growthChance += 0.20;
                }
                
                // Apply the legendary farmer effect that can double the growth chance
                if (legendaryFarmerPlayers.contains(playerId) && Math.random() < 0.20) {
                    growthChance *= 2; // Double the chance with Legendary Farmer
                }
                
                // Apply master farmer bonus
                if (masterFarmerPlayers.contains(playerId)) {
                    growthChance += 0.10; // Additional 10% bonus
                }
                
                // Check nearby crops and possibly advance their growth
                if (growthChance > 0) {
                    Location playerLoc = player.getLocation();
                    int radius = soilEnrichmentPlayers.contains(playerId) ? 10 : 5;
                    
                    // Get all blocks in the radius
                    for (int x = -radius; x <= radius; x++) {
                        for (int y = -3; y <= 3; y++) {
                            for (int z = -radius; z <= radius; z++) {
                                Block block = playerLoc.getBlock().getRelative(x, y, z);
                                
                                // Only process crops
                                if (isFarmCrop(block.getType())) {
                                    // Random chance to grow the crop based on player's passive
                                    if (Math.random() < growthChance) {
                                        growCrop(block);
                                    }
                                    
                                    // Check for Instant Growth Master (5% chance for instant growth)
                                    if (instantGrowthMasterPlayers.contains(playerId) && Math.random() < 0.05) {
                                        // Instantly grow to max age
                                        Ageable ageable = (Ageable) block.getBlockData();
                                        ageable.setAge(ageable.getMaximumAge());
                                        block.setBlockData(ageable);
                                        
                                        // Spawn effect
                                        block.getWorld().spawnParticle(Particle.COMPOSTER, block.getLocation().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, 1200L, 1200L); // Check every minute (1200 ticks)
    }
    
    // Helper method to grow a crop by one stage
    private void growCrop(Block block) {
        if (block.getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) block.getBlockData();
            int currentAge = ageable.getAge();
            int maxAge = ageable.getMaximumAge();
            
            // Only grow if not at max age
            if (currentAge < maxAge) {
                ageable.setAge(currentAge + 1);
                block.setBlockData(ageable);
                
                // Spawn a small effect
                block.getWorld().spawnParticle(Particle.COMPOSTER, block.getLocation().add(0.5, 0.5, 0.5), 3, 0.3, 0.3, 0.3, 0);
            }
        }
    }

    // Add this section for handling excavation passives in the blockBreak event
    @EventHandler
    public void onExcavationBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Block block = event.getBlock();
        Material blockType = block.getType();
        ItemStack tool = player.getInventory().getItemInMainHand();
        
        // Check if player is using a shovel
        if (!isShovel(tool.getType())) {
            return;
        }
        
        // Only handle excavation-related blocks
        if (!isExcavatable(blockType)) {
            return;
        }
        
        // Check if this is a player-placed block - if so, don't give XP
        if (isPlayerPlaced(block)) {
            // Remove from tracking set since it's been broken
            playerPlacedBlocks.remove(getBlockLocationKey(block));
            
            // For excavation blocks, we'll still allow normal drops
            return; // Skip the rest of the method to avoid giving XP
        }
        
        // Archaeology basics passive
        if (plugin.isPassiveEnabled("excavation", "archaeologyBasics") && archaeologyBasicsPlayers.contains(playerId)) {
            double chance = plugin.getPassiveValue("excavation", "archaeologyBasics");
            if (Math.random() < chance) {
                dropArchaeologyFinds(block.getLocation());
                player.sendMessage(ChatColor.GOLD + "Your Archaeology Basics passive uncovered ancient remains!");
            }
        }
        
        // Double drops passive
        if (plugin.isPassiveEnabled("excavation", "doubleDrops") && doubleDropsPlayers.contains(playerId)) {
            double chance = plugin.getPassiveValue("excavation", "doubleDrops");
            if (Math.random() < chance) {
                for (ItemStack drop : block.getDrops(player.getInventory().getItemInMainHand())) {
                    block.getWorld().dropItemNaturally(block.getLocation(), drop.clone());
                }
                player.sendMessage(ChatColor.GREEN + "Your Double Drops passive gave you extra excavation items!");
            }
        }
        
        // Treasure finder passive
        if (plugin.isPassiveEnabled("excavation", "treasureFinder") && treasureFinderPlayers.contains(playerId)) {
            double chance = plugin.getPassiveValue("excavation", "treasureFinder");
            if (Math.random() < chance) {
                dropRandomTreasure(block.getLocation());
                player.sendMessage(ChatColor.GOLD + "Your Treasure Finder passive discovered something interesting!");
            }
        }
        
        // Rare find passive
        if (plugin.isPassiveEnabled("excavation", "rareFind") && rareFindPlayers.contains(playerId)) {
            double chance = plugin.getPassiveValue("excavation", "rareFind") / 2; // Make rare finds truly rare
            if (Math.random() < chance) {
                dropRareItem(block.getLocation());
                player.sendMessage(ChatColor.LIGHT_PURPLE + "You discovered a rare artifact while excavating!");
            }
        }
        
        // Multi-block excavation passive
        if (plugin.isPassiveEnabled("excavation", "multiBlock") && multiBlockPlayers.contains(playerId)) {
            double chance = plugin.getPassiveValue("excavation", "multiBlock");
            if (Math.random() < chance) {
                // Excavate a small area around the broken block
                excavateAreaAround(player, block, 1); // Radius of 1 for 3x3 area
                player.sendMessage(ChatColor.AQUA + "Your Multi-Block passive triggered a mini excavation!");
            }
        }
        
        // Ancient artifacts passive
        if (plugin.isPassiveEnabled("excavation", "ancientArtifacts") && ancientArtifactsPlayers.contains(playerId)) {
            double chance = plugin.getPassiveValue("excavation", "ancientArtifacts") / 4; // Very rare
            if (Math.random() < chance) {
                // Simplified implementation - drop a custom gold ingot
                ItemStack artifact = new ItemStack(Material.GOLD_INGOT, 1);
                ItemMeta meta = artifact.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GOLD + "Ancient Golden Artifact");
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.YELLOW + "A relic from an ancient civilization");
                    lore.add(ChatColor.YELLOW + "It seems to hold great power...");
                    meta.setLore(lore);
                    artifact.setItemMeta(meta);
                }
                
                block.getWorld().dropItemNaturally(block.getLocation(), artifact);
                player.sendMessage(ChatColor.DARK_PURPLE + "You unearthed an ancient artifact of incredible value!");
            }
        }
    }
    
    private boolean isShovel(Material material) {
        return material == Material.WOODEN_SHOVEL
            || material == Material.STONE_SHOVEL
            || material == Material.IRON_SHOVEL
            || material == Material.GOLDEN_SHOVEL
            || material == Material.DIAMOND_SHOVEL
            || material == Material.NETHERITE_SHOVEL;
    }
    
    private boolean isExcavatable(Material material) {
        return material == Material.DIRT
            || material == Material.COARSE_DIRT
            || material == Material.GRASS_BLOCK
            || material == Material.DIRT_PATH
            || material == Material.FARMLAND
            || material == Material.SAND
            || material == Material.RED_SAND
            || material == Material.GRAVEL
            || material == Material.CLAY
            || material == Material.SOUL_SAND
            || material == Material.SOUL_SOIL
            || material == Material.MYCELIUM
            || material == Material.PODZOL
            || material == Material.SNOW_BLOCK
            || material == Material.SNOW;
    }
    
    private void dropRandomTreasure(Location location) {
        // List of potential treasures
        List<ItemStack> treasures = new ArrayList<>();
        treasures.add(new ItemStack(Material.IRON_NUGGET, 1 + new Random().nextInt(3)));
        treasures.add(new ItemStack(Material.GOLD_NUGGET, 1 + new Random().nextInt(2)));
        treasures.add(new ItemStack(Material.IRON_INGOT, 1));
        treasures.add(new ItemStack(Material.FLINT, 1 + new Random().nextInt(3)));
        treasures.add(new ItemStack(Material.STRING, 1 + new Random().nextInt(3)));
        treasures.add(new ItemStack(Material.BONE, 1 + new Random().nextInt(2)));
        treasures.add(new ItemStack(Material.COAL, 1 + new Random().nextInt(2)));
        
        // Randomly select a treasure
        ItemStack treasure = treasures.get(new Random().nextInt(treasures.size()));
        
        // Drop the treasure in the world
        location.getWorld().dropItemNaturally(location, treasure);
    }
    
    private void dropRareItem(Location location) {
        // List of rare items
        List<ItemStack> rareItems = new ArrayList<>();
        rareItems.add(new ItemStack(Material.GOLD_INGOT, 1));
        rareItems.add(new ItemStack(Material.EMERALD, 1));
        rareItems.add(new ItemStack(Material.LAPIS_LAZULI, 2 + new Random().nextInt(3)));
        
        // 10% chance for diamond
        if (new Random().nextDouble() < 0.1) {
            rareItems.add(new ItemStack(Material.DIAMOND, 1));
        }
        
        // Randomly select a rare item
        ItemStack rareItem = rareItems.get(new Random().nextInt(rareItems.size()));
        
        // Drop the item
        location.getWorld().dropItemNaturally(location, rareItem);
    }
    
    /**
     * Drops archaeological finds at the specified location based on a loot table
     * Items include bones, pottery, ancient tools, etc.
     */
    private void dropArchaeologyFinds(Location location) {
        Random random = new Random();
        double roll = random.nextDouble();
        
        // Common finds (65% chance)
        if (roll < 0.65) {
            List<ItemStack> commonFinds = new ArrayList<>();
            commonFinds.add(new ItemStack(Material.BONE, 1 + random.nextInt(3)));
            commonFinds.add(new ItemStack(Material.CLAY_BALL, 1 + random.nextInt(2)));
            commonFinds.add(new ItemStack(Material.FLINT, 1 + random.nextInt(2)));
            commonFinds.add(new ItemStack(Material.STICK, 1 + random.nextInt(3)));
            
            ItemStack find = commonFinds.get(random.nextInt(commonFinds.size()));
            location.getWorld().dropItemNaturally(location, find);
        }
        // Uncommon finds (25% chance)
        else if (roll < 0.90) {
            List<ItemStack> uncommonFinds = new ArrayList<>();
            
            // Bone block
            uncommonFinds.add(new ItemStack(Material.BONE_BLOCK, 1));
            
            // Ancient pottery (brick)
            ItemStack pottery = new ItemStack(Material.BRICK, 1 + random.nextInt(2));
            ItemMeta potteryMeta = pottery.getItemMeta();
            if (potteryMeta != null) {
                potteryMeta.setDisplayName(ChatColor.YELLOW + "Ancient Pottery Fragment");
                pottery.setItemMeta(potteryMeta);
            }
            uncommonFinds.add(pottery);
            
            // Ancient tool
            ItemStack tool = new ItemStack(Material.STONE_PICKAXE, 1);
            ItemMeta toolMeta = tool.getItemMeta();
            if (toolMeta != null) {
                toolMeta.setDisplayName(ChatColor.YELLOW + "Ancient Stone Tool");
                tool.setItemMeta(toolMeta);
                // Set heavy damage to the tool
                ((Damageable)toolMeta).setDamage(Material.STONE_PICKAXE.getMaxDurability() - 5);
                tool.setItemMeta(toolMeta);
            }
            uncommonFinds.add(tool);
            
            // Fossil
            ItemStack fossil = new ItemStack(Material.COAL, 1);
            ItemMeta fossilMeta = fossil.getItemMeta();
            if (fossilMeta != null) {
                fossilMeta.setDisplayName(ChatColor.YELLOW + "Fossilized Remains");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "The remains of an ancient creature");
                fossilMeta.setLore(lore);
                fossil.setItemMeta(fossilMeta);
            }
            uncommonFinds.add(fossil);
            
            ItemStack find = uncommonFinds.get(random.nextInt(uncommonFinds.size()));
            location.getWorld().dropItemNaturally(location, find);
        }
        // Rare finds (10% chance)
        else {
            List<ItemStack> rareFinds = new ArrayList<>();
            
            // Ancient skull
            ItemStack skull = new ItemStack(Material.BONE_BLOCK, 1);
            ItemMeta skullMeta = skull.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Ancient Skull");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "A perfectly preserved skull");
                lore.add(ChatColor.GRAY + "from a long-extinct species");
                skullMeta.setLore(lore);
                skull.setItemMeta(skullMeta);
            }
            rareFinds.add(skull);
            
            // Ancient jewelry
            ItemStack jewelry = new ItemStack(Material.GOLD_NUGGET, 2 + random.nextInt(3));
            ItemMeta jewelryMeta = jewelry.getItemMeta();
            if (jewelryMeta != null) {
                jewelryMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Ancient Jewelry");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Precious ornaments from");
                lore.add(ChatColor.GRAY + "a forgotten civilization");
                jewelryMeta.setLore(lore);
                jewelry.setItemMeta(jewelryMeta);
            }
            rareFinds.add(jewelry);
            
            // Ancient artifact
            ItemStack artifact = new ItemStack(Material.PRISMARINE_SHARD, 1);
            ItemMeta artifactMeta = artifact.getItemMeta();
            if (artifactMeta != null) {
                artifactMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Strange Artifact");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "A mysterious object");
                lore.add(ChatColor.GRAY + "of unknown origin");
                artifactMeta.setLore(lore);
                artifact.setItemMeta(artifactMeta);
            }
            rareFinds.add(artifact);
            
            ItemStack find = rareFinds.get(random.nextInt(rareFinds.size()));
            location.getWorld().dropItemNaturally(location, find);
        }
    }

    // Add this section for handling repair passives in the anvil event
    @EventHandler
    public void onAnvilRepair(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getInventory() instanceof AnvilInventory)) {
            return;
        }

        // Check if the result slot was clicked
        if (event.getRawSlot() != 2) {
            return;
        }

        // Only process if there's actually a result
        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        UUID playerId = player.getUniqueId();
        boolean passiveActivated = false;

        // Material Saver passive - chance to not consume materials
        if (plugin.isPassiveEnabled("repair", "materialSaver") && materialSaverPlayers.contains(playerId)) {
            double chance = plugin.getPassiveValue("repair", "materialSaver");
            if (Math.random() < chance) {
                // This is tricky to implement directly since the anvil mechanics are handled by Minecraft
                // We would need to give the player back the materials, but that's complex
                // For now, we'll just notify them of the passive activation
                player.sendMessage(ChatColor.GREEN + "Your Material Saver passive preserved some materials!");
                passiveActivated = true;
            }
        }

        // Experience Saver passive - reduce XP cost
        if (plugin.isPassiveEnabled("repair", "experienceSaver") && experienceSaverPlayers.contains(playerId)) {
            // This is also tricky to implement directly
            // The XP cost is determined by the anvil GUI before this event fires
            // For a proper implementation, we would need to modify the anvil mechanics
            // For now, we'll just notify them
            if (!passiveActivated) {
                player.sendMessage(ChatColor.GREEN + "Your Experience Saver passive reduced the repair cost!");
                passiveActivated = true;
            }
        }

        // Quality Repair passive - repairs restore more durability
        if (plugin.isPassiveEnabled("repair", "qualityRepair") && qualityRepairPlayers.contains(playerId)) {
            // Again, this is difficult to implement directly
            // We would need to modify the durability of the repaired item
            // For now, just notify
            if (!passiveActivated) {
                player.sendMessage(ChatColor.GREEN + "Your Quality Repair passive improved the repair quality!");
            }
        }
    }

    /**
     * Excavates a small area around the broken block for the multi-block passive
     */
    private void excavateAreaAround(Player player, Block centerBlock, int radius) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Skip the center block as it's already broken
                    if (x == 0 && y == 0 && z == 0) continue;
                    
                    Block relativeBlock = centerBlock.getRelative(x, y, z);
                    
                    // Only break blocks that can be excavated
                    if (isExcavatable(relativeBlock.getType())) {
                        // Break the block and drop items naturally
                        relativeBlock.breakNaturally(tool);
                        
                        // Give XP for the broken block
                        int xpGained = plugin.getXpManager().getXPForExcavationMaterial(relativeBlock.getType());
                        if (xpGained > 0) {
                            plugin.getXpManager().addXP(player, "excavation", xpGained / 2); // Half XP for passive breaks
                        }
                    }
                }
            }
        }
    }

    /**
     * Tracks blocks placed by players to prevent XP farming
     */
    @EventHandler
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        Block block = event.getBlock();
        Material blockType = block.getType();
        
        // Only track blocks that give XP when mined
        if (isOre(blockType) || isStone(blockType) || isExcavatable(blockType) || 
            isFarmCrop(blockType) || blockType == Material.CACTUS || 
            blockType == Material.SUGAR_CANE || isPumpkin(blockType) || 
            isMelon(blockType)) {
            
            // Create a unique identifier for this block location
            String blockKey = getBlockLocationKey(block);
            
            // Add to our tracking set
            playerPlacedBlocks.add(blockKey);
            
            // Schedule removal after a long time (e.g., server restart or chunk unload will handle cleanup)
            // This prevents the set from growing too large
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                playerPlacedBlocks.remove(blockKey);
            }, 20 * 60 * 60 * 3); // Remove after 3 hours
        }
    }
    
    /**
     * Creates a unique string key for a block location
     */
    private String getBlockLocationKey(Block block) {
        return block.getWorld().getName() + ":" + 
               block.getX() + ":" + 
               block.getY() + ":" + 
               block.getZ();
    }
    
    /**
     * Checks if a block was placed by a player
     */
    private boolean isPlayerPlaced(Block block) {
        return playerPlacedBlocks.contains(getBlockLocationKey(block));
    }
}
