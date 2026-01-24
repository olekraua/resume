--
-- PostgreSQL database dump
--

-- Dumped from database version 16.8 (Homebrew)
-- Dumped by pg_dump version 16.8 (Homebrew)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: certificate_seq; Type: SEQUENCE; Schema: public; Owner: resume
--

CREATE SEQUENCE public.certificate_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.certificate_seq OWNER TO resume;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: certificate; Type: TABLE; Schema: public; Owner: resume
--

CREATE TABLE public.certificate (
    id bigint DEFAULT nextval('public.certificate_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    name character varying(50) NOT NULL,
    large_url character varying(255) NOT NULL,
    small_url character varying(255) NOT NULL,
    issuer character varying(50) NOT NULL
);


ALTER TABLE public.certificate OWNER TO resume;

--
-- Name: course_seq; Type: SEQUENCE; Schema: public; Owner: resume
--

CREATE SEQUENCE public.course_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.course_seq OWNER TO resume;

--
-- Name: course; Type: TABLE; Schema: public; Owner: resume
--

CREATE TABLE public.course (
    id bigint DEFAULT nextval('public.course_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    name character varying(60) NOT NULL,
    school character varying(60) NOT NULL,
    finish_date date
);


ALTER TABLE public.course OWNER TO resume;

--
-- Name: education_seq; Type: SEQUENCE; Schema: public; Owner: resume
--

CREATE SEQUENCE public.education_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.education_seq OWNER TO resume;

--
-- Name: education; Type: TABLE; Schema: public; Owner: resume
--

CREATE TABLE public.education (
    id bigint DEFAULT nextval('public.education_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    summary character varying(100) NOT NULL,
    begin_year integer NOT NULL,
    finish_year integer,
    university text NOT NULL,
    faculty character varying(255) NOT NULL
);


ALTER TABLE public.education OWNER TO resume;

--
-- Name: hobby_seq; Type: SEQUENCE; Schema: public; Owner: resume
--

CREATE SEQUENCE public.hobby_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.hobby_seq OWNER TO resume;

--
-- Name: hobby; Type: TABLE; Schema: public; Owner: resume
--

CREATE TABLE public.hobby (
    id bigint DEFAULT nextval('public.hobby_seq'::regclass) NOT NULL,
    name character varying(30) NOT NULL
);


ALTER TABLE public.hobby OWNER TO resume;

--
-- Name: language_seq; Type: SEQUENCE; Schema: public; Owner: resume
--

CREATE SEQUENCE public.language_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.language_seq OWNER TO resume;

--
-- Name: language; Type: TABLE; Schema: public; Owner: resume
--

CREATE TABLE public.language (
    id bigint DEFAULT nextval('public.language_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    name character varying(30) NOT NULL,
    level character varying(18) NOT NULL,
    type character varying(7) DEFAULT 'all'::character varying NOT NULL,
    CONSTRAINT language_level_check CHECK (((level)::text = ANY ((ARRAY['beginner'::character varying, 'elementary'::character varying, 'pre_intermediate'::character varying, 'intermediate'::character varying, 'upper_intermediate'::character varying, 'advanced'::character varying, 'proficiency'::character varying])::text[]))),
    CONSTRAINT language_type_check CHECK (((type)::text = ANY ((ARRAY['all'::character varying, 'spoken'::character varying, 'writing'::character varying])::text[])))
);


ALTER TABLE public.language OWNER TO resume;

--
-- Name: persistent_logins; Type: TABLE; Schema: public; Owner: resume
--

CREATE TABLE public.persistent_logins (
    username character varying(64) NOT NULL,
    series character varying(64) NOT NULL,
    token character varying(64) NOT NULL,
    last_used timestamp without time zone NOT NULL
);


ALTER TABLE public.persistent_logins OWNER TO resume;

--
-- Name: practic_seq; Type: SEQUENCE; Schema: public; Owner: resume
--

CREATE SEQUENCE public.practic_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.practic_seq OWNER TO resume;

--
-- Name: practic; Type: TABLE; Schema: public; Owner: resume
--

CREATE TABLE public.practic (
    id bigint DEFAULT nextval('public.practic_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    job_position character varying(100) NOT NULL,
    company character varying(100) NOT NULL,
    begin_date date NOT NULL,
    finish_date date,
    responsibilities text NOT NULL,
    demo character varying(255),
    src character varying(255)
);


ALTER TABLE public.practic OWNER TO resume;

--
-- Name: profile_seq; Type: SEQUENCE; Schema: public; Owner: resume
--

CREATE SEQUENCE public.profile_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.profile_seq OWNER TO resume;

--
-- Name: profile; Type: TABLE; Schema: public; Owner: resume
--

CREATE TABLE public.profile (
    id bigint DEFAULT nextval('public.profile_seq'::regclass) NOT NULL,
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
    CONSTRAINT chk_profile_uid_format CHECK (((uid)::text ~ '^[a-z0-9_-]{3,64}$'::text)),
    CONSTRAINT chk_profile_uid_lowercase CHECK (((uid)::text = lower((uid)::text)))
);


ALTER TABLE public.profile OWNER TO resume;

--
-- Name: profile_hobby; Type: TABLE; Schema: public; Owner: resume
--

CREATE TABLE public.profile_hobby (
    id_profile bigint NOT NULL,
    id_hobby bigint NOT NULL
);


ALTER TABLE public.profile_hobby OWNER TO resume;

--
-- Name: profile_restore; Type: TABLE; Schema: public; Owner: resume
--

CREATE TABLE public.profile_restore (
    id bigint NOT NULL,
    token character varying(64) NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    profile_id bigint NOT NULL
);


ALTER TABLE public.profile_restore OWNER TO resume;

--
-- Name: profile_restore_id_seq; Type: SEQUENCE; Schema: public; Owner: resume
--

ALTER TABLE public.profile_restore ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.profile_restore_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: skill_seq; Type: SEQUENCE; Schema: public; Owner: resume
--

CREATE SEQUENCE public.skill_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.skill_seq OWNER TO resume;

--
-- Name: skill; Type: TABLE; Schema: public; Owner: resume
--

CREATE TABLE public.skill (
    id bigint DEFAULT nextval('public.skill_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    category character varying(50) NOT NULL,
    value text NOT NULL
);


ALTER TABLE public.skill OWNER TO resume;

--
-- Name: skill_category_seq; Type: SEQUENCE; Schema: public; Owner: resume
--

CREATE SEQUENCE public.skill_category_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.skill_category_seq OWNER TO resume;

--
-- Name: skill_category; Type: TABLE; Schema: public; Owner: resume
--

CREATE TABLE public.skill_category (
    id bigint DEFAULT nextval('public.skill_category_seq'::regclass) NOT NULL,
    category character varying(50) NOT NULL
);


ALTER TABLE public.skill_category OWNER TO resume;

--
-- Name: certificate certificate_pkey; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.certificate
    ADD CONSTRAINT certificate_pkey PRIMARY KEY (id);


--
-- Name: course course_pkey; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.course
    ADD CONSTRAINT course_pkey PRIMARY KEY (id);


--
-- Name: education education_pkey; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.education
    ADD CONSTRAINT education_pkey PRIMARY KEY (id);


--
-- Name: hobby hobby_name_unique; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.hobby
    ADD CONSTRAINT hobby_name_unique UNIQUE (name);


--
-- Name: hobby hobby_pkey; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.hobby
    ADD CONSTRAINT hobby_pkey PRIMARY KEY (id);


--
-- Name: language language_pkey; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.language
    ADD CONSTRAINT language_pkey PRIMARY KEY (id);


--
-- Name: language language_profile_name_type_key; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.language
    ADD CONSTRAINT language_profile_name_type_key UNIQUE (id_profile, name, type);


--
-- Name: persistent_logins persistent_logins_pkey; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.persistent_logins
    ADD CONSTRAINT persistent_logins_pkey PRIMARY KEY (username, series);


--
-- Name: practic practic_pkey; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.practic
    ADD CONSTRAINT practic_pkey PRIMARY KEY (id);


--
-- Name: profile profile_email_key; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_email_key UNIQUE (email);


--
-- Name: profile_hobby profile_hobby_pkey; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.profile_hobby
    ADD CONSTRAINT profile_hobby_pkey PRIMARY KEY (id_profile, id_hobby);


--
-- Name: profile profile_phone_key; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_phone_key UNIQUE (phone);


--
-- Name: profile profile_pkey; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_pkey PRIMARY KEY (id);


--
-- Name: profile_restore profile_restore_pkey; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.profile_restore
    ADD CONSTRAINT profile_restore_pkey PRIMARY KEY (id);


--
-- Name: profile_restore profile_restore_uid_key; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.profile_restore
    ADD CONSTRAINT profile_restore_uid_key UNIQUE (token);


--
-- Name: profile profile_uid_key; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_uid_key UNIQUE (uid);


--
-- Name: skill_category skill_category_category_key; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.skill_category
    ADD CONSTRAINT skill_category_category_key UNIQUE (category);


--
-- Name: skill_category skill_category_pkey; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.skill_category
    ADD CONSTRAINT skill_category_pkey PRIMARY KEY (id);


--
-- Name: skill skill_pkey; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.skill
    ADD CONSTRAINT skill_pkey PRIMARY KEY (id);


--
-- Name: profile_restore uk_profile_restore_profile; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.profile_restore
    ADD CONSTRAINT uk_profile_restore_profile UNIQUE (profile_id);


--
-- Name: certificate_idx; Type: INDEX; Schema: public; Owner: resume
--

CREATE INDEX certificate_idx ON public.certificate USING btree (id_profile);


--
-- Name: course_idx; Type: INDEX; Schema: public; Owner: resume
--

CREATE INDEX course_idx ON public.course USING btree (finish_date);


--
-- Name: course_idx1; Type: INDEX; Schema: public; Owner: resume
--

CREATE INDEX course_idx1 ON public.course USING btree (id_profile);


--
-- Name: education_idx; Type: INDEX; Schema: public; Owner: resume
--

CREATE INDEX education_idx ON public.education USING btree (id_profile);


--
-- Name: education_idx1; Type: INDEX; Schema: public; Owner: resume
--

CREATE INDEX education_idx1 ON public.education USING btree (finish_year);


--
-- Name: language_idx; Type: INDEX; Schema: public; Owner: resume
--

CREATE INDEX language_idx ON public.language USING btree (id_profile);


--
-- Name: practic_idx; Type: INDEX; Schema: public; Owner: resume
--

CREATE INDEX practic_idx ON public.practic USING btree (id_profile);


--
-- Name: practic_idx1; Type: INDEX; Schema: public; Owner: resume
--

CREATE INDEX practic_idx1 ON public.practic USING btree (finish_date);


--
-- Name: profile_hobby_hobby_idx; Type: INDEX; Schema: public; Owner: resume
--

CREATE INDEX profile_hobby_hobby_idx ON public.profile_hobby USING btree (id_hobby);


--
-- Name: profile_hobby_profile_idx; Type: INDEX; Schema: public; Owner: resume
--

CREATE INDEX profile_hobby_profile_idx ON public.profile_hobby USING btree (id_profile);


--
-- Name: skill_idx; Type: INDEX; Schema: public; Owner: resume
--

CREATE INDEX skill_idx ON public.skill USING btree (id_profile);


--
-- Name: certificate certificate_fk; Type: FK CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.certificate
    ADD CONSTRAINT certificate_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: course course_fk; Type: FK CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.course
    ADD CONSTRAINT course_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: education education_fk; Type: FK CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.education
    ADD CONSTRAINT education_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profile_restore fk_profile_restore_profile; Type: FK CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.profile_restore
    ADD CONSTRAINT fk_profile_restore_profile FOREIGN KEY (profile_id) REFERENCES public.profile(id) ON DELETE CASCADE;


--
-- Name: language language_fk; Type: FK CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.language
    ADD CONSTRAINT language_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: persistent_logins persistent_logins_fk; Type: FK CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.persistent_logins
    ADD CONSTRAINT persistent_logins_fk FOREIGN KEY (username) REFERENCES public.profile(uid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: practic practic_fk; Type: FK CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.practic
    ADD CONSTRAINT practic_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profile_hobby profile_hobby_hobby_fk; Type: FK CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.profile_hobby
    ADD CONSTRAINT profile_hobby_hobby_fk FOREIGN KEY (id_hobby) REFERENCES public.hobby(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profile_hobby profile_hobby_profile_fk; Type: FK CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.profile_hobby
    ADD CONSTRAINT profile_hobby_profile_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: skill skill_fk; Type: FK CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.skill
    ADD CONSTRAINT skill_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: pg_database_owner
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO resume;
GRANT ALL ON SCHEMA public TO postgres;


--
-- PostgreSQL database dump complete
--

