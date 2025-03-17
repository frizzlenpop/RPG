# RPG Skills Plugin API Documentation

This document provides comprehensive documentation for the RPG Skills Plugin API, which allows other plugins to interact with the RPG Skills system.

## Table of Contents

1. [Getting Started](#getting-started)
2. [API Methods](#api-methods)
   - [Skill Methods](#skill-methods)
   - [XP Methods](#xp-methods)
   - [Passive Ability Methods](#passive-ability-methods)
3. [Events](#events)
   - [SkillLevelUpEvent](#skilllevelupevent)
   - [SkillXPGainEvent](#skillxpgainevent)
   - [PassiveAbilityUnlockEvent](#passiveabilityunlockevent)
4. [Examples](#examples)
   - [Basic Integration](#basic-integration)
   - [Listening to Events](#listening-to-events)
   - [Custom XP Sources](#custom-xp-sources)
   - [Rewarding Passive Abilities](#rewarding-passive-abilities)
5. [Best Practices](#best-practices)
6. [Party System API](#party-system-api)
   - [Accessing the Party API](#accessing-the-party-api)
   - [Party Management Methods](#party-management-methods)
   - [Party Information Methods](#party-information-methods)
   - [Party Events](#party-events)

## Getting Started

To use the RPG Skills API in your plugin, you need to add the RPG Skills Plugin as a dependency in your `plugin.yml`:

```yaml
depend: [RPGSkillsPlugin]
```

Then, you can access the API in your code:

```java
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.api.RPGSkillsAPI;

public class YourPlugin extends JavaPlugin {
    private RPGSkillsAPI rpgSkillsAPI;

    @Override
    public void onEnable() {
        // Get the RPG Skills Plugin
        RPGSkillsPlugin rpgSkillsPlugin = (RPGSkillsPlugin) Bukkit.getPluginManager().getPlugin("RPGSkillsPlugin");
        
        // Get the API instance
        rpgSkillsAPI = RPGSkillsAPI.getInstance(rpgSkillsPlugin);
        
        // Now you can use the API
    }
}
```

## API Methods

### Skill Methods

#### Getting Skill Levels

```java
// Get a player's skill level
int level = rpgSkillsAPI.getSkillLevel(player, "mining");

// Get a player's skill level by UUID
int level = rpgSkillsAPI.getSkillLevel(playerUUID, "mining");

// Get the highest level a player has achieved in a skill
int highestLevel = rpgSkillsAPI.getHighestSkillLevel(player, "mining");
```

#### Setting Skill Levels

```java
// Set a player's skill level
rpgSkillsAPI.setSkillLevel(player, "mining", 10);

// Set a player's skill level by UUID
rpgSkillsAPI.setSkillLevel(playerUUID, "mining", 10);
```

### XP Methods

#### Getting XP

```java
// Get a player's current XP in a skill
int xp = rpgSkillsAPI.getSkillXP(player, "mining");

// Get a player's current XP in a skill by UUID
int xp = rpgSkillsAPI.getSkillXP(playerUUID, "mining");

// Get the total XP a player has earned in a skill
int totalXP = rpgSkillsAPI.getTotalSkillXPEarned(player, "mining");

// Get the XP required for a specific level
int requiredXP = rpgSkillsAPI.getRequiredXP(10); // XP required for level 10
```

#### Setting and Adding XP

```java
// Set a player's XP in a skill
rpgSkillsAPI.setSkillXP(player, "mining", 500);

// Set a player's XP in a skill by UUID
rpgSkillsAPI.setSkillXP(playerUUID, "mining", 500);

// Add XP to a player's skill (handles level ups and passive ability unlocks)
rpgSkillsAPI.addXP(player, "mining", 100);
```

### Passive Ability Methods

#### Checking Passive Abilities

```java
// Check if a player has a specific passive ability
boolean hasPassive = rpgSkillsAPI.hasPassive(player, "mining", "autoSmelt");

// Check if a player has a specific passive ability by UUID
boolean hasPassive = rpgSkillsAPI.hasPassive(playerUUID, "mining", "autoSmelt");

// Get all passive abilities for a player and skill
Set<String> passives = rpgSkillsAPI.getPassives(player, "mining");

// Get all passive abilities for a player
Map<String, Set<String>> allPassives = rpgSkillsAPI.getAllPassives(player);
```

#### Adding and Removing Passive Abilities

```java
// Add a passive ability to a player
rpgSkillsAPI.addPassive(player, "mining", "autoSmelt");

// Add a passive ability to a player by UUID
rpgSkillsAPI.addPassive(playerUUID, "mining", "autoSmelt");

// Remove a passive ability from a player
rpgSkillsAPI.removePassive(player, "mining", "autoSmelt");

// Remove a passive ability from a player by UUID
rpgSkillsAPI.removePassive(playerUUID, "mining", "autoSmelt");
```

## Events

The RPG Skills Plugin provides several events that your plugin can listen to.

### SkillLevelUpEvent

This event is fired when a player levels up a skill.

```java
@EventHandler
public void onSkillLevelUp(SkillLevelUpEvent event) {
    Player player = event.getPlayer();
    String skill = event.getSkill();
    int oldLevel = event.getOldLevel();
    int newLevel = event.getNewLevel();
    int levelsGained = event.getLevelsGained();
    
    // Do something when a player levels up
}
```

### SkillXPGainEvent

This event is fired when a player gains XP in a skill. It is cancellable, allowing you to prevent XP gain.

```java
@EventHandler
public void onSkillXPGain(SkillXPGainEvent event) {
    Player player = event.getPlayer();
    String skill = event.getSkill();
    int xpGained = event.getXPGained();
    
    // Modify the XP gained
    event.setXPGained(xpGained * 2); // Double the XP
    
    // Or cancel the XP gain
    // event.setCancelled(true);
}
```

### PassiveAbilityUnlockEvent

This event is fired when a player unlocks a passive ability.

```java
@EventHandler
public void onPassiveAbilityUnlock(PassiveAbilityUnlockEvent event) {
    Player player = event.getPlayer();
    String skill = event.getSkill();
    String passive = event.getPassive();
    
    // Do something when a player unlocks a passive ability
}
```

## Examples

### Basic Integration

Here's a simple example of integrating with the RPG Skills Plugin:

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.api.RPGSkillsAPI;

public class YourPlugin extends JavaPlugin {
    private RPGSkillsAPI rpgSkillsAPI;

    @Override
    public void onEnable() {
        // Get the RPG Skills Plugin
        RPGSkillsPlugin rpgSkillsPlugin = (RPGSkillsPlugin) Bukkit.getPluginManager().getPlugin("RPGSkillsPlugin");
        
        // Get the API instance
        rpgSkillsAPI = RPGSkillsAPI.getInstance(rpgSkillsPlugin);
        
        // Register commands and listeners
        getCommand("checkskill").setExecutor(new CheckSkillCommand(this));
    }
    
    public RPGSkillsAPI getRpgSkillsAPI() {
        return rpgSkillsAPI;
    }
}

class CheckSkillCommand implements CommandExecutor {
    private final YourPlugin plugin;
    
    public CheckSkillCommand(YourPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 1) {
            player.sendMessage("Usage: /checkskill <skill>");
            return true;
        }
        
        String skill = args[0].toLowerCase();
        
        // Get the player's skill level and XP
        int level = plugin.getRpgSkillsAPI().getSkillLevel(player, skill);
        int xp = plugin.getRpgSkillsAPI().getSkillXP(player, skill);
        int requiredXP = plugin.getRpgSkillsAPI().getRequiredXP(level);
        
        player.sendMessage("Your " + skill + " level is " + level + " (" + xp + "/" + requiredXP + " XP)");
        
        // Get the player's passive abilities for this skill
        Set<String> passives = plugin.getRpgSkillsAPI().getPassives(player, skill);
        
        if (!passives.isEmpty()) {
            player.sendMessage("Passive abilities: " + String.join(", ", passives));
        }
        
        return true;
    }
}
```

### Listening to Events

Here's an example of listening to RPG Skills events:

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.frizzlenpop.rPGSkillsPlugin.api.events.SkillLevelUpEvent;
import org.frizzlenpop.rPGSkillsPlugin.api.events.SkillXPGainEvent;
import org.frizzlenpop.rPGSkillsPlugin.api.events.PassiveAbilityUnlockEvent;

public class RPGSkillsListener implements Listener {
    private final YourPlugin plugin;
    
    public RPGSkillsListener(YourPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onSkillLevelUp(SkillLevelUpEvent event) {
        Player player = event.getPlayer();
        String skill = event.getSkill();
        int newLevel = event.getNewLevel();
        
        // Give the player a reward for leveling up
        if (newLevel % 10 == 0) { // Every 10 levels
            player.sendMessage("Congratulations! You've reached level " + newLevel + " in " + skill + "!");
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, newLevel / 10));
        }
    }
    
    @EventHandler
    public void onSkillXPGain(SkillXPGainEvent event) {
        Player player = event.getPlayer();
        String skill = event.getSkill();
        
        // Double XP for VIP players
        if (player.hasPermission("yourplugin.vip")) {
            event.setXPGained(event.getXPGained() * 2);
            player.sendMessage("VIP bonus: Double XP!");
        }
    }
    
    @EventHandler
    public void onPassiveAbilityUnlock(PassiveAbilityUnlockEvent event) {
        Player player = event.getPlayer();
        String skill = event.getSkill();
        String passive = event.getPassive();
        
        // Announce when a player unlocks a rare passive ability
        if (passive.equals("autoSmeltUpgrade") || passive.equals("masterFortune")) {
            Bukkit.broadcastMessage(player.getName() + " has unlocked the rare " + passive + " ability!");
        }
    }
}
```

### Custom XP Sources

Here's an example of adding custom XP sources:

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class CustomXPSource implements Listener {
    private final YourPlugin plugin;
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    
    public CustomXPSource(YourPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Location currentLocation = player.getLocation();
        
        // Only process if the player has moved at least 1 block
        if (lastLocations.containsKey(playerUUID)) {
            Location lastLocation = lastLocations.get(playerUUID);
            
            if (lastLocation.getWorld().equals(currentLocation.getWorld())) {
                double distance = lastLocation.distance(currentLocation);
                
                // Award XP for traveling distance (1 XP per 100 blocks)
                if (distance >= 100) {
                    int xpToAward = (int) (distance / 100);
                    
                    // Add XP to the player's "exploration" skill
                    plugin.getRpgSkillsAPI().addXP(player, "exploration", xpToAward);
                    
                    // Update the last location
                    lastLocations.put(playerUUID, currentLocation);
                }
            }
        } else {
            // First time seeing this player, just store their location
            lastLocations.put(playerUUID, currentLocation);
        }
    }
}
```

### Rewarding Passive Abilities

Here's an example of rewarding passive abilities for completing custom tasks:

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class CustomPassiveReward implements Listener {
    private final YourPlugin plugin;
    private final Map<UUID, Integer> specialBlockInteractions = new HashMap<>();
    
    public CustomPassiveReward(YourPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Check if the player is interacting with a special block
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && 
            event.getClickedBlock() != null && 
            event.getClickedBlock().getType() == Material.DRAGON_EGG) {
            
            // Increment the counter for this player
            int interactions = specialBlockInteractions.getOrDefault(playerUUID, 0) + 1;
            specialBlockInteractions.put(playerUUID, interactions);
            
            // Check if the player has reached the threshold for a reward
            if (interactions >= 10) {
                // Reset the counter
                specialBlockInteractions.put(playerUUID, 0);
                
                // Check if the player already has the passive ability
                if (!plugin.getRpgSkillsAPI().hasPassive(player, "mining", "dragonMiner")) {
                    // Award the passive ability
                    plugin.getRpgSkillsAPI().addPassive(player, "mining", "dragonMiner");
                    
                    player.sendMessage("You have unlocked the Dragon Miner passive ability!");
                }
            } else {
                player.sendMessage("Dragon Egg interactions: " + interactions + "/10");
            }
        }
    }
}
```

## Best Practices

1. **Always check if the RPG Skills Plugin is available before using the API.**
   ```java
   if (Bukkit.getPluginManager().getPlugin("RPGSkillsPlugin") != null) {
       // Use the API
   }
   ```

2. **Cache the API instance instead of getting it every time you need it.**
   ```java
   private RPGSkillsAPI rpgSkillsAPI;
   
   @Override
   public void onEnable() {
       RPGSkillsPlugin rpgSkillsPlugin = (RPGSkillsPlugin) Bukkit.getPluginManager().getPlugin("RPGSkillsPlugin");
       rpgSkillsAPI = RPGSkillsAPI.getInstance(rpgSkillsPlugin);
   }
   ```

3. **Use the appropriate methods for your needs.**
   - Use `getSkillLevel` and `getSkillXP` for displaying information to players.
   - Use `addXP` instead of `setSkillXP` when awarding XP to ensure level ups and passive abilities are handled correctly.
   - Use `hasPassive` to check if a player has a specific passive ability before applying its effects.

4. **Be mindful of performance.**
   - Cache results when appropriate to avoid excessive database queries.
   - Avoid calling API methods in tight loops or frequently executed code.
   - Consider using async tasks for operations that don't need to be immediate.

5. **Respect the plugin's design.**
   - Don't set skill levels or XP excessively high.
   - Don't add passive abilities that the player wouldn't normally be able to unlock.
   - Use the events provided by the plugin to integrate with it rather than overriding its behavior.

6. **Handle errors gracefully.**
   - Check for null values when getting data from the API.
   - Catch exceptions when calling API methods.
   - Provide meaningful error messages to players when something goes wrong.

7. **Document your integration.**
   - Let your users know that your plugin integrates with the RPG Skills Plugin.
   - Explain how your plugin interacts with the RPG Skills system.
   - Provide examples of how your plugin enhances the RPG Skills experience.

## Party System API

The RPG Skills Plugin provides a comprehensive API for interacting with the party system. This allows other plugins to create, manage, and monitor parties.

### Accessing the Party API

```java
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.api.RPGSkillsAPI;

public class YourPlugin extends JavaPlugin {
    private RPGSkillsAPI rpgSkillsAPI;
    
    @Override
    public void onEnable() {
        // Get the RPG Skills Plugin
        Plugin plugin = getServer().getPluginManager().getPlugin("RPGSkills");
        
        if (plugin instanceof RPGSkillsPlugin) {
            // Get the API
            rpgSkillsAPI = ((RPGSkillsPlugin) plugin).getAPI();
            getLogger().info("Successfully hooked into RPG Skills Plugin!");
        } else {
            getLogger().warning("Failed to hook into RPG Skills Plugin!");
        }
    }
}
```

### Party Management Methods

```java
// Check if a player is in a party
boolean isInParty = rpgSkillsAPI.isInParty(player);

// Check if a player is the leader of their party
boolean isLeader = rpgSkillsAPI.isPartyLeader(player);

// Get the party leader's UUID
UUID leaderUUID = rpgSkillsAPI.getPartyLeader(player);

// Get all members of a party
Set<UUID> members = rpgSkillsAPI.getPartyMembers(leaderUUID);

// Get all online members of a party
List<Player> onlineMembers = rpgSkillsAPI.getOnlinePartyMembers(leaderUUID);

// Create a new party
boolean created = rpgSkillsAPI.createParty(player);

// Invite a player to join a party
boolean invited = rpgSkillsAPI.invitePlayer(leader, invitee);

// Accept a party invitation
boolean accepted = rpgSkillsAPI.acceptInvitation(player);

// Leave a party
rpgSkillsAPI.leaveParty(player);

// Disband a party
boolean disbanded = rpgSkillsAPI.disbandParty(leader);

// Kick a player from a party
boolean kicked = rpgSkillsAPI.kickPlayer(leader, target);
```

### Party Information Methods

```java
// Get the party level
int level = rpgSkillsAPI.getPartyLevel(leaderUUID);

// Get the party's total shared XP
long totalXP = rpgSkillsAPI.getPartyTotalSharedXp(leaderUUID);

// Get the XP required for the party to reach the next level
long xpForNextLevel = rpgSkillsAPI.getXpForNextLevel(leaderUUID);

// Get the party's XP sharing percentage
double sharePercent = rpgSkillsAPI.getXpSharePercent(leaderUUID);

// Get the party's bonus XP percentage based on party level
double bonusPercent = rpgSkillsAPI.getPartyBonusPercent(leaderUUID);

// Get the maximum party size
int maxSize = rpgSkillsAPI.getMaxPartySize(leaderUUID);

// Get formatted information about a party
String partyInfo = rpgSkillsAPI.getPartyInfo(player);
```

### Party Events

The RPG Skills Plugin provides several events that other plugins can listen to:

#### PartyCreateEvent

Fired when a party is created.

```java
@EventHandler
public void onPartyCreate(PartyCreateEvent event) {
    Player leader = event.getLeader();
    
    // You can cancel the event to prevent the party from being created
    // event.setCancelled(true);
    
    getLogger().info(leader.getName() + " created a new party!");
}
```

#### PartyJoinEvent

Fired when a player joins a party.

```java
@EventHandler
public void onPartyJoin(PartyJoinEvent event) {
    Player player = event.getPlayer();
    UUID leaderUUID = event.getPartyLeaderUUID();
    
    // You can cancel the event to prevent the player from joining the party
    // event.setCancelled(true);
    
    getLogger().info(player.getName() + " joined a party!");
}
```

#### PartyLeaveEvent

Fired when a player leaves a party.

```java
@EventHandler
public void onPartyLeave(PartyLeaveEvent event) {
    UUID playerUUID = event.getPlayerUUID();
    UUID leaderUUID = event.getPartyLeaderUUID();
    boolean isKicked = event.isKicked();
    boolean isDisband = event.isDisband();
    
    if (isKicked) {
        getLogger().info("A player was kicked from a party!");
    } else if (isDisband) {
        getLogger().info("A party was disbanded!");
    } else {
        getLogger().info("A player left a party!");
    }
}
```

#### PartyLevelUpEvent

Fired when a party levels up.

```java
@EventHandler
public void onPartyLevelUp(PartyLevelUpEvent event) {
    UUID leaderUUID = event.getPartyLeaderUUID();
    int oldLevel = event.getOldLevel();
    int newLevel = event.getNewLevel();
    int levelsGained = event.getLevelsGained();
    
    getLogger().info("A party leveled up from " + oldLevel + " to " + newLevel + "!");
}
``` 