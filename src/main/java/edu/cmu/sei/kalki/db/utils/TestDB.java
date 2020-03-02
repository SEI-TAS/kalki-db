package edu.cmu.sei.kalki.db.utils;

import edu.cmu.sei.kalki.db.database.Postgres;

import java.util.logging.Logger;

/***
 * Simple class for setting up a test DB.
 */
public class TestDB
{
    private static Logger logger = Logger.getLogger(TestDB.class.getName());

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
            logger.warning("No test data file provided.");
            return;
        }

        logger.info("Inserting test data.");
        Postgres.executeSQLFile(fileName);
        logger.info("Test data finished inserting.");
    }

}
