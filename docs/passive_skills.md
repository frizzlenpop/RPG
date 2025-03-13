# Passive Skills

## Overview

Passive Skills are automatic abilities that activate based on specific in-game conditions once unlocked. Unlike active abilities, they don't require manual activation - they simply enhance normal gameplay activities automatically after players reach certain skill levels.

## Unlocking Passive Skills

Passive skills are unlocked by reaching specific milestone levels in various skills:
- **Level 1**: Basic passives
- **Level 3-5**: Early passives
- **Level 7-10**: Intermediate passives
- **Level 12-15**: Advanced passives
- **Level 17-20**: Master passives

## Passive Skills by Type

### Mining Passives

| Passive Ability | Level | Description | Effect |
|----------------|-------|-------------|--------|
| Mining Basics | 1 | Basic mining efficiency | Slightly faster mining speed |
| Stone Efficiency | 3 | Improved stone mining | Mine stone blocks faster |
| Double Ore Drop | 5 | Chance for double ore drops | 15% chance for double ore drops |
| Mining XP Boost | 7 | Gain bonus XP from mining | +10% mining XP |
| Auto-Smelt | 10 | Chance to automatically smelt ores | 25% chance to get smelted form of ores |
| Coal Specialization | 12 | Better coal mining | +25% XP from coal ores |
| Iron Specialization | 14 | Better iron mining | +25% XP from iron ores |
| Fortune Boost | 15 | Enhanced fortune effects | Fortune enchantments more effective |
| Miner's Haste | 17 | Mining speed buff | Occasional Haste I effect while mining |
| Auto-Smelt Upgrade | 20 | Improved auto-smelting | 50% chance to get smelted form of ores |

### Logging Passives

| Passive Ability | Level | Description | Effect |
|----------------|-------|-------------|--------|
| Woodcutter Basics | 1 | Basic woodcutting | Slightly faster log chopping |
| Double Log Chance | 5 | Chance for double logs | 15% chance for double log drops |
| Leaf Destroyer | 8 | Break leaves faster | Significantly increased leaf breaking speed |
| Logging XP Boost | 10 | Gain bonus XP from logging | +10% logging XP |
| Sapling Finder | 12 | Better sapling drops | +25% chance for sapling drops |
| Timber Sense | 15 | Detect trees nearby | Nearby logs glow when holding an axe |

### Farming Passives

| Passive Ability | Level | Description | Effect |
|----------------|-------|-------------|--------|
| Farmer Basics | 1 | Basic farming | Slightly faster crop harvesting |
| Green Thumb | 3 | Better crop growth | Small chance to speed up crop growth |
| Double Crop Chance | 5 | Chance for double crops | 20% chance for double crop drops |
| Farming XP Boost | 7 | Gain bonus XP from farming | +10% farming XP |
| Seed Master | 10 | Better seed returns | +25% chance for extra seeds when harvesting |
| Auto-Replant | 15 | Automatically replant crops | 50% chance to automatically replant harvested crops |

### Fighting Passives

| Passive Ability | Level | Description | Effect |
|----------------|-------|-------------|--------|
| Combat Basics | 1 | Basic combat training | Small increase in damage |
| Damage Reduction | 5 | Take less damage | 15% damage reduction from all sources |
| Combat XP Boost | 7 | Gain bonus XP from fighting | +10% fighting XP |
| Life Steal | 10 | Heal from attacks | 5% of damage dealt is converted to health |
| Critical Master | 12 | Better critical hits | +15% critical hit chance |
| Combat Rage | 15 | Damage boost when low health | +25% damage when below 30% health |

### Fishing Passives

| Passive Ability | Level | Description | Effect |
|----------------|-------|-------------|--------|
| Fishing Basics | 1 | Basic fishing skills | Slightly faster fishing |
| Treasure Hunter | 5 | Better treasure chances | 15% better chance for treasure items |
| Fishing XP Boost | 7 | Gain bonus XP from fishing | +10% fishing XP |
| Master Angler | 10 | Faster fishing | 20% reduction in fishing time |
| Rare Fish Finder | 15 | Better rare fish chances | 30% better chance for rare fish |

### Enchanting Passives

| Passive Ability | Level | Description | Effect |
|----------------|-------|-------------|--------|
| Enchanting Basics | 1 | Basic enchanting | Slightly better enchanting odds |
| Double Enchant Chance | 5 | Chance for double enchantment power | 10% chance to double enchantment effectiveness |
| Enchanting XP Boost | 7 | Gain bonus XP from enchanting | +10% enchanting XP |
| Lapis Saver | 10 | Chance to not consume lapis | 25% chance to preserve lapis when enchanting |
| Enchantment Preserver | 15 | Keep books when enchanting | 10% chance to preserve book when combining items |

### Excavation Passives

| Passive Ability | Level | Description | Effect |
|----------------|-------|-------------|--------|
| Excavation Basics | 1 | Basic digging | Slightly faster digging |
| Double Drops | 5 | Chance for double resource drops | 15% chance for double drops |
| Archaeology Basics | 8 | Chance to find special items | 12% chance to find special artifacts |
| Treasure Finder | 10 | Find valuable items | 10% chance to find valuable items |
| Rare Find | 15 | Find rare materials | 5% chance to find rare items |
| Multi-Block | 17 | Dig multiple blocks | 8% chance to break adjacent blocks |
| Ancient Artifacts | 20 | Find ancient artifacts | 2% chance to discover ancient artifacts |

### Repair Passives

| Passive Ability | Level | Description | Effect |
|----------------|-------|-------------|--------|
| Repair Basics | 1 | Basic repair skills | Slightly more efficient repairs |
| Material Saver | 5 | Save materials when repairing | 10% chance to not consume materials |
| Experience Saver | 10 | Save XP when repairing | 15% reduction in XP cost for anvil repairs |
| Quality Repair | 15 | Better durability from repairs | 10% bonus durability when repairing items |
| Master Smith | 20 | Very efficient repairs | 20% more efficient repairs overall |

## Viewing Passive Skills

Players can view their unlocked passive skills and the ones they have yet to unlock by using:

```
/passives
```

This will open a GUI that displays all passive skills, with locked skills in gray and unlocked skills in green.

## Configuration

Server administrators can configure passive skills in the `config.yml` file:

```yaml
passives:
  mining:
    doubleOreChance:
      chance: 0.15
      enabled: true
  fishing:
    treasureHunter:
      chance: 0.15
      enabled: true
```

Individual passive abilities can be enabled or disabled, and their effect values can be adjusted.

## Technical Implementation

Passive skills are implemented through:

- `PassiveSkillManager.java`: Core class for managing passive skills
- Event listeners for different game activities that check for passive skill triggers
- Configuration system for adjusting passive skill parameters

## Integration with Other Systems

The Passive Skills system integrates with:

- [Skills System](skills_system.md): Skills level progression unlocks passive abilities
- [XP System](xp_system.md): Passive skills can provide XP boosts
- [Skill Tree](skill_tree.md): Skill tree nodes can enhance passive ability effects
- [Party System](party_system.md): Some party perks can enhance passive ability effects 