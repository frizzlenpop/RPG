# Skills System

## Overview

The Skills System is the core feature of the RPG Skills Plugin. It allows players to level up 8 different skills by performing related activities in the game. As players gain experience and level up, they unlock passive abilities, active skills, and other benefits.

## Available Skills

| Skill | Activities | Base XP Values |
|-------|------------|----------------|
| Mining | Breaking ores, stone | Stone: 1, Coal: 5, Iron: 10, Gold: 15, Diamond: 30, etc. |
| Logging | Chopping trees | Wood logs: 5 XP per log |
| Farming | Harvesting crops | Wheat/Carrots/Potatoes/Beetroots: 5 XP |
| Fighting | Killing mobs | Varies by mob (Zombie: 10, Skeleton: 15, Enderman: 25, etc.) |
| Fishing | Catching fish | Cod: 5, Salmon: 7, Pufferfish: 10, Tropical Fish: 15 |
| Enchanting | Using enchanting tables | Varies by enchantment level |
| Excavation | Digging with shovels | Dirt: 10, Sand: 15, Gravel: 20, Clay: 25 |
| Repair | Repairing items | Based on material and durability repaired |

## Leveling System

### XP Requirements

The amount of XP required to advance to the next level follows this formula:

```
Required XP = 100 * current_level
```

For example:
- Level 1 to 2: 100 XP
- Level 2 to 3: 200 XP
- Level 3 to 4: 300 XP

### Level Benefits

As players level up their skills, they gain various benefits:

- **Level 5**: Basic passive abilities unlock
- **Level 10**: Intermediate passive abilities unlock
- **Level 15**: Advanced passive abilities + Active ability unlocks
- **Higher levels**: Continued stat improvements and bonuses

## Skill XP Gain

XP is gained naturally through gameplay activities. The amount of XP gained depends on:

1. **Base XP**: Each activity has a base XP value
2. **XP Multipliers**: From passive skills, skill tree, or party bonuses
3. **XP Boosters**: Special items that boost XP gain

### XP Notifications

When players gain XP, they receive action bar messages showing:
- The skill that gained XP
- The amount of XP gained
- Any bonus XP from multipliers
- Shared XP (if in a party)

## Viewing Skills

Players can view their skill levels and progress using:

```
/skills
```

This opens a GUI that displays:
- Current skill levels
- XP progress toward the next level
- Unlocked passive abilities
- Active ability status

## Skill Synergies

Skills are designed to complement each other:

- **Mining + Repair**: Better tools and more efficient repairs
- **Logging + Farming**: Enhanced resource gathering and food production
- **Fighting + Fishing**: Combat abilities and alternative resource gathering
- **Enchanting + All skills**: Enhanced equipment for all activities

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/skills` | Open the skills GUI | `rpgskills.use` |
| `/abilities <skill>` | Activate a skill ability | `rpgskills.<ability>` |
| `/passives` | View passive abilities | `rpgskills.passive` |

## Admin Commands

Server administrators can manage player skills using:

```
/skillsadmin <player> <skill> <set/add> <level/xp> <amount>
```

For more details on admin commands, see the [Admin Commands](admin_commands.md) documentation.

## Configuration

Skill XP gains, level requirements, and other settings can be configured in the plugin's configuration files. See the [Configuration](configuration.md) guide for details.

## Integration with Other Systems

The Skills System integrates with:

- [Passive Skills System](passive_skills.md): Automatic abilities that trigger as you level up
- [Active Abilities](active_abilities.md): Special powers you can activate with commands
- [Skill Tree](skill_tree.md): Advanced progression system that builds on skill levels
- [Party System](party_system.md): Share XP with party members
- [XP Boosters](xp_boosters.md): Items that enhance XP gain 