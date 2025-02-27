package org.frizzlenpop.rPGSkillsPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;

public class ToggleSkillMessagesCommand implements CommandExecutor {
    private final PlayerDataManager playerDataManager;

    public ToggleSkillMessagesCommand(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        FileConfiguration playerData = playerDataManager.getPlayerData(player.getUniqueId());
        
        // Toggle the messages setting
        boolean currentSetting = playerData.getBoolean("settings.skillMessages", true);
        playerData.set("settings.skillMessages", !currentSetting);
        
        // Save the updated setting
        playerDataManager.savePlayerData(player.getUniqueId(), playerData);

        String status = !currentSetting ? "enabled" : "disabled";
        player.sendMessage(ChatColor.GREEN + "Skill messages have been " + status + "!");
        
        return true;
    }
}