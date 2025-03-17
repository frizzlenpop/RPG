package org.frizzlenpop.rPGSkillsPlugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
    private final Map<UUID, Long> animationTimeouts = new HashMap<>();
    
    // Standard sound effects
    public static final Sound OPEN_SOUND = Sound.BLOCK_ENDER_CHEST_OPEN;
    public static final Sound CLOSE_SOUND = Sound.BLOCK_ENDER_CHEST_CLOSE;
    public static final Sound CLICK_SOUND = Sound.UI_BUTTON_CLICK;
    public static final Sound SUCCESS_SOUND = Sound.ENTITY_PLAYER_LEVELUP;
    public static final Sound ERROR_SOUND = Sound.ENTITY_VILLAGER_NO;
    
    // Animation timeout in milliseconds (30 seconds)
    private static final long ANIMATION_TIMEOUT = 30000;
    
    /**
     * Creates a new InventoryManager
     * 
     * @param plugin The plugin instance
     */
    public InventoryManager(JavaPlugin plugin) {
        this.plugin = plugin;
        
        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // Start animation timeout checker
        startAnimationTimeoutChecker();
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
                
        // If this is an animation inventory, set a timeout
        if (inventoryTitle.contains("Opening") || inventoryTitle.contains("Animation")) {
            animationTimeouts.put(playerUUID, System.currentTimeMillis() + ANIMATION_TIMEOUT);
        }
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
        
        // Remove animation timeout if this was an animation inventory
        if (inventoryTitle.contains("Opening") || inventoryTitle.contains("Animation")) {
            animationTimeouts.remove(playerUUID);
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
            
            // Check if this is a hub button click
            if (event.getCurrentItem() != null && 
                event.getCurrentItem().getItemMeta() != null && 
                event.getCurrentItem().getItemMeta().getDisplayName().contains("Back to RPG Hub")) {
                
                // Close inventory and open hub
                player.closeInventory();
                player.playSound(player.getLocation(), CLICK_SOUND, 0.5f, 1.0f);
                
                // Schedule the hub command to run after inventory is closed
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.performCommand("rpghub");
                }, 1L);
            }
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
        animationTimeouts.remove(playerUUID);
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
    
    /**
     * Plays the standard open sound for a player
     * 
     * @param player The player
     */
    public void playOpenSound(Player player) {
        player.playSound(player.getLocation(), OPEN_SOUND, 0.5f, 1.0f);
    }
    
    /**
     * Plays the standard close sound for a player
     * 
     * @param player The player
     */
    public void playCloseSound(Player player) {
        player.playSound(player.getLocation(), CLOSE_SOUND, 0.5f, 1.0f);
    }
    
    /**
     * Plays the standard click sound for a player
     * 
     * @param player The player
     */
    public void playClickSound(Player player) {
        player.playSound(player.getLocation(), CLICK_SOUND, 0.5f, 1.0f);
    }
    
    /**
     * Plays the standard success sound for a player
     * 
     * @param player The player
     */
    public void playSuccessSound(Player player) {
        player.playSound(player.getLocation(), SUCCESS_SOUND, 0.5f, 1.0f);
    }
    
    /**
     * Plays the standard error sound for a player
     * 
     * @param player The player
     */
    public void playErrorSound(Player player) {
        player.playSound(player.getLocation(), ERROR_SOUND, 0.5f, 1.0f);
    }
    
    /**
     * Starts the animation timeout checker task
     */
    private void startAnimationTimeoutChecker() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            
            // Check for timed out animations
            for (Map.Entry<UUID, Long> entry : new HashMap<>(animationTimeouts).entrySet()) {
                if (currentTime > entry.getValue()) {
                    // Animation has timed out
                    UUID playerUUID = entry.getKey();
                    Player player = Bukkit.getPlayer(playerUUID);
                    
                    if (player != null && player.isOnline()) {
                        // Close the player's inventory
                        player.closeInventory();
                        player.sendMessage("§cAnimation timed out. Please try again.");
                        playErrorSound(player);
                    }
                    
                    // Remove the timeout
                    animationTimeouts.remove(playerUUID);
                }
            }
        }, 20L, 20L); // Check every second
    }
} 