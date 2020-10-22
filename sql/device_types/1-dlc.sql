CREATE OR REPLACE FUNCTION configureDlink()
    RETURNS VOID AS $$
DECLARE
    deviceType RECORD;
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

END;
$$ LANGUAGE plpgsql;

SELECT configureDlink();






