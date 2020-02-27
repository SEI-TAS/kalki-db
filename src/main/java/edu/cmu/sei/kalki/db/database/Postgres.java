package edu.cmu.sei.kalki.db.database;

import edu.cmu.sei.kalki.db.utils.Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Postgres {
    private static final String DEFAULT_IP = "localhost";
    private static final String DEFAULT_PORT = "5432";

    private static final String DEFAULT_ROOT_USER = "kalkiuser";
    private static final String BASE_DB = "postgres";
    private static final String POSTGRES_URL_SCHEMA = "jdbc:postgresql://";

    public static final String TRIGGER_NOTIF_NEW_DEV_SEC_STATE = "devicesecuritystateinsert";
    public static final String TRIGGER_NOTIF_NEW_DEV_STATUS = "devicestatusinsert";
    public static final String TRIGGER_NOTIF_NEW_POLICY_INSTANCE = "policyruleloginsert";
    public static final String TRIGGER_NOTIF_NEW_ALERT = "alerthistoryinsert";

    private static Logger logger = Logger.getLogger("myLogger");
    private static String dbName;
    private static String dbUser;
    private static String dbPassword;
    private static Postgres postgresInstance = null;

    public static Connection dbConn = null;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //    General setup.
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Singleton constructor.
     */
    private Postgres(String ip, String port, String newDbName, String newDbUser, String newDbPassword) {
        try {
            //Read ip, port from config file
            dbName = newDbName;
            dbUser = newDbUser;
            dbPassword = newDbPassword;
            while((dbConn = makeConnection(ip, port)) == null) {
                logger.info("Waiting for DB engine to be available...");
                Thread.sleep(1000);
            }
            logger.info("DB connection established.");
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.severe("Error initializing postgres: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Initialize the singleton assuming the Config object has been loaded.
     */
    public static void initializeFromConfig() {
        // Mandatory params, minimum need to connect to DB.
        String dbName = Config.getValue("db_name");
        String dbUser = Config.getValue("db_user");
        String dbPass = Config.getValue("db_password");

        // Optional params, only needed if one wants to re-create the DB and user.
        String recreateDB = Config.getValue("db_recreate");
        if(recreateDB != null && recreateDB.equals("true"))
        {
            String rootUser = Config.getValue("db_root_user");
            String rootPassword = Config.getValue("db_root_password");
            String dbHost = DEFAULT_IP;

            // Recreate DB and user.
            Postgres.removeDatabase(dbHost, rootUser, rootPassword, dbName);
            Postgres.removeUser(dbHost, rootUser, rootPassword, dbUser);
            Postgres.createUserIfNotExists(dbHost, rootUser, rootPassword, dbUser, dbPass);
            Postgres.createDBIfNotExists(dbHost, rootUser, rootPassword, dbName, dbUser);
        }

        Postgres.initialize(dbName, dbUser, dbPass);
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
            Postgres.setupDatabase();
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
        logger.setLevel(lvl);
    }

    /**
     * Connects to the postgres database, allowing for database ops.
     *
     * @return a connection to the database.
     */
    private Connection makeConnection(String ip, String port) {
        try {
            Class.forName("org.postgresql.Driver");
            dbConn = DriverManager
                    .getConnection(POSTGRES_URL_SCHEMA + ip + ":" + port + "/" + Postgres.dbName, Postgres.dbUser, Postgres.dbPassword);
            return dbConn;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            logger.severe("Error connecting to database : " + e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Close the current connection.
     */
    public static void dropConnection() {
        try {
            dbConn.close();
        } catch (SQLException e) {
            logger.severe("Error dropping connection: "+e.getMessage());
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //    DB and User Management.
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a DB if it does not exist.
     */
    public static boolean createDBIfNotExists(String rootPassword, String dbName, String dbOwner) {
        return createDBIfNotExists(DEFAULT_IP, DEFAULT_ROOT_USER, rootPassword, dbName, dbOwner);
    }

    /**
     * Creates a DB if it does not exist.
     */
    public static boolean createDBIfNotExists(String ip, String rootUser, String rootPassword, String dbName, String dbOwner) {
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
        }
        return false;
    }

    /**
     * Creates a postgres database.
     */
    private static void makeDatabase(Connection rootConnection, String dbName, String dbOwner) {
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
     * First time database setup.
     * Creates necessary extensions, databases, and tables. Also inserts default device types.
     */
    public static void setupDatabase()
    {
        setupTables();
        setupDefaultDeviceTypes();
    }

    /**
     * First time database setup.
     * Creates necessary extensions, tables, and initial data.
     */
    public static void setupTables() {
        int numTables = getTableCount();
        logger.info("Current number of tables: " + numTables);
        if(numTables != 0) {
            logger.info("Database has been setup already.");
            return;
        }

        // Extensions to support compound fields.
        createHstoreExtension();

        // DB tables and triggers.
        executeSQLResource("db-tables.sql");
        executeSQLResource("db-triggers.sql");

        // DB initial data.
        executeSQLResource("db-security-states.sql");
        executeSQLResource("db-common-alert-types.sql");
    }

    /**
     * Add the hstore extension to the postgres database.
     */
    private static void createHstoreExtension() {
        logger.info("Adding hstore extension.");
        executeCommand("CREATE EXTENSION IF NOT EXISTS hstore;");
    }

    /**
     * Inserts default device types and their information.
     */
    private static void setupDefaultDeviceTypes() {
        // Device Type Specific configuration
        executeSQLResource("deviceTypes/dlc.sql");
        executeSQLResource("deviceTypes/phle.sql");
        executeSQLResource("deviceTypes/unts.sql");
        executeSQLResource("deviceTypes/wemo.sql");

        // Device instances in use.
        // executeSQLResource("deviceTypes/db-devices.sql");
    }

    /**
     * Drops and recreates all tables.
     */
    public static void resetDatabase() {
        logger.info("Resetting Database.");
        dropTables();
        setupDatabase();
    }

    /**
     * Returns the amount of tables in the DB.
     * @return
     */
    private static int getTableCount() {
        checkDBConnection();
        try {
            PreparedStatement st = dbConn.prepareStatement("SELECT COUNT(table_name) FROM information_schema.tables WHERE table_schema='public'");
            ResultSet rs = st.executeQuery();
            if(rs.next()){
                int count = rs.getInt("count");
                rs.close();
                return count;
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
        executeSQLResource("db-drop-tables.sql");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //    Generic DB Actions
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Checks if the DB connection has been initializes, throws an exception if it has not.
     */
    private static void checkDBConnection() {
        if (dbConn == null) {
            String message = "Trying to execute commands with null connection. Initialize Postgres first!";
            logger.severe(message);
            throw new RuntimeException(message);
        }
    }

    /**
     * Returns a prepared statement from the current connection.
     * @param sql
     * @return
     * @throws SQLException
     */
    public static PreparedStatement prepareStatement(String sql) throws SQLException {
        checkDBConnection();
        return dbConn.prepareStatement(sql);
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
    private static void executeCommand(String command, Connection connection) {
        if (connection == null) {
            String message = "Trying to execute commands with null connection.";
            logger.severe(message);
            throw new RuntimeException(message);
        }
        logger.info(String.format("Executing command: %s", command));
        try(Statement st = connection.createStatement()) {
            st.execute(command);
        } catch (SQLException e) {
            logger.severe("Error executing database command: '" + command + "' " +
                    e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reads from the specified resource file
     *
     * @param fileName the file containing SQL commands to execute
     */
    private static void executeSQLResource(String fileName) {
        logger.info("Executing script from resource: " + fileName);
        InputStream is = Postgres.class.getResourceAsStream("/" + fileName);
        executeSQLScript(is);
    }

    /***
     * Executes SQL from the given file.
     */
    public static void executeSQLFile(String fileName)
    {
        logger.info("Executing script from file: " + fileName);
        try {
            InputStream is = new FileInputStream(fileName);
            executeSQLScript(is);
        } catch (IOException e) {
            logger.severe("Error opening file: ");
            e.printStackTrace();
        }
    }

    /***
     * Executes SQL from the given input stream.
     */
    private static void executeSQLScript(InputStream is)
    {
        Scanner s = new Scanner(is);

        String line;
        StringBuilder statement = new StringBuilder();
        while(s.hasNextLine()){
            line = s.nextLine();
            if(line.equals("") || line.equals(" ")) {
                Postgres.executeCommand(statement.toString());
                statement = new StringBuilder();
            } else {
                statement.append(line);
            }
        }
        if (!statement.toString().equals(""))
            Postgres.executeCommand(statement.toString());
    }
}
