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

public class DeviceTest extends AUsesDatabase {
    private static SecurityState normalState;
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static Group group;
    private static Device device;
    private static Device deviceTwo;
    private static UmboxImage umboxImage;
    private static UmboxInstance umboxInstance;
    private static AlertType alertType;
    private static AlertType alertTypeReset;
    private static DeviceStatus deviceStatus;
    private static Alert alertIoT;
    private static Alert alertUmBox;

     /*
        Test Device Actions
     */

    @Test
    public void testFindDevice() {
        assertEquals(device.getDescription(), Postgres.findDevice(device.getId()).getDescription());
        assertEquals(deviceTwo.getDescription(), Postgres.findDevice(deviceTwo.getId()).getDescription());
    }

    @Test
    public void testFindAllDevices() {
        assertEquals(2, Postgres.findAllDevices().size());
    }

    @Test
    public void testFindDevicesByGroup() {
        ArrayList<Device> foundDevices = new ArrayList<Device>(Postgres.findDevicesByGroup(group.getId()));

        assertEquals(1, foundDevices.size());
        assertEquals(deviceTwo.getDescription(), foundDevices.get(0).getDescription());
    }

    @Test
    public void testFindDeviceByAlert() {
        Device foundDevice = Postgres.findDeviceByAlert(alertIoT);
        assertEquals(device.getDescription(), foundDevice.getDescription());
    }

    @Test
    public void testFindDevicesByType() {
        ArrayList<Device> foundDevices = new ArrayList<Device>(Postgres.findDevicesByType(deviceTypeTwo.getId()));

        assertEquals(1, foundDevices.size());
        assertEquals(deviceTwo.getDescription(), foundDevices.get(0).getDescription());
    }

    @Test
    public void testInsertOrUpdateDevice() {
        assertEquals(2, Postgres.findAllDevices().size());

        device.setDescription("new description");
        device.insertOrUpdate();

        assertEquals(2, Postgres.findAllDevices().size());

        Device newDevice = new Device("Device 3", "this is a newly added device", deviceType, "0.0.0.0", 2, 2);
        int newId = newDevice.insertOrUpdate();

        assertEquals(3, Postgres.findAllDevices().size());
        assertEquals(newDevice.getDescription(), Postgres.findDevice(newId).getDescription());
    }

    @Test
    public void testDeleteDevice() {
        assertNotNull(Postgres.findDevice(deviceTwo.getId()));

        Postgres.deleteDevice(deviceTwo.getId());

        assertEquals(null, Postgres.findDevice(deviceTwo.getId()));
    }

    @Test
    public void testResetSecurityState() {
        ArrayList<Alert> foundAlerts = new ArrayList<Alert>(Postgres.findAlertsByDevice(deviceTwo.getId()));
        assertEquals(0, foundAlerts.size());

        DeviceSecurityState newState = deviceTwo.resetSecurityState();

        foundAlerts = new ArrayList<Alert>(Postgres.findAlertsByDevice(deviceTwo.getId()));
        assertEquals(1, foundAlerts.size());
        assertEquals(deviceTwo.getCurrentState().toString(), newState.toString());
    }

    public void insertData() {
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
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1);
        device.insert();

        deviceTwo = new Device("Device 2", "this is also a test device", deviceTypeTwo.getId(), group.getId(), "0.0.0.1", 1, 1);
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
        alertIoT = new Alert(alertType.getName(), deviceStatus.getId(), alertType.getId());
        alertIoT.insert();

        //insert state reset alert type
        alertTypeReset = new AlertType("state-reset", "state reset", "Dashboard");
        alertTypeReset.insert();
    }
}