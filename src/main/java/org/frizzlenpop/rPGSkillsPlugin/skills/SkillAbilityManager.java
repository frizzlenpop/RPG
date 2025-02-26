package org.frizzlenpop.rPGSkillsPlugin.skills;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SkillAbilityManager implements Listener {
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final Set timberChopPlayers = new HashSet<>();

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
        }
    }

    @EventHandler
    public void onTreeBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (timberChopPlayers.contains(player.getUniqueId()) && isLog(block.getType())) {
            chopTree(block);
            timberChopPlayers.remove(player.getUniqueId());
        }
    }

    private boolean isLog(Material material) {
        return material.name().endsWith("_LOG"); // Works for all wood types
    }

    private void chopTree(Block block) {
        Queue<Block> logsToBreak = new LinkedList<>();
        logsToBreak.add(block);

        while (!logsToBreak.isEmpty()) {
            Block current = logsToBreak.poll();
            if (isLog(current.getType())) {
                current.breakNaturally();
                for (Block relative : getAdjacentBlocks(current)) {
                    logsToBreak.add(relative);
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
    private final Map<String, List<String>> skillAbilities = new HashMap<>();

    public SkillAbilityManager() {
        skillAbilities.put("mining", Arrays.asList("Mining Burst"));
        skillAbilities.put("logging", Arrays.asList("Timber Chop"));
        skillAbilities.put("fighting", Arrays.asList("Berserker Rage"));
    }

    public List<String> getAbilitiesForSkill(String skill) {
        return skillAbilities.getOrDefault(skill, new ArrayList<>());
    }

    public void activateAbility(Player player, String ability) {
        switch (ability) {
            case "Mining Burst":
                player.sendMessage("§a[Skill] Mining Burst activated!");
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 5 * 20, 2));
                break;
            case "Timber Chop":
                player.sendMessage("§a[Skill] Timber Chop activated!");
                break;
            case "Berserker Rage":
                player.sendMessage("§a[Skill] Berserker Rage activated!");
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 10 * 20, 1));
                break;
        }
    }
}
