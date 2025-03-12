package org.frizzlenpop.rPGSkillsPlugin.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;

public class FishingListener implements Listener {

    private final XPManager xpManager;

    public FishingListener(XPManager xpManager) {
        this.xpManager = xpManager;
    }

    @EventHandler
    public void onFishCatch(PlayerFishEvent event) {
        Player player = event.getPlayer();
        
        // Log the fishing event state
        player.sendMessage("§7[Debug] Fishing event state: " + event.getState());
        
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Entity caught = event.getCaught();
            
            if (caught == null) {
                player.sendMessage("§7[Debug] Caught entity is null!");
                return;
            }
            
            player.sendMessage("§7[Debug] Caught entity type: " + caught.getType());
            
            if (caught instanceof Item) {
                Item itemCaught = (Item) caught;
                ItemStack itemStack = itemCaught.getItemStack();
                String itemType = itemStack.getType().toString();
                
                player.sendMessage("§7[Debug] Caught item type: " + itemType);
                
                // Get XP for the caught fish
                int xpGained = xpManager.getXPForFish(itemType);
                player.sendMessage("§7[Debug] XP gained for this item: " + xpGained);
                
                if (xpGained > 0) {
                    xpManager.addXP(player, "fishing", xpGained);
                    player.sendMessage("§b+" + xpGained + " Fishing XP");
                } else {
                    player.sendMessage("§7[Debug] No XP gained for item type: " + itemType);
                }
            } else {
                player.sendMessage("§7[Debug] Caught entity is not an Item!");
            }
        }
    }
}
