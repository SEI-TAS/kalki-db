package edu.cmu.sei.kalki.db.database;

import edu.cmu.sei.kalki.db.utils.TestDB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.SQLException;
import java.util.logging.Level;

public abstract class AUsesDatabase {
    @BeforeEach
    public void resetDB() {
        //System.out.println("Resetting Test DB.");
        try {
            Postgres.setLoggingLevel(Level.SEVERE);

            // Drops and recreates DB from template.
            TestDB.recreateTestDB();
            TestDB.initialize();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        insertData();
    }

    @AfterEach
    public void closeConnections() {
        // Ensure singleton is closed so that DB can be recreated later.
        Postgres.cleanup();
    }

    public abstract void insertData();
}
