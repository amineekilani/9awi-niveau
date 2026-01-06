-- Vérifier si la table level_notifications existe
SELECT 
    TABLE_NAME,
    TABLE_SCHEMA,
    ENGINE,
    TABLE_ROWS
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'level_notifications';

-- Si la table existe, afficher sa structure
DESCRIBE level_notifications;

-- Compter les notifications existantes
SELECT COUNT(*) as total_notifications FROM level_notifications;