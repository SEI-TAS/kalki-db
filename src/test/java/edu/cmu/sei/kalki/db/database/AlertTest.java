package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.cmu.sei.kalki.db.daos.AlertDAO;
import edu.cmu.sei.kalki.db.models.Alert;
import edu.cmu.sei.kalki.db.models.AlertType;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceStatus;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.Group;
import edu.cmu.sei.kalki.db.models.UmboxImage;
import edu.cmu.sei.kalki.db.models.UmboxInstance;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import edu.cmu.sei.kalki.db.models.*;

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
        assertEquals(alertIoT.toString(), AlertDAO.findAlert(alertIoT.getId()).toString());
    }

    @Test
    public void testFindAlerts() {
        ArrayList<String> alerterIds = new ArrayList<String>();
        alerterIds.add(umboxInstance.getAlerterId());

        ArrayList<Alert> foundAlerts = new ArrayList<Alert>(AlertDAO.findAlerts(alerterIds));

        assertEquals(1, foundAlerts.size());
        assertEquals(alertUmBox.toString(), foundAlerts.get(0).toString());
    }

    @Test
    public void testFindAlertsByDevice() {
        ArrayList<Alert> foundAlerts = new ArrayList<Alert>(AlertDAO.findAlertsByDevice(device.getId()));

        assertEquals(2, foundAlerts.size());

        foundAlerts = new ArrayList<Alert>(AlertDAO.findAlertsByDevice(deviceTwo.getId()));
        assertEquals(0, foundAlerts.size());
    }

    @Test
    public void testInsertAlert() {
        Alert newAlert = new Alert(alertType.getName(), umboxInstance.getAlerterId(), alertType.getId(), "");
        assertEquals(null, AlertDAO.findAlert(3));

        newAlert.insert();

        assertEquals(newAlert.toString(), AlertDAO.findAlert(3).toString());

        newAlert = new Alert(alertType.getName(), deviceStatus.getId(), alertType.getId(), "");
        assertEquals(null, AlertDAO.findAlert(4));

        newAlert.insert();

        assertEquals(newAlert.toString(), AlertDAO.findAlert(4).toString());
    }

    @Test
    public void testUpdateAlert() {
        assertEquals(alertIoT.toString(), AlertDAO.findAlert(alertIoT.getId()).toString());

        alertIoT.setName("new iot alert name");
        AlertDAO.updateAlert(alertIoT);

        assertEquals(alertIoT.toString(), AlertDAO.findAlert(alertIoT.getId()).toString());
    }

    @Test
    public void testDeleteAlert() {
        assertEquals(alertIoT.toString(), AlertDAO.findAlert(alertIoT.getId()).toString());

        AlertDAO.deleteAlert(alertIoT.getId());
        assertEquals(null, AlertDAO.findAlert(alertIoT.getId()));
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

        deviceTwo = new Device("Device 2", "this is also a test device", deviceTypeTwo.getId(), group.getId(), "0.0.0.1", 1, 1, 1);
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
        alertIoT = new Alert(alertType.getName(), deviceStatus.getId(), alertType.getId(), "");
        alertIoT.insert();

        // insert alert for alerter_id/alert_type
        alertUmBox = new Alert(alertType.getName(), umboxInstance.getAlerterId(), alertType.getId(), "");
        alertUmBox.insert();
    }
}
