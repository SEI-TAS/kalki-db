package edu.cmu.sei.ttg.kalki.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.sei.ttg.kalki.database.Postgres;
import edu.cmu.sei.ttg.kalki.models.*;

import edu.cmu.sei.ttg.kalki.database.AUsesDatabase;

public class SecurityStateTest extends AUsesDatabase {
    private static SecurityState securityState;

    /*
        Security State Action Tests
     */

    @Test
    public void testFindSecurityState() {
        assertEquals(securityState.toString(), Postgres.findSecurityState(securityState.getId()).toString());
    }

    @Test
    public void testFindAllSecurityStates() {
        ArrayList<SecurityState> foundSecurityStates = new ArrayList<SecurityState>(Postgres.findAllSecurityStates());
        assertEquals(1, foundSecurityStates.size());
    }

    @Test
    public void testInsertOrUpdateSecurityState() {
        ArrayList<SecurityState> foundStates = new ArrayList<SecurityState>(Postgres.findAllSecurityStates());
        assertEquals(1, foundStates.size());

        securityState.setName("changed security state");
        securityState.insertOrUpdate();

        foundStates = new ArrayList<SecurityState>(Postgres.findAllSecurityStates());
        assertEquals(1, foundStates.size());
        assertEquals(securityState.toString(), Postgres.findSecurityState(securityState.getId()).toString());

        SecurityState newSecurityState = new SecurityState("new security state");
        int newId = newSecurityState.insertOrUpdate();

        foundStates = new ArrayList<SecurityState>(Postgres.findAllSecurityStates());
        assertEquals(2, foundStates.size());
        assertEquals(newSecurityState.toString(), Postgres.findSecurityState(newSecurityState.getId()).toString());
    }

    @Test
    public void testDeleteSecurityState() {
        assertEquals(securityState.toString(), Postgres.findSecurityState(securityState.getId()).toString());

        Postgres.deleteSecurityState(securityState.getId());

        assertEquals(null, Postgres.findSecurityState(securityState.getId()));
    }

    public void insertData() {
        // insert security state(s)
        securityState = new SecurityState("Normal");
        securityState.insert();
    }
}
