-- Script pour tester manuellement une montée de niveau
-- Remplacez 'votre.email@example.com' par votre email réel

-- 1. Trouver votre utilisateur
SELECT 
    id,
    email,
    first_name,
    last_name
FROM users 
WHERE email = 'votre.email@example.com'  -- REMPLACEZ PAR VOTRE EMAIL
LIMIT 1;

-- 2. Vérifier votre profil XP actuel
SELECT 
    u.email,
    ux.total_xp,
    ux.current_level,
    ux.xp_to_next_level
FROM users u
JOIN user_xp ux ON u.id = ux.user_id
WHERE u.email = 'votre.email@example.com'  -- REMPLACEZ PAR VOTRE EMAIL
LIMIT 1;

-- 3. Vérifier les niveaux disponibles
SELECT 
    level,
    xp_required,
    name,
    description
FROM levels
ORDER BY level;

-- 4. Simuler une montée de niveau (ATTENTION: Ceci va modifier vos données réelles)
-- Décommentez les lignes suivantes SEULEMENT si vous voulez tester

/*
-- Récupérer l'ID utilisateur
SET @user_id = (SELECT id FROM users WHERE email = 'votre.email@example.com' LIMIT 1);

-- Sauvegarder l'état actuel
SELECT 
    @user_id as user_id,
    total_xp as old_total_xp,
    current_level as old_level,
    xp_to_next_level as old_xp_to_next
FROM user_xp 
WHERE user_id = @user_id;

-- Ajouter des XP pour déclencher une montée de niveau
UPDATE user_xp 
SET 
    total_xp = total_xp + 200,  -- Ajouter 200 XP
    last_updated = UNIX_TIMESTAMP() * 1000
WHERE user_id = @user_id;

-- Vérifier le nouveau statut
SELECT 
    'Après ajout XP' as status,
    total_xp,
    current_level,
    xp_to_next_level
FROM user_xp 
WHERE user_id = @user_id;

-- Note: Le backend Java se chargera de détecter la montée de niveau
-- et de créer la notification automatiquement lors de la prochaine action
*/

-- 5. Vérifier les notifications de niveau existantes
SELECT 
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
WHERE u.email = 'votre.email@example.com'  -- REMPLACEZ PAR VOTRE EMAIL
ORDER BY ln.created_at DESC;