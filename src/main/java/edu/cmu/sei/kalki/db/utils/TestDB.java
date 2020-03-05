package edu.cmu.sei.kalki.db.utils;

import edu.cmu.sei.kalki.db.database.Postgres;

import java.sql.SQLException;
import java.util.logging.Logger;

/***
 * Simple class for setting up a test DB.
 */
public class TestDB
{
    private static final String DB_HOST = "127.0.0.1";
    private static final String PORT = "5433";
    private static final String DB_TEMPLATE_NAME = "kalkidb_test";
    private static final String DB_NAME = "kalkidb_test_instance";
    private static final String DB_USER = "kalkiuser_test";
    private static final String DB_PASS = "kalkipass_test";

    private static Logger logger = Logger.getLogger(TestDB.class.getName());

    /**
     * Recreates the test DB from the template.
     */
    public static void recreateTestDB() throws SQLException {
        logger.info("Test DB setup.");

        // Connect first to template db, and recreate DB instance from it.
        Postgres.initialize(DB_HOST, PORT, DB_TEMPLATE_NAME, DB_USER, DB_PASS);
        Postgres.recreateDB(DB_NAME, DB_TEMPLATE_NAME);
        Postgres.cleanup();
    }

    /**
     * Sets up all parameters to connect to test DB.
     */
    public static void initialize() {
        // Reconfigure to connect to newly reset DB.
        Postgres.initialize(DB_HOST, PORT, DB_NAME, DB_USER, DB_PASS);
    }

    /**
     * Cleans up singleton setup.
     */
    public static void cleanup() {
        Postgres.cleanup();
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
