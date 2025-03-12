package org.frizzlenpop.rPGSkillsPlugin.skilltree;

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

import java.util.*;

/**
 * GUI for the skill tree system
 */
public class SkillTreeGUI implements Listener {
    private final RPGSkillsPlugin plugin;
    private final SkillTreeManager skillTreeManager;
    
    private static final String GUI_TITLE = "Skill Tree";
    private static final int GUI_SIZE = 54; // 6 rows of 9 slots
    private static final Map<UUID, Integer> playerPages = new HashMap<>();
    private static final Map<Integer, Map<Integer, String>> pageLayouts = new HashMap<>();
    
    // Special slots
    private static final int PREV_PAGE_SLOT = 45;
    private static final int NEXT_PAGE_SLOT = 53;
    private static final int INFO_SLOT = 49;
    
    /**
     * Constructor for the skill tree GUI
     */
    public SkillTreeGUI(RPGSkillsPlugin plugin, SkillTreeManager skillTreeManager) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;
        
        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // Initialize page layouts
        initializePageLayouts();
    }
    
    /**
     * Initialize the layout of nodes on each page
     */
    private void initializePageLayouts() {
        // Page 1: Warrior tree
        Map<Integer, String> warriorPage = new HashMap<>();
        warriorPage.put(13, "warrior_strength");
        warriorPage.put(22, "warrior_vitality");
        warriorPage.put(31, "warrior_toughness");
        warriorPage.put(21, "warrior_agility");
        warriorPage.put(23, "warrior_power");
        
        // Page 2: Mining tree
        Map<Integer, String> miningPage = new HashMap<>();
        miningPage.put(13, "mining_efficiency");
        miningPage.put(22, "mining_fortune");
        miningPage.put(31, "mining_mastery");
        miningPage.put(21, "mining_xp_boost");
        miningPage.put(23, "mining_treasure_hunter");
        
        // Page 3: Logging tree
        Map<Integer, String> loggingPage = new HashMap<>();
        loggingPage.put(13, "logging_efficiency");
        loggingPage.put(22, "logging_harvester");
        loggingPage.put(31, "logging_mastery");
        loggingPage.put(21, "logging_naturalist");
        loggingPage.put(23, "logging_xp_boost");
        
        // Page 4: Farming tree
        Map<Integer, String> farmingPage = new HashMap<>();
        farmingPage.put(13, "farming_green_thumb");
        farmingPage.put(22, "farming_harvester");
        farmingPage.put(31, "farming_mastery");
        farmingPage.put(21, "farming_animal_whisperer");
        farmingPage.put(23, "farming_xp_boost");
        
        // Page 5: Fighting tree
        Map<Integer, String> fightingPage = new HashMap<>();
        fightingPage.put(13, "fighting_strength");
        fightingPage.put(22, "fighting_precision");
        fightingPage.put(31, "fighting_mastery");
        fightingPage.put(21, "fighting_agility");
        fightingPage.put(23, "fighting_xp_boost");
        
        // Page 6: Fishing tree
        Map<Integer, String> fishingPage = new HashMap<>();
        fishingPage.put(13, "fishing_luck");
        fishingPage.put(22, "fishing_patience");
        fishingPage.put(31, "fishing_mastery");
        fishingPage.put(21, "fishing_treasure_hunter");
        fishingPage.put(23, "fishing_xp_boost");
        
        // Add pages to the layout map
        pageLayouts.put(0, warriorPage);
        pageLayouts.put(1, miningPage);
        pageLayouts.put(2, loggingPage);
        pageLayouts.put(3, farmingPage);
        pageLayouts.put(4, fightingPage);
        pageLayouts.put(5, fishingPage);
    }
    
    /**
     * Open the skill tree GUI for a player
     */
    public void openSkillTree(Player player) {
        openSkillTree(player, 0); // Start with first page
    }
    
    /**
     * Open a specific page of the skill tree GUI for a player
     */
    public void openSkillTree(Player player, int page) {
        // Ensure valid page
        int maxPages = pageLayouts.size();
        if (page < 0) page = 0;
        if (page >= maxPages) page = maxPages - 1;
        
        // Create inventory
        Inventory inv = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE + " - Page " + (page + 1));
        
        // Store player's current page
        playerPages.put(player.getUniqueId(), page);
        
        // Add nodes for this page
        Map<Integer, String> pageLayout = pageLayouts.get(page);
        if (pageLayout != null) {
            for (Map.Entry<Integer, String> entry : pageLayout.entrySet()) {
                int slot = entry.getKey();
                String nodeId = entry.getValue();
                
                SkillTreeNode node = skillTreeManager.getAllNodes().get(nodeId);
                if (node != null) {
                    boolean unlocked = skillTreeManager.hasUnlockedNode(player, nodeId);
                    boolean available = skillTreeManager.canUnlockNode(player, nodeId);
                    
                    ItemStack icon = node.createIcon(unlocked, available);
                    inv.setItem(slot, icon);
                }
            }
        }
        
        // Add navigation items
        addNavigationItems(inv, player, page, maxPages);
        
        // Show the inventory to the player
        player.openInventory(inv);
    }
    
    /**
     * Add navigation items to the inventory
     */
    private void addNavigationItems(Inventory inv, Player player, int currentPage, int maxPages) {
        // Previous page button (if not on first page)
        if (currentPage > 0) {
            ItemStack prevPage = createGuiItem(Material.ARROW, "§aPrevious Page", 
                "§7Click to go to the previous page");
            inv.setItem(PREV_PAGE_SLOT, prevPage);
        } else {
            inv.setItem(PREV_PAGE_SLOT, new ItemStack(Material.AIR));
        }
        
        // Next page button (if not on last page)
        if (currentPage < maxPages - 1) {
            ItemStack nextPage = createGuiItem(Material.ARROW, "§aNext Page", 
                "§7Click to go to the next page");
            inv.setItem(NEXT_PAGE_SLOT, nextPage);
        } else {
            inv.setItem(NEXT_PAGE_SLOT, new ItemStack(Material.AIR));
        }
        
        // Player info
        int playerLevel = skillTreeManager.getPlayerLevel(player);
        int availablePoints = skillTreeManager.getAvailableSkillPoints(player);
        double progress = skillTreeManager.getLevelProgress(player);
        String progressBar = createProgressBar(progress, 20);
        
        ItemStack infoItem = createGuiItem(Material.EXPERIENCE_BOTTLE, 
            "§6Player Level: §e" + playerLevel, 
            "§7Available Points: §e" + availablePoints,
            "§7Progress to next level:",
            "§a" + progressBar + " §f" + String.format("%.1f%%", progress * 100));
        
        inv.setItem(INFO_SLOT, infoItem);
        
        // Page number indicators
        for (int i = 0; i < maxPages; i++) {
            Material material = (i == currentPage) ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
            String name = getPageName(i);
            
            ItemStack pageIndicator = createGuiItem(material, "§e" + name, 
                "§7Click to view this tree");
            
            inv.setItem(45 + i, pageIndicator);
        }
    }
    
    /**
     * Get the name of a specific page
     */
    private String getPageName(int page) {
        switch (page) {
            case 0: return "Warrior Tree";
            case 1: return "Mining Tree";
            case 2: return "Logging Tree";
            case 3: return "Farming Tree";
            case 4: return "Fighting Tree";
            case 5: return "Fishing Tree";
            default: return "Unknown Tree";
        }
    }
    
    /**
     * Get the layout for a specific page - used for debugging
     */
    public Map<Integer, String> getPageLayout(int page) {
        return pageLayouts.get(page);
    }
    
    /**
     * Create a progress bar string
     */
    private String createProgressBar(double progress, int length) {
        int filledBars = (int) Math.round(progress * length);
        StringBuilder bar = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            if (i < filledBars) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        
        return bar.toString();
    }
    
    /**
     * Create a GUI item with a custom name and lore
     */
    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                Collections.addAll(loreList, lore);
                meta.setLore(loreList);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Handle inventory click events
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        
        // Check if this is our GUI
        if (!event.getView().getTitle().startsWith(GUI_TITLE)) {
            return;
        }
        
        // Cancel the event to prevent item movement
        event.setCancelled(true);
        
        // Get the current page
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
        
        // Handle navigation clicks
        if (event.getSlot() == PREV_PAGE_SLOT && currentPage > 0) {
            openSkillTree(player, currentPage - 1);
            return;
        } else if (event.getSlot() == NEXT_PAGE_SLOT && currentPage < pageLayouts.size() - 1) {
            openSkillTree(player, currentPage + 1);
            return;
        } else if (event.getSlot() >= 45 && event.getSlot() <= 53) {
            int pageClicked = event.getSlot() - 45;
            if (pageClicked >= 0 && pageClicked < pageLayouts.size()) {
                openSkillTree(player, pageClicked);
                return;
            }
        }
        
        // Handle node clicks
        Map<Integer, String> pageLayout = pageLayouts.get(currentPage);
        if (pageLayout != null && pageLayout.containsKey(event.getSlot())) {
            String nodeId = pageLayout.get(event.getSlot());
            
            if (skillTreeManager.canUnlockNode(player, nodeId)) {
                // Unlock the node
                if (skillTreeManager.unlockNode(player, nodeId)) {
                    // Refresh the inventory
                    openSkillTree(player, currentPage);
                    
                    // Play sound/effect for unlocking
                    player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
                }
            } else if (skillTreeManager.hasUnlockedNode(player, nodeId)) {
                player.sendMessage(ChatColor.YELLOW + "You have already unlocked this node.");
            } else {
                player.sendMessage(ChatColor.RED + "You cannot unlock this node yet. Check the requirements.");
            }
        }
    }
} 