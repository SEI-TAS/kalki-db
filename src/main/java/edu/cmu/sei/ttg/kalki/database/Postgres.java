package edu.cmu.sei.ttg.kalki.database;

import edu.cmu.sei.ttg.kalki.models.*;
import edu.cmu.sei.ttg.kalki.listeners.*;
import org.postgresql.util.HStoreConverter;
import org.postgresql.util.PSQLException;

import java.io.FileInputStream;
import java.sql.*;
import java.util.*;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Postgres {
    private static final String DEFAULT_IP = "localhost";
    private static final String DEFAULT_PORT = "5432";

    private static final String DEFAULT_ROOT_USER = "kalkiuser";
    private static final String BASE_DB = "postgres";
    private static final String POSTGRES_URL_SCHEMA = "jdbc:postgresql://";
    private static final int TABLE_COUNT=17;

    public static final String TRIGGER_NOTIF_NEW_DEV_SEC_STATE = "devicesecuritystateinsert";
    public static final String TRIGGER_NOTIF_NEW_POLICY_INSTANCE = "policyinstanceinsert";

    private static Logger logger = Logger.getLogger("myLogger");
    private static String dbName;
    private static String dbUser;
    private static String dbPassword;
    private static Postgres postgresInstance = null;

    public static Connection dbConn = null;

    private Postgres(String ip, String port, String dbName, String dbUser, String dbPassword) {
        try {
            //Read ip, port from config file
            this.dbName = dbName;
            this.dbUser = dbUser;
            this.dbPassword = dbPassword;
            while((this.dbConn = makeConnection(ip, port))==null) {
                ProcessBuilder builder = new ProcessBuilder();
                logger.info("Starting postgres container.");
                builder.command("bash", "-c", "docker run -p 5432:5432 --net=kalki_nw --rm --name kalki-postgres -e POSTGRES_USER=kalkiuser -e POSTGRES_PASSWORD=kalkipass -e POSTGRES_DB=kalkidb -d postgres");
                Process p = builder.start();
                p.waitFor();
                try { Thread.sleep(2000); } catch(Exception e) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error initializing postgres: " + e.getClass().getName() + ": " + e.getMessage());
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
        if (postgresInstance == null) {
            logger.info("Initializing database");
            postgresInstance = new Postgres(ip, port, dbName, dbUser, dbPassword);
            postgresInstance.setupDatabase();
        } else {
            logger.info("Database already initialized");
        }
    }

    /**
     * Sets the logging level to reduce console clutter
     * Possible values:
     * ALL
     * INFO
     * OFF
     * SEVERE
     * WARNING
     */
    public static void setLoggingLevel(Level lvl) {
        try {
            logger.setLevel(lvl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Connects to the postgres database, allowing for database ops.
     *
     * @return a connection to the database.
     */
    public Connection makeConnection(String ip, String port) {
        try {
            Class.forName("org.postgresql.Driver");
            dbConn = DriverManager
                    .getConnection(POSTGRES_URL_SCHEMA + ip + ":" + port + "/" + this.dbName, this.dbUser, this.dbPassword);
            return dbConn;
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error connecting to database : " + e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }

    public static void dropConnection() {
        try {
            dbConn.close();
        } catch (SQLException e) {
            logger.severe("Error dropping connection: "+e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a DB if it does not exist.
     */
    public static boolean createDBIfNotExists(String rootPassword, String dbName, String dbOwner) throws SQLException {
        return createDBIfNotExists(DEFAULT_IP, DEFAULT_ROOT_USER, rootPassword, dbName, dbOwner);
    }

    /**
     * Creates a DB if it does not exist.
     */
    public static boolean createDBIfNotExists(String ip, String rootUser, String rootPassword, String dbName, String dbOwner) throws SQLException {
        // First check it DB exists.
        String checkDB = "SELECT datname FROM pg_catalog.pg_database "
                + "WHERE datname = '" + dbName + "';";
        try (Connection rootConn = getRootConnection(ip, rootUser, rootPassword);
             Statement stmt = rootConn.createStatement();
             ResultSet result = stmt.executeQuery(checkDB)) {
            if (!result.next()) {
                // If there was no DB with this name, then create it.
                makeDatabase(rootConn, dbName, dbOwner);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return false;
    }

    /**
     * Creates a postgres database.
     */
    public static void makeDatabase(Connection rootConnection, String dbName, String dbOwner) {
        logger.info("Creating database.");
        executeCommand("CREATE DATABASE " + dbName
                + " WITH OWNER= " + dbOwner
                + " ENCODING = 'UTF8' TEMPLATE = template0 "
                + " CONNECTION LIMIT = -1;", rootConnection);
    }

    /**
     * Removes the database.
     */
    public static void removeDatabase(String rootPassword, String dbName) {
        removeDatabase(DEFAULT_IP, DEFAULT_ROOT_USER, rootPassword, dbName);
    }

    /**
     * Removes the database.
     */
    public static void removeDatabase(String ip, String rootUser, String rootPassword, String dbName) {
        logger.info("Removing database.");
        try (Connection rootConn = getRootConnection(ip, rootUser, rootPassword)) {
            executeCommand("DROP DATABASE IF EXISTS " + dbName + ";", rootConn);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error removing database:" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Removes the user.
     */
    public static void removeUser(String rootPassword, String userName) {
        removeUser(DEFAULT_IP, DEFAULT_ROOT_USER, rootPassword, userName);
    }

    /**
     * Removes the user.
     */
    public static void removeUser(String ip, String rootUser, String rootPassword, String userName) {
        logger.info("Removing user.");
        try (Connection rootConn = getRootConnection(ip, rootUser, rootPassword)) {
            executeCommand("DROP ROLE IF EXISTS " + userName + ";", rootConn);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error removing user:" + e.getClass().getName() + ": " + e.getMessage());
        }

    }

    /**
     * Creates the user if it does not exist.
     */
    public static void createUserIfNotExists(String rootPassword, String user, String password) {
        createUserIfNotExists(DEFAULT_IP, DEFAULT_ROOT_USER, rootPassword, user, password);
    }

    /**
     * Creates the user if it does not exist.
     */
    public static void createUserIfNotExists(String ip, String rootUser, String rootPassword, String user, String password) {
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
        try (Connection rootConn = getRootConnection(ip, rootUser, rootPassword)) {
            executeCommand(createUser, rootConn);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error creating user:" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Gets a connection to the root user.
     */
    private static Connection getRootConnection(String ip, String rootUser, String rootPwd) throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", rootUser);
        connectionProps.put("password", rootPwd);
        return DriverManager.getConnection(POSTGRES_URL_SCHEMA + ip + ":" + DEFAULT_PORT + "/" + BASE_DB, connectionProps);
    }


    /**
     * Executes the given SQL command in the database, using the already set up, default connection.
     *
     * @param command SQL commmand string
     */
    public static void executeCommand(String command) {
        executeCommand(command, Postgres.dbConn);
    }

    /**
     * Executes the given SQL command in the database.
     *
     * @param command SQL commmand string
     */
    public static void executeCommand(String command, Connection connection) {
        if (connection == null) {
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
    public static void setupDatabase() {
        int numTables = getTableCount();
        logger.info("Current number of tables: " + numTables);
        if(numTables != 0) {//tables have been initialized
            logger.info("Database has been setup by another component");
            return;
        }
        logger.info("Setting up database.");
        createHstoreExtension();
        // DB Structure
        initDB("db-tables.sql");
        initDB("db-triggers.sql");

        // DB Initial Data
        initDB("db-alert-types.sql");
        initDB("db-device-types.sql");
        initDB("db-security-states.sql");
        initDB("db-command-lookups.sql");
//        initDB("db-umbox-images.sql");
        initDB("db-alert-type-lookups.sql");
//        initDB("db-devices.sql");

    }

    /**
     * First time database setup for tests.
     * Creates necessary extensions, databases, and tables
     */
    public static void setupTestDatabase() {
        logger.info("Setting up test database.");
        dropTables();
        int numTables = getTableCount();
        if(numTables != 0){
            return;
        }
        createHstoreExtension();
        // DB Structure
        initDB("db-tables.sql");
        initDB("db-triggers.sql");
        initDB("db-security-states.sql");
    }

    /**
     * Add the hstore extension to the postgres database.
     */
    public static void createHstoreExtension() {
        logger.info("Adding hstore extension.");
        executeCommand("CREATE EXTENSION IF NOT EXISTS hstore;");
    }

    /**
     * Drops and recreates all tables.
     */
    public static void resetDatabase() {
        logger.info("Resetting Database.");
        dropTables();
        setupDatabase();
    }

    private static int getTableCount() {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = dbConn.prepareStatement("SELECT COUNT(table_name) FROM information_schema.tables WHERE table_schema='public'");
            rs = st.executeQuery();
            if(rs.next()){
                return rs.getInt("count");
            }
        } catch (SQLException e){
            logger.severe("There was an getting the current table count: "+ e.getMessage());
        }
        return -1;
    }

    /**
     * Drops all tables from the database.
     */
    public static void dropTables() {
        logger.info("Dropping tables.");
        initDB("db-drop-tables.sql");
    }


    /**
     * Reads from the specified file to initialize db
     *
     * @param fileName the file containing SQL commands to execute
     */
    public static void initDB(String fileName) {
//        logger.info("\n\n"+fileName+"\n\n");
        try {
            InputStream is = Postgres.class.getResourceAsStream("/" + fileName);
            Scanner s = new Scanner(is);

            String line, statement = "";
            while (s.hasNextLine()) {
                line = s.nextLine();
                if (line.equals("") || line.equals(" ")) { // statements are white space delimited
                    executeCommand(statement);
                    statement = "";
                } else {
                    statement += line;
                }
            }
            if (!statement.equals(""))
                executeCommand(statement);
        } catch (Exception e) {
            logger.severe("Error initializing db:");
            e.printStackTrace();
        }

    }

    /**
     * Lists all postgres databases. Primarily for testing.
     */
    public static void listAllDatabases() {
        try {
            PreparedStatement ps = dbConn
                    .prepareStatement("SELECT datname FROM pg_database WHERE datistemplate = false;");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                logger.info("Database: " + rs.getString(1));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        Generic DB Actions
    */

    /**
     * Finds a database entry in a given table by id
     *
     * @param id        id of the entry to find
     * @param tableName name of the table to search
     * @return the resultset of the query if something is found, null otherwise
     */
    private static ResultSet findById(int id, String tableName) {
        logger.info(String.format("Finding by id = %d in %s", id, tableName));
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement(String.format("SELECT * FROM %s WHERE id = ?", tableName));
            st.setInt(1, id);
            rs = st.executeQuery();
            // Moves the result set to the first row if it exists. Returns null otherwise.
            if (!rs.next()) {
                return null;
            }
            // closes rs. Need to close it somewhere else
//            st.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Exception finding by ID: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return rs;
    }

    /**
     * Uses Postgresql pg_get_serial_sequence to select the most recent id for the given table
     *
     * @param tableName The table to get the latest id
     * @return The latest id on success or -1 on failure
     */
    private static int getLatestId(String tableName) {
        try {
            int serialNum = 0;
            Statement stmt = dbConn.createStatement();
//            String query = String.format("select currval(pg_get_serial_sequence(%s, 'id')", tableName);
            String query = String.format("select currval('%s_id_seq')", tableName);
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting most recent in table: " + tableName);
        }
        return -1;
    }

    /**
     * Finds all entries in a given table.
     *
     * @param tableName name of the table.
     * @return a list of all entries in the table.
     */
    private static ResultSet getAllFromTable(String tableName) {
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            Statement st = dbConn.createStatement();
            rs = st.executeQuery("SELECT * FROM " + tableName);
            // This line closes the result set too
//            st.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting all entries: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return rs;
    }

    /**
     * Saves given tag/type/group to the database.
     *
     * @param name  name of the tag/type/group to be inserted.
     * @param table one of tag, type, or group where the name should be inserted.
     * @return auto incremented id
     */
    public static Integer addRowToTable(String table, String name) {
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
            if (rs.next()) {
                serialNum = rs.getInt(1);
                return serialNum;
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error adding row to table: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            try {
                if (st != null) st.close();
            } catch (Exception e) {
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
    public static Boolean deleteById(String table, int id) {
        logger.info(String.format("Deleting by id = %d in %s", id, table));
        PreparedStatement st = null;
        try {
            st = dbConn.prepareStatement(String.format("DELETE FROM %s WHERE id=?", table));
            st.setInt(1, id);
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.severe("Error deleting id: "+id+" from table: "+table+". "+e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (st != null) st.close();
            } catch (Exception e) {
            }
        }
        return false;
    }

    /*
     *      Alert specific actions.
     */

    /**
     * Finds an Alert from the databse with the given id
     *
     * @param id The id of the desired Alert
     * @return An Alert with desired id
     */
    public static Alert findAlert(int id) {
        ResultSet rs = findById(id, "alert");

        if (rs == null) {
            return null;
        } else {
            return rsToAlert(rs);
        }
    }

    /**
     * Finds all Alerts from the database for the given list of UmboxInstance alerterIds.
     *
     * @param alerterIds a list of alerterIds of UmboxInstances.
     * @return a list of all Alerts in the database where the the alert was created by a UmboxInstance with
     * alerterId in alerterIds.
     */
    public static List<Alert> findAlerts(List<String> alerterIds) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        List<Alert> alertHistory = new ArrayList<Alert>();
        for (String alerterId : alerterIds) {
            try {
                st = dbConn.prepareStatement("SELECT * FROM alert WHERE alerter_id = ?");
                st.setString(1, alerterId);
                rs = st.executeQuery();
                while (rs.next()) {
                    alertHistory.add(rsToAlert(rs));
                }
            } catch (SQLException e) {
                logger.severe("Sql exception getting all alert histories: " + e.getClass().getName() + ": " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting alert histories: " + e.getClass().getName() + ": " + e.getMessage());
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
        }
        return alertHistory;
    }

    /**
     * Finds all Alerts from the database for the given deviceId.
     *
     * @param id of the device
     * @return a list of all Alerts in the database associated to the device with the given id
     */
    public static List<Alert> findAlertsByDevice(int deviceId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        List<Alert> alertHistory = new ArrayList<Alert>();

        try {
            st = dbConn.prepareStatement("SELECT * FROM alert WHERE device_id = ?");
            st.setInt(1, deviceId);
            rs = st.executeQuery();
            while (rs.next()) {
                alertHistory.add(rsToAlert(rs));
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all alert histories: " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting alert histories: " + e.getClass().getName() + ": " + e.getMessage());
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
        return alertHistory;
    }

    /**
     * Extract an Alert from the result set of a database query.
     *
     * @param rs ResultSet from a Alert query.
     * @return The Alert that was found.
     */
    private static Alert rsToAlert(ResultSet rs) {
        Alert alert = null;
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            Timestamp timestamp = rs.getTimestamp("timestamp");
            String alerterId = rs.getString("alerter_id");
            int deviceStatusId = rs.getInt("device_status_id");
            int alertTypeId = rs.getInt("alert_type_id");
            int deviceId = rs.getInt("device_id");
            String info = "";
            try { info = rs.getString("info"); }catch (PSQLException e1) { }
            alert = new Alert(id, name, timestamp, alerterId, deviceId, deviceStatusId, alertTypeId, info);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to Alert: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return alert;
    }

    /**
     * Insert a row into the alert table
     * Will insert the alert with either an alerterId or deviceStatusId, but not both
     *
     * @param alert The Alert to be added
     * @return id of new Alert on success. -1 on error
     */
    public static Integer insertAlert(Alert alert) {
        logger.info("Inserting alert: " + alert.toString());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }

        try {
            PreparedStatement insertAlert;
            ResultSet rs;
            int deviceId = alert.getDeviceId();
            if(alert.getDeviceStatusId() == 0) {
                if(deviceId == 0) {
                    PreparedStatement findDeviceId = dbConn.prepareStatement("SELECT device_id FROM umbox_instance WHERE alerter_id = ?;");
                    findDeviceId.setString(1, alert.getAlerterId());
                    rs = findDeviceId.executeQuery();

                    if(rs.next())
                    {
                        deviceId = rs.getInt("device_id");
                        alert.setDeviceId(deviceId);
                    }
                    else
                    {
                        throw new SQLException("Device ID not found for umbox_instance with alerter_id " + alert.getAlerterId());
                    }
                }

                insertAlert = dbConn.prepareStatement("INSERT INTO alert(name, timestamp, alert_type_id, device_id, alerter_id, info) VALUES (?,?,?,?,?,?);");
                insertAlert.setString(1, alert.getName());
                insertAlert.setTimestamp(2, alert.getTimestamp());
                insertAlert.setInt(3, alert.getAlertTypeId());
                insertAlert.setInt(4, deviceId);
                insertAlert.setString(5, alert.getAlerterId());
                insertAlert.setString(6, alert.getInfo());
            }
            else {
                if(deviceId == 0) {
                    PreparedStatement findDeviceId = dbConn.prepareStatement("SELECT device_id FROM device_status WHERE id = ?;");
                    findDeviceId.setInt(1, alert.getDeviceStatusId());
                    rs = findDeviceId.executeQuery();

                    if(rs.next())
                    {
                        deviceId = rs.getInt("device_id");
                        alert.setDeviceId(deviceId);
                    }
                    else
                    {
                        throw new SQLException("Device ID not found for device_status with id " + alert.getDeviceStatusId());
                    }
                }
                insertAlert = dbConn.prepareStatement("INSERT INTO alert(name, timestamp, alert_type_id, alerter_id, device_id, device_status_id, info) VALUES (?,?,?,?,?,?,?);");
                insertAlert.setString(1, alert.getName());
                insertAlert.setTimestamp(2, alert.getTimestamp());
                insertAlert.setInt(3, alert.getAlertTypeId());
                insertAlert.setString(4, alert.getAlerterId());
                insertAlert.setInt(5, deviceId);
                insertAlert.setInt(6, alert.getDeviceStatusId());
                insertAlert.setString(7, alert.getInfo());
            }

            insertAlert.executeUpdate();

            return getLatestId("alert");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting Alert: " + alert.toString() + " " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates Alert with given id to have the parameters of the given Alert.
     *
     * @param alert Alert holding new parameters to be saved in the database.
     * @return the id of the updated Alert on success. -1 on failure
     */
    public static Integer updateAlert(Alert alert) {
        logger.info(String.format("Updating Alert with id = %d with values: %s", alert.getId(), alert));
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
        } else {
            try {
                if (alert.getDeviceStatusId() == 0) {
                    PreparedStatement update = dbConn.prepareStatement("UPDATE alert " +
                            "SET name = ?, timestamp = ?, alerter_id = ?, device_id = ?, alert_type_id = ?, info = ? " +
                            "WHERE id = ?");
                    update.setString(1, alert.getName());
                    update.setTimestamp(2, alert.getTimestamp());
                    update.setString(3, alert.getAlerterId());
                    update.setInt(4, alert.getDeviceId());
                    update.setInt(5, alert.getAlertTypeId());
                    update.setString(6, alert.getInfo());
                    update.setInt(7, alert.getId());
                    update.executeUpdate();
                } else {
                    PreparedStatement update = dbConn.prepareStatement("UPDATE alert " +
                            "SET name = ?, timestamp = ?, alerter_id = ?, device_status_id = ?, device_id = ?, alert_type_id = ?, info = ?" +
                            "WHERE id = ?");
                    update.setString(1, alert.getName());
                    update.setTimestamp(2, alert.getTimestamp());
                    update.setString(3, alert.getAlerterId());
                    update.setInt(4, alert.getDeviceStatusId());
                    update.setInt(5, alert.getDeviceId());
                    update.setInt(6, alert.getAlertTypeId());
                    update.setString(7, alert.getInfo());
                    update.setInt(8, alert.getId());
                    update.executeUpdate();
                }

                return alert.getId();
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error updating Alert: " + e.getClass().toString() + ": " + e.getMessage());
            }
        }
        return -1;
    }

    /**
     * Deletes an Alert by its id.
     *
     * @param id id of the Alert to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteAlert(int id) {
        return deleteById("alert", id);
    }


    /*
     *      AlertCondition specific actions
     */

    /**
     * Finds an AlertCondition from the databse with the given id
     *
     * @param id The id of the desired AlertCondition
     * @return An AlertCondition with desired id
     */
    public static AlertCondition findAlertCondition(int id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        AlertCondition alertCondition = new AlertCondition();
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT ac.*, d.name AS device_name, at.name AS alert_type_name " +
                    "FROM alert_condition AS ac, device AS d, alert_type AS at, alert_type_lookup as atl " +
                    "WHERE ac.id=? AND ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id");
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                alertCondition = rsToAlertCondition(rs);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting all AlertConditions: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return alertCondition;
    }

    /**
     * Finds all AlertConditions in the database
     *
     * @return a list of AlertCondition
     */
    public static List<AlertCondition> findAllAlertConditions() {
        PreparedStatement st = null;
        ResultSet rs = null;
        List<AlertCondition> alertConditionList = new ArrayList<AlertCondition>();
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT ac.*, d.name AS device_name, at.name AS alert_type_name " +
                                         "FROM alert_condition AS ac, device AS d, alert_type AS at, alert_type_lookup as atl " +
                                         "WHERE ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id");
            rs = st.executeQuery();
            while (rs.next()) {
                alertConditionList.add(rsToAlertCondition(rs));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting all AlertConditions: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return alertConditionList;
    }

    /**
     * Finds most recent AlertConditions from the database for the given device_id
     *
     * @param deviceId an id of a device
     * @return a list of all most recent AlertConditions in the database related to the given device
     */
    public static List<AlertCondition> findAlertConditionsByDevice(int deviceId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        List<AlertCondition> conditionList = new ArrayList<AlertCondition>();
        try {
            st = dbConn.prepareStatement("SELECT DISTINCT ON (atl.id) alert_type_lookup_id, ac.id, ac.device_id, d.name AS device_name, at.name AS alert_type_name, ac.variables " +
                                         "FROM alert_condition AS ac, device AS d, alert_type AS at, alert_type_lookup AS atl " +
                                         "WHERE ac.device_id = ? AND ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id");
            st.setInt(1, deviceId);
            rs = st.executeQuery();
            while (rs.next()) {
                conditionList.add(rsToAlertCondition(rs));
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all alert conditions: ");
            e.printStackTrace();
        } catch (Exception e1) {
            logger.severe("Error getting alert conditions: ");
            e1.printStackTrace();
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
        return conditionList;
    }

    /**
     * Extract an AlertCondition from the result set of a database query.
     *
     * @param rs ResultSet from a AlertCondition query.
     * @return The AlertCondition that was found.
     */
    private static AlertCondition rsToAlertCondition(ResultSet rs) {
        AlertCondition cond = null;
        try {
            int id = rs.getInt("id");
            int deviceId = rs.getInt("device_id");
            String deviceName = rs.getString("device_name");
            int alertTypeLookupId = rs.getInt("alert_type_lookup_id");
            String alertTypeName = rs.getString("alert_type_name");
            Map<String, String> variables = null;
            if (rs.getString("variables") != null) {
                variables = HStoreConverter.fromString(rs.getString("variables"));
            }
            cond = new AlertCondition(id, deviceId, deviceName, alertTypeLookupId, alertTypeName, variables);
        } catch (Exception e) {
            logger.severe("Error converting rs to Alert:");
            e.printStackTrace();
        }
        return cond;
    }

    /**
     * Insert a row into the AlertCondition table
     *
     * @param cond The AlertCondition to be added
     * @return id of new AlertCondition on success. -1 on error
     */
    public static Integer insertAlertCondition(AlertCondition cond) {
        logger.info("Inserting alert condition for device: " + cond.getDeviceId());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {
            PreparedStatement insertAlertCondition = dbConn.prepareStatement("INSERT INTO alert_condition(variables, device_id, alert_type_lookup_id) VALUES (?,?,?)");
            insertAlertCondition.setObject(1, cond.getVariables());
            insertAlertCondition.setInt(2, cond.getDeviceId());
            insertAlertCondition.setInt(3, cond.getAlertTypeLookupId());
            insertAlertCondition.executeUpdate();
            return getLatestId("alert_condition");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertCondition: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Insert row(s) into the AlertCondition table based on the given Device's type
     *
     * @param id the Id of the device
     * @return 1 on success. -1 on error
     */
    public static Integer insertAlertConditionForDevice(int id) {
        logger.info("Inserting alert conditions for device: " + id);
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }

        Device d = findDevice(id);
        List<AlertTypeLookup> atlList = findAlertTypeLookupsByDeviceType(d.getType().getId());
        PreparedStatement insertAlertCondition = null;
        for(AlertTypeLookup atl: atlList){
            AlertCondition ac = new AlertCondition(id, atl.getId(), atl.getVariables());
            ac.insertOrUpdate();
            if(ac.getId()<0) //insert failed
                return -1;
        }

        return 1;
    }

    /**
     * Insert row(s) into the AlertCondition table for devices in type specified on the AlertTypeLookup
     *
     * @param cond The AlertCondition to be added
     * @return id of new AlertCondition on success. -1 on error
     */
    public static Integer updateAlertConditionsForDeviceType(AlertTypeLookup alertTypeLookup) {
        logger.info("Inserting alert conditions for device type: " + alertTypeLookup.getDeviceTypeId());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {

            List<Device> deviceList = findDevicesByType(alertTypeLookup.getDeviceTypeId());

            for (Device d : deviceList) {
                PreparedStatement insertAlertCondition = dbConn.prepareStatement("INSERT INTO alert_condition(variables, device_id, alert_type_lookup_id) VALUES (?,?,?)");
                insertAlertCondition.setObject(1, alertTypeLookup.getVariables());
                insertAlertCondition.setInt(2, d.getId());
                insertAlertCondition.setInt(3, alertTypeLookup.getId());
                insertAlertCondition.executeUpdate();
            }

            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertCondition: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Deletes an AlertCondition by its id.
     *
     * @param id id of the AlertCondition to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteAlertCondition(int id) {
        return deleteById("alert_condition", id);
    }

    /*
     *      AlertType specific actions
     */

    /**
     * Finds an AlertType from the databse with the given id
     *
     * @param id The id of the desired AlertType
     * @return An AlertType with desired id
     */
    public static AlertType findAlertType(int id) {
        ResultSet rs = findById(id, "alert_type");
        if (rs == null) {
            return null;
        } else {
            return rsToAlertType(rs);
        }
    }

    /**
     * Finds all AlertTypes from the database for the given type_id
     *
     * @param deviceTypeId an id of a DeviceType
     * @return a list of all AlertTypes in the database for the given DeviceType
     */
    public static List<AlertType> findAlertTypesByDeviceType(int deviceTypeId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        List<AlertType> alertTypeList = new ArrayList<AlertType>();
        try {
            st = dbConn.prepareStatement("SELECT alert_type.id, alert_type.name, alert_type.description, alert_type.source " +
                    "FROM alert_type, alert_type_lookup AS atl " +
                    "WHERE alert_type.id = atl.alert_type_id AND atl.device_type_id = ?;");
            st.setInt(1, deviceTypeId);
            rs = st.executeQuery();
            while (rs.next()) {
                alertTypeList.add(rsToAlertType(rs));
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all alert types: " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting alert types: " + e.getClass().getName() + ": " + e.getMessage());
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
        return alertTypeList;
    }

    /**
     * Finds all AlertTypes in the database
     *
     * @return a list of AlertTypes
     */
    public static List<AlertType> findAllAlertTypes() {
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        List<AlertType> alertTypeList = new ArrayList<AlertType>();
        try {
            ResultSet rs = getAllFromTable("alert_type");
            while (rs.next()) {
                alertTypeList.add(rsToAlertType(rs));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting all AlertTypes: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return alertTypeList;
    }

    /**
     * Extract an AlertType from the result set of a database query.
     *
     * @param rs ResultSet from a AlertType query.
     * @return The AlertType that was found.
     */
    private static AlertType rsToAlertType(ResultSet rs) {
        AlertType type = null;
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String description = rs.getString("description");
            String source = rs.getString("source");
            type = new AlertType(id, name, description, source);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to AlertType: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return type;
    }

    /**
     * Insert a row into the AlertType table
     *
     * @param type The AlertType to be added
     * @return id of new AlertType on success. -1 on error
     */
    public static Integer insertAlertType(AlertType type) {
        logger.info("Inserting alert type: " + type.getName());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {
            PreparedStatement insertAlertType = dbConn.prepareStatement("INSERT INTO alert_type(name, description, source) VALUES (?,?,?);");
            insertAlertType.setString(1, type.getName());
            insertAlertType.setString(2, type.getDescription());
            insertAlertType.setString(3, type.getSource());
            insertAlertType.executeUpdate();
            return getLatestId("alert_type");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertType: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates provided AlertType
     *
     * @param type AlertType holding new values to be saved in the database.
     * @return the id of the updated Alert on success. -1 on failure
     */
    public static Integer updateAlertType(AlertType type) {
        logger.info(String.format("Updating AlertType with id = %d with values: %s", type.getId(), type));
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
        } else {
            try {
                PreparedStatement update = dbConn.prepareStatement("UPDATE alert_type " +
                        "SET name = ?, description = ?, source = ?" +
                        "WHERE id = ?");
                update.setString(1, type.getName());
                update.setString(2, type.getDescription());
                update.setString(3, type.getSource());
                update.setInt(4, type.getId());
                update.executeUpdate();

                return type.getId();
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error updating AlertType: " + e.getClass().toString() + ": " + e.getMessage());
            }
        }
        return -1;
    }

    /**
     * First, attempts to find the AlertType in the database.
     * If successful, updates the existing AlertType with the given AlertType's parameters Otherwise,
     * inserts the given AlertType.
     *
     * @param type AlertType to be inserted or updated.
     */
    public static Integer insertOrUpdateAlertType(AlertType type) {
        AlertType a = findAlertType(type.getId());
        if (a == null) {
            return insertAlertType(type);
        } else {
            return updateAlertType(type);
        }
    }

    /**
     * Deletes an AlertType by its id.
     *
     * @param id id of the AlertType to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteAlertType(int id) {
        logger.info(String.format("Deleting AlertType with id = %d", id));
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
        } else {
            try {
                PreparedStatement deleteAlertTypeLookup = dbConn.prepareStatement("DELETE FROM alert_type_lookup WHERE alert_type_id = ?");
                deleteAlertTypeLookup.setInt(1, id);
                deleteAlertTypeLookup.executeUpdate();

                PreparedStatement deleteAlertType = dbConn.prepareStatement("DELETE FROM alert_type WHERE id = ?");
                deleteAlertType.setInt(1, id);
                deleteAlertType.executeUpdate();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error updating AlertType: " + e.getClass().toString() + ": " + e.getMessage());
            }
        }
        return false;
    }

    /*
     *      AlertTypeLookup specific actions
     */

    /**
     * Returns the row from alert_type_lookup with the given id
     * @param id of the row
     */
    public static AlertTypeLookup findAlertTypeLookup(int id) {
        ResultSet rs = findById(id, "alert_type_lookup");
        if (rs == null) {
            return null;
        } else {
            return rsToAlertTypeLookup(rs);
        }
    }

    /**
     * Returns all rows from alert_type_lookup for the given device_type
     * @param typeId The device_type id
     */
    public static List<AlertTypeLookup> findAlertTypeLookupsByDeviceType(int typeId){
        ResultSet rs = null;
        PreparedStatement st = null;
        List<AlertTypeLookup> atlList = new ArrayList<AlertTypeLookup>();
        if(dbConn == null){
            logger.severe("Tyring to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try{
            st = dbConn.prepareStatement("Select * from alert_type_lookup WHERE device_type_id=?");
            st.setInt(1, typeId);
            rs = st.executeQuery();
            while(rs.next()){
                atlList.add(rsToAlertTypeLookup(rs));
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            logger.severe("Sql exception getting all alert_type_lookups for the device type: "+typeId);
            e.printStackTrace();
        }
        return atlList;
    }

    /**
     * Returns all rows from the alert_type_lookup table
     * @return A list of AlertTypeLookups
     */
    public static List<AlertTypeLookup> findAllAlertTypeLookups() {
        ResultSet rs = getAllFromTable("alert_type_lookup");
        List<AlertTypeLookup> atlList = new ArrayList<AlertTypeLookup>();
        try {
            while (rs.next()) {
                atlList.add(rsToAlertTypeLookup(rs));
            }
            rs.close();
        } catch (SQLException e) {
            logger.severe("Sql exception getting all alert_type_lookups.");
        }
        return atlList;
    }

    private static AlertTypeLookup rsToAlertTypeLookup(ResultSet rs){
        AlertTypeLookup atl = null;
        try {
            int id = rs.getInt("id");
            int alertTypeId = rs.getInt("alert_type_id");
            int deviceTypeId = rs.getInt("device_type_id");
            Map<String, String> variables = null;
            if (rs.getString("variables") != null) {
                variables = HStoreConverter.fromString(rs.getString("variables"));
            }
            atl = new AlertTypeLookup(id, alertTypeId, deviceTypeId, variables);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to AlertType: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return atl;
    }

    /**
     * Inserts the given AlertTypeLookup into the alert_type_lookup table
     * @param atl
     * @return The id of the new AlertTypeLookup. -1 on failure
     */
    public static int insertAlertTypeLookup(AlertTypeLookup atl){
        logger.info("Inserting AlertTypeLookup: " + atl.toString());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {
            PreparedStatement insertAtl = dbConn.prepareStatement("INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES (?,?,?)");
            insertAtl.setInt(1, atl.getAlertTypeId());
            insertAtl.setInt(2, atl.getDeviceTypeId());
            insertAtl.setObject(3, atl.getVariables());
            insertAtl.executeUpdate();
            return getLatestId("alert_type_lookup");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertTypeLookup: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates the row for the given AlertTypeLookup
     * @param atl The object with new values for the row
     * @return The id of the AlertTypeLookup. -1 on failure
     */
    public static int updateAlertTypeLookup(AlertTypeLookup atl){
        logger.info("Updating AlertTypeLookup; atlId: " +atl.getId());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
        }
        try {
            PreparedStatement updateAtl = dbConn.prepareStatement("UPDATE alert_type_lookup SET alert_type_id = ?, device_type_id = ?, variables = ? WHERE id = ?");
            updateAtl.setInt(1, atl.getAlertTypeId());
            updateAtl.setInt(2, atl.getDeviceTypeId());
            updateAtl.setObject(3, atl.getVariables());
            updateAtl.setInt(4, atl.getId());
            updateAtl.executeUpdate();

            return atl.getId();
        } catch (SQLException e) {
            logger.severe("Error updating AlertTypeLookup: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Checks if row exists in alert_type_lookup for the given AlertTypeLookup
     * If there is now row, insert it. Otherwise update the row
     * @param alertTypeLookup
     * @return
     */
    public static int insertOrUpdateAlertTypeLookup(AlertTypeLookup alertTypeLookup) {
        AlertTypeLookup atl = findAlertTypeLookup(alertTypeLookup.getId());
        if(atl == null){
            return insertAlertTypeLookup(alertTypeLookup);
        } else {
            return updateAlertTypeLookup(alertTypeLookup);
        }

    }
    /**
     * Deletes an AlertTypeLookup by its id.
     *
     * @param id id of the AlertTypeLookup to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteAlertTypeLookup(int id) {
        logger.info(String.format("Deleting alert_type_lookup with id = %d", id));
        return deleteById("alert_type_lookup", id);
    }

    /*
     *      Command specific actions
     */

    /**
     * Finds a command based on the given id
     */
    public static DeviceCommand findCommand(int id) {
        logger.info("Finding command with id = " + id);

        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement(String.format("SELECT * FROM command WHERE id = ?"));
            st.setInt(1, id);
            rs = st.executeQuery();
            // Moves the result set to the first row if it exists. Returns null otherwise.
            if (!rs.next()) {
                rs = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Exception finding by ID: " + e.getClass().getName() + ": " + e.getMessage());
        }

        if (rs == null) {
            return null;
        } else {
            DeviceCommand command = rsToCommand(rs);
            return command;
        }
    }

    /**
     * Finds all rows in the command table
     */
    public static List<DeviceCommand> findAllCommands() {
        ResultSet rs = getAllFromTable("command");
        List<DeviceCommand> commands = new ArrayList<DeviceCommand>();
        try {
            while (rs.next()) {
                commands.add(rsToCommand(rs));
            }
            rs.close();
        } catch (SQLException e) {
            logger.severe("Sql exception getting all device commands.");
        }
        return commands;
    }

    //Replaced by findCommandsByPolicyInstance
    /**
     * Finds commands for device based on the state of triggeringDevice
     * @param device the device the commands are for
     * @param triggeringDevice the device that changed state
     * @return List of commands for device
     */
//    public static List<DeviceCommand> findCommandsForGroup(Device device, Device triggeringDevice) {
//        PreparedStatement st = null;
//        ResultSet rs = null;
//        if (dbConn == null) {
//            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
//            return null;
//        }
//        try {
//            List<DeviceCommand> commands = new ArrayList<DeviceCommand>();
//
//            int previousStateId = findPreviousDeviceSecurityStateId(triggeringDevice);
//            if(previousStateId < 0){
//                return commands;
//            }
//            st = dbConn.prepareStatement("SELECT c.id, c.name, c.device_type_id " +
//                    "FROM command_lookup AS cl, command AS c WHERE c.device_type_id = ? AND cl.current_state_id = ? AND cl.previous_state_id = ? AND cl.device_type_id=? AND c.id = cl.command_id");
//            st.setInt(1, device.getType().getId());
//            st.setInt(2, triggeringDevice.getCurrentState().getStateId());
//            st.setInt(3, previousStateId);
//            st.setInt(4, triggeringDevice.getType().getId());
//            rs = st.executeQuery();
//
//            while (rs.next()) {
//                commands.add(rsToCommand(rs));
//            }
//            return commands;
//        } catch (SQLException e) {
//            logger.severe("Sql exception getting all commands for device: " + e.getClass().getName() + ": " + e.getMessage());
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.severe("Error getting device commands: " + e.getClass().getName() + ": " + e.getMessage());
//        } finally {
//            try {
//                if (rs != null) {
//                    rs.close();
//                }
//            } catch (Exception e) {
//            }
//            try {
//                if (st != null) {
//                    st.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//        return null;
//    }

    /**
     * Finds the commands for the device from the policy instance
     *
     * @param instanceId The policy instance id
     * @return commands A list of command names
     */
    public static List<DeviceCommand> findCommandsByPolicyInstance(int instanceId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            List<DeviceCommand> commands = new ArrayList<DeviceCommand>();

            st = dbConn.prepareStatement("SELECT c.id, c.name, c.device_type_id FROM command_lookup AS cl, command AS c, policy_instance AS pi " +
                    "WHERE pi.policy_id = cl.policy_id AND c.id = cl.command_id AND pi.id = ?");
            st.setInt(1, instanceId);
            rs = st.executeQuery();

            while (rs.next()) {
                commands.add(rsToCommand(rs));
            }
            return commands;
        } catch (SQLException e) {
            logger.severe("Sql exception getting all commands for device: " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting device commands: " + e.getClass().getName() + ": " + e.getMessage());
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
        return null;
    }

    /**
     * Extract a Command name from the result set of a database query.
     *
     * @param rs ResultSet from a Command query.
     * @return The command.
     */
    private static DeviceCommand rsToCommand(ResultSet rs) {
        DeviceCommand command = null;
        Integer id = null;
        String name = "";
        Integer deviceTypeId = null;

        try {
            id = rs.getInt("id");
            name = rs.getString("name");
            deviceTypeId = rs.getInt("device_type_id");
            command = new DeviceCommand(id, name, deviceTypeId);
            return command;
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to Command name: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * insert a command into the db
     */
    public static Integer insertCommand(DeviceCommand command) {
        logger.info("Inserting command: " + command.getName());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {
            PreparedStatement insertCommand = dbConn.prepareStatement("INSERT INTO command(name, device_type_id) VALUES (?,?);");
            insertCommand.setString(1, command.getName());
            insertCommand.setInt(2, command.getDeviceTypeId());
            insertCommand.executeUpdate();
            return getLatestId("command");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting Command: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates the command entry
     *
     * @input the command to update
     */
    public static Integer updateCommand(DeviceCommand command) {
        logger.info("Updating command; commandId: " +command.getId()+ " name: " +command.getName());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
        }
        try {
            PreparedStatement updatecommand = dbConn.prepareStatement("UPDATE command SET name = ?, device_type_id = ? WHERE id = ?");
            updatecommand.setString(1, command.getName());
            updatecommand.setInt(2, command.getDeviceTypeId());
            updatecommand.setInt(3, command.getId());
            updatecommand.executeUpdate();

            return command.getId();
        } catch (SQLException e) {
            logger.severe("Error updating Command: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * First, attempts to find the Command id in the database.
     * If successful, updates the existing Command with the given Device Commands's parameters Otherwise,
     * inserts the given Device Command as a Command Lookup.
     */
    public static Integer insertOrUpdateCommand(DeviceCommand command) {
        DeviceCommand dc = findCommand(command.getId());
        if (dc == null) {
            return insertCommand(command);
        } else {
            return updateCommand(command);
        }
    }

    /**
     * Deletes a Command by its id.
     *
     * @param id id of the Command Lookup to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteCommand(int id) {
        logger.info(String.format("Deleting command with id = %d", id));
        return deleteById("command", id);
    }

    /*
     *      CommandLookup specific actions
     */

    /**
     * Finds a command lookup based on the given id
     */
    public static DeviceCommandLookup findCommandLookup(int id) {
        logger.info("Finding command lookup with id = " + id);
        return rsToCommandLookup(findById(id, "command_lookup"));
    }

    /**
     * Finds all command lookups based on the given device id
     */
    public static List<DeviceCommandLookup> findCommandLookupsByDevice(int deviceId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        List<DeviceCommandLookup> lookupList = new ArrayList<DeviceCommandLookup>();
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {

            st = dbConn.prepareStatement("SELECT cl.* FROM command_lookup cl, device d, command c " +
                    "WHERE d.id = ? AND c.device_type_id = d.type_id AND c.id=cl.command_id");
            st.setInt(1,deviceId);
            rs = st.executeQuery();
            while (rs.next()) {
                lookupList.add(rsToCommandLookup(rs));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Exception finding umbox lookup: " + e.getClass().getName() + ": " + e.getMessage());
        }

        return lookupList;
    }

    /**
     * Finds all rows in the command lookup table
     */
    public static List<DeviceCommandLookup> findAllCommandLookups() {
        ResultSet rs = getAllFromTable("command_lookup");
        List<DeviceCommandLookup> commands = new ArrayList<DeviceCommandLookup>();
        try {
            while (rs.next()) {
                commands.add(rsToCommandLookup(rs));
            }
            rs.close();
        } catch (SQLException e) {
            logger.severe("Sql exception getting all device command lookups.");
        }
        return commands;
    }

    /**
     * Extract a Command from the result set of a database query.
     *
     * @param rs ResultSet from a CommandLookup query.
     * @return The command.
     */
    private static DeviceCommandLookup rsToCommandLookup(ResultSet rs) {
        DeviceCommandLookup commandLookup = null;
        try {
            int id = rs.getInt("id");
            int commandId = rs.getInt("command_id");
            int policyId = rs.getInt("policy_id");
            commandLookup = new DeviceCommandLookup(id, commandId, policyId);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to CommandLookup name: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return commandLookup;
    }

    /**
     * inserts into command lookup to relate a state and command
     *
     * @return -1 on failure, 1 on success
     */
    public static int insertCommandLookup(DeviceCommandLookup commandLookup) {
        logger.info("Inserting command lookup; commandId: "+ commandLookup.getCommandId());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {
            PreparedStatement insertCommandLookup =
                    dbConn.prepareStatement("INSERT INTO command_lookup(command_id, policy_id) VALUES (?,?)");
            insertCommandLookup.setInt(1, commandLookup.getCommandId());
            insertCommandLookup.setInt(2, commandLookup.getPolicyId());
            insertCommandLookup.executeUpdate();

            return getLatestId("command_lookup");
        } catch (SQLException e) {
            logger.severe("Error inserting CommandLookup: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * First, attempts to find the Command Lookup id in the database.
     * If successful, updates the existing Command Lookup with the given Device Commands's parameters Otherwise,
     * inserts the given Device Command as a Command Lookup.
     */
    public static Integer insertOrUpdateCommandLookup(DeviceCommandLookup commandLookup) {
        DeviceCommandLookup cl = findCommandLookup(commandLookup.getId());
        if (cl == null) {
            return insertCommandLookup(commandLookup);
        } else {
            return updateCommandLookup(commandLookup);
        }
    }

    /**
     * Updates the command lookup entry
     *
     * @input the command lookup id to update
     * @input the device command to update
     */

    public static Integer updateCommandLookup(DeviceCommandLookup commandLookup) {
        logger.info("Updating command lookup; commandId: " +commandLookup.getCommandId());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
        }
        try {
            PreparedStatement updatecommand = dbConn.prepareStatement("UPDATE command_lookup SET command_id = ?, policy_id = ? WHERE id = ?");
            updatecommand.setInt(1, commandLookup.getCommandId());
            updatecommand.setInt(2, commandLookup.getPolicyId());
            updatecommand.setInt(3, commandLookup.getId());
            updatecommand.executeUpdate();

            return commandLookup.getId();
        } catch (SQLException e) {
            logger.severe("Error inserting Command: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Deletes a Command Lookup by its id.
     *
     * @param id id of the Command Lookup to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteCommandLookup(int id) {
        logger.info(String.format("Deleting command lookup with id = %d", id));
        return deleteById("command_lookup", id);
    }

    /*
     *       Device specific actions
     */

    /**
     * Finds a Device from the database by its id.
     *
     * @param id id of the Device to find.
     * @return the Device if it exists in the database, else null.
     */
    public static Device findDevice(int id) {
        logger.info("Finding device with id = " + id);
        ResultSet rs = findById(id, "device");
        if (rs == null) {
            return null;
        } else {
            Device device = rsToDevice(rs);
            List<Integer> tagIds = findTagIds(device.getId());
            device.setTagIds(tagIds);

            DeviceSecurityState ss = findDeviceSecurityStateByDevice(device.getId());
            device.setCurrentState(ss);

            return device;
        }
    }

    /**
     * Finds all Devices in the database.
     *
     * @return a list of all Devices in the database.
     */
    public static List<Device> findAllDevices() {
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        List<Device> devices = new ArrayList<Device>();
        try {
            ResultSet rs = getAllFromTable("device");
            while (rs.next()) {
                Device d = rsToDevice(rs);
                List<Integer> tagIds = findTagIds(d.getId());
                d.setTagIds(tagIds);
                DeviceSecurityState ss = findDeviceSecurityStateByDevice(d.getId());
                d.setCurrentState(ss);

                devices.add(d);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting all Devices: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return devices;
    }

    /**
     * Find devices in a given group
     *
     * @return a list of Devices with the given groupId
     * @parameter groupId the id of the group
     */
    public static List<Device> findDevicesByGroup(int groupId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        List<Device> devices = new ArrayList<Device>();
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT * FROM device WHERE group_id = ?");
            st.setInt(1, groupId);
            rs = st.executeQuery();

            while (rs.next()) {
                Device d = rsToDevice(rs);
                d.setCurrentState(findDeviceSecurityStateByDevice(d.getId()));
                devices.add(d);
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all devices for group: " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting device statuses: " + e.getClass().getName() + ": " + e.getMessage());
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
        return devices;
    }

    public static Device findDeviceByDeviceSecurityState(int dssId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try{
            st = dbConn.prepareStatement("SELECT device_id FROM device_security_state WHERE id = ?");
            st.setInt(1, dssId);

            rs = st.executeQuery();
            if(rs.next()){
                int id = rs.getInt("device_id");
                Device d = findDevice(id);
                d.setCurrentState(findDeviceSecurityStateByDevice(d.getId()));
                return d;
            }
        } catch (Exception e) {
            logger.severe("Error while finding the device with device security state: "+dssId+".");
            logger.severe(e.getMessage());
        }
        return null;
    }

    /**
     * Finds the Device related to the given Alert id
     *
     * @param the Alert
     * @return the Device associated with the alert
     */
    public static Device findDeviceByAlert(Alert alert) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        if (alert.getDeviceId() != null) {
            return findDevice(alert.getDeviceId());
        } else {
            logger.severe("Error: alert has no associated DeviceStatus OR UmboxInstance!");
            return null;
        }
    }

    /**
     * Finds the Device related to the given DeviceType id
     *
     * @param the Alert
     * @return the Device associated with the alert
     */
    public static List<Device> findDevicesByType(int id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        List<Device> deviceList = new ArrayList<Device>();

        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {

            st = dbConn.prepareStatement("SELECT * FROM device WHERE type_id = ?");
            st.setInt(1, id);
            rs = st.executeQuery();
            while (rs.next()){
                Device d = rsToDevice(rs);
                d.setCurrentState(findDeviceSecurityStateByDevice(d.getId()));
                deviceList.add(d);
            }


            return deviceList;
        } catch (SQLException e) {
            logger.severe("Sql exception getting the device for the alert: " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting device for the alert: " + e.getClass().getName() + ": " + e.getMessage());
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
        return null;
    }

    /**
     * Extract a Device from the result set of a database query.
     *
     * @param rs ResultSet from a Device query.
     * @return The Device that was found.
     */
    private static Device rsToDevice(ResultSet rs) {
        Device device = null;
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String description = rs.getString("description");
            int typeId = rs.getInt("type_id");
            int groupId = rs.getInt("group_id");
            String ip = rs.getString("ip_address");
            int statusHistorySize = rs.getInt("status_history_size");
            int samplingRate = rs.getInt("sampling_rate");
            int defaultSamplingRate = rs.getInt("default_sampling_rate");
            device = new Device(id, name, description, typeId, groupId, ip, statusHistorySize, samplingRate, defaultSamplingRate);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to Device: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return device;
    }

    /**
     * Saves given Device to the database.
     *
     * @param device Device to be inserted.
     * @return auto incremented id
     */
    public static Device insertDevice(Device device) {
        logger.info("Inserting device: " + device);
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            PreparedStatement update = dbConn.prepareStatement
                    ("INSERT INTO device(description, name, type_id, group_id, ip_address," +
                            "status_history_size, sampling_rate, default_sampling_rate) values(?,?,?,?,?,?,?,?)");
            update.setString(1, device.getDescription());
            update.setString(2, device.getName());
            update.setInt(3, device.getType().getId());
            if (device.getGroup() != null) {
                update.setInt(4, device.getGroup().getId());
            } else {
                update.setObject(4, null);
            }

            update.setString(5, device.getIp());
            update.setInt(6, device.getStatusHistorySize());
            update.setInt(7, device.getSamplingRate());
            update.setInt(8, device.getDefaultSamplingRate());
            update.executeUpdate();

            int serialNum = getLatestId("device");
            device.setId(serialNum);

            DeviceSecurityState currentState = device.getCurrentState();
            Integer stateId = null;

            //give the device a normal security state if it is not specified
            if(currentState == null) {
                //get the id of normal security state
                PreparedStatement st = dbConn.prepareStatement("SELECT * FROM security_state WHERE name = ?;");
                st.setString(1, "Normal");
                ResultSet rs = st.executeQuery();

                if(rs.next()) {
                    SecurityState securityState = rsToSecurityState(rs);

                    DeviceSecurityState normalDeviceState = new DeviceSecurityState(device.getId(), securityState.getId(), securityState.getName());
                    normalDeviceState.insert();

                    device.setCurrentState(normalDeviceState);
                }
                else {
                    throw new NoSuchElementException("No normal security state has been added to the database");
                }
            }
            else {
                logger.info("current state isnt null");
                stateId = currentState.getId();
            }

            //Insert tags into device_tag
            List<Integer> tagIds = device.getTagIds();
            if (tagIds != null) {
                for (int tagId : tagIds) {
                    executeCommand(String.format("INSERT INTO device_tag(device_id, tag_id) values (%d,%d)", serialNum, tagId));
                }
            }

            return device;
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error inserting Device: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * First, attempts to find the Device in the database.
     * If successful, updates the existing Device with the given Device's parameters Otherwise,
     * inserts the given Device.
     *
     * @param device Device to be inserted or updated.
     */
    public static Device insertOrUpdateDevice(Device device) {
        Device d = findDevice(device.getId());
        if (d == null) {
            return insertDevice(device);
        } else {
            return updateDevice(device);
        }
    }

    /**
     * Updates Device with given id to have the parameters of the given Device.
     *
     * @param device Device holding new parameters to be saved in the database.
     * @return the id of the updated device
     */
    public static Device updateDevice(Device device) {
        logger.info(String.format("Updating Device with id = %d with values: %s", device.getId(), device));
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
        } else {
            try {
                // Delete existing tags
                executeCommand(String.format("DELETE FROM device_tag WHERE device_id = %d", device.getId()));

                PreparedStatement update = dbConn.prepareStatement("UPDATE device " +
                        "SET name = ?, description = ?, type_id = ?, group_id = ?, ip_address = ?, status_history_size = ?, sampling_rate = ? " +
                        "WHERE id = ?");
                update.setString(1, device.getName());
                update.setString(2, device.getDescription());
                update.setInt(3, device.getType().getId());
                if (device.getGroup() != null)
                    update.setInt(4, device.getGroup().getId());
                else
                    update.setObject(4, null);
                update.setString(5, device.getIp());
                update.setInt(6, device.getStatusHistorySize());
                update.setInt(7, device.getSamplingRate());

                update.setInt(8, device.getId());
                update.executeUpdate();

                // Insert tags into device_tag
                List<Integer> tagIds = device.getTagIds();
                if (tagIds != null) {
                    for (int tagId : tagIds) {
                        executeCommand(String.format("INSERT INTO device_tag(device_id, tag_id) values (%d,%d)", device.getId(), tagId));
                    }
                }
                return device;
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error updating Device: " + e.getClass().getName() + ": " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Deletes a Device by its id.
     *
     * @param id id of the Device to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteDevice(int id) {
        logger.info(String.format("Deleting device with id = %d", id));
        PreparedStatement st = null;
        try {
            deleteById("device", id);
            return true;
        } catch (Exception e) {
            logger.severe("Error while deleting device with id: "+id+". "+e.getMessage());
        } finally {
            try {
                if (st != null) st.close();
            } catch (Exception e) {
            }
            return false;
        }
    }

    /**
     * inserts a state reset alert for the given device id
     */
    public static void resetSecurityState(int deviceId) {
        logger.info("Inserting a state reset alert for device id: " +deviceId);
        PreparedStatement st = null;
        ResultSet rs;

        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
        }
        try {

            st = dbConn.prepareStatement("SELECT name, id FROM alert_type WHERE name = ?;");
            st.setString(1, "state-reset");
            rs = st.executeQuery();

            if(rs.next()) {
                String name = rs.getString("name");
                int alertTypeId = rs.getInt("id");

                Alert alert = new Alert(deviceId, name, alertTypeId, "State reset");
                alert.insert();
            }
        } catch (SQLException e) {
            logger.severe("Sql exception inserting state reset alert: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /*
     *     DeviceStatus specific actions
     */

    /**
     * Finds a DeviceStatus from the database by its id.
     *
     * @param id id of the DeviceStatus to find.
     * @return the DeviceStatus if it exists in the database, else null.
     */
    public static DeviceStatus findDeviceStatus(int id) {
        ResultSet rs = findById(id, "device_status");
        if (rs == null) {
            return null;
        } else {
            return rsToDeviceStatus(rs);
        }
    }

    /**
     * Finds all DeviceStatuses from the database for the given device.
     *
     * @param deviceId the id of the device.
     * @return a list of all DeviceStatuses in the database where the device_id field is equal to deviceId.
     */
    public static List<DeviceStatus> findDeviceStatuses(int deviceId) {
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
        return null;
    }

    /**
     * Finds the last N DeviceStatuses for the given device
     *
     * @param deviceId the id of the device
     * @param N        the number of statuses to retrieve
     * @return a list of N device statuses
     */
    public static List<DeviceStatus> findNDeviceStatuses(int deviceId, int N) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            logger.info("Finding last " + N + "device statuses for device: " + deviceId);
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
        return null;
    }

    /**
     * Finds numStatuses worth of device statuses where their id < startingId
     * @param deviceId
     * @param numStatuses
     * @param startingId
     * @return list of device statuses
     */
    public static List<DeviceStatus> findSubsetNDeviceStatuses(int deviceId, int numStatuses, int startingId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            logger.info("Finding "+numStatuses+" previous statuses from id: "+startingId);
            st = dbConn.prepareStatement("SELECT * FROM device_status WHERE id < ? AND device_id = ? ORDER BY id DESC LIMIT ?");
            st.setInt(1, startingId);
            st.setInt(2, deviceId);
            st.setInt(3, numStatuses);
            rs = st.executeQuery();

            List<DeviceStatus> deviceStatusList = new ArrayList<DeviceStatus>();
            while(rs.next()){
                deviceStatusList.add(rsToDeviceStatus(rs));
            }
            return deviceStatusList;
        } catch(SQLException e) {
            logger.severe("SQL Exception getting subset of device statuses: "+e.getClass().getName()+": "+e.getMessage());
        } catch(Exception e){
            logger.severe("Error getting subset of device statuses: "+e.getClass().getName()+": "+e.getMessage());
        } finally {
            try{
                if(rs!=null)
                    rs.close();
            } catch (Exception e) {logger.severe("Error closing result set: "+e.getMessage());}
            try{
                if(st!=null)
                    st.close();
            }catch (Exception e) {logger.severe("Error closing prepared statement: "+e.getMessage());}
        }
        return null;
    }

    /**
     * Finds the last N DeviceStatuses for the given device
     *
     * @param deviceId the id of the device
     * @param startingTime The timestamp to start
     * @param period The amount of time back to search
     * @param timeUnit the unit of time to use (minute(s), hour(s), day(s))
     * @return a list of N device statuses
     */
    public static List<DeviceStatus> findDeviceStatusesOverTime(int deviceId, Timestamp startingTime, int period, String timeUnit) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            String interval = String.valueOf(period)+" "+timeUnit;
            st = dbConn.prepareStatement("SELECT * FROM device_status WHERE device_id = ? AND timestamp between (?::timestamp - (?::interval)) and ?::timestamp");
            st.setInt(1, deviceId);
            st.setTimestamp(2, startingTime);
            st.setString(3, interval);
            st.setTimestamp(4, startingTime);
            logger.info("Parameter count: "+String.valueOf(st.getParameterMetaData().getParameterCount()));
            rs = st.executeQuery();

            List<DeviceStatus> deviceHistories = new ArrayList<DeviceStatus>();
            while (rs.next()) {
                deviceHistories.add(rsToDeviceStatus(rs));
            }
            return deviceHistories;
        } catch (SQLException e) {
            logger.severe("Sql exception getting all device statuses: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting device statuses: " + e.getClass().getName() + ": " + e.getMessage());
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
        return null;
    }

    /**
     * Returns a list of device statuses for devices with the given type id. One device status per device
     *
     * @param typeId The typeid for the requested devices
     * @return A map pairing a device with its most recent DeviceStatus
     */

    public static Map<Device, DeviceStatus> findDeviceStatusesByType(int typeId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
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
                while (resultSet.next()) {
                    deviceStatus = rsToDeviceStatus(resultSet);
                }
                deviceStatusMap.put(device, deviceStatus);
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting devices for type: " + typeId + " " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting devices for type: " + typeId + " " + e.getClass().getName() + ": " + e.getMessage());
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
        return deviceStatusMap;
    }

    /**
     * Returns a list of device statuses for devices with the given group id. One device status per device
     *
     * @param groupId The typeid for the requested devices
     * @return A map pairing a device with its most recent DeviceStatus
     */

    public static Map<Device, DeviceStatus> findDeviceStatusesByGroup(int groupId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        Map<Device, DeviceStatus> deviceStatusMap = new HashMap<Device, DeviceStatus>();
        try {
            st = dbConn.prepareStatement("SELECT * FROM device WHERE group_id = ?");
            st.setInt(1, groupId);
            rs = st.executeQuery();
            while (rs.next()) {
                Device device = rsToDevice(rs);
                PreparedStatement statement = dbConn.prepareStatement("SELECT * FROM device_status WHERE device_id = ? ORDER BY id DESC LIMIT 1");
                statement.setInt(1, device.getId());
                ResultSet resultSet = statement.executeQuery();

                DeviceStatus deviceStatus = null;
                while (resultSet.next()) {
                    deviceStatus = rsToDeviceStatus(resultSet);
                }
                deviceStatusMap.put(device, deviceStatus);
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting devices for group: " + groupId + " " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting devices for group: " + groupId + " " + e.getClass().getName() + ": " + e.getMessage());
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
        return deviceStatusMap;
    }

    /**
     * Finds all DeviceStatuses in the database.
     *
     * @return a list of all DeviceStatuses in the database.
     */
    public static List<DeviceStatus> findAllDeviceStatuses() {
        ResultSet rs = getAllFromTable("device_status");
        List<DeviceStatus> deviceStatuses = new ArrayList<DeviceStatus>();
        try {
            while (rs.next()) {
                deviceStatuses.add(rsToDeviceStatus(rs));
            }
            rs.close();
        } catch (SQLException e) {
            logger.severe("Sql exception getting all device statuses.");
        }
        return deviceStatuses;
    }

    /**
     * Extract a DeviceStatus from the result set of a database query.
     *
     * @param rs ResultSet from a DeviceStatus query.
     * @return The DeviceStatus that was found.
     */
    private static DeviceStatus rsToDeviceStatus(ResultSet rs) {
        DeviceStatus deviceStatus = null;
        try {
            int deviceId = rs.getInt("device_id");
            Map<String, String> attributes = HStoreConverter.fromString(rs.getString("attributes"));
            Timestamp timestamp = rs.getTimestamp("timestamp");
            int statusId = rs.getInt("id");

            deviceStatus = new DeviceStatus(deviceId, attributes, timestamp, statusId);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error converting rs to DeviceStatus: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return deviceStatus;
    }

    /**
     * Saves given DeviceStatus to the database.
     *
     * @param deviceStatus DeviceStatus to be inserted.
     * @return auto incremented id
     */
    public static Integer insertDeviceStatus(DeviceStatus deviceStatus) {
        logger.info("Inserting device_status: " + deviceStatus.toString());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {
            PreparedStatement update = dbConn.prepareStatement
                    ("INSERT INTO device_status(device_id, timestamp, attributes) values(?,?,?)");
            update.setInt(1, deviceStatus.getDeviceId());
            update.setTimestamp(2, deviceStatus.getTimestamp());
            update.setObject(3, deviceStatus.getAttributes());
            update.executeUpdate();

            return getLatestId("device_status");
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error inserting DeviceStatus: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }


    /**
     * First, attempts to find the DeviceStatus in the database.
     * If successful, updates the existing DeviceStatus with the given DeviceStatus's parameters Otherwise,
     * inserts the given DeviceStatus.
     *
     * @param deviceStatus DeviceStatus to be inserted or updated.
     */
    public static Integer insertOrUpdateDeviceStatus(DeviceStatus deviceStatus) {
        DeviceStatus ds = findDeviceStatus(deviceStatus.getId());
        if (ds == null) {
            return insertDeviceStatus(deviceStatus);
        } else {
            return updateDeviceStatus(deviceStatus);
        }
    }

    /**
     * Updates DeviceStatus with given id to have the parameters of the given DeviceStatus.
     *
     * @param deviceStatus DeviceStatus holding new parameters to be saved in the database.
     */
    public static Integer updateDeviceStatus(DeviceStatus deviceStatus) {
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
    }

    /**
     * Deletes an DeviceStatus by its id.
     *
     * @param id id of the DeviceStatus to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteDeviceStatus(int id) {
        return deleteById("device_status", id);
    }

    /*
     *      Group specific actions
     */

    /**
     * Finds a Group from the database by its id.
     *
     * @param id id of the Group to find.
     * @return the Group if it exists in the database, else null.
     */
    public static Group findGroup(int id) {
        ResultSet rs = findById(id, "device_group");
        if (rs == null) {
            return null;
        } else {
            Group group = rsToGroup(rs);
            return group;
        }
    }

    /**
     * Finds all Groups in the database.
     *
     * @return a list of all Groups in the database.
     */
    public static List<Group> findAllGroups() {
        ResultSet rs = getAllFromTable("device_group");
        List<Group> groups = new ArrayList<Group>();
        try {
            while (rs.next()) {
                groups.add(rsToGroup(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("SQLException getting all Groups: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
            }
        }
        return groups;
    }

    /**
     * Extract a Group from the result set of a database query.
     *
     * @param rs ResultSet from a Group query.
     * @return The first Group in rs.
     */
    private static Group rsToGroup(ResultSet rs) {
        Group group = null;
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            group = new Group(id, name);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to Group: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return group;
    }

    /**
     * Saves given Device Group to the database.
     *
     * @param group Device Group to be inserted.
     * @return auto incremented id
     */
    public static Integer insertGroup(Group group) {
        logger.info("Inserting group: " + group.getName());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {
            PreparedStatement update = dbConn.prepareStatement
                    ("INSERT INTO device_group(name)" +
                            "values(?)");
            update.setString(1, group.getName());
            update.executeUpdate();
            return getLatestId("device_group");
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error inserting Group: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }


    /**
     * Updates Group with given id to have the parameters of the given Group.
     *
     * @param group group holding new parameters to be saved in the database.
     */
    public static Integer updateGroup(Group group) {
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
    }

    /**
     * First, attempts to find the Group in the database.
     * If successful, updates the existing Group with the given Groups's parameters. Otherwise,
     * inserts the given Group.
     *
     * @param group Group to be inserted or updated.
     */
    public static Integer insertOrUpdateGroup(Group group) {
        Group g = findGroup(group.getId());
        if (g == null) {
            return insertGroup(group);
        } else {
            return updateGroup(group);
        }
    }

    /**
     * Deletes a Group by its id.
     *
     * @param id id of the Group to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteGroup(int id) {
        return deleteById("device_group", id);
    }

    /*
     *      Policy specific actions
     */

    public static Policy findPolicy(int id) {
        return rsToPolicy(findById(id, "policy"));
    }

    /**
     * Finds the policy given the StateTransition PolicyCondition and DeviceType id's
     * @param stateTransId
     * @param policyCondId
     * @param devTypeId
     * @return
     */
    public static Policy findPolicy(int stateTransId, int policyCondId, int devTypeId) {
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }

        try {
            PreparedStatement query = dbConn.prepareStatement("SELECT * FROM policy WHERE " +
                    "state_trans_id = ? AND " +
                    "policy_cond_id = ? AND " +
                    "device_type_id = ?");
            query.setInt(1, stateTransId);
            query.setInt(2, policyCondId);
            query.setInt(3, devTypeId);

            ResultSet rs = query.executeQuery();
            rs.next();
            return rsToPolicy(rs);
        } catch (Exception e) {
            logger.severe("Error finding Policy: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Converts a ResultSet obj to a Policy
     * @param rs Result set of a query to the policy table
     * @return The object representing the query result
     */
    public static Policy rsToPolicy(ResultSet rs) {
        Policy policy = null;
        try {
            int id = rs.getInt("id");
            int stateTransId = rs.getInt("state_trans_id");
            int policyCondId = rs.getInt("policy_cond_id");
            int devTypeId = rs.getInt("device_type_id");
            int samplingRate = rs.getInt("sampling_rate");
            policy = new Policy(id, stateTransId, policyCondId, devTypeId, samplingRate);
        } catch (Exception e) {
            logger.severe("Error converting rs to Policy: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return policy;
    }

    /**
     * Inserts the given Policy obj to the policy table
     * @param policy The obj to insert
     * @return Row's id on success. -1 otherwise
     */
    public static Integer insertPolicy(Policy policy) {
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }

        try {
            PreparedStatement insert = dbConn.prepareStatement("INSERT INTO policy(state_trans_id, policy_cond_id, device_type_id, sampling_rate) VALUES(?,?,?,?)");
            insert.setInt(1, policy.getStateTransId());
            insert.setInt(2, policy.getPolicyCondId());
            insert.setInt(3, policy.getDevTypeId());
            insert.setInt(4, policy.getSamplingRate());
            insert.executeUpdate();
            return getLatestId("policy");
        } catch (Exception e) {
            logger.severe("Error inserting Policy: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Updates the row in the policy table with the given id
     * @param policy
     * @return The id of the given policy on success. -1 otherwise
     */
    public static Integer updatePolicy(Policy policy) {
        if (dbConn == null) {
           logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
           return -1;
        }

        try {
            PreparedStatement update = dbConn.prepareStatement("UPDATE policy SET " +
                    "state_trans_id = ? " +
                    "policy_cond_id = ? " +
                    "device_type_id = ? " +
                    "sampling_rate = ? " +
                    "WHERE id = ?");
            update.setInt(1, policy.getStateTransId());
            update.setInt(2, policy.getPolicyCondId());
            update.setInt(3, policy.getDevTypeId());
            update.setInt(4, policy.getSamplingRate());
            update.setInt(5, policy.getId());
            update.executeUpdate();
            return policy.getId();
        } catch (Exception e) {
            logger.severe("Error updating Policy: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /***
     * Delete a row in the policy table with the given id
     * @param policyId
     * @return True on success, false otherwise
     */
    public static boolean deletePolicy(int policyId) {
        return deleteById("policy", policyId);
    }

    /*
     *      PolicyCondition specific actions
     */

    /**
     * Find a PolicyCondition and it's associated AlertType id's
     * @param id
     * @return A PolicyCondition obj. Null otherwise
     */
    public static PolicyCondition findPolicyCondition(int id) {
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }

        try {
            PolicyCondition policyCondition = rsToPolicyCondition(findById(id,"policy_condition"));
            PreparedStatement query = dbConn.prepareStatement("SELECT * FROM policy_condition_alert WHERE policy_cond_id = ?");
            query.setInt(1, policyCondition.getId());
            ResultSet rs = query.executeQuery();

            List<Integer> alertTypeIds = new ArrayList<>();
            while(rs.next()) {
                alertTypeIds.add(rs.getInt("alert_type_id"));
            }
            policyCondition.setAlertTypeIds(alertTypeIds);

            return policyCondition;
        } catch (Exception e) {
            logger.severe("Error converting rs to PolicyCondition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Converts a ResultSet obj to a PolicyCondition
     * @param rs Result set of a query to the policy table
     * @return The object representing the query result
     */
    public static PolicyCondition rsToPolicyCondition(ResultSet rs) {
        PolicyCondition policyCondition = null;
        try {
            int id = rs.getInt("id");
            int threshold = rs.getInt("threshold");
            policyCondition = new PolicyCondition(id, threshold, null);
        } catch (Exception e) {
            logger.severe("Error converting rs to PolicyCondition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return policyCondition;
    }

    /**
     * Inserts a row into the policy_condition table and a row for each alert_type_id in policy_condition_alert
     * @param policyCondition
     * @return
     */
    public static Integer insertPolicyCondition(PolicyCondition policyCondition) {
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }

        try {
            PreparedStatement insert = dbConn.prepareStatement("INSERT INTO policy_condition(threshold) VALUES(?)");
            insert.setInt(1, policyCondition.getThreshold());
            insert.executeUpdate();
            policyCondition.setId(getLatestId("policy_condition"));

            if(policyCondition.getAlertTypeIds() != null){
                for(int i=0; i<policyCondition.getAlertTypeIds().size(); i++) {
                    int id = policyCondition.getAlertTypeIds().get(i);
                    insert = dbConn.prepareStatement("INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES(?,?)");
                    insert.setInt(1, policyCondition.getId());
                    insert.setInt(2, id);
                    insert.executeQuery();
                }
            }

            return policyCondition.getId();
        } catch (Exception e) {
            logger.severe("Error inserting PolicyCondition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Updates a row in policy_condition and related rows in policy_condition_alert
     * @param policyCondition The policy condition to update
     * @return the condition's id on succes; -1 on failure
     */
    public static Integer updatePolicyCondition(PolicyCondition policyCondition) {
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }

        try {
            // Update PolicyCondition table
            PreparedStatement update = dbConn.prepareStatement("UPDATE policy_condition SET threshold = ? WHERE id = ?");
            update.setInt(1, policyCondition.getThreshold());
            update.setInt(2, policyCondition.getId());
            update.executeUpdate();

            // Update PolicyConditionAlert table
            if(!deletePolicyConditionAlertRows(policyCondition.getId()))
                return -1;

            for(Integer alertId: policyCondition.getAlertTypeIds()) {
                PreparedStatement insert = dbConn.prepareStatement("INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES(?,?)");
                insert.setInt(1, policyCondition.getId());
                insert.setInt(2, alertId);
                insert.executeUpdate();
            }

            return policyCondition.getId();
        } catch (Exception e) {
            logger.severe("Error updating PolicyCondition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Helper function to remove all rows from policy_condition_alert for the givein PolicyCondition id
     * @param policyConditionId
     * @return
     */
    private static boolean deletePolicyConditionAlertRows(int policyConditionId){
        try {
            PreparedStatement delete = dbConn.prepareStatement("DELETE FROM policy_condition_alert WHERE policy_cond_id = ?");
            delete.setInt(1, policyConditionId);
            delete.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.severe("Error deleting PolicyConditionAlert rows: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a row in the policy_condition table with the given id
     * @param id
     * @return
     */
    public static Boolean deletePolicyCondition(int id) {
        return deleteById("policy_condition", id);
    }

    /*
     *      Policy instance specific actions
     */

    /**
     * Finds a row in the policy_instance table with the given id
     * @param id
     * @return
     */
    public static PolicyInstance findPolicyInstance(int id) {
        return rsToPolicyInstance(findById(id, "policy_instance"));
    }

    /**
     * Converts a ResultSet from a query on policy instance to a java PolicyInstance
     * @param rs
     * @return
     */
    private static PolicyInstance rsToPolicyInstance(ResultSet rs) {
        PolicyInstance inst = null;
        try {
            int id = rs.getInt("id");
            int policyId = rs.getInt("policy_id");
            int deviceId = rs.getInt("device_id");
            Timestamp timestamp = rs.getTimestamp("timestamp");
            inst = new PolicyInstance(id, policyId, deviceId, timestamp);
        } catch (Exception e) {
            logger.severe("Error converting rs to PolicyInstance: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }

        return inst;
    }

    /**
     * Inserts a PolicyInstance to the policy_instance table
     * @param instance
     * @return
     */
    public static Integer insertPolicyInstance(PolicyInstance instance) {
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }

        try {
            PreparedStatement insert = dbConn.prepareStatement("INSERT INTO policy_instance(policy_id, device_id, timestamp) VALUES(?,?,?)");
            insert.setInt(1, instance.getPolicyId());
            insert.setInt(2, instance.getDeviceId());
            insert.setTimestamp(3, instance.getTimestamp());
            insert.executeUpdate();
            return getLatestId("policy_instance");
        } catch (Exception e) {
            logger.severe("Error inserting StateTransition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Deletes a row in the policy_instance table with the given id
     * @param id
     * @return
     */
    public static boolean deletePolicyInstance(int id) {
        return deleteById("policy_instance", id);
    }

    /*
     *      StateTransition specific actions
     */

    public static StateTransition findStateTransition(int id) {
        return rsToStateTransition(findById(id, "state_transition"));
    }

    /**
     * Converts a ResultSet obj to a Policy
     * @param rs Result set of a query to the policy table
     * @return The object representing the query result
     */
    public static StateTransition rsToStateTransition(ResultSet rs) {
        StateTransition stateTransition = null;
        try {
            int id = rs.getInt("id");
            int startSecStateId = rs.getInt("start_sec_state_id");
            int finishSecStateId = rs.getInt("finish_sec_state_id");
            stateTransition = new StateTransition(id, startSecStateId, finishSecStateId);
        } catch (Exception e) {
            logger.severe("Error converting rs to StateTransition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return stateTransition;
    }

    /**
     * Inserts the given StateTransition obj to the state_transition table
     * @param trans The obj to insert
     * @return Row's id on success. -1 otherwise
     */
    public static Integer insertStateTransition(StateTransition trans) {
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }

        try {
            PreparedStatement insert = dbConn.prepareStatement("INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) VALUES(?,?)");
            insert.setInt(1, trans.getStartStateId());
            insert.setInt(2, trans.getFinishStateId());
            insert.executeUpdate();
            return getLatestId("state_transition");
        } catch (Exception e) {
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
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }

        try {
            PreparedStatement update = dbConn.prepareStatement("UPDATE state_transition SET " +
                    "start_sec_state_id = ? " +
                    "finish_sec_state_id = ? " +
                    "WHERE id = ?");
            update.setInt(1, trans.getStartStateId());
            update.setInt(2, trans.getFinishStateId());
            update.setInt(3, trans.getId());
            update.executeUpdate();
            return trans.getId();
        } catch (Exception e) {
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

    /*
     *      DeviceSecurityState specific actions
     */

    /**
     * Finds the DeviceSecurityState with the supplied id
     *
     * @param the id for the DeviceSecurityState
     * @return the DeviceSecurityState, if it exists
     */
    public static DeviceSecurityState findDeviceSecurityState(int id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        DeviceSecurityState dss = null;

        logger.info("Finding device security state with id: " + id);
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }

        try {
            st = dbConn.prepareStatement("SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                    "FROM device_security_state dss, security_state ss " +
                    "WHERE dss.id=? AND dss.state_id = ss.id " +
                    "ORDER BY timestamp DESC " +
                    "LIMIT 1");
            st.setInt(1, id);
            rs = st.executeQuery();

            if (rs.next()) {
                dss = rsToDeviceSecurityState(rs);
            }
        } catch (SQLException e) {
            logger.severe("SQL exception getting the device state: " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting device state: " + e.getClass().getName() + ": " + e.getMessage());
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
        return dss;
    }

    public static List<DeviceSecurityState> findAllDeviceSecurityStates() {
        PreparedStatement st = null;
        ResultSet rs = null;
        List<DeviceSecurityState> stateList = new ArrayList<>();
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT dss.id, dss.device_id, dss.timestamp, dss.state_id, ss.name FROM device_security_state AS dss, security_state AS ss WHERE dss.state_id=ss.id");
            rs = st.executeQuery();
            while(rs.next()){
                stateList.add(rsToDeviceSecurityState(rs));
            }
        } catch (Exception e){
            logger.severe("Error while trying to find all DeviceSecurityStates: "+e.getMessage());
        }
        return stateList;
    }

    /**
     * Finds the most recent DeviceSecurityState from the database for the given device
     *
     * @param deviceId the id of the device
     * @return the most recent DeviceSecurityState entered for a device
     */
    public static DeviceSecurityState findDeviceSecurityStateByDevice(int deviceId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        DeviceSecurityState ss = new DeviceSecurityState();

        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }

        try {
            st = dbConn.prepareStatement("SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                    "FROM device_security_state dss, security_state ss " +
                    "WHERE dss.device_id=? AND dss.state_id = ss.id " +
                    "ORDER BY timestamp DESC " +
                    "LIMIT 1");
            st.setInt(1, deviceId);
            rs = st.executeQuery();

            if (!rs.next()) {
                return null;
            } else {
                ss = rsToDeviceSecurityState(rs);
            }

            while (rs.next()) {
                ss = rsToDeviceSecurityState(rs);
            }
        } catch (SQLException e) {
            logger.severe("SQL exception getting the device state: " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting device state: " + e.getClass().getName() + ": " + e.getMessage());
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
        return ss;
    }

    public static int findPreviousDeviceSecurityStateId(Device device) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if(dbConn == null)  {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }

        try {
            st = dbConn.prepareStatement("SELECT dss.state_id AS state_id " +
                    "FROM device_security_state dss " +
                    "WHERE dss.device_id=? AND dss.id < ? " +
                    "ORDER BY dss.id DESC " +
                    "LIMIT 1");
            st.setInt(1, device.getId());
            st.setInt(2, device.getCurrentState().getId());
            rs = st.executeQuery();

            if(rs.next()){
                int id = rs.getInt("state_id");
                return id;
            } else {
                logger.info("Only 1 device security state entered for device with id: "+device.getId());
                return -1;
            }

        } catch (Exception e) {
            logger.severe("Error finding previous device security state for device: "+device.getId());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Finds all DeviceSecurityState from the database for the given device.
     *
     * @param deviceId the id of the device.
     * @return a list of all DeviceSecurityState in the database where the device_id field is equal to deviceId.
     */
    public static List<DeviceSecurityState> findDeviceSecurityStates(int deviceId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        List<DeviceSecurityState> deviceStateList = new ArrayList<DeviceSecurityState>();

        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                    "FROM device_security_state dss, security_state ss " +
                    "WHERE dss.device_id=? AND dss.state_id = ss.id " +
                    "ORDER BY timestamp DESC");
            st.setInt(1, deviceId);
            rs = st.executeQuery();

            while (rs.next()) {
                deviceStateList.add(rsToDeviceSecurityState(rs));
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all device states: " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting device states: " + e.getClass().getName() + ": " + e.getMessage());
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
        return deviceStateList;
    }

    /**
     * Extract a DeviceSecurityState from the result set of a database query.
     *
     * @param rs ResultSet from a DeviceState query.
     * @return The DeviceSecurityState that was found.
     */
    private static DeviceSecurityState rsToDeviceSecurityState(ResultSet rs) {
        DeviceSecurityState deviceState = null;
        try {
            int id = rs.getInt("id");
            int deviceId = rs.getInt("device_id");
            int stateId = rs.getInt("state_id");
            Timestamp timestamp = rs.getTimestamp("timestamp");
            String name = rs.getString("name");
            deviceState = new DeviceSecurityState(id, deviceId, stateId, timestamp, name);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to DeviceSecurityState: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return deviceState;
    }

    /**
     * Saves given DeviceSecurityState to the database.
     *
     * @param deviceState DeviceSecurityState to be inserted.
     * @return The id of the new DeviceSecurityState if successful
     */
    public static Integer insertDeviceSecurityState(DeviceSecurityState deviceState) {
        logger.info("Inserting DeviceSecurityState");
        PreparedStatement insert = null;
        ResultSet rs = null;

        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {
            insert = dbConn.prepareStatement
                    ("INSERT INTO device_security_state(device_id, timestamp, state_id) " +
                            "values(?,?,?) " +
                            "RETURNING id");
            insert.setInt(1, deviceState.getDeviceId());
            insert.setTimestamp(2, deviceState.getTimestamp());
            insert.setInt(3, deviceState.getStateId());
            rs = insert.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error inserting DeviceState: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Deletes a DeviceSecurityState by its id.
     *
     * @param id id of the DeviceSecurityState to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteDeviceSecurityState(int id) {
        return deleteById("device_security_state", id);
    }

    /*
     *      SecurityState specific actions
     */

    /**
     * Search the security_state table for a row with the given id
     *
     * @param id The id of the security state
     * @return the row from the table
     */
    public static SecurityState findSecurityState(int id) {
        ResultSet rs = findById(id, "security_state");
        if (rs == null) {
            return null;
        } else {
            SecurityState state = rsToSecurityState(rs);
            return state;
        }
    }

    /**
     * Finds all SecurityStates in the database.
     *
     * @return a list of all SecurityStates in the database.
     */
    public static List<SecurityState> findAllSecurityStates() {
        ResultSet rs = getAllFromTable("security_state");
        List<SecurityState> states = new ArrayList<SecurityState>();
        try {
            while (rs.next()) {
                states.add(rsToSecurityState(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("SQLException getting all SecurityStates: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
            }
        }
        return states;
    }

    /**
     * Take a ResultSet from a DB query and convert to the java object
     *
     * @param rs
     * @return
     */
    private static SecurityState rsToSecurityState(ResultSet rs) {
        SecurityState state = null;
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            state = new SecurityState(id, name);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to SecurityState: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return state;
    }

    /**
     * Inserts the given SecurityState into the db
     *
     * @param the security state to enter
     * @return the id of the newly inserted SecurityState
     */
    public static Integer insertSecurityState(SecurityState state) {
        logger.info("Inserting SecurityState");
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {
            PreparedStatement update = dbConn.prepareStatement
                    ("INSERT INTO security_state(name)" +
                            "values(?)");
            update.setString(1, state.getName());
            update.executeUpdate();
            return getLatestId("security_state");
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error inserting SecurityState: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Update row in security_state corresponding to the parameter
     *
     * @param state The security state to update
     * @return The id of the updated row
     */
    public static Integer updateSecurityState(SecurityState state) {
        logger.info("Updating SecurityState with id=" + state.getId());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {
            PreparedStatement update = dbConn.prepareStatement
                    ("UPDATE security_state SET name = ?" +
                            "WHERE id=?");
            update.setString(1, state.getName());
            update.setInt(2, state.getId());
            update.executeUpdate();
            return state.getId();
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error updating Security: " + e.getClass().getName() + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * First, attempts to find the SecurityState in the database.
     * If successful, updates the existing SecurityState with the given SecurityState's parameters Otherwise,
     * inserts the given SecurityState.
     *
     * @param state SecurityState to be inserted or updated.
     */
    public static Integer insertOrUpdateSecurityState(SecurityState state) {
        SecurityState ss = findSecurityState(state.getId());
        if (ss == null) {
            return insertSecurityState(state);
        } else {
            return updateSecurityState(state);
        }
    }

    /**
     * Delete row from security_state with the given id
     *
     * @param id The id of the row to delete
     * @return True if successful
     */
    public static Boolean deleteSecurityState(int id) {
        return deleteById("security_state", id);
    }

    /*
     *      Tag specific actions
     */

    /**
     * Search the tag table for a row with the given id
     *
     * @param id The id of the tag
     * @return the row from the table
     */
    public static Tag findTag(int id) {
        ResultSet rs = findById(id, "tag");
        if (rs == null) {
            return null;
        } else {
            Tag tag = rsToTag(rs);
            return tag;
        }
    }

    /**
     * Find the respective tags for given device id
     *
     * @param deviceId The device id the tags are for
     * @return A list of tags or null
     */
    public static List<Tag> findTagsByDevice(int deviceId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = dbConn.prepareStatement("SELECT tag.* FROM tag, device_tag " +
                    "WHERE tag.id = device_tag.tag_id AND device_tag.device_id = ?");
            st.setInt(1, deviceId);
            rs = st.executeQuery();

            List<Tag> tags = new ArrayList<Tag>();
            while (rs.next()) {
                tags.add(rsToTag(rs));
            }
            return tags;
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error finding tags by device_id: " + deviceId + ": " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
            }
            try {
                if (st != null) st.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * Find the respective tag ids for given device id
     *
     * @param deviceId The device id the tags are for
     * @return A list of tag ids or null
     */
    private static List<Integer> findTagIds(int deviceId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = dbConn.prepareStatement("SELECT * FROM device_tag WHERE device_id = ?");
            st.setInt(1, deviceId);
            rs = st.executeQuery();

            List<Integer> tagIds = new ArrayList<Integer>();
            while (rs.next()) {
                tagIds.add(rs.getInt(2));
            }
            return tagIds;
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error finding tags by device_id: " + deviceId + ": " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
            }
            try {
                if (st != null) st.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * Finds all Tags in the database.
     *
     * @return a list of all Tags in the database.
     */
    public static List<Tag> findAllTags() {
        ResultSet rs = getAllFromTable("tag");
        List<Tag> tags = new ArrayList<Tag>();
        try {
            while (rs.next()) {
                tags.add(rsToTag(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("SQLException getting all Tags: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
            }
        }
        return tags;
    }

    /**
     * Extract a Tag from the result set of a database query.
     *
     * @param rs ResultSet from a Tag query.
     * @return The first Tag in rs.
     */
    private static Tag rsToTag(ResultSet rs) {
        Tag tag = null;
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            tag = new Tag(id, name);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to Tag: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return tag;
    }

    /**
     * Saves given Tag to the database.
     *
     * @param tag Tag to be inserted.
     * @return auto incremented id
     */
    public static Integer insertTag(Tag tag) {
        logger.info("Inserting Tag: " + tag.getId());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {
            PreparedStatement update = dbConn.prepareStatement
                    ("INSERT INTO tag(name)" +
                            "values(?)");
            update.setString(1, tag.getName());
            update.executeUpdate();
            return getLatestId("tag");
        } catch (Exception e) {
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
        PreparedStatement st = null;
        try {
            //remove references to tag from device
            st = dbConn.prepareStatement("DELETE FROM device_tag WHERE tag_id = ?");
            st.setInt(1, id);
            st.executeUpdate();
            //remove the tag itself
            deleteById("tag", id);
            return true;
        } catch (SQLException e) {
        } finally {
            try {
                if (st != null) st.close();
            } catch (Exception e) {
            }
        }
        return false;
    }

    /*
     *      DeviceType specific actions
     */

    /**
     * Finds a DeviceType from the database by its id.
     *
     * @param id id of the DeviceType to find.
     * @return the DeviceType if it exists in the database, else null.
     */
    public static DeviceType findDeviceType(int id) {
        ResultSet rs = findById(id, "device_type");
        if (rs == null) {
            return null;
        } else {
            DeviceType type = rsToDeviceType(rs);
            return type;
        }
    }

    /**
     * Finds all DeviceTypes in the database.
     *
     * @return a list of all DeviceTypes in the database.
     */
    public static List<DeviceType> findAllDeviceTypes() {
        ResultSet rs = getAllFromTable("device_type");
        List<DeviceType> types = new ArrayList<DeviceType>();
        try {
            while (rs.next()) {
                types.add(rsToDeviceType(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("SQLException getting all DeviceTypes: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
            }
        }
        return types;
    }

    /**
     * Extract a DeviceType from the result set of a database query.
     *
     * @param rs ResultSet from a DeviceType query.
     * @return The first DeviceType in rs.
     */
    private static DeviceType rsToDeviceType(ResultSet rs) {
        DeviceType type = null;
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            byte[] policyFile = rs.getBytes("policy_file");
            String policyFileName = rs.getString("policy_file_name");
            type = new DeviceType(id, name, policyFile, policyFileName);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to DeviceType: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return type;
    }

    /**
     * Saves given DeviceType to the database.
     *
     * @param type DeviceType to be inserted.
     * @return auto incremented id
     */
    public static Integer insertDeviceType(DeviceType type) {
        logger.info("Inserting DeviceType: " + type.getId());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {
            PreparedStatement update = dbConn.prepareStatement
                    ("INSERT INTO device_type(name, policy_file, policy_file_name)" +
                            "values(?,?,?)");
            update.setString(1, type.getName());
            update.setBytes(2, type.getPolicyFile());
            update.setString(3, type.getPolicyFileName());
            update.executeUpdate();
            return getLatestId("device_type");
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error inserting DeviceType: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates DeviceType with given id to have the parameters of the given DeviceType.
     *
     * @param type DeviceType holding new parameters to be saved in the database.
     */
    public static Integer updateDeviceType(DeviceType type) {
        logger.info("Updating DeviceType with id=" + type.getId());
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {
            PreparedStatement update = dbConn.prepareStatement
                    ("UPDATE device_type SET name = ?, policy_file = ?, policy_file_name = ?" +
                            "WHERE id=?");
            update.setString(1, type.getName());
            update.setBytes(2, type.getPolicyFile());
            update.setString(3, type.getPolicyFileName());
            update.setInt(4, type.getId());
            update.executeUpdate();
            return type.getId();
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error updating DeviceType: " + e.getClass().getName() + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * First, attempts to find the DeviceType in the database.
     * If successful, updates the existing DeviceType with the given DeviceType's parameters Otherwise,
     * inserts the given DeviceType.
     *
     * @param type DeviceType to be inserted or updated.
     */
    public static Integer insertOrUpdateDeviceType(DeviceType type) {
        DeviceType dt = findDeviceType(type.getId());
        if (dt == null) {
            return insertDeviceType(type);
        } else {
            return updateDeviceType(type);
        }
    }

    /**
     * Deletes a DeviceType by its id.
     *
     * @param id id of the DeviceType to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteDeviceType(int id) {
        return deleteById("device_type", id);
    }

    /*
     *      UmboxImage specific actions
     */

    /**
     * Find a UmboxImage based on its id
     *
     * @param id ID of the desired UmboxImage
     * @return The desired UmboxImage on success or null on failure
     */
    public static UmboxImage findUmboxImage(int id) {
        ResultSet rs = findById(id, "umbox_image");
        if (rs == null) {
            return null;
        } else {
            return rsToUmboxImageNoDagOrder(rs);
        }
    }

    /**
     * Finds the UmboxImages relating to the device type and the security state
     *
     * @param the id of the device type
     * @param the id of the security state
     * @return A list of UmboxImages for the given device type id and state id
     */
    public static List<UmboxImage> findUmboxImagesByDeviceTypeAndSecState(int devTypeId, int secStateId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT ui.id, ui.name, ui.file_name, ul.dag_order " +
                    "FROM umbox_image ui, umbox_lookup ul " +
                    "WHERE ul.device_type_id = ? AND ul.state_id = ? AND ul.umbox_image_id = ui.id");
            st.setInt(1, devTypeId);
            st.setInt(2, secStateId);
            rs = st.executeQuery();
            List<UmboxImage> umboxImageList = new ArrayList<UmboxImage>();
            while (rs.next()) {
                umboxImageList.add(rsToUmboxImage(rs));
            }
            return umboxImageList;
        } catch (SQLException e) {
            logger.severe("Sql exception getting all UmboxImages: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting UmboxImages: " + e.getClass().getName() + ": " + e.getMessage());
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
        return null;
    }

    /**
     * Finds all UmboxImages in the database.
     *
     * @return a list of all UmboxImages in the database.
     */
    public static List<UmboxImage> findAllUmboxImages() {
        logger.info("Getting umbox images.");
        ResultSet rs = getAllFromTable("umbox_image");
        List<UmboxImage> umboxImages = new ArrayList<UmboxImage>();
        try {
            while (rs.next()) {
                umboxImages.add(rsToUmboxImageNoDagOrder(rs));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Sql exception getting all umbox images: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return umboxImages;
    }

    /**
     * Extract a UmboxImage from the result set of a database query that DOES NOT include umbox_lookup.
     *
     * @param rs ResultSet from a UmboxImage query.
     * @return The first UmboxImage in rs.
     */
    private static UmboxImage rsToUmboxImageNoDagOrder(ResultSet rs) {
        UmboxImage umboxImage = null;
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String fileName = rs.getString("file_name");
            umboxImage = new UmboxImage(id, name, fileName);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to UmboxImage: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return umboxImage;
    }

    /**
     * Extract a UmboxImage from the result set of a database query that includes umbox_lookup.
     *
     * @param rs ResultSet from a UmboxImage query.
     * @return The first UmboxImage in rs.
     */
    private static UmboxImage rsToUmboxImage(ResultSet rs) {
        UmboxImage umboxImage = null;
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String fileName = rs.getString("file_name");
            int dagOrder = rs.getInt("dag_order");
            umboxImage = new UmboxImage(id, name, fileName, dagOrder);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to UmboxImage: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return umboxImage;
    }

    /**
     * Inserts given UmboxImage into the database
     *
     * @param u the UmboxImage to be inserted
     * @return The id of the inserted UmboxImage on success or -1 on failure
     */
    public static Integer insertUmboxImage(UmboxImage u) {
        logger.info("Adding umbox image: " + u);
        PreparedStatement st = null;
        try {
            st = dbConn.prepareStatement("INSERT INTO umbox_image (name, file_name) VALUES (?, ?)");
            st.setString(1, u.getName());
            st.setString(2, u.getFileName());
            st.executeUpdate();
            return getLatestId("umbox_image");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("SQL exception adding umbox iamge: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            try {
                if (st != null) st.close();
            } catch (Exception e) {
            }
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
        PreparedStatement st = null;
        try {
            st = dbConn.prepareStatement("UPDATE umbox_image " +
                    "SET name = ?, file_name = ? " +
                    "WHERE id = ?");
            st.setString(1, u.getName());
            st.setString(2, u.getFileName());
            st.setInt(3, u.getId());
            st.executeUpdate();
            return u.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("SQL exception editing umbox iamge: " + e.getClass().getName() + ": " + e.getMessage());
        } catch (NumberFormatException e) {
        } finally {
            try {
                if (st != null) st.close();
            } catch (Exception e) {
            }
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

    /*
     *      UmboxInstance specific actions
     */

    /**
     * Find a umbox instance by its alerter id
     *
     * @param alerterId The ID of desired UmboxInstance
     * @return The desired UmboxInstance on success or null on failure
     */
    public static UmboxInstance findUmboxInstance(String alerterId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement(String.format("SELECT * FROM umbox_instance WHERE alerter_id = ?"));
            st.setString(1, alerterId);
            rs = st.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return rsToUmboxInstance(rs);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Exception finding by ID: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
            }
            try {
                if (st != null) st.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * Finds all UmboxInstances from the database for the given device.
     *
     * @param deviceId the id of the device.
     * @return a list of all UmboxInstaces in the database where the device_id field is equal to deviceId.
     */
    public static List<UmboxInstance> findUmboxInstances(int deviceId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT * FROM umbox_instance WHERE device_id = ?");
            st.setInt(1, deviceId);
            rs = st.executeQuery();
            List<UmboxInstance> umboxInstances = new ArrayList<UmboxInstance>();
            while (rs.next()) {
                umboxInstances.add(rsToUmboxInstance(rs));
            }
            return umboxInstances;
        } catch (SQLException e) {
            logger.severe("Sql exception getting all UmboxInstances: " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting UmboxInstances: " + e.getClass().getName() + ": " + e.getMessage());
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
        return null;
    }

    /**
     * Extract a UmboxInstance from the result set of a database query.
     *
     * @param rs ResultSet from a UmboxInstance query.
     * @return The UmboxInstance that was found.
     */
    private static UmboxInstance rsToUmboxInstance(ResultSet rs) {
        UmboxInstance umboxInstance = null;
        try {
            int id = rs.getInt("id");
            String alerterId = rs.getString("alerter_id");
            int imageId = rs.getInt("umbox_image_id");
            int deviceId = rs.getInt("device_id");
            Timestamp startedAt = rs.getTimestamp("started_at");
            umboxInstance = new UmboxInstance(id, alerterId, imageId, deviceId, startedAt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to UmboxInstance: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return umboxInstance;
    }

    /**
     * Adds the desired UmboxInstance to the database
     *
     * @param u UmboxInstance to add
     * @return
     */
    public static Integer insertUmboxInstance(UmboxInstance u) {
        logger.info("Adding umbox instance: " + u);
        PreparedStatement st = null;
        try {
            st = dbConn.prepareStatement("INSERT INTO umbox_instance (alerter_id, umbox_image_id, device_id, started_at) VALUES (?,?,?,?)");
            st.setString(1, u.getAlerterId());
            st.setInt(2, u.getUmboxImageId());
            st.setInt(3, u.getDeviceId());
            st.setTimestamp(4, u.getStartedAt());
            st.executeUpdate();
            return getLatestId("umbox_instance");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("SQL exception adding umbox instance: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Edit desired UmboxInstance
     *
     * @param u The instance to be updated
     * @return
     */
    public static Integer updateUmboxInstance(UmboxInstance u) {
        logger.info("Editing umbox intance: " + u);
        PreparedStatement st = null;
        try {
            st = dbConn.prepareStatement("UPDATE umbox_instance " +
                    "SET alerter_id = ?, umbox_image_id = ?, device_id = ?, started_at = ?" +
                    "WHERE id = ?");
            st.setString(1, u.getAlerterId());
            st.setInt(2, u.getUmboxImageId());
            st.setInt(3, u.getDeviceId());
            st.setTimestamp(4, u.getStartedAt());
            st.setInt(5, u.getId());
            st.executeUpdate();
            return u.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("SQL exception editing umbox iamge: " + e.getClass().getName() + ": " + e.getMessage());
        } catch (NumberFormatException e) {
        } finally {
            try {
                if (st != null) st.close();
            } catch (Exception e) {
            }
        }
        return -1;
    }

    /**
     * Deletes a UmboxInstance by its id.
     *
     * @param id id of the UmboxInstance to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteUmboxInstance(int id) {
        return deleteById("umbox_instance", id);
    }

    /*
     * UmboxLookup functions
     */

    /**
     * Finds a UmboxLookup from the database by its id.
     *
     * @param id id of the UmboxLookup to find.
     * @return the UmboxLookup if it exists in the database, else null.
     */
    public static UmboxLookup findUmboxLookup(int id) {
        logger.info("Finding umboxLookup with id = " + id);
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT * FROM umbox_lookup WHERE id = ?");
            st.setInt(1, id);
            rs = st.executeQuery();
            if (!rs.next()) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Exception finding umbox lookup by ID: " + e.getClass().getName() + ": " + e.getMessage());
        }

        return rsToUmboxLookup(rs);
    }

    /**
     * Finds all umbox lookups based on the given device id
     */
    public static List<UmboxLookup> findUmboxLookupsByDevice(int deviceId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        List<UmboxLookup> lookupList = new ArrayList<UmboxLookup>();
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT ul.* FROM umbox_lookup ul, device d, policy p " +
                    "WHERE ul.policy_id = p.id AND p.device_type_id = d.type_id AND d.id = ?;");
            st.setInt(1, deviceId);
            rs = st.executeQuery();
            while (rs.next()) {
                lookupList.add(rsToUmboxLookup(rs));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Exception finding umbox lookup: " + e.getClass().getName() + ": " + e.getMessage());
        }

        return lookupList;
    }


    /**
     * Finds all umboxLookup entries
     */
    public static List<UmboxLookup> findAllUmboxLookups() {
        ResultSet rs = getAllFromTable("umbox_lookup");
        List<UmboxLookup> umboxLookups = new ArrayList<UmboxLookup>();
        try {
            while (rs.next()) {
                umboxLookups.add(rsToUmboxLookup(rs));
            }
            rs.close();
        } catch (SQLException e) {
            logger.severe("Sql exception getting all Umbox Lookups.");
        }
        return umboxLookups;
    }

    /**
     * Extract a UmboxLookup from the result set of a database query.
     */
    private static UmboxLookup rsToUmboxLookup(ResultSet rs) {
        UmboxLookup umboxLookup = null;
        Integer id = null;
        Integer policyId = null;
        Integer deviceTypeId = null;
        Integer umboxImageId = null;
        Integer dagOrder = null;
        try {
            id = rs.getInt("id");
            policyId = rs.getInt("policy_id");
            umboxImageId = rs.getInt("umbox_image_id");
            dagOrder = rs.getInt("dag_order");
            umboxLookup = new UmboxLookup(id, policyId, umboxImageId, dagOrder);
            return umboxLookup;
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to UmboxLookup: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Adds the desired UmboxLookup to the database
     */
    public static Integer insertUmboxLookup(UmboxLookup ul) {
        logger.info("Adding umbox lookup: ");
        PreparedStatement st = null;
        try {
            st = dbConn.prepareStatement("INSERT INTO umbox_lookup (policy_id, umbox_image_id, dag_order) VALUES (?,?,?)");
            st.setInt(1, ul.getPolicyId());
            st.setInt(2, ul.getUmboxImageId());
            st.setInt(3, ul.getDagOrder());
            st.executeUpdate();
            return getLatestId("umbox_lookup");
        }
        catch (SQLException e){
            e.printStackTrace();
            logger.severe("SQL exception adding umbox lookup: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            try {
                if (st != null) st.close();
            } catch (Exception e) {
            }
        }
        return -1;
    }

    /**
     * Edit desired UmboxLookup
     */
    public static Integer updateUmboxLookup(UmboxLookup ul) {
        logger.info(String.format("Updating UmboxLookup with id = %d with values: %s", ul.getId(), ul));
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
        } else {
            try {
                PreparedStatement update = dbConn.prepareStatement("UPDATE umbox_lookup " +
                        "SET policy_id = ?, umbox_image_id = ?, dag_order = ?" +
                        "WHERE id = ?");
                update.setInt(1, ul.getPolicyId());
                update.setInt(2, ul.getUmboxImageId());
                update.setInt(3, ul.getDagOrder());
                update.setInt(4, ul.getId());
                update.executeUpdate();

                return ul.getId();
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error updating UmboxLookup: " + e.getClass().toString() + ": " + e.getMessage());
            }
        }
        return -1;
    }

    /**
     * First, attempts to find the UmboxLookup in the database.
     * If successful, updates the existing UmboxLookup with the given parameters Otherwise,
     * inserts the given UmboxLookup.
     *
     * @param ul UmboxLookup to be inserted or updated.
     */
    public static Integer insertOrUpdateUmboxLookup(UmboxLookup ul) {
        UmboxLookup foundUl = findUmboxLookup(ul.getId());

        if (foundUl == null) {
            return insertUmboxLookup(ul);
        } else {
            return updateUmboxLookup(ul);
        }
    }

    /**
     * Deletes a UmboxLookup by its id.
     */
    public static Boolean deleteUmboxLookup(int id) {
        return deleteById("umbox_lookup", id);
    }

    /*
        UmboxLog specific actions
     */

    /**
     * Finds the row in umbox_log table with given id
     * @param id
     * @return UmboxLog object representing row; Null if an exception is thrown
     */
    public static UmboxLog findUmboxLog(int id){
        logger.info("Finding UmboxLog with id = "+id);
        ResultSet rs = findById(id, "umbox_log");
        return rsToUmboxLog(rs);
    }

    /**
     * Returns all rows from the umbox_log table
     * @return List of UmboxLogs in the umbox_log table
     */
    public static List<UmboxLog> findAllUmboxLogs() {
        logger.info("Finding all UmboxLogs");
        PreparedStatement st = null;
        ResultSet rs = null;
        List<UmboxLog> umboxLogList = new ArrayList<>();
        if(dbConn == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT * FROM umbox_log");
            rs = st.executeQuery();
            while(rs.next()){
                umboxLogList.add(rsToUmboxLog(rs));
            }
        } catch (Exception e) {
            logger.severe("Exception finding all UmboxLogs "+e.getClass().getName() + ": "+e.getMessage());
            e.printStackTrace();
        }
        return umboxLogList;
    }

    /**
     * Finds rows in the umbox_log table with the given alerter_id
     * @param alerter_id
     * @return List of UmboxLogs with given alerter_id
     *
     */
    public static List<UmboxLog> findAllUmboxLogsForAlerterId(String alerter_id) {
        logger.info("Finding UmboxLogs with alerter_id: "+alerter_id);
        PreparedStatement st = null;
        ResultSet rs = null;
        List<UmboxLog> umboxLogList = new ArrayList<>();
        if(dbConn == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT * FROM umbox_log WHERE alerter_id = ?");
            st.setString(1, alerter_id);
            rs = st.executeQuery();
            while(rs.next()){
                umboxLogList.add(rsToUmboxLog(rs));
            }
        } catch (Exception e) {
            logger.severe("Exception finding UmboxLogs for alerter_id="+alerter_id+"; "+e.getClass().getName() + ": "+e.getMessage());
            e.printStackTrace();
        }
        return umboxLogList;
    }

    public static List<UmboxLog> findAllUmboxLogsForDevice(int deviceId) {
        logger.info("Finding UmboxLogs for device: "+deviceId);
        PreparedStatement st = null;
        ResultSet rs = null;
        List<UmboxLog> logList = new ArrayList<>();
        if(dbConn == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT log.* FROM umbox_log AS log, umbox_instance AS inst WHERE " +
                    "inst.device_id = ? AND inst.alerter_id = log.alerter_id " +
                    "ORDER BY log.id DESC");
            st.setInt(1, deviceId);
            rs = st.executeQuery();
            while(rs.next()){
                logList.add(rsToUmboxLog(rs));
            }
        } catch (Exception e) {
            logger.severe("Exception finding UmboxLogs for device: "+deviceId+"; "+e.getMessage());
            e.printStackTrace();
        }
        return logList;
    }

    /**
     * Converts a row from the umbox_log table to a UmboxLog object
     * @param rs The ResultSet representing the row in the table
     * @return the UmboxLog ojbect representation of the row; Null if an exception is thrown
     */
    public static UmboxLog rsToUmboxLog(ResultSet rs){
        int id = -1;
        String alerterId = "";
        String details = "";
        Timestamp timestamp = null;
        try {
            id = rs.getInt("id");
            alerterId = rs.getString("alerter_id");
            details = rs.getString("details");
            timestamp = rs.getTimestamp("timestamp");
            return new UmboxLog(id, alerterId, details, timestamp);
        } catch (Exception e){
            logger.severe("Error converting ResultSet to UmboxLog: "+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Inserts the given UmboxLog into the umbox_log table
     * @param umboxLog
     * @return the id of the inserted row
     */
    public static int insertUmboxLog(UmboxLog umboxLog){
        logger.info("Inserting new UmboxLog: "+umboxLog.toString());
        PreparedStatement st = null;
        ResultSet rs = null;
        int latestId = -1;
        try{
            st = dbConn.prepareStatement("INSERT INTO umbox_log (alerter_id, details) VALUES(?,?)");
            st.setString(1, umboxLog.getAlerterId());
            st.setString(2, umboxLog.getDetails());
            st.executeUpdate();
            latestId = getLatestId("umbox_log");
        } catch (Exception e){
            logger.severe("Error insert UmboxLog into db: "+e.getClass().getName()+": "+e.getMessage());
            e.printStackTrace();
        }
        return latestId;
    }
    /*
        StageLog specific actions
     */

    /**
     * Finds the row in stage_log for the given id
     * @param id
     * @return StageLog representing row with given id
     */
    public static StageLog findStageLog(int id) {
        logger.info("Finding StageLog with id = " + id);
        ResultSet rs = findById(id, "stage_log");
        return rsToStageLog(rs);
    }

    /**
     * Returns all rows in the stage_log table
     * @return a List of all StageLogs
     */
    public static List<StageLog> findAllStageLogs(){
        logger.info("Finding all StageLogs");
        PreparedStatement st = null;
        ResultSet rs = null;
        List<StageLog> stageLogList = new ArrayList<>();
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT * FROM stage_log ORDER BY timestamp ASC");
            rs = st.executeQuery();
            while (rs.next()) {
                stageLogList.add(rsToStageLog(rs));
            }
        } catch (Exception e) {
            logger.severe("Exception finding all StageLogs " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return stageLogList;
    }

    /**
     * Returns all rows in the stage_log related to the given device
     * @param deviceId
     * @return a List of StageLogs related to the given device id
     */
    public static List<StageLog> findAllStageLogsForDevice(int deviceId) {
        logger.info("Finding all StageLogs for device with id: "+deviceId);
        PreparedStatement st = null;
        ResultSet rs = null;
        List<StageLog> stageLogList = new ArrayList<>();
        if(dbConn == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT sl.id, sl.device_sec_state_id, sl.timestamp, sl.action, sl.stage, sl.info " +
                    "FROM stage_log sl, device_security_state dss " +
                    "WHERE dss.device_id=? AND sl.device_sec_state_id=dss.id");
            st.setInt(1, deviceId);
            rs = st.executeQuery();
            while(rs.next()){
                stageLogList.add(rsToStageLog(rs));
            }
        } catch (Exception e){
            logger.severe("Exception finding all StageLogs for device "+deviceId+" "+e.getClass().getName()+": "+e.getMessage());
            e.printStackTrace();
        }
        return stageLogList;
    }

    public static List<String> findStageLogActions(){
        PreparedStatement st = null;
        ResultSet rs = null;
        List<String> actions = new ArrayList<>();
        if(dbConn == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
        }
        try {
            st = dbConn.prepareStatement("SELECT action FROM stage_log WHERE stage=?");
            st.setString(1, StageLog.Stage.FINISH.convert());
            rs = st.executeQuery();
            while(rs.next()){
                actions.add(rs.getString("action"));
            }
        } catch (Exception e) {
            logger.severe("Error getting all actions that finished in stage_log: "+e.getMessage());
        }
        return actions;

    }

//    public static int findTimeBetweenStages(String stageOne, String stageTwo) {
//        PreparedStatement st = null;
//        ResultSet rs = null;
//        List<String> actions = new ArrayList<>();
//        if(dbConn == null){
//            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
//        }
//        try {
//            st = dbConn.prepareStatement("SELECT action FROM stage_log WHERE stage=?");
//            st.setString(1, StageLog.Stage.FINISH.convert());
//            rs = st.executeQuery();
//            while(rs.next()){
//                actions.add(rs.getString("action"));
//            }
//        } catch (Exception e) {
//            logger.severe("Error getting all actions that finished in stage_log: "+e.getMessage());
//        }
//        return actions;
//    }

    /**
     * Converts a result set to a StageLog object
     * @param rs
     * @return
     */
    public static StageLog rsToStageLog(ResultSet rs) {
        int id = -1;
        int deviceSecurityStateId = -1;
        Timestamp timestamp = null;
        String action = "";
        String stage = "";
        String info = "";
        try {
            id = rs.getInt("id");
            deviceSecurityStateId = rs.getInt("device_sec_state_id");
            timestamp = rs.getTimestamp("timestamp");
            action = rs.getString("action");
            stage = rs.getString("stage");
            info = rs.getString("info");

            return new StageLog(id, deviceSecurityStateId, timestamp, action, stage, info);
        } catch (Exception e){
            logger.severe("Error converting ResultSet to StageLog: "+e.getMessage());
            return null;
        }
    }

    /**
     * Inserts the given StageLog into the stage_log table
     * @param stageLog
     * @return
     */
    public static int insertStageLog(StageLog stageLog){
        logger.info("Inserting new stage log: "+stageLog.toString());
        PreparedStatement st = null;
        ResultSet rs = null;
        int latestId = -1;
        long timestamp = System.currentTimeMillis();
        stageLog.setTimestamp(new Timestamp(timestamp));
        try{
            st = dbConn.prepareStatement("INSERT INTO stage_log (device_sec_state_id, action, stage, info, timestamp) VALUES(?,?,?,?,?)");
            st.setInt(1, stageLog.getDeviceSecurityStateId());
            st.setString(2, stageLog.getAction());
            st.setString(3, stageLog.getStage());
            st.setString(4, stageLog.getInfo());
            st.setTimestamp(5, stageLog.getTimestamp());

            st.executeUpdate();

            latestId = getLatestId("stage_log");
        } catch (Exception e){
            logger.severe("Error insert StageLog into db: "+e.getClass().getName()+": "+e.getMessage());
            e.printStackTrace();
        }
        return latestId;
    }

    /*
        Methods used for giving the dashboard new updates
    */
    private static List<Integer> newStateIds = Collections.synchronizedList(new ArrayList<Integer>());
    private static List<Integer> newAlertIds = Collections.synchronizedList(new ArrayList<Integer>());
    private static List<Integer> newStatusIds = Collections.synchronizedList(new ArrayList<Integer>());

    /**
     * Start up a notification listener.  This will clear all current handlers and
     * current list of newIds
     */
    public static void startListener() {
        InsertListener.startListening();
        InsertListener.clearHandlers();

        InsertListener.addHandler("alerthistoryinsert", new AlertHandler());
        InsertListener.addHandler("devicesecuritystateinsert", new StateHandler());
        InsertListener.addHandler("devicestatusinsert", new StatusHandler());

        newAlertIds.clear();
        newStateIds.clear();
        newStatusIds.clear();
    }

    public static void stopListener() {
        InsertListener.stopListening();
    }

    /**
     * adds a given alert id to the list of new alert ids to be given to the dashboard
     * @param newId
     */
    public static void newAlertId(int newId) {
        newAlertIds.add(newId);
    }

    /**
     * adds a given state id to the list of new state ids to be given to the dashboard
     * @param newId
     */
    public static void newStateId(int newId) {
        newStateIds.add(newId);
    }

    /**
     * adds a given status id to the list of new status ids to be given to the dashboard
     * @param newId
     */
    public static void newStatusId(int newId) {
        newStatusIds.add(newId);
    }

    /**
     * return the latest alerts unless there are no alerts and then returns null
     *
     * @return the next new alert to be given to the dashboard in the queue
     */
    public static List<Alert> getNewAlerts() {
        if(newAlertIds.size() != 0) {
            List<Alert> newAlerts = new ArrayList<>();
            synchronized (newAlertIds) {
                for(int alertId: newAlertIds) {
                    Alert newAlert = Postgres.findAlert(alertId);
                    newAlerts.add(newAlert);
                }
                newAlertIds.clear();
            }

            return newAlerts;
        } else {
            return null;
        }
    }

    /**
     * return the latest states unless there are no states and then returns null
     *
     * @return the next new device security state to be given to the dashboard in the queue
     */
    public static List<DeviceSecurityState> getNewStates() {
        if(newStateIds.size() != 0) {
            List<DeviceSecurityState> newStates = new ArrayList<>();
            synchronized (newStateIds) {
                for(int stateId: newStateIds) {
                    DeviceSecurityState newState = Postgres.findDeviceSecurityState(stateId);
                    newStates.add(newState);
                }
                newStateIds.clear();
            }

            return newStates;
        } else {
            return null;
        }
    }

    /**
     * return the latest statuses unless there are no statuses and then returns null
     *
     * @return the next new device security state to be given to the dashboard in the queue
     */
    public static List<DeviceStatus> getNewStatuses() {
        if(newStatusIds.size() != 0) {
            List<DeviceStatus> newStatuses = new ArrayList<>();
            synchronized (newStatusIds) {
                for(int statusId: newStatusIds) {
                    DeviceStatus newStatus = Postgres.findDeviceStatus(statusId);
                    newStatuses.add(newStatus);
                }
                newStatusIds.clear();
            }

            return newStatuses;
        } else {
            return null;
        }
    }

    /***
     * Executes SQL from the given file.
     */
    public static void executeSQLFile(String fileName)
    {
        System.out.println("Reading from file: "+fileName);
        try {
            InputStream is = new FileInputStream(fileName);
            Scanner s = new Scanner(is);

            String line, statement="";
            while(s.hasNextLine()){
                line = s.nextLine();
                if(line.equals("") || line.equals(" ")) {
                    Postgres.executeCommand(statement);
                    statement="";
                } else {
                    statement += line;
                }
            }
            if (!statement.equals(""))
                Postgres.executeCommand(statement);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
