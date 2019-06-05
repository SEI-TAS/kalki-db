package edu.cmu.sei.ttg.kalki.database;

import edu.cmu.sei.ttg.kalki.models.*;
import org.postgresql.util.HStoreConverter;

import java.sql.*;
import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Postgres {
    private static final String DEFAULT_IP = "localhost";
    private static final String DEFAULT_PORT = "5432";

    private static final String ROOT_USER = "postgres";
    private static final String BASE_DB = "postgres";
    private static final String DEFAULT_DB_URL = "jdbc:postgresql://" + DEFAULT_IP + ":" + DEFAULT_PORT;

    public static final String TRIGGER_NOTIF_NEW_DEV_SEC_STATE = "devicesecuritystateinsert";

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
        // DB Structure
        initDB("db-tables.sql");
        initDB("db-triggers.sql");

        // DB Initial Data
        initDB("db-alert-types.sql");
        initDB("db-device-types.sql");
        initDB("db-security-states.sql");
        initDB("db-command-lookups.sql");
        initDB("db-umbox-images.sql");
        initDB("db-alert-type-lookups.sql");

        //TODO:
        //initDB("db-umbox-images.sql");
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
        tableNames.add("alert_type_lookup");
        tableNames.add("alert_condition");
        tableNames.add("alert");
        tableNames.add("umbox_lookup");
        tableNames.add("umbox_instance");
        tableNames.add("command_lookup");
        tableNames.add("device_tag");
        tableNames.add("device_security_state");
        tableNames.add("device_status");
        tableNames.add("device");
        tableNames.add("umbox_image");
        tableNames.add("tag");
        tableNames.add("security_state");
        tableNames.add("device_group");
        tableNames.add("device_type");
        tableNames.add("alert_type");

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
     * Reads from the specified file to initialize db
     * @param fileName the file containing SQL commands to execute
     */
    public static void initDB(String fileName){
//        logger.info("\n\n"+fileName+"\n\n");
        try {
            InputStream is = Postgres.class.getResourceAsStream("/"+fileName);
            Scanner s = new Scanner(is);

            String line, statement = "";
            while(s.hasNextLine()){
                line = s.nextLine();
                if(line.equals("") || line.equals(" ")){ // statements are white space delimited
                    executeCommand(statement);
                    statement = "";
                } else {
                    statement += line;
                }
            }
            if(!statement.equals(""))
                executeCommand(statement);
        } catch (Exception e) {
            logger.severe("Error initializing db:");
            e.printStackTrace();
        }

    }

    /**
     * Insert the default types into the database.
     */
    public static void insertDefaultDeviceTypes() {
        logger.info("Inserting default types.");
        List<String> typeNames = new ArrayList<String>();
        typeNames.add("Hue Light");
        typeNames.add("Dlink Camera");
        typeNames.add("WeMo Insight");
        typeNames.add("Udoo Neo");
        for(String typeName: typeNames){
            executeCommand("INSERT INTO device_type (name) VALUES ('" + typeName + "')");
        }
    }

    /**
     * Insert the default security states into the database.
     */
    public static void insertDefaultSecurityStates() {
        logger.info("Inserting default security states.");
        List<String> stateNames = new ArrayList<>();
        stateNames.add("normal");
        stateNames.add("suspicious");
        stateNames.add("under_attack");
        for(String stateName: stateNames){
            executeCommand("INSERT INTO security_state (name) VALUES ('" + stateName + "')");
        }
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
            logger.info(String.format("Finding by id = %d in %s", id, tableName));
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
     *      Alert specific actions.
     */
    /**
     * Finds an Alert from the databse with the given id
     * @param id The id of the desired Alert
     * @return An Alert with desired id
     */
    public static CompletionStage<Alert> findAlert(int id) {
        return findById(id, "alert").thenApplyAsync(rs -> {
            if(rs == null) {
                return null;
            } else {
                return rsToAlert(rs);
            }
        });
    }

    /**
     * Finds all Alerts from the database for the given list of UmboxInstance alerterIds.
     * @param alerterIds a list of alerterIds of UmboxInstances.
     * @return a list of all Alerts in the database where the the alert was created by a UmboxInstance with
     *         alerterId in alerterIds.
     */
    public static CompletionStage<List<Alert>> findAlerts(List<String> alerterIds) {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            List<Alert> alertHistory = new ArrayList<Alert>();
            for(String alerterId : alerterIds) {
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
                }  finally {
                    try { if (rs != null) { rs.close(); } } catch (Exception e) {}
                    try { if (st != null) { st.close(); } } catch (Exception e) {}
                }
            }
            return alertHistory;
        });

    }

    /**
     * Extract an Alert from the result set of a database query.
     * @param rs ResultSet from a Alert query.
     * @return The Alert that was found.
     */
    private static Alert rsToAlert(ResultSet rs){
        Alert alert = null;
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            Timestamp timestamp = rs.getTimestamp("timestamp");
            String alerterId = rs.getString("alerter_id");
            int deviceStatusId = rs.getInt("device_status_id");
            int alertTypeId = rs.getInt("alert_type_id");
            alert = new Alert(id, name, timestamp, alerterId, deviceStatusId, alertTypeId);
        }
        catch(Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to Alert: " + e.getClass().getName()+": "+e.getMessage());
        }
        return alert;
    }

    /**
     * Insert a row into the alert table
     * @param alert The Alert to be added
     * @return id of new Alert on success. -1 on error
     */
    public static CompletionStage<Integer> insertAlert(Alert alert) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Inserting alert: " + alert.toString());
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try {
                PreparedStatement insertAlert = dbConn.prepareStatement("INSERT INTO alert(name, timestamp, alert_type_id, alerter_id, device_status_id) VALUES (?,?,?,?,?);");
                insertAlert.setString(1, alert.getName());
                insertAlert.setTimestamp(2, alert.getTimestamp());
                insertAlert.setInt(3, alert.getAlertTypeId());
                insertAlert.setString(4, alert.getAlerterId());
                insertAlert.setInt(5, alert.getDeviceStatusId());
                insertAlert.executeUpdate();
                return getLatestId("alert");
            } catch (SQLException e) {
                e.printStackTrace();
                logger.severe("Error inserting Alert: "+ alert.toString() + " " + e.getClass().getName() + ": " + e.getMessage());
            }
            return -1;
        });
    }

    /**
     * Updates Alert with given id to have the parameters of the given Alert.
     * @param alert Alert holding new parameters to be saved in the database.
     * @return the id of the updated Alert on success. -1 on failure
     */
    public static CompletionStage<Integer> updateAlert(Alert alert) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info(String.format("Updating Alert with id = %d with values: %s", alert.getId(), alert));
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            } else {
                try {
                    PreparedStatement update = dbConn.prepareStatement("UPDATE alert " +
                            "SET name = ?, timestamp = ?, alerter_id = ?, device_status_id = ?, alert_type_id = ?" +
                            "WHERE id = ?");
                    update.setString(1, alert.getName());
                    update.setTimestamp(2, alert.getTimestamp());
                    update.setString(3, alert.getAlerterId());
                    update.setInt(4, alert.getDeviceStatusId());
                    update.setInt(5, alert.getAlertTypeId());
                    update.setInt(6, alert.getId());
                    update.executeUpdate();

                    return alert.getId();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("Error updating Alert: " + e.getClass().toString() + ": " + e.getMessage());
                }
            }
            return -1;
        });
    }

    /**
     * Deletes an Alert by its id.
     * @param id id of the Alert to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static CompletionStage<Boolean> deleteAlert(int id) {
        return deleteById("alert", id);
    }


    /*
     *      AlertCondition specific actions
     */

    /**
     * Finds an AlertCondition from the databse with the given id
     * @param id The id of the desired AlertCondition
     * @return An AlertCondition with desired id
     */
    public static CompletionStage<AlertCondition> findAlertCondition(int id){
        return findById(id, "alert_condition").thenApplyAsync(rs -> {
            if(rs == null) {
                return null;
            } else {
                return rsToAlertCondition(rs);
            }
        });
    }

    /**
     * Finds all AlertConditions from the database for the given device_id
     * @param deviceId an id of a device
     * @return a list of all AlertConditions in the database related to the given device
     */
    public static CompletionStage<List<AlertCondition>> findAlertConditionsByDevice(int deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            List<AlertCondition> conditionList = new ArrayList<AlertCondition>();
            try {
                st = dbConn.prepareStatement("SELECT * FROM alert_condition WHERE device_id = ?");
                st.setInt(1, deviceId);
                rs = st.executeQuery();
                while (rs.next()) {
                    conditionList.add(rsToAlertCondition(rs));
                }
            } catch (SQLException e) {
                logger.severe("Sql exception getting all alert conditions: " + e.getClass().getName() + ": " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting alert conditions: " + e.getClass().getName() + ": " + e.getMessage());
            }  finally {
                try { if (rs != null) { rs.close(); } } catch (Exception e) {}
                try { if (st != null) { st.close(); } } catch (Exception e) {}
            }
            return conditionList;
        });

    }

    /**
     * Extract an AlertCondition from the result set of a database query.
     * @param rs ResultSet from a AlertCondition query.
     * @return The AlertCondition that was found.
     */
    private static AlertCondition rsToAlertCondition(ResultSet rs){
        AlertCondition cond = null;
        try {
            int id = rs.getInt("id");
            Map<String, String> variables = HStoreConverter.fromString(rs.getString("variables"));
            int deviceId = rs.getInt("device_id");
            int alertTypeId = rs.getInt("alert_type_id");
            cond = new AlertCondition(id, variables, deviceId, alertTypeId);
        }
        catch(Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to Alert: " + e.getClass().getName()+": "+e.getMessage());
        }
        return cond;
    }

    /**
     * Insert a row into the AlertCondition table
     * @param cond The AlertCondition to be added
     * @return id of new AlertCondition on success. -1 on error
     */
    public static CompletionStage<Integer> insertAlertCondition(AlertCondition cond) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Inserting alert condition for device: " + cond.getDeviceId());
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try {
                PreparedStatement insertAlertCondition = dbConn.prepareStatement("INSERT INTO alert_condition(variables, device_id, alert_type_id) VALUES (?,?,?);");
                insertAlertCondition.setObject(1, cond.getVariables());
                insertAlertCondition.setInt(2, cond.getDeviceId());
                insertAlertCondition.setInt(3, cond.getAlertTypeId());
                insertAlertCondition.executeUpdate();
                return getLatestId("alert_condition");
            } catch (SQLException e) {
                e.printStackTrace();
                logger.severe("Error inserting AlertCondition: " + e.getClass().getName() + ": " + e.getMessage());
            }
            return -1;
        });
    }

    /**
     * Updates provided AlertCondition
     * @param condition AlertCondition holding new values to be saved in the database.
     * @return the id of the updated Alert on success. -1 on failure
     */
    public static CompletionStage<Integer> updateAlertCondition(AlertCondition condition) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info(String.format("Updating AlertCondition with id = %d with values: %s", condition.getId(), condition));
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            } else {
                try {
                    PreparedStatement update = dbConn.prepareStatement("UPDATE alert_condition " +
                            "SET variables = ?, device_id = ?, alert_type_id = ?" +
                            "WHERE id = ?");
                    update.setObject(1, condition.getVariables());
                    update.setInt(2, condition.getDeviceId());
                    update.setInt(3, condition.getAlertTypeId());
                    update.setInt(4, condition.getId());
                    update.executeUpdate();

                    return condition.getId();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("Error updating AlertCondition: " + e.getClass().toString() + ": " + e.getMessage());
                }
            }
            return -1;
        });
    }

    /**
     * First, attempts to find the AlertCondition in the database.
     * If successful, updates the existing AlertCondition with the given AlertCondition's parameters. Otherwise,
     * inserts the given AlertCondition.
     * @param condition AlertCondition to be inserted or updated.
     */
    public static CompletionStage<Integer> insertOrUpdateAlertCondition(AlertCondition condition){
        return findAlertCondition(condition.getId()).thenApplyAsync(c -> {
            if(c == null) {
                insertAlertCondition(condition);
                return 0;
            } else {
                updateAlertCondition(condition);
                return 1;
            }
        });
    }

    /**
     * Deletes an AlertCondition by its id.
     * @param id id of the AlertCondition to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static CompletionStage<Boolean> deleteAlertCondition(int id) {
        return deleteById("alert_condition", id);
    }

    /*
     *      AlertType specific actions
     */

    /**
     * Finds an AlertType from the databse with the given id
     * @param id The id of the desired AlertType
     * @return An AlertType with desired id
     */
    public static CompletionStage<AlertType> findAlertType(int id){
        return findById(id, "alert_type").thenApplyAsync(rs -> {
            if(rs == null) {
                return null;
            } else {
                return rsToAlertType(rs);
            }
        });
    }

    /**
     * Finds all AlertTypes from the database for the given type_id
     * @param deviceTypeId an id of a DeviceType
     * @return a list of all AlertTypes in the database for the given DeviceType
     */
    public static CompletionStage<List<AlertType>> findAlertTypesByDeviceType(int deviceTypeId) {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            if(dbConn == null){
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
            }  finally {
                try { if (rs != null) { rs.close(); } } catch (Exception e) {}
                try { if (st != null) { st.close(); } } catch (Exception e) {}
            }
            return alertTypeList;
        });

    }

    /**
     * Finds all AlertTypes in the database
     * @return a list of AlertTypes
     */
    public static List<AlertType> findAllAlertTypes() {
        if(dbConn == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        List<AlertType> alertTypeList = new ArrayList<AlertType>();
        try{
            ResultSet rs = getAllFromTable("alert_type");
            while (rs.next()) {
                alertTypeList.add(rsToAlertType(rs));
            }
            rs.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting all AlertTypes: " + e.getClass().getName()+": "+e.getMessage());
        }
        return alertTypeList;
    }

    /**
     * Extract an AlertType from the result set of a database query.
     * @param rs ResultSet from a AlertType query.
     * @return The AlertType that was found.
     */
    private static AlertType rsToAlertType(ResultSet rs){
        AlertType type = null;
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String description = rs.getString("description");
            String source = rs.getString("source");
            type = new AlertType(id, name, description, source);
        }
        catch(Exception e) {
            e.printStackTrace();
            logger.severe("Error converting rs to AlertType: " + e.getClass().getName()+": "+e.getMessage());
        }
        return type;
    }

    /**
     * Insert a row into the AlertType table
     * @param type The AlertType to be added
     * @return id of new AlertType on success. -1 on error
     */
    public static CompletionStage<Integer> insertAlertType(AlertType type) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Inserting alert type: " + type.getName());
            if(dbConn == null){
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
        });
    }

    /**
     * Updates provided AlertType
     * @param type AlertType holding new values to be saved in the database.
     * @return the id of the updated Alert on success. -1 on failure
     */
    public static CompletionStage<Integer> updateAlertType(AlertType type) {
        return CompletableFuture.supplyAsync(() -> {
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
        });
    }

    /**
     * First, attempts to find the AlertType in the database.
     * If successful, updates the existing AlertType with the given AlertType's parameters Otherwise,
     * inserts the given AlertType.
     * @param type AlertType to be inserted or updated.
     */
    public static CompletionStage<Integer> insertOrUpdateAlertType(AlertType type){
        return findAlertType(type.getId()).thenApplyAsync(a -> {
            if(a == null) {
                insertAlertType(type);
                return 0;
            } else {
                updateAlertType(type);
                return 1;
            }
        });
    }

    /**
     * Deletes an AlertType by its id.
     * @param id id of the AlertType to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static CompletionStage<Boolean> deleteAlertType(int id) {
        return CompletableFuture.supplyAsync(()->{
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
        });

    }

    /*
     *      CommandLookup specific actions
     */

    /**
     * Finds the commands for the device in its current state
     * @param device The device in question
     * @return commands A list of command names
     */
    public static CompletionStage<List<String>> findCommandsByDevice(Device device) {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            if (dbConn == null) {
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            try {
                st = dbConn.prepareStatement("SELECT * FROM command_lookup WHERE device_type_id = ? AND state_id = ?");
                st.setInt(1, device.getType().getId());
                st.setInt(2, device.getCurrentState().getStateId());
                rs = st.executeQuery();

                List<String> commands = new ArrayList<String>();
                while (rs.next()) {
                    commands.add(rsToCommandLookup(rs));
                }
                return commands;
            } catch (SQLException e) {
                logger.severe("Sql exception getting all commands for device: " + e.getClass().getName() + ": " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting device commands: " + e.getClass().getName() + ": " + e.getMessage());
            } finally {
                try { if (rs != null) { rs.close(); } } catch (Exception e) { }
                try { if (st != null) { st.close(); } } catch (Exception e) { }
            }
            return null;
        });
    }

    /**
     * Extract a Command name from the result set of a database query.
     * @param rs ResultSet from a CommandLookup query.
     * @return The name that was found.
     */
    private static String rsToCommandLookup(ResultSet rs){
        String name = "";
        try{
            name = rs.getString("name");
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to CommandLookup name: " + e.getClass().getName()+": "+e.getMessage());
        }
        return name;
    }

    /**
     * insert a command into the db
     */
    public static int insertCommand(int deviceTypeId, int stateId, String name){
        logger.info("Inserting command: " + name);
        if(dbConn == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try {
            PreparedStatement insertCommand = dbConn.prepareStatement("INSERT INTO command_lookup(device_type_id, state_id, name) VALUES (?,?,?);");
            insertCommand.setInt(1, deviceTypeId);
            insertCommand.setInt(2, stateId);
            insertCommand.setString(3, name);
            insertCommand.executeUpdate();
            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting Command: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
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

                DeviceSecurityState ss = findDeviceSecurityStateByDevice(device.getId());
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
                    DeviceSecurityState ss = findDeviceSecurityStateByDevice(d.getId());
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
     * Find devices in a given group
     * @parameter groupId the id of the group
     * @return a list of Devices with the given groupId
     */
    public static List<Device> findDevicesByGroup(int groupId){
        PreparedStatement st = null;
        ResultSet rs = null;
        if (dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try {
            st = dbConn.prepareStatement("SELECT * FROM device WHERE group_id = ?");
            st.setInt(1, groupId);
            rs = st.executeQuery();

            List<Device> devices = new ArrayList<Device>();
            while (rs.next()) {
                devices.add(rsToDevice(rs));
            }
            return devices;
        } catch (SQLException e) {
            logger.severe("Sql exception getting all devices for group: " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting device statuses: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            try { if (rs != null) { rs.close(); } } catch (Exception e) { }
            try { if (st != null) { st.close(); } } catch (Exception e) { }
        }
        return null;
    }

    /**
     * Finds the Device related to the given Alert id
     * @param the Alert
     * @return the Device associated with the alert
     */
    public static Device findDeviceByAlert(Alert alert){
        PreparedStatement st = null;
        ResultSet rs = null;
        if(dbConn == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try{

            if(alert.getDeviceStatusId() != null){
                st = dbConn.prepareStatement("SELECT * FROM device WHERE id = (SELECT device_id FROM device_status WHERE id = ?)");
                st.setInt(1, alert.getDeviceStatusId());
                rs = st.executeQuery();
                if(rs.next())
                    return rsToDevice(rs);
            }
            else if(alert.getAlerterId() != null) {
                st = dbConn.prepareStatement("SELECT * FROM device WHERE id = (SELECT device_id FROM umbox_instance WHERE alerter_id = ?)");
                st.setString(1, alert.getAlerterId());
                if(rs.next())
                    return rsToDevice(rs);
            }
            else {
                logger.severe("Error: alert has no associated DeviceStatus OR UmboxInstance!");
                return null;
            }

        } catch (SQLException e) {
            logger.severe("Sql exception getting the device for the alert: " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting device for the alert: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            try { if (rs != null) { rs.close(); } } catch (Exception e) { }
            try { if (st != null) { st.close(); } } catch (Exception e) { }
        }
        return null;
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
                update.setInt(3, device.getType().getId());
                update.setInt(4, device.getGroup().getId());
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
                    update.setInt(3, device.getType().getId());
                    update.setInt(4, device.getGroup().getId());
                    update.setString(5, device.getIp());
                    update.setInt(6, device.getStatusHistorySize());
                    update.setInt(7, device.getSamplingRate());

                    if(device.getCurrentState() != null){
                        update.setInt(8, device.getCurrentState().getStateId());
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
     * @param length the number of statuses to retrieve
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
                String query = String.format("SELECT * FROM device_status WHERE (device_id = %d) AND (timestamp between (now() - interval '%s %s') and now())", deviceId, Integer.toString(length), timeUnit);
                st = dbConn.prepareStatement(query);
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

    public static Map<Device, DeviceStatus> findDeviceStatusesByType(int typeId){
//        return CompletableFuture.supplyAsync(() -> {
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
//        });
    }

    /**
     * Returns a list of device statuses for devices with the given group id. One device status per device
     * @param groupId The typeid for the requested devices
     * @return A map pairing a device with its most recent DeviceStatus
     */

    public static Map<Device, DeviceStatus> findDeviceStatusesByGroup(int groupId){
//        return CompletableFuture.supplyAsync(() -> {
        PreparedStatement st = null;
        ResultSet rs = null;
        if(dbConn == null){
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
                while(resultSet.next()){
                    deviceStatus = rsToDeviceStatus(resultSet);
                }
                deviceStatusMap.put(device, deviceStatus);
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting devices for group: "+ groupId+" "+ e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting devices for group: "+ groupId+" " + e.getClass().getName() + ": " + e.getMessage());
        }  finally {
            try { if (rs != null) { rs.close(); } } catch (Exception e) {}
            try { if (st != null) { st.close(); } } catch (Exception e) {}
        }
        return deviceStatusMap;
//        });
    }

//
//    public static DeviceStatus findLastDeviceStatusAttributeChange(int deviceId, String attribute) {
//        PreparedStatement st = null;
//        ResultSet rs = null;
//        if(dbConn == null) {
//            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
//            return null;
//        }
//
//        try {
////            st = dbConn.prepareStatement
//        } catch (SQLException e) {
//            logger.severe("Sql exception getting device statuses for device: "+ deviceId+" "+ e.getClass().getName() + ": " + e.getMessage());
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.severe("Error getting devices statuses for device: "+ deviceId+" " + e.getClass().getName() + ": " + e.getMessage());
//        }  finally {
//            try { if (rs != null) { rs.close(); } } catch (Exception e) {}
//            try { if (st != null) { st.close(); } } catch (Exception e) {}
//        }
//    }

    /**
     * Finds all DeviceStatuses in the database.
     * @return a list of all DeviceStatuses in the database.
     */
    public static CompletionStage<List<DeviceStatus>> findAllDeviceStatuses() {
        return CompletableFuture.supplyAsync(() -> {
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
     * @param group Device Group to be inserted.
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
     * First, attempts to find the Group in the database.
     * If successful, updates the existing Group with the given Groups's parameters. Otherwise,
     * inserts the given Group.
     * @param group Group to be inserted or updated.
     */
    public static CompletionStage<Integer> insertOrUpdateGroup(Group group){
        return findGroup(group.getId()).thenApplyAsync(g -> {
            if(g == null) {
                insertGroup(group);
                return 0;
            } else {
                updateGroup(group);
                return 1;
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
     *      DeviceSecurityState specific actions
     */

    /**
     * Finds the DeviceSecurityState with the supplied id
     * @param the id for the DeviceSecurityState
     * @return the DeviceSecurityState, if it exists
     */
    public static DeviceSecurityState findDeviceSecurityState(int id){
        PreparedStatement st = null;
        ResultSet rs = null;
        DeviceSecurityState dss = null;

        logger.info("Finding device security state with id: " + id);
        if(dbConn == null) {
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }

        try{
            st = dbConn.prepareStatement("SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                    "FROM device_security_state dss, security_state ss " +
                    "WHERE dss.id=? AND dss.state_id = ss.id " +
                    "ORDER BY timestamp DESC " +
                    "LIMIT 1");
            st.setInt(1, id);
            rs = st.executeQuery();

            if(rs.next()){
                dss = rsToDeviceSecurityState(rs);
            }
        }
        catch (SQLException e){
            logger.severe("SQL exception getting the device state: "+e.getClass().getName()+": "+e.getMessage());
        }
        catch (Exception e){
            e.printStackTrace();
            logger.severe("Error getting device state: " + e.getClass().getName()+": "+e.getMessage());
        }
        finally {
            try { if(rs != null) { rs.close(); } } catch(Exception e) {}
            try { if(st != null) { st.close(); } } catch(Exception e) {}
        }
        return dss;
    }

    /**
     * Finds the most recent DeviceSecurityState from the database for the given device
     * @param deviceId the id of the device
     * @return the most recent DeviceSecurityState entered for a device
     */
    public static DeviceSecurityState findDeviceSecurityStateByDevice(int deviceId){
//        return CompletableFuture.supplyAsync(() -> {
           PreparedStatement st = null;
           ResultSet rs = null;
           DeviceSecurityState ss = new DeviceSecurityState();

           if(dbConn == null) {
               logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
               return null;
           }

           try{
               st = dbConn.prepareStatement("SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                                            "FROM device_security_state dss, security_state ss " +
                                            "WHERE dss.device_id=? AND dss.state_id = ss.id " +
                                            "ORDER BY timestamp DESC " +
                                            "LIMIT 1");
               st.setInt(1, deviceId);
               rs = st.executeQuery();

               while(rs.next()){
                   ss = rsToDeviceSecurityState(rs);
               }
           }
           catch (SQLException e){
               logger.severe("SQL exception getting the device state: "+e.getClass().getName()+": "+e.getMessage());
           }
           catch (Exception e){
               e.printStackTrace();
               logger.severe("Error getting device state: " + e.getClass().getName()+": "+e.getMessage());
           }
           finally {
               try { if(rs != null) { rs.close(); } } catch(Exception e) {}
               try { if(st != null) { st.close(); } } catch(Exception e) {}
           }
            return ss;
//        });
    }

    /**
     * Finds all DeviceSecurityState from the database for the given device.
     * @param deviceId the id of the device.
     * @return a list of all DeviceSecurityState in the database where the device_id field is equal to deviceId.
     */
    public static CompletionStage<List<DeviceSecurityState>> findDeviceSecurityStates(int deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement st = null;
            ResultSet rs = null;
            List<DeviceSecurityState> deviceStateList = new ArrayList<DeviceSecurityState>();

            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return null;
            }
            try{
                st = dbConn.prepareStatement("SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                                             "FROM device_security_state dss, security_state ss " +
                                             "WHERE dss.device_id=? AND dss.state_id = ss.id " +
                                             "ORDER BY timestamp DESC");
                st.setInt(1, deviceId);
                rs = st.executeQuery();

                while (rs.next()) {
                    deviceStateList.add(rsToDeviceSecurityState(rs));
                }
            }
            catch(SQLException e) {
                logger.severe("Sql exception getting all device states: " + e.getClass().getName()+": "+e.getMessage());
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.severe("Error getting device states: " + e.getClass().getName()+": "+e.getMessage());
            }
            finally {
                try { if(rs != null) { rs.close(); } } catch(Exception e) {}
                try { if(st != null) { st.close(); } } catch(Exception e) {}
            }
            return deviceStateList;
        });
    }

    /**
     * Extract a DeviceSecurityState from the result set of a database query.
     * @param rs ResultSet from a DeviceState query.
     * @return The DeviceSecurityState that was found.
     */
    private static DeviceSecurityState rsToDeviceSecurityState(ResultSet rs){
        DeviceSecurityState deviceState = null;
        try{
            int id = rs.getInt("id");
            int deviceId = rs.getInt("device_id");
            int stateId = rs.getInt("state_id");
            Timestamp timestamp = rs.getTimestamp("timestamp");
            String name = rs.getString("name");
            deviceState = new DeviceSecurityState(id, deviceId, stateId, timestamp, name);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to DeviceSecurityState: " + e.getClass().getName()+": "+e.getMessage());
        }
        return deviceState;
    }

    /**
     * Saves given DeviceSecurityState to the database.
     * @param deviceState DeviceSecurityState to be inserted.
     * @return The id of the new DeviceSecurityState if successful
     */
    public static CompletionStage<Integer> insertDeviceSecurityState(DeviceSecurityState deviceState){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Inserting DeviceSecurityState");
            PreparedStatement insert = null;
            ResultSet rs = null;

            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try{
                insert = dbConn.prepareStatement
                        ("INSERT INTO device_security_state(device_id, timestamp, state_id) " +
                                "values(?,?,?) " +
                                "RETURNING id");
                insert.setInt(1, deviceState.getDeviceId());
                insert.setTimestamp(2, deviceState.getTimestamp());
                insert.setInt(3, deviceState.getStateId());
                rs = insert.executeQuery();
                if(rs.next()){
                    return rs.getInt("id");
                }
            }
            catch(Exception e){
                e.printStackTrace();
                logger.severe("Error inserting DeviceState: " + e.getClass().getName()+": "+e.getMessage());
            }
            return -1;
        });
    }

// THE FOLLOWING FUNCTION SHOULDNT BE NEEDED
// DEVICE_SECURITY_STATES SHOULD ONLY BE INSERTED/QUERIED
//    /**
//     * Updates DeviceSecurityState with given id to have the parameters of the given DeviceState.
//     * @param deviceState DeviceSecurityState holding new parameters to be saved in the database.
//     */
//    public static CompletionStage<Integer> updateDeviceSecurityState(DeviceSecurityState deviceState){
//        return CompletableFuture.supplyAsync(() -> {
//            logger.info("Updating DeviceState with id=" + deviceState.getId());
//            if (dbConn == null) {
//                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
//                return -1;
//            }
//            try {
//                PreparedStatement update = dbConn.prepareStatement
//                        ("UPDATE device_state SET device_id = ?, timestamp = ?, state = ?" +
//                                "WHERE id=?");
//                update.setInt(1, deviceState.getDeviceId());
//                update.setTimestamp(2, deviceState.getTimestamp());
//                update.setString(3, deviceState.getName());
//                update.setInt(4, deviceState.getId());
//                update.executeUpdate();
//                return deviceState.getId();
//            } catch (Exception e) {
//                e.printStackTrace();
//                logger.severe("Error updating DeviceState: " + e.getClass().toString() + ": " + e.getMessage());
//            }
//            return -1;
//        });
//    }

    /**
     * Deletes a DeviceSecurityState by its id.
     * @param id id of the DeviceSecurityState to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static CompletionStage<Boolean> deleteDeviceSecurityState(int id) {
        return deleteById("device_id", id);
    }

    /*
     *      SecurityState specific actions
     */

    /**
     * Search the security_state table for a row with the given id
     * @param id The id of the security state
     * @return the row from the table
     */
    public static CompletionStage<SecurityState> findSecurityState(int id){
        return findById(id, "security_state").thenApplyAsync(rs -> {
            if(rs == null) {
                return null;
            } else {
                SecurityState state = rsToSecurityState(rs);
                return state;
            }
        });
    }

    /**
     * Take a ResultSet from a DB query and convert to the java object
     * @param rs
     * @return
     */
    private static SecurityState rsToSecurityState(ResultSet rs) {
        SecurityState state = null;
        try{
            int id = rs.getInt("id");
            String name = rs.getString("name");
            state = new SecurityState(id, name);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to SecurityState: " + e.getClass().getName()+": "+e.getMessage());
        }
        return state;
    }

    /**
     * Inserts the given SecurityState into the db
     * @param the security state to enter
     * @return the id of the newly inserted SecurityState
     */
    public static Integer insertSecurityState(SecurityState state){
        logger.info("Inserting SecurityState");
        if(dbConn == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return -1;
        }
        try{
            PreparedStatement update = dbConn.prepareStatement
                    ("INSERT INTO security_state(name)" +
                            "values(?)");
            update.setString(1, state.getName());
            update.executeUpdate();
            return getLatestId("security_state");
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error inserting SecurityState: " + e.getClass().getName()+": "+e.getMessage());
        }
        return -1;
    }

    /**
     * Update row in security_state corresponding to the parameter
     * @param state The security state to update
     * @return The id of the updated row
     */
    public static CompletionStage<Integer> updateSecurityState(SecurityState state) {
        return CompletableFuture.supplyAsync(() -> {
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
        });
    }

    /**
     * First, attempts to find the SecurityState in the database.
     * If successful, updates the existing SecurityState with the given SecurityState's parameters Otherwise,
     * inserts the given SecurityState.
     * @param state SecurityState to be inserted or updated.
     */
    public static CompletionStage<Integer> insertOrUpdateSecurityState(SecurityState state){
        return findSecurityState(state.getId()).thenApplyAsync(s -> {
            if(s == null) {
                insertSecurityState(state);
                return 0;
            } else {
                updateSecurityState(state);
                return 1;
            }
        });
    }

    /**
     * Delete row from security_state with the given id
     * @param id The id of the row to delete
     * @return True if successful
     */
    public static CompletionStage<Boolean> deleteSecurityState(int id) {
        return deleteById("security_state", id);
    }

    /*
     *      Tag specific actions
     */

    /**
     * Search the tag table for a row with the given id
     * @param id The id of the tag
     * @return the row from the table
     */
    public static CompletionStage<Tag> findTag(int id){
        return findById(id, "tag").thenApplyAsync(rs -> {
            if(rs == null) {
                return null;
            } else {
                Tag tag = rsToTag(rs);
                return tag;
            }
        });
    }

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
     * First, attempts to find the Tag in the database.
     * If successful, updates the existing Tag with the given Tag's parameters Otherwise,
     * inserts the given Tag.
     * @param tag Tag to be inserted or updated.
     */
    public static CompletionStage<Integer> insertOrUpdateTag(Tag tag){
        return findTag(tag.getId()).thenApplyAsync(t -> {
            if(t == null) {
                insertTag(tag);
                return 0;
            } else {
                updateTag(tag);
                return 1;
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
     *      DeviceType specific actions
     */
    /**
     * Finds a DeviceType from the database by its id.
     * @param id id of the DeviceType to find.
     * @return the DeviceType if it exists in the database, else null.
     */
    public static CompletionStage<DeviceType> findDeviceType(int id) {
        return findById(id, "device_type").thenApplyAsync(rs -> {
            if(rs == null) {
                return null;
            } else {
                DeviceType type = rsToDeviceType(rs);
                return type;
            }
        });
    }

    /**
     * Finds all DeviceTypes in the database.
     * @return a list of all DeviceTypes in the database.
     */
    public static CompletionStage<List<DeviceType>> findAllDeviceTypes() {
        return CompletableFuture.supplyAsync(() -> {
            ResultSet rs = getAllFromTable("device_type");
            List<DeviceType> types = new ArrayList<DeviceType>();
            try {
                while (rs.next()) {
                    types.add(rsToDeviceType(rs));
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
                logger.severe("SQLException getting all DeviceTypes: " + e.getClass().getName()+": "+e.getMessage());
            }
            finally {
                try { if(rs != null) rs.close(); } catch (Exception e) {}
            }
            return types;
        });
    }

    /**
     * Extract a DeviceType from the result set of a database query.
     * @param rs ResultSet from a DeviceType query.
     * @return The first DeviceType in rs.
     */
    private static DeviceType rsToDeviceType(ResultSet rs){
        DeviceType type = null;
        try{
            int id = rs.getInt("id");
            String name = rs.getString("name");
            byte[] policyFile = rs.getBytes("policy_file");
            String policyFileName = rs.getString("policy_file_name");
            type = new DeviceType(id, name, policyFile, policyFileName);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.severe("Error converting rs to DeviceType: " + e.getClass().getName()+": "+e.getMessage());
        }
        return type;
    }

    /**
     * Saves given DeviceType to the database.
     * @param type DeviceType to be inserted.
     * @return auto incremented id
     */
    public static CompletionStage<Integer> insertDeviceType(DeviceType type){
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Inserting DeviceType: " + type.getId());
            if(dbConn == null){
                logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
                return -1;
            }
            try{
                PreparedStatement update = dbConn.prepareStatement
                        ("INSERT INTO device_type(name, policy_file, policy_file_name)" +
                                "values(?,?,?)");
                update.setString(1, type.getName());
                update.setBytes(2, type.getPolicyFile());
                update.setString(3, type.getPolicyFileName());
                update.executeUpdate();
                return getLatestId("device_type");
            }
            catch(Exception e){
                e.printStackTrace();
                logger.severe("Error inserting DeviceType: " + e.getClass().getName()+": "+e.getMessage());
            }
            return -1;
        });
    }

    /**
     * Updates DeviceType with given id to have the parameters of the given DeviceType.
     * @param type DeviceType holding new parameters to be saved in the database.
     */
    public static CompletionStage<Integer> updateDeviceType(DeviceType type){
        return CompletableFuture.supplyAsync(() -> {
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
        });
    }

    /**
     * First, attempts to find the DeviceType in the database.
     * If successful, updates the existing DeviceType with the given DeviceType's parameters Otherwise,
     * inserts the given DeviceType.
     * @param type DeviceType to be inserted or updated.
     */
    public static CompletionStage<Integer> insertOrUpdateDeviceType(DeviceType type){
        return findAlertType(type.getId()).thenApplyAsync(a -> {
            if(a == null) {
                insertDeviceType(type);
                return 0;
            } else {
                updateDeviceType(type);
                return 1;
            }
        });
    }

    /**
     * Deletes a DeviceType by its id.
     * @param id id of the DeviceType to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static CompletionStage<Boolean> deleteDeviceType(int id) {
        return deleteById("device_type", id);
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
                return rsToUmboxImageNoDagOrder(rs);
            }
        });
    }

    /**
     * Finds the UmboxImages relating to the device type and the security state
     * @param the id of the device type
     * @param the id of the security state
     * @return A list of UmboxImages for the given device type id and state id
     */
    public static List<UmboxImage> findUmboxImagesByDeviceTypeAndSecState(int devTypeId, int secStateId){
        PreparedStatement st = null;
        ResultSet rs = null;
        if(dbConn == null){
            logger.severe("Trying to execute commands with null connection. Initialize Postgres first!");
            return null;
        }
        try{
            st = dbConn.prepareStatement("SELECT ui.id, ui.name, ui.path, ul.dag_order " +
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
        }
        catch(SQLException e) {
            logger.severe("Sql exception getting all UmboxImages: " + e.getClass().getName()+": "+e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error getting UmboxImages: " + e.getClass().getName()+": "+e.getMessage());
        }
        finally {
            try { if(rs != null) { rs.close(); } } catch(Exception e) {}
            try { if(st != null) { st.close(); } } catch(Exception e) {}
        }
        return null;
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
     * Extract a UmboxImage from the result set of a database query that DOES NOT include umbox_lookup.
     * @param rs ResultSet from a UmboxImage query.
     * @return The first UmboxImage in rs.
     */
    private static UmboxImage rsToUmboxImageNoDagOrder(ResultSet rs){
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
     * Extract a UmboxImage from the result set of a database query that includes umbox_lookup.
     * @param rs ResultSet from a UmboxImage query.
     * @return The first UmboxImage in rs.
     */
    private static UmboxImage rsToUmboxImage(ResultSet rs){
        UmboxImage umboxImage = null;
        try{
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String path = rs.getString("path");
            int dagOrder = rs.getInt("dag_order");
            umboxImage = new UmboxImage(id, name, path, dagOrder);
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
     * First, attempts to find the UmboxImage in the database.
     * If successful, updates the existing UmboxImage with the given UmboxImage's parameters Otherwise,
     * inserts the given UmboxImage.
     * @param image UmboxImage to be inserted or updated.
     */
    public static CompletionStage<Integer> insertOrUpdateUmboxImage(UmboxImage image){
        return findUmboxImage(image.getId()).thenApplyAsync(i -> {
            if(i == null) {
                insertUmboxImage(image);
                return 0;
            } else {
                updateUmboxImage(image);
                return 1;
            }
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
     * @param alerterId The ID of desired UmboxInstance
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
            int deviceId = rs.getInt("device_id");
            Timestamp startedAt = rs.getTimestamp("started_at");
            umboxInstance = new UmboxInstance(id, alerterId, imageId, deviceId, startedAt);
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
               st = dbConn.prepareStatement("INSERT INTO umbox_instance (alerter_id, umbox_image_id, device_id, started_at) VALUES (?,?,?,?)");
               st.setString(1, u.getAlerterId());
               st.setInt(2, u.getUmboxImageId());
               st.setInt(3, u.getDeviceId());
               st.setTimestamp(4, u.getStartedAt());
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
                        "SET alerter_id = ?, umbox_image_id = ?, device_id = ?, started_at = ?" +
                        "WHERE id = ?");
                st.setString(1, u.getAlerterId());
                st.setInt(2, u.getUmboxImageId());
                st.setInt(3, u.getDeviceId());
                st.setTimestamp(4, u.getStartedAt());
                st.setInt(5, u.getId());
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

    /*
     * UmboxLookup functions
     */

    public static int insertUmboxLookup(int umboxImageId, int deviceTypeId, int secStateId, int dagOrder){
        logger.info("Adding umbox lookup: ");
        PreparedStatement st = null;
        try{
            st = dbConn.prepareStatement("INSERT INTO umbox_lookup (state_id, umbox_image_id, device_type_id, dag_order) VALUES (?,?,?,?)");
            st.setInt(1, secStateId);
            st.setInt(2, umboxImageId);
            st.setInt(3, deviceTypeId);
            st.setInt(4, dagOrder);
            st.executeUpdate();
            return 1;
        }
        catch (SQLException e){
            e.printStackTrace();
            logger.severe("SQL exception adding umbox instance: " + e.getClass().getName()+": "+e.getMessage());
        }
        return -1;
    }

}