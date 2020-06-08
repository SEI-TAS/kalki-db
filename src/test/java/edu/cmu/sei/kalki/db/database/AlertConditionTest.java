package edu.cmu.sei.kalki.db.database;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.cmu.sei.kalki.db.daos.AlertConditionDAO;
import edu.cmu.sei.kalki.db.models.AlertContext;
import edu.cmu.sei.kalki.db.daos.AlertContextDAO;
import edu.cmu.sei.kalki.db.models.AlertCondition;
import edu.cmu.sei.kalki.db.models.AlertType;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import edu.cmu.sei.kalki.db.models.DataNode;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.DeviceSensor;
import edu.cmu.sei.kalki.db.models.SecurityState;

public class AlertConditionTest extends AUsesDatabase {
    private static DeviceType deviceType;
    private static DeviceSensor deviceSensor;
    private static Device device;
    private static AlertType alertType;
    private static AlertTypeLookup alertTypeLookup;
    private static AlertContext alertContext;

    /*
        Alert Condition Action Tests
     */

    @Test
    public void testInsertAlertCondition() {
        AlertCondition alertCondition = new AlertCondition(device.getId(), deviceSensor.getId(), deviceSensor.getName(), 1, AlertCondition.ComparisonOperator.EQUAL, AlertCondition.Calculation.AVERAGE, 1, "");
        alertCondition.insert();
        Assertions.assertNotEquals(-1, alertCondition.getId());
    }

    @Test
    public void testUpdateAlertCondition() {
        AlertCondition alertCondition = new AlertCondition(device.getId(), deviceSensor.getId(), deviceSensor.getName(), 1, AlertCondition.ComparisonOperator.EQUAL, AlertCondition.Calculation.AVERAGE, 1, "");
        alertCondition.insert();

        String newThrehsold = "test";
        alertCondition.setThresholdValue(newThrehsold);

        AlertConditionDAO.updateAlertCondition(alertCondition);


        AlertCondition test = AlertConditionDAO.findAlertCondition(alertCondition.getId());
        Assertions.assertEquals(newThrehsold, test.getThresholdValue());

    }

    @Test
    public void testFindAlertCondition() {
        AlertCondition alertCondition = new AlertCondition(device.getId(), deviceSensor.getId(), deviceSensor.getName(), 1, AlertCondition.ComparisonOperator.EQUAL, AlertCondition.Calculation.AVERAGE, 1, "");
        alertCondition.insert();

        AlertCondition ac = AlertConditionDAO.findAlertCondition(alertCondition.getId());
        Assertions.assertEquals(alertCondition.toString(), ac.toString());
    }

    @Test
    public void testFindAlertConditionsForContext() {
        AlertCondition alertCondition = new AlertCondition(device.getId(), deviceSensor.getId(), deviceSensor.getName(), 1, AlertCondition.ComparisonOperator.EQUAL, AlertCondition.Calculation.AVERAGE, 1, "");
        alertCondition.insert();

        alertContext.addCondition(alertCondition);
        AlertContextDAO.updateAlertCircumstance(alertContext);

        List<AlertCondition> testList = AlertConditionDAO.findAlertConditionsForContext(alertContext.getId());

        Assertions.assertEquals(1, testList.size());
        Assertions.assertEquals(alertCondition.toString(), testList.get(0).toString());
    }

    @Test
    public void testInsertOrUpdateAlertCondition() {
        AlertCondition alertCondition = new AlertCondition(device.getId(), deviceSensor.getId(), deviceSensor.getName(), 1, AlertCondition.ComparisonOperator.EQUAL, AlertCondition.Calculation.AVERAGE, 1, "");
        alertCondition.insertOrUpdate();
        int insertId = alertCondition.getId();

        Assertions.assertEquals(1, insertId);

        alertCondition.setThresholdValue("testing");
        alertCondition.insertOrUpdate();
        int updateId = alertCondition.getId();

        Assertions.assertEquals(insertId, updateId);
        Assertions.assertEquals("testing", alertCondition.getThresholdValue());
    }

    public void insertData() {
        // insert normal security state
        SecurityState normal = new SecurityState("Normal");
        normal.insert();

        // insert device_type
        deviceType = new DeviceType(0, "test device type");
        deviceType.insert();

        deviceSensor = new DeviceSensor("test sensor", deviceType.getId());
        deviceSensor.insert();

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1, dataNode);
        device.insert();

        // insert alert_type unts-temperature
        alertType = new AlertType("UNTS-Temperature", "test alert type", "IoT Monitor");
        alertType.insert();

        // insert into alert_type_lookup
        alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId());
        alertTypeLookup.insert();

        // insert into alert_context
        alertContext = new AlertContext(device.getId(), alertTypeLookup.getId(), AlertContext.LogicalOperator.NONE);
        alertContext.insert();

    }
}
