package edu.cmu.sei.ttg.kalki.models;

import java.sql.Timestamp;

public class StageLog {
    private int id;
    private int deviceSecurityStateId;
    private Timestamp timestamp;
    private String action;
    private String stage;
    private String info;

    public StageLog(){}

    public StageLog(int devSecStateId, String action, String stage) {
        this.deviceSecurityStateId = devSecStateId;
        this.action = action;
        this.stage = stage;
        this.timestamp = null;
        this.info = "";
    }

    public StageLog(int devSecStateId, String action, String stage, String info) {
        this(devSecStateId, action, stage);
        this.info = info;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getDeviceSecurityStateId() {
        return deviceSecurityStateId;
    }

    public void setDeviceSecurityStateId(int deviceSecurityStateId) {
        this.deviceSecurityStateId = deviceSecurityStateId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStage() {
        return stage;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
