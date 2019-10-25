INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'device-unavailable';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'state-reset';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name='brute-force'), (SELECT id FROM device_type WHERE name='Udoo Neo'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name = 'unts-abnormal-traffic'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES((SELECT id FROM alert_type WHERE name = 'unts-acceleration'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                               '"accelerometerX"=>"0.0344","accelerometerY"=>"0.0766","accelerometerZ"=>"1.126","modulus"=>"1.12964","state"=>"Normal"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES((SELECT id FROM alert_type WHERE name = 'unts-acceleration-avg'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                               '"average"=>"50","accelerometerX"=>"0.0344","accelerometerY"=>"0.0766","accelerometerZ"=>"1.126","modulus"=>"1.12964","state"=>"Suspicious"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-gryo'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"gyroscopeX"=>"2.875","gyroscopeY"=>"1.8125","gyroscopeZ"=>"0.6875","modulus"=>"3.4675","state"=>"Normal"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-gyro-avg'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"average"=>"50","gyroscopeX"=>"2.875","gyroscopeY"=>"1.8125","gyroscopeZ"=>"0.6875","modulus"=>"3.4675","state"=>"Suspicious"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-magnetometer'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"magnetometerX"=>"76.6","magnetometerY"=>"112.1","magnetometerZ"=>"117.1","modulus"=>"179.29","state"=>"Normal"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-magnetometer-avg'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"average"=>"50","magnetometerX"=>"76.6","magnetometerY"=>"112.1","magnetometerZ"=>"117.1","modulus"=>"179.29","state"=>"Suspicious"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-temperature'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"temp_input_lower"=>"20.0", "temp_input_upper"=>"25.0","state"=>"Normal"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-temperature-avg'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"average"=>"50","state"=>"Suspicious"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'max-login-attempts'), (SELECT id FROM device_type WHERE name = 'DLink Camera'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'dlc-motion-sense'), (SELECT id FROM device_type WHERE name = 'DLink Camera'),
                                                                                '"motion_detected"=>"true","isOn"=>"true"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'phle-time-on'), (SELECT id FROM device_type WHERE name = 'Phillips Hue Light Emulator'),
                                                                                '"isOn"=>"false","time-last-change"=>"30"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'phle-odd-one-out'), (SELECT id FROM device_type WHERE name = 'Phillips Hue Light Emulator'),
                                                                                '"isOn"=>"false","group_isOn"=>"true"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name='brute-force'), (SELECT id FROM device_type WHERE name='Phillips Hue Light Emulator'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-current-mw-greater-low'), (SELECT id FROM device_type WHERE name = 'WeMo Insight'),
                                                                                '"currentmw"=>"17040","state"=>"Normal"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-current-mw-greater-low-suspicious'), (SELECT id FROM device_type WHERE name = 'WeMo Insight'),
                                                                                '"currentmw"=>"17040","duration"=>"10","state"=>"Suspicious"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-current-mw-greater-high'), (SELECT id FROM device_type WHERE name = 'WeMo Insight'),
                                                                                '"currentmw"=>"17050"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-time-on'), (SELECT id FROM device_type WHERE name = 'WeMo Insight'),
                                                                                '"today_on_time"=>"32400"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'wemo-today-kwh'), (SELECT id FROM device_type WHERE name = 'WeMo Insight'),
                                                                                '"today_kwh"=>"0.220"');
