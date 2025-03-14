package org.frizzlenpop.rPGSkillsPlugin.mounts;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.mounts.effects.ProtocolLibEffectManager;

import java.util.*;
import java.io.File;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Manages visual effects and customizations for mounts.
 */
public class MountVisualManager {
    private final RPGSkillsPlugin plugin;
    private final Map<UUID, BukkitTask> effectTasks = new HashMap<>();
    private final Map<UUID, List<Entity>> mountDecorations = new HashMap<>();
    private ProtocolLibEffectManager protocolLibEffectManager;
    private final Map<UUID, BukkitTask> activeEffects = new HashMap<>();
    private final Map<String, CustomEffect> customEffects = new HashMap<>();
    
    public MountVisualManager(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        loadCustomEffects();
    }
    
    /**
     * Sets the ProtocolLib effect manager for enhanced visuals
     * 
     * @param protocolLibEffectManager The ProtocolLib effect manager
     */
    public void setProtocolLibEffectManager(ProtocolLibEffectManager protocolLibEffectManager) {
        this.protocolLibEffectManager = protocolLibEffectManager;
    }
    
    /**
     * Load custom effect definitions from config
     */
    private void loadCustomEffects() {
        // Load from mounts.yml
        File configFile = new File(plugin.getDataFolder(), "mounts.yml");
        if (!configFile.exists()) {
            plugin.saveResource("mounts.yml", false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection visualsSection = config.getConfigurationSection("visuals.custom_effects");
        
        if (visualsSection != null) {
            for (String effectName : visualsSection.getKeys(false)) {
                ConfigurationSection effectSection = visualsSection.getConfigurationSection(effectName);
                
                if (effectSection != null) {
                    List<String> particleNames = effectSection.getStringList("particles");
                    double radius = effectSection.getDouble("radius", 1.0);
                    double height = effectSection.getDouble("height", 1.0);
                    int frequency = effectSection.getInt("frequency", 10);
                    
                    CustomEffect effect = new CustomEffect(
                            effectName,
                            particleNames.toArray(new String[0]),
                            radius,
                            height,
                            frequency
                    );
                    
                    customEffects.put(effectName, effect);
                    plugin.getLogger().info("Loaded custom effect: " + effectName);
                }
            }
        }
    }
    
    /**
     * Class representing a custom particle effect
     */
    public static class CustomEffect {
        private final String name;
        private final String[] particleNames;
        private final double radius;
        private final double height;
        private final int frequency;
        
        public CustomEffect(String name, String[] particleNames, double radius, double height, int frequency) {
            this.name = name;
            this.particleNames = particleNames;
            this.radius = radius;
            this.height = height;
            this.frequency = frequency;
        }
        
        public String getName() {
            return name;
        }
        
        public String[] getParticleNames() {
            return particleNames;
        }
        
        public double getRadius() {
            return radius;
        }
        
        public double getHeight() {
            return height;
        }
        
        public int getFrequency() {
            return frequency;
        }
    }
    
    /**
     * Apply custom mount visuals based on configuration
     */
    public void enhanceMount(Mount mount) {
        if (mount == null || !mount.isValid()) {
            return;
        }
        
        try {
            Entity entity = mount.getEntity();
            if (entity == null) return;
            
            UUID entityUuid = entity.getUniqueId();
            
            // Cancel any existing effects for this entity
            if (activeEffects.containsKey(entityUuid)) {
                activeEffects.get(entityUuid).cancel();
                activeEffects.remove(entityUuid);
            }
            
            // Get mount type from config
            MountType mountType = mount.getType();
            
            // Apply basic customization
            applyBasicCustomization(entity, mountType);
            
            // Apply visual effects if enabled
            if (plugin.getConfig().getBoolean("settings.enable-particles", true)) {
                // Check if this mount type has custom visuals
                ConfigurationSection visualSection = getVisualSection(mountType.getId());
                
                if (visualSection != null) {
                    String idleEffect = visualSection.getString("idle-effect");
                    String mainParticle = visualSection.getString("main-particle");
                    boolean trailEffect = visualSection.getBoolean("trail-effect", false);
                    
                    // Apply idle effect if configured
                    if (idleEffect != null && customEffects.containsKey(idleEffect)) {
                        applyCustomEffect(entity, customEffects.get(idleEffect));
                    } 
                    // Otherwise use main particle if configured
                    else if (mainParticle != null) {
                        applyBasicParticleEffect(entity, mainParticle, trailEffect);
                    }
                    
                    // Apply enhanced visuals if ProtocolLib is available
                    if (protocolLibEffectManager != null) {
                        try {
                            // Use reflection to safely call the method if it exists
                            protocolLibEffectManager.getClass().getMethod("enhanceMount", Mount.class)
                                .invoke(protocolLibEffectManager, mount);
                        } catch (Exception e) {
                            // Method doesn't exist or there was an error
                            plugin.getLogger().warning("Failed to call enhanceMount on ProtocolLibEffectManager: " + e.getMessage());
                        }
                    }
                } else {
                    // Apply default effects based on entity type
                    applyDefaultEffects(entity, mountType);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error enhancing mount: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get the visual section for this mount type
     */
    private ConfigurationSection getVisualSection(String mountTypeId) {
        File configFile = new File(plugin.getDataFolder(), "mounts.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        return config.getConfigurationSection("mounts." + mountTypeId + ".visuals");
    }
    
    /**
     * Apply a custom effect to an entity
     */
    private void applyCustomEffect(Entity entity, CustomEffect effect) {
        UUID entityUuid = entity.getUniqueId();
        
        BukkitTask task = new BukkitRunnable() {
            private int tick = 0;
            private final Random random = new Random();
            
            @Override
            public void run() {
                if (entity == null || !entity.isValid()) {
                    cancel();
                    activeEffects.remove(entityUuid);
                    return;
                }
                
                Location location = entity.getLocation();
                
                // Only spawn particles on certain ticks based on frequency
                if (tick % Math.max(1, (20 / effect.getFrequency())) == 0) {
                    for (String particleName : effect.getParticleNames()) {
                        try {
                            // Try to spawn the particle
                            Particle particle = getParticleByName(particleName);
                            
                            // Create effect based on pattern
                            if (effect.getName().contains("pulse")) {
                                createPulseEffect(location, particle, effect.getRadius(), effect.getHeight());
                            } else if (effect.getName().contains("aura")) {
                                createAuraEffect(location, particle, effect.getRadius(), effect.getHeight());
                            } else if (effect.getName().contains("explosion")) {
                                // Only show explosion effects occasionally
                                if (random.nextDouble() < 0.1) { // 10% chance
                                    createExplosionEffect(location, particle, effect.getRadius(), effect.getHeight());
                                }
                            } else {
                                // Default effect
                                location.getWorld().spawnParticle(
                                    particle,
                                    location.clone().add(0, 1.0, 0),
                                    10,
                                    effect.getRadius() * 0.5,
                                    effect.getHeight() * 0.3,
                                    effect.getRadius() * 0.5,
                                    0.01
                                );
                            }
                        } catch (Exception e) {
                            // Log once and continue
                            if (tick == 0) {
                                plugin.getLogger().log(Level.WARNING, 
                                        "Could not spawn particle effect " + particleName + ": " + e.getMessage());
                            }
                        }
                    }
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        activeEffects.put(entityUuid, task);
    }
    
    /**
     * Create a pulse effect (expanding ring)
     */
    private void createPulseEffect(Location location, Particle particle, double radius, double height) {
        Location center = location.clone().add(0, 0.5, 0);
        World world = location.getWorld();
        
        // Create a ring of particles that expands
        int numPoints = 12;
        double expandSpeed = 0.1;
        
        for (int i = 0; i < 5; i++) { // 5 rings with different sizes
            double currentRadius = (radius * 0.2) + (i * expandSpeed);
            
            // Schedule each ring to appear with delay
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (int j = 0; j < numPoints; j++) {
                    double angle = (Math.PI * 2) * ((double) j / numPoints);
                    double x = Math.cos(angle) * currentRadius;
                    double z = Math.sin(angle) * currentRadius;
                    
                    world.spawnParticle(
                        particle,
                        center.clone().add(x, 0, z),
                        1, 0, 0, 0, 0
                    );
                }
            }, i * 2L); // 2 tick delay between rings
        }
    }
    
    /**
     * Create an aura effect (particles around entity)
     */
    private void createAuraEffect(Location location, Particle particle, double radius, double height) {
        Location center = location.clone().add(0, 0.5, 0);
        Random random = new Random();
        
        // Spawn random particles around the entity
        for (int i = 0; i < 5; i++) {
            double x = (random.nextDouble() * 2 - 1) * radius;
            double y = (random.nextDouble() * height);
            double z = (random.nextDouble() * 2 - 1) * radius;
            
            location.getWorld().spawnParticle(
                particle,
                center.clone().add(x, y, z),
                1, 0, 0, 0, 0
            );
        }
    }
    
    /**
     * Create an explosion effect (burst of particles)
     */
    private void createExplosionEffect(Location location, Particle particle, double radius, double height) {
        location.getWorld().spawnParticle(
            particle,
            location.clone().add(0, 1.0, 0),
            25,
            radius * 0.6,
            height * 0.4,
            radius * 0.6,
            0.1
        );
        
        // Play an appropriate sound based on the effect type
        if (particle.name().contains("FLAME") || particle.name().contains("LAVA")) {
            location.getWorld().playSound(location, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f);
        } else if (particle.name().contains("SMOKE")) {
            location.getWorld().playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 1.2f);
        } else if (particle.name().contains("PORTAL") || particle.name().contains("SPELL") || particle.name().contains("ENCHANT")) {
            location.getWorld().playSound(location, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.8f, 1.2f);
        } else if (particle.name().contains("END_ROD") || particle.name().contains("FIREWORK")) {
            location.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.8f, 1.0f);
        }
    }
    
    /**
     * Apply simple particle effect to an entity
     */
    private void applyBasicParticleEffect(Entity entity, String particleName, boolean trailEffect) {
        UUID entityUuid = entity.getUniqueId();
        
        BukkitTask task = new BukkitRunnable() {
            private int tick = 0;
            private final List<Location> trailLocations = new ArrayList<>();
            private static final int MAX_TRAIL_POINTS = 10;
            
            @Override
            public void run() {
                if (entity == null || !entity.isValid()) {
                    cancel();
                    activeEffects.remove(entityUuid);
                    return;
                }
                
                Location location = entity.getLocation();
                
                // Try to get the particle
                try {
                    Particle particle = getParticleByName(particleName);
                    
                    // Main effect around the entity
                    location.getWorld().spawnParticle(
                        particle,
                        location.clone().add(0, 1.0, 0),
                        5,
                        0.4, 0.3, 0.4,
                        0.01
                    );
                    
                    // Handle trail effect if enabled
                    if (trailEffect && tick % 5 == 0) { // Every 5 ticks
                        // Add current location to trail
                        if (entity.getVelocity().lengthSquared() > 0.01) { // Only when moving
                            trailLocations.add(location.clone());
                            
                            // Keep trail at a reasonable length
                            while (trailLocations.size() > MAX_TRAIL_POINTS) {
                                trailLocations.remove(0);
                            }
                            
                            // Spawn particles along the trail
                            for (Location trailLoc : trailLocations) {
                                location.getWorld().spawnParticle(
                                    particle,
                                    trailLoc.clone().add(0, 0.3, 0),
                                    2,
                                    0.1, 0.1, 0.1,
                                    0.01
                                );
                            }
                        }
                    }
                } catch (Exception e) {
                    // Log once and continue
                    if (tick == 0) {
                        plugin.getLogger().warning("Could not spawn particle " + particleName + ": " + e.getMessage());
                    }
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L); // Run every 2 ticks
        
        activeEffects.put(entityUuid, task);
    }
    
    /**
     * Apply default visual effects based on entity type
     */
    private void applyDefaultEffects(Entity entity, MountType mountType) {
        // Based on entity type, apply default effects
        if (entity instanceof Horse) {
            // Default horse effects
            applyBasicParticleEffect(entity, "CLOUD", true);
        } else if (entity instanceof Wolf) {
            // Default wolf effects
            applyBasicParticleEffect(entity, "PORTAL", false);
        } else if (entity instanceof Pig) {
            // Default pig effects
            applyBasicParticleEffect(entity, "SPELL_WITCH", true);
        } else {
            // Generic effects
            applyBasicParticleEffect(entity, "CLOUD", false);
        }
    }
    
    /**
     * Apply basic entity customization from config
     */
    private void applyBasicCustomization(Entity entity, MountType mountType) {
        ConfigurationSection customization = getCustomizationSection(mountType.getId());
        
        if (customization == null) return;
        
        if (entity instanceof Horse horse) {
            // Apply horse-specific customization
            if (customization.contains("horse-color")) {
                try {
                    String colorName = customization.getString("horse-color");
                    if (colorName != null) {
                        Horse.Color color = Horse.Color.valueOf(colorName);
                        horse.setColor(color);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid horse color: " + e.getMessage());
                }
            }
            
            if (customization.contains("horse-style")) {
                try {
                    String styleName = customization.getString("horse-style");
                    if (styleName != null) {
                        Horse.Style style = Horse.Style.valueOf(styleName);
                        horse.setStyle(style);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid horse style: " + e.getMessage());
                }
            }
        } else if (entity instanceof Wolf wolf) {
            // Apply wolf-specific customization
            if (customization.contains("wolf-angry")) {
                wolf.setAngry(customization.getBoolean("wolf-angry", false));
            }
            
            if (customization.contains("wolf-collar-color")) {
                try {
                    String colorName = customization.getString("wolf-collar-color");
                    if (colorName != null) {
                        // Try to parse org.bukkit.Color constants
                        org.bukkit.DyeColor dyeColor = org.bukkit.DyeColor.valueOf(colorName);
                        wolf.setCollarColor(dyeColor);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid wolf collar color: " + e.getMessage());
                }
            }
        } else if (entity instanceof Pig pig) {
            // Apply pig-specific customization
            if (customization.contains("pig-saddle")) {
                pig.setSaddle(customization.getBoolean("pig-saddle", true));
            }
        }
    }
    
    /**
     * Get customization section for mount type
     */
    private ConfigurationSection getCustomizationSection(String mountTypeId) {
        File configFile = new File(plugin.getDataFolder(), "mounts.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        return config.getConfigurationSection("mounts." + mountTypeId + ".customization");
    }
    
    /**
     * Safe way to get a particle by name with fallback
     */
    private Particle getParticleByName(String name) {
        try {
            return Particle.valueOf(name);
        } catch (IllegalArgumentException e) {
            // Fallback to a basic particle that exists in most versions
            return Particle.CLOUD;
        }
    }
    
    /**
     * Cleanup all active visual effects
     */
    public void cleanup() {
        for (BukkitTask task : activeEffects.values()) {
            task.cancel();
        }
        activeEffects.clear();
    }
}