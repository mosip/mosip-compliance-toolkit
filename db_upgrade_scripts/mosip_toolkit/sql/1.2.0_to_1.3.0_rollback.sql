\c mosip_toolkit sysadmin

DROP TABLE IF EXISTS toolkit.datashare_tokens;

COMMENT ON COLUMN toolkit.abis_projects.modality IS NULL;
ALTER TABLE toolkit.abis_projects DROP COLUMN modality;

COMMENT ON COLUMN toolkit.sbi_projects.device_image1 IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.device_image2 IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.device_image3 IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.device_image4 IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.sbi_hash IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.website_url IS NULL;
ALTER TABLE toolkit.sbi_projects DROP COLUMN device_image1;
ALTER TABLE toolkit.sbi_projects DROP COLUMN device_image2;
ALTER TABLE toolkit.sbi_projects DROP COLUMN device_image3;
ALTER TABLE toolkit.sbi_projects DROP COLUMN device_image4;
ALTER TABLE toolkit.sbi_projects DROP COLUMN sbi_hash;
ALTER TABLE toolkit.sbi_projects DROP COLUMN website_url;

COMMENT ON COLUMN toolkit.sdk_projects.sdk_hash IS NULL;
COMMENT ON COLUMN toolkit.sdk_projects.website_url IS NULL;
ALTER TABLE toolkit.sdk_projects DROP COLUMN sdk_hash;
ALTER TABLE toolkit.sdk_projects DROP COLUMN website_url;

COMMENT ON COLUMN toolkit.abis_projects.abis_hash IS NULL;
COMMENT ON COLUMN toolkit.abis_projects.website_url IS NULL;
ALTER TABLE toolkit.abis_projects DROP COLUMN abis_hash;
ALTER TABLE toolkit.abis_projects DROP COLUMN website_url;

ALTER TABLE toolkit.test_run DROP CONSTRAINT test_run_execution_status_values;
ALTER TABLE toolkit.test_run DROP CONSTRAINT test_run_run_status_values;


COMMENT ON COLUMN toolkit.test_run.execution_status IS NULL;
COMMENT ON COLUMN toolkit.test_run.run_status IS NULL;
ALTER TABLE toolkit.test_run DROP COLUMN execution_status;
ALTER TABLE toolkit.test_run DROP COLUMN run_status;

COMMENT ON COLUMN toolkit.collections.collection_type IS NULL;
ALTER TABLE toolkit.collections DROP COLUMN collection_type;

DROP TABLE IF EXISTS toolkit.biometric_scores;

ALTER TABLE toolkit.test_run_details DROP COLUMN method_id;
COMMENT ON COLUMN toolkit.test_run_details.method_id IS NULL;

ALTER TABLE toolkit.test_run_details
DROP CONSTRAINT test_run_details_id_pk;

ALTER TABLE toolkit.test_run_details
ADD CONSTRAINT test_run_details_id_pk PRIMARY KEY (run_id, testcase_id);

ALTER TABLE toolkit.test_run_details_archive DROP COLUMN method_id;
COMMENT ON COLUMN toolkit.test_run_details_archive.method_id IS NULL;

ALTER TABLE toolkit.test_run_details_archive
DROP CONSTRAINT test_run_details_archive_id_pk;

ALTER TABLE toolkit.test_run_details_archive
ADD CONSTRAINT test_run_details_archive_id_pk PRIMARY KEY (run_id, testcase_id);