-- Correction des badges manquants "Premier Cours" et "Premier Défi"
-- Ce script crée les badges s'ils n'existent pas déjà

-- 1. Vérifier les badges existants
SELECT 
    'Badges existants avant correction' as section,
    id,
    name,
    criteria_type,
    criteria_value,
    is_active
FROM badges
WHERE criteria_type IN ('FIRST_COURSE', 'CHALLENGE_COMPLETED')
ORDER BY criteria_type, criteria_value;

-- 2. Créer le badge "Étudiant Assidu" (Premier Cours) s'il n'existe pas
INSERT IGNORE INTO badges (
    name,
    description,
    icon_url,
    criteria_type,
    criteria_value,
    is_active,
    created_at,
    updated_at
) VALUES (
    'Étudiant Assidu',
    'Terminer votre premier cours',
    'first-course.svg',
    'FIRST_COURSE',
    1,
    TRUE,
    UNIX_TIMESTAMP() * 1000,
    UNIX_TIMESTAMP() * 1000
);

-- 3. Créer le badge "Premier Défi" s'il n'existe pas
INSERT IGNORE INTO badges (
    name,
    description,
    icon_url,
    criteria_type,
    criteria_value,
    is_active,
    created_at,
    updated_at
) VALUES (
    'Premier Défi',
    'Terminer votre premier défi',
    'challenge-master.svg',
    'CHALLENGE_COMPLETED',
    1,
    TRUE,
    UNIX_TIMESTAMP() * 1000,
    UNIX_TIMESTAMP() * 1000
);

-- 4. Vérifier les badges après création
SELECT 
    'Badges après correction' as section,
    id,
    name,
    description,
    criteria_type,
    criteria_value,
    is_active
FROM badges
WHERE criteria_type IN ('FIRST_COURSE', 'CHALLENGE_COMPLETED')
ORDER BY criteria_type, criteria_value;

-- 5. Tester l'attribution manuelle pour un utilisateur test
-- (Optionnel - pour tester immédiatement)

-- Trouver un utilisateur qui a terminé au moins un cours
SET @test_user_id = (
    SELECT u.id 
    FROM users u
    JOIN enrollments e ON u.id = e.user_id
    WHERE e.progress >= 100
    AND (u.email LIKE '%test%' OR u.email LIKE '%admin%' OR u.role = 'APPRENANT')
    ORDER BY u.id
    LIMIT 1
);

-- Attribuer le badge "Premier Cours" si l'utilisateur n'en a pas
INSERT IGNORE INTO user_badges (user_id, badge_id, earned_at)
SELECT 
    @test_user_id,
    b.id,
    UNIX_TIMESTAMP() * 1000
FROM badges b
WHERE b.criteria_type = 'FIRST_COURSE'
AND b.criteria_value = 1
AND b.is_active = TRUE
AND @test_user_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM user_badges ub 
    WHERE ub.user_id = @test_user_id AND ub.badge_id = b.id
);

-- Trouver un utilisateur qui a terminé au moins un défi
SET @test_user_challenge_id = (
    SELECT u.id 
    FROM users u
    JOIN user_challenges uc ON u.id = uc.user_id
    WHERE uc.is_completed = TRUE
    AND (u.email LIKE '%test%' OR u.email LIKE '%admin%' OR u.role = 'APPRENANT')
    ORDER BY u.id
    LIMIT 1
);

-- Attribuer le badge "Premier Défi" si l'utilisateur n'en a pas
INSERT IGNORE INTO user_badges (user_id, badge_id, earned_at)
SELECT 
    @test_user_challenge_id,
    b.id,
    UNIX_TIMESTAMP() * 1000
FROM badges b
WHERE b.criteria_type = 'CHALLENGE_COMPLETED'
AND b.criteria_value = 1
AND b.is_active = TRUE
AND @test_user_challenge_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM user_badges ub 
    WHERE ub.user_id = @test_user_challenge_id AND ub.badge_id = b.id
);

-- 6. Vérifier les attributions
SELECT 
    'Badges attribués aux utilisateurs test' as section,
    u.email,
    b.name as badge_name,
    b.criteria_type,
    FROM_UNIXTIME(ub.earned_at/1000) as earned_date
FROM user_badges ub
JOIN users u ON ub.user_id = u.id
JOIN badges b ON ub.badge_id = b.id
WHERE b.criteria_type IN ('FIRST_COURSE', 'CHALLENGE_COMPLETED')
AND (u.email LIKE '%test%' OR u.email LIKE '%admin%' OR u.role = 'APPRENANT')
ORDER BY ub.earned_at DESC;

-- 7. Instructions
SELECT '
✅ CORRECTION TERMINÉE !

BADGES CRÉÉS:
- Étudiant Assidu (FIRST_COURSE, valeur: 1)
- Premier Défi (CHALLENGE_COMPLETED, valeur: 1)

PROCHAINES ÉTAPES:
1. Redémarrez le backend pour que les modifications du code Java prennent effet
2. Testez en terminant un cours ou un défi
3. Vérifiez que les badges sont maintenant attribués automatiquement

VÉRIFICATION:
- Les badges devraient maintenant apparaître quand vous terminez votre premier cours/défi
- Vérifiez les logs du backend pour voir les messages de debug
- Utilisez debug_badges_first_course.sql pour diagnostiquer si nécessaire
' as instructions;