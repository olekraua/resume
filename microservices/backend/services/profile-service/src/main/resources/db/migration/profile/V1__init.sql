-- Profile service DB schema (extracted from resume_db_schema_2026-02-05.sql)

--
-- Name: certificate_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.certificate_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

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
    type character varying(7) DEFAULT 'all'::character varying NOT NULL,
    CONSTRAINT language_level_check CHECK (((level)::text = ANY ((ARRAY['beginner'::character varying, 'elementary'::character varying, 'pre_intermediate'::character varying, 'intermediate'::character varying, 'upper_intermediate'::character varying, 'advanced'::character varying, 'proficiency'::character varying])::text[]))),
    CONSTRAINT language_type_check CHECK (((type)::text = ANY ((ARRAY['all'::character varying, 'spoken'::character varying, 'writing'::character varying])::text[])))
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
    completed boolean NOT NULL,
    created timestamp(0) without time zone DEFAULT now() NOT NULL,
    facebook character varying(255),
    linkedin character varying(255),
    github character varying(255),
    stackoverflow character varying(255),
    connections_visible boolean DEFAULT true NOT NULL,
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
-- Name: profile_connection id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_connection ALTER COLUMN id SET DEFAULT nextval('public.profile_connection_id_seq'::regclass);

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
-- Name: hobby hobby_name_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hobby
    ADD CONSTRAINT hobby_name_unique UNIQUE (name);

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
-- Name: language language_profile_name_type_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.language
    ADD CONSTRAINT language_profile_name_type_key UNIQUE (id_profile, name, type);

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
-- Name: profile profile_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_email_key UNIQUE (email);

--
-- Name: profile_hobby profile_hobby_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_hobby
    ADD CONSTRAINT profile_hobby_pkey PRIMARY KEY (id_profile, id_hobby);

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
-- Name: certificate_unique_profile_name_issuer_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX certificate_unique_profile_name_issuer_idx ON public.certificate USING btree (id_profile, lower(regexp_replace(TRIM(BOTH FROM name), '\s+'::text, ' '::text, 'g'::text)), lower(regexp_replace(TRIM(BOTH FROM issuer), '\s+'::text, ' '::text, 'g'::text)));

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
-- Name: idx_profile_connection_addressee_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_profile_connection_addressee_status ON public.profile_connection USING btree (addressee_id, status);

--
-- Name: idx_profile_connection_requester_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_profile_connection_requester_status ON public.profile_connection USING btree (requester_id, status);

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
-- Name: profile_hobby_hobby_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX profile_hobby_hobby_idx ON public.profile_hobby USING btree (id_hobby);

--
-- Name: profile_hobby_profile_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX profile_hobby_profile_idx ON public.profile_hobby USING btree (id_profile);

--
-- Name: skill_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX skill_idx ON public.skill USING btree (id_profile);

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
    ADD CONSTRAINT fk_profile_connection_addressee FOREIGN KEY (addressee_id) REFERENCES public.profile(id) ON DELETE CASCADE;

--
-- Name: profile_connection fk_profile_connection_requester; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile_connection
    ADD CONSTRAINT fk_profile_connection_requester FOREIGN KEY (requester_id) REFERENCES public.profile(id) ON DELETE CASCADE;

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
