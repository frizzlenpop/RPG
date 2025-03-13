# XP System

## Overview

The XP (Experience Points) System is the foundation of progression in the RPG Skills Plugin. It tracks player advancement across different skills and determines when players level up and unlock new abilities.

## How XP Works

For each skill in the plugin, players earn XP by performing specific actions related to that skill. The XP accumulates until the player reaches the threshold required for the next level, at which point they level up and the XP counter resets for the next level.

## XP Gain Methods

### Activity-Based XP

Each skill has its own set of activities that award XP:

| Skill | XP Sources |
|-------|------------|
| Mining | Breaking ores, stone blocks |
| Logging | Chopping wood logs |
| Farming | Harvesting fully grown crops |
| Fighting | Defeating mobs and players |
| Fishing | Catching fish and treasure |
| Enchanting | Using enchantment tables |
| Excavation | Digging dirt, sand, gravel |
| Repair | Repairing tools and weapons |

### XP Values

Different activities award different amounts of XP based on their difficulty or rarity:

#### Mining Examples
- Stone: 1 XP
- Coal Ore: 5 XP
- Iron Ore: 10 XP
- Gold Ore: 15 XP
- Diamond Ore: 30 XP
- Emerald Ore: 35 XP
- Ancient Debris: 50 XP

#### Fighting Examples
- Zombie: 10 XP
- Skeleton: 15 XP
- Creeper: 20 XP
- Enderman: 25 XP
- Ender Dragon: 500 XP
- Wither: 350 XP

*For the complete list of XP values, see the [Configuration](configuration.md) document.*

## XP Multipliers and Boosts

The base XP values can be modified by several factors:

### 1. Passive Skill Multipliers

As players level up skills, they unlock passive abilities that increase XP gain:

- Various passive abilities from the skill tree can increase XP gain by 10-50%
- These multiply the base XP before other modifiers

### 2. XP Boosters

Special items that can be applied to tools to increase XP gain for specific skills:

- Booster multipliers range from 1.1x (10%) to 10.0x (900%)
- Boosters can be temporary or permanent
- For more details, see the [XP Boosters](xp_boosters.md) documentation

### 3. Party XP Sharing

When in a party, XP can be shared with other party members:

- A configurable percentage of XP is shared among party members
- Party bonuses can increase the total XP gained
- Higher party levels provide better XP sharing benefits
- See the [Party System](party_system.md) for more details

## XP Calculation Formula

The final XP awarded to a player is calculated using this formula:

```
Final XP = Base XP × Passive Multiplier × Booster Multiplier
```

If the player is in a party, the XP sharing calculation is:

```
Shared XP = Final XP × Party Share Percentage
Player XP = Final XP - Shared XP
```

The shared XP is then distributed among party members based on party settings.

## Level Requirements

The amount of XP required to advance from one level to the next follows this formula:

```
Required XP = 100 × Current Level
```

Examples:
- Level 1 to 2: 100 XP
- Level 2 to 3: 200 XP
- Level 5 to 6: 500 XP
- Level 10 to 11: 1,000 XP
- Level 20 to 21: 2,000 XP

## XP Notifications

When a player earns XP, they receive an action bar message that shows:

- The skill that earned XP
- The amount of XP gained
- Any bonus XP from multipliers
- XP shared from party members (if applicable)

These notifications can be toggled on/off with the `/toggleskillmessages` command.

## XP Storage and Persistence

Player XP data is stored in YAML files in the plugin's data folder:

- Each player has their own data file identified by UUID
- XP values are saved when players log out or the server shuts down
- The data structure includes current XP and level for each skill

## Admin Commands for XP

Server administrators can manipulate player XP using these commands:

```
/skillsadmin <player> <skill> set xp <amount>
/skillsadmin <player> <skill> add xp <amount>
```

For more information, see the [Admin Commands](admin_commands.md) documentation.

## Integration with Other Systems

The XP System integrates with several other plugin components:

- [Skills System](skills_system.md): XP drives skill progression
- [Passive Skills](passive_skills.md): Unlocked at specific XP thresholds
- [Active Abilities](active_abilities.md): Become available at level 15
- [Skill Tree](skill_tree.md): Uses skill levels to determine available nodes
- [Party System](party_system.md): Enables XP sharing between players
- [XP Boosters](xp_boosters.md): Enhances XP gain through special items

## Technical Implementation

For developers, the XP system is implemented through:

- `XPManager.java`: Core class for XP calculations and management
- `PlayerDataManager.java`: Handles data persistence
- Event listeners for different skills that hook into player actions
- XP notification system through action bar messages 