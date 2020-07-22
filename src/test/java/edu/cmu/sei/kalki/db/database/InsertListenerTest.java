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

import edu.cmu.sei.kalki.db.models.*;
import edu.cmu.sei.kalki.db.listeners.*;

import java.util.concurrent.TimeUnit;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InsertListenerTest extends AUsesDatabase {
    private InsertListener insertListener;
    private TestHandler testHandler;
    private DeviceType type;

    @Test
    public void testDeviceInsert() {
        insertListener.addHandler("deviceinsert", testHandler);

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        Device d  = new Device("Test device", "test", type, "ip", 1,1, dataNode);
        d.insert();

        sleep();

        assertEquals(true, testHandler.hasReceivedNotification());
    }

    @Test
    public void testDeviceUpdate() {
        insertListener.addHandler("deviceupdate", testHandler);
        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        Device d  = new Device("Test device", "test", type, "ip", 1,1, dataNode);
        d.insert();
        d.setSamplingRate(2);
        d.insertOrUpdate();

        sleep();

        assertEquals(true, testHandler.hasReceivedNotification());
    }

    @Test
    public void testDeviceStatusInsert() {
        insertListener.addHandler("devicestatusinsert", testHandler);

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        Device d  = new Device("Test device", "test", type, "ip", 1,1, dataNode);
        d.insert();

        DeviceStatus deviceStatus = new DeviceStatus(d.getId(), new HashMap<>());
        deviceStatus.insert();

        sleep();

        assertEquals(true, testHandler.hasReceivedNotification());
    }

    @Test
    public void testAlertHistoryInsert() {
        insertListener.addHandler("alerthistoryinsert", testHandler);

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        Device d  = new Device("Test device", "test", type, "ip", 1,1, dataNode);
        d.insert();

        DeviceStatus deviceStatus = new DeviceStatus(d.getId(), new HashMap<>());
        deviceStatus.insert();

        AlertType alertType = new AlertType("Name", "Description", "Source");
        alertType.insert();

        Alert alert = new Alert(d.getId(), alertType.getName(), alertType.getId(), "Info");
        alert.insert();

        sleep();

        assertEquals(true, testHandler.hasReceivedNotification());
    }

    @Test
    public void testDeviceSecurityStateInsert() {
        insertListener.addHandler("devicesecuritystateinsert", testHandler);

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        Device d  = new Device("Test device", "test", type, "ip", 1,1, dataNode);
        d.insert(); //DSS is inserted when a new device is inserted

        sleep();

        assertEquals(true, testHandler.hasReceivedNotification());
    }

    @Test
    public void testPolicyRuleLogInsert() {
        insertListener.addHandler("policyruleloginsert", testHandler);

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        Device d  = new Device("Test device", "test", type, "ip", 1,1, dataNode);
        d.insert();

        StateTransition stateTransition = new StateTransition(1,2);
        stateTransition.insert();

        PolicyCondition policyCondition = new PolicyCondition(1, null);
        policyCondition.insert();

        PolicyRule policyRule = new PolicyRule(stateTransition.getId(), policyCondition.getId(), d.getType().getId(), 1);
        policyRule.insert();

        PolicyRuleLog policyRuleLog = new PolicyRuleLog(policyRule.getId(), d.getId());
        policyRuleLog.insert();

        sleep();

        assertEquals(true, testHandler.hasReceivedNotification());
    }

    @AfterEach
    public void stopListener() {
        insertListener.clearHandlers();
        insertListener.stopListening();
    }

    public void insertData() {
        type = new DeviceType(-1, "Test type");
        type.insert();
        insertListener.startListening();

        testHandler = new TestHandler();
    }

    private void sleep(){
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {}
    }

    class TestHandler implements InsertHandler {
        private boolean receivedNotification;

        public TestHandler() { receivedNotification = false; }

        public void handleNewInsertion(int newId){
            receivedNotification = true;
        }

        public boolean hasReceivedNotification() { return receivedNotification; }
    }
}
