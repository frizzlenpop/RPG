package org.frizzlenpop.rPGSkillsPlugin.mounts.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.gui.InventoryManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GUI for purchasing mounts.
 */
public class MountShopGUI {
    private final RPGSkillsPlugin plugin;
    private final MountManager mountManager;
    private final InventoryManager inventoryManager;
    private final Map<UUID, Inventory> activeGUIs = new HashMap<>();
    
    // GUI titles
    private static final String SHOP_GUI_TITLE = ChatColor.DARK_PURPLE + "Mount Shop";
    private static final String PURCHASE_GUI_TITLE = ChatColor.DARK_PURPLE + "Confirm Purchase";
    
    /**
     * Creates a new mount shop GUI manager
     * 
     * @param plugin The plugin instance
     * @param mountManager The mount manager
     */
    public MountShopGUI(RPGSkillsPlugin plugin, MountManager mountManager) {
        this.plugin = plugin;
        this.mountManager = mountManager;
        this.inventoryManager = plugin.getInventoryManager();
    }
    
    /**
     * Opens the mount shop GUI for a player
     * 
     * @param player The player
     */
    public void openShopGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, SHOP_GUI_TITLE);
        UUID playerUUID = player.getUniqueId();
        
        // Get player's balance
        int balance = (int) plugin.getEconomyManager().getBalance(player);
        
        // Add mount shop items
        int slot = 10;
        for (Map.Entry<String, MountType> entry : mountManager.getMountTypes().entrySet()) {
            String mountId = entry.getKey();
            MountType mountType = entry.getValue();
            
            // Skip special mounts that shouldn't be purchasable
            if (mountId.contains("special_") || mountId.contains("event_")) {
                continue;
            }
            
            // Check if player already owns this mount
            boolean owned = mountManager.ownsMount(playerUUID, mountId);
            
            // Create shop item
            ItemStack shopItem = createMountShopItem(mountType, owned);
            inventory.setItem(slot, shopItem);
            
            // Increment slot, handling row wrapping
            slot++;
            if (slot % 9 == 8) {
                slot += 2; // Skip to next row
            }
            
            // Stop if we run out of space
            if (slot >= 45) break;
        }
        
        // Add player balance display
        inventory.setItem(49, createGuiItem(
                Material.GOLD_INGOT,
                ChatColor.GOLD + "Your Balance: " + ChatColor.YELLOW + balance + " coins",
                ChatColor.GRAY + "Click on a mount to purchase it"
        ));
        
        // Add back button
        inventory.setItem(45, createGuiItem(
                Material.ARROW,
                ChatColor.GOLD + "« Back to RPG Hub",
                ChatColor.GRAY + "Return to the main RPG Hub"
        ));
        
        // Add decorative glass panes
        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                    inventory.setItem(i, createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, " "));
                }
            }
        }
        
        // Store active GUI reference
        activeGUIs.put(playerUUID, inventory);
        
        // Open inventory for player
        player.openInventory(inventory);
        
        // Register this inventory with the inventory manager to prevent item theft
        inventoryManager.registerInventory(player, SHOP_GUI_TITLE);
        
        // Play sound
        inventoryManager.playOpenSound(player);
    }
    
    /**
     * Creates a GUI item with a name and lore
     * 
     * @param material The material
     * @param name The name
     * @param lore The lore
     * @return The created ItemStack
     */
    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            
            if (lore != null && lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(line);
                }
                meta.setLore(loreList);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Creates a mount shop item
     * 
     * @param mountType The mount type
     * @param owned Whether the player already owns this mount
     * @return The created ItemStack
     */
    private ItemStack createMountShopItem(MountType mountType, boolean owned) {
        Material material;
        
        // Select appropriate material based on mount type
        switch (mountType.getId()) {
            case "phoenix_blaze" -> material = Material.BLAZE_POWDER;
            case "shadow_steed" -> material = Material.COAL;
            case "crystal_drake" -> material = Material.DIAMOND;
            case "storm_charger" -> material = Material.LIGHTNING_ROD;
            case "ancient_golem" -> material = Material.STONE;
            default -> material = Material.SADDLE;
        }
        
        // Create base item
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set name and lore
            meta.setDisplayName(ChatColor.GOLD + mountType.getDisplayName());
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + mountType.getDescription());
            lore.add("");
            
            // Add stats
            lore.add(ChatColor.YELLOW + "Stats:");
            lore.add(ChatColor.GRAY + "Speed: " + getStarRating(mountType.getSpeed(), 0.5));
            lore.add(ChatColor.GRAY + "Jump: " + getStarRating(mountType.getJump(), 0.5));
            lore.add(ChatColor.GRAY + "Health: " + getStarRating(mountType.getHealth(), 5.0));
            lore.add("");
            
            // Add price or owned status
            if (owned) {
                lore.add(ChatColor.GREEN + "✓ Already Owned");
            } else {
                lore.add(ChatColor.YELLOW + "Price: " + ChatColor.WHITE + mountType.getBaseCost() + " coins");
                lore.add("");
                lore.add(ChatColor.GREEN + "Click to purchase");
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Converts a numeric value to a star rating
     * 
     * @param value The value to convert
     * @param maxPerStar The maximum value per star
     * @return The star rating string
     */
    private String getStarRating(double value, double maxPerStar) {
        int stars = (int) Math.ceil(value / maxPerStar);
        stars = Math.min(stars, 5); // Cap at 5 stars
        
        StringBuilder rating = new StringBuilder();
        for (int i = 0; i < stars; i++) {
            rating.append(ChatColor.GOLD).append("★");
        }
        
        for (int i = stars; i < 5; i++) {
            rating.append(ChatColor.GRAY).append("★");
        }
        
        return rating.toString();
    }
    
    /**
     * Handles inventory clicks in the shop GUI
     * 
     * @param player The player who clicked
     * @param slot The clicked slot
     * @return True if the click was handled
     */
    public boolean handleInventoryClick(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();
        
        // Check if this is one of our GUIs
        if (!activeGUIs.containsKey(playerUUID)) {
            return false;
        }
        
        // Get the clicked item
        ItemStack clickedItem = activeGUIs.get(playerUUID).getItem(slot);
        if (clickedItem == null || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return true;
        }
        
        // Handle back button
        if (slot == 45) {
            player.closeInventory();
            player.performCommand("rpghub");
            return true;
        }
        
        // Handle mount purchase
        if (slot >= 10 && slot < 45 && slot % 9 != 0 && slot % 9 != 8) {
            String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            
            // Find which mount was clicked
            for (Map.Entry<String, MountType> entry : mountManager.getMountTypes().entrySet()) {
                MountType mountType = entry.getValue();
                
                if (displayName.equals(ChatColor.stripColor(mountType.getDisplayName()))) {
                    // Check if already owned
                    if (mountManager.ownsMount(playerUUID, entry.getKey())) {
                        player.sendMessage(ChatColor.RED + "You already own this mount!");
                        inventoryManager.playErrorSound(player);
                    } else {
                        // Attempt to purchase
                        attemptPurchase(player, entry.getKey());
                    }
                    return true;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Attempts to purchase a mount for a player
     * 
     * @param player The player
     * @param mountId The mount ID
     */
    private void attemptPurchase(Player player, String mountId) {
        MountType mountType = mountManager.getMountType(mountId);
        
        if (mountType == null) {
            player.sendMessage(ChatColor.RED + "Invalid mount!");
            inventoryManager.playErrorSound(player);
            return;
        }
        
        int cost = mountType.getBaseCost();
        int balance = (int) plugin.getEconomyManager().getBalance(player);
        
        if (balance < cost) {
            player.sendMessage(ChatColor.RED + "You don't have enough coins to purchase this mount!");
            player.sendMessage(ChatColor.RED + "Cost: " + cost + " coins, Your balance: " + balance + " coins");
            inventoryManager.playErrorSound(player);
            return;
        }
        
        // Deduct coins and give mount
        plugin.getEconomyManager().withdrawMoney(player, cost);
        mountManager.addMountToPlayer(player.getUniqueId(), mountId);
        
        // Send success message
        player.sendMessage(ChatColor.GREEN + "You have purchased the " + mountType.getDisplayName() + 
                ChatColor.GREEN + " mount for " + cost + " coins!");
        player.sendMessage(ChatColor.GREEN + "Use '/mount summon " + mountId + "' to ride it!");
        
        // Play success sound
        inventoryManager.playSuccessSound(player);
        
        // Close inventory and reopen shop
        player.closeInventory();
        openShopGUI(player);
    }
    
    /**
     * Handles inventory close events
     * 
     * @param playerUUID The player's UUID
     */
    public void handleInventoryClose(UUID playerUUID) {
        // Clean up
        activeGUIs.remove(playerUUID);
        
        // Unregister this inventory with the inventory manager
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            inventoryManager.unregisterInventory(player, SHOP_GUI_TITLE);
            inventoryManager.playCloseSound(player);
        }
    }
} 