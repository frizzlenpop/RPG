package org.frizzlenpop.rPGSkillsPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public class RPGSkillsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("RPG Skills Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RPG Skills Plugin has been disabled!");
    }
}
