package org.frizzlenpop.rPGSkillsPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.rPGSkillsPlugin.commands.SkillsCommand;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.gui.SkillsGUI;
import org.frizzlenpop.rPGSkillsPlugin.listeners.EnchantingListener;
import org.frizzlenpop.rPGSkillsPlugin.listeners.FarmingListener;
import org.frizzlenpop.rPGSkillsPlugin.listeners.FightingListener;
import org.frizzlenpop.rPGSkillsPlugin.listeners.FishingListener;
import org.frizzlenpop.rPGSkillsPlugin.listeners.LoggingListener;
import org.frizzlenpop.rPGSkillsPlugin.listeners.MiningListener;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

public class RPGSkillsPlugin extends JavaPlugin {

    private PlayerDataManager playerDataManager;
    private XPManager xpManager;
    private SkillsGUI skillsGUI;

    @Override
    public void onEnable() {
        getLogger().info("RPG Skills Plugin has been enabled!");

        // Initialize the data manager
        playerDataManager = new PlayerDataManager();
        getLogger().info("Player data directory: " + playerDataManager.getPlayerDataFolder().getAbsolutePath());

        // Initialize XP Manager & GUI
        xpManager = new XPManager(playerDataManager);
        skillsGUI = new SkillsGUI(playerDataManager);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new MiningListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new LoggingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FarmingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FightingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FishingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new EnchantingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(skillsGUI, this);

        // Register commands
        getCommand("skills").setExecutor(new SkillsCommand(skillsGUI));
    }

    @Override
    public void onDisable() {
        getLogger().info("RPG Skills Plugin has been disabled!");
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
