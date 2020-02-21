INSERT INTO device_type(name) values ('DLink Camera');

----------------------------------------------
-- Alert types specifically for this dev type.
----------------------------------------------

INSERT INTO alert_type(name, description, source) values('dlc-motion-sense', 'DLC.motion && PHLE.on/off = off', 'Iot Interface');

----------------------------------------------
-- Configurations for those alerts.
----------------------------------------------
INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'max-login-attempts'), (SELECT id FROM device_type WHERE name = 'DLink Camera'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'dlc-motion-sense'), (SELECT id FROM device_type WHERE name = 'DLink Camera'),
                                                                                '"motion_detected"=>"true","isOn"=>"true"');
