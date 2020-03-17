package edu.cmu.sei.kalki.db.notifications;

import edu.cmu.sei.kalki.db.listeners.InsertHandler;

import java.util.logging.Logger;

public class StateHandler implements InsertHandler
{
    private static Logger logger = Logger.getLogger("myLogger");

    public StateHandler() {
    }

    @Override
    public void handleNewInsertion(int newItemId) {
        logger.info("Detected new state inserted with id " + newItemId);
        AsyncNotificationStorage.newStateId(newItemId);
    }
}