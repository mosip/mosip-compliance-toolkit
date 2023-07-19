\c mosip_toolkit sysadmin

ALTER TABLE toolkit.abis_projects Add COLUMN modality character varying(256) NOT NULL DEFAULT 'All';
COMMENT ON COLUMN toolkit.abis_projects.modality IS 'modality: different modalities combination';

-- This table has all the data share tokens for a test run for a given collection in ABIS project.
CREATE TABLE toolkit.datashare_tokens(
    partner_id character varying(36) NOT NULL,
    testcase_id character varying(36) NOT NULL,
    testrun_id character varying(36) NOT NULL,
    token character varying NOT NULL,
    result character varying(256),
    CONSTRAINT datashare_tokens_pk PRIMARY KEY (partner_id, testCase_id, testRun_id) 
);
COMMENT ON TABLE toolkit.datashare_tokens IS 'This table has all the data share tokens for a test run for a given collection in ABIS project.';
COMMENT ON COLUMN toolkit.datashare_tokens.partner_id IS 'Partner Id: partner id who has created this project.';
COMMENT ON COLUMN toolkit.datashare_tokens.testcase_id IS 'Testcase ID: Id of the corresponding testcase.';
COMMENT ON COLUMN toolkit.datashare_tokens.testrun_id IS 'testRun_id: Unique run Id generated for an test run.';
COMMENT ON COLUMN toolkit.datashare_tokens.token IS 'token: Data share token test run';
COMMENT ON COLUMN toolkit.datashare_tokens.result IS 'result: result of data share tokens.';