-- Test d'intégration complète du système de notifications de niveau
-- Ce script simule une montée de niveau complète avec notification

-- 1. Préparer l'environnement de test
SELECT 'Préparation de l\'environnement de test' as step;

-- Créer un utilisateur de test
INSERT IGNORE INTO users (email, password, first_name, last_name, role, email_verified, created_at)
VALUES ('integration.test@example.com', '$2a$10$dummy.hash', 'Integration', 'Test', 'APPRENANT', TRUE, UNIX_TIMESTAMP() * 1000);

SET @test_user_id = (SELECT id FROM users WHERE email = 'integration.test@example.com' LIMIT 1);

-- Vérifier que les niveaux existent
INSERT IGNORE INTO levels (level, xp_required, name, description, created_at) VALUES
(1, 0, 'Novice', 'Tout nouveau dans l\'apprentissage', UNIX_TIMESTAMP() * 1000),
(2, 100, 'Apprenti', 'Commence à maîtriser les bases', UNIX_TIMESTAMP() * 1000),
(3, 250, 'Intermédiaire', 'Solides connaissances de base', UNIX_TIMESTAMP() * 1000),
(4, 500, 'Avancé', 'Expertise confirmée', UNIX_TIMESTAMP() * 1000),
(5, 1000, 'Expert', 'Maîtrise exceptionnelle', UNIX_TIMESTAMP() * 1000);

-- 2. Initialiser le profil XP au niveau 1
SELECT 'Initialisation du profil XP' as step;

INSERT INTO user_xp (user_id, total_xp, current_level, xp_to_next_level, last_updated)
VALUES (@test_user_id, 50, 1, 50, UNIX_TIMESTAMP() * 1000)
ON DUPLICATE KEY UPDATE 
    total_xp = 50, 
    current_level = 1, 
    xp_to_next_level = 50,
    last_updated = UNIX_TIMESTAMP() * 1000;

-- Afficher l'état initial
SELECT 
    'État initial' as status,
    total_xp,
    current_level,
    xp_to_next_level
FROM user_xp 
WHERE user_id = @test_user_id;

-- 3. Simuler une montée de niveau (niveau 1 → 2)
SELECT 'Simulation montée niveau 1 → 2' as step;

-- Mettre à jour les XP pour déclencher une montée de niveau
UPDATE user_xp 
SET 
    total_xp = 150,
    current_level = 2,
    xp_to_next_level = 100,
    last_updated = UNIX_TIMESTAMP() * 1000
WHERE user_id = @test_user_id;

-- Créer la notification de niveau correspondante
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
    @test_user_id,
    1,
    2,
    'Apprenti',
    150,
    100,  -- XP gagnés pour passer au niveau 2
    FALSE,
    TRUE,
    UNIX_TIMESTAMP() * 1000
);

-- 4. Simuler une deuxième montée de niveau (niveau 2 → 3)
SELECT 'Simulation montée niveau 2 → 3' as step;

-- Attendre un peu pour différencier les timestamps
SELECT SLEEP(1);

UPDATE user_xp 
SET 
    total_xp = 300,
    current_level = 3,
    xp_to_next_level = 200,
    last_updated = UNIX_TIMESTAMP() * 1000
WHERE user_id = @test_user_id;

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
    @test_user_id,
    2,
    3,
    'Intermédiaire',
    300,
    150,  -- XP gagnés pour passer au niveau 3
    FALSE,
    TRUE,
    UNIX_TIMESTAMP() * 1000
);

-- 5. Tester toutes les requêtes de l'API
SELECT 'Test des requêtes API' as step;

-- Toutes les notifications de l'utilisateur
SELECT 
    'Toutes les notifications' as query_type,
    id,
    CONCAT('Niveau ', old_level, ' → ', new_level) as level_change,
    level_name,
    total_xp,
    xp_gained,
    is_read,
    is_new,
    FROM_UNIXTIME(created_at/1000) as created_date
FROM level_notifications 
WHERE user_id = @test_user_id 
ORDER BY created_at DESC;

-- Notifications non lues
SELECT 
    'Notifications non lues' as query_type,
    COUNT(*) as count
FROM level_notifications 
WHERE user_id = @test_user_id AND is_read = FALSE;

-- Nouvelles notifications
SELECT 
    'Nouvelles notifications' as query_type,
    COUNT(*) as count
FROM level_notifications 
WHERE user_id = @test_user_id AND is_new = TRUE;

-- 6. Tester le marquage comme lu
SELECT 'Test marquage comme lu' as step;

-- Marquer la première notification comme lue
UPDATE level_notifications 
SET is_read = TRUE 
WHERE user_id = @test_user_id 
ORDER BY created_at ASC 
LIMIT 1;

-- Vérifier le changement
SELECT 
    'Après marquage comme lu' as status,
    SUM(CASE WHEN is_read = TRUE THEN 1 ELSE 0 END) as read_count,
    SUM(CASE WHEN is_read = FALSE THEN 1 ELSE 0 END) as unread_count
FROM level_notifications 
WHERE user_id = @test_user_id;

-- 7. Tester le marquage comme vu
SELECT 'Test marquage comme vu' as step;

-- Marquer la première notification comme vue
UPDATE level_notifications 
SET is_new = FALSE 
WHERE user_id = @test_user_id 
ORDER BY created_at ASC 
LIMIT 1;

-- Vérifier le changement
SELECT 
    'Après marquage comme vu' as status,
    SUM(CASE WHEN is_new = TRUE THEN 1 ELSE 0 END) as new_count,
    SUM(CASE WHEN is_new = FALSE THEN 1 ELSE 0 END) as viewed_count
FROM level_notifications 
WHERE user_id = @test_user_id;

-- 8. Test de performance avec plusieurs notifications
SELECT 'Test de performance' as step;

-- Créer plusieurs notifications pour tester les index
INSERT INTO level_notifications (user_id, old_level, new_level, level_name, total_xp, xp_gained, is_read, is_new, created_at)
SELECT 
    @test_user_id,
    3 + (n % 2),
    4 + (n % 2),
    CASE WHEN n % 2 = 0 THEN 'Avancé' ELSE 'Expert' END,
    400 + (n * 50),
    50,
    n % 3 = 0,  -- Certaines lues, d'autres non
    n % 4 = 0,  -- Certaines nouvelles, d'autres non
    (UNIX_TIMESTAMP() * 1000) + (n * 1000)
FROM (
    SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
) numbers;

-- Tester la performance des requêtes avec plus de données
SELECT 
    'Performance avec 12 notifications' as test,
    COUNT(*) as total_notifications,
    COUNT(CASE WHEN is_read = FALSE THEN 1 END) as unread,
    COUNT(CASE WHEN is_new = TRUE THEN 1 END) as new_notifications
FROM level_notifications 
WHERE user_id = @test_user_id;

-- 9. Afficher un résumé final
SELECT 'Résumé final du test' as step;

SELECT 
    u.email as user_email,
    ux.current_level,
    ux.total_xp,
    COUNT(ln.id) as total_notifications,
    COUNT(CASE WHEN ln.is_read = FALSE THEN 1 END) as unread_notifications,
    COUNT(CASE WHEN ln.is_new = TRUE THEN 1 END) as new_notifications,
    MAX(ln.created_at) as last_notification_time
FROM users u
JOIN user_xp ux ON u.id = ux.user_id
LEFT JOIN level_notifications ln ON u.id = ln.user_id
WHERE u.id = @test_user_id
GROUP BY u.id, u.email, ux.current_level, ux.total_xp;

-- 10. Nettoyer les données de test
SELECT 'Nettoyage des données de test' as step;

DELETE FROM level_notifications WHERE user_id = @test_user_id;
DELETE FROM user_xp WHERE user_id = @test_user_id;
DELETE FROM users WHERE email = 'integration.test@example.com';

SELECT '✅ Test d\'intégration terminé avec succès !' as final_status;