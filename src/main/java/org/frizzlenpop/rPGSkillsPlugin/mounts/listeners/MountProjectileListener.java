package org.frizzlenpop.rPGSkillsPlugin.mounts.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.mounts.Mount;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.abilities.MountAbilityManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.abilities.MountProjectileAbility;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Listener class for handling mount projectile interactions
 */
public class MountProjectileListener implements Listener {
    private final RPGSkillsPlugin plugin;
    private final MountManager mountManager;
    private final MountAbilityManager abilityManager;
    private final XPManager xpManager;
    private final Map<UUID, Long> rightClickCooldowns = new HashMap<>();
    private static final long RIGHT_CLICK_COOLDOWN_MS = 1000; // 1 second cooldown for right-click attacks
    
    public MountProjectileListener(RPGSkillsPlugin plugin, MountManager mountManager, 
                                  MountAbilityManager abilityManager, XPManager xpManager) {
        this.plugin = plugin;
        this.mountManager = mountManager;
        this.abilityManager = abilityManager;
        this.xpManager = xpManager;
    }
    
    /**
     * Handle player right-click to trigger mount attacks
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is right-clicking with main hand
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        // Check if player is riding a mount
        Entity vehicle = player.getVehicle();
        if (vehicle == null) {
            return;
        }
        
        // Get the mount instance
        Mount mount = mountManager.getMountByEntity(vehicle);
        if (mount == null) {
            return;
        }
        
        // Check cooldown
        UUID playerUUID = player.getUniqueId();
        if (isOnRightClickCooldown(playerUUID)) {
            long remaining = getRightClickCooldownRemaining(playerUUID);
            if (remaining > 0) {
                // Don't spam the player with messages, only show if the cooldown is significant
                if (remaining > 500) {
                    player.sendMessage("§cYour mount's attack is on cooldown for " + 
                            String.format("%.1f", remaining / 1000.0) + " seconds!");
                }
                return;
            }
        }
        
        // Execute the projectile attack
        MountProjectileAbility.ProjectileType bestProjectile = abilityManager.getProjectileAbility().getBestProjectileForMount(mount);
        boolean success = abilityManager.getProjectileAbility().launchProjectile(player, mount, bestProjectile);
        
        if (success) {
            // Apply right-click cooldown
            applyRightClickCooldown(playerUUID);
            
            // Send success message
            player.sendMessage("§aYour " + mount.getType().getDisplayName() + 
                    " unleashed a " + bestProjectile.getDisplayName() + "!");
                    
            // Prevent the interaction from triggering anything else
            event.setCancelled(true);
        }
    }
    
    /**
     * Handle player interactions to trigger secondary mount actions with off-hand
     */
    @EventHandler
    public void onPlayerOffHandInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is interacting with the offhand
        if (event.getHand() != EquipmentSlot.OFF_HAND) {
            return;
        }
        
        // Check if player is riding a mount
        Entity vehicle = player.getVehicle();
        if (vehicle == null) {
            return;
        }
        
        // Get the mount instance
        Mount mount = mountManager.getMountByEntity(vehicle);
        if (mount == null) {
            return;
        }
        
        // Execute the secondary action
        boolean success = abilityManager.executeSecondaryAction(player, mount);
        
        if (success) {
            // Prevent the interaction from triggering anything else
            event.setCancelled(true);
        }
    }
    
    /**
     * Handle entity deaths to track XP from mount projectile kills
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        
        // Check if entity was damaged by a mount projectile
        if (!entity.hasMetadata("mount_projectile_damage")) {
            return;
        }
        
        try {
            // Get metadata values
            List<MetadataValue> values = entity.getMetadata("mount_projectile_damage");
            if (values.isEmpty()) {
                return;
            }
            
            // Get the player UUID from metadata
            String playerUUIDString = values.get(0).asString();
            UUID playerUUID = UUID.fromString(playerUUIDString);
            
            // Get the player
            Player player = plugin.getServer().getPlayer(playerUUID);
            if (player == null || !player.isOnline()) {
                return;
            }
            
            // Award XP to the player - use the standard fighting XP
            xpManager.addXP(player, "fighting", calculateXPForEntity(entity));
            
            // Clear the metadata
            entity.removeMetadata("mount_projectile_damage", plugin);
            entity.removeMetadata("mount_damage_amount", plugin);
            entity.removeMetadata("mount_rarity", plugin);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error processing mount projectile XP: " + e.getMessage());
        }
    }
    
    /**
     * Calculate fighting XP for an entity
     */
    private int calculateXPForEntity(Entity entity) {
        // Base XP values similar to what might be in the FightingListener
        String entityType = entity.getType().toString().toLowerCase();
        
        // Default values that could be overridden by configuration
        return switch (entityType) {
            case "zombie", "skeleton", "spider", "creeper" -> 5;
            case "enderman", "witch", "blaze" -> 10;
            case "wither_skeleton" -> 15;
            case "warden", "ender_dragon", "wither" -> 100;
            default -> 3; // Default for other mobs
        };
    }
    
    /**
     * Handle direct damage from projectiles to track in damage stats
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        
        // Check if entity was damaged by a mount projectile
        if (!damaged.hasMetadata("mount_projectile_damage")) {
            return;
        }
        
        // Get the damage amount (could be different from original due to armor etc.)
        double finalDamage = event.getFinalDamage();
        
        try {
            // Get metadata values
            List<MetadataValue> values = damaged.getMetadata("mount_projectile_damage");
            if (values.isEmpty()) {
                return;
            }
            
            // Get the player UUID from metadata
            String playerUUIDString = values.get(0).asString();
            UUID playerUUID = UUID.fromString(playerUUIDString);
            
            // Get the player
            Player player = plugin.getServer().getPlayer(playerUUID);
            if (player == null || !player.isOnline()) {
                return;
            }
            
            // You could update damage statistics here if needed
            player.sendMessage("§6Your mount dealt §c" + String.format("%.1f", finalDamage) + " §6damage!");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error processing mount projectile damage: " + e.getMessage());
        }
    }
    
    /**
     * Apply a cooldown for right-click attacks
     */
    private void applyRightClickCooldown(UUID playerUUID) {
        rightClickCooldowns.put(playerUUID, System.currentTimeMillis() + RIGHT_CLICK_COOLDOWN_MS);
    }
    
    /**
     * Check if player is on cooldown for right-click attacks
     */
    private boolean isOnRightClickCooldown(UUID playerUUID) {
        Long cooldownEnd = rightClickCooldowns.get(playerUUID);
        return cooldownEnd != null && cooldownEnd > System.currentTimeMillis();
    }
    
    /**
     * Get the remaining cooldown time for right-click attacks in milliseconds
     */
    private long getRightClickCooldownRemaining(UUID playerUUID) {
        Long cooldownEnd = rightClickCooldowns.get(playerUUID);
        if (cooldownEnd == null || cooldownEnd <= System.currentTimeMillis()) {
            return 0;
        }
        return cooldownEnd - System.currentTimeMillis();
    }
} 