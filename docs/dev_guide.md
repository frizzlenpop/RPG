# Developer Guide

This guide is intended for developers who want to extend or modify the RPG Skills Plugin, create addons, or integrate it with their own plugins.

## Project Structure

The RPG Skills Plugin follows a modular architecture with these main components:

```
org.frizzlenpop.rpgskills/
├── RPGSkills.java                  # Main plugin class
├── api/                            # Public API package
│   ├── RPGSkillsAPI.java           # API interface
│   └── impl/                       # API implementation
├── commands/                       # Command handlers
├── config/                         # Configuration management
├── data/                           # Data storage and management
│   ├── PlayerData.java
│   ├── SkillData.java
│   └── storage/                    # Storage implementations
├── events/                         # Custom events
├── gui/                            # GUI management
├── listeners/                      # Event listeners
├── managers/                       # System managers
│   ├── SkillManager.java
│   ├── XPManager.java
│   ├── PassiveSkillManager.java
│   ├── SkillTreeManager.java
│   ├── PartyManager.java
│   └── XPBoosterManager.java
├── models/                         # Data models
├── skills/                         # Skill definitions
├── utils/                          # Utility classes
└── integrations/                   # Third-party integrations
```

## Setting Up Development Environment

1. **Clone the repository**:
   ```bash
   git clone https://github.com/frizzlenpop/RPGSkillsPlugin.git
   ```

2. **Set up with Maven**:
   ```bash
   cd RPGSkillsPlugin
   mvn clean install
   ```

3. **Import into your IDE**:
   - For IntelliJ IDEA: File > Open > Select the pom.xml
   - For Eclipse: File > Import > Maven > Existing Maven Projects

## Creating an Addon

### Basic Addon Structure

```
your-addon/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── yourdomain/
│                   └── rpgskillsaddon/
│                       ├── YourAddon.java             # Main class
│                       ├── listeners/                 # Your custom listeners
│                       ├── commands/                  # Your custom commands
│                       └── integrations/              # Your custom integrations
└── pom.xml                                           # Maven configuration
```

### Addon Main Class Example

```java
package com.yourdomain.rpgskillsaddon;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.rpgskills.api.RPGSkillsAPI;

public class YourAddon extends JavaPlugin {
    private RPGSkillsAPI rpgSkillsAPI;
    
    @Override
    public void onEnable() {
        // Check if RPG Skills is loaded
        Plugin rpgSkills = getServer().getPluginManager().getPlugin("RPGSkills");
        if (rpgSkills == null || !rpgSkills.isEnabled()) {
            getLogger().severe("RPG Skills not found or not enabled! Disabling addon.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Get the API
        rpgSkillsAPI = ((RPGSkills) rpgSkills).getAPI();
        
        // Register your listeners
        getServer().getPluginManager().registerEvents(new YourListener(this), this);
        
        // Register your commands
        getCommand("youraddoncommand").setExecutor(new YourCommand(this));
        
        getLogger().info("Your RPG Skills Addon has been enabled!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Your RPG Skills Addon has been disabled!");
    }
    
    public RPGSkillsAPI getRPGSkillsAPI() {
        return rpgSkillsAPI;
    }
}
```

### plugin.yml Example

```yaml
name: YourRPGSkillsAddon
version: 1.0.0
main: com.yourdomain.rpgskillsaddon.YourAddon
api-version: 1.16
depend: [RPGSkills]
commands:
  youraddoncommand:
    description: Your addon command
    usage: /youraddoncommand
    permission: youraddon.command
permissions:
  youraddon.command:
    description: Allows use of your addon command
    default: true
```

## Common Extension Points

### Adding Custom Skills

```java
// Register a new custom skill
rpgSkillsAPI.registerCustomSkill("alchemy", "Alchemy", Material.BREWING_STAND);

// Register XP sources for the custom skill
Map<String, Double> xpValues = new HashMap<>();
xpValues.put("BLAZE_POWDER", 5.0);
xpValues.put("GHAST_TEAR", 10.0);
xpValues.put("GOLDEN_APPLE", 15.0);
rpgSkillsAPI.registerCustomSkillXPValues("alchemy", xpValues);

// Register a custom XP listener
public class AlchemyListener implements Listener {
    private final RPGSkillsAPI api;
    
    public AlchemyListener(RPGSkillsAPI api) {
        this.api = api;
    }
    
    @EventHandler
    public void onBrewComplete(BrewEvent event) {
        // Get the player who brewed the potion
        Player player = findPlayerWhoTriggeredBrew(event);
        if (player != null) {
            // Add XP for brewing
            api.addXP(player, "alchemy", 10.0, XPSource.CUSTOM, 1.0);
        }
    }
}
```

### Adding Custom Passive Abilities

```java
// Register a custom passive ability for the alchemy skill
PassiveAbilityBuilder builder = new PassiveAbilityBuilder("potionMaster")
    .displayName("Potion Master")
    .description("Chance to brew two potions at once")
    .icon(Material.POTION)
    .skillName("alchemy")
    .levelRequirement(15)
    .triggerChance(0.25);
    
rpgSkillsAPI.registerCustomPassiveAbility(builder.build());

// Implement the passive ability logic
@EventHandler
public void onBrewComplete(BrewEvent event) {
    Player player = findPlayerWhoTriggeredBrew(event);
    if (player != null) {
        if (rpgSkillsAPI.hasPassiveAbility(player, "potionMaster")) {
            // Get the trigger chance
            double chance = rpgSkillsAPI.getPassiveTriggerChance(player, "potionMaster");
            
            // Roll for ability trigger
            if (Math.random() < chance) {
                // Duplicate the brewed potion
                duplicateBrewedPotion(event);
                
                // Notify the player
                player.sendMessage("Your Potion Master ability created an extra potion!");
            }
        }
    }
}
```

### Adding Custom Active Abilities

```java
// Register a custom active ability
ActiveAbilityBuilder builder = new ActiveAbilityBuilder("alchemicalSurge")
    .displayName("Alchemical Surge")
    .description("Instantly brew all in-progress potions")
    .icon(Material.BREWING_STAND)
    .skillName("alchemy")
    .levelRequirement(25)
    .cooldown(300); // 5 minutes
    
rpgSkillsAPI.registerCustomActiveAbility(builder.build());

// Implement the ability execution
public class AlchemicalSurgeAbility implements AbilityExecutor {
    @Override
    public boolean execute(Player player) {
        // Find all brewing stands within 10 blocks
        for (Block block : getNearbyBlocks(player.getLocation(), 10)) {
            if (block.getType() == Material.BREWING_STAND) {
                BrewingStand stand = (BrewingStand) block.getState();
                // Complete the brewing process instantly
                completeBrewingProcess(stand);
            }
        }
        
        // Play effects
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.0f);
        player.spawnParticle(Particle.SPELL_WITCH, player.getLocation(), 50, 1, 1, 1, 0.1);
        
        return true;
    }
}
```

### Creating Custom Skill Tree Nodes

```java
// Register a custom skill tree node
SkillTreeNodeBuilder builder = new SkillTreeNodeBuilder("alchemy_master")
    .displayName("Alchemy Master")
    .description("Increases potion duration by 25%")
    .icon(Material.DRAGON_BREATH)
    .cost(3) // Skill points cost
    .requirements(Map.of(
        "player_level", 20,
        "alchemy_level", 30
    ))
    .effects(Map.of(
        "potion_duration_multiplier", 1.25
    ))
    .position(5, 3) // Position in the skill tree UI
    .connections(List.of("alchemy_efficiency")); // Connected nodes
    
rpgSkillsAPI.registerCustomSkillTreeNode(builder.build());

// Implement the node effect
@EventHandler
public void onPotionEffect(EntityPotionEffectEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    
    Player player = (Player) event.getEntity();
    
    if (rpgSkillsAPI.isNodeUnlocked(player, "alchemy_master")) {
        PotionEffect oldEffect = event.getOldEffect();
        PotionEffect newEffect = event.getNewEffect();
        
        if (newEffect != null && event.getCause() == EntityPotionEffectEvent.Cause.CONSUMPTION) {
            // Calculate new duration (25% longer)
            int newDuration = (int) (newEffect.getDuration() * 1.25);
            
            // Apply modified effect
            event.setCancelled(true);
            player.addPotionEffect(new PotionEffect(
                newEffect.getType(),
                newDuration,
                newEffect.getAmplifier(),
                newEffect.isAmbient(),
                newEffect.hasParticles(),
                newEffect.hasIcon()
            ));
        }
    }
}
```

## Advanced Integration

### Custom Data Storage

You can create custom data storage systems by implementing the `DataStorage` interface:

```java
public class CustomDataStorage implements DataStorage {
    // Implement the required methods
    @Override
    public void savePlayerData(UUID playerId, PlayerData data) {
        // Your implementation
    }
    
    @Override
    public PlayerData loadPlayerData(UUID playerId) {
        // Your implementation
        return data;
    }
    
    // Other required methods...
}

// Register your custom storage
rpgSkillsAPI.registerCustomStorage(new CustomDataStorage());
```

### Custom GUI Extensions

You can extend the plugin's GUI system:

```java
public class CustomSkillGUI extends BaseGUI {
    public CustomSkillGUI(Player player) {
        super(player, "Your Custom Skill GUI", 3);
        
        // Add your custom items
        setItem(13, createInfoItem());
        
        // Add custom actions
        setAction(13, event -> {
            // Handle click
            return true;
        });
    }
    
    private ItemStack createInfoItem() {
        return new ItemBuilder(Material.DIAMOND)
            .name("§6Special Skill Info")
            .lore("§7Click for special information")
            .build();
    }
}

// Open your custom GUI
new CustomSkillGUI(player).open();
```

### Hooking Into Economy

```java
public class CustomEconomyHook {
    private final RPGSkillsAPI api;
    private final Economy economy;
    
    public CustomEconomyHook(RPGSkillsAPI api, Economy economy) {
        this.api = api;
        this.economy = economy;
    }
    
    // Sell skill-based products
    public boolean sellProduct(Player player, String skillName, String product, double price) {
        // Check skill requirements
        if (!api.hasSkillLevel(player, skillName, getRequiredLevel(product))) {
            player.sendMessage("You don't have the required " + skillName + " level to sell this product!");
            return false;
        }
        
        // Add money to player
        economy.depositPlayer(player, price);
        
        // Add a small amount of XP for the transaction
        api.addXP(player, skillName, getXPForSale(product), XPSource.CUSTOM, 1.0);
        
        player.sendMessage("You sold " + product + " for " + price + " coins!");
        return true;
    }
}
```

## Best Development Practices

1. **Graceful Degradation**: If RPG Skills isn't present, your addon should disable gracefully
2. **Respect Configurations**: Don't override user configurations
3. **Efficient Event Handling**: Only listen to events you need
4. **Player Data Validation**: Always validate player data before using it
5. **Error Handling**: Implement proper try/catch blocks for all API calls
6. **Documentation**: Document your addon's features and commands
7. **Version Checking**: Verify API version compatibility at startup

## Common Pitfalls

1. **Direct Access to Internal Classes**: Always use the API, not internal classes
2. **Heavy Operations**: Avoid heavy calculations in event listeners
3. **Database Overloading**: Don't save data too frequently
4. **Memory Leaks**: Clean up resources, especially when players log out
5. **Conflicting Permissions**: Use unique permission nodes for your addon

## Testing Your Integration

1. **Set up a test server** with RPG Skills installed
2. **Create test scenarios** for each feature of your addon
3. **Test with different configurations** of RPG Skills
4. **Check for conflicts** with other plugins
5. **Verify performance** under load

## Contributing to the Main Plugin

If you'd like to contribute to the main RPG Skills Plugin:

1. **Fork the repository** on GitHub
2. **Create a feature branch**
3. **Make your changes**
4. **Add tests** if applicable
5. **Submit a pull request** with a clear description of the changes

### Coding Standards

- Follow Java naming conventions
- Use meaningful variable and method names
- Comment complex code sections
- Write JavaDoc for public methods
- Keep methods small and focused
- Follow the existing code structure

## Resources

- [GitHub Repository](https://github.com/frizzlenpop/RPGSkillsPlugin)
- [Issue Tracker](https://github.com/frizzlenpop/RPGSkillsPlugin/issues)
- [API Documentation](api.md)
- [Discord Support Channel](https://discord.gg/rpgskills)

---

*For specific API methods and events, refer to the [API Documentation](api.md).* 