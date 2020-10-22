CREATE OR REPLACE FUNCTION insertPhleDevicePolicies()
    RETURNS VOID AS $$
DECLARE
    d RECORD;
    normal RECORD;
    suspicious RECORD;
    attack RECORD;
    u1 RECORD;
    u9 RECORD;
    u10 RECORD;
    u4 RECORD;
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
    -- Security states to reference.
    ----------------------------------------------
    SELECT INTO normal id FROM security_state WHERE name='Normal';
    SELECT INTO suspicious id FROM security_state WHERE name='Suspicious';
    SELECT INTO attack id FROM security_state WHERE name='Attack';

    ----------------------------------------------
    -- Umbox Images for this device type.
    ----------------------------------------------
    SELECT INTO u1 id FROM umbox_image WHERE name='kalki-ssd_lab/u1-antidos';
    SELECT INTO u9 id FROM umbox_image WHERE name='kalki-ssd_lab/u9-phillips-brute-force';
    SELECT INTO u10 id FROM umbox_image WHERE name='kalki-ssd_lab/u10-phillips-brute-force-restrict';
    SELECT INTO u4 id FROM umbox_image WHERE name='kalki-ssd_lab/u4-block-all';

    ----------------------------------------------
    -- Umbox image lookups
    ----------------------------------------------
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (normal.id, dType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (normal.id, dType.id, u9.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, dType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, dType.id, u10.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (attack.id, dType.id, u4.id, 1);

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
    SELECT INTO normalToSuspicious id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 2;
    SELECT INTO normalToAttack id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 3;
    SELECT INTO suspiciousToNormal id FROM state_transistion WHERE start_sec_state_id = 2 AND finish_sec_state_id = 1;
    SELECT INTO suspiciousToSuspicious id FROM state_transistion WHERE start_sec_state_id = 2 AND finish_sec_state_id = 2;
    SELECT INTO suspiciousToAttack id FROM state_transistion WHERE start_sec_state_id = 2 AND finish_sec_state_id = 3;
    SELECT INTO attackToNormal id FROM state_transistion WHERE start_sec_state_id = 3 AND finish_sec_state_id = 1;
    SELECT INTO attackToSuspicious id FROM state_transistion WHERE start_sec_state_id = 3 AND finish_sec_state_id = 2;
    SELECT INTO attackToAttack id FROM state_transistion WHERE start_sec_state_id = 3 AND finish_sec_state_id = 3;

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

SELECT insertPhleDevicePolicies();
