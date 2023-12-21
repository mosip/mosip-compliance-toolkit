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

-- This table has all the details for a test run for a given compliance collection
CREATE TABLE toolkit.compliance_testrun_summary(
	partner_id character varying(36) NOT NULL, 
	project_id character varying(36) NOT NULL,
	collection_id character varying(36) NOT NULL,
    run_id character varying(36) NOT NULL,
	project_type character varying(64) NOT NULL,
	org_name character varying(64) NOT NULL,
    report_data_json character varying NOT NULL,
    report_status character varying(36) NOT NULL,
	partner_comments character varying,
	admin_comments character varying,
	review_dttimes timestamp,
	approve_reject_dttimes timestamp,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT compliance_testrun_summary_pk PRIMARY KEY (partner_id,project_id,collection_id) 
);

CREATE INDEX IF NOT EXISTS idx_compliance_testrun_summary_id ON toolkit.compliance_testrun_summary USING btree (project_id,collection_id,run_id);
ALTER TABLE toolkit.compliance_testrun_summary
 ADD CONSTRAINT report_status_values CHECK (report_status IN ('draft','review','approved','rejected'));
COMMENT ON TABLE toolkit.compliance_testrun_summary IS 'This table has all the details for a test run for a given compliance collection.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.project_id IS 'Project ID: Project Id of the corresponding project.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.collection_id IS 'Collection ID: Collection Id of the corresponding collection.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.run_id IS 'Run ID: Run Id of the corresponding test run.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.project_type IS 'Project Type: Type of project.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.partner_id IS 'Partner Id: partner id who has created this project.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.org_name IS 'Org Name: organization name to which partner belongs to.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.report_data_json IS 'Report Data Json: JSON with report data';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.report_status IS 'Report Status: Status of the report - draft, review,approved,rejected';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.partner_comments IS 'Partner Comments: Comments by partner';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.admin_comments IS 'Admin Comments: Comments by admin';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.review_dttimes IS 'Review DateTimestamp : Date and Timestamp when the report is submitted for review';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.approve_reject_dttimes IS 'Approve / Reject DateTimestamp : Date and Timestamp when the report is approved or rejected';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.is_deleted IS 'is Deleted :flag to store soft delete status';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when record is deleted.';

-- add new columns in collections table.
ALTER TABLE toolkit.collections Add COLUMN collection_type character varying(256) NOT NULL DEFAULT 'custom_collection';
ALTER TABLE toolkit.collections Add COLUMN org_name character varying(64) NOT NULL DEFAULT 'Not_Available';
COMMENT ON COLUMN toolkit.collections.collection_type IS 'Collection Type: this can be custom_collection, compliance_collection or quality_assessment_collection';
COMMENT ON COLUMN toolkit.collections.org_name IS 'orgname: organization name to which partner belongs to.';
ALTER TABLE toolkit.collections ADD CONSTRAINT collections_collection_type_values CHECK (collection_type IN ('custom_collection','compliance_collection','quality_assessment_collection'));

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
ALTER TABLE toolkit.sbi_projects Add COLUMN is_android_sbi character varying NOT NULL DEFAULT 'no';
ALTER TABLE toolkit.sbi_projects Add COLUMN device_image1 character varying;
ALTER TABLE toolkit.sbi_projects Add COLUMN device_image2 character varying;
ALTER TABLE toolkit.sbi_projects Add COLUMN device_image3 character varying;
ALTER TABLE toolkit.sbi_projects Add COLUMN device_image4 character varying;
ALTER TABLE toolkit.sbi_projects Add COLUMN sbi_hash character varying NOT NULL DEFAULT 'To_Be_Added';
ALTER TABLE toolkit.sbi_projects Add COLUMN website_url character varying(256) NOT NULL DEFAULT 'To_Be_Added';
ALTER TABLE toolkit.sbi_projects Add COLUMN org_name character varying(64) NOT NULL DEFAULT 'Not_Available';
COMMENT ON COLUMN toolkit.sbi_projects.is_android_sbi IS 'is_android_sbi: flag to indicate if project is created in android app or browser';
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
ALTER TABLE toolkit.test_run Add COLUMN org_name character varying(64) NOT NULL DEFAULT 'Not_Available';
COMMENT ON COLUMN toolkit.test_run.execution_status IS 'Execution Status: test run execution status incomplete or complete.';
COMMENT ON COLUMN toolkit.test_run.run_status IS 'Test Run Status: test run status as failure/success';
COMMENT ON COLUMN toolkit.test_run.org_name IS 'orgname: organization name to which partner belongs to.';
ALTER TABLE toolkit.test_run ADD CONSTRAINT test_run_execution_status_values CHECK (execution_status IN ('incomplete','complete'));
ALTER TABLE toolkit.test_run ADD CONSTRAINT test_run_run_status_values CHECK (run_status IN ('success','failure'));
	
-- add new columns in test_run_archive table
ALTER TABLE toolkit.test_run_archive ADD COLUMN execution_status character varying(36) NOT NULL DEFAULT 'incomplete';
ALTER TABLE toolkit.test_run_archive ADD COLUMN run_status character varying(36) NOT NULL DEFAULT 'failure';
ALTER TABLE toolkit.test_run_archive Add COLUMN org_name character varying(64) NOT NULL DEFAULT 'Not_Available';
COMMENT ON COLUMN toolkit.test_run_archive.execution_status IS 'Execution Status: test run execution status Incomplete or Complete.';
COMMENT ON COLUMN toolkit.test_run_archive.run_status IS 'Test Run Status: test run status as Failure/Success';
COMMENT ON COLUMN toolkit.test_run_archive.org_name IS 'orgname: organization name to which partner belongs to.';

-- add new columns in test_run_details table
ALTER TABLE toolkit.test_run_details ADD COLUMN method_id character varying(150) NOT NULL DEFAULT 'Not_Available';
ALTER TABLE toolkit.test_run_details ADD COLUMN execution_status character varying(36) NOT NULL DEFAULT 'Not_Available';
ALTER TABLE toolkit.test_run_details Add COLUMN org_name character varying(64) NOT NULL DEFAULT 'Not_Available';
COMMENT ON COLUMN toolkit.test_run_details.method_id IS 'Method ID: Unique method Id created for each method response';
COMMENT ON COLUMN toolkit.test_run_details.execution_status IS 'Execution Status: test case execution status Incomplete or Complete.';
COMMENT ON COLUMN toolkit.test_run_details.org_name IS 'orgname: organization name to which partner belongs to.';
ALTER TABLE toolkit.test_run_details DROP CONSTRAINT test_run_details_id_pk;
ALTER TABLE toolkit.test_run_details ADD CONSTRAINT test_run_details_id_pk PRIMARY KEY (run_id, testcase_id, method_id);
ALTER TABLE toolkit.test_run_details ADD CONSTRAINT test_run_details_execution_status_values CHECK (execution_status IN ('incomplete','complete'));
ALTER TABLE toolkit.test_run_details ADD CONSTRAINT test_run_details_result_status_values CHECK (result_status IN ('success','failure'));

-- add new columns in test_run_details_archive table
ALTER TABLE toolkit.test_run_details_archive ADD COLUMN method_id character varying(150) NOT NULL DEFAULT 'Not_Available';
ALTER TABLE toolkit.test_run_details_archive ADD COLUMN execution_status character varying(36) NOT NULL DEFAULT 'Not_Available';
ALTER TABLE toolkit.test_run_details_archive Add COLUMN org_name character varying(64) NOT NULL DEFAULT 'Not_Available';
COMMENT ON COLUMN toolkit.test_run_details_archive.method_id IS 'Method ID: Unique method Id created for each method response';
COMMENT ON COLUMN toolkit.test_run_details_archive.execution_status IS 'Execution Status: test case execution status Incomplete or Complete.';
COMMENT ON COLUMN toolkit.test_run_details_archive.org_name IS 'orgname: organization name to which partner belongs to.';
ALTER TABLE toolkit.test_run_details_archive DROP CONSTRAINT test_run_details_archive_id_pk;
ALTER TABLE toolkit.test_run_details_archive ADD CONSTRAINT test_run_details_archive_id_pk PRIMARY KEY (run_id, testcase_id, method_id);

-- add new columns in biometric_testdata table
ALTER TABLE toolkit.biometric_testdata Add COLUMN org_name character varying(64) NOT NULL DEFAULT 'Not_Available';
COMMENT ON COLUMN toolkit.biometric_testdata.org_name IS 'orgname: organization name to which partner belongs to.';

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
  );

--Script to populate the newly added column 'org_name' for existing tables
-- Check if the dblink extension exists
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'dblink') THEN
    -- Create the dblink extension if it doesn't exist
    CREATE EXTENSION dblink;
  END IF;
END $$;
-- Set properties 
\set db_super_user `grep -oP 'SU_USER=\K[^ ]+' upgrade.properties`
\set db_password `grep -oP 'SU_USER_PWD=\K[^ ]+' upgrade.properties`

-- Set the connection string using string concatenation
\set conn_str 'dbname=mosip_pms user=' :db_super_user ' password=' :db_password

-- update org_name table for abis_projects table
UPDATE mosip_toolkit.toolkit.abis_projects AS t
SET org_name = i.name
FROM dblink(
  :'conn_str',
  'SELECT id, name FROM partner'
) AS i(id TEXT, name TEXT)
WHERE partner_id = i.id AND t.org_name = 'Not_Available';

-- update org_name table for sbi_projects table
UPDATE mosip_toolkit.toolkit.sbi_projects AS t
SET org_name = i.name
FROM dblink(
  :'conn_str',
  'SELECT id, name FROM partner'
) AS i(id TEXT, name TEXT)
WHERE partner_id = i.id AND t.org_name = 'Not_Available';

-- update org_name table for sdk_projects table
UPDATE mosip_toolkit.toolkit.sdk_projects AS t
SET org_name = i.name
FROM dblink(
  :'conn_str',
  'SELECT id, name FROM partner'
) AS i(id TEXT, name TEXT)
WHERE partner_id = i.id AND t.org_name = 'Not_Available';

-- update org_name table for biometric_scores table
UPDATE mosip_toolkit.toolkit.biometric_scores AS t
SET org_name = i.name
FROM dblink(
  :'conn_str',
  'SELECT id, name FROM partner'
) AS i(id TEXT, name TEXT)
WHERE partner_id = i.id AND t.org_name = 'Not_Available';

-- update org_name table for collections table
UPDATE mosip_toolkit.toolkit.collections AS t
SET org_name = i.name
FROM dblink(
  :'conn_str',
  'SELECT id, name FROM partner'
) AS i(id TEXT, name TEXT)
WHERE partner_id = i.id AND t.org_name = 'Not_Available';

-- update org_name table for test_run table
UPDATE mosip_toolkit.toolkit.test_run AS t
SET org_name = i.name
FROM dblink(
  :'conn_str',
  'SELECT id, name FROM partner'
) AS i(id TEXT, name TEXT)
WHERE partner_id = i.id AND t.org_name = 'Not_Available';

-- update org_name table for biometric_testdata table
UPDATE mosip_toolkit.toolkit.biometric_testdata AS t
SET org_name = i.name
FROM dblink(
  :'conn_str',
  'SELECT id, name FROM partner'
) AS i(id TEXT, name TEXT)
WHERE partner_id = i.id AND t.org_name = 'Not_Available';

-- update org_name table for test_run_details table
UPDATE mosip_toolkit.toolkit.test_run_details AS t
SET org_name = i.name
FROM dblink(
  :'conn_str',
  'SELECT id, name FROM partner'
) AS i(id TEXT, name TEXT)
WHERE partner_id = i.id AND t.org_name = 'Not_Available';

-- update sbi_projects and set the is_android_sbi as 'yes'
-- based on previous discovery test run
UPDATE toolkit.sbi_projects
	SET is_android_sbi='yes' where id in (
SELECT sbi_project_id FROM toolkit.collections
	where id in (
SELECT collection_id from toolkit.test_run where id in (
	SELECT run_id FROM toolkit.test_run_details where method_url='io.sbi.device')))