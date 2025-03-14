package org.frizzlenpop.rPGSkillsPlugin.mounts.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
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
    
    /**
     * Creates a new mount GUI manager
     * 
     * @param plugin The plugin instance
     * @param mountManager The mount manager
     */
    public MountGUI(RPGSkillsPlugin plugin, MountManager mountManager) {
        this.plugin = plugin;
        this.mountManager = mountManager;
        this.xpManager = mountManager.getXPManager();
    }
    
    /**
     * Opens the main mount GUI for a player
     * 
     * @param player The player
     */
    public void openMainGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Mount Manager");
        UUID playerUUID = player.getUniqueId();
        
        // Get player's owned mounts
        Set<String> ownedMounts = mountManager.getPlayerOwnedMounts(playerUUID);
        
        // Add mount items to inventory
        int slot = 10;
        for (String mountId : ownedMounts) {
            MountType mountType = mountManager.getMountType(mountId);
            if (mountType == null) continue;
            
            // Get level data
            int level = xpManager.getMountLevel(playerUUID, mountId);
            
            // Create item
            ItemStack mountItem = createMountItem(mountType, level);
            inventory.setItem(slot, mountItem);
            
            // Increment slot, handling row wrapping
            slot++;
            if (slot % 9 == 8) {
                slot += 2; // Skip to next row
            }
            
            // Stop if we run out of space
            if (slot >= 45) break;
        }
        
        // Add action buttons
        inventory.setItem(45, createGuiItem(Material.SADDLE, ChatColor.GREEN + "Summon Selected Mount", 
                "Click to summon the selected mount"));
        
        inventory.setItem(46, createGuiItem(Material.BARRIER, ChatColor.RED + "Dismiss Current Mount", 
                "Click to dismiss your current mount"));
        
        inventory.setItem(47, createGuiItem(Material.EXPERIENCE_BOTTLE, ChatColor.AQUA + "View Mount Stats", 
                "Click to view detailed stats for the selected mount"));
        
        inventory.setItem(49, createGuiItem(Material.BOOK, ChatColor.GOLD + "Mount Abilities", 
                "Click to view and use mount abilities"));
        
        inventory.setItem(53, createGuiItem(Material.BARRIER, ChatColor.RED + "Close", 
                "Click to close this menu"));
        
        // Store active GUI reference
        activeGUIs.put(playerUUID, inventory);
        
        // Open inventory for player
        player.openInventory(inventory);
    }
    
    /**
     * Opens the mount details GUI for a player
     * 
     * @param player The player
     * @param mountId The mount ID to view
     */
    public void openMountDetailsGUI(Player player, String mountId) {
        UUID playerUUID = player.getUniqueId();
        MountType mountType = mountManager.getMountType(mountId);
        
        if (mountType == null) {
            player.sendMessage(ChatColor.RED + "Unknown mount type: " + mountId);
            return;
        }
        
        // Set this as selected mount
        selectedMounts.put(playerUUID, mountId);
        
        // Get mount level data
        int level = xpManager.getMountLevel(playerUUID, mountId);
        int xp = xpManager.getMountXP(playerUUID, mountId);
        int nextLevelXP = xpManager.getRequiredXP(level + 1);
        int progress = xpManager.getLevelProgress(playerUUID, mountId);
        
        // Create inventory
        String title = ChatColor.GOLD + mountType.getDisplayName() + " Details";
        Inventory inventory = Bukkit.createInventory(null, 54, title);
        
        // Mount info item (center top)
        ItemStack infoItem = createGuiItem(Material.NAME_TAG, 
                ChatColor.GOLD + mountType.getDisplayName(),
                ChatColor.GRAY + mountType.getDescription(),
                "",
                ChatColor.YELLOW + "Level: " + ChatColor.WHITE + level,
                ChatColor.YELLOW + "XP: " + ChatColor.WHITE + xp + " / " + nextLevelXP,
                ChatColor.YELLOW + "Progress: " + ChatColor.WHITE + progress + "%");
        inventory.setItem(4, infoItem);
        
        // Stats (left side)
        ItemStack statsItem = createGuiItem(Material.DIAMOND_HORSE_ARMOR,
                ChatColor.AQUA + "Mount Stats",
                "",
                ChatColor.GRAY + "Speed: " + ChatColor.WHITE + mountType.getSpeed(),
                ChatColor.GRAY + "Jump: " + ChatColor.WHITE + mountType.getJump(),
                ChatColor.GRAY + "Health: " + ChatColor.WHITE + mountType.getHealth());
        inventory.setItem(20, statsItem);
        
        // Abilities (right side)
        Map<String, MountType.MountAbility> abilities = mountType.getAbilities();
        List<String> abilityLore = new ArrayList<>();
        abilityLore.add("");
        
        if (abilities.isEmpty()) {
            abilityLore.add(ChatColor.GRAY + "None");
        } else {
            for (MountType.MountAbility ability : abilities.values()) {
                String status = level >= ability.getMinLevel() ? ChatColor.GREEN + "✓ " : ChatColor.RED + "✗ ";
                String type = ability.isPassive() ? " (Passive)" : "";
                abilityLore.add(status + ChatColor.YELLOW + ability.getKey() + ChatColor.GRAY + type);
                abilityLore.add(ChatColor.GRAY + "  Unlocked at level " + ability.getMinLevel());
            }
        }
        
        ItemStack abilitiesItem = createGuiItem(Material.BLAZE_POWDER,
                ChatColor.GOLD + "Mount Abilities",
                abilityLore.toArray(new String[0]));
        inventory.setItem(24, abilitiesItem);
        
        // Control buttons
        inventory.setItem(45, createGuiItem(Material.SADDLE, ChatColor.GREEN + "Summon This Mount", 
                "Click to summon this mount"));
        
        inventory.setItem(49, createGuiItem(Material.ARROW, ChatColor.YELLOW + "Back", 
                "Return to mount list"));
        
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
        if (inventoryTitle.equals(ChatColor.DARK_PURPLE + "Mount Manager")) {
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
                case 53: // Close
                    player.closeInventory();
                    return true;
            }
        }
        // Handle mount details GUI
        else if (inventoryTitle.contains("Details")) {
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
     * Removes a player's GUI reference when they close the inventory
     * 
     * @param playerUUID The player's UUID
     */
    public void handleInventoryClose(UUID playerUUID) {
        activeGUIs.remove(playerUUID);
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