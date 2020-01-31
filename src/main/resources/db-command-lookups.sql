
INSERT INTO command(name, device_type_id) values ('turn-off', (SELECT id FROM device_type WHERE name='Philips Hue Light Emulator'));

INSERT INTO command(name, device_type_id) values ('turn-on', (SELECT id FROM device_type WHERE name='Philips Hue Light Emulator'));

INSERT INTO command(name, device_type_id) values ('turn-off', (SELECT id FROM device_type WHERE name='WeMo Insight'));
