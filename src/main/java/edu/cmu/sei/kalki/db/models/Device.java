package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DeviceDAO;
import edu.cmu.sei.kalki.db.daos.DeviceStatusDAO;
import edu.cmu.sei.kalki.db.daos.DeviceTypeDAO;
import edu.cmu.sei.kalki.db.daos.GroupDAO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

public class Device {

    private int id;
    private String name;
    private String description;
    private DeviceType type;
    private Group group;
    private String ip;
    private int statusHistorySize;
    private int samplingRate;
    private int defaultSamplingRate;
    private List<Integer> tagIds;
    private DeviceSecurityState currentState;
    private Alert lastAlert;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public Device() {

    }

    public Device(String name, String description, DeviceType type, String ip, int statusHistorySize, int samplingRate) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.ip = ip;
        this.statusHistorySize = statusHistorySize;
        this.samplingRate = samplingRate;
        this.defaultSamplingRate = samplingRate;
    }

    public Device(String name, String description, DeviceType type, Group group, String ip, int statusHistorySize, int samplingRate,int defaultSamplingRate, DeviceSecurityState currentState, Alert lastAlert){
        this.name = name;
        this.description = description;
        this.type = type;
        this.group = group;
        this.ip = ip;
        this.statusHistorySize = statusHistorySize;
        this.samplingRate = samplingRate;
        this.defaultSamplingRate = defaultSamplingRate;
        this.currentState = currentState;
        this.lastAlert = lastAlert;
    }

    public Device(String name, String description, int typeId, int groupId, String ip, int statusHistorySize, int samplingRate, int defaultSamplingRate){
        this.name = name;
        this.description = description;
        try {
            this.type = DeviceTypeDAO.findDeviceType(typeId);
            this.group = GroupDAO.findGroup(groupId);
        } catch (Exception e) {
            System.out.println("ERROR initializing Device: "+name);
            e.printStackTrace();
        }
        this.ip = ip;
        this.statusHistorySize = statusHistorySize;
        this.samplingRate = samplingRate;
        this.defaultSamplingRate = defaultSamplingRate;
    }

    public Device(int id, String name, String description, int typeId, int groupId, String ip,
                  int statusHistorySize, int samplingRate, int defaultSamplingRate) {
        this.id = id;
        this.description = description;
        this.name = name;
        try {
            this.type = DeviceTypeDAO.findDeviceType(typeId);
            this.group = GroupDAO.findGroup(groupId);
        } catch (Exception e) {
            System.out.println("ERROR initializing Device: "+name);
            e.printStackTrace();
        }
        this.statusHistorySize = statusHistorySize;
        this.samplingRate = samplingRate;
        this.defaultSamplingRate = defaultSamplingRate;
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

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
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

    public Group getGroup() {
        return group;
    }

    public void setGroupId(Group group) {
        this.group = group;
    }

    public List<Integer> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Integer> tagIds) {
        this.tagIds = tagIds;
    }

    public DeviceSecurityState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(DeviceSecurityState state){
        this.currentState = state;
    }

    public Alert getLastAlert() {
        return lastAlert;
    }

    public void setLastAlert(Alert lastAlert) {
        this.lastAlert = lastAlert;
    }

    public int getDefaultSamplingRate() {
        return defaultSamplingRate;
    }

    public void setDefaultSamplingRate(int defaultSamplingRate) {
        this.defaultSamplingRate = defaultSamplingRate;
    }

    public Integer insert(){
        Device data = DeviceDAO.insertDevice(this);
        setCurrentState(data.getCurrentState());
        setId(data.getId());
        return this.id;
    }

    public Integer insertOrUpdate(){
        Device data = DeviceDAO.insertOrUpdateDevice(this);
        setCurrentState(data.getCurrentState());
        setId(data.getId());
        return this.id;
    }

    public void resetSecurityState() {
        DeviceDAO.resetSecurityState(this.id);
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad device";
        }
    }

    public List<DeviceStatus> lastNSamples(int N){
        return DeviceStatusDAO.findNDeviceStatuses(this.id, N);
    }

    public List<DeviceStatus> samplesOverTime(Timestamp startingTime, int duration, String timeUnit){
        return DeviceStatusDAO.findDeviceStatusesOverTime(this.id, startingTime, duration, timeUnit);
    }

    public Map<Device, DeviceStatus> statusesOfSameType() { return DeviceStatusDAO.findDeviceStatusesByType(this.type.getId()); }

    public Map<Device, DeviceStatus> statusesOfSameGroup() { return DeviceStatusDAO.findDeviceStatusesByGroup(this.group.getId()); }
}
