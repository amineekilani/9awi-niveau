-- Migration pour ajouter la table des préférences utilisateur pour les recommandations IA
-- Date: 2025-01-05
-- Description: Table pour stocker les préférences d'apprentissage des utilisateurs

-- Créer la table user_preferences
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    preferred_categories TEXT COMMENT 'JSON array des catégories préférées',
    preferred_difficulty ENUM('DEBUTANT', 'INTERMEDIAIRE', 'AVANCE', 'EXPERT') COMMENT 'Niveau de difficulté préféré',
    learning_style VARCHAR(50) COMMENT 'Style d\'apprentissage: VISUAL, AUDITORY, KINESTHETIC, READING',
    time_availability_hours INT COMMENT 'Heures disponibles par semaine',
    learning_goals TEXT COMMENT 'JSON array des objectifs d\'apprentissage',
    interests TEXT COMMENT 'JSON array des centres d\'intérêt',
    career_focus VARCHAR(100) COMMENT 'Orientation professionnelle',
    preferred_duration_min INT COMMENT 'Durée minimale préférée (heures)',
    preferred_duration_max INT COMMENT 'Durée maximale préférée (heures)',
    challenge_preference VARCHAR(20) COMMENT 'Niveau de défi souhaité: LOW, MEDIUM, HIGH',
    certification_important BOOLEAN DEFAULT FALSE COMMENT 'Importance des certificats',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Contraintes
    CONSTRAINT fk_user_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Index pour les performances
    INDEX idx_user_preferences_user_id (user_id),
    INDEX idx_user_preferences_difficulty (preferred_difficulty),
    INDEX idx_user_preferences_learning_style (learning_style),
    INDEX idx_user_preferences_career_focus (career_focus)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Préférences utilisateur pour les recommandations IA';

-- Ajouter quelques préférences par défaut pour les utilisateurs existants (optionnel)
-- Ceci peut être fait plus tard via l'interface utilisateur

-- Vérifier la création de la table
SELECT 'Table user_preferences créée avec succès' as status;

-- Afficher la structure de la table
DESCRIBE user_preferences;