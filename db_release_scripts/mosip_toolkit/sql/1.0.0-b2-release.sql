-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_toolkit
-- Release Version 	: 1.0.0-b2
-- Purpose    		: Database Alter scripts for the release for compliance toolkit DB.       
-- Create By   		: Srinivas Prabhakar
-- Created Date		: Nov-2022
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_toolkit

ALTER TABLE IF EXISTS toolkit.biometric_testdata
    ADD COLUMN IF NOT EXISTS file_hash character varying(64) NOT NULL;
	
ALTER TABLE IF EXISTS toolkit.test_run_details
    ADD COLUMN IF NOT EXISTS test_data_source character varying(256);
	
ALTER TABLE IF EXISTS toolkit.test_run_details_archive
    ADD COLUMN IF NOT EXISTS test_data_source character varying(256);