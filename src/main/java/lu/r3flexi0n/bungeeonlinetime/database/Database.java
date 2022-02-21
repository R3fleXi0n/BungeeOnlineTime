package lu.r3flexi0n.bungeeonlinetime.database;

import lu.r3flexi0n.bungeeonlinetime.objects.OnlineTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public abstract class Database {

    public final String databaseName;

    public final String[] databaseClass;

    public final String databaseUrl;

    public Properties databaseProperties = new Properties();

    public Database(String databaseName, String[] databaseClass, String databaseUrl) {
        this.databaseName = databaseName;
        this.databaseClass = databaseClass;
        this.databaseUrl = databaseUrl;
    }

    private boolean isSupported() {
        for (String className : databaseClass) {
            try {
                Class.forName(className);
                return true;
            } catch (ClassNotFoundException ex) {
            }
        }
        return false;
    }

    private Connection connection;

    public void openConnection() throws SQLException {
        if (connection == null) {
            boolean supported = isSupported(); //todo
            connection = DriverManager.getConnection(databaseUrl, databaseProperties);
        }
    }

    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS BungeeOnlineTime (uuid VARCHAR(36) UNIQUE, name VARCHAR(16), time BIGINT);";
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

    public void createIndex() throws SQLException {
        if (this instanceof MySQLDatabase) {

            String sql = "SHOW INDEX FROM BungeeOnlineTime WHERE Key_Name = 'BungeeOnlineTimeIndex';";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            int count = 0;
            while (resultSet.next()) {
                count++;
            }

            statement.close();

            if (count == 0) { //CREATE INDEX IF NOT EXISTS is not available in mysql
                sql = "CREATE INDEX BungeeOnlineTimeIndex ON BungeeOnlineTime (name, time);";
                statement = connection.createStatement();
                statement.executeUpdate(sql);
                statement.close();
            }

        } else {

            String sql = "CREATE INDEX IF NOT EXISTS BungeeOnlineTimeIndex ON BungeeOnlineTime (name, time);";
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();

        }
    }

    public void updateOnlineTime(String uuid, String name, long time) throws SQLException {
        String sql;
        if (this instanceof MySQLDatabase) {
            sql = "INSERT IGNORE INTO BungeeOnlineTime VALUES (?, ?, ?);";
        } else {
            sql = "INSERT OR IGNORE INTO BungeeOnlineTime VALUES (?, ?, ?);";
        }
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, uuid);
        statement.setString(2, name);
        statement.setLong(3, 0L);
        statement.executeUpdate();
        statement.close();

        sql = "UPDATE BungeeOnlineTime SET name = ?, time = time + ? WHERE uuid = ?;";
        statement = connection.prepareStatement(sql);
        statement.setString(1, name);
        statement.setLong(2, time);
        statement.setString(3, uuid);
        statement.executeUpdate();
        statement.close();
    }

    public List<OnlineTime> getOnlineTime(String uuidOrName) throws SQLException {
        String sql = uuidOrName.length() == 36
                ? "SELECT * FROM BungeeOnlineTime WHERE uuid = ?;"
                : "SELECT * FROM BungeeOnlineTime WHERE name = ?;";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, uuidOrName);
        ResultSet resultSet = statement.executeQuery();
        List<OnlineTime> result = getOnlineTimesFromResultSet(resultSet);
        statement.close();
        return result;
    }

    public List<OnlineTime> getTopOnlineTimes(int page, int perPage) throws SQLException {
        String sql = "SELECT * FROM BungeeOnlineTime ORDER BY time DESC LIMIT ? OFFSET ?;";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, perPage);
        statement.setInt(2, (page - 1) * perPage);
        ResultSet resultSet = statement.executeQuery();
        List<OnlineTime> result = getOnlineTimesFromResultSet(resultSet);
        statement.close();
        return result;
    }

    public void resetOnlineTime(String name) throws SQLException {
        String sql = "DELETE FROM BungeeOnlineTime WHERE name = ?;";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, name);
        statement.executeUpdate();
        statement.close();
    }

    public void resetAllOnlineTimes() throws SQLException {
        String sql = "DELETE FROM BungeeOnlineTime;";
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

    private List<OnlineTime> getOnlineTimesFromResultSet(ResultSet resultSet) throws SQLException {
        List<OnlineTime> result = new ArrayList<>();
        while (resultSet.next()) {
            UUID uuid = UUID.fromString(resultSet.getString("uuid"));
            String name = resultSet.getString("name");
            long time = resultSet.getLong("time");
            result.add(new OnlineTime(uuid, name, time));
        }
        return result;
    }

    /*

    public void generateFakeDatabase() throws SQLException {
        connection.setAutoCommit(false);

        Random random = new Random();
        for (int i = 0; i < 5000; i++) {
            UUID uuid = UUID.randomUUID();
            String name = getRandomString(8);
            long time = random.nextInt(360000000);
            updateOnlineTime(uuid.toString(), name, time);
        }

        connection.commit();
        connection.setAutoCommit(true);
    }

    private String getRandomString(int length) {
        Random random = new Random();
        char[] characters = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        char[] randomString = new char[length];
        for (int i = 0; i < length; i++) {
            randomString[i] = characters[random.nextInt(characters.length)];
        }
        return String.valueOf(randomString);
    }

    */
}
