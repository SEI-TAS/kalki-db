package edu.cmu.sei.kalki.db.models;

import java.sql.Timestamp;

import edu.cmu.sei.kalki.db.daos.UmboxLogDAO;

public class UmboxLog extends Model  {
    private String alerter_id;
    private String details;
    private Timestamp timestamp;

    public UmboxLog() {}

    public UmboxLog(String alerter_id, String details){
        this.alerter_id = alerter_id;
        this.details = details;
    }

    public UmboxLog(int id, String alerter_id, String details, Timestamp timestamp) {
        this.id = id;
        this.alerter_id = alerter_id;
        this.details = details;
        this.timestamp = timestamp;
    }

    public String getAlerterId() {
        return alerter_id;
    }

    public void setAlerterId(String alerter_id) {
        this.alerter_id = alerter_id;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int insert() {
        this.id = UmboxLogDAO.insertUmboxLog(this);

        UmboxLog temp = UmboxLogDAO.findUmboxLog(this.id);
        this.timestamp = temp.getTimestamp();

        return this.id;
    }
}

