-- Script pour créer des données de test pour le classement
-- Exécuter ce script si la table user_xp est vide

-- 1. Vérifier s'il y a des utilisateurs actifs
SELECT 'Utilisateurs actifs:' as info, COUNT(*) as count FROM users WHERE archived = false;

-- 2. Vérifier s'il y a des données UserXP
SELECT 'Données UserXP existantes:' as info, COUNT(*) as count FROM user_xp;

-- 3. Créer des données UserXP pour tous les utilisateurs actifs qui n'en ont pas
INSERT INTO user_xp (user_id, total_xp, current_level, xp_to_next_level, last_updated)
SELECT 
    u.id,
    FLOOR(RAND() * 1000) + 100 as total_xp,  -- XP aléatoire entre 100 et 1100
    CASE 
        WHEN FLOOR(RAND() * 1000) + 100 < 200 THEN 1
        WHEN FLOOR(RAND() * 1000) + 100 < 400 THEN 2
        WHEN FLOOR(RAND() * 1000) + 100 < 600 THEN 3
        WHEN FLOOR(RAND() * 1000) + 100 < 800 THEN 4
        ELSE 5
    END as current_level,
    100 as xp_to_next_level,
    UNIX_TIMESTAMP() * 1000 as last_updated
FROM users u
WHERE u.archived = false 
AND u.id NOT IN (SELECT DISTINCT user_id FROM user_xp WHERE user_id IS NOT NULL);

-- 4. Vérifier les résultats
SELECT 'Données UserXP après insertion:' as info, COUNT(*) as count FROM user_xp;

-- 5. Afficher le classement
SELECT 
    u.first_name,
    u.last_name,
    u.email,
    ux.total_xp,
    ux.current_level
FROM user_xp ux
JOIN users u ON ux.user_id = u.id
WHERE u.archived = false
ORDER BY ux.total_xp DESC
LIMIT 10;