-- Test complet des badges "Premier Cours" et "Premier Défi"
-- Ce script teste l'attribution automatique des badges après les corrections

-- 1. Nettoyer les données de test précédentes (optionnel)
-- DELETE FROM user_badges WHERE badge_id IN (
--     SELECT id FROM badges WHERE criteria_type IN ('FIRST_COURSE', 'CHALLENGE_COMPLETED')
-- );

-- 2. Vérifier qu'un utilisateur test existe et a des cours/défis
SELECT 
    'Utilisateur test - État actuel' as section,
    u.id,
    u.email,
    COUNT(DISTINCT e.id) as enrollments_total,
    COUNT(DISTINCT CASE WHEN e.progress >= 100 THEN e.id END) as cours_termines,
    COUNT(DISTINCT uc.id) as defis_total,
    COUNT(DISTINCT CASE WHEN uc.is_completed = TRUE THEN uc.id END) as defis_termines
FROM users u
LEFT JOIN enrollments e ON u.id = e.user_id
LEFT JOIN user_challenges uc ON u.id = uc.user_id
WHERE (u.email LIKE '%test%' OR u.email LIKE '%admin%' OR u.role = 'APPRENANT')
GROUP BY u.id, u.email
ORDER BY cours_termines DESC, defis_termines DESC
LIMIT 3;

-- 3. Vérifier les badges disponibles
SELECT 
    'Badges disponibles' as section,
    id,
    name,
    description,
    criteria_type,
    criteria_value,
    is_active
FROM badges
WHERE criteria_type IN ('FIRST_COURSE', 'CHALLENGE_COMPLETED', 'COURS_COMPLETED')
ORDER BY criteria_type, criteria_value;

-- 4. Simuler la completion d'un cours pour déclencher le badge
-- Trouver un utilisateur qui n'a pas encore le badge FIRST_COURSE
SET @test_user = (
    SELECT u.id
    FROM users u
    WHERE (u.email LIKE '%test%' OR u.email LIKE '%admin%' OR u.role = 'APPRENANT')
    AND NOT EXISTS (
        SELECT 1 FROM user_badges ub 
        JOIN badges b ON ub.badge_id = b.id 
        WHERE ub.user_id = u.id AND b.criteria_type = 'FIRST_COURSE'
    )
    ORDER BY u.id
    LIMIT 1
);

-- S'assurer que cet utilisateur a au moins un cours terminé
UPDATE enrollments 
SET progress = 100 
WHERE user_id = @test_user 
AND progress < 100
LIMIT 1;

-- 5. Simuler la completion d'un défi
-- Trouver ou créer un défi actif
INSERT IGNORE INTO challenges (
    name,
    description,
    challenge_type,
    target_value,
    xp_reward,
    start_date,
    end_date,
    is_active,
    created_at,
    updated_at
) VALUES (
    'Test Premier Défi',
    'Défi de test pour valider les badges',
    'COMPLETE_COURSES',
    1,
    100,
    UNIX_TIMESTAMP() * 1000 - 86400000, -- Hier
    UNIX_TIMESTAMP() * 1000 + 86400000, -- Demain
    TRUE,
    UNIX_TIMESTAMP() * 1000,
    UNIX_TIMESTAMP() * 1000
);

-- Inscrire l'utilisateur test au défi et le marquer comme terminé
SET @test_challenge = (SELECT id FROM challenges WHERE name = 'Test Premier Défi' LIMIT 1);

INSERT IGNORE INTO user_challenges (
    user_id,
    challenge_id,
    current_progress,
    is_completed,
    joined_at,
    completed_at
) VALUES (
    @test_user,
    @test_challenge,
    1,
    TRUE,
    UNIX_TIMESTAMP() * 1000 - 3600000, -- Il y a 1 heure
    UNIX_TIMESTAMP() * 1000
);

-- 6. Attribuer manuellement les badges pour tester (simulation de ce que le code Java devrait faire)
-- Badge Premier Cours
INSERT IGNORE INTO user_badges (user_id, badge_id, earned_at)
SELECT 
    @test_user,
    b.id,
    UNIX_TIMESTAMP() * 1000
FROM badges b
WHERE b.criteria_type = 'FIRST_COURSE'
AND b.criteria_value = 1
AND b.is_active = TRUE
AND @test_user IS NOT NULL;

-- Badge Premier Défi
INSERT IGNORE INTO user_badges (user_id, badge_id, earned_at)
SELECT 
    @test_user,
    b.id,
    UNIX_TIMESTAMP() * 1000
FROM badges b
WHERE b.criteria_type = 'CHALLENGE_COMPLETED'
AND b.criteria_value = 1
AND b.is_active = TRUE
AND @test_user IS NOT NULL;

-- 7. Vérifier les résultats
SELECT 
    'Badges attribués après test' as section,
    u.email,
    b.name as badge_name,
    b.criteria_type,
    b.criteria_value,
    FROM_UNIXTIME(ub.earned_at/1000) as earned_date
FROM user_badges ub
JOIN users u ON ub.user_id = u.id
JOIN badges b ON ub.badge_id = b.id
WHERE b.criteria_type IN ('FIRST_COURSE', 'CHALLENGE_COMPLETED')
AND u.id = @test_user
ORDER BY ub.earned_at DESC;

-- 8. Vérifier les notifications créées
SELECT 
    'Notifications de badge créées' as section,
    u.email,
    b.name as badge_name,
    bn.is_read,
    bn.is_new,
    FROM_UNIXTIME(bn.created_at/1000) as created_date
FROM badge_notifications bn
JOIN users u ON bn.user_id = u.id
JOIN badges b ON bn.badge_id = b.id
WHERE u.id = @test_user
ORDER BY bn.created_at DESC
LIMIT 5;

-- 9. Instructions finales
SELECT CONCAT('
🧪 TEST TERMINÉ !

Utilisateur test: ', COALESCE((SELECT email FROM users WHERE id = @test_user), 'Aucun trouvé'), '
Défi test: ', COALESCE((SELECT name FROM challenges WHERE id = @test_challenge), 'Aucun créé'), '

PROCHAINES ÉTAPES:
1. Redémarrez le backend Java pour appliquer les corrections du code
2. Connectez-vous avec le compte test dans l\'application
3. Terminez un nouveau cours ou défi
4. Vérifiez que les badges sont maintenant attribués automatiquement

VÉRIFICATION MANUELLE:
- Les badges "Étudiant Assidu" et "Premier Défi" devraient être visibles
- Ils ne devraient plus être verrouillés
- Les notifications devraient apparaître dans l\'interface

Si les badges restent verrouillés:
1. Vérifiez les logs du backend pour les erreurs
2. Exécutez debug_badges_first_course.sql pour plus de détails
3. Vérifiez que les méthodes onCourseCompleted et onChallengeCompleted sont bien appelées
') as instructions;