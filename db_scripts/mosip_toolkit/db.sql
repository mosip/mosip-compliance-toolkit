CREATE DATABASE mosip_toolkit
	ENCODING = 'UTF8'
	LC_COLLATE = 'en_US.UTF-8'
	LC_CTYPE = 'en_US.UTF-8'
	TABLESPACE = pg_default
	OWNER = postgres
	TEMPLATE  = template0;
COMMENT ON DATABASE mosip_toolkit IS 'Toolkit database to store the data for compliance testing';

\c mosip_toolkit 

DROP SCHEMA IF EXISTS toolkit CASCADE;
CREATE SCHEMA toolkit;
ALTER SCHEMA toolkit OWNER TO postgres;
ALTER DATABASE mosip_toolkit SET search_path TO toolkit,pg_catalog,public;
