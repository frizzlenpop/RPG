package org.frizzlenpop.rPGSkillsPlugin.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for GUI related functions
 */
public class GUIUtils {
    
    /**
     * Creates a GUI item with a name and lore
     * 
     * @param material The material
     * @param name The name
     * @param lore The lore
     * @return The created ItemStack
     */
    public static ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            
            if (lore != null && lore.length > 0) {
                List<String> loreList = new ArrayList<>(Arrays.asList(lore));
                meta.setLore(loreList);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Creates a GUI item with a custom model data value
     * 
     * @param material The material
     * @param name The name
     * @param customModelData The custom model data value
     * @param lore The lore
     * @return The created ItemStack
     */
    public static ItemStack createGuiItem(Material material, String name, int customModelData, String... lore) {
        ItemStack item = createGuiItem(material, name, lore);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }
        
        return item;
    }
} 