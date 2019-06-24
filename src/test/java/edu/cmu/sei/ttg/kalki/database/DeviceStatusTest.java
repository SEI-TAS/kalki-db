package edu.cmu.sei.ttg.kalki.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.Before;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.sei.ttg.kalki.database.Postgres;
import edu.cmu.sei.ttg.kalki.models.*;

import edu.cmu.sei.ttg.kalki.database.AUsesDatabase;

public class DeviceStatusTest extends AUsesDatabase {
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static Group group;
    private static Device device;
    private static Device deviceTwo;
    private static DeviceStatus deviceStatus;

    @Before
    public void resetDB() {
        Postgres.resetDatabase();
        insertData();
    }

    /*
      Test Device Status Actions
   */
    @Test
    public void testFindDeviceStatus() {
        assertEquals(deviceStatus.toString(), Postgres.findDeviceStatus(deviceStatus.getId()).toString());
    }

    @Test
    public void testFindDeviceStatuses() {
        ArrayList<DeviceStatus> foundStatuses =
                new ArrayList<DeviceStatus>(Postgres.findDeviceStatuses(device.getId()));

        assertEquals(1, foundStatuses.size());
        assertEquals(deviceStatus.toString(), foundStatuses.get(0).toString());

        foundStatuses = new ArrayList<DeviceStatus>(Postgres.findDeviceStatuses(deviceTwo.getId()));

        assertEquals(0, foundStatuses.size());
    }

    @Test
    public void testFindNDeviceStatuses() {
        ArrayList<DeviceStatus> foundStatuses =
                new ArrayList<DeviceStatus>(Postgres.findNDeviceStatuses(device.getId(), 1));
        assertEquals(1, foundStatuses.size());

        foundStatuses = new ArrayList<DeviceStatus>(Postgres.findNDeviceStatuses(deviceTwo.getId(), 0));
        assertEquals(0, foundStatuses.size());
    }

    @Test
    public void testFindAllDeviceStatuses() {
        ArrayList<DeviceStatus> foundStatuses = new ArrayList<DeviceStatus>(Postgres.findAllDeviceStatuses());
        assertEquals(1, foundStatuses.size());
        assertEquals(deviceStatus.toString(), foundStatuses.get(0).toString());
    }

    @Test
    public void testInsertOrUpdateDeviceStatus() {
        assertEquals(1, Postgres.findAllDeviceStatuses().size());

        deviceStatus.setDeviceId(deviceTwo.getId());
        deviceStatus.insertOrUpdate();

        assertEquals(1, Postgres.findAllDeviceStatuses().size());

        DeviceStatus newDeviceStatus = new DeviceStatus(device.getId());
        int newId = newDeviceStatus.insertOrUpdate();

        assertEquals(2, Postgres.findAllDeviceStatuses().size());
        assertEquals(newDeviceStatus.toString(), Postgres.findDeviceStatus(newId).toString());
    }

    @Test
    public void testDeleteDeviceStatus() {
        assertEquals(1, Postgres.findAllDeviceStatuses().size());

        Postgres.deleteDeviceStatus(deviceStatus.getId());

        assertEquals(0, Postgres.findAllDeviceStatuses().size());
        assertEquals(null, Postgres.findDeviceStatus(deviceStatus.getId()));
    }

    private static void insertData() {
        // insert device_type
        deviceType = new DeviceType(0, "Udoo Neo");
        deviceType.insert();

        deviceTypeTwo = new DeviceType(0, "test device type");
        deviceTypeTwo.insert();

        // insert Group
        group = new Group("Test Group");
        group.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1);
        device.insert();

        deviceTwo = new Device("Device 2", "this is also a test device", deviceTypeTwo.getId(), group.getId(), "0.0.0.1", 1, 1);
        deviceTwo.insert();

        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put("test", "test");

        // insert device_status
        deviceStatus = new DeviceStatus(device.getId(), hmap);
        deviceStatus.insert();
    }
}
