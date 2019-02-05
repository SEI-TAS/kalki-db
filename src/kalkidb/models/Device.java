package kalkidb.models;
import kalkidb.database.Postgres;
import java.util.concurrent.CompletionStage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Map;


public class Device {

    private int id;
    private String name;
    private String description;
    private int typeId;
    private int groupId;
    private String ip;
    private int statusHistorySize;
    private int samplingRate;
    private List<Integer> tagIds;
    private SecurityState currentState;
    private AlertHistory lastAlert;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public Device() {

    }

    public Device(String name, String description, int typeId, int groupId, String ip, int statusHistorySize, int samplingRate){
        this.name = name;
        this.description = description;
        this.typeId = typeId;
        this.groupId = groupId;
        this.ip = ip;
        this.statusHistorySize = statusHistorySize;
        this.samplingRate = samplingRate;
    }

    public Device(int id, String name, String description, int typeId, int groupId, String ip,
                  int statusHistorySize, int samplingRate) {
        this.id = id;
        this.description = description;
        this.name = name;
        this.typeId = typeId;
        this.groupId = groupId;
        this.statusHistorySize = statusHistorySize;
        this.samplingRate = samplingRate;
        this.ip = ip;
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

    public int getStatusHistorySize() {
        return statusHistorySize;
    }

    public void setStatusHistorySize(int statusHistorySize) {
        this.statusHistorySize = statusHistorySize;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
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

    public SecurityState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(SecurityState state){
        this.currentState = state;
    }

    public AlertHistory getLastAlert() {
        return lastAlert;
    }

    public void setLastAlert(AlertHistory lastAlert) {
        this.lastAlert = lastAlert;
    }

    public void insert(){
        Postgres.insertDevice(this).thenApplyAsync(id -> {
            this.id = id;
            return id;
        });
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

    public CompletionStage<List<DeviceStatus>> lastNSamples(int N){
        return Postgres.findNDeviceStatuses(this.id, N);
    }

    public CompletionStage<List<DeviceStatus>> samplesOverTime(int length, String timeUnit){
        return Postgres.findDeviceStatusesOverTime(this.id, length, timeUnit);
    }

    public CompletionStage<Map<Device, DeviceStatus>> statusesOfSameType() { return Postgres.findDeviceStatusesByType(this.typeId); }
}
