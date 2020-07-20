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
import edu.cmu.sei.kalki.db.models.SecurityState;
import edu.cmu.sei.kalki.db.models.StateTransition;

import javax.swing.plaf.nimbus.State;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StateTransitionDAO extends DAO
{
    /**
     * Converts a ResultSet obj to a StateTransition
     */
    public static StateTransition createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int startSecStateId = rs.getInt("start_sec_state_id");
        int finishSecStateId = rs.getInt("finish_sec_state_id");
        return new StateTransition(id, startSecStateId, finishSecStateId);
    }

    /**
     * Finds a specific state transition.
     * @param id
     * @return
     */
    public static StateTransition findStateTransition(int id) {
        return (StateTransition) findObjectByIdAndTable(id, "state_transition", StateTransitionDAO.class);
    }

    /**
     * Find all state transitions.
     * @return
     */
    public static List<StateTransition> findAll() {
        return (List<StateTransition>) findObjectsByTable("state_transition", StateTransitionDAO.class);
    }

    /**
     * Find a state transitions between the two given states. Assumes there is only one, returning the first one
     * in case it is somehow duplicated.
     */
    public static StateTransition findByStateNames(String initStateName, String endStateName) {
        SecurityState initState = SecurityStateDAO.findByName(initStateName);
        SecurityState endState = SecurityStateDAO.findByName(endStateName);
        if(initState == null || endState == null) {
            throw new RuntimeException("State name not found");
        }
        return findByStateIds(initState.getId(), endState.getId());
    }

    /**
     * Find a state transitions between the two given states. Assumes there is only one, returning the first one
     * in case it is somehow duplicated.
     */
    public static StateTransition findByStateIds(int initStateId, int endStateId) {
        List<Integer> stateIds = new ArrayList<>();
        stateIds.add(initStateId);
        stateIds.add(endStateId);
        String query = "SELECT * from state_transition WHERE start_sec_state_id = ? and finish_sec_state_id = ?";
        List<StateTransition> transitions = (List<StateTransition>) findObjectsByIntListAndQuery(stateIds, query, StateTransitionDAO.class);
        if(transitions.size() == 0) {
            throw new RuntimeException("No state transition found for the given states.");
        }

        // We assume there is either only one transition, or we only consider the first one anyway.
        return transitions.get(0);
    }

    /**
     * Inserts the given StateTransition obj to the state_transition table
     * @param trans The obj to insert
     * @return Row's id on success. -1 otherwise
     */
    public static Integer insertStateTransition(StateTransition trans) {
        try(Connection con = Postgres.getConnection();
        PreparedStatement st = con.prepareStatement("INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) VALUES(?,?) RETURNING id")) {
            st.setInt(1, trans.getStartStateId());
            st.setInt(2, trans.getFinishStateId());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            logger.severe("Error inserting StateTransition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Updates the row in the StateTransition table with the given id
     * @param trans
     * @return The id of the given transition on success. -1 otherwise
     */
    public static Integer updateStateTransition(StateTransition trans) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE state_transition SET " +
                "start_sec_state_id = ? " +
                ", finish_sec_state_id = ? " +
                "WHERE id = ?")) {
            st.setInt(1, trans.getStartStateId());
            st.setInt(2, trans.getFinishStateId());
            st.setInt(3, trans.getId());
            st.executeUpdate();
            return trans.getId();
        } catch (SQLException e) {
            logger.severe("Error updating StateTransition: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /***
     * Delete a row in the state_transition table with the given id
     * @param id
     * @return True on success, false otherwise
     */
    public static boolean deleteStateTransition(int id) {
        return deleteById("state_transition", id);
    }
}
