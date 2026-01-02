-- Script simple pour créer les tables de gamification
USE 9awi_niveau;

-- Vérifier si les tables existent déjà
DROP TABLE IF EXISTS user_challenges;
DROP TABLE IF EXISTS challenges;
DROP TABLE IF EXISTS user_badges;
DROP TABLE IF EXISTS badges;
DROP TABLE IF EXISTS user_xp;
DROP TABLE IF EXISTS levels;

-- Table des niveaux
CREATE TABLE levels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level INT NOT NULL UNIQUE,
    xp_required INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    reward_description VARCHAR(500),
    created_at BIGINT
);

-- Table des badges
CREATE TABLE badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    icon_url VARCHAR(500),
    criteria_type VARCHAR(50) NOT NULL,
    criteria_value INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at BIGINT,
    updated_at BIGINT
);

-- Table des XP utilisateurs
CREATE TABLE user_xp (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    total_xp INT DEFAULT 0,
    current_level INT DEFAULT 1,
    xp_to_next_level INT DEFAULT 100,
    last_updated BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table des badges utilisateurs
CREATE TABLE user_badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    earned_at BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_badge (user_id, badge_id)
);

-- Table des défis
CREATE TABLE challenges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    challenge_type VARCHAR(50) NOT NULL,
    target_value INT NOT NULL,
    xp_reward INT NOT NULL,
    start_date BIGINT,
    end_date BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at BIGINT,
    updated_at BIGINT
);

-- Table des défis utilisateurs
CREATE TABLE user_challenges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    challenge_id BIGINT NOT NULL,
    current_progress INT DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    completed_at BIGINT,
    joined_at BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_challenge (user_id, challenge_id)
);

-- Insertion des niveaux par défaut
INSERT INTO levels (level, xp_required, name, description, reward_description) VALUES
(1, 0, 'Débutant', 'Bienvenue dans votre parcours d\'apprentissage !', 'Accès aux cours de base'),
(2, 100, 'Apprenti', 'Vous commencez à maîtriser les bases', 'Badge de progression'),
(3, 250, 'Étudiant', 'Vous progressez bien dans vos études', 'Accès aux quiz avancés'),
(4, 500, 'Avancé', 'Vous avez acquis de solides compétences', 'Certificat de niveau'),
(5, 1000, 'Expert', 'Vous maîtrisez votre domaine', 'Accès aux cours premium');

-- Insertion des badges par défaut
INSERT INTO badges (name, description, icon_url, criteria_type, criteria_value, is_active) VALUES
('Premier Quiz', 'Réussissez votre premier quiz', '/icons/first-quiz.svg', 'QUIZ_PASSED', 1, TRUE),
('Perfectionniste', 'Obtenez un score parfait à un quiz', '/icons/perfect-score.svg', 'PERFECT_SCORE', 1, TRUE),
('Expert Quiz', 'Réussissez 10 quiz', '/icons/quiz-expert.svg', 'QUIZ_PASSED', 10, TRUE);

SELECT 'Tables de gamification créées avec succès!' as message;