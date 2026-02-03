CREATE TABLE IF NOT EXISTS conversation (
    id bigserial PRIMARY KEY,
    type varchar(16) NOT NULL,
    pair_key varchar(64),
    last_message_id bigint,
    last_message_at timestamptz,
    created timestamptz NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_conversation_pair
    ON conversation(pair_key)
    WHERE pair_key IS NOT NULL;

CREATE TABLE IF NOT EXISTS conversation_participant (
    id bigserial PRIMARY KEY,
    conversation_id bigint NOT NULL,
    profile_id bigint NOT NULL,
    last_read_message_id bigint,
    last_read_at timestamptz,
    CONSTRAINT uk_conversation_participant UNIQUE (conversation_id, profile_id)
);

CREATE INDEX IF NOT EXISTS idx_conversation_participant_profile
    ON conversation_participant(profile_id);

CREATE INDEX IF NOT EXISTS idx_conversation_participant_conversation
    ON conversation_participant(conversation_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_conversation_participant_conversation'
          AND conrelid = 'conversation_participant'::regclass
    ) THEN
        ALTER TABLE conversation_participant
            ADD CONSTRAINT fk_conversation_participant_conversation
            FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE;
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS message (
    id bigserial PRIMARY KEY,
    conversation_id bigint NOT NULL,
    sender_id bigint NOT NULL,
    body text NOT NULL,
    created timestamptz NOT NULL,
    edited timestamptz,
    deleted timestamptz
);

CREATE INDEX IF NOT EXISTS idx_message_conversation_created
    ON message(conversation_id, created DESC);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_message_conversation'
          AND conrelid = 'message'::regclass
    ) THEN
        ALTER TABLE message
            ADD CONSTRAINT fk_message_conversation
            FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE;
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS message_attachment (
    id bigserial PRIMARY KEY,
    message_id bigint,
    conversation_id bigint NOT NULL,
    uploader_id bigint NOT NULL,
    storage_key varchar(255) NOT NULL,
    original_name varchar(255) NOT NULL,
    content_type varchar(100) NOT NULL,
    size bigint NOT NULL,
    created timestamptz NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_message_attachment_conversation
    ON message_attachment(conversation_id);

CREATE INDEX IF NOT EXISTS idx_message_attachment_message
    ON message_attachment(message_id);

CREATE INDEX IF NOT EXISTS idx_message_attachment_uploader
    ON message_attachment(uploader_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_message_attachment_conversation'
          AND conrelid = 'message_attachment'::regclass
    ) THEN
        ALTER TABLE message_attachment
            ADD CONSTRAINT fk_message_attachment_conversation
            FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_message_attachment_message'
          AND conrelid = 'message_attachment'::regclass
    ) THEN
        ALTER TABLE message_attachment
            ADD CONSTRAINT fk_message_attachment_message
            FOREIGN KEY (message_id) REFERENCES message(id) ON DELETE SET NULL;
    END IF;
END$$;
