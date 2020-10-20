/*
 * Kalki - A Software-Defined IoT Security Platform
 * Copyright 2020 Carnegie Mellon University.
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF ANY KIND WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 * Released under a MIT (SEI)-style license, please see license.txt or contact permission@sei.cmu.edu for full terms.
 * [DISTRIBUTION STATEMENT A] This material has been approved for public release and unlimited distribution.  Please see Copyright notice for non-US Government use and distribution.
 * This Software includes and/or makes use of the following Third-Party Software subject to its own license:
 * 1. Google Guava (https://github.com/google/guava) Copyright 2007 The Guava Authors.
 * 2. JSON.simple (https://code.google.com/archive/p/json-simple/) Copyright 2006-2009 Yidong Fang, Chris Nokleberg.
 * 3. JUnit (https://junit.org/junit5/docs/5.0.1/api/overview-summary.html) Copyright 2020 The JUnit Team.
 * 4. Play Framework (https://www.playframework.com/) Copyright 2020 Lightbend Inc..
 * 5. PostgreSQL (https://opensource.org/licenses/postgresql) Copyright 1996-2020 The PostgreSQL Global Development Group.
 * 6. Jackson (https://github.com/FasterXML/jackson-core) Copyright 2013 FasterXML.
 * 7. JSON (https://www.json.org/license.html) Copyright 2002 JSON.org.
 * 8. Apache Commons (https://commons.apache.org/) Copyright 2004 The Apache Software Foundation.
 * 9. RuleBook (https://github.com/deliveredtechnologies/rulebook/blob/develop/LICENSE.txt) Copyright 2020 Delivered Technologies.
 * 10. SLF4J (http://www.slf4j.org/license.html) Copyright 2004-2017 QOS.ch.
 * 11. Eclipse Jetty (https://www.eclipse.org/jetty/licenses.html) Copyright 1995-2020 Mort Bay Consulting Pty Ltd and others..
 * 12. Mockito (https://github.com/mockito/mockito/wiki/License) Copyright 2007 Mockito contributors.
 * 13. SubEtha SMTP (https://github.com/voodoodyne/subethasmtp) Copyright 2006-2007 SubEthaMail.org.
 * 14. JSch - Java Secure Channel (http://www.jcraft.com/jsch/) Copyright 2002-2015 Atsuhiko Yamanaka, JCraft,Inc. .
 * 15. ouimeaux (https://github.com/iancmcc/ouimeaux) Copyright 2014 Ian McCracken.
 * 16. Flask (https://github.com/pallets/flask) Copyright 2010 Pallets.
 * 17. Flask-RESTful (https://github.com/flask-restful/flask-restful) Copyright 2013 Twilio, Inc..
 * 18. libvirt-python (https://github.com/libvirt/libvirt-python) Copyright 2016 RedHat, Fedora project.
 * 19. Requests: HTTP for Humans (https://github.com/psf/requests) Copyright 2019 Kenneth Reitz.
 * 20. netifaces (https://github.com/al45tair/netifaces) Copyright 2007-2018 Alastair Houghton.
 * 21. ipaddress (https://github.com/phihag/ipaddress) Copyright 2001-2014 Python Software Foundation.
 * DM20-0543
 *
 */
package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.DeviceSensor;
import edu.cmu.sei.kalki.db.models.DeviceType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DeviceTypeDAO extends DAO
{
    /**
     * Extract a DeviceType from the result set of a database query.
     */
    public static DeviceType createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        return new DeviceType(id, name);
    }

    /**
     * Finds a DeviceType from the database by its id.
     *
     * @param id id of the DeviceType to find.
     * @return the DeviceType if it exists in the database, else null.
     */
    public static DeviceType findDeviceType(int id) {
        DeviceType type = (DeviceType) findObjectByIdAndTable(id, "device_type", DeviceTypeDAO.class);
        if(type != null)
            type.setSensors(DeviceSensorDAO.findSensorsForDeviceType(type.getId()));
        return type;
    }

    /**
     * Finds all DeviceTypes in the database.
     *
     * @return a list of all DeviceTypes in the database.
     */
    public static List<DeviceType> findAllDeviceTypes() {
        List<DeviceType> deviceTypes = (List<DeviceType>) findObjectsByTable("device_type", DeviceTypeDAO.class);
        for(DeviceType deviceType : deviceTypes) {
            List<DeviceSensor> sensors = DeviceSensorDAO.findSensorsForDeviceType(deviceType.getId());
            deviceType.setSensors(sensors);
        }
        return deviceTypes;
    }

    /**
     * Saves given DeviceType to the database.
     *
     * @param type DeviceType to be inserted.
     * @return auto incremented id
     */
    public static DeviceType insertDeviceType(DeviceType type) {
        logger.info("Inserting DeviceType: " + type.getName());
        try(Connection con = Postgres.getConnection();
        PreparedStatement st = con.prepareStatement
                ("INSERT INTO device_type(name)" +
                        "values(?) RETURNING id")) {
            st.setString(1, type.getName());
            st.execute();
            int id = getLatestId(st);
            type.setId(id);
            DeviceSensorDAO.insertDeviceSensorForDeviceType(type);

        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting DeviceType: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return type;
    }

    /**
     * Updates DeviceType with given id to have the parameters of the given DeviceType.
     * NOTE: does NOT update associated device sensors. This is very inconsistent, but more definitive
     * solution would imply removing sensors as part of the DeviceType object, which would require too many changes.
     * @param type DeviceType holding new parameters to be saved in the database.
     */
    public static DeviceType updateDeviceType(DeviceType type) {
        logger.info("Updating DeviceType with id=" + type.getId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                ("UPDATE device_type SET name = ? " +
                        "WHERE id=?")) {
            st.setString(1, type.getName());
            st.setInt(2, type.getId());
            st.executeUpdate();
            //DeviceSensorDAO.updateDeviceSensorForDeviceType(type);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating DeviceType: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return type;
    }

    /**
     * First, attempts to find the DeviceType in the database.
     * If successful, updates the existing DeviceType with the given DeviceType's parameters Otherwise,
     * inserts the given DeviceType.
     *
     * @param type DeviceType to be inserted or updated.
     */
    public static DeviceType insertOrUpdateDeviceType(DeviceType type) {
        DeviceType dt = findDeviceType(type.getId());
        if (dt == null) {
            return insertDeviceType(type);
        } else {
            return updateDeviceType(type);
        }
    }

    /**
     * Deletes a DeviceType by its id.
     *
     * @param id id of the DeviceType to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteDeviceType(int id) {
        return deleteById("device_type", id);
    }
    
}
