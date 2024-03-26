-- This table has compliance toolkit templates.
CREATE TABLE toolkit.master_templates(
    id character varying(36) NOT NULL,
    lang_code character varying(36) NOT NULL,
    template_name character varying(64) NOT NULL,
    template character varying NOT NULL,
    cr_dtimes timestamp NOT NULL,
    cr_by character varying(64) NOT NULL,
    version character varying(36) NOT NULL,
    CONSTRAINT master_templates_pk PRIMARY KEY (id)
);
COMMENT ON TABLE toolkit.master_templates IS 'This table has templates of Compliance Toolkit.';
COMMENT ON COLUMN toolkit.master_templates.id IS 'ID: Unique Id generated for an template.';
COMMENT ON COLUMN toolkit.master_templates.lang_code IS 'Lang Code: Language of the template stored.';
COMMENT ON COLUMN toolkit.master_templates.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.master_templates.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.master_templates.template_name IS 'Template Name: Name of the template saved.';
COMMENT ON COLUMN toolkit.master_templates.template IS 'Template: Stores the actual template data.';
COMMENT ON COLUMN toolkit.master_templates.version IS 'Version: Indicates the version of the template.';