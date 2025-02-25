package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

public class FarmingListener implements Listener {

    private final XPManager xpManager;

    public FarmingListener(XPManager xpManager) {
        this.xpManager = xpManager;
    }

    @EventHandler
    public void onCropHarvest(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        // Get XP for the harvested crop
        int xpGained = xpManager.getXPForCrop(blockType);
        if (xpGained > 0) {
            xpManager.addXP(player, "farming", xpGained);
        }
    }
}
