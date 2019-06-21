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
        assertEquals(alertCondition.toString(), ac.toString());
    }

    @Test
    public void testFindAlertConditionsByDevice() {
        List<AlertCondition> acList = Postgres.findAlertConditionsByDevice(device.getId());
        for (AlertCondition ac : acList) {
            assertEquals(alertCondition.toString(), ac.toString());
        }

        List<AlertCondition> acList2 = Postgres.findAlertConditionsByDevice(deviceTwo.getId());
        for (AlertCondition ac2 : acList2) {
            assertEquals(alertConditionTwo.toString(), ac2.toString());
        }
    }

    @Test
    public void testFindAllAlertConditions() {
        ArrayList<AlertCondition> acList = new ArrayList<AlertCondition>(Postgres.findAllAlertConditions());
        assertEquals(alertCondition.toString(), acList.get(0).toString());
        assertEquals(alertConditionTwo.toString(), acList.get(1).toString());
    }

    @Test
    public void testUpdateAlertCondition() {
        alertCondition.getVariables().put("test2", "test2");
        Postgres.updateAlertCondition(alertCondition);

        AlertCondition updatedCondition = Postgres.findAlertCondition(alertCondition.getId());
        assertEquals(alertCondition.toString(), updatedCondition.toString());
    }

    @Test
    public void testInsertOrUpdateAlertCondition() {
        assertEquals(2, Postgres.findAllAlertConditions().size());

        alertCondition.getVariables().put("testKey1", "testValue1");
        alertCondition.insertOrUpdate();

        assertEquals(2, Postgres.findAllAlertConditions().size());

        AlertCondition newAlertCondition = new AlertCondition(null, device.getId(), alertType.getId());
        int newId = newAlertCondition.insertOrUpdate();

        assertEquals(3, Postgres.findAllAlertConditions().size());
        assertEquals(newAlertCondition.toString(), Postgres.findAlertCondition(newId).toString());
    }

    @Test
    public void testInsertAlertConditionByDeviceType() {
        Postgres.insertAlertConditionByDeviceType(alertCondition); //device type is null

        assertEquals(2, Postgres.findAllAlertConditions().size());

        alertCondition.setDeviceTypeId(deviceType.getId());

        Postgres.insertAlertConditionByDeviceType(alertCondition);

        assertEquals(3, Postgres.findAllAlertConditions().size());
    }

    /*
        Alert Type Action Tests
     */

    @Test
    public void testFindAlertType() {
        AlertType at = Postgres.findAlertType(alertType.getId());
        assertEquals(alertType.toString(), at.toString());
    }

    @Test
    public void testFindAllAlertTypes() {
        List<AlertType> alertTypeList = new ArrayList<AlertType>(Postgres.findAllAlertTypes());

        assertEquals(24, alertTypeList.size());     //alertType plus the 23 added in Postgres.setupDatabase()
        assertEquals(alertType.toString(), alertTypeList.get(23).toString());
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
        assertEquals(24, Postgres.findAllAlertTypes().size());

        alertType.setDescription("new description");
        alertType.insertOrUpdate();

        assertEquals(24, Postgres.findAllAlertTypes().size());

        AlertType newAlertType = new AlertType("AlertType2", "test alert type 2", "IoT Monitor");
        int newId = newAlertType.insertOrUpdate();

        assertEquals(25, Postgres.findAllAlertTypes().size());
        assertEquals(newAlertType.toString(), Postgres.findAlertType(newId).toString());
    }

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

        Postgres.deleteAlertCondition(alertConditionTwo.getId());   //must delete before deleting device
        Postgres.deleteDevice(deviceTwo.getId());

        assertEquals(null, Postgres.findDevice(deviceTwo.getId()));
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

//    @Test
//    public void testFindDeviceStatusesOverTime() {
//        ArrayList<DeviceStatus> foundStatuses =
//                new ArrayList<DeviceStatus>(Postgres.findDeviceStatusesOverTime(device.getId(), 6000000, "seconds"));
//
//        System.out.println(deviceStatus);
//
//        assertEquals(foundStatuses.size(), 0);
//    }

    //Is this important to test?
//    @Test
//    public void testFindDeviceStatusesByType() {
//    }

    //Is this important to test?
//    @Test
//    public void testFindDeviceStatusesByGroup() {
//    }

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

        Postgres.deleteAlert(alertIoT.getId()); //have to delete the alert first
        Postgres.deleteAlert(alertUmBox.getId());
        Postgres.deleteDeviceStatus(deviceStatus.getId());

        assertEquals(0, Postgres.findAllDeviceStatuses().size());
        assertEquals(null, Postgres.findDeviceStatus(deviceStatus.getId()));
    }

    /*
        Test Device Security State Actions
     */

    @Test
    public void testFindDeviceSecurityState() {
        DeviceSecurityState foundState = Postgres.findDeviceSecurityState(deviceSecurityState.getId());

        //toStrings are not equal becuase when finding a deviceSecurityState it adds the state name
        assertEquals(deviceSecurityState.getId(), foundState.getId());
        assertEquals(deviceSecurityState.getStateId(), foundState.getStateId());
    }

    @Test
    public void testFindDeviceSecurityStateByDevice() {
        DeviceSecurityState foundState = Postgres.findDeviceSecurityStateByDevice(device.getId());
        assertEquals(deviceSecurityState.getId(), foundState.getId());
        assertEquals(deviceSecurityState.getStateId(), foundState.getStateId());

        foundState = Postgres.findDeviceSecurityStateByDevice(deviceTwo.getId());
        assertEquals(null, foundState);
    }

    @Test
    public void testFindDeviceSecurityStates() {
        ArrayList<DeviceSecurityState> foundStates =
                new ArrayList<DeviceSecurityState>(Postgres.findDeviceSecurityStates(device.getId()));
        assertEquals(1, foundStates.size());

        foundStates = new ArrayList<DeviceSecurityState>(Postgres.findDeviceSecurityStates(deviceTwo.getId()));
        assertEquals(0, foundStates.size());
    }

    @Test
    public void testInsertDeviceSecurityState() {
        assertEquals(1, Postgres.findDeviceSecurityStates(device.getId()).size());

        DeviceSecurityState newState = new DeviceSecurityState(device.getId(), securityState.getId());
        newState.insert();

        assertEquals(2, Postgres.findDeviceSecurityStates(device.getId()).size());
    }

    //Confused why delete isn't working because nothing references the device security state
//    @Test
//    public void testDeleteDeviceSecurityState() {
//        DeviceSecurityState foundState = Postgres.findDeviceSecurityState(deviceSecurityState.getId());
//
//        assertEquals(deviceSecurityState.getId(), foundState.getId());
//        assertEquals(deviceSecurityState.getStateId(), foundState.getStateId());
//
//        Postgres.deleteDeviceSecurityState(deviceSecurityState.getId());
//
//        assertEquals(null, Postgres.findDeviceSecurityState(deviceSecurityState.getId()));
//    }


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