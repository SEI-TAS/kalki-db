package edu.cmu.sei.kalki.db.models;

import java.sql.Timestamp;

import edu.cmu.sei.kalki.db.daos.DeviceSecurityStateDAO;

public class DeviceSecurityState extends Model  {
    private int deviceId;
    private int stateId;
    private Timestamp timestamp;
    private String name;

    public DeviceSecurityState() {

    }

    public DeviceSecurityState(int deviceId, int stateId) {
        this.deviceId = deviceId;
        this.stateId = stateId;
        long millis = System.currentTimeMillis();
        this.timestamp = new Timestamp(millis);
    }

    public DeviceSecurityState(int deviceId, int stateId, String name) {
        this.deviceId = deviceId;
        this.stateId = stateId;
        this.name = name;
        long millis = System.currentTimeMillis();
        this.timestamp = new Timestamp(millis);
    }

    public DeviceSecurityState(int deviceId, int stateId, Timestamp timestamp, String name) {
        this.deviceId = deviceId;
        this.stateId = stateId;
        this.timestamp = timestamp;
        this.name = name;
    }

    public DeviceSecurityState(int id, int deviceId, int stateId, Timestamp timestamp, String name) {
        this.id = id;
        this.deviceId = deviceId;
        this.stateId = stateId;
        this.timestamp = timestamp;
        this.name = name;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int insert(){
        this.id = DeviceSecurityStateDAO.insertDeviceSecurityState(this);
        return this.id;
    }
}
