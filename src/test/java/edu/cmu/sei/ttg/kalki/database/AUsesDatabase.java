package edu.cmu.sei.ttg.kalki.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import edu.cmu.sei.ttg.kalki.database.Postgres;

public abstract class AUsesDatabase {
    private static boolean hasRun = false;

    @BeforeAll
    public static void initializeDB() {
        if (!hasRun) {
            String rootPassword = "kalkipass";  //based on run script
            String dbHost = "localhost";        //based on run script
            String dbPort = "5432";             //based on run script
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
                Postgres.initialize(dbHost, dbPort, dbName, dbUser, dbPass);
            } catch (Exception e) {
                System.out.println(e);
            }
            hasRun = true;
        }
    }

    @BeforeEach
    public void resetDB() {
        Postgres.setupTestDatabase();
        insertData();
    }

    public abstract void insertData();
}
