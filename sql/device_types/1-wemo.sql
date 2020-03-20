----------------------------------------------
-- Device type itself.
----------------------------------------------
INSERT INTO device_type(name) values ('WeMo Insight');

----------------------------------------------
-- Commands that apply to this device type.
----------------------------------------------
INSERT INTO command(name, device_type_id) values ('turn-off', (SELECT dt.id FROM device_type AS dt WHERE dt.name='WeMo Insight'));

----------------------------------------------
-- Alert types specifically for this dev type.
----------------------------------------------
INSERT INTO alert_type(name, description, source) values('wemo-current-mw-greater-low', 'wemo.currentmw > X', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-current-mw-greater-low-suspicious', 'wemo.currentmw > X for Y minutes', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-current-mw-greater-high', 'wemo.currentmw > Y', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-time-on', 'wemo.today_on_time > T', 'Iot Interface');

INSERT INTO alert_type(name, description, source) values('wemo-today-kwh', 'wemo.today_kwh > K', 'Iot Interface');

----------------------------------------------
-- Configurations for those alerts.
----------------------------------------------
INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'device-unavailable'), (SELECT id FROM device_type WHERE name = 'WeMo Insight'));

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) VALUES ((SELECT at.id FROM alert_type AS at WHERE at.name = 'state-reset'), (SELECT id FROM device_type WHERE name = 'WeMo Insight'));

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
