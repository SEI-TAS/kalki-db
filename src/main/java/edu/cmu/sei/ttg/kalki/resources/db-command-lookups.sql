
INSERT INTO command(name) values ('turn off');

INSERT INTO command_lookup(device_type_id, state_id, command_id) values ((SELECT id FROM device_type WHERE name='Udoo Neo'), (SELECT id FROM security_state WHERE name='Normal'), (SELECT id FROM command WHERE name='turn off'));
