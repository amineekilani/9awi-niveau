-- Migration pour ajouter la table lecons

CREATE TABLE IF NOT EXISTS lecons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    type_contenu VARCHAR(20) NOT NULL,
    contenu_texte TEXT,
    fichier_url VARCHAR(500),
    ordre INT,
    duree INT,
    created_at BIGINT,
    updated_at BIGINT,
    module_id BIGINT NOT NULL,
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE
);

-- Créer des index pour améliorer les performances
CREATE INDEX idx_lecons_module ON lecons(module_id);
CREATE INDEX idx_lecons_ordre ON lecons(ordre);
CREATE INDEX idx_lecons_type ON lecons(type_contenu);
