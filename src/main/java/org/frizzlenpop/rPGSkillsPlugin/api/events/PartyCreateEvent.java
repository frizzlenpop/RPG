package org.frizzlenpop.rPGSkillsPlugin.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a party is created.
 */
public class PartyCreateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player leader;
    private boolean cancelled = false;

    /**
     * Creates a new PartyCreateEvent.
     * 
     * @param leader The player who created the party
     */
    public PartyCreateEvent(Player leader) {
        this.leader = leader;
    }

    /**
     * Gets the player who created the party.
     * 
     * @return The party leader
     */
    public Player getLeader() {
        return leader;
    }

    /**
     * Checks if the event is cancelled.
     * 
     * @return True if the event is cancelled, false otherwise
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether the event is cancelled.
     * 
     * @param cancelled True to cancel the event, false otherwise
     */
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