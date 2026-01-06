-- Script de test pour le système de notifications de niveau
-- Date: 2025-01-06

-- 1. Vérifier la structure de la table
SELECT 'Vérification de la structure de la table level_notifications' as test_step;
DESCRIBE level_notifications;

-- 2. Vérifier les contraintes et index
SELECT 'Vérification des contraintes et index' as test_step;
SHOW INDEX FROM level_notifications;

-- 3. Créer un utilisateur de test si nécessaire
INSERT IGNORE INTO users (email, password, first_name, last_name, role, email_verified, created_at)
VALUES ('test.level@example.com', '$2a$10$dummy.hash', 'Test', 'Level', 'APPRENANT', TRUE, UNIX_TIMESTAMP() * 1000);

-- 4. Récupérer l'ID de l'utilisateur de test
SET @test_user_id = (SELECT id FROM users WHERE email = 'test.level@example.com' LIMIT 1);

-- 5. Créer des niveaux de test si nécessaire
INSERT IGNORE INTO levels (level, xp_required, name, description, created_at) VALUES
(1, 0, 'Débutant', 'Premier niveau', UNIX_TIMESTAMP() * 1000),
(2, 100, 'Apprenti', 'Deuxième niveau', UNIX_TIMESTAMP() * 1000),
(3, 250, 'Intermédiaire', 'Troisième niveau', UNIX_TIMESTAMP() * 1000),
(4, 500, 'Avancé', 'Quatrième niveau', UNIX_TIMESTAMP() * 1000);

-- 6. Créer un profil XP de test
INSERT INTO user_xp (user_id, total_xp, current_level, xp_to_next_level, last_updated)
VALUES (@test_user_id, 50, 1, 50, UNIX_TIMESTAMP() * 1000)
ON DUPLICATE KEY UPDATE 
    total_xp = 50, 
    current_level = 1, 
    xp_to_next_level = 50;

-- 7. Insérer une notification de niveau de test
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
    50,
    FALSE,
    TRUE,
    UNIX_TIMESTAMP() * 1000
);

-- 8. Tester les requêtes principales
SELECT 'Test des requêtes de base' as test_step;

-- Compter les notifications non lues
SELECT COUNT(*) as unread_count 
FROM level_notifications 
WHERE user_id = @test_user_id AND is_read = FALSE;

-- Compter les nouvelles notifications
SELECT COUNT(*) as new_count 
FROM level_notifications 
WHERE user_id = @test_user_id AND is_new = TRUE;

-- Récupérer les notifications de l'utilisateur
SELECT 
    id,
    old_level,
    new_level,
    level_name,
    total_xp,
    xp_gained,
    is_read,
    is_new,
    created_at
FROM level_notifications 
WHERE user_id = @test_user_id 
ORDER BY created_at DESC;

-- 9. Tester la mise à jour des statuts
UPDATE level_notifications 
SET is_read = TRUE 
WHERE user_id = @test_user_id AND id = LAST_INSERT_ID();

UPDATE level_notifications 
SET is_new = FALSE 
WHERE user_id = @test_user_id AND id = LAST_INSERT_ID();

-- 10. Vérifier les mises à jour
SELECT 
    'Après mise à jour' as status,
    is_read,
    is_new
FROM level_notifications 
WHERE user_id = @test_user_id AND id = LAST_INSERT_ID();

-- 11. Nettoyer les données de test
DELETE FROM level_notifications WHERE user_id = @test_user_id;
DELETE FROM user_xp WHERE user_id = @test_user_id;
DELETE FROM users WHERE email = 'test.level@example.com';

SELECT 'Tests terminés avec succès ✅' as final_status;