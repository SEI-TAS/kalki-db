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
import edu.cmu.sei.kalki.db.models.DeviceStatus;
import edu.cmu.sei.kalki.db.models.UmboxInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO extends DAO
{
    /**
     * Extract an Alert from the result set of a database query.
     *
     * @param rs ResultSet from a Alert query.
     * @return The Alert that was found.
     */
    public static Alert createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        Timestamp timestamp = rs.getTimestamp("timestamp");
        String alerterId = rs.getString("alerter_id");
        int deviceStatusId = rs.getInt("device_status_id");
        int alertTypeId = rs.getInt("alert_type_id");
        int deviceId = rs.getInt("device_id");
        String info = rs.getString("info");
        return new Alert(id, name, timestamp, alerterId, deviceId, deviceStatusId, alertTypeId, info);
    }

    /**
     * Finds an Alert from the database with the given id.
     */
    public static Alert findAlert(int id) {
        return (Alert) findObjectByIdAndTable(id, "alert", AlertDAO.class);
    }

    /**
     * Finds all Alerts from the database for the given list of UmboxInstance alerterIds.
     *
     * @param alerterIds a list of alerterIds of UmboxInstances.
     * @return a list of all Alerts in the database where the the alert was created by a UmboxInstance with
     * alerterId in alerterIds.
     */
    public static List<Alert> findAlerts(List<String> alerterIds) {
        return (List<Alert>) findObjectsByStringIds(alerterIds, "alert", "alerter_id", AlertDAO.class);
    }

    /**
     * Finds all Alerts from the database for the given deviceId.
     *
     * @param id of the device
     * @return a list of all Alerts in the database associated to the device with the given id
     */
    public static List<Alert> findAlertsByDevice(int deviceId) {
        List<Integer> deviceIds = new ArrayList<>();
        deviceIds.add(deviceId);
        return (List<Alert>) findObjectsByIntIds(deviceIds, "alert", "device_id", AlertDAO.class);
    }

    /**
     * Finds all Alerts from the database for the given deviceId and type.
     *
     * @param deviceId of the device
     * @param alertTypeId type of alerts to look for
     * @return a list of all Alerts in the database associated to the device with the given id and type.
     */
    public static List<Alert> findAlertsByDeviceAndType(int deviceId, int alertTypeId) {
        List<Integer> deviceIds = new ArrayList<>();
        deviceIds.add(deviceId);
        return (List<Alert>) findObjectsByIntIds(deviceIds, "alert", "device_id", AlertDAO.class);
    }


    /**
     * Finds the last alerts for a device and type given a timeframe.
     *
     * @param deviceId the id of the device
     * @param alertType the type of alerts to find.
     * @param startingTime The timestamp to start
     * @param period The amount of time back to search
     * @param timeUnit the unit of time to use (minute(s), hour(s), day(s))
     * @return a list of N device statuses
     */
    public static List<Alert> findAlertsOverTime(int deviceId, int alertTypeId, Timestamp startingTime, int period, String timeUnit) {
        String interval = period + " " + timeUnit;
        List<Alert> alerts = new ArrayList<>();
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT * FROM alert WHERE device_id = ? AND alert_type_id = ? AND timestamp between (?::timestamp - (?::interval)) and ?::timestamp")) {
            st.setInt(1, deviceId);
            st.setInt(2, alertTypeId);
            st.setTimestamp(3, startingTime);
            st.setString(4, interval);
            st.setTimestamp(5, startingTime);
            logger.info("Parameter count: " + st.getParameterMetaData().getParameterCount());
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    alerts.add(createFromRs(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all device statuses: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return alerts;
    }

    /**
     * Insert a row into the alert table
     * Will insert the alert with either an alerterId or deviceStatusId, but not both
     *
     * @param alert The Alert to be added
     * @return id of new Alert on success. -1 on error
     */
    public static Integer insertAlert(Alert alert) {
        logger.info("Inserting alert: " + alert.toString());

        try {
            int deviceId = alert.getDeviceId();
            if(alert.getDeviceStatusId() == 0) {
                if(deviceId == 0) {
                    UmboxInstance instance = UmboxInstanceDAO.findUmboxInstance(alert.getAlerterId());
                    alert.setDeviceId(instance.getDeviceId());
                }

                try(Connection con = Postgres.getConnection();
                    PreparedStatement st = con.prepareStatement("INSERT INTO alert(name, timestamp, alert_type_id, device_id, alerter_id, info) VALUES (?,?,?,?,?,?) RETURNING id")) {
                    st.setString(1, alert.getName());
                    st.setTimestamp(2, alert.getTimestamp());
                    st.setInt(3, alert.getAlertTypeId());
                    st.setInt(4, alert.getDeviceId());
                    st.setString(5, alert.getAlerterId());
                    st.setString(6, alert.getInfo());
                    st.execute();
                    return getLatestId(st);
                }
            }
            else {
                if(deviceId == 0) {
                    DeviceStatus deviceStatus = DeviceStatusDAO.findDeviceStatus(alert.getDeviceStatusId());
                    alert.setDeviceId(deviceStatus.getDeviceId());
                }

                try(Connection con = Postgres.getConnection();
                    PreparedStatement st = con.prepareStatement("INSERT INTO alert(name, timestamp, alert_type_id, alerter_id, device_id, device_status_id, info) VALUES (?,?,?,?,?,?,?) RETURNING id")) {
                    st.setString(1, alert.getName());
                    st.setTimestamp(2, alert.getTimestamp());
                    st.setInt(3, alert.getAlertTypeId());
                    st.setString(4, alert.getAlerterId());
                    st.setInt(5, alert.getDeviceId());
                    st.setInt(6, alert.getDeviceStatusId());
                    st.setString(7, alert.getInfo());
                    st.execute();
                    return getLatestId(st);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting Alert: " + alert.toString() + " " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates Alert with given id to have the parameters of the given Alert.
     *
     * @param alert Alert holding new parameters to be saved in the database.
     * @return the id of the updated Alert on success. -1 on failure
     */
    public static Integer updateAlert(Alert alert) {
        logger.info(String.format("Updating Alert with id = %d with values: %s", alert.getId(), alert));

        try {
            if (alert.getDeviceStatusId() == 0) {
                try(Connection con = Postgres.getConnection();
                    PreparedStatement st = con.prepareStatement("UPDATE alert " +
                        "SET name = ?, timestamp = ?, alerter_id = ?, device_id = ?, alert_type_id = ?, info = ? " +
                        "WHERE id = ?")) {
                    st.setString(1, alert.getName());
                    st.setTimestamp(2, alert.getTimestamp());
                    st.setString(3, alert.getAlerterId());
                    st.setInt(4, alert.getDeviceId());
                    st.setInt(5, alert.getAlertTypeId());
                    st.setString(6, alert.getInfo());
                    st.setInt(7, alert.getId());
                    st.executeUpdate();
                }
            } else {
                try(Connection con = Postgres.getConnection();
                    PreparedStatement st = con.prepareStatement("UPDATE alert " +
                        "SET name = ?, timestamp = ?, alerter_id = ?, device_status_id = ?, device_id = ?, alert_type_id = ?, info = ?" +
                        "WHERE id = ?")) {
                    st.setString(1, alert.getName());
                    st.setTimestamp(2, alert.getTimestamp());
                    st.setString(3, alert.getAlerterId());
                    st.setInt(4, alert.getDeviceStatusId());
                    st.setInt(5, alert.getDeviceId());
                    st.setInt(6, alert.getAlertTypeId());
                    st.setString(7, alert.getInfo());
                    st.setInt(8, alert.getId());
                    st.executeUpdate();
                }
            }

            return alert.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating Alert: " + e.getClass().toString() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Deletes an Alert by its id.
     *
     * @param id id of the Alert to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteAlert(int id) {
        return deleteById("alert", id);
    }

}
