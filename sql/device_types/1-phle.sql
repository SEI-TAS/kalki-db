CREATE OR REPLACE FUNCTION configurePhle()
    RETURNS VOID AS $$
DECLARE
    deviceType RECORD;
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

END;
$$ LANGUAGE plpgsql;

SELECT configurePhle();
