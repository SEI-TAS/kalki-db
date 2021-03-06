
-- Base condition for state-reset.
INSERT INTO policy_condition(threshold) values(10);

INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) values(1, (SELECT at.id FROM alert_type AS at WHERE at.name = 'state-reset'));

-- Policy condition for state-reset.
INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES(
                                                                                                 (SELECT st.id FROM state_transition AS st WHERE st.start_sec_state_id = 3 AND st.finish_sec_state_id = 1),
                                                                                                 1,
                                                                                                 null,
                                                                                                 1
                                                                                             );

INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES(
                                                                                                 (SELECT st.id FROM state_transition AS st WHERE st.start_sec_state_id = 2 AND st.finish_sec_state_id = 1),
                                                                                                 1,
                                                                                                 null,
                                                                                                 1
                                                                                             );
