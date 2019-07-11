package edu.cmu.sei.ttg.kalki.listeners;

import edu.cmu.sei.ttg.kalki.database.Postgres;

public class AlertHandler implements InsertHandler {
    public AlertHandler() {
    }

    @Override
    public void handleNewInsertion(int newItemId) {
        Postgres.newAlertId(newItemId);
    }
}