ALTER TABLE remember_me_token
    ADD COLUMN IF NOT EXISTS username varchar(64);

UPDATE remember_me_token r
SET username = p.uid
FROM profile p
WHERE r.profile_id = p.id AND r.username IS NULL;

ALTER TABLE remember_me_token
    ALTER COLUMN username SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_remember_me_username ON remember_me_token(username);
