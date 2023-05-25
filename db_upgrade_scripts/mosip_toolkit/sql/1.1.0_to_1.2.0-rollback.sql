\c mosip_toolkit sysadmin

COMMENT ON COLUMN toolkit.abis_projects.abis_version IS NULL;
COMMENT ON COLUMN toolkit.abis_projects.bio_test_data_file_name IS NULL;
COMMENT ON COLUMN toolkit.abis_projects.outbound_queue_name IS NULL;
COMMENT ON COLUMN toolkit.abis_projects.inbound_queue_name IS NULL;
ALTER TABLE toolkit.abis_projects DROP COLUMN abis_version;
ALTER TABLE toolkit.abis_projects DROP COLUMN bio_test_data_file_name;
ALTER TABLE toolkit.abis_projects DROP COLUMN outbound_queue_name;
ALTER TABLE toolkit.abis_projects RENAME COLUMN inbound_queue_name TO queue_name;
ALTER TABLE toolkit.abis_projects ADD COLUMN purpose;


