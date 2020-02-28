package edu.cmu.sei.kalki.db.database;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.logging.Level;

public abstract class AUsesDatabase {
    // Used to create DB only once, not once per each test class.
    private static boolean hasRun = false;

    @BeforeAll
    public static void initializeDB() {
        //System.out.println("Checking if Test DB has already been setup: " + hasRun);
        if (!hasRun) {
            System.out.println("Initial Test DB setup.");
            String rootPassword = "kalkipass";
            String dbName = "kalkidb_test";
            String dbUser = "kalkiuser_test";
            String dbPass = "kalkipass";

            try {
                // Recreate DB and user.
                Postgres.setLoggingLevel(Level.SEVERE);
                Postgres.removeDatabase(rootPassword, dbName);
                Postgres.removeUser(rootPassword, dbUser);
                Postgres.createUserIfNotExists(rootPassword, dbUser, dbPass);
                Postgres.createDBIfNotExists(rootPassword, dbName, dbUser);

                //initialize test DB
                Postgres.initialize(dbName, dbUser, dbPass);
            } catch (Exception e) {
                System.out.println("Error with initial test DB setup: " + e.toString());
                e.printStackTrace();
            }
            hasRun = true;
        }
    }

    @BeforeEach
    public void resetDB() {
        //System.out.println("Resetting Test DB.");
        Postgres.resetDatabase();
        insertData();
    }

    public abstract void insertData();
}
