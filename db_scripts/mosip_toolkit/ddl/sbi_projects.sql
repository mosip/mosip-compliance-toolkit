-- This table all the sbi projects for the user.

CREATE TABLE toolkit.sbi_projects(
	id character varying(36) NOT NULL,
	name character varying(64) NOT NULL,
	project_type character varying(64) NOT NULL,
	sbi_version character varying(256) NOT NULL,
	purpose character varying(256) NOT NULL,
	device_type character varying(64) NOT NULL,
	device_sub_type character varying(64) NOT NULL,
	device_image1 character varying,
	device_image2 character varying,
	device_image3 character varying,
	device_image4 character varying,
	device_image5 character varying,
	sbi_hash character varying(256) NOT NULL DEFAULT 'To_Be_Added',
	website_url character varying(256) NOT NULL DEFAULT 'To_Be_Added',
	partner_id character varying(36) NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT sbiprojectsid_pk PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_sbi_projects_id ON toolkit.sbi_projects USING btree (id);
CREATE INDEX IF NOT EXISTS idx_sbi_projects_partner_id ON toolkit.sbi_projects USING btree (partner_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_sbi_projects_name_partner_id ON toolkit.sbi_projects USING btree(LOWER(name), LOWER(partner_id));
COMMENT ON TABLE toolkit.sbi_projects IS 'This table all the sbi projects for the user.';
COMMENT ON COLUMN toolkit.sbi_projects.id IS 'ID: Unique Id generated for an project.';
COMMENT ON COLUMN toolkit.sbi_projects.name IS 'Name: name of the project.';
COMMENT ON COLUMN toolkit.sbi_projects.project_type IS 'Type: typeof project SDK, SBI or ABIS.';
COMMENT ON COLUMN toolkit.sbi_projects.sbi_version IS 'sbi_version: the sbi_version is the version of sbi.';
COMMENT ON COLUMN toolkit.sbi_projects.purpose IS 'Purpose: the purpose for testing';
COMMENT ON COLUMN toolkit.sbi_projects.device_type IS 'device_type: the device type for testing';
COMMENT ON COLUMN toolkit.sbi_projects.device_sub_type IS 'device_sub_type: the device sub type for testing';
COMMENT ON COLUMN toolkit.sbi_projects.device_image1 IS 'device_image1: Base64 value of device image';
COMMENT ON COLUMN toolkit.sbi_projects.device_image2 IS 'device_image2: Base64 value of device image';
COMMENT ON COLUMN toolkit.sbi_projects.device_image3 IS 'device_image3: Base64 value of device image';
COMMENT ON COLUMN toolkit.sbi_projects.device_image4 IS 'device_image4: Base64 value of device image';
COMMENT ON COLUMN toolkit.sbi_projects.device_image5 IS 'device_image5: Base64 value of device image';
COMMENT ON COLUMN toolkit.sbi_projects.sbi_hash IS 'sbi_hash: Encoded hash of SBI installation file';
COMMENT ON COLUMN toolkit.sbi_projects.website_url IS 'website_url: Partner website url';
COMMENT ON COLUMN toolkit.sbi_projects.partner_id IS 'Partner Id: partner id who has created this project.';
COMMENT ON COLUMN toolkit.sbi_projects.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.sbi_projects.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.sbi_projects.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN toolkit.sbi_projects.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN toolkit.sbi_projects.is_deleted IS 'is Deleted :flag to store soft delete status';
COMMENT ON COLUMN toolkit.sbi_projects.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when record is deleted.';

