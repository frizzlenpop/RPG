package org.frizzlenpop.rPGSkillsPlugin.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a player gains XP in a skill.
 * This event is cancellable, allowing other plugins to prevent XP gain.
 */
public class SkillXPGainEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String skill;
    private int xpGained;
    private boolean cancelled;

    /**
     * Creates a new SkillXPGainEvent.
     * 
     * @param player The player who gained XP
     * @param skill The skill in which XP was gained
     * @param xpGained The amount of XP gained
     */
    public SkillXPGainEvent(Player player, String skill, int xpGained) {
        this.player = player;
        this.skill = skill;
        this.xpGained = xpGained;
        this.cancelled = false;
    }

    /**
     * Gets the player who gained XP.
     * 
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the skill in which XP was gained.
     * 
     * @return The skill name
     */
    public String getSkill() {
        return skill;
    }

    /**
     * Gets the amount of XP gained.
     * 
     * @return The amount of XP
     */
    public int getXPGained() {
        return xpGained;
    }

    /**
     * Sets the amount of XP gained.
     * This can be used to modify the amount of XP a player receives.
     * 
     * @param xpGained The new amount of XP
     */
    public void setXPGained(int xpGained) {
        this.xpGained = xpGained;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
} 