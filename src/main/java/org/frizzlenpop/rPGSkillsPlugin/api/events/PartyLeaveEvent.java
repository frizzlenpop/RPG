package org.frizzlenpop.rPGSkillsPlugin.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Event that is called when a player leaves a party.
 */
public class PartyLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID playerUUID;
    private final UUID partyLeaderUUID;
    private final boolean isKicked;
    private final boolean isDisband;

    /**
     * Creates a new PartyLeaveEvent.
     * 
     * @param playerUUID The UUID of the player who left the party
     * @param partyLeaderUUID The UUID of the party leader
     * @param isKicked Whether the player was kicked from the party
     * @param isDisband Whether the party was disbanded
     */
    public PartyLeaveEvent(UUID playerUUID, UUID partyLeaderUUID, boolean isKicked, boolean isDisband) {
        this.playerUUID = playerUUID;
        this.partyLeaderUUID = partyLeaderUUID;
        this.isKicked = isKicked;
        this.isDisband = isDisband;
    }

    /**
     * Gets the UUID of the player who left the party.
     * 
     * @return The player's UUID
     */
    public UUID getPlayerUUID() {
        return playerUUID;
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
     * Checks if the player was kicked from the party.
     * 
     * @return True if the player was kicked, false otherwise
     */
    public boolean isKicked() {
        return isKicked;
    }

    /**
     * Checks if the party was disbanded.
     * 
     * @return True if the party was disbanded, false otherwise
     */
    public boolean isDisband() {
        return isDisband;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
} 