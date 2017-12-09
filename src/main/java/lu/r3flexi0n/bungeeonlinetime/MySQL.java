package lu.r3flexi0n.bungeeonlinetime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.UUID;

public class MySQL {

    private final String host, database, username, password;
    private final int port;

    private Connection connection;

    private final String TABLE_NAME = "onlineTimes";

    public MySQL(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public void openConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isClosed() throws SQLException {
        return connection == null || connection.isClosed() || !connection.isValid(0);
    }

    public void createTable() throws SQLException, ClassNotFoundException {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (uuid VARCHAR(36), joinTime BIGINT, leaveTime BIGINT);";
        execute(sql);
    }

    public void addOnlineTime(UUID uuid, long joinTime, long leaveTime) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO " + TABLE_NAME + " (uuid, joinTime, leaveTime) VALUES ('" + uuid + "','" + joinTime + "','" + leaveTime + "');";
        execute(sql);
    }

    public long getOnlineTime(UUID uuid, long since) throws SQLException, ClassNotFoundException {

        String sql = "SELECT SUM(leaveTime - joinTime) AS time FROM " + TABLE_NAME + " WHERE uuid = '" + uuid + "' AND joinTime > '" + since + "' GROUP BY uuid;";

        if (isClosed()) {
            openConnection();
        }

        int onlineTime = 0;

        Statement statement = getConnection().createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        while (resultset.next()) {
            onlineTime = (int) (resultset.getLong("time") / 1000);
        }

        resultset.close();
        statement.close();

        return onlineTime;
    }

    public LinkedHashMap<UUID, Long> getTopOnlineTimes(int amount, long since) throws SQLException, ClassNotFoundException {

        String sql = "SELECT uuid, SUM(leaveTime - joinTime) AS time FROM " + TABLE_NAME + " WHERE joinTime > '" + since + "' GROUP BY uuid ORDER BY time DESC LIMIT " + amount + ";";

        if (isClosed()) {
            openConnection();
        }

        LinkedHashMap<UUID, Long> times = new LinkedHashMap<>();

        Statement statement = getConnection().createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        while (resultset.next()) {
            times.put(UUID.fromString(resultset.getString("uuid")), resultset.getLong("time") / 1000);
        }

        resultset.close();
        statement.close();

        return times;
    }

    public void resetAll(long before) throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE joinTime < '" + before + "';";
        execute(sql);
    }

    public void reset(UUID uuid, long before) throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE uuid = '" + uuid + "' AND joinTime < '" + before + "';";
        execute(sql);
    }

    private void execute(String sql) throws SQLException, ClassNotFoundException {
        if (isClosed()) {
            openConnection();
        }

        Statement statement = getConnection().createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }
}
