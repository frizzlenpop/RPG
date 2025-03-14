package org.frizzlenpop.rPGSkillsPlugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Centralized manager for tracking inventories to prevent item theft
 */
public class InventoryManager implements Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, Set<String>> trackedInventories = new HashMap<>();
    
    /**
     * Creates a new InventoryManager
     * 
     * @param plugin The plugin instance
     */
    public InventoryManager(JavaPlugin plugin) {
        this.plugin = plugin;
        
        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Registers an inventory to be tracked for a player
     * 
     * @param player The player
     * @param inventoryTitle The inventory title
     */
    public void registerInventory(Player player, String inventoryTitle) {
        UUID playerUUID = player.getUniqueId();
        trackedInventories.computeIfAbsent(playerUUID, k -> new HashSet<>())
                .add(inventoryTitle);
    }
    
    /**
     * Unregisters an inventory for a player
     * 
     * @param player The player
     * @param inventoryTitle The inventory title
     */
    public void unregisterInventory(Player player, String inventoryTitle) {
        UUID playerUUID = player.getUniqueId();
        if (trackedInventories.containsKey(playerUUID)) {
            trackedInventories.get(playerUUID).remove(inventoryTitle);
            
            // Clean up if no inventories are tracked
            if (trackedInventories.get(playerUUID).isEmpty()) {
                trackedInventories.remove(playerUUID);
            }
        }
    }
    
    /**
     * Checks if an inventory is tracked for a player
     * 
     * @param player The player
     * @param inventoryTitle The inventory title
     * @return True if the inventory is tracked, false otherwise
     */
    public boolean isInventoryTracked(Player player, String inventoryTitle) {
        UUID playerUUID = player.getUniqueId();
        return trackedInventories.containsKey(playerUUID) && 
               trackedInventories.get(playerUUID).contains(inventoryTitle);
    }
    
    /**
     * Handles inventory clicks to prevent item theft
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String inventoryTitle = event.getView().getTitle();
        
        if (isInventoryTracked(player, inventoryTitle)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Handles inventory drags to prevent item theft
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String inventoryTitle = event.getView().getTitle();
        
        if (isInventoryTracked(player, inventoryTitle)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Handles inventory closing to update tracking
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        String inventoryTitle = event.getView().getTitle();
        
        unregisterInventory(player, inventoryTitle);
    }
    
    /**
     * Handles player quit to clean up tracking data
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        trackedInventories.remove(playerUUID);
    }
    
    /**
     * Adds a "Back to Hub" button to any inventory
     * 
     * @param inventory The inventory to add the button to
     * @param slot The slot to place the button
     */
    public void addHubButton(Inventory inventory, int slot) {
        ItemStack hubButton = GUIUtils.createGuiItem(
                org.bukkit.Material.NETHER_STAR, 
                "§6§l« Back to RPG Hub »",
                "§7Click to return to the main hub"
        );
        
        inventory.setItem(slot, hubButton);
    }
} 