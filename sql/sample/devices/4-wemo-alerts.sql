CREATE OR REPLACE FUNCTION insertWemoAlerts()
    RETURNS VOID AS $$
DECLARE
    d RECORD;
    dType RECORD;
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
    -- Get device and type info.
    SELECT INTO d id FROM device WHERE name='WEMO1';
    SELECT INTO dType type_id AS id FROM device WHERE name='WEMO1';

    ----------------------------------------------
    -- Get device type sensors.
    ----------------------------------------------
    SELECT INTO todayKwh id FROM device_sensor WHERE name='today_kwh' AND type_id=dType.id;
    SELECT INTO currentPower id FROM device_sensor WHERE name='current_power' AND type_id=dType.id;
    SELECT INTO todayStandby id FROM device_sensor WHERE name='today_standby_time' AND type_id=dType.id;
    SELECT INTO lastChange id FROM device_sensor WHERE name='lastchange' AND type_id=dType.id;
    SELECT INTO isOn id FROM device_sensor WHERE name='isOn' AND type_id=dType.id;

    ----------------------------------------------
    -- wemo-current-mw-greater-low
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-current-mw-greater-low';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;
    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'NONE') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, currentPower.id, 1, '>','None', null, '17040') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    ----------------------------------------------
    -- wemo-current-mw-greater-high
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-current-mw-greater-high';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;
    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'NONE') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, currentPower.id, 1, '>','None', null, '17050') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    ----------------------------------------------
    -- wemo-last-change
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-last-change';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;
    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'OR') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, lastChange.id, 1, '>','None', null, '10') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, isOn.id, 1, '=','None', null, 'True') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    ----------------------------------------------
    -- wemo-time-on
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-time-on';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;
    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'NONE') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, todayStandby.id, 1, '>','None', null, '32400') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

    ----------------------------------------------
    -- wemo-today-kwh
    ----------------------------------------------
    SELECT INTO alertType1 id FROM alert_type WHERE name = 'wemo-today-kwh';
    SELECT INTO alertTypeLookup1 id FROM alert_type_lookup WHERE alert_type_id = alertType1.id AND device_type_id = dType.id;
    INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (d.id, alertTypeLookup1.id, 'NONE') RETURNING id INTO alertContext;
    INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES (d.id, todayKwh.id, 1, '>','None', null, '0.220') RETURNING id INTO alertCondition;
    INSERT INTO alert_circumstance(context_id, condition_id) VALUES (alertContext.id, alertCondition.id);

END;
$$ LANGUAGE plpgsql;

SELECT insertWemoAlerts();
