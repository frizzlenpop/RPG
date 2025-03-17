package org.frizzlenpop.rPGSkillsPlugin.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a player unlocks a passive ability.
 */
public class PassiveAbilityUnlockEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String skill;
    private final String passive;

    /**
     * Creates a new PassiveAbilityUnlockEvent.
     * 
     * @param player The player who unlocked the passive ability
     * @param skill The skill associated with the passive ability
     * @param passive The passive ability that was unlocked
     */
    public PassiveAbilityUnlockEvent(Player player, String skill, String passive) {
        this.player = player;
        this.skill = skill;
        this.passive = passive;
    }

    /**
     * Gets the player who unlocked the passive ability.
     * 
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the skill associated with the passive ability.
     * 
     * @return The skill name
     */
    public String getSkill() {
        return skill;
    }

    /**
     * Gets the passive ability that was unlocked.
     * 
     * @return The passive ability name
     */
    public String getPassive() {
        return passive;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
} 