package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceSecurityState;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.SecurityState;
import edu.cmu.sei.kalki.db.models.StageLog;
import org.junit.jupiter.api.Test;

import java.util.List;

import edu.cmu.sei.kalki.db.models.*;

public class StageLogTest extends AUsesDatabase {
    private Device device;
    private DeviceType deviceType;
    private SecurityState securityState;
    private DeviceSecurityState deviceSecurityState;
//    private StageLog log;
    /*
        Test StageLog Actions
     */

    @Test
    public void testFindStageLog() {
        StageLog log = new StageLog(deviceSecurityState.getId(), StageLog.Action.INCREASE_SAMPLE_RATE, StageLog.Stage.TRIGGER, "Info");
        log.insert();

        StageLog log1 = Postgres.findStageLog(log.getId());
        assertNotNull(log1);
        assertEquals(log.toString(), log1.toString());
    }

    @Test
    public void testFindAllStageLogs() {
        StageLog log = new StageLog(deviceSecurityState.getId(), StageLog.Action.SEND_COMMAND, StageLog.Stage.REACT, "Info");
        log.insert();

        List<StageLog> stageLogList = Postgres.findAllStageLogs();
        assertNotEquals(0, stageLogList.size());
    }

    @Test
    public void testFindAllStageLogsForDevice() {
        StageLog log = new StageLog(deviceSecurityState.getId(), StageLog.Action.DEPLOY_UMBOX, StageLog.Stage.FINISH, "Info");
        log.insert();

        List<StageLog> stageLogList = Postgres.findAllStageLogsForDevice(device.getId());
        assertNotEquals(0, stageLogList.size());
    }

    public void insertData() {
        deviceType = new DeviceType(-1, "Test Device Type");
        deviceType.insert();


        securityState = new SecurityState(-1, "Normal");
        securityState.insert();

        device = new Device("Device Name", "Dev Description", deviceType, "ip", 1, 1);
        device.insert();

        deviceSecurityState = new DeviceSecurityState(device.getId(), securityState.getId());
        deviceSecurityState.insert();

    }
}
