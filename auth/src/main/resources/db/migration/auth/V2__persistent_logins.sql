CREATE TABLE IF NOT EXISTS persistent_logins (
    username character varying(64) NOT NULL,
    series character varying(64) NOT NULL,
    token character varying(64) NOT NULL,
    last_used timestamp without time zone NOT NULL
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'persistent_logins_pkey'
          AND conrelid = 'persistent_logins'::regclass
    ) THEN
        ALTER TABLE ONLY persistent_logins
            ADD CONSTRAINT persistent_logins_pkey PRIMARY KEY (username, series);
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS profile_restore (
    id bigserial NOT NULL,
    token character varying(64) NOT NULL,
    created timestamp with time zone NOT NULL DEFAULT now(),
    profile_id bigint NOT NULL
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'profile_restore_pkey'
          AND conrelid = 'profile_restore'::regclass
    ) THEN
        ALTER TABLE ONLY profile_restore
            ADD CONSTRAINT profile_restore_pkey PRIMARY KEY (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_profile_restore_token'
          AND conrelid = 'profile_restore'::regclass
    ) THEN
        ALTER TABLE ONLY profile_restore
            ADD CONSTRAINT uk_profile_restore_token UNIQUE (token);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_profile_restore_profile'
          AND conrelid = 'profile_restore'::regclass
    ) THEN
        ALTER TABLE ONLY profile_restore
            ADD CONSTRAINT uk_profile_restore_profile UNIQUE (profile_id);
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS remember_me_token (
    series character varying(64) NOT NULL,
    token character varying(64) NOT NULL,
    last_used timestamp with time zone NOT NULL,
    profile_id bigint NOT NULL,
    username character varying(64) NOT NULL
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'remember_me_token_pkey'
          AND conrelid = 'remember_me_token'::regclass
    ) THEN
        ALTER TABLE ONLY remember_me_token
            ADD CONSTRAINT remember_me_token_pkey PRIMARY KEY (series);
    END IF;
END$$;

CREATE INDEX IF NOT EXISTS idx_remember_me_profile ON remember_me_token(profile_id);
CREATE INDEX IF NOT EXISTS idx_remember_me_username ON remember_me_token(username);
