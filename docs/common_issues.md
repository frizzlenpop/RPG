# Common Issues & Troubleshooting

This guide addresses the most common issues encountered with the RPG Skills Plugin and provides solutions to resolve them.

## Installation Issues

### Plugin Won't Load

**Symptoms:**
- Plugin doesn't appear in `/plugins` list
- Errors in console during server startup

**Solutions:**
1. **Check Server Version**: Ensure you're running Spigot/Paper 1.16.5 or higher
2. **Check Java Version**: Verify you're using Java 11 or higher
3. **Check for Dependencies**: Vault is required for economy features
4. **Check Console Errors**: Look for specific error messages during startup
5. **Try a Clean Install**: Delete the RPGSkills folder and let it regenerate

### Missing Dependencies

**Symptoms:**
- Console shows "Could not find Vault"
- Economy features don't work

**Solutions:**
1. **Install Vault**: Download and install Vault
2. **Install an Economy Plugin**: EssentialsX, CMI, or another Vault-compatible economy plugin
3. **Check Load Order**: Some plugins need to load before others

## XP Issues

### Players Not Gaining XP

**Symptoms:**
- Players perform actions but don't receive XP
- No XP notifications appearing

**Solutions:**
1. **Check Skill Enablement**: Ensure the skill is enabled in `skills.yml`
2. **Check Block/Action XP Values**: Verify XP values are set for those actions
3. **Check WorldGuard Regions**: If using WorldGuard, check if XP gain is blocked in that region
4. **Check Permissions**: Ensure player has permission for that skill
5. **Check Debug Mode**: Enable debug mode in config to see detailed XP logs

### XP Balance Issues

**Symptoms:**
- Some skills level up too fast/slow
- Player progress seems unbalanced

**Solutions:**
1. **Adjust Global Multiplier**: Change `xp.global_multiplier` in `config.yml`
2. **Adjust Skill-Specific Multipliers**: Change values in `xp.skill_multipliers`
3. **Review XP Values**: Check individual action XP values in `skills.yml`
4. **Review Level Formula**: Adjust the level requirement formula in `config.yml`

## Ability Issues

### Active Abilities Not Working

**Symptoms:**
- Nothing happens when using ability commands
- Error messages when attempting to use abilities

**Solutions:**
1. **Check Permissions**: Ensure player has the ability-specific permission
2. **Check Level Requirements**: Verify player meets the level requirement
3. **Check Cooldowns**: Player may be on cooldown (check with `/abilities`)
4. **Check Tool Requirements**: Some abilities require specific tools
5. **Check Configuration**: Ensure ability is enabled in `abilities.yml`

### Passive Abilities Not Triggering

**Symptoms:**
- Passive abilities don't seem to activate
- No notifications for passive ability triggers

**Solutions:**
1. **Check Chance Values**: Passive abilities use chance-based triggers
2. **Check Level Requirements**: Ensure player has reached required level
3. **Check Configurations**: Verify the passive is enabled in `passives.yml`
4. **Test with Higher Chance**: Temporarily increase trigger chance for testing

## Party Issues

### Can't Create/Join Parties

**Symptoms:**
- Party commands give errors
- Party invitations don't work

**Solutions:**
1. **Check Party System Enablement**: Ensure party system is enabled in `config.yml`
2. **Check Permissions**: Verify player has `rpgskills.party.create` permission
3. **Check Party Size Limits**: Player might be trying to join a full party
4. **Check Console for Errors**: Look for specific error messages
5. **Try Database Reset**: If database is corrupted, try resetting party data

### Party XP Sharing Issues

**Symptoms:**
- XP doesn't seem to be shared with party members
- Uneven XP distribution

**Solutions:**
1. **Check Share Percentage**: Verify `party.default_xp_share_percent` in `config.yml`
2. **Check Distance Settings**: Players might be too far apart
3. **Verify Party Status**: Ensure players are actually in the same party
4. **Check World Restrictions**: Some configurations restrict sharing across worlds

## Skill Tree Issues

### Can't Unlock Nodes

**Symptoms:**
- Clicking nodes doesn't unlock them
- Error messages when attempting to unlock

**Solutions:**
1. **Check Skill Points**: Ensure player has available skill points
2. **Check Requirements**: Verify player meets level and prerequisite requirements
3. **Check Connections**: Nodes must be connected to an already unlocked node
4. **Check Configuration**: Verify node exists in `skill_tree.yml`

### Missing Skill Points

**Symptoms:**
- Players not receiving skill points when leveling
- Skill points disappearing

**Solutions:**
1. **Check Point Formula**: Review skill point award formula in `config.yml`
2. **Check for Resets**: A recent skill tree reset might have returned points
3. **Check for Admin Commands**: Admin might have modified points
4. **Enable Debug Mode**: Get detailed logging about skill point transactions

## Economy Issues

### Can't Purchase Perks/Features

**Symptoms:**
- "Not enough money" messages
- Purchases fail with errors

**Solutions:**
1. **Check Economy Plugin**: Ensure your economy plugin is working properly
2. **Verify Vault Integration**: Check that Vault is connecting to your economy
3. **Check Prices**: Ensure prices in configuration are reasonable for your economy
4. **Check Player Balance**: Verify player actually has enough money
5. **Check Console for Errors**: Look for transaction failure messages

## Performance Issues

### Server Lag

**Symptoms:**
- TPS drops when many players use the plugin
- Lag spikes during certain activities

**Solutions:**
1. **Reduce XP Notifications**: Set `display_xp_notifications` to false in `config.yml`
2. **Optimize Scoreboard**: Increase the update interval in `scoreboard.update_interval`
3. **Limit Debug Logging**: Ensure `debug_mode` is set to false
4. **Check Event Handlers**: Plugin hooks into many events, ensure they're efficient
5. **Increase Save Interval**: Increase `data_save_interval` to reduce database writes

### Memory Usage

**Symptoms:**
- High memory usage
- OutOfMemoryErrors

**Solutions:**
1. **Increase Server Memory**: Allocate more RAM to your server
2. **Reduce Caching**: Adjust cache settings in advanced configuration
3. **Regular Restarts**: Schedule regular server restarts
4. **Update to Latest Version**: Newer versions often include optimizations

## Data Issues

### Lost Player Data

**Symptoms:**
- Players lose levels/progress
- Skills reset unexpectedly

**Solutions:**
1. **Check Backups**: Restore from automatic backup files
2. **Verify Storage Method**: Check if using YAML or MySQL storage
3. **Check for Corruption**: Database files might be corrupted
4. **Restore Default Data**: In worst case, may need to reset player data
5. **Enable Auto-Backups**: Set up more frequent backups

### Database Connection Issues

**Symptoms:**
- Console errors about database connection
- Player data not saving

**Solutions:**
1. **Check MySQL Settings**: Verify connection details in `config.yml`
2. **Check Database Server**: Ensure MySQL server is running and accessible
3. **Try Reconnect**: Use `/rpgskills dbreconnect` command
4. **Switch to YAML**: Temporarily switch to YAML storage if MySQL is problematic

## Permission Issues

### Permission Plugin Conflicts

**Symptoms:**
- Permissions don't apply correctly
- Players can't access features they should have

**Solutions:**
1. **Check Permission Syntax**: Verify exact permission nodes
2. **Check Permission Plugin**: Ensure your permission plugin is working
3. **Use Wildcards Correctly**: Understanding how * permissions work
4. **Check Permission Inheritance**: Group inheritance might cause issues

## GUI Issues

### GUIs Not Opening/Displaying Correctly

**Symptoms:**
- Clicking menu items does nothing
- GUI appears empty or with incorrect items

**Solutions:**
1. **Check Material Names**: Minecraft version changes may affect item names
2. **Check for Conflicts**: Other plugins might intercept inventory events
3. **Check Server Version**: GUI system is version-specific
4. **Try Resetting User Data**: Clear player-specific GUI data

## Getting Additional Help

If your issue is not listed here or the provided solutions don't resolve it:

1. **Check Full Documentation**: Review all documentation pages for your specific feature
2. **Join Discord Support**: Ask for help in our [Discord server](https://discord.gg/rpgskills)
3. **Submit GitHub Issue**: For potential bugs, [submit an issue](https://github.com/frizzlenpop/RPGSkillsPlugin/issues)
4. **Debug Mode**: Enable debug mode in config.yml to get more detailed logs

---

*This troubleshooting guide is regularly updated based on common user questions and issues. Last updated: June 2023.* 