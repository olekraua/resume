-- Outbox table for profile indexing events

CREATE SEQUENCE public.profile_outbox_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.profile_outbox (
    id bigint DEFAULT nextval('public.profile_outbox_seq'::regclass) NOT NULL,
    profile_id bigint,
    event_type character varying(32) NOT NULL,
    payload text NOT NULL,
    status character varying(16) NOT NULL,
    attempts integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    available_at timestamp with time zone DEFAULT now() NOT NULL,
    sent_at timestamp with time zone,
    last_error text
);

ALTER TABLE ONLY public.profile_outbox
    ADD CONSTRAINT profile_outbox_pkey PRIMARY KEY (id);

CREATE INDEX profile_outbox_status_available_idx
    ON public.profile_outbox (status, available_at, id);
