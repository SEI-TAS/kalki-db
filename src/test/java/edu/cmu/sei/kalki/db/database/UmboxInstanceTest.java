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

import edu.cmu.sei.kalki.db.daos.UmboxInstanceDAO;
import edu.cmu.sei.kalki.db.models.DataNode;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.Group;
import edu.cmu.sei.kalki.db.models.UmboxImage;
import edu.cmu.sei.kalki.db.models.UmboxInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class UmboxInstanceTest extends AUsesDatabase {
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static Group group;
    private static Device device;
    private static Device deviceTwo;
    private static UmboxImage umboxImage;
    private static UmboxInstance umboxInstance;

     /*
        Test umbox instance actions
     */

    @Test
    public void testFindUmboxInstance() {
        Assertions.assertEquals(umboxInstance.toString(), UmboxInstanceDAO.findUmboxInstance(umboxInstance.getAlerterId()).toString());
    }

    @Test
    public void testFindUmboxInstances() {
        ArrayList<UmboxInstance> foundInstances =
                new ArrayList<UmboxInstance>(UmboxInstanceDAO.findUmboxInstances(device.getId()));

        assertEquals(1, foundInstances.size());
        assertEquals(umboxInstance.toString(), foundInstances.get(0).toString());
    }

    @Test
    public void testInsertUmboxInstance() {
        UmboxInstance newUmboxInstance = new UmboxInstance("new alerter id", umboxImage.getId(), device.getId());

        Assertions.assertEquals(null, UmboxInstanceDAO.findUmboxInstance(newUmboxInstance.getAlerterId()));

        newUmboxInstance.insert();

        Assertions.assertEquals(newUmboxInstance.toString(),
                UmboxInstanceDAO.findUmboxInstance(newUmboxInstance.getAlerterId()).toString());
    }

    @Test
    public void testUpdateUmboxInstance() {
        Assertions.assertEquals(umboxInstance.getAlerterId(),
                UmboxInstanceDAO.findUmboxInstance(umboxInstance.getAlerterId()).getAlerterId());

        umboxInstance.setAlerterId("changed alerter id");

        UmboxInstanceDAO.updateUmboxInstance(umboxInstance);

        Assertions.assertEquals(umboxInstance.getAlerterId(),
                UmboxInstanceDAO.findUmboxInstance(umboxInstance.getAlerterId()).getAlerterId());
    }

    @Test
    public void testDeleteUmboxInstance() {
        Assertions.assertEquals(umboxInstance.toString(), UmboxInstanceDAO.findUmboxInstance(umboxInstance.getAlerterId()).toString());
        UmboxInstanceDAO.deleteUmboxInstance(umboxInstance.getId());
        Assertions.assertEquals(null, UmboxInstanceDAO.findUmboxInstance(umboxInstance.getAlerterId()));
    }

    public void insertData() {
        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        // insert device_type
        deviceType = new DeviceType(0, "Udoo Neo");
        deviceType.insert();

        deviceTypeTwo = new DeviceType(0, "test device type");
        deviceTypeTwo.insert();

        // insert Group
        group = new Group("Test Group");
        group.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1, dataNode);
        device.insert();

        deviceTwo = new Device("Device 2", "this is also a test device", deviceTypeTwo.getId(), group.getId(), "0.0.0.1", 1, 1, 1, dataNode.getId());
        deviceTwo.insert();

        // insert umbox_image
        umboxImage = new UmboxImage("UmboxImage", "path/to/image");
        umboxImage.insert();

        // insert umbox_instance from umbox_lookup
        umboxInstance = new UmboxInstance("testing123", umboxImage.getId(), device.getId());
        umboxInstance.insert();
    }
}
