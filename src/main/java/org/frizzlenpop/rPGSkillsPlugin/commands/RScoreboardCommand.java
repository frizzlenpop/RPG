package org.frizzlenpop.rPGSkillsPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.gui.RPGScoreboardManager;

/**
 * Command handler for the /rscoreboard command
 * Toggles the visibility of the RPG skills scoreboard
 */
public class RScoreboardCommand implements CommandExecutor {
    private final RPGSkillsPlugin plugin;
    private final RPGScoreboardManager scoreboardManager;
    
    public RScoreboardCommand(RPGSkillsPlugin plugin, RPGScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Toggle scoreboard visibility
        boolean enabled = scoreboardManager.toggleScoreboard(player.getUniqueId());
        
        if (enabled) {
            player.sendMessage(ChatColor.GREEN + "RPG Skills scoreboard enabled!");
            // Update immediately
            scoreboardManager.updateScoreboard(player);
        } else {
            player.sendMessage(ChatColor.YELLOW + "RPG Skills scoreboard disabled.");
        }
        
        return true;
    }
} 