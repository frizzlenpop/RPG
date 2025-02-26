package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;
import org.bukkit.NamespacedKey;

import java.util.*;

public class EnchantingListener implements Listener {
    private final XPManager xpManager;
    private final Map<Material, Integer> enchantingXPValues;
    private final Map<Enchantment, Integer> enchantmentRarity;
    private final Random random = new Random();

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
        enchantmentRarity.put(Enchantment.FORTUNE, 8);
    }
    @EventHandler
    public void onEnchantScrollUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if item is a custom enchant scroll
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();

            if (displayName.startsWith("§dUnknown Enchant Scroll")) {
                event.setCancelled(true); // Prevent any default interaction

                // Extract stored enchantment from lore
                List<String> lore = item.getItemMeta().getLore();
                if (lore == null || lore.isEmpty()) {
                    player.sendMessage("§cThis scroll seems blank...");
                    return;
                }

                // Extract enchantment key from lore
                String enchantKey = lore.get(0).replace("§7Enchantment: ", "").toUpperCase();
                Enchantment enchantment = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(enchantKey));

                if (enchantment == null) {
                    player.sendMessage("§cThis enchantment seems corrupted...");
                    return;
                }

                // Check if the player is holding a valid item to apply enchantment
                ItemStack targetItem = player.getInventory().getItemInOffHand(); // Off-hand for application
                if (targetItem.getType() == Material.AIR) {
                    player.sendMessage("§cHold an item in your off-hand to apply the enchantment!");
                    return;
                }

                // Apply the enchantment
                targetItem.addUnsafeEnchantment(enchantment, 1);
                player.sendMessage("§aApplied " + enchantKey + " to your " + targetItem.getType().name() + "!");

                // Consume the scroll
                item.setAmount(item.getAmount() - 1);
            }
        }
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
                if (random.nextDouble() < 0.20) {
                    Enchantment newEnchant = getRandomEnchantment();
                    player.sendMessage("§6[Enchanting] You discovered: " + newEnchant.getKey().getKey() + "!");
                    giveUnknownScroll(player, newEnchant);
                }
            }
        }
    }

    private Enchantment getRandomEnchantment() {
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

    private void giveUnknownScroll(Player player, Enchantment enchantment) {
        ItemStack scroll = new ItemStack(Material.PAPER);
        ItemMeta meta = scroll.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§rUnknown Enchant Scroll");
            List<String> lore = new ArrayList<>();
            lore.add("§7A mysterious scroll containing an unknown enchantment.");
            lore.add("§eRight-click to identify!");

            // Store the enchantment inside the item by using PersistentDataContainer
            NamespacedKey key = new NamespacedKey("rpgskills", "unknown_enchant");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, enchantment.getKey().getKey());

            meta.setLore(lore);
            scroll.setItemMeta(meta);
        }

        player.getInventory().addItem(scroll);
    }

    @EventHandler
    public void onScrollIdentification(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && item.getType() == Material.PAPER && item.getItemMeta().getDisplayName().equals("§rUnknown Enchant Scroll")) {
            // Identify the enchantment
            Enchantment identifiedEnchant = getRandomEnchantment();

            // Create an Enchanted Book with the identified enchantment
            ItemStack identifiedBook = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta meta = identifiedBook.getItemMeta();
            meta.setDisplayName("§r" + identifiedEnchant.getKey().getKey() + " Enchantment");
            List<String> lore = new ArrayList<>();
            lore.add("§7A newly identified enchantment.");
            meta.setLore(lore);
            identifiedBook.setItemMeta(meta);

            // Remove one scroll and give the enchanted book
            item.setAmount(item.getAmount() - 1);
            player.getInventory().addItem(identifiedBook);
            player.sendMessage("§aYou identified an enchantment: " + identifiedEnchant.getKey().getKey() + "!");
        }
    }

    @EventHandler
    public void onCombineEnchants(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (inv == null) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack firstItem = inv.getItem(11);
        ItemStack secondItem = inv.getItem(15);

        if (firstItem != null && secondItem != null) {
            if (firstItem.getType() == Material.ENCHANTED_BOOK && secondItem.getType() == Material.ENCHANTED_BOOK) {
                // Combine Enchantments
                ItemStack newBook = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta newMeta = newBook.getItemMeta();
                List<String> newLore = new ArrayList<>();

                // Extract enchantment names from the books
                String firstEnchant = firstItem.getItemMeta().getDisplayName().replace("§r", "");
                String secondEnchant = secondItem.getItemMeta().getDisplayName().replace("§r", "");

                // Merge enchantments into a new one
                String mergedEnchant = mergeEnchantments(firstEnchant, secondEnchant);
                newMeta.setDisplayName("§r" + mergedEnchant + " Enchantment");
                newLore.add("§7A powerful merged enchantment.");
                newMeta.setLore(newLore);
                newBook.setItemMeta(newMeta);

                // Remove the two books and give the merged one
                firstItem.setAmount(0);
                secondItem.setAmount(0);
                player.getInventory().addItem(newBook);
                player.sendMessage("§6You have combined " + firstEnchant + " and " + secondEnchant + " into " + mergedEnchant + "!");
            }
        }
    }

    private String mergeEnchantments(String first, String second) {
        Map<String, String> mergeResults = new HashMap<>();
        mergeResults.put("Sharpness", "Power Strike");
        mergeResults.put("Protection", "Fortified Shield");
        mergeResults.put("Fire Aspect", "Inferno Touch");
        mergeResults.put("Mending", "Everlasting Rune");
        mergeResults.put("Fortune", "Prosperity");

        return mergeResults.getOrDefault(first, first) + " & " + mergeResults.getOrDefault(second, second);
    }
}
