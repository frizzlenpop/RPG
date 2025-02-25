package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

public class FishingListener implements Listener {

    private final XPManager xpManager;

    public FishingListener(XPManager xpManager) {
        this.xpManager = xpManager;
    }

    @EventHandler
    public void onFishCatch(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();
            String fishType = event.getCaught().getType().toString();

            // Get XP for the caught fish
            int xpGained = xpManager.getXPForFish(fishType);
            if (xpGained > 0) {
                xpManager.addXP(player, "fishing", xpGained);
            }
        }
    }
}
