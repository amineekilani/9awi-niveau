-- Migration V4: Add created_at and phone_number columns to users table
ALTER TABLE users ADD COLUMN created_at BIGINT DEFAULT NULL;
ALTER TABLE users ADD COLUMN phone_number VARCHAR(20) DEFAULT NULL;
