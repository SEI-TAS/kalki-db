CREATE OR REPLACE FUNCTION insertUdooNeoDevicePolicies()
    RETURNS VOID AS $$
DECLARE
    d RECORD;
    dType RECORD;
    normal RECORD;
    suspicious RECORD;
    attack RECORD;
    u1 RECORD;
    u4 RECORD;
    u6 RECORD;
    u7 RECORD;
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
    -- Security states to reference.
    ----------------------------------------------
    SELECT INTO normal id FROM security_state WHERE name='Normal';
    SELECT INTO suspicious id FROM security_state WHERE name='Suspicious';
    SELECT INTO attack id FROM security_state WHERE name='Attack';

    ----------------------------------------------
    -- Umbox Images for this device type.
    ----------------------------------------------
    SELECT INTO u1 id FROM umbox_image WHERE name='kalki-ssd_lab/u1-antidos';
    SELECT INTO u6 id FROM umbox_image WHERE name='kalki-ssd_lab/u6-udoo-brute-force';
    SELECT INTO u7 id FROM umbox_image WHERE name='kalki-ssd_lab/u7-udoo-brute-force-block';
    SELECT INTO u4 id FROM umbox_image WHERE name='kalki-ssd_lab/u4-block-all';

    ----------------------------------------------
    -- Umbox image lookups
    ----------------------------------------------
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (normal.id, dType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (normal.id, dType.id, u6.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, dType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, dType.id, u7.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (attack.id, dType.id, u4.id, 1);

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
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);SELECT insertUdooNeoDevicePolicies();

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

SELECT insertUdooNeoDevicePolicies();
