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
import edu.cmu.sei.kalki.db.models.Tag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TagDAO extends DAO
{
    /**
     * Extract a Tag from the result set of a database query.
     */
    public static Tag createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        return new Tag(id, name);
    }
    
    /**
     * Search the tag table for a row with the given id
     *
     * @param id The id of the tag
     * @return the row from the table
     */
    public static Tag findTag(int id) {
        return (Tag) findObjectByIdAndTable(id, "tag", TagDAO.class);
    }

    /**
     * Find the respective tags for given device id
     *
     * @param deviceId The device id the tags are for
     * @return A list of tags or null
     */
    public static List<Tag> findTagsByDevice(int deviceId) {
        String query = "SELECT tag.* FROM tag, device_tag " +
                "WHERE tag.id = device_tag.tag_id AND device_tag.device_id = ?";
        return (List<Tag>) findObjectsByIdAndQuery(deviceId, query, TagDAO.class);
    }

    /**
     * Find the respective tag ids for given device id
     *
     * @param deviceId The device id the tags are for
     * @return A list of tag ids or null
     */
    public static List<Integer> findTagIds(int deviceId) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT * FROM device_tag WHERE device_id = ?")) {
            st.setInt(1, deviceId);
            List<Integer> tagIds = new ArrayList<>();
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    tagIds.add(rs.getInt(2));
                }
            }
            return tagIds;
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error finding tags by device_id: " + deviceId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds all Tags in the database.
     *
     * @return a list of all Tags in the database.
     */
    public static List<Tag> findAllTags() {
        return (List<Tag>) findObjectsByTable("tag", TagDAO.class);
    }

    /**
     * Saves given Tag to the database.
     *
     * @param tag Tag to be inserted.
     * @return auto incremented id
     */
    public static Integer insertTag(Tag tag) {
        logger.info("Inserting Tag: " + tag.getId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                ("INSERT INTO tag(name)" +
                        "values(?) RETURNING id")) {
            st.setString(1, tag.getName());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting Tag: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates Tag with given id to have the parameters of the given Tag.
     *
     * @param tag Tag holding new parameters to be saved in the database.
     */
    public static Integer updateTag(Tag tag) {
        logger.info("Updating Tag with id=" + tag.getId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                ("UPDATE tag SET name = ?" +
                        "WHERE id=?")) {
            st.setString(1, tag.getName());
            st.setInt(2, tag.getId());
            st.executeUpdate();
            return tag.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating Tag: " + e.getClass().getName() + ": " + e.getMessage());
            return -1;
        }
    }


    /**
     * First, attempts to find the Tag in the database.
     * If successful, updates the existing Tag with the given Tag's parameters Otherwise,
     * inserts the given Tag.
     *
     * @param tag Tag to be inserted or updated.
     */
    public static Integer insertOrUpdateTag(Tag tag) {
        Tag t = findTag(tag.getId());
        if (t == null) {
            return insertTag(tag);
        } else {
            return updateTag(tag);
        }
    }

    /**
     * Deletes a Tag by its id.
     *
     * @param id id of the Tag to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteTag(int id) {
        logger.info(String.format("Deleting Tag with id = %d", id));
        return deleteById("tag", id);
    }    
}
