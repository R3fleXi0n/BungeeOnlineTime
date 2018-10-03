package lu.r3flexi0n.bungeeonlinetime.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;
import lu.r3flexi0n.bungeeonlinetime.BungeeOnlineTime;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class Utils {

    public static void addDefault(Configuration config, String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    public static Date getDate(String date) throws ParseException {
        DateFormat format = new SimpleDateFormat(BungeeOnlineTime.dateFormat);
        return format.parse(date);
    }

    public static LinkedHashMap<UUID, String> nameCache = new LinkedHashMap<>();

    public static String getName(UUID uuid) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }

        String name = nameCache.get(uuid);
        if (name != null) {
            return name;
        }

        try {
            name = getNameFromMojang(uuid);
            if (nameCache.size() > 100) {
                nameCache.clear();
            } else {
                nameCache.put(uuid, name);
            }
            return name;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static UUID getUUID(String name) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(name);
        if (player != null) {
            return player.getUniqueId();
        }

        try {
            return getUUIDFromMojang(name);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static final JsonParser JSON = new JsonParser();

    private static JsonElement getJsonFromURL(String link) throws IOException {
        URL url = new URL(link);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        JsonElement element = JSON.parse(reader);
        reader.close();
        return element;
    }

    public static String getNameFromMojang(UUID uuid) throws IOException {
        JsonElement root = getJsonFromURL("https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names");
        if (!root.isJsonArray()) {
            return null;
        }
        JsonArray array = (JsonArray) root;
        JsonObject object = (JsonObject) array.get(array.size() - 1);
        return object.get("name").getAsString();
    }

    public static UUID getUUIDFromMojang(String name) throws IOException {
        JsonElement root = getJsonFromURL("https://api.mojang.com/users/profiles/minecraft/" + name);
        if (!root.isJsonObject()) {
            return null;
        }
        JsonObject object = (JsonObject) root;
        String uuid = object.get("id").getAsString().replaceAll("(.{8})(.{4})(.{4})(.{4})(.+)", "$1-$2-$3-$4-$5");
        return UUID.fromString(uuid);
    }

}
