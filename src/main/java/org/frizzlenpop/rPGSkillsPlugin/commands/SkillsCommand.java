package org.frizzlenpop.rPGSkillsPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.gui.SkillsGUI;

public class SkillsCommand implements CommandExecutor {

    private final SkillsGUI skillsGUI;

    public SkillsCommand(SkillsGUI skillsGUI) {
        this.skillsGUI = skillsGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        skillsGUI.openSkillsMenu(player);
        return true;
    }
}
