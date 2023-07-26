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

ALTER TABLE toolkit.sbi_projects Add COLUMN device_images character varying(256) NOT NULL DEFAULT 'To_Be_Added';
ALTER TABLE toolkit.sbi_projects Add COLUMN sbi_hash character varying(256) NOT NULL DEFAULT 'To_Be_Added';
ALTER TABLE toolkit.sbi_projects Add COLUMN website_url character varying(256) NOT NULL DEFAULT 'To_Be_Added';
COMMENT ON COLUMN toolkit.sbi_projects.device_images IS 'device_images: Name of the device images';
COMMENT ON COLUMN toolkit.sbi_projects.sbi_hash IS 'sbi_hash: Encoded hash of SBI installation file';
COMMENT ON COLUMN toolkit.sbi_projects.website_url IS 'website_url: Partner website url';

ALTER TABLE toolkit.sdk_projects Add COLUMN sdk_hash character varying(256) NOT NULL DEFAULT 'To_Be_Added';
ALTER TABLE toolkit.sdk_projects Add COLUMN website_url character varying(256) NOT NULL DEFAULT 'To_Be_Added';
COMMENT ON COLUMN toolkit.sdk_projects.sdk_hash IS 'sdk_hash: Encoded hash of SDK installation file';
COMMENT ON COLUMN toolkit.sdk_projects.website_url IS 'website_url: Partner website url';

ALTER TABLE toolkit.abis_projects Add COLUMN abis_hash character varying(256) NOT NULL DEFAULT 'To_Be_Added';
ALTER TABLE toolkit.abis_projects Add COLUMN website_url character varying(256) NOT NULL DEFAULT 'To_Be_Added';
COMMENT ON COLUMN toolkit.abis_projects.abis_hash IS 'abis_hash: Encoded hash of ABIS installation file';
COMMENT ON COLUMN toolkit.abis_projects.website_url IS 'website_url: Partner website url';