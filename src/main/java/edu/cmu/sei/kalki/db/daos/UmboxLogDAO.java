package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.UmboxLog;

import java.sql.Connection;
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
        return (UmboxLog) findObjectByIdAndTable(id, "umbox_log", UmboxLogDAO.class);
    }

    /**
     * Returns all rows from the umbox_log table
     * @return List of UmboxLogs in the umbox_log table
     */
    public static List<UmboxLog> findAllUmboxLogs() {
        return (List<UmboxLog>) findObjectsByTable("umbox_log", UmboxLogDAO.class);
    }

    /**
     * Finds rows in the umbox_log table with the given alerter_id
     * @param alerter_id
     * @return List of UmboxLogs with given alerter_id
     *
     */
    public static List<UmboxLog> findAllUmboxLogsForAlerterId(String alerterId) {
        List<String> alerterIds = new ArrayList<>();
        alerterIds.add(alerterId);
        return (List<UmboxLog>) findObjectsByStringIds(alerterIds, "umbox_log", "alerter_id", UmboxLogDAO.class);
    }

    public static List<UmboxLog> findAllUmboxLogsForDevice(int deviceId) {
        String query = "SELECT log.* FROM umbox_log AS log, umbox_instance AS inst WHERE " +
                "inst.device_id = ? AND inst.alerter_id = log.alerter_id " +
                "ORDER BY log.id DESC";
        return (List<UmboxLog>) findObjectsByIdAndQuery(deviceId, query, UmboxLogDAO.class);
    }

    /**
     * Inserts the given UmboxLog into the umbox_log table
     * @param umboxLog
     * @return the id of the inserted row
     */
    public static int insertUmboxLog(UmboxLog umboxLog){
        logger.info("Inserting new UmboxLog: "+umboxLog.toString());
        int latestId = -1;
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO umbox_log (alerter_id, details) VALUES(?,?) RETURNING id")) {
            st.setString(1, umboxLog.getAlerterId());
            st.setString(2, umboxLog.getDetails());
            st.execute();
            latestId = getLatestId(st);
        } catch (Exception e){
            logger.severe("Error insert UmboxLog into db: "+e.getClass().getName()+": "+e.getMessage());
            e.printStackTrace();
        }
        return latestId;
    }
    
}
