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
import edu.cmu.sei.kalki.db.models.DeviceCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Arrays;

public class DeviceCommandDAO extends DAO
{
    /**
     * Extract a Command name from the result set of a database query.
     */
    public static DeviceCommand createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int deviceTypeId = rs.getInt("device_type_id");
        return new DeviceCommand(id, name, deviceTypeId);
    }

    /**
     * Finds a command based on the given id
     */
    public static DeviceCommand findCommand(int id) {
        return (DeviceCommand) findObjectByIdAndTable(id,"command", DeviceCommandDAO.class);
    }

    /**
     * Finds all rows in the command table
     */
    public static List<DeviceCommand> findAllCommands() {
        return (List<DeviceCommand>) findObjectsByTable("command", DeviceCommandDAO.class);
    }

    
    /**
     * Finds all rows in the command table with the specified device type id
     */
    public static List<DeviceCommand> findAllCommandsByDeviceType(int id) {
        String query = "SELECT * FROM command where device_type_id=" + id;
        return (List<DeviceCommand>) findObjectsByQuery(query, DeviceCommandDAO.class);
    }

    /**
     * Finds the commands for the device from the policy rule log
     *
     * @param policyRuleId The policy rule id
     * @return commands A list of command names
     */
    public static List<DeviceCommand> findCommandsByPolicyRuleLog(int policyRuleId) {
        String query = "SELECT c.id, c.name, c.device_type_id FROM command_lookup AS cl, command AS c, policy_rule_log AS pi " +
                "WHERE pi.policy_rule_id = cl.policy_rule_id AND c.id = cl.command_id AND pi.id = ?";
        return (List<DeviceCommand>) findObjectsByIdAndQuery(policyRuleId, query, DeviceCommandDAO.class);
    }

    /**
     * Finds commands for the given device type id and policy rule id
     * @param policyRuleId
     * @param device
     * @return  A list of device commands
     */
    public static List<DeviceCommand> findCommandsForDeviceTypeByPolicyRuleLog(int policyRuleLogId, int deviceTypeId) {
        List<Integer> paramList = Arrays.asList(policyRuleLogId, deviceTypeId);
        String query = "SELECT c.* FROM command AS c, command_lookup AS cl, policy_rule_log AS prl " +
                "WHERE prl.id=? AND prl.policy_rule_id=cl.policy_rule_id AND cl.command_id=c.id AND c.device_type_id=?";
        return (List<DeviceCommand>) findObjectsByIntListAndQuery(paramList, query, DeviceCommandDAO.class);
    }

    /**
     * insert a command into the db
     */
    public static Integer insertCommand(DeviceCommand command) {
        logger.info("Inserting command: " + command.getName());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO command(name, device_type_id) VALUES (?,?) RETURNING id")) {
            st.setString(1, command.getName());
            st.setInt(2, command.getDeviceTypeId());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting Command: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates the command entry
     *
     * @input the command to update
     */
    public static Integer updateCommand(DeviceCommand command) {
        logger.info("Updating command; commandId: " +command.getId()+ " name: " +command.getName());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE command SET name = ?, device_type_id = ? WHERE id = ?")) {
            st.setString(1, command.getName());
            st.setInt(2, command.getDeviceTypeId());
            st.setInt(3, command.getId());
            st.executeUpdate();

            return command.getId();
        } catch (SQLException e) {
            logger.severe("Error updating Command: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * First, attempts to find the Command id in the database.
     * If successful, updates the existing Command with the given Device Commands's parameters Otherwise,
     * inserts the given Device Command as a Command Lookup.
     */
    public static Integer insertOrUpdateCommand(DeviceCommand command) {
        DeviceCommand dc = findCommand(command.getId());
        if (dc == null) {
            return insertCommand(command);
        } else {
            return updateCommand(command);
        }
    }

    /**
     * Deletes a Command by its id.
     *
     * @param id id of the Command Lookup to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteCommand(int id) {
        logger.info(String.format("Deleting command with id = %d", id));
        return deleteById("command", id);
    }    
}
