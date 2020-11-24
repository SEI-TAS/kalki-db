CREATE OR REPLACE FUNCTION insertPhleDevicePolicies()
    RETURNS VOID AS $$
DECLARE
    normal RECORD;
    suspicious RECORD;
    attack RECORD;
    u1 RECORD;
    u9 RECORD;
    u10 RECORD;
    u4 RECORD;
    dType RECORD;
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
    -- Get device type commands.
    ----------------------------------------------
    SELECT INTO turnOnCommand id FROM command WHERE name='turn-on' AND device_type_id=dType.id;
    SELECT INTO turnOffCommand id FROM command WHERE name='turn-off' AND device_type_id=dType.id;

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
    -- Policy for phle-time-on
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'phle-time-on';
    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;
    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);
    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToNormal.id, policyCondition.id, dType.id, 1) RETURNING id INTO policyRule;
    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOffCommand.id, policyRule.id);

    ----------------------------------------------
    -- Policy for phle-odd-one-out
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'phle-odd-one-out';
    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;
    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);
    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToSuspicious.id, policyCondition.id, dType.id, 2) RETURNING id INTO policyRule;
    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (turnOnCommand.id, policyRule.id);

END;
$$ LANGUAGE plpgsql;

SELECT insertPhleDevicePolicies();
