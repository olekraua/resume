-- Step 1: allow profile.password to be NULL (safe transition)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'profile'
          AND column_name = 'password'
          AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE profile ALTER COLUMN password DROP NOT NULL;
    END IF;
END $$;
