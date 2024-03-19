\c mosip_toolkit sysadmin

-- Batch Job Tables required by Spring Framework
CREATE TABLE toolkit.batch_job_execution
(
    job_execution_id bigint NOT NULL,
    version bigint,
    job_instance_id bigint NOT NULL,
    create_time timestamp without time zone NOT NULL,
    start_time timestamp without time zone,
    end_time timestamp without time zone,
    status character varying(10) COLLATE pg_catalog."default",
    exit_code character varying(2500) COLLATE pg_catalog."default",
    exit_message character varying(2500) COLLATE pg_catalog."default",
    last_updated timestamp without time zone,
    job_configuration_location character varying(2500) COLLATE pg_catalog."default",
    CONSTRAINT batch_job_execution_pkey PRIMARY KEY (job_execution_id)
)
WITH (
    OIDS = FALSE
);
CREATE TABLE toolkit.batch_job_execution_context
(
    job_execution_id bigint NOT NULL,
    short_context character varying(2500) COLLATE pg_catalog."default" NOT NULL,
    serialized_context text COLLATE pg_catalog."default",
    CONSTRAINT batch_job_execution_context_pkey PRIMARY KEY (job_execution_id)
)
WITH (
    OIDS = FALSE
);
CREATE TABLE toolkit.batch_job_execution_params
(
    job_execution_id bigint NOT NULL,
    type_cd character varying(6) COLLATE pg_catalog."default" NOT NULL,
    key_name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    string_val character varying(250) COLLATE pg_catalog."default",
    date_val timestamp without time zone,
    long_val bigint,
    double_val double precision,
    identifying character(1) COLLATE pg_catalog."default" NOT NULL    
)
WITH (
    OIDS = FALSE
);
CREATE TABLE toolkit.batch_job_instance
(
    job_instance_id bigint NOT NULL,
    version bigint,
    job_name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    job_key character varying(32) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT batch_job_instance_pkey PRIMARY KEY (job_instance_id),
    CONSTRAINT job_inst_un UNIQUE (job_name, job_key)
)
WITH (
    OIDS = FALSE
);
CREATE TABLE toolkit.batch_step_execution
(
    step_execution_id bigint NOT NULL,
    version bigint NOT NULL,
    step_name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    job_execution_id bigint NOT NULL,
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    status character varying(10) COLLATE pg_catalog."default",
    commit_count bigint,
    read_count bigint,
    filter_count bigint,
    write_count bigint,
    read_skip_count bigint,
    write_skip_count bigint,
    process_skip_count bigint,
    rollback_count bigint,
    exit_code character varying(2500) COLLATE pg_catalog."default",
    exit_message character varying(2500) COLLATE pg_catalog."default",
    last_updated timestamp without time zone,
    CONSTRAINT batch_step_execution_pkey PRIMARY KEY (step_execution_id)
)
WITH (
    OIDS = FALSE
);
CREATE TABLE toolkit.batch_step_execution_context
(
    step_execution_id bigint NOT NULL,
    short_context character varying(2500) COLLATE pg_catalog."default" NOT NULL,
    serialized_context text COLLATE pg_catalog."default",
    CONSTRAINT batch_step_execution_context_pkey PRIMARY KEY (step_execution_id)
    
)
WITH (
    OIDS = FALSE
);
ALTER TABLE toolkit.batch_job_execution_params ADD CONSTRAINT job_exec_params_fk FOREIGN KEY (job_execution_id)
        REFERENCES toolkit.batch_job_execution (job_execution_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION ;

ALTER TABLE toolkit.batch_job_execution_context ADD CONSTRAINT job_exec_ctx_fk FOREIGN KEY (job_execution_id)
        REFERENCES toolkit.batch_job_execution (job_execution_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE toolkit.batch_job_execution ADD CONSTRAINT job_inst_exec_fk FOREIGN KEY (job_instance_id)
        REFERENCES toolkit.batch_job_instance (job_instance_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE toolkit.batch_step_execution ADD CONSTRAINT job_exec_step_fk FOREIGN KEY (job_execution_id)
        REFERENCES toolkit.batch_job_execution (job_execution_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE toolkit.batch_step_execution_context ADD CONSTRAINT step_exec_ctx_fk FOREIGN KEY (step_execution_id)
        REFERENCES toolkit.batch_step_execution (step_execution_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;
		
CREATE SEQUENCE toolkit.batch_step_execution_seq;
CREATE SEQUENCE toolkit.batch_job_execution_seq;
CREATE SEQUENCE toolkit.batch_job_seq;

-- grants to access all sequences
GRANT usage, SELECT ON ALL SEQUENCES 
   IN SCHEMA toolkit
   TO toolkituser;

-- This table has consents of partners for uploading biometrics.
CREATE TABLE toolkit.partner_profile(
    partner_id character varying(36) NOT NULL,
    org_name character varying(64) NOT NULL,
    consent_for_biometrics character varying(36) NOT NULL DEFAULT 'NO',
    consent_acceptance_dtimes timestamp NOT NULL,
    cr_dtimes timestamp NOT NULL,
    cr_by character varying(64) NOT NULL,
    upd_by character varying(64),
    upd_dtimes timestamp,
    CONSTRAINT partner_profile_pk PRIMARY KEY (partner_id,org_name),
    CONSTRAINT consent_for_biometrics CHECK (consent_for_biometrics IN ('YES', 'NO'))
);
COMMENT ON TABLE toolkit.partner_profile IS 'This table has consents of partners.';
COMMENT ON COLUMN toolkit.partner_profile.partner_id IS 'Partner Id: partner who has logged in.';
COMMENT ON COLUMN toolkit.partner_profile.consent_acceptance_dtimes IS 'Consent Acceptance DateTimestamp : Date and Timestamp when the consent is accepted.';
COMMENT ON COLUMN toolkit.partner_profile.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.partner_profile.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.partner_profile.org_name IS 'Orgname: organization name to which partner belongs to.';
COMMENT ON COLUMN toolkit.partner_profile.consent_for_biometrics IS 'Consent for biometrics';
COMMENT ON COLUMN toolkit.partner_profile.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN toolkit.partner_profile.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';

-- This table has compliance toolkit templates.
CREATE TABLE toolkit.custom_templates(
	id character varying(36) NOT NULL,
    lang_code character varying(36) NOT NULL,
    template_name character varying(64) NOT NULL,
    template character varying NOT NULL,
    cr_dtimes timestamp NOT NULL,
    cr_by character varying(64) NOT NULL,
    upd_by character varying(64),
    upd_dtimes timestamp,
    CONSTRAINT custom_templates_pk PRIMARY KEY (id)
);
COMMENT ON TABLE toolkit.custom_templates IS 'This table has templates of Compliance Toolkit.';
COMMENT ON COLUMN toolkit.custom_templates.id IS 'ID: Unique Id generated for an template.';
COMMENT ON COLUMN toolkit.custom_templates.lang_code IS 'Lang Code: Language of the template stored.';
COMMENT ON COLUMN toolkit.custom_templates.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.custom_templates.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.custom_templates.template_name IS 'Template Name: Name of the template saved.';
COMMENT ON COLUMN toolkit.custom_templates.template IS 'Template: Stores the actual template data.';
COMMENT ON COLUMN toolkit.custom_templates.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN toolkit.custom_templates.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';

-- update username and password in base64 encode format
UPDATE toolkit.abis_projects SET username = encode(username::bytea, 'base64');
UPDATE toolkit.abis_projects SET password = encode(password::bytea, 'base64');