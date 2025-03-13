# Admin Commands

## Overview

The RPG Skills Plugin includes several powerful admin commands that allow server operators to manage the plugin, adjust player skills, and configure various aspects of the system. These commands are designed for server administrators and moderators to help manage the progression system effectively.

## Admin Permission

Most admin commands require the `rpgskills.admin` permission. This should be assigned only to trusted staff members as these commands can significantly alter player progression.

## SkillsAdmin Command

The main command for managing player skills is `/skillsadmin`:

```
/skillsadmin <player> <skill> <set/add> <level/xp> <amount>
```

### Parameters

- `<player>`: The target player's name
- `<skill>`: The skill to modify (mining, logging, farming, fishing, fighting, enchanting, excavation, repair)
- `<set/add>`: Whether to set to a specific value or add to the current value
- `<level/xp>`: Whether to modify the skill level or XP
- `<amount>`: The amount to set or add

### Examples

```
# Set a player's mining level to 10
/skillsadmin PlayerName mining set level 10

# Add 5 levels to a player's fishing skill
/skillsadmin PlayerName fishing add level 5

# Set a player's farming XP to 500
/skillsadmin PlayerName farming set xp 500

# Add 1000 XP to a player's fighting skill
/skillsadmin PlayerName fighting add xp 1000
```

### Tab Completion

The command includes tab completion for:
- Player names
- Skill names
- Action types (set/add)
- Value types (level/xp)
- Common amount values

## XP Booster Command

Administrators can manage XP boosters with the `/rpgbooster` command:

```
/rpgbooster <apply/remove/check/help> [parameters]
```

### Subcommands

- **apply**: Apply an XP booster to a player's held item
  ```
  /rpgbooster apply <player> <skill> <multiplier> [duration]
  ```
  Example: `/rpgbooster apply PlayerName mining 1.5 2h`

- **remove**: Remove an XP booster from a player's held item
  ```
  /rpgbooster remove <player>
  ```
  Example: `/rpgbooster remove PlayerName`

- **check**: Check if a held item has an XP booster
  ```
  /rpgbooster check
  ```

- **help**: Show help information about booster commands
  ```
  /rpgbooster help
  ```

For more details, see the [XP Boosters](xp_boosters.md) documentation.

## Skill Tree Admin Commands

Administrators can manage the skill tree with specialized commands:

```
/skilltree reset <player>
```
Resets a player's skill tree, refunding all spent points.

```
/skilltree debug
```
Shows debug information about the skill tree configuration.

## Party Admin Commands

Admins can override normal party limitations with:

```
/rparty forcejoin <player> <leader>
```
Forces a player to join another player's party.

```
/rparty forcekick <player>
```
Forces a player to be removed from their party.

```
/rparty forceperks <leader> <perk>
```
Grants a specific perk to a party without requiring payment.

## Plugin Management Commands

General plugin management commands:

```
/rpgskills reload
```
Reloads the plugin configuration from disk.

```
/rpgskills version
```
Displays the current plugin version information.

```
/rpgskills debug <on/off>
```
Toggles debug mode for troubleshooting.

## Configuration Commands

Commands for managing plugin configuration:

```
/rpgconfig xpmultiplier <skill> <value>
```
Sets the XP multiplier for a specific skill.

```
/rpgconfig cooldown <ability> <seconds>
```
Sets the cooldown time for a specific ability.

```
/rpgconfig maxlevel <skill> <level>
```
Sets the maximum level for a specific skill.

## Bulk Management Commands

Commands for managing multiple players at once:

```
/rpgadmin resetall <skill>
```
Resets the specified skill for all players.

```
/rpgadmin awardall <skill> <xp>
```
Awards XP to all online players for a skill.

```
/rpgadmin importdata <file>
```
Imports player data from a file.

```
/rpgadmin exportdata <file>
```
Exports player data to a file.

## Debugging Commands

Commands for troubleshooting:

```
/rpgdebug player <player>
```
Shows detailed skill information for a player.

```
/rpgdebug listeners
```
Lists all active event listeners for the plugin.

```
/rpgdebug timings <start/stop>
```
Tracks timing information for performance analysis.

## Console Commands

Some commands can be run from the server console:

```
rpgskills:globalboost <skill> <multiplier> <duration>
```
Activates a global XP boost for all players.

```
rpgskills:maintenance <on/off>
```
Puts the skills system in maintenance mode.

## Command Permissions

| Command | Permission |
|---------|------------|
| `/skillsadmin` | `rpgskills.admin` |
| `/rpgbooster` | `rpgskills.admin.xpbooster` |
| `/skilltree reset` | `rpgskills.admin` |
| `/skilltree debug` | `rpgskills.admin` |
| `/rparty force*` | `rpgskills.admin.party` |
| `/rpgskills reload` | `rpgskills.admin.reload` |
| `/rpgconfig` | `rpgskills.admin.config` |
| `/rpgadmin` | `rpgskills.admin.manage` |
| `/rpgdebug` | `rpgskills.admin.debug` |

## Best Practices

1. **Use with Caution**: Admin commands can significantly impact game balance and player progression. Use them sparingly.

2. **Document Changes**: Keep a log of admin actions, especially when modifying player skill levels.

3. **Check Before Setting**: Use player info commands before making significant changes.

4. **Test on Test Servers**: Test major changes on a test server before applying them to your main server.

5. **Regular Backups**: Always maintain backups of player data before making bulk changes.

## Technical Implementation

The admin commands are implemented through:

- `SkillsAdminCommand.java`: Main skills management commands
- `XPBoosterCommand.java`: XP booster management
- `SkillTreeCommand.java`: Skill tree management commands
- Various other command handlers for specific subsystems 