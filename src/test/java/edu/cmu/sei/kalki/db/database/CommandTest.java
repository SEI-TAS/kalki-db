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

import edu.cmu.sei.kalki.db.daos.DeviceCommandDAO;
import edu.cmu.sei.kalki.db.daos.DeviceCommandLookupDAO;
import edu.cmu.sei.kalki.db.daos.SecurityStateDAO;
import edu.cmu.sei.kalki.db.models.DataNode;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceCommand;
import edu.cmu.sei.kalki.db.models.DeviceCommandLookup;
import edu.cmu.sei.kalki.db.models.DeviceSecurityState;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.PolicyCondition;
import edu.cmu.sei.kalki.db.models.PolicyRule;
import edu.cmu.sei.kalki.db.models.PolicyRuleLog;
import edu.cmu.sei.kalki.db.models.SecurityState;
import edu.cmu.sei.kalki.db.models.StateTransition;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class CommandTest extends AUsesDatabase {
    private static SecurityState securityState;
    private static SecurityState securityStateTwo;
    private static StateTransition stateTransition;
    private static Device device;
    private static Device deviceTwo;
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static DeviceCommand deviceCommand;
    private static DeviceCommand deviceCommandTwo;
    private static DeviceCommandLookup deviceCommandLookup;
    private static DeviceCommandLookup deviceCommandLookupTwo;
    private static DeviceSecurityState deviceSecurityState;
    private static DeviceSecurityState deviceSecurityStateTwo;
    private static PolicyCondition policyCondition;
    private static PolicyRule policyRule;
    private static PolicyRuleLog policyRuleLog;

    /*
        Command Action Tests
     */

    @Test
    public void testFindCommand() {
        assertEquals(deviceCommand.toString(), DeviceCommandDAO.findCommand(deviceCommand.getId()).toString());
    }

    @Test
    public void testFindAllCommands() {
        assertEquals(2, DeviceCommandDAO.findAllCommands().size());
    }

    @Test
    public void testFindCommandsByPolicyRuleLog() {
        device.setCurrentState(deviceSecurityStateTwo);
        device.insertOrUpdate();

        ArrayList<DeviceCommand> foundCommands = new ArrayList<DeviceCommand>(DeviceCommandDAO.findCommandsByPolicyRuleLog(policyRuleLog.getId()));

        assertEquals(2, foundCommands.size());
        assertEquals(deviceCommand.toString(), foundCommands.get(0).toString());
    }

    @Test
    public void testFindCommandsForDeviceTypeByPolicyRuleLog() {
        ArrayList<DeviceCommand> foundCommands = new ArrayList<DeviceCommand>(DeviceCommandDAO.findCommandsForDeviceTypeByPolicyRuleLog(policyRuleLog.getId(), device.getType().getId()));
        assertEquals(1, foundCommands.size());

        ArrayList<DeviceCommand> foundCommandsTwo = new ArrayList<DeviceCommand>(DeviceCommandDAO.findCommandsForDeviceTypeByPolicyRuleLog(policyRuleLog.getId(), deviceTwo.getType().getId()));
        assertEquals(1, foundCommandsTwo.size());

        assertNotEquals(foundCommands.get(0).toString(), foundCommandsTwo.get(0).toString());
    }

    @Test
    public void testInsertOrUpdateCommand() {
        assertEquals(2, DeviceCommandDAO.findAllCommands().size());

        deviceCommand.setName("new command");
        deviceCommand.insertOrUpdate();

        assertEquals(deviceCommand.getName(), DeviceCommandDAO.findCommand(deviceCommand.getId()).getName());
        assertEquals(2, DeviceCommandDAO.findAllCommands().size());

        DeviceCommand newCommand = new DeviceCommand("new command 2", deviceType.getId());

        int newId = newCommand.insertOrUpdate();

        assertEquals(3, DeviceCommandDAO.findAllCommands().size());
        assertEquals(newCommand.toString(), DeviceCommandDAO.findCommand(newId).toString());
    }

    @Test
    public void testDeleteCommand() {
        assertEquals(deviceCommand.toString(), DeviceCommandDAO.findCommand(deviceCommand.getId()).toString());

        DeviceCommandLookupDAO.deleteCommandLookup(deviceCommandLookup.getId());
        DeviceCommandDAO.deleteCommand(deviceCommand.getId());

        assertEquals(null, DeviceCommandDAO.findCommand(deviceCommand.getId()));
    }

    public void insertData() {
        securityState = SecurityStateDAO.findSecurityState(1);
        securityStateTwo = SecurityStateDAO.findSecurityState(2);
        stateTransition = new StateTransition(securityState.getId(), securityStateTwo.getId());
        stateTransition.insert();

        // insert device_type
        deviceType = new DeviceType(0, "Udoo Neo");
        deviceType.insert();

        deviceTypeTwo = new DeviceType(0, "DLink Camera");
        deviceTypeTwo.insert();

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1, dataNode);
        device.insert();

        deviceTwo = new Device("Device 2", "", deviceTypeTwo, "0.0.0.1", 1, 1, dataNode);
        deviceTwo.insert();

        deviceSecurityState = device.getCurrentState();

        deviceSecurityStateTwo = new DeviceSecurityState(device.getId(), 2);
        deviceSecurityStateTwo.insert();

        // insert policy
        policyCondition = new PolicyCondition(1, null);
        policyCondition.insert();

        policyRule = new PolicyRule(stateTransition.getId(), policyCondition.getId(), deviceType.getId(), 1);
        policyRule.insert();

        policyRuleLog = new PolicyRuleLog(policyRule.getId(), device.getId());
        policyRuleLog.insert();

        // insert command
        deviceCommand = new DeviceCommand("Test Command", deviceType.getId());
        deviceCommand.insert();

        deviceCommandTwo = new DeviceCommand("Second Command", deviceTypeTwo.getId());
        deviceCommandTwo.insert();

        deviceCommandLookup = new DeviceCommandLookup(deviceCommand.getId(), policyRule.getId());
        deviceCommandLookup.insert();

        deviceCommandLookupTwo = new DeviceCommandLookup(deviceCommandTwo.getId(), policyRule.getId());
        deviceCommandLookupTwo.insert();
    }
}
