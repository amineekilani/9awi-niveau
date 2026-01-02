-- Test rapide pour le classement
-- Vérifier et créer des données de test

-- 1. Vérifier les utilisateurs actifs
SELECT 'Utilisateurs actifs' as info, COUNT(*) as count FROM users WHERE archived = false;

-- 2. Vérifier les données UserXP existantes
SELECT 'UserXP existants' as info, COUNT(*) as count FROM user_xp;

-- 3. Si pas de UserXP, créer pour l'admin
INSERT IGNORE INTO user_xp (user_id, total_xp, current_level, xp_to_next_level, last_updated)
SELECT 
    id,
    500,  -- 500 XP
    3,    -- Niveau 3
    100,  -- XP pour niveau suivant
    UNIX_TIMESTAMP() * 1000
FROM users 
WHERE role = 'ADMIN' AND archived = false
LIMIT 1;

-- 4. Vérifier le résultat
SELECT 
    'Classement test' as info,
    u.first_name,
    u.last_name,
    ux.total_xp,
    ux.current_level
FROM user_xp ux
JOIN users u ON ux.user_id = u.id
WHERE u.archived = false
ORDER BY ux.total_xp DESC
LIMIT 5;