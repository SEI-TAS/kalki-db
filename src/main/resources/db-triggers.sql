CREATE OR REPLACE FUNCTION backFillAlertConditions(deviceTypeId INTEGER, deviceId INTEGER)
    RETURNS VOID AS $$
        DECLARE
            ac RECORD;
            query TEXT;
        BEGIN
            query := 'SELECT * FROM alert_type_lookup WHERE device_type_id = ' || deviceTypeId;
            FOR ac IN EXECUTE query
                LOOP
                    INSERT INTO alert_condition(variables, device_id, alert_type_lookup_id) VALUES(ac.variables, deviceId, ac.id);
                END LOOP;
        END;
    $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION deviceNotify ()
    RETURNS TRIGGER AS $$
        DECLARE
            payload TEXT;
        BEGIN
            payload := NEW.id;
        PERFORM backFillAlertConditions(NEW.type_id, NEW.id);
        PERFORM pg_notify('deviceinsert', payload);
            RETURN NEW;
        END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS deviceNotify ON device;

CREATE TRIGGER deviceNotify
    AFTER INSERT ON device
    FOR EACH ROW EXECUTE PROCEDURE deviceNotify();

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

CREATE TRIGGER deviceUpdateNotify
    AFTER UPDATE ON device
    FOR EACH ROW EXECUTE PROCEDURE deviceUpdateNotify();

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

CREATE TRIGGER deviceStatusNotify
    AFTER INSERT ON device_status
    FOR EACH ROW EXECUTE PROCEDURE deviceStatusNotify();

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

CREATE TRIGGER alertHistoryNotify
    AFTER INSERT ON alert
    FOR EACH ROW EXECUTE PROCEDURE alertHistoryNotify();

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

CREATE TRIGGER deviceSecurityStateNotify
    AFTER INSERT ON device_security_state
    FOR EACH ROW EXECUTE PROCEDURE deviceSecurityStateNotify()

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

CREATE TRIGGER policyRuleLogNotify
    AFTER INSERT ON policy_rule_log
    FOR EACH ROW EXECUTE PROCEDURE policyRuleLogNotify();
