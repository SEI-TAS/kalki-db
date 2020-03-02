package edu.cmu.sei.kalki.db.database;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.sql.SQLException;
import java.util.logging.Level;

public abstract class AUsesDatabase {
    // Used to create DB only once, not once per each test class.
    private static boolean hasRun = false;

    @BeforeAll
    public static void initializeDB() {
        //System.out.println("Checking if Test DB has already been setup: " + hasRun);
        if (!hasRun) {
            System.out.println("Initial Test DB setup.");
            String testPort = "5433";
            String dbName = "kalkidb_test";
            String dbUser = "kalkiuser_test";
            String dbPass = "kalkipass_test";

            try {
                //initialize test DB
                Postgres.initialize("127.0.0.1", testPort, dbName, dbUser, dbPass);
                Postgres.setLoggingLevel(Level.SEVERE);

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
        try {
            Postgres.resetDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        insertData();
    }

    public abstract void insertData();
}
