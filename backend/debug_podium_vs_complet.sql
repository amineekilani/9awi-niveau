-- Debug : Pourquoi le podium est vide mais le classement complet fonctionne ?

-- 1. Vérifier tous les utilisateurs
SELECT 'TOUS LES UTILISATEURS' as info;
SELECT id, email, first_name, last_name, role, archived FROM users;

-- 2. Vérifier tous les UserXP
SELECT 'TOUS LES USERXP' as info;
SELECT ux.id, ux.user_id, u.email, u.first_name, u.last_name, ux.total_xp, u.archived
FROM user_xp ux 
JOIN users u ON ux.user_id = u.id
ORDER BY ux.total_xp DESC;

-- 3. Requête utilisée par le classement complet (avec pagination)
SELECT 'CLASSEMENT COMPLET (avec pagination)' as info;
SELECT ux.id, u.email, u.first_name, u.last_name, ux.total_xp, u.archived
FROM user_xp ux 
JOIN users u ON ux.user_id = u.id
WHERE u.archived = false
ORDER BY ux.total_xp DESC
LIMIT 20;

-- 4. Requête utilisée par le podium top 10 (avec limite)
SELECT 'PODIUM TOP 10 (avec limite)' as info;
SELECT ux.id, u.email, u.first_name, u.last_name, ux.total_xp, u.archived
FROM user_xp ux 
JOIN users u ON ux.user_id = u.id
WHERE u.archived = false
ORDER BY ux.total_xp DESC
LIMIT 10;

-- 5. Vérifier s'il y a des utilisateurs archivés avec des XP
SELECT 'UTILISATEURS ARCHIVES AVEC XP' as info;
SELECT ux.id, u.email, u.first_name, u.last_name, ux.total_xp, u.archived
FROM user_xp ux 
JOIN users u ON ux.user_id = u.id
WHERE u.archived = true;

-- 6. Corriger les utilisateurs archivés si nécessaire
UPDATE users SET archived = false WHERE role = 'ADMIN';

-- 7. Vérifier après correction
SELECT 'APRES CORRECTION' as info;
SELECT ux.id, u.email, u.first_name, u.last_name, ux.total_xp, u.archived
FROM user_xp ux 
JOIN users u ON ux.user_id = u.id
WHERE u.archived = false
ORDER BY ux.total_xp DESC
LIMIT 10;