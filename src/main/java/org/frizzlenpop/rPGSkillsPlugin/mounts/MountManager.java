package org.frizzlenpop.rPGSkillsPlugin.mounts;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.data.EconomyManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.abilities.MountAbilityManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.gui.MountGUI;
import org.frizzlenpop.rPGSkillsPlugin.mounts.xp.MountXPManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountVisualManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.types.DireWolfMount;
import org.frizzlenpop.rPGSkillsPlugin.mounts.types.WingedPigMount;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountDismissedEvent;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountSummonedEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Main class to manage mounts, their summoning, and their abilities.
 */
public class MountManager implements Listener {
    private final RPGSkillsPlugin plugin;
    private final Map<UUID, Mount> activeMounts = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> mountEffectTasks = new ConcurrentHashMap<>();
    private final Map<String, MountType> mountTypes = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Set<String>> playerOwnedMounts = new HashMap<>();
    private final PlayerDataManager playerDataManager;
    private final EconomyManager economyManager;
    private final MountAbilityManager abilityManager;
    private final MountXPManager xpManager;
    private MountVisualManager visualManager;
    private FileConfiguration mountConfig;
    private final Map<UUID, List<Entity>> mountDecorations = new ConcurrentHashMap<>();

    /**
     * Creates a new Mount Manager instance
     * 
     * @param plugin Reference to the main plugin
     */
    public MountManager(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.economyManager = plugin.getEconomyManager();
        loadMountConfig();
        loadMountTypes();
        this.abilityManager = new MountAbilityManager(plugin, this);
        this.xpManager = new MountXPManager(plugin, this);
        loadPlayerMounts();
        
        // Register this class as a listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getPluginManager().registerEvents(abilityManager, plugin);
        plugin.getServer().getPluginManager().registerEvents(xpManager, plugin);
        
        // Start a task to process mount effects and passive XP gain
        startMountEffectsTask();
        
        // Start orphaned mount check
        startOrphanedMountCheckTask();
    }
    
    /**
     * Sets the visual manager for enhanced mount visuals
     * 
     * @param visualManager The visual manager to use
     */
    public void setVisualManager(MountVisualManager visualManager) {
        this.visualManager = visualManager;
    }
    
    /**
     * Loads mount configuration from mounts.yml file
     */
    private void loadMountConfig() {
        File configFile = new File(plugin.getDataFolder(), "mounts.yml");
        if (!configFile.exists()) {
            plugin.saveResource("mounts.yml", false);
        }
        
        mountConfig = YamlConfiguration.loadConfiguration(configFile);
    }
    
    /**
     * Loads all mount types from configuration
     */
    private void loadMountTypes() {
        ConfigurationSection mountsSection = mountConfig.getConfigurationSection("mounts");
        if (mountsSection == null) {
            plugin.getLogger().warning("No mounts defined in mounts.yml!");
            return;
        }
        
        for (String key : mountsSection.getKeys(false)) {
            ConfigurationSection mountSection = mountsSection.getConfigurationSection(key);
            if (mountSection == null) continue;
            
            String displayName = ChatColor.translateAlternateColorCodes('&', mountSection.getString("display-name", key));
            String description = ChatColor.translateAlternateColorCodes('&', mountSection.getString("description", ""));
            EntityType entityType = EntityType.valueOf(mountSection.getString("entity-type", "HORSE"));
            int baseCost = mountSection.getInt("base-cost", 25000);
            
            // Load stats
            ConfigurationSection statsSection = mountSection.getConfigurationSection("stats");
            double speed = statsSection != null ? statsSection.getDouble("speed", 0.2) : 0.2;
            double jump = statsSection != null ? statsSection.getDouble("jump", 0.7) : 0.7;
            double health = statsSection != null ? statsSection.getDouble("health", 20.0) : 20.0;
            
            // Create mount type
            MountType mountType = new MountType(key, displayName, description, entityType, baseCost, speed, jump, health);
            
            // Load customization
            ConfigurationSection customSection = mountSection.getConfigurationSection("customization");
            if (customSection != null) {
                for (String customKey : customSection.getKeys(false)) {
                    mountType.setCustomization(customKey, customSection.getString(customKey));
                }
            }
            
            // Load abilities
            ConfigurationSection abilitiesSection = mountSection.getConfigurationSection("abilities");
            if (abilitiesSection != null) {
                for (String abilityKey : abilitiesSection.getKeys(false)) {
                    ConfigurationSection abilitySection = abilitiesSection.getConfigurationSection(abilityKey);
                    if (abilitySection == null) continue;
                    
                    boolean enabled = abilitySection.getBoolean("enabled", true);
                    int cooldown = abilitySection.getInt("cooldown", 30);
                    boolean passive = abilitySection.getBoolean("passive", false);
                    int minLevel = abilitySection.getInt("min-level", 1);
                    
                    mountType.addAbility(abilityKey, enabled, cooldown, passive, minLevel);
                }
            }
            
            // Load effects
            ConfigurationSection effectsSection = mountSection.getConfigurationSection("effects");
            if (effectsSection != null) {
                ConfigurationSection particleSection = effectsSection.getConfigurationSection("particle_effects");
                if (particleSection != null) {
                    for (String particleKey : particleSection.getKeys(false)) {
                        mountType.addParticleEffect(particleKey, particleSection.getString(particleKey));
                    }
                }
                
                ConfigurationSection soundSection = effectsSection.getConfigurationSection("sound_effects");
                if (soundSection != null) {
                    for (String soundKey : soundSection.getKeys(false)) {
                        mountType.addSoundEffect(soundKey, soundSection.getString(soundKey));
                    }
                }
            }
            
            mountTypes.put(key, mountType);
            plugin.getLogger().info("Loaded mount type: " + key);
        }
    }
    
    /**
     * Loads player mount data from storage
     */
    private void loadPlayerMounts() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerMounts(player.getUniqueId());
        }
    }
    
    /**
     * Loads mount data for a specific player
     * 
     * @param playerUUID The player's UUID
     */
    private void loadPlayerMounts(UUID playerUUID) {
        FileConfiguration playerData = playerDataManager.getPlayerData(playerUUID);
        if (playerData == null) return;
        
        // Load owned mounts
        List<String> ownedMounts = playerData.getStringList("mounts.owned");
        playerOwnedMounts.put(playerUUID, new HashSet<>(ownedMounts));
        
        // Check for active mount to resummon if persistent_mounts is enabled
        if (mountConfig.getBoolean("general.persistent_mounts", false)) {
            String activeMountId = playerData.getString("mounts.active");
            if (activeMountId != null && !activeMountId.isEmpty()) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    // Delay mount summoning to ensure player is fully loaded
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        summonMount(player, activeMountId);
                    }, 40L); // 2-second delay
                }
            }
        }
    }
    
    /**
     * Saves player's mount data
     * 
     * @param playerUUID The player's UUID
     */
    public void savePlayerMounts(UUID playerUUID) {
        FileConfiguration playerData = playerDataManager.getPlayerData(playerUUID);
        if (playerData == null) return;
        
        // Save owned mounts
        Set<String> ownedMounts = playerOwnedMounts.getOrDefault(playerUUID, new HashSet<>());
        playerData.set("mounts.owned", new ArrayList<>(ownedMounts));
        
        // Save active mount
        Mount activeMount = activeMounts.get(playerUUID);
        if (activeMount != null) {
            playerData.set("mounts.active", activeMount.getType().getId());
        } else {
            playerData.set("mounts.active", null);
        }
        
        // Save mount data
        playerDataManager.savePlayerData(playerUUID, playerData);
    }
    
    /**
     * Purchases a mount for a player
     * 
     * @param player The player purchasing the mount
     * @param mountId The ID of the mount to purchase
     * @return true if purchase was successful, false otherwise
     */
    public boolean purchaseMount(Player player, String mountId) {
        MountType mountType = mountTypes.get(mountId);
        if (mountType == null) {
            player.sendMessage(formatMessage("purchase.invalid-mount"));
            return false;
        }
        
        // Check if player already owns this mount
        if (ownsMount(player.getUniqueId(), mountId)) {
            player.sendMessage(formatMessage("purchase.already-owned")
                    .replace("%mount%", mountType.getDisplayName()));
            return false;
        }
        
        // Check if player has enough money
        int cost = mountType.getBaseCost();
        if (!economyManager.isEconomyEnabled() || !economyManager.withdrawMoney(player, cost)) {
            player.sendMessage(formatMessage("purchase.not-enough-money")
                    .replace("%cost%", String.valueOf(cost))
                    .replace("%balance%", String.valueOf(economyManager.getBalance(player))));
            return false;
        }
        
        // Add mount to player's owned mounts
        addMountToPlayer(player.getUniqueId(), mountId);
        
        // Send success message
        player.sendMessage(formatMessage("purchase.success")
                .replace("%mount%", mountType.getDisplayName())
                .replace("%cost%", String.valueOf(cost)));
        
        return true;
    }
    
    /**
     * Adds a mount to a player's owned mounts
     * 
     * @param playerUUID The player's UUID
     * @param mountId The mount's ID
     */
    public void addMountToPlayer(UUID playerUUID, String mountId) {
        Set<String> ownedMounts = playerOwnedMounts.computeIfAbsent(playerUUID, k -> new HashSet<>());
        ownedMounts.add(mountId);
        savePlayerMounts(playerUUID);
    }
    
    /**
     * Removes a mount from a player
     * 
     * @param playerUUID The player's UUID
     * @param mountId The mount's ID
     * @return true if removed successfully, false if player didn't own the mount
     */
    public boolean removeMountFromPlayer(UUID playerUUID, String mountId) {
        Set<String> ownedMounts = playerOwnedMounts.get(playerUUID);
        if (ownedMounts == null || !ownedMounts.contains(mountId)) {
            return false;
        }
        
        // If it's the active mount, dismiss it first
        Player player = Bukkit.getPlayer(playerUUID);
        Mount activeMount = activeMounts.get(playerUUID);
        if (activeMount != null && activeMount.getType().getId().equals(mountId)) {
            dismissMount(playerUUID);
        }
        
        // Remove from owned mounts
        ownedMounts.remove(mountId);
        savePlayerMounts(playerUUID);
        return true;
    }
    
    /**
     * Checks if a player owns a specific mount
     * 
     * @param playerUUID The player's UUID
     * @param mountId The mount's ID
     * @return true if player owns the mount
     */
    public boolean ownsMount(UUID playerUUID, String mountId) {
        Set<String> ownedMounts = playerOwnedMounts.get(playerUUID);
        return ownedMounts != null && ownedMounts.contains(mountId);
    }
    
    /**
     * Gets all mounts owned by a player
     * 
     * @param playerUUID The player's UUID
     * @return Set of mount IDs owned by the player
     */
    public Set<String> getPlayerOwnedMounts(UUID playerUUID) {
        return playerOwnedMounts.getOrDefault(playerUUID, new HashSet<>());
    }
    
    /**
     * Gets all available mount types
     * 
     * @return Map of mount types
     */
    public Map<String, MountType> getMountTypes() {
        return Collections.unmodifiableMap(mountTypes);
    }
    
    /**
     * Gets a mount type by ID
     * 
     * @param mountId The mount's ID
     * @return The mount type or null if not found
     */
    public MountType getMountType(String mountId) {
        return mountTypes.get(mountId);
    }
    
    /**
     * Summons a mount for a player
     * 
     * @param player The player
     * @param mountId The mount's ID
     * @return true if successfully summoned
     */
    public boolean summonMount(Player player, String mountId) {
        // Check if player is in a world where mounts are disabled
        if (isInDisabledWorld(player)) {
            player.sendMessage(ChatColor.RED + "Mounts are disabled in this world.");
            return false;
        }
        
        // Check if player owns this mount
        if (!ownsMount(player.getUniqueId(), mountId)) {
            player.sendMessage(formatMessage("summon.not-owned"));
            return false;
        }
        
        // Check cooldown
        if (isOnCooldown(player.getUniqueId())) {
            long remaining = getCooldownTimeRemaining(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "You must wait " + remaining + " seconds before summoning a mount!");
            return false;
        }
        
        // Dismiss any existing mount
        dismissMount(player.getUniqueId());
        
        // Get mount type
        MountType mountType = mountTypes.get(mountId);
        if (mountType == null) {
            player.sendMessage(formatMessage("summon.invalid-mount"));
            return false;
        }
        
        // Get mount rarity and level data
        FileConfiguration playerData = playerDataManager.getPlayerData(player.getUniqueId());
        MountRarity rarity = MountRarity.COMMON; // Default
        if (playerData.contains("mounts.data." + mountId + ".rarity")) {
            String rarityStr = playerData.getString("mounts.data." + mountId + ".rarity", "COMMON");
            rarity = MountRarity.valueOf(rarityStr);
        }
        
        // Create the actual mount entity
        try {
            Location spawnLocation = player.getLocation();
            Entity mountEntity = player.getWorld().spawnEntity(spawnLocation, mountType.getEntityType());
            
            // Configure the mount and get the mount instance
            Mount mount = configureMountEntity(mountEntity, mountType, player, rarity);
            
            if (mount == null) {
                mountEntity.remove();
                player.sendMessage(formatMessage("summon.failed"));
                return false;
            }
            
            // Store the mount instance
            activeMounts.put(player.getUniqueId(), mount);
            
            // Set the player as passenger
            mountEntity.addPassenger(player);
            
            // Apply visual enhancements if visual manager is available
            if (visualManager != null) {
                try {
                    visualManager.enhanceMount(mount);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to apply visual effects to mount: " + e.getMessage());
                    // Continue without visual effects rather than failing mount summoning
                }
            }
            
            // Start mount effects
            startMountEffects(player.getUniqueId(), mount);
            
            // Apply cooldown
            applyCooldown(player.getUniqueId());
            
            // Fire mount summoned event
            Bukkit.getPluginManager().callEvent(new MountSummonedEvent(player, mount));
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error summoning mount", e);
            player.sendMessage(formatMessage("summon.failed"));
            return false;
        }
    }
    
    /**
     * Configure the mount entity based on its type
     * 
     * @param mountEntity The entity to configure
     * @param mountType The mount type
     * @param player The player summoning the mount
     * @param rarity The mount's rarity
     * @return The configured Mount instance
     */
    private Mount configureMountEntity(Entity mountEntity, MountType mountType, Player player, MountRarity rarity) {
        // Apply metadata to mark this as a plugin mount
        mountEntity.setMetadata("rpgskills_mount", new FixedMetadataValue(plugin, true));
        mountEntity.setMetadata("rpgskills_mount_type", new FixedMetadataValue(plugin, mountType.getId()));
        mountEntity.setMetadata("rpgskills_mount_owner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        
        // Check for special mount types
        if (mountType.getId().equals("winged_pig")) {
            // Create a specialized winged pig mount
            WingedPigMount wingedPigMount = new WingedPigMount(plugin, player.getUniqueId(), rarity);
            wingedPigMount.setEntity(mountEntity);
            wingedPigMount.setType(mountType);
            return wingedPigMount;
        } else if (mountType.getId().equals("dire_wolf")) {
            // Create a specialized dire wolf mount
            DireWolfMount direWolfMount = new DireWolfMount(plugin, player.getUniqueId(), rarity);
            direWolfMount.setEntity(mountEntity);
            direWolfMount.setType(mountType);
            return direWolfMount;
        }
        
        // Configure the mount entity based on its type
        boolean configSuccess = false;
        
        if (mountEntity instanceof AbstractHorse horse) {
            configSuccess = configureHorseMount(horse, mountType, rarity);
        } else if (mountEntity instanceof Wolf wolf) {
            configSuccess = configureWolfMount(wolf, mountType, rarity);
        } else if (mountEntity instanceof Pig pig) {
            configSuccess = configurePigMount(pig, mountType, rarity);
        } else {
            // Other entity types could be added here
            return null;
        }
        
        if (!configSuccess) {
            return null;
        }
        
        // Create a standard mount instance
        return new ExtendedMount(mountEntity, mountType, player.getUniqueId(), rarity);
    }
    
    /**
     * Configure a pig mount
     * 
     * @param pig The pig entity
     * @param mountType The mount type
     * @param rarity The mount's rarity
     * @return true if successful
     */
    private boolean configurePigMount(Pig pig, MountType mountType, MountRarity rarity) {
        try {
            // Set basic properties
            setMountCustomName(pig, mountType.getDisplayName(), true);
            
            pig.setSaddle(true);
            
            // Apply rarity-based speed boost if the pig has the attribute
            AttributeInstance speedAttribute = pig.getAttribute(Attribute.valueOf("GENERIC_MOVEMENT_SPEED"));
            if (speedAttribute != null) {
                double baseSpeed = mountType.getSpeed();
                double rarityBonus = rarity.getStatMultiplier() - 1.0; // Convert multiplier to bonus
                speedAttribute.setBaseValue(baseSpeed * (1 + rarityBonus));
            }
            
            // Apply rarity-based health boost
            AttributeInstance healthAttribute = pig.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH"));
            if (healthAttribute != null) {
                double baseHealth = mountType.getHealth();
                double rarityBonus = rarity.getStatMultiplier() - 1.0;
                healthAttribute.setBaseValue(baseHealth * (1 + rarityBonus));
                pig.setHealth(healthAttribute.getBaseValue());
            }
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error configuring pig mount", e);
            return false;
        }
    }
    
    /**
     * Configures a horse-type mount with visual customizations
     * 
     * @param horse The horse entity
     * @param mountType The mount type
     * @param rarity The mount's rarity
     * @return true if configuration succeeded
     */
    private boolean configureHorseMount(AbstractHorse horse, MountType mountType, MountRarity rarity) {
        // Make the horse invulnerable 
        horse.setInvulnerable(true);
        
        // Set the owner
        String ownerUUIDString = horse.getMetadata("rpgskills_mount_owner").get(0).asString();
        Player owner = Bukkit.getPlayer(UUID.fromString(ownerUUIDString));
        if (owner != null) {
            horse.setOwner(owner);
        }
        horse.setTamed(true);
        
        // Calculate multipliers based on rarity
        double speedMultiplier = getRarityStatMultiplier(rarity);
        double jumpMultiplier = getRarityStatMultiplier(rarity);
        double healthMultiplier = getRarityStatMultiplier(rarity);
        
        // Configure attributes
        horse.setJumpStrength(mountType.getJump() * jumpMultiplier);
        
        // Make the horse fast
        AttributeInstance speedAttribute = horse.getAttribute(Attribute.valueOf("GENERIC_MOVEMENT_SPEED"));
        if (speedAttribute != null) {
            speedAttribute.setBaseValue(mountType.getSpeed() * speedMultiplier);
        }
        
        // Set health
        AttributeInstance healthAttribute = horse.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH"));
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(mountType.getHealth() * healthMultiplier);
            horse.setHealth(mountType.getHealth() * healthMultiplier);
        }
        
        // Apply visual customizations if it's a regular horse
        if (horse instanceof Horse regularHorse) {
            // Try to set color
            String colorStr = mountType.getCustomization("horse-color");
            if (colorStr != null) {
                try {
                    Horse.Color color = Horse.Color.valueOf(colorStr);
                    regularHorse.setColor(color);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid horse color: " + colorStr);
                }
            }
            
            // Try to set style
            String styleStr = mountType.getCustomization("horse-style");
            if (styleStr != null) {
                try {
                    Horse.Style style = Horse.Style.valueOf(styleStr);
                    regularHorse.setStyle(style);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid horse style: " + styleStr);
                }
            }
            
            // Add a saddle so the horse can be ridden properly
            ItemStack saddle = new ItemStack(Material.SADDLE);
            regularHorse.getInventory().setSaddle(saddle);
            
            // Don't allow armor by default (can be customized per mount type)
            regularHorse.getInventory().setArmor(null);
            
            // Apply visual effects based on rarity
            switch (rarity) {
                case UNCOMMON, RARE -> {
                    setMountCustomName(regularHorse, ChatColor.BLUE + mountType.getDisplayName(), true);
                }
                case EPIC -> {
                    setMountCustomName(regularHorse, ChatColor.DARK_PURPLE + mountType.getDisplayName(), true);
                    regularHorse.setGlowing(true);
                }
                case LEGENDARY, MYTHIC -> {
                    setMountCustomName(regularHorse, ChatColor.GOLD + mountType.getDisplayName(), true);
                    regularHorse.setGlowing(true);
                }
                default -> {
                    setMountCustomName(regularHorse, ChatColor.WHITE + mountType.getDisplayName(), true);
                }
            }
        } else {
            // For other AbstractHorse types like skeleton horses, zombie horses, etc.
            // Make sure they also have saddles if applicable
            if (horse instanceof SkeletonHorse skeletonHorse) {
                skeletonHorse.setTamed(true);
                // Skeleton horses can have saddles
                skeletonHorse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            } else if (horse instanceof ZombieHorse zombieHorse) {
                zombieHorse.setTamed(true);
                // Zombie horses can have saddles
                zombieHorse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            }
            
            // Apply name and visual effects based on rarity
            switch (rarity) {
                case UNCOMMON, RARE -> {
                    setMountCustomName(horse, ChatColor.BLUE + mountType.getDisplayName(), true);
                }
                case EPIC -> {
                    setMountCustomName(horse, ChatColor.DARK_PURPLE + mountType.getDisplayName(), true);
                    horse.setGlowing(true);
                }
                case LEGENDARY, MYTHIC -> {
                    setMountCustomName(horse, ChatColor.GOLD + mountType.getDisplayName(), true);
                    horse.setGlowing(true);
                }
                default -> {
                    setMountCustomName(horse, ChatColor.WHITE + mountType.getDisplayName(), true);
                }
            }
        }
        
        return true;
    }
    
    /**
     * Configures a wolf mount with customizations to make it function as a mount
     * 
     * @param wolf The wolf entity
     * @param mountType The mount type
     * @param rarity The mount's rarity
     * @return true if configuration succeeded
     */
    private boolean configureWolfMount(Wolf wolf, MountType mountType, MountRarity rarity) {
        // Make the wolf invulnerable
        wolf.setInvulnerable(true);
        
        // Set the wolf as tamed
        wolf.setTamed(true);
        
        // Set the owner
        String ownerUUIDString = wolf.getMetadata("rpgskills_mount_owner").get(0).asString();
        Player owner = Bukkit.getPlayer(UUID.fromString(ownerUUIDString));
        if (owner != null) {
            wolf.setOwner(owner);
        }
        
        // Make the wolf stay in place when not ridden
        wolf.setSitting(false);
        
        // Configure speed based on mount type
        AttributeInstance attribute = wolf.getAttribute(Attribute.valueOf("GENERIC_MOVEMENT_SPEED"));
        if (attribute != null) {
            // Wolves are naturally slower than horses, so we use a multiplier to make them faster when used as mounts
            double speedMultiplier = 2.0; // This can be adjusted if wolves are too fast/slow
            attribute.setBaseValue(mountType.getSpeed() * speedMultiplier);
        }
        
        // Set health based on mount type
        AttributeInstance healthAttribute = wolf.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH"));
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(mountType.getHealth());
            wolf.setHealth(mountType.getHealth());
        }
        
        // Apply visual effects based on rarity and customizations
        
        // Try to set collar color based on customization
        String collarColorStr = mountType.getCustomization("wolf-collar-color");
        if (collarColorStr != null) {
            try {
                org.bukkit.DyeColor collarColor = org.bukkit.DyeColor.valueOf(collarColorStr);
                wolf.setCollarColor(collarColor);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid wolf collar color: " + collarColorStr);
            }
        }
        
        // Set name and visual effects based on rarity
        switch (rarity) {
            case UNCOMMON, RARE -> {
                setMountCustomName(wolf, ChatColor.BLUE + mountType.getDisplayName(), true);
            }
            case EPIC -> {
                setMountCustomName(wolf, ChatColor.DARK_PURPLE + mountType.getDisplayName(), true);
                wolf.setGlowing(true);
            }
            case LEGENDARY, MYTHIC -> {
                setMountCustomName(wolf, ChatColor.GOLD + mountType.getDisplayName(), true);
                wolf.setGlowing(true);
                
                // For legendary/mythic wolves, add extra visual effects
                wolf.setAngry(false); // Not angry by default
                
                // Wolf will appear larger for legendary/mythic rarities
                // This requires using the Bukkit Metadata API to flag for custom rendering
                wolf.setMetadata("rpgskills_large_wolf", new FixedMetadataValue(plugin, true));
            }
            default -> {
                setMountCustomName(wolf, ChatColor.WHITE + mountType.getDisplayName(), true);
            }
        }
        
        // Set up control mechanism for wolf movement
        // This will make the wolf respond to player controls like a horse would
        startWolfControlTask(wolf, mountType);
        
        return true;
    }
    
    /**
     * Starts a task to control wolf movement based on player input
     * 
     * @param wolf The wolf entity
     * @param mountType The mount type
     */
    private void startWolfControlTask(Wolf wolf, MountType mountType) {
        // Get the mount speed for calculations
        double speed = mountType.getSpeed();
        
        // Task runs every tick to process movement
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            // Stop the task if the wolf is removed or no longer has passengers
            if (wolf == null || wolf.isDead() || wolf.getPassengers().isEmpty()) {
                task.cancel();
                return;
            }
            
            // Get the player riding the wolf
            if (!(wolf.getPassengers().get(0) instanceof Player player)) {
                return;
            }
            
            // Get player's looking direction
            float yaw = player.getLocation().getYaw();
            wolf.setRotation(yaw, 0); // Set wolf to face the same direction as player
            
            // Process movement based on player input
            // This is similar to how horses handle movement
            if (player.isSneaking()) {
                // Player is sneaking - make the wolf sit
                wolf.setVelocity(wolf.getVelocity().multiply(0.4)); // Slow down movement
            } else {
                // Check if player is moving
                if (player.getVelocity().length() > 0.01) {
                    // Calculate movement direction
                    double rad = Math.toRadians(yaw);
                    double sin = Math.sin(rad);
                    double cos = Math.cos(rad);
                    
                    // Forward/backward movement
                    double forward = 0;
                    if (player.getLocation().getPitch() < 45) {
                        forward = 1.0; // Move forward
                    } else if (player.getLocation().getPitch() > 60) {
                        forward = -0.5; // Move backward
                    }
                    
                    // Calculate velocity components
                    double vx = -sin * forward * speed;
                    double vz = cos * forward * speed;
                    
                    // Apply movement
                    if (forward != 0) {
                        wolf.setVelocity(new org.bukkit.util.Vector(vx, wolf.getVelocity().getY(), vz));
                    }
                    
                    // Jump handling
                    if (player.getLocation().getPitch() < -20 && wolf.isOnGround()) {
                        // Player looking up indicates jump
                        double jumpHeight = mountType.getJump();
                        wolf.setVelocity(wolf.getVelocity().setY(jumpHeight * 0.6));
                    }
                }
            }
        }, 0L, 1L); // Run every tick
    }
    
    /**
     * Applies rarity stat multiplier based on mount rarity
     * 
     * @param rarity The mount rarity
     * @return The multiplier to apply to base stats
     */
    private double getRarityStatMultiplier(MountRarity rarity) {
        return switch (rarity) {
            case COMMON -> 1.0;
            case UNCOMMON -> 1.05; // +5%
            case RARE -> 1.1;      // +10%
            case EPIC -> 1.2;      // +20%
            case LEGENDARY -> 1.35; // +35%
            case MYTHIC -> 1.5;    // +50%
        };
    }
    
    /**
     * Dismisses an active mount for a player
     * 
     * @param playerUUID The player's UUID
     * @return true if a mount was dismissed
     */
    public boolean dismissMount(UUID playerUUID) {
        Mount mount = activeMounts.get(playerUUID);
        if (mount == null) {
            return false;
        }
        
        // Cancel effect tasks
        BukkitTask effectTask = mountEffectTasks.remove(playerUUID);
        if (effectTask != null) {
            effectTask.cancel();
        }
        
        // Fire event before dismissing
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            MountDismissedEvent event = new MountDismissedEvent(player, mount);
            Bukkit.getPluginManager().callEvent(event);
            
            player.sendMessage(formatMessage("dismiss.success")
                    .replace("%mount%", mount.getType().getDisplayName()));
        }
        
        // Clean up decorations like name tags
        cleanupMountDecorations(mount);
        
        // Remove entity and references
        Entity entity = mount.getEntity();
        if (entity != null && !entity.isDead()) {
            entity.eject(); // Dismount rider
            entity.remove(); // Remove entity
        }
        
        activeMounts.remove(playerUUID);
        
        // Update player data
        FileConfiguration playerData = playerDataManager.getPlayerData(playerUUID);
        if (playerData != null) {
            playerData.set("mounts.active", null);
            playerDataManager.savePlayerData(playerUUID, playerData);
        }
        
        return true;
    }
    
    /**
     * Dismisses an active mount for a player
     * 
     * @param player The player dismissing their mount
     * @return true if a mount was dismissed
     */
    public boolean dismissMount(Player player) {
        return dismissMount(player.getUniqueId());
    }
    
    /**
     * Gets a player's active mount
     * 
     * @param playerUUID The player's UUID
     * @return The active mount or null if none
     */
    public Mount getActiveMount(UUID playerUUID) {
        return activeMounts.get(playerUUID);
    }
    
    /**
     * Applies mount summon cooldown
     * 
     * @param playerUUID The player's UUID
     */
    private void applyCooldown(UUID playerUUID) {
        int cooldownSeconds = mountConfig.getInt("settings.summon-cooldown", 10);
        cooldowns.put(playerUUID, System.currentTimeMillis() + (cooldownSeconds * 1000L));
    }
    
    /**
     * Checks if a player is on mount cooldown
     * 
     * @param playerUUID The player's UUID
     * @return true if on cooldown
     */
    private boolean isOnCooldown(UUID playerUUID) {
        Long cooldownEnd = cooldowns.get(playerUUID);
        return cooldownEnd != null && cooldownEnd > System.currentTimeMillis();
    }
    
    /**
     * Gets remaining cooldown time in seconds
     * 
     * @param playerUUID The player's UUID
     * @return Seconds remaining or 0 if not on cooldown
     */
    private long getCooldownTimeRemaining(UUID playerUUID) {
        Long cooldownEnd = cooldowns.get(playerUUID);
        if (cooldownEnd == null || cooldownEnd <= System.currentTimeMillis()) {
            return 0;
        }
        return (cooldownEnd - System.currentTimeMillis()) / 1000;
    }
    
    /**
     * Checks if a world has mounts disabled
     * 
     * @param player The player to check
     * @return true if mounts are disabled in this world
     */
    private boolean isInDisabledWorld(Player player) {
        List<String> disabledWorlds = mountConfig.getStringList("disabled_worlds");
        return disabledWorlds.contains(player.getWorld().getName());
    }
    
    /**
     * Starts mount effect processing for a player
     * 
     * @param playerUUID The player's UUID
     * @param mount The mount to process effects for
     */
    private void startMountEffects(UUID playerUUID, Mount mount) {
        // Cancel any existing tasks
        BukkitTask existingTask = mountEffectTasks.remove(playerUUID);
        if (existingTask != null) {
            existingTask.cancel();
        }
        
        // Start new task
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Process mount effects and abilities
            Entity entity = mount.getEntity();
            if (entity == null || entity.isDead() || entity.getPassengers().isEmpty()) {
                // Mount was removed or player dismounted
                BukkitTask taskToCancel = mountEffectTasks.remove(playerUUID);
                if (taskToCancel != null) {
                    taskToCancel.cancel();
                }
                return;
            }
            
            // Process particle effects
            abilityManager.processParticleEffects(mount);
            
            // Process passive abilities
            abilityManager.processPassiveAbilities(mount);
            
            // Process mount XP gain
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                xpManager.processPassiveXPGain(player, mount);
            }
        }, 20L, 20L); // Run every second
        
        mountEffectTasks.put(playerUUID, task);
    }
    
    /**
     * Starts a task to process all mount effects and XP
     */
    private void startMountEffectsTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Process all active mounts
            for (Map.Entry<UUID, Mount> entry : activeMounts.entrySet()) {
                UUID playerUUID = entry.getKey();
                Mount mount = entry.getValue();
                
                Player player = Bukkit.getPlayer(playerUUID);
                if (player == null || !player.isOnline()) continue;
                
                // Only process if player is riding the mount
                Entity entity = mount.getEntity();
                if (entity == null || entity.isDead() || !entity.getPassengers().contains(player)) continue;
                
                // Process XP gain
                xpManager.processPassiveXPGain(player, mount);
            }
        }, 60L, 1200L); // Run every minute (20 ticks * 60 = 1200 ticks)
    }
    
    /**
     * Gets a formatted message from the configuration
     * 
     * @param key The message key
     * @return The formatted message
     */
    private String formatMessage(String key) {
        String message = mountConfig.getString("messages." + key, "§cMessage not found: " + key);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Saves mount data for all players
     */
    public void saveAllMountData() {
        for (UUID playerUUID : playerOwnedMounts.keySet()) {
            savePlayerMounts(playerUUID);
        }
    }
    
    /**
     * Event handler for player join
     * 
     * @param event The player join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadPlayerMounts(player.getUniqueId());
    }
    
    /**
     * Event handler for player quit
     * 
     * @param event The player quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Save data
        savePlayerMounts(playerUUID);
        
        // Dismiss mount unless persistence is enabled
        if (!mountConfig.getBoolean("general.persistent_mounts", false)) {
            dismissMount(playerUUID);
        }
        
        // Clean up references
        cooldowns.remove(playerUUID);
    }
    
    /**
     * Event handler for vehicle exit
     * 
     * @param event The vehicle exit event
     */
    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player player)) return;
        
        // Get the mount if this was a plugin mount
        Entity vehicle = event.getVehicle();
        if (!vehicle.hasMetadata("rpgskills_mount")) return;
        
        UUID playerUUID = player.getUniqueId();
        Mount mount = activeMounts.get(playerUUID);
        if (mount == null) return;
        
        // Get the entity type to handle mount-specific behavior
        EntityType entityType = vehicle.getType();
        
        // Remove mount after delay unless auto-recovery is enabled
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // If mount has auto-recovery, don't dismiss it
            boolean hasAutoRecovery = xpManager.hasPassivePerk(playerUUID, mount.getType().getId(), "auto-recovery");
            
            // Get updated passenger list
            List<Entity> currentPassengers = vehicle.getPassengers();
            boolean playerIsRiding = currentPassengers.contains(player);
            
            // Debug logging to help diagnose the issue
            plugin.getLogger().info("Mount exit check: Type=" + entityType + 
                                   ", HasAutoRecovery=" + hasAutoRecovery + 
                                   ", PlayerIsRiding=" + playerIsRiding);
            
            // Always dismiss non-horse mounts unless player is still riding OR has auto-recovery
            if (!hasAutoRecovery && !playerIsRiding) {
                // For non-horse entities, we need to make sure they're properly dismissed
                plugin.getLogger().info("Dismissing mount of type: " + entityType);
                dismissMount(playerUUID);
            }
        }, 100L); // 5-second delay
    }
    
    /**
     * Event handler for entity damage
     * 
     * @param event The entity damage event
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // Make sure mount entities don't take damage
        Entity entity = event.getEntity();
        if (entity.hasMetadata("rpgskills_mount")) {
            event.setCancelled(true);
        }
        
        // Handle abilities like damage reduction for riders
        if (event.getEntity() instanceof Player player) {
            Mount mount = getActiveMount(player.getUniqueId());
            if (mount != null) {
                abilityManager.processRiderDamageAbilities(player, mount, event);
            }
        }
    }
    
    /**
     * Gets the mount configuration
     * 
     * @return The mount configuration
     */
    public FileConfiguration getMountConfig() {
        return mountConfig;
    }
    
    /**
     * Gets the mount ability manager
     * 
     * @return The ability manager
     */
    public MountAbilityManager getAbilityManager() {
        return abilityManager;
    }
    
    /**
     * Gets the mount XP manager
     * 
     * @return The XP manager
     */
    public MountXPManager getXPManager() {
        return xpManager;
    }
    
    /**
     * Reloads the mount configuration
     */
    public void reloadConfig() {
        loadMountConfig();
        mountTypes.clear();
        loadMountTypes();
    }
    
    /**
     * Gets a mount by its entity
     * 
     * @param entity The entity to check
     * @return The mount if found, null otherwise
     */
    public Mount getMountByEntity(Entity entity) {
        if (entity == null) return null;
        
        // Check each active mount to see if it has this entity
        for (Map.Entry<UUID, Mount> entry : activeMounts.entrySet()) {
            Mount mount = entry.getValue();
            if (mount.getEntity() != null && mount.getEntity().getUniqueId().equals(entity.getUniqueId())) {
                return mount;
            }
        }
        
        return null;
    }
    
    /**
     * Set the mount's custom name with the appropriate offset
     * 
     * @param entity The mount entity
     * @param name The name to set
     * @param visible Whether the name should be visible
     */
    private void setMountCustomName(Entity entity, String name, boolean visible) {
        // Create a name tag ArmorStand that is positioned higher than the default
        ArmorStand nameTag = (ArmorStand) entity.getWorld().spawnEntity(
            entity.getLocation().clone().add(0, 1.5, 0), // Offset by 1.5 blocks up
            EntityType.ARMOR_STAND
        );
        
        // Configure the armor stand to be just a floating name
        nameTag.setVisible(false);
        nameTag.setGravity(false);
        nameTag.setCustomName(name);
        nameTag.setCustomNameVisible(visible);
        nameTag.setMarker(true);
        nameTag.setSmall(true);
        
        // Add metadata to link the name tag to the mount
        nameTag.setMetadata("rpgskills_mount_nametag", new FixedMetadataValue(plugin, entity.getUniqueId().toString()));
        entity.setMetadata("rpgskills_mount_nametag_id", new FixedMetadataValue(plugin, nameTag.getUniqueId().toString()));
        
        // Hide the entity's own name
        entity.setCustomNameVisible(false);
        
        // Keep track of the name tag to move it with the mount
        List<Entity> decorations = new ArrayList<>();
        decorations.add(nameTag);
        
        UUID entityUuid = entity.getUniqueId();
        if (!mountDecorations.containsKey(entityUuid)) {
            mountDecorations.put(entityUuid, decorations);
        } else {
            mountDecorations.get(entityUuid).addAll(decorations);
        }
        
        // Start a task to move the name tag with the mount
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!entity.isValid() || entity.isDead()) {
                    nameTag.remove();
                    this.cancel();
                    return;
                }
                
                // Update position to follow the mount
                nameTag.teleport(entity.getLocation().clone().add(0, 1.5, 0));
            }
        };
        
        task.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Cleanup mount decorations when a mount is dismissed
     * 
     * @param mount The mount being dismissed
     */
    private void cleanupMountDecorations(Mount mount) {
        if (mount == null || !mount.isValid()) {
            return;
        }
        
        Entity entity = mount.getEntity();
        if (entity == null) return;
        
        UUID entityUuid = entity.getUniqueId();
        
        // Remove decorations like name tags
        if (mountDecorations.containsKey(entityUuid)) {
            List<Entity> decorations = mountDecorations.get(entityUuid);
            for (Entity decoration : decorations) {
                if (decoration != null && decoration.isValid()) {
                    decoration.remove();
                }
            }
            mountDecorations.remove(entityUuid);
        }
        
        // Check for metadata name tag
        if (entity.hasMetadata("rpgskills_mount_nametag_id")) {
            try {
                String nameTagUuidStr = entity.getMetadata("rpgskills_mount_nametag_id").get(0).asString();
                UUID nameTagUuid = UUID.fromString(nameTagUuidStr);
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    for (Entity e : world.getEntities()) {
                        if (e.getUniqueId().equals(nameTagUuid)) {
                            e.remove();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error removing name tag: " + e.getMessage());
            }
        }
    }
    
    /**
     * Starts an orphaned mount check task
     */
    private void startOrphanedMountCheckTask() {
        // Run every 10 seconds (200 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Keep track of any mounts we need to dismiss
            List<UUID> toRemove = new ArrayList<>();
            
            // Process all active mounts
            for (Map.Entry<UUID, Mount> entry : activeMounts.entrySet()) {
                UUID playerUUID = entry.getKey();
                Mount mount = entry.getValue();
                
                // Get the mount entity
                Entity entity = mount.getEntity();
                if (entity == null || entity.isDead()) {
                    toRemove.add(playerUUID);
                    continue;
                }
                
                // Get the player
                Player player = Bukkit.getPlayer(playerUUID);
                if (player == null || !player.isOnline()) {
                    if (!mountConfig.getBoolean("general.persistent_mounts", false)) {
                        toRemove.add(playerUUID);
                    }
                    continue;
                }
                
                // Special handling for non-horse entities (pigs, wolves, etc.)
                if (!(entity instanceof AbstractHorse)) {
                    // Check if player is still riding
                    boolean isRiding = false;
                    for (Entity passenger : entity.getPassengers()) {
                        if (passenger.equals(player)) {
                            isRiding = true;
                            break;
                        }
                    }
                    
                    // If not riding and sufficient time has passed, mark for removal
                    if (!isRiding) {
                        long timeSinceSummon = System.currentTimeMillis() - mount.getSummonTime();
                        if (timeSinceSummon > 5000) { // 5-second grace period after summoning
                            plugin.getLogger().info("Found orphaned non-horse mount (type: " + 
                                    entity.getType() + ") for player: " + player.getName());
                            toRemove.add(playerUUID);
                        }
                    }
                }
            }
            
            // Now dismiss all mounts that need to be removed
            for (UUID playerUUID : toRemove) {
                dismissMount(playerUUID);
            }
        }, 60L, 200L); // Start after 3 seconds, then check every 10 seconds
    }
} 