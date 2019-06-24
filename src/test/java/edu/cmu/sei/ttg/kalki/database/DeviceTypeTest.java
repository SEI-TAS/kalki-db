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

public class DeviceTypeTest extends AUsesDatabase {
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;

    @Before
    public void resetDB() {
        Postgres.resetDatabase();
        insertData();
    }

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

    @Test
    public void testDeleteDeviceType() {
        assertEquals(deviceType.toString(), Postgres.findDeviceType(deviceType.getId()).toString());

        Postgres.deleteDeviceType(deviceType.getId());

        assertEquals(null, Postgres.findDeviceType(deviceType.getId()));
    }


    private static void insertData() {
        // insert device_type
        deviceType = new DeviceType(0, "Udoo Neo");
        deviceType.insert();

        deviceTypeTwo = new DeviceType(0, "test device type");
        deviceTypeTwo.insert();
    }
}