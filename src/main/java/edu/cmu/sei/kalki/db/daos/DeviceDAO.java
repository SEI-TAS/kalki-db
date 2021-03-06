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
package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.Alert;
import edu.cmu.sei.kalki.db.models.AlertType;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceSecurityState;
import edu.cmu.sei.kalki.db.models.SecurityState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

public class DeviceDAO extends DAO
{
    /**
     * Extract a Device from the result set of a database query.
     */
    public static Device createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        int typeId = rs.getInt("type_id");
        int groupId = rs.getInt("group_id");
        String ip = rs.getString("ip_address");
        int statusHistorySize = rs.getInt("status_history_size");
        int samplingRate = rs.getInt("sampling_rate");
        int defaultSamplingRate = rs.getInt("default_sampling_rate");
        int dataNodeId = rs.getInt("data_node_id");
        String credentials = rs.getString("credentials");
        return new Device(id, name, description, typeId, groupId, ip, statusHistorySize, samplingRate, defaultSamplingRate, dataNodeId, credentials);
    }

    /**
     * Finds a Device from the database by its id.
     *
     * @param id id of the Device to find.
     * @return the Device if it exists in the database, else null.
     */
    public static Device findDevice(int id) {
        Device device = (Device) findObjectByIdAndTable(id, "device", DeviceDAO.class);
        if(device != null) {
            List<Integer> tagIds = TagDAO.findTagIds(device.getId());
            device.setTagIds(tagIds);

            DeviceSecurityState ss = DeviceSecurityStateDAO.findDeviceSecurityStateByDevice(device.getId());
            device.setCurrentState(ss);
        }
        return device;
    }

    /**
     * Finds all Devices in the database.
     *
     * @return a list of all Devices in the database.
     */
    public static List<Device> findAllDevices() {
        List<Device> devices = (List<Device>) findObjectsByTable("device", DeviceDAO.class);
        for(Device device : devices) {
            List<Integer> tagIds = TagDAO.findTagIds(device.getId());
            device.setTagIds(tagIds);
            DeviceSecurityState ss = DeviceSecurityStateDAO.findDeviceSecurityStateByDevice(device.getId());
            device.setCurrentState(ss);
        }
        return devices;
    }

    /**
     * Find devices in a given group
     *
     * @return a list of Devices with the given groupId
     * @parameter groupId the id of the group
     */
    public static List<Device> findDevicesByGroup(int groupId) {
        String query = "SELECT * FROM device WHERE group_id = ?";
        List<Device> devices = (List<Device>) findObjectsByIdAndQuery(groupId, query, DeviceDAO.class);
        for(Device device : devices) {
            DeviceSecurityState ss = DeviceSecurityStateDAO.findDeviceSecurityStateByDevice(device.getId());
            device.setCurrentState(ss);
        }
        return devices;
    }

    /**
     * Get a device given a device security state id.
     * @param dssId
     * @return
     */
    public static Device findDeviceByDeviceSecurityState(int dssId) {
        DeviceSecurityState deviceSecurityState = DeviceSecurityStateDAO.findDeviceSecurityState(dssId);
        if(deviceSecurityState != null) {
            Device device = findDevice(deviceSecurityState.getDeviceId());
            device.setCurrentState(deviceSecurityState);
            return device;
        }
        else {
            return null;
        }
    }

    /**
     * Finds the Device related to the given Alert id
     *
     * @param the Alert
     * @return the Device associated with the alert
     */
    public static Device findDeviceByAlert(Alert alert) {
        if (alert.getDeviceId() != null) {
            return findDevice(alert.getDeviceId());
        } else {
            logger.severe("Error: alert has no associated DeviceStatus OR UmboxInstance!");
            return null;
        }
    }

    /**
     * Finds the Devices related to the given DeviceType id
     *
     * @param int id of the type
     * @return the Devices associated with the type
     */
    public static List<Device> findDevicesByType(int deviceTypeId) {
        String query = "SELECT * FROM device WHERE type_id = ?";
        List<Device> deviceList = (List<Device>) findObjectsByIdAndQuery(deviceTypeId, query, DeviceDAO.class);
        for(Device device : deviceList) {
            device.setCurrentState(DeviceSecurityStateDAO.findDeviceSecurityStateByDevice(device.getId()));
        }
        return deviceList;
    }

    /**
     * Saves given Device to the database.
     *
     * @param device Device to be inserted.
     * @return auto incremented id
     */
    public static Device insertDevice(Device device) {
        logger.info("Inserting device: " + device);
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                ("INSERT INTO device(description, name, type_id, group_id, ip_address," +
                        "status_history_size, sampling_rate, default_sampling_rate, data_node_id, credentials) values(?,?,?,?,?,?,?,?,?,?) RETURNING id")) {
            st.setString(1, device.getDescription());
            st.setString(2, device.getName());
            st.setInt(3, device.getType().getId());
            if (device.getGroup() != null) {
                st.setInt(4, device.getGroup().getId());
            } else {
                st.setObject(4, null);
            }

            st.setString(5, device.getIp());
            st.setInt(6, device.getStatusHistorySize());
            st.setInt(7, device.getSamplingRate());
            st.setInt(8, device.getDefaultSamplingRate());
            st.setInt(9, device.getDataNode().getId());
            st.setString(10, device.getCredentials());
            st.execute();
            int serialNum = getLatestId(st);
            device.setId(serialNum);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting Device: " + e.getClass().getName() + ": " + e.getMessage());
        }

        DeviceSecurityState currentState = device.getCurrentState();

        if(currentState == null) {
            // Give the device a normal security state if it is not specified
            SecurityState securityState = SecurityStateDAO.findByName("Normal");
            if (securityState != null) {
                DeviceSecurityState normalDeviceState = new DeviceSecurityState(device.getId(), securityState.getId(), securityState.getName());
                normalDeviceState.insert();
                device.setCurrentState(normalDeviceState);
            } else {
                throw new NoSuchElementException("No normal security state has been added to the database");
            }

            // Insert tags into device_tag
            List<Integer> tagIds = device.getTagIds();
            if (tagIds != null) {
                for (int tagId : tagIds) {
                    Postgres.executeCommand(String.format("INSERT INTO device_tag(device_id, tag_id) values (%d,%d)", device.getId(), tagId));
                }
            }
        }

        return device;
    }

    /**
     * First, attempts to find the Device in the database.
     * If successful, updates the existing Device with the given Device's parameters Otherwise,
     * inserts the given Device.
     *
     * @param device Device to be inserted or updated.
     */
    public static Device insertOrUpdateDevice(Device device) {
        Device d = findDevice(device.getId());
        if (d == null) {
            return insertDevice(device);
        } else {
            return updateDevice(device);
        }
    }

    /**
     * Updates Device with given id to have the parameters of the given Device.
     *
     * @param device Device holding new parameters to be saved in the database.
     * @return the id of the updated device
     */
    public static Device updateDevice(Device device) {
        logger.info(String.format("Updating Device with id = %d with values: %s", device.getId(), device));

        // Delete existing tags
        Postgres.executeCommand(String.format("DELETE FROM device_tag WHERE device_id = %d", device.getId()));

        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE device " +
                "SET name = ?, description = ?, type_id = ?, group_id = ?, ip_address = ?, status_history_size = ?, sampling_rate = ?," +
                    " data_node_id = ?, credentials = ? " +
                "WHERE id = ?")) {
            st.setString(1, device.getName());
            st.setString(2, device.getDescription());
            st.setInt(3, device.getType().getId());
            if (device.getGroup() != null)
                st.setInt(4, device.getGroup().getId());
            else
                st.setObject(4, null);
            st.setString(5, device.getIp());
            st.setInt(6, device.getStatusHistorySize());
            st.setInt(7, device.getSamplingRate());
            st.setInt(8, device.getDataNode().getId());
            st.setString(9, device.getCredentials());

            st.setInt(10, device.getId());

            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating Device: " + e.getClass().getName() + ": " + e.getMessage());
        }

        // Insert tags into device_tag
        List<Integer> tagIds = device.getTagIds();
        if (tagIds != null) {
            for (int tagId : tagIds) {
                Postgres.executeCommand(String.format("INSERT INTO device_tag(device_id, tag_id) values (%d,%d)", device.getId(), tagId));
            }
        }
        return device;
    }

    /**
     * Deletes a Device by its id.
     *
     * @param id id of the Device to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteDevice(int id) {
        logger.info(String.format("Deleting device with id = %d", id));
        return deleteById("device", id);
    }

    /**
     * inserts a state reset alert for the given device id
     */
    public static void resetSecurityState(int deviceId) {
        logger.info("Inserting a state reset alert for device id: " +deviceId);
        AlertType stateResetAlertType = AlertTypeDAO.findAlertTypeByName("state-reset");
        if(stateResetAlertType != null) {
            Alert alert = new Alert(deviceId, stateResetAlertType.getName(), stateResetAlertType.getId(), "State reset");
            alert.insert();
        }
    }
    
}
