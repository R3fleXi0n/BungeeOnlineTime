package lu.r3flexi0n.bungeeonlinetime.database;

import java.io.File;

public class SQLiteDatabase extends Database {

    public SQLiteDatabase(File file) {
        super(
                "SQLite",
                new String[]{"org.sqlite.JDBC"},
                "jdbc:sqlite:" + file.getPath()
        );
    }

}
