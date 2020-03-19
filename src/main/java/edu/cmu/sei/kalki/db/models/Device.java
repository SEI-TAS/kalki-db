package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DataNodeDAO;
import edu.cmu.sei.kalki.db.daos.DeviceDAO;
import edu.cmu.sei.kalki.db.daos.DeviceStatusDAO;
import edu.cmu.sei.kalki.db.daos.DeviceTypeDAO;
import edu.cmu.sei.kalki.db.daos.GroupDAO;

import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

public class Device extends Model {

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
    private DataNode dataNode;

    public Device() {

    }

    public Device(String name, String description, DeviceType type, String ip,
                  int statusHistorySize, int samplingRate, DataNode dataNode) {
        this(name, description, type, null, ip, statusHistorySize, samplingRate, samplingRate, null, null, dataNode);
    }

    public Device(String name, String description, DeviceType type, Group group, String ip,
                  int statusHistorySize, int samplingRate,int defaultSamplingRate, DeviceSecurityState currentState, Alert lastAlert, DataNode dataNode){
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
        this.dataNode = dataNode;
    }

    public Device(String name, String description, int typeId, int groupId, String ip,
                  int statusHistorySize, int samplingRate, int defaultSamplingRate, int dataNodeId){
        this(0, name, description, typeId, groupId, ip, statusHistorySize, samplingRate, defaultSamplingRate, dataNodeId);
    }

    public Device(int id, String name, String description, int typeId, int groupId, String ip,
                  int statusHistorySize, int samplingRate, int defaultSamplingRate, int dataNodeId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = DeviceTypeDAO.findDeviceType(typeId);
        this.group = GroupDAO.findGroup(groupId);
        this.ip = ip;
        this.statusHistorySize = statusHistorySize;
        this.samplingRate = samplingRate;
        this.defaultSamplingRate = defaultSamplingRate;
        this.dataNode = DataNodeDAO.findDataNode(dataNodeId);
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

    public DataNode getDataNode() {
        return dataNode;
    }

    public void setDataNode(DataNode dataNode) {
        this.dataNode = dataNode;
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

    public List<DeviceStatus> lastNSamples(int N){
        return DeviceStatusDAO.findNDeviceStatuses(this.id, N);
    }

    public List<DeviceStatus> samplesOverTime(Timestamp startingTime, int duration, String timeUnit){
        return DeviceStatusDAO.findDeviceStatusesOverTime(this.id, startingTime, duration, timeUnit);
    }

    public Map<Device, DeviceStatus> statusesOfSameType() { return DeviceStatusDAO.findDeviceStatusesByType(this.type.getId()); }

    public Map<Device, DeviceStatus> statusesOfSameGroup() { return DeviceStatusDAO.findDeviceStatusesByGroup(this.group.getId()); }
}
