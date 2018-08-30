package kalkidb.database;

import kalkidb.models.*;
import org.postgresql.util.HStoreConverter;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.lang.Thread.sleep;

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
     * Drops and recreates all tables.
     */
    public static CompletionStage<Void> resetDatabase(){
        logger.info("Resetting Database.");
        return dropTables().thenRunAsync(() -> {
            makeTables().thenRunAsync(() -> {
                insertDefaultTypes();
                createTriggers();
            });
        });
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
    public static boolean createDBIfNotExist(String rootPassword, String dbName, String dbOwner) throws SQLException
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
                makeDatabase(rootPassword);
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
                "      CREATE ROLE " + user + " LOGIN PASSWORD '"
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
            logger.severe("Error creating user:" +
                    e.getClass().getName() + ": " + e.getMessage());
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
     * Creates all necessary triggers in the database, and necessary helper functions.
     * Namely, creates a trigger and function to send notifications on device insert.
     */
    public static CompletionStage<Void> createTriggers(){
        return CompletableFuture.runAsync(() -> {
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
        });
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

    /**
     * Drops all tables from the database.
     */
    public static CompletionStage<Void> dropTables(){
        return CompletableFuture.runAsync(() -> {
            logger.info("Dropping tables.");
            List<String> tableNames = new ArrayList<String>();
            tableNames.add("device");
            tableNames.add("device_history");
            tableNames.add("state_history");
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
        });
    }

    /**
     * Drop a table from the database.
     * @param tableName name of the table to be dropped
     */
    public static CompletionStage<Void> dropTable(String tableName){
        return CompletableFuture.runAsync(() -> {
            executeCommand("DROP TABLE IF EXISTS " + tableName);
        });
    }

    /**
     * Creates a postgres database.
     */
    public static CompletionStage<Void> makeDatabase(String rootPassword){
        return CompletableFuture.runAsync(() -> {
            logger.info("Creating database.");
            executeCommand("CREATE DATABASE " + dbName
                    + " WITH OWNER= " + dbUser
                    + " ENCODING = 'UTF8' TEMPLATE = template0 "
                    + " CONNECTION LIMIT = -1;");
        });
    }

    /**
     * Create tables for each model.
     */
    public static CompletionStage<Void> makeTables(){
        return CompletableFuture.runAsync(() -> {
            logger.info("Making tables.");

            executeCommand("CREATE TABLE IF NOT EXISTS device(" +
                    "id             serial   PRIMARY KEY," +
                    "name           varchar(255) NOT NULL," +
                    "description    varchar(255)," +
                    "type_id        int NOT NULL," +
                    "group_id       int," +
                    "ip_address     varchar(255)," +
                    "history_size   int NOT NULL," +
                    "sampling_rate  int NOT NULL," +
                    "policy_file    bytea," +
                    "policy_file_name    varchar(255)" +
                    ");"
            );

            executeCommand("CREATE TABLE IF NOT EXISTS device_history(" +
                    "device_id     int NOT NULL," +
                    "attributes    hstore," +
                    "timestamp     TIMESTAMP," +
                    "id            serial    PRIMARY KEY" +
                    ");"
            );

            executeCommand("CREATE TABLE IF NOT EXISTS state_history(" +
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
                    "name  varchar(255)" +
                    ");"
            );

            executeCommand("CREATE TABLE IF NOT EXISTS device_group(" +
                    "id    serial PRIMARY KEY, " +
                    "name  varchar(255)" +
                    ");"
            );

            executeCommand("CREATE TABLE IF NOT EXISTS alert_history(" +
                    "id                 serial PRIMARY KEY," +
                    "received_at        timestampz NOT NULL DEFAULT now()," +
                    "umbox_external_id  varchar(255) NOT NULL," +
                    "info               varchar(255)" +
                    ");"
            );

            executeCommand("CREATE TABLE IF NOT EXISTS umbox_instance(" +
                    "id                 serial PRIMARY KEY, " +
                    "umbox_external_id  varchar(255) NOT NULL, " +
                    "device_id          int NOT NULL, " +
                    "started_at         timestampz NOT NULL DEFAULT now()" +
                    ");"
            );

            executeCommand("CREATE TABLE IF NOT EXISTS umbox_image(" +
                    "id           serial PRIMARY KEY," +
                    "name         varchar(255) NOT NULL," +
                    "path         varchar(255) NOT NULL" +
                    ");"
            );
        });
    }

    /**
     * Insert the default types into the database.
     */
    public static CompletionStage<Void> insertDefaultTypes() {
        return CompletableFuture.runAsync(() -> {
            logger.info("Inserting default types.");
            List<String> typeNames = new ArrayList<String>();
            typeNames.add("Hue Light");
            typeNames.add("Dlink Camera");
            typeNames.add("WeMo Insight");
            typeNames.add("Udoo Neo");
            for(String typeName: typeNames){
                executeCommand("INSERT INTO type (name) VALUES ('" + typeName + "')");
            }
        });
    }

    /**
     * Add the hstore extension to the postgres database.
     */
    public static CompletionStage<Void> createHstoreExtension(){
        return CompletableFuture.runAsync(() -> {
            logger.info("Adding hstore extension.");
            executeCommand("CREATE EXTENSION hstore;");
        });
    }

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

    private static void executeStatement(PreparedStatement update) {
        try{
            update.executeUpdate();
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error executing statement: " + e.getClass().getName()+": "+e.getMessage());
        }
    }

    /*
     *     DeviceHistory specific methods
     */


    /**
     * Finds all DeviceHistories in the database.
     * @return a list of all DeviceHistories in the database.
     */
    public static CompletionStage<List<DeviceHistory>> getAllDeviceHistories() {
        return CompletableFuture.supplyAsync(() -> {
            ResultSet rs = getAllFromTable("device_history");
            List<DeviceHistory> deviceHistories = new ArrayList<DeviceHistory>();
            try {
                while (rs.next()) {
                    deviceHistories.add(rsToDeviceHistory(rs));
                }
                rs.close();
            } catch (SQLException e) {
                logger.severe("Sql exception getting all device histories.");
            }
            return deviceHistories;
        });
    }

    /**
     * Finds all DeviceHistories from the database for the given device.
     * @param deviceId the id of the device.
     * @return a list of all DeviceHistories in the database where the device_id field is equal to deviceId.
     */
    public static CompletionStage<List<DeviceHistory>> getDeviceHistory(int deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            try {
                st = dbConn.prepareStatement("SELECT * FROM device_history WHERE device_id = ?");
                st.setInt(1, deviceId);
                rs = st.executeQuery();
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting device histories: " + e.getClass().getName() + ": " + e.getMessage());
            }
            List<DeviceHistory> deviceHistories = new ArrayList<DeviceHistory>();
            try {
                while (rs.next()) {
                    deviceHistories.add(rsToDeviceHistory(rs));
                }
            } catch (SQLException e) {
                logger.severe("Sql exception getting all device histories: " + e.getClass().getName() + ": " + e.getMessage());
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                } catch (Exception e) {
                }
                try {
                    if (st != null) {
                        st.close();
                    }
                } catch (Exception e) {
                }
            }
            return deviceHistories;
        });
    }

    /**
     * Extract a DeviceHistory from the result set of a database query.
     * @param rs ResultSet from a DeviceHistory query.
     * @return The DeviceHistory that was found.
     */
    private static DeviceHistory rsToDeviceHistory(ResultSet rs){
        DeviceHistory deviceHistory = null;
        try{
            int deviceId = rs.getInt(1);
            Map<String, String> attributes = HStoreConverter.fromString(rs.getString(2));
            Timestamp timestamp = rs.getTimestamp(3);
            int historyId = rs.getInt(4);

            deviceHistory = new DeviceHistory(deviceId, attributes, timestamp, historyId);
        }
        catch(SQLException e){
            e.printStackTrace();
            logger.severe("Error converting rs to DeviceHistory: " + e.getClass().getName()+": "+e.getMessage());
        }
        return deviceHistory;
    }

    /**
     * Saves given DeviceHistory to the database.
     * @param deviceHistory DeviceHistory to be inserted.
     * @return auto incremented id
     */
    public static CompletionStage<Integer> insertDeviceHistory(DeviceHistory deviceHistory){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Inserting device_history: " + deviceHistory.toString());
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try{
                PreparedStatement update = dbConn.prepareStatement
                        ("INSERT INTO device_history(device_id, attributes, timestamp) values(?,?,?)");
                update.setInt(1, deviceHistory.deviceId);
                update.setObject(2, deviceHistory.attributes);
                update.setTimestamp(3, deviceHistory.timestamp);

                update.executeUpdate();

                int serialNum = 0;
                Statement stmt = dbConn.createStatement();

                // get the postgresql serial field value with this query
                String query = "select currval('device_id_seq')";
                ResultSet rs = stmt.executeQuery(query);
                if ( rs.next() )
                {
                    serialNum = rs.getInt(1);
                    return serialNum;
                }
                stmt.close();
            }
            catch(Exception e){
                e.printStackTrace();
                logger.severe("Error inserting DeviceHistory: " + e.getClass().getName()+": "+e.getMessage());
            }
            return -1;
        });
    }

    /**
     * Updates DeviceHistory with given id to have the parameters of the given DeviceHistory.
     * @param deviceHistory DeviceHistory holding new parameters to be saved in the database.
     */
    public static CompletionStage<Integer> updateDeviceHistory(DeviceHistory deviceHistory){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Updating DeviceHistory with id=" + deviceHistory.id);
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try {
                PreparedStatement update = dbConn.prepareStatement
                        ("UPDATE device_history SET device_id = ?, attributes = ?, timestamp = ?, id = ? " +
                                "WHERE id=?");

                update.setInt(1, deviceHistory.deviceId);
                update.setObject(2, deviceHistory.attributes);
                update.setTimestamp(3, deviceHistory.timestamp);
                update.setInt(4, deviceHistory.id);
                update.setInt(5, deviceHistory.id);

                update.executeUpdate();
                return deviceHistory.id;
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error updating DeviceHistory: " + e.getClass().getName() + ": " + e.getMessage());
                return -1;
            }
        });
    }

    /**
     * First, attempts to find the DeviceHistory in the database.
     * If successful, updates the existing DeviceHistory with the given DeviceHistory's parameters Otherwise,
     * inserts the given DeviceHistory.
     * @param deviceHistory DeviceHistory to be inserted or updated.
     */
    public static CompletionStage<Integer> insertOrUpdateDeviceHistory(DeviceHistory deviceHistory){
        return findDeviceHistory(deviceHistory.id).thenApplyAsync(d -> {
            if(d == null) {
                insertDeviceHistory(deviceHistory);
                return 0;
            } else {
                updateDeviceHistory(deviceHistory);
                return 1;
            }
        });
    }


    /**
     * Finds a DeviceHistory from the database by its id.
     * @param id id of the DeviceHistory to find.
     * @return the DeviceHistory if it exists in the database, else null.
     */
    public static CompletionStage<DeviceHistory> findDeviceHistory(int id){
        return findById(id, "device_history").thenApplyAsync(rs -> {
            if(rs == null) {
                return null;
            } else {
                return rsToDeviceHistory(rs);
            }
        });
    }

    /*
     *       Device specific methods
     */

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
                                "history_size, sampling_rate, policy_file, policy_file_name) values(?,?,?,?,?,?,?,?,?)");
                update.setString(1, device.getDescription());
                update.setString(2, device.getName());
                update.setInt(3, device.getTypeId());
                update.setInt(4, device.getGroupId());
                update.setString(5, device.getIp());
                update.setInt(6, device.getHistorySize());
                update.setInt(7, device.getSamplingRate());
                update.setBytes(8, device.getPolicyFile());
                update.setString(9, device.getPolicyFileName());

                update.executeUpdate();

                int serialNum = 0;
                Statement stmt = dbConn.createStatement();

                // get the postgresql serial field value with this query
                String query = "select currval('device_id_seq')";
                ResultSet rs = stmt.executeQuery(query);
                if ( rs.next() )
                {
                    serialNum = rs.getInt(1);
                    //Insert tags into device_tag
                    List<Integer> tagIds = device.getTagIds();
                    if(tagIds != null) {
                        for(int tagId : tagIds) {
                            executeCommand(String.format("INSERT INTO device_tag(device_id, tag_id) values (%d,%d)", serialNum, tagId));
                        }
                    }
                    return serialNum;
                }
                stmt.close();
            }
            catch(Exception e){
                e.printStackTrace();
                logger.severe("Error inserting Device: " + e.getClass().getName()+": "+e.getMessage());
            }
            return -1;
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
                            "SET name = ?, description = ?, type_id = ?, group_id = ?, ip_address = ?, history_size = ?, sampling_rate = ?, policy_file = ?, policy_file_name = ? " +
                            "WHERE id = ?");
                    update.setString(1, device.getName());
                    update.setString(2, device.getDescription());
                    update.setInt(3, device.getTypeId());
                    update.setInt(4, device.getGroupId());
                    update.setString(5, device.getIp());
                    update.setInt(6, device.getHistorySize());
                    update.setInt(7, device.getSamplingRate());
                    update.setBytes(8, device.getPolicyFile());
                    update.setString(9, device.getPolicyFileName());
                    update.setInt(10, device.getId());
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
     * Finds all Devices in the database.
     * @return a list of all Devices in the database.
     */
    public static CompletionStage<List<Device>> getAllDevices(){
        return CompletableFuture.supplyAsync(() -> {
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            List<Device> devices = new ArrayList<Device>();
            try{
                Statement st = dbConn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM device");
                while (rs.next())
                {
                    devices.add(rsToDevice(rs));
                }
                rs.close();
                st.close();
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
            int historySize = rs.getInt(7);
            int samplingRate = rs.getInt(8);
            byte[] policyFile = rs.getBytes(9);
            String policyFileName = rs.getString(10);

            device = new Device(id, name, description, typeId, groupId, ip, historySize, samplingRate, policyFile, policyFileName);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to Device: " + e.getClass().getName()+": "+e.getMessage());
        }
        return device;
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
                PreparedStatement st = null;
                ResultSet tagRs = null;

                try {
                    st = dbConn.prepareStatement("SELECT * FROM device_tag WHERE device_id = ?");
                    st.setInt(1, id);
                    tagRs = st.executeQuery();

                    List<Integer> tagIds = new ArrayList<Integer>();
                    while(tagRs.next()) {
                        tagIds.add(tagRs.getInt(2));
                    }
                    device.setTagIds(tagIds);
                }
                catch(SQLException e) {
                    e.printStackTrace();
                    logger.severe("Error finding device by id: " + e.getClass().getName()+": "+e.getMessage());
                }
                finally {
                    try { if (tagRs != null) tagRs.close(); } catch (Exception e) {}
                    try { if (st != null) st.close(); } catch (Exception e) {}
                }
                return device;
            }
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

                st = dbConn.prepareStatement("DELETE FROM device WHERE id = ?");
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
     *      StateHistory specific methods
     */

    /**
     * Finds all StateHistories from the database for the given device.
     * @param deviceId the id of the device.
     * @return a list of all StateHistories in the database where the device_id field is equal to deviceId.
     */
    public static CompletionStage<List<StateHistory>> getStateHistory(int deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            try{
                st = dbConn.prepareStatement("SELECT * FROM state_history WHERE device_id = ?");
                st.setInt(1, deviceId);
                rs = st.executeQuery();
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting state histories: " + e.getClass().getName()+": "+e.getMessage());
            }
            List<StateHistory> stateHistories = new ArrayList<StateHistory>();
            try {
                while (rs.next()) {
                    stateHistories.add(rsToStateHistory(rs));
                }
            }
            catch(SQLException e) {
                logger.severe("Sql exception getting all state histories: " + e.getClass().getName()+": "+e.getMessage());
            }
            finally {
                try { if(rs != null) { rs.close(); } } catch(Exception e) {}
                try { if(st != null) { st.close(); } } catch(Exception e) {}
            }
            return stateHistories;
        });
    }

    /**
     * Extract a StateHistory from the result set of a database query.
     * @param rs ResultSet from a StateHistory query.
     * @return The StateHistory that was found.
     */
    private static StateHistory rsToStateHistory(ResultSet rs){
        StateHistory stateHistory = null;
        try{
            int id = rs.getInt(1);
            int deviceId = rs.getInt(2);
            Timestamp timestamp = rs.getTimestamp(3);
            String state = rs.getString(4);
            stateHistory = new StateHistory(id, deviceId, timestamp, state);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to StateHistory: " + e.getClass().getName()+": "+e.getMessage());
        }
        return stateHistory;
    }

    /*
     *      AlertHistory specific methods.
     */

    /**
     * Finds all AlertHistories from the database for the given list of UmboxInstance externalIds.
     * @param externalIds a list of externalIds of UmboxInstances.
     * @return a list of all AlertHistories in the database where the the alert was created by a UmboxInstance with
     *         externalId in externalIds.
     */
    public static List<AlertHistory> getAlertHistory(List<String> externalIds) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if(dbConn == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        List<AlertHistory> alertHistories = new ArrayList<AlertHistory>();
        for(String externalId : externalIds) {
            try {
                st = dbConn.prepareStatement("SELECT * FROM alert_history WHERE umbox_external_id = ?");
                st.setString(1, externalId);
                rs = st.executeQuery();
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting alert histories: " + e.getClass().getName() + ": " + e.getMessage());
            }
            try {
                while (rs.next()) {
                    alertHistories.add(rsToAlertHistory(rs));
                }
            } catch (SQLException e) {
                logger.severe("Sql exception getting all alert histories: " + e.getClass().getName() + ": " + e.getMessage());
            } finally {
                try { if (rs != null) { rs.close(); } } catch (Exception e) {}
                try { if (st != null) { st.close(); } } catch (Exception e) {}
            }
        }
        return alertHistories;
    }

    /**
     * Extract an AlertHistory from the result set of a database query.
     * @param rs ResultSet from a AlertHistory query.
     * @return The AlertHistory that was found.
     */
    private static AlertHistory rsToAlertHistory(ResultSet rs){
        AlertHistory alertHistory = null;
        try{
            int id = rs.getInt("id");
            Timestamp timestamp = rs.getTimestamp("received_at");
            String externalId = rs.getString("umbox_external_id");
            String name = rs.getString("info");
            alertHistory = new AlertHistory(id, timestamp, externalId, name);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to AlertHistory: " + e.getClass().getName()+": "+e.getMessage());
        }
        return alertHistory;
    }

    /**
     * Finds all UmboxInstances from the database for the given device.
     * @param deviceId the id of the device.
     * @return a list of all UmboxInstaces in the database where the device_id field is equal to deviceId.
     */
    public static CompletionStage<List<UmboxInstance>> getUmboxInstances(int deviceId) {
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
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting UmboxInstances: " + e.getClass().getName()+": "+e.getMessage());
            }
            List<UmboxInstance> umboxInstances = new ArrayList<UmboxInstance>();
            try {
                while (rs.next()) {
                    umboxInstances.add(rsToUmboxInstance(rs));
                }
            }
            catch(SQLException e) {
                logger.severe("Sql exception getting all UmboxInstances: " + e.getClass().getName()+": "+e.getMessage());
            }
            finally {
                try { if(rs != null) { rs.close(); } } catch(Exception e) {}
                try { if(st != null) { st.close(); } } catch(Exception e) {}
            }
            return umboxInstances;
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
            String umboxExternalId = rs.getString("umbox_external_id");
            int deviceId = rs.getInt("device_id");
            Timestamp startedAt = rs.getTimestamp("started_at");
            umboxInstance = new UmboxInstance(id, umboxExternalId, deviceId, startedAt);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to UmboxInstance: " + e.getClass().getName()+": "+e.getMessage());
        }
        return umboxInstance;
    }

    public static CompletionStage<Void> addUmboxImage(UmboxImage u) {
        return CompletableFuture.runAsync(() -> {
            logger.info("Adding umbox image: " + u);
            PreparedStatement st = null;
            try {
                st = dbConn.prepareStatement("INSERT INTO umbox_image (name, path) VALUES (?, ?)");
                st.setString(1, u.getName());
                st.setString(2, u.getPath());
                st.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
                logger.severe("SQL exception adding umbox iamge: " + e.getClass().getName()+": "+e.getMessage());
            }
            finally {
                try { if(st != null) st.close(); } catch (Exception e) {}
            }
        });
    }

    public static CompletionStage<Void> editUmboxImage(UmboxImage u) {
        return CompletableFuture.runAsync(() -> {
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
            }
            catch (SQLException e) {
                e.printStackTrace();
                logger.severe("SQL exception editing umbox iamge: " + e.getClass().getName()+": "+e.getMessage());
            }
            catch (NumberFormatException e) {}
            finally {
                try { if(st != null) st.close(); } catch (Exception e) {}
            }
        });
    }

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

    /**
     * Finds all UmboxImages in the database.
     * @return a list of all UmboxImages in the database.
     */
    public static CompletionStage<List<UmboxImage>> getAllUmboxImages() {
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
            int id = rs.getInt(1);
            String name = rs.getString(2);
            String path = rs.getString(3);
            umboxImage = new UmboxImage(id, name, path);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to UmboxImage: " + e.getClass().getName()+": "+e.getMessage());
        }
        return umboxImage;
    }

    /**
     * Finds all Groups in the database.
     * @return a list of all Groups in the database.
     */
    public static CompletionStage<List<Group>> getAllGroups() {
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
            int id = rs.getInt(1);
            String name = rs.getString(2);
            group = new Group(id, name);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to Group: " + e.getClass().getName()+": "+e.getMessage());
        }
        return group;
    }

    /**
     * Finds all Types in the database.
     * @return a list of all Types in the database.
     */
    public static CompletionStage<List<Type>> getAllTypes() {
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
            int id = rs.getInt(1);
            String name = rs.getString(2);
            type = new Type(id, name);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to Type: " + e.getClass().getName()+": "+e.getMessage());
        }
        return type;
    }

    /**
     * Finds all Tags in the database.
     * @return a list of all Tags in the database.
     */
    public static CompletionStage<List<Tag>> getAllTags() {
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
            int id = rs.getInt(1);
            String name = rs.getString(2);
            tag = new Tag(id, name);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to Tag: " + e.getClass().getName()+": "+e.getMessage());
        }
        return tag;
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

}