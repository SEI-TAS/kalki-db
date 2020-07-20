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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DAO
{
    private static final String DEFAULT_SELECT_QUERY = "SELECT * FROM %s WHERE %s = ?";

    protected static Logger logger = Logger.getLogger(DAO.class.getName());

    /**
     * Finds a database entry in a given table and column, plus key.
     * @param key           id or key of the entry to find
     * @param selectQuery   the select query.
     * @return the object of the query if something is found, null otherwise
     */
    protected static Object findByInt(int key, String selectQuery, Class objectClass) {
        logger.info(String.format("Finding by key = %d for query %s", key, selectQuery));
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement(selectQuery)) {
            st.setInt(1, key);
            try(ResultSet rs = st.executeQuery()) {
                // Moves the result set to the first row if it exists. Returns null otherwise.
                if (rs.next()) {
                    return createFromRs(objectClass, rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Exception finding by int: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds a database entry in a given table and column, plus key.
     *
     * @param key       key of the entry to find
     * @param query     select query.
     * @return the object of the query if something is found, null otherwise
     */
    protected static Object findByString(String key, String query, Class objectClass) {
        logger.info(String.format("Finding by key = %s for query %s", key, query));
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement(query)) {
            st.setString(1, key);
            try(ResultSet rs = st.executeQuery()) {
                // Moves the result set to the first row if it exists. Returns null otherwise.
                if (rs.next()) {
                    return createFromRs(objectClass, rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Exception finding by int: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Assumes a RETURNING clause in an executed statement was included, gets result from resultset.
     *
     * @param tableName The table to get the latest id
     * @return The latest id on success
     */
    protected static int getLatestId(Statement st) throws SQLException {
        try(ResultSet rs = st.getResultSet()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Delete a row with the given id from the given table
     *
     * @param table The name of the table for the row to be deleted
     * @param id    The id of the row to be deleted
     * @return True if the deletion was successful
     */
    protected static Boolean deleteById(String table, int id) {
        logger.info(String.format("Deleting by id = %d in %s", id, table));
        try (Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement(String.format("DELETE FROM %s WHERE id=?", table))) {
            st.setInt(1, id);
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.severe("Error deleting id: " + id + " from table: " + table + ". " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Creates an object from a result set, given the class name of the object.
     * @param dbObjectClass the class of object to create
     * @param rs the result set of the information
     * @return anb object of the given type with the information.
     */
    protected static Object createFromRs(Class dbObjectClass, ResultSet rs) {
        try {
            Method method = dbObjectClass.getMethod("createFromRs", ResultSet.class);
            return method.invoke(null, rs);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a DB object of the given class, from the given table.
     * @param id DB id of the object to get
     * @param tableName table to get the data from
     * @param objectClass class of the object to return.
     * @return
     */
    protected static Object findObjectByIdAndTable(int id, String tableName, Class objectClass) {
        logger.info(String.format("Finding by key = %d in column %s in table %s", id, "id", tableName));
        return findByInt(id, String.format(DEFAULT_SELECT_QUERY, tableName, "id"), objectClass);
    }

    /**
     * Returns a DB object of the given class, from the given table.
     * @param id DB id of the object to get
     * @param query query to use.
     * @param objectClass class of the object to return.*
     * @return
     */
    protected static Object findObjectByIdAndQuery(int id, String query, Class objectClass) {
        return findByInt(id, query, objectClass);
    }

    /**
     * Returns a DB object of the given class, from the given table.
     * @param key DB key of the object to get
     * @param query query to use.
     * @param objectClass class of the object to return.*
     * @return
     */
    protected static Object findObjectByStringAndQuery(String key, String query, Class objectClass) {
        return findByString(key, query, objectClass);
    }

    /**
     * Gets all elements that match a list of ids.
     * @param ids
     * @param tableName
     * @param column
     * @return
     */
    protected static List<?> findObjectsByIntIds(List<Integer> ids, String tableName, String column, Class objectClass) {
        if(ids.isEmpty()) {
            throw new RuntimeException("Empty set of ids received.");
        }

        StringBuilder allIds = new StringBuilder();
        for (int id : ids) {
            allIds.append(id).append(",");
        }
        allIds.deleteCharAt(allIds.length() - 1); // Remove trailing comma.

        return findObjectsByIds(allIds.toString(), tableName, column, objectClass);
    }

    /**
     * Gets all elements that match a list of ids.
     * @param ids
     * @param tableName
     * @param column
     * @return
     */
    protected static List<?> findObjectsByStringIds(List<String> ids, String tableName, String column, Class objectClass) {
        if(ids.isEmpty()) {
            throw new RuntimeException("Empty set of ids received.");
        }

        StringBuilder allIds = new StringBuilder();
        for (String id : ids) {
            allIds.append("'").append(id).append("',");
        }
        allIds.deleteCharAt(allIds.length() - 1); // Remove trailing comma.

        return findObjectsByIds(allIds.toString(), tableName, column, objectClass);
    }

    /**
     * Gets all elements that match a list of ids.
     * @param idList    a string with a list of ids, separated by commas, in quotes if needed.
     * @param tableName
     * @param column
     * @return
     */
    private static List<?> findObjectsByIds(String idList, String tableName, String column, Class objectClass) {
        List<Object> allObjects = new ArrayList<>();
        try (Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement(String.format("SELECT * FROM %s WHERE %s in (%s)", tableName, column, idList))) {
            //logger.info(st.toString());
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    allObjects.add(createFromRs(objectClass, rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all objects: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return allObjects;
    }

    /**
     * Finds all entries in a given table.
     *
     * @param tableName name of the table.
     * @return a list of all entries in the table.
     */
    public static List<?> findObjectsByTable(String tableName, Class objectClass) {
        return findObjectsByQuery("SELECT * FROM " + tableName, objectClass);
    }

    /**
     * Returns all items obtained by the given query.
     * @param query
     * @return
     */
    public  static List<?> findObjectsByQuery(String query, Class objectClass) {
        List<Object> objectList = new ArrayList<>();
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement(query)) {
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    objectList.add(createFromRs(objectClass, rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error getting all entries: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return objectList;
    }

    /**
     * Finds all in the database
     *
     * @return a list of items
     */
    protected static List<?> findObjectsByIdAndQuery(int id, String query, Class objectClass) {
        List<Object> objectList = new ArrayList<>();
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement(query)) {
            st.setInt(1, id);
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    objectList.add(createFromRs(objectClass, rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error getting all entries: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return objectList;

    }

    /**
     * Finds all entries matching query with a variable size list of inputs
     * @param ints List of integers to set values on prepared statement
     * @param query String representation of the query
     * @param objectClass Class representation of query result
     * @return List of results
     */
    protected static List<?> findObjectsByIntListAndQuery(List<Integer> ints, String query, Class objectClass) {
        List<Object> objectList = new ArrayList<>();
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement(query)) {
            for(int i=1; i<=ints.size(); i++){
                st.setInt(i, ints.get(i-1));
            }
            try(ResultSet rs = st.executeQuery()){
                while(rs.next()){
                    objectList.add(createFromRs(objectClass, rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error getting all entries: "+ e.getClass().getName() + ": " + e.getMessage());
        }
        return objectList;
    }
}
