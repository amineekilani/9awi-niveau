-- Script pour créer une notification de niveau de test
-- REMPLACEZ 'votre.email@example.com' par votre email réel

-- 1. Trouver votre utilisateur
SET @user_email = 'votre.email@example.com';  -- REMPLACEZ PAR VOTRE EMAIL
SET @user_id = (SELECT id FROM users WHERE email = @user_email LIMIT 1);

-- Vérifier que l'utilisateur existe
SELECT 
    CASE 
        WHEN @user_id IS NOT NULL THEN CONCAT('✅ Utilisateur trouvé: ID ', @user_id)
        ELSE '❌ Utilisateur non trouvé ! Vérifiez l\'email.'
    END as status;

-- 2. Vérifier l'état actuel
SELECT 
    u.email,
    ux.total_xp,
    ux.current_level,
    ux.xp_to_next_level
FROM users u
LEFT JOIN user_xp ux ON u.id = ux.user_id
WHERE u.id = @user_id;

-- 3. Créer une notification de niveau de test (seulement si l'utilisateur existe)
INSERT INTO level_notifications (
    user_id, 
    old_level, 
    new_level, 
    level_name, 
    total_xp, 
    xp_gained, 
    is_read, 
    is_new, 
    created_at
)
SELECT 
    @user_id,
    1,  -- Ancien niveau
    2,  -- Nouveau niveau
    'Apprenti',  -- Nom du niveau
    150,  -- Total XP
    100,  -- XP gagnés
    FALSE,  -- Non lu
    TRUE,   -- Nouveau
    UNIX_TIMESTAMP() * 1000  -- Timestamp actuel
WHERE @user_id IS NOT NULL;

-- 4. Vérifier que la notification a été créée
SELECT 
    CASE 
        WHEN ROW_COUNT() > 0 THEN '✅ Notification de niveau créée avec succès !'
        ELSE '❌ Échec de la création de la notification'
    END as result;

-- 5. Afficher la notification créée
SELECT 
    ln.id,
    u.email,
    CONCAT('Niveau ', ln.old_level, ' → ', ln.new_level) as level_change,
    ln.level_name,
    ln.total_xp,
    ln.xp_gained,
    ln.is_read,
    ln.is_new,
    FROM_UNIXTIME(ln.created_at/1000) as created_date
FROM level_notifications ln
JOIN users u ON ln.user_id = u.id
WHERE ln.user_id = @user_id
ORDER BY ln.created_at DESC
LIMIT 1;

-- 6. Instructions pour tester
SELECT '
🧪 INSTRUCTIONS POUR TESTER :

1. Exécutez ce script en remplaçant votre email
2. Allez sur la page d\'accueil de l\'application
3. Cliquez sur le bouton "🧪 Tester les notifications"
4. Vous devriez voir une popup SweetAlert de montée de niveau !

Si ça ne marche pas :
- Vérifiez la console du navigateur (F12)
- Assurez-vous que le backend est démarré
- Vérifiez que la table level_notifications existe

' as instructions;