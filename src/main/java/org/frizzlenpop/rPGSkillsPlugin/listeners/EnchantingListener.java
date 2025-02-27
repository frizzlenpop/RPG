package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

import java.util.*;

public class EnchantingListener implements Listener {
    private final XPManager xpManager;
    private final RPGSkillsPlugin plugin;
    private final Map<Material, Integer> enchantingXPValues;
    private final Map<Enchantment, Integer> enchantmentRarity;
    private final Random random = new Random();

    private static final NamespacedKey SCROLL_ENCHANT_KEY = new NamespacedKey("rpgskills", "scroll_enchant");
    private static final NamespacedKey SCROLL_IDENTIFIED_KEY = new NamespacedKey("rpgskills", "identified");

    public EnchantingListener(XPManager xpManager, RPGSkillsPlugin plugin) {
        this.xpManager = xpManager;
        this.plugin = plugin;
        this.enchantingXPValues = initializeXPValues();
        this.enchantmentRarity = initializeEnchantmentRarity();
    }

    private Map<Material, Integer> initializeXPValues() {
        Map<Material, Integer> values = new HashMap<>();
        // Common ores
        values.put(Material.COAL_ORE, 5);
        values.put(Material.COPPER_ORE, 8);
        values.put(Material.IRON_ORE, 10);
        values.put(Material.REDSTONE_ORE, 12);
        values.put(Material.LAPIS_ORE, 15);
        values.put(Material.NETHER_QUARTZ_ORE, 15);
        values.put(Material.NETHER_GOLD_ORE, 18);
        values.put(Material.GOLD_ORE, 20);

        // Rare ores
        values.put(Material.DIAMOND_ORE, 30);
        values.put(Material.EMERALD_ORE, 35);
        values.put(Material.ANCIENT_DEBRIS, 50);

        // Deepslate variants (slightly more XP)
        values.put(Material.DEEPSLATE_COAL_ORE, 7);
        values.put(Material.DEEPSLATE_COPPER_ORE, 10);
        values.put(Material.DEEPSLATE_IRON_ORE, 12);
        values.put(Material.DEEPSLATE_REDSTONE_ORE, 14);
        values.put(Material.DEEPSLATE_LAPIS_ORE, 17);
        values.put(Material.DEEPSLATE_GOLD_ORE, 22);
        values.put(Material.DEEPSLATE_DIAMOND_ORE, 33);
        values.put(Material.DEEPSLATE_EMERALD_ORE, 38);

        // Raw materials
        values.put(Material.RAW_IRON, 5);
        values.put(Material.RAW_COPPER, 4);
        values.put(Material.RAW_GOLD, 10);

        // Ingots and gems
        values.put(Material.IRON_INGOT, 8);
        values.put(Material.GOLD_INGOT, 15);
        values.put(Material.COPPER_INGOT, 6);
        values.put(Material.DIAMOND, 25);
        values.put(Material.EMERALD, 30);
        values.put(Material.NETHERITE_INGOT, 60);
        values.put(Material.NETHERITE_SCRAP, 30);
        values.put(Material.AMETHYST_SHARD, 12);
        values.put(Material.QUARTZ, 8);

        // Special items
        values.put(Material.NETHER_STAR, 250);
        values.put(Material.DRAGON_EGG, 500);
        values.put(Material.DRAGON_HEAD, 300);
        values.put(Material.ELYTRA, 200);
        values.put(Material.ENCHANTED_GOLDEN_APPLE, 150);

        return values;
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
    public void onOreClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() != Material.AIR && enchantingXPValues.containsKey(itemInHand.getType())) {
            event.setCancelled(true); // Prevent any block interaction

            // Reduce the item stack by 1
            if (itemInHand.getAmount() > 1) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

// Award XP
            Integer xpValue = enchantingXPValues.get(itemInHand.getType());
            if (xpValue == null) return;

            xpManager.addXP(player, "enchanting", xpValue);
            player.sendMessage("§6+" + xpValue + " Enchanting XP");

// Calculate chance based on XP value
// Base chance of 5% + up to additional 15% based on XP value
            double scrollChance = 5.0 + (xpValue * 0.15);  // Using xpValue instead of recalculating

// Roll for scroll with the calculated chance
            if (random.nextDouble() * 100 < scrollChance) {
                giveUnknownScroll(player, getRandomEnchantment());
                player.sendMessage("§6You found an unknown enchantment scroll!");
            }



        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Allow placing scrolls back into inventory
        if (isIdentifiedScroll(cursor)) {
            // If clicking into an empty slot or onto another item, allow the normal inventory behavior
            if (current == null || current.getType() == Material.AIR) {
                return; // Don't cancel the event, let the item be placed
            }

            // If clicking onto an enchantable item, try to apply the enchantment
            event.setCancelled(true);

            PersistentDataContainer container = cursor.getItemMeta().getPersistentDataContainer();
            String enchantName = container.get(SCROLL_ENCHANT_KEY, PersistentDataType.STRING);
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));

            if (enchantment != null && enchantment.canEnchantItem(current)) {
                // Apply the enchantment
                current.addEnchantment(enchantment, 1);
                // Remove one scroll from the stack
                if (cursor.getAmount() > 1) {
                    cursor.setAmount(cursor.getAmount() - 1);
                } else {
                    event.getView().setCursor(null);
                }
                player.sendMessage("§aSuccessfully applied " + formatEnchantmentName(enchantment) + " to your item!");
            } else {
                player.sendMessage("§cThis enchantment cannot be applied to this item!");
            }
        }

        // Check if trying to identify an unknown scroll
        else if (isUnknownScroll(current) && event.getClick().isRightClick()) {
            event.setCancelled(true);
            identifyScroll(current, player);
        }
    }

    private void identifyScroll(ItemStack scroll, Player player) {
        ItemMeta meta = scroll.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String enchantName = container.get(SCROLL_ENCHANT_KEY, PersistentDataType.STRING);

        if (enchantName != null) {
            container.set(SCROLL_IDENTIFIED_KEY, PersistentDataType.BYTE, (byte)1);
            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));
            if (enchant != null) {
                meta.setDisplayName("§6" + formatEnchantmentName(enchant) + " Scroll");
                List<String> lore = new ArrayList<>();
                lore.add("§7Right-click on an item to apply");
                meta.setLore(lore);
                scroll.setItemMeta(meta);
                player.sendMessage("§aYou identified the scroll! It contains: " + formatEnchantmentName(enchant));
            }
        }
    }

    private void giveUnknownScroll(Player player, Enchantment enchantment) {
        ItemStack scroll = new ItemStack(Material.PAPER);
        ItemMeta meta = scroll.getItemMeta();
        if (meta == null) return;

        meta.setDisplayName("§5Unknown Enchantment Scroll");
        List<String> lore = new ArrayList<>();
        lore.add("§7Right-click to identify");
        meta.setLore(lore);

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(SCROLL_ENCHANT_KEY, PersistentDataType.STRING, enchantment.getKey().getKey());

        scroll.setItemMeta(meta);
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(scroll);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), scroll);
        }
    }

    private boolean isUnknownScroll(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(SCROLL_ENCHANT_KEY, PersistentDataType.STRING) &&
                !meta.getPersistentDataContainer().has(SCROLL_IDENTIFIED_KEY, PersistentDataType.BYTE);
    }

    private boolean isIdentifiedScroll(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(SCROLL_ENCHANT_KEY, PersistentDataType.STRING) &&
                meta.getPersistentDataContainer().has(SCROLL_IDENTIFIED_KEY, PersistentDataType.BYTE);
    }

    private String formatEnchantmentName(Enchantment enchant) {
        return enchant.getKey().getKey()
                .replace('_', ' ')
                .toLowerCase()
                .replace(" v", " V")
                .replace(" iv", " IV")
                .replace(" iii", " III")
                .replace(" ii", " II")
                .replace(" i", " I");
    }

    private Enchantment getRandomEnchantment() {
        List<Enchantment> enchants = new ArrayList<>(enchantmentRarity.keySet());
        return enchants.get(random.nextInt(enchants.size()));
    }
}