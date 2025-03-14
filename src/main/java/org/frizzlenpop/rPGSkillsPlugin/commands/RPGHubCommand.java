package org.frizzlenpop.rPGSkillsPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.gui.RPGHubGUI;

/**
 * Command to open the centralized RPG Hub GUI
 */
public class RPGHubCommand implements CommandExecutor {
    private final RPGHubGUI rpgHubGUI;
    
    /**
     * Creates a new RPGHubCommand
     * 
     * @param rpgHubGUI The RPG Hub GUI
     */
    public RPGHubCommand(RPGHubGUI rpgHubGUI) {
        this.rpgHubGUI = rpgHubGUI;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Open the RPG Hub GUI
        rpgHubGUI.openGUI(player);
        
        return true;
    }
} 