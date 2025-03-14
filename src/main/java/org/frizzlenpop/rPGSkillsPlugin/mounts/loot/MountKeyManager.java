package org.frizzlenpop.rPGSkillsPlugin.mounts.loot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.mounts.MountRarity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages mount keys for the loot chest system.
 */
public class MountKeyManager {
    private final RPGSkillsPlugin plugin;
    private final Map<KeyTier, Map<String, Double>> mountDropRates = new EnumMap<>(KeyTier.class);
    private final Map<UUID, Map<KeyTier, Integer>> playerKeys = new HashMap<>();
    private final NamespacedKey keyTypeKey;
    private FileConfiguration keyConfig;
    private File keyFile;
    
    /**
     * Enum representing key tiers with their drop rates
     */
    public enum KeyTier {
        COMMON(Material.IRON_NUGGET, ChatColor.WHITE, "Common Key", createCommonDropChances()),
        UNCOMMON(Material.GOLD_NUGGET, ChatColor.GREEN, "Uncommon Key", createUncommonDropChances()),
        RARE(Material.LAPIS_LAZULI, ChatColor.BLUE, "Rare Key", createRareDropChances()),
        EPIC(Material.DIAMOND, ChatColor.LIGHT_PURPLE, "Epic Key", createEpicDropChances()),
        LEGENDARY(Material.NETHER_STAR, ChatColor.GOLD, "Legendary Key", createLegendaryDropChances());
        
        private final Material material;
        private final ChatColor color;
        private final String displayName;
        private final Map<MountRarity, Double> rarityDropChances;
        
        KeyTier(Material material, ChatColor color, String displayName, Map<MountRarity, Double> rarityDropChances) {
            this.material = material;
            this.color = color;
            this.displayName = displayName;
            this.rarityDropChances = rarityDropChances;
        }
        
        public Material getMaterial() {
            return material;
        }
        
        public ChatColor getColor() {
            return color;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getFormattedName() {
            return color + displayName;
        }
        
        /**
         * Create drop chances for common key
         * 
         * @return Map of rarities to drop chances
         */
        private static Map<MountRarity, Double> createCommonDropChances() {
            Map<MountRarity, Double> chances = new EnumMap<>(MountRarity.class);
            chances.put(MountRarity.COMMON, 80.0);
            chances.put(MountRarity.UNCOMMON, 18.0);
            chances.put(MountRarity.RARE, 2.0);
            chances.put(MountRarity.EPIC, 0.0);
            chances.put(MountRarity.LEGENDARY, 0.0);
            chances.put(MountRarity.MYTHIC, 0.0);
            return chances;
        }
        
        /**
         * Create drop chances for uncommon key
         * 
         * @return Map of rarities to drop chances
         */
        private static Map<MountRarity, Double> createUncommonDropChances() {
            Map<MountRarity, Double> chances = new EnumMap<>(MountRarity.class);
            chances.put(MountRarity.COMMON, 50.0);
            chances.put(MountRarity.UNCOMMON, 40.0);
            chances.put(MountRarity.RARE, 9.5);
            chances.put(MountRarity.EPIC, 0.5);
            chances.put(MountRarity.LEGENDARY, 0.0);
            chances.put(MountRarity.MYTHIC, 0.0);
            return chances;
        }
        
        /**
         * Create drop chances for rare key
         * 
         * @return Map of rarities to drop chances
         */
        private static Map<MountRarity, Double> createRareDropChances() {
            Map<MountRarity, Double> chances = new EnumMap<>(MountRarity.class);
            chances.put(MountRarity.COMMON, 20.0);
            chances.put(MountRarity.UNCOMMON, 50.0);
            chances.put(MountRarity.RARE, 25.0);
            chances.put(MountRarity.EPIC, 4.9);
            chances.put(MountRarity.LEGENDARY, 0.1);
            chances.put(MountRarity.MYTHIC, 0.0);
            return chances;
        }
        
        /**
         * Create drop chances for epic key
         * 
         * @return Map of rarities to drop chances
         */
        private static Map<MountRarity, Double> createEpicDropChances() {
            Map<MountRarity, Double> chances = new EnumMap<>(MountRarity.class);
            chances.put(MountRarity.COMMON, 5.0);
            chances.put(MountRarity.UNCOMMON, 30.0);
            chances.put(MountRarity.RARE, 45.0);
            chances.put(MountRarity.EPIC, 19.0);
            chances.put(MountRarity.LEGENDARY, 1.0);
            chances.put(MountRarity.MYTHIC, 0.0);
            return chances;
        }
        
        /**
         * Create drop chances for legendary key
         * 
         * @return Map of rarities to drop chances
         */
        private static Map<MountRarity, Double> createLegendaryDropChances() {
            Map<MountRarity, Double> chances = new EnumMap<>(MountRarity.class);
            chances.put(MountRarity.COMMON, 0.0);
            chances.put(MountRarity.UNCOMMON, 10.0);
            chances.put(MountRarity.RARE, 40.0);
            chances.put(MountRarity.EPIC, 40.0);
            chances.put(MountRarity.LEGENDARY, 9.9);
            chances.put(MountRarity.MYTHIC, 0.1);
            return chances;
        }
        
        public Map<MountRarity, Double> getRarityDropChances() {
            return rarityDropChances;
        }
        
        /**
         * Roll for a random mount rarity based on this key's drop chances
         * 
         * @return The rolled rarity
         */
        public MountRarity rollRarity() {
            double roll = Math.random() * 100;
            double cumulativeChance = 0.0;
            
            for (Map.Entry<MountRarity, Double> entry : rarityDropChances.entrySet()) {
                cumulativeChance += entry.getValue();
                if (roll < cumulativeChance) {
                    return entry.getKey();
                }
            }
            
            // Default fallback
            return MountRarity.COMMON;
        }
    }
    
    /**
     * Creates a new mount key manager
     * 
     * @param plugin The plugin instance
     */
    public MountKeyManager(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        this.keyTypeKey = new NamespacedKey(plugin, "mount_key_type");
        loadKeyConfig();
        loadKeyDropRates();
        loadPlayerKeys();
    }
    
    /**
     * Loads the key configuration file
     */
    private void loadKeyConfig() {
        if (keyFile == null) {
            keyFile = new File(plugin.getDataFolder(), "mount_keys.yml");
        }
        
        if (!keyFile.exists()) {
            // Instead of trying to load from resources which might not exist
            // Create a default configuration file from scratch
            keyConfig = new YamlConfiguration();
            
            // Add default sections
            keyConfig.createSection("player_keys");
            
            ConfigurationSection dropRatesSection = keyConfig.createSection("drop_rates");
            
            // Add default drop rates for each key tier
            for (KeyTier tier : KeyTier.values()) {
                ConfigurationSection tierSection = dropRatesSection.createSection(tier.name().toLowerCase());
                tierSection.set("phoenix_blaze", 20.0);
                tierSection.set("shadow_steed", 20.0);
                tierSection.set("crystal_drake", 20.0);
                tierSection.set("storm_charger", 20.0);
                tierSection.set("ancient_golem", 20.0);
            }
            
            // Save the default configuration
            try {
                keyConfig.save(keyFile);
                plugin.getLogger().info("Created default mount_keys.yml configuration file");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create default mount_keys.yml file: " + e.getMessage());
            }
        } else {
            // Load existing configuration
            keyConfig = YamlConfiguration.loadConfiguration(keyFile);
        }
    }
    
    /**
     * Loads drop rates for mounts from each key type
     */
    private void loadKeyDropRates() {
        // Initialize with default values
        for (KeyTier tier : KeyTier.values()) {
            mountDropRates.put(tier, new HashMap<>());
            
            // Default 20% chance for each mount type
            mountDropRates.get(tier).put("phoenix_blaze", 20.0);
            mountDropRates.get(tier).put("shadow_steed", 20.0);
            mountDropRates.get(tier).put("crystal_drake", 20.0);
            mountDropRates.get(tier).put("storm_charger", 20.0);
            mountDropRates.get(tier).put("ancient_golem", 20.0);
        }
        
        // Load from config if specified
        ConfigurationSection dropRatesSection = keyConfig.getConfigurationSection("drop_rates");
        if (dropRatesSection != null) {
            for (String tierKey : dropRatesSection.getKeys(false)) {
                try {
                    KeyTier tier = KeyTier.valueOf(tierKey.toUpperCase());
                    ConfigurationSection tierSection = dropRatesSection.getConfigurationSection(tierKey);
                    
                    if (tierSection != null) {
                        for (String mountId : tierSection.getKeys(false)) {
                            double rate = tierSection.getDouble(mountId);
                            mountDropRates.get(tier).put(mountId, rate);
                        }
                    }
                } catch (IllegalArgumentException ignored) {
                    // Invalid tier name, skip
                }
            }
        }
    }
    
    /**
     * Loads player key counts from configuration
     */
    private void loadPlayerKeys() {
        ConfigurationSection keysSection = keyConfig.getConfigurationSection("player_keys");
        if (keysSection != null) {
            for (String uuidStr : keysSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    ConfigurationSection playerSection = keysSection.getConfigurationSection(uuidStr);
                    
                    if (playerSection != null) {
                        Map<KeyTier, Integer> keys = new HashMap<>();
                        
                        for (KeyTier tier : KeyTier.values()) {
                            String tierName = tier.name().toLowerCase();
                            int count = playerSection.getInt(tierName, 0);
                            keys.put(tier, count);
                        }
                        
                        playerKeys.put(uuid, keys);
                    }
                } catch (IllegalArgumentException ignored) {
                    // Invalid UUID, skip
                }
            }
        }
    }
    
    /**
     * Saves the key configuration
     */
    public void saveKeyConfig() {
        // Save player keys
        ConfigurationSection keysSection = keyConfig.createSection("player_keys");
        
        for (Map.Entry<UUID, Map<KeyTier, Integer>> entry : playerKeys.entrySet()) {
            ConfigurationSection playerSection = keysSection.createSection(entry.getKey().toString());
            
            for (Map.Entry<KeyTier, Integer> tierEntry : entry.getValue().entrySet()) {
                String tierName = tierEntry.getKey().name().toLowerCase();
                playerSection.set(tierName, tierEntry.getValue());
            }
        }
        
        // Save drop rates
        ConfigurationSection dropRatesSection = keyConfig.createSection("drop_rates");
        
        for (Map.Entry<KeyTier, Map<String, Double>> entry : mountDropRates.entrySet()) {
            ConfigurationSection tierSection = dropRatesSection.createSection(entry.getKey().name().toLowerCase());
            
            for (Map.Entry<String, Double> mountEntry : entry.getValue().entrySet()) {
                tierSection.set(mountEntry.getKey(), mountEntry.getValue());
            }
        }
        
        // Save to file
        try {
            keyConfig.save(keyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save mount keys config: " + e.getMessage());
        }
    }
    
    /**
     * Get the number of keys a player has of a specific tier
     * 
     * @param playerUUID The player's UUID
     * @param tier The key tier
     * @return The number of keys
     */
    public int getPlayerKeyCount(UUID playerUUID, KeyTier tier) {
        Map<KeyTier, Integer> keys = playerKeys.getOrDefault(playerUUID, new HashMap<>());
        return keys.getOrDefault(tier, 0);
    }
    
    /**
     * Get the total number of keys a player has
     * 
     * @param playerUUID The player's UUID
     * @return Map of key tiers to counts
     */
    public Map<KeyTier, Integer> getAllPlayerKeys(UUID playerUUID) {
        return new HashMap<>(playerKeys.getOrDefault(playerUUID, new HashMap<>()));
    }
    
    /**
     * Add keys to a player
     * 
     * @param playerUUID The player's UUID
     * @param tier The key tier
     * @param amount The amount to add
     */
    public void addPlayerKeys(UUID playerUUID, KeyTier tier, int amount) {
        Map<KeyTier, Integer> keys = playerKeys.computeIfAbsent(playerUUID, k -> new HashMap<>());
        int currentAmount = keys.getOrDefault(tier, 0);
        keys.put(tier, currentAmount + amount);
    }
    
    /**
     * Remove keys from a player
     * 
     * @param playerUUID The player's UUID
     * @param tier The key tier
     * @param amount The amount to remove
     * @return true if successful (player had enough keys)
     */
    public boolean removePlayerKeys(UUID playerUUID, KeyTier tier, int amount) {
        Map<KeyTier, Integer> keys = playerKeys.getOrDefault(playerUUID, new HashMap<>());
        int currentAmount = keys.getOrDefault(tier, 0);
        
        if (currentAmount < amount) {
            return false;
        }
        
        keys.put(tier, currentAmount - amount);
        playerKeys.put(playerUUID, keys);
        return true;
    }
    
    /**
     * Create an ItemStack representing a mount key
     * 
     * @param tier The key tier
     * @return The ItemStack
     */
    public ItemStack createKeyItem(KeyTier tier) {
        ItemStack item = new ItemStack(tier.getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName(tier.getColor() + tier.getDisplayName());
            
            // Add lore
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Use this key to open a");
            lore.add(ChatColor.GRAY + "Mount Chest for a chance");
            lore.add(ChatColor.GRAY + "to receive a special mount.");
            lore.add("");
            
            // Add drop chances info
            lore.add(ChatColor.YELLOW + "Drop Chances:");
            for (Map.Entry<MountRarity, Double> entry : tier.getRarityDropChances().entrySet()) {
                if (entry.getValue() > 0) {
                    lore.add(entry.getKey().getColor() + " " + entry.getKey().name() + ": " + 
                            ChatColor.WHITE + entry.getValue() + "%");
                }
            }
            
            meta.setLore(lore);
            
            // Store key type in persistent data
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(keyTypeKey, PersistentDataType.STRING, tier.name());
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Get the key tier from an ItemStack
     * 
     * @param item The ItemStack
     * @return The key tier, or null if not a key
     */
    public KeyTier getKeyTier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(keyTypeKey, PersistentDataType.STRING)) {
            return null;
        }
        
        String tierName = container.get(keyTypeKey, PersistentDataType.STRING);
        if (tierName == null) {
            return null;
        }
        
        try {
            return KeyTier.valueOf(tierName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Give a physical key item to a player
     * 
     * @param player The player
     * @param tier The key tier
     * @param amount The amount
     */
    public void giveKeyItem(Player player, KeyTier tier, int amount) {
        ItemStack keyItem = createKeyItem(tier);
        keyItem.setAmount(amount);
        
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(keyItem);
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }
        
        player.sendMessage(ChatColor.GREEN + "You received " + amount + "x " + 
                tier.getFormattedName() + ChatColor.GREEN + "!");
    }
    
    /**
     * Load or initialize keys for a player
     * 
     * @param playerUUID The player's UUID
     */
    public void loadPlayerKeysData(UUID playerUUID) {
        if (!playerKeys.containsKey(playerUUID)) {
            Map<KeyTier, Integer> keys = new HashMap<>();
            for (KeyTier tier : KeyTier.values()) {
                keys.put(tier, 0);
            }
            playerKeys.put(playerUUID, keys);
        }
    }
    
    /**
     * Get the drop rate for a specific mount and key tier
     * 
     * @param tier The key tier
     * @param mountId The mount ID
     * @return The drop rate percentage
     */
    public double getMountDropRate(KeyTier tier, String mountId) {
        Map<String, Double> tierRates = mountDropRates.get(tier);
        if (tierRates == null) {
            return 0.0;
        }
        return tierRates.getOrDefault(mountId, 0.0);
    }
    
    /**
     * Set the drop rate for a specific mount and key tier
     * 
     * @param tier The key tier
     * @param mountId The mount ID
     * @param rate The drop rate percentage
     */
    public void setMountDropRate(KeyTier tier, String mountId, double rate) {
        Map<String, Double> tierRates = mountDropRates.computeIfAbsent(tier, k -> new HashMap<>());
        tierRates.put(mountId, rate);
    }
    
    /**
     * Roll for a random mount type based on key tier
     * 
     * @param tier The key tier
     * @return The mount ID
     */
    public String rollMountType(KeyTier tier) {
        Map<String, Double> tierRates = mountDropRates.get(tier);
        if (tierRates == null || tierRates.isEmpty()) {
            // Default to phoenix if no rates are defined
            return "phoenix_blaze";
        }
        
        double totalChance = tierRates.values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = Math.random() * totalChance;
        double cumulativeChance = 0.0;
        
        for (Map.Entry<String, Double> entry : tierRates.entrySet()) {
            cumulativeChance += entry.getValue();
            if (roll < cumulativeChance) {
                return entry.getKey();
            }
        }
        
        // Default fallback
        return tierRates.keySet().iterator().next();
    }
    
    /**
     * Get a KeyTier from its name
     * 
     * @param name The name of the key tier
     * @return The KeyTier enum value, or null if not found
     */
    public KeyTier getKeyTierByName(String name) {
        try {
            return KeyTier.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
} 