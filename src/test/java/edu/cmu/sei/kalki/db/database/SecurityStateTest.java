package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.cmu.sei.kalki.db.models.SecurityState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import edu.cmu.sei.kalki.db.models.*;

public class SecurityStateTest extends AUsesDatabase {
    private static SecurityState securityState;

    /*
        Security State Action Tests
     */

    @Test
    public void testFindSecurityState() {
        Assertions.assertEquals(securityState.toString(), Postgres.findSecurityState(securityState.getId()).toString());
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
        Assertions.assertEquals(securityState.toString(), Postgres.findSecurityState(securityState.getId()).toString());

        SecurityState newSecurityState = new SecurityState("new security state");
        int newId = newSecurityState.insertOrUpdate();

        foundStates = new ArrayList<SecurityState>(Postgres.findAllSecurityStates());
        assertEquals(5, foundStates.size());    //including normal, suspicious, and attack
        Assertions.assertEquals(newSecurityState.toString(), Postgres.findSecurityState(newSecurityState.getId()).toString());
    }

    @Test
    public void testDeleteSecurityState() {
        Assertions.assertEquals(securityState.toString(), Postgres.findSecurityState(securityState.getId()).toString());

        Postgres.deleteSecurityState(securityState.getId());

        Assertions.assertEquals(null, Postgres.findSecurityState(securityState.getId()));
    }

    public void insertData() {
        // insert security state(s)
        securityState = new SecurityState("Normal");
        securityState.insert();
    }
}
