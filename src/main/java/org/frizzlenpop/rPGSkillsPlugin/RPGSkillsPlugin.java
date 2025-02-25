package org.frizzlenpop.rPGSkillsPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.bukkit.Bukkit;

public class RPGSkillsPlugin extends JavaPlugin {

    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        getLogger().info("RPG Skills Plugin has been enabled!");

        // Initialize the data manager
        playerDataManager = new PlayerDataManager();
        getLogger().info("Player data directory: " + playerDataManager.getPlayerDataFolder().getAbsolutePath());
    }

    @Override
    public void onDisable() {
        getLogger().info("RPG Skills Plugin has been disabled!");
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
