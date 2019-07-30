package edu.cmu.sei.ttg.kalki.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
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
    private static AlertTypeLookup alertTypeLookup;
    private static AlertTypeLookup alertTypeLookupTwo;
    private static AlertTypeLookup alertTypeLookupThree;
    /*
        Alert Condition Action Tests
     */

    @Test
    public void testInsertAlertCondition() {
        alertCondition = new AlertCondition(device.getId(), alertTypeLookup.getId(), alertTypeLookup.getVariables());
        alertCondition.insertOrUpdate();
        assertNotEquals(-1, alertCondition.getId());
    }

    // Should match alertConditon from testInsertAlertCondition()
    @Test
    public void testFindAlertCondition() {
        AlertCondition ac = Postgres.findAlertCondition(alertCondition.getId());
        assertEquals(alertCondition.toString(), ac.toString());
    }

//    @Test
//    public void testFindAlertConditionByAlertType() {
//        AlertCondition ac = Postgres.findAlertConditionByAlertType(alertType.getId());
//        assertEquals(alertConditionTwo.toString(), ac.toString());
//    }

    // Should only be one AlertCondition from testInsertAlertCondition()
    @Test
    public void testFindAllAlertConditions() {
        ArrayList<AlertCondition> acList = new ArrayList<AlertCondition>(Postgres.findAllAlertConditions());
        assertEquals(1, acList.size());
        assertEquals(alertCondition.toString(), acList.get(0).toString());
    }

    @Test
    public void testInsertAlertConditionForDevice(){
        Postgres.insertAlertConditionForDevice(device.getId()); //should insert 1
        List<AlertCondition> acList = Postgres.findAllAlertConditions();
        assertEquals(2, acList.size()); // 2 devices, 1 alert condition per
    }

    @Test
    public void testUpdateAlertConditionsForDeviceType() {
        alertTypeLookup.getVariables().replace("test", "testing");
        alertTypeLookup.insertOrUpdate();

        int result = Postgres.updateAlertConditionsForDeviceType(alertTypeLookup);
        assertEquals(1, result); // returns 1 on success

        List<AlertCondition> allAlertConditions = Postgres.findAllAlertConditions();
        assertEquals(4, allAlertConditions.size()); // should be doubled from previous test

    }

    @Test
    public void testFindAlertConditionsByDevice() {
        List<AlertCondition> acList = Postgres.findAlertConditionsByDevice(device.getId());

        assertEquals(1, acList.size()); // should only return newest row
        assertNotEquals(alertCondition.toString(), acList.get(0).toString()); // shouldn't be the same alertCondition
    }

    public void insertData() {
        // insert device_type
        deviceType = new DeviceType(0, "test device type");
        deviceType.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1);
        device.insert();

        deviceTwo = new Device("Device 2", "this is also a test device", deviceType, "0.0.0.1", 1, 1);
        deviceTwo.insert();

        deviceThree = new Device("Device 3", "this is too a test device", deviceType, "0.0.0.2", 1, 1);
        deviceThree.insert();

        // insert alert_type unts-temperature
        alertType = new AlertType("UNTS-Temperature", "test alert type", "IoT Monitor");
        alertType.insert();

        // insert into alert_type_lookup
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put("test", "test");
        alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        // insert alert_condition

//
//        alertConditionTwo = new AlertCondition(null, deviceTwo.getId(), alertType.getId());
//        alertConditionTwo.insert();
    }
}
