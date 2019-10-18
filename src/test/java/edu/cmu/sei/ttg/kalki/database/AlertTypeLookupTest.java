package edu.cmu.sei.ttg.kalki.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;

import edu.cmu.sei.ttg.kalki.database.Postgres;
import edu.cmu.sei.ttg.kalki.models.*;

import edu.cmu.sei.ttg.kalki.database.AUsesDatabase;

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

        List<AlertTypeLookup> atlList = Postgres.findAlertTypeLookupsByDeviceType(deviceType.getId());
        assertEquals(1, atlList.size());
        assertEquals(deviceType.getId(), atlList.get(0).getDeviceTypeId());
    }

    @Test
    public void testFindAllAlertTypeLookups() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        List<AlertTypeLookup> atlList = Postgres.findAllAlertTypeLookups();
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
        Postgres.updateAlertTypeLookup(alertTypeLookup);
        assertEquals(alertTypeLookup.toString(), Postgres.findAlertTypeLookup(alertTypeLookup.getId()).toString());
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
        assertEquals(alertTypeLookup.toString(), Postgres.findAlertTypeLookup(alertTypeLookup.getId()).toString());
        assertEquals(1, Postgres.findAllAlertTypeLookups().size());
    }

    @Test
    public void testFindAlertTypesByDeviceType() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        List<AlertType> atList = Postgres.findAlertTypesByDeviceType(deviceType.getId());
        assertEquals(1, atList.size());
        assertEquals(alertType.toString(), atList.get(0).toString());

        DeviceType deviceType1 = new DeviceType(-1, "Test type 1");
        deviceType1.insert();
        atList = Postgres.findAlertTypesByDeviceType(deviceType1.getId());
        assertEquals(0, atList.size());
    }


    public void insertData(){
        deviceType = new DeviceType(-1, "Device Type");
        deviceType.insert();

        alertType = new AlertType("Name", "Description", "Source");
        alertType.insert();

        device = new Device("Name", "Description", deviceType, "1.1.1.1", 1, 1);
        device.insert();

        hmap = new HashMap<String, String>();
        hmap.put("test", "test-var");
    }
}
