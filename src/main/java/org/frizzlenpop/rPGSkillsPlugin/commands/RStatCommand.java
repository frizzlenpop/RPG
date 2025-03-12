package org.frizzlenpop.rPGSkillsPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.skills.PassiveSkillManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillTreeManager;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillTreeNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Command to display all XP boosts from both skills and skill tree systems
 */
public class RStatCommand implements CommandExecutor {
    private final RPGSkillsPlugin plugin;
    private final XPManager xpManager;
    private final PassiveSkillManager passiveSkillManager;
    private final SkillTreeManager skillTreeManager;
    
    private static final String[] SKILL_TYPES = {"mining", "logging", "farming", "fighting", "fishing", "enchanting"};
    
    /**
     * Constructor for the RStatCommand
     */
    public RStatCommand(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        this.xpManager = plugin.getXpManager();
        this.passiveSkillManager = plugin.getPassiveSkillManager();
        this.skillTreeManager = plugin.getSkillTreeManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Calculate and display XP boosts from both systems
        Map<String, Double> totalBoosts = calculateTotalXPBoosts(player);
        
        // Display results to player
        displayXPBoosts(player, totalBoosts);
        
        return true;
    }
    
    /**
     * Calculate the total XP boosts from both skill system and skill tree system
     */
    private Map<String, Double> calculateTotalXPBoosts(Player player) {
        Map<String, Double> totalBoosts = new HashMap<>();
        
        // Initialize boost values for all skills
        for (String skill : SKILL_TYPES) {
            totalBoosts.put(skill, 0.0);
        }
        
        // Add boosts from passive skill system
        calculatePassiveSkillBoosts(player, totalBoosts);
        
        // Add boosts from skill tree system
        calculateSkillTreeBoosts(player, totalBoosts);
        
        return totalBoosts;
    }
    
    /**
     * Calculate XP boosts from passive skill system
     */
    private void calculatePassiveSkillBoosts(Player player, Map<String, Double> totalBoosts) {
        UUID playerId = player.getUniqueId();
        
        // Mining XP boosts
        int miningLevel = plugin.getPlayerDataManager().getSkillLevel(playerId, "mining");
        if (passiveSkillManager.hasPassive(playerId, "miningXpBoost")) {
            if (miningLevel >= 82) { // XP Boost V
                totalBoosts.put("mining", totalBoosts.get("mining") + 0.30); // +30%
            } else if (miningLevel >= 64) { // XP Boost IV
                totalBoosts.put("mining", totalBoosts.get("mining") + 0.25); // +25%
            } else if (miningLevel >= 45) { // XP Boost III
                totalBoosts.put("mining", totalBoosts.get("mining") + 0.20); // +20%
            } else if (miningLevel >= 24) { // XP Boost II
                totalBoosts.put("mining", totalBoosts.get("mining") + 0.15); // +15%
            } else if (miningLevel >= 7) { // XP Boost I
                totalBoosts.put("mining", totalBoosts.get("mining") + 0.10); // +10%
            }
        }
        
        if (passiveSkillManager.hasPassive(playerId, "masterMiner")) {
            totalBoosts.put("mining", totalBoosts.get("mining") + 0.25); // +25% Master Miner
        }
        
        // Logging XP boosts
        int loggingLevel = plugin.getPlayerDataManager().getSkillLevel(playerId, "logging");
        if (passiveSkillManager.hasPassive(playerId, "xpBoost5")) {
            totalBoosts.put("logging", totalBoosts.get("logging") + 0.30); // +30%
        } else if (passiveSkillManager.hasPassive(playerId, "xpBoost4")) {
            totalBoosts.put("logging", totalBoosts.get("logging") + 0.25); // +25%
        } else if (passiveSkillManager.hasPassive(playerId, "xpBoost3")) {
            totalBoosts.put("logging", totalBoosts.get("logging") + 0.20); // +20%
        } else if (passiveSkillManager.hasPassive(playerId, "xpBoost2")) {
            totalBoosts.put("logging", totalBoosts.get("logging") + 0.15); // +15%
        } else if (passiveSkillManager.hasPassive(playerId, "xpBoost1")) {
            totalBoosts.put("logging", totalBoosts.get("logging") + 0.10); // +10%
        }
        
        // Farming XP boosts
        // Add farming XP boosts based on the implementation
        
        // Fighting XP boosts
        if (passiveSkillManager.hasPassive(playerId, "combatXpBoost5")) {
            totalBoosts.put("fighting", totalBoosts.get("fighting") + 0.30); // +30%
        } else if (passiveSkillManager.hasPassive(playerId, "combatXpBoost4")) {
            totalBoosts.put("fighting", totalBoosts.get("fighting") + 0.25); // +25%
        } else if (passiveSkillManager.hasPassive(playerId, "combatXpBoost3")) {
            totalBoosts.put("fighting", totalBoosts.get("fighting") + 0.20); // +20%
        } else if (passiveSkillManager.hasPassive(playerId, "combatXpBoost2")) {
            totalBoosts.put("fighting", totalBoosts.get("fighting") + 0.15); // +15%
        } else if (passiveSkillManager.hasPassive(playerId, "combatXpBoost1")) {
            totalBoosts.put("fighting", totalBoosts.get("fighting") + 0.10); // +10%
        }
        
        // Fishing XP boosts
        if (passiveSkillManager.hasPassive(player, "fishing", "XP Boost VI")) {
            totalBoosts.put("fishing", totalBoosts.get("fishing") + 0.40); // +40%
        } else if (passiveSkillManager.hasPassive(player, "fishing", "XP Boost V")) {
            totalBoosts.put("fishing", totalBoosts.get("fishing") + 0.30); // +30%
        } else if (passiveSkillManager.hasPassive(player, "fishing", "XP Boost IV")) {
            totalBoosts.put("fishing", totalBoosts.get("fishing") + 0.25); // +25%
        } else if (passiveSkillManager.hasPassive(player, "fishing", "XP Boost III")) {
            totalBoosts.put("fishing", totalBoosts.get("fishing") + 0.20); // +20%
        } else if (passiveSkillManager.hasPassive(player, "fishing", "XP Boost II")) {
            totalBoosts.put("fishing", totalBoosts.get("fishing") + 0.15); // +15%
        } else if (passiveSkillManager.hasPassive(player, "fishing", "XP Boost I")) {
            totalBoosts.put("fishing", totalBoosts.get("fishing") + 0.10); // +10%
        }
        
        if (passiveSkillManager.hasPassive(player, "fishing", "Master Angler")) {
            totalBoosts.put("fishing", totalBoosts.get("fishing") + 0.10); // +10%
        }
        
        if (passiveSkillManager.hasPassive(player, "fishing", "Legendary Fisher")) {
            totalBoosts.put("fishing", totalBoosts.get("fishing") + 0.25); // +25%
        }
        
        if (passiveSkillManager.hasPassive(player, "fishing", "Master Fisher")) {
            totalBoosts.put("fishing", totalBoosts.get("fishing") + 0.50); // +50%
        }
        
        // Enchanting XP boosts
        
        // Research Master passives
        if (passiveSkillManager.hasPassive(player, "enchanting", "Research Master V")) {
            totalBoosts.put("enchanting", totalBoosts.get("enchanting") + 0.45); // +45%
        } else if (passiveSkillManager.hasPassive(player, "enchanting", "Research Master IV")) {
            totalBoosts.put("enchanting", totalBoosts.get("enchanting") + 0.40); // +40%
        } else if (passiveSkillManager.hasPassive(player, "enchanting", "Research Master III")) {
            totalBoosts.put("enchanting", totalBoosts.get("enchanting") + 0.35); // +35%
        } else if (passiveSkillManager.hasPassive(player, "enchanting", "Research Master II")) {
            totalBoosts.put("enchanting", totalBoosts.get("enchanting") + 0.30); // +30%
        } else if (passiveSkillManager.hasPassive(player, "enchanting", "Research Master I")) {
            totalBoosts.put("enchanting", totalBoosts.get("enchanting") + 0.25); // +25%
        }
        
        // Enchanting XP Boost passives
        if (passiveSkillManager.hasPassive(player, "enchanting", "Enchanting XP Boost V")) {
            totalBoosts.put("enchanting", totalBoosts.get("enchanting") + 0.25); // +25%
        } else if (passiveSkillManager.hasPassive(player, "enchanting", "Enchanting XP Boost IV")) {
            totalBoosts.put("enchanting", totalBoosts.get("enchanting") + 0.20); // +20%
        } else if (passiveSkillManager.hasPassive(player, "enchanting", "Enchanting XP Boost III")) {
            totalBoosts.put("enchanting", totalBoosts.get("enchanting") + 0.15); // +15%
        } else if (passiveSkillManager.hasPassive(player, "enchanting", "Enchanting XP Boost II")) {
            totalBoosts.put("enchanting", totalBoosts.get("enchanting") + 0.10); // +10%
        } else if (passiveSkillManager.hasPassive(player, "enchanting", "Enchanting XP Boost I")) {
            totalBoosts.put("enchanting", totalBoosts.get("enchanting") + 0.05); // +5%
        }
        
        // Master Enchanter
        if (passiveSkillManager.hasPassive(player, "enchanting", "Master Enchanter")) {
            totalBoosts.put("enchanting", totalBoosts.get("enchanting") + 0.30); // +30%
        }
        
        // Legendary Enchanter
        if (passiveSkillManager.hasPassive(player, "enchanting", "Legendary Enchanter")) {
            totalBoosts.put("enchanting", totalBoosts.get("enchanting") + 0.25); // +25%
        }
    }
    
    /**
     * Calculate XP boosts from skill tree system
     */
    private void calculateSkillTreeBoosts(Player player, Map<String, Double> totalBoosts) {
        // Get player's unlocked nodes
        Set<String> unlockedNodes = skillTreeManager.getPlayerUnlockedNodes(player);
        
        // Map of all nodes
        Map<String, SkillTreeNode> allNodes = skillTreeManager.getAllNodes();
        
        // Check each unlocked node for XP boosts
        for (String nodeId : unlockedNodes) {
            SkillTreeNode node = allNodes.get(nodeId);
            if (node != null) {
                // Check each effect on the node
                for (SkillTreeNode.NodeEffect effect : node.getEffects()) {
                    if (effect.getType() == SkillTreeNode.EffectType.SKILL_XP_BOOST) {
                        String targetSkill = effect.getTarget();
                        double boostValue = effect.getValue();
                        
                        // Add to total boost for this skill
                        if (totalBoosts.containsKey(targetSkill)) {
                            totalBoosts.put(targetSkill, totalBoosts.get(targetSkill) + boostValue);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Display XP boosts to the player
     */
    private void displayXPBoosts(Player player, Map<String, Double> totalBoosts) {
        // Create a visual header with border
        String border = ChatColor.GOLD + "+======================+";
        player.sendMessage(border);
        player.sendMessage(ChatColor.GOLD + "| " + ChatColor.YELLOW + "XP BOOST SUMMARY" + ChatColor.GOLD + "     |");
        player.sendMessage(border);
        
        // Display each skill's total boost with a formatted bar representation
        for (String skill : SKILL_TYPES) {
            double boost = totalBoosts.getOrDefault(skill, 0.0);
            int boostPercent = (int)(boost * 100);
            
            // Skill name formatting (capitalized and right-padded)
            String skillName = skill.substring(0, 1).toUpperCase() + skill.substring(1);
            while (skillName.length() < 8) skillName += " ";
            
            // Create visual boost bar
            String boostBar = createBoostBar(boost);
            
            // Color coding based on boost value
            ChatColor color;
            if (boost >= 0.5) color = ChatColor.DARK_GREEN; // 50%+ boost
            else if (boost >= 0.25) color = ChatColor.GREEN; // 25%+ boost
            else if (boost >= 0.1) color = ChatColor.YELLOW; // 10%+ boost
            else color = ChatColor.RED;
            
            // Format like: | Mining   [■■■■■     ] 50% |
            player.sendMessage(ChatColor.GOLD + "| " + ChatColor.AQUA + skillName + 
                    ChatColor.WHITE + " " + boostBar + " " + 
                    color + boostPercent + "%" + ChatColor.GRAY + 
                    " ".repeat(Math.max(0, 3 - String.valueOf(boostPercent).length())) + ChatColor.GOLD + " |");
        }
        
        // Add total combined boost
        double totalBoostValue = totalBoosts.values().stream().mapToDouble(Double::doubleValue).sum();
        double averageBoost = totalBoostValue / SKILL_TYPES.length;
        int avgBoostPercent = (int)(averageBoost * 100);
        
        // Format average boost
        player.sendMessage(border);
        player.sendMessage(ChatColor.GOLD + "| " + ChatColor.YELLOW + "AVERAGE " + 
                createBoostBar(averageBoost) + " " + ChatColor.GOLD + avgBoostPercent + "%" + 
                " ".repeat(Math.max(0, 3 - String.valueOf(avgBoostPercent).length())) + ChatColor.GOLD + " |");
        
        // Add effective XP rate
        double totalMultiplier = 1 + averageBoost;
        player.sendMessage(ChatColor.GOLD + "| " + ChatColor.WHITE + "Effective XP Rate: " + 
                ChatColor.YELLOW + String.format("%.2fx", totalMultiplier) + 
                ChatColor.GOLD + "  |");
        
        player.sendMessage(border);
    }
    
    /**
     * Create a visual boost bar representation
     */
    private String createBoostBar(double boost) {
        int filledBlocks = (int)(boost * 10);
        if (filledBlocks > 10) filledBlocks = 10;
        
        StringBuilder bar = new StringBuilder(ChatColor.DARK_GRAY + "[");
        
        // Add filled blocks
        if (filledBlocks > 0) {
            if (boost >= 0.5) bar.append(ChatColor.DARK_GREEN);
            else if (boost >= 0.25) bar.append(ChatColor.GREEN);
            else if (boost >= 0.1) bar.append(ChatColor.YELLOW);
            else bar.append(ChatColor.RED);
            
            bar.append("■".repeat(filledBlocks));
        }
        
        // Add empty blocks
        if (filledBlocks < 10) {
            bar.append(ChatColor.DARK_GRAY);
            bar.append("□".repeat(10 - filledBlocks));
        }
        
        bar.append(ChatColor.DARK_GRAY + "]");
        return bar.toString();
    }
} 