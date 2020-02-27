package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.UmboxLog;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UmboxLogDAO extends DAO
{

    /**
     * Converts a row from the umbox_log table to a UmboxLog object
     */
    public static UmboxLog createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String alerterId = rs.getString("alerter_id");
        String details = rs.getString("details");
        Timestamp timestamp = rs.getTimestamp("timestamp");
        return new UmboxLog(id, alerterId, details, timestamp);
    }

    /**
     * Finds the row in umbox_log table with given id
     * @param id
     * @return UmboxLog object representing row; Null if an exception is thrown
     */
    public static UmboxLog findUmboxLog(int id){
        logger.info("Finding UmboxLog with id = "+id);
        ResultSet rs = findById(id, "umbox_log");
        UmboxLog umboxLog = null;
        try {
            umboxLog = createFromRs(rs);
        } catch (SQLException e) {
            logger.severe("Sql exception creating object");
            e.printStackTrace();
        }
        closeResources(rs);
        return umboxLog;
    }

    /**
     * Returns all rows from the umbox_log table
     * @return List of UmboxLogs in the umbox_log table
     */
    public static List<UmboxLog> findAllUmboxLogs() {
        logger.info("Finding all UmboxLogs");
        List<UmboxLog> umboxLogList = new ArrayList<>();
        try(PreparedStatement st = Postgres.prepareStatement("SELECT * FROM umbox_log")) {
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    umboxLogList.add(createFromRs(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Exception finding all UmboxLogs "+e.getClass().getName() + ": "+e.getMessage());
            e.printStackTrace();
        }
        return umboxLogList;
    }

    /**
     * Finds rows in the umbox_log table with the given alerter_id
     * @param alerter_id
     * @return List of UmboxLogs with given alerter_id
     *
     */
    public static List<UmboxLog> findAllUmboxLogsForAlerterId(String alerter_id) {
        logger.info("Finding UmboxLogs with alerter_id: "+alerter_id);
        List<UmboxLog> umboxLogList = new ArrayList<>();
        try(PreparedStatement st = Postgres.prepareStatement("SELECT * FROM umbox_log WHERE alerter_id = ?")) {
            st.setString(1, alerter_id);
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    umboxLogList.add(createFromRs(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Exception finding UmboxLogs for alerter_id="+alerter_id+"; "+e.getClass().getName() + ": "+e.getMessage());
            e.printStackTrace();
        }
        return umboxLogList;
    }

    public static List<UmboxLog> findAllUmboxLogsForDevice(int deviceId) {
        logger.info("Finding UmboxLogs for device: "+deviceId);
        List<UmboxLog> logList = new ArrayList<>();
        try(PreparedStatement st = Postgres.prepareStatement("SELECT log.* FROM umbox_log AS log, umbox_instance AS inst WHERE " +
                "inst.device_id = ? AND inst.alerter_id = log.alerter_id " +
                "ORDER BY log.id DESC")) {
            st.setInt(1, deviceId);
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    logList.add(createFromRs(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Exception finding UmboxLogs for device: "+deviceId+"; "+e.getMessage());
            e.printStackTrace();
        }
        return logList;
    }

    /**
     * Inserts the given UmboxLog into the umbox_log table
     * @param umboxLog
     * @return the id of the inserted row
     */
    public static int insertUmboxLog(UmboxLog umboxLog){
        logger.info("Inserting new UmboxLog: "+umboxLog.toString());
        int latestId = -1;
        try(PreparedStatement st = Postgres.prepareStatement("INSERT INTO umbox_log (alerter_id, details) VALUES(?,?)")) {
            st.setString(1, umboxLog.getAlerterId());
            st.setString(2, umboxLog.getDetails());
            st.executeUpdate();
            latestId = getLatestId("umbox_log");
        } catch (Exception e){
            logger.severe("Error insert UmboxLog into db: "+e.getClass().getName()+": "+e.getMessage());
            e.printStackTrace();
        }
        return latestId;
    }
    
}
