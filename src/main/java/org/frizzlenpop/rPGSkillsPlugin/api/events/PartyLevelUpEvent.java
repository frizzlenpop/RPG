package org.frizzlenpop.rPGSkillsPlugin.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Event that is called when a party levels up.
 */
public class PartyLevelUpEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID partyLeaderUUID;
    private final int oldLevel;
    private final int newLevel;

    /**
     * Creates a new PartyLevelUpEvent.
     * 
     * @param partyLeaderUUID The UUID of the party leader
     * @param oldLevel The old party level
     * @param newLevel The new party level
     */
    public PartyLevelUpEvent(UUID partyLeaderUUID, int oldLevel, int newLevel) {
        this.partyLeaderUUID = partyLeaderUUID;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
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
     * Gets the old party level.
     * 
     * @return The old level
     */
    public int getOldLevel() {
        return oldLevel;
    }

    /**
     * Gets the new party level.
     * 
     * @return The new level
     */
    public int getNewLevel() {
        return newLevel;
    }

    /**
     * Gets the number of levels gained.
     * 
     * @return The number of levels gained
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