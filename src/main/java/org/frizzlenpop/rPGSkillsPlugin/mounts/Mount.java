package org.frizzlenpop.rPGSkillsPlugin.mounts;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Represents an active mount instance for a player.
 */
public abstract class Mount {
    private final RPGSkillsPlugin plugin;
    private final String id;
    private final UUID ownerUUID;
    private final MountRarity rarity;
    private WeakReference<Entity> entityReference;
    private final long summonTime;
    private MountType type;
    
    /**
     * Creates a new basic mount instance
     * 
     * @param entity The mount's entity
     * @param type The mount type
     * @param ownerUUID The UUID of the owner
     * @param rarity The mount's rarity level
     */
    public Mount(Entity entity, MountType type, UUID ownerUUID, MountRarity rarity) {
        this.plugin = null;
        this.entityReference = new WeakReference<>(entity);
        this.type = type;
        this.id = type.getId();
        this.ownerUUID = ownerUUID;
        this.rarity = rarity;
        this.summonTime = System.currentTimeMillis();
    }
    
    /**
     * Creates a new specialized mount instance
     * 
     * @param plugin The plugin instance
     * @param id The mount id
     * @param ownerUUID The UUID of the owner
     * @param entityType The entity type for this mount
     * @param rarity The mount's rarity level
     */
    public Mount(RPGSkillsPlugin plugin, String id, UUID ownerUUID, org.bukkit.entity.EntityType entityType, MountRarity rarity) {
        this.plugin = plugin;
        this.id = id;
        this.ownerUUID = ownerUUID;
        this.rarity = rarity;
        this.summonTime = System.currentTimeMillis();
        // Note: Entity will be set later by MountManager
    }
    
    /**
     * Sets the entity reference for this mount
     * 
     * @param entity The entity to set
     */
    public void setEntity(Entity entity) {
        this.entityReference = new WeakReference<>(entity);
    }
    
    /**
     * Sets the mount type for this mount
     * 
     * @param type The mount type
     */
    public void setType(MountType type) {
        this.type = type;
    }
    
    /**
     * Gets the mount's entity
     * 
     * @return The entity
     */
    public Entity getEntity() {
        return entityReference != null ? entityReference.get() : null;
    }
    
    /**
     * Gets a weak reference to the mount's entity
     * 
     * @return The entity reference
     */
    public WeakReference<Entity> getEntityReference() {
        return entityReference;
    }
    
    /**
     * Gets the mount's type
     * 
     * @return The mount type
     */
    public MountType getType() {
        return type;
    }
    
    /**
     * Gets the UUID of the mount's owner
     * 
     * @return The owner's UUID
     */
    public UUID getOwnerUUID() {
        return ownerUUID;
    }
    
    /**
     * Gets the owner of this mount
     * 
     * @return The owner UUID
     */
    public UUID getOwner() {
        return ownerUUID;
    }
    
    /**
     * Gets the mount's rarity
     * 
     * @return The rarity level
     */
    public MountRarity getRarity() {
        return rarity;
    }
    
    /**
     * Gets the time when this mount was summoned
     * 
     * @return The summoned time in milliseconds
     */
    public long getSummonTime() {
        return summonTime;
    }
    
    /**
     * Gets the mount's ID
     * 
     * @return The mount ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the mount's display name
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return type != null ? type.getDisplayName() : id;
    }
    
    /**
     * Gets the mount's name
     * 
     * @return The name of the mount
     */
    public String getName() {
        return getDisplayName();
    }
    
    /**
     * Checks if this mount is alive and valid
     * 
     * @return true if valid
     */
    public boolean isValid() {
        Entity entity = getEntity();
        return entity != null && !entity.isDead();
    }
    
    /**
     * Checks if this mount is currently spawned
     * 
     * @return true if spawned and valid
     */
    public boolean isSpawned() {
        return isValid();
    }
    
    /**
     * Gets the current rider of this mount, if any
     * 
     * @return The rider, or null if none
     */
    public Player getCurrentRider() {
        Entity entity = getEntity();
        if (entity != null && !entity.getPassengers().isEmpty()) {
            Entity passenger = entity.getPassengers().get(0);
            if (passenger instanceof Player) {
                return (Player) passenger;
            }
        }
        return null;
    }
    
    /**
     * Gets stat multiplier based on rarity
     * 
     * @return The multiplier for stats
     */
    public double getRarityMultiplier() {
        return rarity.getStatMultiplier();
    }
    
    /**
     * Gets the plugin instance
     * 
     * @return The plugin instance
     */
    public RPGSkillsPlugin getPlugin() {
        return plugin;
    }
    
    /**
     * Creates an item stack representing this mount
     * 
     * @return The mount item
     */
    public abstract ItemStack createMountItem();
    
    /**
     * Called when this mount is spawned
     */
    public abstract void onSpawn();
    
    /**
     * Called when a player dismounts from this mount
     * 
     * @param player The player who dismounted
     */
    public void onDismount(Player player) {
        // Default implementation does nothing
    }
    
    /**
     * Called when a player toggles the mount's ability
     * 
     * @param player The player using the ability
     * @return true if the ability was used successfully
     */
    public boolean onToggleAbility(Player player) {
        // Default implementation does nothing
        return false;
    }
    
    /**
     * Gets the base speed of this mount
     * 
     * @return The mount's speed
     */
    public abstract double getSpeed();
} 