-- Script de debug pour le classement
-- Vérifier les données dans les tables

-- 1. Vérifier les utilisateurs
SELECT 'USERS' as table_name, COUNT(*) as count FROM users WHERE archived = false;
SELECT id, email, first_name, last_name, role FROM users WHERE archived = false LIMIT 5;

-- 2. Vérifier les données UserXP
SELECT 'USER_XP' as table_name, COUNT(*) as count FROM user_xp;
SELECT ux.id, ux.user_id, u.email, ux.total_xp, ux.current_level 
FROM user_xp ux 
JOIN users u ON ux.user_id = u.id 
WHERE u.archived = false
ORDER BY ux.total_xp DESC 
LIMIT 10;

-- 3. Vérifier s'il y a des doublons dans user_xp
SELECT user_id, COUNT(*) as count 
FROM user_xp 
GROUP BY user_id 
HAVING COUNT(*) > 1;

-- 4. Vérifier les niveaux
SELECT 'LEVELS' as table_name, COUNT(*) as count FROM levels;
SELECT * FROM levels ORDER BY level LIMIT 5;

-- 5. Vérifier les badges utilisateur
SELECT 'USER_BADGES' as table_name, COUNT(*) as count FROM user_badges;

-- 6. Requête complète pour le classement (comme dans le code)
SELECT 
    ux.id,
    u.id as user_id,
    u.first_name,
    u.last_name,
    u.email,
    ux.total_xp,
    ux.current_level,
    l.name as level_name,
    (SELECT COUNT(*) FROM user_badges ub WHERE ub.user_id = u.id) as badges_count
FROM user_xp ux
JOIN users u ON ux.user_id = u.id
LEFT JOIN levels l ON l.level = ux.current_level
WHERE u.archived = false
ORDER BY ux.total_xp DESC
LIMIT 10;