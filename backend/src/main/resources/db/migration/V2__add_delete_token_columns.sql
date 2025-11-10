-- Add delete token columns to users table
ALTER TABLE users ADD COLUMN delete_token VARCHAR(255);
ALTER TABLE users ADD COLUMN delete_token_expiry BIGINT;
