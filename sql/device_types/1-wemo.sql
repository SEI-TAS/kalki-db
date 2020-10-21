CREATE OR REPLACE FUNCTION configureWemo()
    RETURNS VOID AS $$
DECLARE
    deviceType RECORD;
    normal RECORD;
    suspicious RECORD;
    attack RECORD;
    u1 RECORD;
    u2 RECORD;
    u3 RECORD;
    u8 RECORD;
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
-- Security states to reference.
----------------------------------------------
    SELECT INTO normal id FROM security_state WHERE name='Normal';
    SELECT INTO suspicious id FROM security_state WHERE name='Suspicious';
    SELECT INTO attack id FROM security_state WHERE name='Attack';

----------------------------------------------
-- Umbox Images for this device type.
----------------------------------------------
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u1-antidos', 'ssd_lab/u1-antidos') RETURNING id INTO u1;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u2-http-auth-proxy', 'ssd_lab/u2-http-auth-proxy') RETURNING id INTO u2;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u3-http-auth-proxy-block', 'ssd_lab/u3-http-auth-proxy-block') RETURNING id INTO u3;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u8-fake-replies', 'ssd_lab/u8-fake-replies') RETURNING id INTO u8;

----------------------------------------------
-- Umbox image lookups
----------------------------------------------
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (normal.id, deviceType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (normal.id, deviceType.id, u2.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, deviceType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, deviceType.id, u3.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (attack.id, deviceType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (attack.id, deviceType.id, u8.id, 1);

END;
$$ LANGUAGE plpgsql;

SELECT configureWemo();






