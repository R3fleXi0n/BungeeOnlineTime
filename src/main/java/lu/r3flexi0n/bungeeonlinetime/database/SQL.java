package lu.r3flexi0n.bungeeonlinetime.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.UUID;
import lu.r3flexi0n.bungeeonlinetime.OnlineTime;
import lu.r3flexi0n.bungeeonlinetime.OnlineTimeListener;

public abstract class SQL {

    private Connection connection;

    private final String TABLE_NAME = "onlineTimes";

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
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (uuid VARCHAR(36), joinTime BIGINT, leaveTime BIGINT, afkTime BIGINT);";
        execute(sql);
        addAFKColumn();
    }

    private void addAFKColumn() throws Exception {
        try {
            String edit = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN afkTime BIGINT DEFAULT 0;";
            execute(edit);
        } catch (Exception ex) {
        }
    }

    public void addOnlineTime(UUID uuid, long joinTime, long leaveTime, long afkTime) throws Exception {
        String sql = "INSERT INTO " + TABLE_NAME + " (uuid, joinTime, leaveTime, afkTime) VALUES ('" + uuid + "','" + joinTime + "','" + leaveTime + "','" + afkTime + "');";
        execute(sql);
    }

    public long getOnlineTime(UUID uuid, long since) throws Exception {

        String sql = "SELECT SUM(leaveTime - joinTime - afkTime) AS time FROM " + TABLE_NAME + " WHERE uuid = '" + uuid + "' AND joinTime > '" + since + "' GROUP BY uuid;";

        if (isClosed()) {
            openConnection();
        }

        int onlineTime = 0;
        OnlineTime time = OnlineTimeListener.ONLINE_TIMES.get(uuid);
        if (time != null) {
            onlineTime = (int) ((System.currentTimeMillis() - time.getJoinTime() - time.getAFK()) / 1000);
        }

        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        while (resultset.next()) {
            onlineTime += (int) (resultset.getLong("time") / 1000);
        }

        resultset.close();
        statement.close();

        return onlineTime;
    }

    public LinkedHashMap<UUID, Long> getTopOnlineTimes(int amount, long since) throws Exception {

        String sql = "SELECT uuid, SUM(leaveTime - joinTime - afkTime) AS time FROM " + TABLE_NAME + " WHERE joinTime > '" + since + "' GROUP BY uuid ORDER BY time DESC LIMIT " + amount + ";";

        if (isClosed()) {
            openConnection();
        }

        LinkedHashMap<UUID, Long> times = new LinkedHashMap<>();

        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        while (resultset.next()) {
            times.put(UUID.fromString(resultset.getString("uuid")), resultset.getLong("time") / 1000);
        }

        resultset.close();
        statement.close();

        return times;
    }

    public void resetAll(long before) throws Exception {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE joinTime < '" + before + "';";
        execute(sql);
    }

    public void reset(UUID uuid, long before) throws Exception {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE uuid = '" + uuid + "' AND joinTime < '" + before + "';";
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
