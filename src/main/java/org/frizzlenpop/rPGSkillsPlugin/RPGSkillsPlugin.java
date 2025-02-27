package org.frizzlenpop.rPGSkillsPlugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.rPGSkillsPlugin.commands.PassivesCommand;
import org.frizzlenpop.rPGSkillsPlugin.commands.SkillsCommand;
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
        // Save default config if it doesn't exist
        saveDefaultConfig();
        // Load the config
        reloadConfig();
        config = getConfig();

        // Add default values if they don't exist
        addDefaultPassiveConfig();
        // Save config
        saveConfig();

        getLogger().info("RPG Skills Plugin has been enabled!");

        // Initialize the data manager
        playerDataManager = new PlayerDataManager();
        getLogger().info("Player data directory: " + playerDataManager.getPlayerDataFolder().getAbsolutePath());

        // Initialize Passive Skill Manager first
        getServer().getPluginManager().registerEvents(passiveSkillManager, this);

        // Initialize XP Manager, GUI, and Abilities
        xpManager = new XPManager(playerDataManager, passiveSkillManager);
        passiveSkillManager = new PassiveSkillManager(xpManager, this);
        skillsGUI = new SkillsGUI(playerDataManager, xpManager, abilityManager, passiveSkillManager);
        abilityManager = new SkillAbilityManager(this);
        getServer().getPluginManager().registerEvents(abilityManager, this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new MiningListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new LoggingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FarmingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FightingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FishingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new EnchantingListener(xpManager, this), this);
        getServer().getPluginManager().registerEvents(skillsGUI, this);

        // Register commands
        if (getCommand("skills") != null) getCommand("skills").setExecutor(new SkillsCommand(skillsGUI));
        if (getCommand("miningburst") != null) getCommand("miningburst").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) abilityManager.activateMiningBurst((Player) sender);
            return true;
        });
        if (getCommand("timberchop") != null) getCommand("timberchop").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) abilityManager.activateTimberChop((Player) sender);
            return true;
        });
        if (getCommand("berserkerrage") != null) getCommand("berserkerrage").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) abilityManager.activateBerserkerRage((Player) sender);
            return true;
        });
        if (getCommand("passives") != null) getCommand("passives").setExecutor(new PassivesCommand(passiveSkillManager));
    }

    private void addDefaultPassiveConfig() {
        // Mining passives
        if (!config.isSet("passives.mining.fortune_boost.chance")) {
            config.set("passives.mining.fortune_boost.chance", 0.25);
        }
        if (!config.isSet("passives.mining.fortune_boost.bonus_amount")) {
            config.set("passives.mining.fortune_boost.bonus_amount", 2);
        }
        if (!config.isSet("passives.mining.auto_smelt_upgrade.bonus_chance")) {
            config.set("passives.mining.auto_smelt_upgrade.bonus_chance", 0.5);
        }

        // Logging passives
        if (!config.isSet("passives.logging.tree_growth_boost.radius")) {
            config.set("passives.logging.tree_growth_boost.radius", 3);
        }
        if (!config.isSet("passives.logging.tree_growth_boost.growth_multiplier")) {
            config.set("passives.logging.tree_growth_boost.growth_multiplier", 2.0);
        }
        if (!config.isSet("passives.logging.triple_log_drop.enabled")) {
            config.set("passives.logging.triple_log_drop.enabled", true);
        }

        // Farming passives
        if (!config.isSet("passives.farming.instant_growth.chance")) {
            config.set("passives.farming.instant_growth.chance", 0.15);
        }
        if (!config.isSet("passives.farming.instant_growth.radius")) {
            config.set("passives.farming.instant_growth.radius", 2);
        }
        if (!config.isSet("passives.farming.auto_harvest.radius")) {
            config.set("passives.farming.auto_harvest.radius", 2);
        }

        // Fighting passives
        if (!config.isSet("passives.fighting.lifesteal.heal_percent")) {
            config.set("passives.fighting.lifesteal.heal_percent", 0.15);
        }
        if (!config.isSet("passives.fighting.damage_reduction.reduction_percent")) {
            config.set("passives.fighting.damage_reduction.reduction_percent", 0.25);
        }
    }

    public double getPassiveValue(String skill, String passive, String property) {
        return config.getDouble("passives." + skill + "." + passive + "." + property);
    }

    public boolean isPassiveEnabled(String skill, String passive) {
        return config.getBoolean("passives." + skill + "." + passive + ".enabled", true);
    }

    @Override
    public void onDisable() {
        getLogger().info("RPG Skills Plugin has been disabled!");
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}