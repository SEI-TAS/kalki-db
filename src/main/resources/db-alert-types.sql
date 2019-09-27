INSERT INTO alert_type(name, description, source) values('brute-force', 'User attempts to brute force access to device', 'umbox');

INSERT INTO alert_type(name, description, source) values('default-login', 'Login attempt using default credentials', 'umbox');

INSERT INTO alert_type(name, description, source) values('device-unavailable', 'Device is unresponsive longer than sampling rate for X number of times', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('max-login-attempts', 'A user attempts to login > X times', 'umbox');

INSERT INTO alert_type(name, description, source) values('state-reset', 'Admin issues state reset through dashboard', 'Dashboard');

INSERT INTO alert_type(name, description, source) values('unts-acceleration', 'unts.acceleration > Z && unts.avgAcceleration > Y', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-gyro', 'unts.gyroscope != (X±d), (Y±d), (Z±d)', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-gyro-secondary', 'unts.gyroscope > avg(last N) && sampleRate > originalRate', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-magnetometer', 'unts.magnetometer != (X±d), (Y±d), (Z±d)', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-magnetometer-online-low', 'unts.magnetometer > ONLINE±X', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-magnetometer-online-high', 'unts.magnetometer > ONLINE±Y (where Y > X)', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-temperature', 'unts.temp > X OR unts.temp < Y', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-temperature-avg', 'unts.temp >/< avg(last N) ± D', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-temperature-online', 'unts.temp >/< ONLINE ± D', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-abnormal-traffic', 'There is network traffic coming from the device that differs from standard responses.', 'umbox');

INSERT INTO alert_type(name, description, source) values('dlc-motion-sense', 'DLC.motion && PHLE.on/off = off', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('phle-time-off', 'PHLE.on/off = off && PHLE.time-last-change > T minutes', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('phle-odd-one-out', 'PHLE.on/off = off && PHLE.on/off = ON (∀ PHLE in same group)', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-current-mw-greater-low', 'wemo.currentmw > X', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-current-mw-greater-high', 'wemo.currentmw > Y', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-current-mw-same-group', 'wemo.currentmw </> avg(same type/group)±X', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-last-change', 'wemo.lastchange > M-minutes && wemo.status == ON', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-time-on', 'wemo.today_on_time > T', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-today-kwh', 'wemo.today_kwh > K', 'Iot Interface');
