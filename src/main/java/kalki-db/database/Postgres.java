package kalkidb.database;

import kalkidb.models.*;
import org.postgresql.util.HStoreConverter;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class Postgres {
    
    private static Logger logger = Logger.getLogger("myLogger");
    private static String dbName;
    private static String dbUser;
    private static String dbPassword = "123";
    private static Postgres postgres = null;

    public static Connection db = null;

    private Postgres(String ip, String port, String dbName, String dbUser){
        try{
            //Read ip, port from config file
            this.dbName = dbName;
            this.dbUser = dbUser;
            db = makeConnection(ip, port);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error initializing postgres: " + e.getClass().getName()+": "+e.getMessage());
        }
    }

    /**
     * Initialize the singleton instance of postgres, connecting to the database.
     * Must be done before any static methods can be used.
     */
    public static void initialize(String port, String ip, String dbName, String dbUser) {
        if (postgres == null){
            postgres = new Postgres(port, ip, dbName, dbUser);
        }
    }

    /**
     * First time database setup.
     * Creates necessary extensions, databases, and tables
     */
    public static void setupDatabase(){
        createHstoreExtension();
        makeDatabase();
        makeTables();
        try {
            sleep(1000);
        } catch(Exception e){}
        createTriggers();
    }

    /**
     * Drops and recreates all tables.
     */
    public static void resetDatabase(){
        dropTables();
        makeTables();
        createTriggers();
    }

    /**
     * Connects to the postgres database, allowing for database ops.
     * @return a connection to the database.
     */
    public Connection makeConnection(String ip, String port){
        try {
            Class.forName("org.postgresql.Driver");
            db = DriverManager
                    .getConnection("jdbc:postgresql://" + ip + ":" + port + "/myDB",
                            "postgres", "123");
            return db;
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error connecting to database : " + e.getClass().getName()+": "+e.getMessage());
            return null;
        }
    }

    /**
     * Creates the user if it does not exist.
     */
    public static void createUserIfNotExists(String user, String password) {
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
        executeCommand(createUser);
        return;
    }

    /**
     * Executes the given SQL command in the database.
     * @param command SQL commmand string
     */
    public static void executeCommand(String command){
        if(db == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return;
        }
        try{
            Statement statement = db.createStatement();
            statement.execute(command);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error executing database command: '" + command + "' " +
                    e.getClass().getName()+": "+e.getMessage());
        }
    }

    /**
     * Creates all necessary triggers in the database, and necessary helper functions.
     * Namely, creates a trigger and function to send notifications on device insert.
     */
    public static void createTriggers(){
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
    }

    /**
     * Lists all postgres databases. Primarily for testing.
     */
    public static void listAllDatabases() {
        try {
            PreparedStatement ps = db
                    .prepareStatement("SELECT datname FROM pg_database WHERE datistemplate = false;");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                logger.info(rs.getString(1));
            }
            rs.close();
            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Drops all tables from the database.
     */
    public static void dropTables(){
        logger.info("Dropping tables.");

        List<String> tableNames = new ArrayList<String>();
        tableNames.add("device_history");
        tableNames.add("device");
        tableNames.add("alert");
        tableNames.add("umbox");
        tableNames.add("tag");
        tableNames.add("device_tag");
        tableNames.add("alert_history");
        tableNames.add("state_history");
        tableNames.add("umbox_image");
        tableNames.add("device_group");


        for(String tableName: tableNames){
            dropTable(tableName);
        }
    }

    /**
     * Drop a table from the database.
     * @param tableName name of the table to be dropped
     */
    public static void dropTable(String tableName){
        executeCommand("DROP TABLE " + tableName);
    }

    /**
     * Creates a postgres database.
     */
    public static void makeDatabase(){
        logger.info("Creating database.");
        executeCommand("CREATE DATABASE " + dbName
                + " WITH OWNER= " + dbUser
                + " ENCODING = 'UTF8' TEMPLATE = template0 "
                + " CONNECTION LIMIT = -1;");
    }

    /**
     * Create tables for each model.
     */
    public static void makeTables(){
        logger.info("Making tables.");
        executeCommand("CREATE TABLE IF NOT EXISTS device_history(" +
                "device_id     varchar(255) NOT NULL," +
                "attributes    hstore, " +
                "timestamp     TIMESTAMP NOT NULL," +
                "id            serial    PRIMARY KEY" +
                ");"
        );
        executeCommand("CREATE TABLE IF NOT EXISTS device (" +
                "id             serial   PRIMARY KEY," +
                "description    varchar(255)," +
                "device_name    varchar(255) NOT NULL," +
                "type_id        varchar(255) NOT NULL," +
                "group_id       varchar(255)," +
                "ip_address     varchar(255)," +
                "history_size   int NOT NULL," +
                "sampling_rate  int NOT NULL," +
                "policy_file varchar(255) NOT NULL" +
                ");"
        );
        executeCommand("CREATE TABLE IF NOT EXISTS alert (" +
                "id           serial PRIMARY KEY, " +
                "umbox_id     varchar(255) NOT NULL, " +
                "info         varchar(255) NOT NULL, " +
                "stamp        bigint NOT NULL " +
                ");"
        );
        executeCommand("CREATE TABLE IF NOT EXISTS umbox (" +
                "id           serial PRIMARY KEY, " +
                "umbox_id     varchar(255) NOT NULL, " +
                "umbox_name   varchar(255) NOT NULL, " +
                "device       varchar(255) NOT NULL, " +
                "started_at   bigint NOT NULL " +
                ");"
        );
        executeCommand("CREATE TABLE IF NOT EXISTS tag(" +
                "id           serial PRIMARY KEY, " +
                "name         varchar(255) NOT NULL" +
                ");");
        executeCommand("CREATE TABLE IF NOT EXISTS device_tag(" +
                "device_id    varchar(255) NOT NULL, " +
                "tag_id       varchar(255) NOT NULL" +
                ");"
        );
        executeCommand("CREATE TABLE IF NOT EXISTS device_group(" +
                "id    serial PRIMARY KEY, " +
                "name  varchar(255)" +
                ");"
        );
        executeCommand("CREATE TABLE IF NOT EXISTS alert_history(" +
                "id           serial PRIMARY KEY," +
                "timestamp    TIMESTAMP NOT NULL," +
                "external_id  varchar(255) NOT NULL," +
                "name         varchar(255)" +
                ");"
        );
        executeCommand("CREATE TABLE IF NOT EXISTS state_history(" +
                "id           serial PRIMARY KEY," +
                "timestamp    TIMESTAMP NOT NULL," +
                "state        varchar(255) NOT NULL" +
                ");"
        );
        executeCommand("CREATE TABLE IF NOT EXISTS umbox_image(" +
                "id           serial PRIMARY KEY," +
                "name         TIMESTAMP NOT NULL," +
                "path         varchar(255) NOT NULL" +
                ");"
        );

    }

    /**
     * Add the hstore extension to the postgres database.
     */
    public static void createHstoreExtension(){
        logger.info("Adding hstore extension.");
        executeCommand("CREATE EXTENSION hstore");
    }

    /**
     * Finds a database entry in a given table by id
     * @param id id of the entry to find
     * @param tableName name of the table to search
     * @return the resultset of the query
     */
    private static ResultSet findById(String id, String tableName){
        ResultSet rs = null;
        if(db == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try{
            Statement st = db.createStatement();
            rs = st.executeQuery("SELECT * FROM " + tableName + " WHERE ID ='" + id+"'");
            st.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error finding deviceHistory: " + e.getClass().getName()+": "+e.getMessage());
        }
        return rs;
    }

    /**
     * Finds all entries in a given table.
     * @param tableName name of the table.
     * @return a list of all entries in the table.
     */
    private static ResultSet getAllFromTable(String tableName){
        ResultSet rs = null;
        if(db == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try{
            Statement st = db.createStatement();
            rs = st.executeQuery("SELECT * FROM " + tableName);
            st.close();
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
     * Finds all deviceHistories in the database.
     * @return a list of all deviceHistories in the database.
     */
    public static List<DeviceHistory> getAllDeviceHistories() {
        ResultSet rs = getAllFromTable("device_history");
        List<DeviceHistory> deviceHistories = new ArrayList<DeviceHistory>();
        try {

            while (rs.next()) {
                deviceHistories.add(rsToDeviceHistory(rs));
            }
            rs.close();
        }
        catch(SQLException e) {
            logger.severe("Sql exception getting all device histories.");
        }
        return deviceHistories;
    }

    /**
     * Extract a deviceHistory from the result set of a database query.
     * @param rs result set from a deviceHistory query.
     * @return The deviceHistory that was found.
     */
    private static DeviceHistory rsToDeviceHistory(ResultSet rs){
        DeviceHistory deviceHistory = null;
        try{
            String deviceId = rs.getString(1);
            Map<String, String> attributes = HStoreConverter.fromString(rs.getString(2));
            Timestamp timestamp = rs.getTimestamp(3);
            String historyId = rs.getString(4);

            deviceHistory = new DeviceHistory(deviceId, attributes, timestamp, historyId);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to deviceHistory: " + e.getClass().getName()+": "+e.getMessage());
        }
        return deviceHistory;
    }

    /**
     * Saves given deviceHistory to the database.
     * @param deviceHistory deviceHistory to be inserted.
     * @return auto incremented id
     */
    public static int insertDeviceHistory(DeviceHistory deviceHistory){
        logger.info("Inserting device_history: " + deviceHistory.toString());
        if(db == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try{
            PreparedStatement update = db.prepareStatement
                    ("INSERT INTO device_history(device_id, attributes, timestamp) values(?,?,?)");
            update.setString(1, deviceHistory.deviceId);
            update.setObject(2, deviceHistory.attributes);
            update.setTimestamp(3, deviceHistory.timestamp);

            update.executeUpdate();

            int serialNum = 0;
            Statement stmt = db.createStatement();

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
            logger.severe("Error inserting deviceHistory: " + e.getClass().getName()+": "+e.getMessage());
        }
        return -1;
    }

    /**
     * Updates deviceHistory with given id to have the parameters of the given deviceHistory.
     * @param deviceHistory holding new parameters to be saved in the database.
     */
    public static void updateDeviceHistory(DeviceHistory deviceHistory){
        logger.info("Updating deviceHistory with id=" + deviceHistory.id);
        if(db == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return;
        }
        try{
            PreparedStatement update = db.prepareStatement
                    ("UPDATE device_history SET device_id = ?, attributes = ?, timestamp = ?, id = ? " +
                            "WHERE id=?");

            update.setString(1, deviceHistory.id);
            update.setObject(2, deviceHistory.attributes);
            update.setTimestamp(3, deviceHistory.timestamp);
            update.setString(4, deviceHistory.id);

            update.executeUpdate();
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error updating deviceHistory: " + e.getClass().getName()+": "+e.getMessage());
        }
    }

    /**
     * First, attempts to find the deviceHistory in the database.
     * If successful, updates the existing deviceHistory with the given deviceHistory's parameters Otherwise,
     * inserts the given deviceHistory.
     * @param deviceHistory deviceHistory to be inserted or updated.
     */
    public static void insertOrUpdateDeviceHistory(DeviceHistory deviceHistory){
        if(findDeviceHistory(deviceHistory.id) != null){
            updateDeviceHistory(deviceHistory);
        }
        else{
            insertDeviceHistory(deviceHistory);
        }
    }


    /**
     * Finds a deviceHistory from the database by its id.
     * @param id id of the device to find.
     * @return the deviceHistory if it exists in the database, else null.
     */
    public static DeviceHistory findDeviceHistory(String id){
        return rsToDeviceHistory(findById(id, "device_history"));
    }

    /*
     *       Device specific methods
     */

    /**
     * Saves given device to the database.
     * @param device deviceHistory to be inserted.
     * @return auto incremented id
     */
    public static int insertDevice(Device device){
        logger.info("Inserting device with id=" + device.id);
        if(db == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try{
            PreparedStatement update = db.prepareStatement
                    ("INSERT INTO device(description, device_name, type_id, group_id, ip_address," +
                            "history_size, sampling_rate, policy_file) values(?,?,?,?,?,?,?,?)");
            update.setString(1, device.description);
            update.setString(2, device.name);
            update.setString(3, device.type);
            update.setString(4, device.groupId);
            update.setString(5, device.ip);
            update.setInt(6, device.historySize);
            update.setInt(7, device.samplingRate);
            update.setString(8, device.policyFile);

            update.executeUpdate();

            int serialNum = 0;
            Statement stmt = db.createStatement();

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
            logger.severe("Error inserting device: " + e.getClass().getName()+": "+e.getMessage());
        }
        return -1;
    }

    /**
     * Finds all devices in the database.
     * @return a list of all deviceHistories in the database.
     */
    public static List<Device> getAllDevices(){
        if(db == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        List<Device> devices = new ArrayList<Device>();
        try{
            Statement st = db.createStatement();
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
            logger.severe("Error getting all deviceHistories: " + e.getClass().getName()+": "+e.getMessage());
        }
        return devices;
    }

    /**
     * Extract a deviceHistory from the result set of a database query.
     * @param rs result set from a deviceHistory query.
     * @return The deviceHistory that was found.
     */
    private static Device rsToDevice(ResultSet rs){
        Device device = null;
        try{
            String id = rs.getString(1);
            String description = rs.getString(2);
            String name = rs.getString(3);
            String type = rs.getString(4);
            String groupId = rs.getString(5);
            String ip = rs.getString(6);
            int historySize = rs.getInt(7);
            int samplingRate = rs.getInt(8);
            String policyFile = rs.getString(9);

            device = new Device(id, description, name, type, groupId, ip, historySize, samplingRate, policyFile);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to deviceHistory: " + e.getClass().getName()+": "+e.getMessage());
        }
        return device;
    }

    /**
     * Finds a deviceHistory from the database by its id.
     * @param id id of the device to find.
     * @return the deviceHistory if it exists in the database, else null.
     */
    public static Device findDevice(String id){
        return rsToDevice(findById(id, "device"));
    }

}