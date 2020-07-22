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
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceStatus;
import org.postgresql.util.HStoreConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceStatusDAO extends DAO
{
    /**
     * Extract a DeviceStatus from the result set of a database query.
     */
    public static DeviceStatus createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int deviceId = rs.getInt("device_id");
        Map<String, String> attributes = HStoreConverter.fromString(rs.getString("attributes"));
        Timestamp timestamp = rs.getTimestamp("timestamp");
        int statusId = rs.getInt("id");
        return new DeviceStatus(deviceId, attributes, timestamp, statusId);
    }
    
    /**
     * Finds a DeviceStatus from the database by its id.
     *
     * @param id id of the DeviceStatus to find.
     * @return the DeviceStatus if it exists in the database, else null.
     */
    public static DeviceStatus findDeviceStatus(int id) {
        return (DeviceStatus) findObjectByIdAndTable(id, "device_status", DeviceStatusDAO.class);
    }

    /**
     * Finds all DeviceStatuses from the database for the given device.
     *
     * @param deviceId the id of the device.
     * @return a list of all DeviceStatuses in the database where the device_id field is equal to deviceId.
     */
    public static List<DeviceStatus> findDeviceStatuses(int deviceId) {
        String query = "SELECT * FROM device_status WHERE device_id = ?";
        return (List<DeviceStatus>) findObjectsByIdAndQuery(deviceId, query, DeviceStatusDAO.class);
    }

    /**
     * Finds the last N DeviceStatuses for the given device
     *
     * @param deviceId the id of the device
     * @param N        the number of statuses to retrieve
     * @return a list of N device statuses
     */
    public static List<DeviceStatus> findNDeviceStatuses(int deviceId, int N) {
        logger.info("Finding last " + N + "device statuses for device: " + deviceId);
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT * FROM device_status WHERE device_id = ? ORDER BY id DESC LIMIT ?")) {
            st.setInt(1, deviceId);
            st.setInt(2, N);
            try(ResultSet rs = st.executeQuery()) {
                List<DeviceStatus> deviceHistories = new ArrayList<>();
                while (rs.next()) {
                    deviceHistories.add(createFromRs(rs));
                }
                return deviceHistories;
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all device statuses: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds numStatuses worth of device statuses where their id < startingId
     * @param deviceId
     * @param numStatuses
     * @param startingId
     * @return list of device statuses
     */
    public static List<DeviceStatus> findSubsetNDeviceStatuses(int deviceId, int numStatuses, int startingId) {
        logger.info("Finding "+numStatuses+" previous statuses from id: "+startingId);
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT * FROM device_status WHERE id < ? AND device_id = ? ORDER BY id DESC LIMIT ?")) {
            st.setInt(1, startingId);
            st.setInt(2, deviceId);
            st.setInt(3, numStatuses);
            try(ResultSet rs = st.executeQuery()) {
                List<DeviceStatus> deviceStatusList = new ArrayList<>();
                while (rs.next()) {
                    deviceStatusList.add(createFromRs(rs));
                }
                return deviceStatusList;
            }
        } catch(SQLException e) {
            logger.severe("SQL Exception getting subset of device statuses: "+e.getClass().getName()+": "+e.getMessage());
        }
        return null;
    }

    /**
     * Finds the last N DeviceStatuses for the given device
     *
     * @param deviceId the id of the device
     * @param startingTime The timestamp to start
     * @param period The amount of time back to search
     * @param timeUnit the unit of time to use (minute(s), hour(s), day(s))
     * @return a list of N device statuses
     */
    public static List<DeviceStatus> findDeviceStatusesOverTime(int deviceId, Timestamp startingTime, int period, String timeUnit) {
        String interval = period + " " + timeUnit;
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT * FROM device_status WHERE device_id = ? AND timestamp between (?::timestamp - (?::interval)) and ?::timestamp")) {
            st.setInt(1, deviceId);
            st.setTimestamp(2, startingTime);
            st.setString(3, interval);
            st.setTimestamp(4, startingTime);
            logger.info("Parameter count: " + st.getParameterMetaData().getParameterCount());
            try(ResultSet rs = st.executeQuery()) {
                List<DeviceStatus> deviceHistories = new ArrayList<>();
                while (rs.next()) {
                    deviceHistories.add(createFromRs(rs));
                }
                return deviceHistories;
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all device statuses: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a list of device statuses for devices with the given type id. One device status per device
     *
     * @param typeId The typeid for the requested devices
     * @return A map pairing a device with its most recent DeviceStatus
     */

    public static Map<Device, DeviceStatus> findDeviceStatusesByType(int typeId) {
        Map<Device, DeviceStatus> deviceStatusMap = new HashMap<>();
        List<Device> devices = DeviceDAO.findDevicesByType(typeId);
        for (Device device : devices) {
            List<DeviceStatus> statuses = findNDeviceStatuses(device.getId(), 1);
            for(DeviceStatus deviceStatus : statuses) {
                deviceStatusMap.put(device, deviceStatus);
            }
        }
        return deviceStatusMap;
    }

    /**
     * Returns a list of device statuses for devices with the given group id. One device status per device
     *
     * @param groupId The typeid for the requested devices
     * @return A map pairing a device with its most recent DeviceStatus
     */

    public static Map<Device, DeviceStatus> findDeviceStatusesByGroup(int groupId) {
        Map<Device, DeviceStatus> deviceStatusMap = new HashMap<>();
        List<Device> devices = DeviceDAO.findDevicesByGroup(groupId);
        for (Device device : devices) {
            List<DeviceStatus> statuses = findNDeviceStatuses(device.getId(), 1);
            for(DeviceStatus deviceStatus : statuses) {
                deviceStatusMap.put(device, deviceStatus);
            }
        }
        return deviceStatusMap;
    }

    /**
     * Finds all DeviceStatuses in the database.
     *
     * @return a list of all DeviceStatuses in the database.
     */
    public static List<DeviceStatus> findAllDeviceStatuses() {
        return (List<DeviceStatus>) findObjectsByTable("device_status", DeviceStatusDAO.class);
    }

    /**
     * Saves given DeviceStatus to the database.
     *
     * @param deviceStatus DeviceStatus to be inserted.
     * @return auto incremented id
     */
    public static Integer insertDeviceStatus(DeviceStatus deviceStatus) {
        logger.info("Inserting device_status: " + deviceStatus.toString());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                ("INSERT INTO device_status(device_id, timestamp, attributes) values(?,?,?) RETURNING id")) {
            st.setInt(1, deviceStatus.getDeviceId());
            st.setTimestamp(2, deviceStatus.getTimestamp());
            st.setObject(3, deviceStatus.getAttributes());
            st.execute();

            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting DeviceStatus: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * First, attempts to find the DeviceStatus in the database.
     * If successful, updates the existing DeviceStatus with the given DeviceStatus's parameters Otherwise,
     * inserts the given DeviceStatus.
     *
     * @param deviceStatus DeviceStatus to be inserted or updated.
     */
    public static Integer insertOrUpdateDeviceStatus(DeviceStatus deviceStatus) {
        DeviceStatus ds = findDeviceStatus(deviceStatus.getId());
        if (ds == null) {
            return insertDeviceStatus(deviceStatus);
        } else {
            return updateDeviceStatus(deviceStatus);
        }
    }

    /**
     * Updates DeviceStatus with given id to have the parameters of the given DeviceStatus.
     *
     * @param deviceStatus DeviceStatus holding new parameters to be saved in the database.
     */
    public static Integer updateDeviceStatus(DeviceStatus deviceStatus) {
        logger.info("Updating DeviceStatus with id=" + deviceStatus.getId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                ("UPDATE device_status SET device_id = ?, attributes = ?, timestamp = ? " +
                        "WHERE id=?")) {
            st.setInt(1, deviceStatus.getDeviceId());
            st.setObject(2, deviceStatus.getAttributes());
            st.setTimestamp(3, deviceStatus.getTimestamp());
            st.setInt(4, deviceStatus.getId());
            st.executeUpdate();
            return deviceStatus.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating DeviceStatus: " + e.getClass().getName() + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * Deletes an DeviceStatus by its id.
     *
     * @param id id of the DeviceStatus to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteDeviceStatus(int id) {
        return deleteById("device_status", id);
    }
    
}
