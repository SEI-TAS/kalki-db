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
import edu.cmu.sei.kalki.db.models.StageLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class StageLogDAO extends DAO
{
    /**
     * Converts a result set to a StageLog object
     */
    public static StageLog createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int deviceSecurityStateId = rs.getInt("device_sec_state_id");
        Timestamp timestamp = rs.getTimestamp("timestamp");
        String action = rs.getString("action");
        String stage = rs.getString("stage");
        String info = rs.getString("info");
        return new StageLog(id, deviceSecurityStateId, timestamp, action, stage, info);
    }
    
    /**
     * Finds the row in stage_log for the given id
     * @param id
     * @return StageLog representing row with given id
     */
    public static StageLog findStageLog(int id) {
        return (StageLog) findObjectByIdAndTable(id, "stage_log", StageLogDAO.class);
    }

    /**
     * Returns all rows in the stage_log table
     * @return a List of all StageLogs
     */
    public static List<StageLog> findAllStageLogs(){
        String query = "SELECT * FROM stage_log ORDER BY timestamp";
        return (List<StageLog>) findObjectsByQuery(query, StageLogDAO.class);
    }

    /**
     * Returns all rows in the stage_log related to the given device
     * @param deviceId
     * @return a List of StageLogs related to the given device id
     */
    public static List<StageLog> findAllStageLogsForDevice(int deviceId) {
        String query = "SELECT sl.id, sl.device_sec_state_id, sl.timestamp, sl.action, sl.stage, sl.info " +
                "FROM stage_log sl, device_security_state dss " +
                "WHERE dss.device_id=? AND sl.device_sec_state_id=dss.id";
        return (List<StageLog>) findObjectsByIdAndQuery(deviceId, query, StageLogDAO.class);
    }

    /**
     *
     * @return
     */
    public static List<String> findStageLogActions(){
        List<String> actions = new ArrayList<>();
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT action FROM stage_log WHERE stage=?")) {
            st.setString(1, StageLog.Stage.FINISH.convert());
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    actions.add(rs.getString("action"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting all actions that finished in stage_log: "+e.getMessage());
        }
        return actions;

    }

    /**
     * Inserts the given StageLog into the stage_log table
     * @param stageLog
     * @return
     */
    public static int insertStageLog(StageLog stageLog){
        logger.info("Inserting new stage log: "+stageLog.toString());
        int latestId = -1;
        long timestamp = System.currentTimeMillis();
        stageLog.setTimestamp(new Timestamp(timestamp));
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO stage_log (device_sec_state_id, action, stage, info, timestamp) VALUES(?,?,?,?,?) RETURNING id")) {
            st.setInt(1, stageLog.getDeviceSecurityStateId());
            st.setString(2, stageLog.getAction());
            st.setString(3, stageLog.getStage());
            st.setString(4, stageLog.getInfo());
            st.setTimestamp(5, stageLog.getTimestamp());

            st.execute();

            latestId = getLatestId(st);
        } catch (Exception e){
            logger.severe("Error insert StageLog into db: "+e.getClass().getName()+": "+e.getMessage());
            e.printStackTrace();
        }
        return latestId;
    }
    
}
