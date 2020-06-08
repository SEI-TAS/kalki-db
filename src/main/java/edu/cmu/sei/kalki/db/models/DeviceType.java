package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DeviceTypeDAO;

import java.util.ArrayList;
import java.util.List;

public class DeviceType extends Model  {
    private String name;
    private List<DeviceSensor> sensors;

    public DeviceType() {

    }

    public DeviceType(String name, List<DeviceSensor> sensors) {
        this.name = name;
        this.sensors = sensors;
    }

    public DeviceType(String name) {
        this(name, new ArrayList<DeviceSensor>());
    }

    public DeviceType(int id, String name) {
        this(name, new ArrayList<DeviceSensor>());
        this.id = id;
    }

    public DeviceType(int id, String name, List<DeviceSensor> sensors) {
        this(name, sensors);
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DeviceSensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<DeviceSensor> sensors) {
        this.sensors = sensors;
    }

    public void addSensor(DeviceSensor sensor) {
        this.sensors.add(sensor);
    }

    public void removeSensor(DeviceSensor sensor) {
        this.sensors.remove(sensor);
    }

    public int insert() {
        DeviceTypeDAO.insertDeviceType(this);
        return this.id;
    }

    public void update() {
        DeviceTypeDAO.updateDeviceType(this);
    }

    public int insertOrUpdate() {
        DeviceTypeDAO.insertOrUpdateDeviceType(this);
        return this.id;
    }
}
