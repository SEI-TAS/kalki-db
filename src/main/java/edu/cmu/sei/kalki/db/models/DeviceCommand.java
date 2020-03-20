package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DeviceCommandDAO;

public class DeviceCommand extends Model  {
    private String name;
    private Integer deviceTypeId;

    public DeviceCommand() {
    }

    public DeviceCommand(String name, Integer deviceTypeId) {
        this.name = name;
        this.deviceTypeId = deviceTypeId;
    }

    public DeviceCommand(int id, String name, Integer deviceTypeId) {
        this.id = id;
        this.name = name;
        this.deviceTypeId = deviceTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDeviceTypeId(Integer deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public int insert() {
        this.id = DeviceCommandDAO.insertCommand(this);
        return this.id;
    }

    public int insertOrUpdate() {
        this.id = DeviceCommandDAO.insertOrUpdateCommand(this);
        return this.id;
    }
}
