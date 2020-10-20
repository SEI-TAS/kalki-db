package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DeviceSensorDAO;
import edu.cmu.sei.kalki.db.daos.DeviceTypeDAO;

public class DeviceSensor extends Model {
    private String name;
    private int typeId;

    public DeviceSensor() {}

    public DeviceSensor(String name){
        this.name = name;
    }

    public DeviceSensor(String name, int typeId) {
        this(name);
        this.typeId = typeId;
    }

    public DeviceSensor(int id, String name, int typeId) {
        this(name, typeId);
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int insert() {
        this.id = DeviceSensorDAO.insertDeviceSensor(this);
        return this.id;
    }

    public void update() {
        DeviceSensorDAO.updateDeviceSensor(this);
    }
}
