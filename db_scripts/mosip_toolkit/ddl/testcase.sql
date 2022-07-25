-- This table has all the test cases for the compliance toolkit

CREATE TABLE toolkit.testcase(
	id character varying(36) NOT NULL,
	testcase_json character varying NOT NULL,
	testcase_type character varying(64) NOT NULL,
	spec_version character varying(20) NOT NULL,
	CONSTRAINT testcaseid_pk PRIMARY KEY (id) 
);

CREATE INDEX IF NOT EXISTS idx_testcase ON toolkit.testcase USING btree (id);
CREATE INDEX IF NOT EXISTS idx_testcase_type ON toolkit.testcase USING btree (testcase_type);
COMMENT ON TABLE toolkit.testcase IS 'This table has all the test cases for the compliance toolkit.';
COMMENT ON COLUMN toolkit.testcase.id IS 'ID: Unique Id generated for an testcase.';
COMMENT ON COLUMN toolkit.testcase.testcase_json IS 'testcase_json: json details of the testcase.';
COMMENT ON COLUMN toolkit.testcase.testcase_type IS 'testcase_type: type of testcase SDK, SBI or ABIS.';