package edu.cmu.sei.kalki.db.database;

import edu.cmu.sei.kalki.db.daos.StateTransitionDAO;
import edu.cmu.sei.kalki.db.models.Group;
import edu.cmu.sei.kalki.db.models.StateTransition;
import org.junit.jupiter.api.Test;

import javax.swing.plaf.nimbus.State;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateTransitionTest extends AUsesDatabase
{
    private static StateTransition stateTransition;

    @Test
    public void testFind() {
        assertEquals(stateTransition.toString(), StateTransitionDAO.findStateTransition(stateTransition.getId()).toString());
    }

    @Test
    public void testFindAll() {
        assertEquals(1, StateTransitionDAO.findAll().size());
    }

    @Test
    public void testInsert() {
        assertEquals(1, StateTransitionDAO.findAll().size());

        stateTransition.setStartStateId(2);
        stateTransition.setFinishStateId(3);
        stateTransition.update();

        assertEquals(1, StateTransitionDAO.findAll().size());
        assertEquals(stateTransition.toString(), StateTransitionDAO.findStateTransition(stateTransition.getId()).toString());

        StateTransition newStateTransition = new StateTransition(3,1);
        newStateTransition.insert();
        assertEquals(2, StateTransitionDAO.findAll().size());
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
        stateTransition = new StateTransition(1, 2);
        stateTransition.insert();
    }
}
