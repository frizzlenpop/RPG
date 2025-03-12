package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.skills.PassiveSkillManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class FishingListener implements Listener {

    private final XPManager xpManager;
    private final PassiveSkillManager passiveManager;
    private final RPGSkillsPlugin plugin;
    private final Random random = new Random();
    
    // Maps to track cooldowns for certain passives
    private final Map<UUID, Long> waterBreathingCooldowns = new HashMap<>();
    private final Map<UUID, Long> nightFisherCooldowns = new HashMap<>();
    private final Map<UUID, Long> rainFisherCooldowns = new HashMap<>();
    
    // Cooldown times (in milliseconds)
    private static final long WATER_BREATHING_COOLDOWN = 180000; // 3 minutes
    private static final long NIGHT_FISHER_COOLDOWN = 60000; // 1 minute
    private static final long RAIN_FISHER_COOLDOWN = 60000; // 1 minute

    public FishingListener(XPManager xpManager, RPGSkillsPlugin plugin, PassiveSkillManager passiveManager) {
        this.xpManager = xpManager;
        this.plugin = plugin;
        this.passiveManager = passiveManager;
        System.out.println("[RPGSkills] FishingListener initialized");
    }

    @EventHandler
    public void onFishCatch(PlayerFishEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Implement water breathing passive (level 30) whenever player is fishing
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            checkWaterBreathingPassive(player);
            // Check time-based passives when fishing starts
            checkNightFisherPassive(player);
            checkRainFisherPassive(player);
        }
        
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Entity caught = event.getCaught();
            
            if (caught == null) {
                return;
            }
            
            if (caught instanceof Item) {
                Item itemCaught = (Item) caught;
                ItemStack itemStack = itemCaught.getItemStack();
                String itemType = itemStack.getType().toString();
                
                // Handle fish type specializations (salmon, tropical fish)
                handleFishTypeSpecialization(player, itemCaught);
                
                // Handle extra catch chance (double/triple catch)
                handleExtraCatchChance(player, itemStack, event.getHook().getLocation());
                
                // Handle junk reducer passive
                handleJunkReducer(player, itemCaught);
                
                // Get XP for the caught fish
                int xpGained = xpManager.getXPForFish(itemType);
                
                if (xpGained > 0) {
                    xpManager.addXP(player, "fishing", xpGained);
                }
            }
        }
    }
    
    /**
     * Checks and applies the Water Breathing passive effect
     * @param player The player to check for the passive
     */
    private void checkWaterBreathingPassive(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (passiveManager.hasPassive(player, "fishing", "Water Breathing")) {
            long currentTime = System.currentTimeMillis();
            
            // Check cooldown
            if (!waterBreathingCooldowns.containsKey(playerId) || 
                currentTime - waterBreathingCooldowns.get(playerId) > WATER_BREATHING_COOLDOWN) {
                
                // Apply water breathing effect for 3 minutes
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 3600, 0));
                player.sendActionBar("§b~ Water Breathing passive activated ~");
                
                // Set cooldown
                waterBreathingCooldowns.put(playerId, currentTime);
            }
        }
    }
    
    /**
     * Checks for the Night Fisher passive (increased catch rate at night)
     * @param player The player to check for the passive
     */
    private void checkNightFisherPassive(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (passiveManager.hasPassive(player, "fishing", "Night Fisher")) {
            long currentTime = System.currentTimeMillis();
            World world = player.getWorld();
            long worldTime = world.getTime();
            
            // Check if it's night time (13000-23000)
            if (worldTime >= 13000 && worldTime <= 23000) {
                // Check cooldown
                if (!nightFisherCooldowns.containsKey(playerId) || 
                    currentTime - nightFisherCooldowns.get(playerId) > NIGHT_FISHER_COOLDOWN) {
                    
                    player.sendActionBar("§9✨ Night Fisher passive is active! 25% better catches at night");
                    
                    // Set cooldown for the message
                    nightFisherCooldowns.put(playerId, currentTime);
                }
            }
        }
    }
    
    /**
     * Checks for the Rain Fisher passive (increased catch rate during rain)
     * @param player The player to check for the passive
     */
    private void checkRainFisherPassive(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (passiveManager.hasPassive(player, "fishing", "Rain Fisher")) {
            long currentTime = System.currentTimeMillis();
            World world = player.getWorld();
            
            // Check if it's raining
            if (world.hasStorm()) {
                // Check cooldown
                if (!rainFisherCooldowns.containsKey(playerId) || 
                    currentTime - rainFisherCooldowns.get(playerId) > RAIN_FISHER_COOLDOWN) {
                    
                    player.sendActionBar("§b☔ Rain Fisher passive is active! 25% better catches in rain");
                    
                    // Set cooldown for the message
                    rainFisherCooldowns.put(playerId, currentTime);
                }
            }
        }
    }
    
    /**
     * Handles fish type specialization passives
     * @param player The player to check for passives
     * @param caughtItem The caught item to potentially modify
     */
    private void handleFishTypeSpecialization(Player player, Item caughtItem) {
        ItemStack itemStack = caughtItem.getItemStack();
        
        // Salmon Specialist (level 12)
        if (passiveManager.hasPassive(player, "fishing", "Salmon Specialist") && 
            itemStack.getType() == Material.SALMON) {
            
            // Increase the amount of salmon caught by 15%
            if (random.nextDouble() < 0.15) {
                itemStack.setAmount(itemStack.getAmount() + 1);
                player.sendActionBar("§c♨ Salmon Specialist: Extra salmon!");
            }
        }
        
        // Tropical Fish Specialist (level 17)
        if (passiveManager.hasPassive(player, "fishing", "Tropical Fish Specialist") && 
            itemStack.getType() == Material.TROPICAL_FISH) {
            
            // Increase the amount of tropical fish caught by 15%
            if (random.nextDouble() < 0.15) {
                itemStack.setAmount(itemStack.getAmount() + 1);
                player.sendActionBar("§d♨ Tropical Fish Specialist: Extra tropical fish!");
            }
        }
        
        // Rare Fish Master I (level 15)
        if (passiveManager.hasPassive(player, "fishing", "Rare Fish Master I") && 
            (itemStack.getType() == Material.COD || itemStack.getType() == Material.SALMON)) {
            
            // 5% chance to convert to rare fish
            if (random.nextDouble() < 0.05) {
                if (random.nextBoolean()) {
                    itemStack.setType(Material.PUFFERFISH);
                } else {
                    itemStack.setType(Material.TROPICAL_FISH);
                }
                player.sendActionBar("§e⭐ Rare Fish Master I: Caught a rare fish!");
            }
        }
        
        // Rare Fish Master II (level 40)
        if (passiveManager.hasPassive(player, "fishing", "Rare Fish Master II") && 
            (itemStack.getType() == Material.COD || itemStack.getType() == Material.SALMON)) {
            
            // 10% chance to convert to rare fish
            if (random.nextDouble() < 0.10) {
                if (random.nextBoolean()) {
                    itemStack.setType(Material.PUFFERFISH);
                } else {
                    itemStack.setType(Material.TROPICAL_FISH);
                }
                player.sendActionBar("§e⭐⭐ Rare Fish Master II: Caught a rare fish!");
            }
        }
    }
    
    /**
     * Handles extra catch chance (double or triple fish)
     * @param player The player to check for passives
     * @param originalCatch The originally caught item
     * @param location The location where to drop extra items
     */
    private void handleExtraCatchChance(Player player, ItemStack originalCatch, Location location) {
        // Only process for fish types
        if (!isFish(originalCatch.getType())) {
            return;
        }
        
        // Double Catch I (level 32)
        if (passiveManager.hasPassive(player, "fishing", "Double Catch I")) {
            if (random.nextDouble() < 0.10) { // 10% chance
                ItemStack extraItem = originalCatch.clone();
                location.getWorld().dropItemNaturally(location, extraItem);
                player.sendActionBar("§a✧ Double Catch I: You caught two fish at once!");
            }
        }
        // Double Catch II (level 52)
        else if (passiveManager.hasPassive(player, "fishing", "Double Catch II")) {
            if (random.nextDouble() < 0.15) { // 15% chance
                ItemStack extraItem = originalCatch.clone();
                location.getWorld().dropItemNaturally(location, extraItem);
                player.sendActionBar("§a✧✧ Double Catch II: You caught two fish at once!");
            }
        }
        // Double Catch III (level 75)
        else if (passiveManager.hasPassive(player, "fishing", "Double Catch III")) {
            if (random.nextDouble() < 0.20) { // 20% chance
                ItemStack extraItem = originalCatch.clone();
                location.getWorld().dropItemNaturally(location, extraItem);
                player.sendActionBar("§a✧✧✧ Double Catch III: You caught two fish at once!");
            }
        }
        
        // Triple Catch (level 90)
        if (passiveManager.hasPassive(player, "fishing", "Triple Catch")) {
            if (random.nextDouble() < 0.05) { // 5% chance
                for (int i = 0; i < 2; i++) { // Drop 2 extra items
                    ItemStack extraItem = originalCatch.clone();
                    location.getWorld().dropItemNaturally(location, extraItem);
                }
                player.sendActionBar("§6★★★ Triple Catch: You caught three fish at once!");
            }
        }
    }
    
    /**
     * Handles junk reducer passive
     * @param player The player to check for passives
     * @param caughtItem The caught item to potentially modify
     */
    private void handleJunkReducer(Player player, Item caughtItem) {
        ItemStack itemStack = caughtItem.getItemStack();
        
        // Don't process if it's already a fish
        if (isFish(itemStack.getType())) {
            return;
        }
        
        // Check for junk items - things that aren't fish, treasure, or enchanted items
        boolean isJunk = !isValuedItem(itemStack.getType()) && !isEnchanted(itemStack);
        
        if (isJunk) {
            double junkReductionChance = 0.0;
            
            // Junk Reducer I (level 25) - 15% less junk
            if (passiveManager.hasPassive(player, "fishing", "Junk Reducer I")) {
                junkReductionChance = 0.15;
            }
            // Junk Reducer II (level 45) - 25% less junk
            else if (passiveManager.hasPassive(player, "fishing", "Junk Reducer II")) {
                junkReductionChance = 0.25;
            }
            // Junk Reducer III (level 62) - 35% less junk
            else if (passiveManager.hasPassive(player, "fishing", "Junk Reducer III")) {
                junkReductionChance = 0.35;
            }
            // Junk Reducer IV (level 82) - 50% less junk
            else if (passiveManager.hasPassive(player, "fishing", "Junk Reducer IV")) {
                junkReductionChance = 0.50;
            }
            
            if (junkReductionChance > 0.0 && random.nextDouble() < junkReductionChance) {
                // Replace junk with a random fish
                Material[] fishTypes = {Material.COD, Material.SALMON, Material.PUFFERFISH, Material.TROPICAL_FISH};
                Material randomFish = fishTypes[random.nextInt(fishTypes.length)];
                itemStack.setType(randomFish);
                player.sendActionBar("§a♻ Junk Reducer converted junk to fish!");
            }
        }
    }
    
    /**
     * Checks if a material is a fish
     * @param material The material to check
     * @return true if the material is a fish, false otherwise
     */
    private boolean isFish(Material material) {
        return material == Material.COD || 
               material == Material.SALMON || 
               material == Material.TROPICAL_FISH || 
               material == Material.PUFFERFISH;
    }
    
    /**
     * Checks if a material is a valued item (treasure)
     * @param material The material to check
     * @return true if the material is a valued item, false otherwise
     */
    private boolean isValuedItem(Material material) {
        return material == Material.NAME_TAG || 
               material == Material.SADDLE || 
               material == Material.NAUTILUS_SHELL || 
               material == Material.ENCHANTED_BOOK || 
               material == Material.BOW || 
               material == Material.FISHING_ROD || 
               material == Material.HEART_OF_THE_SEA || 
               material == Material.TRIDENT;
    }
    
    /**
     * Checks if an item is enchanted
     * @param item The item to check
     * @return true if the item is enchanted, false otherwise
     */
    private boolean isEnchanted(ItemStack item) {
        return item.hasItemMeta() && item.getItemMeta().hasEnchants();
    }
}
