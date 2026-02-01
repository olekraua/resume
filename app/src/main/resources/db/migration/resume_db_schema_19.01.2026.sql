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
-- Data for Name: certificate; Type: TABLE DATA; Schema: public; Owner: resume
--

COPY public.certificate (id, id_profile, name, large_url, small_url, issuer) FROM stdin;
35	4	Mongo Certificate	/uploads/certificates/4f1ca458-8eef-41eb-abe5-81507fe82e21.jpg	/uploads/certificates/4f1ca458-8eef-41eb-abe5-81507fe82e21-sm.jpg	Mongo
\.


--
-- Data for Name: course; Type: TABLE DATA; Schema: public; Owner: resume
--

COPY public.course (id, id_profile, name, school, finish_date) FROM stdin;
\.


--
-- Data for Name: education; Type: TABLE DATA; Schema: public; Owner: resume
--

COPY public.education (id, id_profile, summary, begin_year, finish_year, university, faculty) FROM stdin;
\.


--
-- Data for Name: hobby; Type: TABLE DATA; Schema: public; Owner: resume
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
-- Data for Name: language; Type: TABLE DATA; Schema: public; Owner: resume
--

COPY public.language (id, id_profile, name, level, type) FROM stdin;
30	4	English	elementary	spoken
31	4	English	intermediate	writing
32	4	German	upper_intermediate	spoken
33	4	German	intermediate	writing
\.


--
-- Data for Name: persistent_logins; Type: TABLE DATA; Schema: public; Owner: resume
--

COPY public.persistent_logins (username, series, token, last_used) FROM stdin;
\.


--
-- Data for Name: practic; Type: TABLE DATA; Schema: public; Owner: resume
--

COPY public.practic (id, id_profile, job_position, company, begin_date, finish_date, responsibilities, demo, src) FROM stdin;
15	4	Java Developer	DevStyde	2024-01-01	2024-12-12	N/A	\N	https://github.com/olekraua/resume
\.


--
-- Data for Name: profile; Type: TABLE DATA; Schema: public; Owner: resume
--

COPY public.profile (id, uid, first_name, last_name, birth_day, phone, email, country, city, objective, summary, large_photo, small_photo, info, password, completed, created, facebook, linkedin, github, stackoverflow) FROM stdin;
3	jane-smith	Jane	Smith	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	test123	t	2025-08-15 12:20:33	\N	\N	\N	\N
2	john	John	Doe	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	$2a$10$EAdRQKejWH/7HQMR78v6eumzRyyKVOGMhCvlFiJ2Z1R2ZWwJu6HOK	t	2025-08-15 12:20:33	\N	\N	\N	\N
4	olekraua	Олександр	Кравченко	1993-05-23	+41765246710	ksanya7@gmail.com	Ukraine	Pyriatyn	Java розробник	Розробляю вдома веб систему "My Resume"	/uploads/photos/864af6ed-0b5f-4a43-973f-61143e4f8c43.jpg	/uploads/photos/864af6ed-0b5f-4a43-973f-61143e4f8c43-sm.jpg	\N	$2a$10$bbcYDAdsgbKUd8SNOhZIIen52yjxiJN7nc3COGLtBY0/iSzhN7jSO	t	2025-12-06 19:07:05	\N	\N	\N	\N
\.


--
-- Data for Name: profile_hobby; Type: TABLE DATA; Schema: public; Owner: resume
--

COPY public.profile_hobby (id_profile, id_hobby) FROM stdin;
4	15
4	17
4	19
4	20
4	39
\.


--
-- Data for Name: profile_restore; Type: TABLE DATA; Schema: public; Owner: resume
--

COPY public.profile_restore (id, token, created, profile_id) FROM stdin;
\.


--
-- Data for Name: skill; Type: TABLE DATA; Schema: public; Owner: resume
--

COPY public.skill (id, id_profile, category, value) FROM stdin;
4	4	Tools	Visual Studio Code, Docker
5	4	Build system	Maven
\.


--
-- Data for Name: skill_category; Type: TABLE DATA; Schema: public; Owner: resume
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
-- Name: certificate_seq; Type: SEQUENCE SET; Schema: public; Owner: resume
--

SELECT pg_catalog.setval('public.certificate_seq', 43, true);


--
-- Name: course_seq; Type: SEQUENCE SET; Schema: public; Owner: resume
--

SELECT pg_catalog.setval('public.course_seq', 1, false);


--
-- Name: education_seq; Type: SEQUENCE SET; Schema: public; Owner: resume
--

SELECT pg_catalog.setval('public.education_seq', 1, true);


--
-- Name: hobby_seq; Type: SEQUENCE SET; Schema: public; Owner: resume
--

SELECT pg_catalog.setval('public.hobby_seq', 48, true);


--
-- Name: language_seq; Type: SEQUENCE SET; Schema: public; Owner: resume
--

SELECT pg_catalog.setval('public.language_seq', 33, true);


--
-- Name: practic_seq; Type: SEQUENCE SET; Schema: public; Owner: resume
--

SELECT pg_catalog.setval('public.practic_seq', 15, true);


--
-- Name: profile_restore_id_seq; Type: SEQUENCE SET; Schema: public; Owner: resume
--

SELECT pg_catalog.setval('public.profile_restore_id_seq', 9, true);


--
-- Name: profile_seq; Type: SEQUENCE SET; Schema: public; Owner: resume
--

SELECT pg_catalog.setval('public.profile_seq', 4, true);


--
-- Name: skill_category_seq; Type: SEQUENCE SET; Schema: public; Owner: resume
--

SELECT pg_catalog.setval('public.skill_category_seq', 13, true);


--
-- Name: skill_seq; Type: SEQUENCE SET; Schema: public; Owner: resume
--

SELECT pg_catalog.setval('public.skill_seq', 5, true);


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
-- Name: certificate_unique_profile_name_issuer_idx; Type: INDEX; Schema: public; Owner: resume
--

CREATE UNIQUE INDEX certificate_unique_profile_name_issuer_idx ON public.certificate USING btree (id_profile, lower(regexp_replace(TRIM(BOTH FROM name), '\s+'::text, ' '::text, 'g'::text)), lower(regexp_replace(TRIM(BOTH FROM issuer), '\s+'::text, ' '::text, 'g'::text)));


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
-- Name: profile_connection; Type: TABLE; Schema: public; Owner: resume
--

CREATE TABLE public.profile_connection (
    id bigserial NOT NULL,
    pair_key character varying(64) NOT NULL,
    requester_id bigint NOT NULL,
    addressee_id bigint NOT NULL,
    status character varying(16) NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    responded timestamp with time zone,
    CONSTRAINT profile_connection_pkey PRIMARY KEY (id),
    CONSTRAINT chk_profile_connection_self CHECK ((requester_id <> addressee_id))
);


ALTER TABLE public.profile_connection OWNER TO resume;

--
-- Name: profile_connection uk_profile_connection_pair; Type: CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.profile_connection
    ADD CONSTRAINT uk_profile_connection_pair UNIQUE (pair_key);

--
-- Name: idx_profile_connection_addressee_status; Type: INDEX; Schema: public; Owner: resume
--

CREATE INDEX idx_profile_connection_addressee_status ON public.profile_connection USING btree (addressee_id, status);

--
-- Name: idx_profile_connection_requester_status; Type: INDEX; Schema: public; Owner: resume
--

CREATE INDEX idx_profile_connection_requester_status ON public.profile_connection USING btree (requester_id, status);

--
-- Name: profile_connection fk_profile_connection_addressee; Type: FK CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.profile_connection
    ADD CONSTRAINT fk_profile_connection_addressee FOREIGN KEY (addressee_id) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;

--
-- Name: profile_connection fk_profile_connection_requester; Type: FK CONSTRAINT; Schema: public; Owner: resume
--

ALTER TABLE ONLY public.profile_connection
    ADD CONSTRAINT fk_profile_connection_requester FOREIGN KEY (requester_id) REFERENCES public.profile(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: pg_database_owner
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO resume;
GRANT ALL ON SCHEMA public TO postgres;


--
-- PostgreSQL database dump complete
--
