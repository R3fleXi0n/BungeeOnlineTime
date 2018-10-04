package lu.r3flexi0n.bungeeonlinetime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import lu.r3flexi0n.bungeeonlinetime.database.MySQL;
import lu.r3flexi0n.bungeeonlinetime.database.SQL;
import lu.r3flexi0n.bungeeonlinetime.database.SQLite;
import lu.r3flexi0n.bungeeonlinetime.utils.Language;
import lu.r3flexi0n.bungeeonlinetime.utils.Utils;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeOnlineTime extends Plugin {

    public static BungeeOnlineTime instance;

    public static SQL sql;

    public static boolean mysql = false;
    public static String dateFormat = "dd/MM/yyyy";
    public static String commandAliases = "ot,pt,playtime";
    public static List<String> disabledServers = Arrays.asList("lobby");

    private String host, database, username, password;
    private Integer port;

    public static File configFile;
    public static ConfigurationProvider configProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);

    public static final String CHANNEL = "bungeeonlinetime:get";

    @Override
    public void onEnable() {

        instance = this;

        try {
            createConfig();
            loadConfig();
        } catch (IOException ex) {
            System.out.println("[BungeeOnlineTime] Error while creating/loading config.");
            ex.printStackTrace();
            return;
        }

        if (mysql) {

            sql = new MySQL(host, port, database, username, password);

        } else {

            try {
                System.out.println("[BungeeOnlineTime] Downloading SQLite...");
                URL url = downloadSQLite();
                addLibrary(url);
                System.out.println("[BungeeOnlineTime] Downloaded SQLite.");
            } catch (Exception ex) {
                System.out.println("[BungeeOnlineTime] Error while downloading SQLite.");
                ex.printStackTrace();
            }

            File dbFile = new File(getDataFolder(), "BungeeOnlineTime.db");
            sql = new SQLite(dbFile);
        }

        try {
            System.out.println("[BungeeOnlineTime] Connecting to SQL...");
            sql.openConnection();
            sql.createTable();
            System.out.println("[BungeeOnlineTime] Connected to SQL.");
        } catch (Exception ex) {
            System.out.println("[BungeeOnlineTime] Error while connecting to SQL.");
            ex.printStackTrace();
        }

        getProxy().getPluginManager().registerCommand(this, new OnlineTimeCommand("onlinetime", null, commandAliases.split(",")));
        getProxy().getPluginManager().registerListener(this, new OnlineTimeListener());
        getProxy().registerChannel(CHANNEL);
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

        Configuration config = configProvider.load(configFile);

        Utils.addDefault(config, "Settings.mysql", mysql);
        Utils.addDefault(config, "Settings.dateFormat", dateFormat);
        Utils.addDefault(config, "Settings.commandAliases", commandAliases);
        Utils.addDefault(config, "Settings.disabledServers", disabledServers);

        Utils.addDefault(config, "MySQL.host", "localhost");
        Utils.addDefault(config, "MySQL.port", 3306);
        Utils.addDefault(config, "MySQL.database", "minecraft");
        Utils.addDefault(config, "MySQL.username", "player");
        Utils.addDefault(config, "MySQL.password", "abc123");

        Language.create(config);

        configProvider.save(config, configFile);
    }

    private void loadConfig() throws IOException {
        Configuration config = configProvider.load(configFile);

        mysql = config.getBoolean("Settings.mysql");
        dateFormat = config.getString("Settings.dateFormat");
        commandAliases = config.getString("Settings.commandAliases");
        disabledServers = config.getStringList("Settings.disabledServers");

        if (mysql) {
            host = config.getString("MySQL.host");
            port = config.getInt("MySQL.port");
            database = config.getString("MySQL.database");
            username = config.getString("MySQL.username");
            password = config.getString("MySQL.password");
        }

        Language.load(config);
    }

    private URL downloadSQLite() throws IOException {
        URL url = new URL("https://bitbucket.org/xerial/sqlite-jdbc/downloads/sqlite-jdbc-3.23.1.jar");
        InputStream in = url.openStream();
        Path path = Paths.get("SQLite.jar");
        Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        in.close();
        return path.toUri().toURL();
    }

    private void addLibrary(URL url) throws Exception {
        URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> loaderClass = URLClassLoader.class;
        Method method = loaderClass.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(loader, new Object[]{url});
    }
}
