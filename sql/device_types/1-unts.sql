CREATE OR REPLACE FUNCTION configureUdooNeo()
    RETURNS VOID AS $$
DECLARE
    deviceType RECORD;
    normal RECORD;
    suspicious RECORD;
    attack RECORD;
    u1 RECORD;
    u4 RECORD;
    u6 RECORD;
    u7 RECORD;
BEGIN
----------------------------------------------
-- Device type itself.
----------------------------------------------
    INSERT INTO device_type(name) values ('Udoo Neo') RETURNING id INTO deviceType;

----------------------------------------------
-- Device type sensors.
----------------------------------------------
    INSERT INTO device_sensor(name, type_id) values ('accelerometerX', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('accelerometerY', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('accelerometerZ', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('magnetometerX', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('magnetometerY', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('magnetometerZ', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('gyroscopeX', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('gyroscopeY', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('gyroscopeZ', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('tempmax', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('tempmax_hyst', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('tempinput', deviceType.id);

----------------------------------------------
-- Commands that apply to this device type.
----------------------------------------------

----------------------------------------------
-- Alert types specifically for this dev type.
----------------------------------------------
    INSERT INTO alert_type(name, description, source) values('unts-abnormal-traffic', 'There is network traffic coming from the device that differs from standard responses.', 'umbox');
    INSERT INTO alert_type(name, description, source) values('unts-acceleration', 'unts.acceleration > Z', 'Iot Interface');
    INSERT INTO alert_type(name, description, source) values('unts-gyro', 'unts.gyroscope != (X±d), (Y±d), (Z±d)', 'Iot Interface');
    INSERT INTO alert_type(name, description, source) values('unts-magnetometer', 'unts.magnetometer != (X±d), (Y±d), (Z±d)', 'Iot Interface');
    INSERT INTO alert_type(name, description, source) values('unts-temperature', 'unts.temp > X OR unts.temp < Y', 'Iot Interface');
    INSERT INTO alert_type(name, description, source) values('unts-temperature-avg', 'unts.temp >/< avg(last N) ± D && state = suspicious', 'Iot Interface');

----------------------------------------------
-- Associating alert types to the device type.
----------------------------------------------
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'device-unavailable'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'state-reset'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name='brute-force'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name = 'unts-abnormal-traffic'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name = 'unts-acceleration'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-gryo'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-magnetometer'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-temperature'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-temperature-avg'), deviceType.id);

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
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u6-udoo-brute-force', 'ssd_lab/u6-udoo-brute-force') RETURNING id INTO u6;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u7-udoo-brute-force-block', 'ssd_lab/u7-udoo-brute-force-block') RETURNING id INTO u7;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u4-block-all', 'ssd_lab/u4-block-all') RETURNING id INTO u4;

----------------------------------------------
-- Umbox image lookups
----------------------------------------------
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (normal.id, deviceType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (normal.id, deviceType.id, u6.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, deviceType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, deviceType.id, u7.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (attack.id, deviceType.id, u4.id, 1);

END;
$$ LANGUAGE plpgsql;

SELECT configureUdooNeo();






