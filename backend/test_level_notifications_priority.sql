-- Test des notifications de niveau avec priorité
-- Ce script teste que les notifications de niveau ne sont pas écrasées par les autres

-- 1. Vérifier l'état actuel d'un utilisateur test
SELECT 
    'État actuel utilisateur test' as section,
    u.email,
    ux.total_xp,
    ux.current_level,
    ux.xp_to_next_level
FROM users u
JOIN user_xp ux ON u.id = ux.user_id
WHERE u.email LIKE '%test%' OR u.email LIKE '%admin%'
ORDER BY ux.total_xp DESC
LIMIT 1;

-- 2. Créer une situation de montée de niveau
-- Augmenter les XP pour déclencher une montée de niveau
UPDATE user_xp 
SET total_xp = total_xp + 500
WHERE user_id = (
    SELECT u.id FROM users u 
    WHERE u.email LIKE '%test%' OR u.email LIKE '%admin%'
    ORDER BY u.id 
    LIMIT 1
);

-- 3. Vérifier les niveaux disponibles
SELECT 
    'Niveaux disponibles' as section,
    level,
    xp_required,
    name,
    description
FROM levels
ORDER BY level;

-- 4. Créer aussi un badge pour tester la priorité
-- Vérifier s'il y a des badges disponibles
SELECT 
    'Badges disponibles' as section,
    id,
    name,
    criteria_type,
    criteria_value,
    is_active
FROM badges
WHERE is_active = TRUE
ORDER BY criteria_value
LIMIT 3;

-- 5. Attribuer un badge à l'utilisateur test (si pas déjà attribué)
INSERT IGNORE INTO user_badges (user_id, badge_id, earned_at)
SELECT 
    u.id,
    b.id,
    UNIX_TIMESTAMP() * 1000
FROM users u
CROSS JOIN badges b
WHERE (u.email LIKE '%test%' OR u.email LIKE '%admin%')
AND b.is_active = TRUE
AND b.criteria_type = 'FIRST_COURSE'
AND NOT EXISTS (
    SELECT 1 FROM user_badges ub 
    WHERE ub.user_id = u.id AND ub.badge_id = b.id
)
ORDER BY u.id, b.id
LIMIT 1;

-- 6. Créer une notification de badge (pour tester la priorité)
INSERT IGNORE INTO badge_notifications (user_id, badge_id, is_read, is_new, created_at)
SELECT 
    ub.user_id,
    ub.badge_id,
    FALSE,
    TRUE,
    UNIX_TIMESTAMP() * 1000
FROM user_badges ub
JOIN users u ON ub.user_id = u.id
WHERE (u.email LIKE '%test%' OR u.email LIKE '%admin%')
AND NOT EXISTS (
    SELECT 1 FROM badge_notifications bn 
    WHERE bn.user_id = ub.user_id AND bn.badge_id = ub.badge_id
)
ORDER BY ub.earned_at DESC
LIMIT 1;

-- 7. Vérifier les notifications créées
SELECT 
    'Notifications de niveau' as type,
    ln.id,
    u.email,
    ln.old_level,
    ln.new_level,
    ln.level_name,
    ln.total_xp,
    ln.xp_gained,
    ln.is_read,
    ln.is_new,
    FROM_UNIXTIME(ln.created_at/1000) as created_date
FROM level_notifications ln
JOIN users u ON ln.user_id = u.id
WHERE (u.email LIKE '%test%' OR u.email LIKE '%admin%')
ORDER BY ln.created_at DESC
LIMIT 3;

SELECT 
    'Notifications de badge' as type,
    bn.id,
    u.email,
    b.name as badge_name,
    bn.is_read,
    bn.is_new,
    FROM_UNIXTIME(bn.created_at/1000) as created_date
FROM badge_notifications bn
JOIN users u ON bn.user_id = u.id
JOIN badges b ON bn.badge_id = b.id
WHERE (u.email LIKE '%test%' OR u.email LIKE '%admin%')
ORDER BY bn.created_at DESC
LIMIT 3;

-- 8. Instructions pour tester
SELECT '
🧪 INSTRUCTIONS DE TEST:

1. Exécutez ce script pour créer des notifications de niveau ET de badge
2. Connectez-vous avec le compte test dans l\'application
3. Allez sur la page d\'accueil ou actualisez
4. Vérifiez que:
   ✅ La notification de NIVEAU apparaît EN PREMIER (style orange/jaune)
   ✅ La notification de badge apparaît APRÈS (style bleu)
   ✅ Les notifications apparaissent dans la barre de navigation
   ✅ Le compteur de notifications est correct

5. Si les notifications n\'apparaissent pas:
   - Vérifiez la console du navigateur pour les erreurs
   - Vérifiez que les tables level_notifications et badge_notifications existent
   - Vérifiez que le service GamificationNotificationService est bien appelé

6. Pour réinitialiser le test:
   DELETE FROM level_notifications WHERE user_id IN (SELECT id FROM users WHERE email LIKE "%test%");
   DELETE FROM badge_notifications WHERE user_id IN (SELECT id FROM users WHERE email LIKE "%test%");
' as instructions;