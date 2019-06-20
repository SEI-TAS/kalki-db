package edu.cmu.sei.ttg.kalki.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.Before;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.Optional;
import java.sql.Timestamp;
import java.util.logging.Level;

import edu.cmu.sei.ttg.kalki.database.Postgres;
import edu.cmu.sei.ttg.kalki.models.*;

public class PostgresTest {
    private static SecurityState securityState;
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static Group group;
    private static Device device;
    private static Device deviceTwo;
    private static DeviceCommand deviceCommand;
    private static DeviceCommand deviceCommandLookup;
    private static DeviceSecurityState deviceSecurityState;
    private static UmboxImage umboxImage;
    private static UmboxInstance umboxInstance;
    private static UmboxLookup umboxLookup;
    private static AlertType alertType;
    private static AlertCondition alertCondition;
    private static AlertCondition alertConditionTwo;
    private static DeviceStatus deviceStatus;
    private static Alert alertIoT;
    private static Alert alertUmBox;

    @BeforeClass
    public static void initializeDB() {
        Postgres.initialize("localhost", "5432", "kalkidb", "kalkiuser", "kalkipass");
    }

    @Before
    public void resetDB() {
        Postgres.resetDatabase();
        insertData();
    }


    /*
        Alert Condition Action Tests
     */
    @Test
    public void testFindAlertCondition() {
        AlertCondition ac = Postgres.findAlertCondition(alertCondition.getId());
        assertEquals(ac.toString(), alertCondition.toString());
    }

    @Test
    public void testFindAlertConditionsByDevice() {
        List<AlertCondition> acList = Postgres.findAlertConditionsByDevice(device.getId());
        for (AlertCondition ac : acList) {
            assertEquals(ac.toString(), alertCondition.toString());
        }

        List<AlertCondition> acList2 = Postgres.findAlertConditionsByDevice(deviceTwo.getId());
        for (AlertCondition ac2 : acList2) {
            assertEquals(ac2.toString(), alertConditionTwo.toString());
        }
    }

    @Test
    public void testFindAllAlertConditions() {
        ArrayList<AlertCondition> acList = new ArrayList<AlertCondition>(Postgres.findAllAlertConditions());
        assertEquals(acList.get(0).toString(), alertCondition.toString());
        assertEquals(acList.get(1).toString(), alertConditionTwo.toString());
    }

    @Test
    public void testUpdateAlertCondition() {
        alertCondition.getVariables().put("test2", "test2");
        Postgres.updateAlertCondition(alertCondition);

        AlertCondition updatedCondition = Postgres.findAlertCondition(alertCondition.getId());
        assertEquals(updatedCondition.toString(), alertCondition.toString());
    }

    @Test
    public void testInsertOrUpdateAlertCondition() {
        assertEquals(Postgres.findAllAlertConditions().size(), 2);

        alertCondition.getVariables().put("testKey1", "testValue1");
        alertCondition.insertOrUpdate();

        assertEquals(Postgres.findAllAlertConditions().size(), 2);

        AlertCondition newAlertCondition = new AlertCondition(null, device.getId(), alertType.getId());
        int newId = newAlertCondition.insertOrUpdate();

        assertEquals(Postgres.findAllAlertConditions().size(), 3);
        assertEquals(Postgres.findAlertCondition(newId).toString(), newAlertCondition.toString());
    }

    @Test
    public void testInsertAlertConditionByDeviceType() {
        Postgres.insertAlertConditionByDeviceType(alertCondition); //device type is null

        assertEquals(Postgres.findAllAlertConditions().size(), 2);

        alertCondition.setDeviceTypeId(deviceType.getId());

        Postgres.insertAlertConditionByDeviceType(alertCondition);

        assertEquals(Postgres.findAllAlertConditions().size(), 3);
    }

    /*
        Alert Type Action Tests
     */

    @Test
    public void testFindAlertType() {
        AlertType at = Postgres.findAlertType(alertType.getId());
        assertEquals(at.toString(), alertType.toString());
    }

    @Test
    public void testFindAllAlertTypes() {
        List<AlertType> alertTypeList = new ArrayList<AlertType>(Postgres.findAllAlertTypes());

        assertEquals(alertTypeList.size(), 24);     //alertType plus the 23 added in Postgres.setupDatabase()
        assertEquals(alertTypeList.get(23).toString(), alertType.toString());
    }

    @Test
    public void testFindAlertTypesByDeviceType() {  //based on the setup database script
        List<AlertType> atList = new ArrayList<AlertType>(Postgres.findAlertTypesByDeviceType(2));

        assertEquals(atList.size(), 14);
    }

    @Test
    public void testUpdateAlertType() {
        assertEquals(alertType.toString(), Postgres.findAlertType(alertType.getId()).toString());

        alertType.setDescription("new description");
        Postgres.updateAlertType(alertType);

        assertEquals(alertType.toString(), Postgres.findAlertType(alertType.getId()).toString());
    }

    @Test
    public void testInsertOrUpdateAlertType() {
        assertEquals(Postgres.findAllAlertTypes().size(), 24);

        alertType.setDescription("new description");
        alertType.insertOrUpdate();

        assertEquals(Postgres.findAllAlertTypes().size(), 24);

        AlertType newAlertType = new AlertType("AlertType2", "test alert type 2", "IoT Monitor");
        int newId = newAlertType.insertOrUpdate();

        assertEquals(Postgres.findAllAlertTypes().size(), 25);
        assertEquals(Postgres.findAlertType(newId).toString(), newAlertType.toString());
    }

    /*
        Alert Action Tests
     */

    @Test
    public void testFindAlert() {
        assertEquals(Postgres.findAlert(alertIoT.getId()).toString(), alertIoT.toString());
    }

    @Test
    public void testFindAlerts() {
        ArrayList<String> alerterIds = new ArrayList<String>();
        alerterIds.add(umboxInstance.getAlerterId());

        ArrayList<Alert> foundAlerts = new ArrayList<Alert>(Postgres.findAlerts(alerterIds));

        assertEquals(foundAlerts.size(), 1);
        assertEquals(foundAlerts.get(0).toString(), alertUmBox.toString());
    }

    @Test
    public void testInsertAlert() {
        Alert newAlert = new Alert(alertType.getName(), umboxInstance.getAlerterId(), deviceStatus.getId(), alertType.getId());
        assertEquals(null, Postgres.findAlert(3));

        newAlert.insert();

        assertEquals(Postgres.findAlert(3).toString(), newAlert.toString());
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

    /*
        Command Action Tests
     */
    /*  CONFUSED ABOUT THESE TESTS

    @Test
    public void testFindAllCommands() {
        assertEquals(Postgres.findAllCommands().size(), 2);
    }

    @Test
    public void testFindCommandLookup() {
        assertEquals(Postgres.findCommandLookup(deviceCommandLookup.getLookupId()).toString(),
                deviceCommandLookup.toString());
    }

    @Test
    public void testFindAllCommandLookups() {

    }

    @Test
    public void testFindCommandsByDevice() {

    }

    @Test
    public void testInsertCommand() {

    }

    @Test
    public void testInsertOrUpdateCommandLookup() {

    }

    @Test
    public void testDeleteCommandLookup() {

    }
    */

    /*
        Test Device Actions
     */

    @Test
    public void testFindDevice() {
        assertEquals(Postgres.findDevice(device.getId()).getDescription(), device.getDescription());
        assertEquals(Postgres.findDevice(deviceTwo.getId()).getDescription(), deviceTwo.getDescription());
    }

    @Test
    public void testFindAllDevices() {
        assertEquals(Postgres.findAllDevices().size(), 2);
    }

    @Test
    public void testFindDevicesByGroup() {
        ArrayList<Device> foundDevices = new ArrayList<Device>(Postgres.findDevicesByGroup(group.getId()));

        assertEquals(foundDevices.size(), 1);
        assertEquals(foundDevices.get(0).getDescription(), deviceTwo.getDescription());
    }

    @Test
    public void testFindDeviceByAlert() {
        Device foundDevice = Postgres.findDeviceByAlert(alertIoT);
        assertEquals(foundDevice.getDescription(), device.getDescription());
    }

    @Test
    public void testFindDevicesByType() {
        ArrayList<Device> foundDevices = new ArrayList<Device>(Postgres.findDevicesByType(deviceTypeTwo.getId()));

        assertEquals(foundDevices.size(), 1);
        assertEquals(foundDevices.get(0).getDescription(), deviceTwo.getDescription());
    }

    @Test
    public void testInsertOrUpdateDevice() {
        assertEquals(Postgres.findAllDevices().size(), 2);

        device.setDescription("new description");
        device.insertOrUpdate();

        assertEquals(Postgres.findAllDevices().size(), 2);

        Device newDevice = new Device("Device 3", "this is a newly added device", deviceType, "0.0.0.0", 2, 2);
        int newId = newDevice.insertOrUpdate();

        assertEquals(Postgres.findAllDevices().size(), 3);
        assertEquals(Postgres.findDevice(newId).getDescription(), newDevice.getDescription());
    }

    @Test
    public void testDeleteDevice() {
        assertNotNull(Postgres.findDevice(deviceTwo.getId()));

        Postgres.deleteAlertCondition(alertConditionTwo.getId());   //must delete before deleting device
        Postgres.deleteDevice(deviceTwo.getId());

        assertEquals(null, Postgres.findDevice(deviceTwo.getId()));
    }

    private static void insertData() {
        // insert security state(s)
        securityState = new SecurityState("Normal");
        securityState.insert();

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

        // insert device_security_state
        deviceSecurityState = new DeviceSecurityState(device.getId(), securityState.getId());
        deviceSecurityState.insert();

        device.setCurrentState(deviceSecurityState);

        // insert command_lookups
        deviceCommand = new DeviceCommand("Test Command");
        deviceCommand.insert();

        deviceCommandLookup = new DeviceCommand("Test Command Lookup");
        deviceCommandLookup.setDeviceTypeId(deviceType.getId());
        deviceCommandLookup.setStateId(securityState.getId());
        deviceCommandLookup.setId(deviceCommand.getId());
        deviceCommandLookup.insertCommandLookup();

        // insert umbox_image
        umboxImage = new UmboxImage("UmboxImage", "path/to/image");
        umboxImage.insert();

        // insert umbox_lookup (should be handle by umbox_image)
        umboxLookup = new UmboxLookup(-1, securityState.getId(), deviceType.getId(), umboxImage.getId(), 1);
        umboxLookup.insertOrUpdate();

        // insert umbox_instance from umbox_lookup
        umboxInstance = new UmboxInstance("testing123", umboxImage.getId(), device.getId());
        umboxInstance.insert();

        // insert alert_type unts-temperature
        alertType = new AlertType("UNTS-Temperature", "test alert type", "IoT Monitor");
        alertType.insert();

        // insert alert_condition
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put("test", "test");
        alertCondition = new AlertCondition(hmap, device.getId(), alertType.getId());
        alertCondition.insert();

        alertConditionTwo = new AlertCondition(null, deviceTwo.getId(), alertType.getId());
        alertConditionTwo.insert();

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