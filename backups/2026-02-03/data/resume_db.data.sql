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
-- Data for Name: profile; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.profile (id, uid, first_name, last_name, birth_day, phone, email, country, city, objective, summary, large_photo, small_photo, info, password, completed, created, facebook, linkedin, github, stackoverflow) VALUES (3, 'jane-smith', 'Jane', 'Smith', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'test123', true, '2025-08-15 12:20:33', NULL, NULL, NULL, NULL);
INSERT INTO public.profile (id, uid, first_name, last_name, birth_day, phone, email, country, city, objective, summary, large_photo, small_photo, info, password, completed, created, facebook, linkedin, github, stackoverflow) VALUES (2, 'john', 'John', 'Doe', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '$2a$10$EAdRQKejWH/7HQMR78v6eumzRyyKVOGMhCvlFiJ2Z1R2ZWwJu6HOK', true, '2025-08-15 12:20:33', NULL, NULL, NULL, NULL);
INSERT INTO public.profile (id, uid, first_name, last_name, birth_day, phone, email, country, city, objective, summary, large_photo, small_photo, info, password, completed, created, facebook, linkedin, github, stackoverflow) VALUES (4, 'olekraua', 'Олександр', 'Кравченко', '1993-05-23', '+41765246710', 'ksanya7@gmail.com', 'Ukraine', 'Pyriatyn', 'Java Applikationsentwickler', 'Akutell beschäftige ich mit einer App-Entwicklung.', '/uploads/photos/e6710c16-4bc0-4d6f-be4b-87fd0cd2b22a.jpg', '/uploads/photos/e6710c16-4bc0-4d6f-be4b-87fd0cd2b22a-sm.jpg', 'Akutell beschäftige ich mit einer App-Entwicklung.', '$2a$10$khQsZor/sLv4EAQyjNo6g.qmIGeJ8jR/DSO3nk.YOYzbCYuTlWK5e', true, '2025-12-06 19:07:05', NULL, 'https://www.linkedin.com/in/olekra/', NULL, NULL);
INSERT INTO public.profile (id, uid, first_name, last_name, birth_day, phone, email, country, city, objective, summary, large_photo, small_photo, info, password, completed, created, facebook, linkedin, github, stackoverflow) VALUES (5, 'testuser1', 'Test', 'User', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '$2a$10$e8OXUA6QVuxjbzI1vaVccOPxGPm9/vTLLJ53DfhoRNhfGluHQp90q', false, '2026-02-01 09:14:19', NULL, NULL, NULL, NULL);
INSERT INTO public.profile (id, uid, first_name, last_name, birth_day, phone, email, country, city, objective, summary, large_photo, small_photo, info, password, completed, created, facebook, linkedin, github, stackoverflow) VALUES (6, 'larysa', 'Larysa', 'Kravchenko', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '$2a$10$fsm6y58pHBOUHWzoHcwZOeiQCEoSMKb9osKHXvdWNCIiQiQ2oK1jC', false, '2026-02-01 09:43:49', NULL, NULL, NULL, NULL);


--
-- Data for Name: certificate; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: course; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: education; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.education (id, id_profile, summary, begin_year, finish_year, university, faculty) VALUES (2, 4, 'Juniorspezialist Fachausbildung „Buchhaltung“ Zweigstelle Europäische Universität, Pyriatyn', 2009, 2011, 'Europäische Universität', 'Wirtschaftswissenschaften und Management');


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES (1, '1', '<< Flyway Baseline >>', 'BASELINE', '<< Flyway Baseline >>', NULL, 'resume', '2026-02-02 00:24:47.45231', 0, true);
INSERT INTO public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES (2, '2', 'language unique by profile name type', 'SQL', 'V2__language_unique_by_profile_name_type.sql', 1516382108, 'resume', '2026-02-02 00:24:47.508636', 66, true);
INSERT INTO public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES (3, '3', 'remember me token', 'SQL', 'V3__remember_me_token.sql', 1931219700, 'resume', '2026-02-02 00:24:47.591669', 2, true);
INSERT INTO public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES (4, '4', 'profile connections', 'SQL', 'V4__profile_connections.sql', 1009914261, 'resume', '2026-02-02 00:24:47.602101', 5, true);
INSERT INTO public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES (5, '5', 'remember me username', 'SQL', 'V5__remember_me_username.sql', -1502650721, 'resume', '2026-02-02 00:24:47.614298', 7, true);


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
-- Data for Name: language; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.language (id, id_profile, name, level, type) VALUES (30, 4, 'English', 'elementary', 'spoken');
INSERT INTO public.language (id, id_profile, name, level, type) VALUES (32, 4, 'German', 'upper_intermediate', 'spoken');
INSERT INTO public.language (id, id_profile, name, level, type) VALUES (31, 4, 'English', 'elementary', 'writing');
INSERT INTO public.language (id, id_profile, name, level, type) VALUES (33, 4, 'German', 'upper_intermediate', 'writing');


--
-- Data for Name: persistent_logins; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: practic; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.practic (id, id_profile, job_position, company, begin_date, finish_date, responsibilities, demo, src) VALUES (15, 4, 'Java Developer', 'DevStyde', '2024-01-01', '2024-12-12', 'N/A', NULL, 'https://github.com/olekraua/resume');


--
-- Data for Name: profile_connection; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.profile_connection (id, pair_key, requester_id, addressee_id, status, created, responded) VALUES (1, '4:6', 4, 6, 'accepted', '2026-02-01 10:06:01.729205+01', '2026-02-01 10:28:22.252546+01');


--
-- Data for Name: profile_hobby; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.profile_hobby (id_profile, id_hobby) VALUES (4, 15);
INSERT INTO public.profile_hobby (id_profile, id_hobby) VALUES (4, 17);
INSERT INTO public.profile_hobby (id_profile, id_hobby) VALUES (4, 19);
INSERT INTO public.profile_hobby (id_profile, id_hobby) VALUES (4, 20);
INSERT INTO public.profile_hobby (id_profile, id_hobby) VALUES (4, 39);


--
-- Data for Name: profile_restore; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: remember_me_token; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: skill; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.skill (id, id_profile, category, value) VALUES (16, 4, 'Tools', 'Visual Studio Code, Docker');
INSERT INTO public.skill (id, id_profile, category, value) VALUES (17, 4, 'Build system', 'Maven');
INSERT INTO public.skill (id, id_profile, category, value) VALUES (18, 4, 'Frameworks', 'Spring');
INSERT INTO public.skill (id, id_profile, category, value) VALUES (19, 4, 'Languages', 'Java, Typescript');
INSERT INTO public.skill (id, id_profile, category, value) VALUES (20, 4, 'Testing', 'JUnit');


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
-- Name: certificate_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.certificate_seq', 43, true);


--
-- Name: course_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.course_seq', 1, false);


--
-- Name: education_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.education_seq', 2, true);


--
-- Name: hobby_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.hobby_seq', 48, true);


--
-- Name: language_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.language_seq', 33, true);


--
-- Name: practic_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.practic_seq', 15, true);


--
-- Name: profile_connection_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.profile_connection_id_seq', 1, true);


--
-- Name: profile_restore_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.profile_restore_id_seq', 11, true);


--
-- Name: profile_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.profile_seq', 6, true);


--
-- Name: skill_category_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.skill_category_seq', 13, true);


--
-- Name: skill_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.skill_seq', 20, true);


--
-- PostgreSQL database dump complete
--

