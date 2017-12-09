package lu.r3flexi0n.bungeeonlinetime;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeOnlineTime extends Plugin {

    public static BungeeOnlineTime instance;

    public static MySQL mysql;
    private String host, database, username, password;
    private Integer port;

    public static File configFile;
    public static ConfigurationProvider configurationProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);

    @Override
    public void onEnable() {

        instance = this;

        try {
            createConfig();
            loadConfig();
        } catch (IOException ex) {
            System.out.println("[BungeeOnlineTime] Error while creating/loading config: " + ex.getMessage());
            return;
        }

        try {
            mysql = new MySQL(host, port, database, username, password);
            mysql.openConnection();
            mysql.createTable();
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("[BungeeOnlineTime] Error while connecting to MySQL: " + ex.getMessage());
            return;
        }

        getProxy().getPluginManager().registerCommand(this, new OnlineTimeCommand("onlinetime", null, "ot"));
        getProxy().getPluginManager().registerListener(this, new OnlineTimeListener());
    }

    private void createConfig() throws IOException {

        File folder = new File(getDataFolder().getPath());
        if (!folder.exists()) {
            folder.mkdir();
        }

        configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            configFile.createNewFile();
        }

        Configuration config = configurationProvider.load(configFile);

        addDefault(config, "MySQL.host", "localhost");
        addDefault(config, "MySQL.port", 3306);
        addDefault(config, "MySQL.database", "minecraft");
        addDefault(config, "MySQL.username", "player");
        addDefault(config, "MySQL.password", "abc123");

        addDefault(config, "Settings.dateFormat", dateFormat);

        addDefault(config, "Language.onlyPlayer", onlyPlayer);
        addDefault(config, "Language.noPermission", noPermission);
        addDefault(config, "Language.error", error);
        addDefault(config, "Language.errorSaving", errorSaving);
        addDefault(config, "Language.playerNotFound", playerNotFound);
        addDefault(config, "Language.wrongFormat", wrongFormat);
        addDefault(config, "Language.onlineTime", onlineTime);
        addDefault(config, "Language.onlineTimeSince", onlineTimeSince);
        addDefault(config, "Language.topTimeAbove", topTimeAbove);
        addDefault(config, "Language.topTime", topTime);
        addDefault(config, "Language.topTimeBelow", topTimeBelow);
        addDefault(config, "Language.topTimeSinceAbove", topTimeSinceAbove);
        addDefault(config, "Language.topTimeSince", topTimeSince);
        addDefault(config, "Language.topTimeSinceBelow", topTimeSinceBelow);
        addDefault(config, "Language.resetAll", resetAll);
        addDefault(config, "Language.resetPlayer", resetPlayer);
        addDefault(config, "Language.resetAllBefore", resetAllBefore);
        addDefault(config, "Language.resetPlayerBefore", resetPlayerBefore);

        configurationProvider.save(config, configFile);
    }

    private void addDefault(Configuration config, String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    private void loadConfig() throws IOException {
        Configuration config = configurationProvider.load(configFile);

        host = config.getString("MySQL.host");
        port = config.getInt("MySQL.port");
        database = config.getString("MySQL.database");
        username = config.getString("MySQL.username");
        password = config.getString("MySQL.password");

        dateFormat = config.getString("Settings.dateFormat");

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

    public static String dateFormat = "dd/MM/yyyy";

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
}
