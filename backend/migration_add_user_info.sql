-- Migration pour ajouter les champs nom, prénom et date de naissance à la table users

ALTER TABLE users ADD COLUMN first_name VARCHAR(255);
ALTER TABLE users ADD COLUMN last_name VARCHAR(255);
ALTER TABLE users ADD COLUMN date_of_birth VARCHAR(255);
