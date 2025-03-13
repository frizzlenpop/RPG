# Permissions Reference

## Overview

The RPG Skills Plugin uses a comprehensive permission system to control access to various features and commands. This document provides a complete list of all available permissions, organized by category, to help server administrators set up appropriate permission structures for their players.

## Basic Permissions

### Player Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `rpgskills.use` | Allows access to the basic skills GUI | true |
| `rpgskills.passive` | Allows access to passive abilities | true |
| `rpgskills.skilltree` | Allows access to the skill tree | true |
| `rpgskills.party` | Allows access to the party system | true |
| `rpgskills.scoreboard` | Allows toggling the skills scoreboard | true |
| `rpgskills.rstat` | Allows viewing XP boosts and multipliers | true |

## Ability Permissions

### Active Ability Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `rpgskills.miningburst` | Allows use of the Mining Burst ability | true |
| `rpgskills.timberchop` | Allows use of the Timber Chop ability | true |
| `rpgskills.berserkerrage` | Allows use of the Berserker Rage ability | true |
| `rpgskills.superharvest` | Allows use of the Super Harvest ability | true |
| `rpgskills.instantcatch` | Allows use of the Instant Catch ability | true |
| `rpgskills.doubleenchant` | Allows use of the Double Enchant ability | true |
| `rpgskills.massexcavate` | Allows use of the Mass Excavation ability | true |
| `rpgskills.perfectrepair` | Allows use of the Perfect Repair ability | true |

## Admin Permissions

### Basic Admin Permission

| Permission | Description | Default |
|------------|-------------|---------|
| `rpgskills.admin` | Master permission for admin features | op |

### Specialized Admin Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `rpgskills.admin.xpbooster` | Allows managing XP boosters | op |
| `rpgskills.admin.party` | Allows admin control of parties | op |
| `rpgskills.admin.reload` | Allows reloading plugin configuration | op |
| `rpgskills.admin.config` | Allows changing plugin configuration | op |
| `rpgskills.admin.manage` | Allows bulk player data management | op |
| `rpgskills.admin.debug` | Allows access to debugging commands | op |

## Permission Group Structure

Below is a recommended permission structure for different player ranks:

### Default Player

```
rpgskills.use
rpgskills.passive
rpgskills.skilltree
rpgskills.party
rpgskills.scoreboard
rpgskills.rstat
```

Plus all active ability permissions:
```
rpgskills.miningburst
rpgskills.timberchop
...etc
```

### VIP Player

All default player permissions, plus:
```
rpgskills.booster.use
rpgskills.booster.check
```

### Moderator

All VIP permissions, plus:
```
rpgskills.admin.party
```

### Administrator

All moderator permissions, plus:
```
rpgskills.admin
rpgskills.admin.xpbooster
rpgskills.admin.reload
rpgskills.admin.config
```

### Owner/Developer

All permissions:
```
rpgskills.*
```

## LuckPerms Setup Examples

Here are some examples of how to set up these permissions using LuckPerms:

### Setting up a default group:

```
/lp group default permission set rpgskills.use true
/lp group default permission set rpgskills.passive true
/lp group default permission set rpgskills.skilltree true
/lp group default permission set rpgskills.party true
/lp group default permission set rpgskills.scoreboard true
/lp group default permission set rpgskills.rstat true
/lp group default permission set rpgskills.miningburst true
/lp group default permission set rpgskills.timberchop true
/lp group default permission set rpgskills.berserkerrage true
... etc for other abilities
```

### Setting up an admin group:

```
/lp group admin permission set rpgskills.admin true
```

## Wildcards and Parent Permissions

The plugin supports permission wildcards for easy management:

| Wildcard | Description |
|----------|-------------|
| `rpgskills.*` | All permissions |
| `rpgskills.admin.*` | All admin permissions |
| `rpgskills.*.burst` | All burst abilities across skills |

## Permission Nodes Hierarchy

The permission system follows this hierarchy:

```
rpgskills
├── use
├── passive
├── skilltree
├── party
├── scoreboard
├── rstat
├── miningburst
├── timberchop
├── ... (other abilities)
├── admin
│   ├── xpbooster
│   ├── party
│   ├── reload
│   ├── config
│   ├── manage
│   └── debug
```

## Negating Permissions

To specifically deny a permission, prefix it with a minus sign in LuckPerms:

```
/lp group vip permission set -rpgskills.admin.xpbooster true
```

## Default Permission Settings

By default, the plugin grants these permissions:

- All basic player permissions to everyone
- All active ability permissions to everyone
- All admin permissions only to operators

You can modify these defaults in the plugin's `config.yml`:

```yaml
permissions:
  default_player_permissions: true
  ability_permissions_require_level_unlock: true
  op_has_all_permissions: true
```

## Permission Checks

The plugin checks permissions at these times:

1. When a player joins the server
2. When a player tries to use a command
3. When a player tries to use an ability
4. When a player tries to access a GUI

## Technical Implementation

Permission checks are implemented through:

- Bukkit's built-in permission system
- Command registration with required permissions
- Event listeners that check permissions before acting
- GUI interactions that verify permissions 