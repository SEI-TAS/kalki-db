CREATE OR REPLACE FUNCTION insertWemoDevicePolicies()
    RETURNS VOID AS $$
DECLARE
    d RECORD;
    dType RECORD;
    normal RECORD;
    suspicious RECORD;
    attack RECORD;
    u1 RECORD;
    u2 RECORD;
    u3 RECORD;
    u8 RECORD;
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
    -- Security states to reference.
    ----------------------------------------------
    SELECT INTO normal id FROM security_state WHERE name='Normal';
    SELECT INTO suspicious id FROM security_state WHERE name='Suspicious';
    SELECT INTO attack id FROM security_state WHERE name='Attack';

    ----------------------------------------------
    -- Umbox Images for this device type.
    ----------------------------------------------
    SELECT INTO u1 id FROM umbox_image WHERE name='kalki-ssd_lab/u1-antidos';
    SELECT INTO u2 id FROM umbox_image WHERE name='kalki-ssd_lab/u2-http-auth-proxy';
    SELECT INTO u3 id FROM umbox_image WHERE name='kalki-ssd_lab/u3-http-auth-proxy-block';
    SELECT INTO u8 id FROM umbox_image WHERE name='kalki-ssd_lab/u8-fake-replies';

    ----------------------------------------------
    -- Umbox image lookups
    ----------------------------------------------
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (normal.id, dType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (normal.id, dType.id, u2.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, dType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, dType.id, u3.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (attack.id, dType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (attack.id, dType.id, u8.id, 1);

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
    SELECT INTO normalToSuspicious id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 2;
    SELECT INTO normalToAttack id FROM state_transistion WHERE start_sec_state_id = 1 AND finish_sec_state_id = 3;
    SELECT INTO suspiciousToNormal id FROM state_transistion WHERE start_sec_state_id = 2 AND finish_sec_state_id = 1;
    SELECT INTO suspiciousToSuspicious id FROM state_transistion WHERE start_sec_state_id = 2 AND finish_sec_state_id = 2;
    SELECT INTO suspiciousToAttack id FROM state_transistion WHERE start_sec_state_id = 2 AND finish_sec_state_id = 3;
    SELECT INTO attackToNormal id FROM state_transistion WHERE start_sec_state_id = 3 AND finish_sec_state_id = 1;
    SELECT INTO attackToSuspicious id FROM state_transistion WHERE start_sec_state_id = 3 AND finish_sec_state_id = 2;
    SELECT INTO attackToAttack id FROM state_transistion WHERE start_sec_state_id = 3 AND finish_sec_state_id = 3;

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

SELECT insertWemoDevicePolicies();