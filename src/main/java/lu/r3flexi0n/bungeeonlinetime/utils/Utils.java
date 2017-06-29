package lu.r3flexi0n.bungeeonlinetime.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Utils {

    public static String getDate() {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        return format.format(date);
    }

    private static JsonParser jsonParser = new JsonParser();

    public static String getName(UUID uuid) throws Exception {
        URL url = new URL("https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        JsonArray array = (JsonArray) jsonParser.parse(reader);
        JsonObject object = (JsonObject) array.get(array.size() - 1);
        connection.disconnect();
        reader.close();
        return object.get("name").getAsString();
    }

    public static UUID getUUID(String name) throws Exception {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        JsonObject object = (JsonObject) jsonParser.parse(reader);
        connection.disconnect();
        reader.close();
        return convertUUID(object.get("id").getAsString());
    }

    public static UUID convertUUID(String uuid) {
        return UUID.fromString(uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
    }
}
