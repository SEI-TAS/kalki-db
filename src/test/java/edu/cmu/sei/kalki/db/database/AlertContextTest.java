package edu.cmu.sei.kalki.db.database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.cmu.sei.kalki.db.daos.AlertContextDAO;
import edu.cmu.sei.kalki.db.models.AlertContext;
import edu.cmu.sei.kalki.db.models.AlertType;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import edu.cmu.sei.kalki.db.models.DataNode;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceType;

import java.util.ArrayList;
import java.util.List;

public class AlertContextTest extends AUsesDatabase {
    private static DeviceType deviceType;
    private static Device device;
    private static AlertTypeLookup alertTypeLookup;

    /*
        Alert Condition Action Tests
     */
    @Test
    public void testInsertAlertContext() {
        AlertContext alertContext = new AlertContext(alertTypeLookup.getId(), AlertContext.LogicalOperator.NONE);
        alertContext.insert();
        Assertions.assertNotEquals(-1, alertContext.getId());
        Assertions.assertNotEquals(0, alertContext.getId());

    }

    @Test
    public void testFindAlertContext() {
        AlertContext ac = new AlertContext(alertTypeLookup.getId(), AlertContext.LogicalOperator.NONE);
        ac.insert();
        AlertContext testAc = AlertContextDAO.findAlertContext(ac.getId());
        Assertions.assertEquals(ac.getId(), testAc.getId());
        Assertions.assertEquals(ac.getAlertTypeLookupId(), testAc.getAlertTypeLookupId());
        Assertions.assertEquals(ac.getLogicalOperator(), testAc.getLogicalOperator());
    }

    @Test
    public void testFindAllAlertContexts() {
        AlertContext alertContext = new AlertContext(alertTypeLookup.getId(), AlertContext.LogicalOperator.NONE);
        alertContext.insert();
        alertContext.insert();

        ArrayList<AlertContext> acList = new ArrayList<AlertContext>(AlertContextDAO.findAllAlertContexts());
        Assertions.assertEquals(2, acList.size());
        Assertions.assertEquals(alertContext.getAlertTypeLookupId(), acList.get(1).getAlertTypeLookupId());
        Assertions.assertEquals(alertContext.getDeviceTypeId(), acList.get(1).getDeviceTypeId());
    }

    @Test
    public void testFindAlertContextsForDeviceType(){
        List<AlertContext> contextList = AlertContextDAO.findAlertContextsForDeviceType(deviceType.getId());

        Assertions.assertEquals(1, contextList.size());
    }

    @Test
    public void testInsertAlertContextForDevice(){
        AlertContextDAO.insertAlertContextForDevice(device.getId()); //should insert 1
        List<AlertContext> acList = AlertContextDAO.findAllAlertContexts();
        Assertions.assertEquals(1, acList.size());

        AlertContextDAO.insertAlertContextForDevice(device.getId()); //should insert 1
        acList = AlertContextDAO.findAllAlertContexts();
        Assertions.assertEquals(2, acList.size()); // 1 device, 1 alert type lookup, 2 inserts by device

    }

    @Test
    public void testFindAlertContextsByDevice() {
        AlertContextDAO.insertAlertContextForDevice(device.getId()); //should insert 1

        List<AlertContext> acList = AlertContextDAO.findAlertContextsByDevice(device.getId());
        Assertions.assertEquals(1, acList.size()); // should only return newest row
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
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1, dataNode, "");
        device.insert();

        // insert alert_type unts-temperature
        AlertType alertType = new AlertType("UNTS-Temperature", "test alert type", "IoT Monitor");
        alertType.insert();

        // insert into alert_type_lookup
        alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId());
        alertTypeLookup.insert();
    }
}
