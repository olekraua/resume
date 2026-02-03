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
-- Name: hobby; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.hobby (
    id bigint NOT NULL,
    name character varying(30) NOT NULL
);


--
-- Name: hobby_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.hobby_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: hobby_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.hobby_id_seq OWNED BY public.hobby.id;


--
-- Name: skill_category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.skill_category (
    id bigint NOT NULL,
    category character varying(50) NOT NULL
);


--
-- Name: skill_category_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.skill_category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: skill_category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.skill_category_id_seq OWNED BY public.skill_category.id;


--
-- Name: hobby id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hobby ALTER COLUMN id SET DEFAULT nextval('public.hobby_id_seq'::regclass);


--
-- Name: skill_category id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skill_category ALTER COLUMN id SET DEFAULT nextval('public.skill_category_id_seq'::regclass);


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	staticdata schema	SQL	V1__staticdata_schema.sql	1104756163	resume	2026-02-01 23:45:20.53177	16	t
\.


--
-- Data for Name: hobby; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.hobby (id, name) FROM stdin;
1	Cycling
2	Handball
3	Football
4	Basketball
5	Bowling
6	Boxing
7	Volleyball
8	Baseball
9	Skating
10	Skiing
11	Table tennis
12	Tennis
13	Weightlifting
14	Automobiles
15	Book reading
16	Cricket
17	Photo
18	Shopping
19	Cooking
20	Codding
21	Animals
22	Traveling
23	Movie
24	Painting
25	Darts
26	Fishing
27	Kayak slalom
28	Games of chance
29	Ice hockey
30	Roller skating
31	Swimming
32	Diving
33	Golf
34	Shooting
35	Rowing
36	Camping
37	Archery
38	Pubs
39	Music
40	Computer games
41	Authorship
42	Singing
43	Foreign lang
44	Billiards
45	Skateboarding
46	Collecting
47	Badminton
48	Disco
\.


--
-- Data for Name: skill_category; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.skill_category (id, category) FROM stdin;
1	Languages
2	DBMS
3	Web
4	Java
5	IDE
6	CVS
7	Web Servers
8	Build system
9	Cloud
10	Frameworks
11	Tools
12	Testing
13	Other
\.


--
-- Name: hobby_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.hobby_id_seq', 1, false);


--
-- Name: skill_category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.skill_category_id_seq', 1, false);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: hobby hobby_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hobby
    ADD CONSTRAINT hobby_pkey PRIMARY KEY (id);


--
-- Name: skill_category skill_category_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skill_category
    ADD CONSTRAINT skill_category_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: hobby_name_unique; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX hobby_name_unique ON public.hobby USING btree (name);


--
-- Name: skill_category_category_key; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX skill_category_category_key ON public.skill_category USING btree (category);


--
-- PostgreSQL database dump complete
--

