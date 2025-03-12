package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.skills.PassiveSkillManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

public class ExcavationListener implements Listener {

    private final XPManager xpManager;
    private final RPGSkillsPlugin plugin;
    private final PassiveSkillManager passiveSkillManager;

    public ExcavationListener(XPManager xpManager, RPGSkillsPlugin plugin, PassiveSkillManager passiveSkillManager) {
        this.xpManager = xpManager;
        this.plugin = plugin;
        this.passiveSkillManager = passiveSkillManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // Check if player is using a shovel
        if (!isShovel(tool.getType())) {
            return;
        }

        // Check if the block can be excavated (dirt, sand, gravel, etc.)
        int xpGained = xpManager.getXPForExcavationMaterial(blockType);
        if (xpGained > 0) {
            // Add XP for excavation
            xpManager.addXP(player, "excavation", xpGained);
        }
    }

    private boolean isShovel(Material material) {
        return material == Material.WOODEN_SHOVEL
            || material == Material.STONE_SHOVEL
            || material == Material.IRON_SHOVEL
            || material == Material.GOLDEN_SHOVEL
            || material == Material.DIAMOND_SHOVEL
            || material == Material.NETHERITE_SHOVEL;
    }
} 