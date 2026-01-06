-- Test de l'affichage du classement avec des données réalistes
-- Ce script crée des données de test pour vérifier l'affichage des rangs

-- 1. Vérifier les utilisateurs existants et leurs XP
SELECT 
    'Utilisateurs actuels et leurs XP' as section,
    u.id,
    u.email,
    u.first_name,
    u.last_name,
    COALESCE(ux.total_xp, 0) as total_xp,
    COALESCE(ux.current_level, 1) as current_level,
    COUNT(DISTINCT ub.badge_id) as badges_count
FROM users u
LEFT JOIN user_xp ux ON u.id = ux.user_id
LEFT JOIN user_badges ub ON u.id = ub.user_id
WHERE u.role = 'APPRENANT'
GROUP BY u.id, u.email, u.first_name, u.last_name, ux.total_xp, ux.current_level
ORDER BY total_xp DESC
LIMIT 10;

-- 2. Créer des utilisateurs de test avec différents niveaux si nécessaire
INSERT IGNORE INTO users (email, password, first_name, last_name, role, created_at, updated_at) VALUES
('alice.martin@test.com', '$2a$10$dummy', 'Alice', 'Martin', 'APPRENANT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('thomas.dubois@test.com', '$2a$10$dummy', 'Thomas', 'Dubois', 'APPRENANT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('sophie.laurent@test.com', '$2a$10$dummy', 'Sophie', 'Laurent', 'APPRENANT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('pierre.moreau@test.com', '$2a$10$dummy', 'Pierre', 'Moreau', 'APPRENANT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('marie.leroy@test.com', '$2a$10$dummy', 'Marie', 'Leroy', 'APPRENANT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 3. Créer des profils XP pour ces utilisateurs
INSERT IGNORE INTO user_xp (user_id, total_xp, current_level, xp_to_next_level, last_updated)
SELECT 
    u.id,
    CASE 
        WHEN u.email = 'alice.martin@test.com' THEN 2850
        WHEN u.email = 'thomas.dubois@test.com' THEN 2640
        WHEN u.email = 'sophie.laurent@test.com' THEN 2420
        WHEN u.email = 'pierre.moreau@test.com' THEN 2180
        WHEN u.email = 'marie.leroy@test.com' THEN 1950
        ELSE 100
    END as total_xp,
    CASE 
        WHEN u.email = 'alice.martin@test.com' THEN 8
        WHEN u.email = 'thomas.dubois@test.com' THEN 7
        WHEN u.email = 'sophie.laurent@test.com' THEN 7
        WHEN u.email = 'pierre.moreau@test.com' THEN 6
        WHEN u.email = 'marie.leroy@test.com' THEN 6
        ELSE 1
    END as current_level,
    100 as xp_to_next_level,
    UNIX_TIMESTAMP() * 1000 as last_updated
FROM users u
WHERE u.email IN ('alice.martin@test.com', 'thomas.dubois@test.com', 'sophie.laurent@test.com', 'pierre.moreau@test.com', 'marie.leroy@test.com');

-- 4. Attribuer quelques badges aux utilisateurs de test
-- Créer des badges de base s'ils n'existent pas
INSERT IGNORE INTO badges (name, description, icon_url, criteria_type, criteria_value, is_active, created_at, updated_at) VALUES
('Premier Pas', 'Premier cours terminé', 'first-course.svg', 'FIRST_COURSE', 1, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Étudiant Régulier', '5 cours terminés', 'regular-student.svg', 'COURS_COMPLETED', 5, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Expert', '10 cours terminés', 'expert.svg', 'COURS_COMPLETED', 10, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- Attribuer des badges aux top utilisateurs
INSERT IGNORE INTO user_badges (user_id, badge_id, earned_at)
SELECT 
    u.id,
    b.id,
    UNIX_TIMESTAMP() * 1000 - (RAND() * 86400000 * 30) -- Badge gagné dans les 30 derniers jours
FROM users u
CROSS JOIN badges b
WHERE u.email IN ('alice.martin@test.com', 'thomas.dubois@test.com', 'sophie.laurent@test.com')
AND b.name IN ('Premier Pas', 'Étudiant Régulier', 'Expert')
AND RAND() > 0.3; -- 70% de chance d'avoir chaque badge

-- 5. Vérifier le classement final
SELECT 
    'Classement final pour test' as section,
    ROW_NUMBER() OVER (ORDER BY COALESCE(ux.total_xp, 0) DESC) as rang,
    CONCAT(u.first_name, ' ', u.last_name) as nom_complet,
    u.email,
    COALESCE(ux.total_xp, 0) as total_xp,
    COALESCE(ux.current_level, 1) as niveau,
    COUNT(DISTINCT ub.badge_id) as badges_count
FROM users u
LEFT JOIN user_xp ux ON u.id = ux.user_id
LEFT JOIN user_badges ub ON u.id = ub.user_id
WHERE u.role = 'APPRENANT'
GROUP BY u.id, u.first_name, u.last_name, u.email, ux.total_xp, ux.current_level
ORDER BY total_xp DESC
LIMIT 10;

-- 6. Instructions de test
SELECT '
🏆 TEST DU CLASSEMENT:

DONNÉES CRÉÉES:
- 5 utilisateurs de test avec différents niveaux XP
- Badges attribués aléatoirement
- Classement réaliste avec podium

POUR TESTER L\'AFFICHAGE:
1. Allez sur la page Classement dans l\'application (http://localhost:4201/classement)
2. Vérifiez que les 3 premiers rangs ont des badges dorés/argentés/bronze
3. Vérifiez que le rang 1 a une animation spéciale (effet de brillance)
4. Vérifiez que tous les badges sont visibles

VÉRIFICATIONS VISUELLES:
✅ Rang 1: Badge doré avec animation de brillance
✅ Rang 2: Badge argenté 
✅ Rang 3: Badge bronze
✅ Rangs 4+: Numéros simples sur fond gris
✅ Tous les badges utilisent l\'icône "award" (trophée)

Si les icônes ne s\'affichent pas:
- Vérifiez que Feather Icons est chargé
- Vérifiez la console du navigateur pour les erreurs
- Actualisez la page
' as instructions;