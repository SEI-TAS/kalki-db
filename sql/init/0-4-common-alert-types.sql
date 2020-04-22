INSERT INTO alert_type(name, description, source) values('device-unavailable', 'Device is unresponsive longer than sampling rate for X number of times', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('state-reset', 'Admin issues state reset through dashboard', 'Dashboard');

INSERT INTO alert_type(name, description, source) values('brute-force', 'User attempts to brute force access to device', 'umbox');

INSERT INTO alert_type(name, description, source) values('default-login', 'Login attempt using default credentials', 'umbox');

INSERT INTO alert_type(name, description, source) values('max-login-attempts', 'A user attempts to login > X times', 'umbox');
