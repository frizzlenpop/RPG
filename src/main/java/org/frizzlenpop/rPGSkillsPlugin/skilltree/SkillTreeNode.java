package org.frizzlenpop.rPGSkillsPlugin.skilltree;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Represents a node in the skill tree that players can spend points on
 */
public class SkillTreeNode {
    private final String id;
    private final String name;
    private final String description;
    private final int pointCost;
    private final List<String> prerequisites;
    private final Material icon;
    private final NodeType type;
    private final List<NodeEffect> effects;
    
    /**
     * Type of node in the skill tree
     */
    public enum NodeType {
        PASSIVE,      // Permanent passive buff
        ABILITY,      // Unlocks an active ability
        STAT_BOOST,   // Increases player stats
        MASTERY       // Special high-tier node that affects multiple skills
    }
    
    /**
     * Effect of a node when activated
     */
    public static class NodeEffect {
        private final EffectType type;
        private final String target;
        private final double value;
        
        public NodeEffect(EffectType type, String target, double value) {
            this.type = type;
            this.target = target;
            this.value = value;
        }
        
        public EffectType getType() {
            return type;
        }
        
        public String getTarget() {
            return target;
        }
        
        public double getValue() {
            return value;
        }
    }
    
    /**
     * Type of effect applied by a node
     */
    public enum EffectType {
        ATTRIBUTE_BOOST,     // Increases a player attribute (health, speed, etc.)
        SKILL_XP_BOOST,      // Increases XP gain for a skill
        DAMAGE_BOOST,        // Increases damage with specific tools/weapons
        RESOURCE_BOOST,      // Increases resource yield (mining, logging, etc.)
        ABILITY_UNLOCK,      // Unlocks a special ability
        COOLDOWN_REDUCTION,  // Reduces cooldowns for abilities
        CUSTOM_EFFECT        // Special effect handled by custom logic
    }
    
    /**
     * Constructor for a skill tree node
     */
    public SkillTreeNode(String id, String name, String description, int pointCost, 
                         List<String> prerequisites, Material icon, NodeType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pointCost = pointCost;
        this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<>();
        this.icon = icon;
        this.type = type;
        this.effects = new ArrayList<>();
    }
    
    /**
     * Add an effect to this node
     */
    public void addEffect(NodeEffect effect) {
        effects.add(effect);
    }
    
    /**
     * Apply this node's effects to a player
     */
    public void applyEffects(Player player) {
        for (NodeEffect effect : effects) {
            applyEffect(player, effect);
        }
    }
    
    /**
     * Remove this node's effects from a player
     */
    public void removeEffects(Player player) {
        for (NodeEffect effect : effects) {
            removeEffect(player, effect);
        }
    }
    
    /**
     * Apply a single effect to a player
     */
    private void applyEffect(Player player, NodeEffect effect) {
        switch (effect.getType()) {
            case ATTRIBUTE_BOOST:
                applyAttributeEffect(player, effect);
                break;
            case SKILL_XP_BOOST:
                // XP boosts are applied dynamically when XP is gained
                break;
            case DAMAGE_BOOST:
                // Damage boosts are applied in damage listener
                break;
            case RESOURCE_BOOST:
                // Resource boosts are applied in block break listener
                break;
            case ABILITY_UNLOCK:
                // Ability unlocks are tracked in player data
                break;
            case COOLDOWN_REDUCTION:
                // Cooldown reductions are applied when abilities are used
                break;
            case CUSTOM_EFFECT:
                // Custom effects are handled separately
                break;
        }
    }
    
    /**
     * Remove a single effect from a player
     */
    private void removeEffect(Player player, NodeEffect effect) {
        if (effect.getType() == EffectType.ATTRIBUTE_BOOST) {
            try {
                Attribute attribute = Attribute.valueOf(effect.getTarget());
                
                // Generate the same UUID used when applying the effect
                UUID modifierUUID = UUID.nameUUIDFromBytes(
                    (player.getUniqueId().toString() + "." + id + "." + attribute.name()).getBytes()
                );
                
                // Remove the modifier with this UUID
                player.getAttribute(attribute).getModifiers().forEach(existingModifier -> {
                    if (existingModifier.getUniqueId().equals(modifierUUID)) {
                        player.getAttribute(attribute).removeModifier(existingModifier);
                    }
                });
            } catch (IllegalArgumentException e) {
                // Invalid attribute
            }
        }
        // Other effect types might need specific removal logic
    }
    
    /**
     * Apply an attribute modification effect
     */
    private void applyAttributeEffect(Player player, NodeEffect effect) {
        try {
            Attribute attribute = Attribute.valueOf(effect.getTarget());
            
            // Generate a consistent UUID based on the player UUID, node ID, and attribute
            // This ensures we don't add duplicate modifiers
            UUID modifierUUID = UUID.nameUUIDFromBytes(
                (player.getUniqueId().toString() + "." + id + "." + attribute.name()).getBytes()
            );
            
            AttributeModifier modifier = new AttributeModifier(
                modifierUUID,
                "skilltree." + id,
                effect.getValue(),
                AttributeModifier.Operation.ADD_NUMBER
            );
            
            // Remove existing modifier with the same UUID if it exists
            player.getAttribute(attribute).getModifiers().forEach(existingModifier -> {
                if (existingModifier.getUniqueId().equals(modifierUUID)) {
                    player.getAttribute(attribute).removeModifier(existingModifier);
                }
            });
            
            // Add the new modifier
            player.getAttribute(attribute).addModifier(modifier);
        } catch (IllegalArgumentException e) {
            // Invalid attribute
        }
    }
    
    /**
     * Create an item representation of this node for GUI
     */
    public ItemStack createIcon(boolean unlocked, boolean available) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name based on status
            if (unlocked) {
                meta.setDisplayName("§a" + name + " §7(Unlocked)");
            } else if (available) {
                meta.setDisplayName("§e" + name + " §7(Available)");
            } else {
                meta.setDisplayName("§8" + name + " §7(Locked)");
            }
            
            // Add description and details
            List<String> lore = new ArrayList<>();
            lore.add("§7" + description);
            lore.add("");
            lore.add("§7Cost: §e" + pointCost + " point" + (pointCost > 1 ? "s" : ""));
            
            // Add prerequisites if any
            if (!prerequisites.isEmpty()) {
                lore.add("");
                lore.add("§7Requires:");
                for (String prereq : prerequisites) {
                    lore.add("§8- " + prereq);
                }
            }
            
            // Add effects
            if (!effects.isEmpty()) {
                lore.add("");
                lore.add("§7Effects:");
                for (NodeEffect effect : effects) {
                    lore.add("§8- " + formatEffect(effect));
                }
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Format an effect description for display
     */
    private String formatEffect(NodeEffect effect) {
        switch (effect.getType()) {
            case ATTRIBUTE_BOOST:
                return String.format("+%.1f %s", effect.getValue(), formatAttributeName(effect.getTarget()));
            case SKILL_XP_BOOST:
                return String.format("+%.0f%% %s XP", effect.getValue() * 100, effect.getTarget());
            case DAMAGE_BOOST:
                return String.format("+%.0f%% damage with %s", effect.getValue() * 100, effect.getTarget());
            case RESOURCE_BOOST:
                return String.format("+%.0f%% %s yields", effect.getValue() * 100, effect.getTarget());
            case ABILITY_UNLOCK:
                return String.format("Unlock %s ability", effect.getTarget());
            case COOLDOWN_REDUCTION:
                return String.format("-%.0f%% cooldown for %s", effect.getValue() * 100, effect.getTarget());
            case CUSTOM_EFFECT:
                return effect.getTarget();
            default:
                return "Unknown effect";
        }
    }
    
    /**
     * Format attribute name for display
     */
    private String formatAttributeName(String attributeName) {
        // Convert GENERIC_MAX_HEALTH to "Max Health" etc.
        String name = attributeName.replace("GENERIC_", "");
        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(word.charAt(0)).append(word.substring(1).toLowerCase());
            }
        }
        
        return result.toString();
    }
    
    // Getters
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getPointCost() {
        return pointCost;
    }
    
    public List<String> getPrerequisites() {
        return prerequisites;
    }
    
    public Material getIcon() {
        return icon;
    }
    
    public NodeType getType() {
        return type;
    }
    
    public List<NodeEffect> getEffects() {
        return effects;
    }
} 