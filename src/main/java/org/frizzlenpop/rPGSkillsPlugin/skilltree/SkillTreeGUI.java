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
        // Instead of hard-coding nodes, let's organize them by category
        Map<String, List<String>> categorizedNodes = organizeNodesByCategory();
        
        // Create pages for each category
        int pageIndex = 0;
        
        // Warrior tree (Page 1)
        if (categorizedNodes.containsKey("warrior")) {
            Map<Integer, String> warriorPage = createLayoutForCategory("warrior", categorizedNodes.get("warrior"));
            pageLayouts.put(pageIndex++, warriorPage);
        }
        
        // Mining tree (Page 2)
        if (categorizedNodes.containsKey("mining")) {
            Map<Integer, String> miningPage = createLayoutForCategory("mining", categorizedNodes.get("mining"));
            pageLayouts.put(pageIndex++, miningPage);
        }
        
        // Logging tree (Page 3)
        if (categorizedNodes.containsKey("logging")) {
            Map<Integer, String> loggingPage = createLayoutForCategory("logging", categorizedNodes.get("logging"));
            pageLayouts.put(pageIndex++, loggingPage);
        }
        
        // Farming tree (Page 4)
        if (categorizedNodes.containsKey("farming")) {
            Map<Integer, String> farmingPage = createLayoutForCategory("farming", categorizedNodes.get("farming"));
            pageLayouts.put(pageIndex++, farmingPage);
        }
        
        // Fighting tree (Page 5)
        if (categorizedNodes.containsKey("fighting")) {
            Map<Integer, String> fightingPage = createLayoutForCategory("fighting", categorizedNodes.get("fighting"));
            pageLayouts.put(pageIndex++, fightingPage);
        }
        
        // Fishing tree (Page 6)
        if (categorizedNodes.containsKey("fishing")) {
            Map<Integer, String> fishingPage = createLayoutForCategory("fishing", categorizedNodes.get("fishing"));
            pageLayouts.put(pageIndex++, fishingPage);
        }
        
        // Excavation tree
        if (categorizedNodes.containsKey("excavation")) {
            Map<Integer, String> excavationPage = createLayoutForCategory("excavation", categorizedNodes.get("excavation"));
            pageLayouts.put(pageIndex++, excavationPage);
        }
        
        // Enchanting tree
        if (categorizedNodes.containsKey("enchanting")) {
            Map<Integer, String> enchantingPage = createLayoutForCategory("enchanting", categorizedNodes.get("enchanting"));
            pageLayouts.put(pageIndex, enchantingPage);
        }
        
        // If no pages were created, create a default page
        if (pageLayouts.isEmpty()) {
            Map<Integer, String> defaultPage = new HashMap<>();
            pageLayouts.put(0, defaultPage);
        }
    }
    
    /**
     * Organize skill tree nodes by category
     */
    private Map<String, List<String>> organizeNodesByCategory() {
        Map<String, List<String>> categorizedNodes = new HashMap<>();
        
        // Get all nodes from the skill tree manager
        Map<String, SkillTreeNode> allNodes = skillTreeManager.getAllNodes();
        
        // Organize nodes by category
        for (String nodeId : allNodes.keySet()) {
            String category = getCategoryFromNodeId(nodeId);
            
            if (!categorizedNodes.containsKey(category)) {
                categorizedNodes.put(category, new ArrayList<>());
            }
            
            categorizedNodes.get(category).add(nodeId);
        }
        
        return categorizedNodes;
    }
    
    /**
     * Extract category from node ID (e.g., "warrior_strength" -> "warrior")
     */
    private String getCategoryFromNodeId(String nodeId) {
        if (nodeId.contains("_")) {
            return nodeId.substring(0, nodeId.indexOf("_"));
        }
        return "unknown";
    }
    
    /**
     * Create a layout for a specific category
     */
    private Map<Integer, String> createLayoutForCategory(String category, List<String> nodes) {
        Map<Integer, String> layout = new HashMap<>();
        
        // Get the nodes sorted by tier (based on prerequisites)
        Map<Integer, List<String>> tierNodes = organizeTiersByPrerequisites(nodes);
        
        // Calculate slots for each tier - expanded to handle up to 30 skills
        int[] tierSlots = {
            // Center slots for Tier 1 (base skills)
            13, 22, 31, 4, 40,
            // Slots around the center for Tier 2
            12, 14, 21, 23, 30, 32, 3, 5, 39, 41,
            // Slots for Tier 3
            2, 6, 11, 15, 20, 24, 29, 33, 38, 42,
            // Additional slots for Tier 4+
            1, 7, 10, 16, 19, 25, 28, 34, 37, 43
        };
        
        int slotIndex = 0;
        
        // If we have more than 30 nodes, create multiple pages
        if (nodes.size() > tierSlots.length) {
            // First page - first 30 nodes
            for (int tier = 1; tier <= tierNodes.size() && slotIndex < tierSlots.length; tier++) {
                List<String> tierNodeList = tierNodes.getOrDefault(tier, new ArrayList<>());
                
                for (String nodeId : tierNodeList) {
                    if (slotIndex < tierSlots.length) {
                        layout.put(tierSlots[slotIndex++], nodeId);
                    }
                }
            }
            
            // If we need more pages, they'll be created in a separate method
            // This would require refactoring the page system, which is beyond the scope of this edit
        } else {
            // Standard layout for up to 30 nodes
            for (int tier = 1; tier <= tierNodes.size(); tier++) {
                List<String> tierNodeList = tierNodes.getOrDefault(tier, new ArrayList<>());
                
                for (String nodeId : tierNodeList) {
                    if (slotIndex < tierSlots.length) {
                        layout.put(tierSlots[slotIndex++], nodeId);
                    }
                }
            }
        }
        
        return layout;
    }
    
    /**
     * Organize nodes into tiers based on prerequisites
     */
    private Map<Integer, List<String>> organizeTiersByPrerequisites(List<String> nodes) {
        Map<Integer, List<String>> tierNodes = new HashMap<>();
        Map<String, SkillTreeNode> allNodes = skillTreeManager.getAllNodes();
        
        // First, identify tier 1 nodes (no prerequisites)
        List<String> processed = new ArrayList<>();
        
        for (String nodeId : nodes) {
            SkillTreeNode node = allNodes.get(nodeId);
            if (node != null && node.getPrerequisites().isEmpty()) {
                // This is a tier 1 node
                if (!tierNodes.containsKey(1)) {
                    tierNodes.put(1, new ArrayList<>());
                }
                tierNodes.get(1).add(nodeId);
                processed.add(nodeId);
            }
        }
        
        // Then process the rest of the nodes
        int currentTier = 2;
        boolean anyProcessed;
        
        do {
            anyProcessed = false;
            List<String> newlyProcessed = new ArrayList<>();
            
            for (String nodeId : nodes) {
                if (processed.contains(nodeId)) continue;
                
                SkillTreeNode node = allNodes.get(nodeId);
                if (node == null) continue;
                
                // Check if all prerequisites are in previous tiers
                boolean allPrereqsProcessed = true;
                for (String prereq : node.getPrerequisites()) {
                    if (!processed.contains(prereq)) {
                        allPrereqsProcessed = false;
                        break;
                    }
                }
                
                if (allPrereqsProcessed) {
                    // All prerequisites are in previous tiers, so this node is in current tier
                    if (!tierNodes.containsKey(currentTier)) {
                        tierNodes.put(currentTier, new ArrayList<>());
                    }
                    tierNodes.get(currentTier).add(nodeId);
                    newlyProcessed.add(nodeId);
                    anyProcessed = true;
                }
            }
            
            processed.addAll(newlyProcessed);
            
            if (anyProcessed) {
                currentTier++;
            }
        } while (anyProcessed);
        
        return tierNodes;
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
        
        // Add debug logging
        int availablePoints = skillTreeManager.getAvailableSkillPoints(player);
        int totalPoints = skillTreeManager.getTotalSkillPoints(player);
        int spentPoints = skillTreeManager.getSpentSkillPoints(player);
        player.sendMessage("§7[Debug] Available Points: §e" + availablePoints + 
                           "§7, Total Points: §e" + totalPoints + 
                           "§7, Spent Points: §e" + spentPoints);
        
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
                    
                    // Add more detailed debug for each node
                    if (!unlocked && !available) {
                        // Check why it's not available
                        boolean hasPoints = availablePoints >= node.getPointCost();
                        boolean hasPrereqs = true;
                        for (String prereq : node.getPrerequisites()) {
                            if (!skillTreeManager.hasUnlockedNode(player, prereq)) {
                                hasPrereqs = false;
                                break;
                            }
                        }
                        
                        if (!hasPoints) {
                            player.sendMessage("§7[Debug] Node §e" + nodeId + "§7 requires §e" + node.getPointCost() + 
                                               "§7 points, but you only have §e" + availablePoints);
                        }
                        if (!hasPrereqs) {
                            player.sendMessage("§7[Debug] Node §e" + nodeId + "§7 has prerequisites you haven't unlocked");
                        }
                    }
                    
                    ItemStack icon = createItem(player, node, unlocked);
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
        if (page < 0 || page >= pageLayouts.size()) {
            return "Unknown Tree";
        }
        
        // Get a sample node ID from the page to determine its category
        Map<Integer, String> pageLayout = pageLayouts.get(page);
        if (pageLayout == null || pageLayout.isEmpty()) {
            return "Empty Tree";
        }
        
        // Get the first node ID to determine category
        String sampleNodeId = pageLayout.values().iterator().next();
        String category = getCategoryFromNodeId(sampleNodeId);
        
        // Format the category name
        String formattedCategory = category.substring(0, 1).toUpperCase() + category.substring(1);
        return formattedCategory + " Tree";
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
    
    private ItemStack createItem(Player player, SkillTreeNode node, boolean unlocked) {
        boolean canUnlock = skillTreeManager.canUnlockNode(player, node.getId());
        boolean isFreeBaseNode = false;
        
        // Check if this is a free base node (first node in category with no prerequisites)
        if (!unlocked && node.getPrerequisites().isEmpty()) {
            // Extract category from node ID (e.g., "warrior" from "warrior_strength")
            String category = node.getId().split("_")[0];
            
            // Check if player has any nodes in this category
            boolean hasNodesInCategory = false;
            for (String unlockedNodeId : skillTreeManager.getUnlockedNodes(player.getUniqueId())) {
                if (unlockedNodeId.startsWith(category + "_")) {
                    hasNodesInCategory = true;
                    break;
                }
            }
            
            isFreeBaseNode = !hasNodesInCategory;
        }
        
        ItemStack item = node.createIcon(unlocked, canUnlock, isFreeBaseNode);
        return item;
    }
} 