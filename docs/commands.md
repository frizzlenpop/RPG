# Commands Reference

This document provides a comprehensive list of all commands available in the RPG Skills Plugin, organized by category. For more detailed information about specific command systems, please refer to their dedicated documentation pages.

## General Commands

### Skills Commands

| Command | Description | Permission | Documentation |
|---------|-------------|------------|---------------|
| `/skills` | Opens the skills GUI | `rpgskills.use` | [Skills System](skills_system.md) |
| `/passives` | Opens the passive skills GUI | `rpgskills.passive` | [Passive Skills](passive_skills.md) |
| `/rstat` | Shows all XP boosts and multipliers | `rpgskills.rstat` | [XP System](xp_system.md) |
| `/toggleskillmessages` | Toggles skill notification messages | none | [XP System](xp_system.md) |

### Skill Tree Commands

| Command | Description | Permission | Documentation |
|---------|-------------|------------|---------------|
| `/skilltree` | Opens the skill tree GUI | `rpgskills.skilltree` | [Skill Tree](skill_tree.md) |
| `/skilltree info` | Shows skill tree progress | `rpgskills.skilltree` | [Skill Tree](skill_tree.md) |
| `/skilltree unlock <nodeId>` | Unlocks a skill tree node | `rpgskills.skilltree` | [Skill Tree](skill_tree.md) |
| `/skilltree level` | Shows player level information | `rpgskills.skilltree` | [Skill Tree](skill_tree.md) |

### Ability Commands

| Command | Description | Permission | Documentation |
|---------|-------------|------------|---------------|
| `/abilities [skill]` | Opens the abilities GUI | varies | [Active Abilities](active_abilities.md) |
| `/miningburst` | Activates Mining Burst ability | `rpgskills.miningburst` | [Active Abilities](active_abilities.md) |
| `/timberchop` | Activates Timber Chop ability | `rpgskills.timberchop` | [Active Abilities](active_abilities.md) |
| `/berserkerrage` | Activates Berserker Rage ability | `rpgskills.berserkerrage` | [Active Abilities](active_abilities.md) |
| `/superharvest` | Activates Super Harvest ability | `rpgskills.superharvest` | [Active Abilities](active_abilities.md) |
| `/instantcatch` | Activates Instant Catch ability | `rpgskills.instantcatch` | [Active Abilities](active_abilities.md) |
| `/doubleenchant` | Activates Double Enchant ability | `rpgskills.doubleenchant` | [Active Abilities](active_abilities.md) |
| `/massexcavate` | Activates Mass Excavation ability | `rpgskills.massexcavate` | [Active Abilities](active_abilities.md) |
| `/perfectrepair` | Activates Perfect Repair ability | `rpgskills.perfectrepair` | [Active Abilities](active_abilities.md) |

### Party Commands

| Command | Description | Permission | Documentation |
|---------|-------------|------------|---------------|
| `/rparty create` | Creates a new party | `rpgskills.party` | [Party System](party_system.md) |
| `/rparty invite <player>` | Invites a player to your party | `rpgskills.party` | [Party System](party_system.md) |
| `/rparty accept` | Accepts a party invitation | `rpgskills.party` | [Party System](party_system.md) |
| `/rparty leave` | Leaves your current party | `rpgskills.party` | [Party System](party_system.md) |
| `/rparty kick <player>` | Kicks a player from your party | `rpgskills.party` | [Party System](party_system.md) |
| `/rparty disband` | Disbands your party | `rpgskills.party` | [Party System](party_system.md) |
| `/rparty list` | Lists all members in your party | `rpgskills.party` | [Party System](party_system.md) |
| `/rparty info` | Shows information about your party | `rpgskills.party` | [Party System](party_system.md) |
| `/rparty chat <message>` | Sends a message to all party members | `rpgskills.party` | [Party System](party_system.md) |
| `/rparty share <percent>` | Sets the XP sharing percentage | `rpgskills.party` | [Party System](party_system.md) |
| `/rparty transfer <player>` | Transfers party leadership | `rpgskills.party` | [Party System](party_system.md) |
| `/rparty perks` | Opens the party perks GUI | `rpgskills.party` | [Party Perks](party_perks.md) |

### UI Commands

| Command | Description | Permission | Documentation |
|---------|-------------|------------|---------------|
| `/rscoreboard` | Toggles the RPG skills scoreboard | `rpgskills.scoreboard` | [Scoreboard](scoreboard.md) |

## Administrative Commands

### Skills Administration

| Command | Description | Permission | Documentation |
|---------|-------------|------------|---------------|
| `/skillsadmin <player> <skill> <set/add> <level/xp> <amount>` | Manages player skills | `rpgskills.admin` | [Admin Commands](admin_commands.md) |

### XP Booster Management

| Command | Description | Permission | Documentation |
|---------|-------------|------------|---------------|
| `/rpgbooster apply <player> <skill> <multiplier> [duration]` | Applies an XP booster | `rpgskills.admin.xpbooster` | [XP Boosters](xp_boosters.md) |
| `/rpgbooster remove <player>` | Removes an XP booster | `rpgskills.admin.xpbooster` | [XP Boosters](xp_boosters.md) |
| `/rpgbooster check` | Checks for XP boosters | `rpgskills.admin.xpbooster` | [XP Boosters](xp_boosters.md) |
| `/rpgbooster help` | Shows XP booster help | `rpgskills.admin.xpbooster` | [XP Boosters](xp_boosters.md) |

### Skill Tree Administration

| Command | Description | Permission | Documentation |
|---------|-------------|------------|---------------|
| `/skilltree reset <player>` | Resets a player's skill tree | `rpgskills.admin` | [Admin Commands](admin_commands.md) |
| `/skilltree debug` | Shows debug information | `rpgskills.admin` | [Admin Commands](admin_commands.md) |

### Party Administration

| Command | Description | Permission | Documentation |
|---------|-------------|------------|---------------|
| `/rparty forcejoin <player> <leader>` | Forces a player to join a party | `rpgskills.admin.party` | [Admin Commands](admin_commands.md) |
| `/rparty forcekick <player>` | Forces a player to leave their party | `rpgskills.admin.party` | [Admin Commands](admin_commands.md) |
| `/rparty forceperks <leader> <perk>` | Grants a perk to a party | `rpgskills.admin.party` | [Admin Commands](admin_commands.md) |

### Plugin Management

| Command | Description | Permission | Documentation |
|---------|-------------|------------|---------------|
| `/rpgskills reload` | Reloads plugin configuration | `rpgskills.admin.reload` | [Admin Commands](admin_commands.md) |
| `/rpgskills version` | Shows plugin version | `rpgskills.admin` | [Admin Commands](admin_commands.md) |
| `/rpgskills debug <on/off>` | Toggles debug mode | `rpgskills.admin` | [Admin Commands](admin_commands.md) |

### Configuration Commands

| Command | Description | Permission | Documentation |
|---------|-------------|------------|---------------|
| `/rpgconfig xpmultiplier <skill> <value>` | Sets XP multiplier | `rpgskills.admin.config` | [Admin Commands](admin_commands.md) |
| `/rpgconfig cooldown <ability> <seconds>` | Sets ability cooldown | `rpgskills.admin.config` | [Admin Commands](admin_commands.md) |
| `/rpgconfig maxlevel <skill> <level>` | Sets maximum skill level | `rpgskills.admin.config` | [Admin Commands](admin_commands.md) |

## Command Aliases

Some commands have aliases for easier use:

| Main Command | Aliases |
|--------------|---------|
| `/skills` | `/rpgskills` |
| `/abilities` | `/rpgability`, `/ability` |
| `/rparty` | `/party` |
| `/skilltree` | `/st`, `/rpgtree` |
| `/rstat` | `/rpgstat` |

## Command Usage Tips

1. **Tab Completion**: Most commands support tab completion to assist with parameter entry
2. **Command Help**: Add `help` to the end of most commands for specific usage information
3. **Parameter Order**: Always enter parameters in the order shown in the command syntax
4. **Player Names**: Player names are case-sensitive in most commands
5. **Permissions**: Ensure you have the necessary permissions before using commands

## Console Commands

These commands can be run from the server console:

| Command | Description | Documentation |
|---------|-------------|---------------|
| `rpgskills:globalboost <skill> <multiplier> <duration>` | Activates global XP boost | [Admin Commands](admin_commands.md) |
| `rpgskills:maintenance <on/off>` | Sets maintenance mode | [Admin Commands](admin_commands.md) |
| `rpgskills:saveall` | Forces data save for all players | [Admin Commands](admin_commands.md) |

## Command Integrations

The plugin's commands integrate with several popular server plugins:

- **Vault**: For economy integration with party perks
- **PlaceholderAPI**: For displaying skill information in chat
- **LuckPerms**: For detailed permission management
- **Essentials**: For teleport integration with party commands 