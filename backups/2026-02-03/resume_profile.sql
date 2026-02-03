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
-- Name: certificate; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.certificate (
    id bigint NOT NULL,
    id_profile bigint NOT NULL,
    name character varying(50) NOT NULL,
    large_url character varying(255) NOT NULL,
    small_url character varying(255) NOT NULL,
    issuer character varying(50) NOT NULL
);


--
-- Name: certificate_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.certificate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: certificate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.certificate_id_seq OWNED BY public.certificate.id;


--
-- Name: course; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.course (
    id bigint NOT NULL,
    id_profile bigint NOT NULL,
    name character varying(60) NOT NULL,
    school character varying(60) NOT NULL,
    finish_date date
);


--
-- Name: course_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.course_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: course_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.course_id_seq OWNED BY public.course.id;


--
-- Name: education; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.education (
    id bigint NOT NULL,
    id_profile bigint NOT NULL,
    summary character varying(100) NOT NULL,
    begin_year integer NOT NULL,
    finish_year integer,
    university text NOT NULL,
    faculty character varying(255) NOT NULL
);


--
-- Name: education_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.education_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: education_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.education_id_seq OWNED BY public.education.id;


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
-- Name: language; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.language (
    id bigint NOT NULL,
    id_profile bigint NOT NULL,
    name character varying(30) NOT NULL,
    level character varying(18) NOT NULL,
    type character varying(7) DEFAULT 'all'::character varying NOT NULL,
    CONSTRAINT language_level_check CHECK (((level)::text = ANY ((ARRAY['beginner'::character varying, 'elementary'::character varying, 'pre_intermediate'::character varying, 'intermediate'::character varying, 'upper_intermediate'::character varying, 'advanced'::character varying, 'proficiency'::character varying])::text[]))),
    CONSTRAINT language_type_check CHECK (((type)::text = ANY ((ARRAY['all'::character varying, 'spoken'::character varying, 'writing'::character varying])::text[])))
);


--
-- Name: language_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.language_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: language_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.language_id_seq OWNED BY public.language.id;


--
-- Name: practic; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.practic (
    id bigint NOT NULL,
    id_profile bigint NOT NULL,
    company character varying(100) NOT NULL,
    demo character varying(255),
    src character varying(255),
    job_position character varying(100) NOT NULL,
    responsibilities text NOT NULL,
    begin_date date NOT NULL,
    finish_date date
);


--
-- Name: practic_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.practic_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: practic_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.practic_id_seq OWNED BY public.practic.id;


--
-- Name: profile; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.profile (
    id bigint NOT NULL,
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
    created timestamp without time zone DEFAULT now() NOT NULL,
    facebook character varying(255),
    linkedin character varying(255),
    github character varying(255),
    stackoverflow character varying(255),
    CONSTRAINT chk_profile_uid_format CHECK (((uid)::text ~ '^[a-z0-9_-]{3,64}$'::text)),
    CONSTRAINT chk_profile_uid_lowercase CHECK (((uid)::text = lower((uid)::text)))
);


--
-- Name: profile_connection; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.profile_connection (
    id bigint NOT NULL,
    pair_key character varying(64) NOT NULL,
    requester_id bigint NOT NULL,
    addressee_id bigint NOT NULL,
    status character varying(16) NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    responded timestamp with time zone,
    CONSTRAINT chk_profile_connection_self CHECK ((requester_id <> addressee_id))
);


--
-- Name: profile_connection_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.profile_connection_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: profile_connection_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.profile_connection_id_seq OWNED BY public.profile_connection.id;


--
-- Name: profile_hobby; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.profile_hobby (
    id_profile bigint NOT NULL,
    id_hobby bigint NOT NULL
);


--
-- Name: profile_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.profile_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: profile_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.profile_id_seq OWNED BY public.profile.id;


--
-- Name: skill; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.skill (
    id bigint NOT NULL,
    id_profile bigint NOT NULL,
    category character varying(50) NOT NULL,
    value text NOT NULL
);


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
-- Name: skill_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.skill_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: skill_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.skill_id_seq OWNED BY public.skill.id;


--
-- Name: certificate id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.certificate ALTER COLUMN id SET DEFAULT nextval('public.certificate_id_seq'::regclass);


--
-- Name: course id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.course ALTER COLUMN id SET DEFAULT nextval('public.course_id_seq'::regclass);


--
-- Name: education id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.education ALTER COLUMN id SET DEFAULT nextval('public.education_id_seq'::regclass);


--
-- Name: hobby id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hobby ALTER COLUMN id SET DEFAULT nextval('public.hobby_id_seq'::regclass);


--
-- Name: language id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.language ALTER COLUMN id SET DEFAULT nextval('public.language_id_seq'::regclass);


--
-- Name: practic id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.practic ALTER COLUMN id SET DEFAULT nextval('public.practic_id_seq'::regclass);


--
-- Name: profile id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile ALTER COLUMN id SET DEFAULT nextval('public.profile_id_seq'::regclass);


--
-- Name: profile_connection id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_connection ALTER COLUMN id SET DEFAULT nextval('public.profile_connection_id_seq'::regclass);


--
-- Name: skill id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skill ALTER COLUMN id SET DEFAULT nextval('public.skill_id_seq'::regclass);


--
-- Name: skill_category id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skill_category ALTER COLUMN id SET DEFAULT nextval('public.skill_category_id_seq'::regclass);


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
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	profile schema	SQL	V1__profile_schema.sql	688068912	resume	2026-02-01 23:45:12.225575	62	t
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
-- Data for Name: language; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.language (id, id_profile, name, level, type) FROM stdin;
\.


--
-- Data for Name: practic; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.practic (id, id_profile, company, demo, src, job_position, responsibilities, begin_date, finish_date) FROM stdin;
\.


--
-- Data for Name: profile; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.profile (id, uid, first_name, last_name, birth_day, phone, email, country, city, objective, summary, large_photo, small_photo, info, password, completed, created, facebook, linkedin, github, stackoverflow) FROM stdin;
\.


--
-- Data for Name: profile_connection; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.profile_connection (id, pair_key, requester_id, addressee_id, status, created, responded) FROM stdin;
\.


--
-- Data for Name: profile_hobby; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.profile_hobby (id_profile, id_hobby) FROM stdin;
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
-- Name: certificate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.certificate_id_seq', 1, false);


--
-- Name: course_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.course_id_seq', 1, false);


--
-- Name: education_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.education_id_seq', 1, false);


--
-- Name: hobby_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.hobby_id_seq', 1, false);


--
-- Name: language_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.language_id_seq', 1, false);


--
-- Name: practic_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.practic_id_seq', 1, false);


--
-- Name: profile_connection_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.profile_connection_id_seq', 1, false);


--
-- Name: profile_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.profile_id_seq', 1, false);


--
-- Name: skill_category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.skill_category_id_seq', 1, false);


--
-- Name: skill_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.skill_id_seq', 1, false);


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
-- Name: language language_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.language
    ADD CONSTRAINT language_pkey PRIMARY KEY (id);


--
-- Name: practic practic_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.practic
    ADD CONSTRAINT practic_pkey PRIMARY KEY (id);


--
-- Name: profile_connection profile_connection_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_connection
    ADD CONSTRAINT profile_connection_pkey PRIMARY KEY (id);


--
-- Name: profile profile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_pkey PRIMARY KEY (id);


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
-- Name: certificate_unique_profile_name_issuer_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX certificate_unique_profile_name_issuer_idx ON public.certificate USING btree (id_profile, lower(regexp_replace(TRIM(BOTH FROM name), '\\s+'::text, ' '::text, 'g'::text)), lower(regexp_replace(TRIM(BOTH FROM issuer), '\\s+'::text, ' '::text, 'g'::text)));


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: hobby_name_unique; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX hobby_name_unique ON public.hobby USING btree (name);


--
-- Name: idx_profile_connection_addressee_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_profile_connection_addressee_status ON public.profile_connection USING btree (addressee_id, status);


--
-- Name: idx_profile_connection_requester_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_profile_connection_requester_status ON public.profile_connection USING btree (requester_id, status);


--
-- Name: language_profile_name_type_key; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX language_profile_name_type_key ON public.language USING btree (id_profile, name, type);


--
-- Name: profile_email_key; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX profile_email_key ON public.profile USING btree (email);


--
-- Name: profile_phone_key; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX profile_phone_key ON public.profile USING btree (phone);


--
-- Name: profile_uid_key; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX profile_uid_key ON public.profile USING btree (uid);


--
-- Name: skill_category_category_key; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX skill_category_category_key ON public.skill_category USING btree (category);


--
-- Name: uk_profile_connection_pair; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_profile_connection_pair ON public.profile_connection USING btree (pair_key);


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
-- Name: profile_connection fk_profile_connection_addressee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_connection
    ADD CONSTRAINT fk_profile_connection_addressee FOREIGN KEY (addressee_id) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profile_connection fk_profile_connection_requester; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_connection
    ADD CONSTRAINT fk_profile_connection_requester FOREIGN KEY (requester_id) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: language language_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.language
    ADD CONSTRAINT language_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: practic practic_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.practic
    ADD CONSTRAINT practic_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profile_hobby profile_hobby_hobby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_hobby
    ADD CONSTRAINT profile_hobby_hobby_fk FOREIGN KEY (id_hobby) REFERENCES public.hobby(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profile_hobby profile_hobby_profile_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_hobby
    ADD CONSTRAINT profile_hobby_profile_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: skill skill_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skill
    ADD CONSTRAINT skill_fk FOREIGN KEY (id_profile) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

