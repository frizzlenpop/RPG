package org.frizzlenpop.rPGSkillsPlugin.mounts.effects;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Manages advanced visual effects using ProtocolLib.
 * This allows for client-side-only effects that don't affect gameplay
 * but enhance the visual experience.
 */
public class ProtocolLibEffectManager {
    private final RPGSkillsPlugin plugin;
    private final ProtocolManager protocolManager;
    private final Random random = new Random();
    private final Map<UUID, List<Integer>> fakeEntityIds = new HashMap<>();
    private boolean safeMode = false;
    
    /**
     * Creates a new ProtocolLib effect manager
     * 
     * @param plugin The plugin instance
     */
    public ProtocolLibEffectManager(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        
        // Try to detect server version
        try {
            String version = Bukkit.getServer().getBukkitVersion();
            plugin.getLogger().info("Server version detected: " + version);
            
            // Enable safe mode to avoid issues with packet encoding
            safeMode = true;
            plugin.getLogger().info("ProtocolLib Effects Manager is running in SAFE MODE to prevent kick issues");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to detect server version: " + e.getMessage());
            safeMode = true;
        }
    }
    
    /**
     * Spawns a circle of armor stands with blocks on their heads around a mount
     * 
     * @param mount The entity to surround with blocks
     * @param players Players who should see the effect
     * @param blockId The block ID to display
     * @param count Number of blocks in the circle
     * @param radius Radius of the circle
     * @param height Height offset from the ground
     * @param rotationSpeed Rotation speed (negative for clockwise)
     */
    public void createBlockCircle(Entity mount, Collection<Player> players, int blockId, int count, 
                                 double radius, double height, double rotationSpeed) {
        if (safeMode) {
            // In safe mode, just spawn particles instead of fake entities
            createSafeParticleEffect(mount, players);
            return;
        }
        
        if (mount == null || !mount.isValid() || players.isEmpty()) return;
        
        try {
            UUID mountUUID = mount.getUniqueId();
            List<Integer> entityIds = new ArrayList<>();
            
            // Remove any existing fake entities for this mount
            removeFakeEntities(mountUUID);
            
            // Create the block circle
            for (int i = 0; i < count; i++) {
                int entityId = generateEntityId();
                entityIds.add(entityId);
                
                // Calculate initial position (evenly distributed around circle)
                double angle = (Math.PI * 2 * i) / count;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                
                // Spawn the armor stand that will hold the block
                Location location = mount.getLocation().clone().add(x, height, z);
                try {
                    spawnFakeArmorStand(players, entityId, location);
                    equipArmorStandWithBlock(players, entityId, blockId);
                } catch (InvocationTargetException e) {
                    plugin.getLogger().warning("Failed to create block circle: " + e.getMessage());
                }
            }
            
            // Store the entity IDs for this mount
            fakeEntityIds.put(mountUUID, entityIds);
            
            // Start a task to rotate the blocks
            new BukkitRunnable() {
                private double currentAngle = 0;
                
                @Override
                public void run() {
                    // Cancel if mount is no longer valid
                    if (!mount.isValid() || mount.isDead()) {
                        cancel();
                        removeFakeEntities(mountUUID);
                        return;
                    }
                    
                    // Update angle
                    currentAngle += rotationSpeed * (Math.PI / 180);
                    if (currentAngle > Math.PI * 2) {
                        currentAngle -= Math.PI * 2;
                    }
                    
                    // Update positions for each block
                    List<Integer> ids = fakeEntityIds.get(mountUUID);
                    if (ids != null) {
                        for (int i = 0; i < ids.size(); i++) {
                            int entityId = ids.get(i);
                            
                            // Calculate new position
                            double angle = currentAngle + ((Math.PI * 2 * i) / count);
                            double x = Math.cos(angle) * radius;
                            double z = Math.sin(angle) * radius;
                            
                            Location newLocation = mount.getLocation().clone().add(x, height, z);
                            try {
                                teleportFakeEntity(players, entityId, newLocation);
                            } catch (InvocationTargetException e) {
                                // Just log and continue
                                plugin.getLogger().warning("Failed to teleport entity: " + e.getMessage());
                            }
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        } catch (Exception e) {
            plugin.getLogger().severe("Error in createBlockCircle: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates fancy particle trails for a mount
     * 
     * @param mount The mount entity
     * @param players Players who should see the effect
     * @param particleType Particle to use
     * @param color Color for the particle (if applicable)
     * @param count Number of particles per step
     * @param height Height offset
     */
    public void createParticleTrails(Entity mount, Collection<Player> players, String particleType,
                                    Color color, int count, double height) {
        // Always use safe particle effects
        createSafeParticleEffect(mount, players);
    }
    
    /**
     * Creates a safe particle effect that won't crash clients
     */
    private void createSafeParticleEffect(Entity mount, Collection<Player> players) {
        if (mount == null || !mount.isValid()) return;
        
        // Start a task to spawn built-in Bukkit particles
        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
            if (!mount.isValid() || mount.isDead()) {
                task.cancel();
                return;
            }
            
            // Only spawn particles when the mount is moving
            if (mount.getVelocity().lengthSquared() > 0.01) {
                Location loc = mount.getLocation().clone().add(0, 0.5, 0);
                
                // Use a safe particle that's available in most versions
                for (Player player : players) {
                    try {
                        // Use the simplest particle type that should work in all versions
                        player.spawnParticle(org.bukkit.Particle.FLAME, loc, 3, 0.2, 0.1, 0.2, 0.01);
                    } catch (Exception e) {
                        // If that fails, don't attempt others to avoid further issues
                        plugin.getLogger().warning("Failed to spawn particles: " + e.getMessage());
                    }
                }
            }
        }, 2L, 2L);
    }
    
    /**
     * Creates floating armor stands with blocks
     * that orbit around the mount
     * 
     * @param mount The mount entity
     * @param players Players who should see the effect
     * @param blockIds Block IDs to use
     * @param count Number of orbiting blocks
     * @param radius Base radius
     * @param height Base height
     */
    public void createOrbitalBlocks(Entity mount, Collection<Player> players, List<Integer> blockIds,
                                   int count, double radius, double height) {
        if (safeMode) {
            // In safe mode, just spawn particles instead of fake entities
            createSafeParticleEffect(mount, players);
            return;
        }
        
        try {
            UUID mountUUID = mount.getUniqueId();
            List<Integer> entityIds = new ArrayList<>();
            
            // Create orbiting blocks
            for (int i = 0; i < count; i++) {
                int entityId = generateEntityId();
                entityIds.add(entityId);
                
                // Random block from the list
                int blockId = blockIds.get(random.nextInt(blockIds.size()));
                
                // Initial position
                Location spawnLoc = mount.getLocation().clone();
                
                try {
                    // Spawn the armor stand
                    spawnFakeArmorStand(players, entityId, spawnLoc);
                    
                    // Set the block
                    equipArmorStandWithBlock(players, entityId, blockId);
                } catch (InvocationTargetException e) {
                    plugin.getLogger().warning("Failed to create orbital block: " + e.getMessage());
                }
            }
            
            // Store entity IDs
            fakeEntityIds.put(mountUUID, entityIds);
            
            // Animation task
            plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                if (!mount.isValid() || mount.isDead()) {
                    removeFakeEntities(mountUUID);
                    task.cancel();
                    return;
                }
                
                // Update each block's position
                double time = System.currentTimeMillis() / 1000.0;
                for (int i = 0; i < entityIds.size(); i++) {
                    int entityId = entityIds.get(i);
                    
                    // Complex orbital animation
                    double phase = (2 * Math.PI / entityIds.size()) * i + time;
                    double verticalPhase = phase * 0.5;
                    
                    double x = radius * Math.cos(phase);
                    double y = height + Math.sin(verticalPhase) * 0.5;
                    double z = radius * Math.sin(phase);
                    
                    Location newLoc = mount.getLocation().clone().add(x, y, z);
                    try {
                        teleportFakeEntity(players, entityId, newLoc);
                    } catch (InvocationTargetException e) {
                        plugin.getLogger().warning("Failed to teleport orbital block: " + e.getMessage());
                    }
                }
            }, 1L, 1L);
        } catch (Exception e) {
            plugin.getLogger().severe("Error in createOrbitalBlocks: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates special effect based on a mount's type, safely using built-in particles
     * 
     * @param mount The mount entity
     * @param mountType The mount's type ID
     * @param players Players who should see the effect
     */
    public void createMountTypeEffect(Entity mount, String mountType, Collection<Player> players) {
        if (mount == null || !mount.isValid()) return;
        
        // Use minimal, safe effects
        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
            if (!mount.isValid() || mount.isDead()) {
                task.cancel();
                return;
            }
            
            // Only process every 10 ticks to reduce server load
            if (mount.getTicksLived() % 10 != 0) return;
            
            Location loc = mount.getLocation().add(0, 0.5, 0);
            
            // Use simple built-in particles based on mount type
            for (Player player : players) {
                try {
                    switch (mountType) {
                        // Use only FLAME and CLOUD which are basic particles available in most versions
                        case "phoenix_blaze", "shadow_steed", "crystal_drake", "storm_charger", "ancient_golem", "dire_wolf" -> 
                            player.spawnParticle(org.bukkit.Particle.FLAME, loc, 3, 0.2, 0.1, 0.2, 0.01);
                            
                        case "winged_pig" -> 
                            player.spawnParticle(org.bukkit.Particle.CLOUD, loc, 2, 0.2, 0.1, 0.2, 0.01);
                            
                        default -> {}
                    }
                } catch (Exception e) {
                    // Don't try fallbacks to avoid further issues
                    plugin.getLogger().warning("Failed to spawn mount particles: " + e.getMessage());
                }
            }
        }, 10L, 10L);
    }
    
    /**
     * Removes fake entities associated with a mount
     * 
     * @param mountUUID The mount's UUID
     */
    public void removeFakeEntities(UUID mountUUID) {
        List<Integer> entityIds = fakeEntityIds.remove(mountUUID);
        if (entityIds == null) return;
        
        // Get all players in the server to send destroy packets to
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        
        // Destroy all fake entities for this mount
        for (int entityId : entityIds) {
            try {
                destroyFakeEntity(players, entityId);
            } catch (InvocationTargetException e) {
                plugin.getLogger().warning("Failed to destroy fake entity: " + e.getMessage());
            }
        }
    }
    
    /**
     * Cleans up all fake entities
     */
    public void cleanup() {
        // Get all online players
        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
        
        // Clean up all fake entities
        for (List<Integer> ids : fakeEntityIds.values()) {
            for (int entityId : ids) {
                try {
                    destroyFakeEntity(players, entityId);
                } catch (InvocationTargetException e) {
                    plugin.getLogger().warning("Failed to destroy fake entity during cleanup: " + e.getMessage());
                }
            }
        }
        
        fakeEntityIds.clear();
    }
    
    /**
     * Generates a unique entity ID
     * 
     * @return A unique entity ID
     */
    private int generateEntityId() {
        // Generate a random negative ID to avoid conflicts
        return -(random.nextInt(10000) + 10000);
    }
    
    /**
     * Spawns a fake armor stand
     * 
     * @param players Players who should see the entity
     * @param entityId The entity ID to use
     * @param location The location to spawn at
     */
    private void spawnFakeArmorStand(Collection<? extends Player> players, int entityId, Location location) throws InvocationTargetException {
        try {
            // Create the spawn packet
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
            
            // Set entity ID, type and coordinates
            packet.getIntegers().write(0, entityId); // Entity ID
            
            try {
                // Use newer method if available
                packet.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
            } catch (Exception e) {
                // Fallback for older versions
                packet.getIntegers().write(1, 78); // Type ID for armor stand
            }
            
            // Set position - handling potential API differences
            try {
                packet.getDoubles().write(0, location.getX());
                packet.getDoubles().write(1, location.getY());
                packet.getDoubles().write(2, location.getZ());
            } catch (Exception e) {
                // Fallback for older versions that might use integers
                packet.getIntegers().write(2, (int)(location.getX() * 32));
                packet.getIntegers().write(3, (int)(location.getY() * 32));
                packet.getIntegers().write(4, (int)(location.getZ() * 32));
            }
    
            // Create metadata packet - try-catch for compatibility
            try {
                PacketContainer metadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
                metadataPacket.getIntegers().write(0, entityId);
                
                WrappedDataWatcher metadata = new WrappedDataWatcher();
                
                // Set invisible and small armorstand flags - try with different indexes for different versions
                try {
                    metadata.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x20); // Invisible
                    metadata.setObject(14, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x01); // Small armorstand
                } catch (Exception e) {
                    // If the above fails, try some other common index combinations
                    try {
                        metadata.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x20);
                        metadata.setObject(15, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x01);
                    } catch (Exception e2) {
                        // Just continue - it's better to have a visible armorstand than a kicked player
                    }
                }
                
                metadataPacket.getWatchableCollectionModifier().write(0, metadata.getWatchableObjects());
                
                // Send metadata packet to players
                for (Player player : players) {
                    protocolManager.sendServerPacket(player, packet);
                    protocolManager.sendServerPacket(player, metadataPacket);
                }
            } catch (Exception e) {
                // If metadata fails, at least send the spawn packet
                for (Player player : players) {
                    protocolManager.sendServerPacket(player, packet);
                }
                plugin.getLogger().warning("Failed to set armor stand metadata: " + e.getMessage());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn fake armor stand: " + e.getMessage());
            // Don't rethrow to prevent the effect system from crashing the whole mount
        }
    }
    
    /**
     * Equips an armor stand with a block on its head
     * 
     * @param players Players who should see the change
     * @param entityId The entity ID
     * @param blockId The block ID to display
     */
    private void equipArmorStandWithBlock(Collection<? extends Player> players, int entityId, int blockId) throws InvocationTargetException {
        try {
            // Create equipment packet for armor stand
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
            
            // Set entity ID
            packet.getIntegers().write(0, entityId);
            
            // Convert numeric block ID to Material
            Material material = getMaterialFromLegacyId(blockId);
            
            // Create ItemStack
            org.bukkit.inventory.ItemStack itemStack = new org.bukkit.inventory.ItemStack(material, 1);
            
            // Set slot based on version
            try {
                // Newer versions use an enum
                packet.getItemSlots().write(0, com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot.HEAD);
            } catch (Exception e) {
                // Older versions might use an integer
                packet.getIntegers().write(1, 4); // 4 = head slot
            }
            
            // Try to set item
            try {
                packet.getItemModifier().write(0, itemStack);
            } catch (Exception e) {
                // If direct method fails, try alternate approach
                plugin.getLogger().warning("Using alternative ItemStack handling method: " + e.getMessage());
                // Don't continue and risk crashes - better to have no block than kicked players
            }
    
            // Send packet to players
            for (Player player : players) {
                protocolManager.sendServerPacket(player, packet);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to equip armor stand with block: " + e.getMessage());
            // Don't rethrow to prevent the effect system from crashing the whole mount
        }
    }
    
    /**
     * Attempts to convert a legacy numerical block ID to a Material
     * 
     * @param blockId Legacy block ID
     * @return Material corresponding to the ID, or STONE as fallback
     */
    private Material getMaterialFromLegacyId(int blockId) {
        // This is a simplistic mapping, with only the most common materials
        switch (blockId) {
            case 1: return Material.STONE;
            case 4: return Material.COBBLESTONE;
            case 5: return Material.OAK_PLANKS;
            case 9: return Material.WATER;
            case 10: case 11: return Material.LAVA;
            case 22: return Material.LAPIS_BLOCK;
            case 35: return Material.WHITE_WOOL;
            case 41: return Material.GOLD_BLOCK;
            case 42: return Material.IRON_BLOCK;
            case 48: return Material.MOSSY_COBBLESTONE;
            case 49: return Material.OBSIDIAN;
            case 57: return Material.DIAMOND_BLOCK;
            case 80: return Material.SNOW_BLOCK;
            case 89: return Material.GLOWSTONE;
            default: return Material.STONE;
        }
    }
    
    /**
     * Teleports a fake entity
     * 
     * @param players Players who should see the teleport
     * @param entityId The entity ID
     * @param location The new location
     */
    private void teleportFakeEntity(Collection<? extends Player> players, int entityId, Location location) throws InvocationTargetException {
        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
            
            // Set entity ID
            packet.getIntegers().write(0, entityId);
            
            // Set coordinates - try different methods for version compatibility
            try {
                packet.getDoubles().write(0, location.getX());
                packet.getDoubles().write(1, location.getY());
                packet.getDoubles().write(2, location.getZ());
            } catch (Exception e) {
                // Fallback for older versions
                packet.getIntegers().write(1, (int)(location.getX() * 32));
                packet.getIntegers().write(2, (int)(location.getY() * 32));
                packet.getIntegers().write(3, (int)(location.getZ() * 32));
            }
            
            // Send packet to players
            for (Player player : players) {
                protocolManager.sendServerPacket(player, packet);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to teleport fake entity: " + e.getMessage());
            // Don't rethrow to prevent the effect system from crashing the whole mount
        }
    }
    
    /**
     * Destroys a fake entity
     * 
     * @param players Players who should see the entity removed
     * @param entityId The entity ID to destroy
     */
    private void destroyFakeEntity(Collection<? extends Player> players, int entityId) throws InvocationTargetException {
        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            
            // Different approaches for different versions
            try {
                // Newer versions
                packet.getIntLists().write(0, List.of(entityId));
            } catch (Exception e) {
                // Older versions might use int arrays
                packet.getIntegerArrays().write(0, new int[]{entityId});
            }
            
            // Send packet to players
            for (Player player : players) {
                protocolManager.sendServerPacket(player, packet);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to destroy fake entity: " + e.getMessage());
            // Don't rethrow to prevent the effect system from crashing the whole mount
        }
    }
} 