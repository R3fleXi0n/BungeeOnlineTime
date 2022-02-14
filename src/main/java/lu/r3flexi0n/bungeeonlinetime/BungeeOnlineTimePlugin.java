package lu.r3flexi0n.bungeeonlinetime;

import lu.r3flexi0n.bungeeonlinetime.database.Database;
import lu.r3flexi0n.bungeeonlinetime.database.MySQLDatabase;
import lu.r3flexi0n.bungeeonlinetime.database.SQLiteDatabase;
import lu.r3flexi0n.bungeeonlinetime.objects.OnlineTimePlayer;
import lu.r3flexi0n.bungeeonlinetime.settings.PluginSettings;
import lu.r3flexi0n.bungeeonlinetime.settings.SettingsFile;
import lu.r3flexi0n.bungeeonlinetime.utils.Utils;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BungeeOnlineTimePlugin extends Plugin {

    public HashMap<String, Object> settings;

    public Database database;

    public final Map<UUID, OnlineTimePlayer> onlineTimePlayers = new HashMap<>();

    public final String pluginMessageChannel = "bungeeonlinetime:get";

    @Override
    public void onEnable() {

        SettingsFile settingsFile = new SettingsFile(new File(getDataFolder(), "settings.yml"));
        try {
            settingsFile.create();
        } catch (Exception ex) {
            Utils.log("Error while creating settings file. Disabling plugin...");
            ex.printStackTrace();
            return;
        }

        PluginSettings pluginSettings = new PluginSettings(settingsFile);
        try {
            pluginSettings.setDefaultSettings();
            settings = pluginSettings.loadSettings();
        } catch (Exception ex) {
            Utils.log("Error while loading settings file. Disabling plugin...");
            ex.printStackTrace();
            return;
        }

        boolean mysqlEnabled = (boolean) settings.get("MySQL.enabled");
        if (mysqlEnabled) {

            String host = (String) settings.get("MySQL.host");
            int port = (int) settings.get("MySQL.port");
            String db = (String) settings.get("MySQL.database");
            String user = (String) settings.get("MySQL.username");
            String pass = (String) settings.get("MySQL.password");

            database = new MySQLDatabase(host, port, db, user, pass);

        } else {

            File databaseFile = new File(getDataFolder(), "BungeeOnlineTime.db");

            database = new SQLiteDatabase(databaseFile);

        }

        try {
            Utils.log("Connecting to " + database.databaseName + "...");
            database.openConnection();
            database.createTable();
            database.createIndex();
            Utils.log("Successfully connected to " + database.databaseName + ".");
        } catch (Exception ex) {
            Utils.log("Error while connecting to " + database.databaseName + ". Disabling plugin...");
            ex.printStackTrace();
            return;
        }

        String[] commandAliases = ((List<String>) settings.get("Plugin.commandAliases")).toArray(new String[0]);

        OnlineTimeCommand command = new OnlineTimeCommand(this, commandAliases[0], null, commandAliases);
        getProxy().getPluginManager().registerCommand(this, command);

        OnlineTimeListener listener = new OnlineTimeListener(this);
        getProxy().getPluginManager().registerListener(this, listener);

        boolean usePlaceholderApi = (boolean) settings.get("Plugin.usePlaceholderApi");
        if (usePlaceholderApi) {
            getProxy().registerChannel(pluginMessageChannel);
        }
    }
}
