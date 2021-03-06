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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.cmu.sei.kalki.db.daos.AlertTypeDAO;
import edu.cmu.sei.kalki.db.models.AlertType;
import edu.cmu.sei.kalki.db.models.DeviceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class AlertTypeTest extends AUsesDatabase {
    private static DeviceType deviceType;

    /*
        Alert Type Action Tests
     */
    @Test
    public void testInsertAlertType() {
        AlertType at = new AlertType("Name", "Description", "source");
        at.insert();
        assertNotEquals(-1, at.getId());
    }

    @Test
    public void testFindAlertType() {
        AlertType alertType = new AlertType("Name", "Description", "source");
        alertType.insert();

        AlertType test = AlertTypeDAO.findAlertType(alertType.getId());
        assertNotNull(test);
        assertEquals(alertType.toString(), test.toString());
    }

    @Test
    public void testFindAllAlertTypes() {
        int initialSize = AlertTypeDAO.findAllAlertTypes().size();

        AlertType alertType = new AlertType("Name", "Description", "source");
        alertType.insert();
        List<AlertType> alertTypeList = new ArrayList<AlertType>(AlertTypeDAO.findAllAlertTypes());

        assertEquals(initialSize + 1, alertTypeList.size());
        assertEquals(alertType.toString(), alertTypeList.get(initialSize + 1 - 1).toString());
    }

    @Test
    public void testUpdateAlertType() {
        AlertType alertType = new AlertType("Name", "Description", "source");
        alertType.insert();

        alertType.setDescription("new description");
        AlertTypeDAO.updateAlertType(alertType);

        Assertions.assertEquals(alertType.toString(), AlertTypeDAO.findAlertType(alertType.getId()).toString());
    }

    @Test
    public void testInsertOrUpdateAlertType() {
        int initialSize = AlertTypeDAO.findAllAlertTypes().size();
        AlertType alertType = new AlertType("Name", "Description", "source");
        alertType.insert();

        assertNotEquals(-1, alertType.getId());
        assertEquals(initialSize + 1, AlertTypeDAO.findAllAlertTypes().size());

        alertType.setDescription("new description");
        alertType.insertOrUpdate();

        assertEquals(initialSize + 1, AlertTypeDAO.findAllAlertTypes().size());
        Assertions.assertEquals(alertType.toString(), AlertTypeDAO.findAlertType(alertType.getId()).toString());

        AlertType newAlertType = new AlertType("AlertType2", "test alert type 2", "IoT Monitor");
        newAlertType.insertOrUpdate();
        assertNotEquals(-1, newAlertType.getId());
        assertEquals(initialSize + 2, AlertTypeDAO.findAllAlertTypes().size());
        Assertions.assertEquals(newAlertType.toString(), AlertTypeDAO.findAlertType(newAlertType.getId()).toString());
    }

    public void insertData() {
        //insert device type
        deviceType = new DeviceType(-1, "testDeviceType");
        deviceType.insert();
    }
}
