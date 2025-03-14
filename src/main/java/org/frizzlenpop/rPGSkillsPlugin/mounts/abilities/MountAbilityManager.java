package org.frizzlenpop.rPGSkillsPlugin.mounts.abilities;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.mounts.Mount;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountType;
import org.frizzlenpop.rPGSkillsPlugin.mounts.xp.MountXPManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.abilities.MountProjectileAbility;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages mount abilities and effects.
 */
public class MountAbilityManager implements Listener {
    private final RPGSkillsPlugin plugin;
    private final MountManager mountManager;
    private final Map<UUID, Map<String, Long>> abilityCooldowns = new ConcurrentHashMap<>();
    private MountProjectileAbility projectileAbility;
    
    /**
     * Creates a new mount ability manager
     * 
     * @param plugin The plugin instance
     * @param mountManager The mount manager
     */
    public MountAbilityManager(RPGSkillsPlugin plugin, MountManager mountManager) {
        this.plugin = plugin;
        this.mountManager = mountManager;
        this.projectileAbility = new MountProjectileAbility(plugin);
    }
    
    /**
     * Processes passive mount abilities
     * 
     * @param mount The mount to process abilities for
     */
    public void processPassiveAbilities(Mount mount) {
        UUID playerUUID = mount.getOwnerUUID();
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !player.isOnline()) return;
        
        MountType mountType = mount.getType();
        
        // Process mount type specific passive abilities
        switch (mountType.getId()) {
            case "phoenix_blaze" -> processPhoenixPassives(player, mount);
            case "shadow_steed" -> processShadowSteedPassives(player, mount);
            case "crystal_drake" -> processCrystalDrakePassives(player, mount);
            case "storm_charger" -> processStormChargerPassives(player, mount);
            case "ancient_golem" -> processAncientGolemPassives(player, mount);
        }
    }
    
    /**
     * Processes phoenix mount passive abilities
     * 
     * @param player The player
     * @param mount The mount
     */
    private void processPhoenixPassives(Player player, Mount mount) {
        // Fire immunity
        if (isAbilityUnlocked(player.getUniqueId(), mount, "fire_immunity") && 
                player.getFireTicks() > 0) {
            player.setFireTicks(0);
        }
    }
    
    /**
     * Processes shadow steed passive abilities
     * 
     * @param player The player
     * @param mount The mount
     */
    private void processShadowSteedPassives(Player player, Mount mount) {
        // Night vision
        if (isAbilityUnlocked(player.getUniqueId(), mount, "night_vision")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName("NIGHT_VISION"), 400, 0, false, false, true));
        }
    }
    
    /**
     * Processes crystal drake passive abilities
     * 
     * @param player The player
     * @param mount The mount
     */
    private void processCrystalDrakePassives(Player player, Mount mount) {
        // Light body (slow falling effect)
        if (isAbilityUnlocked(player.getUniqueId(), mount, "light_body") && player.getFallDistance() > 3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName("SLOW_FALLING"), 100, 0, false, false, true));
        }
    }
    
    /**
     * Processes storm charger passive abilities
     * 
     * @param player The player
     * @param mount The mount
     */
    private void processStormChargerPassives(Player player, Mount mount) {
        // Static aura
        if (isAbilityUnlocked(player.getUniqueId(), mount, "static_aura")) {
            // Small chance to give speed boost
            if (Math.random() < 0.01) { // 1% chance per second
                player.addPotionEffect(new PotionEffect(PotionEffectType.getByName("SPEED"), 100, 0, false, false, true));
                
                // Play effect
                Location location = player.getLocation();
                player.getWorld().spawnParticle(
                    Particle.valueOf("ELECTRIC_SPARK"), 
                    location, 
                    20, 
                    0.5, 0.5, 0.5, 
                    0.1
                );
                player.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.2f);
            }
        }
    }
    
    /**
     * Processes ancient golem passive abilities
     * 
     * @param player The player
     * @param mount The mount
     */
    private void processAncientGolemPassives(Player player, Mount mount) {
        // Stone skin
        if (isAbilityUnlocked(player.getUniqueId(), mount, "stone_skin")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName("DAMAGE_RESISTANCE"), 40, 0, false, false, true));
        }
        
        // Mountain climber
        if (isAbilityUnlocked(player.getUniqueId(), mount, "mountain_climber")) {
            Block block = player.getLocation().getBlock();
            
            // Check if player is against a wall/steep slope
            if (isClimbableSurface(player)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.getByName("LEVITATION"), 20, 0, false, false, true));
            }
        }
    }
    
    /**
     * Checks if a player is against a climbable surface
     * 
     * @param player The player
     * @return true if against a climbable surface
     */
    private boolean isClimbableSurface(Player player) {
        Location loc = player.getLocation();
        
        // Check for blocks around the player
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // Skip player's block
                
                Block block = loc.getBlock().getRelative(x, 0, z);
                
                // If there's a solid block next to player and empty block above it
                if (block.getType().isSolid() && 
                        !block.getRelative(0, 1, 0).getType().isSolid() &&
                        !block.getRelative(0, 2, 0).getType().isSolid()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Process particle effects for a mount
     * 
     * @param mount The mount
     */
    public void processParticleEffects(Mount mount) {
        Entity entity = mount.getEntity();
        if (entity == null || entity.isDead()) return;
        
        MountType mountType = mount.getType();
        Location location = entity.getLocation().add(0, 0.5, 0);
        World world = entity.getWorld();
        
        // Process mount type specific particle effects
        switch (mountType.getId()) {
            case "phoenix_blaze" -> {
                // Fire particles
                world.spawnParticle(Particle.valueOf("FLAME"), location, 10, 0.3, 0.3, 0.3, 0.02);
                
                // 10% chance for larger effect
                if (Math.random() < 0.1) {
                    world.spawnParticle(Particle.valueOf("LAVA"), location, 3, 0.2, 0.2, 0.2, 0);
                    world.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 0.5f, 1.0f);
                }
            }
            case "shadow_steed" -> {
                // Shadow particles
                world.spawnParticle(Particle.valueOf("SMOKE_NORMAL"), location, 8, 0.3, 0.3, 0.3, 0.01);
                
                // 5% chance for larger effect
                if (Math.random() < 0.05) {
                    world.spawnParticle(Particle.valueOf("SMOKE_LARGE"), location, 5, 0.3, 0.3, 0.3, 0.01);
                    world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.2f, 0.8f);
                }
            }
            case "crystal_drake" -> {
                // Crystal particles
                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(30, 200, 255), 1.5f);
                world.spawnParticle(Particle.valueOf("REDSTONE"), location, 10, 0.3, 0.3, 0.3, dustOptions);
                
                // 15% chance for sparkle effect
                if (Math.random() < 0.15) {
                    world.spawnParticle(Particle.valueOf("SPELL_INSTANT"), location, 10, 0.4, 0.4, 0.4, 0.01);
                    world.playSound(location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.3f, 1.2f);
                }
            }
            case "storm_charger" -> {
                // Electric particles
                world.spawnParticle(Particle.valueOf("ELECTRIC_SPARK"), location, 5, 0.3, 0.3, 0.3, 0.05);
                
                // 5% chance for lightning effect
                if (Math.random() < 0.05) {
                    world.spawnParticle(Particle.valueOf("WAX_OFF"), location, 20, 0.3, 0.3, 0.3, 0.5);
                    world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.2f, 1.5f);
                }
            }
            case "ancient_golem" -> {
                // Stone particles
                world.spawnParticle(Particle.valueOf("FALLING_DUST"), location, 3, 0.3, 0.2, 0.3, 0.01);
                
                // Footstep sounds - 10% chance
                if (Math.random() < 0.1) {
                    world.playSound(location, Sound.BLOCK_STONE_STEP, 0.5f, 0.7f);
                }
            }
        }
    }
    
    /**
     * Processes abilities that affect rider damage
     * 
     * @param player The player
     * @param mount The mount
     * @param event The damage event
     */
    public void processRiderDamageAbilities(Player player, Mount mount, EntityDamageEvent event) {
        MountType mountType = mount.getType();
        
        // Process abilities by mount type
        switch (mountType.getId()) {
            case "phoenix_blaze" -> {
                // Fire immunity
                if (isAbilityUnlocked(player.getUniqueId(), mount, "fire_immunity") &&
                        (event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                         event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                         event.getCause() == EntityDamageEvent.DamageCause.LAVA ||
                         event.getCause() == EntityDamageEvent.DamageCause.HOT_FLOOR)) {
                    event.setCancelled(true);
                }
            }
            case "ancient_golem" -> {
                // Stone Shield - damage reduction
                if (isAbilityUnlocked(player.getUniqueId(), mount, "stone_skin")) {
                    double damage = event.getDamage();
                    double reduction = 0.25; // 25% damage reduction
                    event.setDamage(damage * (1 - reduction));
                }
            }
            case "crystal_drake" -> {
                // Crystal Shield - chance to reflect
                if (isAbilityUnlocked(player.getUniqueId(), mount, "crystal_shield") &&
                        Math.random() < 0.20) { // 20% chance
                    double damage = event.getDamage();
                    double reflection = 0.3; // 30% reflection
                    
                    // Reduce damage
                    event.setDamage(damage * 0.7);
                    
                    // Visual effect
                    player.getWorld().spawnParticle(
                        Particle.valueOf("SPELL_INSTANT"),
                        player.getLocation().add(0, 1, 0),
                        30, 0.5, 0.5, 0.5, 0.05
                    );
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);
                }
            }
            case "storm_charger" -> {
                // Storm Aura - chance to deflect projectiles
                if (isAbilityUnlocked(player.getUniqueId(), mount, "storm_aura") &&
                        event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE &&
                        Math.random() < 0.25) { // 25% chance
                    event.setCancelled(true);
                    
                    // Visual effect
                    player.getWorld().strikeLightningEffect(player.getLocation());
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.8f, 1.2f);
                }
            }
        }
    }
    
    /**
     * Activates a mount ability
     * 
     * @param player The player
     * @param abilityName The ability name
     * @return true if activation was successful
     */
    public boolean activateAbility(Player player, String abilityName) {
        UUID playerUUID = player.getUniqueId();
        
        // Check if player has an active mount
        Mount mount = mountManager.getActiveMount(playerUUID);
        if (mount == null) {
            player.sendMessage("§cYou must be on a mount to use mount abilities!");
            return false;
        }
        
        // Check if the mount has this ability
        MountType.MountAbility ability = mount.getType().getAbility(abilityName);
        if (ability == null || !ability.isEnabled()) {
            player.sendMessage("§cThis mount doesn't have that ability!");
            return false;
        }
        
        // Check if the ability is unlocked based on mount level
        if (!isAbilityUnlocked(playerUUID, mount, abilityName)) {
            player.sendMessage("§cYou need to level up your mount to use this ability!");
            return false;
        }
        
        // Check cooldown
        if (isOnCooldown(playerUUID, abilityName)) {
            long remainingCooldown = getCooldownTimeRemaining(playerUUID, abilityName);
            player.sendMessage("§cThis ability is on cooldown for " + remainingCooldown + " more seconds!");
            return false;
        }
        
        // Process ability based on mount type
        boolean success = switch (mount.getType().getId()) {
            case "phoenix_blaze" -> activatePhoenixAbility(player, mount, abilityName);
            case "shadow_steed" -> activateShadowSteedAbility(player, mount, abilityName);
            case "crystal_drake" -> activateCrystalDrakeAbility(player, mount, abilityName);
            case "storm_charger" -> activateStormChargerAbility(player, mount, abilityName);
            case "ancient_golem" -> activateAncientGolemAbility(player, mount, abilityName);
            default -> false;
        };
        
        // Apply cooldown if successful
        if (success) {
            applyCooldown(playerUUID, abilityName, ability.getCooldown());
        }
        
        return success;
    }
    
    /**
     * Activates a Phoenix mount ability
     * 
     * @param player The player
     * @param mount The mount
     * @param abilityName The ability name
     * @return true if activation was successful
     */
    private boolean activatePhoenixAbility(Player player, Mount mount, String abilityName) {
        World world = player.getWorld();
        Location location = player.getLocation();
        
        switch (abilityName) {
            case "flame_trail" -> {
                // Create a trail of fire particles
                spawnTrailingEffect(player, Particle.valueOf("FLAME"), 30, 5.0);
                world.playSound(location, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
                return true;
            }
            case "thermal_updraft" -> {
                // Double jump with fire effects
                player.setVelocity(player.getVelocity().add(player.getLocation().getDirection().multiply(1.5).setY(1.0)));
                world.spawnParticle(Particle.valueOf("LAVA"), location, 20, 0.5, 0.5, 0.5, 0.1);
                world.playSound(location, Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
                return true;
            }
            default -> {
                return false;
            }
        }
    }
    
    /**
     * Activates a Shadow Steed mount ability
     * 
     * @param player The player
     * @param mount The mount
     * @param abilityName The ability name
     * @return true if activation was successful
     */
    private boolean activateShadowSteedAbility(Player player, Mount mount, String abilityName) {
        World world = player.getWorld();
        Location location = player.getLocation();
        
        switch (abilityName) {
            case "shadow_dash" -> {
                // Short-range teleport
                Location target = player.getLocation().add(player.getLocation().getDirection().multiply(10));
                player.teleport(target);
                world.spawnParticle(Particle.valueOf("SMOKE_LARGE"), location, 30, 0.5, 0.5, 0.5, 0.1);
                world.spawnParticle(Particle.valueOf("SMOKE_LARGE"), target, 30, 0.5, 0.5, 0.5, 0.1);
                world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                return true;
            }
            case "shadow_cloak" -> {
                // Temporary invisibility
                player.addPotionEffect(new PotionEffect(PotionEffectType.getByName("INVISIBILITY"), 15 * 20, 0, false, false, true));
                world.spawnParticle(Particle.valueOf("SMOKE_LARGE"), location, 50, 1.0, 1.0, 1.0, 0.1);
                world.playSound(location, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.5f);
                return true;
            }
            default -> {
                return false;
            }
        }
    }
    
    /**
     * Activates a Crystal Drake mount ability
     * 
     * @param player The player
     * @param mount The mount
     * @param abilityName The ability name
     * @return true if activation was successful
     */
    private boolean activateCrystalDrakeAbility(Player player, Mount mount, String abilityName) {
        World world = player.getWorld();
        Location location = player.getLocation();
        
        switch (abilityName) {
            case "crystal_teleport" -> {
                // Teleport to visible location within 50 blocks
                Block target = player.getTargetBlock(null, 50);
                if (target != null && !target.getType().isAir()) {
                    Location targetLoc = target.getLocation().add(0.5, 1.0, 0.5);
                    targetLoc.setYaw(player.getLocation().getYaw());
                    targetLoc.setPitch(player.getLocation().getPitch());
                    
                    player.teleport(targetLoc);
                    world.spawnParticle(Particle.valueOf("END_ROD"), location, 30, 0.5, 0.5, 0.5, 0.1);
                    world.spawnParticle(Particle.valueOf("END_ROD"), targetLoc, 30, 0.5, 0.5, 0.5, 0.1);
                    world.playSound(location, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.0f);
                    world.playSound(targetLoc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.0f);
                    return true;
                }
                
                player.sendMessage("§cNo valid teleport target found!");
                return false;
            }
            case "prismatic_beam" -> {
                // Beam attack in front of player
                location = player.getEyeLocation();
                for (int i = 1; i <= 20; i++) {
                    Location beamLoc = location.clone().add(location.getDirection().multiply(i));
                    
                    // Rainbow effect
                    float hue = i / 20.0f;
                    Color color = Color.fromRGB(
                        java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF
                    );
                    
                    Particle.DustOptions dustOptions = new Particle.DustOptions(color, 2.0f);
                    world.spawnParticle(Particle.valueOf("REDSTONE"), beamLoc, 5, 0.1, 0.1, 0.1, dustOptions);
                    
                    // Damage entities in beam
                    beamLoc.getWorld().getNearbyEntities(beamLoc, 1, 1, 1).forEach(entity -> {
                        if (entity != player && entity != mount.getEntity() && entity instanceof org.bukkit.entity.LivingEntity livingEntity) {
                            livingEntity.damage(5, player);
                        }
                    });
                }
                
                world.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 2.0f);
                return true;
            }
            default -> {
                return false;
            }
        }
    }
    
    /**
     * Activates a Storm Charger mount ability
     * 
     * @param player The player
     * @param mount The mount
     * @param abilityName The ability name
     * @return true if activation was successful
     */
    private boolean activateStormChargerAbility(Player player, Mount mount, String abilityName) {
        World world = player.getWorld();
        Location location = player.getLocation();
        
        switch (abilityName) {
            case "lightning_strike" -> {
                // Strike lightning at target
                Block target = player.getTargetBlock(null, 30);
                if (target != null && !target.getType().isAir()) {
                    Location targetLoc = target.getLocation();
                    world.strikeLightningEffect(targetLoc);
                    
                    // Damage nearby entities
                    targetLoc.getWorld().getNearbyEntities(targetLoc, 3, 3, 3).forEach(entity -> {
                        if (entity != player && entity != mount.getEntity() && entity instanceof org.bukkit.entity.LivingEntity livingEntity) {
                            livingEntity.damage(8, player);
                        }
                    });
                    
                    return true;
                }
                
                player.sendMessage("§cNo valid target found!");
                return false;
            }
            case "lightning_dash" -> {
                // Extreme speed boost
                player.addPotionEffect(new PotionEffect(PotionEffectType.getByName("SPEED"), 5 * 20, 2, false, false, true));
                
                // Trailing lightning effect
                spawnTrailingEffect(player, Particle.valueOf("ELECTRIC_SPARK"), 20, 3.0);
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                return true;
            }
            case "thunder_step" -> {
                // Small AOE damage around mount when sprinting
                world.spawnParticle(Particle.valueOf("ELECTRIC_SPARK"), location, 50, 3.0, 0.5, 3.0, 0.1);
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.0f);
                
                // Damage and stun nearby entities
                location.getWorld().getNearbyEntities(location, 5, 2, 5).forEach(entity -> {
                    if (entity != player && entity != mount.getEntity() && entity instanceof org.bukkit.entity.LivingEntity livingEntity) {
                        livingEntity.damage(5, player);
                        if (livingEntity instanceof Player targetPlayer) {
                            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.getByName("SLOW"), 3 * 20, 1, false, false, true));
                        }
                    }
                });
                
                return true;
            }
            default -> {
                return false;
            }
        }
    }
    
    /**
     * Activates an Ancient Golem mount ability
     * 
     * @param player The player
     * @param mount The mount
     * @param abilityName The ability name
     * @return true if activation was successful
     */
    private boolean activateAncientGolemAbility(Player player, Mount mount, String abilityName) {
        World world = player.getWorld();
        Location location = player.getLocation();
        
        switch (abilityName) {
            case "ground_pound" -> {
                // AOE stun/damage effect
                world.spawnParticle(Particle.valueOf("EXPLOSION_LARGE"), location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.valueOf("BLOCK_CRACK"), location, 100, 3, 0.5, 3, 0.1, location.getBlock().getBlockData());
                world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
                
                // Damage and stun nearby entities
                location.getWorld().getNearbyEntities(location, 5, 2, 5).forEach(entity -> {
                    if (entity != player && entity != mount.getEntity() && entity instanceof org.bukkit.entity.LivingEntity livingEntity) {
                        livingEntity.damage(8, player);
                        if (livingEntity instanceof Player targetPlayer) {
                            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.getByName("SLOW"), 5 * 20, 2, false, false, true));
                            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.getByName("CONFUSION"), 5 * 20, 0, false, false, true));
                        }
                    }
                });
                
                return true;
            }
            case "mountain_leap" -> {
                // Extremely high jump with slow fall
                player.setVelocity(player.getVelocity().add(new org.bukkit.util.Vector(0, 2.0, 0)));
                player.addPotionEffect(new PotionEffect(PotionEffectType.getByName("SLOW_FALLING"), 7 * 20, 0, false, false, true));
                world.spawnParticle(Particle.valueOf("BLOCK_CRACK"), location, 50, 0.5, 0.1, 0.5, 0.1, location.getBlock().getBlockData());
                world.playSound(location, Sound.BLOCK_STONE_BREAK, 1.0f, 0.5f);
                return true;
            }
            default -> {
                return false;
            }
        }
    }
    
    /**
     * Spawns a trailing effect that follows the player's movement
     * 
     * @param player The player
     * @param particle The particle type
     * @param count Particle count
     * @param duration Duration in seconds
     */
    private void spawnTrailingEffect(Player player, Particle particle, int count, double duration) {
        UUID playerUUID = player.getUniqueId();
        
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!player.isOnline() || mountManager.getActiveMount(playerUUID) == null) {
                task.cancel();
                return;
            }
            
            Location location = player.getLocation();
            player.getWorld().spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0.05);
            
            // Update duration counter
            if (task.getTaskId() > duration * 20) {
                task.cancel();
            }
        }, 0L, 5L); // Every 1/4 second
    }
    
    /**
     * Checks if an ability is unlocked based on mount level
     * 
     * @param playerUUID The player's UUID
     * @param mount The mount
     * @param abilityName The ability name
     * @return true if the ability is unlocked
     */
    public boolean isAbilityUnlocked(UUID playerUUID, Mount mount, String abilityName) {
        // Get ability
        MountType.MountAbility ability = mount.getType().getAbility(abilityName);
        if (ability == null) return false;
        
        // Get mount level
        MountXPManager xpManager = mountManager.getXPManager();
        int mountLevel = xpManager.getMountLevel(playerUUID, mount.getType().getId());
        
        // Check if level meets requirement
        return mountLevel >= ability.getMinLevel();
    }
    
    /**
     * Applies a cooldown to an ability
     * 
     * @param playerUUID The player's UUID
     * @param abilityName The ability name
     * @param cooldownSeconds The cooldown duration in seconds
     */
    private void applyCooldown(UUID playerUUID, String abilityName, int cooldownSeconds) {
        // If no cooldown map for this player, create one
        Map<String, Long> playerCooldowns = abilityCooldowns.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
        
        // Calculate cooldown end time and store it
        long cooldownEnd = System.currentTimeMillis() + (cooldownSeconds * 1000L);
        playerCooldowns.put(abilityName, cooldownEnd);
    }
    
    /**
     * Checks if an ability is on cooldown
     * 
     * @param playerUUID The player's UUID
     * @param abilityName The ability name
     * @return true if on cooldown
     */
    private boolean isOnCooldown(UUID playerUUID, String abilityName) {
        Map<String, Long> playerCooldowns = abilityCooldowns.get(playerUUID);
        if (playerCooldowns == null) return false;
        
        Long cooldownEnd = playerCooldowns.get(abilityName);
        return cooldownEnd != null && cooldownEnd > System.currentTimeMillis();
    }
    
    /**
     * Gets the remaining cooldown time for an ability
     * 
     * @param playerUUID The player's UUID
     * @param abilityName The ability name
     * @return Seconds remaining or 0 if not on cooldown
     */
    private long getCooldownTimeRemaining(UUID playerUUID, String abilityName) {
        Map<String, Long> playerCooldowns = abilityCooldowns.get(playerUUID);
        if (playerCooldowns == null) return 0;
        
        Long cooldownEnd = playerCooldowns.get(abilityName);
        if (cooldownEnd == null || cooldownEnd <= System.currentTimeMillis()) {
            return 0;
        }
        
        return (cooldownEnd - System.currentTimeMillis()) / 1000;
    }
    
    /**
     * Event handler for player movement
     * 
     * @param event The player move event
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Mount mount = mountManager.getActiveMount(player.getUniqueId());
        
        // Process movement-based effects if player is mounted
        if (mount != null && mount.isValid()) {
            // Process flame trail for Phoenix mount
            if (mount.getType().getId().equals("phoenix_blaze") && 
                    isAbilityUnlocked(player.getUniqueId(), mount, "flame_trail")) {
                
                // Only process if moving significantly
                Location from = event.getFrom();
                Location to = event.getTo();
                if (to != null && from.distance(to) > 0.2) {
                    player.getWorld().spawnParticle(Particle.valueOf("FLAME"), player.getLocation(), 5, 0.2, 0, 0.2, 0.01);
                }
            }
        }
    }
    
    /**
     * Event handler for entity damage
     * 
     * @param event The entity damage event
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // If the damaged entity is a player
        if (event.getEntity() instanceof Player player) {
            // Get player's mount
            Mount mount = mountManager.getActiveMount(player.getUniqueId());
            
            // Process damage-related abilities
            if (mount != null && mount.isValid()) {
                processRiderDamageAbilities(player, mount, event);
            }
        }
    }

    /**
     * Executes the secondary action on a mount (for example, shooting a projectile)
     * 
     * @param player The player triggering the action
     * @param mount The mount to execute the action on
     * @return true if the action was executed successfully
     */
    public boolean executeSecondaryAction(Player player, Mount mount) {
        MountProjectileAbility.ProjectileType bestProjectile = projectileAbility.getBestProjectileForMount(mount);
        return projectileAbility.launchProjectile(player, mount, bestProjectile);
    }

    /**
     * Gets the projectile ability handler
     * 
     * @return The projectile ability handler
     */
    public MountProjectileAbility getProjectileAbility() {
        return projectileAbility;
    }
} 