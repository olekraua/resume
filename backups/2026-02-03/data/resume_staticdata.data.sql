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
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES (1, '1', 'staticdata schema', 'SQL', 'V1__staticdata_schema.sql', 1104756163, 'resume', '2026-02-01 23:45:20.53177', 16, true);


--
-- Data for Name: hobby; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.hobby (id, name) VALUES (1, 'Cycling');
INSERT INTO public.hobby (id, name) VALUES (2, 'Handball');
INSERT INTO public.hobby (id, name) VALUES (3, 'Football');
INSERT INTO public.hobby (id, name) VALUES (4, 'Basketball');
INSERT INTO public.hobby (id, name) VALUES (5, 'Bowling');
INSERT INTO public.hobby (id, name) VALUES (6, 'Boxing');
INSERT INTO public.hobby (id, name) VALUES (7, 'Volleyball');
INSERT INTO public.hobby (id, name) VALUES (8, 'Baseball');
INSERT INTO public.hobby (id, name) VALUES (9, 'Skating');
INSERT INTO public.hobby (id, name) VALUES (10, 'Skiing');
INSERT INTO public.hobby (id, name) VALUES (11, 'Table tennis');
INSERT INTO public.hobby (id, name) VALUES (12, 'Tennis');
INSERT INTO public.hobby (id, name) VALUES (13, 'Weightlifting');
INSERT INTO public.hobby (id, name) VALUES (14, 'Automobiles');
INSERT INTO public.hobby (id, name) VALUES (15, 'Book reading');
INSERT INTO public.hobby (id, name) VALUES (16, 'Cricket');
INSERT INTO public.hobby (id, name) VALUES (17, 'Photo');
INSERT INTO public.hobby (id, name) VALUES (18, 'Shopping');
INSERT INTO public.hobby (id, name) VALUES (19, 'Cooking');
INSERT INTO public.hobby (id, name) VALUES (20, 'Codding');
INSERT INTO public.hobby (id, name) VALUES (21, 'Animals');
INSERT INTO public.hobby (id, name) VALUES (22, 'Traveling');
INSERT INTO public.hobby (id, name) VALUES (23, 'Movie');
INSERT INTO public.hobby (id, name) VALUES (24, 'Painting');
INSERT INTO public.hobby (id, name) VALUES (25, 'Darts');
INSERT INTO public.hobby (id, name) VALUES (26, 'Fishing');
INSERT INTO public.hobby (id, name) VALUES (27, 'Kayak slalom');
INSERT INTO public.hobby (id, name) VALUES (28, 'Games of chance');
INSERT INTO public.hobby (id, name) VALUES (29, 'Ice hockey');
INSERT INTO public.hobby (id, name) VALUES (30, 'Roller skating');
INSERT INTO public.hobby (id, name) VALUES (31, 'Swimming');
INSERT INTO public.hobby (id, name) VALUES (32, 'Diving');
INSERT INTO public.hobby (id, name) VALUES (33, 'Golf');
INSERT INTO public.hobby (id, name) VALUES (34, 'Shooting');
INSERT INTO public.hobby (id, name) VALUES (35, 'Rowing');
INSERT INTO public.hobby (id, name) VALUES (36, 'Camping');
INSERT INTO public.hobby (id, name) VALUES (37, 'Archery');
INSERT INTO public.hobby (id, name) VALUES (38, 'Pubs');
INSERT INTO public.hobby (id, name) VALUES (39, 'Music');
INSERT INTO public.hobby (id, name) VALUES (40, 'Computer games');
INSERT INTO public.hobby (id, name) VALUES (41, 'Authorship');
INSERT INTO public.hobby (id, name) VALUES (42, 'Singing');
INSERT INTO public.hobby (id, name) VALUES (43, 'Foreign lang');
INSERT INTO public.hobby (id, name) VALUES (44, 'Billiards');
INSERT INTO public.hobby (id, name) VALUES (45, 'Skateboarding');
INSERT INTO public.hobby (id, name) VALUES (46, 'Collecting');
INSERT INTO public.hobby (id, name) VALUES (47, 'Badminton');
INSERT INTO public.hobby (id, name) VALUES (48, 'Disco');


--
-- Data for Name: skill_category; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.skill_category (id, category) VALUES (1, 'Languages');
INSERT INTO public.skill_category (id, category) VALUES (2, 'DBMS');
INSERT INTO public.skill_category (id, category) VALUES (3, 'Web');
INSERT INTO public.skill_category (id, category) VALUES (4, 'Java');
INSERT INTO public.skill_category (id, category) VALUES (5, 'IDE');
INSERT INTO public.skill_category (id, category) VALUES (6, 'CVS');
INSERT INTO public.skill_category (id, category) VALUES (7, 'Web Servers');
INSERT INTO public.skill_category (id, category) VALUES (8, 'Build system');
INSERT INTO public.skill_category (id, category) VALUES (9, 'Cloud');
INSERT INTO public.skill_category (id, category) VALUES (10, 'Frameworks');
INSERT INTO public.skill_category (id, category) VALUES (11, 'Tools');
INSERT INTO public.skill_category (id, category) VALUES (12, 'Testing');
INSERT INTO public.skill_category (id, category) VALUES (13, 'Other');


--
-- Name: hobby_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.hobby_id_seq', 1, false);


--
-- Name: skill_category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.skill_category_id_seq', 1, false);


--
-- PostgreSQL database dump complete
--

