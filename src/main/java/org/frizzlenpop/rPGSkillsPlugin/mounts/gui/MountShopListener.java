package org.frizzlenpop.rPGSkillsPlugin.mounts.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Listener for the Mount Shop GUI
 */
public class MountShopListener implements Listener {
    private final MountShopGUI gui;
    
    /**
     * Creates a new MountShopListener
     * 
     * @param gui The MountShopGUI instance
     */
    public MountShopListener(MountShopGUI gui) {
        this.gui = gui;
    }
    
    /**
     * Handles inventory click events
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Check if this is one of our GUIs
        if (title.contains("Mount Shop") || title.contains("Purchase") || title.contains("Confirm")) {
            // Let the GUI handle the click
            if (gui.handleInventoryClick(player, event.getSlot())) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Handles inventory close events
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        
        // Check if this is one of our GUIs
        if (title.contains("Mount Shop") || title.contains("Purchase") || title.contains("Confirm")) {
            // Let the GUI handle the close
            gui.handleInventoryClose(player.getUniqueId());
        }
    }
} 