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
import edu.cmu.sei.kalki.db.models.AlertCondition;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import edu.cmu.sei.kalki.db.models.Device;
import org.postgresql.util.HStoreConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlertConditionDAO extends DAO
{
    /**
     * Extract an AlertCondition from the result set of a database query.
     */
    public static AlertCondition createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int deviceId = rs.getInt("device_id");
        String deviceName = rs.getString("device_name");
        int alertTypeLookupId = rs.getInt("alert_type_lookup_id");
        String alertTypeName = rs.getString("alert_type_name");
        Map<String, String> variables = null;
        if (rs.getString("variables") != null) {
            variables = HStoreConverter.fromString(rs.getString("variables"));
        }
        return new AlertCondition(id, deviceId, deviceName, alertTypeLookupId, alertTypeName, variables);
    }

    /**
     * Finds an AlertCondition from the database with the given id
     *
     * @param id The id of the desired AlertCondition
     * @return An AlertCondition with desired id
     */
    public static AlertCondition findAlertCondition(int id) {
        String query = "SELECT ac.*, d.name AS device_name, at.name AS alert_type_name " +
                "FROM alert_condition AS ac, device AS d, alert_type AS at, alert_type_lookup as atl " +
                "WHERE ac.id=? AND ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id";
        return (AlertCondition) findObjectByIdAndQuery(id, query, AlertConditionDAO.class);
    }

    /**
     * Finds all AlertConditions in the database
     *
     * @return a list of AlertCondition
     */
    public static List<AlertCondition> findAllAlertConditions() {
        String query = "SELECT ac.*, d.name AS device_name, at.name AS alert_type_name " +
                "FROM alert_condition AS ac, device AS d, alert_type AS at, alert_type_lookup as atl " +
                "WHERE ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id";
        return (List<AlertCondition>) findObjectsByQuery(query, AlertConditionDAO.class);
    }

    /**
     * Finds most recent AlertConditions from the database for the given device_id
     *
     * @param deviceId an id of a device
     * @return a list of all most recent AlertConditions in the database related to the given device
     */
    public static List<AlertCondition> findAlertConditionsByDevice(int deviceId) {
        String query = "SELECT DISTINCT ON (atl.id) alert_type_lookup_id, ac.id, ac.device_id, d.name AS device_name, at.name AS alert_type_name, ac.variables " +
                "FROM alert_condition AS ac, device AS d, alert_type AS at, alert_type_lookup AS atl " +
                "WHERE ac.device_id = ? AND ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id";
        return (List<AlertCondition>) findObjectsByIdAndQuery(deviceId, query, AlertConditionDAO.class);
    }

    /**
     * Insert a row into the AlertCondition table
     *
     * @param cond The AlertCondition to be added
     * @return id of new AlertCondition on success. -1 on error
     */
    public static Integer insertAlertCondition(AlertCondition cond) {
        logger.info("Inserting alert condition for device: " + cond.getDeviceId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO alert_condition(variables, device_id, alert_type_lookup_id) VALUES (?,?,?) RETURNING id")) {
            st.setObject(1, cond.getVariables());
            st.setInt(2, cond.getDeviceId());
            st.setInt(3, cond.getAlertTypeLookupId());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertCondition: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Insert row(s) into the AlertCondition table based on the given Device's type
     *
     * @param id the Id of the device
     * @return 1 on success. -1 on error
     */
    public static Integer insertAlertConditionForDevice(int id) {
        logger.info("Inserting alert conditions for device: " + id);

        Device d = DeviceDAO.findDevice(id);
        List<AlertTypeLookup> atlList = AlertTypeLookupDAO.findAlertTypeLookupsByDeviceType(d.getType().getId());
        for(AlertTypeLookup atl: atlList){
            AlertCondition ac = new AlertCondition(id, atl.getId(), atl.getVariables());
            ac.insert();
            if(ac.getId()<0) //insert failed
                return -1;
        }

        return 1;
    }

    /**
     * Insert row(s) into the AlertCondition table for devices in type specified on the AlertTypeLookup
     *
     * @param cond The AlertCondition to be added
     * @return id of new AlertCondition on success. -1 on error
     */
    public static Integer updateAlertConditionsForDeviceType(AlertTypeLookup alertTypeLookup) {
        logger.info("Inserting alert conditions for device type: " + alertTypeLookup.getDeviceTypeId());
        List<Device> deviceList = DeviceDAO.findDevicesByType(alertTypeLookup.getDeviceTypeId());
        if(deviceList != null) {
            for (Device d : deviceList) {
                AlertCondition alertCondition = new AlertCondition(d.getId(), alertTypeLookup.getId(), alertTypeLookup.getVariables());
                insertAlertCondition(alertCondition);
            }
        }
        return 1;
    }

    /**
     * Deletes an AlertCondition by its id.
     *
     * @param id id of the AlertCondition to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteAlertCondition(int id) {
        return deleteById("alert_condition", id);
    }

}
