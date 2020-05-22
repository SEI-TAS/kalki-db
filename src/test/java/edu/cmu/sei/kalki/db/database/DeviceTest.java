package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.cmu.sei.kalki.db.daos.AlertContextDAO;
import edu.cmu.sei.kalki.db.daos.AlertDAO;
import edu.cmu.sei.kalki.db.daos.AlertTypeLookupDAO;
import edu.cmu.sei.kalki.db.daos.DeviceDAO;
import edu.cmu.sei.kalki.db.models.Alert;
import edu.cmu.sei.kalki.db.models.AlertType;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import edu.cmu.sei.kalki.db.models.DataNode;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceStatus;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.Group;
import edu.cmu.sei.kalki.db.models.SecurityState;
import edu.cmu.sei.kalki.db.models.UmboxImage;
import edu.cmu.sei.kalki.db.models.UmboxInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class DeviceTest extends AUsesDatabase {
    private static SecurityState normalState;
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static Group group;
    private static Device device;
    private static Device deviceTwo;
    private static AlertType alertType;
    private static AlertType alertTypeReset;
    private static DeviceStatus deviceStatus;
    private static Alert alertIoT;

     /*
        Test Device Actions
     */

    @Test
    public void testInsertDevice() {
        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        Device test = new Device("test", "test desc", deviceType, "1.1.1.1", 1000, 1000, dataNode);
        test.insert();

        assertNotNull(test.getCurrentState());
        assertNotEquals(0, AlertTypeLookupDAO.findAlertTypeLookupsByDeviceType(test.getType().getId()));
        assertNotEquals(0, AlertContextDAO.findAlertContextsByDevice(test.getId()).size());
    }
    @Test
    public void testFindDevice() {
        Assertions.assertEquals(device.getDescription(), DeviceDAO.findDevice(device.getId()).getDescription());
        Assertions.assertEquals(deviceTwo.getDescription(), DeviceDAO.findDevice(deviceTwo.getId()).getDescription());
    }

    @Test
    public void testFindAllDevices() {
        assertEquals(2, DeviceDAO.findAllDevices().size());
    }

    @Test
    public void testFindDevicesByGroup() {
        ArrayList<Device> foundDevices = new ArrayList<Device>(DeviceDAO.findDevicesByGroup(group.getId()));

        assertEquals(1, foundDevices.size());
        assertEquals(deviceTwo.getDescription(), foundDevices.get(0).getDescription());
    }

    @Test
    public void testFindDeviceByAlert() {
        Device foundDevice = DeviceDAO.findDeviceByAlert(alertIoT);
        assertEquals(device.toString(), foundDevice.toString());
    }

    @Test
    public void testFindDevicesByType() {
        ArrayList<Device> foundDevices = new ArrayList<Device>(DeviceDAO.findDevicesByType(deviceTypeTwo.getId()));

        assertEquals(1, foundDevices.size());
        assertEquals(deviceTwo.getDescription(), foundDevices.get(0).getDescription());
    }

    @Test
    public void testInsertOrUpdateDevice() {
        assertEquals(2, DeviceDAO.findAllDevices().size());

        device.setDescription("new description");
        device.insertOrUpdate();

        assertEquals(2, DeviceDAO.findAllDevices().size());

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        Device newDevice = new Device("Device 3", "this is a newly added device", deviceType, "0.0.0.0", 2, 2, dataNode);
        int newId = newDevice.insertOrUpdate();

        assertEquals(3, DeviceDAO.findAllDevices().size());
        Assertions.assertEquals(newDevice.getDescription(), DeviceDAO.findDevice(newId).getDescription());
    }

    @Test
    public void testDeleteDevice() {
        assertNotNull(DeviceDAO.findDevice(deviceTwo.getId()));

        DeviceDAO.deleteDevice(deviceTwo.getId());

        Assertions.assertNull(DeviceDAO.findDevice(deviceTwo.getId()));
    }

    @Test
    public void testResetSecurityState() {
        List<Alert> foundAlerts = AlertDAO.findAlertsByDevice(deviceTwo.getId());
        int initialAlertsSize = foundAlerts.size();

        deviceTwo.resetSecurityState();
        foundAlerts = AlertDAO.findAlertsByDevice(deviceTwo.getId());
        assertEquals(initialAlertsSize + 1, foundAlerts.size());
    }

    public void insertData() {
        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        //insert normal securityState
        normalState = new SecurityState("Normal");
        normalState.insert();

        // insert device_type
        deviceType = new DeviceType(0, "Udoo Neo");
        deviceType.insert();

        deviceTypeTwo = new DeviceType(0, "test device type");
        deviceTypeTwo.insert();

        // insert Group
        group = new Group("Test Group");
        group.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1, dataNode);
        device.setTagIds(new ArrayList<Integer>());
        device.insert();

        deviceTwo = new Device("Device 2", "this is also a test device", deviceTypeTwo.getId(), group.getId(), "0.0.0.1", 1, 1, 1, dataNode.getId());
        deviceTwo.insert();

        // insert alert_type unts-temperature
        alertType = new AlertType("UNTS-Temperature", "test alert type", "IoT Monitor");
        alertType.insert();

        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put("test", "test");

        // insert device_status
        deviceStatus = new DeviceStatus(device.getId(), hmap);
        deviceStatus.insert();

        // insert alert for device_status/alert_type
        alertIoT = new Alert(alertType.getName(), deviceStatus.getId(), alertType.getId(), "");
        alertIoT.insert();

        //insert state reset alert type
        alertTypeReset = new AlertType("state-reset", "state reset", "Dashboard");
        alertTypeReset.insert();

        AlertTypeLookup atl = new AlertTypeLookup(alertType.getId(), deviceType.getId(), null);
        atl.insert();
    }
}
