# Party System

## Overview

The Party System allows players to team up and share XP earned from various activities. It encourages cooperative gameplay and provides mechanisms for groups to progress together, even when players have different skill levels or focuses.

## Key Features

- **XP Sharing**: Party members receive a portion of XP earned by other members
- **Party Perks**: Unlockable bonuses using in-game economy
- **Party Leveling**: Parties gain levels as members earn XP
- **Party Chat**: Dedicated communication channel for party members
- **Offline Support**: Parties persist when members log off
- **Leadership Transfer**: Party ownership can be transferred

## Party Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/rparty create` | Create a new party | `rpgskills.party` |
| `/rparty invite <player>` | Invite a player to your party | `rpgskills.party` |
| `/rparty accept` | Accept a party invitation | `rpgskills.party` |
| `/rparty leave` | Leave your current party | `rpgskills.party` |
| `/rparty kick <player>` | Kick a player from your party (leader only) | `rpgskills.party` |
| `/rparty disband` | Disband your party (leader only) | `rpgskills.party` |
| `/rparty list` | List all members in your party | `rpgskills.party` |
| `/rparty info` | Display information about your party | `rpgskills.party` |
| `/rparty chat <message>` | Send a message to all party members | `rpgskills.party` |
| `/rparty share <percent>` | Set the XP sharing percentage (leader only) | `rpgskills.party` |
| `/rparty transfer <player>` | Transfer party leadership (leader only) | `rpgskills.party` |
| `/rparty perks` | Open the party perks GUI (leader only) | `rpgskills.party` |

## Creating and Managing Parties

### Creating a Party

To create a party, use:
```
/rparty create
```

This makes you the party leader with full control over the party.

### Inviting Players

Party leaders can invite other players:
```
/rparty invite <player>
```

The invited player will receive a clickable invitation message.

### Accepting Invitations

To accept a party invitation:
```
/rparty accept
```

This adds you to the party that most recently invited you.

### Leaving a Party

Any member can leave a party at any time:
```
/rparty leave
```

If the leader leaves, leadership is automatically transferred to another member.

### Disbanding a Party

Party leaders can disband the entire party:
```
/rparty disband
```

This removes all members and deletes the party.

## XP Sharing System

### How XP Sharing Works

When a party member earns XP:

1. A configurable percentage of that XP is set aside for sharing
2. The shared XP is distributed among other online party members
3. The distribution can be affected by party perks

### Setting the Share Percentage

Party leaders can adjust the XP sharing percentage:
```
/rparty share <percent>
```

Valid percentages range from 0% (no sharing) to 50% (maximum sharing).

### XP Sharing Formula

The XP sharing calculation works as follows:

```
Shared XP = Earned XP × Share Percentage
Per-Member XP = Shared XP / Number of Online Members
```

If party bonus perks are active, additional XP may be added:

```
Bonus XP = Per-Member XP × Party Bonus Percentage
Total Member XP = Per-Member XP + Bonus XP
```

## Party Leveling

### Party Level

Parties gain experience as members earn XP:

- Every 1000 XP earned by party members adds 1 party XP
- Party levels follow a progressive XP requirement
- Higher party levels unlock more powerful perks

### Party Level Benefits

As party level increases:
- Maximum party size increases
- XP sharing efficiency improves
- More powerful perks become available
- Party bonus percentages increase

## Party Perks

Party perks are special bonuses that can be purchased using server economy:

### Available Perks

| Perk | Effect | Cost | Required Level |
|------|--------|------|----------------|
| XP Boost I | +5% XP for all party members | 5,000 | 1 |
| XP Boost II | +10% XP for all party members | 15,000 | 3 |
| XP Boost III | +15% XP for all party members | 30,000 | 5 |
| Party Size I | Increase max party size to 6 | 10,000 | 2 |
| Party Size II | Increase max party size to 8 | 25,000 | 4 |
| Party Size III | Increase max party size to 10 | 50,000 | 8 |
| Sharing Boost I | Improve XP sharing by 10% | 7,500 | 2 |
| Sharing Boost II | Improve XP sharing by 20% | 20,000 | 4 |
| Offline Sharing | Share XP with offline members at 50% rate | 40,000 | 6 |
| Quick Recovery | Reduce respawn time by 50% | 15,000 | 3 |

### Purchasing Perks

Party leaders can purchase perks through the perks GUI:
```
/rparty perks
```

Perks are tied to the party, not individual players, and persist even if leadership changes.

## Party Chat

Party members can communicate through a dedicated party chat:

```
/rparty chat <message>
```

This sends a message to all online party members, prefixed with a party chat indicator.

## Technical Implementation

The Party System is implemented through:

- `PartyManager.java`: Core class that manages parties and XP sharing
- `PartyCommand.java`: Handles all party-related commands
- `PartyPerksGUI.java`: Provides the GUI for purchasing party perks
- `EconomyManager.java`: Handles economic transactions for perks
- YAML storage system for party data persistence

## Integration with Other Systems

The Party System integrates with:

- [XP System](xp_system.md): For XP sharing calculations
- [Economy Integration](economy_integration.md): For purchasing perks
- [GUI System](gui_system.md): For the party perks interface
- [Scoreboard](scoreboard.md): For displaying party information 