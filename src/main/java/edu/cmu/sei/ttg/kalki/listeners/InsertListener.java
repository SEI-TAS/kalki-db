package edu.cmu.sei.ttg.kalki.listeners;

import edu.cmu.sei.ttg.kalki.database.Postgres;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class InsertListener extends TimerTask
{
    private static Logger logger = Logger.getLogger("myLogger");

    private PGConnection pgconn;
    private IInsertHandler handler;

    public static void startUpListener(String triggerName, IInsertHandler handler)
    {
        int pollInterval = 1000;
        Timer timer = new Timer();
        timer.schedule(new InsertListener(triggerName, handler), pollInterval, pollInterval);
    }

    private InsertListener(String triggerName, IInsertHandler handler)
    {
        this.handler = handler;
        this.pgconn = (PGConnection) Postgres.dbConn;
        Postgres.executeCommand("LISTEN " + triggerName);

        // Issue a dummy query to contact the backend and receive any pending notifications.
        Postgres.executeCommand("SELECT 1");
    }

    public void run()
    {
        try
        {
            PGNotification notifications[] = pgconn.getNotifications();
            if (notifications != null)
            {
                for (PGNotification notification : notifications)
                {
                    int id = Integer.parseInt(notification.getParameter());
                    logger.info("Detected new item inserted with id " + notification.getParameter());
                    handler.handleNewInsertion(id);
                }
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}