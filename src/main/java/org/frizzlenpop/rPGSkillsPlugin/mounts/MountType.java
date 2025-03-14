package org.frizzlenpop.rPGSkillsPlugin.mounts;

import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a type of mount with its properties and abilities.
 */
public class MountType {
    private final String id;
    private final String displayName;
    private final String description;
    private final EntityType entityType;
    private final int baseCost;
    private final double speed;
    private final double jump;
    private final double health;
    private final Map<String, String> customizationOptions = new HashMap<>();
    private final Map<String, MountAbility> abilities = new HashMap<>();
    private final Map<String, String> particleEffects = new HashMap<>();
    private final Map<String, String> soundEffects = new HashMap<>();

    /**
     * Creates a new mount type
     * 
     * @param id The unique identifier for this mount type
     * @param displayName The name shown to players
     * @param description The description of this mount
     * @param entityType The entity type used for this mount
     * @param baseCost The base cost to purchase this mount
     * @param speed The base speed attribute
     * @param jump The base jump attribute
     * @param health The base health attribute
     */
    public MountType(String id, String displayName, String description, EntityType entityType, 
                    int baseCost, double speed, double jump, double health) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.entityType = entityType;
        this.baseCost = baseCost;
        this.speed = speed;
        this.jump = jump;
        this.health = health;
    }

    /**
     * Gets the unique ID for this mount type
     * 
     * @return The mount ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the display name for this mount
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the description for this mount
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the entity type used for this mount
     * 
     * @return The entity type
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * Gets the base cost to purchase this mount
     * 
     * @return The base cost
     */
    public int getBaseCost() {
        return baseCost;
    }

    /**
     * Gets the base speed attribute
     * 
     * @return The base speed
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Gets the base jump attribute
     * 
     * @return The base jump height
     */
    public double getJump() {
        return jump;
    }

    /**
     * Gets the base health attribute
     * 
     * @return The base health
     */
    public double getHealth() {
        return health;
    }

    /**
     * Sets a customization option for this mount type
     * 
     * @param key The customization key
     * @param value The customization value
     */
    public void setCustomization(String key, String value) {
        customizationOptions.put(key, value);
    }

    /**
     * Gets a customization option
     * 
     * @param key The customization key
     * @return The value or null if not set
     */
    public String getCustomization(String key) {
        return customizationOptions.get(key);
    }

    /**
     * Gets all customization options
     * 
     * @return Map of all customization options
     */
    public Map<String, String> getCustomizationOptions() {
        return new HashMap<>(customizationOptions);
    }

    /**
     * Adds an ability to this mount type
     * 
     * @param key The ability key
     * @param enabled Whether the ability is enabled
     * @param cooldown The cooldown in seconds
     * @param passive Whether it's a passive ability
     * @param minLevel The minimum level required to use it
     */
    public void addAbility(String key, boolean enabled, int cooldown, boolean passive, int minLevel) {
        abilities.put(key, new MountAbility(key, enabled, cooldown, passive, minLevel));
    }

    /**
     * Gets an ability by key
     * 
     * @param key The ability key
     * @return The ability or null if not found
     */
    public MountAbility getAbility(String key) {
        return abilities.get(key);
    }

    /**
     * Gets all abilities for this mount type
     * 
     * @return Map of all abilities
     */
    public Map<String, MountAbility> getAbilities() {
        return new HashMap<>(abilities);
    }

    /**
     * Adds a particle effect
     * 
     * @param key The effect key
     * @param effect The effect value
     */
    public void addParticleEffect(String key, String effect) {
        particleEffects.put(key, effect);
    }

    /**
     * Gets a particle effect
     * 
     * @param key The effect key
     * @return The effect value or null
     */
    public String getParticleEffect(String key) {
        return particleEffects.get(key);
    }

    /**
     * Gets all particle effects
     * 
     * @return Map of all particle effects
     */
    public Map<String, String> getParticleEffects() {
        return new HashMap<>(particleEffects);
    }

    /**
     * Adds a sound effect
     * 
     * @param key The effect key
     * @param sound The sound value
     */
    public void addSoundEffect(String key, String sound) {
        soundEffects.put(key, sound);
    }

    /**
     * Gets a sound effect
     * 
     * @param key The effect key
     * @return The sound value or null
     */
    public String getSoundEffect(String key) {
        return soundEffects.get(key);
    }

    /**
     * Gets all sound effects
     * 
     * @return Map of all sound effects
     */
    public Map<String, String> getSoundEffects() {
        return new HashMap<>(soundEffects);
    }

    /**
     * Inner class representing a mount ability
     */
    public static class MountAbility {
        private final String key;
        private final boolean enabled;
        private final int cooldown;
        private final boolean passive;
        private final int minLevel;

        /**
         * Creates a new mount ability
         * 
         * @param key The ability key
         * @param enabled Whether it's enabled
         * @param cooldown The cooldown in seconds
         * @param passive Whether it's passive
         * @param minLevel Minimum level required
         */
        public MountAbility(String key, boolean enabled, int cooldown, boolean passive, int minLevel) {
            this.key = key;
            this.enabled = enabled;
            this.cooldown = cooldown;
            this.passive = passive;
            this.minLevel = minLevel;
        }

        /**
         * Gets the ability key
         * 
         * @return The ability key
         */
        public String getKey() {
            return key;
        }

        /**
         * Checks if ability is enabled
         * 
         * @return true if enabled
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Gets the cooldown in seconds
         * 
         * @return The cooldown
         */
        public int getCooldown() {
            return cooldown;
        }

        /**
         * Checks if this is a passive ability
         * 
         * @return true if passive
         */
        public boolean isPassive() {
            return passive;
        }

        /**
         * Gets the minimum level required
         * 
         * @return The minimum level
         */
        public int getMinLevel() {
            return minLevel;
        }
    }
} 