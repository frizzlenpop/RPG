package org.frizzlenpop.rPGSkillsPlugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;

import java.util.*;

public class EnchantingManager {

    private final PlayerDataManager dataManager;
    private final Random random = new Random();

    public EnchantingManager(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    // Opens the Enchanting Research GUI
    public void openResearchGUI(Player player) {
        Inventory researchGUI = Bukkit.createInventory(null, 27, "§aEnchanting Research");

        // Add items required for research
        researchGUI.setItem(11, createGuiItem(Material.BOOK, "§bResearch Book", "§7Place a book here"));
        researchGUI.setItem(13, createGuiItem(Material.LAPIS_LAZULI, "§bLapis Lazuli", "§7Place lapis here"));
        researchGUI.setItem(15, createGuiItem(Material.NETHER_STAR, "§bNether Star", "§7Place a nether star here"));

        // Open the GUI for the player
        player.openInventory(researchGUI);
    }

    // Handles the identification of unknown enchant scrolls
    public void identifyScroll(Player player, ItemStack scroll) {
        // Check if the player has the required skill level
        int enchantingLevel = dataManager.getSkillLevel(player.getUniqueId(), "enchanting");
        if (enchantingLevel < 5) {
            player.sendMessage("§cYou need at least level 5 in Enchanting to identify scrolls.");
            return;
        }

        // Determine the rarity and enchantment based on the player's level
        String enchantment = generateRandomEnchantment(enchantingLevel);

        // Create the identified enchantment scroll
        ItemStack identifiedScroll = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = identifiedScroll.getItemMeta();
        meta.setDisplayName("§r" + enchantment + " Enchantment");
        identifiedScroll.setItemMeta(meta);

        // Remove one unknown scroll and add the identified scroll
        scroll.setAmount(scroll.getAmount() - 1);
        player.getInventory().addItem(identifiedScroll);
        player.sendMessage("§aYou have identified a " + enchantment + " enchantment!");
    }

    // Generates a random enchantment based on the player's enchanting level
    private String generateRandomEnchantment(int level) {
        List<String> possibleEnchantments = new ArrayList<>(Arrays.asList("Flame", "Frost", "Thunder", "LifeSteal"));
        int index = random.nextInt(Math.min(level, possibleEnchantments.size()));
        return possibleEnchantments.get(index);
    }

    // Creates GUI items with specified name and lore
    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
