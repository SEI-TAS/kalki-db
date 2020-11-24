package edu.cmu.sei.kalki.db.database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.cmu.sei.kalki.db.daos.AlertContextDAO;
import edu.cmu.sei.kalki.db.models.AlertContext;
import edu.cmu.sei.kalki.db.models.AlertType;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import edu.cmu.sei.kalki.db.models.DataNode;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceType;

import java.util.ArrayList;

public class AlertContextTest extends AUsesDatabase {
    private static DeviceType deviceType;
    private static Device device;
    private static AlertTypeLookup alertTypeLookup;

    /*
        Alert Condition Action Tests
     */
    @Test
    public void testInsertAlertContext() {
        AlertContext alertContext = new AlertContext(alertTypeLookup.getId(), AlertContext.LogicalOperator.NONE);
        alertContext.insert();
        Assertions.assertNotEquals(-1, alertContext.getId());
        Assertions.assertNotEquals(0, alertContext.getId());
    }

    @Test
    public void testUpdateAlertCondition() {
        AlertContext alertContext = new AlertContext(alertTypeLookup.getId(), AlertContext.LogicalOperator.NONE);
        alertContext.insert();

        String newLogicalOperator = AlertContext.LogicalOperator.AND.convert();
        alertContext.setLogicalOperator(newLogicalOperator);

        AlertContextDAO.updateAlertContext(alertContext);

        AlertContext test = AlertContextDAO.findAlertContext(alertContext.getId());
        Assertions.assertEquals(newLogicalOperator, test.getLogicalOperator());
    }

    @Test
    public void testFindAlertContext() {
        AlertContext ac = new AlertContext(alertTypeLookup.getId(), AlertContext.LogicalOperator.NONE);
        ac.insert();
        AlertContext testAc = AlertContextDAO.findAlertContext(ac.getId());
        Assertions.assertEquals(ac.getId(), testAc.getId());
        Assertions.assertEquals(ac.getAlertTypeLookupId(), testAc.getAlertTypeLookupId());
        Assertions.assertEquals(ac.getLogicalOperator(), testAc.getLogicalOperator());
    }

    @Test
    public void testFindAlertContextsForDeviceType() {
        // Inserting it twice inserts two contexts.
        AlertContext alertContext = new AlertContext(alertTypeLookup.getId(), AlertContext.LogicalOperator.NONE);
        alertContext.insert();
        alertContext.insert();

        // Should not find this one.
        DeviceType deviceType2 = new DeviceType("test device type2");
        deviceType2.insert();
        AlertType alertType2 = new AlertType("ALERT2", "test alert type2", "IoT Interface");
        alertType2.insert();
        AlertTypeLookup alertTypeLookup2 = new AlertTypeLookup(alertType2.getId(), deviceType2.getId());
        alertTypeLookup2.insert();
        AlertContext alertContext3 = new AlertContext(alertTypeLookup2.getId(), AlertContext.LogicalOperator.AND);
        alertContext3.insert();

        ArrayList<AlertContext> acList = new ArrayList<AlertContext>(AlertContextDAO.findAlertContextsForDeviceType(deviceType.getId()));
        Assertions.assertEquals(2, acList.size());
        Assertions.assertEquals(alertContext.getAlertTypeLookupId(), acList.get(1).getAlertTypeLookupId());
        Assertions.assertEquals(alertContext.getLogicalOperator(), acList.get(1).getLogicalOperator());
    }

    public void insertData() {
        // insert device_type
        deviceType = new DeviceType("test device type");
        deviceType.insert();

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1, dataNode, "");
        device.insert();

        // insert alert_type unts-temperature
        AlertType alertType = new AlertType("UNTS-Temperature", "test alert type", "IoT Monitor");
        alertType.insert();

        // insert into alert_type_lookup
        alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId());
        alertTypeLookup.insert();
    }
}
