package lu.r3flexi0n.bungeeonlinetime.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lu.r3flexi0n.bungeeonlinetime.BungeeOnlineTime;
import lu.r3flexi0n.bungeeonlinetime.OnlinePlayer;
import lu.r3flexi0n.bungeeonlinetime.OnlineTime;

public abstract class SQL {

    private final String TABLE_NAME = "BungeeOnlineTime";

    private Connection connection;

    public abstract void openConnection() throws Exception;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isClosed() throws SQLException {
        return connection == null || connection.isClosed() || !connection.isValid(0);
    }

    public void createTable() throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (uuid VARCHAR(36) UNIQUE, name VARCHAR(16), time BIGINT);";
        execute(sql);
    }

    public void addOnlineTimeSQLite(UUID uuid, String name) throws Exception { //ON DUPLICATE KEY NOT SUPPORTED IN SQLITE -.-
        String sql = "INSERT OR IGNORE INTO " + TABLE_NAME + " VALUES ('" + uuid + "','" + name + "','0')";
        execute(sql);
    }

    public void updateOnlineTime(UUID uuid, String name, long time) throws Exception {
        String sql = BungeeOnlineTime.MYSQL_ENABLED
                ? "INSERT INTO " + TABLE_NAME + " (uuid, name, time) VALUES ('" + uuid + "','" + name + "','" + time + "') ON DUPLICATE KEY UPDATE name = '" + name + "', time = time + " + time + ";"
                : "UPDATE " + TABLE_NAME + " SET name = '" + name + "', time = time + " + time + " WHERE uuid = '" + uuid + "';";
        execute(sql);
    }

    public OnlineTime getOnlineTime(UUID uuid) throws Exception {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE uuid = '" + uuid + "';";
        return get(sql);
    }

    public OnlineTime getOnlineTime(String name) throws Exception {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE name = '" + name + "';";
        return get(sql);
    }

    private OnlineTime get(String sql) throws Exception {
        if (isClosed()) {
            openConnection();
        }

        OnlineTime onlineTime = null;

        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        if (resultset.next()) {

            UUID uuid = UUID.fromString(resultset.getString("uuid"));
            String name = resultset.getString("name");
            long time = resultset.getLong("time");

            OnlinePlayer onlinePlayer = BungeeOnlineTime.ONLINE_PLAYERS.get(uuid);
            if (onlinePlayer != null) {
                time += onlinePlayer.getNoAFKTime();
            }

            onlineTime = new OnlineTime(name, time);
        }

        resultset.close();
        statement.close();

        return onlineTime;
    }

    public List<OnlineTime> getTopOnlineTimes(int amount) throws Exception {

        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY time DESC LIMIT " + amount + ";";

        if (isClosed()) {
            openConnection();
        }

        List<OnlineTime> onlineTimes = new ArrayList<>();

        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        while (resultset.next()) {

            UUID uuid = UUID.fromString(resultset.getString("uuid"));
            String name = resultset.getString("name");
            long time = resultset.getLong("time");

            OnlinePlayer onlinePlayer = BungeeOnlineTime.ONLINE_PLAYERS.get(uuid);
            if (onlinePlayer != null) {
                time += onlinePlayer.getNoAFKTime();
            }

            onlineTimes.add(new OnlineTime(name, time));
        }

        resultset.close();
        statement.close();

        return onlineTimes;
    }

    public void reset(String name) throws Exception {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE name = '" + name + "';";
        execute(sql);
    }

    public void resetAll() throws Exception {
        String sql = "DELETE FROM " + TABLE_NAME + ";";
        execute(sql);
    }

    private void execute(String sql) throws Exception {
        if (isClosed()) {
            openConnection();
        }

        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

}
