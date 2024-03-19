\c mosip_toolkit sysadmin

DROP SEQUENCE IF EXISTS toolkit.batch_step_execution_seq;
DROP SEQUENCE IF EXISTS toolkit.batch_job_execution_seq;
DROP SEQUENCE IF EXISTS toolkit.batch_job_seq;
ALTER TABLE toolkit.batch_job_execution_params DROP CONSTRAINT job_exec_params_fk;
ALTER TABLE toolkit.batch_job_execution_context DROP CONSTRAINT job_exec_ctx_fk;
ALTER TABLE toolkit.batch_job_execution DROP CONSTRAINT job_inst_exec_fk;
ALTER TABLE toolkit.batch_step_execution DROP CONSTRAINT job_exec_step_fk;
ALTER TABLE toolkit.batch_step_execution_context DROP CONSTRAINT step_exec_ctx_fk;
DROP TABLE IF EXISTS toolkit.batch_job_execution_params;
DROP TABLE IF EXISTS toolkit.batch_job_execution_context;
DROP TABLE IF EXISTS toolkit.batch_step_execution;
DROP TABLE IF EXISTS toolkit.batch_step_execution_context;
DROP TABLE IF EXISTS toolkit.batch_job_execution;
DROP TABLE IF EXISTS toolkit.batch_job_instance;

-- decode username and password in abis projects
UPDATE toolkit.abis_projects SET username = convert_from(decode(username, 'base64'), 'UTF8');
UPDATE toolkit.abis_projects SET password = convert_from(decode(password, 'base64'), 'UTF8');

-- custom_templates
DROP TABLE IF EXISTS toolkit.custom_templates;

-- partner_profile
DROP TABLE IF EXISTS toolkit.partner_profile;