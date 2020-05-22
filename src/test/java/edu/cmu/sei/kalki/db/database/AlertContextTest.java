package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import edu.cmu.sei.kalki.db.daos.AlertContextDAO;
import edu.cmu.sei.kalki.db.models.AlertContext;
import edu.cmu.sei.kalki.db.models.AlertType;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import edu.cmu.sei.kalki.db.models.DataNode;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class AlertContextTest extends AUsesDatabase {
    private static DeviceType deviceType;
    private static Device device;
    private static AlertType alertType;
    private static AlertTypeLookup alertTypeLookup;
    /*
        Alert Condition Action Tests
     */

    @Test
    public void testInsertAlertContext() {
        AlertContext alertCondition = new AlertContext(device.getId(), alertTypeLookup.getId());
        alertCondition.insert();
        assertNotEquals(-1, alertCondition.getId());
    }

    @Test
    public void testFindAlertContext() {
        AlertContext alertContext = new AlertContext(device.getId(), alertTypeLookup.getId());
        alertContext.insert();

        AlertContext ac = AlertContextDAO.findAlertContext(alertContext.getId());
        assertEquals(alertContext.getAlertTypeLookupId(), ac.getAlertTypeLookupId());
        assertEquals(alertContext.getDeviceId(), ac.getDeviceId());
    }

    @Test
    public void testFindAllAlertContexts() {
        AlertContext alertContext = new AlertContext(device.getId(), alertTypeLookup.getId());
        alertContext.insert();
        alertContext.insert();

        ArrayList<AlertContext> acList = new ArrayList<AlertContext>(AlertContextDAO.findAllAlertContexts());
        assertEquals(2, acList.size());
        assertEquals(alertContext.getAlertTypeLookupId(), acList.get(1).getAlertTypeLookupId());
        assertEquals(alertContext.getDeviceId(), acList.get(1).getDeviceId());
    }

    @Test
    public void testInsertAlertContextForDevice(){
        AlertContextDAO.insertAlertContextForDevice(device.getId()); //should insert 1
        List<AlertContext> acList = AlertContextDAO.findAllAlertContexts();
        assertEquals(1, acList.size());

        AlertContextDAO.insertAlertContextForDevice(device.getId()); //should insert 1
        acList = AlertContextDAO.findAllAlertContexts();
        assertEquals(2, acList.size()); // 1 device, 1 alert type lookup, 2 inserts by device

    }

    @Test
    public void testFindAlertContextsByDevice() {
        AlertContextDAO.insertAlertContextForDevice(device.getId()); //should insert 1
        AlertContextDAO.insertAlertContextForDevice(device.getId()); //should insert 1

        List<AlertContext> acList = AlertContextDAO.findAlertContextsByDevice(device.getId());
        assertEquals(1, acList.size()); // should only return newest row
    }

    @Test
    public void testUpdateAlertContextsForDeviceType() {
//        AlertContextDAO.insertAlertContextForDevice(device.getId()); //should insert 1
//
//        alertTypeLookup.getVariables().replace("test", "testing");
//        alertTypeLookup.insertOrUpdate();
//
//        int result = AlertContextDAO.updateAlertContextsForDeviceType(alertTypeLookup);
//        assertEquals(1, result); // returns 1 on success
//
//        List<AlertContext> allAlertContexts = AlertContextDAO.findAllAlertContexts();
//        assertEquals(2, allAlertContexts.size());
//
//        List<AlertContext> deviceAlertContexts = AlertContextDAO.findAlertContextsByDevice(device.getId());
//        assertEquals(alertTypeLookup.getVariables().get("test"), deviceAlertContexts.get(0).getVariables().get("test"));
    }

    public void insertData() {
        // insert device_type
        deviceType = new DeviceType(0, "test device type");
        deviceType.insert();

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1, dataNode);
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
