INSERT INTO device_type(name) values ('Udoo Neo');

----------------------------------------------
-- Alert types specifically for this dev type.
----------------------------------------------

INSERT INTO alert_type(name, description, source) values('unts-abnormal-traffic', 'There is network traffic coming from the device that differs from standard responses.', 'umbox');

INSERT INTO alert_type(name, description, source) values('unts-acceleration', 'unts.acceleration > Z && unts.avgAcceleration > Y', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-acceleration-avg', 'unts.acceleration > avg(last n) && state = suspicious', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-gyro', 'unts.gyroscope != (X±d), (Y±d), (Z±d)', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-gyro-avg', 'unts.gyroscope > avg(last N) && state = suspicious', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-magnetometer', 'unts.magnetometer != (X±d), (Y±d), (Z±d)', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-magnetometer-avg', 'unts.magnetometer > avg(last N) && state = suspicious', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-temperature', 'unts.temp > X OR unts.temp < Y', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-temperature-avg', 'unts.temp >/< avg(last N) ± D && state = suspicious', 'Iot Interface');

----------------------------------------------
-- Configurations for those alerts.
----------------------------------------------

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name='brute-force'), (SELECT id FROM device_type WHERE name='Udoo Neo'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES((SELECT id FROM alert_type WHERE name = 'unts-abnormal-traffic'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES((SELECT id FROM alert_type WHERE name = 'unts-acceleration'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                               '"accelerometerX"=>"0.0344","accelerometerY"=>"0.0766","accelerometerZ"=>"1.126","modulus"=>"1.13064","state"=>"Normal"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES((SELECT id FROM alert_type WHERE name = 'unts-acceleration-avg'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                               '"average"=>"50","accelerometerX"=>"0.0344","accelerometerY"=>"0.0766","accelerometerZ"=>"1.126","modulus"=>"1.13064","state"=>"Suspicious"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-gryo'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"gyroscopeX"=>"2.875","gyroscopeY"=>"1.8125","gyroscopeZ"=>"0.6875","modulus"=>"3.4675","state"=>"Normal"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-gyro-avg'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"average"=>"50","gyroscopeX"=>"2.875","gyroscopeY"=>"1.8125","gyroscopeZ"=>"0.6875","modulus"=>"3.4675","state"=>"Suspicious"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-magnetometer'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"magnetometerX"=>"115.6","magnetometerY"=>"115.6","magnetometerZ"=>"137.5","modulus"=>"213.25","state"=>"Normal"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-magnetometer-avg'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"average"=>"50","magnetometerX"=>"115.6","magnetometerY"=>"115.6","magnetometerZ"=>"137.5","modulus"=>"213.25","state"=>"Suspicious"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-temperature'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"temp_input_lower"=>"20.0", "temp_input_upper"=>"25.0","state"=>"Normal"');

INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES ((SELECT id FROM alert_type WHERE name = 'unts-temperature-avg'), (SELECT id FROM device_type WHERE name = 'Udoo Neo'),
                                                                                '"average"=>"50","state"=>"Suspicious"');
