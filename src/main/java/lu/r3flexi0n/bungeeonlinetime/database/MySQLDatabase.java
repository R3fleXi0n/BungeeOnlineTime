package lu.r3flexi0n.bungeeonlinetime.database;

public class MySQLDatabase extends Database {

    public MySQLDatabase(String host, int port, String database, String username, String password) {
        super(
                "MySQL",
                new String[]{"com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver"},
                "jdbc:mysql://" + host + ":" + port + "/" + database
        );

        databaseProperties.put("user", username);
        databaseProperties.put("password", password);
        databaseProperties.put("autoReconnect", "true");
    }
}
