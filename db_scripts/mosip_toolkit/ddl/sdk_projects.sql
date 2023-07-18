-- This table all the SDK projects for the user.

CREATE TABLE toolkit.sdk_projects(
	id character varying(36) NOT NULL,
	name character varying(64) NOT NULL,
	project_type character varying(64) NOT NULL,
	sdk_version character varying(36) NOT NULL,
	url character varying(256) NOT NULL,
	bio_test_data_file_name character varying(64) NOT NULL,
	purpose character varying(256) NOT NULL,
	sdk_hash character varying(256) NOT NULL DEFAULT 'To_Be_Added',
	website_url character varying(256) NOT NULL DEFAULT 'To_Be_Added',
	partner_id character varying(36) NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT sdkprojectsid_pk PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_sdk_projects_id ON toolkit.sdk_projects USING btree (id);
CREATE INDEX IF NOT EXISTS idx_sdk_projects_partner_id ON toolkit.sdk_projects USING btree (partner_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_sdk_projects_name_partner_id ON toolkit.sdk_projects USING btree(LOWER(name), LOWER(partner_id));
COMMENT ON TABLE toolkit.sdk_projects IS 'This table all the SDK projects for the user.';
COMMENT ON COLUMN toolkit.sdk_projects.id IS 'ID: Unique Id generated for an project.';
COMMENT ON COLUMN toolkit.sdk_projects.name IS 'Name: name of the project.';
COMMENT ON COLUMN toolkit.sdk_projects.project_type IS 'Type: typeof project SDK, SBI or ABIS.';
COMMENT ON COLUMN toolkit.sdk_projects.sdk_version IS 'sdk_version: the sdk_version is the version of sdk.';
COMMENT ON COLUMN toolkit.sdk_projects.url IS 'URL: the url where SDK is running on users machine.';
COMMENT ON COLUMN toolkit.sdk_projects.bio_test_data_file_name IS 'Biometric Test Data File Name : Name of the biometric test data file to be used';
COMMENT ON COLUMN toolkit.sdk_projects.purpose IS 'Purpose: the purpose for testing';
COMMENT ON COLUMN toolkit.sdk_projects.sdk_hash IS 'sdk_hash: Encoded hash of SDK installation file';
COMMENT ON COLUMN toolkit.sdk_projects.website_url IS 'website_url: Partner website url';
COMMENT ON COLUMN toolkit.sdk_projects.partner_id IS 'Partner Id: partner id who has created this project.';
COMMENT ON COLUMN toolkit.sdk_projects.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.sdk_projects.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.sdk_projects.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN toolkit.sdk_projects.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN toolkit.sdk_projects.is_deleted IS 'is Deleted :flag to store soft delete status';
COMMENT ON COLUMN toolkit.sdk_projects.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when record is deleted.';

