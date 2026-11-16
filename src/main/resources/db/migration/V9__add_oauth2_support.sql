-- V9__add_oauth2_support.sql
-- Add OAuth2 fields (provider, provider_id) and allow null password for social login users

-- Allow password to be NULL for OAuth2 users
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;

-- Add provider and provider_id for social login
ALTER TABLE users ADD COLUMN IF NOT EXISTS provider VARCHAR(50) DEFAULT 'LOCAL';
ALTER TABLE users ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255);

-- Add index on email and provider for fast authentication lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_provider ON users(provider, provider_id);
