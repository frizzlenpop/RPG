package org.frizzlenpop.rPGSkillsPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.listeners.LoggingListener;
import org.frizzlenpop.rPGSkillsPlugin.listeners.MiningListener;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

public class RPGSkillsPlugin extends JavaPlugin {

    private PlayerDataManager playerDataManager;
    private XPManager xpManager;

    @Override
    public void onEnable() {
        getLogger().info("RPG Skills Plugin has been enabled!");

        // Initialize the data manager
        playerDataManager = new PlayerDataManager();
        getLogger().info("Player data directory: " + playerDataManager.getPlayerDataFolder().getAbsolutePath());

        // Initialize XP Manager
        xpManager = new XPManager(playerDataManager);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new MiningListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new LoggingListener(xpManager), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("RPG Skills Plugin has been disabled!");
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
