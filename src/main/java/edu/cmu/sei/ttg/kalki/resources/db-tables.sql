CREATE TABLE IF NOT EXISTS alert_type(
    id                 serial PRIMARY KEY,
    name               varchar(255) NOT NULL,
    description        varchar(255),
    source             varchar(255)
);

CREATE TABLE IF NOT EXISTS device_type(
    id    serial PRIMARY KEY,
    name  varchar(255),
    policy_file    bytea,
    policy_file_name    varchar(255)
);

CREATE TABLE IF NOT EXISTS device_group(
    id    serial PRIMARY KEY,
    name  varchar(255)
);

CREATE TABLE IF NOT EXISTS security_state(
    id     serial PRIMARY KEY,
    name   varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS tag(
    id           serial PRIMARY KEY,
    name         varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS umbox_image(
    id           serial PRIMARY KEY,
    name         varchar(255) NOT NULL,
    path         varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS device(
    id                     serial PRIMARY KEY,
    name                   varchar(255) NOT NULL,
    description            varchar(255),
    type_id                int NOT NULL REFERENCES device_type(id),
    group_id               int,
    ip_address             varchar(255),
    status_history_size    int NOT NULL,
    sampling_rate          int NOT NULL,
    current_state_id       int,
    last_alert_id          int
);

CREATE TABLE IF NOT EXISTS device_status(
    device_id     int NOT NULL REFERENCES device(id),
    attributes    hstore,
    timestamp     TIMESTAMP,
    id            serial    PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS device_security_state(
    id           serial PRIMARY KEY,
    device_id    int NOT NULL REFERENCES device(id),
    timestamp    TIMESTAMP,
    state_id     int NOT NULL
);

CREATE TABLE IF NOT EXISTS device_tag(
    device_id    int NOT NULL REFERENCES device(id),
    tag_id       int NOT NULL REFERENCES tag(id),
    PRIMARY KEY (device_id, tag_id)
);

CREATE TABLE IF NOT EXISTS command(
    id                 serial PRIMARY KEY,
    name               varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS command_lookup(
    id                 serial PRIMARY KEY,
    device_type_id     int NOT NULL REFERENCES device_type(id),
    state_id           int NOT NULL REFERENCES security_state(id),
    command_id         int NOT NULL REFERENCES command(id),
    UNIQUE(device_type_id, state_id, command_id)
);

CREATE TABLE IF NOT EXISTS umbox_instance(
    id                 serial PRIMARY KEY,
    alerter_id         varchar(255) UNIQUE,
    umbox_image_id     int NOT NULL REFERENCES umbox_image(id),
    device_id          int NOT NULL REFERENCES device(id),
    started_at         timestamp NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS umbox_lookup(
    state_id           int NOT NULL,
    umbox_image_id     int NOT NULL REFERENCES umbox_image(id),
    device_type_id     int NOT NULL REFERENCES device_type(id),
    dag_order          int NOT NULL,
    PRIMARY KEY(state_id, umbox_image_id, device_type_id)
);

CREATE TABLE IF NOT EXISTS alert(
    id                 serial PRIMARY KEY,
    name               varchar(255) NOT NULL,
    timestamp          timestamp NOT NULL DEFAULT now(),
    alert_type_id      int REFERENCES alert_type(id),
    alerter_id         varchar(255) REFERENCES umbox_instance(alerter_id),
    device_status_id   int REFERENCES device_status(id)
);

CREATE TABLE IF NOT EXISTS alert_condition(
    id                 serial PRIMARY KEY,
    variables          hstore,
    device_id          int REFERENCES device(id),
    alert_type_id      int REFERENCES alert_type(id)
);

CREATE TABLE IF NOT EXISTS alert_type_lookup(
    alert_type_id      int REFERENCES alert_type(id),
    device_type_id     int REFERENCES device_type(id),
    PRIMARY KEY(alert_type_id, device_type_id)
);
