INSERT INTO command_lookup(device_type_id, state_id, name) values ((SELECT id FROM device_type WHERE name='Udoo Neo'), (SELECT id FROM security_state WHERE name='Normal'), 'turn-off' );
