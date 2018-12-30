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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lu.r3flexi0n.bungeeonlinetime.database.MySQL;
import lu.r3flexi0n.bungeeonlinetime.database.SQL;
import lu.r3flexi0n.bungeeonlinetime.database.SQLite;
import lu.r3flexi0n.bungeeonlinetime.utils.Language;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeOnlineTime extends Plugin {

    public static BungeeOnlineTime INSTANCE;

    public static SQL SQL;

    public static boolean MYSQL_ENABLED = false;
    public static String COMMAND_ALIASES = "ot,pt,playtime";
    public static List<String> DISABLED_SERVERS = Arrays.asList("lobby");
    public static int TOP_ONLINETIMES_LIMIT = 10;

    private String host, database, username, password;
    private Integer port;

    public static File CONFIG_FILE;
    public static ConfigurationProvider CONFIG_PROVIDER = ConfigurationProvider.getProvider(YamlConfiguration.class);

    public static final String CHANNEL = "bungeeonlinetime:get";

    public static final Map<UUID, OnlinePlayer> ONLINE_PLAYERS = new HashMap<>();

    @Override
    public void onEnable() {

        INSTANCE = this;

        try {
            createConfig();
            loadConfig();
        } catch (IOException ex) {
            System.out.println("[BungeeOnlineTime] Error while creating/loading config.");
            ex.printStackTrace();
            return;
        }

        if (MYSQL_ENABLED) {

            SQL = new MySQL(host, port, database, username, password);

        } else {

            try {
                System.out.println("[BungeeOnlineTime] Downloading SQLite...");
                URL url = downloadSQLite();
                addLibrary(url);
                System.out.println("[BungeeOnlineTime] Downloadeded SQLite.");
            } catch (Exception ex) {
                System.out.println("[BungeeOnlineTime] Error while downloading SQLite.");
                ex.printStackTrace();
                return;
            }

            File dbFile = new File(getDataFolder(), "BungeeOnlineTime.db");
            SQL = new SQLite(dbFile);
        }

        try {
            System.out.println("[BungeeOnlineTime] Connecting to SQL...");
            SQL.openConnection();
            SQL.createTable();
            System.out.println("[BungeeOnlineTime] Connected to SQL.");
        } catch (Exception ex) {
            System.out.println("[BungeeOnlineTime] Error while connecting to SQL.");
            ex.printStackTrace();
            return;
        }

        getProxy().getPluginManager().registerCommand(this, new OnlineTimeCommand("onlinetime", null, COMMAND_ALIASES.split(",")));
        getProxy().getPluginManager().registerListener(this, new OnlineTimeListener());
        getProxy().registerChannel(CHANNEL);
    }

    private void createConfig() throws IOException {

        File folder = new File(getDataFolder().getPath());
        if (!folder.exists()) {
            folder.mkdir();
        }

        CONFIG_FILE = new File(getDataFolder(), "config.yml");

        if (!CONFIG_FILE.exists()) {
            CONFIG_FILE.createNewFile();
        }

        Configuration config = CONFIG_PROVIDER.load(CONFIG_FILE);

        addDefault(config, "Settings.mysql", MYSQL_ENABLED);
        addDefault(config, "Settings.commandAliases", COMMAND_ALIASES);
        addDefault(config, "Settings.disabledServers", DISABLED_SERVERS);
        addDefault(config, "Settings.topOnlineTimesLimit", TOP_ONLINETIMES_LIMIT);

        addDefault(config, "MySQL.host", "localhost");
        addDefault(config, "MySQL.port", 3306);
        addDefault(config, "MySQL.database", "minecraft");
        addDefault(config, "MySQL.username", "player");
        addDefault(config, "MySQL.password", "abc123");

        Language.create(config);

        CONFIG_PROVIDER.save(config, CONFIG_FILE);
    }

    private void loadConfig() throws IOException {
        Configuration config = CONFIG_PROVIDER.load(CONFIG_FILE);

        MYSQL_ENABLED = config.getBoolean("Settings.mysql");
        COMMAND_ALIASES = config.getString("Settings.commandAliases");
        DISABLED_SERVERS = config.getStringList("Settings.disabledServers");
        TOP_ONLINETIMES_LIMIT = config.getInt("Settings.topOnlineTimesLimit");

        if (MYSQL_ENABLED) {
            host = config.getString("MySQL.host");
            port = config.getInt("MySQL.port");
            database = config.getString("MySQL.database");
            username = config.getString("MySQL.username");
            password = config.getString("MySQL.password");
        }

        Language.load(config);
    }

    private void addDefault(Configuration config, String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    private URL downloadSQLite() throws IOException {
        Path path = Paths.get("plugins/BungeeOnlineTime/SQLite.jar");
        if (!Files.exists(path)) {
            URL url = new URL("https://bitbucket.org/xerial/sqlite-jdbc/downloads/sqlite-jdbc-3.23.1.jar");
            InputStream in = url.openStream();
            Files.copy(in, path);
            in.close();
        }
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
