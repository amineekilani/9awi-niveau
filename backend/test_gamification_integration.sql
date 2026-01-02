-- Test de l'intégration gamification
-- Vérifier que les tables existent et sont correctement liées

-- 1. Vérifier les tables de gamification
SELECT 'Tables de gamification:' as test;
SHOW TABLES LIKE '%badge%';
SHOW TABLES LIKE '%level%';
SHOW TABLES LIKE '%user_xp%';
SHOW TABLES LIKE '%challenge%';

-- 2. Vérifier les données par défaut
SELECT 'Niveaux par défaut:' as test;
SELECT level, xp_required, name FROM levels ORDER BY level;

SELECT 'Badges par défaut:' as test;
SELECT name, criteria_type, criteria_value, is_active FROM badges;

-- 3. Vérifier les utilisateurs avec XP
SELECT 'Utilisateurs avec XP:' as test;
SELECT u.email, ux.total_xp, ux.current_level 
FROM users u 
LEFT JOIN user_xp ux ON u.id = ux.user_id 
WHERE u.archived = false;

-- 4. Vérifier les badges obtenus
SELECT 'Badges obtenus:' as test;
SELECT u.email, b.name, ub.earned_at
FROM user_badges ub
JOIN users u ON ub.user_id = u.id
JOIN badges b ON ub.badge_id = b.id
ORDER BY ub.earned_at DESC;

-- 5. Vérifier les résultats de quiz récents
SELECT 'Quiz récents (pour test gamification):' as test;
SELECT u.email, q.titre, rq.score, rq.date_passed
FROM resultat_quiz rq
JOIN users u ON rq.user_id = u.id
JOIN quiz q ON rq.quiz_id = q.id
ORDER BY rq.date_passed DESC
LIMIT 10;