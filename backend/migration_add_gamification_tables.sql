-- Migration pour ajouter les tables de gamification

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
(5, 1000, 'Expert', 'Vous maîtrisez votre domaine', 'Accès aux cours premium'),
(6, 2000, 'Maître', 'Vous êtes un véritable expert', 'Badge de maître'),
(7, 3500, 'Sage', 'Votre sagesse inspire les autres', 'Statut de mentor'),
(8, 5500, 'Légende', 'Vous êtes une légende vivante', 'Reconnaissance spéciale'),
(9, 8000, 'Champion', 'Vous êtes au sommet de votre art', 'Titre de champion'),
(10, 12000, 'Grand Maître', 'Le niveau ultime d\'excellence', 'Statut de grand maître');

-- Insertion des badges par défaut
INSERT INTO badges (name, description, icon_url, criteria_type, criteria_value, is_active) VALUES
('Premier Pas', 'Félicitations pour votre première connexion !', '/icons/first-login.svg', 'FIRST_COURSE', 1, TRUE),
('Étudiant Assidu', 'Terminez votre premier cours', '/icons/first-course.svg', 'COURS_COMPLETED', 1, TRUE),
('Quiz Master', 'Réussissez votre premier quiz', '/icons/first-quiz.svg', 'QUIZ_PASSED', 1, TRUE),
('Perfectionniste', 'Obtenez un score parfait à un quiz', '/icons/perfect-score.svg', 'PERFECT_SCORE', 1, TRUE),
('Marathonien', 'Connectez-vous 7 jours consécutifs', '/icons/streak.svg', 'STREAK_DAYS', 7, TRUE),
('Collectionneur', 'Terminez 5 cours', '/icons/collector.svg', 'COURS_COMPLETED', 5, TRUE),
('Expert Quiz', 'Réussissez 10 quiz', '/icons/quiz-expert.svg', 'QUIZ_PASSED', 10, TRUE),
('Montée en Niveau', 'Atteignez le niveau 5', '/icons/level-up.svg', 'LEVEL_REACHED', 5, TRUE),
('Chasseur de Points', 'Gagnez 1000 points XP', '/icons/xp-hunter.svg', 'XP_EARNED', 1000, TRUE),
('Défi Relevé', 'Terminez votre premier défi', '/icons/challenge.svg', 'CHALLENGE_COMPLETED', 1, TRUE);