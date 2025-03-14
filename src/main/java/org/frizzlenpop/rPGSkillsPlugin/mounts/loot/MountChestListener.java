package org.frizzlenpop.rPGSkillsPlugin.mounts.loot;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Listener for mount chest GUI events.
 */
public class MountChestListener implements Listener {
    private final MountChestGUI chestGUI;
    
    /**
     * Creates a new mount chest listener
     * 
     * @param chestGUI The mount chest GUI
     */
    public MountChestListener(MountChestGUI chestGUI) {
        this.chestGUI = chestGUI;
    }
    
    /**
     * Handles inventory click events in the mount chest GUI
     * 
     * @param event The inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        // Check if this is one of our GUIs
        String title = event.getView().getTitle();
        if (title.contains("Mount Chest") || title.contains("Opening") || title.contains("Fusion Complete")) {
            event.setCancelled(true); // Cancel all clicks by default
            
            // Let the GUI handle the click if it's a valid action
            if (chestGUI.handleInventoryClick(player, title, event.getRawSlot())) {
                // The click was handled by the GUI
                return;
            }
        }
    }
    
    /**
     * Handles inventory close events for the mount chest GUI
     * 
     * @param event The inventory close event
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        
        // Check if this is one of our GUIs
        String title = event.getView().getTitle();
        if (title.contains("Mount Chest") || title.contains("Opening") || title.contains("Fusion Complete")) {
            // Notify the GUI that this inventory was closed
            chestGUI.handleInventoryClose(player.getUniqueId());
        }
    }
} 