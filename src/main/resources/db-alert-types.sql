INSERT INTO alert_type(name, description, source) values('brute-force', 'User attempts to brute force access to device', 'umbox');

INSERT INTO alert_type(name, description, source) values('default-login', 'Login attempt using default credentials', 'umbox');

INSERT INTO alert_type(name, description, source) values('device-unavailable', 'Device is unresponsive longer than sampling rate for X number of times', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('max-login-attempts', 'A user attempts to login > X times', 'umbox');

INSERT INTO alert_type(name, description, source) values('abnormal-traffic', 'Device starts connections when it is not supposed to.', 'umbox');

INSERT INTO alert_type(name, description, source) values('state-reset', 'Admin issues state reset through dashboard', 'Dashboard');

INSERT INTO alert_type(name, description, source) values('unts-acceleration', 'unts.acceleration > Z && unts.avgAcceleration > Y', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-acceleration-avg', 'unts.acceleration > avg(last n) && state = suspicious', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-gyro', 'unts.gyroscope != (X±d), (Y±d), (Z±d)', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-gyro-avg', 'unts.gyroscope > avg(last N) && state = suspicious', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-magnetometer', 'unts.magnetometer != (X±d), (Y±d), (Z±d)', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-magnetometer-avg', 'unts.magnetometer > avg(last N) && state = suspicious', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-temperature', 'unts.temp > X OR unts.temp < Y', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-temperature-avg', 'unts.temp >/< avg(last N) ± D && state = suspicious', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('unts-abnormal-traffic', 'There is network traffic coming from the device that differs from standard responses.', 'umbox');

INSERT INTO alert_type(name, description, source) values('dlc-motion-sense', 'DLC.motion && PHLE.on/off = off', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('phle-time-on', 'PHLE.on/off = on && !DLC.motion_sense > T minutes', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('phle-odd-one-out', 'PHLE.on/off = off && PHLE.on/off = ON (∀ PHLE in same group)', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-current-mw-greater-low', 'wemo.currentmw > X', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-current-mw-greater-low-suspicious', 'wemo.currentmw > X for Y minutes', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-current-mw-greater-high', 'wemo.currentmw > Y', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-time-on', 'wemo.today_on_time > T', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-today-kwh', 'wemo.today_kwh > K', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('vizio-connected-devices', 'size of list of connected devices > 1', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('vizio-input-source', 'input source changes', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('vizio-unexpected-auth', 'If authentication attempt from outside the network', 'umbox');

INSERT INTO alert_type(name, description, source) values('vizio-combination-alert', 'Combination of input-source and connected-devices', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('roomba-unexpected-command', 'If we have a Weekly Schedule, and a command is received', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('roomba-cloud-traffic', 'If traffic from cloud API is detected', 'umbox');

INSERT INTO alert_type(name, description, source) values('roomba-unexpected-auth', 'If authentication attempt from outside the networ', 'umbox');

INSERT INTO alert_type(name, description, source) values('roomba-auth-attempts', 'If authentication attempt from outside the networ', 'umbox');
