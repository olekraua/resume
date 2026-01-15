ALTER TABLE language
    DROP CONSTRAINT IF EXISTS language_profile_name_key;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'language_profile_name_type_key'
          AND conrelid = 'language'::regclass
    ) THEN
        ALTER TABLE language
            ADD CONSTRAINT language_profile_name_type_key UNIQUE (id_profile, name, type);
    END IF;
END $$;
