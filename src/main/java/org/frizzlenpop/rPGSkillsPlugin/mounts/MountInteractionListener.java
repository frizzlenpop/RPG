package org.frizzlenpop.rPGSkillsPlugin.mounts;

import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.HorseInventory;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

/**
 * Listener to protect mounts from unwanted interactions.
 * Prevents players from accessing mount inventories while riding
 * and other players from manipulating others' mounts.
 */
public class MountInteractionListener implements Listener {
    private final RPGSkillsPlugin plugin;
    private final MountManager mountManager;
    
    /**
     * Creates a new mount interaction listener
     * 
     * @param plugin The plugin instance
     * @param mountManager The mount manager
     */
    public MountInteractionListener(RPGSkillsPlugin plugin, MountManager mountManager) {
        this.plugin = plugin;
        this.mountManager = mountManager;
    }
    
    /**
     * Prevents players from accessing their mount's inventory while riding it.
     * This stops players from removing saddles and armor while riding.
     * 
     * @param event The inventory open event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        
        // Check if the inventory belongs to a horse
        if (event.getInventory().getHolder() instanceof AbstractHorse horse) {
            // Check if the player is riding this horse
            if (player.getVehicle() != null && player.getVehicle().equals(horse)) {
                // Cancel the inventory opening - can't access inventory while riding
                event.setCancelled(true);
                player.sendMessage("§cYou cannot access the mount's inventory while riding it.");
            }
        }
    }
    
    /**
     * Prevents players from interacting with another player's mount.
     * This adds additional protection for player mounts.
     * 
     * @param event The player interact entity event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity targetEntity = event.getRightClicked();
        
        // Check if the entity is a mount owned by the system
        if (targetEntity instanceof AbstractHorse horse) {
            // If this is a managed mount and not owned by this player, cancel interaction
            Mount mount = mountManager.getMountByEntity(targetEntity);
            if (mount != null && !mount.getOwnerUUID().equals(player.getUniqueId())) {
                // Player is not the owner, cancel interaction
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Prevents players from damaging another player's mount.
     * 
     * @param event The entity damage by entity event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        
        Entity targetEntity = event.getEntity();
        
        // Check if the entity is a managed mount
        if (mountManager.getMountByEntity(targetEntity) != null) {
            // Check if player is not the owner
            Mount mount = mountManager.getMountByEntity(targetEntity);
            if (mount != null && !mount.getOwnerUUID().equals(player.getUniqueId())) {
                // Player is not the owner, cancel damage
                event.setCancelled(true);
            }
        }
    }
} 