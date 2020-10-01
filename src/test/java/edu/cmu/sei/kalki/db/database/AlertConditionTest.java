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
package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import edu.cmu.sei.kalki.db.daos.AlertConditionDAO;
import edu.cmu.sei.kalki.db.models.AlertCondition;
import edu.cmu.sei.kalki.db.models.AlertType;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import edu.cmu.sei.kalki.db.models.DataNode;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class AlertConditionTest extends AUsesDatabase {
    private static DeviceType deviceType;
    private static Device device;
    private static AlertType alertType;
    private static AlertTypeLookup alertTypeLookup;
    /*
        Alert Condition Action Tests
     */

    @Test
    public void testInsertAlertCondition() {
        AlertCondition alertCondition = new AlertCondition(device.getId(), alertTypeLookup.getId(), alertTypeLookup.getVariables());
        alertCondition.insert();
        assertNotEquals(-1, alertCondition.getId());
    }

    @Test
    public void testFindAlertCondition() {
        AlertCondition alertCondition = new AlertCondition(device.getId(), alertTypeLookup.getId(), alertTypeLookup.getVariables());
        alertCondition.insert();

        AlertCondition ac = AlertConditionDAO.findAlertCondition(alertCondition.getId());
        assertEquals(alertCondition.getAlertTypeLookupId(), ac.getAlertTypeLookupId());
        assertEquals(alertCondition.getDeviceId(), ac.getDeviceId());
        assertEquals(alertCondition.getVariables(), ac.getVariables());
    }

    @Test
    public void testFindAllAlertConditions() {
        AlertCondition alertCondition = new AlertCondition(device.getId(), alertTypeLookup.getId(), alertTypeLookup.getVariables());
        alertCondition.insert();
        alertCondition.insert();

        ArrayList<AlertCondition> acList = new ArrayList<AlertCondition>(AlertConditionDAO.findAllAlertConditions());
        assertEquals(2, acList.size());
        assertEquals(alertCondition.getAlertTypeLookupId(), acList.get(1).getAlertTypeLookupId());
        assertEquals(alertCondition.getDeviceId(), acList.get(1).getDeviceId());
        assertEquals(alertCondition.getVariables(), acList.get(1).getVariables());
    }

    @Test
    public void testInsertAlertConditionForDevice(){
        AlertConditionDAO.insertAlertConditionForDevice(device.getId()); //should insert 1
        List<AlertCondition> acList = AlertConditionDAO.findAllAlertConditions();
        assertEquals(1, acList.size());

        AlertConditionDAO.insertAlertConditionForDevice(device.getId()); //should insert 1
        acList = AlertConditionDAO.findAllAlertConditions();
        assertEquals(2, acList.size()); // 1 device, 1 alert type lookup, 2 inserts by device

    }

    @Test
    public void testFindAlertConditionsByDevice() {
        AlertConditionDAO.insertAlertConditionForDevice(device.getId()); //should insert 1
        AlertConditionDAO.insertAlertConditionForDevice(device.getId()); //should insert 1

        List<AlertCondition> acList = AlertConditionDAO.findAlertConditionsByDevice(device.getId());
        assertEquals(1, acList.size()); // should only return newest row
    }

    @Test
    public void testUpdateAlertConditionsForDeviceType() {
        AlertConditionDAO.insertAlertConditionForDevice(device.getId()); //should insert 1

        alertTypeLookup.getVariables().replace("test", "testing");
        alertTypeLookup.insertOrUpdate();

        int result = AlertConditionDAO.updateAlertConditionsForDeviceType(alertTypeLookup);
        assertEquals(1, result); // returns 1 on success

        List<AlertCondition> allAlertConditions = AlertConditionDAO.findAllAlertConditions();
        assertEquals(2, allAlertConditions.size());

        List<AlertCondition> deviceAlertConditions = AlertConditionDAO.findAlertConditionsByDevice(device.getId());
        assertEquals(alertTypeLookup.getVariables().get("test"), deviceAlertConditions.get(0).getVariables().get("test"));
    }

    public void insertData() {
        // insert device_type
        deviceType = new DeviceType(0, "test device type");
        deviceType.insert();

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1, dataNode, "");
        device.insert();

        // insert alert_type unts-temperature
        alertType = new AlertType("UNTS-Temperature", "test alert type", "IoT Monitor");
        alertType.insert();

        // insert into alert_type_lookup
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put("test", "test");
        alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();
    }
}
