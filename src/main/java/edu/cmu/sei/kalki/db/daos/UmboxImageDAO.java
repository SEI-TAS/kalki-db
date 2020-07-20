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
import edu.cmu.sei.kalki.db.models.UmboxImage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UmboxImageDAO extends DAO
{
    /**
     * Extract a UmboxImage from the result set of a database query that includes umbox_lookup.
     */
    public static UmboxImage createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String fileName = rs.getString("file_name");
        try {
            // Only if it contains dagOrder.
            int dagOrder = rs.getInt("dag_order");
            return new UmboxImage(id, name, fileName, dagOrder);
        } catch (SQLException ignore) {
            return new UmboxImage(id, name, fileName);
        }
    }
    
    /**
     * Find a UmboxImage based on its id
     *
     * @param id ID of the desired UmboxImage
     * @return The desired UmboxImage on success or null on failure
     */
    public static UmboxImage findUmboxImage(int id) {
        return (UmboxImage) findObjectByIdAndTable(id, "umbox_image", UmboxImageDAO.class);
    }

    /**
     * Finds the UmboxImages relating to the device type and the security state
     *
     * @param deviceTypeId id of the device type
     * @param secStateId id of the security state
     * @return A list of UmboxImages for the given device type id and state id
     */
    public static List<UmboxImage> findUmboxImagesByDeviceTypeAndSecState(int deviceTypeId, int secStateId) {
        try(Connection con = Postgres.getConnection();
        PreparedStatement st = con.prepareStatement("SELECT ui.id, ui.name, ui.file_name, ul.dag_order " +
                "FROM umbox_image ui, umbox_lookup ul, device_type dt, security_state st " +
                "WHERE dt.id = ? " +
                "AND st.id = ? " +
                "AND ul.umbox_image_id = ui.id AND ul.device_type_id = dt.id AND ul.security_state_id = st.id")) {
            st.setInt(1, deviceTypeId);
            st.setInt(2, secStateId);
            List<UmboxImage> umboxImageList = new ArrayList<>();
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    umboxImageList.add(createFromRs(rs));
                }
            }
            return umboxImageList;
        } catch (SQLException e) {
            logger.severe("Sql exception getting all UmboxImages: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds all UmboxImages in the database.
     *
     * @return a list of all UmboxImages in the database.
     */
    public static List<UmboxImage> findAllUmboxImages() {
        return (List<UmboxImage>) findObjectsByTable("umbox_image", UmboxImageDAO.class);
    }

    /**
     * Inserts given UmboxImage into the database
     *
     * @param u the UmboxImage to be inserted
     * @return The id of the inserted UmboxImage on success or -1 on failure
     */
    public static Integer insertUmboxImage(UmboxImage u) {
        logger.info("Adding umbox image: " + u);
        try(Connection con = Postgres.getConnection();
        PreparedStatement st = con.prepareStatement("INSERT INTO umbox_image (name, file_name) VALUES (?, ?) RETURNING id")) {
            st.setString(1, u.getName());
            st.setString(2, u.getFileName());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("SQL exception adding umbox iamge: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates given UmboxImage in the database
     *
     * @param u the UmboxImage to be updated
     * @return The ID of the updated UmboxImage or -1 on failure
     */
    public static Integer updateUmboxImage(UmboxImage u) {
        logger.info("Editing umbox image: " + u);
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE umbox_image " +
                "SET name = ?, file_name = ? " +
                "WHERE id = ?")) {
            st.setString(1, u.getName());
            st.setString(2, u.getFileName());
            st.setInt(3, u.getId());
            st.executeUpdate();
            return u.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("SQL exception editing umbox iamge: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * First, attempts to find the UmboxImage in the database.
     * If successful, updates the existing UmboxImage with the given UmboxImage's parameters Otherwise,
     * inserts the given UmboxImage.
     *
     * @param image UmboxImage to be inserted or updated.
     */
    public static Integer insertOrUpdateUmboxImage(UmboxImage image) {
        UmboxImage ui = findUmboxImage(image.getId());
        if (ui == null) {
            return insertUmboxImage(image);
        } else {
            return updateUmboxImage(image);
        }
    }

    /**
     * Deletes a UmboxImage by its id.
     *
     * @param id id of the UmboxImage to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteUmboxImage(int id) {
        return deleteById("umbox_image", id);
    }
    
}
