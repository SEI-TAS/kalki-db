package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.Alert;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO extends DAO
{
    /**
     * Extract an Alert from the result set of a database query.
     *
     * @param rs ResultSet from a Alert query.
     * @return The Alert that was found.
     */
    public static Alert createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        Timestamp timestamp = rs.getTimestamp("timestamp");
        String alerterId = rs.getString("alerter_id");
        int deviceStatusId = rs.getInt("device_status_id");
        int alertTypeId = rs.getInt("alert_type_id");
        int deviceId = rs.getInt("device_id");
        String info = rs.getString("info");
        return new Alert(id, name, timestamp, alerterId, deviceId, deviceStatusId, alertTypeId, info);
    }

    /**
     * Finds an Alert from the database with the given id.
     */
    public static Alert findAlert(int id) {
        return (Alert) findObject(id, "alert", AlertDAO.class);
    }

    /**
     * Finds all Alerts from the database for the given list of UmboxInstance alerterIds.
     *
     * @param alerterIds a list of alerterIds of UmboxInstances.
     * @return a list of all Alerts in the database where the the alert was created by a UmboxInstance with
     * alerterId in alerterIds.
     */
    public static List<Alert> findAlerts(List<String> alerterIds) {
        return (List<Alert>) findObjectsByStringIds(alerterIds, "alert", "alerter_id", AlertDAO.class);
    }

    /**
     * Finds all Alerts from the database for the given deviceId.
     *
     * @param id of the device
     * @return a list of all Alerts in the database associated to the device with the given id
     */
    public static List<Alert> findAlertsByDevice(int deviceId) {
        List<Integer> deviceIds = new ArrayList<>();
        deviceIds.add(deviceId);
        return (List<Alert>) findObjectsByIntIds(deviceIds, "alert", "device_id", AlertDAO.class);
    }

    /**
     * Insert a row into the alert table
     * Will insert the alert with either an alerterId or deviceStatusId, but not both
     *
     * @param alert The Alert to be added
     * @return id of new Alert on success. -1 on error
     */
    public static Integer insertAlert(Alert alert) {
        logger.info("Inserting alert: " + alert.toString());

        try {
            int deviceId = alert.getDeviceId();
            if(alert.getDeviceStatusId() == 0) {
                if(deviceId == 0) {
                    try(PreparedStatement findDeviceId = Postgres.prepareStatement("SELECT device_id FROM umbox_instance WHERE alerter_id = ?;")) {
                        findDeviceId.setString(1, alert.getAlerterId());
                        try(ResultSet rs = findDeviceId.executeQuery()) {
                            if (rs.next()) {
                                deviceId = rs.getInt("device_id");
                                alert.setDeviceId(deviceId);
                            } else {
                                throw new SQLException("Device ID not found for umbox_instance with alerter_id " + alert.getAlerterId());
                            }
                        }
                    }
                }

                try(PreparedStatement insertAlert = Postgres.prepareStatement("INSERT INTO alert(name, timestamp, alert_type_id, device_id, alerter_id, info) VALUES (?,?,?,?,?,?);")) {
                    insertAlert.setString(1, alert.getName());
                    insertAlert.setTimestamp(2, alert.getTimestamp());
                    insertAlert.setInt(3, alert.getAlertTypeId());
                    insertAlert.setInt(4, deviceId);
                    insertAlert.setString(5, alert.getAlerterId());
                    insertAlert.setString(6, alert.getInfo());
                    insertAlert.executeUpdate();
                }
            }
            else {
                if(deviceId == 0) {
                    try(PreparedStatement findDeviceId = Postgres.prepareStatement("SELECT device_id FROM device_status WHERE id = ?;")) {
                        findDeviceId.setInt(1, alert.getDeviceStatusId());
                        try(ResultSet rs = findDeviceId.executeQuery()) {
                            if (rs.next()) {
                                deviceId = rs.getInt("device_id");
                                alert.setDeviceId(deviceId);
                            } else {
                                throw new SQLException("Device ID not found for device_status with id " + alert.getDeviceStatusId());
                            }
                        }
                    }
                }

                try(PreparedStatement insertAlert = Postgres.prepareStatement("INSERT INTO alert(name, timestamp, alert_type_id, alerter_id, device_id, device_status_id, info) VALUES (?,?,?,?,?,?,?);")) {
                    insertAlert.setString(1, alert.getName());
                    insertAlert.setTimestamp(2, alert.getTimestamp());
                    insertAlert.setInt(3, alert.getAlertTypeId());
                    insertAlert.setString(4, alert.getAlerterId());
                    insertAlert.setInt(5, deviceId);
                    insertAlert.setInt(6, alert.getDeviceStatusId());
                    insertAlert.setString(7, alert.getInfo());
                    insertAlert.executeUpdate();
                }
            }

            return getLatestId("alert");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting Alert: " + alert.toString() + " " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates Alert with given id to have the parameters of the given Alert.
     *
     * @param alert Alert holding new parameters to be saved in the database.
     * @return the id of the updated Alert on success. -1 on failure
     */
    public static Integer updateAlert(Alert alert) {
        logger.info(String.format("Updating Alert with id = %d with values: %s", alert.getId(), alert));

        try {
            if (alert.getDeviceStatusId() == 0) {
                try(PreparedStatement update = Postgres.prepareStatement("UPDATE alert " +
                        "SET name = ?, timestamp = ?, alerter_id = ?, device_id = ?, alert_type_id = ?, info = ? " +
                        "WHERE id = ?")) {
                    update.setString(1, alert.getName());
                    update.setTimestamp(2, alert.getTimestamp());
                    update.setString(3, alert.getAlerterId());
                    update.setInt(4, alert.getDeviceId());
                    update.setInt(5, alert.getAlertTypeId());
                    update.setString(6, alert.getInfo());
                    update.setInt(7, alert.getId());
                    update.executeUpdate();
                }
            } else {
                try(PreparedStatement update = Postgres.prepareStatement("UPDATE alert " +
                        "SET name = ?, timestamp = ?, alerter_id = ?, device_status_id = ?, device_id = ?, alert_type_id = ?, info = ?" +
                        "WHERE id = ?")) {
                    update.setString(1, alert.getName());
                    update.setTimestamp(2, alert.getTimestamp());
                    update.setString(3, alert.getAlerterId());
                    update.setInt(4, alert.getDeviceStatusId());
                    update.setInt(5, alert.getDeviceId());
                    update.setInt(6, alert.getAlertTypeId());
                    update.setString(7, alert.getInfo());
                    update.setInt(8, alert.getId());
                    update.executeUpdate();
                }
            }

            return alert.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating Alert: " + e.getClass().toString() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Deletes an Alert by its id.
     *
     * @param id id of the Alert to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteAlert(int id) {
        return deleteById("alert", id);
    }

}
