package org.frizzlenpop.rPGSkillsPlugin.skills;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Item;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class PassiveSkillManager implements Listener {
    private final Set<UUID> autoSmeltPlayers = new HashSet<>();
    private final Set<UUID> autoReplantPlayers = new HashSet<>();
    private final Set<UUID> doubleOreDropPlayers = new HashSet<>();
    private final Set<UUID> doubleWoodDropPlayers = new HashSet<>();
    private final Set<UUID> doubleCropYieldPlayers = new HashSet<>();
    private final Set<UUID> healOnKillPlayers = new HashSet<>();

    public void applyPassiveEffect(Player player, String effect) {
        UUID playerUUID = player.getUniqueId();

        switch (effect) {
            case "auto_smelt":
                autoSmeltPlayers.add(playerUUID);
                player.sendMessage("§e[Passive] Auto-smelt enabled for ores!");
                break;
            case "auto_replant":
                autoReplantPlayers.add(playerUUID);
                player.sendMessage("§e[Passive] Crops will now auto-replant!");
                break;
            case "double_ore_drops":
                doubleOreDropPlayers.add(playerUUID);
                player.sendMessage("§e[Passive] You now have a chance to double ore drops!");
                break;
            case "double_wood_drops":
                doubleWoodDropPlayers.add(playerUUID);
                player.sendMessage("§e[Passive] You now have a chance to double wood drops!");
                break;
            case "double_crop_yield":
                doubleCropYieldPlayers.add(playerUUID);
                player.sendMessage("§e[Passive] Your crops now have a chance to double yield!");
                break;
            case "heal_on_kill":
                healOnKillPlayers.add(playerUUID);
                player.sendMessage("§e[Passive] You now heal when killing mobs!");
                break;
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (autoSmeltPlayers.contains(player.getUniqueId()) && isOre(block.getType())) {
            event.setDropItems(false);
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(getSmeltedOre(block.getType())));
        }

        if (doubleOreDropPlayers.contains(player.getUniqueId()) && isOre(block.getType())) {
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(getOreDrop(block.getType())));
        }
    }

    @EventHandler
    public void onHarvest(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();
        if (autoReplantPlayers.contains(player.getUniqueId())) {
            event.getHarvestedBlock().setType(event.getHarvestedBlock().getType()); // Replant crop
        }
        if (doubleCropYieldPlayers.contains(player.getUniqueId())) {
            event.getItemsHarvested().add(event.getItemsHarvested().get(0)); // Double drops
        }
    }
    public List<String> getActivePassives(Player player) {
        List<String> activePassives = new ArrayList<>();
        UUID playerUUID = player.getUniqueId();

        if (autoSmeltPlayers.contains(playerUUID)) activePassives.add("Auto Smelt");
        if (autoReplantPlayers.contains(playerUUID)) activePassives.add("Auto Replant");
        if (doubleOreDropPlayers.contains(playerUUID)) activePassives.add("Double Ore Drops");
        if (doubleWoodDropPlayers.contains(playerUUID)) activePassives.add("Double Wood Drops");
        if (doubleCropYieldPlayers.contains(playerUUID)) activePassives.add("Double Crop Yield");
        if (healOnKillPlayers.contains(playerUUID)) activePassives.add("Heal on Kill");

        return activePassives;
    }


    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player != null && healOnKillPlayers.contains(player.getUniqueId())) {
            player.setHealth(Math.min(player.getHealth() + 2.0, player.getMaxHealth())); // Heal 1 heart
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (doubleOreDropPlayers.contains(player.getUniqueId()) && event.getCaught() instanceof Item) {
            Item caughtItem = (Item) event.getCaught();
            event.getCaught().getWorld().dropItemNaturally(event.getCaught().getLocation(), caughtItem.getItemStack());
        }
    }

    private boolean isOre(Material material) {
        return material.name().endsWith("_ORE");
    }

    private Material getSmeltedOre(Material ore) {
        switch (ore) {
            case IRON_ORE:
                return Material.IRON_INGOT;
            case GOLD_ORE:
                return Material.GOLD_INGOT;
            default:
                return ore;
        }
    }

    private Material getOreDrop(Material ore) {
        return ore; // Returns itself for duplication
    }
}
