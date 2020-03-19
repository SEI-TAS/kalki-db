package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.cmu.sei.kalki.db.daos.DeviceStatusDAO;
import edu.cmu.sei.kalki.db.models.DataNode;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceStatus;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.Group;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class DeviceStatusTest extends AUsesDatabase {
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static Group group;
    private static Device device;
    private static Device deviceTwo;
    private static DeviceStatus deviceStatus;

    /*
      Test Device Status Actions
   */
    @Test
    public void testFindDeviceStatus() {
        assertEquals(deviceStatus.toString(), DeviceStatusDAO.findDeviceStatus(deviceStatus.getId()).toString());
    }

    @Test
    public void testFindDeviceStatuses() {
        ArrayList<DeviceStatus> foundStatuses =
                new ArrayList<DeviceStatus>(DeviceStatusDAO.findDeviceStatuses(device.getId()));

        assertEquals(1, foundStatuses.size());
        assertEquals(deviceStatus.toString(), foundStatuses.get(0).toString());

        foundStatuses = new ArrayList<DeviceStatus>(DeviceStatusDAO.findDeviceStatuses(deviceTwo.getId()));

        assertEquals(0, foundStatuses.size());
    }

    @Test
    public void testFindNDeviceStatuses() {
        ArrayList<DeviceStatus> foundStatuses =
                new ArrayList<DeviceStatus>(DeviceStatusDAO.findNDeviceStatuses(device.getId(), 1));
        assertEquals(1, foundStatuses.size());

        foundStatuses = new ArrayList<DeviceStatus>(DeviceStatusDAO.findNDeviceStatuses(deviceTwo.getId(), 0));
        assertEquals(0, foundStatuses.size());
    }

    @Test
    public void testFindAllDeviceStatuses() {
        ArrayList<DeviceStatus> foundStatuses = new ArrayList<DeviceStatus>(DeviceStatusDAO.findAllDeviceStatuses());
        assertEquals(1, foundStatuses.size());
        assertEquals(deviceStatus.toString(), foundStatuses.get(0).toString());
    }

    @Test
    public void testInsertOrUpdateDeviceStatus() {
        assertEquals(1, DeviceStatusDAO.findAllDeviceStatuses().size());

        deviceStatus.setDeviceId(deviceTwo.getId());
        deviceStatus.insertOrUpdate();

        assertEquals(1, DeviceStatusDAO.findAllDeviceStatuses().size());

        DeviceStatus newDeviceStatus = new DeviceStatus(device.getId());
        int newId = newDeviceStatus.insertOrUpdate();

        assertEquals(2, DeviceStatusDAO.findAllDeviceStatuses().size());
        assertEquals(newDeviceStatus.toString(), DeviceStatusDAO.findDeviceStatus(newId).toString());
    }

    @Test
    public void testDeleteDeviceStatus() {
        assertEquals(1, DeviceStatusDAO.findAllDeviceStatuses().size());

        DeviceStatusDAO.deleteDeviceStatus(deviceStatus.getId());

        assertEquals(0, DeviceStatusDAO.findAllDeviceStatuses().size());
        assertEquals(null, DeviceStatusDAO.findDeviceStatus(deviceStatus.getId()));
    }

    public void insertData() {
        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

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
        device.insert();

        deviceTwo = new Device("Device 2", "this is also a test device", deviceTypeTwo.getId(), group.getId(), "0.0.0.1", 1, 1, 1, dataNode.getId());
        deviceTwo.insert();

        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put("test", "test");

        // insert device_status
        deviceStatus = new DeviceStatus(device.getId(), hmap);
        deviceStatus.insert();
    }
}
