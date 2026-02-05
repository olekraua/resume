-- Password moved to auth_user in auth-service
ALTER TABLE profile DROP COLUMN IF EXISTS password;
