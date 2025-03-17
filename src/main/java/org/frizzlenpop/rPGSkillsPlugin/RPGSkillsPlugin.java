package org.frizzlenpop.rPGSkillsPlugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.rPGSkillsPlugin.commands.*;
import org.frizzlenpop.rPGSkillsPlugin.data.DatabaseManager;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.gui.InventoryManager;
import org.frizzlenpop.rPGSkillsPlugin.gui.RPGHubGUI;
import org.frizzlenpop.rPGSkillsPlugin.gui.SkillsGUI;
import org.frizzlenpop.rPGSkillsPlugin.items.CustomEnchantScroll;
import org.frizzlenpop.rPGSkillsPlugin.listeners.*;
import org.frizzlenpop.rPGSkillsPlugin.skills.PassiveSkillManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.SkillAbilityManager;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillTreeManager;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillTreeGUI;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillXPListener;
import org.frizzlenpop.rPGSkillsPlugin.listeners.ExcavationListener;
import org.frizzlenpop.rPGSkillsPlugin.listeners.RepairListener;
import org.frizzlenpop.rPGSkillsPlugin.data.PartyManager;
import org.frizzlenpop.rPGSkillsPlugin.data.EconomyManager;
import org.frizzlenpop.rPGSkillsPlugin.data.XPBoosterManager;
import org.frizzlenpop.rPGSkillsPlugin.commands.PartyCommand;
import org.frizzlenpop.rPGSkillsPlugin.gui.RPGScoreboardManager;
import org.frizzlenpop.rPGSkillsPlugin.gui.PartyPerksGUI;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.abilities.MountAbilityManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.commands.MountCommand;
import org.frizzlenpop.rPGSkillsPlugin.mounts.fusion.MountCombinationGUI;
import org.frizzlenpop.rPGSkillsPlugin.mounts.gui.MountGUI;
import org.frizzlenpop.rPGSkillsPlugin.mounts.gui.MountGUIListener;
import org.frizzlenpop.rPGSkillsPlugin.mounts.gui.MountShopGUI;
import org.frizzlenpop.rPGSkillsPlugin.mounts.loot.MountChestCommand;
import org.frizzlenpop.rPGSkillsPlugin.mounts.loot.MountChestGUI;
import org.frizzlenpop.rPGSkillsPlugin.mounts.loot.MountChestListener;
import org.frizzlenpop.rPGSkillsPlugin.mounts.loot.MountKeyManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.xp.MountXPManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountVisualManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountInteractionListener;
import org.frizzlenpop.rPGSkillsPlugin.mounts.listeners.MountProjectileListener;
import org.frizzlenpop.rPGSkillsPlugin.mounts.abilities.MountAbilityManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.effects.ProtocolLibEffectManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.fusion.MountCombinationListener;
import org.frizzlenpop.rPGSkillsPlugin.mounts.gui.MountShopListener;
import org.bukkit.Bukkit;
import org.frizzlenpop.rPGSkillsPlugin.api.RPGSkillsAPI;

public class RPGSkillsPlugin extends JavaPlugin {
    private PlayerDataManager playerDataManager;
    private XPManager xpManager;
    private SkillsGUI skillsGUI;
    private SkillAbilityManager abilityManager;
    private PassiveSkillManager passiveSkillManager;
    private CustomEnchantScroll customEnchantScroll;
    private SkillTreeManager skillTreeManager;
    private SkillTreeGUI skillTreeGUI;
    private SkillXPListener skillXPListener;
    private PartyManager partyManager;
    private EconomyManager economyManager;
    private PartyPerksGUI partyPerksGUI;
    private RPGScoreboardManager scoreboardManager;
    private XPBoosterManager xpBoosterManager;
    private FileConfiguration config;
    private MountManager mountManager;
    private MountGUI mountGUI;
    private MountShopGUI mountShopGUI;
    private MountCombinationGUI mountCombinationGUI;
    private MountKeyManager mountKeyManager;
    private MountChestGUI mountChestGUI;
    private MountChestCommand mountChestCommand;
    private MountVisualManager mountVisualManager;
    private ProtocolLibEffectManager protocolLibEffectManager;
    private MountAbilityManager mountAbilityManager;
    private RPGHubGUI rpgHubGUI;
    private InventoryManager inventoryManager;
    private DatabaseManager databaseManager;
    private boolean databaseEnabled = false;
    private RPGSkillsAPI api;

    @Override
    public void onEnable() {
        // Create plugin folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Set up configuration
        saveDefaultConfig();
        this.config = getConfig();
        
        // Add default database configuration if it doesn't exist
        addDefaultDatabaseConfig();

        // Add default passive configuration
        addDefaultPassiveConfig();
        saveConfig();
        
        // Initialize database manager if enabled
        initializeDatabase();

        // Initialize inventory manager first (needed by GUIs)
        this.inventoryManager = new InventoryManager(this);

        // Initialize managers in the correct order
        this.playerDataManager = new PlayerDataManager(this);
        
        // Connect PlayerDataManager to database if enabled
        if (databaseEnabled) {
            playerDataManager.setDatabaseManager(databaseManager);
        }
        
        this.xpBoosterManager = new XPBoosterManager(this);
        this.xpManager = new XPManager(playerDataManager, this);
        this.abilityManager = new SkillAbilityManager(this);
        
        // Initialize PassiveSkillManager with database connection if enabled
        initializePassiveSkillManager();
        
        this.skillsGUI = new SkillsGUI(playerDataManager, xpManager, abilityManager, passiveSkillManager);
        this.customEnchantScroll = new CustomEnchantScroll(this);
        
        // Initialize economy manager
        this.economyManager = new EconomyManager(this);
        
        // Initialize party manager for XP sharing
        this.partyManager = new PartyManager(this);
        
        // Set up dependencies
        this.xpManager.setPartyManager(partyManager);
        this.xpManager.setPassiveSkillManager(passiveSkillManager);
        
        // Initialize party perks GUI with necessary dependencies
        this.partyPerksGUI = new PartyPerksGUI(this, partyManager, economyManager);
        
        // Connect PartyPerksGUI with PartyManager
        partyManager.setPartyPerksGUI(partyPerksGUI);
        
        // Initialize skill tree system
        this.skillTreeManager = new SkillTreeManager(this, playerDataManager, xpManager);
        this.skillTreeGUI = new SkillTreeGUI(this, skillTreeManager);
        this.skillXPListener = new SkillXPListener(this, skillTreeManager);
        
        // Initialize scoreboard manager
        this.scoreboardManager = new RPGScoreboardManager(this);

        // Initialize mount system
        mountManager = new MountManager(this);
        mountGUI = new MountGUI(this, mountManager);
        
        // Initialize mount shop GUI
        mountShopGUI = new MountShopGUI(this, mountManager);
        
        // Initialize mount combination/fusion GUI
        mountCombinationGUI = new MountCombinationGUI(this, mountManager);
        
        // Initialize mount visual enhancements
        mountVisualManager = new MountVisualManager(this);
        
        // Initialize ProtocolLib effect manager if ProtocolLib is available
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            getLogger().info("ProtocolLib found! Enabling enhanced visual effects for mounts.");
            protocolLibEffectManager = new ProtocolLibEffectManager(this);
            mountVisualManager.setProtocolLibEffectManager(protocolLibEffectManager);
        } else {
            getLogger().warning("ProtocolLib not found. Enhanced visual effects will be disabled.");
        }
        
        // Connect mount visual manager to mount manager
        mountManager.setVisualManager(mountVisualManager);
        
        // Initialize mount ability manager
        mountAbilityManager = mountManager.getAbilityManager();
        
        // Initialize mount chest system
        mountKeyManager = new MountKeyManager(this);
        mountChestGUI = new MountChestGUI(this, mountKeyManager, mountManager);
        mountChestCommand = new MountChestCommand(this, mountKeyManager, mountChestGUI);
        
        // Initialize the centralized hub GUI AFTER all other systems
        rpgHubGUI = new RPGHubGUI(this);

        // Register commands
        registerCommands();

        // Register all listeners
        registerListeners();

        // Initialize the API
        this.api = RPGSkillsAPI.getInstance(this);

        getLogger().info("RPGSkills plugin has been enabled!");
    }

    private void registerCommands() {
        // Register commands
        getCommand("skills").setExecutor(new SkillsCommand(skillsGUI));
        getCommand("abilities").setExecutor(new AbilitiesCommand(this, playerDataManager));
        SkillsAdminCommand skillsAdminCommand = new SkillsAdminCommand(playerDataManager, xpManager);
        getCommand("skillsadmin").setExecutor(skillsAdminCommand);
        getCommand("skillsadmin").setTabCompleter(skillsAdminCommand);
        getCommand("toggleskillmessages").setExecutor(new ToggleSkillMessagesCommand(playerDataManager));
        getCommand("passives").setExecutor(new PassivesCommand(passiveSkillManager));
        getCommand("skilltree").setExecutor(new SkillTreeCommand(this, skillTreeGUI, skillTreeManager));
        getCommand("rstat").setExecutor(new RStatCommand(this));
        
        // Register party commands
        PartyCommand partyCommand = new PartyCommand(this, partyManager, partyPerksGUI);
        getCommand("rparty").setExecutor(partyCommand);
        getCommand("rparty").setTabCompleter(partyCommand);
        
        // Register XP booster command
        XPBoosterCommand xpBoosterCommand = new XPBoosterCommand(this, xpBoosterManager);
        getCommand("rpgbooster").setExecutor(xpBoosterCommand);
        getCommand("rpgbooster").setTabCompleter(xpBoosterCommand);
        
        // Register scoreboard command
        getCommand("rscoreboard").setExecutor(new RScoreboardCommand(this, scoreboardManager));

        // Register mount command
        getCommand("mount").setExecutor(new MountCommand(this));
        
        // Register mount chest command
        getCommand("mountchest").setExecutor(mountChestCommand);
        getCommand("mountchest").setTabCompleter(mountChestCommand);
        
        // Register central hub command
        getCommand("rpghub").setExecutor(new RPGHubCommand(rpgHubGUI));
        
        // Register database command
        DatabaseCommand databaseCommand = new DatabaseCommand(this);
        getCommand("rpgdb").setExecutor(databaseCommand);
        getCommand("rpgdb").setTabCompleter(databaseCommand);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerDataListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new MiningListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FishingListener(xpManager, this, passiveSkillManager), this);
        getServer().getPluginManager().registerEvents(new EnchantingListener(xpManager, this), this);
        getServer().getPluginManager().registerEvents(skillsGUI, this);
        getServer().getPluginManager().registerEvents(new LoggingListener(xpManager, this, passiveSkillManager), this);
        getServer().getPluginManager().registerEvents(new FarmingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FightingListener(xpManager, this, passiveSkillManager), this);
        getServer().getPluginManager().registerEvents(abilityManager, this);
        getServer().getPluginManager().registerEvents(new ExcavationListener(xpManager, this, passiveSkillManager), this);
        getServer().getPluginManager().registerEvents(new RepairListener(xpManager), this);
        
        // Register inventory manager to prevent item theft
        getServer().getPluginManager().registerEvents(inventoryManager, this);
        
        // Register all GUI listeners
        getServer().getPluginManager().registerEvents(rpgHubGUI, this);
        getServer().getPluginManager().registerEvents(skillTreeGUI, this);
        getServer().getPluginManager().registerEvents(partyPerksGUI, this);
        
        // Skill tree listeners are registered in their respective classes

        // Register mount listeners
        getServer().getPluginManager().registerEvents(mountManager, this);
        getServer().getPluginManager().registerEvents(mountManager.getAbilityManager(), this);
        getServer().getPluginManager().registerEvents(mountManager.getXPManager(), this);
        getServer().getPluginManager().registerEvents(new MountGUIListener(this, mountGUI), this);
        getServer().getPluginManager().registerEvents(new MountInteractionListener(this, mountManager), this);
        getServer().getPluginManager().registerEvents(new MountProjectileListener(this, mountManager, mountAbilityManager, xpManager), this);
        
        // Register mount chest listeners
        if (mountChestGUI != null) {
            getServer().getPluginManager().registerEvents(new MountChestListener(mountChestGUI), this);
        }
        
        // Register mount combination GUI listener
        if (mountCombinationGUI != null) {
            getServer().getPluginManager().registerEvents(new MountCombinationListener(mountCombinationGUI), this);
        }
        
        // Register mount shop GUI listener
        if (mountShopGUI != null) {
            getServer().getPluginManager().registerEvents(new MountShopListener(mountShopGUI), this);
        }
    }

    private void addDefaultPassiveConfig() {
        // Mining passives
        addDefaultPassiveIfNotExists("mining", "doubleOreChance", "chance", 0.1);
        addDefaultPassiveIfNotExists("mining", "doubleOreChance", "enabled", true);

        // Fishing passives
        addDefaultPassiveIfNotExists("fishing", "treasureHunter", "chance", 0.15);
        addDefaultPassiveIfNotExists("fishing", "treasureHunter", "enabled", true);

        // Farming passives
        addDefaultPassiveIfNotExists("farming", "doubleCropChance", "chance", 0.2);
        addDefaultPassiveIfNotExists("farming", "doubleCropChance", "enabled", true);

        // Logging passives
        addDefaultPassiveIfNotExists("logging", "doubleLogChance", "chance", 0.15);
        addDefaultPassiveIfNotExists("logging", "doubleLogChance", "enabled", true);

        // Fighting passives
        addDefaultPassiveIfNotExists("fighting", "damageReduction", "reduction", 0.15);
        addDefaultPassiveIfNotExists("fighting", "damageReduction", "enabled", true);

        // Enchanting passives
        addDefaultPassiveIfNotExists("enchanting", "doubleEnchantChance", "chance", 0.1);
        addDefaultPassiveIfNotExists("enchanting", "doubleEnchantChance", "enabled", true);
        
        // Excavation passives
        addDefaultPassiveIfNotExists("excavation", "doubleDrops", "chance", 0.15);
        addDefaultPassiveIfNotExists("excavation", "doubleDrops", "enabled", true);
        addDefaultPassiveIfNotExists("excavation", "archaeologyBasics", "chance", 0.12);
        addDefaultPassiveIfNotExists("excavation", "archaeologyBasics", "enabled", true);
        addDefaultPassiveIfNotExists("excavation", "treasureFinder", "chance", 0.1);
        addDefaultPassiveIfNotExists("excavation", "treasureFinder", "enabled", true);
        addDefaultPassiveIfNotExists("excavation", "rareFind", "chance", 0.05);
        addDefaultPassiveIfNotExists("excavation", "rareFind", "enabled", true);
        addDefaultPassiveIfNotExists("excavation", "multiBlock", "chance", 0.08);
        addDefaultPassiveIfNotExists("excavation", "multiBlock", "enabled", true);
        addDefaultPassiveIfNotExists("excavation", "ancientArtifacts", "chance", 0.02);
        addDefaultPassiveIfNotExists("excavation", "ancientArtifacts", "enabled", true);
        
        // Repair passives
        addDefaultPassiveIfNotExists("repair", "materialSaver", "chance", 0.10);
        addDefaultPassiveIfNotExists("repair", "materialSaver", "enabled", true);
        addDefaultPassiveIfNotExists("repair", "experienceSaver", "reduction", 0.15);
        addDefaultPassiveIfNotExists("repair", "experienceSaver", "enabled", true);
        addDefaultPassiveIfNotExists("repair", "qualityRepair", "bonusDurability", 0.10);
        addDefaultPassiveIfNotExists("repair", "qualityRepair", "enabled", true);
        addDefaultPassiveIfNotExists("repair", "masterSmith", "efficiency", 0.20);
        addDefaultPassiveIfNotExists("repair", "masterSmith", "enabled", true);
    }

    private void addDefaultPassiveIfNotExists(String skill, String passive, String property, Object value) {
        String path = "passives." + skill + "." + passive + "." + property;
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    public double getPassiveValue(String skill, String passive) {
        String path = "passives." + skill + "." + passive + "." + "chance";
        return config.getDouble(path);
    }

    public boolean isPassiveEnabled(String skill, String passive) {
        String path = "passives." + skill + "." + passive + ".enabled";
        return config.getBoolean(path, true);
    }

    @Override
    public void onDisable() {
        // Close database connection if it's open
        if (databaseEnabled && databaseManager != null) {
            databaseManager.close();
            getLogger().info("Database connection closed.");
        }
        
        // Save player data
        if (playerDataManager != null) {
            for (Player player : getServer().getOnlinePlayers()) {
                FileConfiguration playerData = playerDataManager.getPlayerData(player.getUniqueId());
                playerDataManager.savePlayerData(player.getUniqueId(), playerData);
            }
        }
        
        // Save party perks
        if (partyPerksGUI != null) {
            partyPerksGUI.savePurchasedPerks();
        }
        
        // Clean up scoreboard
        if (scoreboardManager != null) {
            scoreboardManager.cleanup();
        }

        // Save mount data
        if (mountManager != null) {
            mountManager.saveAllMountData();
        }
        
        // Clean up mount visual effects
        if (mountVisualManager != null) {
            mountVisualManager.cleanup();
        }

        // Clean up ProtocolLib effects if enabled
        if (protocolLibEffectManager != null) {
            protocolLibEffectManager.cleanup();
        }

        // Save configuration
        saveConfig();

        getLogger().info("RPGSkills plugin has been disabled!");
    }

    // Getters for accessing managers from other classes
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public XPManager getXpManager() {
        return xpManager;
    }

    public SkillAbilityManager getAbilityManager() {
        return abilityManager;
    }

    public PassiveSkillManager getPassiveSkillManager() {
        return passiveSkillManager;
    }
    
    public CustomEnchantScroll getCustomEnchantScroll() {
        return customEnchantScroll;
    }

    /**
     * Get the skill tree manager
     */
    public SkillTreeManager getSkillTreeManager() {
        return skillTreeManager;
    }
    
    /**
     * Get the skill tree GUI
     */
    public SkillTreeGUI getSkillTreeGUI() {
        return skillTreeGUI;
    }

    /**
     * Get the party manager for XP sharing
     */
    public PartyManager getPartyManager() {
        return partyManager;
    }
    
    /**
     * Get the economy manager
     */
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    /**
     * Get the party perks GUI
     */
    public PartyPerksGUI getPartyPerksGUI() {
        return partyPerksGUI;
    }
    
    /**
     * Get the scoreboard manager
     */
    public RPGScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    /**
     * Get the XP booster manager
     */
    public XPBoosterManager getXPBoosterManager() {
        return xpBoosterManager;
    }

    /**
     * Get the mount manager
     * 
     * @return The mount manager
     */
    public MountManager getMountManager() {
        return mountManager;
    }

    /**
     * Get the mount GUI manager
     * 
     * @return The mount GUI
     */
    public MountGUI getMountGUI() {
        return mountGUI;
    }

    /**
     * Get the mount chest command handler
     * 
     * @return The mount chest command
     */
    public MountChestCommand getMountChestCommand() {
        return mountChestCommand;
    }
    
    /**
     * Get the mount shop GUI
     * 
     * @return The mount shop GUI
     */
    public MountShopGUI getMountShopGUI() {
        return mountShopGUI;
    }
    
    /**
     * Get the mount combination GUI
     * 
     * @return The mount combination GUI
     */
    public MountCombinationGUI getMountCombinationGUI() {
        return mountCombinationGUI;
    }

    /**
     * Reload the configuration and refresh skill tree
     */
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.config = getConfig();
        
        // Reinitialize skill tree with new config
        if (skillTreeManager != null) {
            // Refresh skill tree nodes
            skillTreeManager.reloadSkillTreeConfig();
        }
    }

    /**
     * Gets the ProtocolLib effect manager
     * 
     * @return The ProtocolLib effect manager, or null if ProtocolLib is not available
     */
    public ProtocolLibEffectManager getProtocolLibEffectManager() {
        return protocolLibEffectManager;
    }

    /**
     * Gets the skills GUI
     * 
     * @return The skills GUI
     */
    public SkillsGUI getSkillsGUI() {
        return skillsGUI;
    }
    
    /**
     * Gets the centralized RPG Hub GUI
     * 
     * @return The RPG Hub GUI
     */
    public RPGHubGUI getRpgHubGUI() {
        return rpgHubGUI;
    }

    /**
     * Gets the inventory manager
     * 
     * @return The inventory manager
     */
    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    /**
     * Adds default database configuration to config.yml
     */
    private void addDefaultDatabaseConfig() {
        if (!config.contains("database")) {
            config.createSection("database");
            
            // MySQL configuration
            config.set("database.mysql.enabled", false);
            config.set("database.mysql.host", "localhost");
            config.set("database.mysql.port", 3306);
            config.set("database.mysql.database", "rpgskills");
            config.set("database.mysql.username", "root");
            config.set("database.mysql.password", "");
            
            // SQLite is used as fallback if MySQL is disabled or fails
            config.set("database.sqlite.enabled", true);
            
            // Migration settings
            config.set("database.migrate_on_startup", false);
            
            saveConfig();
        }
    }
    
    /**
     * Initializes the database connection
     */
    private void initializeDatabase() {
        boolean mysqlEnabled = config.getBoolean("database.mysql.enabled", false);
        boolean sqliteEnabled = config.getBoolean("database.sqlite.enabled", true);
        
        if (mysqlEnabled || sqliteEnabled) {
            try {
                this.databaseManager = new DatabaseManager(this);
                this.databaseEnabled = true;
                getLogger().info("Database connection established successfully!");
                
                // Check if we should migrate data from YAML to database
                if (config.getBoolean("database.migrate_on_startup", false)) {
                    getLogger().info("Data migration from YAML to database is enabled. Will migrate after startup.");
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        databaseManager.migrateFromYAML();
                    }, 100L); // Wait 5 seconds after startup to begin migration
                }
            } catch (Exception e) {
                getLogger().severe("Failed to initialize database: " + e.getMessage());
                getLogger().warning("Falling back to YAML storage.");
                this.databaseEnabled = false;
            }
        } else {
            getLogger().info("Database storage is disabled. Using YAML files for data storage.");
        }
    }
    
    /**
     * Initializes the PassiveSkillManager
     */
    private void initializePassiveSkillManager() {
        this.passiveSkillManager = new PassiveSkillManager(xpManager, this);
        
        // Connect PassiveSkillManager to database if enabled
        if (databaseEnabled) {
            passiveSkillManager.setDatabaseManager(databaseManager);
        }
    }

    /**
     * Gets the database manager
     * 
     * @return The database manager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    /**
     * Checks if database storage is enabled
     * 
     * @return True if database storage is enabled, false otherwise
     */
    public boolean isDatabaseEnabled() {
        return databaseEnabled;
    }

    /**
     * Gets the API instance for other plugins to use.
     * 
     * @return The API instance
     */
    public RPGSkillsAPI getAPI() {
        return api;
    }
}