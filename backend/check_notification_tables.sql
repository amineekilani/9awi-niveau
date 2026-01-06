-- Vérifier si toutes les tables de notifications existent

-- 1. Vérifier les tables existantes
SELECT 
    TABLE_NAME,
    TABLE_ROWS
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME LIKE '%notification%'
ORDER BY TABLE_NAME;

-- 2. Vérifier spécifiquement les nouvelles tables
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ Table level_notifications existe'
        ELSE '❌ Table level_notifications manquante'
    END as level_notifications_status
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'level_notifications';

SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ Table badge_notifications existe'
        ELSE '❌ Table badge_notifications manquante'
    END as badge_notifications_status
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'badge_notifications';

SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ Table challenge_notifications existe'
        ELSE '❌ Table challenge_notifications manquante'
    END as challenge_notifications_status
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'challenge_notifications';

-- 3. Si les tables existent, compter les notifications
SELECT 'Notifications de niveau' as type, COUNT(*) as count FROM level_notifications
UNION ALL
SELECT 'Notifications de badge' as type, COUNT(*) as count FROM badge_notifications
UNION ALL  
SELECT 'Notifications de défi' as type, COUNT(*) as count FROM challenge_notifications;