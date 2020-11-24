
CREATE OR REPLACE FUNCTION insertDlinkDevicePolicies()
    RETURNS VOID AS $$
DECLARE
    normal RECORD;
    suspicious RECORD;
    attack RECORD;
    u1 RECORD;
    u5 RECORD;
    u4 RECORD;
    dType RECORD;
    phleOnCommand RECORD;
    alertType1 RECORD;
    policyCondition RECORD;
    policyRule1 RECORD;
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
    -- Security states to reference.
    ----------------------------------------------
    SELECT INTO normal id FROM security_state WHERE name='Normal';
    SELECT INTO suspicious id FROM security_state WHERE name='Suspicious';
    SELECT INTO attack id FROM security_state WHERE name='Attack';

    ----------------------------------------------
    -- Umbox Images for this device type.
    ----------------------------------------------
    SELECT INTO u1 id FROM umbox_image WHERE name='kalki-ssd_lab/u1-antidos';
    SELECT INTO u5 id FROM umbox_image WHERE name='kalki-ssd_lab/u5-sniffer-log-stats';
    SELECT INTO u4 id FROM umbox_image WHERE name='kalki-ssd_lab/u4-block-all';

    ----------------------------------------------
    -- Umbox image lookups
    ----------------------------------------------
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (normal.id, dType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, dType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, dType.id, u5.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (attack.id, dType.id, u4.id, 1);

    ----------------------------------------------
    -- Get other device info for polices.
    ----------------------------------------------
    SELECT INTO phleOnCommand id FROM command WHERE name='turn-on' AND device_type_id=(SELECT id FROM device_type WHERE name='Philips Hue Light Emulator');

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
    -- Policy for dlc-motion-sense
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'dlc-motion-sense';
    INSERT INTO policy_condition(threshold) values (10) RETURNING id INTO policyCondition;
    INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES (policyCondition.id, alertType1.id);
    INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES (normalToNormal.id, policyCondition.id, dType.id, 1) RETURNING id INTO policyRule1;
    INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (phleOnCommand.id, policyRule1.id);

END;
$$ LANGUAGE plpgsql;

SELECT insertDlinkDevicePolicies();
