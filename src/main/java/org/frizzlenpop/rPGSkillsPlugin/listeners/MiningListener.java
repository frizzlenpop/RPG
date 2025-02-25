package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

public class MiningListener implements Listener {

    private final XPManager xpManager;

    public MiningListener(XPManager xpManager) {
        this.xpManager = xpManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        // Get XP for the mined block
        int xpGained = xpManager.getXPForMaterial(blockType);
        if (xpGained > 0) {
            xpManager.addXP(player, "mining", xpGained);
        }
    }
}
