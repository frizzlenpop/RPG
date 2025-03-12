package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.items.CustomEnchantScroll;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

import java.util.*;
import java.util.logging.Level;

public class EnchantingListener implements Listener {
    private final XPManager xpManager;
    private final RPGSkillsPlugin plugin;
    private final Map<Enchantment, Integer> enchantmentRarity;
    private final Random random = new Random();
    private final CustomEnchantScroll customEnchantScroll;

    public EnchantingListener(XPManager xpManager, RPGSkillsPlugin plugin) {
        this.xpManager = xpManager;
        this.plugin = plugin;
        this.enchantmentRarity = initializeEnchantmentRarity();
        this.customEnchantScroll = new CustomEnchantScroll(plugin);
        plugin.getLogger().info("[RPGSkills] EnchantingListener has been initialized");
    }

    private Map<Enchantment, Integer> initializeEnchantmentRarity() {
        Map<Enchantment, Integer> rarity = new HashMap<>();

        // Common enchantments (1) - Basic utility and common combat enchantments
        rarity.put(Enchantment.UNBREAKING, 1);
        rarity.put(Enchantment.PROTECTION, 1); // Protection
        rarity.put(Enchantment.INFINITY, 1); // Arrow Infinity
        rarity.put(Enchantment.EFFICIENCY, 1); // Dig Speed
        rarity.put(Enchantment.RESPIRATION, 1); // Oxygen
        rarity.put(Enchantment.AQUA_AFFINITY, 1); // Water Worker
        rarity.put(Enchantment.SHARPNESS, 1); // Damage All
        rarity.put(Enchantment.POWER, 1); // Arrow Damage
        rarity.put(Enchantment.PROJECTILE_PROTECTION, 1);
        rarity.put(Enchantment.FIRE_PROTECTION, 1);
        rarity.put(Enchantment.BLAST_PROTECTION, 1);
        rarity.put(Enchantment.SWIFT_SNEAK, 1);

        // Uncommon enchantments (2) - More specialized combat and utility
        rarity.put(Enchantment.FORTUNE, 2); // Fortune
        rarity.put(Enchantment.LOOTING, 2); // Looting
        rarity.put(Enchantment.BANE_OF_ARTHROPODS, 2);
        rarity.put(Enchantment.SMITE, 2);
        rarity.put(Enchantment.PUNCH, 2); // Arrow Knockback
        rarity.put(Enchantment.KNOCKBACK, 2);
        rarity.put(Enchantment.THORNS, 2);
        rarity.put(Enchantment.DEPTH_STRIDER, 2);
        rarity.put(Enchantment.FROST_WALKER, 2);
        rarity.put(Enchantment.BINDING_CURSE, 2);
        rarity.put(Enchantment.SOUL_SPEED, 2);

        // Rare enchantments (3) - Powerful effects that significantly change gameplay
        rarity.put(Enchantment.MENDING, 3);
        rarity.put(Enchantment.SILK_TOUCH, 3);
        rarity.put(Enchantment.SWEEPING_EDGE, 3);
        rarity.put(Enchantment.MULTISHOT, 3);
        rarity.put(Enchantment.QUICK_CHARGE, 3);
        rarity.put(Enchantment.PIERCING, 3);
        rarity.put(Enchantment.LOYALTY, 3);
        rarity.put(Enchantment.IMPALING, 3);
        rarity.put(Enchantment.RIPTIDE, 3);
        rarity.put(Enchantment.LUCK_OF_THE_SEA, 3); // Luck
        rarity.put(Enchantment.LURE, 3);

        // Very Rare enchantments (4) - Game-changing abilities
        rarity.put(Enchantment.CHANNELING, 4);
        rarity.put(Enchantment.FLAME, 4);
        rarity.put(Enchantment.FIRE_ASPECT, 4); // Arrow Fire

        // Legendary enchantments (5) - The most powerful and sought-after
        rarity.put(Enchantment.VANISHING_CURSE, 5);

        return rarity;
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        plugin.getLogger().info("[RPGSkills] EnchantItemEvent triggered!");
        
        Player player = event.getEnchanter();
        int expLevelCost = event.getExpLevelCost();
        Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();
        
        plugin.getLogger().info("[RPGSkills] Player: " + player.getName() + 
                              ", ExpCost: " + expLevelCost + 
                              ", Enchants: " + enchants.size());
        
        // Log each enchantment being added
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            plugin.getLogger().info("[RPGSkills] - Adding " + entry.getKey().getKey() + 
                                  " level " + entry.getValue());
        }
        
        // Calculate XP based on level cost and number of enchantments
        int baseXp = expLevelCost * 10; // Base XP is 10 times the level cost
        int bonusXp = enchants.size() * 5; // Additional 5 XP per enchantment
        int totalXp = baseXp + bonusXp;
        
        plugin.getLogger().info("[RPGSkills] Calculated XP: base=" + baseXp + 
                              ", bonus=" + bonusXp + 
                              ", total=" + totalXp);
        
        try {
            // Award the enchanting XP
            xpManager.addXP(player, "enchanting", totalXp);
            plugin.getLogger().info("[RPGSkills] Successfully added " + totalXp + " enchanting XP to " + player.getName());
            player.sendMessage("§6+" + totalXp + " Enchanting XP");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[RPGSkills] Error adding XP: " + e.getMessage(), e);
        }
        
        // Calculate chance for enchantment scroll
        // Base chance of 5% + up to additional 15% based on exp cost
        double scrollChance = 5.0 + (expLevelCost * 0.5);
        
        // Increase chance based on rarity of enchantments added
        int highestRarity = 0;
        for (Enchantment enchant : enchants.keySet()) {
            int rarity = enchantmentRarity.getOrDefault(enchant, 1);
            if (rarity > highestRarity) {
                highestRarity = rarity;
            }
        }
        
        // Add 2% per rarity level of the highest enchantment
        scrollChance += (highestRarity * 2.0);
        
        // Cap the chance at 40%
        scrollChance = Math.min(scrollChance, 40.0);
        
        plugin.getLogger().info("[RPGSkills] Scroll chance: " + scrollChance + "%");
        
        // Roll for scroll with the calculated chance
        if (random.nextDouble() * 100 < scrollChance) {
            // Weighted selection based on the highest rarity enchantment present
            Enchantment scrollEnchant = getRandomEnchantmentWeighted(highestRarity);
            customEnchantScroll.giveUnknownScroll(player, scrollEnchant);
            plugin.getLogger().info("[RPGSkills] Gave " + player.getName() + " a scroll with " + scrollEnchant.getKey());
            player.sendMessage("§6You found an unknown enchantment scroll!");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Allow placing scrolls back into inventory
        if (CustomEnchantScroll.isIdentifiedScroll(cursor)) {
            // If clicking into an empty slot or onto another item, allow the normal inventory behavior
            if (current == null || current.getType() == Material.AIR) {
                return; // Don't cancel the event, let the item be placed
            }

            // If clicking onto an enchantable item, try to apply the enchantment
            event.setCancelled(true);
            
            // Try to apply the scroll to the item
            if (customEnchantScroll.applyScrollToItem(cursor, current, player)) {
                // Remove one scroll from the stack on success
                if (cursor.getAmount() > 1) {
                    cursor.setAmount(cursor.getAmount() - 1);
                } else {
                    event.getView().setCursor(null);
                }
            }
        }

        // Check if trying to identify an unknown scroll
        else if (CustomEnchantScroll.isUnknownScroll(current) && event.getClick().isRightClick()) {
            event.setCancelled(true);
            customEnchantScroll.identifyScroll(current, player);
        }
    }

    private Enchantment getRandomEnchantment() {
        List<Enchantment> enchants = new ArrayList<>(enchantmentRarity.keySet());
        return enchants.get(random.nextInt(enchants.size()));
    }
    
    private Enchantment getRandomEnchantmentWeighted(int targetRarity) {
        // Group enchantments by rarity
        Map<Integer, List<Enchantment>> enchantsByRarity = new HashMap<>();
        
        for (Map.Entry<Enchantment, Integer> entry : enchantmentRarity.entrySet()) {
            int rarity = entry.getValue();
            enchantsByRarity.computeIfAbsent(rarity, k -> new ArrayList<>()).add(entry.getKey());
        }
        
        // Determine which rarity group to pick from based on the target rarity
        // Higher chance to pick from the target rarity group, but can also pick from adjacent groups
        int finalRarity = targetRarity;
        
        // 60% chance to get target rarity, 20% chance to get one lower, 20% chance to get one higher
        double roll = random.nextDouble();
        if (roll < 0.2 && targetRarity > 1) {
            finalRarity = targetRarity - 1;
        } else if (roll >= 0.8 && targetRarity < 5) {
            finalRarity = targetRarity + 1;
        }
        
        // Get enchantments of the determined rarity
        List<Enchantment> enchants = enchantsByRarity.getOrDefault(finalRarity, new ArrayList<>());
        
        // If no enchantments of that rarity, fall back to any random enchantment
        if (enchants.isEmpty()) {
            return getRandomEnchantment();
        }
        
        // Return a random enchantment from the selected rarity group
        return enchants.get(random.nextInt(enchants.size()));
    }
}