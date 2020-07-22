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
import edu.cmu.sei.kalki.db.models.DeviceSecurityState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DeviceSecurityStateDAO extends DAO
{
    /**
     * Extract a DeviceSecurityState from the result set of a database query.
     */
    public static DeviceSecurityState createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int deviceId = rs.getInt("device_id");
        int stateId = rs.getInt("state_id");
        Timestamp timestamp = rs.getTimestamp("timestamp");
        String name = rs.getString("name");
        return new DeviceSecurityState(id, deviceId, stateId, timestamp, name);
    }
    
    /**
     * Finds the DeviceSecurityState with the supplied id
     *
     * @param the id for the DeviceSecurityState
     * @return the DeviceSecurityState, if it exists
     */
    public static DeviceSecurityState findDeviceSecurityState(int id) {
        String query = "SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                "FROM device_security_state dss, security_state ss " +
                "WHERE dss.id=? AND dss.state_id = ss.id " +
                "ORDER BY timestamp DESC " +
                "LIMIT 1";
        return (DeviceSecurityState) findObjectByIdAndQuery(id, query, DeviceSecurityStateDAO.class);
    }

    /**
     * Returns all device security states in the DB.
     * @return
     */
    public static List<DeviceSecurityState> findAllDeviceSecurityStates() {
        String query = "SELECT dss.id, dss.device_id, dss.timestamp, dss.state_id, ss.name FROM device_security_state AS dss, security_state AS ss WHERE dss.state_id=ss.id";
        return (List<DeviceSecurityState>) findObjectsByQuery(query, DeviceSecurityStateDAO.class);
    }

    /**
     * Finds the most recent DeviceSecurityState from the database for the given device
     *
     * @param deviceId the id of the device
     * @return the most recent DeviceSecurityState entered for a device
     */
    public static DeviceSecurityState findDeviceSecurityStateByDevice(int deviceId) {
        String query = "SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                "FROM device_security_state dss, security_state ss " +
                "WHERE dss.device_id=? AND dss.state_id = ss.id " +
                "ORDER BY timestamp DESC " +
                "LIMIT 1";
        return (DeviceSecurityState) findObjectByIdAndQuery(deviceId, query, DeviceSecurityStateDAO.class);
    }

    /**
     * Finds the next to last device security state for a device.
     * @param device
     * @return
     */
    public static int findPreviousDeviceSecurityStateId(Device device) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT dss.state_id AS state_id " +
                "FROM device_security_state dss " +
                "WHERE dss.device_id=? AND dss.id < ? " +
                "ORDER BY dss.id DESC " +
                "LIMIT 1")) {
            st.setInt(1, device.getId());
            st.setInt(2, device.getCurrentState().getId());
            try(ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("state_id");
                } else {
                    logger.info("Only 1 device security state entered for device with id: " + device.getId());
                    return -1;
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding previous device security state for device: "+device.getId());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Finds all DeviceSecurityState from the database for the given device.
     *
     * @param deviceId the id of the device.
     * @return a list of all DeviceSecurityState in the database where the device_id field is equal to deviceId.
     */
    public static List<DeviceSecurityState> findDeviceSecurityStates(int deviceId) {
        String query = "SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                "FROM device_security_state dss, security_state ss " +
                "WHERE dss.device_id=? AND dss.state_id = ss.id " +
                "ORDER BY timestamp DESC";
        return (List<DeviceSecurityState>) findObjectsByIdAndQuery(deviceId, query, DeviceSecurityStateDAO.class);
    }

    /**
     * Saves given DeviceSecurityState to the database.
     *
     * @param deviceState DeviceSecurityState to be inserted.
     * @return The id of the new DeviceSecurityState if successful
     */
    public static Integer insertDeviceSecurityState(DeviceSecurityState deviceState) {
        logger.info("Inserting DeviceSecurityState");
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                ("INSERT INTO device_security_state(device_id, timestamp, state_id) " +
                        "values(?,?,?) " +
                        "RETURNING id")) {
            st.setInt(1, deviceState.getDeviceId());
            st.setTimestamp(2, deviceState.getTimestamp());
            st.setInt(3, deviceState.getStateId());
            try(ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting DeviceState: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Deletes a DeviceSecurityState by its id.
     *
     * @param id id of the DeviceSecurityState to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteDeviceSecurityState(int id) {
        return deleteById("device_security_state", id);
    }
    
}
