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
        assertEquals(4, foundSecurityStates.size());    //including normal, suspicious, and attack
    }

    @Test
    public void testInsertOrUpdateSecurityState() {
        ArrayList<SecurityState> foundStates = new ArrayList<SecurityState>(Postgres.findAllSecurityStates());
        assertEquals(4, foundStates.size());    //including normal, suspicious, and attack

        securityState.setName("changed security state");
        securityState.insertOrUpdate();

        foundStates = new ArrayList<SecurityState>(Postgres.findAllSecurityStates());
        assertEquals(4, foundStates.size());    //including normal, suspicious, and attack
        assertEquals(securityState.toString(), Postgres.findSecurityState(securityState.getId()).toString());

        SecurityState newSecurityState = new SecurityState("new security state");
        int newId = newSecurityState.insertOrUpdate();

        foundStates = new ArrayList<SecurityState>(Postgres.findAllSecurityStates());
        assertEquals(5, foundStates.size());    //including normal, suspicious, and attack
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
