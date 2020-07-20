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
import edu.cmu.sei.kalki.db.models.PolicyCondition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PolicyConditionDAO extends DAO
{
    /**
     * Converts a ResultSet obj to a PolicyCondition
     */
    public static PolicyCondition createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) { return null; }
        int id = rs.getInt("id");
        int threshold = rs.getInt("threshold");
        return new PolicyCondition(id, threshold, null);
    }
    
    /**
     * Find a PolicyCondition and its associated AlertType ids
     * @param id
     * @return A PolicyCondition obj. Null otherwise
     */
    public static PolicyCondition findPolicyCondition(int id) {
        try {
            PolicyCondition policyCondition = (PolicyCondition) findObjectByIdAndTable(id, "policy_condition", PolicyConditionDAO.class);
            if(policyCondition != null) {
                List<Integer> alertTypeIds = new ArrayList<>();
                try (Connection con = Postgres.getConnection();
                     PreparedStatement st = con.prepareStatement("SELECT * FROM policy_condition_alert WHERE policy_cond_id = ?")) {
                    st.setInt(1, policyCondition.getId());
                    try (ResultSet rs = st.executeQuery()) {
                        while (rs.next()) {
                            alertTypeIds.add(rs.getInt("alert_type_id"));
                        }
                    }
                }
                policyCondition.setAlertTypeIds(alertTypeIds);
            }

            return policyCondition;
        } catch (SQLException e) {
            logger.severe("Error finding PolicyCondition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Finds all PolicyConditions in the database.
     *
     * @return a list of all PolicyConditions in the database.
     */
    public static List<PolicyCondition> findAllPolicyConditions() {
        try {
            List<PolicyCondition> policyConditions = (List<PolicyCondition>) findObjectsByTable("policy_condition", PolicyConditionDAO.class);
            if (policyConditions != null) {
                for(int i=0; i<policyConditions.size(); i++) {
                    PolicyCondition policyCondition = policyConditions.get(i);
                    List<Integer> alertTypeIds = new ArrayList<>();
                    try (Connection con = Postgres.getConnection();
                         PreparedStatement st = con.prepareStatement("SELECT * FROM policy_condition_alert WHERE policy_cond_id = ?")) {
                        st.setInt(1, policyCondition.getId());
                        try (ResultSet rs = st.executeQuery()) {
                            while (rs.next()) {
                                alertTypeIds.add(rs.getInt("alert_type_id"));
                            }
                        }
                    }
                    policyCondition.setAlertTypeIds(alertTypeIds);
                }
            }

            return policyConditions;
        } catch (SQLException e) {
            logger.severe("Error finding PolicyCondition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Inserts a row into the policy_condition table and a row for each alert_type_id in policy_condition_alert
     * @param policyCondition
     * @return
     */
    public static Integer insertPolicyCondition(PolicyCondition policyCondition) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO policy_condition(threshold) VALUES(?) RETURNING id")) {
            st.setInt(1, policyCondition.getThreshold());
            st.execute();
            policyCondition.setId(getLatestId(st));

            if(policyCondition.getAlertTypeIds() != null){
                for(int i=0; i<policyCondition.getAlertTypeIds().size(); i++) {
                    int id = policyCondition.getAlertTypeIds().get(i);
                    try(PreparedStatement insert2 = con.prepareStatement("INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES(?,?)")) {
                        insert2.setInt(1, policyCondition.getId());
                        insert2.setInt(2, id);
                        insert2.executeUpdate();
                    }
                }
            }

            return policyCondition.getId();
        } catch (SQLException e) {
            logger.severe("Error inserting PolicyCondition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Updates a row in policy_condition and related rows in policy_condition_alert
     * @param policyCondition The policy condition to update
     * @return the condition's id on succes; -1 on failure
     */
    public static Integer updatePolicyCondition(PolicyCondition policyCondition) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE policy_condition SET threshold = ? WHERE id = ?")) {
            // Update PolicyCondition table
            st.setInt(1, policyCondition.getThreshold());
            st.setInt(2, policyCondition.getId());
            st.executeUpdate();

            // Update PolicyConditionAlert table
            if(!deletePolicyConditionAlertRows(policyCondition.getId()))
                return -1;

            for(Integer alertId: policyCondition.getAlertTypeIds()) {
                try(PreparedStatement insert = con.prepareStatement("INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES(?,?)")) {
                    insert.setInt(1, policyCondition.getId());
                    insert.setInt(2, alertId);
                    insert.executeUpdate();
                }
            }

            return policyCondition.getId();
        } catch (SQLException e) {
            logger.severe("Error updating PolicyCondition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Helper function to remove all rows from policy_condition_alert for the given PolicyCondition id
     * @param policyConditionId
     * @return
     */
    private static boolean deletePolicyConditionAlertRows(int policyConditionId){
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("DELETE FROM policy_condition_alert WHERE policy_cond_id = ?")) {
            st.setInt(1, policyConditionId);
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.severe("Error deleting PolicyConditionAlert rows: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a row in the policy_condition table with the given id
     * @param id
     * @return
     */
    public static Boolean deletePolicyCondition(int id) {
        return deleteById("policy_condition", id);
    }
    
}
