\c mosip_toolkit sysadmin

-- abis_projects
COMMENT ON COLUMN toolkit.abis_projects.modality IS NULL;
COMMENT ON COLUMN toolkit.abis_projects.abis_hash IS NULL;
COMMENT ON COLUMN toolkit.abis_projects.website_url IS NULL;
COMMENT ON COLUMN toolkit.abis_projects.org_name IS NULL;
ALTER TABLE toolkit.abis_projects DROP COLUMN modality;
ALTER TABLE toolkit.abis_projects DROP COLUMN abis_hash;
ALTER TABLE toolkit.abis_projects DROP COLUMN website_url;
ALTER TABLE toolkit.abis_projects DROP COLUMN org_name;

-- biometric_scores
DROP TABLE IF EXISTS toolkit.biometric_scores;

-- compliance_testrun_summary
DROP TABLE IF EXISTS toolkit.compliance_testrun_summary;

-- collections
COMMENT ON COLUMN toolkit.collections.collection_type IS NULL;
COMMENT ON COLUMN toolkit.collections.org_name IS NULL;
ALTER TABLE toolkit.collections DROP COLUMN collection_type;
ALTER TABLE toolkit.collections DROP COLUMN org_name;

-- datashare_tokens
DROP TABLE IF EXISTS toolkit.datashare_tokens;

-- sbi_projects
COMMENT ON COLUMN toolkit.sbi_projects.device_image1 IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.device_image2 IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.device_image3 IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.device_image4 IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.sbi_hash IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.website_url IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.org_name IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.is_android_sbi IS NULL;
ALTER TABLE toolkit.sbi_projects DROP COLUMN device_image1;
ALTER TABLE toolkit.sbi_projects DROP COLUMN device_image2;
ALTER TABLE toolkit.sbi_projects DROP COLUMN device_image3;
ALTER TABLE toolkit.sbi_projects DROP COLUMN device_image4;
ALTER TABLE toolkit.sbi_projects DROP COLUMN sbi_hash;
ALTER TABLE toolkit.sbi_projects DROP COLUMN website_url;
ALTER TABLE toolkit.sbi_projects DROP COLUMN org_name;
ALTER TABLE toolkit.sbi_projects DROP COLUMN is_android_sbi;

-- sdk_projects
COMMENT ON COLUMN toolkit.sdk_projects.sdk_hash IS NULL;
COMMENT ON COLUMN toolkit.sdk_projects.website_url IS NULL;
COMMENT ON COLUMN toolkit.sdk_projects.org_name IS NULL;
ALTER TABLE toolkit.sdk_projects DROP COLUMN sdk_hash;
ALTER TABLE toolkit.sdk_projects DROP COLUMN website_url;
ALTER TABLE toolkit.sdk_projects DROP COLUMN org_name;

-- test_run
ALTER TABLE toolkit.test_run DROP CONSTRAINT test_run_execution_status_values;
ALTER TABLE toolkit.test_run DROP CONSTRAINT test_run_run_status_values;

COMMENT ON COLUMN toolkit.test_run.execution_status IS NULL;
COMMENT ON COLUMN toolkit.test_run.run_status IS NULL;
COMMENT ON COLUMN toolkit.test_run.org_name IS NULL;
ALTER TABLE toolkit.test_run DROP COLUMN execution_status;
ALTER TABLE toolkit.test_run DROP COLUMN run_status;
ALTER TABLE toolkit.test_run DROP COLUMN org_name;

-- test_run_archive
COMMENT ON COLUMN toolkit.test_run_archive.execution_status IS NULL;
COMMENT ON COLUMN toolkit.test_run_archive.run_status IS NULL;
COMMENT ON COLUMN toolkit.test_run_archive.org_name IS NULL;
ALTER TABLE toolkit.test_run_archive DROP COLUMN execution_status;
ALTER TABLE toolkit.test_run_archive DROP COLUMN run_status;
ALTER TABLE toolkit.test_run_archive DROP COLUMN org_name;

-- test_run_details
ALTER TABLE toolkit.test_run_details DROP CONSTRAINT test_run_details_execution_status_values;
ALTER TABLE toolkit.test_run_details DROP CONSTRAINT test_run_details_result_status_values;
ALTER TABLE toolkit.test_run_details DROP CONSTRAINT test_run_details_id_pk;
COMMENT ON COLUMN toolkit.test_run_details.method_id IS NULL;
COMMENT ON COLUMN toolkit.test_run_details.execution_status IS NULL;
COMMENT ON COLUMN toolkit.test_run_details.org_name IS NULL;
ALTER TABLE toolkit.test_run_details DROP COLUMN method_id;
ALTER TABLE toolkit.test_run_details DROP COLUMN execution_status;
ALTER TABLE toolkit.test_run_details ADD CONSTRAINT test_run_details_id_pk PRIMARY KEY (run_id, testcase_id);
ALTER TABLE toolkit.test_run_details DROP COLUMN org_name;

-- test_run_details_archive
ALTER TABLE toolkit.test_run_details_archive DROP CONSTRAINT test_run_details_archive_id_pk;
COMMENT ON COLUMN toolkit.test_run_details_archive.method_id IS NULL;
COMMENT ON COLUMN toolkit.test_run_details_archive.execution_status IS NULL;
COMMENT ON COLUMN toolkit.test_run_details_archive.org_name IS NULL;
ALTER TABLE toolkit.test_run_details_archive DROP COLUMN method_id;
ALTER TABLE toolkit.test_run_details_archive DROP COLUMN execution_status;
ALTER TABLE toolkit.test_run_details_archive DROP COLUMN org_name;
ADD CONSTRAINT test_run_details_archive_id_pk PRIMARY KEY (run_id, testcase_id);

-- partner_profile
DROP TABLE IF EXISTS toolkit.partner_profile;