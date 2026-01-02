-- Script pour initialiser les données de gamification pour les utilisateurs existants

-- Créer des entrées user_xp pour tous les utilisateurs qui n'en ont pas
INSERT INTO user_xp (user_id, total_xp, current_level, xp_to_next_level, last_updated)
SELECT 
    u.id,
    0 as total_xp,
    1 as current_level,
    100 as xp_to_next_level,
    UNIX_TIMESTAMP() * 1000 as last_updated
FROM users u
WHERE u.archived = FALSE 
AND u.id NOT IN (SELECT user_id FROM user_xp);

-- Ajouter quelques défis par défaut
INSERT INTO challenges (name, description, challenge_type, target_value, xp_reward, start_date, end_date, is_active, created_at, updated_at) VALUES
('Premier Quiz', 'Réussissez votre premier quiz', 'QUIZ_PASSED', 1, 50, UNIX_TIMESTAMP() * 1000, NULL, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Cours Terminé', 'Terminez votre premier cours', 'COURS_COMPLETED', 1, 100, UNIX_TIMESTAMP() * 1000, NULL, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Étudiant Régulier', 'Connectez-vous 5 jours consécutifs', 'STREAK_DAYS', 5, 150, UNIX_TIMESTAMP() * 1000, NULL, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Collectionneur de Points', 'Gagnez 500 points XP', 'XP_EARNED', 500, 200, UNIX_TIMESTAMP() * 1000, NULL, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- Inscrire tous les utilisateurs aux défis par défaut
INSERT INTO user_challenges (user_id, challenge_id, current_progress, is_completed, completed_at, joined_at)
SELECT 
    u.id,
    c.id,
    0 as current_progress,
    FALSE as is_completed,
    NULL as completed_at,
    UNIX_TIMESTAMP() * 1000 as joined_at
FROM users u
CROSS JOIN challenges c
WHERE u.archived = FALSE 
AND c.is_active = TRUE
AND NOT EXISTS (
    SELECT 1 FROM user_challenges uc 
    WHERE uc.user_id = u.id AND uc.challenge_id = c.id
);

-- Donner le badge "Premier Pas" à tous les utilisateurs existants
INSERT INTO user_badges (user_id, badge_id, earned_at)
SELECT 
    u.id,
    b.id,
    UNIX_TIMESTAMP() * 1000 as earned_at
FROM users u
CROSS JOIN badges b
WHERE u.archived = FALSE 
AND b.name = 'Premier Pas'
AND NOT EXISTS (
    SELECT 1 FROM user_badges ub 
    WHERE ub.user_id = u.id AND ub.badge_id = b.id
);

-- Vérifier les données créées
SELECT 'Utilisateurs avec XP' as type, COUNT(*) as count FROM user_xp
UNION ALL
SELECT 'Badges attribués' as type, COUNT(*) as count FROM user_badges
UNION ALL
SELECT 'Défis inscrits' as type, COUNT(*) as count FROM user_challenges
UNION ALL
SELECT 'Défis disponibles' as type, COUNT(*) as count FROM challenges WHERE is_active = TRUE;