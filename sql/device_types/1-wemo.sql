CREATE OR REPLACE FUNCTION configureWemo()
    RETURNS VOID AS $$
DECLARE
    deviceType RECORD;
    alertType1 RECORD;
    alertTypeLookup1 RECORD;
    alertCondition RECORD;
    alertContext RECORD;
    todayKwh RECORD;
    currentPower RECORD;
    todayStandby RECORD;
    lastChange RECORD;
    isOn RECORD;
BEGIN
    ----------------------------------------------
    -- Device type itself.
    ----------------------------------------------
    INSERT INTO device_type(name) values ('WeMo Insight') RETURNING id INTO deviceType;

    ----------------------------------------------
    -- Device type sensors.
    ----------------------------------------------
    INSERT INTO device_sensor(name, type_id) values ('today_kwh', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('current_power', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('today_standby_time', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('lastchange', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('isOn', deviceType.id);

    ----------------------------------------------
    -- Commands that apply to this device type.
    ----------------------------------------------
    INSERT INTO command(name, device_type_id) values ('turn-off', deviceType.id);
    INSERT INTO command(name, device_type_id) values ('turn-on', deviceType.id);

    ----------------------------------------------
    -- Alert types specifically for this dev type.
    ----------------------------------------------
    INSERT INTO alert_type(name, description, source) values('wemo-current-mw-greater-low', 'wemo.currentmw > X', 'Iot Interface');
    INSERT INTO alert_type(name, description, source) values('wemo-current-mw-greater-low-suspicious', 'wemo.currentmw > X for Y minutes', 'Iot Interface');
    INSERT INTO alert_type(name, description, source) values('wemo-current-mw-greater-high', 'wemo.currentmw > Y', 'Iot Interface');
    INSERT INTO alert_type(name, description, source) values('wemo-time-on', 'wemo.today_on_time > T', 'Iot Interface');
    INSERT INTO alert_type(name, description, source) values('wemo-today-kwh', 'wemo.today_kwh > K', 'Iot Interface');

    ----------------------------------------------
    -- Associating alert types to the device type.
    ----------------------------------------------
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'device-unavailable'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'state-reset'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name='brute-force'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name='max-login-attempts'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-current-mw-greater-low'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-current-mw-greater-low-suspicious'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-current-mw-greater-high'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-time-on'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-today-kwh'), deviceType.id);

    ----------------------------------------------
    -- ALERT CONDITIONS.
    ----------------------------------------------

    ----------------------------------------------
    -- wemo-current-mw-greater-low
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-current-mw-greater-low';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = deviceType.id;
    INSERT INTO alert_context(alert_type_lookup_id, logical_operator) VALUES (alertTypeLookup1.id, 'NONE') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, currentPower.id, 1, '>','None', '17040') RETURNING id INTO alertCondition;

    ----------------------------------------------
    -- wemo-current-mw-greater-high
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-current-mw-greater-high';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = deviceType.id;
    INSERT INTO alert_context(alert_type_lookup_id, logical_operator) VALUES (alertTypeLookup1.id, 'NONE') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, currentPower.id, 1, '>','None', '17050') RETURNING id INTO alertCondition;

    ----------------------------------------------
    -- wemo-last-change
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-last-change';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = deviceType.id;
    INSERT INTO alert_context(alert_type_lookup_id, logical_operator) VALUES (alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, lastChange.id, 1, '>','None', '10') RETURNING id INTO alertCondition;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, isOn.id, 1, '=','None', 'True') RETURNING id INTO alertCondition;

    ----------------------------------------------
    -- wemo-time-on
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-time-on';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = deviceType.id;
    INSERT INTO alert_context(alert_type_lookup_id, logical_operator) VALUES (alertTypeLookup1.id, 'NONE') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, todayStandby.id, 1, '>','None', '32400') RETURNING id INTO alertCondition;

    ----------------------------------------------
    -- wemo-today-kwh
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-today-kwh';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = deviceType.id;
    INSERT INTO alert_context(alert_type_lookup_id, logical_operator) VALUES (alertTypeLookup1.id, 'NONE') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(context_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_value) VALUES (alertContext.id, todayKwh.id, 1, '>','None', '0.220') RETURNING id INTO alertCondition;

END;
$$ LANGUAGE plpgsql;

SELECT configureWemo();






