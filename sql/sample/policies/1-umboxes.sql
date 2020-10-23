
CREATE OR REPLACE FUNCTION insertUmboxImages()
    RETURNS VOID AS $$
DECLARE
    u1 RECORD;
    u2 RECORD;
    u3 RECORD;
    u4 RECORD;
    u5 RECORD;
    u6 RECORD;
    u7 RECORD;
    u8 RECORD;
    u9 RECORD;
    u10 RECORD;
BEGIN
    ----------------------------------------------
    -- Umbox Images.
    ----------------------------------------------
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u1-antidos', '') RETURNING id INTO u1;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u2-http-auth-proxy', '') RETURNING id INTO u2;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u3-http-auth-proxy-block', '') RETURNING id INTO u3;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u4-block-all', '') RETURNING id INTO u4;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u5-sniffer-log-stats', '') RETURNING id INTO u5;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u6-udoo-brute-force', '') RETURNING id INTO u6;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u7-udoo-brute-force-block', '') RETURNING id INTO u7;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u8-fake-replies', '') RETURNING id INTO u8;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u9-phillips-brute-force', '') RETURNING id INTO u9;
    INSERT INTO umbox_image(name, file_name) VALUES ('kalki-ssd_lab/u10-phillips-brute-force-restrict', '') RETURNING id INTO u10;

END;
$$ LANGUAGE plpgsql;

SELECT insertUmboxImages();
