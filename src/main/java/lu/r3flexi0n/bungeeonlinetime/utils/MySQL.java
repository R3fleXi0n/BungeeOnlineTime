package lu.r3flexi0n.bungeeonlinetime.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

public class MySQL {

    private String host, database, username, password;
    private int port;

    private Connection connection;

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
        String sql = "CREATE TABLE IF NOT EXISTS bungeeonlinetime (uuid VARCHAR(36) UNIQUE, onlinetime INT);";

        if (isClosed()) {
            openConnection();
        }

        Statement statement = getConnection().createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

    public int getOnlineTime(UUID uuid) throws SQLException, ClassNotFoundException {

        int onlineTime = 0;

        String sql = "SELECT onlinetime FROM bungeeonlinetime WHERE uuid = '" + uuid + "';";

        if (isClosed()) {
            openConnection();
        }

        Statement statement = getConnection().createStatement();

        ResultSet resultset = statement.executeQuery(sql);
        if (resultset.next()) {
            onlineTime = resultset.getInt("onlinetime");
        }

        resultset.close();
        statement.close();

        return onlineTime;
    }

    public ArrayList<String> getTopOnlineTimes() throws SQLException, ClassNotFoundException {

        ArrayList<String> top = new ArrayList<String>();

        String sql = "SELECT * FROM bungeeonlinetime ORDER BY onlinetime DESC LIMIT 10;";

        if (isClosed()) {
            openConnection();
        }

        Statement statement = getConnection().createStatement();

        ResultSet resultset = statement.executeQuery(sql);
        while (resultset.next()) {
            top.add(resultset.getString("uuid") + "," + resultset.getString("onlinetime"));
        }

        resultset.close();
        statement.close();

        return top;
    }

    public void resetOnlineTimes() throws SQLException, ClassNotFoundException {
        String sql = "UPDATE bungeeonlinetime SET onlinetime = '0'";

        if (isClosed()) {
            openConnection();
        }

        Statement statement = getConnection().createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

    public void resetOnlineTime(UUID uuid) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE bungeeonlinetime SET onlinetime = '0' WHERE uuid = '" + uuid + "';";

        if (isClosed()) {
            openConnection();
        }

        Statement statement = getConnection().createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

    public void addOnlineTime(ArrayList<UUID> uuidList) throws SQLException, ClassNotFoundException {

        if (isClosed()) {
            openConnection();
        }

        Statement statement = connection.createStatement();
        for (UUID uuids : uuidList) {
            statement.addBatch("INSERT INTO bungeeonlinetime (uuid, onlinetime) VALUES ('" + uuids + "','1') ON DUPLICATE KEY UPDATE onlinetime = onlinetime + 1;");
        }
        statement.executeBatch();
        statement.close();
    }

}
