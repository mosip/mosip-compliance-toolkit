-- This table all the ABIS projects for the user.

CREATE TABLE toolkit.abis_projects(
	id character varying(36) NOT NULL,
	name character varying(64) NOT NULL,
	project_type character varying(64) NOT NULL,
	url character varying(256) NOT NULL,
	username character varying(256) NOT NULL,
	password character varying(256) NOT NULL,
	queue_name character varying(256) NOT NULL,
	partner_id character varying(36) NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT abisprojectsid_pk PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_abis_projects_id ON toolkit.abis_projects USING btree (id);
CREATE INDEX IF NOT EXISTS idx_abis_projects_partner_id ON toolkit.abis_projects USING btree (partner_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_abis_projects_name_partner_id ON toolkit.abis_projects USING btree(LOWER(name), LOWER(partner_id));
COMMENT ON TABLE toolkit.abis_projects IS 'This table all the abis projects for the user.';
COMMENT ON COLUMN toolkit.abis_projects.id IS 'ID: Unique Id generated for an project.';
COMMENT ON COLUMN toolkit.abis_projects.name IS 'Name: name of the project.';
COMMENT ON COLUMN toolkit.abis_projects.project_type IS 'Type: typeof project SDK, SBI or ABIS.';
COMMENT ON COLUMN toolkit.abis_projects.url IS 'URL: the url where abis is running on users machine.';
COMMENT ON COLUMN toolkit.abis_projects.username IS 'username: the username for ABIS queue';
COMMENT ON COLUMN toolkit.abis_projects.password IS 'password: the password for ABIS queue';
COMMENT ON COLUMN toolkit.abis_projects.queue_name IS 'queue_name: the queue name for ABIS queue';
COMMENT ON COLUMN toolkit.abis_projects.partner_id IS 'Partner Id: partner id who has created this project.';
COMMENT ON COLUMN toolkit.abis_projects.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.abis_projects.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.abis_projects.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN toolkit.abis_projects.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN toolkit.abis_projects.is_deleted IS 'is Deleted :flag to store soft delete status';
COMMENT ON COLUMN toolkit.abis_projects.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when record is deleted.';

