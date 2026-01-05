-- Script de test pour le système de recommandations IA
-- Date: 2025-01-05
-- Description: Test complet du système de recommandations

-- 1. Vérifier que la table user_preferences existe
SELECT 'Vérification de la table user_preferences' as test_step;
DESCRIBE user_preferences;

-- 2. Créer des données de test pour les préférences utilisateur
SELECT 'Insertion de données de test pour les préférences' as test_step;

-- Insérer des préférences pour un utilisateur de test (ID 1)
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
) VALUES (
    1,
    '["Programmation", "Web Development", "Data Science"]',
    'INTERMEDIAIRE',
    'VISUAL',
    15,
    '["Améliorer mes compétences actuelles", "Obtenir une certification", "Créer mon propre projet"]',
    '["Applications web", "Intelligence artificielle", "Analyse de données"]',
    'Développeur Full-Stack',
    10,
    40,
    'MEDIUM',
    true
);

-- 3. Vérifier les données insérées
SELECT 'Vérification des données de préférences insérées' as test_step;
SELECT * FROM user_preferences WHERE user_id = 1;

-- 4. Vérifier les parcours disponibles pour les recommandations
SELECT 'Vérification des parcours disponibles' as test_step;
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
ORDER BY created_at DESC
LIMIT 5;

-- 5. Vérifier les inscriptions existantes pour éviter les doublons
SELECT 'Vérification des inscriptions existantes' as test_step;
SELECT 
    pi.user_id,
    pa.titre as parcours_titre,
    pi.progression_pourcentage,
    pi.is_completed
FROM parcours_inscriptions pi
JOIN parcours_apprentissage pa ON pi.parcours_id = pa.id
WHERE pi.user_id = 1
ORDER BY pi.date_inscription DESC;

-- 6. Statistiques utilisateur pour les recommandations
SELECT 'Statistiques utilisateur pour les recommandations' as test_step;

-- XP et niveau de l'utilisateur
SELECT 
    u.email,
    ux.total_xp,
    ux.current_level,
    ux.xp_to_next_level
FROM users u
LEFT JOIN user_xp ux ON u.id = ux.user_id
WHERE u.id = 1;

-- Parcours complétés par catégorie
SELECT 
    pa.categorie,
    COUNT(*) as parcours_completes,
    AVG(pi.progression_pourcentage) as progression_moyenne
FROM parcours_inscriptions pi
JOIN parcours_apprentissage pa ON pi.parcours_id = pa.id
WHERE pi.user_id = 1 AND pi.is_completed = true
GROUP BY pa.categorie
ORDER BY parcours_completes DESC;

-- Performance moyenne aux quiz
SELECT 
    AVG(rq.score) as score_moyen_quiz,
    COUNT(rq.id) as total_quiz_passes
FROM resultat_quiz rq
WHERE rq.user_id = 1;

-- 7. Test de la logique de filtrage des parcours
SELECT 'Test de la logique de filtrage' as test_step;

-- Parcours non inscrits par l'utilisateur
SELECT 
    pa.id,
    pa.titre,
    pa.categorie,
    pa.niveau_difficulte,
    pa.duree_estimee_heures,
    CASE 
        WHEN pi.id IS NULL THEN 'Non inscrit'
        ELSE 'Déjà inscrit'
    END as statut_inscription
FROM parcours_apprentissage pa
LEFT JOIN parcours_inscriptions pi ON pa.id = pi.parcours_id AND pi.user_id = 1
WHERE pa.is_published = true
ORDER BY pa.created_at DESC
LIMIT 10;

-- 8. Calcul de popularité des parcours
SELECT 'Calcul de popularité des parcours' as test_step;
SELECT 
    pa.id,
    pa.titre,
    COUNT(pi.id) as nombre_inscriptions,
    COUNT(CASE WHEN pi.is_completed = true THEN 1 END) as nombre_completions,
    CASE 
        WHEN COUNT(pi.id) > 0 
        THEN ROUND((COUNT(CASE WHEN pi.is_completed = true THEN 1 END) * 100.0 / COUNT(pi.id)), 2)
        ELSE 0 
    END as taux_completion_pct
FROM parcours_apprentissage pa
LEFT JOIN parcours_inscriptions pi ON pa.id = pi.parcours_id
WHERE pa.is_published = true
GROUP BY pa.id, pa.titre
ORDER BY nombre_inscriptions DESC, taux_completion_pct DESC
LIMIT 10;

-- 9. Vérifier les données nécessaires pour le scoring
SELECT 'Vérification des données pour le scoring' as test_step;

-- Catégories disponibles
SELECT DISTINCT categorie, COUNT(*) as nombre_parcours
FROM parcours_apprentissage 
WHERE is_published = true AND categorie IS NOT NULL
GROUP BY categorie
ORDER BY nombre_parcours DESC;

-- Niveaux de difficulté disponibles
SELECT DISTINCT niveau_difficulte, COUNT(*) as nombre_parcours
FROM parcours_apprentissage 
WHERE is_published = true AND niveau_difficulte IS NOT NULL
GROUP BY niveau_difficulte
ORDER BY nombre_parcours DESC;

-- Distribution des durées
SELECT 
    CASE 
        WHEN duree_estimee_heures <= 10 THEN 'Court (≤10h)'
        WHEN duree_estimee_heures <= 25 THEN 'Moyen (11-25h)'
        WHEN duree_estimee_heures <= 50 THEN 'Long (26-50h)'
        ELSE 'Très long (>50h)'
    END as categorie_duree,
    COUNT(*) as nombre_parcours,
    AVG(duree_estimee_heures) as duree_moyenne
FROM parcours_apprentissage 
WHERE is_published = true AND duree_estimee_heures IS NOT NULL
GROUP BY 
    CASE 
        WHEN duree_estimee_heures <= 10 THEN 'Court (≤10h)'
        WHEN duree_estimee_heures <= 25 THEN 'Moyen (11-25h)'
        WHEN duree_estimee_heures <= 50 THEN 'Long (26-50h)'
        ELSE 'Très long (>50h)'
    END
ORDER BY duree_moyenne;

-- 10. Test final - Simulation d'une requête de recommandation
SELECT 'Simulation d\'une requête de recommandation' as test_step;

-- Parcours recommandés basés sur les préférences de l'utilisateur 1
SELECT 
    pa.id,
    pa.titre,
    pa.categorie,
    pa.niveau_difficulte,
    pa.duree_estimee_heures,
    pa.points_bonus,
    pa.certificat_enabled,
    COUNT(pi_stats.id) as popularite,
    AVG(pi_stats.progression_pourcentage) as progression_moyenne,
    -- Score de correspondance simulé
    CASE 
        WHEN pa.categorie IN ('Programmation', 'Web Development', 'Data Science') THEN 30
        ELSE 10
    END +
    CASE 
        WHEN pa.niveau_difficulte = 'INTERMEDIAIRE' THEN 25
        WHEN pa.niveau_difficulte IN ('DEBUTANT', 'AVANCE') THEN 15
        ELSE 5
    END +
    CASE 
        WHEN pa.duree_estimee_heures BETWEEN 10 AND 40 THEN 20
        WHEN pa.duree_estimee_heures BETWEEN 5 AND 50 THEN 10
        ELSE 5
    END +
    CASE 
        WHEN pa.certificat_enabled = true THEN 10
        ELSE 0
    END as score_simulation
FROM parcours_apprentissage pa
LEFT JOIN parcours_inscriptions pi_stats ON pa.id = pi_stats.parcours_id
LEFT JOIN parcours_inscriptions pi_user ON pa.id = pi_user.parcours_id AND pi_user.user_id = 1
WHERE pa.is_published = true 
    AND pi_user.id IS NULL  -- Pas encore inscrit
GROUP BY pa.id, pa.titre, pa.categorie, pa.niveau_difficulte, pa.duree_estimee_heures, pa.points_bonus, pa.certificat_enabled
ORDER BY score_simulation DESC, popularite DESC
LIMIT 5;

SELECT 'Test du système de recommandations terminé avec succès !' as result;