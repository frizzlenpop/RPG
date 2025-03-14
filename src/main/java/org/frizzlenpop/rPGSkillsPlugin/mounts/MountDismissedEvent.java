package org.frizzlenpop.rPGSkillsPlugin.mounts;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that fires when a mount is dismissed.
 */
public class MountDismissedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Mount mount;
    
    /**
     * Creates a new mount dismissed event
     * 
     * @param player The player who dismissed the mount
     * @param mount The mount that was dismissed
     */
    public MountDismissedEvent(Player player, Mount mount) {
        this.player = player;
        this.mount = mount;
    }
    
    /**
     * Gets the player who dismissed the mount
     * 
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Gets the mount that was dismissed
     * 
     * @return The mount
     */
    public Mount getMount() {
        return mount;
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
} 