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

public class AlertTypeTest extends AUsesDatabase {
    private static AlertType alertType;
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;

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

        assertEquals(1, alertTypeList.size());
        assertEquals(alertType.toString(), alertTypeList.get(0).toString());
    }

    @Test
    public void testFindAlertTypesByDeviceType() {
        List<AlertType> atList =
                new ArrayList<AlertType>(Postgres.findAlertTypesByDeviceType(deviceType.getId()));

        assertEquals(1, atList.size());
        assertEquals(alertType.toString(), atList.get(0).toString());

        atList = new ArrayList<AlertType>(Postgres.findAlertTypesByDeviceType(deviceTypeTwo.getId()));
        assertEquals(0, atList.size());
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
        assertEquals(1, Postgres.findAllAlertTypes().size());

        alertType.setDescription("new description");
        alertType.insertOrUpdate();

        assertEquals(1, Postgres.findAllAlertTypes().size());

        AlertType newAlertType = new AlertType("AlertType2", "test alert type 2", "IoT Monitor");
        int newId = newAlertType.insertOrUpdate();

        assertEquals(2, Postgres.findAllAlertTypes().size());
        assertEquals(newAlertType.toString(), Postgres.findAlertType(newId).toString());
    }

    public void insertData() {
        // insert alert_type unts-temperature
        alertType = new AlertType("UNTS-Temperature", "test alert type", "IoT Monitor");
        alertType.insert();

        //insert device type
        deviceType = new DeviceType(-1, "testDeviceType");
        deviceType.insert();

        deviceTypeTwo = new DeviceType(-1, "testDeviceType2");
        deviceTypeTwo.insert();

        Postgres.executeCommand("INSERT INTO alert_type_lookup(alert_type_id, device_type_id) values " +
                "("+alertType.getId()+ ", " +deviceType.getId()+");");
    }
}
