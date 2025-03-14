package org.frizzlenpop.rPGSkillsPlugin.mounts.types;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.mounts.ExtendedMount;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountRarity;
import org.frizzlenpop.rPGSkillsPlugin.utils.ColorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A special winged pig mount that can temporarily fly and then glide
 */
public class WingedPigMount extends ExtendedMount {
    private static final double BASE_SPEED = 0.4;
    private static final double MAX_FLIGHT_HEIGHT = 30;
    private static final int FLIGHT_DURATION_TICKS = 200; // 10 seconds
    private static final int GLIDE_DURATION_TICKS = 400; // 20 seconds
    
    private final Map<UUID, FlightState> playerFlightStates = new HashMap<>();
    private final Map<UUID, Long> flightCooldowns = new HashMap<>();
    private static final long FLIGHT_COOLDOWN_MS = 30000; // 30 seconds
    
    /**
     * Tracks the flight state of players using the winged pig
     */
    private enum FlightState {
        NONE,
        FLYING,
        GLIDING
    }
    
    /**
     * Creates a new winged pig mount
     * 
     * @param plugin The plugin instance
     * @param owner The owner of the mount
     * @param rarity The rarity of the mount
     */
    public WingedPigMount(RPGSkillsPlugin plugin, UUID owner, MountRarity rarity) {
        super(plugin, "winged_pig", owner, EntityType.PIG, rarity);
        
        // Register flight task
        Bukkit.getScheduler().runTaskTimer(plugin, this::handleFlight, 1L, 1L);
    }
    
    @Override
    public double getSpeed() {
        double rarityBonus = switch (getRarity()) {
            case COMMON -> 0.0;
            case UNCOMMON -> 0.1;
            case RARE -> 0.2;
            case EPIC -> 0.3;
            case LEGENDARY -> 0.4;
            case MYTHIC -> 0.5;
        };
        
        return BASE_SPEED + rarityBonus;
    }
    
    @Override
    public ItemStack createMountItem() {
        ItemStack item = new ItemStack(Material.SADDLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtils.color("&d&lWinged Pig Mount"));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtils.color("&7A special pig with wings that"));
        lore.add(ColorUtils.color("&7can fly for a short duration."));
        lore.add(ColorUtils.color("&7"));
        lore.add(ColorUtils.color("&7Rarity: " + getRarity().getDisplayName()));
        lore.add(ColorUtils.color("&7"));
        lore.add(ColorUtils.color("&7Speed: " + String.format("%.1f", getSpeed())));
        lore.add(ColorUtils.color("&7"));
        lore.add(ColorUtils.color("&e&lAbility: &7Double jump to fly"));
        lore.add(ColorUtils.color("&7for 10 seconds, then glide for"));
        lore.add(ColorUtils.color("&7another 20 seconds."));
        lore.add(ColorUtils.color("&7"));
        lore.add(ColorUtils.color("&7ID: " + getId()));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    @Override
    public void onSpawn() {
        if (getEntityReference() == null) return;
        
        Entity entity = getEntityReference().get();
        if (entity instanceof Pig pig) {
            // Set pig properties
            pig.setCustomName(ColorUtils.color("&d&l" + getName() + " &7(Owned by " + 
                Bukkit.getOfflinePlayer(getOwner()).getName() + ")"));
            pig.setCustomNameVisible(true);
            
            // Make the pig appear to have wings (through visual effects)
            spawnWingsEffect(pig);
        }
    }
    
    @Override
    public void onDismount(Player player) {
        super.onDismount(player);
        
        // Reset flight state
        playerFlightStates.remove(player.getUniqueId());
    }
    
    @Override
    public boolean onToggleAbility(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Check if mount is spawned and the player is riding it
        if (!isSpawned() || !player.equals(getCurrentRider())) {
            return false;
        }
        
        // Check if on cooldown
        Long cooldownTime = flightCooldowns.get(playerId);
        if (cooldownTime != null && System.currentTimeMillis() - cooldownTime < FLIGHT_COOLDOWN_MS) {
            long remainingSeconds = (FLIGHT_COOLDOWN_MS - (System.currentTimeMillis() - cooldownTime)) / 1000;
            player.sendMessage(ColorUtils.color("&c&lFlight Ability &7on cooldown for &c" + remainingSeconds + " &7seconds!"));
            return false;
        }
        
        // Get current flight state
        FlightState currentState = playerFlightStates.getOrDefault(playerId, FlightState.NONE);
        
        // Only activate if not already in a flight state
        if (currentState == FlightState.NONE) {
            // Start flying
            playerFlightStates.put(playerId, FlightState.FLYING);
            
            // Schedule state change to gliding after flight duration
            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                if (playerFlightStates.getOrDefault(playerId, FlightState.NONE) == FlightState.FLYING) {
                    playerFlightStates.put(playerId, FlightState.GLIDING);
                    
                    // Play transition sound
                    if (player.isOnline()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 0.5f);
                        player.sendMessage(ColorUtils.color("&e&lYour wings are tiring! &7Now gliding..."));
                    }
                    
                    // Schedule end of gliding
                    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                        if (playerFlightStates.getOrDefault(playerId, FlightState.NONE) == FlightState.GLIDING) {
                            playerFlightStates.put(playerId, FlightState.NONE);
                            flightCooldowns.put(playerId, System.currentTimeMillis());
                            
                            if (player.isOnline()) {
                                player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_HURT, 1.0f, 0.5f);
                                player.sendMessage(ColorUtils.color("&c&lYour wings are exhausted! &7Flight ability on cooldown."));
                            }
                        }
                    }, GLIDE_DURATION_TICKS);
                }
            }, FLIGHT_DURATION_TICKS);
            
            // Visual and sound effects
            player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_PIG_AMBIENT, 1.0f, 1.5f);
            player.sendMessage(ColorUtils.color("&a&lWings activated! &7Flying for 10 seconds!"));
            
            // Get the pig entity and play effects
            if (getEntityReference() != null && getEntityReference().get() instanceof Pig pig) {
                pig.playEffect(EntityEffect.LOVE_HEARTS);
                
                // Spawn wing flap particles
                for (int i = 0; i < 20; i++) {
                    pig.getWorld().spawnParticle(
                        Particle.valueOf("CLOUD"),
                        pig.getLocation().add(0, 1, 0),
                        10,
                        0.5, 0.3, 0.5,
                        0.05
                    );
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Handle the flying and gliding mechanics
     */
    private void handleFlight() {
        if (!isSpawned() || getEntityReference() == null) return;
        
        Entity entity = getEntityReference().get();
        if (!(entity instanceof Pig pig) || pig.getPassengers().isEmpty()) return;
        
        Entity passenger = pig.getPassengers().get(0);
        if (!(passenger instanceof Player player)) return;
        
        UUID playerId = player.getUniqueId();
        FlightState state = playerFlightStates.getOrDefault(playerId, FlightState.NONE);
        
        switch (state) {
            case FLYING -> {
                // Get the direction the player is looking
                Vector direction = player.getLocation().getDirection().multiply(getSpeed() * 1.5);
                
                // Apply upward force
                Vector velocity = direction.clone().add(new Vector(0, 0.3, 0));
                
                // Apply the velocity to the pig
                pig.setVelocity(velocity);
                
                // Spawn wing particles
                if (entity.getTicksLived() % 5 == 0) {
                    pig.getWorld().spawnParticle(
                        Particle.valueOf("CLOUD"),
                        pig.getLocation().add(0, 0.5, 0),
                        3,
                        0.5, 0.1, 0.5,
                        0.01
                    );
                }
                
                // Height limit check
                if (pig.getLocation().getY() > MAX_FLIGHT_HEIGHT) {
                    Vector limitedVelocity = velocity.clone();
                    limitedVelocity.setY(Math.min(0, limitedVelocity.getY()));
                    pig.setVelocity(limitedVelocity);
                }
            }
            case GLIDING -> {
                // Get the direction the player is looking
                Vector direction = player.getLocation().getDirection().multiply(getSpeed() * 1.2);
                
                // Apply gentle downward force
                Vector velocity = direction.clone().add(new Vector(0, -0.05, 0));
                
                // Apply the velocity to the pig
                pig.setVelocity(velocity);
                
                // Spawn occasional gliding particles
                if (entity.getTicksLived() % 10 == 0) {
                    pig.getWorld().spawnParticle(
                        Particle.valueOf("CLOUD"),
                        pig.getLocation().add(0, 0.5, 0),
                        1,
                        0.3, 0.1, 0.3,
                        0.01
                    );
                }
            }
            case NONE -> {
                // Regular movement, no special handling
            }
        }
    }
    
    /**
     * Creates a visual effect of wings on the pig
     * 
     * @param pig The pig entity
     */
    private void spawnWingsEffect(Pig pig) {
        // Create a repeating task for wing particles
        Bukkit.getScheduler().runTaskTimer(getPlugin(), task -> {
            if (!isSpawned() || pig.isDead()) {
                task.cancel();
                return;
            }
            
            // Spawn wing-like particles on the sides of the pig
            double offset = pig.getWidth() / 2;
            
            // Right wing
            Vector rightWing = new Vector(-offset, 0.7, 0);
            rotateVector(rightWing, pig.getLocation().getYaw());
            
            // Left wing
            Vector leftWing = new Vector(offset, 0.7, 0);
            rotateVector(leftWing, pig.getLocation().getYaw());
            
            // Spawn particles at wing positions
            pig.getWorld().spawnParticle(
                Particle.valueOf("REDSTONE"),
                pig.getLocation().add(rightWing),
                3,
                0.2, 0.1, 0.2,
                new org.bukkit.Particle.DustOptions(org.bukkit.Color.WHITE, 1.0f)
            );
            
            pig.getWorld().spawnParticle(
                Particle.valueOf("REDSTONE"),
                pig.getLocation().add(leftWing),
                3,
                0.2, 0.1, 0.2,
                new org.bukkit.Particle.DustOptions(org.bukkit.Color.WHITE, 1.0f)
            );
            
        }, 0L, 5L); // Run every 5 ticks (1/4 second)
    }
    
    /**
     * Rotates a vector around the Y axis
     * 
     * @param vector The vector to rotate
     * @param yaw The yaw angle in degrees
     */
    private void rotateVector(Vector vector, float yaw) {
        double rad = Math.toRadians(yaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);
        
        double x = vector.getX() * cos - vector.getZ() * sin;
        double z = vector.getX() * sin + vector.getZ() * cos;
        
        vector.setX(x).setZ(z);
    }
} 