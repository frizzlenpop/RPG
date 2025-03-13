# Mount System

## Overview

The Mount System allows players to acquire, summon, and upgrade special mounts that provide unique abilities, statistical boosts, and visual effects. Unlike standard Minecraft horses or other tameable mobs, these mounts are highly customized with special abilities, particle effects, sounds, and progression paths.

## Mount Types

The plugin features five unique mounts, each with their own theme, abilities, and upgrade paths:

### Phoenix Blaze

![Phoenix Blaze](https://i.imgur.com/example1.png)

A majestic fiery bird that grants its rider flame-based powers.

**Base Stats:**
- Speed: ★★★☆☆
- Jump: ★★★★☆
- Health: ★★☆☆☆

**Special Abilities:**
- **Flame Trail**: Leaves a trail of fire particles and temporary fire blocks
- **Fire Immunity**: Rider takes no fire damage while mounted
- **Thermal Updraft**: Double jump ability with flame effects

**Cosmetic Effects:**
- Constant fire particles emanating from wings and tail
- Flame sound effects while moving
- Red/orange glowing aura

### Shadow Steed

![Shadow Steed](https://i.imgur.com/example2.png)

A mysterious dark horse wreathed in shadow that excels in stealth and night operations.

**Base Stats:**
- Speed: ★★★★☆
- Jump: ★★★☆☆
- Health: ★★★☆☆

**Special Abilities:**
- **Shadow Dash**: Short-range teleport with cooldown
- **Night Vision**: Rider gains night vision effect
- **Shadow Cloak**: Temporary invisibility (30-second cooldown)

**Cosmetic Effects:**
- Shadow particles trail behind the mount
- Purple/black color scheme with glowing eyes
- Silent movement (reduced sound effects)

### Crystal Drake

![Crystal Drake](https://i.imgur.com/example3.png)

A dazzling crystalline dragon that manipulates light and energy.

**Base Stats:**
- Speed: ★★☆☆☆
- Jump: ★★★☆☆
- Health: ★★★★☆

**Special Abilities:**
- **Crystal Shield**: Damage reflection (20% chance, 30% damage)
- **Prismatic Beam**: Temporary beam attack (60-second cooldown)
- **Crystal Teleport**: Teleport to visible location within 50 blocks

**Cosmetic Effects:**
- Reflective crystalline body that changes colors
- Sparkling particles in mount's wake
- Crystalline sound effects for movement and abilities

### Storm Charger

![Storm Charger](https://i.imgur.com/example4.png)

An electrified steed born from thunderstorms with lightning-fast speed.

**Base Stats:**
- Speed: ★★★★★
- Jump: ★★☆☆☆
- Health: ★★☆☆☆

**Special Abilities:**
- **Lightning Dash**: Temporary extreme speed boost (30-second cooldown)
- **Thunder Step**: Small AOE damage around mount when sprinting
- **Storm Aura**: Has a chance to deflect projectiles with lightning

**Cosmetic Effects:**
- Electric particles constantly crackling around hooves
- Lightning strikes with certain abilities
- Blue/white color scheme with glowing effects

### Ancient Golem

![Ancient Golem](https://i.imgur.com/example5.png)

A massive stone construct that provides unparalleled protection and strength.

**Base Stats:**
- Speed: ★☆☆☆☆
- Jump: ★★★★★
- Health: ★★★★★

**Special Abilities:**
- **Ground Pound**: AOE stun/damage effect (45-second cooldown)
- **Stone Shield**: Damage reduction for rider (25%)
- **Mountain Leap**: Extremely high jump with slow fall

**Cosmetic Effects:**
- Stone particles crumbling from body as it moves
- Glowing runes across its body
- Heavy footstep sounds and effects

## Mount Acquisition

Players can purchase mounts through a specialized Mount Merchant NPC or via the `/rmount shop` command, which opens the Mount Shop GUI.

Initial mount prices:
- Phoenix Blaze: 25,000 coins
- Shadow Steed: 25,000 coins
- Crystal Drake: 25,000 coins
- Storm Charger: 25,000 coins
- Ancient Golem: 25,000 coins

## Mount Upgrades

Each mount has three upgrade paths:

1. **Speed Upgrades**: Increases movement speed
2. **Ability Upgrades**: Enhances special abilities (cooldown reduction, effect increase)
3. **Appearance Upgrades**: Unlocks additional visual effects and customizations

### Upgrade Tiers

Each upgrade path has 5 tiers:

| Tier | Cost | Effect |
|------|------|--------|
| 1 | 5,000 coins | Basic improvement (+10%) |
| 2 | 10,000 coins | Moderate improvement (+15%) |
| 3 | 20,000 coins | Significant improvement (+20%) |
| 4 | 40,000 coins | Major improvement (+25%) |
| 5 | 80,000 coins | Maximum improvement (+30%) |

## Mount Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/rmount` | Opens the main mount GUI | `rpgskills.mount` |
| `/rmount summon [mountName]` | Summons your mount | `rpgskills.mount` |
| `/rmount dismiss` | Dismisses your active mount | `rpgskills.mount` |
| `/rmount shop` | Opens the mount shop | `rpgskills.mount` |
| `/rmount upgrade` | Opens the upgrade interface | `rpgskills.mount` |
| `/rmount list` | Lists all owned mounts | `rpgskills.mount` |
| `/rmount set <mountName>` | Sets your active mount | `rpgskills.mount` |
| `/rmount info <mountName>` | Shows information about a mount | `rpgskills.mount` |

## Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/rmount admin give <player> <mountName>` | Gives a mount to a player | `rpgskills.admin.mount` |
| `/rmount admin remove <player> <mountName>` | Removes a mount from a player | `rpgskills.admin.mount` |
| `/rmount admin upgradeall <player>` | Maxes all upgrades for a player | `rpgskills.admin.mount` |
| `/rmount admin setcost <mountName> <amount>` | Sets the cost of a mount | `rpgskills.admin.mount` |
| `/rmount admin reload` | Reloads mount configuration | `rpgskills.admin.reload` |

## GUI System

The mount system features several intuitive GUIs:

### Main Mount GUI
Accessed via `/rmount`, shows all owned mounts with options to summon, customize, or upgrade.

### Mount Shop GUI
Displays all available mounts for purchase with costs and preview options.

### Mount Upgrade GUI
Interface for purchasing upgrades for your currently active mount.

### Mount Customization GUI
Allows players to adjust visual effects and appearance options for owned mounts.

## Mount Storage and Persistence

Mount data is stored in the player's data file, including:
- Owned mounts
- Current active mount
- Upgrade levels for each mount
- Customization settings
- Usage statistics

## Integration with Other Systems

The Mount System integrates with:

- **Economy System**: Uses the same currency as the party system
- **Skills System**: Some mounts may receive bonuses based on skill levels
- **Party System**: Special mount effects for party leaders and members
- **XP System**: Potential XP bonuses while using certain mounts

## Configuration

Mounts can be extensively configured in the `mounts.yml` file:

```yaml
mounts:
  phoenix_blaze:
    display_name: "Phoenix Blaze"
    base_cost: 25000
    entity_type: HORSE
    base_speed: 0.3
    base_jump: 0.8
    base_health: 20
    abilities:
      flame_trail:
        enabled: true
        cooldown: 0
      fire_immunity:
        enabled: true
      thermal_updraft:
        enabled: true
        cooldown: 30
    effects:
      particle_effects: 
        - FLAME
        - LAVA
      sound_effects:
        walk: "entity.blaze.burn"
        ability: "entity.blaze.shoot"
```

## Technical Implementation

The mount system uses:
- Custom entity metadata to track mount types
- Attribute modifiers for speed and other stats
- Entity AI manipulation for special movements
- Particle and sound effect managers
- Custom event handlers for mount interactions

## Upcoming Features

Future updates may include:
- Mount racing competitions
- Mount-specific quests and challenges
- Mount evolution paths
- Additional mount types
- Rare/legendary mount variants
- Mount breeding system 

## Mount Loot Chest/Key System

An alternative acquisition method for mounts through special loot chests opened with keys.

### Key Tiers
- **Common Key**: Low chance for rare mounts (60% Common, 30% Uncommon, 9% Rare, 1% Epic, 0% Legendary, 0% Mythic)
- **Uncommon Key**: Moderate chances (40% Common, 40% Uncommon, 15% Rare, 5% Epic, 0% Legendary, 0% Mythic)
- **Rare Key**: Better chances (15% Common, 30% Uncommon, 40% Rare, 13% Epic, 2% Legendary, 0% Mythic)
- **Epic Key**: High chance for rare mounts (0% Common, 20% Uncommon, 40% Rare, 30% Epic, 9% Legendary, 1% Mythic) 
- **Legendary Key**: Guaranteed rare mount or better (0% Common, 0% Uncommon, 25% Rare, 50% Epic, 20% Legendary, 5% Mythic)

### Key Acquisition Methods
- **Daily Login**: Common key every 5 days, Uncommon every 10 days
- **Weekly Quests**: Tasks that reward various key tiers
- **Boss Drops**: Higher tier bosses drop better keys
- **Events**: Special server events with key rewards
- **Achievements**: One-time keys for reaching milestones
- **Purchase**: Keys available in shop for coins

### Mount Chest Mechanics
- **Visual Design**: Animated chest with particle effects matching key tier
- **Opening Animation**: Effects based on potential mounts (fire effects when Phoenix possible)
- **Duplicate Handling**: Receiving a mount you already own converts it to:
  - Mount upgrade materials
  - Mount customization tokens 
  - Fusion material for same mount type
  - Partial coin refund

### Mount Chest Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/rmount chest` | Opens the chest GUI | `rpgskills.mount.chest` |
| `/rmount keys` | Shows keys owned | `rpgskills.mount.chest` |
| `/rmount open <keyType>` | Opens a chest with specified key | `rpgskills.mount.chest` |

### Admin Key Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/rmount admin givekey <player> <keyType> <amount>` | Gives keys to a player | `rpgskills.admin.mount` |
| `/rmount admin editdroprate <mountName> <keyType> <rate>` | Edit drop rate from chest | `rpgskills.admin.mount` |

## Mount Leveling System

A progression system where mounts gain XP and levels, unlocking new abilities and stats.

### XP Acquisition
- **Passive XP Gain**: 10 XP per minute while mounted
- **Player XP Share**: 25% of player XP also goes to mount
- **Distance Traveled**: 1 XP per 100 blocks traveled
- **Combat**: 5 XP per mob killed, 100 XP per boss while mounted
- **Quest Completion**: Bonus XP for completing quests while mounted

### Level Progression
- **Level Cap**: Maximum level of 50 for each mount
- **Scaling XP Requirements**: Each level requires more XP than the previous
  - Example: Level 1→2: 100 XP, Level 49→50: 25,000 XP
- **Milestone Levels**: Special rewards at levels 5, 10, 15, 20, 25, 30, 35, 40, 45, and 50

### Level Benefits

#### Stat Improvements
- Every level: +0.5% speed, +0.5% health
- Every 5 levels: +3% to mount's primary stat (varies by mount type)

#### Ability Unlocks
- **Level 5**: First basic ability unlocked
- **Level 15**: Second ability unlocked
- **Level 30**: Enhanced version of first ability
- **Level 50**: Ultimate mount ability

#### Passive Perks
- **Level 10**: Mount summon time reduced by 50%
- **Level 20**: Reduced fall damage while mounted
- **Level 35**: Auto-recovery when dismounted (resummons after 30s)
- **Level 45**: Generate resource specific to mount type

#### Cosmetic Improvements
- **Level 5**: Basic particle effects
- **Level 15**: Enhanced visual effects
- **Level 25**: Custom saddle/armor appearances
- **Level 40**: Aura effects that surround mount and rider
- **Level 50**: Legendary visual transformation

### Mount Commands (Additional)

| Command | Description | Permission |
|---------|-------------|------------|
| `/rmount stats [mountName]` | Shows mount XP and stats | `rpgskills.mount` |
| `/rmount levelup` | Shows available level rewards | `rpgskills.mount` |
| `/rmount xpboost <boosterItem>` | Applies XP booster to active mount | `rpgskills.mount` |

### Configuration Example
```yaml
mount_leveling:
  enabled: true
  xp_sources:
    passive_gain:
      rate: 10 # XP per minute while mounted
      player_xp_percent: 25 # % of player XP also given to mount
    distance_traveled: 1 # XP per 100 blocks
    mob_kills: 5 # XP per mob
    boss_kills: 100 # XP per boss
  level_scaling:
    base_requirement: 100 # XP needed for level 1
    scaling_factor: 1.2 # Multiplier for each subsequent level
  bonuses:
    per_level:
      health_percent: 0.5
      speed_percent: 0.5
    milestone_levels: [5, 10, 15, 20, 25, 30, 35, 40, 45, 50]
```

## Mount Rarity & Combination System

A system where mounts have rarity tiers, and identical mounts can be combined to preserve XP and potentially upgrade rarity.

### Mount Rarity Tiers
- **Common** (White): Base mounts, most frequently found
- **Uncommon** (Green): Slightly enhanced stats (+5% to base stats)
- **Rare** (Blue): Better stats (+10% to base stats) and minor visual enhancements
- **Epic** (Purple): Significantly better stats (+20% to base stats) and distinct visual effects
- **Legendary** (Orange): Premium stats (+35% to base stats) with dramatic visual effects
- **Mythic** (Red): Ultimate tier (+50% to base stats) with spectacular visual effects and unique abilities

### Rarity Benefits

#### Stat Bonuses
Each rarity tier provides an increasing percentage boost to base mount stats:

| Rarity | Speed | Jump | Health | Ability Effectiveness |
|--------|-------|------|--------|----------------------|
| Common | Base | Base | Base | Base |
| Uncommon | +5% | +5% | +5% | +5% |
| Rare | +10% | +10% | +10% | +10% |
| Epic | +20% | +20% | +20% | +20% |
| Legendary | +35% | +35% | +35% | +35% |
| Mythic | +50% | +50% | +50% | +50% |

#### Visual Enhancements
- **Uncommon**: Subtle particle effects
- **Rare**: Enhanced particles and minor glow
- **Epic**: Custom particle trails and distinct glow
- **Legendary**: Advanced particle effects, auras, and significant visual changes
- **Mythic**: Spectacular visual overhaul, custom sounds, and ground effects

#### Special Features
- **Rare+**: Custom name tags visible to other players
- **Epic+**: Special mount/dismount animations
- **Legendary+**: Custom footstep effects
- **Mythic**: Unique ability variants not available at lower rarities

### Mount Combination Mechanics

#### Basic Rules
- **Same Mount + Same Rarity**: Can be combined (e.g., Common Phoenix + Common Phoenix)
- **Different Rarity**: Cannot be combined (e.g., Common Phoenix + Rare Phoenix)
- **Different Mount Types**: Cannot be combined (e.g., Phoenix + Shadow Steed)

#### Combination Process
1. Player accesses Mount Fusion GUI via `/rmount combine` or through Mount GUI
2. Player places two identical rarity mounts in the fusion slots
3. GUI shows preview of resulting mount (new rarity, combined XP, etc.)
4. Player confirms fusion (potentially requires fusion materials or currency)
5. Animation plays showing mounts combining
6. New mount replaces the two source mounts in inventory

#### XP & Level Preservation
- **Combined XP**: Total XP = Mount A XP + Mount B XP
- **Level Calculation**: New mount's level is calculated based on combined XP
- **No XP Loss**: 100% of both mounts' XP is preserved

#### Rarity Upgrade Rules
- Combining two mounts of the same rarity has a **chance** to upgrade to the next rarity tier
- Chance increases with mount level and certain catalysts
- Example upgrade chances:
  - Common → Uncommon: 75% chance
  - Uncommon → Rare: 50% chance
  - Rare → Epic: 30% chance
  - Epic → Legendary: 15% chance
  - Legendary → Mythic: 5% chance

### Fusion Enhancement Items
- **Fusion Catalyst**: Increases chance of rarity upgrade
- **Stability Stone**: Guarantees no mount data loss during fusion
- **Rarity Lock**: Prevents rarity change if desired (for aesthetic reasons)
- **XP Booster**: Adds bonus XP during fusion (e.g., +10% extra XP)

### Mount Combination Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/rmount combine` | Opens the mount combination GUI | `rpgskills.mount.combine` |
| `/rmount preview <mountID1> <mountID2>` | Preview combination result | `rpgskills.mount.combine` |
| `/rmount catalysts` | View owned fusion catalysts | `rpgskills.mount.combine` |

### Configuration Example
```yaml
mount_combination:
  enabled: true
  fusion_costs:
    common: 1000
    uncommon: 2500
    rare: 5000
    epic: 10000
    legendary: 25000
  rarity_upgrade_chances:
    common_to_uncommon: 75
    uncommon_to_rare: 50
    rare_to_epic: 30
    epic_to_legendary: 15
    legendary_to_mythic: 5
  catalyst_boost_percent: 25 # Increases upgrade chance by 25%
  fusion_materials:
    common: "IRON_INGOT:5"
    uncommon: "GOLD_INGOT:5"
    rare: "DIAMOND:3"
    epic: "EMERALD:3"
    legendary: "NETHERITE_INGOT:1"
```

## Mount Collection System

A complementary system to encourage collecting all mount variants and rarities.

### Mount Codex
- Encyclopedia showing all available mounts (discovered and undiscovered)
- Tracks owned mounts by rarity and level
- Shows progress toward collection completion

### Collection Bonuses
- **Mount Type Completion**: Owning all rarities of one mount type grants special cosmetic
- **Rarity Tier Completion**: Owning one mount of each type at a specific rarity grants stat boost
- **Full Collection**: Special title and mount for collecting all mounts at all rarities

### Mount Display
- Special pedestals to show off mounts in player homes or guild halls
- Mount statues that can be crafted as trophies
- Mount showcase events where players can display their best mounts

### Mount Events
- Weekly featured mount with bonus XP
- Mount racing competitions with special tracks for different mount types
- Mount battle arenas testing different mount abilities 