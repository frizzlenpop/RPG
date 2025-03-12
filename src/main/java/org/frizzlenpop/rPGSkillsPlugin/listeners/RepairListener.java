package org.frizzlenpop.rPGSkillsPlugin.listeners;

// Add any missing imports
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

public class RepairListener implements Listener {

    private final XPManager xpManager;

    public RepairListener(XPManager xpManager) {
        this.xpManager = xpManager;
    }

    @EventHandler
    public void onAnvilUse(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getInventory() instanceof AnvilInventory)) {
            return;
        }

        // Check if the result slot was clicked
        if (event.getRawSlot() != 2) {
            return;
        }

        // Only process if there's actually a result
        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        // Add repair XP based on the item being repaired
        int xpGained = getRepairXP(result.getType());
        if (xpGained > 0) {
            xpManager.addXP(player, "repair", xpGained);
        }
    }

    private int getRepairXP(Material material) {
        // Return different XP values based on the material type
        return switch (material) {
            case WOODEN_SWORD, WOODEN_AXE, WOODEN_PICKAXE, WOODEN_SHOVEL, WOODEN_HOE, LEATHER_HELMET, 
                 LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS -> 5;
                 
            case STONE_SWORD, STONE_AXE, STONE_PICKAXE, STONE_SHOVEL, STONE_HOE -> 10;
            
            case IRON_SWORD, IRON_AXE, IRON_PICKAXE, IRON_SHOVEL, IRON_HOE, IRON_HELMET,
                 IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS, CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE,
                 CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS, SHIELD -> 15;
                 
            case GOLDEN_SWORD, GOLDEN_AXE, GOLDEN_PICKAXE, GOLDEN_SHOVEL, GOLDEN_HOE, GOLDEN_HELMET,
                 GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS -> 20;
                 
            case DIAMOND_SWORD, DIAMOND_AXE, DIAMOND_PICKAXE, DIAMOND_SHOVEL, DIAMOND_HOE, DIAMOND_HELMET,
                 DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS -> 30;
                 
            case NETHERITE_SWORD, NETHERITE_AXE, NETHERITE_PICKAXE, NETHERITE_SHOVEL, NETHERITE_HOE, 
                 NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS -> 40;
                 
            case BOW, CROSSBOW, FISHING_ROD, TRIDENT -> 25;
            
            default -> 0;
        };
    }
} 