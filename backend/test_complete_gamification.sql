-- Test complet de l'automatisation de gamification

-- 1. Créer la table user_logins si elle n'existe pas
CREATE TABLE IF NOT EXISTS user_logins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    login_time BIGINT NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_login_time (user_id, login_time)
);

-- 2. Vérifier les badges par défaut avec tous les types nécessaires
SELECT 'Vérification des badges par défaut...' as status;

-- Insérer les badges manquants s'ils n'existent pas
INSERT IGNORE INTO badges (name, description, icon_url, criteria_type, criteria_value, is_active, created_at) VALUES
('Premier Pas', 'Félicitations pour votre première leçon terminée !', '/icons/first-step.svg', 'FIRST_COURSE', 1, TRUE, UNIX_TIMESTAMP() * 1000),
('Bienvenue', 'Bienvenue dans votre parcours d\'apprentissage !', '/icons/welcome.svg', 'FIRST_COURSE', 1, TRUE, UNIX_TIMESTAMP() * 1000),
('Assidu', 'Connectez-vous 7 jours consécutifs', '/icons/streak-7.svg', 'STREAK_DAYS', 7, TRUE, UNIX_TIMESTAMP() * 1000),
('Marathonien', 'Connectez-vous 30 jours consécutifs', '/icons/streak-30.svg', 'STREAK_DAYS', 30, TRUE, UNIX_TIMESTAMP() * 1000),
('Perfectionniste Plus', 'Obtenez 5 scores parfaits', '/icons/perfect-5.svg', 'PERFECT_SCORE', 5, TRUE, UNIX_TIMESTAMP() * 1000),
('Explorateur', 'Terminez 3 cours différents', '/icons/explorer.svg', 'COURS_COMPLETED', 3, TRUE, UNIX_TIMESTAMP() * 1000),
('Chasseur XP', 'Gagnez 500 points XP', '/icons/xp-500.svg', 'XP_EARNED', 500, TRUE, UNIX_TIMESTAMP() * 1000),
('Maître Quiz', 'Réussissez 20 quiz', '/icons/quiz-master.svg', 'QUIZ_PASSED', 20, TRUE, UNIX_TIMESTAMP() * 1000);

-- 3. Créer des défis par défaut pour tester l'automatisation
INSERT IGNORE INTO challenges (name, description, challenge_type, target_value, xp_reward, start_date, end_date, is_active, created_at) VALUES
('Défi Quotidien - Quiz', 'Réussissez 1 quiz aujourd\'hui', 'PASS_QUIZZES', 1, 20, UNIX_TIMESTAMP() * 1000, (UNIX_TIMESTAMP() + 86400) * 1000, TRUE, UNIX_TIMESTAMP() * 1000),
('Défi Hebdomadaire - Cours', 'Terminez 1 cours cette semaine', 'COMPLETE_COURSES', 1, 100, UNIX_TIMESTAMP() * 1000, (UNIX_TIMESTAMP() + 604800) * 1000, TRUE, UNIX_TIMESTAMP() * 1000),
('Défi XP - 100 Points', 'Gagnez 100 points XP', 'EARN_XP', 100, 50, UNIX_TIMESTAMP() * 1000, (UNIX_TIMESTAMP() + 2592000) * 1000, TRUE, UNIX_TIMESTAMP() * 1000),
('Défi Connexion', 'Connectez-vous 5 jours consécutifs', 'DAILY_LOGIN', 5, 75, UNIX_TIMESTAMP() * 1000, (UNIX_TIMESTAMP() + 1209600) * 1000, TRUE, UNIX_TIMESTAMP() * 1000),
('Défi Perfectionniste', 'Obtenez 3 scores parfaits', 'PERFECT_SCORES', 3, 150, UNIX_TIMESTAMP() * 1000, (UNIX_TIMESTAMP() + 2592000) * 1000, TRUE, UNIX_TIMESTAMP() * 1000);

-- 4. Vérifier les statistiques actuelles
SELECT 'Statistiques avant test:' as status;

SELECT 
    'Utilisateurs' as type,
    COUNT(*) as count
FROM users 
WHERE archived = false

UNION ALL

SELECT 
    'Profils XP' as type,
    COUNT(*) as count
FROM user_xp

UNION ALL

SELECT 
    'Badges obtenus' as type,
    COUNT(*) as count
FROM user_badges

UNION ALL

SELECT 
    'Défis actifs' as type,
    COUNT(*) as count
FROM challenges 
WHERE is_active = true

UNION ALL

SELECT 
    'Participations défis' as type,
    COUNT(*) as count
FROM user_challenges;

-- 5. Afficher les badges disponibles par type
SELECT 'Badges disponibles par type:' as status;

SELECT 
    criteria_type,
    COUNT(*) as badge_count,
    GROUP_CONCAT(name SEPARATOR ', ') as badge_names
FROM badges 
WHERE is_active = true 
GROUP BY criteria_type
ORDER BY criteria_type;

-- 6. Afficher les défis actifs
SELECT 'Défis actifs:' as status;

SELECT 
    name,
    challenge_type,
    target_value,
    xp_reward,
    FROM_UNIXTIME(start_date / 1000) as start_date,
    FROM_UNIXTIME(end_date / 1000) as end_date
FROM challenges 
WHERE is_active = true 
ORDER BY created_at DESC;

-- 7. Test de cohérence des données
SELECT 'Test de cohérence:' as status;

-- Utilisateurs sans profil XP
SELECT 
    CONCAT('Utilisateurs sans profil XP: ', COUNT(*)) as issue
FROM users u 
LEFT JOIN user_xp ux ON u.id = ux.user_id 
WHERE u.archived = false AND ux.id IS NULL

UNION ALL

-- Doublons dans user_xp
SELECT 
    CONCAT('Doublons user_xp: ', COUNT(*)) as issue
FROM (
    SELECT user_id 
    FROM user_xp 
    GROUP BY user_id 
    HAVING COUNT(*) > 1
) duplicates

UNION ALL

-- Badges inactifs
SELECT 
    CONCAT('Badges inactifs: ', COUNT(*)) as issue
FROM badges 
WHERE is_active = false;

SELECT 'Test complet terminé ! Système prêt pour l\'automatisation.' as status;