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

import edu.cmu.sei.kalki.db.daos.AlertTypeDAO;
import edu.cmu.sei.kalki.db.daos.AlertTypeLookupDAO;
import edu.cmu.sei.kalki.db.models.AlertType;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import edu.cmu.sei.kalki.db.models.DataNode;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;

public class AlertTypeLookupTest extends AUsesDatabase {
    private AlertType alertType;
    private DeviceType deviceType;
    private Device device;
    private HashMap<String, String> hmap;

    @Test
    public void testFindAlertTypeLookupBy() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        assertNotEquals(-1, alertTypeLookup.getId());
    }

    @Test
    public void testFindAlertTypeLookupsByDeviceType() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        List<AlertTypeLookup> atlList = AlertTypeLookupDAO.findAlertTypeLookupsByDeviceType(deviceType.getId());
        assertEquals(1, atlList.size());
        assertEquals(deviceType.getId(), atlList.get(0).getDeviceTypeId());
    }

    @Test
    public void testFindAllAlertTypeLookups() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        List<AlertTypeLookup> atlList = AlertTypeLookupDAO.findAllAlertTypeLookups();
        assertEquals(1, atlList.size());
        assertEquals(alertTypeLookup.toString(), atlList.get(0).toString());
    }

    @Test
    public void testInsertAlertTypeLookup() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        assertNotEquals(-1, alertTypeLookup.getId());
    }

    @Test
    public void testUpdateAlertTypeLookup() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        AlertType alertType1 = new AlertType("Name2", "Description", "Source");
        alertType1.insert();

        alertTypeLookup.setAlertTypeId(alertType1.getId());
        AlertTypeLookupDAO.updateAlertTypeLookup(alertTypeLookup);
        Assertions.assertEquals(alertTypeLookup.toString(), AlertTypeLookupDAO.findAlertTypeLookup(alertTypeLookup.getId()).toString());
    }

    @Test
    public void testInsertOrUpdateAlertTypeLookup() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insertOrUpdate();
        assertNotEquals(-1, alertTypeLookup.getId());

        AlertType alertType1 = new AlertType("Name2", "Description", "Source");
        alertType1.insert();

        alertTypeLookup.setAlertTypeId(alertType1.getId());
        alertTypeLookup.insertOrUpdate();
        Assertions.assertEquals(alertTypeLookup.toString(), AlertTypeLookupDAO.findAlertTypeLookup(alertTypeLookup.getId()).toString());
        assertEquals(1, AlertTypeLookupDAO.findAllAlertTypeLookups().size());
    }

    @Test
    public void testFindAlertTypesByDeviceType() {
        AlertTypeLookup alertTypeLookup = new AlertTypeLookup(alertType.getId(), deviceType.getId(), hmap);
        alertTypeLookup.insert();

        List<AlertType> atList = AlertTypeDAO.findAlertTypesByDeviceType(deviceType.getId());
        assertEquals(1, atList.size());
        assertEquals(alertType.toString(), atList.get(0).toString());

        DeviceType deviceType1 = new DeviceType(-1, "Test type 1");
        deviceType1.insert();
        atList = AlertTypeDAO.findAlertTypesByDeviceType(deviceType1.getId());
        assertEquals(0, atList.size());
    }


    public void insertData(){
        deviceType = new DeviceType(-1, "Device Type");
        deviceType.insert();

        alertType = new AlertType("Name", "Description", "Source");
        alertType.insert();

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        device = new Device("Name", "Description", deviceType, "1.1.1.1", 1, 1, dataNode, "");
        device.insert();

        hmap = new HashMap<String, String>();
        hmap.put("test", "test-var");
    }
}
