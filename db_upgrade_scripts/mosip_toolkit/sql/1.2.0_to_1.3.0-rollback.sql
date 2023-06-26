\c mosip_toolkit sysadmin

DROP INDEX IF EXISTS idx_datashare_tokens_partner_id_testCase_id_testRun_id;
DROP TABLE IF EXISTS toolkit.datashare_tokens;
ALTER TABLE toolkit.abis_projects DROP COLUMN modality;
COMMENT ON COLUMN toolkit.abis_projects.modality IS NULL;