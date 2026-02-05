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
INSERT INTO public.hobby (id, name)
SELECT id, name FROM tmp_hobby
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;
DROP TABLE tmp_hobby;


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
INSERT INTO public.skill_category (id, category)
SELECT id, category FROM tmp_skill_category
ON CONFLICT (id) DO UPDATE SET category = EXCLUDED.category;
DROP TABLE tmp_skill_category;


--
-- PostgreSQL database dump complete
--
