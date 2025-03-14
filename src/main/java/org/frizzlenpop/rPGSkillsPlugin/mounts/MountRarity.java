package org.frizzlenpop.rPGSkillsPlugin.mounts;

import org.bukkit.ChatColor;

/**
 * Enum representing the different rarity levels of mounts.
 */
public enum MountRarity {
    
    COMMON(ChatColor.WHITE, "Common", 1.0),
    UNCOMMON(ChatColor.GREEN, "Uncommon", 1.05),
    RARE(ChatColor.BLUE, "Rare", 1.1),
    EPIC(ChatColor.DARK_PURPLE, "Epic", 1.2),
    LEGENDARY(ChatColor.GOLD, "Legendary", 1.35),
    MYTHIC(ChatColor.RED, "Mythic", 1.5);
    
    private final ChatColor color;
    private final String displayName;
    private final double statMultiplier;
    
    /**
     * Creates a new mount rarity
     * 
     * @param color The color for this rarity
     * @param displayName The display name
     * @param statMultiplier The stat multiplier
     */
    MountRarity(ChatColor color, String displayName, double statMultiplier) {
        this.color = color;
        this.displayName = displayName;
        this.statMultiplier = statMultiplier;
    }
    
    /**
     * Gets the color associated with this rarity
     * 
     * @return The chat color
     */
    public ChatColor getColor() {
        return color;
    }
    
    /**
     * Gets the display name
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the formatted display name with color
     * 
     * @return Colored display name
     */
    public String getFormattedName() {
        return color + displayName;
    }
    
    /**
     * Gets the stat multiplier for this rarity
     * 
     * @return The stat multiplier
     */
    public double getStatMultiplier() {
        return statMultiplier;
    }
    
    /**
     * Gets the next higher rarity
     * 
     * @return The next rarity or null if already at highest
     */
    public MountRarity getNextRarity() {
        int ordinal = this.ordinal();
        if (ordinal < values().length - 1) {
            return values()[ordinal + 1];
        }
        return null;
    }
    
    /**
     * Gets the upgrade chance to the next rarity
     * 
     * @return The base percentage chance (0-100)
     */
    public int getUpgradeChance() {
        return switch (this) {
            case COMMON -> 75;
            case UNCOMMON -> 50;
            case RARE -> 30;
            case EPIC -> 15;
            case LEGENDARY -> 5;
            case MYTHIC -> 0;
        };
    }
} 