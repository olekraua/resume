-- Create auth_user table and backfill from profile if possible (monolith DB)

CREATE TABLE IF NOT EXISTS auth_user (
    id bigint NOT NULL,
    uid character varying(64) NOT NULL,
    password_hash character varying(255) NOT NULL,
    first_name character varying(64) NOT NULL,
    last_name character varying(64) NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL,
    enabled boolean DEFAULT true NOT NULL
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'auth_user_pkey'
          AND conrelid = 'auth_user'::regclass
    ) THEN
        ALTER TABLE auth_user
            ADD CONSTRAINT auth_user_pkey PRIMARY KEY (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'auth_user_uid_key'
          AND conrelid = 'auth_user'::regclass
    ) THEN
        ALTER TABLE auth_user
            ADD CONSTRAINT auth_user_uid_key UNIQUE (uid);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_auth_user_uid_format'
          AND conrelid = 'auth_user'::regclass
    ) THEN
        ALTER TABLE auth_user
            ADD CONSTRAINT chk_auth_user_uid_format CHECK ((uid)::text ~ '^[a-z0-9_-]{3,64}$'::text);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_auth_user_uid_lowercase'
          AND conrelid = 'auth_user'::regclass
    ) THEN
        ALTER TABLE auth_user
            ADD CONSTRAINT chk_auth_user_uid_lowercase CHECK ((uid)::text = lower((uid)::text));
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'profile'
          AND column_name = 'password'
    ) THEN
        INSERT INTO auth_user (id, uid, password_hash, first_name, last_name, created, enabled)
        SELECT p.id,
               p.uid,
               p.password,
               p.first_name,
               p.last_name,
               COALESCE(p.created, now()),
               true
        FROM profile p
        WHERE p.id IS NOT NULL
        ON CONFLICT DO NOTHING;
    END IF;
END $$;
