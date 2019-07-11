package edu.cmu.sei.ttg.kalki.listeners;

import edu.cmu.sei.ttg.kalki.database.Postgres;

public class StateHandler implements InsertHandler {
    public StateHandler() {
    }

    @Override
    public void handleNewInsertion(int newItemId) {
        Postgres.newStateId(newItemId);
    }
}