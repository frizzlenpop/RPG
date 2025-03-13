# Changelog

All notable changes to the RPG Skills Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Enhanced documentation with detailed guides for all plugin systems
- New developer API for easier integration with other plugins
- Placeholder for upcoming features

## [1.5.0] - 2023-06-15

### Added
- XP Booster system allowing tools to provide skill XP multipliers
- Command to apply, remove, and check XP boosters (`/rpgbooster`)
- Support for both permanent and temporary boosters
- Visual indicators for boosted tools in lore text
- Admin API for managing boosters programmatically
- Documentation for XP booster system

### Changed
- Improved XP calculation system to account for boosters
- Updated GUI system for better performance
- Enhanced command tab completion for all commands

### Fixed
- Issue with XP not being properly saved in some cases
- Party XP sharing calculation bug
- Multiple minor bugs in skill tree system

## [1.4.0] - 2023-04-20

### Added
- Party system with XP sharing between members
- Party leveling with unlockable perks
- Party chat functionality
- Commands for party management (`/party`)
- Economy integration for purchasing party perks
- Configuration options for parties in `party.yml`

### Changed
- Reworked XP sharing mechanics to be more balanced
- Updated scoreboard system to display party information
- Improved performance for player data loading

### Fixed
- Skill tree GUI not displaying correctly on some resolutions
- Permission checking bug in admin commands
- Data loss issue when server crashed during save

## [1.3.0] - 2023-02-10

### Added
- Skill tree system with unlockable nodes
- Skill points earned when leveling up
- GUI for interacting with the skill tree
- Commands for skill tree management (`/skilltree`)
- Node types: stat boosts, perks, masteries, and connectors
- Configuration file for skill tree in `skill_tree.yml`

### Changed
- Refactored player data storage for better performance
- Enhanced command feedback messages
- Updated all GUIs to match the new design style

### Fixed
- Issue with passive abilities not triggering in certain worlds
- Problem with XP formula calculation at higher levels
- Several minor bugs in the skills system

## [1.2.0] - 2022-12-05

### Added
- Active abilities that can be triggered with commands
- Cooldown system for abilities
- GUI for viewing and using abilities (`/abilities`)
- Particle and sound effects for ability activation
- Configuration options for abilities in `abilities.yml`
- PlaceholderAPI integration for ability information

### Changed
- Improved passive ability activation logic
- Updated notification system for ability triggers
- Optimized database queries for player data

### Fixed
- Bug with passive abilities not working in certain regions
- Issue with XP notifications stacking excessively
- Problem with admin commands not respecting permission hierarchy

## [1.1.0] - 2022-10-15

### Added
- Passive abilities that trigger automatically based on skill level
- Chance-based activation system for passives
- Configuration file for passive abilities (`passives.yml`)
- Admin commands for managing player skills (`/skillsadmin`)
- MySQL support for data storage (alternative to YAML)
- API for other plugins to hook into the skills system

### Changed
- Reworked XP calculation formula for better progression
- Improved GUI design for skill information
- Enhanced command feedback with more detailed messages

### Fixed
- Several performance issues with large player counts
- Bug with XP not being awarded in certain cases
- Issue with skill levels not loading correctly on server restart

## [1.0.0] - 2022-08-01

### Added
- Initial release of RPG Skills Plugin
- 8 core skills: Mining, Logging, Farming, Fishing, Fighting, Excavation, Enchanting, and Repair
- Leveling system with XP gain from activities
- Custom XP values configurable in `skills.yml`
- Basic commands (`/skills`, `/skills info`)
- Skills GUI to view progress and information
- Player data storage in YAML files
- Configuration system with `config.yml`
- Permission system for accessing skills

[Unreleased]: https://github.com/frizzlenpop/RPGSkillsPlugin/compare/v1.5.0...HEAD
[1.5.0]: https://github.com/frizzlenpop/RPGSkillsPlugin/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/frizzlenpop/RPGSkillsPlugin/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/frizzlenpop/RPGSkillsPlugin/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/frizzlenpop/RPGSkillsPlugin/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/frizzlenpop/RPGSkillsPlugin/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/frizzlenpop/RPGSkillsPlugin/releases/tag/v1.0.0 