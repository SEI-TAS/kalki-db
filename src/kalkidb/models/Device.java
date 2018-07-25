package kalkidb.models;
import kalkidb.database.Postgres;
import java.util.concurrent.CompletionStage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;

public class Device {

    private int id;
    private String name;
    private String description;
    private int typeId;
    private int groupId;
    private String ip;
    private int historySize;
    private int samplingRate;
    private String policyFile;
    private List<Integer> tagIds;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public Device() {

    }

    public Device(int id, String name, String description, int typeId, int groupId, String ip,
                  int historySize, int samplingRate, String policyFile){
        this.id = id;
        this.description = description;
        this.name = name;
        this.typeId = typeId;
        this.groupId = groupId;
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

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
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

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public List<Integer> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Integer> tagIds) {
        this.tagIds = tagIds;
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
