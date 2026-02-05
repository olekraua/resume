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
-- Data for Name: auth_user; Type: TABLE DATA; Schema: public; Owner: resume
--

COPY public.auth_user (id, uid, password_hash, first_name, last_name, created, enabled) FROM stdin;
3	jane-smith	test123	Jane	Smith	2025-08-15 12:20:33	t
2	john	$2a$10$EAdRQKejWH/7HQMR78v6eumzRyyKVOGMhCvlFiJ2Z1R2ZWwJu6HOK	John	Doe	2025-08-15 12:20:33	t
5	testuser1	$2a$10$e8OXUA6QVuxjbzI1vaVccOPxGPm9/vTLLJ53DfhoRNhfGluHQp90q	Test	User	2026-02-01 09:14:19	t
6	larysa	$2a$10$fsm6y58pHBOUHWzoHcwZOeiQCEoSMKb9osKHXvdWNCIiQiQ2oK1jC	Larysa	Kravchenko	2026-02-01 09:43:49	t
4	olekraua	$2a$10$q5x.sw6Cvzkydkk7Tp/3jOlLX29pu17f7NnW1qJEjt5zF.fKETfJy	Олександр	Кравченко	2025-12-06 19:07:05	t
\.


--
-- Data for Name: profile_restore; Type: TABLE DATA; Schema: public; Owner: resume
--

COPY public.profile_restore (id, token, created, profile_id) FROM stdin;
\.


--
-- Data for Name: remember_me_token; Type: TABLE DATA; Schema: public; Owner: resume
--

COPY public.remember_me_token (series, token, last_used, profile_id, username) FROM stdin;
\.


--
-- Name: profile_restore_id_seq; Type: SEQUENCE SET; Schema: public; Owner: resume
--

SELECT pg_catalog.setval('public.profile_restore_id_seq', 11, true);


--
-- PostgreSQL database dump complete
--

