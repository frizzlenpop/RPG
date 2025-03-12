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
        System.out.println("[RPGSkills] FightingListener initialized");
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        Player killer = event.getEntity().getKiller();

        System.out.println("[RPGSkills] Entity death detected: " + entity.getType());

        if (killer != null) {
            String entityName = entity.getType().toString();
            System.out.println("[RPGSkills] Killed by player: " + killer.getName() + " | Entity type: " + entityName);

            // Get XP for the killed mob
            int xpGained = xpManager.getXPForMob(entityName);
            System.out.println("[RPGSkills] XP gained for " + entityName + ": " + xpGained);

            if (xpGained > 0) {
                xpManager.addXP(killer, "fighting", xpGained);
                killer.sendMessage("§c+" + xpGained + " Fighting XP");
                System.out.println("[RPGSkills] Added " + xpGained + " fighting XP to " + killer.getName());
            } else {
                System.out.println("[RPGSkills] No XP configured for entity type: " + entityName);
            }
        } else {
            System.out.println("[RPGSkills] No player killer found for this entity death");
        }
    }
}
