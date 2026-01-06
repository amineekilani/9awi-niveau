-- Script complet pour créer les nouvelles tables de notifications et migrer les données
-- Date: 2025-01-06

-- 1. Créer les nouvelles tables
CREATE TABLE IF NOT EXISTS badge_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_new BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    
    CONSTRAINT fk_badge_notifications_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_badge_notifications_badge 
        FOREIGN KEY (badge_id) REFERENCES badges(id) 
        ON DELETE CASCADE,
    
    INDEX idx_badge_notifications_user_id (user_id),
    INDEX idx_badge_notifications_created_at (created_at),
    INDEX idx_badge_notifications_is_read (is_read),
    INDEX idx_badge_notifications_is_new (is_new)
);

CREATE TABLE IF NOT EXISTS challenge_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    challenge_id BIGINT NOT NULL,
    xp_earned INT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_new BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    
    CONSTRAINT fk_challenge_notifications_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_challenge_notifications_challenge 
        FOREIGN KEY (challenge_id) REFERENCES challenges(id) 
        ON DELETE CASCADE,
    
    INDEX idx_challenge_notifications_user_id (user_id),
    INDEX idx_challenge_notifications_created_at (created_at),
    INDEX idx_challenge_notifications_is_read (is_read),
    INDEX idx_challenge_notifications_is_new (is_new)
);

-- 2. Migrer les badges existants vers les notifications (optionnel)
-- Créer des notifications pour les badges récents (derniers 7 jours)
INSERT INTO badge_notifications (user_id, badge_id, is_read, is_new, created_at)
SELECT 
    ub.user_id,
    ub.badge_id,
    FALSE, -- Non lu
    FALSE, -- Pas nouveau (pour éviter les spams)
    ub.earned_at
FROM user_badges ub
WHERE ub.earned_at > (UNIX_TIMESTAMP() - 7*24*60*60) * 1000 -- Derniers 7 jours
AND NOT EXISTS (
    SELECT 1 FROM badge_notifications bn 
    WHERE bn.user_id = ub.user_id AND bn.badge_id = ub.badge_id
);

-- 3. Migrer les défis terminés récents vers les notifications (optionnel)
-- Créer des notifications pour les défis récents (derniers 7 jours)
INSERT INTO challenge_notifications (user_id, challenge_id, xp_earned, is_read, is_new, created_at)
SELECT 
    uc.user_id,
    uc.challenge_id,
    c.xp_reward,
    FALSE, -- Non lu
    FALSE, -- Pas nouveau (pour éviter les spams)
    uc.completed_at
FROM user_challenges uc
JOIN challenges c ON uc.challenge_id = c.id
WHERE uc.is_completed = TRUE 
AND uc.completed_at > (UNIX_TIMESTAMP() - 7*24*60*60) * 1000 -- Derniers 7 jours
AND NOT EXISTS (
    SELECT 1 FROM challenge_notifications cn 
    WHERE cn.user_id = uc.user_id AND cn.challenge_id = uc.challenge_id
);

-- 4. Vérifier les résultats
SELECT 'Résultats de la migration' as section;

SELECT 
    'Badge notifications créées' as type,
    COUNT(*) as count
FROM badge_notifications;

SELECT 
    'Challenge notifications créées' as type,
    COUNT(*) as count
FROM challenge_notifications;

-- 5. Afficher un échantillon des nouvelles notifications
SELECT 
    'Échantillon notifications de badge' as section,
    bn.id,
    u.email,
    b.name as badge_name,
    bn.is_read,
    bn.is_new,
    FROM_UNIXTIME(bn.created_at/1000) as created_date
FROM badge_notifications bn
JOIN users u ON bn.user_id = u.id
JOIN badges b ON bn.badge_id = b.id
ORDER BY bn.created_at DESC
LIMIT 5;

SELECT 
    'Échantillon notifications de défi' as section,
    cn.id,
    u.email,
    c.name as challenge_name,
    cn.xp_earned,
    cn.is_read,
    cn.is_new,
    FROM_UNIXTIME(cn.created_at/1000) as created_date
FROM challenge_notifications cn
JOIN users u ON cn.user_id = u.id
JOIN challenges c ON cn.challenge_id = c.id
ORDER BY cn.created_at DESC
LIMIT 5;

SELECT '✅ Migration terminée avec succès !' as status;