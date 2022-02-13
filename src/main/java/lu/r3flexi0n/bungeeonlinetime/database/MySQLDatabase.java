package lu.r3flexi0n.bungeeonlinetime.database;

public class MySQLDatabase extends Database {

    public MySQLDatabase(String host, int port, String database, String username, String password) {
        super(
                "MySQL",
                "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://(host=" + host + ",port=" + port + ",user=" + username + ",password=" + password + ")/" + database
        );
    }

}
