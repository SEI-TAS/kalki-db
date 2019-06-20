package edu.cmu.sei.ttg.kalki.database;

import static org.junit.Assert.assertEquals;

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
    }

    @Test
    public void testInsertAlertConditionByDeviceType() {
        Postgres.insertAlertConditionByDeviceType(alertCondition); //device type is null

        assertEquals(Postgres.findAllAlertConditions().size(), 2);

        alertCondition.setDeviceTypeId(deviceType.getId());

        Postgres.insertAlertConditionByDeviceType(alertCondition);

        assertEquals(Postgres.findAllAlertConditions().size(), 4);
    }

    private static void insertData() {
        // insert security state(s)
        securityState = new SecurityState("Normal");
        securityState.insert();

        // insert device_type
        deviceType = new DeviceType(1, "Udoo Neo");
        deviceType.insert();

        // insert Group
        group = new Group("Test Group");
        group.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType.getId(), group.getId(), "0.0.0.0", 1, 1);
        device.insert();

        deviceTwo = new Device("Device 2", "this is also a test device", deviceType.getId(), group.getId(), "0.0.0.1", 1, 1);
        deviceTwo.insert();

        // insert device_security_state
        deviceSecurityState = new DeviceSecurityState(device.getId(), securityState.getId());
        deviceSecurityState.insert();

        device.setCurrentState(deviceSecurityState);

        // insert command_lookups
        deviceCommand = new DeviceCommand("Test Command");
        deviceCommand.insert();
        deviceCommand.setDeviceTypeId(deviceType.getId());
        deviceCommand.setStateId(securityState.getId());
        Postgres.insertCommandLookup(deviceCommand);

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
        alertUmBox = new Alert(alertType.getName(), umboxInstance.getAlerterId(), null, alertType.getId());
        //alertUmBox.insert();
    }

}