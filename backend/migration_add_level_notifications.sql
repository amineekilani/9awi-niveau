-- Migration pour ajouter la table des notifications de niveau
-- Date: 2025-01-06
-- Description: Créer la table level_notifications pour stocker les notifications de montée de niveau

-- Créer la table level_notifications
CREATE TABLE IF NOT EXISTS level_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    old_level INT NOT NULL,
    new_level INT NOT NULL,
    level_name VARCHAR(255) NOT NULL,
    total_xp INT NOT NULL,
    xp_gained INT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_new BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    
    -- Contraintes
    CONSTRAINT fk_level_notifications_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE,
    
    -- Index pour améliorer les performances
    INDEX idx_level_notifications_user_id (user_id),
    INDEX idx_level_notifications_created_at (created_at),
    INDEX idx_level_notifications_is_read (is_read),
    INDEX idx_level_notifications_is_new (is_new),
    INDEX idx_level_notifications_user_unread (user_id, is_read),
    INDEX idx_level_notifications_user_new (user_id, is_new)
);

-- Vérifier que la table a été créée
SELECT 'Table level_notifications créée avec succès' as status;

-- Afficher la structure de la table
DESCRIBE level_notifications;