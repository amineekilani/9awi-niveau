-- Vérification des tables de notifications
-- Ce script vérifie que toutes les tables de notifications existent et sont correctement configurées

-- 1. Vérifier l'existence des tables
SELECT 
    'Tables de notifications existantes' as section,
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    UPDATE_TIME
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME LIKE '%notification%'
ORDER BY TABLE_NAME;

-- 2. Vérifier la structure de level_notifications
DESCRIBE level_notifications;

-- 3. Vérifier la structure de badge_notifications
DESCRIBE badge_notifications;

-- 4. Vérifier la structure de challenge_notifications
DESCRIBE challenge_notifications;

-- 5. Compter les notifications par type
SELECT 
    'Notifications de niveau' as type,
    COUNT(*) as total,
    COUNT(CASE WHEN is_read = FALSE THEN 1 END) as non_lues,
    COUNT(CASE WHEN is_new = TRUE THEN 1 END) as nouvelles
FROM level_notifications
WHERE EXISTS (SELECT 1 FROM level_notifications LIMIT 1)

UNION ALL

SELECT 
    'Notifications de badge' as type,
    COUNT(*) as total,
    COUNT(CASE WHEN is_read = FALSE THEN 1 END) as non_lues,
    COUNT(CASE WHEN is_new = TRUE THEN 1 END) as nouvelles
FROM badge_notifications
WHERE EXISTS (SELECT 1 FROM badge_notifications LIMIT 1)

UNION ALL

SELECT 
    'Notifications de défi' as type,
    COUNT(*) as total,
    COUNT(CASE WHEN is_read = FALSE THEN 1 END) as non_lues,
    COUNT(CASE WHEN is_new = TRUE THEN 1 END) as nouvelles
FROM challenge_notifications
WHERE EXISTS (SELECT 1 FROM challenge_notifications LIMIT 1);

-- 6. Vérifier les contraintes de clés étrangères
SELECT 
    'Contraintes de clés étrangères' as section,
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME LIKE '%notification%'
AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, CONSTRAINT_NAME;

-- 7. Vérifier les index
SELECT 
    'Index des tables de notifications' as section,
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME LIKE '%notification%'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- 8. Échantillon de données récentes
SELECT 
    'Échantillon notifications de niveau récentes' as section,
    ln.id,
    u.email,
    ln.old_level,
    ln.new_level,
    ln.is_read,
    ln.is_new,
    FROM_UNIXTIME(ln.created_at/1000) as created_date
FROM level_notifications ln
JOIN users u ON ln.user_id = u.id
ORDER BY ln.created_at DESC
LIMIT 5;

SELECT 
    'Échantillon notifications de badge récentes' as section,
    bn.id,
    u.email,
    b.name as badge_name,
    bn.is_read,
    bn.is_new,
    FROM_UNIXTIME(bn.created_at/1000) as created_date
FROM badge_notifications bn
JOIN users u ON bn.user_id = u.id
JOIN badges b ON bn.badge_id = b.id
ORDER BY bn.created_at DESC
LIMIT 5;

-- 9. Diagnostic des problèmes potentiels
SELECT '
🔍 DIAGNOSTIC DES NOTIFICATIONS:

✅ Si toutes les tables existent et ont des données, le système fonctionne
❌ Si une table manque, exécutez le script create_and_migrate_notifications.sql
⚠️  Si les contraintes FK manquent, il peut y avoir des erreurs d\'intégrité
🔧 Si les index manquent, les performances peuvent être dégradées

PROCHAINES ÉTAPES:
1. Si tout est OK, testez avec test_level_notifications_priority.sql
2. Si des tables manquent, exécutez create_and_migrate_notifications.sql
3. Vérifiez les logs du backend pour les erreurs de création de notifications
' as diagnostic;