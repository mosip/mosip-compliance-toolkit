\c mosip_toolkit sysadmin

-- This table has consents of partners for uploading biometrics.
CREATE TABLE toolkit.partner_profile(
    partner_id character varying(36) NOT NULL,
    org_name character varying(64) NOT NULL,
    consent_for_sdk_abis_biometrics character varying(36) NOT NULL,
    consent_for_sbi_biometrics character varying(36) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    cr_by character varying(64) NOT NULL,
    upd_by character varying(64),
	upd_dtimes timestamp,
    CONSTRAINT partner_profile_pk PRIMARY KEY (partner_id,org_name)
);
COMMENT ON TABLE toolkit.partner_profile IS 'This table has consents of partners.';
COMMENT ON COLUMN toolkit.partner_profile.partner_id IS 'Partner Id: partner who has logged in.';
COMMENT ON COLUMN toolkit.partner_profile.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.biometric_scores.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.partner_profile.org_name IS 'Orgname: organization name to which partner belongs to.';
COMMENT ON COLUMN toolkit.partner_profile.consent_for_sdk_abis_biometrics IS 'Consent for sdk and abis biometrics';
COMMENT ON COLUMN toolkit.partner_profile.consent_for_sbi_biometrics IS 'Consent for sbi biometrics';
COMMENT ON COLUMN toolkit.test_run.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN toolkit.test_run.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';