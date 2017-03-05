package lu.r3flexi0n.bungeeonlinetime.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Consumer;
import lu.r3flexi0n.bungeeonlinetime.BungeeOnlineTime;
import net.md_5.bungee.api.ProxyServer;

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

    public void openConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?useSSL=false", this.username, this.password);
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isClosed() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(0)) {
            return true;
        } else {
            return false;
        }
    }

    public void updateAsync(final String sql, final Consumer<Boolean> consumer) {
        ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, new Runnable() {
            public void run() {
                boolean executed = updateSync(sql);
                consumer.accept(executed);
            }
        });
    }

    public void batchAsync(final List<String> sql, final Consumer<Boolean> consumer) {
        ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, new Runnable() {
            public void run() {
                boolean executed = batchSync(sql);
                consumer.accept(executed);
            }
        });
    }

    public void queryAsync(final String sql, final Consumer<ResultSet> consumer) {
        ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, new Runnable() {
            public void run() {
                ResultSet result = querySync(sql);
                consumer.accept(result);
            }
        });
    }

    public boolean updateSync(String sql) {
        try {
            if (isClosed()) {
                openConnection();
            }
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean batchSync(List<String> sql) {
        try {
            if (isClosed()) {
                openConnection();
            }
            Statement statement = connection.createStatement();
            for (String updates : sql) {
                statement.addBatch(updates);
            }
            statement.executeBatch();
            statement.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ResultSet querySync(String sql) {
        try {
            if (isClosed()) {
                openConnection();
            }
            PreparedStatement statement = connection.prepareStatement(sql);
            return statement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
