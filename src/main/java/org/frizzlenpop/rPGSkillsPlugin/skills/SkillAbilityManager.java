package org.frizzlenpop.rPGSkillsPlugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class SkillAbilityManager implements Listener {
    private final RPGSkillsPlugin plugin; // Reference to your main plugin instance
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final Set<UUID> timberChopPlayers = new HashSet<>();
    private final Set<UUID> activeDoubleEnchant = new HashSet<>();
    private final Set<UUID> activeInstantCatch = new HashSet<>();
    private final Map<String, List<String>> skillAbilities = new HashMap<>();

    public SkillAbilityManager(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        // Initialize skill abilities for each skill category
        skillAbilities.put("mining", Arrays.asList("Mining Burst"));
        skillAbilities.put("logging", Arrays.asList("Timber Chop"));
        skillAbilities.put("fighting", Arrays.asList("Berserker Rage"));
        skillAbilities.put("farming", Arrays.asList("Super Harvest"));
        skillAbilities.put("fishing", Arrays.asList("Instant Catch"));
        skillAbilities.put("enchanting", Arrays.asList("Double Enchant"));
    }

    public boolean canUseAbility(Player player, String ability, int cooldownSeconds) {
        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long lastUsed = cooldowns.getOrDefault(playerUUID, 0L);

        if (currentTime - lastUsed >= cooldownSeconds * 1000) {
            cooldowns.put(playerUUID, currentTime);
            return true;
        } else {
            player.sendMessage("§cThis ability is on cooldown!");
            return false;
        }
    }

    public void activateMiningBurst(Player player) {
        if (canUseAbility(player, "MiningBurst", 60)) {
            player.sendMessage("§a[Skill] Mining Burst activated! You mine 3x faster for 5 seconds.");
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 5 * 20, 2)); // Haste III for 5s
        }
    }

    public void activateTimberChop(Player player) {
        if (canUseAbility(player, "TimberChop", 30)) {
            player.sendMessage("§a[Skill] Timber Chop activated! The next tree you break will fall instantly.");

            timberChopPlayers.add(player.getUniqueId());
            Bukkit.broadcastMessage(Arrays.toString(timberChopPlayers.toArray()));
        }
    }

    @EventHandler
    public void onTreeBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (timberChopPlayers.contains(player.getUniqueId()) && isLog(block.getType())) {
            Bukkit.broadcastMessage("Block broken: " + block.getType());
            chopTree(block);
            timberChopPlayers.remove(player.getUniqueId());
        }
    }

    private boolean isLog(Material material) {
        return material.name().contains("_LOG"); // Works for all wood types
    }

    private void chopTree(Block block) {
        Queue<Block> logsToBreak = new LinkedList<>();
        Set<Block> visited = new HashSet<>();

        logsToBreak.add(block);
        visited.add(block);

        while (!logsToBreak.isEmpty()) {
            Block current = logsToBreak.poll();
            if (isLog(current.getType())) {
                current.breakNaturally();

                for (Block relative : getAdjacentBlocks(current)) {
                    if (!visited.contains(relative) && isLog(relative.getType())) {
                        logsToBreak.add(relative);
                        visited.add(relative);
                    }
                }
            }
        }
    }


    private List<Block> getAdjacentBlocks(Block block) {
        List<Block> adjacent = new ArrayList<>();
        adjacent.add(block.getRelative(1, 0, 0));
        adjacent.add(block.getRelative(-1, 0, 0));
        adjacent.add(block.getRelative(0, 1, 0));
        adjacent.add(block.getRelative(0, -1, 0));
        adjacent.add(block.getRelative(0, 0, 1));
        adjacent.add(block.getRelative(0, 0, -1));
        return adjacent;
    }

    public void activateBerserkerRage(Player player) {
        if (canUseAbility(player, "BerserkerRage", 90)) {
            player.sendMessage("§a[Skill] Berserker Rage activated! +50% damage for 10 seconds.");
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 10 * 20, 1)); // Strength II for 10s
        }
    }

    public List<String> getAbilitiesForSkill(String skill) {
        return skillAbilities.getOrDefault(skill, new ArrayList<>());
    }

    public void activateAbility(Player player, String ability) {
        switch (ability) {
            case "Mining Burst":
                activateMiningBurst(player);
                break;
            case "Timber Chop":
                activateTimberChop(player);
                break;
            case "Berserker Rage":
                activateBerserkerRage(player);
                break;
        }
    }

    // New ability: Super Harvest
    public void activateSuperHarvest(Player player) {
        if (!canUseAbility(player, "superharvest", 60)) return;

        Location playerLoc = player.getLocation();
        int radius = 5;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = playerLoc.getBlock().getRelative(x, y, z);
                    if (isCrop(block.getType())) {
                        Ageable crop = (Ageable) block.getBlockData();
                        if (crop.getAge() < crop.getMaximumAge()) {
                            crop.setAge(crop.getMaximumAge());
                            block.setBlockData(crop);
                        }
                        // Harvest fully grown crops
                        if (crop.getAge() == crop.getMaximumAge()) {
                            block.breakNaturally();
                            // Auto replant if player has the passive (implement hasAutoReplantPassive as needed)
                            if (hasAutoReplantPassive(player)) {
                                block.setType(block.getType());
                            }
                        }
                    }
                }
            }
        }

        player.playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 1.0f, 1.0f);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColor.GREEN + "Super Harvest activated!"));
    }

    // New ability: Instant Catch
    public void activateInstantCatch(Player player) {
        if (!canUseAbility(player, "instantcatch", 75)) return;

        activeInstantCatch.add(player.getUniqueId());

        // Schedule removal after 10 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            activeInstantCatch.remove(player.getUniqueId());
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(ChatColor.RED + "Instant Catch ended!"));
        }, 200L); // 10 seconds * 20 ticks

        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 1.0f);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColor.GREEN + "Instant Catch activated!"));
    }

    // New ability: Double Enchant
    public void activateDoubleEnchant(Player player) {
        if (!canUseAbility(player, "doubleenchant", 90)) return;

        activeDoubleEnchant.add(player.getUniqueId());

        // Schedule removal after 30 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            activeDoubleEnchant.remove(player.getUniqueId());
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(ChatColor.RED + "Double Enchant ended!"));
        }, 600L); // 30 seconds * 20 ticks

        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColor.GREEN + "Double Enchant activated!"));
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (activeInstantCatch.contains(player.getUniqueId())) {
            if (event.getState() == PlayerFishEvent.State.FISHING) {
                // Force instant catch after a short delay
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Set bite chance to maximum
                    event.getHook().setBiteChance(1.0);

                    // Create a new fishing event to simulate a catch
                    // We can't directly set the state, so we'll pull in the hook
                    player.launchProjectile(event.getHook().getClass());
                    event.getHook().remove();

                    // Give player a random fish to simulate a catch
                    ItemStack fishItem = getRandomFishItem();
                    player.getInventory().addItem(fishItem);
                    player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 1.0f);
                }, 20L); // 1 second delay
            }
        }
    }

    private ItemStack getRandomFishItem() {
        Material[] fishItems = {
                Material.COD, Material.SALMON, Material.TROPICAL_FISH, Material.PUFFERFISH
        };
        return new ItemStack(fishItems[(int)(Math.random() * fishItems.length)]);
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        if (activeDoubleEnchant.contains(player.getUniqueId())) {
            if (Math.random() < 0.5) { // 50% chance
                Map<Enchantment, Integer> enchants = new HashMap<>(event.getEnchantsToAdd());
                // Get a random valid enchantment for the item
                Enchantment randomEnchant = getRandomValidEnchantment(event.getItem());
                if (randomEnchant != null) {
                    int level = 1 + (int)(Math.random() * randomEnchant.getMaxLevel());
                    enchants.put(randomEnchant, level);
                    event.getEnchantsToAdd().putAll(enchants);
                    player.sendMessage(ChatColor.GREEN + "Double Enchant triggered!");
                }
            }
        }
    }

    private boolean isCrop(Material material) {
        return material == Material.WHEAT || material == Material.CARROTS ||
                material == Material.POTATOES || material == Material.BEETROOTS ||
                material == Material.NETHER_WART;
    }

    private Enchantment getRandomValidEnchantment(ItemStack item) {
        List<Enchantment> validEnchants = Arrays.stream(Enchantment.values())
                .filter(e -> e.canEnchantItem(item))
                .collect(Collectors.toList());
        if (validEnchants.isEmpty()) return null;
        return validEnchants.get((int)(Math.random() * validEnchants.size()));
    }

    // Placeholder method: implement a check for the auto replant passive as needed
    private boolean hasAutoReplantPassive(Player player) {
        // Return true if the player has the auto replant passive; otherwise false.
        return false;
    }
}
