package lu.r3flexi0n.bungeeonlinetime.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
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

    public static String getName(UUID uuid) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        try {
            return getNameFromMojang(uuid);
        } catch (IOException ex) {
            ex.printStackTrace();
            return "?";
        }
    }

    private static final JsonParser JSON = new JsonParser();

    public static String getNameFromMojang(UUID uuid) throws IOException {
        URL url = new URL("https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        JsonElement element = JSON.parse(reader);
        if (element.isJsonNull()) {
            connection.disconnect();
            reader.close();
            return null;
        }
        JsonArray array = (JsonArray) element;
        JsonObject object = (JsonObject) array.get(array.size() - 1);
        connection.disconnect();
        reader.close();
        return object.get("name").getAsString();
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

    public static UUID getUUIDFromMojang(String name) throws IOException {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        JsonElement element = JSON.parse(reader);
        if (element.isJsonNull()) {
            connection.disconnect();
            reader.close();
            return null;
        }
        JsonObject object = (JsonObject) element;
        connection.disconnect();
        reader.close();
        String uuid = object.get("id").getAsString();
        return UUID.fromString(uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
    }

}
