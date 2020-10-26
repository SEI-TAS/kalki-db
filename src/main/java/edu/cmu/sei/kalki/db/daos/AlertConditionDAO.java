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
import org.postgresql.util.PSQLException;

import java.sql.*;
import java.util.List;

public class AlertConditionDAO extends DAO {

    /**
     * Extract an AlertCondition from the result set of a database query
     */
    public static AlertCondition createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;

        int id = rs.getInt("id");
        int contextId = rs.getInt("context_id");
        int attributeId = rs.getInt("attribute_id");
        String attributeName = rs.getString("attribute_name");
        int numStatuses = rs.getInt("num_statuses");
        String compOperator = rs.getString("comparison_operator");
        String calculation = rs.getString("calculation");
        Integer thresholdId = 0;
        try {
            thresholdId = (Integer) rs.getObject("threshold_id");
        } catch (PSQLException e) { logger.info("No threshold_id on ResultSet"); }
        String thresholdValue = rs.getString("threshold_value");
        Integer deviceId = 0;
        try {
            deviceId = (Integer) rs.getObject("device_id");
        } catch (PSQLException e) { logger.info("No device_id on ResultSet"); }

        return new AlertCondition(id, contextId, attributeId, attributeName, numStatuses, compOperator, calculation, thresholdId, thresholdValue, deviceId);
    }

    /**
     * Finds an AlertCondition from the database with the given id
     */
    public static AlertCondition findAlertCondition(int id) {
        String query = "SELECT ac.*, ds.name AS attribute_name " +
                "FROM alert_condition AS ac, device_sensor as ds " +
                "WHERE ac.attribute_id = ds.id AND ac.id = ?";
        return (AlertCondition) findObjectByIdAndQuery(id, query, AlertConditionDAO.class);
    }

    /**
     * Finds all AlertConditions in the database
     *
     * @return a list of AlertCondition
     */
    public static List<AlertCondition> findAllAlertConditions() {
        String query = "SELECT ac.*, ds.name AS attribute_name " +
                "FROM alert_condition AS ac, device_sensor as ds " +
                "WHERE ac.attribute_id = ds.id " +
                "ORDER BY ac.id";
        return (List<AlertCondition>) findObjectsByQuery(query, AlertConditionDAO.class);
    }

    /**
     * Finds all AlertConditions for a specific AlertContext
     */
    public static List<AlertCondition> findAlertConditionsForContext(int contextId) {
        String query = "SELECT ac.*, ds.name AS attribute_name " +
                "FROM alert_condition AS ac, device_sensor as ds " +
                "WHERE ac.context_id = ? AND ac.attribute_id = ds.id " +
                "ORDER BY ac.id";
        return (List<AlertCondition>) findObjectsByIdAndQuery(contextId, query, AlertConditionDAO.class);
    }

    /**
     * Finds all AlertConditions for a specific device
     */
    public static List<AlertCondition> findAlertConditionsForDevice(int deviceId) {
        String query = "SELECT ac.*, ds.name AS attribute_name " +
                "FROM alert_condition AS ac, device_sensor as ds, device AS d " +
                "WHERE ac.attribute_id = ds.id AND ac.device_id = ? " +
                "ORDER BY ac.id";
        return (List<AlertCondition>) findObjectsByIdAndQuery(deviceId, query, AlertConditionDAO.class);
    }

    public static Boolean deleteAlertCondition(int id) {
        return deleteById("alert_condition", id);
    }

    /**
     * Insert a row into the AlertCondition table
     */
    public static AlertCondition insertAlertCondition(AlertCondition cond) {
        logger.info("Inserting alert condition");
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement(
                    "INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, " +
                            "calculation, threshold_id, threshold_value, device_id) VALUES(?,?,?,?,?,?,?,?) RETURNING  id")) {
            st.setInt(1, cond.getContextId());
            st.setInt(2, cond.getAttributeId());
            st.setInt(3, cond.getNumStatues());
            st.setString(4, cond.getCompOperator());
            st.setString(5, cond.getCalculation());
            st.setObject(6, cond.getThresholdId());
            st.setString(7, cond.getThresholdValue());
            st.setObject(8, cond.getDeviceId());
            st.execute();
            int id = getLatestId(st);
            cond.setId(id);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertCondition: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return cond;
    }

    /**
     * Updates a row in the AlertCondition table
     */
    public static AlertCondition updateAlertCondition(AlertCondition cond) {
        logger.info("Updating alert condition: "+cond.getId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE alert_condition " +
                    "SET context_id = ?, attribute_id = ?, num_statuses = ?, comparison_operator = ?, calculation = ?, threshold_id = ?, threshold_value = ?, device_id = ? " +
                    "WHERE id = ?")) {
            st.setInt(1, cond.getContextId());
            st.setInt(2, cond.getAttributeId());
            st.setInt(3, cond.getNumStatues());
            st.setString(4, cond.getCompOperator());
            st.setString(5, cond.getCalculation());
            st.setObject(6, cond.getAttributeId());
            st.setString(7, cond.getThresholdValue());
            st.setObject(8, cond.getDeviceId());
            st.setInt(9, cond.getId());
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating AlertCondition: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return cond;
    }

    /**
     * Inserts a row into AlertCondition table if it does not exist.
     * Otherwise it will update the corresponding row
     */
    public static AlertCondition insertOrUpdateAlertCondition(AlertCondition cond) {
        AlertCondition c = findAlertCondition(cond.getId());
        if(c == null)
            return insertAlertCondition(cond);
        else
            return updateAlertCondition(cond);
    }
}
