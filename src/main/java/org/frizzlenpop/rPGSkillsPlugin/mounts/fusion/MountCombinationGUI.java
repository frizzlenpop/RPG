package org.frizzlenpop.rPGSkillsPlugin.mounts.fusion;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.gui.InventoryManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountRarity;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountType;
import org.frizzlenpop.rPGSkillsPlugin.mounts.xp.MountXPManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * GUI for combining/fusing mounts of the same type.
 */
public class MountCombinationGUI {
    private final RPGSkillsPlugin plugin;
    private final MountManager mountManager;
    private final MountXPManager xpManager;
    private final InventoryManager inventoryManager;
    private final Map<UUID, Inventory> activeGUIs = new HashMap<>();
    private final Map<UUID, String> selectedFirstMount = new HashMap<>();
    private final Map<UUID, String> selectedSecondMount = new HashMap<>();
    private final Map<UUID, String> selectedCatalyst = new HashMap<>();
    private final Random random = new Random();
    
    /**
     * Represents a fusion catalyst
     */
    public enum Catalyst {
        NONE(Material.BARRIER, 0, "None", "No catalyst selected"),
        BASIC(Material.GLOWSTONE_DUST, 25, "Basic Catalyst", "+25% chance of rarity upgrade"),
        ADVANCED(Material.GLOWSTONE, 50, "Advanced Catalyst", "+50% chance of rarity upgrade"),
        PERFECT(Material.SEA_LANTERN, 100, "Perfect Catalyst", "Guaranteed rarity upgrade");
        
        private final Material material;
        private final int upgradeChanceBoost;
        private final String name;
        private final String description;
        
        Catalyst(Material material, int upgradeChanceBoost, String name, String description) {
            this.material = material;
            this.upgradeChanceBoost = upgradeChanceBoost;
            this.name = name;
            this.description = description;
        }
        
        public Material getMaterial() {
            return material;
        }
        
        public int getUpgradeChanceBoost() {
            return upgradeChanceBoost;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Creates a new mount combination GUI manager
     * 
     * @param plugin The plugin instance
     * @param mountManager The mount manager
     */
    public MountCombinationGUI(RPGSkillsPlugin plugin, MountManager mountManager) {
        this.plugin = plugin;
        this.mountManager = mountManager;
        this.xpManager = mountManager.getXPManager();
        this.inventoryManager = plugin.getInventoryManager();
    }
    
    /**
     * Opens the main combination GUI for a player
     * 
     * @param player The player
     */
    public void openMainGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Mount Fusion");
        UUID playerUUID = player.getUniqueId();
        
        // Add owned mounts to inventory for selection
        addMountsToGUI(inventory, playerUUID);
        
        // Add info and controls
        inventory.setItem(4, createGuiItem(Material.ANVIL, 
                ChatColor.GOLD + "Mount Fusion",
                ChatColor.GRAY + "Combine two mounts of the same type and rarity",
                ChatColor.GRAY + "for a chance to upgrade their rarity!",
                "",
                ChatColor.YELLOW + "Instructions:",
                ChatColor.WHITE + "1. Select first mount",
                ChatColor.WHITE + "2. Select second mount (same type & rarity)",
                ChatColor.WHITE + "3. Click the combine button"));
        
        // Add catalyst selection button
        Catalyst catalyst = selectedCatalyst.containsKey(playerUUID) ? 
                            Catalyst.valueOf(selectedCatalyst.get(playerUUID)) : 
                            Catalyst.NONE;
        inventory.setItem(49, createCatalystItem(catalyst));
        
        // Add fusion button (disabled initially)
        inventory.setItem(31, createFusionButton(playerUUID));
        
        // Close button
        inventory.setItem(53, createGuiItem(Material.BARRIER, ChatColor.RED + "Close", 
                "Click to close this menu"));
        
        // Store active GUI reference
        activeGUIs.put(playerUUID, inventory);
        
        // Open inventory for player
        player.openInventory(inventory);
        
        // Register this inventory with the inventory manager to prevent item theft
        inventoryManager.registerInventory(player, ChatColor.DARK_PURPLE + "Mount Fusion");
        
        // Play sound
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.0f);
    }
    
    /**
     * Adds the player's owned mounts to the GUI
     * 
     * @param inventory The inventory
     * @param playerUUID The player's UUID
     */
    private void addMountsToGUI(Inventory inventory, UUID playerUUID) {
        // Get player's owned mounts
        Set<String> ownedMounts = mountManager.getPlayerOwnedMounts(playerUUID);
        
        // Track slots for mount items
        int slot = 10;
        
        // Map to track counts of each mount type/rarity
        Map<String, Map<MountRarity, Integer>> mountCounts = new HashMap<>();
        
        // Count mounts by type and rarity
        for (String mountId : ownedMounts) {
            MountType mountType = mountManager.getMountType(mountId);
            if (mountType == null) continue;
            
            // Determine mount rarity (would need to be stored in player data)
            MountRarity rarity = MountRarity.COMMON; // Default, should be retrieved from player data
            
            // Add to count map
            mountCounts.computeIfAbsent(mountId, k -> new HashMap<>())
                    .put(rarity, mountCounts.getOrDefault(mountId, new HashMap<>())
                            .getOrDefault(rarity, 0) + 1);
            
            // Only show mounts that the player has at least 2 of (can be combined)
            if (mountCounts.get(mountId).get(rarity) >= 2) {
                // Create item
                ItemStack mountItem = createMountItem(mountType, rarity, 
                        mountCounts.get(mountId).get(rarity));
                inventory.setItem(slot, mountItem);
                
                // Increment slot, handling row wrapping
                slot++;
                if (slot % 9 == 8) {
                    slot += 2; // Skip to next row
                }
                
                // Stop if we run out of space
                if (slot >= 45) break;
            }
        }
    }
    
    /**
     * Creates a mount item for the GUI
     * 
     * @param mountType The mount type
     * @param rarity The mount rarity
     * @param count The number owned
     * @return The ItemStack
     */
    private ItemStack createMountItem(MountType mountType, MountRarity rarity, int count) {
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
            meta.setDisplayName(rarity.getColor() + mountType.getDisplayName() + 
                    ChatColor.GRAY + " x" + count);
            
            // Add lore
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + mountType.getDescription());
            lore.add("");
            lore.add(rarity.getColor() + "Rarity: " + rarity.getFormattedName());
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to select this mount");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Creates a catalyst selection item
     * 
     * @param catalyst The current catalyst
     * @return The ItemStack
     */
    private ItemStack createCatalystItem(Catalyst catalyst) {
        ItemStack item = new ItemStack(catalyst.getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName(ChatColor.GOLD + "Selected Catalyst: " + ChatColor.AQUA + catalyst.getName());
            
            // Add lore
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + catalyst.getDescription());
            lore.add("");
            lore.add(ChatColor.YELLOW + "Available Catalysts:");
            
            for (Catalyst c : Catalyst.values()) {
                String selected = c == catalyst ? ChatColor.GREEN + "✓ " : "";
                lore.add(selected + ChatColor.WHITE + c.getName() + 
                        (c.getUpgradeChanceBoost() > 0 ? 
                        ChatColor.GRAY + " (+" + c.getUpgradeChanceBoost() + "%)" : ""));
            }
            
            lore.add("");
            lore.add(ChatColor.AQUA + "Click to change catalyst");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Creates the fusion button
     * 
     * @param playerUUID The player's UUID
     * @return The ItemStack
     */
    private ItemStack createFusionButton(UUID playerUUID) {
        String firstMountId = selectedFirstMount.get(playerUUID);
        String secondMountId = selectedSecondMount.get(playerUUID);
        
        boolean canFuse = firstMountId != null && secondMountId != null && 
                firstMountId.equals(secondMountId);
        
        Material material = canFuse ? Material.BEACON : Material.BARRIER;
        String name = canFuse ? ChatColor.GREEN + "Combine Mounts" : 
                ChatColor.RED + "Select Two Mounts First";
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            
            List<String> lore = new ArrayList<>();
            if (canFuse) {
                MountType mountType = mountManager.getMountType(firstMountId);
                MountRarity rarity = MountRarity.COMMON; // Should be retrieved from player data
                Catalyst catalyst = selectedCatalyst.containsKey(playerUUID) ? 
                                    Catalyst.valueOf(selectedCatalyst.get(playerUUID)) : 
                                    Catalyst.NONE;
                
                int baseChance = getBaseUpgradeChance(rarity);
                int totalChance = Math.min(100, baseChance + catalyst.getUpgradeChanceBoost());
                
                lore.add(ChatColor.YELLOW + "Combining:");
                lore.add(ChatColor.WHITE + "2x " + rarity.getFormattedName() + " " + mountType.getDisplayName());
                lore.add("");
                lore.add(ChatColor.YELLOW + "Chance to Upgrade: " + ChatColor.WHITE + totalChance + "%");
                lore.add(ChatColor.YELLOW + "Catalyst: " + ChatColor.WHITE + catalyst.getName());
                lore.add("");
                lore.add(ChatColor.GREEN + "Click to combine mounts");
            } else {
                lore.add(ChatColor.GRAY + "Select two mounts of the same");
                lore.add(ChatColor.GRAY + "type and rarity to combine them");
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Gets the base upgrade chance for a rarity
     * 
     * @param rarity The rarity
     * @return The base upgrade chance percentage
     */
    private int getBaseUpgradeChance(MountRarity rarity) {
        return switch (rarity) {
            case COMMON -> 75;
            case UNCOMMON -> 50;
            case RARE -> 30;
            case EPIC -> 15;
            case LEGENDARY -> 5;
            case MYTHIC -> 0;
            default -> 0;
        };
    }
    
    /**
     * Opens the catalyst selection GUI
     * 
     * @param player The player
     */
    private void openCatalystGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Select Catalyst");
        
        // Add all catalysts
        for (int i = 0; i < Catalyst.values().length; i++) {
            Catalyst catalyst = Catalyst.values()[i];
            
            ItemStack item = new ItemStack(catalyst.getMaterial());
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + catalyst.getName());
                
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + catalyst.getDescription());
                
                if (catalyst.getUpgradeChanceBoost() > 0) {
                    lore.add(ChatColor.YELLOW + "Upgrade Boost: " + 
                            ChatColor.WHITE + "+" + catalyst.getUpgradeChanceBoost() + "%");
                }
                
                lore.add("");
                lore.add(ChatColor.GREEN + "Click to select");
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            inventory.setItem(10 + i, item);
        }
        
        // Add close button
        inventory.setItem(26, createGuiItem(Material.BARRIER, ChatColor.RED + "Cancel", 
                "Click to cancel"));
        
        // Store active GUI reference
        activeGUIs.put(player.getUniqueId(), inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Register this inventory with the inventory manager to prevent item theft
        inventoryManager.registerInventory(player, ChatColor.GOLD + "Select Catalyst");
        
        // Play sound
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1.0f);
    }
    
    /**
     * Performs the mount fusion
     * 
     * @param player The player
     */
    private void performFusion(Player player) {
        UUID playerUUID = player.getUniqueId();
        String mountId = selectedFirstMount.get(playerUUID);
        
        if (mountId == null) {
            return;
        }
        
        MountType mountType = mountManager.getMountType(mountId);
        MountRarity currentRarity = MountRarity.COMMON; // Should be fetched from player data
        Catalyst catalyst = selectedCatalyst.containsKey(playerUUID) ? 
                            Catalyst.valueOf(selectedCatalyst.get(playerUUID)) : 
                            Catalyst.NONE;
        
        // Calculate chance of rarity upgrade
        int baseChance = getBaseUpgradeChance(currentRarity);
        int totalChance = Math.min(100, baseChance + catalyst.getUpgradeChanceBoost());
        
        // Calculate fusion costs
        int fusionCost = getFusionCost(currentRarity);
        
        // Check if player has enough money
        if (!plugin.getEconomyManager().withdrawMoney(player, fusionCost)) {
            player.sendMessage(ChatColor.RED + "You don't have enough money for this fusion! " +
                    "Cost: " + fusionCost + " coins");
            return;
        }
        
        // Combine XP from both mounts
        int xp1 = xpManager.getMountXP(playerUUID, mountId);
        int xp2 = xpManager.getMountXP(playerUUID, mountId);
        int combinedXP = xp1 + xp2;
        
        // Remove the two mounts
        mountManager.removeMountFromPlayer(playerUUID, mountId);
        mountManager.removeMountFromPlayer(playerUUID, mountId);
        
        // Determine if rarity upgrade occurs
        boolean rarityUpgrade = random.nextInt(100) < totalChance;
        MountRarity newRarity = currentRarity;
        
        if (rarityUpgrade && currentRarity != MountRarity.MYTHIC) {
            // Get next rarity
            newRarity = currentRarity.getNextRarity();
        }
        
        // Add the new mount with combined XP
        mountManager.addMountToPlayer(playerUUID, mountId);
        
        // Set mount XP (use addMountXP since setMountXP doesn't exist)
        xpManager.addMountXP(playerUUID, mountId, combinedXP);
        
        // Clear selections
        selectedFirstMount.remove(playerUUID);
        selectedSecondMount.remove(playerUUID);
        
        // Show fusion result GUI
        showFusionResultGUI(player, mountType, currentRarity, newRarity, combinedXP);
    }
    
    /**
     * Gets the fusion cost for a rarity
     * 
     * @param rarity The rarity
     * @return The fusion cost
     */
    private int getFusionCost(MountRarity rarity) {
        return switch (rarity) {
            case COMMON -> 1000;
            case UNCOMMON -> 2500;
            case RARE -> 5000;
            case EPIC -> 10000;
            case LEGENDARY -> 25000;
            case MYTHIC -> 50000;
            default -> 1000;
        };
    }
    
    /**
     * Shows the fusion result GUI
     * 
     * @param player The player
     * @param mountType The mount type
     * @param oldRarity The old rarity
     * @param newRarity The new rarity
     * @param combinedXP The combined XP
     */
    private void showFusionResultGUI(Player player, MountType mountType, 
                                    MountRarity oldRarity, MountRarity newRarity, int combinedXP) {
        Inventory inventory = Bukkit.createInventory(null, 27, 
                ChatColor.GOLD + "Fusion Complete!");
        
        // Play sound
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        if (oldRarity != newRarity) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
        }
        
        // Fill with glass panes
        ItemStack glassFiller = createGuiItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glassFiller);
        }
        
        // Add result item
        Material material;
        switch (mountType.getId()) {
            case "phoenix_blaze" -> material = Material.BLAZE_POWDER;
            case "shadow_steed" -> material = Material.COAL;
            case "crystal_drake" -> material = Material.DIAMOND;
            case "storm_charger" -> material = Material.LIGHTNING_ROD;
            case "ancient_golem" -> material = Material.STONE;
            default -> material = Material.SADDLE;
        }
        
        ItemStack resultItem = new ItemStack(material);
        ItemMeta meta = resultItem.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName(newRarity.getColor() + mountType.getDisplayName());
            
            // Add lore
            List<String> lore = new ArrayList<>();
            
            if (oldRarity != newRarity) {
                lore.add(ChatColor.GREEN + "Rarity Upgraded!");
                lore.add(oldRarity.getColor() + oldRarity.name() + ChatColor.WHITE + " → " + 
                        newRarity.getColor() + newRarity.name());
            } else {
                lore.add(ChatColor.YELLOW + "Combined Successfully");
                lore.add(newRarity.getColor() + "Rarity: " + newRarity.name());
            }
            
            lore.add("");
            lore.add(ChatColor.YELLOW + "Combined XP: " + ChatColor.WHITE + combinedXP);
            // Use getMountLevel instead of calculateLevel
            int level = xpManager.getMountLevel(player.getUniqueId(), mountType.getId());
            lore.add(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + level);
            
            meta.setLore(lore);
            resultItem.setItemMeta(meta);
        }
        
        inventory.setItem(13, resultItem);
        
        // Add return button
        inventory.setItem(26, createGuiItem(Material.ARROW, ChatColor.GREEN + "Return", 
                "Click to return to fusion menu"));
        
        // Store active GUI reference
        activeGUIs.put(player.getUniqueId(), inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Register this inventory with the inventory manager to prevent item theft
        inventoryManager.registerInventory(player, ChatColor.GOLD + "Fusion Complete!");
    }
    
    /**
     * Creates an item for the GUI with custom name and lore
     * 
     * @param material The item material
     * @param name The item name
     * @param lore The item lore lines
     * @return The ItemStack
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
     * Handles a click in the mount fusion GUI
     * 
     * @param player The player
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
        
        // Handle main fusion GUI
        if (inventoryTitle.equals(ChatColor.DARK_PURPLE + "Mount Fusion")) {
            // Handle mount selection slots (10-44, excluding border slots)
            if (slot >= 10 && slot < 45 && slot % 9 != 0 && slot % 9 != 8) {
                handleMountSelection(player, slot);
                return true;
            }
            
            // Handle catalyst button
            if (slot == 49) {
                openCatalystGUI(player);
                return true;
            }
            
            // Handle fusion button
            if (slot == 31) {
                String firstMountId = selectedFirstMount.get(playerUUID);
                String secondMountId = selectedSecondMount.get(playerUUID);
                
                if (firstMountId != null && secondMountId != null && 
                        firstMountId.equals(secondMountId)) {
                    performFusion(player);
                }
                return true;
            }
            
            // Handle close button
            if (slot == 53) {
                player.closeInventory();
                return true;
            }
        }
        // Handle catalyst selection GUI
        else if (inventoryTitle.equals(ChatColor.GOLD + "Select Catalyst")) {
            // Handle catalyst slots
            for (int i = 0; i < Catalyst.values().length; i++) {
                if (slot == 10 + i) {
                    selectedCatalyst.put(playerUUID, Catalyst.values()[i].name());
                    openMainGUI(player);
                    return true;
                }
            }
            
            // Handle close button
            if (slot == 26) {
                openMainGUI(player);
                return true;
            }
        }
        // Handle fusion result GUI
        else if (inventoryTitle.equals(ChatColor.GOLD + "Fusion Complete!")) {
            // Handle return button
            if (slot == 26) {
                openMainGUI(player);
                return true;
            }
            
            // Block all other clicks
            return true;
        }
        
        return false;
    }
    
    /**
     * Handles mount selection in the fusion GUI
     * 
     * @param player The player
     * @param slot The clicked slot
     */
    private void handleMountSelection(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();
        Inventory inventory = activeGUIs.get(playerUUID);
        
        if (inventory == null) {
            return;
        }
        
        ItemStack item = inventory.getItem(slot);
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }
        
        // Find which mount was clicked
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        for (Map.Entry<String, MountType> entry : mountManager.getMountTypes().entrySet()) {
            MountType mountType = entry.getValue();
            
            if (displayName.startsWith(ChatColor.stripColor(mountType.getDisplayName()))) {
                // Handle mount selection
                String mountId = entry.getKey();
                
                if (selectedFirstMount.containsKey(playerUUID)) {
                    // Second mount selected
                    if (mountId.equals(selectedFirstMount.get(playerUUID))) {
                        // Same mount type, set as second mount
                        selectedSecondMount.put(playerUUID, mountId);
                        player.sendMessage(ChatColor.GREEN + "Second mount selected: " + 
                                mountType.getDisplayName());
                    } else {
                        // Different mount type, start over
                        selectedFirstMount.put(playerUUID, mountId);
                        selectedSecondMount.remove(playerUUID);
                        player.sendMessage(ChatColor.YELLOW + "First mount selected: " + 
                                mountType.getDisplayName());
                    }
                } else {
                    // First mount selected
                    selectedFirstMount.put(playerUUID, mountId);
                    player.sendMessage(ChatColor.YELLOW + "First mount selected: " + 
                            mountType.getDisplayName());
                }
                
                // Update fusion button
                inventory.setItem(31, createFusionButton(playerUUID));
                break;
            }
        }
    }
    
    /**
     * Handles inventory close events
     * 
     * @param player The player
     * @param inventoryTitle The title of the closed inventory
     */
    public void handleInventoryClose(Player player, String inventoryTitle) {
        // Unregister this inventory with the inventory manager
        inventoryManager.unregisterInventory(player, inventoryTitle);
        
        // Play sound
        if (inventoryTitle.equals(ChatColor.DARK_PURPLE + "Mount Fusion") || 
            inventoryTitle.equals(ChatColor.GOLD + "Select Catalyst") || 
            inventoryTitle.equals(ChatColor.GOLD + "Fusion Complete!")) {
            player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, 0.5f, 1.0f);
        }
    }
} 