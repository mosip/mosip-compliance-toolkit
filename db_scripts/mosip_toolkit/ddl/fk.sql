
-- Foreign Key Constraints Same DB/Schema tables.

-- FOREIGN KEY CONSTRAINTS : mosip_toolkit database/schema.

ALTER TABLE toolkit.collections ADD CONSTRAINT fk_sbi_projects_collections FOREIGN KEY (sbi_project_id)
REFERENCES toolkit.sbi_projects (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
--ddl-end --

ALTER TABLE toolkit.collections ADD CONSTRAINT fk_abis_projects_collections FOREIGN KEY (abis_project_id)
REFERENCES toolkit.abis_projects (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
--ddl-end --

ALTER TABLE toolkit.collections ADD CONSTRAINT fk_sdk_projects_collections FOREIGN KEY (sdk_project_id)
REFERENCES toolkit.sdk_projects (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
--ddl-end --
