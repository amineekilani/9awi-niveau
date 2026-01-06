-- Diagnostic des badges "Premier Cours" et "Premier Défi"
-- Ce script aide à comprendre pourquoi ces badges ne sont pas attribués

-- 1. Vérifier les badges existants avec leurs critères
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

-- 2. Vérifier un utilisateur test et ses cours terminés
SELECT 
    'Utilisateur test et ses enrollments' as section,
    u.email,
    COUNT(e.id) as total_enrollments,
    COUNT(CASE WHEN e.progress >= 100 THEN 1 END) as cours_termines
FROM users u
LEFT JOIN enrollments e ON u.id = e.user_id
WHERE u.email LIKE '%test%' OR u.email LIKE '%admin%' OR u.role = 'APPRENANT'
GROUP BY u.id, u.email
ORDER BY cours_termines DESC
LIMIT 5;

-- 3. Vérifier les badges déjà attribués à cet utilisateur
SELECT 
    'Badges attribués à l\'utilisateur test' as section,
    u.email,
    b.name as badge_name,
    b.criteria_type,
    b.criteria_value,
    FROM_UNIXTIME(ub.earned_at/1000) as earned_date
FROM user_badges ub
JOIN users u ON ub.user_id = u.id
JOIN badges b ON ub.badge_id = b.id
WHERE (u.email LIKE '%test%' OR u.email LIKE '%admin%' OR u.role = 'APPRENANT')
ORDER BY ub.earned_at DESC;

-- 4. Vérifier les défis terminés
SELECT 
    'Défis terminés par l\'utilisateur test' as section,
    u.email,
    c.name as challenge_name,
    uc.is_completed,
    uc.current_progress,
    c.target_value,
    FROM_UNIXTIME(uc.completed_at/1000) as completed_date
FROM user_challenges uc
JOIN users u ON uc.user_id = u.id
JOIN challenges c ON uc.challenge_id = c.id
WHERE (u.email LIKE '%test%' OR u.email LIKE '%admin%' OR u.role = 'APPRENANT')
AND uc.is_completed = TRUE
ORDER BY uc.completed_at DESC;

-- 5. Diagnostic des problèmes potentiels
SELECT 
    'Diagnostic' as section,
    CASE 
        WHEN (SELECT COUNT(*) FROM badges WHERE criteria_type = 'FIRST_COURSE' AND is_active = TRUE) = 0 
        THEN '❌ Aucun badge FIRST_COURSE actif trouvé'
        
        WHEN (SELECT COUNT(*) FROM badges WHERE criteria_type = 'CHALLENGE_COMPLETED' AND is_active = TRUE) = 0 
        THEN '❌ Aucun badge CHALLENGE_COMPLETED actif trouvé'
        
        WHEN (SELECT COUNT(*) FROM enrollments WHERE progress >= 100) = 0 
        THEN '❌ Aucun cours terminé trouvé'
        
        WHEN (SELECT COUNT(*) FROM user_challenges WHERE is_completed = TRUE) = 0 
        THEN '❌ Aucun défi terminé trouvé'
        
        ELSE '✅ Données de base présentes - Problème probablement dans la logique du code'
    END as diagnostic;

-- 6. Vérifier si les badges existent et sont actifs
SELECT 
    'Vérification badges FIRST_COURSE' as check_type,
    COUNT(*) as count,
    GROUP_CONCAT(name) as badge_names
FROM badges 
WHERE criteria_type = 'FIRST_COURSE' AND is_active = TRUE;

SELECT 
    'Vérification badges CHALLENGE_COMPLETED' as check_type,
    COUNT(*) as count,
    GROUP_CONCAT(name) as badge_names
FROM badges 
WHERE criteria_type = 'CHALLENGE_COMPLETED' AND is_active = TRUE;

-- 7. Instructions de correction
SELECT '
🔍 DIAGNOSTIC DES BADGES:

PROBLÈMES POSSIBLES:
1. ❌ Badge FIRST_COURSE n\'existe pas ou n\'est pas actif
2. ❌ Badge CHALLENGE_COMPLETED n\'existe pas ou n\'est pas actif  
3. ❌ La méthode onCourseCompleted() ne vérifie pas le badge FIRST_COURSE
4. ❌ La méthode onChallengeCompleted() ne vérifie pas le badge CHALLENGE_COMPLETED
5. ❌ Les critères des badges ne correspondent pas aux valeurs attendues

SOLUTIONS:
1. Créer les badges manquants avec les bons critères
2. Modifier le code Java pour vérifier ces badges
3. Tester manuellement l\'attribution des badges

PROCHAINES ÉTAPES:
1. Exécutez ce script pour identifier le problème
2. Si les badges manquent, créez-les
3. Si le code ne les vérifie pas, modifiez GamificationService.java
' as instructions;