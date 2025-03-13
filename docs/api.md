# API Documentation

## Overview

The RPG Skills Plugin provides an extensive API that allows other plugins to interact with its systems, listen for events, and extend its functionality. This documentation outlines the available classes, methods, and events that developers can use.

## Accessing the API

To access the RPG Skills Plugin API, add it as a dependency in your plugin.yml:

```yaml
depend: [RPGSkills]
```

Then, in your code, get the API instance:

```java
import org.frizzlenpop.rpgskills.api.RPGSkillsAPI;

public class YourPlugin extends JavaPlugin {
    private RPGSkillsAPI rpgSkillsAPI;
    
    @Override
    public void onEnable() {
        // Get the API instance
        Plugin plugin = getServer().getPluginManager().getPlugin("RPGSkills");
        if (plugin != null) {
            rpgSkillsAPI = ((RPGSkills) plugin).getAPI();
        }
    }
}
```

## Core API Methods

### Player Skills

```java
// Get a player's skill level
int getSkillLevel(Player player, String skillName);

// Get a player's skill XP
double getSkillXP(Player player, String skillName);

// Get XP needed for next level
double getXPForNextLevel(Player player, String skillName);

// Set a player's skill level
void setSkillLevel(Player player, String skillName, int level);

// Add XP to a player's skill
void addSkillXP(Player player, String skillName, double amount, XPSource source);

// Check if a player has a certain skill level
boolean hasSkillLevel(Player player, String skillName, int level);

// Get all skill data for a player
Map<String, SkillData> getAllSkillData(Player player);
```

### Player XP

```java
// Add XP with custom source and multiplier
void addXP(Player player, String skillName, double amount, XPSource source, double multiplier);

// Get global XP multiplier
double getGlobalXPMultiplier();

// Set global XP multiplier 
void setGlobalXPMultiplier(double multiplier);

// Get skill-specific multiplier
double getSkillMultiplier(String skillName);
```

### XP Boosters

```java
// Apply an XP booster to a player's item
boolean applyBooster(Player player, String skillName, double multiplier, long duration);

// Remove a booster from a player's item
boolean removeBooster(Player player);

// Check if a player's current item has a booster
boolean hasBooster(Player player);

// Get booster details
BoosterData getBoosterData(Player player);
```

### Passive Abilities

```java
// Check if a player has unlocked a passive ability
boolean hasPassiveAbility(Player player, String abilityName);

// Get passive ability trigger chance
double getPassiveTriggerChance(Player player, String abilityName);

// Manually trigger a passive ability (for testing)
boolean triggerPassiveAbility(Player player, String abilityName, Event triggeringEvent);

// Get all unlocked passive abilities for a player
List<String> getUnlockedPassiveAbilities(Player player);
```

### Active Abilities

```java
// Check if a player has unlocked an active ability
boolean hasActiveAbility(Player player, String abilityName);

// Get ability cooldown time remaining
long getAbilityCooldown(Player player, String abilityName);

// Execute an active ability
boolean executeAbility(Player player, String abilityName);

// Get all unlocked active abilities for a player
List<String> getUnlockedActiveAbilities(Player player);
```

### Skill Tree

```java
// Get available skill points
int getSkillPoints(Player player);

// Set skill points
void setSkillPoints(Player player, int points);

// Add skill points
void addSkillPoints(Player player, int points);

// Check if a node is unlocked
boolean isNodeUnlocked(Player player, String nodeId);

// Unlock a node
boolean unlockNode(Player player, String nodeId);

// Get all unlocked nodes
Set<String> getUnlockedNodes(Player player);
```

### Party System

```java
// Check if a player is in a party
boolean isInParty(Player player);

// Get a player's party
Party getParty(Player player);

// Create a party
Party createParty(Player leader, String partyName);

// Add player to party
boolean addToParty(Party party, Player player);

// Remove player from party
boolean removeFromParty(Party party, Player player);

// Get party XP sharing percentage
double getXPSharePercentage(Party party);
```

## Custom Events

You can listen for the following events in your plugin:

### Skill Events

```java
// When a player gains XP
SkillXPGainEvent

// When a player levels up a skill
SkillLevelUpEvent

// When a player uses an active ability
AbilityUseEvent

// When a passive ability triggers
PassiveAbilityTriggerEvent
```

### Party Events

```java
// When a party is created
PartyCreateEvent

// When a player joins a party
PartyJoinEvent

// When a player leaves a party
PartyLeaveEvent

// When a party gains a level
PartyLevelUpEvent
```

### Skill Tree Events

```java
// When a player unlocks a node
NodeUnlockEvent

// When skill points are added
SkillPointsChangeEvent
```

### XP Booster Events

```java
// When a booster is applied
BoosterApplyEvent

// When a booster expires
BoosterExpireEvent
```

## Event Listening Example

```java
public class ExampleListener implements Listener {
    @EventHandler
    public void onSkillLevelUp(SkillLevelUpEvent event) {
        Player player = event.getPlayer();
        String skillName = event.getSkillName();
        int newLevel = event.getNewLevel();
        
        player.sendMessage("Congratulations from MyPlugin on reaching " + 
                           skillName + " level " + newLevel + "!");
        
        // Do something special at certain milestones
        if (newLevel == 50) {
            // Give a reward
        }
    }
    
    @EventHandler
    public void onPassiveAbilityTrigger(PassiveAbilityTriggerEvent event) {
        // Do something when passive abilities trigger
    }
}
```

## Integration Example

Here's a complete example of integrating with the RPG Skills API:

```java
public class MyRPGSkillsIntegration {
    private final RPGSkillsAPI api;
    
    public MyRPGSkillsIntegration(Plugin rpgSkillsPlugin) {
        this.api = ((RPGSkills) rpgSkillsPlugin).getAPI();
    }
    
    // Give bonus XP for completing a quest
    public void giveQuestReward(Player player, String skillName, double xpAmount) {
        api.addXP(player, skillName, xpAmount, XPSource.CUSTOM, 1.0);
        player.sendMessage("You received " + xpAmount + " bonus XP in " + skillName + " for completing the quest!");
    }
    
    // Check if player meets skill requirements for an item
    public boolean meetsItemRequirements(Player player, Map<String, Integer> requiredSkills) {
        for (Map.Entry<String, Integer> entry : requiredSkills.entrySet()) {
            String skill = entry.getKey();
            int requiredLevel = entry.getValue();
            
            if (!api.hasSkillLevel(player, skill, requiredLevel)) {
                return false;
            }
        }
        return true;
    }
    
    // Create a custom party
    public void createGuildParty(Player leader, List<Player> members, String guildName) {
        Party party = api.createParty(leader, guildName + " Party");
        for (Player member : members) {
            if (member != leader) {
                api.addToParty(party, member);
            }
        }
    }
}
```

## Extending the Plugin

For more extensive integrations, you can extend the plugin's capabilities:

### Custom Skills

You can register custom skills:

```java
api.registerCustomSkill("alchemy", "Alchemy", Material.BREWING_STAND);
```

### Custom XP Sources

You can create custom XP sources:

```java
// Register XP values for custom actions
api.registerCustomXPSource("CUSTOM_QUEST", "questName", 50.0);
```

## Maven/Gradle Integration

### Maven

```xml
<repositories>
    <repository>
        <id>rpgskills-repo</id>
        <url>https://repo.frizzlenpop.org/rpgskills</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.frizzlenpop</groupId>
        <artifactId>rpgskills</artifactId>
        <version>1.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Gradle

```gradle
repositories {
    maven { url = 'https://repo.frizzlenpop.org/rpgskills' }
}

dependencies {
    compileOnly 'org.frizzlenpop:rpgskills:1.0.0'
}
```

## API Class Reference

The API includes these main classes:

- **RPGSkillsAPI** - Main API access point
- **SkillData** - Contains skill level and XP information
- **Party** - Represents a player party
- **BoosterData** - Contains XP booster information
- **XPSource** - Enum of XP sources (MINING, LOGGING, CUSTOM, etc.)

## Best Practices

1. **Always check if the API is available** before using it
2. **Handle exceptions** that might occur during API calls
3. **Don't override core functionality** without understanding the consequences
4. **Consider performance impact** of frequent API calls
5. **Update your integration** when new API versions are released

## API Version Compatibility

| Plugin Version | API Version | Minecraft Versions |
|----------------|-------------|-------------------|
| 1.0.0 - 1.5.0  | 1.0         | 1.16.5 - 1.17.1   |
| 1.6.0+         | 2.0         | 1.18+             |

## Support & Issues

If you encounter issues with the API or have questions:

1. Check the [GitHub repository](https://github.com/frizzlenpop/RPGSkillsPlugin) for the latest updates
2. Join our [Discord server](https://discord.gg/rpgskills) for developer support
3. Submit an issue on GitHub with detailed information about your integration

---

*This API documentation is subject to change as the plugin evolves. Always refer to the latest documentation for the most up-to-date information.* 