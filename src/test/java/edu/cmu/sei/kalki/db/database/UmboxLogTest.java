package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.UmboxImage;
import edu.cmu.sei.kalki.db.models.UmboxInstance;
import edu.cmu.sei.kalki.db.models.UmboxLog;
import org.junit.jupiter.api.Test;

import java.util.List;

import edu.cmu.sei.kalki.db.models.*;

public class UmboxLogTest extends AUsesDatabase {
    private Device device;
    private DeviceType deviceType;
    private UmboxImage umboxImage;
    private UmboxInstance umboxInstance;

//    private UmboxLog log;
    /*
        Test UmboxLog Actions
     */

    @Test
    public void testFindUmboxLog() {
        UmboxLog log = new UmboxLog(umboxInstance.getAlerterId(), "The details");
        log.insert();

        UmboxLog log1 = Postgres.findUmboxLog(log.getId());
        assertNotNull(log1);
        assertEquals(log.toString(), log1.toString());
    }

    @Test
    public void testFindAllUmboxLogs() {
        UmboxLog log = new UmboxLog(umboxInstance.getAlerterId(), "The details");
        log.insert();

        List<UmboxLog> UmboxLogList = Postgres.findAllUmboxLogs();
        assertNotEquals(0, UmboxLogList.size());
    }

    @Test
    public void testFindAllUmboxLogsForAlerterId() {
        UmboxLog log = new UmboxLog(umboxInstance.getAlerterId(), "The details");
        log.insert();

        List<UmboxLog> UmboxLogList = Postgres.findAllUmboxLogsForAlerterId(umboxInstance.getAlerterId());
        assertNotEquals(0, UmboxLogList.size());
    }

    public void insertData() {
        deviceType = new DeviceType(-1, "Test Device Type");
        deviceType.insert();

        device = new Device("Device Name", "Dev Description", deviceType, "ip", 1, 1);
        device.insert();

        umboxImage = new UmboxImage("Image Name", "Location");
        umboxImage.insert();

        umboxInstance = new UmboxInstance("alerter_id", umboxImage.getId(), device.getId());
        umboxInstance.insert();

    }
}
