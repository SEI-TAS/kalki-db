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
import edu.cmu.sei.kalki.db.models.DeviceCommandLookup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DeviceCommandLookupDAO extends DAO
{
    /**
     * Extract a Command from the result set of a database query.
     */
    public static DeviceCommandLookup createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) { return null; }
        int id = rs.getInt("id");
        int commandId = rs.getInt("command_id");
        int policyRuleId = rs.getInt("policy_rule_id");
        return new DeviceCommandLookup(id, commandId, policyRuleId);
    }

    /**
     * Finds a command lookup based on the given id
     */
    public static DeviceCommandLookup findCommandLookup(int id) {
        return (DeviceCommandLookup) findObjectByIdAndTable(id, "command_lookup", DeviceCommandLookupDAO.class);
    }

    /**
     * Finds all command lookups based on the given device id
     */
    public static List<DeviceCommandLookup> findCommandLookupsByDevice(int deviceId) {
        String query = "SELECT cl.* FROM command_lookup cl, device d, command c WHERE d.id = ? AND c.device_type_id = d.type_id AND c.id=cl.command_id";
        return (List<DeviceCommandLookup>) findObjectsByIdAndQuery(deviceId, query, DeviceCommandLookupDAO.class);
    }

    /**
     * Finds all rows in the command lookup table
     */
    public static List<DeviceCommandLookup> findAllCommandLookups() {
        return (List<DeviceCommandLookup>) findObjectsByTable("command_lookup", DeviceCommandLookupDAO.class);
    }

    /**
     * inserts into command lookup to relate a state and command
     *
     * @return -1 on failure, 1 on success
     */
    public static int insertCommandLookup(DeviceCommandLookup commandLookup) {
        logger.info("Inserting command lookup; commandId: "+ commandLookup.getCommandId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (?,?) RETURNING id")) {
            st.setInt(1, commandLookup.getCommandId());
            st.setInt(2, commandLookup.getPolicyRuleId());
            st.execute();

            return getLatestId(st);
        } catch (SQLException e) {
            logger.severe("Error inserting CommandLookup: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * First, attempts to find the Command Lookup id in the database.
     * If successful, updates the existing Command Lookup with the given Device Commands's parameters Otherwise,
     * inserts the given Device Command as a Command Lookup.
     */
    public static Integer insertOrUpdateCommandLookup(DeviceCommandLookup commandLookup) {
        DeviceCommandLookup cl = findCommandLookup(commandLookup.getId());
        if (cl == null) {
            return insertCommandLookup(commandLookup);
        } else {
            return updateCommandLookup(commandLookup);
        }
    }

    /**
     * Updates the command lookup entry
     *
     * @input the command lookup id to update
     * @input the device command to update
     */

    public static Integer updateCommandLookup(DeviceCommandLookup commandLookup) {
        logger.info("Updating command lookup; commandId: " +commandLookup.getCommandId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE command_lookup SET command_id = ?, policy_rule_id = ? WHERE id = ?")) {
            st.setInt(1, commandLookup.getCommandId());
            st.setInt(2, commandLookup.getPolicyRuleId());
            st.setInt(3, commandLookup.getId());
            st.executeUpdate();

            return commandLookup.getId();
        } catch (SQLException e) {
            logger.severe("Error inserting Command: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Deletes all Command Lookups by a policy rule id.
     *
     * @param id id of the Policy Rule to delete Command Lookups by.
     * @return true if the deletions succeeded, false otherwise.
     */
    public static Boolean deleteCommandLookupsByPolicyRule(int id) {
        logger.info(String.format("Deleting command lookups associated with policy rule with id = %d", id));
        try (Connection con = Postgres.getConnection();
             PreparedStatement st = con.prepareStatement("DELETE FROM command_lookup WHERE policy_rule_id=?")) {
            st.setInt(1, id);
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.severe("Error deleting id: " + id + " from table: command_lookup. " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a Command Lookup by its id.
     *
     * @param id id of the Command Lookup to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteCommandLookup(int id) {
        logger.info(String.format("Deleting command lookup with id = %d", id));
        return deleteById("command_lookup", id);
    }
    
}
