
INSERT INTO command(name, device_type_id) values ('turn off', (SELECT id FROM device_type WHERE name='Philips Hue Light Emulator'));

INSERT INTO command(name, device_type_id) values ('turn on', (SELECT id FROM device_type WHERE name='Philips Hue Light Emulator'));

INSERT INTO command_lookup(current_state_id, previous_state_id, command_id) values ((SELECT id FROM security_state WHERE name='Normal'), (SELECT id FROM security_state WHERE name='Normal'), (SELECT id FROM command WHERE name='turn off' AND device_type_id=(SELECT id FROM device_type WHERE name='Philips Hue Light Emulator')));

INSERT INTO command_lookup(current_state_id, previous_state_id, command_id) values ((SELECT id FROM security_state WHERE name='Suspicious'), (SELECT id FROM security_state WHERE name='Suspicious'), (SELECT id FROM command WHERE name='turn off' AND device_type_id=(SELECT id FROM device_type WHERE name='Philips Hue Light Emulator')));

INSERT INTO command_lookup(current_state_id, previous_state_id, command_id) values ((SELECT id FROM security_state WHERE name='Suspicious'), (SELECT id FROM security_state WHERE name='Normal'), (SELECT id FROM command WHERE name='turn on' AND device_type_id=(SELECT id FROM device_type WHERE name='Philips Hue Light Emulator')));
