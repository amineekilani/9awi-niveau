-- Script de debug pour les notifications de niveau

-- 1. Vérifier que la table existe
SELECT 'Vérification de la table level_notifications' as step;
SHOW TABLES LIKE 'level_notifications';

-- 2. Vérifier la structure de la table
SELECT 'Structure de la table' as step;
DESCRIBE level_notifications;

-- 3. Compter toutes les notifications
SELECT 
    'Total des notifications de niveau' as metric,
    COUNT(*) as count
FROM level_notifications;

-- 4. Afficher toutes les notifications avec détails utilisateur
SELECT 
    'Toutes les notifications existantes' as step,
    ln.id,
    u.email,
    CONCAT('Niveau ', ln.old_level, ' → ', ln.new_level) as level_change,
    ln.level_name,
    ln.total_xp,
    ln.xp_gained,
    CASE WHEN ln.is_read THEN '✅ Lu' ELSE '❌ Non lu' END as read_status,
    CASE WHEN ln.is_new THEN '🆕 Nouveau' ELSE '👁️ Vu' END as new_status,
    FROM_UNIXTIME(ln.created_at/1000) as created_date
FROM level_notifications ln
JOIN users u ON ln.user_id = u.id
ORDER BY ln.created_at DESC;

-- 5. Statistiques par utilisateur
SELECT 
    'Statistiques par utilisateur' as step,
    u.email,
    COUNT(ln.id) as total_notifications,
    SUM(CASE WHEN ln.is_read = FALSE THEN 1 ELSE 0 END) as unread_count,
    SUM(CASE WHEN ln.is_new = TRUE THEN 1 ELSE 0 END) as new_count,
    MAX(FROM_UNIXTIME(ln.created_at/1000)) as last_notification
FROM users u
LEFT JOIN level_notifications ln ON u.id = ln.user_id
WHERE u.role = 'APPRENANT'
GROUP BY u.id, u.email
HAVING COUNT(ln.id) > 0
ORDER BY total_notifications DESC;

-- 6. Vérifier les niveaux disponibles
SELECT 
    'Niveaux configurés' as step,
    level,
    xp_required,
    name,
    description
FROM levels
ORDER BY level;

-- 7. Vérifier les profils XP des utilisateurs
SELECT 
    'Profils XP des utilisateurs' as step,
    u.email,
    ux.total_xp,
    ux.current_level,
    ux.xp_to_next_level,
    FROM_UNIXTIME(ux.last_updated/1000) as last_updated
FROM users u
LEFT JOIN user_xp ux ON u.id = ux.user_id
WHERE u.role = 'APPRENANT'
ORDER BY ux.total_xp DESC
LIMIT 10;

-- 8. Recommandations de debug
SELECT '
🔍 GUIDE DE DEBUG :

1. Si aucune notification n\'existe :
   - Exécutez create_test_level_notification.sql
   - Ou gagnez des XP dans l\'application

2. Si les notifications existent mais pas de popup :
   - Vérifiez la console du navigateur (F12)
   - Assurez-vous que SweetAlert2 est installé
   - Vérifiez que le service GamificationNotificationService fonctionne

3. Si les popups ne se déclenchent pas automatiquement :
   - Utilisez le bouton "🧪 Tester les notifications" sur la page d\'accueil
   - Vérifiez que l\'auto-refresh fonctionne dans la navbar

4. Pour forcer une montée de niveau réelle :
   - Utilisez l\'endpoint /api/test/level/add-xp/100
   - Ou terminez un quiz/cours pour gagner des XP

' as debug_guide;