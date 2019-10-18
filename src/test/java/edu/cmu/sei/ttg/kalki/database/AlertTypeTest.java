package edu.cmu.sei.ttg.kalki.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

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
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;

    /*
        Alert Type Action Tests
     */

    @Test
    public void testInsertAlertType() {
        AlertType at = new AlertType("Name", "Description", "source");
        at.insert();
        assertNotEquals(-1, at.getId());
    }

    @Test
    public void testFindAlertType() {
        AlertType alertType = new AlertType("Name", "Description", "source");
        alertType.insert();

        AlertType test = Postgres.findAlertType(alertType.getId());
        assertNotNull(test);
        assertEquals(alertType.toString(), test.toString());
    }

    @Test
    public void testFindAllAlertTypes() {
        AlertType alertType = new AlertType("Name", "Description", "source");
        alertType.insert();
        List<AlertType> alertTypeList = new ArrayList<AlertType>(Postgres.findAllAlertTypes());

        assertEquals(1, alertTypeList.size());
        assertEquals(alertType.toString(), alertTypeList.get(0).toString());
    }

    @Test
    public void testUpdateAlertType() {
        AlertType alertType = new AlertType("Name", "Description", "source");
        alertType.insert();

        alertType.setDescription("new description");
        Postgres.updateAlertType(alertType);

        assertEquals(alertType.toString(), Postgres.findAlertType(alertType.getId()).toString());
    }

    @Test
    public void testInsertOrUpdateAlertType() {
        AlertType alertType = new AlertType("Name", "Description", "source");
        alertType.insert();

        assertNotEquals(-1, alertType.getId());
        assertEquals(1, Postgres.findAllAlertTypes().size());

        alertType.setDescription("new description");
        alertType.insertOrUpdate();

        assertEquals(1, Postgres.findAllAlertTypes().size());
        assertEquals(alertType.toString(), Postgres.findAlertType(alertType.getId()).toString());

        AlertType newAlertType = new AlertType("AlertType2", "test alert type 2", "IoT Monitor");
        newAlertType.insertOrUpdate();
        assertNotEquals(-1, newAlertType.getId());
        assertEquals(2, Postgres.findAllAlertTypes().size());
        assertEquals(newAlertType.toString(), Postgres.findAlertType(newAlertType.getId()).toString());
    }

    public void insertData() {
        //insert device type
        deviceType = new DeviceType(-1, "testDeviceType");
        deviceType.insert();
    }
}
