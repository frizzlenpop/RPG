package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

public class FightingListener implements Listener {

    private final XPManager xpManager;

    public FightingListener(XPManager xpManager) {
        this.xpManager = xpManager;
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        Player killer = event.getEntity().getKiller();

        if (killer != null) {
            String entityName = entity.getType().toString();

            // Get XP for the killed mob
            int xpGained = xpManager.getXPForMob(entityName);
            if (xpGained > 0) {
                xpManager.addXP(killer, "fighting", xpGained);
            }
        }
    }
}
