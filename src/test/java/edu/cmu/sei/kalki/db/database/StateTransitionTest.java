package edu.cmu.sei.kalki.db.database;

import edu.cmu.sei.kalki.db.daos.StateTransitionDAO;
import edu.cmu.sei.kalki.db.models.StateTransition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateTransitionTest extends AUsesDatabase
{
    private static final int BASE_STATE_TRANSITIONS = 12;
    private static StateTransition stateTransition;

    @Test
    public void testFind() {
        assertEquals(stateTransition.toString(), StateTransitionDAO.findStateTransition(stateTransition.getId()).toString());
    }

    @Test
    public void testFindGivenStateIds() {
        StateTransition transition = StateTransitionDAO.findByStateIds(1, 2);
        assertEquals(1, transition.getStartStateId());
        assertEquals(2, transition.getFinishStateId());
    }

    @Test
    public void testFindGivenStateNames() {
        StateTransition transition = StateTransitionDAO.findByStateNames("Normal", "Suspicious");
        assertEquals(1, transition.getStartStateId());
        assertEquals(2, transition.getFinishStateId());
    }
    @Test
    public void testFindAll() {
        assertEquals(BASE_STATE_TRANSITIONS, StateTransitionDAO.findAll().size());
    }

    @Test
    public void testInsert() {
        assertEquals(BASE_STATE_TRANSITIONS, StateTransitionDAO.findAll().size());

        stateTransition.setStartStateId(2);
        stateTransition.setFinishStateId(3);
        stateTransition.update();

        assertEquals(BASE_STATE_TRANSITIONS, StateTransitionDAO.findAll().size());
        assertEquals(stateTransition.toString(), StateTransitionDAO.findStateTransition(stateTransition.getId()).toString());

        StateTransition newStateTransition = new StateTransition(3,1);
        newStateTransition.insert();
        assertEquals(BASE_STATE_TRANSITIONS + 1, StateTransitionDAO.findAll().size());
        assertEquals(newStateTransition.toString(), StateTransitionDAO.findStateTransition(newStateTransition.getId()).toString());
    }

    @Test
    public void testDelete() {
        assertEquals(stateTransition.toString(), StateTransitionDAO.findStateTransition(stateTransition.getId()).toString());
        StateTransitionDAO.deleteStateTransition(stateTransition.getId());
        assertEquals(null, StateTransitionDAO.findStateTransition(stateTransition.getId()));
    }

    public void insertData() {
        // insert Group
        stateTransition = StateTransitionDAO.findByStateIds(1, 2);
        //stateTransition.insert();
    }
}
