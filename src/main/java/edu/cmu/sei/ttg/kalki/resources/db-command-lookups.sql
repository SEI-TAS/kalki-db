
INSERT INTO command(name, device_type_id) values ('turn off', (SELECT id FROM device_type WHERE name='Udoo Neo'));

INSERT INTO command_lookup(state_id, command_id) values ((SELECT id FROM security_state WHERE name='Normal'), (SELECT id FROM command WHERE name='turn off'));
