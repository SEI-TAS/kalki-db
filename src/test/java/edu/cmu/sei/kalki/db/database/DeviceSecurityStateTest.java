package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceSecurityState;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.Group;
import edu.cmu.sei.kalki.db.models.SecurityState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import edu.cmu.sei.ttg.kalki.models.*;

public class DeviceSecurityStateTest extends AUsesDatabase {
    private static SecurityState securityState;
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static Group group;
    private static Device device;
    private static Device deviceTwo;
    private static DeviceSecurityState deviceSecurityState;

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
    }

    @Test
    public void testFindDeviceSecurityStates() {
        ArrayList<DeviceSecurityState> foundStates =
                new ArrayList<DeviceSecurityState>(Postgres.findDeviceSecurityStates(device.getId()));
        assertEquals(1, foundStates.size()); //the default normal state plus the new state
    }

    @Test
    public void testInsertDeviceSecurityState() {
        assertEquals(1, Postgres.findDeviceSecurityStates(device.getId()).size());

        DeviceSecurityState newState = new DeviceSecurityState(device.getId(), securityState.getId());
        newState.insert();

        assertEquals(2, Postgres.findDeviceSecurityStates(device.getId()).size());
    }

    @Test
    public void testDeleteDeviceSecurityState() {
        DeviceSecurityState foundState = Postgres.findDeviceSecurityState(deviceSecurityState.getId());

        assertEquals(deviceSecurityState.getId(), foundState.getId());
        assertEquals(deviceSecurityState.getStateId(), foundState.getStateId());

        Postgres.deleteDeviceSecurityState(deviceSecurityState.getId());

        assertEquals(null, Postgres.findDeviceSecurityState(deviceSecurityState.getId()));
    }

    public void insertData() {
        // insert security state(s)
        securityState = new SecurityState("testState");
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

        deviceTwo = new Device("Device 2", "this is also a test device", deviceTypeTwo.getId(), group.getId(), "0.0.0.1", 1, 1, 1);
        deviceTwo.insert();

        // insert device_security_state
        deviceSecurityState = device.getCurrentState();
    }
}
