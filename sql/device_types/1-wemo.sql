CREATE OR REPLACE FUNCTION configureWemo()
    RETURNS VOID AS $$
DECLARE
    deviceType RECORD;
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

END;
$$ LANGUAGE plpgsql;

SELECT configureWemo();






