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
import edu.cmu.sei.kalki.db.models.UmboxLookup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UmboxLookupDAO extends DAO
{
    /**
     * Extract a UmboxLookup from the result set of a database query.
     */
    public static UmboxLookup createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int securityStateId = rs.getInt("security_state_id");
        int devcieTypeId = rs.getInt("device_type_id");
        int umboxImageId = rs.getInt("umbox_image_id");
        int dagOrder = rs.getInt("dag_order");
        return new UmboxLookup(id, securityStateId, devcieTypeId, umboxImageId, dagOrder);
    }

    /**
     * Finds a UmboxLookup from the database by its id.
     *
     * @param id id of the UmboxLookup to find.
     * @return the UmboxLookup if it exists in the database, else null.
     */
    public static UmboxLookup findUmboxLookup(int id) {
        return (UmboxLookup) findObjectByIdAndTable(id, "umbox_lookup", UmboxLookupDAO.class);
    }

    /**
     * Finds all umbox lookups based on the given device id
     */
    public static List<UmboxLookup> findUmboxLookupsByDevice(int deviceId) {
        /*String query = "SELECT ul.* FROM umbox_lookup ul, device d, device_type dt " +
                "WHERE ul.device_type_id = dt.id AND dt.id = d.type_id AND d.id = ?";
        return (List<UmboxLookup>) findObjectsByIdAndQuery(deviceId, query, UmboxLookupDAO.class);*/
        String query = "SELECT * FROM umbox_lookup WHERE device_type_id = " + deviceId;
        return (List<UmboxLookup>) findObjectsByQuery(query, UmboxLookupDAO.class);
    }

    /**
     * Finds all umboxLookup entries
     */
    public static List<UmboxLookup> findAllUmboxLookups() {
        return (List<UmboxLookup>) findObjectsByTable("umbox_lookup", UmboxLookupDAO.class);
    }

    /**
     * Adds the desired UmboxLookup to the database
     */
    public static Integer insertUmboxLookup(UmboxLookup ul) {
        logger.info("Adding umbox lookup: ");
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO umbox_lookup (security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (?,?,?,?) RETURNING id")) {
            st.setInt(1, ul.getSecurityStateId());
            st.setInt(2, ul.getDeviceTypeId());
            st.setInt(3, ul.getUmboxImageId());
            st.setInt(4, ul.getDagOrder());
            st.execute();
            return getLatestId(st);
        }
        catch (SQLException e){
            e.printStackTrace();
            logger.severe("SQL exception adding umbox lookup: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Edit desired UmboxLookup
     */
    public static Integer updateUmboxLookup(UmboxLookup ul) {
        logger.info(String.format("Updating UmboxLookup with id = %d with values: %s", ul.getId(), ul));
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE umbox_lookup " +
                "SET security_state_id = ?, device_type_id = ?, umbox_image_id = ?, dag_order = ?" +
                "WHERE id = ?")) {
            st.setInt(1, ul.getSecurityStateId());
            st.setInt(2, ul.getDeviceTypeId());
            st.setInt(3, ul.getUmboxImageId());
            st.setInt(4, ul.getDagOrder());
            st.setInt(5, ul.getId());
            st.executeUpdate();

            return ul.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating UmboxLookup: " + e.getClass().toString() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * First, attempts to find the UmboxLookup in the database.
     * If successful, updates the existing UmboxLookup with the given parameters Otherwise,
     * inserts the given UmboxLookup.
     *
     * @param ul UmboxLookup to be inserted or updated.
     */
    public static Integer insertOrUpdateUmboxLookup(UmboxLookup ul) {
        UmboxLookup foundUl = findUmboxLookup(ul.getId());

        if (foundUl == null) {
            return insertUmboxLookup(ul);
        } else {
            return updateUmboxLookup(ul);
        }
    }

    /**
     * Deletes a UmboxLookup by its id.
     */
    public static Boolean deleteUmboxLookup(int id) {
        return deleteById("umbox_lookup", id);
    }

    
}
