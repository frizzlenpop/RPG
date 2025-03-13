# PlaceholderAPI Integration

## Overview

The RPG Skills Plugin provides extensive PlaceholderAPI integration, allowing server administrators to display skill information, player levels, XP, party details, and more in various plugins that support PlaceholderAPI (such as scoreboard plugins, tab list plugins, or chat formatting plugins).

## Setting Up PlaceholderAPI

To use these placeholders, you need:

1. **PlaceholderAPI** installed on your server
2. The **RPG Skills Plugin** installed and configured
3. Run `/papi reload` after installing RPG Skills Plugin

## Available Placeholders

### General Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%rpgskills_player_level%` | The player's overall level | 42 |
| `%rpgskills_player_total_xp%` | Total XP across all skills | 125,897 |
| `%rpgskills_player_skill_points%` | Available skill points | 3 |
| `%rpgskills_player_total_skill_levels%` | Sum of all skill levels | 376 |

### Skill-Specific Placeholders

Replace `<skill>` with any skill name (mining, logging, farming, fishing, fighting, enchanting, excavation, repair).

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%rpgskills_<skill>_level%` | Current level in the skill | 54 |
| `%rpgskills_<skill>_xp%` | Current XP in the skill | 12,345 |
| `%rpgskills_<skill>_xp_needed%` | XP needed for next level | 5,432 |
| `%rpgskills_<skill>_xp_percent%` | Progress percentage to next level | 65% |
| `%rpgskills_<skill>_xp_progress_bar%` | Visual progress bar to next level | ■■■■■□□□□□ |
| `%rpgskills_<skill>_rank%` | Player's server rank in this skill | #7 |
| `%rpgskills_<skill>_booster%` | Active XP booster multiplier | 1.5x |
| `%rpgskills_<skill>_passive_count%` | Number of unlocked passive abilities | 4 |

### Passive Abilities Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%rpgskills_passive_count%` | Total unlocked passive abilities | 17 |
| `%rpgskills_passive_<ability>_unlocked%` | Whether a specific passive is unlocked | Yes |
| `%rpgskills_passive_<ability>_chance%` | Trigger chance of a passive ability | 35% |

### Active Abilities Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%rpgskills_ability_count%` | Total unlocked active abilities | 5 |
| `%rpgskills_ability_<ability>_unlocked%` | Whether a specific ability is unlocked | Yes |
| `%rpgskills_ability_<ability>_cooldown%` | Remaining cooldown time | 24s |
| `%rpgskills_ability_<ability>_cooldown_formatted%` | Formatted cooldown time | 00:24 |

### Skill Tree Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%rpgskills_skilltree_unlocked_nodes%` | Number of unlocked nodes | 14 |
| `%rpgskills_skilltree_total_nodes%` | Total nodes in the skill tree | 48 |
| `%rpgskills_skilltree_completion%` | Completion percentage | 29% |
| `%rpgskills_skilltree_node_<nodeId>_unlocked%` | Whether a specific node is unlocked | Yes |

### Party Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%rpgskills_party_name%` | Player's party name | Dragon Slayers |
| `%rpgskills_party_size%` | Current party size | 3/5 |
| `%rpgskills_party_leader%` | Party leader's name | MasterCrafter |
| `%rpgskills_party_level%` | Party level | 7 |
| `%rpgskills_party_xp%` | Current party XP | 3,456 |
| `%rpgskills_party_xp_needed%` | XP needed for next party level | 10,000 |
| `%rpgskills_party_xp_share%` | Party XP sharing percentage | 25% |
| `%rpgskills_party_active_perks%` | Count of active party perks | 3 |
| `%rpgskills_party_members%` | List of party members | Steve, Alex, Notch |

### Booster Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%rpgskills_booster_count%` | Number of active boosters | 2 |
| `%rpgskills_booster_<skill>_active%` | Whether a specific booster is active | Yes |
| `%rpgskills_booster_<skill>_multiplier%` | Booster multiplier | 1.5x |
| `%rpgskills_booster_<skill>_time_left%` | Time left on booster | 30m 45s |

### Leaderboard Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%rpgskills_top_<skill>_<position>_name%` | Player name at position | MiningPro |
| `%rpgskills_top_<skill>_<position>_level%` | Player level at position | 87 |
| `%rpgskills_top_player_<position>_name%` | Top player by total level | SkillMaster |
| `%rpgskills_top_player_<position>_level%` | Top player's total level | 563 |

## Placeholder Examples in Other Plugins

### In ScoreboardPlugin (example)

```yaml
scoreboard:
  title: "&6&lRPG Skills"
  lines:
    - "&7Player: &f%player_name%"
    - "&7Level: &f%rpgskills_player_level%"
    - ""
    - "&aMining: &f%rpgskills_mining_level% &7(%rpgskills_mining_xp_percent%%)"
    - "&aLogging: &f%rpgskills_logging_level% &7(%rpgskills_logging_xp_percent%%)"
    - ""
    - "&dParty: &f%rpgskills_party_name%"
    - "&dMembers: &f%rpgskills_party_size%"
```

### In DeluxeChat (example)

```yaml
formats:
  default:
    prefix: "[%rpgskills_player_level%]"
    chat: "%player_name%: %message%"
```

### In TabList (example)

```yaml
header: "&6RPG Skills Server"
footer: "&7Mining: %rpgskills_mining_level% | Fishing: %rpgskills_fishing_level%"
```

## Custom Placeholder Formatting

You can customize how certain placeholders display by editing the `config.yml` file:

```yaml
placeholders:
  progress_bar:
    length: 10
    completed_char: "■"
    incomplete_char: "□"
  number_format:
    use_thousands_separator: true
    decimal_places: 0
```

## Using Placeholders in Commands

Many plugins allow placeholders in commands. For example:

```
/broadcast Top miner: %rpgskills_top_mining_1_name% (Level %rpgskills_top_mining_1_level%)
```

## Default Values for Placeholders

If a value is not available (e.g., player not in party), these placeholders will show:

```yaml
placeholders:
  default_values:
    not_in_party: "None"
    no_booster: "None"
    ability_locked: "Locked"
```

## Performance Considerations

Some placeholders may cause more server load than others. For high-traffic servers, consider:

1. Using caching plugins compatible with PlaceholderAPI
2. Limiting use of complex placeholders in frequently updated displays
3. Using the refresh interval setting in plugins that support it

## Technical Implementation

These placeholders are registered through PlaceholderAPI's expansion system. The plugin registers a custom expansion that handles all placeholders.

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Placeholders not working | Run `/papi reload` or restart your server |
| Showing as %rpgskills_...% | Ensure PlaceholderAPI is properly installed |
| Missing data | Verify the player has that skill/feature unlocked |
| Errors in console | Check for typos in placeholder names |

---

For more information on PlaceholderAPI, visit [their wiki](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki). 