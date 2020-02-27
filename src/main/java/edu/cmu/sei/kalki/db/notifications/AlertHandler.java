package edu.cmu.sei.kalki.db.notifications;

import edu.cmu.sei.kalki.db.listeners.InsertHandler;

import java.util.logging.Logger;

public class AlertHandler implements InsertHandler
{
    private static Logger logger = Logger.getLogger("myLogger");

    public AlertHandler() {
    }

    @Override
    public void handleNewInsertion(int newItemId) {
        logger.info("Detected new alert inserted with id " + newItemId);
        AsyncNotificationStorage.newAlertId(newItemId);
    }
}