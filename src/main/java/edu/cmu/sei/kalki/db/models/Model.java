package edu.cmu.sei.kalki.db.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class Model
{
    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Problem writing object as JSON string";
        }
    }
}
