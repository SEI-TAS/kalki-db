CREATE OR REPLACE FUNCTION configureUdooNeo()
    RETURNS VOID AS $$
DECLARE
    deviceType RECORD;
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
    alertType1 RECORD;
    alertTypeLookup1 RECORD;
    alertCondition RECORD;
    alertContext RECORD;
BEGIN
    ----------------------------------------------
    -- Device type itself.
    ----------------------------------------------
    INSERT INTO device_type(name) values ('Udoo Neo') RETURNING id INTO deviceType;

    ----------------------------------------------
    -- Device type sensors.
    ----------------------------------------------
    INSERT INTO device_sensor(name, type_id) values ('accelerometerX', deviceType.id) RETURNING id INTO accelX;
    INSERT INTO device_sensor(name, type_id) values ('accelerometerY', deviceType.id) RETURNING id INTO accelY;
    INSERT INTO device_sensor(name, type_id) values ('accelerometerZ', deviceType.id) RETURNING id INTO accelZ;
    INSERT INTO device_sensor(name, type_id) values ('magnetometerX', deviceType.id) RETURNING id INTO magX;
    INSERT INTO device_sensor(name, type_id) values ('magnetometerY', deviceType.id) RETURNING id INTO magY;
    INSERT INTO device_sensor(name, type_id) values ('magnetometerZ', deviceType.id) RETURNING id INTO magZ;
    INSERT INTO device_sensor(name, type_id) values ('gyroscopeX', deviceType.id) RETURNING id INTO gyroX;
    INSERT INTO device_sensor(name, type_id) values ('gyroscopeY', deviceType.id) RETURNING id INTO gyroY;
    INSERT INTO device_sensor(name, type_id) values ('gyroscopeZ', deviceType.id) RETURNING id INTO gyroZ;
    INSERT INTO device_sensor(name, type_id) values ('tempmax', deviceType.id) RETURNING id INTO tempMax;
    INSERT INTO device_sensor(name, type_id) values ('tempmax_hyst', deviceType.id) RETURNING id INTO tempMaxHyst;
    INSERT INTO device_sensor(name, type_id) values ('tempinput', deviceType.id) RETURNING id INTO tempInput;

    ----------------------------------------------
    -- Commands that apply to this device type.
    ----------------------------------------------

    ----------------------------------------------
    -- Alert types specifically for this dev type.
    ----------------------------------------------
    INSERT INTO alert_type(name, description, source) values('unts-abnormal-traffic', 'There is network traffic coming from the device that differs from standard responses.', 'Network');
    INSERT INTO alert_type(name, description, source) values('unts-acceleration', 'unts.acceleration > Z', 'Device');
    INSERT INTO alert_type(name, description, source) values('unts-gyro', 'unts.gyroscope != (X±d), (Y±d), (Z±d)', 'Device');
    INSERT INTO alert_type(name, description, source) values('unts-magnetometer', 'unts.magnetometer != (X±d), (Y±d), (Z±d)', 'Device');
    INSERT INTO alert_type(name, description, source) values('unts-temperature', 'unts.temp > X OR unts.temp < Y', 'Device');
    INSERT INTO alert_type(name, description, source) values('unts-temperature-avg', 'unts.temp >/< avg(last N) ± D && state = suspicious', 'Device');

    ----------------------------------------------
    -- Associating alert types to the device type.
    ----------------------------------------------
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'device-unavailable'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'state-reset'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name='brute-force'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name = 'unts-abnormal-traffic'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name = 'unts-acceleration'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-gyro'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-magnetometer'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-temperature'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-temperature-avg'), deviceType.id);

    ----------------------------------------------
    -- ALERT CONDITIONS.
    ----------------------------------------------

    ----------------------------------------------
    -- unts-acceleration
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-acceleration';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = deviceType.id;
    INSERT INTO alert_context(alert_type_lookup_id, logical_operator) VALUES (alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, accelX.id, 1, '<','None', '-0.1') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, accelX.id, 1, '>','None', '0.1') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, accelY.id, 1, '<','None', '-0.0766') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, accelY.id, 1, '>','None', '-0.0376') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, accelZ.id, 1, '<','None', '-1.126') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, accelZ.id, 1, '>','None', '1.000') RETURNING id INTO alertCondition;

    ----------------------------------------------
    -- unts-gyro
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-gyro';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = deviceType.id;
    INSERT INTO alert_context(alert_type_lookup_id, logical_operator) VALUES (alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, gyroX.id, 1, '<','None', '-45') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, gyroX.id, 1, '>','None', '45') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, gyroY.id, 1, '<','None', '-60') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, gyroY.id, 1, '>','None', '60') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, gyroZ.id, 1, '<','None', '-15') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, gyroZ.id, 1, '>','None', '15') RETURNING id INTO alertCondition;

    ----------------------------------------------
    -- unts-magnetometer
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-magnetometer';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = deviceType.id;
    INSERT INTO alert_context(alert_type_lookup_id, logical_operator) VALUES (alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, magX.id, 1, '<','None', '50') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, magX.id, 1, '>','None', '90') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, magY.id, 1, '<','None', '50') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, magY.id, 1, '>','None', '90') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, magZ.id, 1, '<','None', '90') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, magZ.id, 1, '>','None', '110') RETURNING id INTO alertCondition;

    ----------------------------------------------
    -- unts-temperature
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-temperature';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = deviceType.id;
    INSERT INTO alert_context(alert_type_lookup_id, logical_operator) VALUES (alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, tempInput.id, 1, '<','None', '20') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, tempInput.id, 1, '>','None', '23') RETURNING id INTO alertCondition;

    ----------------------------------------------
    -- unts-temperature
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'unts-temperature-avg';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = deviceType.id;
    INSERT INTO alert_context(alert_type_lookup_id, logical_operator) VALUES (alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, tempInput.id, 10, '<','Average', '20') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, tempInput.id, 10, '>','Average', '23') RETURNING id INTO alertCondition;


END;
$$ LANGUAGE plpgsql;

SELECT configureUdooNeo();






