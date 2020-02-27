package edu.cmu.sei.kalki.db.dashboard;

import edu.cmu.sei.kalki.db.daos.AlertDAO;
import edu.cmu.sei.kalki.db.daos.DeviceSecurityStateDAO;
import edu.cmu.sei.kalki.db.daos.DeviceStatusDAO;
import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.listeners.InsertListener;
import edu.cmu.sei.kalki.db.models.Alert;
import edu.cmu.sei.kalki.db.models.DeviceSecurityState;
import edu.cmu.sei.kalki.db.models.DeviceStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Used by Dashboard to get new ids.
public class NotificationStorage
{
    private static final List<Integer> newStateIds = Collections.synchronizedList(new ArrayList<>());
    private static final List<Integer> newAlertIds = Collections.synchronizedList(new ArrayList<>());
    private static final List<Integer> newStatusIds = Collections.synchronizedList(new ArrayList<>());

    /**
     * Start up a notification listener.  This will clear all current handlers and
     * current list of newIds
     */
    public static void startListener() {
        InsertListener.startListening();
        InsertListener.clearHandlers();

        InsertListener.addHandler(Postgres.TRIGGER_NOTIF_NEW_ALERT, new AlertHandler());
        InsertListener.addHandler(Postgres.TRIGGER_NOTIF_NEW_DEV_SEC_STATE, new StateHandler());
        InsertListener.addHandler(Postgres.TRIGGER_NOTIF_NEW_DEV_STATUS, new StatusHandler());

        newAlertIds.clear();
        newStateIds.clear();
        newStatusIds.clear();
    }

    public static void stopListener() {
        InsertListener.stopListening();
    }

    /**
     * adds a given alert id to the list of new alert ids to be given to the dashboard
     * @param newId
     */
    public static void newAlertId(int newId) {
        newAlertIds.add(newId);
    }

    /**
     * adds a given state id to the list of new state ids to be given to the dashboard
     * @param newId
     */
    public static void newStateId(int newId) {
        newStateIds.add(newId);
    }

    /**
     * adds a given status id to the list of new status ids to be given to the dashboard
     * @param newId
     */
    public static void newStatusId(int newId) {
        newStatusIds.add(newId);
    }

    /**
     * return the latest alerts unless there are no alerts and then returns null
     *
     * @return the next new alert to be given to the dashboard in the queue
     */
    public static List<Alert> getNewAlerts() {
        if(newAlertIds.size() != 0) {
            List<Alert> newAlerts = new ArrayList<>();
            synchronized (newAlertIds) {
                for(int alertId: newAlertIds) {
                    Alert newAlert = AlertDAO.findAlert(alertId);
                    newAlerts.add(newAlert);
                }
                newAlertIds.clear();
            }

            return newAlerts;
        } else {
            return null;
        }
    }

    /**
     * return the latest states unless there are no states and then returns null
     *
     * @return the next new device security state to be given to the dashboard in the queue
     */
    public static List<DeviceSecurityState> getNewStates() {
        if(newStateIds.size() != 0) {
            List<DeviceSecurityState> newStates = new ArrayList<>();
            synchronized (newStateIds) {
                for(int stateId: newStateIds) {
                    DeviceSecurityState newState = DeviceSecurityStateDAO.findDeviceSecurityState(stateId);
                    newStates.add(newState);
                }
                newStateIds.clear();
            }

            return newStates;
        } else {
            return null;
        }
    }

    /**
     * return the latest statuses unless there are no statuses and then returns null
     *
     * @return the next new device security state to be given to the dashboard in the queue
     */
    public static List<DeviceStatus> getNewStatuses() {
        if(newStatusIds.size() != 0) {
            List<DeviceStatus> newStatuses = new ArrayList<>();
            synchronized (newStatusIds) {
                for(int statusId: newStatusIds) {
                    DeviceStatus newStatus = DeviceStatusDAO.findDeviceStatus(statusId);
                    newStatuses.add(newStatus);
                }
                newStatusIds.clear();
            }

            return newStatuses;
        } else {
            return null;
        }
    }
}
