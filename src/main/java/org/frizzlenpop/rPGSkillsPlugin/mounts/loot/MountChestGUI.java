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
import org.frizzlenpop.rPGSkillsPlugin.gui.InventoryManager;
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
    private final InventoryManager inventoryManager;
    private final Map<UUID, Inventory> activeGUIs = new HashMap<>();
    private final Map<UUID, MountKeyManager.KeyTier> openingChests = new HashMap<>();
    private final Random random = new Random();
    
    // GUI titles
    private static final String MAIN_GUI_TITLE = ChatColor.DARK_PURPLE + "Mount Chests";
    private static final String OPENING_GUI_TITLE = ChatColor.DARK_PURPLE + "Opening Chest...";
    private static final String RESULT_GUI_TITLE = ChatColor.DARK_PURPLE + "Chest Result";
    
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
        this.inventoryManager = plugin.getInventoryManager();
    }
    
    /**
     * Opens the main chest GUI for a player
     * 
     * @param player The player
     */
    public void openMainGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, MAIN_GUI_TITLE);
        UUID playerUUID = player.getUniqueId();
        
        // Load player key data
        Map<MountKeyManager.KeyTier, Integer> playerKeys = keyManager.getPlayerKeys(playerUUID);
        
        // Add key items
        inventory.setItem(20, createKeyItem(MountKeyManager.KeyTier.COMMON, playerKeys.getOrDefault(MountKeyManager.KeyTier.COMMON, 0)));
        inventory.setItem(22, createKeyItem(MountKeyManager.KeyTier.RARE, playerKeys.getOrDefault(MountKeyManager.KeyTier.RARE, 0)));
        inventory.setItem(24, createKeyItem(MountKeyManager.KeyTier.EPIC, playerKeys.getOrDefault(MountKeyManager.KeyTier.EPIC, 0)));
        
        // Add chest items
        inventory.setItem(38, createChestItem(MountKeyManager.KeyTier.COMMON));
        inventory.setItem(40, createChestItem(MountKeyManager.KeyTier.RARE));
        inventory.setItem(42, createChestItem(MountKeyManager.KeyTier.EPIC));
        
        // Add info item
        inventory.setItem(4, createGuiItem(
                Material.BOOK,
                ChatColor.GOLD + "Mount Chests",
                ChatColor.GRAY + "Use keys to open chests and",
                ChatColor.GRAY + "obtain new mounts!",
                "",
                ChatColor.YELLOW + "Instructions:",
                ChatColor.WHITE + "1. Click on a chest to open it",
                ChatColor.WHITE + "2. You must have a matching key",
                ChatColor.WHITE + "3. Higher tier chests give better mounts"
        ));
        
        // Add back button
        inventory.setItem(49, createGuiItem(
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
        
        // Store active GUI
        activeGUIs.put(playerUUID, inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Register this inventory with the inventory manager to prevent item theft
        inventoryManager.registerInventory(player, MAIN_GUI_TITLE);
        
        // Play sound
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
    }
    
    /**
     * Opens a chest for a player
     * 
     * @param player The player
     * @param tier The chest tier
     * @return True if the chest was opened, false otherwise
     */
    public boolean openChest(Player player, MountKeyManager.KeyTier tier) {
        UUID playerUUID = player.getUniqueId();
        
        // Check if player has a key
        if (!keyManager.hasKey(playerUUID, tier)) {
            player.sendMessage(ChatColor.RED + "You don't have a " + tier.getDisplayName() + " key!");
            return false;
        }
        
        // Use key
        keyManager.useKey(playerUUID, tier);
        
        // Start chest opening animation
        startChestOpeningAnimation(player, tier);
        
        return true;
    }
    
    /**
     * Starts the chest opening animation
     * 
     * @param player The player
     * @param tier The chest tier
     */
    private void startChestOpeningAnimation(Player player, MountKeyManager.KeyTier tier) {
        UUID playerUUID = player.getUniqueId();
        
        // Create animation inventory
        Inventory inventory = Bukkit.createInventory(null, 27, OPENING_GUI_TITLE);
        
        // Fill with glass panes
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
        
        // Add chest in the middle
        inventory.setItem(13, createGuiItem(
                Material.CHEST,
                ChatColor.GOLD + "Opening " + tier.getDisplayName() + " Chest...",
                ChatColor.GRAY + "Please wait..."
        ));
        
        // Store active GUI and opening chest
        activeGUIs.put(playerUUID, inventory);
        openingChests.put(playerUUID, tier);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Register this inventory with the inventory manager to prevent item theft
        inventoryManager.registerInventory(player, OPENING_GUI_TITLE);
        
        // Play sound
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 0.8f);
        
        // Start animation
        new BukkitRunnable() {
            private int tick = 0;
            private final Material[] materials = {
                    Material.BLACK_STAINED_GLASS_PANE,
                    Material.BLUE_STAINED_GLASS_PANE,
                    Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                    Material.CYAN_STAINED_GLASS_PANE,
                    Material.GREEN_STAINED_GLASS_PANE,
                    Material.LIME_STAINED_GLASS_PANE,
                    Material.YELLOW_STAINED_GLASS_PANE,
                    Material.ORANGE_STAINED_GLASS_PANE,
                    Material.RED_STAINED_GLASS_PANE,
                    Material.MAGENTA_STAINED_GLASS_PANE,
                    Material.PURPLE_STAINED_GLASS_PANE
            };
            
            @Override
            public void run() {
                // Check if player still has GUI open
                if (!activeGUIs.containsKey(playerUUID) || tick >= 40) {
                    // Animation complete, show result
                    showChestResult(player, tier);
                    this.cancel();
                    return;
                }
                
                // Update animation
                Inventory inv = activeGUIs.get(playerUUID);
                
                // Update border
                Material material = materials[tick % materials.length];
                for (int i = 0; i < 27; i++) {
                    if (i != 13) { // Skip the chest
                        inv.setItem(i, createGuiItem(material, " "));
                    }
                }
                
                // Play sound every few ticks
                if (tick % 5 == 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 0.5f + (tick / 40f));
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 5L, 2L);
    }
    
    /**
     * Shows the chest opening result
     * 
     * @param player The player
     * @param tier The chest tier
     */
    private void showChestResult(Player player, MountKeyManager.KeyTier tier) {
        UUID playerUUID = player.getUniqueId();
        
        // Determine mount reward
        MountType rewardMount = determineReward(tier);
        MountRarity rewardRarity = determineRewardRarity(tier);
        
        // Add mount to player
        mountManager.addMountToPlayer(playerUUID, rewardMount.getId());
        
        // Create result inventory
        Inventory inventory = Bukkit.createInventory(null, 27, RESULT_GUI_TITLE);
        
        // Fill with glass panes based on rarity
        Material glassMaterial;
        switch (rewardRarity) {
            case LEGENDARY:
                glassMaterial = Material.YELLOW_STAINED_GLASS_PANE;
                break;
            case EPIC:
                glassMaterial = Material.PURPLE_STAINED_GLASS_PANE;
                break;
            case RARE:
                glassMaterial = Material.BLUE_STAINED_GLASS_PANE;
                break;
            case UNCOMMON:
                glassMaterial = Material.GREEN_STAINED_GLASS_PANE;
                break;
            default:
                glassMaterial = Material.WHITE_STAINED_GLASS_PANE;
                break;
        }
        
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, createGuiItem(glassMaterial, " "));
        }
        
        // Add mount item in the middle
        ItemStack mountItem = createMountRewardItem(rewardMount, rewardRarity);
        inventory.setItem(13, mountItem);
        
        // Add continue button
        inventory.setItem(22, createGuiItem(
                Material.ARROW,
                ChatColor.GREEN + "Continue",
                ChatColor.GRAY + "Click to return to the chest menu"
        ));
        
        // Store active GUI
        activeGUIs.put(playerUUID, inventory);
        openingChests.remove(playerUUID);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Register this inventory with the inventory manager to prevent item theft
        inventoryManager.registerInventory(player, RESULT_GUI_TITLE);
        
        // Play sound based on rarity
        Sound sound;
        float pitch;
        
        switch (rewardRarity) {
            case LEGENDARY:
                sound = Sound.UI_TOAST_CHALLENGE_COMPLETE;
                pitch = 1.0f;
                break;
            case EPIC:
                sound = Sound.ENTITY_PLAYER_LEVELUP;
                pitch = 1.2f;
                break;
            case RARE:
                sound = Sound.ENTITY_PLAYER_LEVELUP;
                pitch = 1.0f;
                break;
            case UNCOMMON:
                sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                pitch = 1.2f;
                break;
            default:
                sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                pitch = 1.0f;
                break;
        }
        
        player.playSound(player.getLocation(), sound, 1.0f, pitch);
        
        // Announce legendary mounts to all players
        if (rewardRarity == MountRarity.LEGENDARY) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "✦ " + ChatColor.YELLOW + player.getName() + 
                    ChatColor.GOLD + " has obtained a " + ChatColor.YELLOW + "LEGENDARY " + 
                    rewardMount.getDisplayName() + ChatColor.GOLD + "! ✦");
        }
    }
    
    /**
     * Handles inventory click events
     * 
     * @param player The player
     * @param inventoryTitle The title of the clicked inventory
     * @param slot The clicked slot
     * @return True if the click was handled
     */
    public boolean handleInventoryClick(Player player, String inventoryTitle, int slot) {
        UUID playerUUID = player.getUniqueId();
        
        // Check if this is the main GUI
        if (inventoryTitle.equals(MAIN_GUI_TITLE)) {
            // Handle chest clicks
            if (slot == 38) {
                return openChest(player, MountKeyManager.KeyTier.COMMON);
            } else if (slot == 40) {
                return openChest(player, MountKeyManager.KeyTier.RARE);
            } else if (slot == 42) {
                return openChest(player, MountKeyManager.KeyTier.EPIC);
            }
            
            // Handle back button
            if (slot == 49) {
                player.closeInventory();
                player.performCommand("rpghub");
                return true;
            }
        }
        // Check if this is the result GUI
        else if (inventoryTitle.equals(RESULT_GUI_TITLE)) {
            // Handle continue button
            if (slot == 22) {
                player.closeInventory();
                openMainGUI(player);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Handles inventory close events
     * 
     * @param player The player
     * @param inventoryTitle The title of the closed inventory
     */
    public void handleInventoryClose(Player player, String inventoryTitle) {
        UUID playerUUID = player.getUniqueId();
        
        // Clean up
        activeGUIs.remove(playerUUID);
        
        // If closing during animation, complete it immediately
        if (openingChests.containsKey(playerUUID)) {
            MountKeyManager.KeyTier tier = openingChests.get(playerUUID);
            showChestResult(player, tier);
        }
        
        // Unregister this inventory with the inventory manager
        inventoryManager.unregisterInventory(player, inventoryTitle);
        
        // Play sound
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.5f, 1.0f);
    }
    
    /**
     * Handles inventory close events (alternative method for UUID parameter)
     * 
     * @param playerUUID The player's UUID
     */
    public void handleInventoryClose(UUID playerUUID) {
        // Clean up
        activeGUIs.remove(playerUUID);
        openingChests.remove(playerUUID);
        
        // Get player and title if possible
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            // Unregister all possible inventories
            inventoryManager.unregisterInventory(player, MAIN_GUI_TITLE);
            inventoryManager.unregisterInventory(player, OPENING_GUI_TITLE);
            inventoryManager.unregisterInventory(player, RESULT_GUI_TITLE);
            
            // Play sound
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.5f, 1.0f);
        }
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
    private ItemStack createKeyItem(MountKeyManager.KeyTier tier, int count) {
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
    private ItemStack createChestItem(MountKeyManager.KeyTier tier) {
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
     * @return The created ItemStack
     */
    private ItemStack createMountRewardItem(MountType mountType, MountRarity rarity) {
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
            meta.setDisplayName(rarity.getColor() + mountType.getDisplayName());
            
            // Add lore
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + mountType.getDescription());
            lore.add("");
            lore.add(rarity.getColor() + "Rarity: " + rarity.getFormattedName());
            lore.add("");
            lore.add(ChatColor.GREEN + "Use '/mount summon " + mountType.getId() + "' to ride it!");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Determines the reward mount based on the chest tier
     * 
     * @param tier The chest tier
     * @return The reward mount
     */
    private MountType determineReward(MountKeyManager.KeyTier tier) {
        // Get all available mount types from the mount manager
        Map<String, MountType> availableMounts = mountManager.getMountTypes();
        
        // If no mounts are available, return null (this should never happen)
        if (availableMounts.isEmpty()) {
            plugin.getLogger().warning("No mount types available for chest rewards!");
            return null;
        }
        
        // Get a list of mount IDs
        List<String> mountIds = new ArrayList<>(availableMounts.keySet());
        
        // Select a random mount ID
        String randomMountId = mountIds.get(random.nextInt(mountIds.size()));
        
        // Return the corresponding mount type
        return availableMounts.get(randomMountId);
    }
    
    /**
     * Determines the reward rarity based on the chest tier
     * 
     * @param tier The chest tier
     * @return The reward rarity
     */
    private MountRarity determineRewardRarity(MountKeyManager.KeyTier tier) {
        // Use the key tier's built-in rarity roll method
        return tier.rollRarity();
    }
} 