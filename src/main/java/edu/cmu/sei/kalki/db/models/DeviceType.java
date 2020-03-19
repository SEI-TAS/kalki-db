package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DeviceTypeDAO;

public class DeviceType extends Model  {

    private int id;
    private String name;

    public DeviceType() {

    }

    public DeviceType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public DeviceType(String name) {
        this.name = name;
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

    public Integer insert() {
        this.id = DeviceTypeDAO.insertDeviceType(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
        this.id = DeviceTypeDAO.insertOrUpdateDeviceType(this);
        return this.id;
    }
}
