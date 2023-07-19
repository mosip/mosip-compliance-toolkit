\c mosip_toolkit sysadmin


-- \ir ../ddl/prereg-applications.sql
COMMENT ON COLUMN toolkit.abis_projects.queue_name IS NULL;
ALTER TABLE toolkit.abis_projects DROP COLUMN queue_name;
ALTER TABLE toolkit.abis_projects Add COLUMN inbound_queue_name character varying(256) NOT NULL;
ALTER TABLE toolkit.abis_projects Add column outbound_queue_name character varying(256) NOT NULL;
ALTER TABLE toolkit.abis_projects Add column bio_test_data_file_name character varying(64) NOT NULL;
ALTER TABLE toolkit.abis_projects Add column abis_version character varying(256) NOT NULL;
COMMENT ON COLUMN toolkit.abis_projects.inbound_queue_name IS 'inbound_queue_name: the inbound queue name for ABIS queue';
COMMENT ON COLUMN toolkit.abis_projects.outbound_queue_name IS 'outbound_queue_name: the outbound queue name for ABIS queue';
COMMENT ON COLUMN toolkit.abis_projects.bio_test_data_file_name IS 'Biometric Test Data File Name : Name of the biometric test data file to be used';
COMMENT ON COLUMN toolkit.abis_projects.abis_version IS 'abis_version: the abis_version is the version of abis.';

