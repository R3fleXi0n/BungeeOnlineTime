package lu.r3flexi0n.bungeeonlinetime.database;

import java.io.File;
import java.sql.DriverManager;

public class SQLite extends SQL {

    private final File file;

    public SQLite(File file) {
        this.file = file;
    }

    @Override
    public void openConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getPath());
    }
}
