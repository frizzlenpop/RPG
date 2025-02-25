package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

public class LoggingListener implements Listener {

    private final XPManager xpManager;

    public LoggingListener(XPManager xpManager) {
        this.xpManager = xpManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        // Get XP for the chopped log
        int xpGained = xpManager.getXPForLog(blockType);
        if (xpGained > 0) {
            xpManager.addXP(player, "logging", xpGained);
        }
    }
}
