/*
 * Kalki - A Software-Defined IoT Security Platform
 * Copyright 2020 Carnegie Mellon University.
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF ANY KIND WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 * Released under a MIT (SEI)-style license, please see license.txt or contact permission@sei.cmu.edu for full terms.
 * [DISTRIBUTION STATEMENT A] This material has been approved for public release and unlimited distribution.  Please see Copyright notice for non-US Government use and distribution.
 * This Software includes and/or makes use of the following Third-Party Software subject to its own license:
 * 1. Google Guava (https://github.com/google/guava) Copyright 2007 The Guava Authors.
 * 2. JSON.simple (https://code.google.com/archive/p/json-simple/) Copyright 2006-2009 Yidong Fang, Chris Nokleberg.
 * 3. JUnit (https://junit.org/junit5/docs/5.0.1/api/overview-summary.html) Copyright 2020 The JUnit Team.
 * 4. Play Framework (https://www.playframework.com/) Copyright 2020 Lightbend Inc..
 * 5. PostgreSQL (https://opensource.org/licenses/postgresql) Copyright 1996-2020 The PostgreSQL Global Development Group.
 * 6. Jackson (https://github.com/FasterXML/jackson-core) Copyright 2013 FasterXML.
 * 7. JSON (https://www.json.org/license.html) Copyright 2002 JSON.org.
 * 8. Apache Commons (https://commons.apache.org/) Copyright 2004 The Apache Software Foundation.
 * 9. RuleBook (https://github.com/deliveredtechnologies/rulebook/blob/develop/LICENSE.txt) Copyright 2020 Delivered Technologies.
 * 10. SLF4J (http://www.slf4j.org/license.html) Copyright 2004-2017 QOS.ch.
 * 11. Eclipse Jetty (https://www.eclipse.org/jetty/licenses.html) Copyright 1995-2020 Mort Bay Consulting Pty Ltd and others..
 * 12. Mockito (https://github.com/mockito/mockito/wiki/License) Copyright 2007 Mockito contributors.
 * 13. SubEtha SMTP (https://github.com/voodoodyne/subethasmtp) Copyright 2006-2007 SubEthaMail.org.
 * 14. JSch - Java Secure Channel (http://www.jcraft.com/jsch/) Copyright 2002-2015 Atsuhiko Yamanaka, JCraft,Inc. .
 * 15. ouimeaux (https://github.com/iancmcc/ouimeaux) Copyright 2014 Ian McCracken.
 * 16. Flask (https://github.com/pallets/flask) Copyright 2010 Pallets.
 * 17. Flask-RESTful (https://github.com/flask-restful/flask-restful) Copyright 2013 Twilio, Inc..
 * 18. libvirt-python (https://github.com/libvirt/libvirt-python) Copyright 2016 RedHat, Fedora project.
 * 19. Requests: HTTP for Humans (https://github.com/psf/requests) Copyright 2019 Kenneth Reitz.
 * 20. netifaces (https://github.com/al45tair/netifaces) Copyright 2007-2018 Alastair Houghton.
 * 21. ipaddress (https://github.com/phihag/ipaddress) Copyright 2001-2014 Python Software Foundation.
 * DM20-0543
 *
 */
package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DataNodeDAO;
import edu.cmu.sei.kalki.db.daos.DeviceDAO;
import edu.cmu.sei.kalki.db.daos.DeviceStatusDAO;
import edu.cmu.sei.kalki.db.daos.DeviceTypeDAO;
import edu.cmu.sei.kalki.db.daos.GroupDAO;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

public class Device extends Model {

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
    private String credentials;

    public Device() {

    }

    public Device(String name, String description, DeviceType type, String ip,
                  int statusHistorySize, int samplingRate, DataNode dataNode, String credentials) {
        this(name, description, type, null, ip, statusHistorySize, samplingRate, samplingRate, null, null, dataNode, credentials);
    }

    public Device(String name, String description, DeviceType type, Group group, String ip,
                  int statusHistorySize, int samplingRate,int defaultSamplingRate, DeviceSecurityState currentState, Alert lastAlert, DataNode dataNode, String credentials){
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
        this.credentials = credentials;
    }

    public Device(String name, String description, int typeId, int groupId, String ip,
                  int statusHistorySize, int samplingRate, int defaultSamplingRate, int dataNodeId, String credentials){
        this(0, name, description, typeId, groupId, ip, statusHistorySize, samplingRate, defaultSamplingRate, dataNodeId, credentials);
    }

    public Device(int id, String name, String description, int typeId, int groupId, String ip,
                  int statusHistorySize, int samplingRate, int defaultSamplingRate, int dataNodeId, String credentials) {
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
        this.credentials = credentials;
    }

    /**
     * Creates a Device from a JSON object
     * @param deviceData
     * @return a device object
     */
    public Device(JSONObject deviceData) {
        id = deviceData.getInt("id");
        name = deviceData.getString("name");
        description = deviceData.getString("description");
        type = deviceData.optJSONObject("type")!=null ? new DeviceType(deviceData.getJSONObject("type")):null;
        group = deviceData.optJSONObject("group")!=null ? new Group(deviceData.getJSONObject("group")):null;
        ip = deviceData.getString("ip");
        statusHistorySize = deviceData.getInt("statusHistorySize");
        samplingRate = deviceData.getInt("samplingRate");
        defaultSamplingRate = deviceData.getInt("defaultSamplingRate");
        currentState = deviceData.optJSONObject("currentState")!=null ? new DeviceSecurityState(deviceData.getJSONObject("currentState")):null;
        lastAlert = deviceData.optJSONObject("lastAlert")!=null ? new Alert(deviceData.getJSONObject("lastAlert")):null;
        dataNode = deviceData.optJSONObject("dataNode")!=null ? new DataNode(deviceData.getJSONObject("dataNode")):null;
        credentials = deviceData.optJSONObject("credentials")!=null ? deviceData.getString("credentials") :"";
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

    public int insert(){
        Device data = DeviceDAO.insertDevice(this);
        setCurrentState(data.getCurrentState());
        setId(data.getId());
        return this.id;
    }

    public int insertOrUpdate(){
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

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }
}
