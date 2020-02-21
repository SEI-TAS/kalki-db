package edu.cmu.sei.kalki.db.database;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class AUsesDatabase {
    // Used to create DB only once, not once per each test class.
    private static boolean hasRun = false;

    @BeforeAll
    public static void initializeDB() {
        if (!hasRun) {
            String rootPassword = "kalkipass";  //based on run script
            String dbName = "kalkidb_test";
            String dbUser = "kalkiuser_test";
            String dbPass = "kalkipass";

            try {
                // Recreate DB and user.
                Postgres.removeDatabase(rootPassword, dbName);
                Postgres.removeUser(rootPassword, dbUser);
                Postgres.createUserIfNotExists(rootPassword, dbUser, dbPass);
                Postgres.createDBIfNotExists(rootPassword, dbName, dbUser);

                //initialize test DB
                Postgres.initialize(dbName, dbUser, dbPass);
            } catch (Exception e) {
                System.out.println(e);
            }
            hasRun = true;
        }
    }

    @BeforeEach
    public void resetDB() {
        Postgres.dropTables();
        Postgres.setupTables();
        insertData();
    }

    public abstract void insertData();
}
