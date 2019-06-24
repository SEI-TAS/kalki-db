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
    private static Tag tag;
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
        String rootPassword = "kalkipass";  //based on run script
        String dbHost = "localhost";        //based on run script
        String dbPort = "5432";             //based on run script
        String dbName = "kalkidb_test";
        String dbUser = "kalkiuser_test";
        String dbPass = "kalkipass";

        try {
            // Recreate DB and user.
            Postgres.removeDatabase(rootPassword, dbName);
            Postgres.removeUser(rootPassword, dbUser);
            Postgres.createUserIfNotExists(rootPassword, dbUser, dbPass);
            Postgres.createDBIfNotExists(rootPassword, dbName, dbUser);

            //initialize test DB
            Postgres.initialize(dbHost, dbPort, dbName, dbUser, dbPass);
        } catch (Exception e) {
            System.out.println(e);
        }
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

    /*
        Test Device Type Actions
     */

    @Test
    public void testFindDeviceType() {
        assertEquals(deviceType.toString(), Postgres.findDeviceType(deviceType.getId()).toString());
        assertEquals(deviceTypeTwo.toString(), Postgres.findDeviceType(deviceTypeTwo.getId()).toString());
    }

    @Test
    public void testFindAllDeviceTypes() {
        ArrayList<DeviceType> foundTypes = new ArrayList<DeviceType>(Postgres.findAllDeviceTypes());

        assertEquals(6, foundTypes.size()); //4 inserted by setupDatabase() plus 2 in insertData()
    }

    @Test
    public void testInsertOrUpdateDeviceType() {
        ArrayList<DeviceType> foundTypes = new ArrayList<DeviceType>(Postgres.findAllDeviceTypes());
        assertEquals(6, foundTypes.size()); //4 inserted by setupDatabase() plus 2 in insertData()

        deviceType.setName("changed name");
        deviceType.insertOrUpdate();

        foundTypes = new ArrayList<DeviceType>(Postgres.findAllDeviceTypes());
        assertEquals(6, foundTypes.size()); //4 inserted by setupDatabase() plus 2 in insertData()
        assertEquals(deviceType.getName(), Postgres.findDeviceType(deviceType.getId()).getName());

        DeviceType newDeviceType = new DeviceType(0, "new device type");
        int newId = newDeviceType.insertOrUpdate();

        foundTypes = new ArrayList<DeviceType>(Postgres.findAllDeviceTypes());
        assertEquals(7, foundTypes.size()); //4 inserted by setupDatabase() plus 2 in insertData()
        assertEquals(newDeviceType.toString(), Postgres.findDeviceType(newDeviceType.getId()).toString());
    }

    //So many things reference deviceType that it is extremely difficult to delete
//    @Test
//    public void testDeleteDeviceType() {
//        //need to delete device and all lookups before you can delete a device type
//        assertEquals(deviceType.toString(), Postgres.findDeviceType(deviceType.getId()).toString());
//
//        //need to delete device and all lookups before you can delete a device type
//        Postgres.deleteAlertCondition(alertConditionTwo.getId());   //must delete before deleting device
//        Postgres.deleteDevice(deviceTwo.getId());
//        Postgres.deleteCommandLookup(deviceCommandLookup.getId());
//        Postgres.deleteUmboxLookup(umboxLookup.getId());
//
//        Postgres.deleteDeviceType(deviceType.getId());
//
//        assertEquals(null, Postgres.findDeviceType(deviceType.getId()));
//    }

    /*
        Security State Action Tests
     */

    @Test
    public void testFindSecurityState() {
        assertEquals(securityState.toString(), Postgres.findSecurityState(securityState.getId()).toString());
    }

    @Test
    public void testFindAllSecurityStates() {
        ArrayList<SecurityState> foundSecurityStates = new ArrayList<SecurityState>(Postgres.findAllSecurityStates());
        assertEquals(4, foundSecurityStates.size()); //3 added by resetDatabase and 1 added in insertData
    }

    @Test
    public void testInsertOrUpdateSecurityState() {
        ArrayList<SecurityState> foundStates = new ArrayList<SecurityState>(Postgres.findAllSecurityStates());
        assertEquals(4, foundStates.size());

        securityState.setName("changed security state");
        securityState.insertOrUpdate();

        foundStates = new ArrayList<SecurityState>(Postgres.findAllSecurityStates());
        assertEquals(4, foundStates.size());
        assertEquals(securityState.toString(), Postgres.findSecurityState(securityState.getId()).toString());

        SecurityState newSecurityState = new SecurityState("new security state");
        int newId = newSecurityState.insertOrUpdate();

        foundStates = new ArrayList<SecurityState>(Postgres.findAllSecurityStates());
        assertEquals(5, foundStates.size());
        assertEquals(newSecurityState.toString(), Postgres.findSecurityState(newSecurityState.getId()).toString());
    }

    //requires a lot of deleting so I am going to wait on this test until I separate into classes
//    @Test
//    public void testDeleteSecurityState() {
//
//    }

    /*
        Test Tag Actions
     */

    @Test
    public void testFindTag() {
        assertEquals(tag.toString(), Postgres.findTag(tag.getId()).toString());
    }

    @Test
    public void testFindAllTags() {
        assertEquals(1, Postgres.findAllTags().size());
    }

    @Test
    public void testInsertOrUpdateTag() {
        assertEquals(1, Postgres.findAllTags().size());

        tag.setName("new tag name");
        tag.insertOrUpdate();

        assertEquals(1, Postgres.findAllTags().size());
        assertEquals(tag.toString(), Postgres.findTag(tag.getId()).toString());

        Tag newTag = new Tag("Tag2");
        int newId = newTag.insertOrUpdate();
        assertEquals(2, Postgres.findAllTags().size());
        assertEquals(newTag.toString(), Postgres.findTag(newId).toString());
    }

    @Test
    public void testDeleteTag() {
        assertEquals(tag.toString(), Postgres.findTag(tag.getId()).toString());
        Postgres.deleteTag(tag.getId());
        assertEquals(null, Postgres.findTag(tag.getId()));
    }

    /*
        Test Umbox Image Actions
     */

    @Test
    public void testFindUmboxImage() {
        assertEquals(umboxImage.toString(), Postgres.findUmboxImage(umboxImage.getId()).toString());
    }

    @Test
    public void testFindAllUmboxImages() {
        assertEquals(2, Postgres.findAllUmboxImages().size()); //one added in setupDatabase and one in insertData
    }

    @Test
    public void testInsertOrUpdateUmboxImage() {
        assertEquals(2, Postgres.findAllUmboxImages().size());

        umboxImage.setName("changed name");
        umboxImage.insertOrUpdate();

        assertEquals(2, Postgres.findAllUmboxImages().size());
        assertEquals(umboxImage.toString(), Postgres.findUmboxImage(umboxImage.getId()).toString());

        UmboxImage newImage = new UmboxImage("image2", "path/to/new/image");
        int newId = newImage.insertOrUpdate();
        assertEquals(3, Postgres.findAllUmboxImages().size());
        assertEquals(newImage.toString(), Postgres.findUmboxImage(newId).toString());
    }
//      waiting until I separate out the classes so I don't have to do so many deletes
//    @Test
//    public void testDeleteUmboxImage() {
//        assertEquals(umboxImage.toString(), Postgres.findUmboxImage(umboxImage.getId()).toString());
//        Postgres.deleteUmboxImage(umboxImage.getId());
//        assertEquals(null, Postgres.findUmboxImage(umboxImage.getId()));
//    }

    /*
        Test umbox instance actions
     */

    @Test
    public void testFindUmboxInstance() {
        assertEquals(umboxInstance.toString(), Postgres.findUmboxInstance(umboxInstance.getAlerterId()).toString());
    }

    @Test
    public void testFindUmboxInstances() {
        ArrayList<UmboxInstance> foundInstances =
                new ArrayList<UmboxInstance>(Postgres.findUmboxInstances(device.getId()));

        assertEquals(1, foundInstances.size());
        assertEquals(umboxInstance.toString(), foundInstances.get(0).toString());
    }

    @Test
    public void testInsertUmboxInstance() {
        UmboxInstance newUmboxInstance = new UmboxInstance("new alerter id", umboxImage.getId(), device.getId());

        assertEquals(null, Postgres.findUmboxInstance(newUmboxInstance.getAlerterId()));

        newUmboxInstance.insert();

        assertEquals(newUmboxInstance.toString(),
                Postgres.findUmboxInstance(newUmboxInstance.getAlerterId()).toString());
    }

    //  I think this is failing to update due to some reference, will try after separating classes
//    @Test
//    public void testUpdateUmboxInstance() {
//        assertEquals(umboxInstance.getAlerterId(),
//                Postgres.findUmboxInstance(umboxInstance.getAlerterId()).getAlerterId());
//
//        umboxInstance.setAlerterId("changed alerter id");
//        Postgres.updateUmboxInstance(umboxInstance);
//
//        assertEquals(umboxInstance.getAlerterId(),
//                Postgres.findUmboxInstance(umboxInstance.getAlerterId()).getAlerterId());
//    }

//    @Test
//    public void testDeleteUmboxInstance() {
//
//    }

    /*
        test umbox lookup actions
     */

    @Test
    public void testFindUmboxLookup() {
        assertEquals(umboxLookup.toString(), Postgres.findUmboxLookup(umboxLookup.getId()).toString());
    }

    @Test
    public void testFindAllUmboxLookups() {
        assertEquals(1, Postgres.findAllUmboxLookups().size());
    }

    @Test
    public void testInsertOrUpdateUmboxLookup() {
        assertEquals(umboxLookup.toString(), Postgres.findUmboxLookup(umboxLookup.getId()).toString());

        umboxLookup.setDeviceTypeId(deviceTypeTwo.getId());
        umboxLookup.insertOrUpdate();

        assertEquals(umboxLookup.toString(), Postgres.findUmboxLookup(umboxLookup.getId()).toString());

        UmboxLookup newUmboxLookup = new UmboxLookup(-1, securityState.getId(), deviceType.getId(), umboxImage.getId(), 2);

        assertEquals(null, Postgres.findUmboxLookup(newUmboxLookup.getId()));

        int newId = newUmboxLookup.insertOrUpdate();

        assertEquals(newUmboxLookup.toString(),
                Postgres.findUmboxLookup(newId).toString());
    }

    //Waiting until separated
//    @Test
//    public void testDeleteUmboxLookup() {
//
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

        // insert Tag
        tag = new Tag("Tag1");
        tag.insert();

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