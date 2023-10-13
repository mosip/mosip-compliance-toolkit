-- This table has all the execution details for a test run for a given collection in compliance toolkit project

CREATE TABLE toolkit.test_run_details(
	run_id character varying(36) NOT NULL,
    testcase_id character varying(36) NOT NULL,
	method_id character varying(150) NOT NULL,
	method_url character varying NOT NULL,
	method_request character varying NOT NULL,
    method_response character varying NOT NULL, 
	execution_status character varying(36) NOT NULL, 
    result_status character varying(256) NOT NULL,     
	result_description character varying NOT NULL,
	test_data_source character varying(256),
	partner_id character varying(36) NOT NULL,
	org_name character varying(64) NOT NULL,
    cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
    upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT test_run_details_id_pk PRIMARY KEY (run_id, testcase_id, method_id) 
);

CREATE INDEX IF NOT EXISTS idx_test_run_details_id ON toolkit.test_run_details USING btree (run_id);
CREATE INDEX IF NOT EXISTS idx_test_run_details_id_partner_id ON toolkit.test_run_details USING btree (run_id, partner_id);
ALTER TABLE toolkit.test_run_details
    ADD CONSTRAINT test_run_details_execution_status_values CHECK (execution_status IN ('incomplete','complete'));
ALTER TABLE toolkit.test_run_details
    ADD CONSTRAINT test_run_details_result_status_values CHECK (result_status IN ('success','failure'));
COMMENT ON TABLE toolkit.test_run_details IS 'This table has all the execution details for a test run for a given collection in compliance toolkit project.';
COMMENT ON COLUMN toolkit.test_run_details.run_id IS 'run_id: Unique run Id generated for an test run.';
COMMENT ON COLUMN toolkit.test_run_details.testcase_id IS 'Testcase ID: Id of the corresponding testcase.';
COMMENT ON COLUMN toolkit.test_run_details.method_id IS 'Method ID: Unique method Id created for each method response';
COMMENT ON COLUMN toolkit.test_run_details.method_url IS 'Method URL: URL used for method execution.';
COMMENT ON COLUMN toolkit.test_run_details.method_request IS 'Method Request: request used for method execution.';
COMMENT ON COLUMN toolkit.test_run_details.method_response IS 'Method Response: response received on method execution.';
COMMENT ON COLUMN toolkit.test_run_details.execution_status IS 'Execution Status: test case execution status Incomplete or Complete.';
COMMENT ON COLUMN toolkit.test_run_details.result_status IS 'result_status: status of a test run execution.';
COMMENT ON COLUMN toolkit.test_run_details.result_description IS 'result_description: description of a test run execution as received from validators.';
COMMENT ON COLUMN toolkit.test_run_details.test_data_source IS 'test_data_source: biometric test data used for this testcase';
COMMENT ON COLUMN toolkit.test_run_details.partner_id IS 'Partner Id: partner id who has created this project.';
COMMENT ON COLUMN toolkit.test_run_details.org_name IS 'orgname: organization name to which partner belongs to.';
COMMENT ON COLUMN toolkit.test_run_details.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.test_run_details.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.test_run_details.is_deleted IS 'is Deleted :flag to store soft delete status';
COMMENT ON COLUMN toolkit.test_run_details.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when record is deleted.';

