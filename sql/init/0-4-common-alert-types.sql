INSERT INTO alert_type(name, description, source) values('device-unavailable', 'Device is unresponsive longer than sampling rate for X number of times', 'Device');

INSERT INTO alert_type(name, description, source) values('state-reset', 'Admin issues state reset through dashboard', 'Dashboard');

INSERT INTO alert_type(name, description, source) values('brute-force', 'User attempts to brute force access to device', 'Network');

INSERT INTO alert_type(name, description, source) values('default-login', 'Login attempt using default credentials', 'Network');

INSERT INTO alert_type(name, description, source) values('max-login-attempts', 'A user attempts to login > X times', 'Network');
