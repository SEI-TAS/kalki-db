INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'brute-force';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'default-login';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'device-unavailable';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'max-login-attempts';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'state-reset';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'unts-acceleration' AND dt.name = 'Udoo Neo';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'unts-gyro' AND dt.name = 'Udoo Neo';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'unts-gyro-secondary' AND dt.name = 'Udoo Neo';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'unts-magnetometer' AND dt.name = 'Udoo Neo';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'unts-magnetometer-online-low' AND dt.name = 'Udoo Neo';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'unts-magnetometer-online-high' AND dt.name = 'Udoo Neo';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'unts-temperature' AND dt.name = 'Udoo Neo';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'unts-temperature-avg' AND dt.name = 'Udoo Neo';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'unts-temperature-online' AND dt.name = 'Udoo Neo';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'dlc-motion-sense' AND dt.name = 'DLink Camera';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'phle-time-off' AND dt.name = 'Phillips Hue Light Emulator';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'phle-odd-one-out' AND dt.name = 'Phillips Hue Light Emulator';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'wemo-current-mw-greater-low' AND dt.name = 'WeMo Insight';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'wemo-current-mw-greater-high' AND dt.name = 'WeMo Insight';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'wemo-current-mw-same-group' AND dt.name = 'WeMo Insight';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'wemo-last-change' AND dt.name = 'WeMo Insight';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'wemo-time-on' AND dt.name = 'WeMo Insight';

INSERT INTO alert_type_lookup(alert_type_id, device_type_id) SELECT at.id, dt.id FROM alert_type AS at, device_type AS dt WHERE at.name = 'wemo-today-kwh' AND dt.name = 'WeMo Insight';
