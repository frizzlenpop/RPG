package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
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

public class FightingListener implements Listener {

    private final XPManager xpManager;
    private final PassiveSkillManager passiveManager;
    private final RPGSkillsPlugin plugin;
    private final Random random = new Random();
    
    // Map to store player's low health status to avoid multiple Berserker activations
    private final Map<UUID, Long> berserkerCooldowns = new HashMap<>();
    private final Map<UUID, Long> battleRageCooldowns = new HashMap<>();
    
    // Cooldown times (in milliseconds)
    private static final long BERSERKER_COOLDOWN = 30000; // 30 seconds
    private static final long BATTLE_RAGE_COOLDOWN = 15000; // 15 seconds
    
    // Constructor updated to include PassiveSkillManager and plugin
    public FightingListener(XPManager xpManager, RPGSkillsPlugin plugin, PassiveSkillManager passiveManager) {
        this.xpManager = xpManager;
        this.plugin = plugin;
        this.passiveManager = passiveManager;
        System.out.println("[RPGSkills] FightingListener initialized");
    }
    
    /**
     * Handle entity death events - award XP and trigger kill-based passives
     */
    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        Player killer = event.getEntity().getKiller();

        if (killer == null) {
            return;
        }
        
        UUID playerId = killer.getUniqueId();
        String entityName = entity.getType().toString();
        
        // Get XP for the killed mob
        int xpGained = xpManager.getXPForMob(entityName);
        
        // Apply Combat XP boosts
        if (passiveManager.hasPassive(playerId, "combatXpBoost5")) {
            xpGained *= 1.3; // +30% XP
        } else if (passiveManager.hasPassive(playerId, "combatXpBoost4")) {
            xpGained *= 1.25; // +25% XP
        } else if (passiveManager.hasPassive(playerId, "combatXpBoost3")) {
            xpGained *= 1.2; // +20% XP
        } else if (passiveManager.hasPassive(playerId, "combatXpBoost2")) {
            xpGained *= 1.15; // +15% XP
        } else if (passiveManager.hasPassive(playerId, "combatXpBoost1")) {
            xpGained *= 1.1; // +10% XP
        }
        
        // Apply Combat Basics passive - small chance for extra XP
        if (passiveManager.hasPassive(playerId, "combatBasics") && random.nextDouble() < 0.15) {
            xpGained = (int)(xpGained * 1.05); // 5% more XP with 15% chance
            killer.sendActionBar("§4Your combat basics gave you bonus XP!");
        }
        
        if (xpGained > 0) {
            xpManager.addXP(killer, "fighting", xpGained);
            killer.sendMessage("§c+" + xpGained + " Fighting XP");
        }
        
        // Apply Heal on Kill passives
        applyHealOnKill(killer);
        
        // Apply Battle Rage passive
        if (passiveManager.hasPassive(playerId, "battleRage")) {
            long currentTime = System.currentTimeMillis();
            if (!battleRageCooldowns.containsKey(playerId) || 
                currentTime - battleRageCooldowns.get(playerId) > BATTLE_RAGE_COOLDOWN) {
                
                // Grant Strength I for 5 seconds
                killer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 5 * 20, 0));
                killer.sendActionBar("§4Battle Rage activated! +Strength for 5 seconds");
                
                // Set cooldown
                battleRageCooldowns.put(playerId, currentTime);
            }
        }
        
        // Apply Combat Master and Master Fighter bonuses on kill
        if (passiveManager.hasPassive(playerId, "masterFighter")) {
            // The ultimate fighting skill - combination of multiple effects
            killer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 8 * 20, 0)); // Strength I for 8 seconds
            killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 0)); // Regeneration I for 5 seconds
            killer.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 5 * 20, 0)); // Resistance I for 5 seconds
            
            killer.sendActionBar("§4✦ Master Fighter powers activated! ✦");
        }
        else if (passiveManager.hasPassive(playerId, "combatMaster")) {
            // Combat Master - lesser version of Master Fighter
            killer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 5 * 20, 0)); // Strength I for 5 seconds
            killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 3 * 20, 0)); // Regeneration I for 3 seconds
            
            killer.sendActionBar("§4Combat Master activated!");
        }
    }
    
    /**
     * Handle damage dealt by players - apply weapon specialization and critical strike passives
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDamageEntity(EntityDamageByEntityEvent event) {
        // Get the player who dealt damage
        Player damager = null;
        boolean isBowAttack = false;
        
        // Handle direct damage and projectile damage (bow)
        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                damager = (Player) arrow.getShooter();
                isBowAttack = true;
            }
        }
        
        if (damager == null) {
            return;
        }
        
        UUID playerId = damager.getUniqueId();
        ItemStack weapon = damager.getInventory().getItemInMainHand();
        String weaponType = getWeaponType(weapon, isBowAttack);
        
        // Calculate base damage modifier from weapon specialization passives
        double damageModifier = calculateDamageModifier(playerId, weaponType);
        
        // Apply critical strike chance passives
        boolean isCriticalHit = applyCriticalStrike(playerId);
        if (isCriticalHit) {
            damageModifier += 0.5; // +50% damage on critical hit
            damager.sendActionBar("§c⚔ Critical Strike! ⚔");
        }
        
        // Apply Legendary Warrior passive (25% increased damage with all weapons)
        if (passiveManager.hasPassive(playerId, "legendaryWarrior")) {
            damageModifier += 0.25;
        }
        
        // Apply Combat Master passive (10% increased damage with all weapons)
        if (passiveManager.hasPassive(playerId, "combatMaster")) {
            damageModifier += 0.1;
        }
        
        // Apply final damage
        if (damageModifier > 0) {
            // Adjust damage using the modifier
            double originalDamage = event.getDamage();
            double newDamage = originalDamage * (1 + damageModifier);
            event.setDamage(newDamage);
            
            // Show message for significant damage increases only
            if (newDamage - originalDamage > 1.0 && !isCriticalHit) {
                damager.sendActionBar("§c⚔ +" + (int)(damageModifier * 100) + "% Damage! ⚔");
            }
        }
        
        // Apply Lifesteal passives
        applyLifesteal(damager, event.getFinalDamage());
        
        // Check for Berserker passive when player is at low health (below 3 hearts)
        checkBerserkerPassive(damager);
    }
    
    /**
     * Handle damage taken by players - apply damage reduction and knockback resistance
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        UUID playerId = player.getUniqueId();
        
        // Apply First Aid passive (better regeneration)
        if (passiveManager.hasPassive(playerId, "firstAid") && random.nextDouble() < 0.10) {
            // 10% chance to give regeneration effect
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 4 * 20, 0)); // Regen I for 4 seconds
            player.sendActionBar("§c❤ First Aid is helping you recover! ❤");
        }
        
        // Apply Knockback Resistance passive
        if (passiveManager.hasPassive(playerId, "knockbackResistance")) {
            // Apply knockback resistance attribute if not already applied
            AttributeInstance knockbackAttribute = player.getAttribute(Attribute.valueOf("GENERIC_KNOCKBACK_RESISTANCE"));
            if (knockbackAttribute != null && knockbackAttribute.getBaseValue() < 0.15) {
                knockbackAttribute.setBaseValue(0.15); // 15% knockback resistance
            }
        }
        
        // Apply Damage Reduction passives
        double damageReduction = 0;
        
        if (passiveManager.hasPassive(playerId, "damageReduction4")) {
            damageReduction = 0.20; // 20% damage reduction
        } else if (passiveManager.hasPassive(playerId, "damageReduction3")) {
            damageReduction = 0.15; // 15% damage reduction
        } else if (passiveManager.hasPassive(playerId, "damageReduction2")) {
            damageReduction = 0.10; // 10% damage reduction
        } else if (passiveManager.hasPassive(playerId, "damageReduction1")) {
            damageReduction = 0.05; // 5% damage reduction
        }
        
        // Apply Combat Master and Master Fighter passives
        if (passiveManager.hasPassive(playerId, "masterFighter")) {
            damageReduction += 0.10; // Additional 10% reduction
        } else if (passiveManager.hasPassive(playerId, "combatMaster")) {
            damageReduction += 0.05; // Additional 5% reduction
        }
        
        // Apply damage reduction if applicable
        if (damageReduction > 0) {
            double originalDamage = event.getDamage();
            double reducedDamage = originalDamage * (1 - damageReduction);
            event.setDamage(reducedDamage);
            
            if (originalDamage - reducedDamage > 0.5) {
                player.sendActionBar("§9🛡 Reduced damage by " + (int)(damageReduction * 100) + "%");
            }
        }
        
        // Check for Berserker passive when player is hit and at low health
        checkBerserkerPassive(player);
    }
    
    /**
     * Check and apply the Berserker passive when a player is at low health
     */
    private void checkBerserkerPassive(Player player) {
        UUID playerId = player.getUniqueId();
        
        // If player has Berserker passive and is below 3 hearts (6 health points)
        if (passiveManager.hasPassive(playerId, "berserker") && player.getHealth() <= 6.0) {
            long currentTime = System.currentTimeMillis();
            
            // Check if the cooldown has expired
            if (!berserkerCooldowns.containsKey(playerId) || 
                currentTime - berserkerCooldowns.get(playerId) > BERSERKER_COOLDOWN) {
                
                // Apply Berserker effects: Strength II and Speed I for 10 seconds
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 10 * 20, 1)); // Strength II
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 0)); // Speed I
                
                player.sendActionBar("§4§l⚡ BERSERKER ACTIVATED! ⚡");
                player.getWorld().playSound(player.getLocation(), 
                                          org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
                
                // Set cooldown
                berserkerCooldowns.put(playerId, currentTime);
            }
        }
    }
    
    /**
     * Apply Heal on Kill passive based on player's level
     */
    private void applyHealOnKill(Player player) {
        UUID playerId = player.getUniqueId();
        double healAmount = 0;
        
        // Determine heal amount based on highest level passive
        if (passiveManager.hasPassive(playerId, "healOnKill4")) {
            healAmount = 8.0; // Heal 4 hearts
        } else if (passiveManager.hasPassive(playerId, "healOnKill3")) {
            healAmount = 6.0; // Heal 3 hearts
        } else if (passiveManager.hasPassive(playerId, "healOnKill2")) {
            healAmount = 4.0; // Heal 2 hearts
        } else if (passiveManager.hasPassive(playerId, "healOnKill1")) {
            healAmount = 2.0; // Heal 1 heart
        }
        
        if (healAmount > 0) {
            // Only heal if not at full health
            double currentHealth = player.getHealth();
            double maxHealth = player.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH")).getValue();
            
            if (currentHealth < maxHealth) {
                double newHealth = Math.min(currentHealth + healAmount, maxHealth);
                player.setHealth(newHealth);
                player.sendActionBar("§c❤ Healed " + (int)(healAmount/2) + " hearts from kill! ❤");
            }
        }
    }
    
    /**
     * Apply Lifesteal passive based on damage dealt
     */
    private void applyLifesteal(Player player, double damage) {
        UUID playerId = player.getUniqueId();
        double chance = 0;
        
        // Determine lifesteal chance based on highest level passive
        if (passiveManager.hasPassive(playerId, "lifesteal4")) {
            chance = 0.10; // 10% chance
        } else if (passiveManager.hasPassive(playerId, "lifesteal3")) {
            chance = 0.07; // 7% chance
        } else if (passiveManager.hasPassive(playerId, "lifesteal2")) {
            chance = 0.05; // 5% chance
        } else if (passiveManager.hasPassive(playerId, "lifesteal1")) {
            chance = 0.03; // 3% chance
        }
        
        if (chance > 0 && random.nextDouble() < chance) {
            // Calculate heal amount (25% of damage dealt)
            double healAmount = damage * 0.25;
            
            // Only heal if not at full health and heal amount is significant
            double currentHealth = player.getHealth();
            double maxHealth = player.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH")).getValue();
            
            if (currentHealth < maxHealth && healAmount > 0.5) {
                double newHealth = Math.min(currentHealth + healAmount, maxHealth);
                player.setHealth(newHealth);
                player.sendActionBar("§c❤ Lifesteal healed " + String.format("%.1f", healAmount/2) + " hearts! ❤");
            }
        }
    }
    
    /**
     * Apply Critical Strike passive - returns true if a critical hit occurs
     */
    private boolean applyCriticalStrike(UUID playerId) {
        double chance = 0;
        
        // Determine critical chance based on highest level passive
        if (passiveManager.hasPassive(playerId, "criticalStrike4")) {
            chance = 0.20; // 20% chance
        } else if (passiveManager.hasPassive(playerId, "criticalStrike3")) {
            chance = 0.15; // 15% chance
        } else if (passiveManager.hasPassive(playerId, "criticalStrike2")) {
            chance = 0.10; // 10% chance
        } else if (passiveManager.hasPassive(playerId, "criticalStrike1")) {
            chance = 0.05; // 5% chance
        }
        
        return chance > 0 && random.nextDouble() < chance;
    }
    
    /**
     * Calculate damage modifier based on weapon specialization passives
     */
    private double calculateDamageModifier(UUID playerId, String weaponType) {
        double modifier = 0;
        
        if (weaponType.equals("sword")) {
            // Apply Sword Specialist passive
            if (passiveManager.hasPassive(playerId, "swordSpecialist4")) {
                modifier = 0.20; // 20% increase
            } else if (passiveManager.hasPassive(playerId, "swordSpecialist3")) {
                modifier = 0.15; // 15% increase
            } else if (passiveManager.hasPassive(playerId, "swordSpecialist2")) {
                modifier = 0.10; // 10% increase
            } else if (passiveManager.hasPassive(playerId, "swordSpecialist1")) {
                modifier = 0.05; // 5% increase
            }
        } else if (weaponType.equals("axe")) {
            // Apply Axe Specialist passive
            if (passiveManager.hasPassive(playerId, "axeSpecialist4")) {
                modifier = 0.20; // 20% increase
            } else if (passiveManager.hasPassive(playerId, "axeSpecialist3")) {
                modifier = 0.15; // 15% increase
            } else if (passiveManager.hasPassive(playerId, "axeSpecialist2")) {
                modifier = 0.10; // 10% increase
            } else if (passiveManager.hasPassive(playerId, "axeSpecialist1")) {
                modifier = 0.05; // 5% increase
            }
        } else if (weaponType.equals("bow")) {
            // Apply Bow Specialist passive
            if (passiveManager.hasPassive(playerId, "bowSpecialist4")) {
                modifier = 0.20; // 20% increase
            } else if (passiveManager.hasPassive(playerId, "bowSpecialist3")) {
                modifier = 0.15; // 15% increase
            } else if (passiveManager.hasPassive(playerId, "bowSpecialist2")) {
                modifier = 0.10; // 10% increase
            } else if (passiveManager.hasPassive(playerId, "bowSpecialist1")) {
                modifier = 0.05; // 5% increase
            }
        }
        
        return modifier;
    }
    
    /**
     * Determine weapon type from item in hand
     */
    private String getWeaponType(ItemStack item, boolean isBowAttack) {
        if (isBowAttack) {
            return "bow";
        }
        
        if (item == null || item.getType().isAir()) {
            return "fist";
        }
        
        String itemName = item.getType().toString().toLowerCase();
        
        if (itemName.contains("sword")) {
            return "sword";
        } else if (itemName.contains("axe")) {
            return "axe";
        } else if (itemName.contains("bow")) {
            return "bow";
        } else if (itemName.contains("trident")) {
            return "trident";
        } else {
            return "other";
        }
    }
}
