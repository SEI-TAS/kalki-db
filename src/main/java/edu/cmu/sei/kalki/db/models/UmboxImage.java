package edu.cmu.sei.kalki.db.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.cmu.sei.kalki.db.database.Postgres;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UmboxImage {

    private int id;
    private String name;
    private String fileName;
    private Integer dagOrder;
    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public UmboxImage() {

    }

    public UmboxImage(String name, String fileName){
        this.name = name;
        this.fileName = fileName;
    }

    public UmboxImage(int id, String name, String fileName){
        this.id = id;
        this.name = name;
        this.fileName = fileName;
        this.dagOrder = null;
    }

    public UmboxImage(int id, String name, String fileName, Integer dagOrder) {
        this.id = id;
        this.name = name;
        this.fileName = fileName;
        this.dagOrder = dagOrder;
    }

    /**
     * Extract a UmboxImage from the result set of a database query that includes umbox_lookup.
     */
    public static UmboxImage createFromRs(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String fileName = rs.getString("file_name");
        int dagOrder = rs.getInt("dag_order");
        return new UmboxImage(id, name, fileName, dagOrder);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getDagOrder() {
        return dagOrder;
    }

    public void setDagOrder(Integer dagOrder) {
        this.dagOrder = dagOrder;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad umbox image";
        }
    }

    public Integer insert() {
        this.id = Postgres.insertUmboxImage(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
        this.id = Postgres.insertOrUpdateUmboxImage(this);
        return this.id;
    }
}
