-- Script pour créer des utilisateurs de test avec des XP pour tester le podium

-- 1. Vérifier les utilisateurs existants
SELECT 'Utilisateurs existants:' as info;
SELECT id, email, first_name, last_name, role, archived FROM users WHERE archived = false;

-- 2. Créer quelques utilisateurs de test (si ils n'existent pas déjà)
INSERT IGNORE INTO users (email, password, first_name, last_name, role, email_verified, created_at, archived)
VALUES 
('test1@example.com', '$2a$10$dummy.hash.for.test.user.1', 'Alice', 'Dupont', 'ETUDIANT', true, UNIX_TIMESTAMP() * 1000, false),
('test2@example.com', '$2a$10$dummy.hash.for.test.user.2', 'Bob', 'Martin', 'ETUDIANT', true, UNIX_TIMESTAMP() * 1000, false),
('test3@example.com', '$2a$10$dummy.hash.for.test.user.3', 'Claire', 'Bernard', 'ETUDIANT', true, UNIX_TIMESTAMP() * 1000, false),
('test4@example.com', '$2a$10$dummy.hash.for.test.user.4', 'David', 'Moreau', 'ETUDIANT', true, UNIX_TIMESTAMP() * 1000, false);

-- 3. Créer des données XP pour ces utilisateurs de test
INSERT IGNORE INTO user_xp (user_id, total_xp, current_level, xp_to_next_level, last_updated)
SELECT 
    u.id,
    CASE 
        WHEN u.email = 'test1@example.com' THEN 1200  -- Alice - 1ère place
        WHEN u.email = 'test2@example.com' THEN 800   -- Bob - 2ème place  
        WHEN u.email = 'test3@example.com' THEN 600   -- Claire - 3ème place
        WHEN u.email = 'test4@example.com' THEN 300   -- David - 4ème place
        ELSE 50  -- Votre compte admin reste à 50 XP
    END as total_xp,
    CASE 
        WHEN u.email = 'test1@example.com' THEN 5
        WHEN u.email = 'test2@example.com' THEN 4
        WHEN u.email = 'test3@example.com' THEN 3
        WHEN u.email = 'test4@example.com' THEN 2
        ELSE 1
    END as current_level,
    100 as xp_to_next_level,
    UNIX_TIMESTAMP() * 1000 as last_updated
FROM users u
WHERE u.archived = false 
AND u.email IN ('test1@example.com', 'test2@example.com', 'test3@example.com', 'test4@example.com');

-- 4. Mettre à jour votre compte admin pour qu'il soit dans le top 3
UPDATE user_xp 
SET total_xp = 1000, current_level = 4 
WHERE user_id = (SELECT id FROM users WHERE role = 'ADMIN' AND archived = false LIMIT 1);

-- 5. Vérifier le classement final
SELECT 'Classement final:' as info;
SELECT 
    u.first_name,
    u.last_name,
    u.email,
    ux.total_xp,
    ux.current_level,
    ROW_NUMBER() OVER (ORDER BY ux.total_xp DESC) as rang
FROM user_xp ux
JOIN users u ON ux.user_id = u.id
WHERE u.archived = false
ORDER BY ux.total_xp DESC
LIMIT 10;