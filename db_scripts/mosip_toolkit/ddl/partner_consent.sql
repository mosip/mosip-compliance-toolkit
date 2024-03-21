-- This table has consents of partners for biometrics.
CREATE TABLE toolkit.partner_consent(
    partner_id character varying(36) NOT NULL,
    org_name character varying(64) NOT NULL,
    consent_given character varying(36) NOT NULL DEFAULT 'NO',
    consent_given_dtimes timestamp NOT NULL,
    cr_dtimes timestamp NOT NULL,
    cr_by character varying(64) NOT NULL,
    upd_by character varying(64),
    upd_dtimes timestamp,
    CONSTRAINT partner_consent_pk PRIMARY KEY (partner_id,org_name),
    CONSTRAINT consent_given CHECK (consent_given IN ('YES', 'NO'))
);
COMMENT ON TABLE toolkit.partner_consent IS 'This table has consents of partners.';
COMMENT ON COLUMN toolkit.partner_consent.partner_id IS 'Partner Id: partner who has logged in.';
COMMENT ON COLUMN toolkit.partner_consent.consent_given_dtimes IS 'Consent given DateTimestamp : Date and Timestamp when the consent is given.';
COMMENT ON COLUMN toolkit.partner_consent.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.partner_consent.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.partner_consent.org_name IS 'Orgname: organization name to which partner belongs to.';
COMMENT ON COLUMN toolkit.partner_consent.consent_given IS 'Consent Given : Indicates whether consent has been given by the partner.';
COMMENT ON COLUMN toolkit.partner_consent.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN toolkit.partner_consent.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';