package kalkidb.database;

import kalkidb.models.*;
import org.postgresql.util.HStoreConverter;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Postgres {
    private static final String DEFAULT_IP = "localhost";
    private static final String DEFAULT_PORT = "5432";

    private static final String ROOT_USER = "postgres";
    private static final String BASE_DB = "postgres";
    private static final String DEFAULT_DB_URL = "jdbc:postgresql://" + DEFAULT_IP + ":" + DEFAULT_PORT;

    private static Logger logger = Logger.getLogger("myLogger");
    private static String dbName;
    private static String dbUser;
    private static String dbPassword;
    private static Postgres postgresInstance = null;

    public static Connection dbConn = null;

    private Postgres(String ip, String port, String dbName, String dbUser, String dbPassword){
        try{
            //Read ip, port from config file
            this.dbName = dbName;
            this.dbUser = dbUser;
            this.dbPassword = dbPassword;
            this.dbConn = makeConnection(ip, port);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error initializing postgres: " + e.getClass().getName()+": "+e.getMessage());
        }
    }

    /**
     * Initialize the singleton instance of postgres, using default IP and port.
     */
    public static void initialize(String dbName, String dbUser, String dbPassword) {
        initialize(DEFAULT_IP, DEFAULT_PORT, dbName, dbUser, dbPassword);
    }

    /**
     * Initialize the singleton instance of postgres, connecting to the database.
     * Must be done before any static methods can be used.
     */
    public static void initialize(String ip, String port, String dbName, String dbUser, String dbPassword) {
        if (postgresInstance == null){
            logger.info("Initializing database");
            postgresInstance = new Postgres(ip, port, dbName, dbUser, dbPassword);
        } else {
            logger.info("Database already initialized");
        }
    }

    /**
     * Connects to the postgres database, allowing for database ops.
     * @return a connection to the database.
     */
    public Connection makeConnection(String ip, String port){
        try {
            Class.forName("org.postgresql.Driver");
            dbConn = DriverManager
                    .getConnection("jdbc:postgresql://" + ip + ":" + port + "/" + this.dbName, this.dbUser, this.dbPassword);
            return dbConn;
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error connecting to database : " + e.getClass().getName()+": "+e.getMessage());
            return null;
        }
    }

    /**
     * Creates a DB if it does not exist.
     */
    public static boolean createDBIfNotExists(String rootPassword, String dbName, String dbOwner) throws SQLException
    {
        // First check it DB exists.
        String checkDB = "SELECT datname FROM pg_catalog.pg_database "
                + "WHERE datname = '" + dbName + "';";
        try (Connection rootConn = getRootConnection(rootPassword);
             Statement stmt = rootConn.createStatement();
             ResultSet result = stmt.executeQuery(checkDB))
        {
            if (!result.next())
            {
                // If there was no DB with this name, then create it.
                makeDatabase(rootConn, dbName, dbOwner);
                return true;
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            throw e;
        }
        return false;
    }

    /**
     * Creates a postgres database.
     */
    public static void makeDatabase(Connection rootConnection, String dbName, String dbOwner){
        logger.info("Creating database.");
        executeCommand("CREATE DATABASE " + dbName
                + " WITH OWNER= " + dbOwner
                + " ENCODING = 'UTF8' TEMPLATE = template0 "
                + " CONNECTION LIMIT = -1;", rootConnection);
    }

    /**
     * Removes the database.
     */
    public static void removeDatabase(String rootPassword, String dbName)
    {
        logger.info("Removing database.");
        try (Connection rootConn = getRootConnection(rootPassword))
        {
            executeCommand("DROP DATABASE IF EXISTS " + dbName + ";", rootConn);
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            logger.severe("Error removing database:" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Removes the user.
     */
    public static void removeUser(String rootPassword, String userName)
    {
        logger.info("Removing user.");
        try (Connection rootConn = getRootConnection(rootPassword))
        {
            executeCommand("DROP ROLE IF EXISTS " + userName + ";", rootConn);
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            logger.severe("Error removing user:" + e.getClass().getName() + ": " + e.getMessage());
        }

    }

    /**
     * Creates the user if it does not exist.
     */
    public static void createUserIfNotExists(String rootPassword, String user, String password) {
        String createUser = "DO\n" +
                "$body$\n" +
                "BEGIN\n" +
                "   IF NOT EXISTS (\n" +
                "      SELECT *\n" +
                "      FROM   pg_catalog.pg_user\n" +
                "      WHERE  usename = '" + user + "') THEN\n" +
                "\n" +
                "      CREATE ROLE " + user + " SUPERUSER LOGIN PASSWORD '"
                + password + "';\n" +
                "   END IF;\n" +
                "END\n" +
                "$body$;";
        try (Connection rootConn = getRootConnection(rootPassword))
        {
            executeCommand(createUser, rootConn);
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            logger.severe("Error creating user:" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Gets a connection to the root user.
     */
    private static Connection getRootConnection(String rootPwd) throws SQLException
    {
        Properties connectionProps = new Properties();
        connectionProps.put("user", ROOT_USER);
        connectionProps.put("password", rootPwd);
        return DriverManager.getConnection(DEFAULT_DB_URL + "/" + BASE_DB, connectionProps);
    }

    /**
     * Executes the given SQL command in the database, using the already set up, default connection.
     * @param command SQL commmand string
     */
    public static void executeCommand(String command)
    {
        executeCommand(command, Postgres.dbConn);
    }

    /**
     * Executes the given SQL command in the database.
     * @param command SQL commmand string
     */
    public static void executeCommand(String command, Connection connection){
        if(connection == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
        } else {
            logger.info(String.format("Executing command: %s", command));
            Statement st = null;
            try {
                st = connection.createStatement();
                st.execute(command);
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error executing database command: '" + command + "' " +
                        e.getClass().getName() + ": " + e.getMessage());
            } finally {
                try {
                    if (st != null) st.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * First time database setup.
     * Creates necessary extensions, databases, and tables
     */
    public static void setupDatabase(){
        logger.info("Setting up database.");
        createHstoreExtension();
        makeTables();
        insertDefaultTypes();
        createTriggers();
    }

    /**
     * Add the hstore extension to the postgres database.
     */
    public static void createHstoreExtension(){
        logger.info("Adding hstore extension.");
        executeCommand("CREATE EXTENSION IF NOT EXISTS hstore;");
    }

    /**
     * Drops and recreates all tables.
     */
    public static void resetDatabase(){
        logger.info("Resetting Database.");
        dropTables();
        setupDatabase();
    }

    /**
     * Drops all tables from the database.
     */
    public static void dropTables(){
        logger.info("Dropping tables.");
        List<String> tableNames = new ArrayList<String>();
        tableNames.add("device");
        tableNames.add("device_status");
        tableNames.add("security_state");
        tableNames.add("device_tag");
        tableNames.add("tag");
        tableNames.add("type");
        tableNames.add("device_group");
        tableNames.add("alert_history");
        tableNames.add("umbox_instance");
        tableNames.add("umbox_image");
        for(String tableName: tableNames){
            dropTable(tableName);
        }
    }

    /**
     * Drop a table from the database.
     * @param tableName name of the table to be dropped
     */
    public static void dropTable(String tableName){
        executeCommand("DROP TABLE IF EXISTS " + tableName);
    }

    /**
     * Create tables for each model.
     */
    public static void makeTables(){
        logger.info("Making tables.");

        executeCommand("CREATE TABLE IF NOT EXISTS device(" +
                "id                     serial PRIMARY KEY," +
                "name                   varchar(255) NOT NULL," +
                "description            varchar(255)," +
                "type_id                int NOT NULL," +
                "group_id               int," +
                "ip_address             varchar(255)," +
                "status_history_size    int NOT NULL," +
                "sampling_rate          int NOT NULL,"  +
                "current_state_id       int," +
                "last_alert_id          int"+
                ");"
        );

        executeCommand("CREATE TABLE IF NOT EXISTS device_status(" +
                "device_id     int NOT NULL," +
                "attributes    hstore," +
                "timestamp     TIMESTAMP," +
                "id            serial    PRIMARY KEY" +
                ");"
        );

        executeCommand("CREATE TABLE IF NOT EXISTS security_state(" +
                "id           serial PRIMARY KEY," +
                "device_id    int NOT NULL," +
                "timestamp    TIMESTAMP," +
                "state        varchar(255) NOT NULL" +
                ");"
        );

        executeCommand("CREATE TABLE IF NOT EXISTS device_tag(" +
                "device_id    int NOT NULL, " +
                "tag_id       int NOT NULL" +
                ");"
        );

        executeCommand("CREATE TABLE IF NOT EXISTS tag(" +
                "id           serial PRIMARY KEY, " +
                "name         varchar(255) NOT NULL" +
                ");"
        );

        executeCommand("CREATE TABLE IF NOT EXISTS type(" +
                "id    serial PRIMARY KEY, " +
                "name  varchar(255)," +
                "policy_file    bytea," +
                "policy_file_name    varchar(255)" +
                ");"
        );

        executeCommand("CREATE TABLE IF NOT EXISTS device_group(" +
                "id    serial PRIMARY KEY, " +
                "name  varchar(255)" +
                ");"
        );

        executeCommand("CREATE TABLE IF NOT EXISTS alert_history(" +
                "id                 serial PRIMARY KEY," +
                "timestamp        timestamp NOT NULL DEFAULT now()," +
                "alerter_id  varchar(255) NOT NULL," +
                "info               varchar(255)" +
                ");"
        );

        executeCommand("CREATE TABLE IF NOT EXISTS umbox_instance(" +
                "id                 serial PRIMARY KEY, " +
                "alerter_id         varchar(255) NOT NULL, " +
                "umbox_image_id     int NOT NULL," +
                "container_id       varchar(255) NOT NULL," +
                "device_id          int NOT NULL, " +
                "started_at         timestamp NOT NULL DEFAULT now()" +
                ");"
        );

        executeCommand("CREATE TABLE IF NOT EXISTS umbox_image(" +
                "id           serial PRIMARY KEY," +
                "name         varchar(255) NOT NULL," +
                "path         varchar(255) NOT NULL" +
                ");"
        );
    }

    /**
     * Insert the default types into the database.
     */
    public static void insertDefaultTypes() {
        logger.info("Inserting default types.");
        List<String> typeNames = new ArrayList<String>();
        typeNames.add("Hue Light");
        typeNames.add("Dlink Camera");
        typeNames.add("WeMo Insight");
        typeNames.add("Udoo Neo");
        for(String typeName: typeNames){
            executeCommand("INSERT INTO type (name) VALUES ('" + typeName + "')");
        }
    }

    /**
     * Creates all necessary triggers in the database, and necessary helper functions.
     * Namely, creates a trigger and function to send notifications on device insert.
     */
    public static void createTriggers(){
        // deviceNotify
        // when a device is inserted
        executeCommand("CREATE OR REPLACE FUNCTION \"deviceNotify\"()\n" +
                "  RETURNS TRIGGER AS $$\n" +
                "DECLARE\n" +
                "  payload TEXT;\n" +
                "BEGIN\n" +
                "  payload := NEW.id;\n" +
                "  PERFORM pg_notify('deviceinsert', payload);\n" +
                "  RETURN NEW;\n" +
                "END;\n" +
                "$$ LANGUAGE plpgsql;");
        executeCommand("CREATE TRIGGER \"deviceNotify\"\n" +
                "AFTER INSERT ON device\n" +
                "FOR EACH ROW EXECUTE PROCEDURE \"deviceNotify\"()");

        // deviceStatusNotify
        // when a DeviceStatus is inserted
        executeCommand("CREATE OR REPLACE FUNCTION \"deviceStatusNotify\"()\n" +
                "  RETURNS TRIGGER AS $$\n" +
                "DECLARE\n" +
                "  payload TEXT;\n" +
                "BEGIN\n" +
                "  payload := NEW.id;\n" +
                "  PERFORM pg_notify('devicestatusinsert', payload);\n" +
                "  RETURN NEW;\n" +
                "END;\n" +
                "$$ LANGUAGE plpgsql;");
        executeCommand("CREATE TRIGGER \"deviceStatusNotify\"\n" +
                "AFTER INSERT ON device_status\n" +
                "FOR EACH ROW EXECUTE PROCEDURE \"deviceStatusNotify\"()");

        // alertHistoryNotify
        // when an AlertHistory is inserted
        executeCommand("CREATE OR REPLACE FUNCTION \"alertHistoryNotify\"()\n" +
                "  RETURNS TRIGGER AS $$\n" +
                "DECLARE\n" +
                "  payload TEXT;\n" +
                "BEGIN\n" +
                "  payload := NEW.id;\n" +
                "  PERFORM pg_notify('alerthistoryinsert', payload);\n" +
                "  RETURN NEW;\n" +
                "END;\n" +
                "$$ LANGUAGE plpgsql;");
        executeCommand("CREATE TRIGGER \"alertHistoryNotify\"\n" +
                "AFTER INSERT ON alert_history\n" +
                "FOR EACH ROW EXECUTE PROCEDURE \"alertHistoryNotify\"()");
    }

    /**
     * Lists all postgres databases. Primarily for testing.
     */
    public static CompletionStage<Void> listAllDatabases() {
        return CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement ps = dbConn
                        .prepareStatement("SELECT datname FROM pg_database WHERE datistemplate = false;");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    logger.info("Database: " + rs.getString(1));
                }
                rs.close();
                ps.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /*
        Generic DB Actions
    */

    /**
     * Finds a database entry in a given table by id
     * @param id id of the entry to find
     * @param tableName name of the table to search
     * @return the resultset of the query if something is found, null otherwise
     */
    private static CompletionStage<ResultSet> findById(int id, String tableName){
        return CompletableFuture.supplyAsync(() -> {
            logger.info(String.format("Finiding by id = %d in %s", id, tableName));
            PreparedStatement st = null;
            ResultSet rs = null;
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            try{
                st = dbConn.prepareStatement(String.format("SELECT * FROM %s WHERE id = ?", tableName));
                st.setInt(1, id);
                rs = st.executeQuery();
                // Moves the result set to the first row if it exists. Returns null otherwise.
                if(!rs.next()) {
                    return null;
                }
                // closes rs. Need to close it somewhere else
//            st.close();
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.severe("Exception finding by ID: " + e.getClass().getName()+": "+e.getMessage());
            }
            return rs;
        });
    }


    /**
     * Uses Postgresql pg_get_serial_sequence to select the most recent id for the given table
     * @param tableName The table to get the latest id
     * @return The latest id on success or -1 on failure
     */
    private static int getLatestId(String tableName){
        try{
            int serialNum = 0;
            Statement stmt = dbConn.createStatement();
//            String query = String.format("select currval(pg_get_serial_sequence(%s, 'id')", tableName);
            String query = String.format("select currval('%s_id_seq')", tableName);
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                return rs.getInt(1);
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("Error getting most recent in table: "+tableName);
        }
        return -1;
    }

    /**
     * Finds all entries in a given table.
     * @param tableName name of the table.
     * @return a list of all entries in the table.
     */
    private static ResultSet getAllFromTable(String tableName){
        ResultSet rs = null;
        if(dbConn == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try{
            Statement st = dbConn.createStatement();
            rs = st.executeQuery("SELECT * FROM " + tableName);
            // This line closes the result set too
//            st.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting all entries: " + e.getClass().getName()+": "+e.getMessage());
        }
        return rs;
    }

    /**
     * Saves given tag/type/group to the database.
     * @param name name of the tag/type/group to be inserted.
     * @param table one of tag, type, or group where the name should be inserted.
     * @return auto incremented id
     */
    public static CompletionStage<Integer> addRowToTable(String table, String name) {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            try {
                st = dbConn.prepareStatement(String.format("INSERT INTO %s (name) VALUES (?)", table));
                st.setString(1, name);
                st.executeUpdate();

                int serialNum = 0;
                Statement stmt = dbConn.createStatement();

                // get the postgresql serial field value with this query
                String query = String.format("select currval('%s_id_seq')", table);
                ResultSet rs = stmt.executeQuery(query);
                if ( rs.next() )
                {
                    serialNum = rs.getInt(1);
                    return serialNum;
                }
                stmt.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
                logger.severe("Error adding row to table: " + e.getClass().getName()+": "+e.getMessage());
            }
            finally {
                try { if(st != null) st.close(); } catch (Exception e) {}
            }
            return -1;
        });
    }

    /**
     * Delete a row with the given id from the given table
     * @param table The name of the table for the row to be deleted
     * @param id The id of the row to be deleted
     * @return True if the deletion was successful
     */
    public static CompletionStage<Boolean> deleteById(String table, int id) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info(String.format("Deleting by id = %d in %s", id, table));
            PreparedStatement st = null;
            try {
                st = dbConn.prepareStatement(String.format("DELETE FROM %s WHERE id = ?", table));
                st.setInt(1, id);
                st.executeUpdate();
                return true;
            } catch (SQLException e) {
            } finally {
                try { if (st != null) st.close(); } catch (Exception e) {}
            }
            return false;
        });
    }

    /*
     *      AlertHistory specific actions.
     */
    /**
     * Finds an AlertHistory from the databse with the given id
     * @param id The id of the desired AlertHistory
     * @return An AlertHistory with desired id
     */
    public static CompletionStage<AlertHistory> findAlertHistory(int id) {
        return findById(id, "alert_history").thenApplyAsync(rs -> {
            if(rs == null) {
                return null;
            } else {
                return rsToAlertHistory(rs);
            }
        });
    }

    /**
     * Finds all AlertHistories from the database for the given list of UmboxInstance alerterIds.
     * @param alerterIds a list of alerterIds of UmboxInstances.
     * @return a list of all AlertHistories in the database where the the alert was created by a UmboxInstance with
     *         alerterId in alerterIds.
     */
    public static CompletionStage<List<AlertHistory>> findAlertHistories(List<String> alerterIds) {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            List<AlertHistory> alertHistories = new ArrayList<AlertHistory>();
            for(String alerterId : alerterIds) {
                try {
                    st = dbConn.prepareStatement("SELECT * FROM alert_history WHERE alerter_id = ?");
                    st.setString(1, alerterId);
                    rs = st.executeQuery();
                    while (rs.next()) {
                        alertHistories.add(rsToAlertHistory(rs));
                    }
                } catch (SQLException e) {
                    logger.severe("Sql exception getting all alert histories: " + e.getClass().getName() + ": " + e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("Error getting alert histories: " + e.getClass().getName() + ": " + e.getMessage());
                }  finally {
                    try { if (rs != null) { rs.close(); } } catch (Exception e) {}
                    try { if (st != null) { st.close(); } } catch (Exception e) {}
                }
            }
            return alertHistories;
        });

    }

    /**
     * Extract an AlertHistory from the result set of a database query.
     * @param rs ResultSet from a AlertHistory query.
     * @return The AlertHistory that was found.
     */
    private static AlertHistory rsToAlertHistory(ResultSet rs){
        AlertHistory alertHistory = null;
        try {
            int id = rs.getInt("id");
            Timestamp timestamp = rs.getTimestamp("timestamp");
            String alerterId = rs.getString("alerter_id");
            String name = rs.getString("info");
            alertHistory = new AlertHistory(id, timestamp, alerterId, name);
        }
        catch(Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to AlertHistory: " + e.getClass().getName()+": "+e.getMessage());
        }
        return alertHistory;
    }

    /**
     * Insert a row into the alert_history table
     * @param alertHistory The AlertHistory to be added
     * @return id of new AlertHistory on success. -1 on error
     */
    public static CompletionStage<Integer> insertAlertHistory(AlertHistory alertHistory) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Inserting alert_history: " + alertHistory.toString());
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try {
                PreparedStatement insertAlert = dbConn.prepareStatement("INSERT INTO alert_history(timestamp, alerter_id, info) VALUES (?,?,?);");
                insertAlert.setTimestamp(1, alertHistory.getTimestamp());
                insertAlert.setString(2, alertHistory.getAlerterId());
                insertAlert.setString(3, alertHistory.getInfo());
                insertAlert.executeUpdate();
                return getLatestId("alert_history");
            } catch (SQLException e) {
                e.printStackTrace();
                logger.severe("Error inserting AlertHistory: " + e.getClass().getName() + ": " + e.getMessage());
            }
            return -1;
        });
    }

    /**
     * Updates AlertHistory with given id to have the parameters of the given AlertHistory.
     * @param alertHistory AlertHistory holding new parameters to be saved in the database.
     * @return the id of the updated AlertHistory on success. -1 on failure
     */
    public static CompletionStage<Integer> updateAlertHistory(AlertHistory alertHistory) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info(String.format("Updating AlertHistory with id = %d with values: %s", alertHistory.getId(), alertHistory));
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            } else {
                try {
                    PreparedStatement update = dbConn.prepareStatement("UPDATE alert_history " +
                            "SET timestamp = ?, alerter_id = ?, info = ?" +
                            "WHERE id = ?");
                    update.setTimestamp(1, alertHistory.getTimestamp());
                    update.setString(2, alertHistory.getAlerterId());
                    update.setString(3, alertHistory.getInfo());
                    update.setInt(4, alertHistory.getId());
                    update.executeUpdate();

                    return alertHistory.getId();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("Error updating AlertHistory: " + e.getClass().toString() + ": " + e.getMessage());
                }
            }
            return -1;
        });
    }

    /**
     * Deletes an AlertHistory by its id.
     * @param id id of the AlertHistory to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static CompletionStage<Boolean> deleteAlertHistory(int id) {
        return deleteById("alert_history", id);
    }

    /*
     *       Device specific actions
     */
    /**
     * Finds a Device from the database by its id.
     * @param id id of the Device to find.
     * @return the Device if it exists in the database, else null.
     */
    public static CompletionStage<Device> findDevice(int id){
        logger.info("Finding device with id = " + id);
        return findById(id, "device").thenApplyAsync(rs -> {
            if(rs == null) {
                return null;
            } else {
                Device device = rsToDevice(rs);
                List<Integer> tagIds = findTagIds(device.getId());
                device.setTagIds(tagIds);

                SecurityState ss = findSecurityStateByDevice(device.getId());
                device.setCurrentState(ss);

                return device;
            }
        });
    }

    /**
     * Finds all Devices in the database.
     * @return a list of all Devices in the database.
     */
    public static CompletionStage<List<Device>> findAllDevices(){
        return CompletableFuture.supplyAsync(() -> {
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            List<Device> devices = new ArrayList<Device>();
            try{
                ResultSet rs = getAllFromTable("device");
                while (rs.next()) {
                    Device d = rsToDevice(rs);
                    List<Integer> tagIds = findTagIds(d.getId());
                    d.setTagIds(tagIds);
                    SecurityState ss = findSecurityStateByDevice(d.getId());
                    d.setCurrentState(ss);

                    devices.add(d);
                }
                rs.close();
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting all Devices: " + e.getClass().getName()+": "+e.getMessage());
            }
            return devices;
        });
    }

    /**
     * Extract a Device from the result set of a database query.
     * @param rs ResultSet from a Device query.
     * @return The Device that was found.
     */
    private static Device rsToDevice(ResultSet rs){
        Device device = null;
        try{
            int id = rs.getInt(1);
            String name = rs.getString(2);
            String description = rs.getString(3);
            int typeId = rs.getInt(4);
            int groupId = rs.getInt(5);
            String ip = rs.getString(6);
            int statusHistorySize = rs.getInt(7);
            int samplingRate = rs.getInt(8);

            device = new Device(id, name, description, typeId, groupId, ip, statusHistorySize, samplingRate);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to Device: " + e.getClass().getName()+": "+e.getMessage());
        }
        return device;
    }

    /**
     * Saves given Device to the database.
     * @param device Device to be inserted.
     * @return auto incremented id
     */
    public static CompletionStage<Integer> insertDevice(Device device){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Inserting device: " + device);
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try{
                PreparedStatement update = dbConn.prepareStatement
                        ("INSERT INTO device(description, name, type_id, group_id, ip_address," +
                                "status_history_size, sampling_rate) values(?,?,?,?,?,?,?)");
                update.setString(1, device.getDescription());
                update.setString(2, device.getName());
                update.setInt(3, device.getTypeId());
                update.setInt(4, device.getGroupId());
                update.setString(5, device.getIp());
                update.setInt(6, device.getStatusHistorySize());
                update.setInt(7, device.getSamplingRate());

                update.executeUpdate();
                int serialNum = getLatestId("device");
                //Insert tags into device_tag
                List<Integer> tagIds = device.getTagIds();
                if(tagIds != null) {
                    for(int tagId : tagIds) {
                        executeCommand(String.format("INSERT INTO device_tag(device_id, tag_id) values (%d,%d)", serialNum, tagId));
                    }
                }
                return serialNum;
            }
            catch(Exception e){
                e.printStackTrace();
                logger.severe("Error inserting Device: " + e.getClass().getName()+": "+e.getMessage());
            }
            return -1;
        });
    }

    /**
     * First, attempts to find the Device in the database.
     * If successful, updates the existing Device with the given Device's parameters Otherwise,
     * inserts the given Device.
     * @param device Device to be inserted or updated.
     */
    public static CompletionStage<Integer> insertOrUpdateDevice(Device device){
        return findDevice(device.getId()).thenApplyAsync(d -> {
            if(d == null) {
                insertDevice(device);
                return 0;
            } else {
                updateDevice(device);
                return 1;
            }
        });
    }

    /**
     * Updates Device with given id to have the parameters of the given Device.
     * @param device Device holding new parameters to be saved in the database.
     * @return the id of the updated device
     */
    public static CompletionStage<Integer> updateDevice(Device device) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info(String.format("Updating Device with id = %d with values: %s", device.getId(), device));
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            } else {
                try {
                    // Delete existing tags
                    executeCommand(String.format("DELETE FROM device_tag WHERE device_id = %d", device.getId()));

                    PreparedStatement update = dbConn.prepareStatement("UPDATE device " +
                            "SET name = ?, description = ?, type_id = ?, group_id = ?, ip_address = ?, status_history_size = ?, sampling_rate = ?, current_state_id = ?" +
                            "WHERE id = ?");
                    update.setString(1, device.getName());
                    update.setString(2, device.getDescription());
                    update.setInt(3, device.getTypeId());
                    update.setInt(4, device.getGroupId());
                    update.setString(5, device.getIp());
                    update.setInt(6, device.getStatusHistorySize());
                    update.setInt(7, device.getSamplingRate());

                    if(device.getCurrentState() != null){
                        update.setInt(8, device.getCurrentState().getId());
                    } else {
                        update.setInt(8, -1);
                    }


                    update.setInt(9, device.getId());
                    update.executeUpdate();

                    // Insert tags into device_tag
                    List<Integer> tagIds = device.getTagIds();
                    if(tagIds != null) {
                        for(int tagId : tagIds) {
                            executeCommand(String.format("INSERT INTO device_tag(device_id, tag_id) values (%d,%d)", device.getId(), tagId));
                        }
                    }
                    return device.getId();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("Error updating Device: " + e.getClass().getName() + ": " + e.getMessage());
                }
            }
            return -1;
        });
    }

    /**
     * Deletes a Device by its id.
     * @param id id of the Device to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static CompletionStage<Boolean> deleteDevice(int id) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info(String.format("Deleting device with id = %d", id));
            PreparedStatement st = null;
            try {
                // Delete associated tags
                executeCommand(String.format("DELETE FROM device_tag WHERE device_id = %d", id));
                deleteById("device", id);
                return true;
            }
            finally {
                try { if (st != null) st.close(); } catch (Exception e) {}
            }
        });
    }

    /*
     *     DeviceStatus specific actions
     */
    /**
     * Finds a DeviceStatus from the database by its id.
     * @param id id of the DeviceStatus to find.
     * @return the DeviceStatus if it exists in the database, else null.
     */
    public static CompletionStage<DeviceStatus> findDeviceStatus(int id){
        return findById(id, "device_status").thenApplyAsync(rs -> {
            if(rs == null) {
                return null;
            } else {
                return rsToDeviceStatus(rs);
            }
        });
    }

    /**
     * Finds all DeviceStatuses from the database for the given device.
     * @param deviceId the id of the device.
     * @return a list of all DeviceStatuses in the database where the device_id field is equal to deviceId.
     */
    public static CompletionStage<List<DeviceStatus>> findDeviceStatuses(int deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            try {
                st = dbConn.prepareStatement("SELECT * FROM device_status WHERE device_id = ?");
                st.setInt(1, deviceId);
                rs = st.executeQuery();

                List<DeviceStatus> deviceHistories = new ArrayList<DeviceStatus>();
                while (rs.next()) {
                    deviceHistories.add(rsToDeviceStatus(rs));
                }
                return deviceHistories;
            } catch (SQLException e) {
                logger.severe("Sql exception getting all device statuses: " + e.getClass().getName() + ": " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting device statuses: " + e.getClass().getName() + ": " + e.getMessage());
            } finally {
                try { if (rs != null) { rs.close(); } } catch (Exception e) { }
                try { if (st != null) { st.close(); } } catch (Exception e) { }
            }
            return null;
        });
    }

    /**
     * Finds the last N DeviceStatuses for the given device
     * @param deviceId the id of the device
     * @param N the number of statuses to retrieve
     * @return a list of N device statuses
     */
    public static CompletionStage<List<DeviceStatus>> findNDeviceStatuses(int deviceId, int N){
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            try {
                logger.info("Finding last "+N+"device statuses for device: "+deviceId);
                st = dbConn.prepareStatement("SELECT * FROM device_status WHERE device_id = ? ORDER BY id DESC LIMIT ?");
                st.setInt(1, deviceId);
                st.setInt(2, N);
                rs = st.executeQuery();

                List<DeviceStatus> deviceHistories = new ArrayList<DeviceStatus>();
                while (rs.next()) {
                    deviceHistories.add(rsToDeviceStatus(rs));
                }
                return deviceHistories;
            } catch (SQLException e) {
                logger.severe("Sql exception getting all device statuses: " + e.getClass().getName() + ": " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting device statuses: " + e.getClass().getName() + ": " + e.getMessage());
            } finally {
                try { if (rs != null) { rs.close(); } } catch (Exception e) { }
                try { if (st != null) { st.close(); } } catch (Exception e) { }
            }
            return null;
        });
    }

    /**
     * Finds the last N DeviceStatuses for the given device
     * @param deviceId the id of the device
     * @param N the number of statuses to retrieve
     * @param timeUnit the unit of time to use (minute(s), hour(s), day(s))
     * @return a list of N device statuses
     */
    public static CompletionStage<List<DeviceStatus>> findDeviceStatusesOverTime(int deviceId, int length, String timeUnit){
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            try {
//                st = dbConn.prepareStatement("SELECT * FROM device_status WHERE device_id = ? AND timestamp between now() and (now() - '? ?'::interval) ORDER BY DESC");
//                st.setInt(1, deviceId);
//                st.setInt(2, length);
//                st.setString(3, timeUnit);
                st = dbConn.prepareStatement("SELECT * FROM device_status WHERE device_id = ? ORDER BY id DESC");
                st.setInt(1, deviceId);
                rs = st.executeQuery();

                List<DeviceStatus> deviceHistories = new ArrayList<DeviceStatus>();
                while (rs.next()) {
                    deviceHistories.add(rsToDeviceStatus(rs));
                }
                return deviceHistories;
            } catch (SQLException e) {
                logger.severe("Sql exception getting all device statuses: " + e.getClass().getName() + ": " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting device statuses: " + e.getClass().getName() + ": " + e.getMessage());
            } finally {
                try { if (rs != null) { rs.close(); } } catch (Exception e) { }
                try { if (st != null) { st.close(); } } catch (Exception e) { }
            }
            return null;
        });
    }

    /**
     * Returns a list of device statuses for devices with the given type id. One device status per device
     * @param typeId The typeid for the requested devices
     * @return A map pairing a device with its most recent DeviceStatus
     */

    public static CompletionStage<Map<Device, DeviceStatus>> findDeviceStatusesByType(int typeId){
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            Map<Device, DeviceStatus> deviceStatusMap = new HashMap<Device, DeviceStatus>();
            try {
                st = dbConn.prepareStatement("SELECT * FROM device WHERE type_id = ?");
                st.setInt(1, typeId);
                rs = st.executeQuery();
                while (rs.next()) {
                    Device device = rsToDevice(rs);
                    PreparedStatement statement = dbConn.prepareStatement("SELECT * FROM device_status WHERE device_id = ? ORDER BY id DESC LIMIT 1");
                    statement.setInt(1, device.getId());
                    ResultSet resultSet = statement.executeQuery();

                    DeviceStatus deviceStatus = null;
                    while(resultSet.next()){
                        deviceStatus = rsToDeviceStatus(resultSet);
                    }
                    deviceStatusMap.put(device, deviceStatus);
                }
            } catch (SQLException e) {
                logger.severe("Sql exception getting devices for type: "+ typeId+" "+ e.getClass().getName() + ": " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting devices for type: "+ typeId+" " + e.getClass().getName() + ": " + e.getMessage());
            }  finally {
                try { if (rs != null) { rs.close(); } } catch (Exception e) {}
                try { if (st != null) { st.close(); } } catch (Exception e) {}
            }
            return deviceStatusMap;
        });
    }

    /**
     * Finds all DeviceHistories in the database.
     * @return a list of all DeviceHistories in the database.
     */
    public static CompletionStage<List<DeviceStatus>> findAllDeviceStatuses() {
        return CompletableFuture.supplyAsync(() -> {
            ResultSet rs = getAllFromTable("device_status");
            List<DeviceStatus> deviceHistories = new ArrayList<DeviceStatus>();
            try {
                while (rs.next()) {
                    deviceHistories.add(rsToDeviceStatus(rs));
                }
                rs.close();
            } catch (SQLException e) {
                logger.severe("Sql exception getting all device histories.");
            }
            return deviceHistories;
        });
    }

    /**
     * Extract a DeviceStatus from the result set of a database query.
     * @param rs ResultSet from a DeviceStatus query.
     * @return The DeviceStatus that was found.
     */
    private static DeviceStatus rsToDeviceStatus(ResultSet rs){
        DeviceStatus deviceStatus = null;
        try{
            logger.severe("Column count: "+rs.getMetaData().getColumnCount());
            int deviceId = rs.getInt("device_id");
            Map<String, String> attributes = HStoreConverter.fromString(rs.getString("attributes"));
            Timestamp timestamp = rs.getTimestamp("timestamp");
            int statusId = rs.getInt("id");

            deviceStatus = new DeviceStatus(deviceId, attributes, timestamp, statusId);
        }
        catch(SQLException e){
            e.printStackTrace();
            logger.severe("Error converting rs to DeviceStatus: " + e.getClass().getName()+": "+e.getMessage());
        }
        return deviceStatus;
    }

    /**
     * Saves given DeviceStatus to the database.
     * @param deviceStatus DeviceStatus to be inserted.
     * @return auto incremented id
     */
    public static CompletionStage<Integer> insertDeviceStatus(DeviceStatus deviceStatus){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Inserting device_status: " + deviceStatus.toString());
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try{
                PreparedStatement update = dbConn.prepareStatement
                        ("INSERT INTO device_status(device_id, timestamp, attributes) values(?,?,?)");
                update.setInt(1, deviceStatus.getDeviceId());
                update.setTimestamp(2, deviceStatus.getTimestamp());
                update.setObject(3, deviceStatus.getAttributes());
                update.executeUpdate();

                return getLatestId("device_status");
            }
            catch(Exception e){
                e.printStackTrace();
                logger.severe("Error inserting DeviceStatus: " + e.getClass().getName()+": "+e.getMessage());
            }
            return -1;
        });
    }


    /**
     * First, attempts to find the DeviceStatus in the database.
     * If successful, updates the existing DeviceStatus with the given DeviceStatus's parameters Otherwise,
     * inserts the given DeviceStatus.
     * @param deviceStatus DeviceStatus to be inserted or updated.
     */
    public static CompletionStage<Integer> insertOrUpdateDeviceStatus(DeviceStatus deviceStatus){
        return findDeviceStatus(deviceStatus.getId()).thenApplyAsync(d -> {
            if(d == null) {
                insertDeviceStatus(deviceStatus);
                return 0;
            } else {
                updateDeviceStatus(deviceStatus);
                return 1;
            }
        });
    }

    /**
     * Updates DeviceStatus with given id to have the parameters of the given DeviceStatus.
     * @param deviceStatus DeviceStatus holding new parameters to be saved in the database.
     */
    public static CompletionStage<Integer> updateDeviceStatus(DeviceStatus deviceStatus){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Updating DeviceStatus with id=" + deviceStatus.getId());
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try {
                PreparedStatement update = dbConn.prepareStatement
                        ("UPDATE device_status SET device_id = ?, attributes = ?, timestamp = ? " +
                                "WHERE id=?");

                update.setInt(1, deviceStatus.getDeviceId());
                update.setObject(2, deviceStatus.getAttributes());
                update.setTimestamp(3, deviceStatus.getTimestamp());
                update.setInt(4, deviceStatus.getId());
                update.executeUpdate();
                return deviceStatus.getId();
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error updating DeviceStatus: " + e.getClass().getName() + ": " + e.getMessage());
                return -1;
            }
        });
    }

    /**
     * Deletes an DeviceStatus by its id.
     * @param id id of the DeviceStatus to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static CompletionStage<Boolean> deleteDeviceStatus(int id) {
        return deleteById("device_status", id);
    }

    /*
     *      Group specific actions
     */
    /**
     * Finds a Group from the database by its id.
     * @param id id of the Group to find.
     * @return the Group if it exists in the database, else null.
     */
    public static CompletionStage<Group> findGroup(int id) {
        return findById(id, "device_group").thenApplyAsync(rs -> {
            if(rs == null) {
                return null;
            } else {
                Group group = rsToGroup(rs);
                return group;
            }
        });
    }

    /**
     * Finds all Groups in the database.
     * @return a list of all Groups in the database.
     */
    public static CompletionStage<List<Group>> findAllGroups() {
        return CompletableFuture.supplyAsync(() -> {
            ResultSet rs = getAllFromTable("device_group");
            List<Group> groups = new ArrayList<Group>();
            try {
                while (rs.next()) {
                    groups.add(rsToGroup(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                logger.severe("SQLException getting all Groups: " + e.getClass().getName()+": "+e.getMessage());
            }
            finally {
                try { if(rs != null) rs.close(); } catch (Exception e) {}
            }
            return groups;
        });
    }

    /**
     * Extract a Group from the result set of a database query.
     * @param rs ResultSet from a Group query.
     * @return The first Group in rs.
     */
    private static Group rsToGroup(ResultSet rs){
        Group group = null;
        try{
            int id = rs.getInt("id");
            String name = rs.getString("name");
            group = new Group(id, name);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to Group: " + e.getClass().getName()+": "+e.getMessage());
        }
        return group;
    }

    /**
     * Saves given Device Group to the database.
     * @param device Device Group to be inserted.
     * @return auto incremented id
     */
    public static CompletionStage<Integer> insertGroup(Group group){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Inserting group: " + group.getName());
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try{
                PreparedStatement update = dbConn.prepareStatement
                        ("INSERT INTO device_group(name)" +
                                "values(?)");
                update.setString(1, group.getName());
                update.executeUpdate();
                return getLatestId("device_group");
            }
            catch(Exception e){
                e.printStackTrace();
                logger.severe("Error inserting Group: " + e.getClass().getName()+": "+e.getMessage());
            }
            return -1;
        });
    }

    /**
     * Updates Group with given id to have the parameters of the given Group.
     * @param group group holding new parameters to be saved in the database.
     */
    public static CompletionStage<Integer> updateGroup(Group group){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Updating Group with id=" + group.getId());
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try {
                PreparedStatement update = dbConn.prepareStatement
                        ("UPDATE device_group SET name = ?" +
                                "WHERE id=?");

                update.setString(1, group.getName());
                update.setInt(2, group.getId());
                update.executeUpdate();
                return group.getId();
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error updating Group: " + e.getClass().getName() + ": " + e.getMessage());
                return -1;
            }
        });
    }

    /**
     * Deletes a Group by its id.
     * @param id id of the Group to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static CompletionStage<Boolean> deleteGroup(int id) {
        return deleteById("device_group", id);
    }

    /*
     *      SecurityState specific actions
     */

    /**
     * Finds the most recent SecurityState from the database for the given device
     * @param deviceId the id of the device
     * @return the most recent SecurityState entered for a device
     */
    public static SecurityState findSecurityState(int id){
//        return CompletableFuture.supplyAsync(() -> {
        PreparedStatement st = null;
        ResultSet rs = null;
        SecurityState ss = new SecurityState();

        if(dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return ss;
        }

        try{
            st = dbConn.prepareStatement("SELECT * FROM security_state WHERE id = ? ");
            st.setInt(1, id);
            rs = st.executeQuery();

            while(rs.next()){
                ss = rsToSecurityState(rs);
            }
        }
        catch (SQLException e){
            logger.severe("SQL exception getting the security state: "+e.getClass().getName()+": "+e.getMessage());
        }
        catch (Exception e){
            e.printStackTrace();
            logger.severe("Error getting security state: " + e.getClass().getName()+": "+e.getMessage());
        }
        finally {
            try { if(rs != null) { rs.close(); } } catch(Exception e) {}
            try { if(st != null) { st.close(); } } catch(Exception e) {}
        }
        return ss;
//        });
    }

    /**
     * Finds the most recent SecurityState from the database for the given device
     * @param deviceId the id of the device
     * @return the most recent SecurityState entered for a device
     */
    public static SecurityState findSecurityStateByDevice(int deviceId){
//        return CompletableFuture.supplyAsync(() -> {
           PreparedStatement st = null;
           ResultSet rs = null;
           SecurityState ss = new SecurityState();

           if(dbConn == null) {
               logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
               return null;
           }

           try{
               st = dbConn.prepareStatement("SELECT * FROM security_state WHERE id = (SELECT MAX(id) FROM security_state WHERE device_id=?) ");
               st.setInt(1, deviceId);
               rs = st.executeQuery();

               while(rs.next()){
                   ss = rsToSecurityState(rs);
               }
           }
           catch (SQLException e){
               logger.severe("SQL exception getting the security state: "+e.getClass().getName()+": "+e.getMessage());
           }
           catch (Exception e){
               e.printStackTrace();
               logger.severe("Error getting security state: " + e.getClass().getName()+": "+e.getMessage());
           }
           finally {
               try { if(rs != null) { rs.close(); } } catch(Exception e) {}
               try { if(st != null) { st.close(); } } catch(Exception e) {}
           }
            return ss;
//        });
    }

    /**
     * Finds all SecurityState from the database for the given device.
     * @param deviceId the id of the device.
     * @return a list of all SecurityStates in the database where the device_id field is equal to deviceId.
     */
    public static CompletionStage<List<SecurityState>> findSecurityStates(int deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            List<SecurityState> securityStateList = new ArrayList<SecurityState>();

            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            try{
                st = dbConn.prepareStatement("SELECT * FROM security_state WHERE device_id = ?");
                st.setInt(1, deviceId);
                rs = st.executeQuery();

                while (rs.next()) {
                    securityStateList.add(rsToSecurityState(rs));
                }
            }
            catch(SQLException e) {
                logger.severe("Sql exception getting all security states: " + e.getClass().getName()+": "+e.getMessage());
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting security states: " + e.getClass().getName()+": "+e.getMessage());
            }
            finally {
                try { if(rs != null) { rs.close(); } } catch(Exception e) {}
                try { if(st != null) { st.close(); } } catch(Exception e) {}
            }
            return securityStateList;
        });
    }

    /**
     * Extract a SecurityState from the result set of a database query.
     * @param rs ResultSet from a SecurityState query.
     * @return The SecurityState that was found.
     */
    private static SecurityState rsToSecurityState(ResultSet rs){
        SecurityState securityState = null;
        try{
            int id = rs.getInt("id");
            int deviceId = rs.getInt("device_id");
            Timestamp timestamp = rs.getTimestamp("timestamp");
            String state = rs.getString("state");
            securityState = new SecurityState(id, deviceId, timestamp, state);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to SecurityState: " + e.getClass().getName()+": "+e.getMessage());
        }
        return securityState;
    }

    /**
     * Saves given SecurityState to the database.
     * @param securityState SecurityState to be inserted.
     * @return auto incremented id
     */
    public static CompletionStage<Integer> insertSecurityState(SecurityState securityState){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Inserting SecurityState: " + securityState.getId());
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try{
                PreparedStatement update = dbConn.prepareStatement
                        ("INSERT INTO security_state(device_id, timestamp, state)" +
                                "values(?,?,?)");
                update.setInt(1, securityState.getDeviceId());
                update.setTimestamp(2, securityState.getTimestamp());
                update.setString(3, securityState.getState());
                update.executeUpdate();
                return getLatestId("security_state");
            }
            catch(Exception e){
                e.printStackTrace();
                logger.severe("Error inserting SecurityState: " + e.getClass().getName()+": "+e.getMessage());
            }
            return -1;
        });
    }

    /**
     * Updates SecurityState with given id to have the parameters of the given SecurityState.
     * @param securityState SecurityState holding new parameters to be saved in the database.
     */
    public static CompletionStage<Integer> updateSecurityState(SecurityState securityState){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Updating SecurityState with id=" + securityState.getId());
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try {
                PreparedStatement update = dbConn.prepareStatement
                        ("UPDATE security_state SET device_id = ?, timestamp = ?, state = ?" +
                                "WHERE id=?");
                update.setInt(1, securityState.getDeviceId());
                update.setTimestamp(2, securityState.getTimestamp());
                update.setString(3, securityState.getState());
                update.setInt(4, securityState.getId());
                update.executeUpdate();
                return securityState.getId();
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error updating SecurityState: " + e.getClass().toString() + ": " + e.getMessage());
            }
            return -1;
        });
    }

    /**
     * Deletes a SecurityState by its id.
     * @param id id of the SecurityState to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static CompletionStage<Boolean> deleteSecurityState(int id) {
        return deleteById("security_state", id);
    }

    /*
     *      Tag specific actions
     */

    /**
     * Find the respective tag ids for given device id
     * @param deviceId The device id the tags are for
     * @return A list of tag ids or null
     */
    private static List<Integer> findTagIds(int deviceId){
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = dbConn.prepareStatement("SELECT * FROM device_tag WHERE device_id = ?");
            st.setInt(1, deviceId);
            rs = st.executeQuery();

            List<Integer> tagIds = new ArrayList<Integer>();
            while(rs.next()) {
                tagIds.add(rs.getInt(2));
            }
            return tagIds;
        }
        catch(SQLException e) {
            e.printStackTrace();
            logger.severe("Error finding tags by device_id: " + deviceId +": "+e.getMessage());
        }
        finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (st != null) st.close(); } catch (Exception e) {}
        }
        return null;
    }

    /**
     * Finds all Tags in the database.
     * @return a list of all Tags in the database.
     */
    public static CompletionStage<List<Tag>> findAllTags() {
        return CompletableFuture.supplyAsync(() -> {
            ResultSet rs = getAllFromTable("tag");
            List<Tag> tags = new ArrayList<Tag>();
            try {
                while (rs.next()) {
                    tags.add(rsToTag(rs));
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
                logger.severe("SQLException getting all Tags: " + e.getClass().getName()+": "+e.getMessage());
            }
            finally {
                try { if (rs != null) rs.close(); } catch(Exception e) {}
            }
            return tags;
        });
    }

    /**
     * Extract a Tag from the result set of a database query.
     * @param rs ResultSet from a Tag query.
     * @return The first Tag in rs.
     */
    private static Tag rsToTag(ResultSet rs){
        Tag tag = null;
        try{
            int id = rs.getInt("id");
            String name = rs.getString("name");
            tag = new Tag(id, name);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to Tag: " + e.getClass().getName()+": "+e.getMessage());
        }
        return tag;
    }

    /**
     * Saves given Tag to the database.
     * @param tag Tag to be inserted.
     * @return auto incremented id
     */
    public static CompletionStage<Integer> insertTag(Tag tag){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Inserting Tag: " + tag.getId());
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try{
                PreparedStatement update = dbConn.prepareStatement
                        ("INSERT INTO tag(name)" +
                                "values(?)");
                update.setString(1, tag.getName());
                update.executeUpdate();
                return getLatestId("tag");
            }
            catch(Exception e){
                e.printStackTrace();
                logger.severe("Error inserting Tag: " + e.getClass().getName()+": "+e.getMessage());
            }
            return -1;
        });
    }

    /**
     * Updates Tag with given id to have the parameters of the given Tag.
     * @param tag Tag holding new parameters to be saved in the database.
     */
    public static CompletionStage<Integer> updateTag(Tag tag){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Updating Tag with id=" + tag.getId());
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try {
                PreparedStatement update = dbConn.prepareStatement
                        ("UPDATE tag SET name = ?" +
                                "WHERE id=?");
                update.setString(1, tag.getName());
                update.setInt(2, tag.getId());
                update.executeUpdate();
                return tag.getId();
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error updating Tag: " + e.getClass().getName() + ": " + e.getMessage());
                return -1;
            }
        });
    }

    /**
     * Deletes a Tag by its id.
     * @param id id of the Tag to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static CompletionStage<Boolean> deleteTag(int id) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info(String.format("Deleting Tag with id = %d", id));
            PreparedStatement st = null;
            try {
                //remove references to tag from device
                st = dbConn.prepareStatement("DELETE FROM device_tag WHERE tag_id = ?");
                st.setInt(1, id);
                st.executeUpdate();
                //remove the tag itself
                deleteById("tag",id);
                return true;
            } catch (SQLException e) {
            } finally {
                try { if (st != null) st.close(); } catch (Exception e) {}
            }
            return false;
        });
    }

    /*
     *      Type specific actions
     */
    /**
     * Finds a Type from the database by its id.
     * @param id id of the Type to find.
     * @return the Type if it exists in the database, else null.
     */
    public static CompletionStage<Type> findType(int id) {
        return findById(id, "type").thenApplyAsync(rs -> {
            if(rs == null) {
                return null;
            } else {
                Type type = rsToType(rs);
                return type;
            }
        });
    }
    /**
     * Finds all Types in the database.
     * @return a list of all Types in the database.
     */
    public static CompletionStage<List<Type>> findAllTypes() {
        return CompletableFuture.supplyAsync(() -> {
            ResultSet rs = getAllFromTable("type");
            List<Type> types = new ArrayList<Type>();
            try {
                while (rs.next()) {
                    types.add(rsToType(rs));
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
                logger.severe("SQLException getting all Types: " + e.getClass().getName()+": "+e.getMessage());
            }
            finally {
                try { if(rs != null) rs.close(); } catch (Exception e) {}
            }
            return types;
        });
    }

    /**
     * Extract a Type from the result set of a database query.
     * @param rs ResultSet from a Type query.
     * @return The first Type in rs.
     */
    private static Type rsToType(ResultSet rs){
        Type type = null;
        try{
            int id = rs.getInt("id");
            String name = rs.getString("name");
            byte[] policyFile = rs.getBytes("policy_file");
            String policyFileName = rs.getString("policy_file_name");
            type = new Type(id, name, policyFile, policyFileName);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to Type: " + e.getClass().getName()+": "+e.getMessage());
        }
        return type;
    }

    /**
     * Saves given Type to the database.
     * @param type Type to be inserted.
     * @return auto incremented id
     */
    public static CompletionStage<Integer> insertType(Type type){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Inserting Type: " + type.getId());
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try{
                PreparedStatement update = dbConn.prepareStatement
                        ("INSERT INTO type(name, policy_file, policy_file_name)" +
                                "values(?,?,?)");
                update.setString(1, type.getName());
                update.setBytes(2, type.getPolicyFile());
                update.setString(3, type.getPolicyFileName());
                update.executeUpdate();
                return getLatestId("type");
            }
            catch(Exception e){
                e.printStackTrace();
                logger.severe("Error inserting Type: " + e.getClass().getName()+": "+e.getMessage());
            }
            return -1;
        });
    }

    /**
     * Updates Type with given id to have the parameters of the given Type.
     * @param type Type holding new parameters to be saved in the database.
     */
    public static CompletionStage<Integer> updateType(Type type){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Updating Type with id=" + type.getId());
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try {
                PreparedStatement update = dbConn.prepareStatement
                        ("UPDATE type SET name = ?, policy_file = ?, policy_file_name = ?" +
                                "WHERE id=?");
                update.setString(1, type.getName());
                update.setBytes(2, type.getPolicyFile());
                update.setString(3, type.getPolicyFileName());
                update.setInt(4, type.getId());
                update.executeUpdate();
                return type.getId();
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error updating Type: " + e.getClass().getName() + ": " + e.getMessage());
                return -1;
            }
        });
    }

    /**
     * Deletes a Type by its id.
     * @param id id of the Type to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static CompletionStage<Boolean> deleteType(int id) {
        return deleteById("type", id);
    }

    /*
     *      UmboxImage specific actions
     */

    /**
     * Find a UmboxImage based on its id
     * @param id ID of the desired UmboxImage
     * @return The desired UmboxImage on success or null on failure
     */
    public static CompletionStage<UmboxImage> findUmboxImage(int id) {
        return findById( id,"umbox_image").thenApplyAsync(rs -> {
            if( rs == null) {
                return null;
            } else {
                return rsToUmboxImage(rs);
            }
        });
    }

    /**
     * Finds all UmboxImages in the database.
     * @return a list of all UmboxImages in the database.
     */
    public static CompletionStage<List<UmboxImage>> findAllUmboxImages() {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Getting umbox images.");
            ResultSet rs = getAllFromTable("umbox_image");
            List<UmboxImage> umboxImages = new ArrayList<UmboxImage>();
            try {
                while (rs.next()) {
                    umboxImages.add(rsToUmboxImage(rs));
                }
                rs.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
                logger.severe("Sql exception getting all umbox images: " + e.getClass().getName()+": "+e.getMessage());
            }
            return umboxImages;
        });
    }

    /**
     * Extract a UmboxImage from the result set of a database query.
     * @param rs ResultSet from a UmboxImage query.
     * @return The first UmboxImage in rs.
     */
    private static UmboxImage rsToUmboxImage(ResultSet rs){
        UmboxImage umboxImage = null;
        try{
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String path = rs.getString("path");
            umboxImage = new UmboxImage(id, name, path);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to UmboxImage: " + e.getClass().getName()+": "+e.getMessage());
        }
        return umboxImage;
    }

    /**
     * Inserts given UmboxImage into the database
     * @param u the UmboxImage to be inserted
     * @return The id of the inserted UmboxImage on success or -1 on failure
     */
    public static CompletionStage<Integer> insertUmboxImage(UmboxImage u) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Adding umbox image: " + u);
            PreparedStatement st = null;
            try {
                st = dbConn.prepareStatement("INSERT INTO umbox_image (name, path) VALUES (?, ?)");
                st.setString(1, u.getName());
                st.setString(2, u.getPath());
                st.executeUpdate();
                return getLatestId("umbox_image");
            }
            catch (SQLException e) {
                e.printStackTrace();
                logger.severe("SQL exception adding umbox iamge: " + e.getClass().getName()+": "+e.getMessage());
            }
            finally {
                try { if(st != null) st.close(); } catch (Exception e) {}
            }
            return -1;
        });
    }

    /**
     * Updates given UmboxImage in the database
     * @param u the UmboxImage to be updated
     * @return The ID of the updated UmboxImage or -1 on failure
     */
    public static CompletionStage<Integer> updateUmboxImage(UmboxImage u) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Editing umbox image: " + u);
            PreparedStatement st = null;
            try {
                st = dbConn.prepareStatement("UPDATE umbox_image " +
                        "SET name = ?, path = ? " +
                        "WHERE id = ?");
                st.setString(1, u.getName());
                st.setString(2, u.getPath());
                st.setInt(3, u.getId());
                st.executeUpdate();
                return u.getId();
            }
            catch (SQLException e) {
                e.printStackTrace();
                logger.severe("SQL exception editing umbox iamge: " + e.getClass().getName()+": "+e.getMessage());
            }
            catch (NumberFormatException e) {}
            finally {
                try { if(st != null) st.close(); } catch (Exception e) {}
            }
            return -1;
        });
    }

    /**
     * Deletes a UmboxImage by its id.
     * @param id id of the UmboxImage to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static CompletionStage<Boolean> deleteUmboxImage(int id) {
        return deleteById("umbox_image", id);
    }

    /*
     *      UmboxInstance specific actions
     */
    /**
     * Find a umbox instance by its alerter id
     * @param id The ID of desired UmboxInstance
     * @return The desired UmboxInstance on success or null on failure
     */
    public static CompletionStage<UmboxInstance> findUmboxInstance(String alerterId){
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            try{
                st = dbConn.prepareStatement(String.format("SELECT * FROM umbox_instance WHERE alerter_id = ?"));
                st.setString(1, alerterId);
                rs = st.executeQuery();
                if(!rs.next()) {
                    return null;
                }
                return rsToUmboxInstance(rs);
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.severe("Exception finding by ID: " + e.getClass().getName()+": "+e.getMessage());
            }
            finally {
                try { if(rs != null) rs.close(); } catch (Exception e) {}
                try { if(st != null) st.close(); } catch (Exception e) {}
            }
            return null;
        });

    }

    /**
     * Finds all UmboxInstances from the database for the given device.
     * @param deviceId the id of the device.
     * @return a list of all UmboxInstaces in the database where the device_id field is equal to deviceId.
     */
    public static CompletionStage<List<UmboxInstance>> findUmboxInstances(int deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            try{
                st = dbConn.prepareStatement("SELECT * FROM umbox_instance WHERE device_id = ?");
                st.setInt(1, deviceId);
                rs = st.executeQuery();
                List<UmboxInstance> umboxInstances = new ArrayList<UmboxInstance>();
                while (rs.next()) {
                    umboxInstances.add(rsToUmboxInstance(rs));
                }
                return umboxInstances;
            }
            catch(SQLException e) {
                logger.severe("Sql exception getting all UmboxInstances: " + e.getClass().getName()+": "+e.getMessage());
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting UmboxInstances: " + e.getClass().getName()+": "+e.getMessage());
            }
            finally {
                try { if(rs != null) { rs.close(); } } catch(Exception e) {}
                try { if(st != null) { st.close(); } } catch(Exception e) {}
            }
            return null;
        });
    }

    /**
     * Extract a UmboxInstance from the result set of a database query.
     * @param rs ResultSet from a UmboxInstance query.
     * @return The UmboxInstance that was found.
     */
    private static UmboxInstance rsToUmboxInstance(ResultSet rs){
        UmboxInstance umboxInstance = null;
        try{
            int id = rs.getInt("id");
            String alerterId = rs.getString("alerter_id");
            int imageId = rs.getInt("umbox_image_id");
            String containerId = rs.getString("container_id");
            int deviceId = rs.getInt("device_id");
            Timestamp startedAt = rs.getTimestamp("started_at");
            umboxInstance = new UmboxInstance(id, alerterId, imageId, containerId, deviceId, startedAt);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to UmboxInstance: " + e.getClass().getName()+": "+e.getMessage());
        }
        return umboxInstance;
    }

    /**
     * Adds the desired UmboxInstance to the database
     * @param u UmboxInstance to add
     * @return
     */
    public static CompletionStage<Integer> insertUmboxInstance(UmboxInstance u){
        return CompletableFuture.supplyAsync(() -> {
           logger.info("Adding umbox instance: "+ u);
           PreparedStatement st = null;
           try{
               st = dbConn.prepareStatement("INSERT INTO umbox_instance (alerter_id, umbox_image_id, container_id, device_id, started_at) VALUES (?,?,?,?,?)");
               st.setString(1, u.getAlerterId());
               st.setInt(2, u.getUmboxImageId());
               st.setString(3, u.getContainerId());
               st.setInt(4, u.getDeviceId());
               st.setTimestamp(5, u.getStartedAt());
               st.executeUpdate();
               return getLatestId("umbox_instance");
           }
           catch (SQLException e){
               e.printStackTrace();
               logger.severe("SQL exception adding umbox instance: " + e.getClass().getName()+": "+e.getMessage());
           }
           return -1;
        });
    }

    /**
     * Edit desired UmboxInstance
     * @param u The instance to be updated
     * @return
     */
    public static CompletionStage<Integer> updateUmboxInstance(UmboxInstance u) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Editing umbox intance: " + u);
            PreparedStatement st = null;
            try {
                st = dbConn.prepareStatement("UPDATE umbox_instance " +
                        "SET alerter_id = ?, umbox_image_id = ?, container_id = ?, device_id = ?, started_at = ?" +
                        "WHERE id = ?");
                st.setString(1, u.getAlerterId());
                st.setInt(2, u.getUmboxImageId());
                st.setString(3, u.getContainerId());
                st.setInt(4, u.getDeviceId());
                st.setTimestamp(5, u.getStartedAt());
                st.setInt(6, u.getId());
                st.executeUpdate();
                return u.getId();
            }
            catch (SQLException e) {
                e.printStackTrace();
                logger.severe("SQL exception editing umbox iamge: " + e.getClass().getName()+": "+e.getMessage());
            }
            catch (NumberFormatException e) {}
            finally {
                try { if(st != null) st.close(); } catch (Exception e) {}
            }
            return -1;
        });
    }

    /**
     * Deletes a UmboxInstance by its id.
     * @param id id of the UmboxInstance to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static CompletionStage<Boolean> deleteUmboxInstance(int id) {
        return deleteById("umbox_instance", id);
    }

}