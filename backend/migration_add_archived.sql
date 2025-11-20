-- Migration pour ajouter les colonnes d'archivage
-- Date: 2025-11-20
-- Description: Ajoute les colonnes 'archived' et 'archived_at' pour archiver les comptes au lieu de les supprimer

-- Ajouter la colonne archived (par défaut false)
ALTER TABLE users ADD COLUMN archived BOOLEAN DEFAULT FALSE;

-- Ajouter la colonne archived_at (timestamp en millisecondes)
ALTER TABLE users ADD COLUMN archived_at BIGINT;

-- Mettre à jour tous les utilisateurs existants pour s'assurer qu'ils ne sont pas archivés
UPDATE users SET archived = FALSE WHERE archived IS NULL;

-- Créer un index pour améliorer les performances des requêtes
CREATE INDEX idx_users_archived ON users(archived);
CREATE INDEX idx_users_email_archived ON users(email, archived);
