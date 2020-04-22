INSERT INTO security_state(name) values ('Normal');

INSERT INTO security_state(name) values ('Suspicious');

INSERT INTO security_state(name) values ('Attack');

-- Transitions for starting up.
INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) values(null, 1);

INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) values(null, 2);

INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) values(null, 3);

-- Regular transitions.
INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) values(1, 2);

INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) values(2, 3);

INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) values(3, 1);

INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) values(1, 3);

INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) values(3, 2);

INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) values(2, 1);

INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) values(1, 1);

INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) values(2, 2);

INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) values(3, 3);

