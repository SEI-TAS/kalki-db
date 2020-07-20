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
import edu.cmu.sei.kalki.db.models.PolicyRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PolicyRuleDAO extends DAO
{
    /**
     * Converts a ResultSet obj to a Policy
     */
    public static PolicyRule createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int stateTransitionId = rs.getInt("state_trans_id");
        int policyConditionId = rs.getInt("policy_cond_id");
        int deviceTypeId = rs.getInt("device_type_id");
        int samplingRate = rs.getInt("sampling_rate_factor");
        return new PolicyRule(id, stateTransitionId, policyConditionId, deviceTypeId, samplingRate);
    }

    /**
     * Returns a policy rule give its id.
     * @param id
     * @return
     */
    public static PolicyRule findPolicyRule(int id) {
        return (PolicyRule) findObjectByIdAndTable(id, "policy_rule", PolicyRuleDAO.class);
    }

    /**
     * Finds all PolicyRules in the database.
     *
     * @return a list of all PolicyRules in the database.
     */
    public static List<PolicyRule> findAllPolicyRules() {
        return (List<PolicyRule>) findObjectsByTable("policy_rule", PolicyRuleDAO.class);
    }

    /**
     * Finds the policy rule given the StateTransition PolicyCondition and DeviceType id's
     * @param stateTransitionId
     * @param policyConditionId
     * @param deviceTypeId
     * @return
     */
    public static PolicyRule findPolicyRule(int stateTransitionId, int policyConditionId, int deviceTypeId) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT * FROM policy_rule WHERE " +
                "state_trans_id = ? AND " +
                "policy_cond_id = ? AND " +
                "(device_type_id = ? OR device_type_id IS NULL)")) {
            st.setInt(1, stateTransitionId);
            st.setInt(2, policyConditionId);
            st.setInt(3, deviceTypeId);

            ResultSet rs = st.executeQuery();
            if(rs.next()) {
                return createFromRs(rs);
            }
        } catch (SQLException e) {
            logger.severe("Error finding Policy Rule: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds all policy rules given the DeviceType ids
     * @param securityStateId
     * @param deviceTypeId
     * @return
     */
    public static List<PolicyRule> findPolicyRules(int deviceTypeId) {
        String query = "SELECT * FROM policy_rule WHERE device_type_id = ? OR device_type_id IS NULL";
        return (List<PolicyRule>) findObjectsByIdAndQuery(deviceTypeId, query, PolicyRuleDAO.class);
    }

    /**
     * Inserts the given PolicyRule obj to the policy table
     * @param policyRule The obj to insert
     * @return Row's id on success. -1 otherwise
     */
    public static Integer insertPolicyRule(PolicyRule policyRule) {
        try(Connection con = Postgres.getConnection();
        PreparedStatement st = con.prepareStatement("INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES(?,?,?,?) RETURNING id")) {
            st.setInt(1, policyRule.getStateTransitionId());
            st.setInt(2, policyRule.getPolicyConditionId());
            st.setInt(3, policyRule.getDeviceTypeId());
            st.setInt(4, policyRule.getSamplingRateFactor());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            logger.severe("Error inserting Policy: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * First, attempts to find the Policy Rule in the database.
     * If successful, updates the existing Policy Rule with the given Policy Rule's parameters Otherwise,
     * inserts the given Policy Rule.
     * @param policyRule Policy Rule to be inserted or updated.
     */
    public static Integer insertOrUpdatePolicyRule(PolicyRule policyRule) {
        PolicyRule pr = findPolicyRule(policyRule.getId());
        if (pr == null) {
            return insertPolicyRule(policyRule);
        } else {
            return updatePolicyRule(policyRule);
        }
    }

    /**
     * Updates the row in the policy table with the given id
     * @param policyRule
     * @return The id of the given policy on success. -1 otherwise
     */
    public static Integer updatePolicyRule(PolicyRule policyRule) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE policy_rule SET " +
                "state_trans_id = ? " +
                ", policy_cond_id = ? " +
                ", device_type_id = ? " +
                ", sampling_rate_factor = ? " +
                "WHERE id = ?")) {
            st.setInt(1, policyRule.getStateTransitionId());
            st.setInt(2, policyRule.getPolicyConditionId());
            st.setInt(3, policyRule.getDeviceTypeId());
            st.setInt(4, policyRule.getSamplingRateFactor());
            st.setInt(5, policyRule.getId());
            st.executeUpdate();
            return policyRule.getId();
        } catch (SQLException e) {
            logger.severe("Error updating Policy: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /***
     * Delete a row in the policy rule table with the given id
     * @param policyRuleId
     * @return True on success, false otherwise
     */
    public static boolean deletePolicyRule(int policyRuleId) {
        return deleteById("policy_rule", policyRuleId);
    }
    
}
