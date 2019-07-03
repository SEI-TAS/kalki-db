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

public class UmboxLookupTest extends AUsesDatabase {
    private static SecurityState securityState;
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static Group group;
    private static Device device;
    private static Device deviceTwo;
    private static UmboxImage umboxImage;
    private static UmboxLookup umboxLookup;

    /*
        test umbox lookup actions
     */

    @Test
    public void testFindUmboxLookup() {
        assertEquals(umboxLookup.toString(), Postgres.findUmboxLookup(umboxLookup.getId()).toString());
    }

    @Test
    public void testFindUmboxLookupByImage() {
        assertEquals(umboxLookup.toString(), Postgres.findUmboxLookupByImage(umboxImage.getId()).toString());
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

    @Test
    public void testDeleteUmboxLookup() {
        assertEquals(umboxLookup.toString(), Postgres.findUmboxLookup(umboxLookup.getId()).toString());
        Postgres.deleteUmboxLookup(umboxLookup.getId());
        assertEquals(null, Postgres.findUmboxLookup(umboxLookup.getId()));
    }

    public void insertData() {
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

        // insert umbox_image
        umboxImage = new UmboxImage("UmboxImage", "path/to/image");
        umboxImage.insert();

        // insert umbox_lookup (should be handle by umbox_image)
        umboxLookup = new UmboxLookup(-1, securityState.getId(), deviceType.getId(), umboxImage.getId(), 1);
        umboxLookup.insertOrUpdate();
    }
}
