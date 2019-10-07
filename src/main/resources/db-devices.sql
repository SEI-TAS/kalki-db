INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate) VALUES ('DLC', 'Camera', (SELECT id FROM device_type WHERE name='DLink Camera'), '10.27.151.114',10000, 10000, 10000);

INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate) VALUES ('UNTS', 'Udoo Neo', (SELECT id FROM device_type WHERE name='Udoo Neo'), '10.27.151.101',10000, 10000, 10000);

INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate) VALUES ('Kalki', 'WeMo Insight', (SELECT id FROM device_type WHERE name='WeMo Insight'), '10.27.151.121',10000, 10000, 10000);

INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate) VALUES ('PHLE', 'Philips Light Emulator', (SELECT id FROM device_type WHERE name='Philips Hue Light Emulator'), '10.27.151.106:80',10000, 10000, 10000);

CREATE OR REPLACE FUNCTION setCurrentState()
    RETURNS VOID AS $$
        DECLARE
            d RECORD;
            SS RECORD;
            dSS RECORD;
            query TEXT;
        BEGIN
            query := 'SELECT id FROM device';
            FOR d IN EXECUTE query
                LOOP
                    SELECT INTO SS id FROM security_state WHERE name='Normal';
                    INSERT INTO device_security_state (device_id, state_id, timestamp) VALUES (d.id, SS.id, current_timestamp) RETURNING id INTO dSS;
                    UPDATE device set current_state_id=dSS.id WHERE id=d.id;
                END LOOP;
END;
$$ LANGUAGE plpgsql;

SELECT setCurrentState();
