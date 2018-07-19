package kalkidb.models;

public class UmboxContainer {

    private String umboxId;
    private String umboxName;
    private String device;
    private int startedAt;

    public String getUmboxId() {
        return umboxId;
    }

    public void setUmboxId(String umboxId) {
        this.umboxId = umboxId;
    }

    public String getUmboxName() {
        return umboxName;
    }

    public void setUmboxName(String umboxName) {
        this.umboxName = umboxName;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public int getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(int startedAt) {
        this.startedAt = startedAt;
    }
}