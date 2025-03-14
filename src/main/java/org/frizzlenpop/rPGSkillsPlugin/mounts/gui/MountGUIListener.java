package org.frizzlenpop.rPGSkillsPlugin.mounts.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

/**
 * Listener for mount GUI interactions.
 */
public class MountGUIListener implements Listener {
    private final RPGSkillsPlugin plugin;
    private final MountGUI mountGUI;
    
    /**
     * Creates a new mount GUI listener
     * 
     * @param plugin The plugin instance
     * @param mountGUI The mount GUI manager
     */
    public MountGUIListener(RPGSkillsPlugin plugin, MountGUI mountGUI) {
        this.plugin = plugin;
        this.mountGUI = mountGUI;
    }
    
    /**
     * Handles clicks in the mount GUI inventories
     * 
     * @param event The inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        // Check if the inventory has a title and if it's one of our GUI screens
        String title = event.getView().getTitle();
        if (title.contains("Mount Manager") || title.contains("Details") || title.contains("Mount Abilities")) {
            event.setCancelled(true); // Prevent item moving in our GUI
            
            // Pass click to the GUI handler
            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                mountGUI.handleInventoryClick(player, title, event.getSlot());
            }
        }
    }
    
    /**
     * Handles inventory close for mount GUI screens
     * 
     * @param event The inventory close event
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        
        // Check if the inventory has a title and if it's one of our GUI screens
        String title = event.getView().getTitle();
        if (title.contains("Mount Manager") || title.contains("Details") || title.contains("Mount Abilities")) {
            mountGUI.handleInventoryClose(player.getUniqueId());
        }
    }
} 