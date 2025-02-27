package org.frizzlenpop.rPGSkillsPlugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Item;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

import java.util.*;

public class PassiveSkillManager implements Listener {
    private final RPGSkillsPlugin plugin;
    private final Map<UUID, Set<String>> activePassives;

    // Existing Sets
    private final Set<UUID> autoSmeltPlayers = new HashSet<>();
    private final Set<UUID> autoReplantPlayers = new HashSet<>();
    private final Set<UUID> doubleOreDropPlayers = new HashSet<>();
    private final Set<UUID> doubleWoodDropPlayers = new HashSet<>();
    private final Set<UUID> doubleCropYieldPlayers = new HashSet<>();
    private final Set<UUID> healOnKillPlayers = new HashSet<>();

    // New Sets for Higher Level Passives
    private final Set<UUID> fortuneBoostPlayers = new HashSet<>();
    private final Set<UUID> autoSmeltUpgradePlayers = new HashSet<>();
    private final Set<UUID> treeGrowthBoostPlayers = new HashSet<>();
    private final Set<UUID> tripleLogDropPlayers = new HashSet<>();
    private final Set<UUID> instantGrowthPlayers = new HashSet<>();
    private final Set<UUID> autoHarvestPlayers = new HashSet<>();
    private final Set<UUID> lifestealPlayers = new HashSet<>();
    private final Set<UUID> damageReductionPlayers = new HashSet<>();

    public PassiveSkillManager(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        this.activePassives = new HashMap<>();

        // Register the player join event
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Load passives for online players (in case of reload)
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            loadPlayerPassives(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayerPassives(event.getPlayer());
    }

    private void loadPlayerPassives(Player player) {
        UUID playerId = player.getUniqueId();
        Set<String> passives = new HashSet<>();

        // Get skill levels from PlayerDataManager
        int miningLevel = plugin.getPlayerDataManager().getSkillLevel(playerId, "mining");
        int loggingLevel = plugin.getPlayerDataManager().getSkillLevel(playerId, "logging");
        int farmingLevel = plugin.getPlayerDataManager().getSkillLevel(playerId, "farming");
        int fightingLevel = plugin.getPlayerDataManager().getSkillLevel(playerId, "fighting");

        // Mining passives
        if (miningLevel >= 5) {
            passives.add("double_ore_drop");
            doubleOreDropPlayers.add(playerId);
        }
        if (miningLevel >= 10) {
            passives.add("auto_smelt");
            autoSmeltPlayers.add(playerId);
        }
        if (miningLevel >= 15) {
            passives.add("fortune_boost");
            fortuneBoostPlayers.add(playerId);
        }
        if (miningLevel >= 20) {
            passives.add("auto_smelt_upgrade");
            autoSmeltUpgradePlayers.add(playerId);
        }

        // Logging passives
        if (loggingLevel >= 5) {
            passives.add("double_wood_drop");
            doubleWoodDropPlayers.add(playerId);
        }
        if (loggingLevel >= 10) {
            passives.add("tree_growth_boost");
            treeGrowthBoostPlayers.add(playerId);
        }
        if (loggingLevel >= 15) {
            passives.add("triple_log_drop");
            tripleLogDropPlayers.add(playerId);
        }

        // Farming passives
        if (farmingLevel >= 5) {
            passives.add("double_crop_yield");
            doubleCropYieldPlayers.add(playerId);
        }
        if (farmingLevel >= 10) {
            passives.add("auto_replant");
            autoReplantPlayers.add(playerId);
        }
        if (farmingLevel >= 15) {
            passives.add("instant_growth");
            instantGrowthPlayers.add(playerId);
        }

        // Fighting passives
        if (fightingLevel >= 5) {
            passives.add("lifesteal");
            lifestealPlayers.add(playerId);
        }
        if (fightingLevel >= 10) {
            passives.add("damage_reduction");
            damageReductionPlayers.add(playerId);
        }
        if (fightingLevel >= 15) {
            passives.add("heal_on_kill");
            healOnKillPlayers.add(playerId);
        }

        activePassives.put(playerId, passives);
    }

    public boolean hasPassive(UUID playerId, String passive) {
        return activePassives.containsKey(playerId) &&
                activePassives.get(playerId).contains(passive);
    }

    public Set<String> getPlayerPassives(UUID playerId) {
        return activePassives.getOrDefault(playerId, new HashSet<>());
    }

    public void addPassive(UUID playerId, String passive) {
        activePassives.computeIfAbsent(playerId, k -> new HashSet<>()).add(passive);
    }

    public void removePassive(UUID playerId, String passive) {
        if (activePassives.containsKey(playerId)) {
            activePassives.get(playerId).remove(passive);
        }
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Your existing onBlockBreak implementation
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Your existing onEntityDeath implementation
    }

    private boolean isCrop(Material material) {
        // Your existing isCrop implementation
        return false; // Replace with your actual implementation
    }

    private ItemStack getCropDrops(Material cropType) {
        // Your existing getCropDrops implementation
        return null; // Replace with your actual implementation
    }

    private boolean isMatureCrop(Block block) {
        // Your existing isMatureCrop implementation
        return false; // Replace with your actual implementation
    }

    private boolean isOre(Material material) {
        // Your existing isOre implementation
        return false; // Replace with your actual implementation
    }

    private Material getOreDrop(Material oreMaterial) {
        // Your existing getOreDrop implementation
        return null; // Replace with your actual implementation
    }

    private Material getSmeltedOre(Material oreMaterial) {
        // Your existing getSmeltedOre implementation
        return null; // Replace with your actual implementation
    }

    public void applyPassiveEffect(Player player, String effect) {
        UUID playerId = player.getUniqueId();
        switch (effect) {
            case "auto_smelt":
                autoSmeltPlayers.add(playerId);
                break;
            case "auto_replant":
                autoReplantPlayers.add(playerId);
                break;
            case "double_ore_drop":
                doubleOreDropPlayers.add(playerId);
                break;
            // Add other cases for new passives
            default:
                break;
        }
        addPassive(playerId, effect);
    }

    public List<String> getActivePassives(Player player) {
        return new ArrayList<>(getPlayerPassives(player.getUniqueId()));
    }
}