-- Static data DB schema (extracted from resume_db_schema_2026-02-03.sql)

CREATE SEQUENCE hobby_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE skill_category_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE hobby (
    id bigint DEFAULT nextval('hobby_seq'::regclass) NOT NULL,
    name character varying(30) NOT NULL
);

CREATE TABLE skill_category (
    id bigint DEFAULT nextval('skill_category_seq'::regclass) NOT NULL,
    category character varying(50) NOT NULL
);

ALTER TABLE ONLY hobby
    ADD CONSTRAINT hobby_pkey PRIMARY KEY (id);

ALTER TABLE ONLY hobby
    ADD CONSTRAINT hobby_name_unique UNIQUE (name);

ALTER TABLE ONLY skill_category
    ADD CONSTRAINT skill_category_pkey PRIMARY KEY (id);

ALTER TABLE ONLY skill_category
    ADD CONSTRAINT skill_category_category_key UNIQUE (category);
