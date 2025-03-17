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
import org.frizzlenpop.rPGSkillsPlugin.mounts.Mount;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountType;
import org.frizzlenpop.rPGSkillsPlugin.mounts.xp.MountXPManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles the GUI for mount management.
 */
public class MountGUI {
    private final RPGSkillsPlugin plugin;
    private final MountManager mountManager;
    private final MountXPManager xpManager;
    private final Map<UUID, Inventory> activeGUIs = new HashMap<>();
    private final Map<UUID, String> selectedMounts = new HashMap<>();
    private final InventoryManager inventoryManager;
    
    private static final String MAIN_GUI_TITLE = ChatColor.DARK_PURPLE + "Mount Manager";
    private static final String DETAILS_GUI_TITLE_PREFIX = ChatColor.DARK_PURPLE + "Mount Details: ";
    
    /**
     * Creates a new MountGUI
     * 
     * @param plugin The plugin instance
     * @param mountManager The mount manager
     */
    public MountGUI(RPGSkillsPlugin plugin, MountManager mountManager) {
        this.plugin = plugin;
        this.mountManager = mountManager;
        this.xpManager = mountManager.getXPManager();
        this.inventoryManager = plugin.getInventoryManager();
    }
    
    /**
     * Opens the main mount GUI for a player
     * 
     * @param player The player
     */
    public void openMainGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, MAIN_GUI_TITLE);
        UUID playerUUID = player.getUniqueId();
        
        // Get player's owned mounts
        Set<String> ownedMounts = mountManager.getPlayerOwnedMounts(playerUUID);
        
        // Add mount items to inventory
        int slot = 10;
        for (String mountId : ownedMounts) {
            MountType mountType = mountManager.getMountType(mountId);
            if (mountType != null) {
                int level = xpManager.getMountLevel(playerUUID, mountId);
                ItemStack mountItem = createMountItem(mountType, level);
                inventory.setItem(slot, mountItem);
                
                // Increment slot, skipping to next row if needed
                slot++;
                if (slot % 9 == 8) {
                    slot += 2;
                }
            }
        }
        
        // Store the active GUI
        activeGUIs.put(playerUUID, inventory);
        
        // Add decorative glass panes
        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                    inventory.setItem(i, createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, " "));
                }
            }
        }
        
        // Add back button
        inventory.setItem(49, createGuiItem(
                Material.ARROW,
                ChatColor.GOLD + "« Back to RPG Hub",
                ChatColor.GRAY + "Return to the main RPG Hub"
        ));
        
        player.openInventory(inventory);
        
        // Register this inventory with the inventory manager to prevent item theft
        inventoryManager.registerInventory(player, MAIN_GUI_TITLE);
        
        // Play sound
        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 0.5f, 1.0f);
    }
    
    /**
     * Opens the mount details GUI for a player
     * 
     * @param player The player
     * @param mountId The mount ID
     */
    public void openMountDetailsGUI(Player player, String mountId) {
        MountType mountType = mountManager.getMountType(mountId);
        if (mountType == null) {
            return;
        }
        
        UUID playerUUID = player.getUniqueId();
        int level = xpManager.getMountLevel(playerUUID, mountId);
        
        String guiTitle = DETAILS_GUI_TITLE_PREFIX + mountType.getDisplayName();
        Inventory inventory = Bukkit.createInventory(null, 54, guiTitle);
        
        // Store the active GUI and selected mount
        activeGUIs.put(playerUUID, inventory);
        selectedMounts.put(playerUUID, mountId);
        
        // Add mount info
        ItemStack mountItem = createMountItem(mountType, level);
        inventory.setItem(13, mountItem);
        
        // Add XP progress
        int currentXP = xpManager.getMountXP(playerUUID, mountId);
        int requiredXP = xpManager.getRequiredXP(level + 1);
        double progress = (double) currentXP / requiredXP;
        
        List<String> xpLore = new ArrayList<>();
        xpLore.add(ChatColor.GRAY + "Current Level: " + ChatColor.GOLD + level);
        xpLore.add(ChatColor.GRAY + "XP: " + ChatColor.GOLD + currentXP + "/" + requiredXP);
        xpLore.add("");
        
        // Create XP bar
        StringBuilder xpBar = new StringBuilder(ChatColor.GRAY + "[");
        int barLength = 20;
        int filledBars = (int) (progress * barLength);
        
        for (int i = 0; i < barLength; i++) {
            if (i < filledBars) {
                xpBar.append(ChatColor.GREEN + "■");
            } else {
                xpBar.append(ChatColor.RED + "■");
            }
        }
        xpBar.append(ChatColor.GRAY + "]");
        xpLore.add(xpBar.toString());
        
        inventory.setItem(22, createGuiItem(
                Material.EXPERIENCE_BOTTLE,
                ChatColor.GOLD + "Mount Level Progress",
                xpLore.toArray(new String[0])
        ));
        
        // Add equip/unequip button
        boolean isEquipped = mountManager.getActiveMount(playerUUID) != null && 
                             mountManager.getActiveMount(playerUUID).getId().equals(mountId);
        
        if (isEquipped) {
            inventory.setItem(31, createGuiItem(
                    Material.BARRIER,
                    ChatColor.RED + "Unequip Mount",
                    ChatColor.GRAY + "Click to unequip this mount"
            ));
        } else {
            inventory.setItem(31, createGuiItem(
                    Material.SADDLE,
                    ChatColor.GREEN + "Equip Mount",
                    ChatColor.GRAY + "Click to equip this mount"
            ));
        }
        
        // Add decorative glass panes
        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                    inventory.setItem(i, createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, " "));
                }
            }
        }
        
        // Add back button
        inventory.setItem(49, createGuiItem(
                Material.ARROW,
                ChatColor.GOLD + "« Back to Mount List",
                ChatColor.GRAY + "Return to your mount collection"
        ));
        
        player.openInventory(inventory);
        
        // Register this inventory with the inventory manager to prevent item theft
        inventoryManager.registerInventory(player, guiTitle);
        
        // Play sound
        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 0.5f, 1.0f);
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
     * Creates an item representing a mount for the GUI
     * 
     * @param mountType The mount type
     * @param level The mount's level
     * @return The created ItemStack
     */
    private ItemStack createMountItem(MountType mountType, int level) {
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
        if (meta == null) return item;
        
        // Set name and lore
        meta.setDisplayName(ChatColor.GOLD + mountType.getDisplayName());
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + mountType.getDescription());
        lore.add("");
        lore.add(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + level);
        lore.add("");
        lore.add(ChatColor.GREEN + "Click to select this mount");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Handles a click in the mount GUI
     * 
     * @param player The player who clicked
     * @param inventoryTitle The title of the clicked inventory
     * @param slot The clicked slot
     * @return true if the click was handled
     */
    public boolean handleInventoryClick(Player player, String inventoryTitle, int slot) {
        UUID playerUUID = player.getUniqueId();
        
        // Check if this is one of our GUIs
        if (!activeGUIs.containsKey(playerUUID)) {
            return false;
        }
        
        // Handle main menu
        if (inventoryTitle.equals(MAIN_GUI_TITLE)) {
            // Handle mount selection (slots 10-44)
            if (slot >= 10 && slot < 45 && slot % 9 != 0 && slot % 9 != 8) {
                ItemStack clickedItem = activeGUIs.get(playerUUID).getItem(slot);
                if (clickedItem == null || !clickedItem.hasItemMeta()) return true;
                
                String displayName = clickedItem.getItemMeta().getDisplayName();
                for (String mountId : mountManager.getPlayerOwnedMounts(playerUUID)) {
                    MountType mountType = mountManager.getMountType(mountId);
                    if (mountType != null && 
                            displayName.contains(mountType.getDisplayName())) {
                        openMountDetailsGUI(player, mountId);
                        return true;
                    }
                }
                return true;
            }
            
            // Handle action buttons
            switch (slot) {
                case 45: // Summon
                    String selectedMount = selectedMounts.get(playerUUID);
                    if (selectedMount != null) {
                        player.closeInventory();
                        mountManager.summonMount(player, selectedMount);
                    } else {
                        player.sendMessage(ChatColor.RED + "You need to select a mount first!");
                    }
                    return true;
                case 46: // Dismiss
                    player.closeInventory();
                    mountManager.dismissMount(playerUUID);
                    return true;
                case 47: // View stats
                    String selectedMountStats = selectedMounts.get(playerUUID);
                    if (selectedMountStats != null) {
                        openMountDetailsGUI(player, selectedMountStats);
                    } else {
                        player.sendMessage(ChatColor.RED + "You need to select a mount first!");
                    }
                    return true;
                case 49: // Back
                    openMainGUI(player);
                    return true;
                case 53: // Close
                    player.closeInventory();
                    return true;
            }
        }
        // Handle mount details GUI
        else if (inventoryTitle.contains(DETAILS_GUI_TITLE_PREFIX)) {
            switch (slot) {
                case 45: // Summon
                    String selectedMount = selectedMounts.get(playerUUID);
                    if (selectedMount != null) {
                        player.closeInventory();
                        mountManager.summonMount(player, selectedMount);
                    }
                    return true;
                case 49: // Back
                    openMainGUI(player);
                    return true;
                case 53: // Close
                    player.closeInventory();
                    return true;
            }
        }
        
        return false;
    }
    
    /**
     * Handles inventory close events
     * 
     * @param playerUUID The player's UUID
     * @param inventoryTitle The title of the closed inventory
     */
    public void handleInventoryClose(UUID playerUUID, String inventoryTitle) {
        // Clean up
        activeGUIs.remove(playerUUID);
        
        // Unregister this inventory with the inventory manager
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            inventoryManager.unregisterInventory(player, inventoryTitle);
        }
    }
    
    /**
     * Gets the currently selected mount for a player
     * 
     * @param playerUUID The player's UUID
     * @return The selected mount ID or null if none
     */
    public String getSelectedMount(UUID playerUUID) {
        return selectedMounts.get(playerUUID);
    }
    
    /**
     * Sets the selected mount for a player
     * 
     * @param playerUUID The player's UUID
     * @param mountId The mount ID
     */
    public void setSelectedMount(UUID playerUUID, String mountId) {
        selectedMounts.put(playerUUID, mountId);
    }
} 