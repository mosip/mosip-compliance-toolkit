CREATE TABLE toolkit.master_data
(
    name character varying(64) NOT NULL,
    description character varying(256),
    value_json character varying NOT NULL,
    cr_by character varying(64) NOT NULL,
    cr_dtimes timestamp without time zone NOT NULL,
    PRIMARY KEY (name)
)
WITH (
    OIDS = FALSE
);

ALTER TABLE IF EXISTS toolkit.master_data
    OWNER to postgres;