package org.frizzlenpop.rPGSkillsPlugin;

import org.bukkit.command.CommandExecutor;
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

    @Override
    public void onEnable() {
        getLogger().info("RPG Skills Plugin has been enabled!");

        // Initialize the data manager
        playerDataManager = new PlayerDataManager();
        getLogger().info("Player data directory: " + playerDataManager.getPlayerDataFolder().getAbsolutePath());

        // Initialize Passive Skill Manager first
        passiveSkillManager = new PassiveSkillManager();
        getServer().getPluginManager().registerEvents(passiveSkillManager, this);

        // Initialize XP Manager, GUI, and Abilities
        xpManager = new XPManager(playerDataManager, passiveSkillManager);
        skillsGUI = new SkillsGUI(playerDataManager, xpManager, abilityManager); // ✅ Fixed here
        abilityManager = new SkillAbilityManager();
        getServer().getPluginManager().registerEvents(abilityManager, this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new MiningListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new LoggingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FarmingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FightingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FishingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new EnchantingListener(xpManager), this);
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

    @Override
    public void onDisable() {
        getLogger().info("RPG Skills Plugin has been disabled!");
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
