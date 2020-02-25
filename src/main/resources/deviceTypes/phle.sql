----------------------------------------------
-- Device type itself.
----------------------------------------------
INSERT INTO device_type(name) values ('Philips Hue Light Emulator');

----------------------------------------------
-- Commands that apply to this device type.
----------------------------------------------
INSERT INTO command(name, device_type_id) values ('turn-on',  (SELECT dt.id FROM device_type AS dt WHERE dt.name = 'Philips Hue Light Emulator'));

INSERT INTO command(name, device_type_id) values ('turn-off', (SELECT dt.id FROM device_type AS dt WHERE dt.name = 'Philips Hue Light Emulator'));

----------------------------------------------
-- Alert types specifically for this dev type.
----------------------------------------------
INSERT INTO alert_type(name, description, source) values('phle-time-on', 'PHLE.on/off = on && !DLC.motion_sense > T minutes', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('phle-odd-one-out', 'PHLE.on/off = off && PHLE.on/off = ON (∀ PHLE in same group)', 'Iot Interface');

----------------------------------------------
-- Alerts that apply to this device type.
----------------------------------------------
INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'device-unavailable'), (SELECT id FROM device_type WHERE name = 'Philips Hue Light Emulator'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'state-reset'), (SELECT id FROM device_type WHERE name = 'Philips Hue Light Emulator'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name='brute-force'), (SELECT id FROM device_type WHERE name='Phillips Hue Light Emulator'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'phle-time-on'), (SELECT id FROM device_type WHERE name = 'Philips Hue Light Emulator'),
                                                                                '"isOn"=>"false","time-last-change"=>"30"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'phle-odd-one-out'), (SELECT id FROM device_type WHERE name = 'Philips Hue Light Emulator'),
                                                                                '"isOn"=>"false","group_isOn"=>"true"');
