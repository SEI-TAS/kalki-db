CREATE OR REPLACE FUNCTION insertPHLEAlerts()
    RETURNS VOID AS $$
DECLARE
    d RECORD;
    dType RECORD;
    alertType1 RECORD;
    alertTypeLookup1 RECORD;
    alertCondition RECORD;
    alertContext RECORD;
    d2 RECORD;
    Dlink RECORD;
    DlinkMotionSensor RECORD;
    hue RECORD;
    isOn RECORD;
    brightness RECORD;
    nameSensor RECORD;
    lightId RECORD;
BEGIN
    -- Get device and type info.
    SELECT INTO d id FROM device WHERE name='PHLE1';
    SELECT INTO dType type_id AS id FROM device WHERE name='PHLE1';

    -- Depends on this device too.
    SELECT INTO d2 id FROM device WHERE name='PHLE2';
    SELECT INTO Dlink id FROM device WHERE name='DLC1';

    ----------------------------------------------
    -- Get device type sensors.
    ----------------------------------------------
    SELECT INTO hue id FROM device_sensor WHERE name='hue' AND type_id=dType.id;
    SELECT INTO isOn id FROM device_sensor WHERE name='isOn' AND type_id=dType.id;
    SELECT INTO brightness id FROM device_sensor WHERE name='brightness' AND type_id=dType.id;
    SELECT INTO nameSensor id FROM device_sensor WHERE name='name' AND type_id=dType.id;
    SELECT INTO lightId id FROM device_sensor WHERE name='lightId' AND type_id=dType.id;

    -- Get other device's sensor info.
    SELECT INTO DlinkMotionSensor id FROM device_sensor WHERE name='motion_detected' AND type_id=(SELECT id FROM device_type WHERE name='DLink Camera');

    ---------------------------------------------
    -- phle-time-on, for both devices.
    ---------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'phle-time-on';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'AND') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, isOn.id, 1, '=','None', null, 'true') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (Dlink.id, DlinkMotionSensor.id, 1, '=','None', null, 'true') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d2.id, alertTypeLookup1.id, 'AND') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d2.id, isOn.id, 1, '=','None', null, 'true') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (Dlink.id, DlinkMotionSensor.id, 1, '=','None', null, 'true') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    ---------------------------------------------
    -- phle-odd-one-out, for both devices
    ---------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'phle-odd-one-out';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;

    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'AND') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, isOn.id, 1, '=','None', null, 'true') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d2.id, isOn.id, 1, '=','None', null, 'false') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d2.id, alertTypeLookup1.id, 'AND') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, isOn.id, 1, '=','None', null, 'true') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d2.id, isOn.id, 1, '=','None', null, 'false') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

END;
$$ LANGUAGE plpgsql;

SELECT insertPHLEAlerts();
