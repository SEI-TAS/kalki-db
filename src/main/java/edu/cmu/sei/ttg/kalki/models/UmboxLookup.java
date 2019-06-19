package edu.cmu.sei.ttg.kalki.models;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.cmu.sei.ttg.kalki.database.Postgres;
import java.util.concurrent.CompletionStage;

public class UmboxLookup {

    private int id;
    private Integer stateId;
    private Integer deviceTypeId;
    private Integer umboxImageId;
    private Integer dagOrder;
    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public UmboxLookup() {

    }

    public UmboxLookup(int id, Integer stateId, Integer deviceTypeId, Integer umboxImageId, Integer dagOrder) {
        this.id = id;
        this.stateId = stateId;
        this.deviceTypeId = deviceTypeId;
        this.umboxImageId = umboxImageId;
        this.dagOrder = dagOrder;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getStateId() {
        return stateId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }

    public Integer getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(Integer deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public Integer getUmboxImageId() {
        return umboxImageId;
    }

    public void setUmboxImageId(Integer umboxImageId) {
        this.umboxImageId = umboxImageId;
    }

    public Integer getDagOrder() {
        return dagOrder;
    }

    public void setDagOrder(Integer dagOrder) {
        this.dagOrder = dagOrder;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad umbox lookup";
        }
    }

    public Integer insert() {
        return Postgres.insertUmboxLookup(this);
    }

    public Integer insertOrUpdate() { return Postgres.insertOrUpdateUmboxLookup(this); }
}
