# Quick Start Guide

This guide will help you get up and running with the RPG Skills Plugin quickly. It covers the basic setup and configuration needed to start using the plugin on your server.

## First Steps After Installation

After [installing the plugin](installation.md), follow these steps to quickly set up the plugin:

1. **Start your server** to generate the default configuration files
2. **Give yourself admin permissions** by running: `/op <your_username>` or setting the permission `rpgskills.admin` with your permissions plugin
3. **Check that the plugin is working** by running: `/skills` - you should see the skills menu

## Basic Player Commands

Here are the essential commands players will use:

| Command | Description |
|---------|-------------|
| `/skills` | Open the main skills menu |
| `/skills info <skill>` | View detailed information about a specific skill |
| `/abilities` | Open the active abilities menu |
| `/skilltree` | Open the skill tree interface |
| `/party create` | Create a new party |
| `/party invite <player>` | Invite a player to your party |

## Essential Admin Commands

As a server administrator, you should familiarize yourself with these commands:

| Command | Description |
|---------|-------------|
| `/skillsadmin help` | View all admin commands |
| `/skillsadmin setlevel <player> <skill> <level>` | Set a player's skill level |
| `/skillsadmin addxp <player> <skill> <amount>` | Add XP to a player's skill |
| `/rpgbooster apply <player> <skill> <multiplier> [duration]` | Apply an XP booster |
| `/rpgskills reload` | Reload the plugin configuration |

## Basic Configuration

The most important configuration files are:

### config.yml

This is the main configuration file. Key settings to consider changing:

```yaml
general:
  debug_mode: false                  # Set to true for detailed logging
  display_level_up_message: true     # Show level up messages
  display_xp_notifications: true     # Show XP gain notifications
  economy_enabled: true              # Enable/disable economy features

xp:
  global_multiplier: 1.0             # Adjust for faster/slower progression
```

### skills.yml

Configure individual skill settings and XP values:

```yaml
skills:
  mining:
    enabled: true                    # Enable/disable the skill
    max_level: 100                   # Maximum achievable level
    xp_values:
      STONE: 1                       # XP for mining stone
      COAL_ORE: 5                    # XP for mining coal ore
      # ... other blocks
```

## Setting Up Permissions

For a basic setup, give your players these permissions:

```
rpgskills.use         # Basic plugin usage
rpgskills.skills.*    # Access to all skills
rpgskills.passive.*   # Access to all passive abilities
rpgskills.abilities.* # Access to all active abilities
rpgskills.party.*     # Access to party system
```

For a LuckPerms server, you might run:

```
/lp group default permission set rpgskills.use true
/lp group default permission set rpgskills.skills.* true
/lp group default permission set rpgskills.passive.* true
```

## Creating Your First XP Booster

To create a mining XP booster for a player named "Steve":

1. Ensure Steve has a pickaxe in their hand
2. Run the command: `/rpgbooster apply Steve mining 1.5 1h`
3. This will apply a 1.5x mining XP boost for 1 hour

## Setting Up a Skill Tree

The skill tree is pre-configured, but you may want to check the `skill_tree.yml` file to ensure it matches your server's needs. Players access it with the `/skilltree` command.

## Enabling the Scoreboard

To enable the skills scoreboard for all players:

1. Edit `config.yml`
2. Set `scoreboard.enabled_by_default: true`
3. Reload the configuration with `/rpgskills reload`

## Testing Your Setup

To test if everything is working:

1. **Mine some blocks** and check if you gain Mining XP
2. **Cut down trees** to gain Logging XP
3. **Check your progress** with `/skills`
4. **Try an active ability** once you reach the required level

## Common Issues for New Installations

| Issue | Solution |
|-------|----------|
| No XP gain | Check if the skill is enabled in `skills.yml` and if the XP value for that block/action is set |
| Commands not working | Ensure permissions are set correctly and the plugin is enabled |
| Economy features not working | Verify Vault and an economy plugin are installed |
| Skills menu not opening | Check console for errors and ensure the player has the `rpgskills.use` permission |

## Next Steps

Once you have the basics working:

1. Explore the [Skills System](skills_system.md) in more detail
2. Configure [Passive Skills](passive_skills.md) to match your server
3. Set up [Active Abilities](active_abilities.md) cooldowns and effects
4. Customize the [Party System](party_system.md) for group play
5. Create a [Balanced Economy](economy.md) for party perks

---

For more detailed information on any aspect of the plugin, refer to the specific documentation pages linked throughout this guide and in the main [README](../README.md). 