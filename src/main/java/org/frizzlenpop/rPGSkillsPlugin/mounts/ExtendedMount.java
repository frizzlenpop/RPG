package org.frizzlenpop.rPGSkillsPlugin.mounts;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Extended mount implementation that provides default methods
 * for specialized mount classes.
 */
public class ExtendedMount extends Mount {
    private final double speed;
    
    /**
     * Creates a new extended mount instance
     * 
     * @param plugin The plugin instance
     * @param id The mount id
     * @param ownerUUID The UUID of the owner
     * @param entityType The entity type for this mount
     * @param rarity The mount's rarity level
     */
    public ExtendedMount(RPGSkillsPlugin plugin, String id, UUID ownerUUID, EntityType entityType, MountRarity rarity) {
        super(plugin, id, ownerUUID, entityType, rarity);
        
        // Calculate speed based on rarity
        this.speed = 0.2 + (rarity.ordinal() * 0.05); // Base speed + rarity bonus
    }
    
    /**
     * Creates a new extended mount instance with entity
     * 
     * @param entity The mount's entity
     * @param type The mount type
     * @param ownerUUID The UUID of the owner
     * @param rarity The mount's rarity level
     */
    public ExtendedMount(Entity entity, MountType type, UUID ownerUUID, MountRarity rarity) {
        super(entity, type, ownerUUID, rarity);
        
        // Calculate speed based on rarity
        this.speed = 0.2 + (rarity.ordinal() * 0.05); // Base speed + rarity bonus
    }
    
    @Override
    public double getSpeed() {
        return speed;
    }
    
    @Override
    public ItemStack createMountItem() {
        // Create a basic mount item
        ItemStack item = new ItemStack(Material.SADDLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtils.color("&6Mount: &f" + getName()));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtils.color("&7Rarity: " + getRarity().getDisplayName()));
        lore.add(ColorUtils.color("&7"));
        lore.add(ColorUtils.color("&7Speed: " + String.format("%.1f", getSpeed())));
        lore.add(ColorUtils.color("&7"));
        lore.add(ColorUtils.color("&7ID: " + getId()));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    @Override
    public void onSpawn() {
        // Default implementation does nothing
    }
} 