-- Step 2: drop profile.password after verification
DO $$
DECLARE
    profile_count bigint;
    auth_count bigint;
    null_hash_count bigint;
    missing_auth_count bigint;
    missing_profile_count bigint;
    uid_mismatch_count bigint;
    bad_created_count bigint;
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'profile'
          AND column_name = 'password'
    ) THEN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.tables
            WHERE table_name = 'auth_user'
        ) THEN
            RAISE EXCEPTION 'auth_user table is missing; aborting drop of profile.password';
        END IF;

        SELECT count(*) INTO profile_count FROM profile;
        SELECT count(*) INTO auth_count FROM auth_user;
        SELECT count(*) INTO null_hash_count FROM auth_user WHERE password_hash IS NULL;
        SELECT count(*) INTO missing_auth_count
        FROM profile p
        LEFT JOIN auth_user a ON a.id = p.id
        WHERE a.id IS NULL;
        SELECT count(*) INTO missing_profile_count
        FROM auth_user a
        LEFT JOIN profile p ON p.id = a.id
        WHERE p.id IS NULL;
        SELECT count(*) INTO uid_mismatch_count
        FROM auth_user a
        JOIN profile p ON p.id = a.id
        WHERE a.uid IS DISTINCT FROM p.uid;
        SELECT count(*) INTO bad_created_count
        FROM auth_user
        WHERE created IS NULL
           OR created > (now() + interval '1 day');

        IF profile_count <> auth_count THEN
            RAISE EXCEPTION 'auth_user count (%) does not match profile count (%)', auth_count, profile_count;
        END IF;
        IF null_hash_count > 0 THEN
            RAISE EXCEPTION 'auth_user has % rows with NULL password_hash', null_hash_count;
        END IF;
        IF missing_auth_count > 0 THEN
            RAISE EXCEPTION 'auth_user is missing for % profile rows', missing_auth_count;
        END IF;
        IF missing_profile_count > 0 THEN
            RAISE EXCEPTION 'profile is missing for % auth_user rows', missing_profile_count;
        END IF;
        IF uid_mismatch_count > 0 THEN
            RAISE EXCEPTION 'uid mismatch between auth_user and profile for % rows', uid_mismatch_count;
        END IF;
        IF bad_created_count > 0 THEN
            RAISE EXCEPTION 'auth_user has % rows with NULL or future created', bad_created_count;
        END IF;

        ALTER TABLE profile DROP COLUMN password;
    END IF;
END $$;
