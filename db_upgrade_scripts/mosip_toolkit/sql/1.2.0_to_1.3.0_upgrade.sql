\c mosip_toolkit sysadmin

-- add new columns in abis_projects table.
ALTER TABLE toolkit.abis_projects Add COLUMN modality character varying(256) NOT NULL DEFAULT 'All';
ALTER TABLE toolkit.abis_projects Add COLUMN abis_hash character varying NOT NULL DEFAULT 'To_Be_Added';
ALTER TABLE toolkit.abis_projects Add COLUMN website_url character varying(256) NOT NULL DEFAULT 'To_Be_Added';
ALTER TABLE toolkit.abis_projects Add COLUMN org_name character varying(64) NOT NULL DEFAULT 'Not_Available';
COMMENT ON COLUMN toolkit.abis_projects.modality IS 'modality: different modalities combination';
COMMENT ON COLUMN toolkit.abis_projects.abis_hash IS 'abis_hash: Encoded hash of ABIS installation file';
COMMENT ON COLUMN toolkit.abis_projects.website_url IS 'website_url: Partner website url';
COMMENT ON COLUMN toolkit.abis_projects.org_name IS 'orgname: organization name to which partner belongs to.';

-- This table has biometric scores of sbi and sdk from biometric quality check validation.
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

-- add new columns in collections table.
ALTER TABLE toolkit.collections Add COLUMN collection_type character varying(256) NOT NULL DEFAULT 'custom_collection';
ALTER TABLE toolkit.collections Add COLUMN org_name character varying(64) NOT NULL DEFAULT 'Not_Available';
COMMENT ON COLUMN toolkit.collections.collection_type IS 'Collection Type: this can be custom_collection or compliance_collection';
COMMENT ON COLUMN toolkit.collections.org_name IS 'orgname: organization name to which partner belongs to.';

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

-- add new columns in sbi_projects table.
ALTER TABLE toolkit.sbi_projects Add COLUMN device_image1 character varying;
ALTER TABLE toolkit.sbi_projects Add COLUMN device_image2 character varying;
ALTER TABLE toolkit.sbi_projects Add COLUMN device_image3 character varying;
ALTER TABLE toolkit.sbi_projects Add COLUMN device_image4 character varying;
ALTER TABLE toolkit.sbi_projects Add COLUMN sbi_hash character varying NOT NULL DEFAULT 'To_Be_Added';
ALTER TABLE toolkit.sbi_projects Add COLUMN website_url character varying(256) NOT NULL DEFAULT 'To_Be_Added';
ALTER TABLE toolkit.sbi_projects Add COLUMN org_name character varying(64) NOT NULL DEFAULT 'Not_Available';
COMMENT ON COLUMN toolkit.sbi_projects.device_image1 IS 'device_image1: Base64 value of device image';
COMMENT ON COLUMN toolkit.sbi_projects.device_image2 IS 'device_image2: Base64 value of device image';
COMMENT ON COLUMN toolkit.sbi_projects.device_image3 IS 'device_image3: Base64 value of device image';
COMMENT ON COLUMN toolkit.sbi_projects.device_image4 IS 'device_image4: Base64 value of device image';
COMMENT ON COLUMN toolkit.sbi_projects.sbi_hash IS 'sbi_hash: Encoded hash of SBI installation file';
COMMENT ON COLUMN toolkit.sbi_projects.website_url IS 'website_url: Partner website url';
COMMENT ON COLUMN toolkit.sbi_projects.org_name IS 'orgname: organization name to which partner belongs to.';

-- add new columns in sdk_projects table.
ALTER TABLE toolkit.sdk_projects Add COLUMN sdk_hash character varying NOT NULL DEFAULT 'To_Be_Added';
ALTER TABLE toolkit.sdk_projects Add COLUMN website_url character varying(256) NOT NULL DEFAULT 'To_Be_Added';
ALTER TABLE toolkit.sdk_projects Add COLUMN org_name character varying(64) NOT NULL DEFAULT 'Not_Available';
COMMENT ON COLUMN toolkit.sdk_projects.sdk_hash IS 'sdk_hash: Encoded hash of SDK installation file';
COMMENT ON COLUMN toolkit.sdk_projects.website_url IS 'website_url: Partner website url';
COMMENT ON COLUMN toolkit.sdk_projects.org_name IS 'orgname: organization name to which partner belongs to.';

-- add new columns and constraints in test_run table
ALTER TABLE toolkit.test_run Add COLUMN execution_status character varying(36) NOT NULL DEFAULT 'incomplete';
ALTER TABLE toolkit.test_run Add COLUMN run_status character varying(36) DEFAULT 'failure';
COMMENT ON COLUMN toolkit.test_run.execution_status IS 'Execution Status: test run execution status incomplete or complete.';
COMMENT ON COLUMN toolkit.test_run.run_status IS 'Test Run Status: test run status as failure/success';
ALTER TABLE toolkit.test_run ADD CONSTRAINT test_run_execution_status_values CHECK (execution_status IN ('incomplete','complete'));
ALTER TABLE toolkit.test_run ADD CONSTRAINT test_run_run_status_values CHECK (run_status IN ('success','failure'));
	
-- add new columns in test_run_archive table
ALTER TABLE toolkit.test_run_archive ADD COLUMN execution_status character varying(36) NOT NULL DEFAULT 'incomplete';
ALTER TABLE toolkit.test_run_archive ADD COLUMN run_status character varying(36) NOT NULL DEFAULT 'failure';
COMMENT ON COLUMN toolkit.test_run_archive.execution_status IS 'Execution Status: test run execution status Incomplete or Complete.';
COMMENT ON COLUMN toolkit.test_run_archive.run_status IS 'Test Run Status: test run status as Failure/Success';

-- add new columns in test_run_details table
ALTER TABLE toolkit.test_run_details ADD COLUMN method_id character varying(150) NOT NULL DEFAULT 'Not_Available';
ALTER TABLE toolkit.test_run_details ADD COLUMN execution_status character varying(36) NOT NULL DEFAULT 'Not_Available';
COMMENT ON COLUMN toolkit.test_run_details.method_id IS 'Method ID: Unique method Id created for each method response';
COMMENT ON COLUMN toolkit.test_run_details.execution_status IS 'Execution Status: test case execution status Incomplete or Complete.';
ALTER TABLE toolkit.test_run_details DROP CONSTRAINT test_run_details_id_pk;
ALTER TABLE toolkit.test_run_details ADD CONSTRAINT test_run_details_id_pk PRIMARY KEY (run_id, testcase_id, method_id);
ALTER TABLE toolkit.test_run_details ADD CONSTRAINT test_run_details_execution_status_values CHECK (execution_status IN ('incomplete','complete'));
ALTER TABLE toolkit.test_run_details ADD CONSTRAINT test_run_details_result_status_values CHECK (result_status IN ('success','failure'));

-- add new columns in test_run_details_archive table
ALTER TABLE toolkit.test_run_details_archive ADD COLUMN method_id character varying(150) NOT NULL DEFAULT 'Not_Available';
ALTER TABLE toolkit.test_run_details_archive ADD COLUMN execution_status character varying(36) NOT NULL DEFAULT 'Not_Available';
COMMENT ON COLUMN toolkit.test_run_details_archive.method_id IS 'Method ID: Unique method Id created for each method response';
COMMENT ON COLUMN toolkit.test_run_details_archive.execution_status IS 'Execution Status: test case execution status Incomplete or Complete.';
ALTER TABLE toolkit.test_run_details_archive DROP CONSTRAINT test_run_details_archive_id_pk;
ALTER TABLE toolkit.test_run_details_archive ADD CONSTRAINT test_run_details_archive_id_pk PRIMARY KEY (run_id, testcase_id, method_id);

--Script to populate the newly added columns 'execution_status' for existing test run details
--can be set as complete since in CTKv1.2.0 only one row exists per run_id, testcase_id
UPDATE toolkit.test_run_details SET execution_status = 'complete';

--Script to populate the newly added columns 'execution_status', 'run_status'
--for existing test runs 
UPDATE 
  toolkit.test_run 
SET 
  run_status = 'success', 
  execution_status = 'complete' 
WHERE 
  id IN (
    SELECT 
      test_run_summary.run_id 
    FROM 
      (
        SELECT 
          collection_id, 
          Count(testcase_id) AS total_testcases 
        FROM 
          toolkit.collection_testcase_mapping 
        GROUP BY 
          collection_id 
        ORDER BY 
          collection_id
      ) collection_summary 
      INNER JOIN (
        SELECT 
          b.collection_id AS collection_id, 
          b.id AS run_id, 
          a.success_count 
        FROM 
          (
            SELECT 
              run_id, 
              Count(
                CASE WHEN Lower(result_status)= 'success' THEN 1 ELSE NULL END
              ) AS success_count 
            FROM 
              test_run_details 
            WHERE 
              run_id IN (
                SELECT 
                  id 
                FROM 
                  toolkit.test_run 
                WHERE 
                  collection_id IN (
                    SELECT 
                      id 
                    FROM 
                      toolkit.collections
                  )
              ) 
            GROUP BY 
              run_id
          ) a, 
          toolkit.test_run b 
        WHERE 
          a.success_count > 0 
          AND a.run_id = b.id 
        ORDER BY 
          b.collection_id
      ) test_run_summary ON collection_summary.collection_id = test_run_summary.collection_id 
    WHERE 
      collection_summary.total_testcases = test_run_summary.success_count
  )
