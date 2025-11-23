-- Migration pour ajouter la table modules

CREATE TABLE IF NOT EXISTS modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    contenu TEXT,
    ordre INT,
    created_at BIGINT,
    updated_at BIGINT,
    cours_id BIGINT NOT NULL,
    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE
);

-- Créer des index pour améliorer les performances
CREATE INDEX idx_modules_cours ON modules(cours_id);
CREATE INDEX idx_modules_ordre ON modules(ordre);
