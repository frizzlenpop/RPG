package org.frizzlenpop.rPGSkillsPlugin.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

import java.util.*;

public class CustomEnchantScroll {
    
    private final RPGSkillsPlugin plugin;
    public static final NamespacedKey SCROLL_ENCHANT_KEY = new NamespacedKey("rpgskills", "scroll_enchant");
    public static final NamespacedKey SCROLL_IDENTIFIED_KEY = new NamespacedKey("rpgskills", "identified");
    
    public CustomEnchantScroll(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Creates and gives a new unknown enchantment scroll to a player
     * @param player The player to give the scroll to
     * @param enchantment The enchantment to store in the scroll
     */
    public void giveUnknownScroll(Player player, Enchantment enchantment) {
        ItemStack scroll = new ItemStack(Material.PAPER);
        ItemMeta meta = scroll.getItemMeta();
        if (meta == null) return;

        meta.setDisplayName("§5Unknown Enchantment Scroll");
        List<String> lore = new ArrayList<>();
        lore.add("§7Right-click to identify");
        meta.setLore(lore);

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(SCROLL_ENCHANT_KEY, PersistentDataType.STRING, enchantment.getKey().getKey());

        scroll.setItemMeta(meta);
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(scroll);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), scroll);
            player.sendMessage("§eInventory full! The scroll was dropped on the ground.");
        }
    }
    
    /**
     * Identifies an unknown scroll
     * @param scroll The scroll ItemStack to identify
     * @param player The player identifying the scroll
     * @return true if successfully identified, false otherwise
     */
    public boolean identifyScroll(ItemStack scroll, Player player) {
        if (!isUnknownScroll(scroll)) return false;
        
        ItemMeta meta = scroll.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String enchantName = container.get(SCROLL_ENCHANT_KEY, PersistentDataType.STRING);

        if (enchantName != null) {
            container.set(SCROLL_IDENTIFIED_KEY, PersistentDataType.BYTE, (byte)1);
            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));
            if (enchant != null) {
                meta.setDisplayName("§6" + formatEnchantmentName(enchant) + " Scroll");
                List<String> lore = new ArrayList<>();
                lore.add("§7Right-click on an item to apply");
                meta.setLore(lore);
                scroll.setItemMeta(meta);
                player.sendMessage("§aYou identified the scroll! It contains: " + formatEnchantmentName(enchant));
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if an ItemStack is an unknown enchantment scroll
     * @param item The ItemStack to check
     * @return true if it's an unknown scroll, false otherwise
     */
    public static boolean isUnknownScroll(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(SCROLL_ENCHANT_KEY, PersistentDataType.STRING) &&
                !meta.getPersistentDataContainer().has(SCROLL_IDENTIFIED_KEY, PersistentDataType.BYTE);
    }
    
    /**
     * Checks if an ItemStack is an identified enchantment scroll
     * @param item The ItemStack to check
     * @return true if it's an identified scroll, false otherwise
     */
    public static boolean isIdentifiedScroll(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(SCROLL_ENCHANT_KEY, PersistentDataType.STRING) &&
                meta.getPersistentDataContainer().has(SCROLL_IDENTIFIED_KEY, PersistentDataType.BYTE);
    }
    
    /**
     * Formats an enchantment name to be more readable
     * @param enchant The enchantment to format
     * @return A formatted string of the enchantment name
     */
    public static String formatEnchantmentName(Enchantment enchant) {
        String name = enchant.getKey().getKey();
        String[] words = name.split("_");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)));
                formatted.append(word.substring(1).toLowerCase());
                formatted.append(" ");
            }
        }
        
        return formatted.toString().trim();
    }
    
    /**
     * Applies an enchantment scroll to an item
     * @param scroll The scroll to apply
     * @param target The target item
     * @param player The player applying the scroll
     * @return true if successfully applied, false otherwise
     */
    public boolean applyScrollToItem(ItemStack scroll, ItemStack target, Player player) {
        if (!isIdentifiedScroll(scroll)) return false;
        
        ItemMeta meta = scroll.getItemMeta();
        if (meta == null) return false;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String enchantName = container.get(SCROLL_ENCHANT_KEY, PersistentDataType.STRING);
        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));
        
        if (enchantment != null && enchantment.canEnchantItem(target)) {
            // Get current enchantment level or 0 if not present
            int currentLevel = target.getEnchantmentLevel(enchantment);
            int newLevel = Math.min(currentLevel + 1, enchantment.getMaxLevel());
            
            // Apply the enchantment at the new level
            target.addUnsafeEnchantment(enchantment, newLevel);
            player.sendMessage("§aSuccessfully applied " + formatEnchantmentName(enchantment) + 
                              (newLevel > 1 ? " " + romanNumerals(newLevel) : "") + 
                              " to your item!");
            return true;
        } else {
            player.sendMessage("§cThis enchantment cannot be applied to this item!");
            return false;
        }
    }
    
    /**
     * Converts an integer to Roman numerals
     * @param number The number to convert
     * @return The Roman numeral representation
     */
    private String romanNumerals(int number) {
        switch (number) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return String.valueOf(number);
        }
    }
} 