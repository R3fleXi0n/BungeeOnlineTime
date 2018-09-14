package lu.r3flexi0n.bungeeonlinetime.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;

public class Language {

    public static String onlyPlayer = "&7This command can only be executed by players.";
    public static String noPermission = "&7You do not have access to this command.";
    public static String error = "&7An error occured.";
    public static String errorSaving = "&7Error while saving your onlinetime.";
    public static String playerNotFound = "&7Player '&6%PLAYER%&7' was not found.";
    public static String wrongFormat = "&7Wrong format: Enter the date like this &6%FORMAT%&7.";
    public static String onlineTime = "&6%PLAYER%&7's onlinetime: &6%HOURS%&7h &6%MINUTES%&7min";
    public static String onlineTimeSince = "&6%PLAYER%&7's onlinetime since &6%DATE%&7: &6%HOURS%&7h &6%MINUTES%&7min";
    public static String topTimeAbove = "&7====== &6Top 10 &7======";
    public static String topTime = "&6%PLAYER%&7: &6%HOURS%&7h &6%MINUTES%&7min";
    public static String topTimeBelow = "&7====== &6Top 10 &7======";
    public static String topTimeSinceAbove = "&7=== &6Top 10 &7since &6%DATE% &7===";
    public static String topTimeSince = "&6%PLAYER%&7: &6%HOURS%&7h &6%MINUTES%&7min";
    public static String topTimeSinceBelow = "&7=== &6Top 10 &7since &6%DATE% &7===";
    public static String resetAll = "&7The database has been reset.";
    public static String resetPlayer = "&6%PLAYER%&7's onlinetime has been reset.";
    public static String resetAllBefore = "&7All entries before &6%DATE% &7have been removed.";
    public static String resetPlayerBefore = "&6%PLAYER%&7's onlinetime before &6%DATE% &7has been removed.";

    public static void create(Configuration config) {
        Utils.addDefault(config, "Language.onlyPlayer", onlyPlayer);
        Utils.addDefault(config, "Language.noPermission", noPermission);
        Utils.addDefault(config, "Language.error", error);
        Utils.addDefault(config, "Language.errorSaving", errorSaving);
        Utils.addDefault(config, "Language.playerNotFound", playerNotFound);
        Utils.addDefault(config, "Language.wrongFormat", wrongFormat);
        Utils.addDefault(config, "Language.onlineTime", onlineTime);
        Utils.addDefault(config, "Language.onlineTimeSince", onlineTimeSince);
        Utils.addDefault(config, "Language.topTimeAbove", topTimeAbove);
        Utils.addDefault(config, "Language.topTime", topTime);
        Utils.addDefault(config, "Language.topTimeBelow", topTimeBelow);
        Utils.addDefault(config, "Language.topTimeSinceAbove", topTimeSinceAbove);
        Utils.addDefault(config, "Language.topTimeSince", topTimeSince);
        Utils.addDefault(config, "Language.topTimeSinceBelow", topTimeSinceBelow);
        Utils.addDefault(config, "Language.resetAll", resetAll);
        Utils.addDefault(config, "Language.resetPlayer", resetPlayer);
        Utils.addDefault(config, "Language.resetAllBefore", resetAllBefore);
        Utils.addDefault(config, "Language.resetPlayerBefore", resetPlayerBefore);
    }

    public static void load(Configuration config) {
        onlyPlayer = ChatColor.translateAlternateColorCodes('&', config.getString("Language.onlyPlayer"));
        noPermission = ChatColor.translateAlternateColorCodes('&', config.getString("Language.noPermission"));
        error = ChatColor.translateAlternateColorCodes('&', config.getString("Language.error"));
        errorSaving = ChatColor.translateAlternateColorCodes('&', config.getString("Language.errorSaving"));
        playerNotFound = ChatColor.translateAlternateColorCodes('&', config.getString("Language.playerNotFound"));
        wrongFormat = ChatColor.translateAlternateColorCodes('&', config.getString("Language.wrongFormat"));
        onlineTime = ChatColor.translateAlternateColorCodes('&', config.getString("Language.onlineTime"));
        onlineTimeSince = ChatColor.translateAlternateColorCodes('&', config.getString("Language.onlineTimeSince"));
        topTimeAbove = ChatColor.translateAlternateColorCodes('&', config.getString("Language.topTimeAbove"));
        topTime = ChatColor.translateAlternateColorCodes('&', config.getString("Language.topTime"));
        topTimeBelow = ChatColor.translateAlternateColorCodes('&', config.getString("Language.topTimeBelow"));
        topTimeSinceAbove = ChatColor.translateAlternateColorCodes('&', config.getString("Language.topTimeSinceAbove"));
        topTimeSince = ChatColor.translateAlternateColorCodes('&', config.getString("Language.topTimeSince"));
        topTimeSinceBelow = ChatColor.translateAlternateColorCodes('&', config.getString("Language.topTimeSinceBelow"));
        resetAll = ChatColor.translateAlternateColorCodes('&', config.getString("Language.resetAll"));
        resetPlayer = ChatColor.translateAlternateColorCodes('&', config.getString("Language.resetPlayer"));
        resetAllBefore = ChatColor.translateAlternateColorCodes('&', config.getString("Language.resetAllBefore"));
        resetPlayerBefore = ChatColor.translateAlternateColorCodes('&', config.getString("Language.resetPlayerBefore"));
    }

}
