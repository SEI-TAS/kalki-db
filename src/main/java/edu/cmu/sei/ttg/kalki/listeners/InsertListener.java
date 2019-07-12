package edu.cmu.sei.ttg.kalki.listeners;

import edu.cmu.sei.ttg.kalki.database.Postgres;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class InsertListener extends TimerTask
{
    private static InsertListener instance = null;
    private static Logger logger = Logger.getLogger("myLogger");
    private static boolean isListening = false;
    private static Map<String, InsertHandler> handlerMap = new HashMap<>();
    private static Timer timer = null;

    private PGConnection pgconn;

    public static void startListening() {
        if(!isListening) {
            int pollInterval = 1000;
            timer = new Timer();
            timer.schedule(new InsertListener(), pollInterval, pollInterval);
            isListening = true;
        }
    }

    public static void stopListening() {
        timer.cancel();
        isListening = false;
    }

    public static void addHandler(String triggerName, InsertHandler handler) {
        handlerMap.put(triggerName, handler);

        Postgres.executeCommand("LISTEN " + triggerName);

        // Issue a dummy query to contact the backend and receive any pending notifications.
        Postgres.executeCommand("SELECT 1");
    }

    public static void clearHandlers() {
        handlerMap.clear();
    }

    private InsertListener()
    {
        this.pgconn = (PGConnection) Postgres.dbConn;
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
                    InsertHandler handler = handlerMap.get(notification.getName());

                    if(handler != null) {
                        int id = Integer.parseInt(notification.getParameter());
                        handler.handleNewInsertion(id);
                    }
                }
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}