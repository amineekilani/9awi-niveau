-- Migration pour ajouter les tables des parcours d'apprentissage
-- À exécuter dans l'ordre

-- 1. Table principale des parcours d'apprentissage
CREATE TABLE IF NOT EXISTS parcours_apprentissage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(500),
    categorie VARCHAR(100),
    niveau_difficulte ENUM('DEBUTANT', 'INTERMEDIAIRE', 'AVANCE', 'EXPERT'),
    duree_estimee_heures INT,
    prerequis TEXT,
    type_parcours ENUM('LINEAIRE', 'FLEXIBLE') DEFAULT 'LINEAIRE',
    points_bonus INT DEFAULT 0,
    badge_completion VARCHAR(255),
    certificat_enabled BOOLEAN DEFAULT FALSE,
    is_published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    formateur_id BIGINT NOT NULL,
    
    FOREIGN KEY (formateur_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_parcours_formateur (formateur_id),
    INDEX idx_parcours_published (is_published),
    INDEX idx_parcours_categorie (categorie),
    INDEX idx_parcours_niveau (niveau_difficulte),
    INDEX idx_parcours_created (created_at)
);

-- 2. Table des étapes du parcours
CREATE TABLE IF NOT EXISTS parcours_etapes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parcours_id BIGINT NOT NULL,
    cours_id BIGINT NOT NULL,
    ordre_etape INT NOT NULL,
    niveau_etape INT DEFAULT 1 COMMENT '1=Fondamental, 2=Intermédiaire, 3=Avancé',
    is_obligatoire BOOLEAN DEFAULT TRUE,
    score_minimum INT DEFAULT 0,
    pourcentage_completion_requis INT DEFAULT 100,
    quiz_obligatoires BOOLEAN DEFAULT FALSE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parcours_id) REFERENCES parcours_apprentissage(id) ON DELETE CASCADE,
    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE,
    UNIQUE KEY unique_parcours_cours (parcours_id, cours_id),
    UNIQUE KEY unique_parcours_ordre (parcours_id, ordre_etape),
    INDEX idx_etapes_parcours (parcours_id),
    INDEX idx_etapes_cours (cours_id),
    INDEX idx_etapes_ordre (ordre_etape),
    INDEX idx_etapes_niveau (niveau_etape)
);

-- 3. Table des conditions de déblocage
CREATE TABLE IF NOT EXISTS parcours_conditions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    etape_id BIGINT NOT NULL,
    etape_prerequise_id BIGINT,
    type_condition ENUM('SCORE_MINIMUM', 'POURCENTAGE_COMPLETION', 'QUIZ_REUSSI', 'ETAPE_PRECEDENTE', 'TEMPS_MINIMUM'),
    valeur_requise INT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (etape_id) REFERENCES parcours_etapes(id) ON DELETE CASCADE,
    FOREIGN KEY (etape_prerequise_id) REFERENCES parcours_etapes(id) ON DELETE CASCADE,
    INDEX idx_conditions_etape (etape_id),
    INDEX idx_conditions_prerequise (etape_prerequise_id),
    INDEX idx_conditions_type (type_condition)
);

-- 4. Table des inscriptions aux parcours
CREATE TABLE IF NOT EXISTS parcours_inscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parcours_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_completion TIMESTAMP NULL,
    progression_pourcentage INT DEFAULT 0,
    etape_courante INT DEFAULT 1,
    points_gagnes INT DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    certificat_genere BOOLEAN DEFAULT FALSE,
    certificat_url VARCHAR(500),
    
    FOREIGN KEY (parcours_id) REFERENCES parcours_apprentissage(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_parcours (user_id, parcours_id),
    INDEX idx_inscriptions_parcours (parcours_id),
    INDEX idx_inscriptions_user (user_id),
    INDEX idx_inscriptions_completed (is_completed),
    INDEX idx_inscriptions_progression (progression_pourcentage),
    INDEX idx_inscriptions_date (date_inscription)
);

-- 5. Ajouter des données de test (optionnel)
-- Vous pouvez décommenter ces lignes pour avoir des données de test

/*
-- Exemple de parcours de test (remplacez les IDs par des IDs valides de votre base)
INSERT INTO parcours_apprentissage (titre, description, categorie, niveau_difficulte, duree_estimee_heures, type_parcours, points_bonus, formateur_id, is_published) 
VALUES 
('Parcours Développement Web Complet', 'Un parcours complet pour apprendre le développement web de A à Z', 'Développement', 'DEBUTANT', 40, 'LINEAIRE', 500, 1, TRUE),
('Parcours Data Science Avancé', 'Maîtrisez les techniques avancées de data science et machine learning', 'Data Science', 'AVANCE', 60, 'FLEXIBLE', 750, 1, TRUE);

-- Exemples d'étapes (remplacez les IDs par des IDs valides)
INSERT INTO parcours_etapes (parcours_id, cours_id, ordre_etape, niveau_etape, is_obligatoire, score_minimum, pourcentage_completion_requis) 
VALUES 
(1, 1, 1, 1, TRUE, 70, 100),
(1, 2, 2, 1, TRUE, 70, 100),
(1, 3, 3, 2, TRUE, 75, 100),
(2, 4, 1, 2, TRUE, 80, 100),
(2, 5, 2, 3, TRUE, 85, 100);
*/

-- 6. Vérification des tables créées
SELECT 
    TABLE_NAME, 
    TABLE_ROWS, 
    CREATE_TIME 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME LIKE 'parcours%'
ORDER BY TABLE_NAME;

-- 7. Afficher la structure des nouvelles tables
DESCRIBE parcours_apprentissage;
DESCRIBE parcours_etapes;
DESCRIBE parcours_conditions;
DESCRIBE parcours_inscriptions;