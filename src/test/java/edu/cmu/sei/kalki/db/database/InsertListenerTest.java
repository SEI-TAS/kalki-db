package edu.cmu.sei.kalki.db.database;

import edu.cmu.sei.kalki.db.models.*;
import edu.cmu.sei.kalki.db.listeners.*;

import java.util.concurrent.TimeUnit;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InsertListenerTest extends AUsesDatabase {
    private InsertListener insertListener;
    private TestHandler testHandler;
    private DeviceType type;

    @Test
    public void testDeviceInsert() {
        insertListener.addHandler("deviceinsert", testHandler);
        Device d  = new Device("Test device", "test", type, "ip", 1,1);
        d.insert();

        sleep();

        assertEquals(true, testHandler.hasReceivedNotification());
    }

    @Test
    public void testDeviceUpdate() {
        insertListener.addHandler("deviceupdate", testHandler);
        Device d  = new Device("Test device", "test", type, "ip", 1,1);
        d.insert();
        d.setSamplingRate(2);
        d.insertOrUpdate();

        sleep();

        assertEquals(true, testHandler.hasReceivedNotification());
    }

    @Test
    public void testDeviceStatusInsert() {
        insertListener.addHandler("devicestatusinsert", testHandler);
        Device d  = new Device("Test device", "test", type, "ip", 1,1);
        d.insert();

        DeviceStatus deviceStatus = new DeviceStatus(d.getId(), new HashMap<>());
        deviceStatus.insert();

        sleep();

        assertEquals(true, testHandler.hasReceivedNotification());
    }

    @Test
    public void testAlertHistoryInsert() {
        insertListener.addHandler("alerthistoryinsert", testHandler);
        Device d  = new Device("Test device", "test", type, "ip", 1,1);
        d.insert();

        DeviceStatus deviceStatus = new DeviceStatus(d.getId(), new HashMap<>());
        deviceStatus.insert();

        AlertType alertType = new AlertType("Name", "Description", "Source");
        alertType.insert();

        Alert alert = new Alert(d.getId(), alertType.getName(), alertType.getId(), "Info");
        alert.insert();

        sleep();

        assertEquals(true, testHandler.hasReceivedNotification());
    }

    @Test
    public void testDeviceSecurityStateInsert() {
        insertListener.addHandler("devicesecuritystateinsert", testHandler);
        Device d  = new Device("Test device", "test", type, "ip", 1,1);
        d.insert(); //DSS is inserted when a new device is inserted

        sleep();

        assertEquals(true, testHandler.hasReceivedNotification());
    }

    @Test
    public void testPolicyRuleLogInsert() {
        insertListener.addHandler("policyruleloginsert", testHandler);
        Device d  = new Device("Test device", "test", type, "ip", 1,1);
        d.insert();

        StateTransition stateTransition = new StateTransition(1,2);
        stateTransition.insert();

        PolicyCondition policyCondition = new PolicyCondition(1, null);
        policyCondition.insert();

        PolicyRule policyRule = new PolicyRule(stateTransition.getId(), policyCondition.getId(), d.getType().getId(), 1);
        policyRule.insert();

        PolicyRuleLog policyRuleLog = new PolicyRuleLog(policyRule.getId(), d.getId());
        policyRuleLog.insert();

        sleep();

        assertEquals(true, testHandler.hasReceivedNotification());
    }

    @AfterEach
    public void stopListener() {
        insertListener.clearHandlers();
        insertListener.stopListening();
    }

    public void insertData() {
        type = new DeviceType(-1, "Test type");
        type.insert();
        insertListener.startListening();

        testHandler = new TestHandler();
    }

    private void sleep(){
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {}
    }

    class TestHandler implements InsertHandler {
        private boolean receivedNotification;

        public TestHandler() { receivedNotification = false; }

        public void handleNewInsertion(int newId){
            receivedNotification = true;
        }

        public boolean hasReceivedNotification() { return receivedNotification; }
    }
}
