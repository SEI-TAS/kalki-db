CREATE OR REPLACE FUNCTION insertDevices()
    RETURNS VOID AS $$
DECLARE
    dataNode RECORD;
BEGIN
    INSERT INTO data_node(name, ip_address) VALUES ('Hertz', '10.27.151.127') RETURNING id INTO dataNode;

    INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate, data_node_id) VALUES ('DLC1', 'Main camera', (SELECT id FROM device_type WHERE name='DLink Camera'), '10.27.151.114',10000, 10000, 10000, dataNode.id);

    INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate, data_node_id) VALUES ('PHLE1', 'Basement lights', (SELECT id FROM device_type WHERE name='Philips Hue Light Emulator'), '10.27.151.106:80',10000, 10000, 10000, dataNode.id);
    INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate, data_node_id) VALUES ('PHLE2', 'Basement lights', (SELECT id FROM device_type WHERE name='Philips Hue Light Emulator'), '10.27.151.106:80',10000, 10000, 10000, dataNode.id);

    INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate, data_node_id) VALUES ('UNTS1', 'Front sensor', (SELECT id FROM device_type WHERE name='Udoo Neo'), '10.27.151.101',10000, 10000, 10000, dataNode.id);

    INSERT INTO device(name, description, type_id, ip_address, status_history_size, sampling_rate, default_sampling_rate, data_node_id) VALUES ('WEMO1', 'Kitchen plug', (SELECT id FROM device_type WHERE name='WeMo Insight'), '10.27.151.121',10000, 10000, 10000, dataNode.id);

END;
$$ LANGUAGE plpgsql;

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
                END LOOP;
END;
$$ LANGUAGE plpgsql;

SELECT insertDevices();
SELECT setCurrentState();
