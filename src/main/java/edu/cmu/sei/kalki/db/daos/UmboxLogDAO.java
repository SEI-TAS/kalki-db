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
import edu.cmu.sei.kalki.db.models.UmboxLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UmboxLogDAO extends DAO
{

    /**
     * Converts a row from the umbox_log table to a UmboxLog object
     */
    public static UmboxLog createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String alerterId = rs.getString("alerter_id");
        String details = rs.getString("details");
        Timestamp timestamp = rs.getTimestamp("timestamp");
        return new UmboxLog(id, alerterId, details, timestamp);
    }

    /**
     * Finds the row in umbox_log table with given id
     * @param id
     * @return UmboxLog object representing row; Null if an exception is thrown
     */
    public static UmboxLog findUmboxLog(int id){
        return (UmboxLog) findObjectByIdAndTable(id, "umbox_log", UmboxLogDAO.class);
    }

    /**
     * Returns all rows from the umbox_log table
     * @return List of UmboxLogs in the umbox_log table
     */
    public static List<UmboxLog> findAllUmboxLogs() {
        return (List<UmboxLog>) findObjectsByTable("umbox_log", UmboxLogDAO.class);
    }

    /**
     * Finds rows in the umbox_log table with the given alerter_id
     * @param alerter_id
     * @return List of UmboxLogs with given alerter_id
     *
     */
    public static List<UmboxLog> findAllUmboxLogsForAlerterId(String alerterId) {
        List<String> alerterIds = new ArrayList<>();
        alerterIds.add(alerterId);
        return (List<UmboxLog>) findObjectsByStringIds(alerterIds, "umbox_log", "alerter_id", UmboxLogDAO.class);
    }

    public static List<UmboxLog> findAllUmboxLogsForDevice(int deviceId) {
        String query = "SELECT log.* FROM umbox_log AS log, umbox_instance AS inst WHERE " +
                "inst.device_id = ? AND inst.alerter_id = log.alerter_id " +
                "ORDER BY log.id DESC";
        return (List<UmboxLog>) findObjectsByIdAndQuery(deviceId, query, UmboxLogDAO.class);
    }

    /**
     * Inserts the given UmboxLog into the umbox_log table
     * @param umboxLog
     * @return the id of the inserted row
     */
    public static int insertUmboxLog(UmboxLog umboxLog){
        logger.info("Inserting new UmboxLog: "+umboxLog.toString());
        int latestId = -1;
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO umbox_log (alerter_id, details) VALUES(?,?) RETURNING id")) {
            st.setString(1, umboxLog.getAlerterId());
            st.setString(2, umboxLog.getDetails());
            st.execute();
            latestId = getLatestId(st);
        } catch (Exception e){
            logger.severe("Error insert UmboxLog into db: "+e.getClass().getName()+": "+e.getMessage());
            e.printStackTrace();
        }
        return latestId;
    }
    
}
