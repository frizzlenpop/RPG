package org.frizzlenpop.rPGSkillsPlugin.mounts.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.mounts.Mount;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountRarity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Handles projectile-based abilities for mounts
 */
public class MountProjectileAbility {
    private final RPGSkillsPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Random random = new Random();
    
    // Cooldown constants
    private static final long BASE_COOLDOWN_MS = 5000; // 5 seconds base cooldown
    
    /**
     * Defines different types of projectiles that mounts can shoot
     */
    public enum ProjectileType {
        FIREBALL("Fireball", 1.5, 20.0, 5.0, "FLAME", Material.FIRE_CHARGE, Sound.ENTITY_BLAZE_SHOOT,
                Sound.ENTITY_GENERIC_EXPLODE, DamageCause.FIRE),
                
        SHADOW_BOLT("Shadow Bolt", 2.0, 25.0, 6.0, "SMOKE_NORMAL", Material.COAL, Sound.ENTITY_WITHER_SHOOT,
                Sound.ENTITY_WITHER_HURT, DamageCause.MAGIC),
                
        CRYSTAL_SHARD("Crystal Shard", 2.5, 15.0, 4.0, "PORTAL", Material.AMETHYST_SHARD, Sound.BLOCK_AMETHYST_BLOCK_BREAK,
                Sound.BLOCK_GLASS_BREAK, DamageCause.MAGIC),
                
        LIGHTNING_BOLT("Lightning Bolt", 3.0, 30.0, 8.0, "CRIT", Material.LIGHTNING_ROD, Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                Sound.ENTITY_LIGHTNING_BOLT_IMPACT, DamageCause.LIGHTNING),
                
        ROCK_THROW("Rock Throw", 1.0, 15.0, 7.0, "EXPLOSION_NORMAL", Material.COBBLESTONE, Sound.ENTITY_WITCH_THROW,
                Sound.BLOCK_STONE_BREAK, DamageCause.FALLING_BLOCK);
        
        private final String displayName;
        private final double speed;
        private final double range;
        private final double damage;
        private final String particleType;
        private final Material projectileMaterial;
        private final Sound launchSound;
        private final Sound impactSound;
        private final DamageCause damageCause;
        
        ProjectileType(String displayName, double speed, double range, double damage, String particleType, 
                      Material projectileMaterial, Sound launchSound, Sound impactSound, DamageCause damageCause) {
            this.displayName = displayName;
            this.speed = speed;
            this.range = range;
            this.damage = damage;
            this.particleType = particleType;
            this.projectileMaterial = projectileMaterial;
            this.launchSound = launchSound;
            this.impactSound = impactSound;
            this.damageCause = damageCause;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public double getSpeed() {
            return speed;
        }
        
        public double getRange() {
            return range;
        }
        
        public double getBaseDamage() {
            return damage;
        }
        
        public String getParticleType() {
            return particleType;
        }
        
        public Material getProjectileMaterial() {
            return projectileMaterial;
        }
        
        public Sound getLaunchSound() {
            return launchSound;
        }
        
        public Sound getImpactSound() {
            return impactSound;
        }
        
        public DamageCause getDamageCause() {
            return damageCause;
        }
    }
    
    public MountProjectileAbility(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Launches a projectile from a mount
     * 
     * @param player The player riding the mount
     * @param mount The mount shooting the projectile
     * @param projectileType The type of projectile to shoot
     * @return true if the projectile was launched, false if on cooldown
     */
    public boolean launchProjectile(Player player, Mount mount, ProjectileType projectileType) {
        UUID playerUUID = player.getUniqueId();
        
        // Check cooldown
        if (isOnCooldown(playerUUID, projectileType)) {
            long remaining = getCooldownRemaining(playerUUID, projectileType);
            player.sendMessage("§c" + projectileType.getDisplayName() + " on cooldown for " + remaining / 1000.0 + " seconds!");
            return false;
        }
        
        // Get entity, location, and direction
        Entity mountEntity = mount.getEntity();
        if (mountEntity == null || !mountEntity.isValid()) {
            return false;
        }
        
        // Get launch position (slightly ahead of mount)
        Location mountLoc = mountEntity.getLocation();
        Vector direction = mountLoc.getDirection().normalize();
        Location launchLoc = mountLoc.clone().add(direction.clone().multiply(1.2)).add(0, 1.0, 0);
        
        // Apply cooldown
        applyCooldown(playerUUID, projectileType, calculateCooldown(mount.getRarity()));
        
        // Play launch sound
        mountEntity.getWorld().playSound(launchLoc, projectileType.getLaunchSound(), 1.0f, 1.0f);
        
        // Track the projectile
        trackProjectile(player, mount, projectileType, launchLoc, direction);
        
        return true;
    }
    
    /**
     * Tracks a projectile's movement and handles impacts
     */
    private void trackProjectile(Player player, Mount mount, ProjectileType projectileType, Location startLoc, Vector direction) {
        World world = startLoc.getWorld();
        if (world == null) return;
        
        // Calculate projectile properties based on mount rarity
        double damage = calculateDamage(projectileType, mount.getRarity());
        double speed = projectileType.getSpeed();
        double maxDistance = projectileType.getRange();
        final double[] distanceTraveled = {0}; // Using array to make it effectively final
        
        // Normalize the direction vector and scale by speed
        Vector velocity = direction.clone().normalize().multiply(speed);
        
        // Get the current location (will be updated in the loop)
        Location currentLoc = startLoc.clone();
        
        // Create a bukkit runnable to move the projectile
        new BukkitRunnable() {
            @Override
            public void run() {
                // Move the projectile
                currentLoc.add(velocity);
                
                // Spawn particles at current location
                spawnProjectileParticles(projectileType, currentLoc);
                
                // Check for collision
                boolean collided = checkCollision(player, mount, projectileType, currentLoc, damage);
                
                // Update distance traveled
                distanceTraveled[0] += speed;
                
                // Check if projectile reached max distance or collided
                if (distanceTraveled[0] >= maxDistance || collided || !world.isChunkLoaded(currentLoc.getBlockX() >> 4, currentLoc.getBlockZ() >> 4)) {
                    if (collided) {
                        // Handle impact at collision point
                        handleProjectileImpact(projectileType, currentLoc, player);
                    }
                    cancel(); // Stop the runnable
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Run every tick
    }
    
    /**
     * Checks if the projectile collided with a block or entity
     * 
     * @return true if collision detected
     */
    private boolean checkCollision(Player shooter, Mount mount, ProjectileType projectileType, Location location, double damage) {
        World world = location.getWorld();
        if (world == null) return false;
        
        // Check for block collision
        if (location.getBlock().getType().isSolid()) {
            return true;
        }
        
        // Check for entity collision (within 1 block radius)
        for (Entity entity : world.getNearbyEntities(location, 1, 1, 1)) {
            // Skip the shooter and their mount
            if (entity.equals(shooter) || entity.equals(mount.getEntity())) {
                continue;
            }
            
            // Skip non-living entities
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }
            
            // Apply damage with attribution to the player
            applyDamageWithAttribution(livingEntity, shooter, damage, projectileType.getDamageCause(), mount);
            return true;
        }
        
        return false;
    }
    
    /**
     * Applies damage to an entity and attributes it to the player for XP tracking
     */
    private void applyDamageWithAttribution(LivingEntity target, Player shooter, double damage, DamageCause cause, Mount mount) {
        // Tag the entity with metadata to track that it was damaged by a mount projectile
        target.setMetadata("mount_projectile_damage", new FixedMetadataValue(plugin, shooter.getUniqueId().toString()));
        target.setMetadata("mount_damage_amount", new FixedMetadataValue(plugin, damage));
        target.setMetadata("mount_rarity", new FixedMetadataValue(plugin, mount.getRarity().name()));
        
        // Apply the damage
        EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(
                shooter, target, cause, damage);
        plugin.getServer().getPluginManager().callEvent(damageEvent);
        
        if (!damageEvent.isCancelled()) {
            target.damage(damageEvent.getDamage(), shooter);
        }
    }
    
    /**
     * Handles the impact effect of a projectile
     */
    private void handleProjectileImpact(ProjectileType projectileType, Location location, Player shooter) {
        World world = location.getWorld();
        if (world == null) return;
        
        // Play impact sound
        world.playSound(location, projectileType.getImpactSound(), 1.0f, 1.0f);
        
        // Create impact particles based on projectile type
        switch (projectileType) {
            case FIREBALL:
                // Fire explosion effect
                createParticleEffect(world, location, "FLAME", 20, 0.5, 0.05);
                createParticleEffect(world, location, "SMOKE_NORMAL", 15, 0.3, 0.03);
                createParticleEffect(world, location, "LAVA", 5, 0.2, 0);
                break;
            case SHADOW_BOLT:
                // Shadow magic effect
                createParticleEffect(world, location, "SMOKE_NORMAL", 30, 0.5, 0.03);
                createParticleEffect(world, location, "PORTAL", 20, 0.3, 0.05);
                break;
            case CRYSTAL_SHARD:
                // Crystal shard explosion
                createParticleEffect(world, location, "CRIT", 20, 0.4, 0.05);
                createParticleEffect(world, location, "PORTAL", 15, 0.3, 0.03);
                break;
            case LIGHTNING_BOLT:
                // Lightning strike effect
                createParticleEffect(world, location, "FLASH", 2, 0.1, 0);
                createParticleEffect(world, location, "CRIT", 30, 0.5, 0.1);
                createParticleEffect(world, location, "FLAME", 20, 0.3, 0.05);
                
                // Add lightning visual (no damage)
                world.strikeLightningEffect(location);
                break;
            case ROCK_THROW:
                // Rock impact
                createParticleEffect(world, location, "EXPLOSION_NORMAL", 10, 0.3, 0.05);
                createBlockDustEffect(world, location, Material.COBBLESTONE, 30, 0.5, 0.05);
                break;
        }
    }
    
    /**
     * Spawns particles for the projectile in flight
     */
    private void spawnProjectileParticles(ProjectileType projectileType, Location location) {
        World world = location.getWorld();
        if (world == null) return;
        
        switch (projectileType) {
            case FIREBALL:
                // Fire trail
                createParticleEffect(world, location, "FLAME", 5, 0.1, 0.01);
                createParticleEffect(world, location, "SMOKE_NORMAL", 2, 0.05, 0);
                break;
            case SHADOW_BOLT:
                // Shadow trail
                createParticleEffect(world, location, "SMOKE_NORMAL", 4, 0.1, 0.01);
                createParticleEffect(world, location, "PORTAL", 1, 0.05, 0);
                break;
            case CRYSTAL_SHARD:
                // Crystal trail
                createParticleEffect(world, location, "CRIT", 3, 0.05, 0.01);
                break;
            case LIGHTNING_BOLT:
                // Lightning trail
                createParticleEffect(world, location, "CRIT", 5, 0.1, 0.01);
                break;
            case ROCK_THROW:
                // Rock trail
                createParticleEffect(world, location, "FLAME", 2, 0.1, 0.01);
                createBlockDustEffect(world, location, Material.COBBLESTONE, 3, 0.1, 0.01);
                break;
        }
    }
    
    /**
     * Creates a particle effect with fallback to basic particles if the specific one is not available
     */
    private void createParticleEffect(World world, Location location, String particleName, int count, double radius, double speed) {
        try {
            Particle particle = Particle.valueOf(particleName);
            world.spawnParticle(particle, location, count, radius, radius, radius, speed);
        } catch (IllegalArgumentException e) {
            // Fallback to a basic particle if the one specified isn't available
            try {
                // Try FLAME as a common fallback
                world.spawnParticle(Particle.FLAME, location, count, radius, radius, radius, speed);
            } catch (Exception ex) {
                // Log the error but don't crash
                plugin.getLogger().log(Level.WARNING, "Could not spawn particle effect: " + e.getMessage());
            }
        }
    }
    
    /**
     * Creates a block dust effect with fallback
     */
    private void createBlockDustEffect(World world, Location location, Material material, int count, double radius, double speed) {
        try {
            // Try to use BLOCK_CRACK if available
            try {
                Particle blockCrack = Particle.valueOf("BLOCK_CRACK");
                world.spawnParticle(blockCrack, location, count, radius, radius, radius, speed, new ItemStack(material).getData());
            } catch (NoSuchMethodError | IllegalArgumentException e) {
                // Fallback to basic particle
                createParticleEffect(world, location, "FLAME", count, radius, speed);
            }
        } catch (Exception e) {
            // Just log and continue if all fails
            plugin.getLogger().log(Level.WARNING, "Could not spawn block dust effect: " + e.getMessage());
        }
    }
    
    /**
     * Calculate damage based on projectile type and mount rarity
     */
    private double calculateDamage(ProjectileType projectileType, MountRarity rarity) {
        double baseDamage = projectileType.getBaseDamage();
        double rarityMultiplier = getRarityDamageMultiplier(rarity);
        
        // Add some randomness (±10%)
        double randomFactor = 0.9 + (random.nextDouble() * 0.2); // 0.9 to 1.1
        
        return baseDamage * rarityMultiplier * randomFactor;
    }
    
    /**
     * Gets the damage multiplier based on mount rarity
     */
    private double getRarityDamageMultiplier(MountRarity rarity) {
        return switch (rarity) {
            case COMMON -> 1.0;
            case UNCOMMON -> 1.2;
            case RARE -> 1.4;
            case EPIC -> 1.7;
            case LEGENDARY -> 2.0;
            case MYTHIC -> 2.5;
        };
    }
    
    /**
     * Calculates cooldown based on mount rarity
     */
    private long calculateCooldown(MountRarity rarity) {
        double cooldownMultiplier = switch (rarity) {
            case COMMON -> 1.0;
            case UNCOMMON -> 0.9;
            case RARE -> 0.8;
            case EPIC -> 0.7;
            case LEGENDARY -> 0.6;
            case MYTHIC -> 0.5;
        };
        
        return (long) (BASE_COOLDOWN_MS * cooldownMultiplier);
    }
    
    /**
     * Apply cooldown for a player and projectile type
     */
    private void applyCooldown(UUID playerUUID, ProjectileType projectileType, long duration) {
        String key = playerUUID.toString() + ":" + projectileType.name();
        cooldowns.put(UUID.fromString(key), System.currentTimeMillis() + duration);
    }
    
    /**
     * Check if player is on cooldown for this projectile type
     */
    private boolean isOnCooldown(UUID playerUUID, ProjectileType projectileType) {
        String key = playerUUID.toString() + ":" + projectileType.name();
        UUID keyUUID = UUID.fromString(key);
        Long cooldownEnd = cooldowns.get(keyUUID);
        return cooldownEnd != null && cooldownEnd > System.currentTimeMillis();
    }
    
    /**
     * Get the remaining cooldown time in milliseconds
     */
    private long getCooldownRemaining(UUID playerUUID, ProjectileType projectileType) {
        String key = playerUUID.toString() + ":" + projectileType.name();
        UUID keyUUID = UUID.fromString(key);
        Long cooldownEnd = cooldowns.get(keyUUID);
        if (cooldownEnd == null || cooldownEnd <= System.currentTimeMillis()) {
            return 0;
        }
        return cooldownEnd - System.currentTimeMillis();
    }
    
    /**
     * Gets the best projectile type available for a given mount based on its rarity
     */
    public ProjectileType getBestProjectileForMount(Mount mount) {
        MountRarity rarity = mount.getRarity();
        
        // Return different projectile types based on mount rarity
        return switch (rarity) {
            case COMMON -> ProjectileType.FIREBALL;
            case UNCOMMON -> ProjectileType.ROCK_THROW;
            case RARE -> ProjectileType.CRYSTAL_SHARD;
            case EPIC -> ProjectileType.SHADOW_BOLT;
            case LEGENDARY, MYTHIC -> ProjectileType.LIGHTNING_BOLT;
        };
    }
    
    /**
     * Gets all available projectile types for a mount based on its rarity
     */
    public ProjectileType[] getAvailableProjectiles(Mount mount) {
        MountRarity rarity = mount.getRarity();
        
        return switch (rarity) {
            case COMMON -> new ProjectileType[] { ProjectileType.FIREBALL };
            case UNCOMMON -> new ProjectileType[] { ProjectileType.FIREBALL, ProjectileType.ROCK_THROW };
            case RARE -> new ProjectileType[] { ProjectileType.FIREBALL, ProjectileType.ROCK_THROW, ProjectileType.CRYSTAL_SHARD };
            case EPIC -> new ProjectileType[] { ProjectileType.FIREBALL, ProjectileType.ROCK_THROW, 
                                                ProjectileType.CRYSTAL_SHARD, ProjectileType.SHADOW_BOLT };
            case LEGENDARY, MYTHIC -> new ProjectileType[] { ProjectileType.FIREBALL, ProjectileType.ROCK_THROW,
                                                           ProjectileType.CRYSTAL_SHARD, ProjectileType.SHADOW_BOLT, 
                                                           ProjectileType.LIGHTNING_BOLT };
        };
    }
} 