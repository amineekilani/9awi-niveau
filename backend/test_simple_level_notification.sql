-- Test simple pour créer une notification de niveau
-- Utilisez ce script pour tester rapidement les notifications de niveau

-- 1. Trouver un utilisateur test
SELECT 
    'Utilisateur test sélectionné' as info,
    u.id,
    u.email,
    ux.total_xp,
    ux.current_level
FROM users u
LEFT JOIN user_xp ux ON u.id = ux.user_id
WHERE u.email LIKE '%test%' OR u.email LIKE '%admin%' OR u.role = 'APPRENANT'
ORDER BY u.id
LIMIT 1;

-- 2. Créer une notification de niveau manuellement pour test
-- (Remplacez USER_ID par l'ID de l'utilisateur trouvé ci-dessus)
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
) VALUES (
    (SELECT u.id FROM users u WHERE u.email LIKE '%test%' OR u.email LIKE '%admin%' OR u.role = 'APPRENANT' ORDER BY u.id LIMIT 1),
    1,  -- ancien niveau
    2,  -- nouveau niveau
    'Explorateur', -- nom du niveau
    150, -- total XP
    50,  -- XP gagnés
    FALSE, -- non lu
    TRUE,  -- nouveau
    UNIX_TIMESTAMP() * 1000 -- timestamp actuel
);

-- 3. Vérifier que la notification a été créée
SELECT 
    'Notification créée' as info,
    ln.id,
    u.email,
    ln.old_level,
    ln.new_level,
    ln.level_name,
    ln.is_new,
    FROM_UNIXTIME(ln.created_at/1000) as created_date
FROM level_notifications ln
JOIN users u ON ln.user_id = u.id
ORDER BY ln.created_at DESC
LIMIT 1;

-- 4. Instructions
SELECT '
🧪 TEST SIMPLE:

1. Exécutez ce script
2. Connectez-vous avec le compte test dans l\'application
3. Allez sur la page d\'accueil
4. Vous devriez voir une alerte orange "MONTÉE DE NIVEAU !"

Si ça ne marche pas:
- Vérifiez la console du navigateur
- Vérifiez que la table level_notifications existe
- Vérifiez les logs du backend

Pour nettoyer après le test:
DELETE FROM level_notifications WHERE level_name = "Explorateur";
' as instructions;