# RPG Skills Plugin - XP Booster System

This document explains how to use the XP Booster system in the RPG Skills Plugin.

## Overview

The XP Booster system allows server administrators to apply special XP multipliers to tools and weapons. 
When players use these boosted items for activities related to the specific skill, they'll earn 
more XP, helping them level up faster.

## Commands

The main command for managing XP boosters is `/rpgbooster`. This command requires the permission
`rpgskills.admin.xpbooster`.

### Command Usage

- `/rpgbooster apply <player> <skill> <multiplier> [duration]`
  - Applies an XP booster to the item the player is holding
  - `<player>`: The target player whose held item will be boosted
  - `<skill>`: The skill to boost (mining, logging, farming, fishing, fighting, enchanting, excavation, repair)
  - `<multiplier>`: The XP multiplier (e.g., 1.5 = 50% more XP, or you can use direct percentages like 50 for 50% more)
  - `[duration]`: Optional. The duration of the booster (e.g., 30m, 2h, 7d). If omitted, the booster is permanent

- `/rpgbooster remove <player>`
  - Removes an XP booster from the item the player is holding

- `/rpgbooster check`
  - Checks if the item you're holding has an XP booster

- `/rpgbooster help`
  - Shows a help message with all available commands

## Examples

1. Apply a permanent 50% mining XP boost to the pickaxe a player is holding:
   ```
   /rpgbooster apply MineCraftPro mining 1.5
   ```
   or
   ```
   /rpgbooster apply MineCraftPro mining 50
   ```

2. Apply a temporary 100% fishing XP boost that lasts for 2 hours:
   ```
   /rpgbooster apply FishingExpert fishing 2.0 2h
   ```

3. Apply a 25% combat XP boost that lasts for 7 days:
   ```
   /rpgbooster apply WarriorPlayer fighting 25 7d
   ```

4. Remove a booster from a player's held item:
   ```
   /rpgbooster remove MineCraftPro
   ```

## Important Notes

- The item must be appropriate for the skill being boosted (e.g., pickaxe for mining, sword for fighting)
- The booster is applied to the specific item, not to the player
- The XP boost only applies when using that specific item
- The multiplier can be between 1.0 and 10.0 (0% to 900% bonus)
- You can specify duration using:
  - Seconds: `30s`
  - Minutes: `5m` 
  - Hours: `2h`
  - Days: `7d`
- When a duration is specified, the booster will automatically expire after that time
- Players can see information about the booster in the item's lore

## Notes for Developers

The XP Booster system works by modifying the following components:

1. `XPBoosterManager`: Manages all XP booster operations
2. `XPBoosterCommand`: Handles the command processing
3. `XPManager.addXP()`: Integrates with the booster system to apply XP multipliers

These tools boost the XP gain by applying a multiplier to the base XP before other calculations (like party sharing) are performed. 