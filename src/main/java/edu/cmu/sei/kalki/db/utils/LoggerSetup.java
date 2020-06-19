package edu.cmu.sei.kalki.db.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

public class LoggerSetup
{
    public static void setup() {
        try(InputStream loggingConfigFile = LoggerSetup.class.getClassLoader().getResourceAsStream("logging.properties"))
        {
            LogManager.getLogManager().readConfiguration(loggingConfigFile);
        }
        catch (final IOException e)
        {
            throw new IllegalStateException("Could not load default logging.properties file", e);
        }
    }
}
