package edu.cmu.sei.kalki.db.utils;

import edu.cmu.sei.kalki.db.database.Postgres;

/***
 * Simple class for setting up a test DB.
 */
public class TestDB
{
    /**
     * Sets up a test DB, initializes it, and inserts test data.
     * @param testFile
     */
    public static void setupTestDBFromConfig(String testFile)
    {
        TestDB.overwriteDBConfig();
        Postgres.initializeFromConfig();
        TestDB.insertTestData(testFile);
    }

    /**
     * Overwrite default DB params to create a new, temp test DB.
     */
    public static void overwriteDBConfig()
    {
        Config.setValue("db_recreate", "true");
        Config.setValue("db_name", "kalkidb_test");
        Config.setValue("db_user", "kalkiuser_test");
    }

    /***
     * Inserts data from the given file to prepare to run simple tests.
     */
    public static void insertTestData(String fileName)
    {
        if(fileName == null || fileName.isEmpty())
        {
            System.out.println("No test data file provided.");
            return;
        }

        System.out.println("Inserting test data.");
        Postgres.executeSQLFile(fileName);
        System.out.println("Test data finished inserting.");
    }

}
