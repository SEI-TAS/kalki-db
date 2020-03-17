package edu.cmu.sei.kalki.db.listeners;

import edu.cmu.sei.kalki.db.database.Postgres;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class InsertListener extends TimerTask
{
    private static boolean isListening = false;
    private static Map<String, InsertHandler> handlerMap = new HashMap<>();
    private static Timer timer = null;

    public static void startListening() {
        if(!isListening) {
            int pollInterval = 1000;
            timer = new Timer();
            timer.schedule(new InsertListener(), pollInterval, pollInterval);
            isListening = true;
        }
    }

    public static void stopListening() {
        for(String triggerName : handlerMap.keySet()) {
            Postgres.executeCommand("UNLISTEN " + triggerName);
        }

        if(timer != null) {
            timer.cancel();
        }

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

    public void run() {
        try(Connection con = Postgres.getConnection()) {
            PGConnection pgConn = Postgres.getPGConnection(con);
            PGNotification notifications[] = pgConn.getNotifications();
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