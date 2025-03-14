package org.frizzlenpop.rPGSkillsPlugin.mounts.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
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
    private final Map<UUID, Inventory> activeGUIs = new HashMap<>();
    
    /**
     * Creates a new mount shop GUI manager
     * 
     * @param plugin The plugin instance
     * @param mountManager The mount manager
     */
    public MountShopGUI(RPGSkillsPlugin plugin, MountManager mountManager) {
        this.plugin = plugin;
        this.mountManager = mountManager;
    }
    
    /**
     * Opens the mount shop GUI for a player
     * 
     * @param player The player
     */
    public void openShopGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Mount Shop");
        UUID playerUUID = player.getUniqueId();
        
        // Get player's balance
        int balance = (int) plugin.getEconomyManager().getBalance(player);
        
        // Add mount items to inventory
        int slot = 10;
        for (Map.Entry<String, MountType> entry : mountManager.getMountTypes().entrySet()) {
            String mountId = entry.getKey();
            MountType mountType = entry.getValue();
            
            // Check if the player already owns this mount
            boolean owned = mountManager.ownsMount(playerUUID, mountId);
            
            // Create mount shop item
            ItemStack mountItem = createMountShopItem(mountType, owned);
            inventory.setItem(slot, mountItem);
            
            // Increment slot, handling row wrapping
            slot++;
            if (slot % 9 == 8) {
                slot += 2; // Skip to next row
            }
            
            // Stop if we run out of space
            if (slot >= 45) break;
        }
        
        // Add info and controls
        inventory.setItem(4, createGuiItem(Material.EMERALD, 
                ChatColor.GOLD + "Mount Shop",
                ChatColor.GRAY + "Purchase special mounts using coins",
                "",
                ChatColor.YELLOW + "Your Balance: " + ChatColor.WHITE + balance + " coins",
                "",
                ChatColor.GREEN + "Click on a mount to purchase it"));
        
        inventory.setItem(49, createGuiItem(Material.BOOK, 
                ChatColor.AQUA + "Mount Information",
                ChatColor.GRAY + "Each mount has unique abilities and stats",
                "",
                ChatColor.YELLOW + "Mount Prices:",
                ChatColor.WHITE + "• Phoenix Blaze: 25,000 coins",
                ChatColor.WHITE + "• Shadow Steed: 25,000 coins", 
                ChatColor.WHITE + "• Crystal Drake: 25,000 coins",
                ChatColor.WHITE + "• Storm Charger: 25,000 coins",
                ChatColor.WHITE + "• Ancient Golem: 25,000 coins"));
        
        inventory.setItem(53, createGuiItem(Material.BARRIER, ChatColor.RED + "Close", 
                "Click to close this menu"));
        
        // Store active GUI reference
        activeGUIs.put(playerUUID, inventory);
        
        // Open inventory for player
        player.openInventory(inventory);
    }
    
    /**
     * Creates an item for the GUI with custom name and lore
     * 
     * @param material The item material
     * @param name The item name
     * @param lore The item lore lines
     * @return The created ItemStack
     */
    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Creates a mount shop item
     * 
     * @param mountType The mount type
     * @param owned Whether the player already owns this mount
     * @return The ItemStack
     */
    private ItemStack createMountShopItem(MountType mountType, boolean owned) {
        Material material;
        switch (mountType.getId()) {
            case "phoenix_blaze" -> material = Material.BLAZE_POWDER;
            case "shadow_steed" -> material = Material.COAL;
            case "crystal_drake" -> material = Material.DIAMOND;
            case "storm_charger" -> material = Material.LIGHTNING_ROD;
            case "ancient_golem" -> material = Material.STONE;
            default -> material = Material.SADDLE;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName(ChatColor.GOLD + mountType.getDisplayName());
            
            // Add lore
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + mountType.getDescription());
            lore.add("");
            
            // Add stats
            lore.add(ChatColor.YELLOW + "Stats:");
            lore.add(ChatColor.GRAY + "• Speed: " + getStarRating(mountType.getSpeed(), 0.5));
            lore.add(ChatColor.GRAY + "• Jump: " + getStarRating(mountType.getJump(), 1.0));
            lore.add(ChatColor.GRAY + "• Health: " + getStarRating(mountType.getHealth(), 25.0));
            lore.add("");
            
            // Add abilities
            if (!mountType.getAbilities().isEmpty()) {
                lore.add(ChatColor.YELLOW + "Abilities:");
                for (MountType.MountAbility ability : mountType.getAbilities().values()) {
                    lore.add(ChatColor.GRAY + "• " + ability.getKey() + 
                            (ability.isPassive() ? " (Passive)" : ""));
                }
                lore.add("");
            }
            
            // Add price or owned status
            if (owned) {
                lore.add(ChatColor.GREEN + "✓ Already Owned");
            } else {
                int price = (int) mountType.getBaseCost();
                lore.add(ChatColor.YELLOW + "Price: " + ChatColor.WHITE + price + " coins");
                lore.add("");
                lore.add(ChatColor.AQUA + "Click to purchase this mount");
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Creates a star rating string based on value
     * 
     * @param value The value to rate
     * @param maxPerStar The maximum value per star
     * @return A string with stars
     */
    private String getStarRating(double value, double maxPerStar) {
        int stars = (int) Math.ceil(value / maxPerStar);
        if (stars > 5) stars = 5;
        
        StringBuilder rating = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < stars) {
                rating.append(ChatColor.YELLOW).append("★");
            } else {
                rating.append(ChatColor.GRAY).append("☆");
            }
        }
        
        return rating.toString();
    }
    
    /**
     * Handles a click in the mount shop GUI
     * 
     * @param player The player
     * @param slot The clicked slot
     * @return true if the click was handled
     */
    public boolean handleInventoryClick(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();
        Inventory inventory = activeGUIs.get(playerUUID);
        
        if (inventory == null) {
            return false;
        }
        
        // Handle mount slots (10-44, excluding border slots)
        if (slot >= 10 && slot < 45 && slot % 9 != 0 && slot % 9 != 8) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
                return true;
            }
            
            // Find which mount was clicked
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            for (Map.Entry<String, MountType> entry : mountManager.getMountTypes().entrySet()) {
                MountType mountType = entry.getValue();
                if (displayName.equals(ChatColor.stripColor(mountType.getDisplayName()))) {
                    // Attempt to purchase the mount
                    attemptPurchase(player, entry.getKey());
                    return true;
                }
            }
        }
        
        // Handle close button
        if (slot == 53) {
            player.closeInventory();
            return true;
        }
        
        return false;
    }
    
    /**
     * Attempts to purchase a mount for the player
     * 
     * @param player The player
     * @param mountId The mount ID
     */
    private void attemptPurchase(Player player, String mountId) {
        // Check if the player already owns this mount
        if (mountManager.ownsMount(player.getUniqueId(), mountId)) {
            player.sendMessage(ChatColor.YELLOW + "You already own this mount!");
            return;
        }
        
        // Try to purchase the mount
        boolean success = mountManager.purchaseMount(player, mountId);
        
        if (success) {
            // Update the GUI after purchase
            openShopGUI(player);
        }
    }
    
    /**
     * Removes a player's GUI reference when they close the inventory
     * 
     * @param playerUUID The player's UUID
     */
    public void handleInventoryClose(UUID playerUUID) {
        activeGUIs.remove(playerUUID);
    }
} 