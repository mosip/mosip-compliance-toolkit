\c mosip_toolkit sysadmin

ALTER TABLE toolkit.abis_projects Add COLUMN modality character varying(256) NOT NULL;
COMMENT ON COLUMN toolkit.abis_projects.modality IS 'modality: different modalities combination';