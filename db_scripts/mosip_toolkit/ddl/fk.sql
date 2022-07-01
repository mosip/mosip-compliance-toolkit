
-- Foreign Key Constraints Same DB/Schema tables.

-- FOREIGN KEY CONSTRAINTS : mosip_toolkit database/schema.

-- Example:
-- object: fk_appldoc_appldem | type: CONSTRAINT --
-- ALTER TABLE prereg.applicant_document DROP CONSTRAINT IF EXISTS fk_appldoc_appldem CASCADE;
-- ALTER TABLE prereg.applicant_document ADD CONSTRAINT fk_appldoc_appldem FOREIGN KEY (prereg_id)
-- REFERENCES prereg.applicant_demographic (prereg_id) MATCH SIMPLE
-- ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --
