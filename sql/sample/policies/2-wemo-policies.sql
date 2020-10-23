CREATE OR REPLACE FUNCTION insertWemoDevicePolicies()
    RETURNS VOID AS $$
DECLARE
    dType RECORD;
    normal RECORD;
    suspicious RECORD;
    attack RECORD;
    u1 RECORD;
    u2 RECORD;
    u3 RECORD;
    u8 RECORD;
    turnOnCommand RECORD;
    turnOffCommand RECORD;
    alertType1 RECORD;
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
    -- wemo-current-mw-greater-low policy - N->S
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-current-mw-greater-low';
    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;
    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);
    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2);
    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOffCommand.id, policyRule.id);

    ----------------------------------------------
    -- wemo-current-mw-greater-high - N->S
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-current-mw-greater-high';
    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;
    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);
    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2);
    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOffCommand.id, policyRule.id);

    ----------------------------------------------
    -- wemo-last-change - N->S
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-last-change';
    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;
    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);
    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2);
    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOffCommand.id, policyRule.id);

    ----------------------------------------------
    -- wemo-time-on - N->S
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-time-on';
    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;
    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);
    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2);
    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOffCommand.id, policyRule.id);

    ----------------------------------------------
    -- wemo-today-kwh - N->S
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-today-kwh';
    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;
    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);
    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2);
    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOffCommand.id, policyRule.id);

END;
$$ LANGUAGE plpgsql;

SELECT insertWemoDevicePolicies();