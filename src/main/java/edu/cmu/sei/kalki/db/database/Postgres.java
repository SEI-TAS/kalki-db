package edu.cmu.sei.kalki.db.database;

import edu.cmu.sei.kalki.db.utils.Config;
import org.apache.commons.dbcp2.BasicDataSource;
import org.postgresql.PGConnection;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Postgres {
    private static final String DEFAULT_IP = "localhost";
    private static final String DEFAULT_PORT = "5432";

    private static final String POSTGRES_URL_SCHEMA = "jdbc:postgresql://";

    public static final String TRIGGER_NOTIF_NEW_DEV_SEC_STATE = "devicesecuritystateinsert";
    public static final String TRIGGER_NOTIF_NEW_DEV_STATUS = "devicestatusinsert";
    public static final String TRIGGER_NOTIF_NEW_POLICY_INSTANCE = "policyruleloginsert";
    public static final String TRIGGER_NOTIF_NEW_ALERT = "alerthistoryinsert";

    private static Logger logger = Logger.getLogger(Postgres.class.getName());
    private static String dbIp;
    private static String dbPort;
    private static String dbName;
    private static String dbUser;
    private static String dbPassword;
    private static Postgres postgresInstance = null;

    private static BasicDataSource dataSource;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //    General setup.
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Singleton constructor.
     */
    private Postgres(String ip, String port, String newDbName, String newDbUser, String newDbPassword) {
        dbIp = ip;
        dbPort = port;
        dbName = newDbName;
        dbUser = newDbUser;
        dbPassword = newDbPassword;

        setupDataSource();
    }

    /**
     * Prepares a connection pool.
     * @return
     */
    private static void setupDataSource() {
        String url = POSTGRES_URL_SCHEMA + dbIp + ":" + dbPort + "/" + Postgres.dbName;
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(Postgres.dbUser);
        dataSource.setPassword(Postgres.dbPassword);

        dataSource.setInitialSize(1);
        dataSource.setMaxTotal(10);
    }

    /**
     * Clears up pending connections.
     */
    public static void close() {
        if(dataSource != null) {
            try {
                dataSource.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        postgresInstance = null;
    }

    /**
     * Initialize the singleton assuming the Config object has been loaded.
     */
    public static void initializeFromConfig() {
        // Mandatory params, minimum need to connect to DB.
        String dbName = Config.getValue("db_name");
        String dbUser = Config.getValue("db_user");
        String dbPass = Config.getValue("db_password");

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
     * Returns a valid connection to the server.
     * @return
     */
    public static Connection getConnection() throws SQLException {
        logger.info("Active connections: " + dataSource.getNumActive());
        return dataSource.getConnection();
        //return waitForConnection();
    }

    /**
     * Attempts a DB connection, waiting and retrying if not possible.
     */
    private static Connection waitForConnection() {
        Connection con;
        while((con = establishConnection(dbIp, dbPort)) == null) {
            logger.info("Waiting for DB engine to be available...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
                throw new RuntimeException("Could not connect to DB");
            }
        }

        logger.info("DB connection established.");
        return con;
    }

    /**
     * Connects to the postgres database, allowing for database ops.
     *
     * @return a connection to the database.
     */
    private static Connection establishConnection(String ip, String port) {
        Connection con = null;
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(POSTGRES_URL_SCHEMA + ip + ":" + port + "/" + Postgres.dbName, Postgres.dbUser, Postgres.dbPassword);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            logger.severe("Error connecting to database : " + e.getClass().getName() + ": " + e.getMessage());
        }
        return con;
    }

    /**
     * Resets structure and default data to DB.
     */
    public static void recreateDB(String databaseName, String templateName) throws SQLException {
        logger.info("Resetting database to given template.");
        executeCommand("DROP DATABASE IF EXISTS " + databaseName);
        executeCommand("CREATE DATABASE " + databaseName + " TEMPLATE " + templateName);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //    Generic DB Actions
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the actual Postgres connection object.
     */
    public static PGConnection getPGConnection(Connection con) throws SQLException {
        return con.unwrap(PGConnection.class);
    }

    /**
     * Executes the given SQL command in the database.
     *
     * @param command SQL commmand string
     */
    public static void executeCommand(String command) {
        logger.info(String.format("Executing command: %s", command));
        try(Connection connection = getConnection();
            Statement st = connection.createStatement()) {
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
    private static void executeSQLResource(String fileName) throws SQLException {
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
    private static void executeSQLScript(InputStream is) {
        Scanner scanner = new Scanner(is);
        String line;
        StringBuilder statement = new StringBuilder();
        while(scanner.hasNextLine()){
            line = scanner.nextLine();

            // Ignore comments.
            if(line.startsWith("--")) {
                continue;
            }

            // Check if an empty line is reached.
            if(line.trim().isEmpty()) {
                // When an empty line is found, execute statements accumulated so far and reset.
                Postgres.executeCommand(statement.toString());
                statement = new StringBuilder();
            } else {
                // If the current line was not empty, add it to the accumulated statements.
                statement.append(line);
            }
        }

        // In case script did not end with an empty line, execute remaining lines.
        if (!statement.toString().equals(""))
            Postgres.executeCommand(statement.toString());
    }
}
