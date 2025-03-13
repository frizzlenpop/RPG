# Skill Tree System

## Overview

The Skill Tree System is an advanced progression feature that allows players to specialize their characters beyond the basic skill leveling. It provides a more customized experience where players can choose their own advancement path and unlock powerful specialized perks.

## Key Concepts

### Skill Points

Skill points are the currency of the skill tree system:

- Players earn 1 skill point for each overall level they gain
- Players earn 2 bonus points for every 10 levels (level 10, 20, 30, etc.)
- Points can be spent to unlock nodes in the skill tree
- Points can only be spent on available (connected) nodes

### Player Level

Unlike individual skill levels, the skill tree uses a unified player level:

- Player level is calculated based on total XP across all skills
- Higher player level = more skill points
- The player level formula uses progressively increasing XP requirements
- Levels provide a way to gate access to more powerful perks

### Nodes

Nodes are the individual perks or benefits that can be unlocked in the skill tree:

- Each node provides a specific benefit
- Nodes are arranged in a hierarchical structure
- Some nodes are prerequisites for other nodes
- Nodes may require a minimum player level or specific skill level

## Using the Skill Tree

### Accessing the Skill Tree

Players can access the skill tree using:

```
/skilltree
```

This opens a GUI that displays the available skill tree nodes, with different colors indicating:
- Green: Unlocked nodes
- Yellow: Available nodes (can be unlocked)
- Red: Locked nodes (prerequisites not met)
- Gray: Unavailable nodes (level requirements not met)

### Unlocking Nodes

To unlock a node, players use:

```
/skilltree unlock <nodeId>
```

If the player has enough skill points and meets all requirements, the node will be unlocked and its benefits applied.

### Viewing Information

Players can view information about their skill tree progress:

```
/skilltree info
```

This displays:
- Current player level
- Available skill points
- Total spent skill points
- Unlocked nodes

### Checking Level Progress

To check level progress and points:

```
/skilltree level
```

This shows:
- Current player level
- XP progress toward next level
- Total skill points
- Spent and available points

## Skill Tree Structure

The skill tree is organized into several branches, each focusing on different aspects of gameplay:

### Resource Gathering

Focuses on improving resource collection and efficiency:
- Mining efficiency
- Logging efficiency
- Farming yield
- Excavation treasure finding

### Combat

Focuses on improving combat capabilities:
- Damage increases
- Defensive bonuses
- Special attack effects
- Health and regeneration

### Economy

Focuses on financial benefits:
- Better selling prices
- Reduced repair costs
- Crafting efficiency
- Treasure finding chances

### Utility

Focuses on convenience and quality-of-life improvements:
- Faster movement
- Reduced hunger
- Extended breath underwater
- Fall damage reduction

## Node Types

The skill tree contains several types of nodes:

### Stat Boost Nodes

Provide permanent increases to player statistics:
- +5% Mining Speed
- +10% Combat Damage
- +15% Fishing Luck

### Perk Nodes

Provide special abilities or effects:
- Auto-smelt ores while mining
- Double wood drops while logging
- Critical hit improvements in combat

### Mastery Nodes

High-tier nodes that provide powerful benefits:
- Multi-ore mining
- AoE damage in combat
- Treasure hunting while fishing

### Connector Nodes

Simple nodes that connect different branches:
- Minor XP boosts
- Small efficiency improvements
- Stepping stones to more powerful nodes

## Node Requirements

Nodes may have various requirements:

- **Prerequisite Nodes**: Must have unlocked specific other nodes first
- **Player Level**: Minimum player level required
- **Skill Level**: Minimum level in a specific skill required
- **Skill Points**: Cost in skill points to unlock

## Configuration

The skill tree is fully configurable through the `skill_tree.yml` file:

```yaml
nodes:
  mining_efficiency_1:
    display_name: "Mining Efficiency I"
    description: "Increases mining speed by 10%"
    icon: IRON_PICKAXE
    cost: 1
    requirements:
      player_level: 5
      mining_level: 10
    effects:
      mining_speed_multiplier: 1.1
    position:
      x: 1
      y: 1
    connections:
      - mining_efficiency_2
```

Administrators can:
- Add new nodes
- Modify existing nodes
- Change node requirements
- Adjust node benefits
- Rearrange the tree structure

## Technical Implementation

The skill tree system is implemented through:

- `SkillTreeManager.java`: Core class that manages the skill tree
- `PlayerLevel.java`: Handles unified player level calculations
- `SkillTreeGUI.java`: Provides the GUI interface
- `SkillTreeCommand.java`: Processes player commands
- `SkillXPListener.java`: Listens for XP changes to update player level

## Integration with Other Systems

The Skill Tree system integrates with:

- [Skills System](skills_system.md): Uses skill levels as requirements
- [XP System](xp_system.md): Uses XP to calculate player level
- [Passive Skills](passive_skills.md): Can enhance passive ability effects
- [Active Abilities](active_abilities.md): Can modify active ability properties
- [GUI System](gui_system.md): Provides interactive visual interface 