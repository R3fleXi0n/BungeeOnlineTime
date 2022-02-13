package lu.r3flexi0n.bungeeonlinetime.database;

public class MySQLDatabase extends Database {

    public MySQLDatabase(String host, int port, String database, String username, String password) {
        super("MySQL", "com.mysql.jdbc.Driver", "jdbc:mysql://(host=" + host + ",port=" + port + ",port=" + database + ",user=" + username + ",password=" + password + ")");
    }

}
