package org.frizzlenpop.rPGSkillsPlugin.data;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Manages XP boosters for tools and weapons
 */
public class XPBoosterManager {
    private final RPGSkillsPlugin plugin;
    
    // NamespacedKeys for item metadata
    private final NamespacedKey BOOSTER_SKILL_KEY;
    private final NamespacedKey BOOSTER_MULTIPLIER_KEY;
    private final NamespacedKey BOOSTER_DURATION_KEY;
    private final NamespacedKey BOOSTER_EXPIRY_KEY;
    private final NamespacedKey BOOSTER_UUID_KEY;
    
    /**
     * Creates a new XPBoosterManager
     * 
     * @param plugin The plugin instance
     */
    public XPBoosterManager(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        
        // Initialize NamespacedKeys
        BOOSTER_SKILL_KEY = new NamespacedKey(plugin, "booster_skill");
        BOOSTER_MULTIPLIER_KEY = new NamespacedKey(plugin, "booster_multiplier");
        BOOSTER_DURATION_KEY = new NamespacedKey(plugin, "booster_duration");
        BOOSTER_EXPIRY_KEY = new NamespacedKey(plugin, "booster_expiry");
        BOOSTER_UUID_KEY = new NamespacedKey(plugin, "booster_uuid");
    }
    
    /**
     * Apply an XP booster to a tool
     * 
     * @param player The player whose item will be boosted
     * @param skill The skill to boost (mining, logging, etc.)
     * @param multiplier The XP multiplier (1.5 = 50% more XP)
     * @param duration The duration in seconds, or 0 for permanent
     * @return true if booster was applied, false otherwise
     */
    public boolean applyBooster(Player player, String skill, double multiplier, long duration) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must hold an item to apply a booster!");
            return false;
        }
        
        // Validate if the item is appropriate for the skill
        if (!isItemValidForSkill(item.getType(), skill)) {
            player.sendMessage(ChatColor.RED + "This item cannot be used with the " + skill + " skill!");
            return false;
        }
        
        // Get item meta
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage(ChatColor.RED + "Cannot apply booster to this item!");
            return false;
        }
        
        // Store booster data in item's persistent data container
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        // Set skill
        container.set(BOOSTER_SKILL_KEY, PersistentDataType.STRING, skill.toLowerCase());
        
        // Set multiplier
        container.set(BOOSTER_MULTIPLIER_KEY, PersistentDataType.DOUBLE, multiplier);
        
        // Set duration and expiry if not permanent
        if (duration > 0) {
            container.set(BOOSTER_DURATION_KEY, PersistentDataType.LONG, duration);
            long expiryTime = System.currentTimeMillis() + (duration * 1000);
            container.set(BOOSTER_EXPIRY_KEY, PersistentDataType.LONG, expiryTime);
        } else {
            // Remove duration and expiry if they exist
            container.remove(BOOSTER_DURATION_KEY);
            container.remove(BOOSTER_EXPIRY_KEY);
        }
        
        // Set unique identifier for the booster
        container.set(BOOSTER_UUID_KEY, PersistentDataType.STRING, UUID.randomUUID().toString());
        
        // Update lore to show booster info
        updateItemLore(meta, skill, multiplier, duration);
        
        // Apply the updated meta to the item
        item.setItemMeta(meta);
        
        // Notify player
        String durationText = (duration > 0) 
            ? formatDuration(duration) 
            : "permanent";
            
        player.sendMessage(ChatColor.GREEN + "Applied " + ChatColor.GOLD + "+" + 
                formatMultiplier(multiplier) + " " + capitalizeSkill(skill) + " XP" + 
                ChatColor.GREEN + " booster to your item! (" + durationText + ")");
        
        return true;
    }
    
    /**
     * Removes an XP booster from a tool
     * 
     * @param player The player whose item will have the booster removed
     * @return true if booster was removed, false otherwise
     */
    public boolean removeBooster(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must hold an item to remove a booster!");
            return false;
        }
        
        // Get item meta
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage(ChatColor.RED + "This item has no metadata!");
            return false;
        }
        
        // Check if item has a booster
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(BOOSTER_SKILL_KEY, PersistentDataType.STRING)) {
            player.sendMessage(ChatColor.RED + "This item doesn't have an XP booster!");
            return false;
        }
        
        // Get skill before removal for message
        String skill = container.get(BOOSTER_SKILL_KEY, PersistentDataType.STRING);
        
        // Remove booster data
        container.remove(BOOSTER_SKILL_KEY);
        container.remove(BOOSTER_MULTIPLIER_KEY);
        container.remove(BOOSTER_DURATION_KEY);
        container.remove(BOOSTER_EXPIRY_KEY);
        container.remove(BOOSTER_UUID_KEY);
        
        // Update lore to remove booster info
        List<String> lore = meta.getLore();
        if (lore != null) {
            List<String> newLore = new ArrayList<>();
            boolean inBoosterSection = false;
            
            for (String line : lore) {
                if (line.contains("⚡ XP Booster ⚡")) {
                    inBoosterSection = true;
                    continue;
                }
                
                if (inBoosterSection && line.isEmpty()) {
                    inBoosterSection = false;
                    continue;
                }
                
                if (!inBoosterSection) {
                    newLore.add(line);
                }
            }
            
            meta.setLore(newLore);
        }
        
        // Apply the updated meta to the item
        item.setItemMeta(meta);
        
        // Notify player
        player.sendMessage(ChatColor.GREEN + "Removed the " + 
                ChatColor.GOLD + capitalizeSkill(skill) + " XP" + 
                ChatColor.GREEN + " booster from your item!");
        
        return true;
    }
    
    /**
     * Checks if an item has an XP booster
     * 
     * @param item The item to check
     * @return true if the item has a booster, false otherwise
     */
    public boolean hasBooster(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(BOOSTER_SKILL_KEY, PersistentDataType.STRING);
    }
    
    /**
     * Gets the boosted XP for a tool
     * 
     * @param item The item to check
     * @param skill The skill being used
     * @param baseXp The base XP amount
     * @return The boosted XP amount, or the base XP if no booster found
     */
    public int getBoostedXp(ItemStack item, String skill, int baseXp) {
        if (!hasBooster(item)) {
            return baseXp;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return baseXp;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        // Check if this booster applies to the current skill
        String boosterSkill = container.get(BOOSTER_SKILL_KEY, PersistentDataType.STRING);
        if (boosterSkill == null || !boosterSkill.equalsIgnoreCase(skill)) {
            return baseXp;
        }
        
        // Check if booster has expired
        if (container.has(BOOSTER_EXPIRY_KEY, PersistentDataType.LONG)) {
            long expiryTime = container.get(BOOSTER_EXPIRY_KEY, PersistentDataType.LONG);
            if (System.currentTimeMillis() > expiryTime) {
                // Booster has expired but we'll remove it next time it's used
                removeExpiredBooster(item);
                return baseXp;
            }
        }
        
        // Get multiplier
        Double multiplier = container.get(BOOSTER_MULTIPLIER_KEY, PersistentDataType.DOUBLE);
        if (multiplier == null) {
            return baseXp;
        }
        
        // Calculate boosted XP
        return (int) Math.ceil(baseXp * multiplier);
    }
    
    /**
     * Gets the XP multiplier for a tool
     * 
     * @param item The item to check
     * @param skill The skill being used
     * @return The XP multiplier, or 1.0 if no booster found
     */
    public double getMultiplier(ItemStack item, String skill) {
        if (!hasBooster(item)) {
            return 1.0;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return 1.0;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        // Check if this booster applies to the current skill
        String boosterSkill = container.get(BOOSTER_SKILL_KEY, PersistentDataType.STRING);
        if (boosterSkill == null || !boosterSkill.equalsIgnoreCase(skill)) {
            return 1.0;
        }
        
        // Check if booster has expired
        if (container.has(BOOSTER_EXPIRY_KEY, PersistentDataType.LONG)) {
            long expiryTime = container.get(BOOSTER_EXPIRY_KEY, PersistentDataType.LONG);
            if (System.currentTimeMillis() > expiryTime) {
                // Booster has expired but we'll remove it next time it's used
                removeExpiredBooster(item);
                return 1.0;
            }
        }
        
        // Get multiplier
        Double multiplier = container.get(BOOSTER_MULTIPLIER_KEY, PersistentDataType.DOUBLE);
        return multiplier != null ? multiplier : 1.0;
    }
    
    /**
     * Get remaining time for a booster in seconds
     * 
     * @param item The item to check
     * @return Remaining time in seconds, or -1 if permanent or no booster
     */
    public long getRemainingTime(ItemStack item) {
        if (!hasBooster(item)) {
            return -1;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return -1;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        // Check if booster has an expiry
        if (!container.has(BOOSTER_EXPIRY_KEY, PersistentDataType.LONG)) {
            return -1; // Permanent booster
        }
        
        Long expiryTime = container.get(BOOSTER_EXPIRY_KEY, PersistentDataType.LONG);
        if (expiryTime == null) {
            return -1;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime > expiryTime) {
            return 0; // Expired
        }
        
        return (expiryTime - currentTime) / 1000; // Convert to seconds
    }
    
    /**
     * Updates an item's lore to display booster information
     * 
     * @param meta The ItemMeta to update
     * @param skill The skill being boosted
     * @param multiplier The XP multiplier
     * @param duration The duration in seconds
     */
    private void updateItemLore(ItemMeta meta, String skill, double multiplier, long duration) {
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        } else {
            // Remove any existing booster lore
            List<String> newLore = new ArrayList<>();
            boolean inBoosterSection = false;
            
            for (String line : lore) {
                if (line.contains("⚡ XP Booster ⚡")) {
                    inBoosterSection = true;
                    continue;
                }
                
                if (inBoosterSection && line.isEmpty()) {
                    inBoosterSection = false;
                    continue;
                }
                
                if (!inBoosterSection) {
                    newLore.add(line);
                }
            }
            
            lore = newLore;
        }
        
        // If lore doesn't end with an empty line, add one
        if (!lore.isEmpty() && !lore.get(lore.size() - 1).isEmpty()) {
            lore.add("");
        }
        
        // Add booster section
        lore.add(ChatColor.GOLD + "⚡ XP Booster ⚡");
        lore.add(ChatColor.YELLOW + "Skill: " + ChatColor.AQUA + capitalizeSkill(skill));
        lore.add(ChatColor.YELLOW + "Bonus: " + ChatColor.GREEN + "+" + formatMultiplier(multiplier));
        
        if (duration > 0) {
            lore.add(ChatColor.YELLOW + "Duration: " + ChatColor.WHITE + formatDuration(duration));
        } else {
            lore.add(ChatColor.YELLOW + "Duration: " + ChatColor.WHITE + "Permanent");
        }
        
        lore.add(""); // Add an empty line after the booster section
        
        meta.setLore(lore);
    }
    
    /**
     * Removes an expired booster from an item
     * 
     * @param item The item to update
     */
    private void removeExpiredBooster(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        // Remove booster data
        container.remove(BOOSTER_SKILL_KEY);
        container.remove(BOOSTER_MULTIPLIER_KEY);
        container.remove(BOOSTER_DURATION_KEY);
        container.remove(BOOSTER_EXPIRY_KEY);
        container.remove(BOOSTER_UUID_KEY);
        
        // Update lore to remove booster info
        List<String> lore = meta.getLore();
        if (lore != null) {
            List<String> newLore = new ArrayList<>();
            boolean inBoosterSection = false;
            
            for (String line : lore) {
                if (line.contains("⚡ XP Booster ⚡")) {
                    inBoosterSection = true;
                    continue;
                }
                
                if (inBoosterSection && line.isEmpty()) {
                    inBoosterSection = false;
                    continue;
                }
                
                if (!inBoosterSection) {
                    newLore.add(line);
                }
            }
            
            meta.setLore(newLore);
        }
        
        // Apply the updated meta to the item
        item.setItemMeta(meta);
    }
    
    /**
     * Formats a duration in seconds to a readable string
     * 
     * @param seconds The duration in seconds
     * @return A formatted string (e.g., "2h 30m")
     */
    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "m";
        }
        
        long hours = minutes / 60;
        minutes %= 60;
        if (hours < 24) {
            return hours + "h" + (minutes > 0 ? " " + minutes + "m" : "");
        }
        
        long days = hours / 24;
        hours %= 24;
        return days + "d" + (hours > 0 ? " " + hours + "h" : "");
    }
    
    /**
     * Formats a multiplier as a percentage
     * 
     * @param multiplier The XP multiplier
     * @return A formatted percentage string
     */
    private String formatMultiplier(double multiplier) {
        int percentage = (int) ((multiplier - 1.0) * 100);
        return percentage + "%";
    }
    
    /**
     * Capitalizes a skill name
     * 
     * @param skill The skill name
     * @return The capitalized skill name
     */
    private String capitalizeSkill(String skill) {
        return skill.substring(0, 1).toUpperCase() + skill.substring(1).toLowerCase();
    }
    
    /**
     * Checks if an item is valid for a particular skill
     * 
     * @param material The item material
     * @param skill The skill to check
     * @return true if the item can be used with the skill, false otherwise
     */
    private boolean isItemValidForSkill(Material material, String skill) {
        skill = skill.toLowerCase();
        
        switch (skill) {
            case "mining":
                return isMiningTool(material);
            case "logging":
            case "woodcutting":
                return isLoggingTool(material);
            case "farming":
                return isFarmingTool(material);
            case "fishing":
                return isFishingTool(material);
            case "fighting":
            case "combat":
                return isFightingWeapon(material);
            case "excavation":
                return isExcavationTool(material);
            default:
                return true; // Allow any item for other skills
        }
    }
    
    /**
     * Checks if a material is a mining tool
     * 
     * @param material The material to check
     * @return true if it's a mining tool, false otherwise
     */
    private boolean isMiningTool(Material material) {
        return material == Material.WOODEN_PICKAXE || 
               material == Material.STONE_PICKAXE || 
               material == Material.IRON_PICKAXE || 
               material == Material.GOLDEN_PICKAXE || 
               material == Material.DIAMOND_PICKAXE ||
               material == Material.NETHERITE_PICKAXE;
    }
    
    /**
     * Checks if a material is a logging tool
     * 
     * @param material The material to check
     * @return true if it's a logging tool, false otherwise
     */
    private boolean isLoggingTool(Material material) {
        return material == Material.WOODEN_AXE || 
               material == Material.STONE_AXE || 
               material == Material.IRON_AXE || 
               material == Material.GOLDEN_AXE || 
               material == Material.DIAMOND_AXE ||
               material == Material.NETHERITE_AXE;
    }
    
    /**
     * Checks if a material is a farming tool
     * 
     * @param material The material to check
     * @return true if it's a farming tool, false otherwise
     */
    private boolean isFarmingTool(Material material) {
        return material == Material.WOODEN_HOE || 
               material == Material.STONE_HOE || 
               material == Material.IRON_HOE || 
               material == Material.GOLDEN_HOE || 
               material == Material.DIAMOND_HOE ||
               material == Material.NETHERITE_HOE;
    }
    
    /**
     * Checks if a material is a fishing tool
     * 
     * @param material The material to check
     * @return true if it's a fishing tool, false otherwise
     */
    private boolean isFishingTool(Material material) {
        return material == Material.FISHING_ROD;
    }
    
    /**
     * Checks if a material is a fighting weapon
     * 
     * @param material The material to check
     * @return true if it's a fighting weapon, false otherwise
     */
    private boolean isFightingWeapon(Material material) {
        return material == Material.WOODEN_SWORD || 
               material == Material.STONE_SWORD || 
               material == Material.IRON_SWORD || 
               material == Material.GOLDEN_SWORD || 
               material == Material.DIAMOND_SWORD ||
               material == Material.NETHERITE_SWORD ||
               material == Material.BOW ||
               material == Material.CROSSBOW ||
               material == Material.TRIDENT;
    }
    
    /**
     * Checks if a material is an excavation tool
     * 
     * @param material The material to check
     * @return true if it's an excavation tool, false otherwise
     */
    private boolean isExcavationTool(Material material) {
        return material == Material.WOODEN_SHOVEL || 
               material == Material.STONE_SHOVEL || 
               material == Material.IRON_SHOVEL || 
               material == Material.GOLDEN_SHOVEL || 
               material == Material.DIAMOND_SHOVEL ||
               material == Material.NETHERITE_SHOVEL;
    }
} 