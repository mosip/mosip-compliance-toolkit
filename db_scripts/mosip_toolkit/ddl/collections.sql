-- This table has all the collections for the compliance toolkit project

CREATE TABLE toolkit.collections(
	id character varying(36) NOT NULL,
        sbi_project_id character varying(36),
	sdk_project_id character varying(36),
	abis_project_id character varying(36),
	name character varying(64) NOT NULL,
        partner_id character varying(36) NOT NULL,
	org_name character varying(64) NOT NULL,
	collection_type character varying(256) NOT NULL DEFAULT 'custom_collection',
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT collectionsid_pk PRIMARY KEY (id) 
);

CREATE INDEX IF NOT EXISTS idx_collections_id ON toolkit.collections USING btree (id);
CREATE INDEX IF NOT EXISTS idx_collections_sbi_project_id ON toolkit.collections USING btree (sbi_project_id);
CREATE INDEX IF NOT EXISTS idx_collections_sdk_project_id ON toolkit.collections USING btree (sdk_project_id);
CREATE INDEX IF NOT EXISTS idx_collections_abis_project_id ON toolkit.collections USING btree (abis_project_id);
COMMENT ON TABLE toolkit.collections IS 'This table has all collections for the compliance toolkit project.';
COMMENT ON COLUMN toolkit.collections.id IS 'ID: Unique Id generated for an collection.';
COMMENT ON COLUMN toolkit.collections.sbi_project_id IS 'Project ID: Id of the corresponding SBI project.';
COMMENT ON COLUMN toolkit.collections.sdk_project_id IS 'Project ID: Id of the corresponding SDK project.';
COMMENT ON COLUMN toolkit.collections.abis_project_id IS 'Project ID: Id of the corresponding ABIS project.';
COMMENT ON COLUMN toolkit.collections.name IS 'Name: name of the collection.';
COMMENT ON COLUMN toolkit.collections.partner_id IS 'Partner Id: partner id who has created this project.';
COMMENT ON COLUMN toolkit.collections.org_name IS 'orgname: organization name to which partner belongs to.';
COMMENT ON COLUMN toolkit.collections.collection_type IS 'Collection Type: this can be custom_collection or compliance_collection';
COMMENT ON COLUMN toolkit.collections.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.collections.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.collections.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN toolkit.collections.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN toolkit.collections.is_deleted IS 'is Deleted :flag to store soft delete status';
COMMENT ON COLUMN toolkit.collections.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when record is deleted.';

