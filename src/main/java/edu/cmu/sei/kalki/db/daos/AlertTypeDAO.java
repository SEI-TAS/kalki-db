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
import edu.cmu.sei.kalki.db.models.AlertType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlertTypeDAO extends DAO
{
    /**
     * Extract an AlertType from the result set of a database query.
     * @param rs ResultSet from a AlertType query.
     * @return The AlertType that was found.
     */
    public static AlertType createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        String source = rs.getString("source");
        return new AlertType(id, name, description, source);
    }

    /**
     * Finds an AlertType from the database with the given id
     */
    public static AlertType findAlertType(int id) {
        return (AlertType) findObjectByIdAndTable(id, "alert_type", AlertTypeDAO.class);
    }

    /**
     * Finds an AlertType from the database with the given alert type name. Assumes there is only one per each name.
     */
    public static AlertType findAlertTypeByName(String name) {
        String query = "SELECT * FROM alert_type WHERE name = ?";
        return (AlertType) findObjectByStringAndQuery(name, query, AlertTypeDAO.class);
    }

    /**
     * Finds all AlertTypes from the database for the given type_id
     *
     * @param deviceTypeId an id of a DeviceType
     * @return a list of all AlertTypes in the database for the given DeviceType
     */
    public static List<AlertType> findAlertTypesByDeviceType(int deviceTypeId) {
        List<AlertType> alertTypeList = new ArrayList<>();
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT alert_type.id, alert_type.name, alert_type.description, alert_type.source " +
                "FROM alert_type, alert_type_lookup AS atl " +
                "WHERE alert_type.id = atl.alert_type_id AND atl.device_type_id = ?;")) {
            st.setInt(1, deviceTypeId);
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    alertTypeList.add(createFromRs(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all alert types: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return alertTypeList;
    }

    /**
     * Finds all AlertTypes in the database
     *
     * @return a list of AlertTypes
     */
    public static List<AlertType> findAllAlertTypes() {
        return (List<AlertType>) findObjectsByTable("alert_type", AlertTypeDAO.class);
    }

    /**
     * Insert a row into the AlertType table
     *
     * @param type The AlertType to be added
     * @return id of new AlertType on success. -1 on error
     */
    public static Integer insertAlertType(AlertType type) {
        logger.info("Inserting alert type: " + type.getName());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO alert_type(name, description, source) VALUES (?,?,?) RETURNING id")) {
            st.setString(1, type.getName());
            st.setString(2, type.getDescription());
            st.setString(3, type.getSource());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertType: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates provided AlertType
     *
     * @param type AlertType holding new values to be saved in the database.
     * @return the id of the updated Alert on success. -1 on failure
     */
    public static Integer updateAlertType(AlertType type) {
        logger.info(String.format("Updating AlertType with id = %d with values: %s", type.getId(), type));
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE alert_type " +
                "SET name = ?, description = ?, source = ?" +
                "WHERE id = ?")) {
            st.setString(1, type.getName());
            st.setString(2, type.getDescription());
            st.setString(3, type.getSource());
            st.setInt(4, type.getId());
            st.executeUpdate();

            return type.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating AlertType: " + e.getClass().toString() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * First, attempts to find the AlertType in the database.
     * If successful, updates the existing AlertType with the given AlertType's parameters Otherwise,
     * inserts the given AlertType.
     *
     * @param type AlertType to be inserted or updated.
     */
    public static Integer insertOrUpdateAlertType(AlertType type) {
        AlertType a = findAlertType(type.getId());
        if (a == null) {
            return insertAlertType(type);
        } else {
            return updateAlertType(type);
        }
    }

    /**
     * Deletes an AlertType by its id.
     *
     * @param id id of the AlertType to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteAlertType(int id) {
        return deleteById("alert_type", id);
    }
}
