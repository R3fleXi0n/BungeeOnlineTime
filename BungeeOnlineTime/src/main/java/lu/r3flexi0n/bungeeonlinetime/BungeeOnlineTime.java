package lu.r3flexi0n.bungeeonlinetime;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lu.r3flexi0n.bungeeonlinetime.command.OnlineTimeCommand;
import lu.r3flexi0n.bungeeonlinetime.utils.MySQL;
import lu.r3flexi0n.bungeeonlinetime.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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

    public static List<String> disabledServers = new ArrayList<String>();

    public static String lastReset;

    public static String noPermission, playerNotFound, onlineTime, resetDatabase, resetPlayer, topPlayersAbove,
            topPlayers, topPlayersBelow, onlyPlayer, error;

    public void onEnable() {
        instance = this;

        try {
            createConfig();
            loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            mysql = new MySQL(host, port, database, username, password);
            mysql.openConnection();

            mysql.updateSync("CREATE TABLE IF NOT EXISTS onlineTime (UUID VARCHAR(36) UNIQUE, Name VARCHAR(16), OnlineTime INT);");

            ResultSet resultset = mysql.querySync("SHOW TABLES LIKE 'BungeeOnlineTime';"); //old Table exists
            if (resultset.next()) {
                mysql.updateSync("INSERT INTO onlineTime SELECT UUID, Player, OnlineTime FROM BungeeOnlineTime GROUP BY UUID;");
                mysql.updateSync("DROP TABLE BungeeOnlineTime;");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        getProxy().getPluginManager().registerCommand(this, new OnlineTimeCommand("onlinetime", null, "ot"));

        startScheduler();
    }

    public void createConfig() throws IOException {

        File folder = new File(getDataFolder().getPath());
        if (!folder.exists()) {
            folder.mkdir();
        }

        configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            configFile.createNewFile();
        }

        Configuration config = configurationProvider.load(configFile);

        addDefault(config, "lastReset", Utils.getDate());

        addDefault(config, "MySQL.host", "localhost");
        addDefault(config, "MySQL.port", 3306);
        addDefault(config, "MySQL.database", "minecraft");
        addDefault(config, "MySQL.username", "player");
        addDefault(config, "MySQL.password", "abc123");

        addDefault(config, "Settings.disabledServers", Arrays.asList("lobby", "testserver"));

        addDefault(config, "Language.noPermission", "&7You do not have access to this command.");
        addDefault(config, "Language.playerNotFound", "&7The player '&6%PLAYER%&7' does not exist.");
        addDefault(config, "Language.onlineTime", "&7Since &6%DATE%&7, &6%PLAYER% &7has been online for &6%HOURS% &7hours and &6%MINUTES% &7minutes.");
        addDefault(config, "Language.resetDatabase", "&7The &6database &7has been &6reset&7.");
        addDefault(config, "Language.resetPlayer", "&6%PLAYER%&7s onlinetime has been &6reset&7.");
        addDefault(config, "Language.topPlayersAbove", "&6[] &7Top 10 players since %DATE% &6[]");
        addDefault(config, "Language.topPlayers", "&6%PLAYER% &7>> &6%HOURS% &7hours, &6%MINUTES% &7minutes");
        addDefault(config, "Language.topPlayersBelow", "&6[] &7Top 10 players since %DATE% &6[]");
        addDefault(config, "Language.onlyPlayer", "This command can only be executed by players.");
        addDefault(config, "Language.error", "&7An error occured.");

        configurationProvider.save(config, configFile);
    }

    private void addDefault(Configuration config, String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    public void loadConfig() throws IOException {
        Configuration config = configurationProvider.load(configFile);

        lastReset = config.getString("lastReset");

        host = config.getString("MySQL.host");
        port = config.getInt("MySQL.port");
        database = config.getString("MySQL.database");
        username = config.getString("MySQL.username");
        password = config.getString("MySQL.password");

        for (String servers : config.getStringList("Settings.disabledServers")) {
            disabledServers.add(servers);
        }

        noPermission = ChatColor.translateAlternateColorCodes('&', config.getString("Language.noPermission"));
        playerNotFound = ChatColor.translateAlternateColorCodes('&', config.getString("Language.playerNotFound"));
        onlineTime = ChatColor.translateAlternateColorCodes('&', config.getString("Language.onlineTime"));
        resetDatabase = ChatColor.translateAlternateColorCodes('&', config.getString("Language.resetDatabase"));
        resetPlayer = ChatColor.translateAlternateColorCodes('&', config.getString("Language.resetPlayer"));
        topPlayersAbove = ChatColor.translateAlternateColorCodes('&', config.getString("Language.topPlayersAbove"));
        topPlayers = ChatColor.translateAlternateColorCodes('&', config.getString("Language.topPlayers"));
        topPlayersBelow = ChatColor.translateAlternateColorCodes('&', config.getString("Language.topPlayersBelow"));
        onlyPlayer = ChatColor.translateAlternateColorCodes('&', config.getString("Language.onlyPlayer"));
        error = ChatColor.translateAlternateColorCodes('&', config.getString("Language.error"));
    }

    public void startScheduler() {
        getProxy().getScheduler().schedule(this, new Runnable() {
            public void run() {
                ArrayList<String> sql = new ArrayList<String>();
                for (ProxiedPlayer players : getProxy().getPlayers()) {
                    if (players.hasPermission("onlinetime.save")) {
                        if (players.getServer() != null && players.getServer().getInfo() != null && !disabledServers.contains(players.getServer().getInfo().getName())) {
                            sql.add("INSERT INTO onlineTime (UUID, Name, OnlineTime) VALUES ('" + players.getUniqueId() + "','" + players.getName() + "','1') ON DUPLICATE KEY UPDATE Name = '" + players.getName() + "', OnlineTime = OnlineTime + 1;");
                        }
                    }
                }
                if (sql.size() > 0) {
                    mysql.batchAsync(sql, new Consumer<Boolean>() {
                        public void accept(Boolean t) {
                        }
                    });
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
}
