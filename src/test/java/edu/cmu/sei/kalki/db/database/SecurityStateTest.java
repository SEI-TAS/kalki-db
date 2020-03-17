package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.cmu.sei.kalki.db.daos.SecurityStateDAO;
import edu.cmu.sei.kalki.db.models.SecurityState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class SecurityStateTest extends AUsesDatabase {
    private static SecurityState securityState;

    /*
        Security State Action Tests
     */

    @Test
    public void testFindSecurityState() {
        Assertions.assertEquals(securityState.toString(), SecurityStateDAO.findSecurityState(securityState.getId()).toString());
    }

    @Test
    public void testFindAllSecurityStates() {
        ArrayList<SecurityState> foundSecurityStates = new ArrayList<SecurityState>(SecurityStateDAO.findAllSecurityStates());
        assertEquals(4, foundSecurityStates.size());    //including normal, suspicious, and attack
    }

    @Test
    public void testInsertOrUpdateSecurityState() {
        ArrayList<SecurityState> foundStates = new ArrayList<SecurityState>(SecurityStateDAO.findAllSecurityStates());
        assertEquals(4, foundStates.size());    //including normal, suspicious, and attack

        securityState.setName("changed security state");
        securityState.insertOrUpdate();

        foundStates = new ArrayList<SecurityState>(SecurityStateDAO.findAllSecurityStates());
        assertEquals(4, foundStates.size());    //including normal, suspicious, and attack
        Assertions.assertEquals(securityState.toString(), SecurityStateDAO.findSecurityState(securityState.getId()).toString());

        SecurityState newSecurityState = new SecurityState("new security state");
        int newId = newSecurityState.insertOrUpdate();

        foundStates = new ArrayList<SecurityState>(SecurityStateDAO.findAllSecurityStates());
        assertEquals(5, foundStates.size());    //including normal, suspicious, and attack
        Assertions.assertEquals(newSecurityState.toString(), SecurityStateDAO.findSecurityState(newSecurityState.getId()).toString());
    }

    @Test
    public void testDeleteSecurityState() {
        Assertions.assertEquals(securityState.toString(), SecurityStateDAO.findSecurityState(securityState.getId()).toString());

        SecurityStateDAO.deleteSecurityState(securityState.getId());

        Assertions.assertEquals(null, SecurityStateDAO.findSecurityState(securityState.getId()));
    }

    public void insertData() {
        // insert security state(s)
        securityState = new SecurityState("Normal");
        securityState.insert();
    }
}
