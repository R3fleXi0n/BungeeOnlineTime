package lu.r3flexi0n.bungeeonlinetime.settings;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SettingsFile {

    private final ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);

    private final File file;

    public SettingsFile(File file) {
        this.file = file;
    }

    public void create() throws Exception {
        File directory = file.getParentFile();
        if (directory != null && !directory.exists()) {
            boolean success = directory.mkdirs();
            if (!success) {
                throw new Exception();
            }
        }

        if (!file.exists()) {
            boolean success = file.createNewFile();
            if (!success) {
                throw new Exception();
            }
        }

    }

    public Configuration loadConfig() throws IOException {
        return provider.load(file);
    }

    public void saveConfig(Configuration config) throws IOException {
        provider.save(config, file);
    }

    public void addDefault(Configuration config, String key, Object value) {
        if (!config.contains(key)) {
            config.set(key, value);
        }
    }
}
