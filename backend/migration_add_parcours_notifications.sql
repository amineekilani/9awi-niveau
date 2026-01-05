-- Migration pour ajouter la table des notifications de parcours
-- Date: 2026-01-04

-- Créer la table des notifications de parcours
CREATE TABLE IF NOT EXISTS parcours_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    parcours_id BIGINT NOT NULL,
    type ENUM('PARCOURS_COMPLETED', 'CERTIFICATE_READY', 'MILESTONE_REACHED') NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    xp_earned INT,
    certificate_ready BOOLEAN DEFAULT FALSE,
    certificate_url VARCHAR(500),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parcours_id) REFERENCES parcours_apprentissage(id) ON DELETE CASCADE,
    
    INDEX idx_user_notifications (user_id, created_at DESC),
    INDEX idx_user_unread (user_id, is_read),
    INDEX idx_notification_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Vérifier que la table a été créée
SELECT 'Table parcours_notifications créée avec succès' as status;

-- Afficher la structure de la table
DESCRIBE parcours_notifications;