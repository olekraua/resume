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
-- Name: certificate_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.certificate_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: certificate; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.certificate (
    id bigint DEFAULT nextval('public.certificate_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    name character varying(50) NOT NULL,
    large_url character varying(255) NOT NULL,
    small_url character varying(255) NOT NULL,
    issuer character varying(50) NOT NULL
);


--
-- Name: course_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.course_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: course; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.course (
    id bigint DEFAULT nextval('public.course_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    name character varying(60) NOT NULL,
    school character varying(60) NOT NULL,
    finish_date date
);


--
-- Name: education_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.education_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: education; Type: TABLE; Schema: public; Owner: -
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


--
-- Name: hobby_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.hobby_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: hobby; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.hobby (
    id bigint DEFAULT nextval('public.hobby_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    name character varying(30) NOT NULL
);


--
-- Name: language_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.language_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: language; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.language (
    id bigint DEFAULT nextval('public.language_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    name character varying(30) NOT NULL,
    level character varying(18) NOT NULL,
    type character varying(7) DEFAULT '0'::character varying NOT NULL
);


--
-- Name: persistent_logins; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.persistent_logins (
    username character varying(64) NOT NULL,
    series character varying(64) NOT NULL,
    token character varying(64) NOT NULL,
    last_used timestamp without time zone NOT NULL
);


--
-- Name: practic_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.practic_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: practic; Type: TABLE; Schema: public; Owner: -
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


--
-- Name: profile_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.profile_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: profile; Type: TABLE; Schema: public; Owner: -
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
    stackoverflow character varying(255)
);


--
-- Name: profile_restore; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.profile_restore (
    id bigint NOT NULL,
    token character varying(255) NOT NULL
);


--
-- Name: skill_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.skill_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: skill; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.skill (
    id bigint DEFAULT nextval('public.skill_seq'::regclass) NOT NULL,
    id_profile bigint NOT NULL,
    category character varying(50) NOT NULL,
    value text NOT NULL
);


--
-- Name: skill_category_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.skill_category_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: skill_category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.skill_category (
    id bigint DEFAULT nextval('public.skill_category_seq'::regclass) NOT NULL,
    category character varying(50) NOT NULL
);


--
-- Data for Name: certificate; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.certificate (id, id_profile, name, large_url, small_url, issuer) FROM stdin;
\.


--
-- Data for Name: course; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.course (id, id_profile, name, school, finish_date) FROM stdin;
\.


--
-- Data for Name: education; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.education (id, id_profile, summary, begin_year, finish_year, university, faculty) FROM stdin;
\.


--
-- Data for Name: hobby; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.hobby (id, id_profile, name) FROM stdin;
\.


--
-- Data for Name: language; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.language (id, id_profile, name, level, type) FROM stdin;
\.


--
-- Data for Name: persistent_logins; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.persistent_logins (username, series, token, last_used) FROM stdin;
\.


--
-- Data for Name: practic; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.practic (id, id_profile, job_position, company, begin_date, finish_date, responsibilities, demo, src) FROM stdin;
\.


--
-- Data for Name: profile; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.profile (id, uid, first_name, last_name, birth_day, phone, email, country, city, objective, summary, large_photo, small_photo, info, password, completed, created, facebook, linkedin, github, stackoverflow) FROM stdin;
2	john-doe	John	Doe	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	test123	t	2025-08-15 12:20:33	\N	\N	\N	\N
3	jane-smith	Jane	Smith	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	test123	t	2025-08-15 12:20:33	\N	\N	\N	\N
\.


--
-- Data for Name: profile_restore; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.profile_restore (id, token) FROM stdin;
\.


--
-- Data for Name: skill; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.skill (id, id_profile, category, value) FROM stdin;
\.


--
-- Data for Name: skill_category; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.skill_category (id, category) FROM stdin;
\.


--
-- Name: certificate_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.certificate_seq', 1, false);


--
-- Name: course_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.course_seq', 1, false);


--
-- Name: education_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.education_seq', 1, false);


--
-- Name: hobby_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.hobby_seq', 1, false);


--
-- Name: language_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.language_seq', 1, false);


--
-- Name: practic_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.practic_seq', 1, false);


--
-- Name: profile_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.profile_seq', 3, true);


--
-- Name: skill_category_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.skill_category_seq', 1, false);


--
-- Name: skill_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.skill_seq', 1, false);


--
-- Name: certificate certificate_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.certificate
    ADD CONSTRAINT certificate_pkey PRIMARY KEY (id);


--
-- Name: course course_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.course
    ADD CONSTRAINT course_pkey PRIMARY KEY (id);


--
-- Name: education education_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.education
    ADD CONSTRAINT education_pkey PRIMARY KEY (id);


--
-- Name: hobby hobby_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hobby
    ADD CONSTRAINT hobby_pkey PRIMARY KEY (id);


--
-- Name: language language_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.language
    ADD CONSTRAINT language_pkey PRIMARY KEY (id);


--
-- Name: persistent_logins persistent_logins_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.persistent_logins
    ADD CONSTRAINT persistent_logins_pkey PRIMARY KEY (username, series);


--
-- Name: practic practic_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.practic
    ADD CONSTRAINT practic_pkey PRIMARY KEY (id);


--
-- Name: profile profile_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_email_key UNIQUE (email);


--
-- Name: profile profile_phone_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_phone_key UNIQUE (phone);


--
-- Name: profile profile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_pkey PRIMARY KEY (id);


--
-- Name: profile_restore profile_restore_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_restore
    ADD CONSTRAINT profile_restore_pkey PRIMARY KEY (id);


--
-- Name: profile_restore profile_restore_uid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_restore
    ADD CONSTRAINT profile_restore_uid_key UNIQUE (token);


--
-- Name: profile profile_uid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_uid_key UNIQUE (uid);


--
-- Name: skill_category skill_category_category_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skill_category
    ADD CONSTRAINT skill_category_category_key UNIQUE (category);


--
-- Name: skill_category skill_category_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skill_category
    ADD CONSTRAINT skill_category_pkey PRIMARY KEY (id);


--
-- Name: skill skill_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skill
    ADD CONSTRAINT skill_pkey PRIMARY KEY (id);


--
-- Name: certificate_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX certificate_idx ON public.certificate USING btree (id_profile);


--
-- Name: course_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX course_idx ON public.course USING btree (finish_date);


--
-- Name: course_idx1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX course_idx1 ON public.course USING btree (id_profile);


--
-- Name: education_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX education_idx ON public.education USING btree (id_profile);


--
-- Name: education_idx1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX education_idx1 ON public.education USING btree (finish_year);


--
-- Name: hobby_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX hobby_idx ON public.hobby USING btree (id_profile);


--
-- Name: language_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX language_idx ON public.language USING btree (id_profile);


--
-- Name: practic_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX practic_idx ON public.practic USING btree (id_profile);


--
-- Name: practic_idx1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX practic_idx1 ON public.practic USING btree (finish_date);


--
-- Name: skill_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX skill_idx ON public.skill USING btree (id_profile);


--
-- Name: certificate certificate_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.certificate
    ADD CONSTRAINT certificate_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: course course_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.course
    ADD CONSTRAINT course_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: education education_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.education
    ADD CONSTRAINT education_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: hobby hobby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hobby
    ADD CONSTRAINT hobby_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: language language_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.language
    ADD CONSTRAINT language_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: persistent_logins persistent_logins_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.persistent_logins
    ADD CONSTRAINT persistent_logins_fk FOREIGN KEY (username) REFERENCES public.profile(uid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: practic practic_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.practic
    ADD CONSTRAINT practic_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profile_restore profile_restore_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_restore
    ADD CONSTRAINT profile_restore_fk FOREIGN KEY (id) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: skill skill_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skill
    ADD CONSTRAINT skill_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: -
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO resume;
GRANT ALL ON SCHEMA public TO postgres;


--
-- PostgreSQL database dump complete
--

