package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EnchantingListener implements Listener {
    private final XPManager xpManager;
    private final Map<Material, Integer> enchantingXPValues;
    private final Map<Enchantment, Integer> enchantmentRarity;

    public EnchantingListener(XPManager xpManager) {
        this.xpManager = xpManager;
        this.enchantingXPValues = new HashMap<>();
        this.enchantmentRarity = new HashMap<>();

        // Define XP values for research materials
        enchantingXPValues.put(Material.ENCHANTED_BOOK, 50);
        enchantingXPValues.put(Material.LAPIS_LAZULI, 10);
        enchantingXPValues.put(Material.NETHER_STAR, 200);
        enchantingXPValues.put(Material.DRAGON_BREATH, 250);
        enchantingXPValues.put(Material.AMETHYST_SHARD, 25);

        // Define enchantment rarity (higher = rarer)
        enchantmentRarity.put(Enchantment.PROTECTION, 5);
        enchantmentRarity.put(Enchantment.SHARPNESS, 10);
        enchantmentRarity.put(Enchantment.FIRE_ASPECT, 15);
        enchantmentRarity.put(Enchantment.MENDING, 2);
        enchantmentRarity.put(Enchantment.FORTUNE, 8); // Fortune
    }

    @EventHandler
    public void onResearchItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null) {
            Material itemType = item.getType();

            // Get XP for research material
            int xpGained = enchantingXPValues.getOrDefault(itemType, 0);
            if (xpGained > 0) {
                xpManager.addXP(player, "enchanting", xpGained);

                // Consume 1 item from stack
                item.setAmount(item.getAmount() - 1);

                // Notify player
                player.sendMessage("§b[Enchanting] You gained " + xpGained + " XP from researching " + itemType.name() + "!");

                // 20% chance to discover a new enchantment
                if (new Random().nextDouble() < 0.20) {
                    Enchantment newEnchant = getRandomEnchantment();
                    player.sendMessage("§6[Enchanting] You discovered: " + newEnchant.getKey().getKey() + "!");
                }
            }
        }
    }

    private Enchantment getRandomEnchantment() {
        Random random = new Random();
        int totalWeight = enchantmentRarity.values().stream().mapToInt(Integer::intValue).sum();
        int chosen = random.nextInt(totalWeight);

        int cumulativeWeight = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantmentRarity.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (chosen < cumulativeWeight) {
                return entry.getKey();
            }
        }
        return Enchantment.PROTECTION; // Default fallback
    }
}
