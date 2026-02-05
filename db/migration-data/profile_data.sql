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
-- Data for Name: profile; Type: TABLE DATA; Schema: public; Owner: resume
--

DROP TABLE IF EXISTS tmp_profile;
CREATE TEMP TABLE tmp_profile (LIKE public.profile EXCLUDING CONSTRAINTS);
COPY tmp_profile (id, uid, first_name, last_name, birth_day, phone, email, country, city, objective, summary, large_photo, small_photo, info, completed, created, facebook, linkedin, github, stackoverflow, connections_visible) FROM stdin;
3	jane-smith	Jane	Smith	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2025-08-15 12:20:33	\N	\N	\N	\N	t
2	john	John	Doe	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2025-08-15 12:20:33	\N	\N	\N	\N	t
5	testuser1	Test	User	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f	2026-02-01 09:14:19	\N	\N	\N	\N	t
6	larysa	Larysa	Kravchenko	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f	2026-02-01 09:43:49	\N	\N	\N	\N	t
4	olekraua	Олександр	Кравченко	1993-05-23	+41765246710	ksanya7@gmail.com	Ukraine	Pyriatyn	Java Applikationsentwickler	Akutell beschäftige ich mit einer App-Entwicklung.	/uploads/photos/e6710c16-4bc0-4d6f-be4b-87fd0cd2b22a.jpg	/uploads/photos/e6710c16-4bc0-4d6f-be4b-87fd0cd2b22a-sm.jpg	Akutell beschäftige ich mit einer App-Entwicklung.	t	2025-12-06 19:07:05	https://www.facebook.com/olekraua	https://www.linkedin.com/in/olekra/	\N	\N	t
\.
INSERT INTO public.profile (id, uid, first_name, last_name, birth_day, phone, email, country, city, objective, summary, large_photo, small_photo, info, completed, created, facebook, linkedin, github, stackoverflow, connections_visible) SELECT id, uid, first_name, last_name, birth_day, phone, email, country, city, objective, summary, large_photo, small_photo, info, completed, created, facebook, linkedin, github, stackoverflow, connections_visible FROM tmp_profile ON CONFLICT (id) DO UPDATE SET uid = EXCLUDED.uid, first_name = EXCLUDED.first_name, last_name = EXCLUDED.last_name, birth_day = EXCLUDED.birth_day, phone = EXCLUDED.phone, email = EXCLUDED.email, country = EXCLUDED.country, city = EXCLUDED.city, objective = EXCLUDED.objective, summary = EXCLUDED.summary, large_photo = EXCLUDED.large_photo, small_photo = EXCLUDED.small_photo, info = EXCLUDED.info, completed = EXCLUDED.completed, created = EXCLUDED.created, facebook = EXCLUDED.facebook, linkedin = EXCLUDED.linkedin, github = EXCLUDED.github, stackoverflow = EXCLUDED.stackoverflow, connections_visible = EXCLUDED.connections_visible;
DROP TABLE tmp_profile;


--
-- Data for Name: certificate; Type: TABLE DATA; Schema: public; Owner: resume
--

DROP TABLE IF EXISTS tmp_certificate;
CREATE TEMP TABLE tmp_certificate (LIKE public.certificate EXCLUDING CONSTRAINTS);
COPY tmp_certificate (id, id_profile, name, large_url, small_url, issuer) FROM stdin;
\.
INSERT INTO public.certificate (id, id_profile, name, large_url, small_url, issuer) SELECT id, id_profile, name, large_url, small_url, issuer FROM tmp_certificate ON CONFLICT (id) DO UPDATE SET id_profile = EXCLUDED.id_profile, name = EXCLUDED.name, large_url = EXCLUDED.large_url, small_url = EXCLUDED.small_url, issuer = EXCLUDED.issuer;
DROP TABLE tmp_certificate;


--
-- Data for Name: course; Type: TABLE DATA; Schema: public; Owner: resume
--

DROP TABLE IF EXISTS tmp_course;
CREATE TEMP TABLE tmp_course (LIKE public.course EXCLUDING CONSTRAINTS);
COPY tmp_course (id, id_profile, name, school, finish_date) FROM stdin;
\.
INSERT INTO public.course (id, id_profile, name, school, finish_date) SELECT id, id_profile, name, school, finish_date FROM tmp_course ON CONFLICT (id) DO UPDATE SET id_profile = EXCLUDED.id_profile, name = EXCLUDED.name, school = EXCLUDED.school, finish_date = EXCLUDED.finish_date;
DROP TABLE tmp_course;


--
-- Data for Name: education; Type: TABLE DATA; Schema: public; Owner: resume
--

DROP TABLE IF EXISTS tmp_education;
CREATE TEMP TABLE tmp_education (LIKE public.education EXCLUDING CONSTRAINTS);
COPY tmp_education (id, id_profile, summary, begin_year, finish_year, university, faculty) FROM stdin;
2	4	Juniorspezialist Fachausbildung „Buchhaltung“ Zweigstelle Europäische Universität, Pyriatyn	2009	2011	Europäische Universität	Wirtschaftswissenschaften und Management
\.
INSERT INTO public.education (id, id_profile, summary, begin_year, finish_year, university, faculty) SELECT id, id_profile, summary, begin_year, finish_year, university, faculty FROM tmp_education ON CONFLICT (id) DO UPDATE SET id_profile = EXCLUDED.id_profile, summary = EXCLUDED.summary, begin_year = EXCLUDED.begin_year, finish_year = EXCLUDED.finish_year, university = EXCLUDED.university, faculty = EXCLUDED.faculty;
DROP TABLE tmp_education;


--
-- Data for Name: hobby; Type: TABLE DATA; Schema: public; Owner: resume
--

DROP TABLE IF EXISTS tmp_hobby;
CREATE TEMP TABLE tmp_hobby (LIKE public.hobby EXCLUDING CONSTRAINTS);
COPY tmp_hobby (id, name) FROM stdin;
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
INSERT INTO public.hobby (id, name) SELECT id, name FROM tmp_hobby ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;
DROP TABLE tmp_hobby;


--
-- Data for Name: language; Type: TABLE DATA; Schema: public; Owner: resume
--

DROP TABLE IF EXISTS tmp_language;
CREATE TEMP TABLE tmp_language (LIKE public.language EXCLUDING CONSTRAINTS);
COPY tmp_language (id, id_profile, name, level, type) FROM stdin;
30	4	English	elementary	spoken
32	4	German	upper_intermediate	spoken
31	4	English	elementary	writing
33	4	German	upper_intermediate	writing
\.
INSERT INTO public.language (id, id_profile, name, level, type) SELECT id, id_profile, name, level, type FROM tmp_language ON CONFLICT (id) DO UPDATE SET id_profile = EXCLUDED.id_profile, name = EXCLUDED.name, level = EXCLUDED.level, type = EXCLUDED.type;
DROP TABLE tmp_language;


--
-- Data for Name: practic; Type: TABLE DATA; Schema: public; Owner: resume
--

DROP TABLE IF EXISTS tmp_practic;
CREATE TEMP TABLE tmp_practic (LIKE public.practic EXCLUDING CONSTRAINTS);
COPY tmp_practic (id, id_profile, job_position, company, begin_date, finish_date, responsibilities, demo, src) FROM stdin;
15	4	Java Developer	DevStyde	2024-01-01	2024-12-12	N/A	\N	https://github.com/olekraua/resume
\.
INSERT INTO public.practic (id, id_profile, job_position, company, begin_date, finish_date, responsibilities, demo, src) SELECT id, id_profile, job_position, company, begin_date, finish_date, responsibilities, demo, src FROM tmp_practic ON CONFLICT (id) DO UPDATE SET id_profile = EXCLUDED.id_profile, job_position = EXCLUDED.job_position, company = EXCLUDED.company, begin_date = EXCLUDED.begin_date, finish_date = EXCLUDED.finish_date, responsibilities = EXCLUDED.responsibilities, demo = EXCLUDED.demo, src = EXCLUDED.src;
DROP TABLE tmp_practic;


--
-- Data for Name: profile_connection; Type: TABLE DATA; Schema: public; Owner: resume
--

DROP TABLE IF EXISTS tmp_profile_connection;
CREATE TEMP TABLE tmp_profile_connection (LIKE public.profile_connection EXCLUDING CONSTRAINTS);
COPY tmp_profile_connection (id, pair_key, requester_id, addressee_id, status, created, responded) FROM stdin;
1	4:6	4	6	accepted	2026-02-01 10:06:01.729205+01	2026-02-01 10:28:22.252546+01
\.
INSERT INTO public.profile_connection (id, pair_key, requester_id, addressee_id, status, created, responded) SELECT id, pair_key, requester_id, addressee_id, status, created, responded FROM tmp_profile_connection ON CONFLICT (id) DO UPDATE SET pair_key = EXCLUDED.pair_key, requester_id = EXCLUDED.requester_id, addressee_id = EXCLUDED.addressee_id, status = EXCLUDED.status, created = EXCLUDED.created, responded = EXCLUDED.responded;
DROP TABLE tmp_profile_connection;


--
-- Data for Name: profile_hobby; Type: TABLE DATA; Schema: public; Owner: resume
--

DROP TABLE IF EXISTS tmp_profile_hobby;
CREATE TEMP TABLE tmp_profile_hobby (LIKE public.profile_hobby EXCLUDING CONSTRAINTS);
COPY tmp_profile_hobby (id_profile, id_hobby) FROM stdin;
4	15
4	17
4	19
4	20
4	39
\.
DELETE FROM public.profile_hobby ph
USING tmp_profile_hobby t
WHERE ph.id_profile = t.id_profile
  AND ph.id_hobby = t.id_hobby;
INSERT INTO public.profile_hobby (id_profile, id_hobby)
SELECT id_profile, id_hobby FROM tmp_profile_hobby;
DROP TABLE tmp_profile_hobby;


--
-- Data for Name: skill; Type: TABLE DATA; Schema: public; Owner: resume
--

DROP TABLE IF EXISTS tmp_skill;
CREATE TEMP TABLE tmp_skill (LIKE public.skill EXCLUDING CONSTRAINTS);
COPY tmp_skill (id, id_profile, category, value) FROM stdin;
16	4	Tools	Visual Studio Code, Docker
17	4	Build system	Maven
18	4	Frameworks	Spring
19	4	Languages	Java, Typescript
20	4	Testing	JUnit
\.
INSERT INTO public.skill (id, id_profile, category, value) SELECT id, id_profile, category, value FROM tmp_skill ON CONFLICT (id) DO UPDATE SET id_profile = EXCLUDED.id_profile, category = EXCLUDED.category, value = EXCLUDED.value;
DROP TABLE tmp_skill;


--
-- Data for Name: skill_category; Type: TABLE DATA; Schema: public; Owner: resume
--

DROP TABLE IF EXISTS tmp_skill_category;
CREATE TEMP TABLE tmp_skill_category (LIKE public.skill_category EXCLUDING CONSTRAINTS);
COPY tmp_skill_category (id, category) FROM stdin;
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
INSERT INTO public.skill_category (id, category) SELECT id, category FROM tmp_skill_category ON CONFLICT (id) DO UPDATE SET category = EXCLUDED.category;
DROP TABLE tmp_skill_category;


--
-- Name: profile_connection_id_seq; Type: SEQUENCE SET; Schema: public; Owner: resume
--

SELECT pg_catalog.setval(
  'public.profile_connection_id_seq',
  (SELECT COALESCE(MAX(id), 1) FROM public.profile_connection),
  true
);


--
-- PostgreSQL database dump complete
--
