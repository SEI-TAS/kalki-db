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
package edu.cmu.sei.kalki.db.notifications;

import edu.cmu.sei.kalki.db.daos.AlertDAO;
import edu.cmu.sei.kalki.db.daos.DeviceSecurityStateDAO;
import edu.cmu.sei.kalki.db.daos.DeviceStatusDAO;
import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.listeners.InsertListener;
import edu.cmu.sei.kalki.db.models.Alert;
import edu.cmu.sei.kalki.db.models.DeviceSecurityState;
import edu.cmu.sei.kalki.db.models.DeviceStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Used by Dashboard to get new ids.
public class AsyncNotificationStorage
{
    private static final List<Integer> newStateIds = Collections.synchronizedList(new ArrayList<>());
    private static final List<Integer> newAlertIds = Collections.synchronizedList(new ArrayList<>());
    private static final List<Integer> newStatusIds = Collections.synchronizedList(new ArrayList<>());

    /**
     * Start up a notification listener.  This will clear all current handlers and
     * current list of newIds
     */
    public static void startListener() {
        InsertListener.startListening();
        InsertListener.clearHandlers();

        InsertListener.addHandler(Postgres.TRIGGER_NOTIF_NEW_ALERT, new AlertHandler());
        InsertListener.addHandler(Postgres.TRIGGER_NOTIF_NEW_DEV_SEC_STATE, new StateHandler());
        InsertListener.addHandler(Postgres.TRIGGER_NOTIF_NEW_DEV_STATUS, new StatusHandler());

        newAlertIds.clear();
        newStateIds.clear();
        newStatusIds.clear();
    }

    public static void stopListener() {
        InsertListener.stopListening();
    }

    /**
     * adds a given alert id to the list of new alert ids to be given to the dashboard
     * @param newId
     */
    public static void newAlertId(int newId) {
        newAlertIds.add(newId);
    }

    /**
     * adds a given state id to the list of new state ids to be given to the dashboard
     * @param newId
     */
    public static void newStateId(int newId) {
        newStateIds.add(newId);
    }

    /**
     * adds a given status id to the list of new status ids to be given to the dashboard
     * @param newId
     */
    public static void newStatusId(int newId) {
        newStatusIds.add(newId);
    }

    /**
     * return the latest alerts unless there are no alerts and then returns null
     *
     * @return the next new alert to be given to the dashboard in the queue
     */
    public static List<Alert> getNewAlerts() {
        if(newAlertIds.size() != 0) {
            List<Alert> newAlerts = new ArrayList<>();
            synchronized (newAlertIds) {
                for(int alertId: newAlertIds) {
                    Alert newAlert = AlertDAO.findAlert(alertId);
                    newAlerts.add(newAlert);
                }
                newAlertIds.clear();
            }

            return newAlerts;
        } else {
            return null;
        }
    }

    /**
     * return the latest states unless there are no states and then returns null
     *
     * @return the next new device security state to be given to the dashboard in the queue
     */
    public static List<DeviceSecurityState> getNewStates() {
        if(newStateIds.size() != 0) {
            List<DeviceSecurityState> newStates = new ArrayList<>();
            synchronized (newStateIds) {
                for(int stateId: newStateIds) {
                    DeviceSecurityState newState = DeviceSecurityStateDAO.findDeviceSecurityState(stateId);
                    newStates.add(newState);
                }
                newStateIds.clear();
            }

            return newStates;
        } else {
            return null;
        }
    }

    /**
     * return the latest statuses unless there are no statuses and then returns null
     *
     * @return the next new device security state to be given to the dashboard in the queue
     */
    public static List<DeviceStatus> getNewStatuses() {
        if(newStatusIds.size() != 0) {
            List<DeviceStatus> newStatuses = new ArrayList<>();
            synchronized (newStatusIds) {
                for(int statusId: newStatusIds) {
                    DeviceStatus newStatus = DeviceStatusDAO.findDeviceStatus(statusId);
                    newStatuses.add(newStatus);
                }
                newStatusIds.clear();
            }

            return newStatuses;
        } else {
            return null;
        }
    }
}
