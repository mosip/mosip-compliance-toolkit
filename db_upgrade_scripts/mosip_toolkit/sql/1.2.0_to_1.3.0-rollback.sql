\c mosip_toolkit sysadmin

DROP TABLE IF EXISTS toolkit.datashare_tokens;
ALTER TABLE toolkit.abis_projects DROP COLUMN modality;
COMMENT ON COLUMN toolkit.abis_projects.modality IS NULL;