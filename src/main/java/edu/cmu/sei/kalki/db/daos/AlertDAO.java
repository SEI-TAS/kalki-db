package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.Alert;

import java.sql.Connection;
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
        return (Alert) findObjectByIdAndTable(id, "alert", AlertDAO.class);
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
                    try(Connection con = Postgres.getConnection();
                        PreparedStatement st = con.prepareStatement("SELECT device_id FROM umbox_instance WHERE alerter_id = ?;")) {
                        st.setString(1, alert.getAlerterId());
                        try(ResultSet rs = st.executeQuery()) {
                            if (rs.next()) {
                                deviceId = rs.getInt("device_id");
                                alert.setDeviceId(deviceId);
                            } else {
                                throw new SQLException("Device ID not found for umbox_instance with alerter_id " + alert.getAlerterId());
                            }
                        }
                    }
                }

                try(Connection con = Postgres.getConnection();
                    PreparedStatement st = con.prepareStatement("INSERT INTO alert(name, timestamp, alert_type_id, device_id, alerter_id, info) VALUES (?,?,?,?,?,?) RETURNING id")) {
                    st.setString(1, alert.getName());
                    st.setTimestamp(2, alert.getTimestamp());
                    st.setInt(3, alert.getAlertTypeId());
                    st.setInt(4, deviceId);
                    st.setString(5, alert.getAlerterId());
                    st.setString(6, alert.getInfo());
                    st.execute();
                    return getLatestId(st);
                }
            }
            else {
                if(deviceId == 0) {
                    try(Connection con = Postgres.getConnection();
                        PreparedStatement st = con.prepareStatement("SELECT device_id FROM device_status WHERE id = ?;")) {
                        st.setInt(1, alert.getDeviceStatusId());
                        try(ResultSet rs = st.executeQuery()) {
                            if (rs.next()) {
                                deviceId = rs.getInt("device_id");
                                alert.setDeviceId(deviceId);
                            } else {
                                throw new SQLException("Device ID not found for device_status with id " + alert.getDeviceStatusId());
                            }
                        }
                    }
                }

                try(Connection con = Postgres.getConnection();
                    PreparedStatement st = con.prepareStatement("INSERT INTO alert(name, timestamp, alert_type_id, alerter_id, device_id, device_status_id, info) VALUES (?,?,?,?,?,?,?) RETURNING id")) {
                    st.setString(1, alert.getName());
                    st.setTimestamp(2, alert.getTimestamp());
                    st.setInt(3, alert.getAlertTypeId());
                    st.setString(4, alert.getAlerterId());
                    st.setInt(5, deviceId);
                    st.setInt(6, alert.getDeviceStatusId());
                    st.setString(7, alert.getInfo());
                    st.execute();
                    return getLatestId(st);
                }
            }
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
                try(Connection con = Postgres.getConnection();
                    PreparedStatement st = con.prepareStatement("UPDATE alert " +
                        "SET name = ?, timestamp = ?, alerter_id = ?, device_id = ?, alert_type_id = ?, info = ? " +
                        "WHERE id = ?")) {
                    st.setString(1, alert.getName());
                    st.setTimestamp(2, alert.getTimestamp());
                    st.setString(3, alert.getAlerterId());
                    st.setInt(4, alert.getDeviceId());
                    st.setInt(5, alert.getAlertTypeId());
                    st.setString(6, alert.getInfo());
                    st.setInt(7, alert.getId());
                    st.executeUpdate();
                }
            } else {
                try(Connection con = Postgres.getConnection();
                    PreparedStatement st = con.prepareStatement("UPDATE alert " +
                        "SET name = ?, timestamp = ?, alerter_id = ?, device_status_id = ?, device_id = ?, alert_type_id = ?, info = ?" +
                        "WHERE id = ?")) {
                    st.setString(1, alert.getName());
                    st.setTimestamp(2, alert.getTimestamp());
                    st.setString(3, alert.getAlerterId());
                    st.setInt(4, alert.getDeviceStatusId());
                    st.setInt(5, alert.getDeviceId());
                    st.setInt(6, alert.getAlertTypeId());
                    st.setString(7, alert.getInfo());
                    st.setInt(8, alert.getId());
                    st.executeUpdate();
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
