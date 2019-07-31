package edu.cmu.sei.ttg.kalki.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


import edu.cmu.sei.ttg.kalki.database.Postgres;
import edu.cmu.sei.ttg.kalki.models.*;

import edu.cmu.sei.ttg.kalki.database.AUsesDatabase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AlertConditionTest extends AUsesDatabase {
    private static DeviceType deviceType;
    private static Device device;
    private static AlertType alertType;
    private static AlertTypeLookup alertTypeLookup;
    /*
        Alert Condition Action Tests
     */

    @Test
    public void testAInsertAlertCondition() {
        AlertCondition alertCondition = new AlertCondition(device.getId(), alertTypeLookup.getId(), alertTypeLookup.getVariables());
        alertCondition.insertOrUpdate();
        assertNotEquals(-1, alertCondition.getId());
    }

    // Should match alertConditon from testInsertAlertCondition()
    @Test
    public void testBFindAlertCondition() {
        AlertCondition alertCondition = new AlertCondition(device.getId(), alertTypeLookup.getId(), alertTypeLookup.getVariables());
        alertCondition.insertOrUpdate();

        AlertCondition ac = Postgres.findAlertCondition(alertCondition.getId());
        assertEquals(alertCondition.getAlertTypeLookupId(), ac.getAlertTypeLookupId());
        assertEquals(alertCondition.getDeviceId(), ac.getDeviceId());
        assertEquals(alertCondition.getVariables(), ac.getVariables());
    }

    // Should only be one AlertCondition from testInsertAlertCondition()
    @Test
    public void testCFindAllAlertConditions() {
        AlertCondition alertCondition = new AlertCondition(device.getId(), alertTypeLookup.getId(), alertTypeLookup.getVariables());
        alertCondition.insertOrUpdate();
        alertCondition.insertOrUpdate();

        ArrayList<AlertCondition> acList = new ArrayList<AlertCondition>(Postgres.findAllAlertConditions());
        assertEquals(2, acList.size());
        assertEquals(alertCondition.getAlertTypeLookupId(), acList.get(1).getAlertTypeLookupId());
        assertEquals(alertCondition.getDeviceId(), acList.get(1).getDeviceId());
        assertEquals(alertCondition.getVariables(), acList.get(1).getVariables());
    }

    @Test
    public void testDInsertAlertConditionForDevice(){
        Postgres.insertAlertConditionForDevice(device.getId()); //should insert 1
        List<AlertCondition> acList = Postgres.findAllAlertConditions();
        assertEquals(1, acList.size());

        Postgres.insertAlertConditionForDevice(device.getId()); //should insert 1
        acList = Postgres.findAllAlertConditions();
        assertEquals(2, acList.size()); // 1 device, 1 alert type lookup, 2 inserts by device

    }

    @Test
    public void testEFindAlertConditionsByDevice() {
        Postgres.insertAlertConditionForDevice(device.getId()); //should insert 1
        Postgres.insertAlertConditionForDevice(device.getId()); //should insert 1

        List<AlertCondition> acList = Postgres.findAlertConditionsByDevice(device.getId());
        assertEquals(1, acList.size()); // should only return newest row
    }

    @Test
    public void testFUpdateAlertConditionsForDeviceType() {
        Postgres.insertAlertConditionForDevice(device.getId()); //should insert 1

        alertTypeLookup.getVariables().replace("test", "testing");
        alertTypeLookup.insertOrUpdate();

        int result = Postgres.updateAlertConditionsForDeviceType(alertTypeLookup);
        assertEquals(1, result); // returns 1 on success

        List<AlertCondition> allAlertConditions = Postgres.findAllAlertConditions();
        assertEquals(2, allAlertConditions.size());

        List<AlertCondition> deviceAlertConditions = Postgres.findAlertConditionsByDevice(device.getId());
        assertEquals(alertTypeLookup.getVariables().get("test"), deviceAlertConditions.get(0).getVariables().get("test"));
    }



//    @Before
    public void insertData() {
        // insert device_type
        deviceType = new DeviceType(0, "test device type");
        deviceType.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1);
        device.insert();

        // insert alert_type unts-temperature
        alertType = new AlertType("UNTS-Temperature", "test alert type", "IoT Monitor");
        alertType.insert();

        // insert into alert_type_lookup
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put("test", "test");
        alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();
    }
}
