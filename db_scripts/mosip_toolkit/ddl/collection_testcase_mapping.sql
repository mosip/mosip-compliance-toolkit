-- This table has all mappings between a collection and test cases for the compliance toolkit.

CREATE TABLE toolkit.collection_testcase_mapping(
	collection_id character varying(36) NOT NULL,
    testcase_id character varying(36) NOT NULL,
	CONSTRAINT collection_testcase_mapping_pk PRIMARY KEY (collection_id, testcase_id) 
);

CREATE INDEX IF NOT EXISTS idx_collection_testcase_mapping ON toolkit.collection_testcase_mapping USING btree (collection_id);
COMMENT ON TABLE toolkit.collection_testcase_mapping IS 'This table has all mappings between a collection and test cases for the compliance toolkit.';
COMMENT ON COLUMN toolkit.collection_testcase_mapping.collection_id IS 'collection_id: unique id for an collection.';
COMMENT ON COLUMN toolkit.collection_testcase_mapping.testcase_id IS 'testcase_id: unique id of the testcase.';
