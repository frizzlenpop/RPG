package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.gui.RPGHubGUI;

public class PlayerDataListener implements Listener {
    private final PlayerDataManager dataManager;
    private final RPGSkillsPlugin plugin;
    private final RPGHubGUI rpgHubGUI;

    public PlayerDataListener(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
        this.plugin = dataManager.getPlugin();
        this.rpgHubGUI = plugin.getRpgHubGUI();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Load player data
        if (!dataManager.hasPlayerData(player.getUniqueId())) {
            dataManager.createDefaultPlayerData(player.getUniqueId());
            
            // For new players, show the RPG Hub after a short delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        rpgHubGUI.openGUI(player);
                    }
                }
            }.runTaskLater(plugin, 40L); // 40 ticks = 2 seconds
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Save player data
        dataManager.savePlayerData(event.getPlayer().getUniqueId());
    }
}
