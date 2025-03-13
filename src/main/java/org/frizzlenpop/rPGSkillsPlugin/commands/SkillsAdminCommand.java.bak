package org.frizzlenpop.rPGSkillsPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

public class SkillsAdminCommand implements CommandExecutor {
    private final PlayerDataManager playerDataManager;
    private final XPManager xpManager;

    public SkillsAdminCommand(PlayerDataManager playerDataManager, XPManager xpManager) {
        this.playerDataManager = playerDataManager;
        this.xpManager = xpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rpgskills.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin <player> <skill> <set/add> <level/xp> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        String skill = args[1].toLowerCase();
        String action = args[2].toLowerCase();
        String type = args[3].toLowerCase();

        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "Please specify an amount!");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[4]);
            
            switch (action) {
                case "set":
                    if (type.equals("level")) {
                        playerDataManager.setSkillLevel(target.getUniqueId(), skill, amount);
                        sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s " + skill + " level to " + amount);
                    } else if (type.equals("xp")) {
                        playerDataManager.setSkillXP(target.getUniqueId(), skill, amount);
                        sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s " + skill + " XP to " + amount);
                    }
                    break;
                    
                case "add":
                    if (type.equals("xp")) {
                        xpManager.addXP(target, skill, amount);
                        sender.sendMessage(ChatColor.GREEN + "Added " + amount + " XP to " + target.getName() + "'s " + skill);
                    }
                    break;
                    
                default:
                    sender.sendMessage(ChatColor.RED + "Invalid action! Use 'set' or 'add'");
                    return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Please enter a valid number!");
            return true;
        }

        return true;
    }
}