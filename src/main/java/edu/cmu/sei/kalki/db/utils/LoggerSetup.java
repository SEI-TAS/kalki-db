package edu.cmu.sei.kalki.db.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggerSetup
{
    private static final String DEFAULT_LOGS_FOLDER = "/logs";

    public static void setup() {
        try(InputStream loggingConfigFile = LoggerSetup.class.getClassLoader().getResourceAsStream("logging.properties"))
        {
            // Load config file.
            LogManager.getLogManager().readConfiguration(loggingConfigFile);

            if(Files.notExists(Paths.get(DEFAULT_LOGS_FOLDER))) {
                // Load handlers to trigger lazy loading exception since folder doesn't exist.
                Logger rootLogger = LogManager.getLogManager().getLogger("");
                System.out.println("***NOTE: Ignore FileHandler exception trace, folder for file handler was not found.");
                rootLogger.getHandlers();
            }
        }
        catch (final IOException e)
        {
            throw new IllegalStateException("Could not load default logging.properties file", e);
        }
    }
}
