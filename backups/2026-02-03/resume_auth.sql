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
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

-- *not* creating schema, since initdb creates it


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: profile_restore; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.profile_restore (
    id bigint NOT NULL,
    token character varying(64) NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    profile_id bigint NOT NULL
);


--
-- Name: profile_restore_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.profile_restore_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: profile_restore_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.profile_restore_id_seq OWNED BY public.profile_restore.id;


--
-- Name: remember_me_token; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.remember_me_token (
    series character varying(64) NOT NULL,
    token character varying(64) NOT NULL,
    last_used timestamp with time zone NOT NULL,
    profile_id bigint NOT NULL,
    username character varying(64) NOT NULL
);


--
-- Name: profile_restore id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_restore ALTER COLUMN id SET DEFAULT nextval('public.profile_restore_id_seq'::regclass);


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	<< Flyway Baseline >>	BASELINE	<< Flyway Baseline >>	\N	resume	2026-02-01 23:43:55.895111	0	t
\.


--
-- Data for Name: profile_restore; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.profile_restore (id, token, created, profile_id) FROM stdin;
\.


--
-- Data for Name: remember_me_token; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.remember_me_token (series, token, last_used, profile_id, username) FROM stdin;
\.


--
-- Name: profile_restore_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.profile_restore_id_seq', 1, false);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: profile_restore profile_restore_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_restore
    ADD CONSTRAINT profile_restore_pkey PRIMARY KEY (id);


--
-- Name: remember_me_token remember_me_token_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.remember_me_token
    ADD CONSTRAINT remember_me_token_pkey PRIMARY KEY (series);


--
-- Name: profile_restore uk_profile_restore_profile; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_restore
    ADD CONSTRAINT uk_profile_restore_profile UNIQUE (profile_id);


--
-- Name: profile_restore uk_profile_restore_token; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_restore
    ADD CONSTRAINT uk_profile_restore_token UNIQUE (token);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_remember_me_profile; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_remember_me_profile ON public.remember_me_token USING btree (profile_id);


--
-- Name: idx_remember_me_username; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_remember_me_username ON public.remember_me_token USING btree (username);


--
-- PostgreSQL database dump complete
--

