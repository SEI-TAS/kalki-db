CREATE OR REPLACE FUNCTION configurePhle()
    RETURNS VOID AS $$
DECLARE
    deviceType RECORD;
    normal RECORD;
    suspicious RECORD;
    attack RECORD;
    u1 RECORD;
    u9 RECORD;
    u10 RECORD;
    u4 RECORD;
BEGIN
----------------------------------------------
-- Device type itself.
----------------------------------------------
    INSERT INTO device_type(name) values ('Philips Hue Light Emulator') RETURNING id INTO deviceType;

----------------------------------------------
-- Device type sensors.
----------------------------------------------
    INSERT INTO device_sensor(name, type_id) values ('hue', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('isOn', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('brightness', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('name', deviceType.id);
    INSERT INTO device_sensor(name, type_id) values ('lightId', deviceType.id);

----------------------------------------------
-- Commands that apply to this device type.
----------------------------------------------
    INSERT INTO command(name, device_type_id) values ('turn-on',  deviceType.id);
    INSERT INTO command(name, device_type_id) values ('turn-off', deviceType.id);

----------------------------------------------
-- Alert types specifically for this dev type.
----------------------------------------------
    INSERT INTO alert_type(name, description, source) values('phle-time-on', 'PHLE.on/off = on && !DLC.motion_sense > T minutes', 'Iot Interface');
    INSERT INTO alert_type(name, description, source) values('phle-odd-one-out', 'PHLE.on/off = off && PHLE.on/off = ON (∀ PHLE in same group)', 'Iot Interface');

----------------------------------------------
-- Associating alert types to the device type.
----------------------------------------------
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'device-unavailable'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'state-reset'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name='brute-force'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'phle-time-on'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'phle-odd-one-out'), deviceType.id);

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
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u9-phillips-brute-force', 'ssd_lab/u9-phillips-brute-force') RETURNING id INTO u9;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u10-phillips-brute-force-restrict', 'ssd_lab/u10-phillips-brute-force-restrict') RETURNING id INTO u10;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u4-block-all', 'ssd_lab/u4-block-all') RETURNING id INTO u4;

----------------------------------------------
-- Umbox image lookups
----------------------------------------------
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (normal.id, deviceType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (normal.id, deviceType.id, u9.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, deviceType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, deviceType.id, u10.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (attack.id, deviceType.id, u4.id, 1);

END;
$$ LANGUAGE plpgsql;

SELECT configurePhle();






