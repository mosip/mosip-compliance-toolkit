\c mosip_toolkit sysadmin

ALTER TABLE toolkit.sbi_projects Add COLUMN device_images character varying(256) NOT NULL;
ALTER TABLE toolkit.sbi_projects Add COLUMN make character varying(256) NOT NULL;
ALTER TABLE toolkit.sbi_projects Add COLUMN model character varying(256) NOT NULL;
ALTER TABLE toolkit.sbi_projects Add COLUMN sbi_hash character varying(256) NOT NULL;
ALTER TABLE toolkit.sbi_projects Add COLUMN website_url character varying(256) NOT NULL;
COMMENT ON COLUMN toolkit.sbi_projects.device_images IS 'device_images: Name of the device images';
COMMENT ON COLUMN toolkit.sbi_projects.make IS 'make: Make information for creating test report';
COMMENT ON COLUMN toolkit.sbi_projects.model IS 'model: Model information for creating test report';
COMMENT ON COLUMN toolkit.sbi_projects.sbi_hash IS 'sbi_hash: Encoded hash of SBI installation file';
COMMENT ON COLUMN toolkit.sbi_projects.website_url IS 'website_url: Partner website url';

ALTER TABLE toolkit.sdk_projects Add COLUMN sdk_hash character varying(256) NOT NULL;
ALTER TABLE toolkit.sdk_projects Add COLUMN website_url character varying(256) NOT NULL;
COMMENT ON COLUMN toolkit.sdk_projects.sdk_hash IS 'sdk_hash: Encoded hash of SDK installation file';
COMMENT ON COLUMN toolkit.sdk_projects.website_url IS 'website_url: Partner website url';

ALTER TABLE toolkit.abis_projects Add COLUMN abis_hash character varying(256) NOT NULL;
ALTER TABLE toolkit.abis_projects Add COLUMN website_url character varying(256) NOT NULL;
COMMENT ON COLUMN toolkit.abis_projects.abis_hash IS 'abis_hash: Encoded hash of ABIS installation file';
COMMENT ON COLUMN toolkit.abis_projects.website_url IS 'website_url: Partner website url';