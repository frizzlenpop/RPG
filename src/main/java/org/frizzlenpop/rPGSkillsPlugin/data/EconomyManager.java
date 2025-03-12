package org.frizzlenpop.rPGSkillsPlugin.data;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;

/**
 * Handles integration with Vault economy
 */
public class EconomyManager {
    private final RPGSkillsPlugin plugin;
    private Economy economy;
    private boolean economyEnabled = false;

    /**
     * Creates a new EconomyManager
     *
     * @param plugin The main plugin instance
     */
    public EconomyManager(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    /**
     * Sets up the economy integration
     *
     * @return true if economy setup was successful, false otherwise
     */
    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found! Economy features will be disabled.");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("No economy plugin found! Economy features will be disabled.");
            return false;
        }

        economy = rsp.getProvider();
        economyEnabled = true;
        plugin.getLogger().info("Vault economy integration enabled.");
        return true;
    }

    /**
     * Checks if economy is enabled
     *
     * @return true if economy is enabled, false otherwise
     */
    public boolean isEconomyEnabled() {
        return economyEnabled;
    }

    /**
     * Gets the player's balance
     *
     * @param player The player
     * @return The player's balance, or 0 if economy is disabled
     */
    public double getBalance(Player player) {
        if (!economyEnabled) return 0;
        return economy.getBalance(player);
    }

    /**
     * Withdraws money from a player
     *
     * @param player The player
     * @param amount The amount to withdraw
     * @return true if the withdrawal was successful, false otherwise
     */
    public boolean withdrawMoney(Player player, double amount) {
        if (!economyEnabled) return false;
        if (amount <= 0) return false;
        
        // Check if player has enough money
        if (economy.getBalance(player) < amount) {
            return false;
        }
        
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    /**
     * Deposits money to a player
     *
     * @param player The player
     * @param amount The amount to deposit
     * @return true if the deposit was successful, false otherwise
     */
    public boolean depositMoney(Player player, double amount) {
        if (!economyEnabled) return false;
        if (amount <= 0) return false;
        
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    /**
     * Formats the amount as a currency string
     *
     * @param amount The amount to format
     * @return The formatted amount, or the plain amount as a string if economy is disabled
     */
    public String formatCurrency(double amount) {
        if (!economyEnabled) {
            return String.format("%.2f", amount);
        }
        return economy.format(amount);
    }
} 