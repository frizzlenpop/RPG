# Configuration Guide

## Overview

The RPG Skills Plugin provides extensive configuration options that allow server administrators to customize nearly every aspect of the plugin. This guide covers all configuration files, their settings, and how to adjust them to suit your server's needs.

## Main Configuration Files

The plugin uses several YAML configuration files:

| File | Purpose |
|------|---------|
| `config.yml` | Main configuration file with general settings |
| `skills.yml` | Configuration for skills, XP values, and level requirements |
| `passives.yml` | Configuration for passive abilities |
| `abilities.yml` | Configuration for active abilities |
| `skill_tree.yml` | Configuration for the skill tree |
| `party.yml` | Configuration for the party system |
| `messages.yml` | Customizable messages and notifications |

## config.yml

This is the main configuration file that controls general plugin settings.

### General Settings

```yaml
general:
  debug_mode: false                  # Enables debug logging
  data_save_interval: 300            # How often to save player data (seconds)
  display_level_up_message: true     # Show level up messages
  display_xp_notifications: true     # Show XP gain notifications
  economy_enabled: true              # Enable economy integration
  max_player_level: 100              # Maximum player level
```

### XP Settings

```yaml
xp:
  global_multiplier: 1.0             # Global XP multiplier applied to all skills
  level_formula: "100 * level"       # Formula for calculating level requirements
  skill_multipliers:                 # Individual skill multipliers
    mining: 1.0
    logging: 1.0
    farming: 1.0
    fishing: 1.0
    fighting: 1.0
    enchanting: 1.0
    excavation: 1.0
    repair: 1.0
```

### GUI Settings

```yaml
gui:
  enable_sounds: true                # Play sounds in GUIs
  skills_gui_rows: 3                 # Number of rows in skills GUI
  abilities_gui_rows: 3              # Number of rows in abilities GUI
  skilltree_gui_size: 5              # Size of skill tree GUI (NxN)
```

### Scoreboard Settings

```yaml
scoreboard:
  enabled_by_default: true           # Enable scoreboard for new players
  update_interval: 20                # Update interval in ticks (20 = 1 second)
  display_skills: 6                  # Number of skills to display
```

## skills.yml

This file configures individual skills, including XP values for different actions.

### Skill Configuration

```yaml
skills:
  mining:
    enabled: true
    display_name: "Mining"
    icon: DIAMOND_PICKAXE
    max_level: 100
    xp_values:
      STONE: 1
      COAL_ORE: 5
      IRON_ORE: 10
      GOLD_ORE: 15
      DIAMOND_ORE: 30
      EMERALD_ORE: 35
      ANCIENT_DEBRIS: 50
      
  logging:
    enabled: true
    display_name: "Logging"
    icon: DIAMOND_AXE
    max_level: 100
    xp_values:
      OAK_LOG: 5
      BIRCH_LOG: 5
      SPRUCE_LOG: 5
      JUNGLE_LOG: 5
      ACACIA_LOG: 5
      DARK_OAK_LOG: 5
      
  # ... other skills configurations
```

## passives.yml

This file configures passive abilities for all skills.

### Passive Ability Configuration

```yaml
passives:
  mining:
    doubleOreChance:
      enabled: true
      display_name: "Double Ore Drop"
      description: "Chance to get double ore drops"
      icon: DIAMOND_ORE
      level_requirement: 5
      chance: 0.15
      
    autoSmelt:
      enabled: true
      display_name: "Auto Smelt"
      description: "Chance to automatically smelt ores"
      icon: FURNACE
      level_requirement: 10
      chance: 0.25
      
  # ... other passive ability configurations
```

## abilities.yml

This file configures active abilities that players can use.

### Active Ability Configuration

```yaml
abilities:
  miningburst:
    enabled: true
    display_name: "Mining Burst"
    description: "Triple mining speed for 5 seconds"
    icon: GOLDEN_PICKAXE
    skill: mining
    level_requirement: 15
    cooldown: 60
    duration: 5
    speed_multiplier: 3.0
    
  timberchop:
    enabled: true
    display_name: "Timber Chop"
    description: "Instantly cut down entire trees"
    icon: GOLDEN_AXE
    skill: logging
    level_requirement: 15
    cooldown: 30
    max_logs: 64
    
  # ... other active ability configurations
```

## skill_tree.yml

This file configures the skill tree nodes and their relationships.

### Skill Tree Configuration

```yaml
skill_tree:
  nodes:
    mining_efficiency_1:
      display_name: "Mining Efficiency I"
      description: "Increases mining speed by 10%"
      icon: IRON_PICKAXE
      cost: 1
      requirements:
        player_level: 5
        mining_level: 10
      effects:
        mining_speed_multiplier: 1.1
      position:
        x: 1
        y: 1
      connections:
        - mining_efficiency_2
        
    # ... other node configurations
```

## party.yml

This file configures the party system.

### Party Configuration

```yaml
party:
  enabled: true
  default_max_size: 4
  default_xp_share_percent: 0.2
  min_xp_share_percent: 0.0
  max_xp_share_percent: 0.5
  
  perks:
    xp_boost_1:
      display_name: "XP Boost I"
      description: "+5% XP for all party members"
      icon: EXPERIENCE_BOTTLE
      cost: 5000
      required_level: 1
      multiplier: 1.05
      
    # ... other perk configurations
```

## messages.yml

This file allows you to customize all messages displayed by the plugin.

### Message Configuration

```yaml
messages:
  prefix: "&8[&bRPGSkills&8] "
  
  skills:
    level_up: "&a✨ Your %skill% skill is now level %level%!"
    xp_gain: "&b+%xp% %skill% XP"
    
  abilities:
    cooldown: "&cAbility is on cooldown for %time% seconds!"
    activated: "&aActivated %ability%!"
    
  party:
    created: "&aParty created successfully!"
    invited: "&aYou have invited %player% to your party!"
    invitation: "&a%player% has invited you to join their party!"
    
  # ... other message configurations
```

## Advanced Configuration

### Custom XP Formulas

The plugin supports custom XP formulas using a simple expression syntax:

```yaml
xp:
  level_formula: "100 * level ^ 1.5"  # More progressive scaling
```

### Integration Settings

Settings for integrations with other plugins:

```yaml
integrations:
  vault:
    enabled: true
    
  placeholderapi:
    enabled: true
    
  worldguard:
    enabled: true
    respect_regions: true
    
  mcmmo:
    enabled: false
    skill_conversion_rate: 0.5
```

### Custom Skill Settings

For adding completely new skills:

```yaml
custom_skills:
  alchemy:
    enabled: true
    display_name: "Alchemy"
    icon: BREWING_STAND
    xp_values:
      # ... custom XP values
```

## Configuration Tips

1. **Backup First**: Always make a backup of configuration files before making changes.

2. **Reload After Changes**: Use `/rpgskills reload` after making configuration changes.

3. **Validate YAML**: Use a YAML validator to check for syntax errors before reloading.

4. **Test on Development Server**: Test major configuration changes on a test server first.

5. **Documentation References**: Refer to the specific documentation pages for each system for detailed configuration options.

## Common Configuration Examples

### Balancing XP Gain

To make skills level up faster or slower:

```yaml
xp:
  global_multiplier: 1.5  # 50% more XP for all skills
```

### Customizing Party System

To adjust the party XP sharing system:

```yaml
party:
  default_xp_share_percent: 0.3  # Share 30% of XP by default
  max_xp_share_percent: 0.7      # Allow up to 70% XP sharing
```

### Adjusting Ability Cooldowns

To make abilities usable more or less frequently:

```yaml
abilities:
  miningburst:
    cooldown: 30  # Reduce cooldown to 30 seconds
```

## Technical Implementation

The configuration system is implemented through:

- YAML file parsing using Bukkit's configuration API
- Default configuration generation on first run
- Configuration reloading with `/rpgskills reload`
- Validation checks to ensure valid configuration values 