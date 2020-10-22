CREATE OR REPLACE FUNCTION insertDevices()
    RETURNS VOID AS $$
DECLARE
    dataNode RECORD;
BEGIN
    INSERT INTO data_node(name, ip_address) VALUES ('Hertz', '10.27.151.127') RETURNING id INTO dataNode;

    INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate, data_node_id) VALUES ('DLC', 'Camera', (SELECT id FROM device_type WHERE name='DLink Camera'), '10.27.151.114',10000, 10000, 10000, dataNode.id);

    INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate, data_node_id) VALUES ('PHLE', 'Philips Light Emulator', (SELECT id FROM device_type WHERE name='Philips Hue Light Emulator'), '10.27.151.106:80',10000, 10000, 10000, dataNode.id);
    INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate, data_node_id) VALUES ('PHLE 2', 'Philips Light Emulator', (SELECT id FROM device_type WHERE name='Philips Hue Light Emulator'), '10.27.151.106:800',10000, 10000, 10000, dataNode.id);

    INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate, data_node_id) VALUES ('UNTS', 'Udoo Neo', (SELECT id FROM device_type WHERE name='Udoo Neo'), '10.27.151.101',10000, 10000, 10000, dataNode.id);

    INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate, data_node_id) VALUES ('Kalki', 'WeMo Insight', (SELECT id FROM device_type WHERE name='WeMo Insight'), '10.27.151.121',10000, 10000, 10000, dataNode.id);

END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION insertUdooNeoDevicePolicies()
    RETURNS VOID AS $$
DECLARE
    d RECORD;
    dType RECORD;
    accelX RECORD;
    accelY RECORD;
    accelZ RECORD;
    gyroX RECORD;
    gyroY RECORD;
    gyroZ RECORD;
    magX RECORD;
    magY RECORD;
    magZ RECORD;
    tempMax RECORD;
    tempMaxHyst RECORD;
    tempInput RECORD;
    alertContext RECORD;
    alertType1 RECORD;
    alertTypeLookup1 RECORD;
    alertCondition RECORD;
    policyCondition RECORD;
    normalToNormal RECORD;
    normalToSuspicious RECORD;
    normalToAttack TEXT;
    suspiciousToNormal RECORD;
    suspiciousToSuspicious RECORD;
    suspiciousToAttack TEXT;
    attackToNormal RECORD;
    attackToSuspicious RECORD;
    attackToAttack TEXT;
BEGIN
----------------------------------------------
-- Get device type.
----------------------------------------------
    SELECT INTO dType id FROM device_type WHERE name='Udoo Neo';

----------------------------------------------
-- Get device type sensors.
----------------------------------------------
    SELECT INTO accelX id FROM device_sensor WHERE name='accelerometerX' AND type_id=dType.id;
    SELECT INTO accelY id FROM device_sensor WHERE name='accelerometerY' AND type_id=dType.id;
    SELECT INTO accelZ id FROM device_sensor WHERE name='accelerometerZ' AND type_id=dType.id;
    SELECT INTO gyroX id FROM device_sensor WHERE name='gyroscopeX' AND type_id=dType.id;
    SELECT INTO gyroY id FROM device_sensor WHERE name='gyroscopeY' AND type_id=dType.id;
    SELECT INTO gyroZ id FROM device_sensor WHERE name='gyroscopeZ' AND type_id=dType.id;
    SELECT INTO magX id FROM device_sensor WHERE name='magnetometerX' AND type_id=dType.id;
    SELECT INTO magY id FROM device_sensor WHERE name='magnetometerY' AND type_id=dType.id;
    SELECT INTO magZ id FROM device_sensor WHERE name='magnetometerZ' AND type_id=dType.id;
    SELECT INTO tempMax id FROM device_sensor WHERE name='tempmax' AND type_id=dType.id;
    SELECT INTO tempMaxHyst id FROM device_sensor WHERE name='tempmaxhyst' AND type_id=dType.id;
    SELECT INTO tempInput id FROM device_sensor WHERE name='tempinput' AND type_id=dType.id;

----------------------------------------------
-- Get state transitions.
----------------------------------------------
    SELECT INTO normalToNormal id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO normalToSuspicious id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO normalToAttack id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO suspiciousToNormal id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO suspiciousToSuspicious id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO suspiciousToAttack id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO attackToNormal id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO attackToSuspicious id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO attackToAttack id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;

----------------------------------------------
-- Get the device.
----------------------------------------------
    SELECT INTO d id FROM device WHERE name='UNTS';

----------------------------------------------
-- Configure a policy and associated alert conditions.
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-acceleration';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (suspiciousToAttack.id, policyCondition.id, dType.id, 1);

    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, accelX.id, 1, '<','None', null, '-0.1') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, accelX.id, 1, '>','None', null, '0.1') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, accelY.id, 1, '<','None', null, '-0.0766') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, accelY.id, 1, '>','None', null, '-0.0376') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, accelZ.id, 1, '<','None', null, '-1.126') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, accelZ.id, 1, '>','None', null, '1.000') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

----------------------------------------------
-- Configure a policy and associated alert conditions
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-gyro';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (suspiciousToAttack.id, policyCondition.id, dType.id, 1);

    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, gyroX.id, 1, '<','None', null, '-45') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, gyroX.id, 1, '>','None', null, '45') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, gyroY.id, 1, '<','None', null, '-60') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, gyroY.id, 1, '>','None', null, '-60') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, gyroZ.id, 1, '<','None', null, '-15') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, gyroZ.id, 1, '>','None', null, '15') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

----------------------------------------------
-- Configure a policy and associated alert conditions
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-magnetometer';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (suspiciousToAttack.id, policyCondition.id, dType.id, 1);

    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, magX.id, 1, '<','None', null, '80') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, magX.id, 1, '>','None', null, '90') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, magY.id, 1, '<','None', null, '80') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, magY.id, 1, '>','None', null, '90') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, magZ.id, 1, '<','None', null, '90') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, magZ.id, 1, '>','None', null, '110') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

----------------------------------------------
-- Configure a policy and associated alert conditions
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-temperature';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (suspiciousToAttack.id, policyCondition.id, dType.id, 1);

    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, tempInput.id, 1, '<','None', null, '20') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, tempInput.id, 1, '>','None', null, '23') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

----------------------------------------------
-- Configure a policy and associated alert conditions
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-temperature-avg';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (suspiciousToAttack.id, policyCondition.id, dType.id, 1);

    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, tempInput.id, 10, '<','Average', null, '20') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, tempInput.id, 10, '>','Average', null, '23') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

----------------------------------------------
-- Configure a policy and associated alert conditions
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-abnormal-traffic';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (suspiciousToAttack.id, policyCondition.id, dType.id, 1);

    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insertDlinkDevicePolicies()
    RETURNS VOID AS $$
DECLARE
    d RECORD;
    phle RECORD;
    dType RECORD;
    phleIsOn RECORD;
    motionDetected RECORD;
    phleOnCommand RECORD;
    alertContext RECORD;
    alertType1 RECORD;
    alertTypeLookup1 RECORD;
    alertCondition RECORD;
    policyCondition RECORD;
    policyRule RECORD;
    normalToNormal RECORD;
    normalToSuspicious RECORD;
    normalToAttack TEXT;
    suspiciousToNormal RECORD;
    suspiciousToSuspicious RECORD;
    suspiciousToAttack TEXT;
    attackToNormal RECORD;
    attackToSuspicious RECORD;
    attackToAttack TEXT;
BEGIN
----------------------------------------------
-- Get device type.
----------------------------------------------
    SELECT INTO dType id FROM device_type WHERE name='DLink Camera';

----------------------------------------------
-- Get device type sensors.
----------------------------------------------
    SELECT INTO motionDetected id FROM device_sensor WHERE name='motion_detected' AND type_id=dType.id;

----------------------------------------------
-- Get other device info for polices.
----------------------------------------------
    SELECT INTO phle id FROM device WHERE name='PHLE';
    SELECT INTO phleIsOn id FROM device_sensor WHERE name='isOn' AND type_id=(SELECT id FROM device_type WHERE name='Philips Hue Light Emulator');
    SELECT INTO phleOnCommand id FROM command WHERE name='turn-on' AND device_type_id=(SELECT id FROM device_type WHERE name='Philips Hue Light Emulator');

----------------------------------------------
-- Get state transitions.
----------------------------------------------
    SELECT INTO normalToNormal id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO normalToSuspicious id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO normalToAttack id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO suspiciousToNormal id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO suspiciousToSuspicious id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO suspiciousToAttack id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO attackToNormal id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO attackToSuspicious id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO attackToAttack id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;

----------------------------------------------
-- Get the device.
----------------------------------------------
    SELECT INTO d id FROM device WHERE name='DLC';

----------------------------------------------
-- Configure a policy and associated alert conditions.
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'dlc-motion-sense';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToNormal.id, policyCondition.id, dType.id, 1) RETURNING id INTO policyRule8;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'AND') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, motionDetected.id, 1, '=','None', null, 'true') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (phle.id, phleIsOn.id, 1, '=','None', null, 'true') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (phleOnCommand.id, policyRule.id);

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insertPhleDevicePolicies()
    RETURNS VOID AS $$
DECLARE
    d RECORD;
    d2 RECORD;
    Dlink RECORD;
    DlinkMotionSensor RECORD;
    dType RECORD;
    hue RECORD;
    isOn RECORD;
    brightness RECORD;
    nameSensor RECORD;
    lightId RECORD;
    turnOnCommand RECORD;
    turnOffCommand RECORD;
    alertContext RECORD;
    alertType1 RECORD;
    alertTypeLookup1 RECORD;
    alertCondition RECORD;
    policyCondition RECORD;
    policyRule RECORD;
    normalToNormal RECORD;
    normalToSuspicious RECORD;
    normalToAttack TEXT;
    suspiciousToNormal RECORD;
    suspiciousToSuspicious RECORD;
    suspiciousToAttack TEXT;
    attackToNormal RECORD;
    attackToSuspicious RECORD;
    attackToAttack TEXT;
BEGIN
----------------------------------------------
-- Get device type.
----------------------------------------------
    SELECT INTO dType id FROM device_type WHERE name='Philips Hue Light Emulator';

----------------------------------------------
-- Get device type sensors.
----------------------------------------------
    SELECT INTO hue id FROM device_sensor WHERE name='hue' AND type_id=dType.id;
    SELECT INTO isOn id FROM device_sensor WHERE name='isOn' AND type_id=dType.id;
    SELECT INTO brightness id FROM device_sensor WHERE name='brightness' AND type_id=dType.id;
    SELECT INTO nameSensor id FROM device_sensor WHERE name='name' AND type_id=dType.id;
    SELECT INTO lightId id FROM device_sensor WHERE name='lightId' AND type_id=dType.id;

----------------------------------------------
-- Get device type commands.
----------------------------------------------
    SELECT INTO turnOnCommand id FROM command WHERE name='turn-on' AND device_type_id=dType.id;
    SELECT INTO turnOffCommand id FROM command WHERE name='turn-off' AND device_type_id=dType.id;

----------------------------------------------
-- Get other device info for polices.
----------------------------------------------
    SELECT INTO Dlink id FROM device WHERE name='DLC';
    SELECT INTO DlinkMotionSensor id FROM device_sensor WHERE name='motion_detected' AND type_id=(SELECT id FROM device_type WHERE name='DLink Camera');

----------------------------------------------
-- Get state transitions.
----------------------------------------------
    SELECT INTO normalToNormal id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO normalToSuspicious id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO normalToAttack id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO suspiciousToNormal id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO suspiciousToSuspicious id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO suspiciousToAttack id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO attackToNormal id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO attackToSuspicious id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO attackToAttack id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;

----------------------------------------------
-- Insert the device.
----------------------------------------------
    INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate) VALUES ('PHLE', 'Philips Light Emulator', dType.id, '10.27.151.106:80',10000, 10000, 10000) RETURNING id INTO d;
    INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate) VALUES ('PHLE 2', 'Philips Light Emulator', dType.id, '10.27.151.106:800',10000, 10000, 10000) RETURNING id INTO d2;

----------------------------------------------
-- Configure a policy and associated alert conditions.
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'phle-time-on';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToNormal.id, policyCondition.id, dType.id, 1) RETURNING id INTO policyRule;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'AND') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, isOn.id, 1, '=','None', null, 'true') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (Dlink.id, DlinkMotionSensor.id, 1, '=','None', null, 'true') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOffCommand.id, policyRule.id);

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToNormal.id, policyCondition.id, dType.id, 1) RETURNING id INTO policyRule;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d2.id, alertTypeLookup1.id, 'AND') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d2.id, isOn.id, 1, '=','None', null, 'true') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (Dlink.id, DlinkMotionSensor.id, 1, '=','None', null, 'true') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOffCommand, policyRule.id);

----------------------------------------------
-- Configure a policy and associated alert conditions.
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'phle-odd-one-out';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2) RETURNING id INTO policyRule;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'AND') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, isOn.id, 1, '<','None', null, '10') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d2.id, isOn.id, 1, '<','None', null, '10') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOnCommand.id, policyRule.id);

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToNormal.id, policyCondition.id, dType.id, 2) RETURNING id INTO policyRule;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d2.id, alertTypeLookup1.id, 'AND') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, isOn.id, 1, '<','None', null, '10') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d2.id, isOn.id, 1, '<','None', null, '10') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOnCommand.id, policyRule.id);

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insertWemoDevicePolicies()
    RETURNS VOID AS $$
DECLARE
    d RECORD;
    dType RECORD;
    todayKwh RECORD;
    currentPower RECORD;
    todayStandby RECORD;
    lastChange RECORD;
    isOn RECORD;
    turnOnCommand RECORD;
    turnOffCommand RECORD;
    alertContext RECORD;
    alertType1 RECORD;
    alertTypeLookup1 RECORD;
    alertCondition RECORD;
    policyCondition RECORD;
    policyRule RECORD;
    normalToNormal RECORD;
    normalToSuspicious RECORD;
    normalToAttack TEXT;
    suspiciousToNormal RECORD;
    suspiciousToSuspicious RECORD;
    suspiciousToAttack TEXT;
    attackToNormal RECORD;
    attackToSuspicious RECORD;
    attackToAttack TEXT;
BEGIN
----------------------------------------------
-- Get device type.
----------------------------------------------
    SELECT INTO dType id FROM device_type WHERE name='WeMo Insight';

----------------------------------------------
-- Get device type sensors.
----------------------------------------------
    SELECT INTO todayKwh id FROM device_sensor WHERE name='today_kwh' AND type_id=dType.id;
    SELECT INTO currentPower id FROM device_sensor WHERE name='current_power' AND type_id=dType.id;
    SELECT INTO todayStandby id FROM device_sensor WHERE name='today_standby_time' AND type_id=dType.id;
    SELECT INTO lastChange id FROM device_sensor WHERE name='lastchange' AND type_id=dType.id;
    SELECT INTO isOn id FROM device_sensor WHERE name='isOn' AND type_id=dType.id;

----------------------------------------------
-- Get device type commands.
----------------------------------------------
    SELECT INTO turnOnCommand id FROM command WHERE name='turn-on' AND device_type_id=dType.id;
    SELECT INTO turnOffCommand id FROM command WHERE name='turn-off' AND device_type_id=dType.id;

----------------------------------------------
-- Get other device info for polices.
----------------------------------------------


----------------------------------------------
-- Get state transitions.
----------------------------------------------
    SELECT INTO normalToNormal id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO normalToSuspicious id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO normalToAttack id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO suspiciousToNormal id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO suspiciousToSuspicious id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO suspiciousToAttack id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO attackToNormal id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO attackToSuspicious id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;
    SELECT INTO attackToAttack id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 1;

----------------------------------------------
-- Get the device.
----------------------------------------------
    SELECT INTO d id FROM device WHERE name='Kalki';

----------------------------------------------
-- Configure a policy and associated alert conditions.
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-current-mw-greater-low';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2) RETURNING id INTO policyRule8;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, currentPower.id, 1, '>','None', null, '17040') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);


    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOffCommand.id, policyRule.id);

----------------------------------------------
-- Configure a policy and associated alert conditions.
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-current-mw-greater-low';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (suspiciousToAttack.id, policyCondition.id, dType.id, 1) RETURNING id INTO policyRule8;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, currentPower.id, 1, '>','None', null, '17040') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

----------------------------------------------
-- Configure a policy and associated alert conditions.
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-current-mw-greater-high';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2) RETURNING id INTO policyRule8;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, currentPower.id, 1, '>','None', null, '17040') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);


    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOffCommand.id, policyRule.id);

----------------------------------------------
-- Configure a policy and associated alert conditions.
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-current-mw-greater-high';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (suspiciousToAttack.id, policyCondition.id, dType.id, 1) RETURNING id INTO policyRule8;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, currentPower.id, 1, '>','None', null, '17040') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

----------------------------------------------
-- Configure a policy and associated alert conditions.
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-last-change';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2) RETURNING id INTO policyRule8;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, lastChange.id, 1, '>','None', null, '10') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, isOn.id, 1, '=','None', null, 'True') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOffCommand.id, policyRule.id);

----------------------------------------------
-- Configure a policy and associated alert conditions.
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-last-change';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (suspiciousToAttack.id, policyCondition.id, dType.id, 1) RETURNING id INTO policyRule8;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, lastChange.id, 1, '>','None', null, '10') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, isOn.id, 1, '=','None', null, 'True') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);
----------------------------------------------
-- Configure a policy and associated alert conditions.
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-time-on';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2) RETURNING id INTO policyRule8;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, todayStandby.id, 1, '>','None', null, '32400') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);


    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOffCommand.id, policyRule.id);

----------------------------------------------
-- Configure a policy and associated alert conditions.
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-time-on';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (suspiciousToAttack.id, policyCondition.id, dType.id, 1) RETURNING id INTO policyRule8;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, todayStandby.id, 1, '>','None', null, '32400') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

----------------------------------------------
-- Configure a policy and associated alert conditions.
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-today-kwh';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2) RETURNING id INTO policyRule8;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, todayKwh.id, 1, '>','None', null, '0.220') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);


    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOffCommand.id, policyRule.id);

----------------------------------------------
-- Configure a policy and associated alert conditions.
----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-today-kwh';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;

    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);

    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (suspiciousToAttack.id, policyCondition.id, dType.id, 1) RETURNING id INTO policyRule8;


    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;

    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, todayKwh.id, 1, '>','None', null, '0.220') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);


END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION setCurrentState()
    RETURNS VOID AS $$
        DECLARE
            d RECORD;
            SS RECORD;
            dSS RECORD;
            query TEXT;
        BEGIN
            query := 'SELECT id FROM device';
            FOR d IN EXECUTE query
                LOOP
                    SELECT INTO SS id FROM security_state WHERE name='Normal';
                    INSERT INTO device_security_state (device_id, state_id, timestamp) VALUES (d.id, SS.id, current_timestamp) RETURNING id INTO dSS;
                END LOOP;
END;
$$ LANGUAGE plpgsql;

SELECT insertDevices();
SELECT insertUdooNeoDevicePolicies();
SELECT insertDlinkDevicePolicies();
SELECT insertPhleDevicePolicies();
SELECT insertWemoDevicePolicies();
SELECT setCurrentState();
