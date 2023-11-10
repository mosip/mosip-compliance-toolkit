-- This table has all the details for a test run for a given compliance collection

CREATE TABLE toolkit.compliance_testrun_summary(
	project_id character varying(36) NOT NULL,
	collection_id character varying(36) NOT NULL,
    run_id character varying(36) NOT NULL,
	project_type character varying(64) NOT NULL,
	partner_id character varying(36) NOT NULL, 
	org_name character varying(64) NOT NULL,
    report_data_json character varying NOT NULL,
    report_status character varying(36) NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT compliance_testrun_summary_pk PRIMARY KEY (project_id,collection_id,run_id) 
);

CREATE INDEX IF NOT EXISTS idx_compliance_testrun_summary_id ON toolkit.compliance_testrun_summary USING btree (project_id,collection_id,run_id);
ALTER TABLE toolkit.compliance_testrun_summary
 ADD CONSTRAINT report_status_values CHECK (report_status IN ('sent for review','approved','rejected'));
COMMENT ON TABLE toolkit.compliance_testrun_summary IS 'This table has all the details for a test run for a given compliance collection.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.project_id IS 'Project ID: Project Id of the corresponding project.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.collection_id IS 'Collection ID: Collection Id of the corresponding collection.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.run_id IS 'Run ID: Run Id of the corresponding test run.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.project_type IS 'Project Type: Type of project.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.partner_id IS 'Partner Id: partner id who has created this project.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.org_name IS 'Org Name: organization name to which partner belongs to.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.report_data_json IS 'Report Data Json: JSON with report data';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.report_status IS 'Report Status: Status of the report - sent for review,approved,rejected';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.is_deleted IS 'is Deleted :flag to store soft delete status';
COMMENT ON COLUMN toolkit.compliance_testrun_summary.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when record is deleted.';

