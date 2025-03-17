# RPG Skills Plugin

A comprehensive RPG skills and progression system for Minecraft servers, allowing players to level up skills, unlock passive and active abilities, navigate skill trees, form parties, and use XP boosters.

## Features

- **8 Core Skills**: Mining, Logging, Farming, Fishing, Fighting, Excavation, Enchanting, and Repair
- **Passive Abilities**: Automatically trigger as you level up skills
- **Active Abilities**: Powerful skills you can activate with commands
- **Skill Tree**: Advanced progression system with unlockable perks
- **Party System**: Form groups with friends for shared XP and bonuses
- **XP Boosters**: Special tools that provide bonus XP when used
- **Economy Integration**: Purchase party perks with in-game currency
- **Custom GUI Menus**: User-friendly interfaces for all systems
- **Admin Commands**: Comprehensive tools for server management
- **Extensive Configuration**: Customize every aspect of the plugin
- **Database Storage**: Store player data in MySQL or SQLite databases

## Database Integration

The plugin now supports storing player data in a database (MySQL or SQLite) instead of YAML files. This provides several benefits:

- **Better Performance**: Faster data retrieval and storage, especially for servers with many players
- **Improved Scalability**: Easily handle large amounts of player data
- **Data Integrity**: Transactions ensure data is saved correctly
- **Cross-Server Support**: Share player data across multiple servers

### Configuration

Database settings can be configured in the `config.yml` file:

```yaml
database:
  # MySQL configuration
  mysql:
    enabled: false  # Set to true to use MySQL
    host: localhost
    port: 3306
    database: rpgskills
    username: root
    password: ""
  
  # SQLite configuration (used as fallback if MySQL is disabled or fails)
  sqlite:
    enabled: true  # Set to false to disable SQLite fallback
  
  # Migration settings
  migrate_on_startup: false  # Set to true to migrate YAML data to database on startup
```

### Migration

You can migrate existing player data from YAML files to the database using the following methods:

1. **Automatic Migration on Startup**: Set `database.migrate_on_startup` to `true` in the config.yml file
2. **Command-Based Migration**: Use the `/rpgdb migrate` command (requires `rpgskills.admin` permission)

### Database Commands

The plugin provides several commands for managing the database:

- `/rpgdb status` - Shows the current database status
- `/rpgdb migrate` - Migrates data from YAML to database
- `/rpgdb help` - Shows help information

All database commands require the `rpgskills.admin` permission.

### Database Schema

The plugin creates the following tables in the database:

- `player_skills` - Stores player skill levels and XP
- `player_passives` - Stores player passive abilities
- `player_placed_blocks` - Tracks blocks placed by players (for XP prevention)
- `player_settings` - Stores player settings and preferences
- `skill_tree_progress` - Tracks skill tree progress

### Fallback Mechanism

If the database connection fails, the plugin will automatically fall back to YAML file storage. This ensures that player data is always accessible, even if there are database issues.

## Documentation

### Getting Started
- [Installation Guide](docs/installation.md) - How to install and set up the plugin
- [Quick Start Guide](docs/quick_start.md) - Get up and running quickly
- [Configuration](docs/configuration.md) - Customize the plugin to your needs

### Core Systems
- [Skills System](docs/skills_system.md) - All about the skills and leveling up
- [XP System](docs/xp_system.md) - How XP works and is calculated
- [Passive Skills](docs/passive_skills.md) - Skills that trigger automatically
- [Active Abilities](docs/active_abilities.md) - Powerful abilities you can use on command
- [Skill Tree](docs/skill_tree.md) - Advanced progression system

### Additional Features
- [Party System](docs/party_system.md) - Form groups with other players
- [XP Boosters](docs/xp_boosters.md) - Tools that provide bonus XP
- [Economy Integration](docs/economy.md) - How the plugin integrates with economy plugins

### Reference
- [Commands](docs/commands.md) - Complete list of all available commands
- [Permissions](docs/permissions.md) - All permission nodes and their usage
- [Admin Commands](docs/admin_commands.md) - Server administration commands
- [Placeholders](docs/placeholders.md) - PlaceholderAPI variables provided by the plugin
- [API Documentation](docs/api.md) - For developers wanting to integrate with the plugin

### Development
- [Developer Guide](docs/dev_guide.md) - How to extend the plugin
- [Contributing Guidelines](docs/contributing.md) - How to contribute to the project
- [Changelog](CHANGELOG.md) - History of changes and updates

## Support

If you encounter any issues or have questions:

- [Common Issues](docs/common_issues.md) - Solutions to frequently encountered problems
- [Discord Support](https://discord.gg/rpgskills) - Join our Discord server for help
- [GitHub Issues](https://github.com/frizzlenpop/RPGSkillsPlugin/issues) - Report bugs or suggest features

## License

This plugin is licensed under the [MIT License](LICENSE.md).

## Credits

- **Developer**: FrizzleNPop
- **Contributors**: Community members who have contributed code or ideas
- **Special Thanks**: To all server owners and players who provide feedback

---

*RPG Skills Plugin is not affiliated with Mojang or Minecraft.*
