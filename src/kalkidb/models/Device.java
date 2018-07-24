package kalkidb.models;
import kalkidb.database.Postgres;
import java.util.concurrent.CompletionStage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Device {

    public int id;
    public String name;
    public String description;
    public String type;
    public String group;
    public String ip;
    public int historySize;
    public int samplingRate;
    public String policyFile;
    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public Device() {

    }

    public Device(int id, String name, String description, String type, String group, String ip,
                  int historySize, int samplingRate, String policyFile){
        this.id = id;
        this.description = description;
        this.name = name;
        this.type = type;
        this.group = group;
        this.historySize = historySize;
        this.samplingRate = samplingRate;
        this.ip = ip;
        this.policyFile = policyFile;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getHistorySize() {
        return historySize;
    }

    public void setHistorySize(int historySize) {
        this.historySize = historySize;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public String getPolicyFile() {
        return policyFile;
    }

    public void setPolicyFile(String policyFile) {
        this.policyFile = policyFile;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void insert(){
        Postgres.insertDevice(this);
    }

    public CompletionStage<Integer> insertOrUpdate(){
        return Postgres.insertOrUpdateDevice(this);
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad device";
        }
    }

}
