package kalkidb;

import kalkidb.database.Postgres;
import kalkidb.models.Device;

import java.util.List;

public class Test {
    public static void Main(String[] args){
        Postgres.initialize();
        Postgres.resetDatabase();
        System.out.println("Successfully cleared tables in database.");

        Device d = new Device("2", "2", "WeMo Insight", "WeMo Insight", "wemo group",
        "", 20, 50, "filepath");
        d.insert();
        //Start monitors for all existing devices in the database.
        List<Device> devices = Postgres.getAllDevices();
    }
}