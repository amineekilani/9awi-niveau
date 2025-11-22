-- Migration pour supprimer la colonne username de la table users
-- Cette migration doit être exécutée après le déploiement du nouveau code

-- Supprimer la colonne username
ALTER TABLE users DROP COLUMN username;

-- Note: Assurez-vous que l'application utilise maintenant l'email pour l'authentification
-- avant d'exécuter cette migration
