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

public class AlertTest extends AUsesDatabase {
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static Group group;
    private static Device device;
    private static Device deviceTwo;
    private static UmboxImage umboxImage;
    private static UmboxInstance umboxInstance;
    private static AlertType alertType;
    private static DeviceStatus deviceStatus;
    private static Alert alertIoT;
    private static Alert alertUmBox;

    /*
        Alert Action Tests
     */

    @Test
    public void testFindAlert() {
        assertEquals(alertIoT.toString(), Postgres.findAlert(alertIoT.getId()).toString());
    }

    @Test
    public void testFindAlerts() {
        ArrayList<String> alerterIds = new ArrayList<String>();
        alerterIds.add(umboxInstance.getAlerterId());

        ArrayList<Alert> foundAlerts = new ArrayList<Alert>(Postgres.findAlerts(alerterIds));

        assertEquals(1, foundAlerts.size());
        assertEquals(alertUmBox.toString(), foundAlerts.get(0).toString());
    }

    @Test
    public void testFindAlertsByDevice() {
        ArrayList<Alert> foundAlerts = new ArrayList<Alert>(Postgres.findAlertsByDevice(device.getId()));

        assertEquals(2, foundAlerts.size());

        foundAlerts = new ArrayList<Alert>(Postgres.findAlertsByDevice(deviceTwo.getId()));
        assertEquals(0, foundAlerts.size());
    }

    @Test
    public void testInsertAlert() {
        Alert newAlert = new Alert(alertType.getName(), umboxInstance.getAlerterId(), deviceStatus.getId(), alertType.getId());
        assertEquals(null, Postgres.findAlert(3));

        newAlert.insert();

        assertEquals(newAlert.toString(), Postgres.findAlert(3).toString());
    }

    @Test
    public void testUpdateAlert() {
        assertEquals(alertIoT.toString(), Postgres.findAlert(alertIoT.getId()).toString());

        alertIoT.setName("new iot alert name");
        Postgres.updateAlert(alertIoT);

        assertEquals(alertIoT.toString(), Postgres.findAlert(alertIoT.getId()).toString());
    }

    @Test
    public void testDeleteAlert() {
        assertEquals(alertIoT.toString(), Postgres.findAlert(alertIoT.getId()).toString());

        Postgres.deleteAlert(alertIoT.getId());
        assertEquals(null, Postgres.findAlert(alertIoT.getId()));
    }

    public void insertData() {
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

        // insert umbox_image
        umboxImage = new UmboxImage("UmboxImage", "path/to/image");
        umboxImage.insert();

        // insert umbox_instance from umbox_lookup
        umboxInstance = new UmboxInstance("testing123", umboxImage.getId(), device.getId());
        umboxInstance.insert();

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

        // insert alert for alerter_id/alert_type
        alertUmBox = new Alert(alertType.getName(), umboxInstance.getAlerterId(), deviceStatus.getId(), alertType.getId());
        alertUmBox.insert();
    }
}