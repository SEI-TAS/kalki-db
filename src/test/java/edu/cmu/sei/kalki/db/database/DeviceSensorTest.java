package edu.cmu.sei.kalki.db.database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import edu.cmu.sei.kalki.db.daos.DeviceSensorDAO;

import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.DeviceSensor;

public class DeviceSensorTest extends AUsesDatabase {
    private DeviceType deviceType;
    private DeviceSensor deviceSensor;

    @Test
    public void testInsertDeviceSensor() {
        DeviceSensor testSensor = new DeviceSensor("testing123", deviceType.getId());
        testSensor.insert();

        Assertions.assertEquals(2, testSensor.getId());
    }

    @Test
    public void testInsertDeviceSensorForDeviceType() {
        DeviceType testType = new DeviceType("Test inserting sensors");
        testType.insert();

        DeviceSensor testSensor = new DeviceSensor("testing", testType.getId());
        testType.addSensor(testSensor);

        DeviceType returnType = DeviceSensorDAO.insertDeviceSensorForDeviceType(testType);
        Assertions.assertEquals(testType.toString(), returnType.toString());
        Assertions.assertEquals(2, testSensor.getId());
    }

    @Test
    public void testFindDeviceSensor() {
        DeviceSensor testSensor = DeviceSensorDAO.findDeviceSensor(deviceSensor.getId());

        Assertions.assertEquals(deviceSensor.toString(), testSensor.toString());
    }

    @Test
    public void testFindSensorsForDeviceType(){
        List<DeviceSensor> sensorList = DeviceSensorDAO.findSensorsForDeviceType(deviceType.getId());

        Assertions.assertEquals(1, sensorList.size());

        DeviceSensor testSensor = sensorList.get(0);
        Assertions.assertEquals(deviceSensor.toString(), testSensor.toString());
    }

    @Test
    public void testUpdateDeviceSensorForDeviceTypeAdd() {
        DeviceSensor newSensor = new DeviceSensor("Sensor two", deviceType.getId());
        deviceType.addSensor(newSensor);

        DeviceSensorDAO.updateDeviceSensorForDeviceType(deviceType);

        List<DeviceSensor> sensorList = DeviceSensorDAO.findSensorsForDeviceType(deviceType.getId());
        Assertions.assertEquals(2, sensorList.size());
    }

    @Test
    public void testUpdateDeviceSensorForDeviceTypeRemove() {
        deviceType.removeSensor(deviceSensor);

        DeviceSensorDAO.updateDeviceSensorForDeviceType(deviceType);

        List<DeviceSensor> sensorList = DeviceSensorDAO.findSensorsForDeviceType(deviceType.getId());
        Assertions.assertEquals(0, sensorList.size());
    }

    @Test
    public void testUpdateDeviceSensorForDeviceTypeExchange() {
        DeviceSensor newSensor = new DeviceSensor("Sensor two", deviceType.getId());
        deviceType.addSensor(newSensor);
        deviceType.removeSensor((deviceSensor));

        DeviceSensorDAO.updateDeviceSensorForDeviceType(deviceType);

        List<DeviceSensor> sensorList = DeviceSensorDAO.findSensorsForDeviceType(deviceType.getId());
        Assertions.assertEquals(1, sensorList.size());
    }

    public void insertData() {
        deviceType = new DeviceType("Test Type");
        deviceType.insert();

        deviceSensor = new DeviceSensor("Test sensor", deviceType.getId());
        deviceSensor.insert();

        deviceType.addSensor(deviceSensor);
    }
}
