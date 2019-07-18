package edu.cmu.sei.ttg.kalki.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


import edu.cmu.sei.ttg.kalki.database.Postgres;
import edu.cmu.sei.ttg.kalki.models.*;

import edu.cmu.sei.ttg.kalki.database.AUsesDatabase;

public class AlertConditionTest extends AUsesDatabase {
    private static AlertCondition alertCondition;
    private static AlertCondition alertConditionTwo;
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static Group group;
    private static Device device;
    private static Device deviceTwo;
    private static Device deviceThree;
    private static AlertType alertType;

    /*
        Alert Condition Action Tests
     */
    @Test
    public void testFindAlertCondition() {
        AlertCondition ac = Postgres.findAlertCondition(alertCondition.getId());
        assertEquals(alertCondition.toString(), ac.toString());
    }

    @Test
    public void testFindAlertConditionByAlertType() {
        AlertCondition ac = Postgres.findAlertConditionByAlertType(alertType.getId());
        assertEquals(alertConditionTwo.toString(), ac.toString());
    }

    @Test
    public void testFindAlertConditionsByDevice() {
        List<AlertCondition> acList = new ArrayList<AlertCondition>(Postgres.findAlertConditionsByDevice(device.getId()));

        assertEquals(1, acList.size());
        assertEquals(alertCondition.toString(), acList.get(0).toString());

        List<AlertCondition> acList2 = new ArrayList<AlertCondition>(Postgres.findAlertConditionsByDevice(deviceTwo.getId()));
        assertEquals(1, acList.size());
        assertEquals(alertConditionTwo.toString(), acList2.get(0).toString());
    }

    @Test
    public void testFindAllAlertConditions() {
        ArrayList<AlertCondition> acList = new ArrayList<AlertCondition>(Postgres.findAllAlertConditions());
        assertEquals(alertCondition.toString(), acList.get(0).toString());
        assertEquals(alertConditionTwo.toString(), acList.get(1).toString());
    }

    @Test
    public void testInsertAlertCondition() {
        assertEquals(2, Postgres.findAllAlertConditions().size());

        alertCondition.getVariables().put("testKey1", "testValue1");
        alertCondition.insert();

        assertEquals(3, Postgres.findAllAlertConditions().size());

        alertCondition.setDeviceTypeId(deviceTypeTwo.getId());
        alertCondition.insert();    //since the deviceType is set, it should insert two alertConditions

        assertEquals(5, Postgres.findAllAlertConditions().size());
    }

    public void insertData() {
        // insert device_type
        deviceType = new DeviceType(0, "test device type");
        deviceType.insert();

        deviceTypeTwo = new DeviceType(0, "test device type two");
        deviceTypeTwo.insert();

        // insert Group
        group = new Group("Test Group");
        group.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1);
        device.insert();

        deviceTwo = new Device("Device 2", "this is also a test device", deviceTypeTwo.getId(), group.getId(), "0.0.0.1", 1, 1);
        deviceTwo.insert();

        deviceThree = new Device("Device 3", "this is too a test device", deviceTypeTwo.getId(), group.getId(), "0.0.0.1", 1, 1);
        deviceThree.insert();

        // insert alert_type unts-temperature
        alertType = new AlertType("UNTS-Temperature", "test alert type", "IoT Monitor");
        alertType.insert();

        // insert alert_condition
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put("test", "test");
        alertCondition = new AlertCondition(hmap, device.getId(), alertType.getId());
        alertCondition.insert();

        alertConditionTwo = new AlertCondition(null, deviceTwo.getId(), alertType.getId());
        alertConditionTwo.insert();
    }
}