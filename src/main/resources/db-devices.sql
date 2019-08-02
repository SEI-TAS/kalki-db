INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate) VALUES ('DLC', 'Camera', (SELECT id FROM device_type WHERE name='DLink Camera'), '10.27.151.114',10000, 10000);

INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate) VALUES ('UNTS', 'Udoo Neo', (SELECT id FROM device_type WHERE name='Udoo Neo'), '10.27.151.101',10000, 10000);

INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate) VALUES ('WeMo', 'WeMo Insight', (SELECT id FROM device_type WHERE name='WeMo Insight'), '10.27.151.121',10000, 10000);

INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate) VALUES ('PHLE', 'Phillips Light Emulator', (SELECT id FROM device_type WHERE name='Phillips Hue Light Emulator'), '10.27.151.106:80',10000, 10000);
