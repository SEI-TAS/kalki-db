package edu.cmu.sei.kalki.db.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CommandExecutor
{
    protected static final Logger logger = Logger.getLogger(CommandExecutor.class.getName());

    /**
     * Executes the given external command.
     * @param commandAndParams
     */
    public static List<String> executeCommand(List<String> commandAndParams, String workingDir)
    {
        logger.info("Command will be executed from dir: " + workingDir);

        List<String> outputs = new ArrayList<>();
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        processBuilder.command(commandAndParams);
        processBuilder.directory(new File(workingDir));

        try
        {
            logger.info("Executing command: " + commandAndParams.toString());
            Process process = processBuilder.start();
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            logger.info("Command output: ");
            String line;
            while((line = outputReader.readLine()) != null)
            {
                outputs.add(line);
                logger.info(line);
            }

            int exitVal = process.waitFor();
            if(exitVal == 0)
            {
                logger.info("Command execution returned successfully.");
            }
            else
            {
                logger.info("Command execution returned unsuccessfully.");
                throw new RuntimeException("Command was not executed successfully!");
            }

        }
        catch(IOException | InterruptedException e)
        {
            e.printStackTrace();
        }

        return outputs;
    }
}
