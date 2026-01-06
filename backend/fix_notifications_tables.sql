-- Script de correction pour créer les tables de notifications manquantes
-- Exécutez ce script IMMÉDIATEMENT pour résoudre le problème de chargement infini

-- 1. Créer la table badge_notifications
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
    INDEX idx_badge_notifications_is_read (is_read),
    INDEX idx_badge_notifications_is_new (is_new)
);

-- 2. Créer la table challenge_notifications
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
    INDEX idx_challenge_notifications_is_read (is_read),
    INDEX idx_challenge_notifications_is_new (is_new)
);

-- 3. Vérifier que les tables ont été créées
SELECT 'Tables créées avec succès' as status;
SHOW TABLES LIKE '%notifications';

-- 4. Vérifier la structure
DESCRIBE badge_notifications;
DESCRIBE challenge_notifications;