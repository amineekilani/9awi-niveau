-- Script pour corriger les données et éviter les erreurs de recommandations
-- Date: 2025-01-06
-- Description: Assure que les données nécessaires existent pour éviter les NullPointerException

-- 1. Vérifier et créer des données UserXP pour tous les utilisateurs
INSERT IGNORE INTO user_xp (user_id, total_xp, current_level, xp_to_next_level, last_updated)
SELECT 
    u.id,
    COALESCE(
        (SELECT SUM(CASE 
            WHEN rq.score >= 80 THEN 20
            WHEN rq.score >= 60 THEN 15
            WHEN rq.score >= 40 THEN 10
            ELSE 5
        END) FROM resultat_quiz rq WHERE rq.user_id = u.id), 0
    ) + 
    COALESCE(
        (SELECT COUNT(*) * 50 FROM enrollments e WHERE e.user_id = u.id AND e.progress = 100), 0
    ) as total_xp,
    CASE 
        WHEN COALESCE(
            (SELECT SUM(CASE 
                WHEN rq.score >= 80 THEN 20
                WHEN rq.score >= 60 THEN 15
                WHEN rq.score >= 40 THEN 10
                ELSE 5
            END) FROM resultat_quiz rq WHERE rq.user_id = u.id), 0
        ) >= 1000 THEN 5
        WHEN COALESCE(
            (SELECT SUM(CASE 
                WHEN rq.score >= 80 THEN 20
                WHEN rq.score >= 60 THEN 15
                WHEN rq.score >= 40 THEN 10
                ELSE 5
            END) FROM resultat_quiz rq WHERE rq.user_id = u.id), 0
        ) >= 500 THEN 4
        WHEN COALESCE(
            (SELECT SUM(CASE 
                WHEN rq.score >= 80 THEN 20
                WHEN rq.score >= 60 THEN 15
                WHEN rq.score >= 40 THEN 10
                ELSE 5
            END) FROM resultat_quiz rq WHERE rq.user_id = u.id), 0
        ) >= 250 THEN 3
        WHEN COALESCE(
            (SELECT SUM(CASE 
                WHEN rq.score >= 80 THEN 20
                WHEN rq.score >= 60 THEN 15
                WHEN rq.score >= 40 THEN 10
                ELSE 5
            END) FROM resultat_quiz rq WHERE rq.user_id = u.id), 0
        ) >= 100 THEN 2
        ELSE 1
    END as current_level,
    100 as xp_to_next_level,
    UNIX_TIMESTAMP() * 1000 as last_updated
FROM users u
WHERE u.role IN ('ETUDIANT', 'FORMATEUR')
AND NOT EXISTS (SELECT 1 FROM user_xp ux WHERE ux.user_id = u.id);

-- 2. Créer des préférences par défaut pour tous les utilisateurs sans préférences
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
    certification_important,
    created_at,
    updated_at
) 
SELECT 
    u.id,
    CASE 
        WHEN u.role = 'ETUDIANT' THEN '["Programmation", "Web Development"]'
        ELSE '["Data Science", "Intelligence Artificielle"]'
    END as preferred_categories,
    'INTERMEDIAIRE' as preferred_difficulty,
    'VISUAL' as learning_style,
    10 as time_availability_hours,
    '["Améliorer mes compétences actuelles", "Obtenir une certification"]' as learning_goals,
    '["Applications web", "Analyse de données"]' as interests,
    CASE 
        WHEN u.role = 'ETUDIANT' THEN 'Développeur Full-Stack'
        ELSE 'Data Scientist'
    END as career_focus,
    5 as preferred_duration_min,
    30 as preferred_duration_max,
    'MEDIUM' as challenge_preference,
    true as certification_important,
    NOW() as created_at,
    NOW() as updated_at
FROM users u 
WHERE u.role IN ('ETUDIANT', 'FORMATEUR') 
AND NOT EXISTS (SELECT 1 FROM user_preferences up WHERE up.user_id = u.id);

-- 3. Créer quelques parcours de test s'il n'y en a pas assez
INSERT IGNORE INTO parcours_apprentissage (
    titre, 
    description, 
    categorie, 
    niveau_difficulte, 
    duree_estimee_heures, 
    prerequis, 
    type_parcours, 
    points_bonus, 
    certificat_enabled, 
    is_published, 
    formateur_id,
    created_at,
    updated_at
)
SELECT 
    'Parcours de Test - ' || categories.categorie,
    'Description du parcours de test pour la catégorie ' || categories.categorie,
    categories.categorie,
    niveaux.niveau,
    FLOOR(10 + (RAND() * 40)) as duree_estimee_heures,
    'Aucun prérequis spécifique',
    'LINEAIRE',
    FLOOR(50 + (RAND() * 150)) as points_bonus,
    true,
    true,
    (SELECT id FROM users WHERE role = 'FORMATEUR' LIMIT 1),
    NOW(),
    NOW()
FROM (
    SELECT 'Programmation' as categorie
    UNION SELECT 'Web Development'
    UNION SELECT 'Data Science'
    UNION SELECT 'Intelligence Artificielle'
    UNION SELECT 'Cybersécurité'
    UNION SELECT 'DevOps'
) categories
CROSS JOIN (
    SELECT 'DEBUTANT' as niveau
    UNION SELECT 'INTERMEDIAIRE'
    UNION SELECT 'AVANCE'
) niveaux
WHERE (SELECT COUNT(*) FROM parcours_apprentissage WHERE is_published = true) < 10
LIMIT 15;

-- 4. Créer quelques inscriptions de test pour avoir des données d'historique
INSERT IGNORE INTO parcours_inscriptions (
    parcours_id,
    user_id,
    date_inscription,
    date_completion,
    progression_pourcentage,
    etape_courante,
    points_gagnes,
    is_completed,
    certificat_genere
)
SELECT 
    pa.id,
    u.id,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 90) DAY),
    CASE 
        WHEN RAND() > 0.7 THEN DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY)
        ELSE NULL
    END,
    CASE 
        WHEN RAND() > 0.7 THEN 100
        ELSE FLOOR(20 + (RAND() * 80))
    END,
    1,
    CASE 
        WHEN RAND() > 0.7 THEN pa.points_bonus
        ELSE FLOOR((pa.points_bonus * RAND()))
    END,
    RAND() > 0.7,
    RAND() > 0.8
FROM parcours_apprentissage pa
CROSS JOIN users u
WHERE pa.is_published = true 
    AND u.role = 'ETUDIANT'
    AND RAND() > 0.6  -- 40% de chance d'inscription
    AND NOT EXISTS (
        SELECT 1 FROM parcours_inscriptions pi 
        WHERE pi.parcours_id = pa.id AND pi.user_id = u.id
    )
LIMIT 20;

-- 5. Créer quelques résultats de quiz pour avoir des statistiques
INSERT IGNORE INTO resultat_quiz (
    user_id,
    quiz_id,
    score,
    date_passed,
    time_taken,
    attempts
)
SELECT 
    u.id,
    q.id,
    ROUND(40 + (RAND() * 60), 2) as score,  -- Score entre 40 et 100
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 60) DAY),
    FLOOR(300 + (RAND() * 1200)) as time_taken,  -- Entre 5 et 25 minutes
    1
FROM users u
CROSS JOIN quiz q
WHERE u.role = 'ETUDIANT'
    AND RAND() > 0.7  -- 30% de chance de passer un quiz
    AND NOT EXISTS (
        SELECT 1 FROM resultat_quiz rq 
        WHERE rq.user_id = u.id AND rq.quiz_id = q.id
    )
LIMIT 30;

-- 6. Vérifications finales
SELECT 'Vérification des données créées:' as info;

SELECT 
    'UserXP' as table_name,
    COUNT(*) as count
FROM user_xp
UNION ALL
SELECT 
    'UserPreferences' as table_name,
    COUNT(*) as count
FROM user_preferences
UNION ALL
SELECT 
    'Parcours publiés' as table_name,
    COUNT(*) as count
FROM parcours_apprentissage WHERE is_published = true
UNION ALL
SELECT 
    'Inscriptions parcours' as table_name,
    COUNT(*) as count
FROM parcours_inscriptions
UNION ALL
SELECT 
    'Résultats quiz' as table_name,
    COUNT(*) as count
FROM resultat_quiz;

-- 7. Test de la requête de statistiques quiz
SELECT 'Test de la requête statistiques quiz:' as info;
SELECT 
    u.email,
    COALESCE(AVG(rq.score), 0) as score_moyen,
    COALESCE(COUNT(rq.id), 0) as quiz_passes
FROM users u
LEFT JOIN resultat_quiz rq ON u.id = rq.user_id
WHERE u.role = 'ETUDIANT'
GROUP BY u.id, u.email
LIMIT 5;

SELECT '✅ Données de test créées avec succès pour éviter les erreurs de recommandations !' as status;