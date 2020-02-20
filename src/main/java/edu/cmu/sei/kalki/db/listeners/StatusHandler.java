package edu.cmu.sei.kalki.db.listeners;

import edu.cmu.sei.kalki.db.database.Postgres;

import java.util.logging.Logger;

public class StatusHandler implements InsertHandler {
    private static Logger logger = Logger.getLogger("myLogger");

    public StatusHandler() {
    }

    @Override
    public void handleNewInsertion(int newItemId) {
        logger.info("Detected new status inserted with id " + newItemId);
        Postgres.newStatusId(newItemId);
    }
}