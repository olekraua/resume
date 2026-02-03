CREATE TABLE IF NOT EXISTS profile_connection (
    id bigserial PRIMARY KEY,
    pair_key varchar(64) NOT NULL,
    requester_id bigint NOT NULL,
    addressee_id bigint NOT NULL,
    status varchar(16) NOT NULL,
    created timestamptz NOT NULL DEFAULT now(),
    responded timestamptz NULL,
    CONSTRAINT chk_profile_connection_self CHECK (requester_id <> addressee_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_profile_connection_pair ON profile_connection(pair_key);
CREATE INDEX IF NOT EXISTS idx_profile_connection_requester_status
    ON profile_connection(requester_id, status);
CREATE INDEX IF NOT EXISTS idx_profile_connection_addressee_status
    ON profile_connection(addressee_id, status);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_profile_connection_requester'
          AND conrelid = 'profile_connection'::regclass
    ) THEN
        ALTER TABLE profile_connection
            ADD CONSTRAINT fk_profile_connection_requester
            FOREIGN KEY (requester_id) REFERENCES profile(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_profile_connection_addressee'
          AND conrelid = 'profile_connection'::regclass
    ) THEN
        ALTER TABLE profile_connection
            ADD CONSTRAINT fk_profile_connection_addressee
            FOREIGN KEY (addressee_id) REFERENCES profile(id) ON DELETE CASCADE;
    END IF;
END $$;
