package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import edu.cmu.sei.kalki.db.daos.AlertTypeDAO;
import edu.cmu.sei.kalki.db.daos.AlertTypeLookupDAO;
import edu.cmu.sei.kalki.db.models.AlertType;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import edu.cmu.sei.kalki.db.models.DataNode;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;

public class AlertTypeLookupTest extends AUsesDatabase {
    private AlertType alertType;
    private DeviceType deviceType;
    private Device device;
    private HashMap<String, String> hmap;

    @Test
    public void testFindAlertTypeLookupBy() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        assertNotEquals(-1, alertTypeLookup.getId());
    }

    @Test
    public void testFindAlertTypeLookupsByDeviceType() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        List<AlertTypeLookup> atlList = AlertTypeLookupDAO.findAlertTypeLookupsByDeviceType(deviceType.getId());
        assertEquals(1, atlList.size());
        assertEquals(deviceType.getId(), atlList.get(0).getDeviceTypeId());
    }

    @Test
    public void testFindAllAlertTypeLookups() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        List<AlertTypeLookup> atlList = AlertTypeLookupDAO.findAllAlertTypeLookups();
        assertEquals(1, atlList.size());
        assertEquals(alertTypeLookup.toString(), atlList.get(0).toString());
    }

    @Test
    public void testInsertAlertTypeLookup() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        assertNotEquals(-1, alertTypeLookup.getId());
    }

    @Test
    public void testUpdateAlertTypeLookup() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        AlertType alertType1 = new AlertType("Name2", "Description", "Source");
        alertType1.insert();

        alertTypeLookup.setAlertTypeId(alertType1.getId());
        AlertTypeLookupDAO.updateAlertTypeLookup(alertTypeLookup);
        Assertions.assertEquals(alertTypeLookup.toString(), AlertTypeLookupDAO.findAlertTypeLookup(alertTypeLookup.getId()).toString());
    }

    @Test
    public void testInsertOrUpdateAlertTypeLookup() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insertOrUpdate();
        assertNotEquals(-1, alertTypeLookup.getId());

        AlertType alertType1 = new AlertType("Name2", "Description", "Source");
        alertType1.insert();

        alertTypeLookup.setAlertTypeId(alertType1.getId());
        alertTypeLookup.insertOrUpdate();
        Assertions.assertEquals(alertTypeLookup.toString(), AlertTypeLookupDAO.findAlertTypeLookup(alertTypeLookup.getId()).toString());
        assertEquals(1, AlertTypeLookupDAO.findAllAlertTypeLookups().size());
    }

    @Test
    public void testFindAlertTypesByDeviceType() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        List<AlertType> atList = AlertTypeDAO.findAlertTypesByDeviceType(deviceType.getId());
        assertEquals(1, atList.size());
        assertEquals(alertType.toString(), atList.get(0).toString());

        DeviceType deviceType1 = new DeviceType(-1, "Test type 1");
        deviceType1.insert();
        atList = AlertTypeDAO.findAlertTypesByDeviceType(deviceType1.getId());
        assertEquals(0, atList.size());
    }


    public void insertData(){
        deviceType = new DeviceType(-1, "Device Type");
        deviceType.insert();

        alertType = new AlertType("Name", "Description", "Source");
        alertType.insert();

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        device = new Device("Name", "Description", deviceType, "1.1.1.1", 1, 1, dataNode);
        device.insert();

        hmap = new HashMap<String, String>();
        hmap.put("test", "test-var");
    }
}
