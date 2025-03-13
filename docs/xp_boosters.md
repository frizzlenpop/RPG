# XP Boosters

## Overview

The XP Boosters system allows server administrators to create special tools and weapons that provide XP multipliers when used. This system adds an exciting dimension to gameplay, allowing for rare or special items that help players level up faster in specific skills.

## Key Features

- **Tool-Specific Boosters**: Apply boosters to specific tools or weapons
- **Skill-Specific Boosts**: Each booster applies to a particular skill
- **Configurable Multipliers**: Set custom XP multiplier values
- **Temporary or Permanent**: Create time-limited or permanent boosters
- **Visual Indicators**: Boosters are visible in item lore
- **Automatic Expiration**: Temporary boosters expire after their duration

## How XP Boosters Work

XP boosters are special enchantments applied to tools or weapons that multiply the XP gained when using that item for a specific skill. For example:

- A pickaxe with a Mining XP Booster will grant bonus XP when mining
- A sword with a Fighting XP Booster will grant bonus XP when killing mobs
- A fishing rod with a Fishing XP Booster will grant bonus XP when catching fish

## Admin Commands

The XP Booster system is managed through the `/rpgbooster` command:

| Command | Description | Permission |
|---------|-------------|------------|
| `/rpgbooster apply <player> <skill> <multiplier> [duration]` | Apply a booster to a player's held item | `rpgskills.admin.xpbooster` |
| `/rpgbooster remove <player>` | Remove a booster from a player's held item | `rpgskills.admin.xpbooster` |
| `/rpgbooster check` | Check if your held item has a booster | `rpgskills.admin.xpbooster` |
| `/rpgbooster help` | Show help information | `rpgskills.admin.xpbooster` |

### Applying Boosters

To apply a booster to a player's held item:

```
/rpgbooster apply <player> <skill> <multiplier> [duration]
```

Parameters:
- `<player>`: The player who will receive the booster
- `<skill>`: The skill to boost (mining, logging, farming, etc.)
- `<multiplier>`: The XP multiplier (1.5 = 50% more XP)
- `[duration]`: Optional. Duration in seconds, or time format (10s, 5m, 2h, 7d)

If duration is omitted, the booster is permanent.

Examples:
```
/rpgbooster apply Steve mining 1.5
/rpgbooster apply Alex fishing 2.0 3600
/rpgbooster apply John fighting 3.0 2h
```

### Removing Boosters

To remove a booster from a player's held item:

```
/rpgbooster remove <player>
```

Example:
```
/rpgbooster remove Steve
```

### Checking Boosters

Players can check if their held item has a booster:

```
/rpgbooster check
```

## XP Multipliers

Boosters can provide various multiplier values:

- **1.1x - 1.5x**: Common boosters (10-50% bonus XP)
- **1.5x - 2.0x**: Rare boosters (50-100% bonus XP)
- **2.0x - 5.0x**: Epic boosters (100-400% bonus XP)
- **5.0x - 10.0x**: Legendary boosters (400-900% bonus XP)

The multiplier is applied to the base XP value before other modifiers like passive skill bonuses or party sharing.

## Booster Duration

Boosters can be:

- **Temporary**: Last for a specific duration, then expire
- **Permanent**: Last indefinitely until removed

Duration can be specified in:
- Seconds (default if just a number)
- Time formats: `30s` (seconds), `5m` (minutes), `2h` (hours), `7d` (days)

## Visual Appearance

When a booster is applied to an item, it adds special lore text:

```
⚡ XP Booster ⚡
Skill: Mining
Bonus: +50%
Duration: 2h
```

For permanent boosters, the duration shows as "Permanent".

## Tool Validation

Boosters can only be applied to appropriate tools for each skill:

| Skill | Valid Tools |
|-------|-------------|
| Mining | Pickaxes |
| Logging | Axes |
| Farming | Hoes |
| Fighting | Swords, Bows, Crossbows, Tridents |
| Fishing | Fishing Rods |
| Excavation | Shovels |
| Repair | Any repairable tool |
| Enchanting | Books, Enchanting Tables |

If a player tries to apply a booster to an inappropriate tool, they will receive an error message.

## How Boosted XP is Calculated

The formula for calculating boosted XP is:

```
Boosted XP = Base XP × Booster Multiplier
```

This calculation happens before other modifiers like passive skills or party sharing are applied.

## Booster Expiration

When a temporary booster expires:
1. The booster effect stops working
2. The booster information is removed from the item's lore
3. The item returns to its normal state

Expiration is checked whenever the item is used for its skill.

## Booster Stacking

XP Boosters do not stack with other boosters on the same item. If a new booster is applied to an item that already has a booster, the old one is replaced.

However, XP Boosters do stack multiplicatively with other XP bonuses from:
- Passive skill bonuses
- Skill tree perks
- Party XP bonuses

For example, a 1.5x XP Booster combined with a 1.2x passive skill bonus would result in a 1.8x total multiplier (1.5 × 1.2 = 1.8).

## Technical Implementation

The XP Booster system is implemented through:

- `XPBoosterManager.java`: Core class for managing booster application and calculation
- `XPBoosterCommand.java`: Handles the admin commands
- Item metadata using PersistentDataContainer to store booster information
- Integration with the XP system to apply boosters during XP calculations

## Integration with Other Systems

The XP Boosters system integrates with:

- [XP System](xp_system.md): Applies multipliers to XP gain
- [Skills System](skills_system.md): Boosts specific skills
- [Party System](party_system.md): Boosted XP can be shared with party members 