package lu.r3flexi0n.bungeeonlinetime.settings;

import net.md_5.bungee.config.Configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PluginSettings {

    private final SettingsFile settingsFile;

    public PluginSettings(SettingsFile settingsFile) {
        this.settingsFile = settingsFile;
    }

    public void setDefaultSettings() throws IOException {
        Configuration config = settingsFile.loadConfig();
        for (Map.Entry<String, Object> entry : getDefaultSettings().entrySet()) {
            settingsFile.addDefault(config, entry.getKey(), entry.getValue());
        }
        settingsFile.saveConfig(config);
    }

    public HashMap<String, Object> loadSettings() throws IOException {
        HashMap<String, Object> settings = new HashMap<>();
        Configuration config = settingsFile.loadConfig();
        for (String key : getDefaultSettings().keySet()) {
            settings.put(key, config.get(key));
        }
        return settings;
    }

    private HashMap<String, Object> getDefaultSettings() {
        HashMap<String, Object> settings = new HashMap<>();
        settings.putAll(getDefaultPluginSettings());
        settings.putAll(getDefaultMySQLSettings());
        settings.putAll(getDefaultLanguageSettings());
        return settings;
    }

    private HashMap<String, Object> getDefaultPluginSettings() {
        HashMap<String, Object> settings = new HashMap<>();
        settings.put("Plugin.commandAliases", Arrays.asList("onlinetime", "ot"));
        settings.put("Plugin.disabledServers", Arrays.asList("lobby-1", "lobby-2"));
        settings.put("Plugin.topOnlineTimePageLimit", 10);
        settings.put("Plugin.usePlaceholderApi", false);
        return settings;
    }

    private HashMap<String, Object> getDefaultMySQLSettings() {
        HashMap<String, Object> settings = new HashMap<>();
        settings.put("MySQL.enabled", false);
        settings.put("MySQL.host", "localhost");
        settings.put("MySQL.port", 3306);
        settings.put("MySQL.database", "minecraft");
        settings.put("MySQL.username", "player");
        settings.put("MySQL.password", "abc123");
        return settings;
    }

    private HashMap<String, Object> getDefaultLanguageSettings() {
        HashMap<String, Object> settings = new HashMap<>();
        settings.put("Language.onlyPlayer", "&7This command can only be executed by players.");
        settings.put("Language.noPermission", "&7You do not have access to this command.");
        settings.put("Language.error", "&7An error occured.");
        settings.put("Language.playerNotFound", "&7Player '&6%PLAYER%&7' was not found.");
        settings.put("Language.onlineTime", "&6%PLAYER%&7's onlinetime: &6%HOURS%&7h &6%MINUTES%&7min");
        settings.put("Language.topTimeAbove", "&7====== &6Top 10 &7======");
        settings.put("Language.topTime", "&7#%RANK% &6%PLAYER%&7: &6%HOURS%&7h &6%MINUTES%&7min");
        settings.put("Language.topTimeBelow", "&7====== &6Page %PAGE% &7======");
        settings.put("Language.resetAll", "&7The database has been reset.");
        settings.put("Language.resetPlayer", "&6%PLAYER%&7's onlinetime has been reset.");
        settings.put("Language.help", "&7Usage:\n&7/onlinetime\n&7/onlinetime get <player>\n&7/onlinetime top [page]\n&7/onlinetime reset <player>\n&7/onlinetime resetall");
        return settings;
    }

}
