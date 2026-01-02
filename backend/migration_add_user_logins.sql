-- Migration pour ajouter le tracking des connexions utilisateur

-- Table pour tracker les connexions
CREATE TABLE user_logins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    login_time BIGINT NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_login_time (user_id, login_time)
);

-- Message de confirmation
SELECT 'Table user_logins créée avec succès!' as message;