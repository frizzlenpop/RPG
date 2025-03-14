package org.frizzlenpop.rPGSkillsPlugin.utils;

import org.bukkit.ChatColor;

/**
 * Utility class for handling color codes in chat messages.
 */
public class ColorUtils {
    
    /**
     * Converts color codes using & symbol to proper Minecraft color codes.
     * 
     * @param text The text to colorize
     * @return The colorized text
     */
    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    /**
     * Removes all color codes from a string.
     * 
     * @param text The text to strip colors from
     * @return The text without color codes
     */
    public static String stripColor(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(text);
    }
    
    /**
     * Gets the ChatColor object from a color code.
     * 
     * @param colorChar The color character (like 'a' for green)
     * @return The ChatColor object, or WHITE if not found
     */
    public static ChatColor getColor(char colorChar) {
        try {
            String colorStr = String.valueOf(colorChar).toLowerCase();
            
            return switch (colorStr) {
                case "0" -> ChatColor.BLACK;
                case "1" -> ChatColor.DARK_BLUE;
                case "2" -> ChatColor.DARK_GREEN;
                case "3" -> ChatColor.DARK_AQUA;
                case "4" -> ChatColor.DARK_RED;
                case "5" -> ChatColor.DARK_PURPLE;
                case "6" -> ChatColor.GOLD;
                case "7" -> ChatColor.GRAY;
                case "8" -> ChatColor.DARK_GRAY;
                case "9" -> ChatColor.BLUE;
                case "a" -> ChatColor.GREEN;
                case "b" -> ChatColor.AQUA;
                case "c" -> ChatColor.RED;
                case "d" -> ChatColor.LIGHT_PURPLE;
                case "e" -> ChatColor.YELLOW;
                case "f" -> ChatColor.WHITE;
                case "k" -> ChatColor.MAGIC;
                case "l" -> ChatColor.BOLD;
                case "m" -> ChatColor.STRIKETHROUGH;
                case "n" -> ChatColor.UNDERLINE;
                case "o" -> ChatColor.ITALIC;
                case "r" -> ChatColor.RESET;
                default -> ChatColor.WHITE;
            };
        } catch (Exception e) {
            return ChatColor.WHITE;
        }
    }
} 