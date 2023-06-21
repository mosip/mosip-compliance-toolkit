\c mosip_toolkit sysadmin

COMMENT ON COLUMN toolkit.abis_projects.modality IS NULL;
ALTER TABLE toolkit.abis_projects DROP COLUMN modality;
