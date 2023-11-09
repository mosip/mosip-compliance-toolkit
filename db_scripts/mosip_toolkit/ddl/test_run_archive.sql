-- This table has all the details for a test run archives for a given collection in compliance toolkit project

CREATE TABLE toolkit.test_run_archive(
	id character varying(36) NOT NULL,
        collection_id character varying(36) NOT NULL,
	run_dtimes timestamp NOT NULL,
        execution_dtimes timestamp, 
        run_configuration_json character varying(256), 
	partner_id character varying(36) NOT NULL,
	org_name character varying(64) NOT NULL,
	execution_status character varying(36) NOT NULL,
	run_status character varying(36) NOT NULL,    
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT test_run_archive_id_pk PRIMARY KEY (id) 
);

CREATE INDEX IF NOT EXISTS idx_test_run_archive_id ON toolkit.test_run_archive USING btree (id);
CREATE INDEX IF NOT EXISTS idx_test_run_archive_collection_id ON toolkit.test_run_archive USING btree (id, collection_id);
CREATE INDEX IF NOT EXISTS idx_test_run_archive_id_partner_id ON toolkit.test_run_archive USING btree (id, partner_id);
COMMENT ON TABLE toolkit.test_run_archive IS 'This table has all the details for a test run archives for a given collection in compliance toolkit project.';
COMMENT ON COLUMN toolkit.test_run_archive.id IS 'ID: Unique Id generated for an test run.';
COMMENT ON COLUMN toolkit.test_run_archive.collection_id IS 'Collection ID: Collection Id of the corresponding collection.';
COMMENT ON COLUMN toolkit.test_run_archive.run_dtimes IS 'Run Dt Time: Timestamp when run was created.';
COMMENT ON COLUMN toolkit.test_run_archive.execution_dtimes IS 'Execution Dt Time: Timestamp when run has completed execution.';
COMMENT ON COLUMN toolkit.test_run_archive.run_configuration_json IS 'Run configuration json: Configuration details for a test run.';
COMMENT ON COLUMN toolkit.test_run_archive.partner_id IS 'Partner Id: partner id who has created this project.';
COMMENT ON COLUMN toolkit.test_run_archive.org_name IS 'orgname: organization name to which partner belongs to.';
COMMENT ON COLUMN toolkit.test_run_archive.execution_status IS 'Execution Status: test run execution status Incomplete or Complete.';
COMMENT ON COLUMN toolkit.test_run_archive.run_status IS 'Test Run Status: test run status as Failure/Success';
COMMENT ON COLUMN toolkit.test_run_archive.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.test_run_archive.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.test_run_archive.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN toolkit.test_run_archive.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN toolkit.test_run_archive.is_deleted IS 'is Deleted :flag to store soft delete status';
COMMENT ON COLUMN toolkit.test_run_archive.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when record is deleted.';

