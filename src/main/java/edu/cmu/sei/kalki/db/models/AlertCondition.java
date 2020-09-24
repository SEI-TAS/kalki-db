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
package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.AlertConditionDAO;
import edu.cmu.sei.kalki.db.daos.AlertContextDAO;

public class AlertCondition extends Model {
    private int deviceId;
    private int attributeId;
    private String attributeName;
    private int numStatues;
    private String compOperator;
    private String calculation;
    private int thresholdId;
    private String thresholdValue;

    public AlertCondition() {}

    public AlertCondition(int deviceId, int attributeId, String attributeName, int numStatues, String compOperator, String calculation, int thresholdId, String thresholdValue) {
        this.deviceId = deviceId;
        this.attributeId = attributeId;
        this.attributeName = attributeName;
        this.numStatues = numStatues;
        this.compOperator = compOperator;
        this.calculation = calculation;
        this.thresholdId = thresholdId;
        this.thresholdValue = thresholdValue;
    }

    public AlertCondition(int deviceId, int attributeId, String attributeName, int numStatues, ComparisonOperator compOperator, Calculation calc, int thresholdId, String thresholdValue) {
        this(deviceId, attributeId, attributeName, numStatues, compOperator.convert(), calc.convert(), thresholdId, thresholdValue);
    }


    public AlertCondition(int id, int deviceId, int attributeId, String attributeName, int numStatues, String compOperator, String calculation, int thresholdId, String thresholdValue) {
        this(deviceId, attributeId, attributeName, numStatues, compOperator, calculation, thresholdId, thresholdValue);
        this.id = id;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public int getNumStatues() {
        return numStatues;
    }

    public void setNumStatues(int numStatues) {
        this.numStatues = numStatues;
    }

    public String getCompOperator() {
        return compOperator;
    }

    public void setCompOperator(String compOperator) {
        this.compOperator = compOperator;
    }

    public void setCompOperator(ComparisonOperator compOperator) {
        this.compOperator = compOperator.convert();
    }

    public String getCalculation() {
        return calculation;
    }

    public void setCalculation(String calculation) {
        this.calculation = calculation;
    }

    public void setCalculation(Calculation calculation) { this.calculation = calculation.convert(); }

    public int getThresholdId() {
        return thresholdId;
    }

    public void setThresholdId(int thresholdId) {
        this.thresholdId = thresholdId;
    }

    public String getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(String thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    @Override
    public int insert() {
        int id = AlertConditionDAO.insertAlertCondition(this).getId();
        this.id = id;
        return id;
    }

    public void insertOrUpdate() {
        AlertCondition cond = AlertConditionDAO.insertOrUpdateAlertCondition(this);
        this.id = cond.getId();
    }

    public enum ComparisonOperator {
        EQUAL,
        GREATER,
        GREATER_OR_EQUAL,
        LESS,
        LESS_OR_EQUAL;

        private ComparisonOperator() {}

        public String convert() {
            switch (this) {
                case EQUAL:
                    return "=";
                case GREATER:
                    return ">";
                case GREATER_OR_EQUAL:
                    return ">=";
                case LESS:
                    return "<";
                case LESS_OR_EQUAL:
                    return "<=";
                default:
                    return "Unsupported operator";
            }
        }
    }

    public enum Calculation {
        AVERAGE,
        SUM,
        NONE;

        private Calculation() {}

        public String convert() {
            switch (this) {
                case AVERAGE:
                    return "Average";
                case SUM:
                    return "Sum";
                case NONE:
                    return "None";
                default:
                    return "Unsupported calculation";
            }
        }
    }
}
