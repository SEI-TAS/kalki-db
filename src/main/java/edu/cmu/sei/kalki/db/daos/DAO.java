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
     * Properly closes a resource set and its parent statement.
     * @param rs
     */
    protected static void closeResources(ResultSet rs) {
        if(rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) { }

            try {
                if(rs.getStatement() != null) {
                    rs.getStatement().close();

                    if(rs.getStatement().getConnection() != null) {
                        rs.getStatement().getConnection().close();
                    }
                }
            } catch (SQLException ignored) { }
        }
    }

    /**
     * Finds a database entry in a given table and column, plus key.
     * NOTE: the RS and PreparedStatement are left open when this function returns so that the RS can be used by the
     * caller function. Both should be closed by the caller. The statement can be obtained from the RS by calling
     * rs.getStatement().
     *
     * @param key           id or key of the entry to find
     * @param selectQuery   the select query.
     * @return the resultset of the query if something is found, null otherwise
     */
    protected static ResultSet findByInt(int key, String selectQuery) {
        logger.info(String.format("Finding by key = %d for query %s", key, selectQuery));
        try {
            Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement(selectQuery);
            st.setInt(1, key);
            ResultSet rs = st.executeQuery();
            // Moves the result set to the first row if it exists. Returns null otherwise.
            if (rs.next()) {
                return rs;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Exception finding by int: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds a database entry in a given table and column, plus key.
     * NOTE: the RS and PreparedStatement are left open when this function returns so that the RS can be used by the
     * caller function. Both should be closed by the caller. The statement can be obtained from the RS by calling
     * rs.getStatement().
     *
     * @param key       key of the entry to find
     * @param query     select query.
     * @return the resultset of the query if something is found, null otherwise
     */
    protected static ResultSet findByString(String key, String query) {
        logger.info(String.format("Finding by key = %s for query %s", key, query));
        try {
            Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement(query);
            st.setString(1, key);
            ResultSet rs = st.executeQuery();
            // Moves the result set to the first row if it exists. Returns null otherwise.
            if (rs.next()) {
                return rs;
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
        ResultSet rs = st.getResultSet();
        if(rs.next()) {
            return rs.getInt(1);
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
        ResultSet rs = findByInt(id, String.format(DEFAULT_SELECT_QUERY, tableName, "id"));
        Object dbObject = createFromRs(objectClass, rs);
        closeResources(rs);
        return dbObject;
    }

    /**
     * Returns a DB object of the given class, from the given table.
     * @param id DB id of the object to get
     * @param query query to use.
     * @param objectClass class of the object to return.*
     * @return
     */
    protected static Object findObjectByIdAndQuery(int id, String query, Class objectClass) {
        ResultSet rs = findByInt(id, query);
        Object dbObject = createFromRs(objectClass, rs);
        closeResources(rs);
        return dbObject;
    }

    /**
     * Returns a DB object of the given class, from the given table.
     * @param key DB key of the object to get
     * @param query query to use.
     * @param objectClass class of the object to return.*
     * @return
     */
    protected static Object findObjectByStringAndQuery(String key, String query, Class objectClass) {
        ResultSet rs = findByString(key, query);
        Object dbObject = createFromRs(objectClass, rs);
        closeResources(rs);
        return dbObject;
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
    protected static List<?> findObjectsByIds(String idList, String tableName, String column, Class objectClass) {
        List<Object> allObjects = new ArrayList<>();
        try (Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement(String.format("SELECT * FROM %s WHERE %s in (%s)", tableName, column, idList))) {
            System.out.println(st.toString());
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
     * NOTE: the RS and PreparedStatement are left open when this function returns so that the RS can be used by the
     * caller function. Both should be closed by the caller. The statement can be obtained from the RS by calling
     * rs.getStatement().
     *
     * @param tableName name of the table.
     * @return a list of all entries in the table.
     */
    protected static ResultSet findAllFromTable(String tableName) {
        return findAllByQuery("SELECT * FROM " + tableName);
    }

    /**
     *
     * @param query
     * @return
     */
    protected static ResultSet findAllByQuery(String query) {
        ResultSet rs = null;
        try {
            Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement(query);
            rs = st.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error getting all entries: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return rs;
    }

    /**
     *
     * @param query
     * @return
     */
    protected static ResultSet findAllByIdAndQuery(int id, String query) {
        ResultSet rs = null;
        try {
            Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement(query);
            st.setInt(1, id);
            rs = st.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error getting all entries: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return rs;
    }


    /**
     * Finds all in the database
     *
     * @return a list of items
     */
    protected static List<?> findObjects(String tableName, Class objectClass) {
        List<Object> objectList = new ArrayList<>();
        try {
            ResultSet rs = findAllFromTable(tableName);
            while (rs.next()) {
                objectList.add(createFromRs(objectClass, rs));
            }
            closeResources(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error getting all: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return objectList;
    }

    /**
     * Finds all in the database
     *
     * @return a list of items
     */
    protected static List<?> findObjectsByQuery(String query, Class objectClass) {
        List<Object> objectList = new ArrayList<>();
        try {
            ResultSet rs = findAllByQuery(query);
            while (rs.next()) {
                objectList.add(createFromRs(objectClass, rs));
            }
            closeResources(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error getting all: " + e.getClass().getName() + ": " + e.getMessage());
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
        try {
            ResultSet rs = findAllByIdAndQuery(id, query);
            while (rs.next()) {
                objectList.add(createFromRs(objectClass, rs));
            }
            closeResources(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error getting all: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return objectList;
    }
}
