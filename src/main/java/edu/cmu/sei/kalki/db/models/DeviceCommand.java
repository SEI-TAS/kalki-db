package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DeviceCommandDAO;

public class DeviceCommand extends Model  {
    private int id;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Integer getDeviceTypeId() {
        return deviceTypeId;
    }

    public int insert() {
        this.id = DeviceCommandDAO.insertCommand(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
        this.id = DeviceCommandDAO.insertOrUpdateCommand(this);
        return this.id;
    }
}
