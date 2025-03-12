package org.frizzlenpop.rPGSkillsPlugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.data.EconomyManager;
import org.frizzlenpop.rPGSkillsPlugin.data.PartyManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * GUI for purchasing and managing party perks
 */
public class PartyPerksGUI implements Listener {
    private final RPGSkillsPlugin plugin;
    private final PartyManager partyManager;
    private final EconomyManager economyManager;
    
    // Map of perk IDs to costs
    private final Map<String, Double> perkCosts = new HashMap<>();
    
    // Map of perk IDs to party leaders who have purchased them
    private final Map<String, Set<UUID>> purchasedPerks = new HashMap<>();
    
    // Map of perk IDs to display items
    private final Map<String, ItemStack> perkItems = new HashMap<>();
    
    // GUI constants
    private static final String GUI_TITLE = "Party Perks";
    private static final int GUI_SIZE = 36; // 4 rows
    
    // Configuration file
    private File perksFile;
    private FileConfiguration perksConfig;
    
    /**
     * Creates a new PartyPerksGUI
     * 
     * @param plugin The plugin instance
     * @param partyManager The party manager
     * @param economyManager The economy manager
     */
    public PartyPerksGUI(RPGSkillsPlugin plugin, PartyManager partyManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
        this.economyManager = economyManager;
        
        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // Initialize perks
        initializePerks();
        
        // Load purchased perks from configuration
        loadPurchasedPerks();
    }
    
    /**
     * Initializes the available perks
     */
    private void initializePerks() {
        // XP boost perk: +5% party XP
        perkCosts.put("xp_boost_1", 5000.0);
        perkItems.put("xp_boost_1", createPerkItem(
            Material.EXPERIENCE_BOTTLE,
            "XP Boost I",
            "Increases party XP gain by 5%",
            5000.0
        ));
        
        // XP boost perk: +10% party XP
        perkCosts.put("xp_boost_2", 15000.0);
        perkItems.put("xp_boost_2", createPerkItem(
            Material.EXPERIENCE_BOTTLE,
            "XP Boost II",
            "Increases party XP gain by 10%",
            15000.0
        ));
        
        // XP boost perk: +15% party XP
        perkCosts.put("xp_boost_3", 30000.0);
        perkItems.put("xp_boost_3", createPerkItem(
            Material.EXPERIENCE_BOTTLE,
            "XP Boost III",
            "Increases party XP gain by 15%",
            30000.0
        ));
        
        // Party size perk: Increases max party size by 1
        perkCosts.put("party_size_1", 10000.0);
        perkItems.put("party_size_1", createPerkItem(
            Material.PLAYER_HEAD,
            "Party Size I",
            "Increases max party size by 1",
            10000.0
        ));
        
        // Party size perk: Increases max party size by 2
        perkCosts.put("party_size_2", 25000.0);
        perkItems.put("party_size_2", createPerkItem(
            Material.PLAYER_HEAD,
            "Party Size II",
            "Increases max party size by 2",
            25000.0
        ));
        
        // Party glow perk: Makes party members glow for each other
        perkCosts.put("party_glow", 20000.0);
        perkItems.put("party_glow", createPerkItem(
            Material.GLOWSTONE,
            "Party Glow",
            "Makes party members glow for each other",
            20000.0
        ));
        
        // Party teleport perk: Allow members to teleport to the party leader
        perkCosts.put("party_teleport", 35000.0);
        perkItems.put("party_teleport", createPerkItem(
            Material.ENDER_PEARL,
            "Party Teleport",
            "Allows members to teleport to the party leader",
            35000.0
        ));
        
        // Party chat perk: Enables private party chat
        perkCosts.put("party_chat", 8000.0);
        perkItems.put("party_chat", createPerkItem(
            Material.WRITABLE_BOOK,
            "Party Chat",
            "Enables private party chat",
            8000.0
        ));
        
        // Offline XP perk: Party XP share for offline members
        perkCosts.put("offline_xp", 40000.0);
        perkItems.put("offline_xp", createPerkItem(
            Material.CLOCK,
            "Offline XP",
            "Party members receive XP share even when offline",
            40000.0
        ));
    }
    
    /**
     * Creates an item stack for a perk
     * 
     * @param material The material to use for the item
     * @param name The name of the perk
     * @param description The description of the perk
     * @param cost The cost of the perk
     * @return The item stack
     */
    private ItemStack createPerkItem(Material material, String name, String description, double cost) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + name);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + description);
            lore.add("");
            lore.add(ChatColor.YELLOW + "Cost: " + ChatColor.GREEN + economyManager.formatCurrency(cost));
            lore.add(ChatColor.YELLOW + "Click to purchase!");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Loads purchased perks from configuration
     */
    private void loadPurchasedPerks() {
        // Initialize empty sets for all perks
        for (String perkId : perkCosts.keySet()) {
            purchasedPerks.put(perkId, new HashSet<>());
        }
        
        // Load perks from configuration file
        perksFile = new File(plugin.getDataFolder(), "party_perks.yml");
        
        if (!perksFile.exists()) {
            plugin.saveResource("party_perks.yml", false);
        }
        
        perksConfig = YamlConfiguration.loadConfiguration(perksFile);
        
        // Load purchased perks for each perk ID
        for (String perkId : perkCosts.keySet()) {
            List<String> uuidStrings = perksConfig.getStringList("purchased_perks." + perkId);
            Set<UUID> owners = new HashSet<>();
            
            for (String uuidString : uuidStrings) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    owners.add(uuid);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in perks file: " + uuidString);
                }
            }
            
            purchasedPerks.put(perkId, owners);
        }
    }
    
    /**
     * Saves purchased perks to configuration
     */
    public void savePurchasedPerks() {
        // Save purchased perks for each perk ID
        for (Map.Entry<String, Set<UUID>> entry : purchasedPerks.entrySet()) {
            String perkId = entry.getKey();
            Set<UUID> owners = entry.getValue();
            
            List<String> uuidStrings = owners.stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
            
            perksConfig.set("purchased_perks." + perkId, uuidStrings);
        }
        
        // Save configuration
        try {
            perksConfig.save(perksFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save party perks to " + perksFile);
            e.printStackTrace();
        }
    }
    
    /**
     * Opens the party perks GUI for a player
     * 
     * @param player The player to open the GUI for
     */
    public void openPerksMenu(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        // Check if player is in a party
        if (!partyManager.isInParty(playerUUID)) {
            player.sendMessage(ChatColor.RED + "You must be in a party to use this command.");
            return;
        }
        
        // Check if player is the party leader
        UUID leaderUUID = partyManager.getPartyLeader(playerUUID);
        boolean isLeader = playerUUID.equals(leaderUUID);
        
        // Create inventory
        Inventory inv = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);
        
        // Add perks
        int slot = 0;
        for (Map.Entry<String, ItemStack> entry : perkItems.entrySet()) {
            String perkId = entry.getKey();
            ItemStack baseItem = entry.getValue();
            
            // Clone the item so we can modify it
            ItemStack item = baseItem.clone();
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                // Check if perk is already purchased
                boolean purchased = purchasedPerks.containsKey(perkId) && 
                                    purchasedPerks.get(perkId).contains(leaderUUID);
                
                List<String> lore = meta.getLore();
                if (lore == null) {
                    lore = new ArrayList<>();
                }
                
                if (purchased) {
                    // Update display for purchased perks
                    meta.setDisplayName(ChatColor.GREEN + meta.getDisplayName().substring(2)); // Remove gold color
                    lore.set(lore.size() - 2, ChatColor.GREEN + "Purchased");
                    lore.set(lore.size() - 1, ChatColor.GREEN + "This perk is active!");
                    
                    // Add enchanted glow effect
                    // item.addEnchantment(Enchantment.DURABILITY, 1);
                    // meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                } else if (!isLeader) {
                    // If player is not the leader, they can't purchase perks
                    lore.set(lore.size() - 1, ChatColor.RED + "Only the party leader can purchase perks");
                }
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            inv.setItem(slot++, item);
        }
        
        // Add information about the party
        ItemStack infoItem = createInfoItem(player, leaderUUID);
        inv.setItem(31, infoItem);
        
        // Add close button
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(ChatColor.RED + "Close");
            closeButton.setItemMeta(closeMeta);
        }
        inv.setItem(35, closeButton);
        
        // Open the inventory
        player.openInventory(inv);
    }
    
    /**
     * Creates an information item about the party
     * 
     * @param player The player to create the item for
     * @param leaderUUID The UUID of the party leader
     * @return The information item
     */
    private ItemStack createInfoItem(Player player, UUID leaderUUID) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Party Information");
            
            int partyLevel = partyManager.getPartyLevel(leaderUUID);
            long totalXp = partyManager.getPartyTotalSharedXp(leaderUUID);
            long nextLevelXp = partyManager.getXpForNextLevel(leaderUUID);
            double bonusPercent = partyManager.getPartyBonusPercent(leaderUUID);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Party Level: " + ChatColor.GREEN + partyLevel);
            lore.add(ChatColor.YELLOW + "XP Bonus: " + ChatColor.GREEN + 
                    String.format("%.0f%%", bonusPercent * 100));
            
            if (nextLevelXp > 0) {
                double progress = (double) totalXp / nextLevelXp;
                lore.add(ChatColor.YELLOW + "Progress: " + ChatColor.GREEN + 
                        String.format("%.1f%%", progress * 100));
                lore.add(ChatColor.YELLOW + "XP: " + ChatColor.GREEN + 
                        totalXp + "/" + nextLevelXp);
            } else {
                lore.add(ChatColor.YELLOW + "Max Level Reached!");
                lore.add(ChatColor.YELLOW + "Total XP: " + ChatColor.GREEN + totalXp);
            }
            
            // Show player's balance if they're the leader
            if (player.getUniqueId().equals(leaderUUID) && economyManager.isEconomyEnabled()) {
                lore.add("");
                lore.add(ChatColor.YELLOW + "Your Balance: " + ChatColor.GREEN + 
                        economyManager.formatCurrency(economyManager.getBalance(player)));
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Handles clicking on the perks menu
     * 
     * @param event The inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        
        // Check if this is our GUI
        if (!event.getView().getTitle().equals(GUI_TITLE)) {
            return;
        }
        
        // Cancel the event to prevent item movement
        event.setCancelled(true);
        
        // Handle clicking on the close button
        if (event.getSlot() == 35) {
            player.closeInventory();
            return;
        }
        
        // Get the item that was clicked
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        // Check if player is in a party
        UUID playerUUID = player.getUniqueId();
        if (!partyManager.isInParty(playerUUID)) {
            player.sendMessage(ChatColor.RED + "You must be in a party to use this command.");
            player.closeInventory();
            return;
        }
        
        // Check if player is the party leader
        UUID leaderUUID = partyManager.getPartyLeader(playerUUID);
        if (!playerUUID.equals(leaderUUID)) {
            player.sendMessage(ChatColor.RED + "Only the party leader can purchase perks.");
            return;
        }
        
        // Find which perk was clicked
        String clickedPerkId = null;
        for (Map.Entry<String, ItemStack> entry : perkItems.entrySet()) {
            if (clickedItem.getType() == entry.getValue().getType() && 
                clickedItem.getItemMeta() != null && 
                entry.getValue().getItemMeta() != null &&
                ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName())
                    .equals(ChatColor.stripColor(entry.getValue().getItemMeta().getDisplayName()))) {
                clickedPerkId = entry.getKey();
                break;
            }
        }
        
        if (clickedPerkId == null) {
            return;
        }
        
        // Check if perk is already purchased
        if (purchasedPerks.containsKey(clickedPerkId) && 
            purchasedPerks.get(clickedPerkId).contains(leaderUUID)) {
            player.sendMessage(ChatColor.YELLOW + "Your party already has this perk!");
            return;
        }
        
        // Check if economy is enabled
        if (!economyManager.isEconomyEnabled()) {
            player.sendMessage(ChatColor.RED + "Economy is not enabled on this server!");
            return;
        }
        
        // Get the cost of the perk
        double cost = perkCosts.getOrDefault(clickedPerkId, 0.0);
        
        // Check if player has enough money
        if (economyManager.getBalance(player) < cost) {
            player.sendMessage(ChatColor.RED + "You don't have enough money to purchase this perk! " +
                               "You need " + economyManager.formatCurrency(cost) + ".");
            return;
        }
        
        // Withdraw the money
        if (!economyManager.withdrawMoney(player, cost)) {
            player.sendMessage(ChatColor.RED + "Failed to withdraw money!");
            return;
        }
        
        // Purchase the perk
        Set<UUID> owners = purchasedPerks.getOrDefault(clickedPerkId, new HashSet<>());
        owners.add(leaderUUID);
        purchasedPerks.put(clickedPerkId, owners);
        
        // Save purchased perks
        savePurchasedPerks();
        
        // Apply perk effect
        applyPerkEffect(clickedPerkId, leaderUUID, true);
        
        // Notify player
        player.sendMessage(ChatColor.GREEN + "You have purchased the " + 
                          ChatColor.GOLD + ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()) + 
                          ChatColor.GREEN + " perk for your party!");
        
        // Update the GUI
        openPerksMenu(player);
    }
    
    /**
     * Applies the effect of a perk
     * 
     * @param perkId The ID of the perk to apply
     * @param leaderUUID The UUID of the party leader
     * @param purchased Whether the perk was just purchased
     */
    public void applyPerkEffect(String perkId, UUID leaderUUID, boolean purchased) {
        // Get all party members
        Set<UUID> members = partyManager.getPartyMembers(leaderUUID);
        
        // Apply perk effect based on perk ID
        if (perkId.startsWith("xp_boost_")) {
            // Handle XP boost perks - this is handled by PartyManager.calculateSharedXp()
            // Just notify the party members if the perk was just purchased
            if (purchased) {
                for (UUID memberUUID : members) {
                    Player member = Bukkit.getPlayer(memberUUID);
                    if (member != null) {
                        int boostLevel = Integer.parseInt(perkId.substring(perkId.length() - 1));
                        int boostPercent = boostLevel * 5;
                        member.sendMessage(ChatColor.GREEN + "Your party now has " + 
                                          ChatColor.GOLD + "XP Boost " + toRoman(boostLevel) + 
                                          ChatColor.GREEN + "! Party XP gain increased by " + 
                                          boostPercent + "%.");
                    }
                }
            }
        } else if (perkId.startsWith("party_size_")) {
            // Handle party size perks - this is handled by PartyManager.invitePlayer()
            // Just notify the party members if the perk was just purchased
            if (purchased) {
                for (UUID memberUUID : members) {
                    Player member = Bukkit.getPlayer(memberUUID);
                    if (member != null) {
                        int sizeLevel = Integer.parseInt(perkId.substring(perkId.length() - 1));
                        int extraSize = sizeLevel;
                        member.sendMessage(ChatColor.GREEN + "Your party now has " + 
                                          ChatColor.GOLD + "Party Size " + toRoman(sizeLevel) + 
                                          ChatColor.GREEN + "! Max party size increased by " + 
                                          extraSize + ".");
                    }
                }
            }
        } else if (perkId.equals("party_glow")) {
            // Handle party glow perk
            if (purchased) {
                for (UUID memberUUID : members) {
                    Player member = Bukkit.getPlayer(memberUUID);
                    if (member != null) {
                        member.sendMessage(ChatColor.GREEN + "Your party now has " + 
                                          ChatColor.GOLD + "Party Glow" + 
                                          ChatColor.GREEN + "! Party members will glow for each other.");
                    }
                }
            }
            
            // Apply glow effect to all online members
            applyGlowEffect(leaderUUID);
        } else if (perkId.equals("party_teleport")) {
            // Handle party teleport perk - this is handled by PartyCommand.handleTeleport()
            if (purchased) {
                for (UUID memberUUID : members) {
                    Player member = Bukkit.getPlayer(memberUUID);
                    if (member != null) {
                        member.sendMessage(ChatColor.GREEN + "Your party now has " + 
                                          ChatColor.GOLD + "Party Teleport" + 
                                          ChatColor.GREEN + "! Members can teleport to the party leader.");
                    }
                }
            }
        } else if (perkId.equals("party_chat")) {
            // Handle party chat perk - this is handled by a chat listener
            if (purchased) {
                for (UUID memberUUID : members) {
                    Player member = Bukkit.getPlayer(memberUUID);
                    if (member != null) {
                        member.sendMessage(ChatColor.GREEN + "Your party now has " + 
                                          ChatColor.GOLD + "Party Chat" + 
                                          ChatColor.GREEN + "! Use /rpartychat or /rpc to chat with party members.");
                    }
                }
            }
        } else if (perkId.equals("offline_xp")) {
            // Handle offline XP perk - this is handled by PartyManager.calculateSharedXp()
            if (purchased) {
                for (UUID memberUUID : members) {
                    Player member = Bukkit.getPlayer(memberUUID);
                    if (member != null) {
                        member.sendMessage(ChatColor.GREEN + "Your party now has " + 
                                          ChatColor.GOLD + "Offline XP" + 
                                          ChatColor.GREEN + "! Party members will receive XP share even when offline.");
                    }
                }
            }
        }
    }
    
    /**
     * Applies the glow effect to all online party members
     * 
     * @param leaderUUID The UUID of the party leader
     */
    private void applyGlowEffect(UUID leaderUUID) {
        // Check if the party leader has the party glow perk
        if (!hasPerk(leaderUUID, "party_glow")) {
            return;
        }
        
        // Get all party members
        Set<UUID> members = partyManager.getPartyMembers(leaderUUID);
        
        // Get all online members
        List<Player> onlineMembers = new ArrayList<>();
        for (UUID memberUUID : members) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                onlineMembers.add(member);
            }
        }
        
        // Apply glow effect
        // TODO: Implement glow effect (may require scoreboard teams)
    }
    
    /**
     * Converts a number to a Roman numeral
     * 
     * @param number The number to convert
     * @return The Roman numeral
     */
    private String toRoman(int number) {
        switch (number) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            case 7: return "VII";
            case 8: return "VIII";
            case 9: return "IX";
            case 10: return "X";
            default: return String.valueOf(number);
        }
    }
    
    /**
     * Checks if a party has a specific perk
     * 
     * @param leaderUUID The UUID of the party leader
     * @param perkId The ID of the perk
     * @return True if the party has the perk, false otherwise
     */
    public boolean hasPerk(UUID leaderUUID, String perkId) {
        return purchasedPerks.containsKey(perkId) && purchasedPerks.get(perkId).contains(leaderUUID);
    }
    
    /**
     * Gets the level of an XP boost perk
     * 
     * @param leaderUUID The UUID of the party leader
     * @return The level of the XP boost perk, or 0 if none
     */
    public int getXpBoostLevel(UUID leaderUUID) {
        if (hasPerk(leaderUUID, "xp_boost_3")) {
            return 3;
        } else if (hasPerk(leaderUUID, "xp_boost_2")) {
            return 2;
        } else if (hasPerk(leaderUUID, "xp_boost_1")) {
            return 1;
        } else {
            return 0;
        }
    }
    
    /**
     * Gets the XP boost percentage
     * 
     * @param leaderUUID The UUID of the party leader
     * @return The XP boost percentage (0-1)
     */
    public double getXpBoostPercent(UUID leaderUUID) {
        return getXpBoostLevel(leaderUUID) * 0.05;
    }
    
    /**
     * Gets the level of a party size perk
     * 
     * @param leaderUUID The UUID of the party leader
     * @return The level of the party size perk, or 0 if none
     */
    public int getPartySizeLevel(UUID leaderUUID) {
        if (hasPerk(leaderUUID, "party_size_2")) {
            return 2;
        } else if (hasPerk(leaderUUID, "party_size_1")) {
            return 1;
        } else {
            return 0;
        }
    }
    
    /**
     * Gets the extra party size
     * 
     * @param leaderUUID The UUID of the party leader
     * @return The extra party size
     */
    public int getExtraPartySize(UUID leaderUUID) {
        return getPartySizeLevel(leaderUUID);
    }
} 