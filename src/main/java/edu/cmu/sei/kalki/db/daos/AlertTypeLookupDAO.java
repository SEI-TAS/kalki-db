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
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import org.postgresql.util.HStoreConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlertTypeLookupDAO extends DAO
{
    /**
     * Extract an AlertTypeLookup from the result set of a database query.
     */
    public static AlertTypeLookup createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int alertTypeId = rs.getInt("alert_type_id");
        int deviceTypeId = rs.getInt("device_type_id");
        Map<String, String> variables = null;
        if (rs.getString("variables") != null) {
            variables = HStoreConverter.fromString(rs.getString("variables"));
        }
        return new AlertTypeLookup(id, alertTypeId, deviceTypeId, variables);
    }

    /**
     * Returns the row from alert_type_lookup with the given id
     * @param id of the row
     */
    public static AlertTypeLookup findAlertTypeLookup(int id) {
        return (AlertTypeLookup) findObjectByIdAndTable(id, "alert_type_lookup", AlertTypeLookupDAO.class);
    }

    /**
     * Returns all rows from alert_type_lookup for the given device_type
     * @param typeId The device_type id
     */
    public static List<AlertTypeLookup> findAlertTypeLookupsByDeviceType(int typeId){
        List<AlertTypeLookup> atlList = new ArrayList<>();
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("Select * from alert_type_lookup WHERE device_type_id=?")) {
            st.setInt(1, typeId);
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    atlList.add(createFromRs(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all alert_type_lookups for the device type: "+typeId);
            e.printStackTrace();
        }
        return atlList;
    }

    /**
     * Returns all rows from the alert_type_lookup table
     * @return A list of AlertTypeLookups
     */
    public static List<AlertTypeLookup> findAllAlertTypeLookups() {
        return (List<AlertTypeLookup>) findObjectsByTable("alert_type_lookup", AlertTypeLookupDAO.class);
    }

    /**
     * Inserts the given AlertTypeLookup into the alert_type_lookup table
     * @param atl
     * @return The id of the new AlertTypeLookup. -1 on failure
     */
    public static int insertAlertTypeLookup(AlertTypeLookup atl){
        logger.info("Inserting AlertTypeLookup: " + atl.toString());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES (?,?,?) RETURNING id")) {
            st.setInt(1, atl.getAlertTypeId());
            st.setInt(2, atl.getDeviceTypeId());
            st.setObject(3, atl.getVariables());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertTypeLookup: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates the row for the given AlertTypeLookup
     * @param atl The object with new values for the row
     * @return The id of the AlertTypeLookup. -1 on failure
     */
    public static int updateAlertTypeLookup(AlertTypeLookup atl){
        logger.info("Updating AlertTypeLookup; atlId: " +atl.getId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE alert_type_lookup SET alert_type_id = ?, device_type_id = ?, variables = ? WHERE id = ?")) {
            st.setInt(1, atl.getAlertTypeId());
            st.setInt(2, atl.getDeviceTypeId());
            st.setObject(3, atl.getVariables());
            st.setInt(4, atl.getId());
            st.executeUpdate();

            return atl.getId();
        } catch (SQLException e) {
            logger.severe("Error updating AlertTypeLookup: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Checks if row exists in alert_type_lookup for the given AlertTypeLookup
     * If there is now row, insert it. Otherwise update the row
     * @param alertTypeLookup
     * @return
     */
    public static int insertOrUpdateAlertTypeLookup(AlertTypeLookup alertTypeLookup) {
        AlertTypeLookup atl = findAlertTypeLookup(alertTypeLookup.getId());
        if(atl == null){
            return insertAlertTypeLookup(alertTypeLookup);
        } else {
            return updateAlertTypeLookup(alertTypeLookup);
        }

    }
    /**
     * Deletes an AlertTypeLookup by its id.
     *
     * @param id id of the AlertTypeLookup to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteAlertTypeLookup(int id) {
        logger.info(String.format("Deleting alert_type_lookup with id = %d", id));
        return deleteById("alert_type_lookup", id);
    }
    
}
