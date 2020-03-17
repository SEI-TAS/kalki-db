package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import edu.cmu.sei.kalki.db.daos.AlertConditionDAO;
import edu.cmu.sei.kalki.db.models.AlertCondition;
import edu.cmu.sei.kalki.db.models.AlertType;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class AlertConditionTest extends AUsesDatabase {
    private static DeviceType deviceType;
    private static Device device;
    private static AlertType alertType;
    private static AlertTypeLookup alertTypeLookup;
    /*
        Alert Condition Action Tests
     */

    @Test
    public void testInsertAlertCondition() {
        AlertCondition alertCondition = new AlertCondition(device.getId(), alertTypeLookup.getId(), alertTypeLookup.getVariables());
        alertCondition.insertOrUpdate();
        assertNotEquals(-1, alertCondition.getId());
    }

    @Test
    public void testFindAlertCondition() {
        AlertCondition alertCondition = new AlertCondition(device.getId(), alertTypeLookup.getId(), alertTypeLookup.getVariables());
        alertCondition.insertOrUpdate();

        AlertCondition ac = AlertConditionDAO.findAlertCondition(alertCondition.getId());
        assertEquals(alertCondition.getAlertTypeLookupId(), ac.getAlertTypeLookupId());
        assertEquals(alertCondition.getDeviceId(), ac.getDeviceId());
        assertEquals(alertCondition.getVariables(), ac.getVariables());
    }

    @Test
    public void testFindAllAlertConditions() {
        AlertCondition alertCondition = new AlertCondition(device.getId(), alertTypeLookup.getId(), alertTypeLookup.getVariables());
        alertCondition.insertOrUpdate();
        alertCondition.insertOrUpdate();

        ArrayList<AlertCondition> acList = new ArrayList<AlertCondition>(AlertConditionDAO.findAllAlertConditions());
        assertEquals(2, acList.size());
        assertEquals(alertCondition.getAlertTypeLookupId(), acList.get(1).getAlertTypeLookupId());
        assertEquals(alertCondition.getDeviceId(), acList.get(1).getDeviceId());
        assertEquals(alertCondition.getVariables(), acList.get(1).getVariables());
    }

    @Test
    public void testInsertAlertConditionForDevice(){
        AlertConditionDAO.insertAlertConditionForDevice(device.getId()); //should insert 1
        List<AlertCondition> acList = AlertConditionDAO.findAllAlertConditions();
        assertEquals(1, acList.size());

        AlertConditionDAO.insertAlertConditionForDevice(device.getId()); //should insert 1
        acList = AlertConditionDAO.findAllAlertConditions();
        assertEquals(2, acList.size()); // 1 device, 1 alert type lookup, 2 inserts by device

    }

    @Test
    public void testFindAlertConditionsByDevice() {
        AlertConditionDAO.insertAlertConditionForDevice(device.getId()); //should insert 1
        AlertConditionDAO.insertAlertConditionForDevice(device.getId()); //should insert 1

        List<AlertCondition> acList = AlertConditionDAO.findAlertConditionsByDevice(device.getId());
        assertEquals(1, acList.size()); // should only return newest row
    }

    @Test
    public void testUpdateAlertConditionsForDeviceType() {
        AlertConditionDAO.insertAlertConditionForDevice(device.getId()); //should insert 1

        alertTypeLookup.getVariables().replace("test", "testing");
        alertTypeLookup.insertOrUpdate();

        int result = AlertConditionDAO.updateAlertConditionsForDeviceType(alertTypeLookup);
        assertEquals(1, result); // returns 1 on success

        List<AlertCondition> allAlertConditions = AlertConditionDAO.findAllAlertConditions();
        assertEquals(2, allAlertConditions.size());

        List<AlertCondition> deviceAlertConditions = AlertConditionDAO.findAlertConditionsByDevice(device.getId());
        assertEquals(alertTypeLookup.getVariables().get("test"), deviceAlertConditions.get(0).getVariables().get("test"));
    }

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
