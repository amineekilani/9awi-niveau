-- Migration pour ajouter les tables Exercice et ExerciceElement

-- Table Exercice
CREATE TABLE IF NOT EXISTS exercice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    type_exercice ENUM('FILL_BLANK', 'DRAG_DROP', 'MATCHING') NOT NULL,
    module_id BIGINT NOT NULL,
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (module_id) REFERENCES module(id) ON DELETE CASCADE,
    UNIQUE KEY unique_module_exercice (module_id)
);

-- Table ExerciceElement
CREATE TABLE IF NOT EXISTS exercice_element (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contenu TEXT NOT NULL,
    type_element ENUM('TEXT', 'BLANK', 'DRAGGABLE', 'DROP_ZONE', 'MATCH_ITEM') NOT NULL,
    position_ordre INT NOT NULL,
    reponse_correcte VARCHAR(500),
    options TEXT, -- JSON pour les options multiples
    exercice_id BIGINT NOT NULL,
    created_at BIGINT,
    FOREIGN KEY (exercice_id) REFERENCES exercice(id) ON DELETE CASCADE
);

-- Index pour améliorer les performances
CREATE INDEX idx_exercice_module ON exercice(module_id);
CREATE INDEX idx_exercice_element_exercice ON exercice_element(exercice_id);
CREATE INDEX idx_exercice_element_ordre ON exercice_element(position_ordre);
CREATE INDEX idx_exercice_type ON exercice(type_exercice);