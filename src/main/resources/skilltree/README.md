# Skill Tree Configuration

This directory contains the configuration files for the RPGSkills plugin's skill tree system. Each file represents a different skill category, and nodes defined in these files will be available in the game.

## Summary of Skill Trees

The skill tree system has been expanded to include over 50 skills across multiple categories:

- **Warrior Skill Tree (18 skills)**: Combat-focused abilities that enhance melee damage, survivability, and special combat techniques.
- **Mining Skill Tree (18 skills)**: Mining-focused abilities that improve mining speed, ore drops, and special mining techniques.
- **Fishing Skill Tree (18 skills)**: Fishing-focused abilities that enhance fishing luck, speed, and underwater capabilities.

Each skill tree is designed with a tiered progression system:
- **Tier 1**: Basic skills with no prerequisites
- **Tier 2**: Advanced skills requiring one Tier 1 skill
- **Tier 3**: Specialized skills requiring Tier 2 skills
- **Tier 4**: Expert skills requiring multiple Tier 3 skills
- **Tier 5**: Master skills requiring multiple Tier 4 skills
- **Ultimate**: Powerful abilities requiring multiple Tier 5 skills

All skills are interconnected through prerequisites, creating meaningful progression paths and encouraging players to specialize in different areas.

## File Structure

- Each category has its own YAML file (e.g., `warrior.yml`, `mining.yml`, etc.)
- The skill tree system automatically loads all `.yml` files in this directory
- New categories can be created by adding new files

## Node Configuration

Each skill node has the following properties:

```yaml
node_id:
  name: "Display Name"
  description: "Description of the skill"
  point_cost: 2  # Cost in skill points to unlock
  prerequisites: ["node_id_1", "node_id_2"]  # List of prerequisites
  icon: "DIAMOND_SWORD"  # Material name for the icon
  type: "PASSIVE"  # PASSIVE or ACTIVE
  effects:
    effect0:
      type: "EFFECT_TYPE"  # Type of effect
      target: "target"     # Target for the effect
      value: 0.1           # Value for the effect
    effect1:
      type: "ANOTHER_EFFECT_TYPE"
      target: "target"
      value: 5.0
```

## Effect Types

The following effect types are supported:

### Attribute Effects
- `ATTRIBUTE`: Modifies a player attribute (e.g., `GENERIC_MAX_HEALTH`, `GENERIC_MOVEMENT_SPEED`)
- `DAMAGE_MULTIPLIER`: Increases damage dealt by a percentage
- `DAMAGE_REDUCTION`: Reduces damage taken by a percentage

### Resource Effects
- `REGENERATION_RATE`: Increases regeneration rate for a resource
- `FOOD_CONSUMPTION`: Modifies food consumption rate (negative values reduce consumption)

### Combat Effects
- `CRITICAL_CHANCE`: Chance to deal critical hit
- `KNOCKBACK_RESISTANCE`: Chance to resist knockback
- `AOE_DAMAGE`: Deal area-of-effect damage to nearby entities
- `PROJECTILE_REFLECTION`: Chance to reflect projectiles
- `LIFESTEAL`: Heal for a percentage of damage dealt
- `ON_KILL_EFFECT`: Apply an effect when killing an entity

### Gathering Effects
- `MINING_SPEED`: Increase mining speed
- `WOODCUTTING_SPEED`: Increase woodcutting speed
- `DIGGING_SPEED`: Increase digging speed
- `FISHING_SPEED`: Increase fishing speed
- `DOUBLE_DROP_CHANCE`: Chance for double drops
- `TRIPLE_DROP_CHANCE`: Chance for triple drops
- `RARE_DROP_CHANCE`: Chance for rare drops
- `AUTO_SMELT_CHANCE`: Chance to automatically smelt items
- `VEIN_MINING`: Chance to mine entire veins at once

### Special Effects
- `LOW_HEALTH_EFFECT`: Apply an effect when health is low
- `NIGHT_VISION`: Grant night vision in dark areas
- `INVISIBILITY`: Grant invisibility
- `FEAR_EFFECT`: Cause entities to flee
- `ORE_DETECTION`: Detect ores through walls
- `STONE_BRIDGE`: Create temporary bridges
- `AREA_MINING`: Mine in an area
- `TERRAIN_MANIPULATION`: Manipulate terrain temporarily
- `TELEPORT_STRIKE`: Teleport to a target and strike
- `SUPER_FORM`: Transform with enhanced abilities
- `PORTABLE_SMELTING`: Access a portable smelting interface
- `DEMOLITION_BLAST`: Create controlled explosions
- `DOUBLE_JUMP`: Perform a double jump

## Admin Commands

Administrators can use the following commands:

- `/skilltree admin reload` - Reload skill tree configuration
- `/skilltree admin resetplayer <player>` - Reset a player's skills

## Migration from config.yml

If skill trees are not found in separate files, the plugin will automatically migrate them from `config.yml` to separate files in this directory. The migration process:

1. Reads the skill nodes from `config.yml`
2. Categorizes them based on their prefix (e.g., "warrior_" nodes go into warrior.yml)
3. Saves each category to its respective file
4. Loads the nodes into memory

If neither separate files nor config.yml contains skill nodes, default skill trees will be created.

## Adding New Skill Trees

To add a new skill tree:

1. Create a new YAML file with the category name (e.g., `archery.yml`)
2. Define nodes for the skill tree
3. Restart the server or use `/skilltree admin reload`

## Node Naming Convention

For best compatibility, follow these naming conventions:

- Use lowercase, underscore-separated names
- Start with the category name (e.g., `warrior_skill_name`)
- Keep names descriptive but concise

## Prerequisites

Prerequisites create the interconnected nature of the skill tree:

- Each node can have multiple prerequisites
- A player must unlock all prerequisites before unlocking a node
- This allows for creating complex, branching skill trees
- Circular dependencies (A requires B, B requires A) are not supported and may cause issues