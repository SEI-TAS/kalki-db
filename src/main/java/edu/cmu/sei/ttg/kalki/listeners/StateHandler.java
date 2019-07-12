package edu.cmu.sei.ttg.kalki.listeners;

import edu.cmu.sei.ttg.kalki.database.Postgres;

import java.util.logging.Logger;

public class StateHandler implements InsertHandler {
    private static Logger logger = Logger.getLogger("myLogger");

    public StateHandler() {
    }

    @Override
    public void handleNewInsertion(int newItemId) {
        logger.info("Detected new state inserted with id " + newItemId);
        Postgres.newStateId(newItemId);
    }
}