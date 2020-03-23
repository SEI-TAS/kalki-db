package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.cmu.sei.kalki.db.daos.UmboxLookupDAO;
import edu.cmu.sei.kalki.db.models.DataNode;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.Group;
import edu.cmu.sei.kalki.db.models.SecurityState;
import edu.cmu.sei.kalki.db.models.UmboxImage;
import edu.cmu.sei.kalki.db.models.UmboxLookup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

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
        Assertions.assertEquals(umboxLookup.toString(), UmboxLookupDAO.findUmboxLookup(umboxLookup.getId()).toString());
    }

    @Test
    public void testFindUmboxLookupsByDevice() {
        ArrayList<UmboxLookup> foundLookups =
                new ArrayList<UmboxLookup>(UmboxLookupDAO.findUmboxLookupsByDevice(device.getId()));

        assertEquals(1, foundLookups.size());
        assertEquals(umboxLookup.toString(), foundLookups.get(0).toString());
    }

    @Test
    public void testFindAllUmboxLookups() {
        assertEquals(1, UmboxLookupDAO.findAllUmboxLookups().size());
    }

    @Test
    public void testInsertOrUpdateUmboxLookup() {
        UmboxLookup newUmboxLookup = new UmboxLookup(securityState.getId(), deviceType.getId(), umboxImage.getId(), 2);
        newUmboxLookup.insertOrUpdate();

        Assertions.assertEquals(newUmboxLookup.toString(), UmboxLookupDAO.findUmboxLookup(newUmboxLookup.getId()).toString());

        umboxLookup.setDagOrder(2);
        umboxLookup.insertOrUpdate();

        Assertions.assertEquals(umboxLookup.toString(), UmboxLookupDAO.findUmboxLookup(umboxLookup.getId()).toString());
    }

    @Test
    public void testDeleteUmboxLookup() {
        Assertions.assertEquals(umboxLookup.toString(), UmboxLookupDAO.findUmboxLookup(umboxLookup.getId()).toString());
        UmboxLookupDAO.deleteUmboxLookup(umboxLookup.getId());
        Assertions.assertEquals(null, UmboxLookupDAO.findUmboxLookup(umboxLookup.getId()));
    }

    public void insertData() {
        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

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
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1, dataNode);
        device.insert();

        deviceTwo = new Device("Device 2", "this is also a test device", deviceTypeTwo.getId(), group.getId(), "0.0.0.1", 1, 1, 1, dataNode.getId());
        deviceTwo.insert();

        // insert umbox_image
        umboxImage = new UmboxImage("UmboxImage", "path/to/image");
        umboxImage.insert();

        // insert umbox_lookup (should be handle by umbox_image)
        umboxLookup = new UmboxLookup(securityState.getId(), deviceType.getId(), umboxImage.getId(), 1);
        umboxLookup.insertOrUpdate();

    }
}
