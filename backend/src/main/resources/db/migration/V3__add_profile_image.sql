-- Add profile_image column to store the profile image filename
ALTER TABLE users ADD COLUMN profile_image VARCHAR(255) DEFAULT NULL;
