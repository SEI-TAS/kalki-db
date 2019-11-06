package edu.cmu.sei.ttg.kalki.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.sei.ttg.kalki.database.Postgres;
import edu.cmu.sei.ttg.kalki.models.*;

import edu.cmu.sei.ttg.kalki.database.AUsesDatabase;

public class UmboxInstanceTest extends AUsesDatabase {
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static Group group;
    private static Device device;
    private static Device deviceTwo;
    private static UmboxImage umboxImage;
    private static UmboxInstance umboxInstance;


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

    @Test
    public void testUpdateUmboxInstance() {
        assertEquals(umboxInstance.getAlerterId(),
                Postgres.findUmboxInstance(umboxInstance.getAlerterId()).getAlerterId());

        umboxInstance.setAlerterId("changed alerter id");

        Postgres.updateUmboxInstance(umboxInstance);

        assertEquals(umboxInstance.getAlerterId(),
                Postgres.findUmboxInstance(umboxInstance.getAlerterId()).getAlerterId());
    }

    @Test
    public void testDeleteUmboxInstance() {
        assertEquals(umboxInstance.toString(), Postgres.findUmboxInstance(umboxInstance.getAlerterId()).toString());
        Postgres.deleteUmboxInstance(umboxInstance.getId());
        assertEquals(null, Postgres.findUmboxInstance(umboxInstance.getAlerterId()));
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
    }
}
