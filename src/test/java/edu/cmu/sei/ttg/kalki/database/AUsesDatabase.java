package edu.cmu.sei.ttg.kalki.database;

import org.junit.Test;
import org.junit.BeforeClass;

import edu.cmu.sei.ttg.kalki.database.Postgres;

public abstract class AUsesDatabase {
    private static boolean hasRun = false;

    @BeforeClass
    public static void initializeDB() {
        if(!hasRun) {
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
}