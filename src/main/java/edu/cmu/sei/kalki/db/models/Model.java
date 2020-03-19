package edu.cmu.sei.kalki.db.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Contains common model parts.
 */
public abstract class Model
{
    protected int id;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Problem writing object as JSON string";
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public abstract int insert();
}
