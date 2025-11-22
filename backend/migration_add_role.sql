-- Migration pour ajouter le système de rôles et la table cours

-- 1. Modifier la colonne role dans la table users
ALTER TABLE users MODIFY COLUMN role VARCHAR(20) NOT NULL DEFAULT 'ETUDIANT';

-- 2. Mettre à jour les valeurs existantes
UPDATE users SET role = 'ETUDIANT' WHERE role = 'USER' OR role IS NULL;

-- 3. Créer la table cours
CREATE TABLE IF NOT EXISTS cours (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    created_at BIGINT,
    updated_at BIGINT,
    archived BOOLEAN DEFAULT FALSE,
    archived_at BIGINT,
    formateur_id BIGINT NOT NULL,
    FOREIGN KEY (formateur_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Créer des index pour améliorer les performances
CREATE INDEX idx_cours_formateur ON cours(formateur_id);
CREATE INDEX idx_cours_archived ON cours(archived);
