CREATE OR REPLACE FUNCTION insertWemoInstance()
    RETURNS VOID AS $$
DECLARE
    d RECORD;
    dType RECORD;
    alertType1 RECORD;
    alertTypeLookup1 RECORD;
    alertCondition RECORD;
    alertContext RECORD;
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
BEGIN
    -- Get device and type info.
    SELECT INTO d id FROM device WHERE name='UNTS1';
    SELECT INTO dType type_id AS id FROM device WHERE name='UNTS1';

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
    -- unts-acceleration
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-acceleration';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;
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
    -- unts-gyro
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-gyro';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;
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
    -- unts-magnetometer
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-magnetometer';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;
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
    -- unts-temperature
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-temperature';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;
    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, tempInput.id, 1, '<','None', null, '20') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, tempInput.id, 1, '>','None', null, '23') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    ----------------------------------------------
    -- unts-temperature
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-temperature';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;
    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, tempInput.id, 10, '<','Average', null, '20') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, tempInput.id, 10, '>','Average', null, '23') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);SELECT insertUdooNeoDevicePolicies();

END;
$$ LANGUAGE plpgsql;

SELECT insertWemoInstance();
