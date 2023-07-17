ALTER TABLE toolkit.sbi_projects DROP COLUMN device_images;
ALTER TABLE toolkit.sbi_projects DROP COLUMN make;
ALTER TABLE toolkit.sbi_projects DROP COLUMN model;
ALTER TABLE toolkit.sbi_projects DROP COLUMN sbi_hash;
ALTER TABLE toolkit.sbi_projects DROP COLUMN website_url;
COMMENT ON COLUMN toolkit.sbi_projects.device_images IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.make IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.model IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.sbi_hash IS NULL;
COMMENT ON COLUMN toolkit.sbi_projects.website_url IS NULL;

ALTER TABLE toolkit.sdk_projects DROP COLUMN sdk_hash;
ALTER TABLE toolkit.sdk_projects DROP COLUMN website_url;
COMMENT ON COLUMN toolkit.sdk_projects.sdk_hash IS NULL;
COMMENT ON COLUMN toolkit.sdk_projects.website_url IS NULL;

ALTER TABLE toolkit.abis_projects DROP COLUMN abis_hash;
ALTER TABLE toolkit.abis_projects DROP COLUMN website_url;
COMMENT ON COLUMN toolkit.abis_projects.abis_hash IS NULL;
COMMENT ON COLUMN toolkit.abis_projects.website_url IS NULL;