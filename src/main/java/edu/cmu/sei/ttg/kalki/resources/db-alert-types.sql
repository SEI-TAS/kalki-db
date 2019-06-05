INSERT INTO alert_type(name, description, source) values('brute-force', 'User attempts to brute force access to device', 'umbox');

INSERT INTO alert_type(name, description, source) values('default-login', 'Login attempt using default credentials', 'umbox');

INSERT INTO alert_type(name, description, source) values('device-unavailable', 'Device is unresponsive longer than sampling rate for X number of times', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('max-login-attempts', 'A user attempts to login > X times', 'umbox');

INSERT INTO alert_type(name, description, source) values('state-reset', 'Admin issues state reset through dashboard', 'Dashboard');

INSERT INTO alert_type(name, description, source) values('unts-acceleration', 'unts.acceleration > Z && unts.avgAcceleration > Y', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('unts-gyro', 'unts.gyroscope != (X±d), (Y±d), (Z±d)', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('unts-gyro-secondary', 'unts.gyroscope > avg(last N) && sampleRate > originalRate', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('unts-magnetometer', 'unts.magnetometer != (X±d), (Y±d), (Z±d)', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('unts-magnetometer-online-low', 'unts.magnetometer > ONLINE±X', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('unts-magnetometer-online-high', 'unts.magnetometer > ONLINE±Y (where Y > X)', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('unts-temperature', 'unts.temp > X OR unts.temp < Y', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('unts-temperature-avg', 'unts.temp >/< avg(last N) ± D', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('unts-temperature-online', 'unts.temp >/< ONLINE ± D', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('dlc-motion-sense', 'DLC.motion && PHLE.on/off = off', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('phle-time-off', 'PHLE.on/off = off && PHLE.time-last-change > T minutes', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('phle-odd-one-out', 'PHLE.on/off = off && PHLE.on/off = ON (∀ PHLE in same group)', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('wemo-current-mw-greater-low', 'wemo.currentmw > X', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('wemo-current-mw-greater-high', 'wemo.currentmw > Y', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('wemo-current-mw-same-group', 'wemo.currentmw </> avg(same type/group)±X', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('wemo-last-change', 'wemo.lastchange > M-minutes && wemo.status == ON', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('wemo-time-on', 'wemo.today_on_time > T', 'IoT Monitor');

INSERT INTO alert_type(name, description, source) values('wemo-today-kwh', 'wemo.today_kwh > K', 'IoT Monitor');
