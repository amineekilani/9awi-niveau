-- Test des calculs XP et niveaux
-- Remplacez 'votre.email@example.com' par votre email réel

SET @user_email = 'votre.email@example.com';  -- REMPLACEZ PAR VOTRE EMAIL
SET @user_id = (SELECT id FROM users WHERE email = @user_email LIMIT 1);

-- 1. Afficher l'état actuel de l'utilisateur
SELECT 
    'État actuel de l\'utilisateur' as section,
    u.email,
    ux.total_xp,
    ux.current_level,
    ux.xp_to_next_level
FROM users u
LEFT JOIN user_xp ux ON u.id = ux.user_id
WHERE u.id = @user_id;

-- 2. Afficher tous les niveaux configurés
SELECT 
    'Niveaux configurés' as section,
    level,
    xp_required,
    name,
    description
FROM levels
ORDER BY level;

-- 3. Calculer le prochain niveau pour cet utilisateur
SELECT 
    'Calcul du prochain niveau' as section,
    l.level as next_level,
    l.xp_required as xp_required_for_next_level,
    l.name as next_level_name,
    (l.xp_required - ux.total_xp) as points_to_next_level
FROM user_xp ux
JOIN levels l ON l.xp_required > ux.total_xp
WHERE ux.user_id = @user_id
ORDER BY l.level ASC
LIMIT 1;

-- 4. Vérifier le niveau actuel basé sur les XP
SELECT 
    'Niveau basé sur les XP actuels' as section,
    l.level,
    l.xp_required,
    l.name,
    ux.total_xp,
    CASE 
        WHEN ux.current_level = l.level THEN '✅ Correct'
        ELSE '❌ Incohérent'
    END as status
FROM user_xp ux
JOIN levels l ON l.xp_required <= ux.total_xp
WHERE ux.user_id = @user_id
ORDER BY l.level DESC
LIMIT 1;

-- 5. Simulation de ce que l'API devrait retourner
SELECT 
    'Simulation API Response' as section,
    ux.total_xp as totalPoints,
    ux.current_level as currentLevel,
    current_level_info.name as levelName,
    current_level_info.description as levelDescription,
    COALESCE(next_level_info.xp_required - ux.total_xp, 0) as pointsToNextLevel,
    COALESCE(next_level_info.xp_required, ux.total_xp) as nextLevelPoints,
    CASE 
        WHEN next_level_info.xp_required IS NOT NULL THEN
            ROUND(
                ((ux.total_xp - current_level_info.xp_required) * 100.0) / 
                (next_level_info.xp_required - current_level_info.xp_required), 
                2
            )
        ELSE 100.0
    END as progressPercent
FROM user_xp ux
LEFT JOIN levels current_level_info ON current_level_info.level = ux.current_level
LEFT JOIN (
    SELECT l.* FROM levels l 
    JOIN user_xp ux2 ON l.xp_required > ux2.total_xp 
    WHERE ux2.user_id = @user_id
    ORDER BY l.level ASC 
    LIMIT 1
) next_level_info ON 1=1
WHERE ux.user_id = @user_id;

-- 6. Recommandations
SELECT '
📊 INTERPRÉTATION DES RÉSULTATS :

1. totalPoints = Vos XP actuels
2. pointsToNextLevel = XP manquants pour le prochain niveau
3. nextLevelPoints = XP totaux nécessaires pour le prochain niveau
4. progressPercent = Pourcentage de progression dans le niveau actuel

AFFICHAGE RECOMMANDÉ :
- Simple : "259 XP (41 pour niveau 4)"
- Détaillé : "259/500 XP (82% vers niveau 4)"
- Barre de progression basée sur progressPercent

' as guide;