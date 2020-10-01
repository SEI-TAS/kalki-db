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

    public static final String TRIGGER_NOTIF_NEW_DEVICE = "deviceinsert";
    public static final String TRIGGER_NOTIF_UPDATE_DEVICE = "deviceupdate";
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
    public static void cleanup() {
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

        // Optional parameters, only use if they are there.
        String dbHost = Config.getValue("db_host");
        if(dbHost == null) {
            dbHost = DEFAULT_IP;
        }
        String dbPort = Config.getValue("db_port");
        if(dbPort == null) {
            dbPort = DEFAULT_PORT;
        }

        Postgres.initialize(dbHost, dbPort, dbName, dbUser, dbPass);
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
        //logger.info("Active connections: " + dataSource.getNumActive());
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
        // make sure no new connections can be made
        executeCommand("UPDATE pg_database SET datallowconn = 'false' WHERE datname = '"+databaseName+"'");

        // force disconnection of all clients connected
        executeCommand("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '"+databaseName+"'");

        // drop the database
        executeCommand("DROP DATABASE IF EXISTS " + databaseName);

        // create new db
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
