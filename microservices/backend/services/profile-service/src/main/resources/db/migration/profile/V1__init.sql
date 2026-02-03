-- Profile DB schema (extracted from resume_db_schema_2026-02-03.sql)
-- Cross-DB foreign keys to staticdata (hobby) are intentionally omitted.

CREATE SEQUENCE certificate_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE course_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE education_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE language_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE practic_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE profile_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE profile_connection_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE skill_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE profile (
    id bigint DEFAULT nextval('profile_seq'::regclass) NOT NULL,
    uid character varying(64) NOT NULL,
    first_name character varying(64) NOT NULL,
    last_name character varying(64) NOT NULL,
    birth_day date,
    phone character varying(20),
    email character varying(100),
    country character varying(60),
    city character varying(100),
    objective text,
    summary text,
    large_photo character varying(255),
    small_photo character varying(255),
    info text,
    password character varying(255) NOT NULL,
    completed boolean NOT NULL,
    created timestamp(0) without time zone DEFAULT now() NOT NULL,
    facebook character varying(255),
    linkedin character varying(255),
    github character varying(255),
    stackoverflow character varying(255),
    connections_visible boolean DEFAULT true NOT NULL,
    CONSTRAINT chk_profile_uid_format CHECK (((uid)::text ~ '^[a-z0-9_-]{3,64}$'::text)),
    CONSTRAINT chk_profile_uid_lowercase CHECK (((uid)::text = lower((uid)::text)))
);

CREATE TABLE certificate (
    id bigint DEFAULT nextval('certificate_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    name character varying(50) NOT NULL,
    large_url character varying(255) NOT NULL,
    small_url character varying(255) NOT NULL,
    issuer character varying(50) NOT NULL
);

CREATE TABLE course (
    id bigint DEFAULT nextval('course_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    name character varying(60) NOT NULL,
    school character varying(60) NOT NULL,
    finish_date date
);

CREATE TABLE education (
    id bigint DEFAULT nextval('education_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    summary character varying(100) NOT NULL,
    begin_year integer NOT NULL,
    finish_year integer,
    university text NOT NULL,
    faculty character varying(255) NOT NULL
);

CREATE TABLE language (
    id bigint DEFAULT nextval('language_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    name character varying(30) NOT NULL,
    level character varying(18) NOT NULL,
    type character varying(7) DEFAULT 'all'::character varying NOT NULL,
    CONSTRAINT language_level_check CHECK (((level)::text = ANY ((ARRAY['beginner'::character varying, 'elementary'::character varying, 'pre_intermediate'::character varying, 'intermediate'::character varying, 'upper_intermediate'::character varying, 'advanced'::character varying, 'proficiency'::character varying])::text[]))),
    CONSTRAINT language_type_check CHECK (((type)::text = ANY ((ARRAY['all'::character varying, 'spoken'::character varying, 'writing'::character varying])::text[])))
);

CREATE TABLE practic (
    id bigint DEFAULT nextval('practic_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    job_position character varying(100) NOT NULL,
    company character varying(100) NOT NULL,
    begin_date date NOT NULL,
    finish_date date,
    responsibilities text NOT NULL,
    demo character varying(255),
    src character varying(255)
);

CREATE TABLE profile_connection (
    id bigint DEFAULT nextval('profile_connection_id_seq'::regclass) NOT NULL,
    pair_key character varying(64) NOT NULL,
    requester_id bigint NOT NULL,
    addressee_id bigint NOT NULL,
    status character varying(16) NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    responded timestamp with time zone,
    CONSTRAINT chk_profile_connection_self CHECK ((requester_id <> addressee_id))
);

CREATE TABLE profile_hobby (
    id_profile bigint NOT NULL,
    id_hobby bigint NOT NULL
);

CREATE TABLE skill (
    id bigint DEFAULT nextval('skill_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    category character varying(50) NOT NULL,
    value text NOT NULL
);

ALTER SEQUENCE profile_connection_id_seq OWNED BY profile_connection.id;

ALTER TABLE ONLY certificate
    ADD CONSTRAINT certificate_pkey PRIMARY KEY (id);

ALTER TABLE ONLY course
    ADD CONSTRAINT course_pkey PRIMARY KEY (id);

ALTER TABLE ONLY education
    ADD CONSTRAINT education_pkey PRIMARY KEY (id);

ALTER TABLE ONLY language
    ADD CONSTRAINT language_pkey PRIMARY KEY (id);

ALTER TABLE ONLY language
    ADD CONSTRAINT language_profile_name_type_key UNIQUE (id_profile, name, type);

ALTER TABLE ONLY practic
    ADD CONSTRAINT practic_pkey PRIMARY KEY (id);

ALTER TABLE ONLY profile_connection
    ADD CONSTRAINT profile_connection_pkey PRIMARY KEY (id);

ALTER TABLE ONLY profile_hobby
    ADD CONSTRAINT profile_hobby_pkey PRIMARY KEY (id_profile, id_hobby);

ALTER TABLE ONLY profile
    ADD CONSTRAINT profile_pkey PRIMARY KEY (id);

ALTER TABLE ONLY profile
    ADD CONSTRAINT profile_email_key UNIQUE (email);

ALTER TABLE ONLY profile
    ADD CONSTRAINT profile_phone_key UNIQUE (phone);

ALTER TABLE ONLY profile
    ADD CONSTRAINT profile_uid_key UNIQUE (uid);

ALTER TABLE ONLY skill
    ADD CONSTRAINT skill_pkey PRIMARY KEY (id);

CREATE INDEX certificate_idx ON certificate USING btree (id_profile);

CREATE UNIQUE INDEX certificate_unique_profile_name_issuer_idx ON certificate USING btree (
    id_profile,
    lower(regexp_replace(TRIM(BOTH FROM name), '\s+'::text, ' '::text, 'g'::text)),
    lower(regexp_replace(TRIM(BOTH FROM issuer), '\s+'::text, ' '::text, 'g'::text))
);

CREATE INDEX course_idx ON course USING btree (finish_date);
CREATE INDEX course_idx1 ON course USING btree (id_profile);

CREATE INDEX education_idx ON education USING btree (id_profile);
CREATE INDEX education_idx1 ON education USING btree (finish_year);

CREATE INDEX language_idx ON language USING btree (id_profile);

CREATE INDEX practic_idx ON practic USING btree (id_profile);
CREATE INDEX practic_idx1 ON practic USING btree (finish_date);

CREATE INDEX idx_profile_connection_addressee_status ON profile_connection USING btree (addressee_id, status);
CREATE INDEX idx_profile_connection_requester_status ON profile_connection USING btree (requester_id, status);

CREATE INDEX profile_hobby_hobby_idx ON profile_hobby USING btree (id_hobby);
CREATE INDEX profile_hobby_profile_idx ON profile_hobby USING btree (id_profile);

CREATE INDEX skill_idx ON skill USING btree (id_profile);

CREATE UNIQUE INDEX uk_profile_connection_pair ON profile_connection USING btree (pair_key);

ALTER TABLE ONLY certificate
    ADD CONSTRAINT certificate_fk FOREIGN KEY (id_profile) REFERENCES profile(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY course
    ADD CONSTRAINT course_fk FOREIGN KEY (id_profile) REFERENCES profile(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY education
    ADD CONSTRAINT education_fk FOREIGN KEY (id_profile) REFERENCES profile(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY language
    ADD CONSTRAINT language_fk FOREIGN KEY (id_profile) REFERENCES profile(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY practic
    ADD CONSTRAINT practic_fk FOREIGN KEY (id_profile) REFERENCES profile(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY profile_connection
    ADD CONSTRAINT fk_profile_connection_addressee FOREIGN KEY (addressee_id) REFERENCES profile(id) ON DELETE CASCADE;

ALTER TABLE ONLY profile_connection
    ADD CONSTRAINT fk_profile_connection_requester FOREIGN KEY (requester_id) REFERENCES profile(id) ON DELETE CASCADE;

ALTER TABLE ONLY profile_hobby
    ADD CONSTRAINT profile_hobby_profile_fk FOREIGN KEY (id_profile) REFERENCES profile(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY skill
    ADD CONSTRAINT skill_fk FOREIGN KEY (id_profile) REFERENCES profile(id) ON UPDATE CASCADE ON DELETE CASCADE;
