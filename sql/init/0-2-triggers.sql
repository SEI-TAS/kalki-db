
------------------------------------------------------------------------------------------------------------------------
-- Device Insertion triggers and functions.
------------------------------------------------------------------------------------------------------------------------

-- Function to create empty alert conditions for a given device.
-- CREATE OR REPLACE FUNCTION backFillAlertContext(deviceTypeId INTEGER, deviceId INTEGER)
--     RETURNS VOID AS $$
--         DECLARE
--             ac RECORD;
--             query TEXT;
--         BEGIN
--             query := 'SELECT * FROM alert_type_lookup WHERE device_type_id = ' || deviceTypeId;
--             FOR ac IN EXECUTE query
--                 LOOP
--                     INSERT INTO alert_context(device_id, alert_type_lookup_id) VALUES(deviceId, ac.id);
--                 END LOOP;
--         END;
--     $$ LANGUAGE plpgsql;

-- Called by trigger when there is a new device inserted, fills out alert conditions for this device's type, and sends
-- a notification up.
CREATE OR REPLACE FUNCTION deviceNotify ()
    RETURNS TRIGGER AS $$
        DECLARE
            payload TEXT;
        BEGIN
            payload := NEW.id;
        PERFORM pg_notify('deviceinsert', payload);
            RETURN NEW;
        END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS deviceNotify ON device;

-- Trigger for a new device insertion.
CREATE TRIGGER deviceNotify
    AFTER INSERT ON device
    FOR EACH ROW EXECUTE PROCEDURE deviceNotify();

------------------------------------------------------------------------------------------------------------------------
-- Device Update triggers and functions.
------------------------------------------------------------------------------------------------------------------------

-- Called when a device is updated, sends a notification up.
CREATE OR REPLACE FUNCTION deviceUpdateNotify ()
    RETURNS TRIGGER AS $$
        DECLARE
            payload TEXT;
        BEGIN
            payload := NEW.id;
            PERFORM pg_notify('deviceupdate', payload);
            RETURN NEW;
        END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS deviceUpdateNotify ON device;

-- Trigger for device update.
CREATE TRIGGER deviceUpdateNotify
    AFTER UPDATE ON device
    FOR EACH ROW EXECUTE PROCEDURE deviceUpdateNotify();

------------------------------------------------------------------------------------------------------------------------
-- Device Status triggers and functions.
------------------------------------------------------------------------------------------------------------------------

-- Called when a device status is inserted, sends a notification up.
CREATE OR REPLACE FUNCTION  deviceStatusNotify ()
    RETURNS TRIGGER AS $$
        DECLARE
            payload TEXT;
        BEGIN
            payload := NEW.id;
        PERFORM pg_notify('devicestatusinsert', payload);
            RETURN NEW;
        END;
    $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS deviceStatusNotify ON device_status;

-- Trigger for device status insertion.
CREATE TRIGGER deviceStatusNotify
    AFTER INSERT ON device_status
    FOR EACH ROW EXECUTE PROCEDURE deviceStatusNotify();

------------------------------------------------------------------------------------------------------------------------
-- Alert History triggers and functions.
------------------------------------------------------------------------------------------------------------------------

-- Called when an alert is inserted, sends a notification up.
CREATE OR REPLACE FUNCTION alertHistoryNotify()
    RETURNS TRIGGER AS $$
        DECLARE
            payload TEXT;
        BEGIN
            payload := NEW.id;
        PERFORM pg_notify('alerthistoryinsert', payload);
            RETURN NEW;
        END;
    $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS alertHistoryNotify ON alert;

-- Trigger for alert history insertion.
CREATE TRIGGER alertHistoryNotify
    AFTER INSERT ON alert
    FOR EACH ROW EXECUTE PROCEDURE alertHistoryNotify();

------------------------------------------------------------------------------------------------------------------------
-- Device Security State triggers and functions.
------------------------------------------------------------------------------------------------------------------------

-- Called when a device security state is inserted, sends a notification up.
CREATE OR REPLACE FUNCTION deviceSecurityStateNotify()
    RETURNS TRIGGER AS $$
        DECLARE
            payload TEXT;
        BEGIN
            payload := NEW.id;
        PERFORM pg_notify('devicesecuritystateinsert', payload);
            RETURN NEW;
        END;
    $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS deviceSecurityStateNotify ON device_security_state;

-- Trigger for device security state insertion.
CREATE TRIGGER deviceSecurityStateNotify
    AFTER INSERT ON device_security_state
    FOR EACH ROW EXECUTE PROCEDURE deviceSecurityStateNotify();

------------------------------------------------------------------------------------------------------------------------
-- Policy Rule Log triggers and functions.
------------------------------------------------------------------------------------------------------------------------

-- Called when a policy rule log is inserted, sends a notification up.
CREATE OR REPLACE FUNCTION policyRuleLogNotify ()
    RETURNS TRIGGER AS $$
        DECLARE
            payload TEXT;
        BEGIN
            payload := NEW.id;
            PERFORM pg_notify('policyruleloginsert', payload);
            RETURN NEW;
        END;
    $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS policyRuleLogNotify ON policy_rule_log;

-- Trigger for policy rule log insertion.
CREATE TRIGGER policyRuleLogNotify
    AFTER INSERT ON policy_rule_log
    FOR EACH ROW EXECUTE PROCEDURE policyRuleLogNotify();
