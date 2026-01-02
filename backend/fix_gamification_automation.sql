-- Script pour corriger et tester l'automatisation de la gamification

-- 1. Vérifier que les tables existent
SELECT 'Vérification des tables de gamification...' as status;

SELECT 
    CASE 
        WHEN COUNT(*) = 6 THEN 'OK - Toutes les tables de gamification existent'
        ELSE CONCAT('ERREUR - Seulement ', COUNT(*), ' tables trouvées sur 6')
    END as table_check
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name IN ('levels', 'badges', 'user_xp', 'user_badges', 'challenges', 'user_challenges');

-- 2. Vérifier les données par défaut
SELECT 'Vérification des données par défaut...' as status;

SELECT 
    CASE 
        WHEN COUNT(*) >= 10 THEN 'OK - Niveaux par défaut présents'
        ELSE CONCAT('ATTENTION - Seulement ', COUNT(*), ' niveaux trouvés')
    END as levels_check
FROM levels;

SELECT 
    CASE 
        WHEN COUNT(*) >= 10 THEN 'OK - Badges par défaut présents'
        ELSE CONCAT('ATTENTION - Seulement ', COUNT(*), ' badges trouvés')
    END as badges_check
FROM badges;

-- 3. Nettoyer les doublons potentiels dans user_xp
SELECT 'Nettoyage des doublons user_xp...' as status;

-- Identifier les doublons
SELECT user_id, COUNT(*) as count
FROM user_xp 
GROUP BY user_id 
HAVING COUNT(*) > 1;

-- Supprimer les doublons (garder le plus récent)
DELETE ux1 FROM user_xp ux1
INNER JOIN user_xp ux2 
WHERE ux1.id < ux2.id 
AND ux1.user_id = ux2.user_id;

-- 4. Vérifier l'intégrité des données
SELECT 'Vérification de l\'intégrité...' as status;

-- Vérifier les utilisateurs sans profil XP
SELECT 
    CONCAT('Utilisateurs sans profil XP: ', COUNT(*)) as missing_xp_profiles
FROM users u 
LEFT JOIN user_xp ux ON u.id = ux.user_id 
WHERE u.archived = false AND ux.id IS NULL;

-- Créer les profils XP manquants
INSERT INTO user_xp (user_id, total_xp, current_level, xp_to_next_level, last_updated)
SELECT u.id, 0, 1, 100, UNIX_TIMESTAMP() * 1000
FROM users u 
LEFT JOIN user_xp ux ON u.id = ux.user_id 
WHERE u.archived = false AND ux.id IS NULL;

-- 5. Vérifier les badges actifs
SELECT 'Badges actifs:' as status;
SELECT name, criteria_type, criteria_value 
FROM badges 
WHERE is_active = true 
ORDER BY criteria_type, criteria_value;

-- 6. Statistiques actuelles
SELECT 'Statistiques actuelles:' as status;

SELECT 
    (SELECT COUNT(*) FROM users WHERE archived = false) as total_users,
    (SELECT COUNT(*) FROM user_xp) as users_with_xp,
    (SELECT COUNT(*) FROM user_badges) as total_badges_earned,
    (SELECT SUM(total_xp) FROM user_xp) as total_xp_awarded,
    (SELECT AVG(total_xp) FROM user_xp) as average_xp_per_user;

-- 7. Test de cohérence des niveaux
SELECT 'Test de cohérence des niveaux...' as status;

SELECT 
    ux.user_id,
    u.email,
    ux.total_xp,
    ux.current_level,
    l.name as level_name,
    l.xp_required,
    CASE 
        WHEN ux.total_xp >= l.xp_required THEN 'OK'
        ELSE 'ERREUR - XP insuffisant pour ce niveau'
    END as level_consistency
FROM user_xp ux
JOIN users u ON ux.user_id = u.id
LEFT JOIN levels l ON ux.current_level = l.level
WHERE u.archived = false
ORDER BY ux.total_xp DESC
LIMIT 10;

SELECT 'Script de correction terminé !' as status;