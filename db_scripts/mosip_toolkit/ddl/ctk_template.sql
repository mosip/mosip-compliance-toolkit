-- This table has compliance toolkit templates.
CREATE TABLE toolkit.ctk_template(
    lang_code character varying(36) NOT NULL,
    template_name character varying(64) NOT NULL,
    template character varying NOT NULL,
    cr_dtimes timestamp NOT NULL,
    cr_by character varying(64) NOT NULL,
    upd_by character varying(64),
    upd_dtimes timestamp,
    CONSTRAINT ctk_template_pk PRIMARY KEY (lang_code,template_name)
);
COMMENT ON TABLE toolkit.ctk_template IS 'This table has templates of Compliance Toolkit.';
COMMENT ON COLUMN toolkit.ctk_template.lang_code IS 'Lang Code: Language of the template stored.';
COMMENT ON COLUMN toolkit.ctk_template.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.ctk_template.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.ctk_template.template_name IS 'Template Name: Name of the template saved.';
COMMENT ON COLUMN toolkit.ctk_template.template IS 'Template: Stores the actual template data.';
COMMENT ON COLUMN toolkit.ctk_template.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN toolkit.ctk_template.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';