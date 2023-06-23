\c mosip_toolkit sysadmin

COMMENT ON COLUMN toolkit.abis_projects.modality IS NULL;
COMMENT ON TABLE toolkit.datashare_tokens IS 'This table has all the data share tokens for a test run for a given collection in ABIS project.';
COMMENT ON TABLE toolkit.datashare_tokens.partner_id IS 'Partner Id: partner id who has created this project.';
COMMENT ON TABLE toolkit.datashare_tokens.testCase_id IS 'Testcase ID: Id of the corresponding testcase.';
COMMENT ON TABLE toolkit.datashare_tokens.testRun_id IS 'testRun_id: Unique run Id generated for an test run.'
COMMENT ON TABLE toolkit.datashare_tokens.token IS 'token: Data share token test run';
COMMENT ON TABLE toolkit.datashare_tokens.result IS 'result: result of data share tokens.';
ALTER TABLE toolkit.abis_projects DROP COLUMN modality;
CREATE TABLE toolkit.datashare_tokens(
    partner_id character varying(36) NOT NULL,
    testCase_id character varying(36) NOT NULL,
    testRun_id character varying(36) NOT NULL,
    token character varying(256) NOT NULL,
    result character varying(256)
)
CREATE INDEX IF NOT EXISTS idx_datashare_tokens_partner_id_testCase_id_testRun_id ON toolkit.datashare_tokens USING btree (partner_id, testCase_id, testRun_id);