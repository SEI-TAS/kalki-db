package edu.cmu.sei.ttg.kalki.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.Before;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.sei.ttg.kalki.database.Postgres;
import edu.cmu.sei.ttg.kalki.models.*;

import edu.cmu.sei.ttg.kalki.database.AUsesDatabase;

public class AlertTypeTest extends AUsesDatabase {
    private static AlertType alertType;

    @Before
    public void resetDB() {
        Postgres.resetDatabase();
        insertData();
    }

    /*
        Alert Type Action Tests
     */

    @Test
    public void testFindAlertType() {
        AlertType at = Postgres.findAlertType(alertType.getId());
        assertEquals(alertType.toString(), at.toString());
    }

    @Test
    public void testFindAllAlertTypes() {
        List<AlertType> alertTypeList = new ArrayList<AlertType>(Postgres.findAllAlertTypes());

        assertEquals(24, alertTypeList.size());     //alertType plus the 23 added in Postgres.setupDatabase()
        assertEquals(alertType.toString(), alertTypeList.get(23).toString());
    }

    @Test
    public void testFindAlertTypesByDeviceType() {  //based on the setup database script
        List<AlertType> atList = new ArrayList<AlertType>(Postgres.findAlertTypesByDeviceType(2));

        assertEquals(atList.size(), 14);
    }

    @Test
    public void testUpdateAlertType() {
        assertEquals(alertType.toString(), Postgres.findAlertType(alertType.getId()).toString());

        alertType.setDescription("new description");
        Postgres.updateAlertType(alertType);

        assertEquals(alertType.toString(), Postgres.findAlertType(alertType.getId()).toString());
    }

    @Test
    public void testInsertOrUpdateAlertType() {
        assertEquals(24, Postgres.findAllAlertTypes().size());

        alertType.setDescription("new description");
        alertType.insertOrUpdate();

        assertEquals(24, Postgres.findAllAlertTypes().size());

        AlertType newAlertType = new AlertType("AlertType2", "test alert type 2", "IoT Monitor");
        int newId = newAlertType.insertOrUpdate();

        assertEquals(25, Postgres.findAllAlertTypes().size());
        assertEquals(newAlertType.toString(), Postgres.findAlertType(newId).toString());
    }

    private static void insertData() {
        // insert alert_type unts-temperature
        alertType = new AlertType("UNTS-Temperature", "test alert type", "IoT Monitor");
        alertType.insert();
    }
}
