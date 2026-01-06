-- Script de démarrage rapide pour le système de recommandations IA
-- Exécutez ce script pour configurer rapidement le système

-- 1. Créer la table des préférences utilisateur
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
    
    CONSTRAINT fk_user_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_preferences_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Insérer des préférences d'exemple pour les utilisateurs existants
INSERT IGNORE INTO user_preferences (
    user_id, 
    preferred_categories, 
    preferred_difficulty, 
    learning_style, 
    time_availability_hours,
    learning_goals,
    interests,
    career_focus,
    preferred_duration_min,
    preferred_duration_max,
    challenge_preference,
    certification_important
) 
SELECT 
    u.id,
    CASE 
        WHEN u.role = 'ETUDIANT' THEN '["Programmation", "Web Development"]'
        ELSE '["Data Science", "Intelligence Artificielle"]'
    END,
    'INTERMEDIAIRE',
    'VISUAL',
    10,
    '["Améliorer mes compétences actuelles", "Obtenir une certification"]',
    '["Applications web", "Analyse de données"]',
    CASE 
        WHEN u.role = 'ETUDIANT' THEN 'Développeur Full-Stack'
        ELSE 'Data Scientist'
    END,
    5,
    30,
    'MEDIUM',
    true
FROM users u 
WHERE u.role IN ('ETUDIANT', 'FORMATEUR') 
AND NOT EXISTS (SELECT 1 FROM user_preferences up WHERE up.user_id = u.id);

-- 3. Vérifier les données créées
SELECT 'Préférences créées pour les utilisateurs:' as info;
SELECT 
    u.email,
    u.role,
    up.preferred_difficulty,
    up.learning_style,
    up.career_focus
FROM users u
JOIN user_preferences up ON u.id = up.user_id
ORDER BY u.email;

-- 4. Afficher les parcours disponibles pour les recommandations
SELECT 'Parcours disponibles pour les recommandations:' as info;
SELECT 
    id,
    titre,
    categorie,
    niveau_difficulte,
    duree_estimee_heures,
    points_bonus,
    certificat_enabled,
    is_published
FROM parcours_apprentissage 
WHERE is_published = true
ORDER BY created_at DESC;

-- 5. Statistiques du système
SELECT 'Statistiques du système de recommandations:' as info;
SELECT 
    (SELECT COUNT(*) FROM user_preferences) as utilisateurs_avec_preferences,
    (SELECT COUNT(*) FROM parcours_apprentissage WHERE is_published = true) as parcours_disponibles,
    (SELECT COUNT(*) FROM parcours_inscriptions) as inscriptions_totales,
    (SELECT COUNT(*) FROM parcours_inscriptions WHERE is_completed = true) as parcours_completes;

SELECT '✅ Système de recommandations IA configuré avec succès !' as status;