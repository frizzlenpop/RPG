package org.frizzlenpop.rPGSkillsPlugin.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Event that is called when a player joins a party.
 */
public class PartyJoinEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final UUID partyLeaderUUID;
    private boolean cancelled = false;

    /**
     * Creates a new PartyJoinEvent.
     * 
     * @param player The player who joined the party
     * @param partyLeaderUUID The UUID of the party leader
     */
    public PartyJoinEvent(Player player, UUID partyLeaderUUID) {
        this.player = player;
        this.partyLeaderUUID = partyLeaderUUID;
    }

    /**
     * Gets the player who joined the party.
     * 
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the UUID of the party leader.
     * 
     * @return The party leader's UUID
     */
    public UUID getPartyLeaderUUID() {
        return partyLeaderUUID;
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