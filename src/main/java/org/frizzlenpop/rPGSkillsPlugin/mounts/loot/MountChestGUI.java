package org.frizzlenpop.rPGSkillsPlugin.mounts.loot;

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
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountRarity;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Handles the Mount Chest GUI for opening mount chests.
 */
public class MountChestGUI {
    private final RPGSkillsPlugin plugin;
    private final MountKeyManager keyManager;
    private final MountManager mountManager;
    private final Map<UUID, Inventory> activeGUIs = new HashMap<>();
    private final Map<UUID, MountKeyManager.KeyTier> openingChests = new HashMap<>();
    private final Random random = new Random();
    
    /**
     * Creates a new mount chest GUI manager
     * 
     * @param plugin The plugin instance
     * @param keyManager The mount key manager
     * @param mountManager The mount manager
     */
    public MountChestGUI(RPGSkillsPlugin plugin, MountKeyManager keyManager, MountManager mountManager) {
        this.plugin = plugin;
        this.keyManager = keyManager;
        this.mountManager = mountManager;
    }
    
    /**
     * Opens the main chest GUI for a player
     * 
     * @param player The player
     */
    public void openMainGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Mount Chests");
        UUID playerUUID = player.getUniqueId();
        
        // Load player key data
        keyManager.loadPlayerKeysData(playerUUID);
        Map<MountKeyManager.KeyTier, Integer> playerKeys = keyManager.getAllPlayerKeys(playerUUID);
        
        // Add key items to inventory
        int slot = 10;
        for (MountKeyManager.KeyTier tier : MountKeyManager.KeyTier.values()) {
            int count = playerKeys.getOrDefault(tier, 0);
            
            // Create key display item
            ItemStack keyItem = createKeyDisplayItem(tier, count);
            inventory.setItem(slot, keyItem);
            
            // Increment slot, handling row wrapping
            slot += 2;
            if (slot % 9 == 8) {
                slot += 2; // Skip to next row
            }
        }
        
        // Add chest items
        slot = 11;
        for (MountKeyManager.KeyTier tier : MountKeyManager.KeyTier.values()) {
            // Create chest display item
            ItemStack chestItem = createChestDisplayItem(tier);
            inventory.setItem(slot, chestItem);
            
            // Increment slot, handling row wrapping
            slot += 2;
            if (slot % 9 == 8) {
                slot += 2; // Skip to next row
            }
        }
        
        // Add decorative and info items
        inventory.setItem(4, createGuiItem(Material.ENDER_CHEST, 
                ChatColor.GOLD + "Mount Chest System",
                ChatColor.GRAY + "Open chests to get special mounts",
                "",
                ChatColor.YELLOW + "Click on a chest to open it",
                ChatColor.YELLOW + "with the corresponding key."));
        
        inventory.setItem(49, createGuiItem(Material.PAPER, 
                ChatColor.AQUA + "Your Key Collection",
                "",
                ChatColor.WHITE + "Common: " + ChatColor.YELLOW + playerKeys.getOrDefault(MountKeyManager.KeyTier.COMMON, 0),
                ChatColor.GREEN + "Uncommon: " + ChatColor.YELLOW + playerKeys.getOrDefault(MountKeyManager.KeyTier.UNCOMMON, 0),
                ChatColor.BLUE + "Rare: " + ChatColor.YELLOW + playerKeys.getOrDefault(MountKeyManager.KeyTier.RARE, 0),
                ChatColor.LIGHT_PURPLE + "Epic: " + ChatColor.YELLOW + playerKeys.getOrDefault(MountKeyManager.KeyTier.EPIC, 0),
                ChatColor.GOLD + "Legendary: " + ChatColor.YELLOW + playerKeys.getOrDefault(MountKeyManager.KeyTier.LEGENDARY, 0)));
        
        inventory.setItem(53, createGuiItem(Material.BARRIER, ChatColor.RED + "Close", 
                "Click to close this menu"));
        
        // Store active GUI reference
        activeGUIs.put(playerUUID, inventory);
        
        // Open inventory for player
        player.openInventory(inventory);
    }
    
    /**
     * Handles the chest opening process
     * 
     * @param player The player
     * @param tier The key tier to use
     */
    public void openChest(Player player, MountKeyManager.KeyTier tier) {
        UUID playerUUID = player.getUniqueId();
        
        // Check if the player has a key of this tier
        if (keyManager.getPlayerKeyCount(playerUUID, tier) < 1) {
            player.sendMessage(ChatColor.RED + "You don't have any " + tier.getFormattedName() + 
                    ChatColor.RED + " keys!");
            return;
        }
        
        // Create the chest opening GUI
        Inventory inventory = Bukkit.createInventory(null, 27, 
                ChatColor.GOLD + "Opening " + tier.getFormattedName() + ChatColor.GOLD + " Chest");
        
        // Fill with glass panes
        ItemStack glassFiller = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glassFiller);
        }
        
        // Add closed chest in the middle
        inventory.setItem(13, createGuiItem(Material.CHEST, 
                ChatColor.GOLD + "Mount Chest",
                ChatColor.GRAY + "Opening..."));
        
        // Remove the key from the player
        keyManager.removePlayerKeys(playerUUID, tier, 1);
        
        // Store reference to opening process
        activeGUIs.put(playerUUID, inventory);
        openingChests.put(playerUUID, tier);
        
        // Open inventory for player
        player.openInventory(inventory);
        
        // Start the opening animation
        startChestOpeningAnimation(player, tier);
    }
    
    /**
     * Plays the chest opening animation and gives rewards
     * 
     * @param player The player
     * @param tier The key tier
     */
    private void startChestOpeningAnimation(Player player, MountKeyManager.KeyTier tier) {
        UUID playerUUID = player.getUniqueId();
        Inventory inventory = activeGUIs.get(playerUUID);
        
        // Pre-determine the reward
        MountRarity rarity = tier.rollRarity();
        String mountId = keyManager.rollMountType(tier);
        
        // Get mount type
        MountType mountType = mountManager.getMountType(mountId);
        if (mountType == null) {
            // Fallback to phoenix_blaze if the mount type doesn't exist
            mountId = "phoenix_blaze";
            mountType = mountManager.getMountType(mountId);
        }
        
        // Final references for the runnable
        final MountType finalMountType = mountType;
        final String finalMountId = mountId;
        
        // Start animation sequence
        new BukkitRunnable() {
            private int step = 0;
            private final int totalSteps = 10;
            
            @Override
            public void run() {
                if (step >= totalSteps) {
                    // Animation complete, show reward
                    completeChestOpening(player, tier, finalMountType, finalMountId, rarity);
                    cancel();
                    return;
                }
                
                // Check if player still has GUI open
                if (!activeGUIs.containsKey(playerUUID) || !openingChests.containsKey(playerUUID)) {
                    cancel();
                    return;
                }
                
                // Update animation
                updateChestAnimation(inventory, step, totalSteps, tier);
                
                // Play sound effects
                player.playSound(player.getLocation(), getAnimationSound(step, totalSteps), 1.0f, getAnimationPitch(step));
                
                step++;
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }
    
    /**
     * Updates the chest animation in the GUI
     * 
     * @param inventory The inventory
     * @param step Current animation step
     * @param totalSteps Total animation steps
     * @param tier Key tier
     */
    private void updateChestAnimation(Inventory inventory, int step, int totalSteps, MountKeyManager.KeyTier tier) {
        if (step < totalSteps / 2) {
            // Early animation: Chest opening sequence
            Material chestMaterial;
            if (step == 0) {
                chestMaterial = Material.CHEST;
            } else if (step == 1) {
                chestMaterial = Material.CHEST;
            } else {
                chestMaterial = Material.ENDER_CHEST;
            }
            
            inventory.setItem(13, createGuiItem(chestMaterial, 
                    ChatColor.GOLD + "Opening Mount Chest",
                    ChatColor.GRAY + "Wait for the reward..."));
            
            // Glass pane colors changing
            Material glassMaterial = getColoredGlass(step);
            ItemStack glass = createGuiItem(glassMaterial, " ");
            
            for (int i = 0; i < inventory.getSize(); i++) {
                if (i != 13) { // Keep the center slot with the chest
                    inventory.setItem(i, glass);
                }
            }
        } else {
            // Later animation: Particles and anticipation
            inventory.setItem(13, createGuiItem(Material.ENDER_CHEST, 
                    ChatColor.GOLD + "Mount Chest",
                    ChatColor.GRAY + "Almost there..."));
            
            // Flashing effect
            boolean flash = (step % 2 == 0);
            Material glassMaterial = flash ? 
                    getKeyGlassColor(tier) : Material.BLACK_STAINED_GLASS_PANE;
            
            ItemStack glass = createGuiItem(glassMaterial, " ");
            for (int i = 0; i < inventory.getSize(); i++) {
                if (i != 13) { // Keep the center slot
                    inventory.setItem(i, glass);
                }
            }
        }
    }
    
    /**
     * Completes the chest opening and gives rewards
     * 
     * @param player The player
     * @param tier The key tier
     * @param mountType The mount type
     * @param mountId The mount ID
     * @param rarity The mount rarity
     */
    private void completeChestOpening(Player player, MountKeyManager.KeyTier tier, 
                                     MountType mountType, String mountId, MountRarity rarity) {
        UUID playerUUID = player.getUniqueId();
        Inventory inventory = activeGUIs.get(playerUUID);
        
        if (inventory == null) {
            return;
        }
        
        // Play reward sound
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        
        // Check if player already owns the mount
        boolean alreadyOwned = mountManager.ownsMount(playerUUID, mountId);
        
        if (!alreadyOwned) {
            // Give the mount to the player
            mountManager.addMountToPlayer(playerUUID, mountId);
            
            // Update the GUI with the reward
            inventory.setItem(13, createMountRewardItem(mountType, rarity, false));
            
            // Send message
            player.sendMessage(ChatColor.GREEN + "You received a " + rarity.getFormattedName() + " " + 
                    mountType.getDisplayName() + ChatColor.GREEN + "!");
        } else {
            // Player already owns this mount - give alternative reward
            handleDuplicateMount(player, tier, mountType, rarity);
            
            // Update the GUI with the duplicate info
            inventory.setItem(13, createMountRewardItem(mountType, rarity, true));
            
            // Send message
            player.sendMessage(ChatColor.YELLOW + "You already own the " + mountType.getDisplayName() + 
                    ChatColor.YELLOW + ". You received an alternative reward instead.");
        }
        
        // Update surrounding slots
        Material glassMaterial = getKeyGlassColor(tier);
        ItemStack glass = createGuiItem(glassMaterial, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i != 13) { // Keep the center slot
                inventory.setItem(i, glass);
            }
        }
        
        // Add close button
        inventory.setItem(26, createGuiItem(Material.BARRIER, ChatColor.RED + "Close", 
                "Click to close this menu"));
        
        // Remove from opening chests map
        openingChests.remove(playerUUID);
    }
    
    /**
     * Handles the case when a player receives a duplicate mount
     * 
     * @param player The player
     * @param tier The key tier
     * @param mountType The mount type
     * @param rarity The mount rarity
     */
    private void handleDuplicateMount(Player player, MountKeyManager.KeyTier tier, 
                                     MountType mountType, MountRarity rarity) {
        // Options for duplicate handling:
        // 1. Give upgrade materials
        // 2. Give customization tokens
        // 3. Give partial coin refund
        
        // For now, implement a simple coin refund
        int refundAmount = calculateRefundAmount(tier, rarity);
        
        // Give coins to the player
        plugin.getEconomyManager().depositMoney(player, refundAmount);
        
        // Send message
        player.sendMessage(ChatColor.YELLOW + "You received " + refundAmount + " coins as compensation.");
    }
    
    /**
     * Calculates the refund amount for a duplicate mount
     * 
     * @param tier The key tier
     * @param rarity The mount rarity
     * @return The refund amount
     */
    private int calculateRefundAmount(MountKeyManager.KeyTier tier, MountRarity rarity) {
        // Base refund based on key tier
        int baseRefund;
        switch (tier) {
            case COMMON -> baseRefund = 1000;
            case UNCOMMON -> baseRefund = 2000;
            case RARE -> baseRefund = 4000;
            case EPIC -> baseRefund = 8000;
            case LEGENDARY -> baseRefund = 15000;
            default -> baseRefund = 1000;
        }
        
        // Multiply by rarity factor
        double rarityMultiplier;
        switch (rarity) {
            case COMMON -> rarityMultiplier = 1.0;
            case UNCOMMON -> rarityMultiplier = 1.5;
            case RARE -> rarityMultiplier = 2.0;
            case EPIC -> rarityMultiplier = 3.0;
            case LEGENDARY -> rarityMultiplier = 5.0;
            case MYTHIC -> rarityMultiplier = 10.0;
            default -> rarityMultiplier = 1.0;
        }
        
        return (int)(baseRefund * rarityMultiplier);
    }
    
    /**
     * Get a colored glass material based on animation step
     * 
     * @param step The animation step
     * @return The glass material
     */
    private Material getColoredGlass(int step) {
        return switch (step % 6) {
            case 0 -> Material.RED_STAINED_GLASS_PANE;
            case 1 -> Material.ORANGE_STAINED_GLASS_PANE;
            case 2 -> Material.YELLOW_STAINED_GLASS_PANE;
            case 3 -> Material.GREEN_STAINED_GLASS_PANE;
            case 4 -> Material.BLUE_STAINED_GLASS_PANE;
            case 5 -> Material.PURPLE_STAINED_GLASS_PANE;
            default -> Material.WHITE_STAINED_GLASS_PANE;
        };
    }
    
    /**
     * Get a glass color based on key tier
     * 
     * @param tier The key tier
     * @return The glass material
     */
    private Material getKeyGlassColor(MountKeyManager.KeyTier tier) {
        return switch (tier) {
            case COMMON -> Material.WHITE_STAINED_GLASS_PANE;
            case UNCOMMON -> Material.LIME_STAINED_GLASS_PANE;
            case RARE -> Material.BLUE_STAINED_GLASS_PANE;
            case EPIC -> Material.PURPLE_STAINED_GLASS_PANE;
            case LEGENDARY -> Material.ORANGE_STAINED_GLASS_PANE;
            default -> Material.WHITE_STAINED_GLASS_PANE;
        };
    }
    
    /**
     * Get a sound for the animation step
     * 
     * @param step The animation step
     * @param totalSteps Total steps
     * @return The sound
     */
    private Sound getAnimationSound(int step, int totalSteps) {
        if (step == 0) {
            return Sound.BLOCK_CHEST_OPEN;
        } else if (step == totalSteps - 1) {
            return Sound.ENTITY_PLAYER_LEVELUP;
        } else if (step > totalSteps / 2) {
            return Sound.BLOCK_NOTE_BLOCK_CHIME;
        } else {
            return Sound.BLOCK_NOTE_BLOCK_BELL;
        }
    }
    
    /**
     * Get a pitch value for the animation sound
     * 
     * @param step The animation step
     * @return The pitch
     */
    private float getAnimationPitch(int step) {
        return 0.5f + ((float) step / 10);
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
     * Creates a key display item for the GUI
     * 
     * @param tier The key tier
     * @param count The number of keys the player has
     * @return The created ItemStack
     */
    private ItemStack createKeyDisplayItem(MountKeyManager.KeyTier tier, int count) {
        ItemStack item = new ItemStack(tier.getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName(tier.getColor() + tier.getDisplayName() + 
                    ChatColor.GRAY + " x" + count);
            
            // Add lore
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "You have " + count + " " + tier.getDisplayName() + "(s)");
            lore.add("");
            
            // Add drop chances info
            lore.add(ChatColor.YELLOW + "Mount Rarities:");
            for (Map.Entry<MountRarity, Double> entry : tier.getRarityDropChances().entrySet()) {
                if (entry.getValue() > 0) {
                    lore.add(entry.getKey().getColor() + " " + entry.getKey().name() + ": " + 
                            ChatColor.WHITE + entry.getValue() + "%");
                }
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Creates a chest display item for the GUI
     * 
     * @param tier The key tier
     * @return The created ItemStack
     */
    private ItemStack createChestDisplayItem(MountKeyManager.KeyTier tier) {
        Material chestMaterial;
        switch (tier) {
            case COMMON -> chestMaterial = Material.CHEST;
            case UNCOMMON -> chestMaterial = Material.CHEST;
            case RARE -> chestMaterial = Material.CHEST;
            case EPIC -> chestMaterial = Material.ENDER_CHEST;
            case LEGENDARY -> chestMaterial = Material.ENDER_CHEST;
            default -> chestMaterial = Material.CHEST;
        }
        
        ItemStack item = new ItemStack(chestMaterial);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName(tier.getColor() + tier.getDisplayName() + " Chest");
            
            // Add lore
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Open with a " + tier.getDisplayName());
            lore.add("");
            lore.add(ChatColor.YELLOW + "Contains one of:");
            lore.add(ChatColor.GRAY + "- Phoenix Blaze");
            lore.add(ChatColor.GRAY + "- Shadow Steed");
            lore.add(ChatColor.GRAY + "- Crystal Drake");
            lore.add(ChatColor.GRAY + "- Storm Charger");
            lore.add(ChatColor.GRAY + "- Ancient Golem");
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to open with a " + tier.getDisplayName());
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Creates a mount reward item
     * 
     * @param mountType The mount type
     * @param rarity The mount rarity
     * @param isDuplicate Whether this is a duplicate mount
     * @return The created ItemStack
     */
    private ItemStack createMountRewardItem(MountType mountType, MountRarity rarity, boolean isDuplicate) {
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
            String displayName;
            if (isDuplicate) {
                displayName = ChatColor.YELLOW + "Duplicate: " + rarity.getColor() + mountType.getDisplayName();
            } else {
                displayName = rarity.getColor() + mountType.getDisplayName();
            }
            meta.setDisplayName(displayName);
            
            // Add lore
            List<String> lore = new ArrayList<>();
            if (isDuplicate) {
                lore.add(ChatColor.GRAY + "You already own this mount");
                lore.add(ChatColor.GRAY + "You received compensation instead.");
                lore.add("");
            }
            
            lore.add(ChatColor.GRAY + mountType.getDescription());
            lore.add("");
            lore.add(rarity.getColor() + "Rarity: " + rarity.getFormattedName());
            lore.add("");
            if (!isDuplicate) {
                lore.add(ChatColor.GREEN + "Use '/mount summon " + mountType.getId() + "' to ride it!");
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Handles a click in the mount chest GUI
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
        if (inventoryTitle.equals(ChatColor.DARK_PURPLE + "Mount Chests")) {
            // Handle chest slots (11, 13, 15, 17, 19, 21, 23, 25)
            int[] chestSlots = {11, 13, 15, 17, 19};
            for (int i = 0; i < chestSlots.length; i++) {
                if (slot == chestSlots[i] && i < MountKeyManager.KeyTier.values().length) {
                    MountKeyManager.KeyTier tier = MountKeyManager.KeyTier.values()[i];
                    openChest(player, tier);
                    return true;
                }
            }
            
            // Close button
            if (slot == 53) {
                player.closeInventory();
                return true;
            }
        }
        // Handle chest opening GUI
        else if (inventoryTitle.contains("Opening") && inventoryTitle.contains("Chest")) {
            // Prevent all clicks during opening animation
            if (openingChests.containsKey(playerUUID)) {
                return true;
            }
            
            // Close button (only available after opening is complete)
            if (slot == 26) {
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
        
        // If the player closes during opening, they still lose their key
        // but the animation is canceled
        openingChests.remove(playerUUID);
    }
} 