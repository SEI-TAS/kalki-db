CREATE EXTENSION IF NOT EXISTS hstore;

CREATE TABLE IF NOT EXISTS device_type(
    id    serial PRIMARY KEY,
    name  varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS device_sensor(
    id serial PRIMARY KEY,
    name varchar(255) NOT NULL,
    type_id int NOT NULL REFERENCES device_type(id)
);

CREATE TABLE IF NOT EXISTS device_group(
    id    serial PRIMARY KEY,
    name  varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS tag(
    id           serial PRIMARY KEY,
    name         varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS data_node(
    id           serial PRIMARY KEY,
    name         varchar(255) NOT NULL,
    ip_address   varchar(255) NOT NULL
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
    default_sampling_rate  int NOT NULL,
    last_alert_id          int,
    data_node_id           int REFERENCES data_node(id),
    credentials            varchar(255)
);

CREATE TABLE IF NOT EXISTS device_tag(
    device_id    int NOT NULL REFERENCES device(id) ON DELETE CASCADE,
    tag_id       int NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (device_id, tag_id)
);

CREATE TABLE IF NOT EXISTS device_status(
    device_id     int NOT NULL REFERENCES device(id) ON DELETE CASCADE,
    attributes    hstore,
    timestamp     TIMESTAMP,
    id            serial    PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS security_state(
    id     serial PRIMARY KEY,
    name   varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS device_security_state(
    id           serial PRIMARY KEY,
    device_id    int NOT NULL REFERENCES device(id) ON DELETE CASCADE,
    timestamp    TIMESTAMP,
    state_id     int NOT NULL REFERENCES security_state(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS state_transition(
    id                  serial PRIMARY KEY,
    start_sec_state_id  int REFERENCES security_state(id) ON DELETE CASCADE,
    finish_sec_state_id int NOT NULL REFERENCES security_state(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS policy_condition(
    id          serial PRIMARY KEY,
    threshold   int NOT NULL
);

CREATE TABLE IF NOT EXISTS policy_rule(
    id                      serial PRIMARY KEY,
    state_trans_id          int NOT NULL REFERENCES state_transition(id) ON DELETE CASCADE,
    policy_cond_id          int REFERENCES policy_condition(id) ON DELETE CASCADE,
    device_type_id          int REFERENCES device_type(id)      ON DELETE CASCADE,
    sampling_rate_factor    int NOT NULL DEFAULT 1,
    UNIQUE(state_trans_id, policy_cond_id, device_type_id)
);

CREATE TABLE IF NOT EXISTS policy_rule_log(
    id          serial PRIMARY KEY,
    policy_rule_id   int NOT NULL REFERENCES policy_rule(id),
    device_id   int NOT NULL REFERENCES device(id) ON DELETE CASCADE,
    timestamp   TIMESTAMP
);

CREATE TABLE IF NOT EXISTS alert_type(
    id                 serial PRIMARY KEY,
    name               varchar(255) NOT NULL,
    description        varchar(255),
    source             varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS policy_condition_alert(
    policy_cond_id      int REFERENCES policy_condition(id) ON DELETE CASCADE,
    alert_type_id       int REFERENCES alert_type(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS umbox_image(
    id           serial PRIMARY KEY,
    name         varchar(255) NOT NULL,
    file_name     varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS umbox_lookup(
    id                 serial PRIMARY KEY,
    security_state_id  int NOT NULL REFERENCES security_state(id),
    device_type_id     int NOT NULL REFERENCES device_type(id),
    umbox_image_id     int NOT NULL REFERENCES umbox_image(id),
    dag_order          int NOT NULL
);

CREATE TABLE IF NOT EXISTS umbox_instance(
    id                 serial PRIMARY KEY,
    alerter_id         varchar(255) UNIQUE,
    umbox_image_id     int NOT NULL REFERENCES umbox_image(id),
    device_id          int NOT NULL REFERENCES device(id) ON DELETE CASCADE,
    started_at         timestamp NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS alert_type_lookup(
    id                 serial PRIMARY KEY,
    alert_type_id      int REFERENCES alert_type(id) ON DELETE CASCADE,
    device_type_id     int REFERENCES device_type(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS alert_context(
    id                 serial PRIMARY KEY,
    alert_type_lookup_id      int NOT NULL REFERENCES alert_type_lookup(id) ON DELETE CASCADE,
    logical_operator   varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS alert_condition(
    id                 serial PRIMARY KEY,
    context_id         int NOT NULL REFERENCES alert_context(id) ON DELETE CASCADE,
    attribute_id       int NOT NULL REFERENCES device_sensor(id) ON DELETE CASCADE,
    num_statuses       int NOT NULL,
    comparison_operator varchar(255) NOT NULL,
    calculation        varchar(255) NOT NULL,
    threshold_value    varchar(255)
);

CREATE TABLE IF NOT EXISTS alert(
    id                 serial PRIMARY KEY,
    name               varchar(255) NOT NULL,
    timestamp          timestamp NOT NULL DEFAULT now(),
    alert_type_id      int REFERENCES alert_type(id) ON DELETE CASCADE,
    device_id          int REFERENCES device(id) ON DELETE CASCADE,
    alerter_id         varchar(255) REFERENCES umbox_instance(alerter_id) ON DELETE CASCADE,
    device_status_id   int REFERENCES device_status(id),
    info               varchar(255)
);

CREATE TABLE IF NOT EXISTS command(
    id                 serial PRIMARY KEY,
    name               varchar(255) NOT NULL,
    device_type_id     int NOT NULL REFERENCES device_type(id)
);

CREATE TABLE IF NOT EXISTS command_lookup(
    id                 serial PRIMARY KEY,
    command_id         int NOT NULL REFERENCES command(id),
    policy_rule_id          int NOT NULL REFERENCES policy_rule(id)
);

CREATE TABLE IF NOT EXISTS stage_log(
    id                   serial PRIMARY KEY,
    device_sec_state_id  int NOT NULL REFERENCES device_security_state(id) ON DELETE CASCADE,
    timestamp            timestamp NOT NULL DEFAULT now(),
    action               varchar(255) NOT NULL,
    stage                varchar(255) NOT NULL,
    info                 varchar(255)
);

CREATE TABLE IF NOT EXISTS umbox_log(
    id                  serial PRIMARY KEY,
    alerter_id          varchar(255) REFERENCES umbox_instance(alerter_id) ON DELETE CASCADE,
    details             varchar(255),
    timestamp           timestamp NOT NULL DEFAULT now()
);
