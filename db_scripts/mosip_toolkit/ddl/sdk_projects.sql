-- This table saves all the demographic details in the user’s application in an encrypted JSON format.

CREATE TABLE toolkit.sdk_projects(
	id character varying(36) NOT NULL,
	name character varying(64) NOT NULL,
	project_type character varying(64) NOT NULL,
	url character varying(256) NOT NULL,
	purpose character varying(256) NOT NULL,
	partner_id character varying(36) NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp
);

CREATE INDEX IF NOT EXISTS idx_sdk_projects_id ON toolkit.sdk_projects USING btree (id);
CREATE INDEX IF NOT EXISTS idx_sdk_projects_partner_id ON toolkit.sdk_projects USING btree (partner_id);
COMMENT ON TABLE toolkit.sdk_projects IS 'This table saves all the demographic details in the user’s application in an encrypted JSON format';
COMMENT ON COLUMN toolkit.sdk_projects.id IS 'ID: Unique Id generated for an project.';
COMMENT ON COLUMN toolkit.sdk_projects.name IS 'Name: name of the project.';
COMMENT ON COLUMN toolkit.sdk_projects.project_type IS 'Type: typeof project SDK, SBI or ABIS.';
COMMENT ON COLUMN toolkit.sdk_projects.url IS 'URL: the url where SDK is running on users machine.';
COMMENT ON COLUMN toolkit.sdk_projects.purpose IS 'Purpose: the purpose for testing';
COMMENT ON COLUMN toolkit.sdk_projects.partner_id IS 'Partner Id: partner id who has created this project.';
COMMENT ON COLUMN toolkit.sdk_projects.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.sdk_projects.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.sdk_projects.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN toolkit.sdk_projects.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';

