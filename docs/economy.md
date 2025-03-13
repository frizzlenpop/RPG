# Economy Integration

## Overview

The RPG Skills Plugin integrates with Vault to provide economy features throughout the plugin. This integration allows players to purchase various upgrades, perks, and features using the server's existing economy.

## Dependencies

To use the economy features, you need:

- **Vault** - The API that connects the plugin to your economy system
- **An Economy Plugin** - Such as EssentialsX, iConomy, or any other Vault-compatible economy plugin

## Configuration

Economy features can be enabled or disabled in the `config.yml` file:

```yaml
general:
  economy_enabled: true  # Set to false to disable all economy features
  
economy:
  currency_name_singular: "coin"  # Name of your currency (singular)
  currency_name_plural: "coins"   # Name of your currency (plural)
  format_money: "%amount% %currency%"  # How to format money displays
```

## Economy-Enabled Features

The following features use the economy system:

### Party Perks

Players can purchase party perks with in-game currency:

```yaml
party:
  perks:
    xp_boost_1:
      display_name: "XP Boost I"
      description: "+5% XP for all party members"
      icon: EXPERIENCE_BOTTLE
      cost: 5000  # Cost in economy currency
      required_level: 1
      multiplier: 1.05
```

### Skill Tree Resets

Players can pay to reset their skill tree:

```yaml
skill_tree:
  reset:
    cost: 10000  # Cost to reset the skill tree
    free_reset_level: 20  # Players below this level can reset for free
```

### Ability Purchases

Some abilities can be purchased rather than unlocked through leveling:

```yaml
abilities:
  miningburst:
    enabled: true
    display_name: "Mining Burst"
    purchase:
      enabled: true
      cost: 7500  # Cost to purchase the ability
```

## Commands

The following commands interact with the economy system:

| Command | Description | Cost |
|---------|-------------|------|
| `/party perk buy <perk>` | Purchase a party perk | Varies by perk |
| `/skilltree reset` | Reset your skill tree | Configurable |
| `/abilities buy <ability>` | Purchase an ability | Varies by ability |

## Transaction Methods

The plugin handles economy transactions through these methods:

1. **Checking Balance** - Before allowing purchases, the plugin verifies the player has sufficient funds
2. **Withdrawing Funds** - Upon successful purchase, the plugin withdraws the appropriate amount
3. **Refunding** - In certain cases (like cancellations), funds may be refunded

## Economy Events

The plugin provides custom events for other plugins to hook into:

- `RPGSkillsEconomyTransactionEvent` - Fired when a transaction occurs
- `RPGSkillsEconomyPurchaseEvent` - Fired specifically for purchases

## Economy Admin Commands

Administrators have access to these economy-related commands:

| Command | Description |
|---------|-------------|
| `/skillsadmin eco setcost <feature> <amount>` | Change the cost of a feature |
| `/skillsadmin eco refund <player> <transaction>` | Refund a specific transaction |
| `/skillsadmin eco transactions <player>` | View a player's transaction history |

## Transaction Logging

When enabled, the plugin logs all economy transactions:

```yaml
economy:
  log_transactions: true  # Set to false to disable logging
  log_file: "economy_log.yml"  # Where to store logs
```

## Best Practices

For server owners, we recommend:

1. **Balance your economy** - Ensure costs are appropriate for your server's economy
2. **Regularly review pricing** - Adjust costs based on server economy inflation
3. **Consider VIP discounts** - Use permission-based discounts for VIP players
4. **Monitor transaction logs** - Watch for unusual patterns or potential exploits

## Disabling Economy Features

If you don't want to use economy features:

1. Set `economy_enabled: false` in `config.yml`
2. Configure alternative unlock methods for features normally purchased

## Vault Hooks

For developers, the plugin hooks into Vault using:

```java
private boolean setupEconomy() {
    if (getServer().getPluginManager().getPlugin("Vault") == null) {
        return false;
    }
    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
        return false;
    }
    economy = rsp.getProvider();
    return economy != null;
}
```

## Compatibility

The economy integration has been tested with:

- **EssentialsX Economy**
- **iConomy**
- **CMI Economy**
- **GemsEconomy**

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Economy features not working | Ensure Vault and an economy plugin are properly installed |
| Transactions failing | Check console for error messages; verify player has sufficient funds |
| Incorrect prices | Ensure your configuration is using the correct format for costs |
| Vault errors | Check that your economy plugin is Vault-compatible |

---

For additional help with economy integration, join our [Discord](https://discord.gg/rpgskills) or check the [Common Issues](common_issues.md) documentation. 