-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_toolkit
-- Release Version 	: 1.0.0-b2
-- Purpose    		: Revoking Database Alter deployement done for release in toolkit DB.      
-- Create By   		: Srinivas Prabhakar
-- Created Date		: Dec-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_toolkit sysadmin

ALTER TABLE IF EXISTS toolkit.biometric_testdata
	DROP COLUMN IF EXISTS file_hash;

ALTER TABLE IF EXISTS toolkit.test_run_details
	DROP COLUMN IF EXISTS test_data_source;

ALTER TABLE IF EXISTS toolkit.test_run_details
	DROP COLUMN IF EXISTS method_url;
	
ALTER TABLE IF EXISTS toolkit.test_run_details_archive
	DROP COLUMN IF EXISTS test_data_source;

ALTER TABLE IF EXISTS toolkit.test_run_details_archive
	DROP COLUMN IF EXISTS method_url;