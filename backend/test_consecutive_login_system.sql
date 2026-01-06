-- Script de test pour le système de connexions consécutives
-- Ce script permet de tester le badge "Marathonien" (7 jours consécutifs)

-- 1. Vérifier que le badge "Marathonien" existe
SELECT * FROM badges WHERE nom = 'Marathonien' AND criteria_type = 'STREAK_DAYS';

-- 2. Vérifier les connexions d'un utilisateur (remplacer 1 par l'ID utilisateur à tester)
SELECT 
    u.email,
    ul.login_time,
    FROM_UNIXTIME(ul.login_time / 1000) as login_datetime,
    DATE(FROM_UNIXTIME(ul.login_time / 1000)) as login_date
FROM user_logins ul
JOIN users u ON ul.user_id = u.id
WHERE u.id = 1  -- Remplacer par l'ID utilisateur à tester
ORDER BY ul.login_time DESC
LIMIT 30;

-- 3. Compter les jours distincts de connexion pour un utilisateur (30 derniers jours)
SELECT 
    u.email,
    COUNT(DISTINCT DATE(FROM_UNIXTIME(ul.login_time / 1000))) as jours_distincts_connexion
FROM user_logins ul
JOIN users u ON ul.user_id = u.id
WHERE u.id = 1  -- Remplacer par l'ID utilisateur à tester
    AND ul.login_time >= (UNIX_TIMESTAMP() - 30 * 24 * 60 * 60) * 1000
GROUP BY u.id, u.email;

-- 4. Vérifier si l'utilisateur a obtenu le badge "Marathonien"
SELECT 
    u.email,
    b.nom as badge_nom,
    b.description,
    ub.earned_at
FROM user_badges ub
JOIN users u ON ub.user_id = u.id
JOIN badges b ON ub.badge_id = b.id
WHERE u.id = 1  -- Remplacer par l'ID utilisateur à tester
    AND b.nom = 'Marathonien';

-- 5. Simuler des connexions consécutives pour tester (ATTENTION: à utiliser avec précaution)
-- Décommentez les lignes suivantes pour créer des connexions de test

/*
-- Créer des connexions pour les 7 derniers jours (y compris aujourd'hui)
INSERT INTO user_logins (user_id, login_time, ip_address, user_agent) VALUES
(1, UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 6 DAY)) * 1000, '127.0.0.1', 'Test Browser'),
(1, UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 5 DAY)) * 1000, '127.0.0.1', 'Test Browser'),
(1, UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 4 DAY)) * 1000, '127.0.0.1', 'Test Browser'),
(1, UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 3 DAY)) * 1000, '127.0.0.1', 'Test Browser'),
(1, UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 2 DAY)) * 1000, '127.0.0.1', 'Test Browser'),
(1, UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 DAY)) * 1000, '127.0.0.1', 'Test Browser'),
(1, UNIX_TIMESTAMP(NOW()) * 1000, '127.0.0.1', 'Test Browser');
*/

-- 6. Vérifier les XP gagnés par connexions
SELECT 
    u.email,
    ux.points,
    ux.reason,
    ux.earned_at
FROM user_xp ux
JOIN users u ON ux.user_id = u.id
WHERE u.id = 1  -- Remplacer par l'ID utilisateur à tester
    AND (ux.reason LIKE '%connexion%' OR ux.reason LIKE '%Bonus connexion%')
ORDER BY ux.earned_at DESC
LIMIT 10;

-- 7. Statistiques globales des connexions consécutives
SELECT 
    'Utilisateurs avec badge Marathonien' as statistique,
    COUNT(*) as nombre
FROM user_badges ub
JOIN badges b ON ub.badge_id = b.id
WHERE b.nom = 'Marathonien'

UNION ALL

SELECT 
    'Total connexions enregistrées' as statistique,
    COUNT(*) as nombre
FROM user_logins

UNION ALL

SELECT 
    'Utilisateurs actifs (30 derniers jours)' as statistique,
    COUNT(DISTINCT user_id) as nombre
FROM user_logins
WHERE login_time >= (UNIX_TIMESTAMP() - 30 * 24 * 60 * 60) * 1000;