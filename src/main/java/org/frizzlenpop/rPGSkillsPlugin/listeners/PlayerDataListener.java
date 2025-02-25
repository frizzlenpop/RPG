package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.UUID;

public class PlayerDataListener implements Listener {

    private final PlayerDataManager dataManager;

    public PlayerDataListener(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        FileConfiguration config = dataManager.getPlayerData(playerUUID);

        // Load player data (no need to modify here)
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        FileConfiguration config = dataManager.getPlayerData(playerUUID);

        // Save data when player quits
        dataManager.savePlayerData(playerUUID, config);
    }
}
