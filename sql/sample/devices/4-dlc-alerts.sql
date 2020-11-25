CREATE OR REPLACE FUNCTION insertDLCAlerts()
    RETURNS VOID AS $$
DECLARE
    d RECORD;
    dType RECORD;
    alertType RECORD;
    alertTypeLookup RECORD;
    alertCondition RECORD;
    alertContext RECORD;
    phle RECORD;
    phleIsOn RECORD;
    motionDetected RECORD;
BEGIN
    -- Get device and type info.
    SELECT INTO d id FROM device WHERE name='DLC1';
    SELECT INTO dType type_id AS id FROM device WHERE name='DLC1';

    -- Depends on this device too.
    SELECT INTO phle id FROM device WHERE name='PHLE1';

    -- Get sensor info.
    SELECT INTO motionDetected id FROM device_sensor WHERE name='motion_detected' AND type_id=dType.id;
    SELECT INTO phleIsOn id FROM device_sensor WHERE name='isOn' AND type_id=(SELECT id FROM device_type WHERE name='Philips Hue Light Emulator');

    ----------------------------------------------
    -- dlc-motion-sense
    ----------------------------------------------
    SELECT INTO alertType id FROM alert_type WHERE name = 'dlc-motion-sense';
    SELECT INTO alertTypeLookup id FROM alert_type_lookup WHERE alert_type_id = alertType.id AND device_type_id = dType.id;
    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup.id, 'AND') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, motionDetected.id, 1, '=','None', null, 'true') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (phle.id, phleIsOn.id, 1, '=','None', null, 'true') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

END;
$$ LANGUAGE plpgsql;

SELECT insertDLCAlerts();
