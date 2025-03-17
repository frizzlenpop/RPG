package org.frizzlenpop.rPGSkillsPlugin.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a player levels up a skill.
 */
public class SkillLevelUpEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String skill;
    private final int oldLevel;
    private final int newLevel;

    /**
     * Creates a new SkillLevelUpEvent.
     * 
     * @param player The player who leveled up
     * @param skill The skill that was leveled up
     * @param oldLevel The old skill level
     * @param newLevel The new skill level
     */
    public SkillLevelUpEvent(Player player, String skill, int oldLevel, int newLevel) {
        this.player = player;
        this.skill = skill;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    /**
     * Gets the player who leveled up.
     * 
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the skill that was leveled up.
     * 
     * @return The skill name
     */
    public String getSkill() {
        return skill;
    }

    /**
     * Gets the old skill level.
     * 
     * @return The old level
     */
    public int getOldLevel() {
        return oldLevel;
    }

    /**
     * Gets the new skill level.
     * 
     * @return The new level
     */
    public int getNewLevel() {
        return newLevel;
    }

    /**
     * Gets the amount of levels gained.
     * 
     * @return The amount of levels gained
     */
    public int getLevelsGained() {
        return newLevel - oldLevel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
} 