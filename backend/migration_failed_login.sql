-- Migration pour ajouter le suivi des tentatives de connexion échouées
-- À exécuter sur votre base de données MySQL

ALTER TABLE users 
ADD COLUMN failed_login_attempts INT DEFAULT 0,
ADD COLUMN last_failed_login BIGINT,
ADD COLUMN account_locked_until BIGINT;

-- Initialiser les valeurs pour les utilisateurs existants
UPDATE users SET failed_login_attempts = 0 WHERE failed_login_attempts IS NULL;
