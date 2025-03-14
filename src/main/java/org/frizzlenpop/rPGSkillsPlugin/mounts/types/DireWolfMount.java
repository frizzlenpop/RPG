package org.frizzlenpop.rPGSkillsPlugin.mounts.types;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
 * A specialized wolf mount with pack abilities
 */
public class DireWolfMount extends ExtendedMount {
    private static final double BASE_SPEED = 0.45;
    private static final double MAX_LEAP_DISTANCE = 15.0;
    private static final int PACK_CALL_RANGE = 30;
    private static final int PACK_CALL_DURATION = 200; // 10 seconds
    
    private final Map<UUID, Long> abilityCooldowns = new HashMap<>();
    private static final long ABILITY_COOLDOWN_MS = 20000; // 20 seconds
    
    // Track summoned pack members
    private final List<Wolf> packMembers = new ArrayList<>();
    
    /**
     * Creates a new dire wolf mount
     * 
     * @param plugin The plugin instance
     * @param owner The owner of the mount
     * @param rarity The rarity of the mount
     */
    public DireWolfMount(RPGSkillsPlugin plugin, UUID owner, MountRarity rarity) {
        super(plugin, "dire_wolf", owner, EntityType.WOLF, rarity);
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
        ItemStack item = new ItemStack(Material.BONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtils.color("&8&lDire Wolf Mount"));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtils.color("&7A fearsome wolf that strikes"));
        lore.add(ColorUtils.color("&7terror into your enemies."));
        lore.add(ColorUtils.color("&7"));
        lore.add(ColorUtils.color("&7Rarity: " + getRarity().getDisplayName()));
        lore.add(ColorUtils.color("&7"));
        lore.add(ColorUtils.color("&7Speed: " + String.format("%.1f", getSpeed())));
        lore.add(ColorUtils.color("&7"));
        lore.add(ColorUtils.color("&e&lAbility: &7Call of the Wild"));
        lore.add(ColorUtils.color("&7Summons allied wolves to fight"));
        lore.add(ColorUtils.color("&7alongside you for a short time."));
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
        if (entity instanceof Wolf wolf) {
            // Set wolf properties
            wolf.setCustomName(ColorUtils.color("&8&l" + getName() + " &7(Owned by " + 
                Bukkit.getOfflinePlayer(getOwner()).getName() + ")"));
            wolf.setCustomNameVisible(true);
            wolf.setAdult();
            wolf.setAngry(false);
            wolf.setSitting(false);
            wolf.setTamed(true);
            wolf.setOwner(Bukkit.getPlayer(getOwner()));
            
            // Apply visual effects
            applyWolfEffects(wolf);
        }
    }
    
    @Override
    public void onDismount(Player player) {
        super.onDismount(player);
        
        // Clean up any summoned pack members
        dismissPackMembers();
    }
    
    @Override
    public boolean onToggleAbility(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Check if mount is spawned and player is riding it
        if (!isSpawned() || !player.equals(getCurrentRider())) {
            return false;
        }
        
        // Check if on cooldown
        Long cooldownTime = abilityCooldowns.get(playerId);
        if (cooldownTime != null && System.currentTimeMillis() - cooldownTime < ABILITY_COOLDOWN_MS) {
            long remainingSeconds = (ABILITY_COOLDOWN_MS - (System.currentTimeMillis() - cooldownTime)) / 1000;
            player.sendMessage(ColorUtils.color("&c&lCall of the Wild &7on cooldown for &c" + remainingSeconds + " &7seconds!"));
            return false;
        }
        
        // Perform pack call ability
        Entity mount = getEntityReference().get();
        if (mount instanceof Wolf mainWolf) {
            // Play wolf howl sound effect
            mainWolf.getWorld().playSound(mainWolf.getLocation(), Sound.ENTITY_WOLF_HOWL, 1.5f, 0.7f);
            
            // Create particle effect around the main wolf
            mainWolf.getWorld().spawnParticle(
                Particle.valueOf("SMOKE_NORMAL"),
                mainWolf.getLocation().add(0, 1, 0),
                30,
                0.5, 0.5, 0.5,
                0.02
            );
            
            // Dismiss old pack members if any
            dismissPackMembers();
            
            // Number of wolves to summon based on rarity
            int wolfCount = switch (getRarity()) {
                case COMMON -> 1;
                case UNCOMMON -> 2;
                case RARE -> 3;
                case EPIC -> 4;
                case LEGENDARY -> 5;
                case MYTHIC -> 6;
            };
            
            // Summon pack members
            summonPackMembers(mainWolf, player, wolfCount);
            
            // Add buffs to the rider
            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName("DAMAGE_RESISTANCE"), PACK_CALL_DURATION, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName("INCREASE_DAMAGE"), PACK_CALL_DURATION, 0));
            
            // Set cooldown
            abilityCooldowns.put(playerId, System.currentTimeMillis());
            
            player.sendMessage(ColorUtils.color("&a&lCall of the Wild! &7Pack members will fight for you!"));
            
            // Schedule pack dismissal
            Bukkit.getScheduler().runTaskLater(getPlugin(), this::dismissPackMembers, PACK_CALL_DURATION);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Summons pack members around the main wolf
     * 
     * @param mainWolf The main wolf (mount)
     * @param player The player riding the wolf
     * @param count Number of wolves to summon
     */
    private void summonPackMembers(Wolf mainWolf, Player player, int count) {
        for (int i = 0; i < count; i++) {
            // Calculate spawn position - spread around in a circle
            double angle = (2 * Math.PI / count) * i;
            double x = 2.5 * Math.cos(angle);
            double z = 2.5 * Math.sin(angle);
            
            // Spawn the wolf
            Wolf packWolf = (Wolf) mainWolf.getWorld().spawnEntity(
                mainWolf.getLocation().add(x, 0, z),
                EntityType.WOLF
            );
            
            // Set pack wolf properties
            packWolf.setCustomName(ColorUtils.color("&8Pack Wolf"));
            packWolf.setCustomNameVisible(true);
            packWolf.setAdult();
            packWolf.setAngry(false);
            packWolf.setSitting(false);
            packWolf.setTamed(true);
            packWolf.setOwner(player);
            
            // Visual effects
            packWolf.getWorld().spawnParticle(
                Particle.valueOf("SMOKE_NORMAL"),
                packWolf.getLocation().add(0, 0.5, 0),
                15,
                0.3, 0.3, 0.3,
                0.02
            );
            
            // Apply some buffs to the pack wolves based on rarity
            int strengthLevel = Math.min(2, (getRarity().ordinal() / 2));
            int speedLevel = Math.min(1, getRarity().ordinal() / 3);
            
            packWolf.addPotionEffect(new PotionEffect(PotionEffectType.getByName("INCREASE_DAMAGE"), PACK_CALL_DURATION, strengthLevel));
            packWolf.addPotionEffect(new PotionEffect(PotionEffectType.getByName("SPEED"), PACK_CALL_DURATION, speedLevel));
            packWolf.addPotionEffect(new PotionEffect(PotionEffectType.getByName("GLOWING"), PACK_CALL_DURATION, 0));
            
            // Store for later cleanup
            packMembers.add(packWolf);
        }
    }
    
    /**
     * Dismisses all summoned pack members
     */
    private void dismissPackMembers() {
        for (Wolf wolf : packMembers) {
            if (wolf != null && !wolf.isDead()) {
                // Particle effect before removal
                wolf.getWorld().spawnParticle(
                    Particle.valueOf("SMOKE_NORMAL"),
                    wolf.getLocation().add(0, 0.5, 0),
                    10,
                    0.3, 0.3, 0.3,
                    0.02
                );
                
                wolf.remove();
            }
        }
        
        packMembers.clear();
    }
    
    /**
     * Applies visual effects to the main wolf mount
     * 
     * @param wolf The wolf entity
     */
    private void applyWolfEffects(Wolf wolf) {
        // Make the wolf larger based on rarity
        float size = 1.0f + (getRarity().ordinal() * 0.1f);
        // Note: In a real plugin, you'd need to use entity metadata to change its size
        
        // Give the wolf some particle effects
        Bukkit.getScheduler().runTaskTimer(getPlugin(), task -> {
            if (!isSpawned() || wolf.isDead()) {
                task.cancel();
                return;
            }
            
            // Only show particles occasionally
            if (wolf.getTicksLived() % 20 != 0) return;
            
            // Different effects based on rarity
            switch (getRarity()) {
                case COMMON, UNCOMMON -> {
                    // Simple smoke effect
                    wolf.getWorld().spawnParticle(
                        Particle.valueOf("SMOKE_NORMAL"),
                        wolf.getLocation().add(0, 0.7, 0),
                        3,
                        0.2, 0.1, 0.2,
                        0.01
                    );
                }
                case RARE, EPIC -> {
                    // Flame footprints
                    wolf.getWorld().spawnParticle(
                        Particle.valueOf("FLAME"),
                        wolf.getLocation().add(0, 0.1, 0),
                        2,
                        0.1, 0.0, 0.1,
                        0.01
                    );
                }
                case LEGENDARY, MYTHIC -> {
                    // Epic particles
                    wolf.getWorld().spawnParticle(
                        Particle.valueOf("DRAGON_BREATH"),
                        wolf.getLocation().add(0, 0.7, 0),
                        3,
                        0.2, 0.1, 0.2,
                        0.01
                    );
                    
                    // Eye effects
                    Vector dir = wolf.getLocation().getDirection().normalize();
                    Location leftEye = wolf.getLocation().add(0, 0.7, 0)
                        .add(new Vector(-dir.getZ(), 0, dir.getX()).normalize().multiply(0.2));
                    Location rightEye = wolf.getLocation().add(0, 0.7, 0)
                        .add(new Vector(dir.getZ(), 0, -dir.getX()).normalize().multiply(0.2));
                    
                    wolf.getWorld().spawnParticle(
                        Particle.valueOf("REDSTONE"),
                        leftEye,
                        1,
                        0.01, 0.01, 0.01,
                        new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 0.8f)
                    );
                    
                    wolf.getWorld().spawnParticle(
                        Particle.valueOf("REDSTONE"),
                        rightEye,
                        1,
                        0.01, 0.01, 0.01,
                        new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 0.8f)
                    );
                }
            }
        }, 0L, 1L);
    }
} 