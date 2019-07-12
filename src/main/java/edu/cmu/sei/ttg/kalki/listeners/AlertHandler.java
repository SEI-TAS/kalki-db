package edu.cmu.sei.ttg.kalki.listeners;

import edu.cmu.sei.ttg.kalki.database.Postgres;

import java.util.logging.Logger;

public class AlertHandler implements InsertHandler {
    private static Logger logger = Logger.getLogger("myLogger");

    public AlertHandler() {
    }

    @Override
    public void handleNewInsertion(int newItemId) {
        logger.info("Detected new alert inserted with id " + newItemId);
        Postgres.newAlertId(newItemId);
    }
}