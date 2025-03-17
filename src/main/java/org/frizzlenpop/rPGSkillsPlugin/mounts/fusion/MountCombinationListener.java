package org.frizzlenpop.rPGSkillsPlugin.mounts.fusion;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Listener for the Mount Combination GUI
 */
public class MountCombinationListener implements Listener {
    private final MountCombinationGUI gui;
    
    /**
     * Creates a new MountCombinationListener
     * 
     * @param gui The MountCombinationGUI instance
     */
    public MountCombinationListener(MountCombinationGUI gui) {
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
        if (title.contains("Mount Fusion") || title.contains("Catalyst") || title.contains("Fusion Complete")) {
            // Let the GUI handle the click
            if (gui.handleInventoryClick(player, title, event.getSlot())) {
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
        if (title.contains("Mount Fusion") || title.contains("Catalyst") || title.contains("Fusion Complete")) {
            // Let the GUI handle the close
            gui.handleInventoryClose(player, title);
        }
    }
} 