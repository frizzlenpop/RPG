package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;

import java.util.UUID;

public class PlayerDataListener implements Listener {

    private final PlayerDataManager dataManager;

    public PlayerDataListener(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Load player data (will create a new file if it doesn't exist)
        FileConfiguration config = dataManager.getPlayerData(playerUUID);

        // Debug message to check if data loads
        player.sendMessage("§a[Skills] Your skill data has been loaded.");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Save player data on quit
        FileConfiguration config = dataManager.getPlayerData(playerUUID);
        dataManager.savePlayerData(playerUUID, config);
    }
}
