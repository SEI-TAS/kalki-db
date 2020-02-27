package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.AlertCondition;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import edu.cmu.sei.kalki.db.models.Device;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlertConditionDAO extends DAO
{
    /**
     * Finds an AlertCondition from the database with the given id
     *
     * @param id The id of the desired AlertCondition
     * @return An AlertCondition with desired id
     */
    public static AlertCondition findAlertCondition(int id) {
        AlertCondition alertCondition = new AlertCondition();
        try(PreparedStatement st = Postgres.prepareStatement("SELECT ac.*, d.name AS device_name, at.name AS alert_type_name " +
                "FROM alert_condition AS ac, device AS d, alert_type AS at, alert_type_lookup as atl " +
                "WHERE ac.id=? AND ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id")) {
            st.setInt(1, id);
            try(ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    alertCondition = (AlertCondition) createFromRs(AlertCondition.class, rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error getting all AlertConditions: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return alertCondition;
    }

    /**
     * Finds all AlertConditions in the database
     *
     * @return a list of AlertCondition
     */
    public static List<AlertCondition> findAllAlertConditions() {
        List<AlertCondition> alertConditionList = new ArrayList<>();
        try(PreparedStatement st = Postgres.prepareStatement("SELECT ac.*, d.name AS device_name, at.name AS alert_type_name " +
                "FROM alert_condition AS ac, device AS d, alert_type AS at, alert_type_lookup as atl " +
                "WHERE ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id")) {
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    alertConditionList.add((AlertCondition) createFromRs(AlertCondition.class, rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error getting all AlertConditions: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return alertConditionList;
    }

    /**
     * Finds most recent AlertConditions from the database for the given device_id
     *
     * @param deviceId an id of a device
     * @return a list of all most recent AlertConditions in the database related to the given device
     */
    public static List<AlertCondition> findAlertConditionsByDevice(int deviceId) {
        List<AlertCondition> conditionList = new ArrayList<>();
        try(PreparedStatement st = Postgres.prepareStatement("SELECT DISTINCT ON (atl.id) alert_type_lookup_id, ac.id, ac.device_id, d.name AS device_name, at.name AS alert_type_name, ac.variables " +
                "FROM alert_condition AS ac, device AS d, alert_type AS at, alert_type_lookup AS atl " +
                "WHERE ac.device_id = ? AND ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id")) {
            st.setInt(1, deviceId);
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    conditionList.add((AlertCondition) createFromRs(AlertCondition.class, rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all alert conditions: ");
            e.printStackTrace();
        }
        return conditionList;
    }

    /**
     * Insert a row into the AlertCondition table
     *
     * @param cond The AlertCondition to be added
     * @return id of new AlertCondition on success. -1 on error
     */
    public static Integer insertAlertCondition(AlertCondition cond) {
        logger.info("Inserting alert condition for device: " + cond.getDeviceId());
        try(PreparedStatement insertAlertCondition = Postgres.prepareStatement("INSERT INTO alert_condition(variables, device_id, alert_type_lookup_id) VALUES (?,?,?)")) {
            insertAlertCondition.setObject(1, cond.getVariables());
            insertAlertCondition.setInt(2, cond.getDeviceId());
            insertAlertCondition.setInt(3, cond.getAlertTypeLookupId());
            insertAlertCondition.executeUpdate();
            return getLatestId("alert_condition");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertCondition: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Insert row(s) into the AlertCondition table based on the given Device's type
     *
     * @param id the Id of the device
     * @return 1 on success. -1 on error
     */
    public static Integer insertAlertConditionForDevice(int id) {
        logger.info("Inserting alert conditions for device: " + id);

        Device d = DeviceDAO.findDevice(id);
        List<AlertTypeLookup> atlList = AlertTypeLookupDAO.findAlertTypeLookupsByDeviceType(d.getType().getId());
        for(AlertTypeLookup atl: atlList){
            AlertCondition ac = new AlertCondition(id, atl.getId(), atl.getVariables());
            ac.insertOrUpdate();
            if(ac.getId()<0) //insert failed
                return -1;
        }

        return 1;
    }

    /**
     * Insert row(s) into the AlertCondition table for devices in type specified on the AlertTypeLookup
     *
     * @param cond The AlertCondition to be added
     * @return id of new AlertCondition on success. -1 on error
     */
    public static Integer updateAlertConditionsForDeviceType(AlertTypeLookup alertTypeLookup) {
        logger.info("Inserting alert conditions for device type: " + alertTypeLookup.getDeviceTypeId());
        try {

            List<Device> deviceList = DeviceDAO.findDevicesByType(alertTypeLookup.getDeviceTypeId());
            if(deviceList != null) {
                for (Device d : deviceList) {
                    try (PreparedStatement insertAlertCondition = Postgres.prepareStatement("INSERT INTO alert_condition(variables, device_id, alert_type_lookup_id) VALUES (?,?,?)")) {
                        insertAlertCondition.setObject(1, alertTypeLookup.getVariables());
                        insertAlertCondition.setInt(2, d.getId());
                        insertAlertCondition.setInt(3, alertTypeLookup.getId());
                        insertAlertCondition.executeUpdate();
                    }
                }
            }

            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertCondition: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Deletes an AlertCondition by its id.
     *
     * @param id id of the AlertCondition to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteAlertCondition(int id) {
        return deleteById("alert_condition", id);
    }

}
