package lu.r3flexi0n.bungeeonlinetime.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;

public class Language {

    public static String ONLY_PLAYER = "&7This command can only be executed by players.";
    public static String NO_PERMISSION = "&7You do not have access to this command.";
    public static String ERROR = "&7An error occured.";
    public static String ERROR_SAVING = "&7Error while saving your onlinetime.";
    public static String PLAYER_NOT_FOUND = "&7Player '&6%PLAYER%&7' was not found.";
    public static String ONLINE_TIME = "&6%PLAYER%&7's onlinetime: &6%HOURS%&7h &6%MINUTES%&7min";
    public static String TOP_TIME_LOADING = "&7The Top 10 is loading...";
    public static String TOP_TIME_ABOVE = "&7====== &6Top 10 &7======";
    public static String TOP_TIME = "&6%PLAYER%&7: &6%HOURS%&7h &6%MINUTES%&7min";
    public static String TOP_TIME_BELOW = "&7====== &6Top 10 &7======";
    public static String RESET_ALL = "&7The database has been reset.";
    public static String RESET_PLAYER = "&6%PLAYER%&7's onlinetime has been reset.";

    public static void create(Configuration config) {
        addDefault(config, "Language.onlyPlayer", ONLY_PLAYER);
        addDefault(config, "Language.noPermission", NO_PERMISSION);
        addDefault(config, "Language.error", ERROR);
        addDefault(config, "Language.errorSaving", ERROR_SAVING);
        addDefault(config, "Language.playerNotFound", PLAYER_NOT_FOUND);
        addDefault(config, "Language.onlineTime", ONLINE_TIME);
        addDefault(config, "Language.topTimeLoading", TOP_TIME_LOADING);
        addDefault(config, "Language.topTimeAbove", TOP_TIME_ABOVE);
        addDefault(config, "Language.topTime", TOP_TIME);
        addDefault(config, "Language.topTimeBelow", TOP_TIME_BELOW);
        addDefault(config, "Language.resetAll", RESET_ALL);
        addDefault(config, "Language.resetPlayer", RESET_PLAYER);
    }

    public static void load(Configuration config) {
        ONLY_PLAYER = ChatColor.translateAlternateColorCodes('&', config.getString("Language.onlyPlayer"));
        NO_PERMISSION = ChatColor.translateAlternateColorCodes('&', config.getString("Language.noPermission"));
        ERROR = ChatColor.translateAlternateColorCodes('&', config.getString("Language.error"));
        ERROR_SAVING = ChatColor.translateAlternateColorCodes('&', config.getString("Language.errorSaving"));
        PLAYER_NOT_FOUND = ChatColor.translateAlternateColorCodes('&', config.getString("Language.playerNotFound"));
        ONLINE_TIME = ChatColor.translateAlternateColorCodes('&', config.getString("Language.onlineTime"));
        TOP_TIME_LOADING = ChatColor.translateAlternateColorCodes('&', config.getString("Language.topTimeLoading"));
        TOP_TIME_ABOVE = ChatColor.translateAlternateColorCodes('&', config.getString("Language.topTimeAbove"));
        TOP_TIME = ChatColor.translateAlternateColorCodes('&', config.getString("Language.topTime"));
        TOP_TIME_BELOW = ChatColor.translateAlternateColorCodes('&', config.getString("Language.topTimeBelow"));
        RESET_ALL = ChatColor.translateAlternateColorCodes('&', config.getString("Language.resetAll"));
        RESET_PLAYER = ChatColor.translateAlternateColorCodes('&', config.getString("Language.resetPlayer"));
    }

    private static void addDefault(Configuration config, String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }
}
