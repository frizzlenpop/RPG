# Installation Guide

## System Requirements

Before installing the RPG Skills Plugin, ensure your server meets these requirements:

- **Server Software**: Spigot, Paper, or any fork that supports Bukkit plugins
- **Minecraft Version**: 1.16.5 or higher
- **Java Version**: Java 11 or higher
- **Minimum RAM**: 2GB recommended (depends on server size)

## Dependencies

The plugin has the following dependencies:

- **Vault** (Required for economy features)
- **PlaceholderAPI** (Optional, for placeholders)
- **WorldGuard** (Optional, for region integration)

## Installation Steps

1. **Download the Plugin**
   - Download the latest version of RPG Skills Plugin from [SpigotMC](https://www.spigotmc.org/resources/rpgskills.XXXXX/)
   - Alternatively, download from our [GitHub Releases](https://github.com/frizzlenpop/RPGSkillsPlugin/releases)

2. **Install Dependencies**
   - Install Vault if you want to use economy features
   - Install other optional dependencies as needed

3. **Upload the Plugin**
   - Stop your Minecraft server if it's running
   - Upload the `RPGSkills.jar` file to your server's `plugins` folder
   - Also upload dependency plugins if needed

4. **Start Your Server**
   - Start your Minecraft server
   - The plugin will generate default configuration files in `plugins/RPGSkills/`

5. **Verify Installation**
   - Check your server console for the RPG Skills Plugin startup message
   - Log into your server and use the command `/skills` to verify the plugin is working

## Post-Installation Setup

1. **Configure Permissions**
   - Set up permissions for your players using a permissions plugin like LuckPerms
   - Basic permission: `rpgskills.use`
   - See the [Permissions Reference](permissions.md) for a complete list

2. **Basic Configuration**
   - Edit `config.yml` to customize basic settings
   - Customize skill XP values in `skills.yml`
   - Configure messages in `messages.yml`

3. **Set Up Economy (Optional)**
   - If using Vault, ensure an economy plugin is installed
   - Configure economy settings in the `config.yml`

4. **Set Admin Permissions**
   - Give server administrators the `rpgskills.admin` permission
   - This allows access to administrative commands

## Upgrading from Previous Versions

When upgrading from a previous version:

1. **Back Up Your Server**
   - Always back up your entire server before upgrading

2. **Back Up Configuration**
   - Create copies of your RPG Skills Plugin configuration files

3. **Review Changelog**
   - Check the [Changelog](../CHANGELOG.md) for breaking changes

4. **Replace the JAR File**
   - Stop your server
   - Replace the old JAR file with the new one
   - Start your server

5. **Update Configuration**
   - Compare your backed-up configuration with the new default configuration
   - Update your configuration files as needed
   - The plugin will add new configuration options automatically, but won't overwrite existing ones

## Troubleshooting

### Common Installation Issues

| Issue | Solution |
|-------|----------|
| Plugin doesn't load | Check server console for error messages. Verify Minecraft and Java versions meet requirements. |
| Missing dependency | Install the required dependencies mentioned above. |
| Configuration errors | Use a YAML validator to check for syntax errors in your configuration files. |
| Permissions not working | Ensure your permissions plugin is configured correctly. Check the exact permission nodes. |
| Economy features not working | Verify Vault is installed and an economy plugin is properly configured. |

### Getting Help

If you encounter issues during installation:

1. Check the [Common Issues](common_issues.md) documentation
2. Join our [Discord Server](https://discord.gg/rpgskills) for community support
3. Submit an issue on [GitHub](https://github.com/frizzlenpop/RPGSkillsPlugin/issues)

## Next Steps

After installation, we recommend:

1. Reading the [Quick Start Guide](quick_start.md)
2. Exploring the [Configuration Guide](configuration.md)
3. Setting up the [Skills System](skills_system.md)
4. Learning about [Admin Commands](admin_commands.md)

---

*If you find this plugin helpful, please consider leaving a review on SpigotMC!* 