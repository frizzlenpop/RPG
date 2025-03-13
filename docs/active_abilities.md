# Active Abilities

## Overview

Active Abilities are special skills that players can manually activate using commands. Unlike passive abilities that work automatically, active abilities provide powerful temporary effects but are limited by cooldowns and require a minimum skill level to unlock.

## Unlocking Active Abilities

Active abilities are unlocked when a player reaches level 15 in the corresponding skill. This represents a significant milestone in skill progression and rewards dedicated players with powerful new capabilities.

## Available Active Abilities

### Mining Abilities

| Ability | Command | Description | Effect | Cooldown |
|---------|---------|-------------|--------|----------|
| Mining Burst | `/miningburst` | Temporary mining speed boost | Triple mining speed for 5 seconds | 60 seconds |

### Logging Abilities

| Ability | Command | Description | Effect | Cooldown |
|---------|---------|-------------|--------|----------|
| Timber Chop | `/timberchop` | Cut down entire trees instantly | Breaks all connected logs when a tree is chopped | 30 seconds |

### Farming Abilities

| Ability | Command | Description | Effect | Cooldown |
|---------|---------|-------------|--------|----------|
| Super Harvest | `/superharvest` | Mass harvest crops in an area | Harvests all fully grown crops in a 5x5 area | 60 seconds |

### Fighting Abilities

| Ability | Command | Description | Effect | Cooldown |
|---------|---------|-------------|--------|----------|
| Berserker Rage | `/berserkerrage` | Temporary damage boost | +50% damage for 10 seconds | 90 seconds |

### Fishing Abilities

| Ability | Command | Description | Effect | Cooldown |
|---------|---------|-------------|--------|----------|
| Instant Catch | `/instantcatch` | Instantly catch fish | Next cast catches fish immediately | 120 seconds |

### Enchanting Abilities

| Ability | Command | Description | Effect | Cooldown |
|---------|---------|-------------|--------|----------|
| Double Enchant | `/doubleenchant` | Enhanced enchanting | Next enchantment has double power | 180 seconds |

### Excavation Abilities

| Ability | Command | Description | Effect | Cooldown |
|---------|---------|-------------|--------|----------|
| Mass Excavation | `/massexcavate` | Dig large areas quickly | Excavates a 3x3 area | 90 seconds |

### Repair Abilities

| Ability | Command | Description | Effect | Cooldown |
|---------|---------|-------------|--------|----------|
| Perfect Repair | `/perfectrepair` | Superior item repair | Next repair restores item to full durability | 300 seconds |

## Using Active Abilities

### Direct Commands

Players can activate abilities using their specific commands (e.g., `/miningburst`, `/timberchop`), provided they have the appropriate permission and their skill level is high enough.

### Abilities Menu

A more user-friendly way to use active abilities is through the abilities menu:

```
/abilities [skill]
```

This opens a GUI displaying all available abilities for a specific skill or all skills if no parameter is provided. Abilities that are on cooldown will show the remaining cooldown time.

## Cooldown System

Each active ability has a cooldown period during which it cannot be used again. This prevents abilities from being overpowered and encourages strategic use.

Cooldowns are:
- Tracked per player
- Persistent across server restarts
- Displayed in the abilities menu
- Configurable in the plugin configuration

## Permissions

Each ability requires a specific permission:

| Ability | Permission |
|---------|------------|
| Mining Burst | `rpgskills.miningburst` |
| Timber Chop | `rpgskills.timberchop` |
| Super Harvest | `rpgskills.superharvest` |
| Berserker Rage | `rpgskills.berserkerrage` |
| Instant Catch | `rpgskills.instantcatch` |
| Double Enchant | `rpgskills.doubleenchant` |
| Mass Excavation | `rpgskills.massexcavate` |
| Perfect Repair | `rpgskills.perfectrepair` |

## Configuration

Active abilities can be configured in the `config.yml` file:

```yaml
abilities:
  miningburst:
    cooldown: 60
    duration: 5
    speedMultiplier: 3.0
  timberchop:
    cooldown: 30
    maxLogs: 64
  berserkerrage:
    cooldown: 90
    duration: 10
    damageMultiplier: 1.5
```

Configuration options include:
- Cooldown time
- Effect duration
- Effect strength
- Area of effect (where applicable)
- Maximum targets/blocks affected

## Visual and Sound Effects

When an ability is activated, players receive:
- A title screen notification
- Particle effects around the player
- Custom sound effects
- Action bar updates about the ability duration

## Technical Implementation

Active abilities are implemented through:

- `SkillAbilityManager.java`: Core class that manages all active abilities
- Individual command handlers for each ability
- Cooldown tracking system
- Integration with player skill levels to verify eligibility

## Integration with Other Systems

The Active Abilities system integrates with:

- [Skills System](skills_system.md): Requires minimum skill level (15) to unlock
- [Skill Tree](skill_tree.md): Can be enhanced by certain skill tree nodes
- [GUI System](gui_system.md): Displays abilities and their cooldowns
- [Configuration](configuration.md): Customizable through plugin settings 