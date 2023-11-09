CREATE TABLE toolkit.biometric_scores(
    id character varying(36) NOT NULL,
    project_id character varying(36) NOT NULL,
    partner_id character varying(36) NOT NULL,
    org_name character varying(64) NOT NULL,
    testrun_id character varying(36) NOT NULL,
    testcase_id character varying(36) NOT NULL,
    cr_by character varying(256) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    scores_json character varying NOT NULL,
    CONSTRAINT biometricscores_pk PRIMARY KEY (id)
);
COMMENT ON TABLE toolkit.biometric_scores IS 'This table has biometric scores';
COMMENT ON COLUMN toolkit.biometric_scores.id IS 'ID: Unique Id generated for biometric scores.';
COMMENT ON COLUMN toolkit.biometric_scores.project_id IS 'Project Id: Id of the corresponding project.';
COMMENT ON COLUMN toolkit.biometric_scores.partner_id IS 'Partner Id: partner id who has created this project.';
COMMENT ON COLUMN toolkit.biometric_scores.testrun_id IS 'testrun_id: Unique run Id generated for an test run.';
COMMENT ON COLUMN toolkit.biometric_scores.testcase_id IS 'Testcase Id: testcase id of corresponding testcase.';
COMMENT ON COLUMN toolkit.biometric_scores.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.biometric_scores.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.biometric_scores.scores_json IS 'Scores Json: Biometric scores of quality check testcases';
COMMENT ON COLUMN toolkit.biometric_scores.org_name IS 'orgname: organization name to which partner belongs to.';
