package org.frizzlenpop.rPGSkillsPlugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.rPGSkillsPlugin.commands.*;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.gui.SkillsGUI;
import org.frizzlenpop.rPGSkillsPlugin.listeners.*;
import org.frizzlenpop.rPGSkillsPlugin.skills.PassiveSkillManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.SkillAbilityManager;

public class RPGSkillsPlugin extends JavaPlugin {
    private PlayerDataManager playerDataManager;
    private XPManager xpManager;
    private SkillsGUI skillsGUI;
    private SkillAbilityManager abilityManager;
    private PassiveSkillManager passiveSkillManager;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        this.config = getConfig();

        // Add default passive configuration
        addDefaultPassiveConfig();
        saveConfig();

        // Initialize managers in the correct order
        this.playerDataManager = new PlayerDataManager(this);
        this.xpManager = new XPManager(playerDataManager);
        this.abilityManager = new SkillAbilityManager(this);
        this.passiveSkillManager = new PassiveSkillManager(xpManager, this);
        this.skillsGUI = new SkillsGUI(playerDataManager, xpManager, abilityManager, passiveSkillManager);


        // Set the passive skill manager after initialization
        xpManager.setPassiveSkillManager(passiveSkillManager);

        // Register commands
        getCommand("skills").setExecutor(new SkillsCommand(skillsGUI));
        getCommand("abilities").setExecutor(new AbilitiesCommand(this, playerDataManager));
        getCommand("skillsadmin").setExecutor(new SkillsAdminCommand(playerDataManager, xpManager));
        getCommand("toggleskillmessages").setExecutor(new ToggleSkillMessagesCommand(playerDataManager));
        getCommand("passives").setExecutor(new PassivesCommand(passiveSkillManager));

        // Register all listeners
        registerListeners();

        getLogger().info("RPGSkills plugin has been enabled!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerDataListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new MiningListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FishingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new EnchantingListener(xpManager, this), this);
        getServer().getPluginManager().registerEvents(skillsGUI, this);
        getServer().getPluginManager().registerEvents(new LoggingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FarmingListener(xpManager), this);

        getServer().getPluginManager().registerEvents(abilityManager, this);
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
        // Save player data
        if (playerDataManager != null) {
            for (Player player : getServer().getOnlinePlayers()) {
                FileConfiguration playerData = playerDataManager.getPlayerData(player.getUniqueId());
                playerDataManager.savePlayerData(player.getUniqueId(), playerData);
            }
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
}