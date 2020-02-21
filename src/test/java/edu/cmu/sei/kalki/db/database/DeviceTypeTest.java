package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.cmu.sei.kalki.db.models.DeviceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import edu.cmu.sei.kalki.db.models.*;

public class DeviceTypeTest extends AUsesDatabase {
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;

    /*
        Test Device Type Actions
     */

    @Test
    public void testFindDeviceType() {
        Assertions.assertEquals(deviceType.toString(), Postgres.findDeviceType(deviceType.getId()).toString());
        Assertions.assertEquals(deviceTypeTwo.toString(), Postgres.findDeviceType(deviceTypeTwo.getId()).toString());
    }

    @Test
    public void testFindAllDeviceTypes() {
        ArrayList<DeviceType> foundTypes = new ArrayList<DeviceType>(Postgres.findAllDeviceTypes());

        assertEquals(2, foundTypes.size()); //4 inserted by setupDatabase() plus 2 in insertData()
    }

    @Test
    public void testInsertOrUpdateDeviceType() {
        ArrayList<DeviceType> foundTypes = new ArrayList<DeviceType>(Postgres.findAllDeviceTypes());
        assertEquals(2, foundTypes.size()); //4 inserted by setupDatabase() plus 2 in insertData()

        deviceType.setName("changed name");
        deviceType.insertOrUpdate();

        foundTypes = new ArrayList<DeviceType>(Postgres.findAllDeviceTypes());
        assertEquals(2, foundTypes.size()); //4 inserted by setupDatabase() plus 2 in insertData()
        Assertions.assertEquals(deviceType.getName(), Postgres.findDeviceType(deviceType.getId()).getName());

        DeviceType newDeviceType = new DeviceType(0, "new device type");
        int newId = newDeviceType.insertOrUpdate();

        foundTypes = new ArrayList<DeviceType>(Postgres.findAllDeviceTypes());
        assertEquals(3, foundTypes.size()); //4 inserted by setupDatabase() plus 2 in insertData()
        Assertions.assertEquals(newDeviceType.toString(), Postgres.findDeviceType(newDeviceType.getId()).toString());
    }

    @Test
    public void testDeleteDeviceType() {
        Assertions.assertEquals(deviceType.toString(), Postgres.findDeviceType(deviceType.getId()).toString());

        Postgres.deleteDeviceType(deviceType.getId());

        Assertions.assertEquals(null, Postgres.findDeviceType(deviceType.getId()));
    }


    public void insertData() {
        // insert device_type
        deviceType = new DeviceType(0, "Udoo Neo");
        deviceType.insert();

        deviceTypeTwo = new DeviceType(0, "test device type");
        deviceTypeTwo.insert();
    }
}
