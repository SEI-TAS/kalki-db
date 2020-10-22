
----------------------------------------------
-- Configurations for those alerts.
----------------------------------------------
INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'device-unavailable'), (SELECT id FROM device_type WHERE name = 'DLink Camera'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'state-reset'), (SELECT id FROM device_type WHERE name = 'DLink Camera'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'max-login-attempts'), (SELECT id FROM device_type WHERE name = 'DLink Camera'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'dlc-motion-sense'), (SELECT id FROM device_type WHERE name = 'DLink Camera'));

CREATE OR REPLACE FUNCTION configureDlink()
    RETURNS VOID AS $$
DECLARE
    deviceType RECORD;
    normal RECORD;
    suspicious RECORD;
    attack RECORD;
    u1 RECORD;
    u5 RECORD;
    u4 RECORD;
BEGIN
----------------------------------------------
-- Device type itself.
----------------------------------------------
    INSERT INTO device_type(name) values ('DLink Camera') RETURNING id INTO deviceType;

----------------------------------------------
-- Device type sensors.
----------------------------------------------
    INSERT INTO device_sensor(name, type_id) values ('motion_detected', deviceType.id);

----------------------------------------------
-- Commands that apply to this device type.
----------------------------------------------


----------------------------------------------
-- Alert types specifically for this dev type.
----------------------------------------------
    INSERT INTO alert_type(name, description, source) values('dlc-motion-sense', 'DLC.motion && PHLE.on/off = off', 'Iot Interface');

----------------------------------------------
-- Associating alert types to the device type.
----------------------------------------------
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'device-unavailable'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'state-reset'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name='brute-force'), deviceType.id);
    INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT id FROM alert_type WHERE name = 'dlc-motion-sense'), deviceType.id);

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
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u5-sniffer-log-stats', 'ssd_lab/u5-sniffer-log-stats') RETURNING id INTO u5;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u4-block-all', 'ssd_lab/u4-block-all') RETURNING id INTO u4;

----------------------------------------------
-- Umbox image lookups
----------------------------------------------
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (normal.id, deviceType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, deviceType.id, u1.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (suspicious.id, deviceType.id, u5.id, 1);
    INSERT INTO umbox_lookup(security_state_id, device_type_id, umbox_image_id, dag_order) VALUES (attack.id, deviceType.id, u4.id, 1);

END;
$$ LANGUAGE plpgsql;

SELECT configureDlink();






