package kalkidb.models;

import java.sql.Timestamp;
import kalkidb.database.Postgres;
import java.util.concurrent.CompletionStage;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class UmboxInstance {

    private int id;
    private String alerterId;
    private int umboxImageId;
    private Optional<Integer> deviceId;
    private Timestamp startedAt;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public UmboxInstance() {

    }

    public UmboxInstance(String alerterId, int umboxImageId, int deviceId){
        this.alerterId = alerterId;
        this.umboxImageId = umboxImageId;
        this.deviceId = Optional.of(deviceId);
        long millis = System.currentTimeMillis();
        this.startedAt = new Timestamp(millis);
    }

    public UmboxInstance(String alerterId, int umboxImageId, int deviceId, Timestamp timestamp){
        this.alerterId = alerterId;
        this.umboxImageId = umboxImageId;
        this.deviceId = Optional.of(deviceId);
        this.startedAt = timestamp;
    }

    public UmboxInstance(int id, String alerterId, int umboxImageId, int deviceId, Timestamp startedAt) {
        this.id = id;
        this.alerterId = alerterId;
        this.umboxImageId = umboxImageId;
        this.deviceId = Optional.of(deviceId);
        this.startedAt = startedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAlerterId() {
        return alerterId;
    }

    public void setAlerterId(String alerterId) {
        this.alerterId = alerterId;
    }

    public int getUmboxImageId() { return umboxImageId; }

    public void setUmboxImageId(int umboxImageId) { this.umboxImageId = umboxImageId; }

    public int getDeviceId() {
        return deviceId.get();
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = Optional.of(deviceId);
    }

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    public CompletionStage<Integer> insert() {
        return Postgres.insertUmboxInstance(this).thenApplyAsync(id -> {
            this.id = id;
            return id;
        });
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad UmboxInstance";
        }
    }
}
