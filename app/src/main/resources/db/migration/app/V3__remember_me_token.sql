CREATE TABLE IF NOT EXISTS remember_me_token (
    series varchar(64) NOT NULL,
    token varchar(64) NOT NULL,
    last_used timestamptz NOT NULL,
    profile_id bigint NOT NULL,
    PRIMARY KEY (series)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_remember_me_profile'
          AND conrelid = 'remember_me_token'::regclass
    ) THEN
        ALTER TABLE remember_me_token
            ADD CONSTRAINT fk_remember_me_profile
            FOREIGN KEY (profile_id) REFERENCES profile(id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_remember_me_profile ON remember_me_token(profile_id);
