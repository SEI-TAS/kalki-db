INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'brute-force';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'default-login';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'device-unavailable';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'max-login-attempts';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'state-reset';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name = 'unts-abnormal-traffic'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES((SELECT id FROM alert_type WHERE name = 'unts-acceleration'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                               '"accelerometerX"=>"0.01","accelerometerY"=>"0.0766","accelerometerZ"=>"1.126","modulus"=>"1.12864"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-gryo'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"gyroscopeX"=>"45","gyroscopeY"=>"60","gyroscopeZ"=>"15","modulus"=>"76.5"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-gyro-secondary'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"modulus"=>"76.5","lastN"=>"50"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-magnetometer'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"magnetometerX"=>"90","magnetometery"=>"90","magnetometerZ"=>"100","modulus"=>"168.226"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-magnetometer-online-low'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),'');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-magnetometer-online-high'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),'');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-temperature'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"temp_input_lower"=>"20.0", "temp_input_upper"=>"20.0"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-temperature-avg'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"average"=>"50"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-temperature-online'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),'');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'dlc-motion-sense'), (SELECT id FROM device_type WHERE name = 'DLink Camera'),
                                                                                '"motion_detected"=>"true","isOn"=>"true"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'phle-time-off'), (SELECT id FROM device_type WHERE name = 'Phillips Hue Light Emulator'),
                                                                                '"isOn"=>"false","time-last-change"=>"480"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'phle-odd-one-out'), (SELECT id FROM device_type WHERE name = 'Phillips Hue Light Emulator'),
                                                                                '"isOn"=>"false","group_isOn"=>"true"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-current-mw-greater-low'), (SELECT id FROM device_type WHERE name = 'WeMo Insight'),
                                                                                '"currentmw"=>"17040"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-current-mw-greater-high'), (SELECT id FROM device_type WHERE name = 'WeMo Insight'),
                                                                                '"currentmw"=>"17050"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-current-mw-same-group'), (SELECT id FROM device_type WHERE name = 'WeMo Insight'),
                                                                                '');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-last-change'), (SELECT id FROM device_type WHERE name = 'WeMo Insight'),
                                                                                '"lastchange"=>"10","status"=>"on"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-time-on'), (SELECT id FROM device_type WHERE name = 'WeMo Insight'),
                                                                                '"today_on_time"=>"32400"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-today-kwh'), (SELECT id FROM device_type WHERE name = 'WeMo Insight'),
                                                                                '"today_kwh"=>"0.220"');
