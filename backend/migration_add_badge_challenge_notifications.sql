-- Migration pour ajouter les tables de notifications de badges et défis
-- Date: 2025-01-06
-- Description: Créer les tables badge_notifications et challenge_notifications

-- Créer la table badge_notifications
CREATE TABLE IF NOT EXISTS badge_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_new BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    
    -- Contraintes
    CONSTRAINT fk_badge_notifications_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_badge_notifications_badge 
        FOREIGN KEY (badge_id) REFERENCES badges(id) 
        ON DELETE CASCADE,
    
    -- Index pour améliorer les performances
    INDEX idx_badge_notifications_user_id (user_id),
    INDEX idx_badge_notifications_created_at (created_at),
    INDEX idx_badge_notifications_is_read (is_read),
    INDEX idx_badge_notifications_is_new (is_new),
    INDEX idx_badge_notifications_user_unread (user_id, is_read),
    INDEX idx_badge_notifications_user_new (user_id, is_new)
);

-- Créer la table challenge_notifications
CREATE TABLE IF NOT EXISTS challenge_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    challenge_id BIGINT NOT NULL,
    xp_earned INT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_new BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    
    -- Contraintes
    CONSTRAINT fk_challenge_notifications_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_challenge_notifications_challenge 
        FOREIGN KEY (challenge_id) REFERENCES challenges(id) 
        ON DELETE CASCADE,
    
    -- Index pour améliorer les performances
    INDEX idx_challenge_notifications_user_id (user_id),
    INDEX idx_challenge_notifications_created_at (created_at),
    INDEX idx_challenge_notifications_is_read (is_read),
    INDEX idx_challenge_notifications_is_new (is_new),
    INDEX idx_challenge_notifications_user_unread (user_id, is_read),
    INDEX idx_challenge_notifications_user_new (user_id, is_new)
);

-- Vérifier que les tables ont été créées
SELECT 'Tables badge_notifications et challenge_notifications créées avec succès' as status;

-- Afficher la structure des tables
DESCRIBE badge_notifications;
DESCRIBE challenge_notifications;