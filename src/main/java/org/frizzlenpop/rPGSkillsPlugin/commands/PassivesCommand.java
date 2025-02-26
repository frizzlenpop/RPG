package org.frizzlenpop.rPGSkillsPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.skills.PassiveSkillManager;

import java.util.List;

public class PassivesCommand implements CommandExecutor {
    private final PassiveSkillManager passiveSkillManager;

    public PassivesCommand(PassiveSkillManager passiveSkillManager) {
        this.passiveSkillManager = passiveSkillManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            List<String> activePassives = passiveSkillManager.getActivePassives(player);

            if (activePassives.isEmpty()) {
                player.sendMessage("§cYou have no active passive abilities.");
            } else {
                player.sendMessage("§a[Passive Abilities] Active:");
                for (String passive : activePassives) {
                    player.sendMessage("§e- " + passive);
                }
            }
            return true;
        }
        sender.sendMessage("Only players can use this command!");
        return true;
    }
}
