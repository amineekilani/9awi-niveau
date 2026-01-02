-- Données par défaut pour la gamification (H2 Database)

-- Insertion des niveaux par défaut
INSERT INTO levels (level, xp_required, name, description, reward_description, created_at) VALUES
(1, 0, 'Débutant', 'Bienvenue dans votre parcours d''apprentissage !', 'Accès aux cours de base', UNIX_TIMESTAMP() * 1000),
(2, 100, 'Apprenti', 'Vous commencez à maîtriser les bases', 'Badge de progression', UNIX_TIMESTAMP() * 1000),
(3, 250, 'Étudiant', 'Vous progressez bien dans vos études', 'Accès aux quiz avancés', UNIX_TIMESTAMP() * 1000),
(4, 500, 'Avancé', 'Vous avez acquis de solides compétences', 'Certificat de niveau', UNIX_TIMESTAMP() * 1000),
(5, 1000, 'Expert', 'Vous maîtrisez votre domaine', 'Accès aux cours premium', UNIX_TIMESTAMP() * 1000),
(6, 2000, 'Maître', 'Vous êtes un véritable expert', 'Badge de maître', UNIX_TIMESTAMP() * 1000),
(7, 3500, 'Sage', 'Votre sagesse inspire les autres', 'Statut de mentor', UNIX_TIMESTAMP() * 1000),
(8, 5500, 'Légende', 'Vous êtes une légende vivante', 'Reconnaissance spéciale', UNIX_TIMESTAMP() * 1000),
(9, 8000, 'Champion', 'Vous êtes au sommet de votre art', 'Titre de champion', UNIX_TIMESTAMP() * 1000),
(10, 12000, 'Grand Maître', 'Le niveau ultime d''excellence', 'Statut de grand maître', UNIX_TIMESTAMP() * 1000);

-- Insertion des badges par défaut
INSERT INTO badges (name, description, icon_url, criteria_type, criteria_value, is_active, created_at, updated_at) VALUES
('Premier Pas', 'Félicitations pour votre première connexion !', '/icons/first-login.svg', 'FIRST_COURSE', 1, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Étudiant Assidu', 'Terminez votre premier cours', '/icons/first-course.svg', 'COURS_COMPLETED', 1, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Quiz Master', 'Réussissez votre premier quiz', '/icons/first-quiz.svg', 'QUIZ_PASSED', 1, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Perfectionniste', 'Obtenez un score parfait à un quiz', '/icons/perfect-score.svg', 'PERFECT_SCORE', 1, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Marathonien', 'Connectez-vous 7 jours consécutifs', '/icons/streak.svg', 'STREAK_DAYS', 7, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Collectionneur', 'Terminez 5 cours', '/icons/collector.svg', 'COURS_COMPLETED', 5, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Expert Quiz', 'Réussissez 10 quiz', '/icons/quiz-expert.svg', 'QUIZ_PASSED', 10, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Montée en Niveau', 'Atteignez le niveau 5', '/icons/level-up.svg', 'LEVEL_REACHED', 5, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Chasseur de Points', 'Gagnez 1000 points XP', '/icons/xp-hunter.svg', 'XP_EARNED', 1000, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Défi Relevé', 'Terminez votre premier défi', '/icons/challenge.svg', 'CHALLENGE_COMPLETED', 1, TRUE, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);