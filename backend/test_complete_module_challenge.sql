-- Script de test pour le défi COMPLETE_MODULE
-- Ce script permet de tester les défis de completion de modules

-- 1. Vérifier que les défis COMPLETE_MODULE existent
SELECT * FROM challenges WHERE challenge_type = 'COMPLETE_MODULE' AND is_active = true;

-- 2. Vérifier les modules d'un cours spécifique (remplacer 1 par l'ID du cours)
SELECT 
    c.titre as cours_titre,
    m.id as module_id,
    m.titre as module_titre,
    m.ordre as module_ordre,
    COUNT(l.id) as nombre_lecons,
    CASE WHEN q.id IS NOT NULL THEN 'Oui' ELSE 'Non' END as a_quiz
FROM cours c
JOIN modules m ON m.cours_id = c.id
LEFT JOIN lecons l ON l.module_id = m.id
LEFT JOIN quiz q ON q.module_id = m.id
WHERE c.id = 1  -- Remplacer par l'ID du cours à tester
GROUP BY c.id, c.titre, m.id, m.titre, m.ordre, q.id
ORDER BY m.ordre;

-- 3. Vérifier la progression d'un utilisateur dans un cours (remplacer les IDs)
SELECT 
    u.email,
    c.titre as cours_titre,
    m.titre as module_titre,
    COUNT(l.id) as total_lecons,
    COUNT(lc.id) as lecons_completees,
    CASE 
        WHEN COUNT(l.id) > 0 THEN ROUND((COUNT(lc.id) * 100.0 / COUNT(l.id)), 2)
        ELSE 0 
    END as progression_module_pct,
    CASE WHEN q.id IS NOT NULL THEN 'Oui' ELSE 'Non' END as a_quiz,
    CASE 
        WHEN q.id IS NOT NULL THEN (
            SELECT MAX(rq.score) 
            FROM resultat_quiz rq 
            WHERE rq.user_id = u.id AND rq.quiz_id = q.id
        )
        ELSE NULL 
    END as meilleur_score_quiz
FROM users u
CROSS JOIN cours c
JOIN modules m ON m.cours_id = c.id
LEFT JOIN lecons l ON l.module_id = m.id
LEFT JOIN lecon_completions lc ON lc.lecon_id = l.id AND lc.enrollment_id IN (
    SELECT e.id FROM enrollments e WHERE e.user_id = u.id AND e.cours_id = c.id
)
LEFT JOIN quiz q ON q.module_id = m.id
WHERE u.id = 1 AND c.id = 1  -- Remplacer par les IDs utilisateur et cours à tester
GROUP BY u.id, u.email, c.id, c.titre, m.id, m.titre, m.ordre, q.id
ORDER BY m.ordre;

-- 4. Calculer les modules terminés pour un utilisateur
SELECT 
    u.email,
    c.titre as cours_titre,
    COUNT(DISTINCT m.id) as total_modules,
    COUNT(DISTINCT CASE 
        WHEN (
            -- Toutes les leçons du module sont terminées
            (SELECT COUNT(*) FROM lecons l2 WHERE l2.module_id = m.id) = 
            (SELECT COUNT(*) FROM lecon_completions lc2 
             JOIN lecons l3 ON lc2.lecon_id = l3.id 
             WHERE l3.module_id = m.id AND lc2.enrollment_id = e.id)
        ) AND (
            -- Pas de quiz OU quiz réussi (score >= 60%)
            NOT EXISTS(SELECT 1 FROM quiz q2 WHERE q2.module_id = m.id) OR
            EXISTS(SELECT 1 FROM quiz q3 
                   JOIN resultat_quiz rq ON rq.quiz_id = q3.id 
                   WHERE q3.module_id = m.id AND rq.user_id = u.id AND rq.score >= 60)
        )
        THEN m.id 
    END) as modules_termines
FROM users u
JOIN enrollments e ON e.user_id = u.id
JOIN cours c ON e.cours_id = c.id
JOIN modules m ON m.cours_id = c.id
WHERE u.id = 1  -- Remplacer par l'ID utilisateur à tester
GROUP BY u.id, u.email, c.id, c.titre;

-- 5. Vérifier les défis COMPLETE_MODULE de l'utilisateur
SELECT 
    u.email,
    ch.name as defi_nom,
    ch.description,
    ch.target_value as modules_requis,
    uc.current_progress as progression_actuelle,
    uc.is_completed as termine,
    CASE WHEN uc.completed_at IS NOT NULL 
         THEN FROM_UNIXTIME(uc.completed_at / 1000) 
         ELSE NULL 
    END as date_completion
FROM users u
LEFT JOIN user_challenges uc ON uc.user_id = u.id
LEFT JOIN challenges ch ON uc.challenge_id = ch.id AND ch.challenge_type = 'COMPLETE_MODULE'
WHERE u.id = 1  -- Remplacer par l'ID utilisateur à tester
ORDER BY ch.target_value;

-- 6. Simuler la completion d'un module (ATTENTION: à utiliser avec précaution)
-- Décommentez les lignes suivantes pour marquer toutes les leçons d'un module comme terminées

/*
-- Marquer toutes les leçons du module 1 comme terminées pour l'utilisateur 1
INSERT IGNORE INTO lecon_completions (enrollment_id, lecon_id, completed_at)
SELECT 
    e.id as enrollment_id,
    l.id as lecon_id,
    UNIX_TIMESTAMP() * 1000 as completed_at
FROM enrollments e
JOIN cours c ON e.cours_id = c.id
JOIN modules m ON m.cours_id = c.id
JOIN lecons l ON l.module_id = m.id
WHERE e.user_id = 1 AND m.id = 1;  -- Remplacer par les IDs utilisateur et module

-- Créer un résultat de quiz réussi si le module a un quiz
INSERT IGNORE INTO resultat_quiz (user_id, quiz_id, score, completed_at)
SELECT 
    1 as user_id,  -- Remplacer par l'ID utilisateur
    q.id as quiz_id,
    75.0 as score,  -- Score de réussite
    UNIX_TIMESTAMP() * 1000 as completed_at
FROM quiz q
WHERE q.module_id = 1;  -- Remplacer par l'ID du module
*/

-- 7. Statistiques globales des défis COMPLETE_MODULE
SELECT 
    'Défis COMPLETE_MODULE actifs' as statistique,
    COUNT(*) as nombre
FROM challenges 
WHERE challenge_type = 'COMPLETE_MODULE' AND is_active = true

UNION ALL

SELECT 
    'Utilisateurs ayant terminé au moins 1 module' as statistique,
    COUNT(DISTINCT uc.user_id) as nombre
FROM user_challenges uc
JOIN challenges ch ON uc.challenge_id = ch.id
WHERE ch.challenge_type = 'COMPLETE_MODULE' AND uc.current_progress > 0

UNION ALL

SELECT 
    'Défis COMPLETE_MODULE terminés' as statistique,
    COUNT(*) as nombre
FROM user_challenges uc
JOIN challenges ch ON uc.challenge_id = ch.id
WHERE ch.challenge_type = 'COMPLETE_MODULE' AND uc.is_completed = true;