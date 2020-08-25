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

import edu.cmu.sei.kalki.db.daos.DeviceCommandLookupDAO;
import edu.cmu.sei.kalki.db.models.DataNode;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceCommand;
import edu.cmu.sei.kalki.db.models.DeviceCommandLookup;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.PolicyCondition;
import edu.cmu.sei.kalki.db.models.PolicyRule;
import edu.cmu.sei.kalki.db.models.SecurityState;
import edu.cmu.sei.kalki.db.models.StateTransition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class CommandLookupTest extends AUsesDatabase {
    private static SecurityState securityState;
    private static SecurityState securityStateTwo;
    private static StateTransition stateTransition;
    private static StateTransition stateTransitionTwo;
    private static PolicyCondition policyCondition;
    private static PolicyRule policyRuleOne;
    private static PolicyRule policyRuleTwo;
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static DeviceCommand deviceCommand;
    private static DeviceCommand deviceCommandTwo;
    private static DeviceCommandLookup deviceCommandLookup;
    private static DeviceCommandLookup deviceCommandLookupTwo;
    private static Device device;

    /*
        Command Lookup Action Tests
     */

    @Test
    public void testFindAllCommandLookups() {
        assertEquals(2, DeviceCommandLookupDAO.findAllCommandLookups().size());
    }

    @Test
    public void testFindCommandLookupsByDevice() {
        ArrayList<DeviceCommandLookup> foundLookups =
                new ArrayList<DeviceCommandLookup>(DeviceCommandLookupDAO.findCommandLookupsByDevice(device.getId()));

        assertEquals(1, foundLookups.size());
        assertEquals(deviceCommandLookup.toString(), foundLookups.get(0).toString());
    }

    @Test
    public void testFindCommandLookup() {
        Assertions.assertEquals(deviceCommandLookup.toString(),
                DeviceCommandLookupDAO.findCommandLookup(deviceCommandLookup.getId()).toString());
    }

    @Test
    public void testInsertOrUpdateCommandLookup() {
        assertEquals(2, DeviceCommandLookupDAO.findAllCommandLookups().size());

        deviceCommandLookup.setPolicyRuleId(policyRuleTwo.getId());
        deviceCommandLookup.insertOrUpdate();

        Assertions.assertEquals(deviceCommandLookup.getPolicyRuleId(),
                DeviceCommandLookupDAO.findCommandLookup(deviceCommandLookup.getId()).getPolicyRuleId());
        assertEquals(2, DeviceCommandLookupDAO.findAllCommandLookups().size());

        DeviceCommandLookup newLookup =
                new DeviceCommandLookup(deviceCommand.getId(), policyRuleOne.getId());

        int newId = newLookup.insertOrUpdate();

        assertEquals(3, DeviceCommandLookupDAO.findAllCommandLookups().size());
        Assertions.assertEquals(newLookup.toString(), DeviceCommandLookupDAO.findCommandLookup(newId).toString());
    }

    @Test
    public void testDeleteCommandLookup() {
        Assertions.assertEquals(deviceCommandLookup.toString(),
                DeviceCommandLookupDAO.findCommandLookup(deviceCommandLookup.getId()).toString());

        DeviceCommandLookupDAO.deleteCommandLookup(deviceCommandLookup.getId());

        Assertions.assertEquals(null, DeviceCommandLookupDAO.findCommandLookup(deviceCommandLookup.getId()));
    }

    public void insertData() {
        // insert security state(s)
        securityState = new SecurityState("Normal");
        securityState.insert();

        securityStateTwo = new SecurityState("Suspicious");
        securityStateTwo.insert();

        stateTransition = new StateTransition(securityState.getId(), securityStateTwo.getId());
        stateTransition.insert();

        stateTransitionTwo  = new StateTransition(securityStateTwo.getId(), securityState.getId());
        stateTransitionTwo.insert();

        // insert device_type
        deviceType = new DeviceType(0, "Udoo Neo");
        deviceType.insert();

        deviceTypeTwo = new DeviceType(0, "test device type");
        deviceTypeTwo.insert();

        // insert policy related objects
        policyCondition = new PolicyCondition(1, null);
        policyCondition.insert();

        policyRuleOne = new PolicyRule(stateTransition.getId(), policyCondition.getId(), deviceType.getId(), 1);
        policyRuleOne.insert();

        policyRuleTwo = new PolicyRule(stateTransitionTwo.getId(), policyCondition.getId(), deviceType.getId(), 1);
        policyRuleTwo.insert();

        // insert command_lookups
        deviceCommand = new DeviceCommand("Test Command", deviceType.getId());
        deviceCommand.insert();

        deviceCommandTwo = new DeviceCommand("Test Command two", deviceTypeTwo.getId());
        deviceCommandTwo.insert();

        deviceCommandLookup = new DeviceCommandLookup(deviceCommand.getId(), policyRuleOne.getId());
        deviceCommandLookup.insert();

        deviceCommandLookupTwo = new DeviceCommandLookup(deviceCommandTwo.getId(), policyRuleOne.getId());
        deviceCommandLookupTwo.insert();

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1, dataNode, "");
        device.insert();
    }
}
