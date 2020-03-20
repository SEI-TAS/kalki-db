INSERT INTO alert_type(name, description, source) values('device-unavailable', 'Device is unresponsive longer than sampling rate for X number of times', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('state-reset', 'Admin issues state reset through dashboard', 'Dashboard');

INSERT INTO alert_type(name, description, source) values('brute-force', 'User attempts to brute force access to device', 'umbox');

INSERT INTO alert_type(name, description, source) values('default-login', 'Login attempt using default credentials', 'umbox');

INSERT INTO alert_type(name, description, source) values('max-login-attempts', 'A user attempts to login > X times', 'umbox');

INSERT INTO policy_condition(threshold) values(10);

INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) values(1, 2);
