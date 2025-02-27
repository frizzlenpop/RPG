package org.frizzlenpop.rPGSkillsPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.skills.SkillAbilityManager;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class AbilitiesCommand implements CommandExecutor {
    private final RPGSkillsPlugin plugin;
    private final PlayerDataManager playerDataManager;

    public AbilitiesCommand(RPGSkillsPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            displayAbilitiesHelp(player);
            return true;
        }

        String skill = args[0].toLowerCase();

        // Verify if it's a valid skill
        if (!isValidSkill(skill)) {
            player.sendMessage(ChatColor.RED + "Invalid skill! Available skills: mining, logging, farming, fighting, fishing, enchanting");
            return true;
        }

        // Check if player has unlocked the ability
        if (!playerDataManager.hasUnlockedActiveSkill(player.getUniqueId(), skill)) {
            player.sendMessage(ChatColor.RED + "You haven't unlocked the " + skill + " ability yet! Reach level 15 to unlock it.");
            return true;
        }

        // Attempt to activate the ability using the plugin's ability manager
        this.plugin.getAbilityManager().activateAbility(player, skill);

        return true;
    }

    private void displayAbilitiesHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== RPG Skills Abilities ===");
        player.sendMessage(ChatColor.YELLOW + "/abilities <skill> " + ChatColor.WHITE + "- Activate the ability for the specified skill");
        player.sendMessage(ChatColor.YELLOW + "Available skills: mining, logging, farming, fighting, fishing, enchanting");
        player.sendMessage(ChatColor.GRAY + "Note: Abilities are unlocked at level 15 in each skill!");
    }

    private boolean isValidSkill(String skill) {
        return skill.matches("mining|logging|farming|fighting|fishing|enchanting");
    }
}