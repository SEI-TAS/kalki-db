package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.StageLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class StageLogDAO extends DAO
{
    /**
     * Converts a result set to a StageLog object
     */
    public static StageLog createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int deviceSecurityStateId = rs.getInt("device_sec_state_id");
        Timestamp timestamp = rs.getTimestamp("timestamp");
        String action = rs.getString("action");
        String stage = rs.getString("stage");
        String info = rs.getString("info");
        return new StageLog(id, deviceSecurityStateId, timestamp, action, stage, info);
    }
    
    /**
     * Finds the row in stage_log for the given id
     * @param id
     * @return StageLog representing row with given id
     */
    public static StageLog findStageLog(int id) {
        return (StageLog) findObjectByIdAndTable(id, "stage_log", StageLogDAO.class);
    }

    /**
     * Returns all rows in the stage_log table
     * @return a List of all StageLogs
     */
    public static List<StageLog> findAllStageLogs(){
        String query = "SELECT * FROM stage_log ORDER BY timestamp";
        return (List<StageLog>) findObjectsByQuery(query, StageLogDAO.class);
    }

    /**
     * Returns all rows in the stage_log related to the given device
     * @param deviceId
     * @return a List of StageLogs related to the given device id
     */
    public static List<StageLog> findAllStageLogsForDevice(int deviceId) {
        String query = "SELECT sl.id, sl.device_sec_state_id, sl.timestamp, sl.action, sl.stage, sl.info " +
                "FROM stage_log sl, device_security_state dss " +
                "WHERE dss.device_id=? AND sl.device_sec_state_id=dss.id";
        return (List<StageLog>) findObjectsByIdAndQuery(deviceId, query, StageLogDAO.class);
    }

    /**
     *
     * @return
     */
    public static List<String> findStageLogActions(){
        List<String> actions = new ArrayList<>();
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT action FROM stage_log WHERE stage=?")) {
            st.setString(1, StageLog.Stage.FINISH.convert());
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    actions.add(rs.getString("action"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting all actions that finished in stage_log: "+e.getMessage());
        }
        return actions;

    }

    /**
     * Inserts the given StageLog into the stage_log table
     * @param stageLog
     * @return
     */
    public static int insertStageLog(StageLog stageLog){
        logger.info("Inserting new stage log: "+stageLog.toString());
        int latestId = -1;
        long timestamp = System.currentTimeMillis();
        stageLog.setTimestamp(new Timestamp(timestamp));
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO stage_log (device_sec_state_id, action, stage, info, timestamp) VALUES(?,?,?,?,?) RETURNING id")) {
            st.setInt(1, stageLog.getDeviceSecurityStateId());
            st.setString(2, stageLog.getAction());
            st.setString(3, stageLog.getStage());
            st.setString(4, stageLog.getInfo());
            st.setTimestamp(5, stageLog.getTimestamp());

            st.execute();

            latestId = getLatestId(st);
        } catch (Exception e){
            logger.severe("Error insert StageLog into db: "+e.getClass().getName()+": "+e.getMessage());
            e.printStackTrace();
        }
        return latestId;
    }
    
}
