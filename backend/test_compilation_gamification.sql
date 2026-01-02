-- Test rapide après correction de compilation

-- 1. Vérifier que les tables existent
SELECT 'Test des tables de gamification...' as status;

SELECT 
    table_name,
    CASE 
        WHEN table_name IS NOT NULL THEN 'OK'
        ELSE 'MANQUANT'
    END as status
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name IN ('levels', 'badges', 'user_xp', 'user_badges', 'challenges', 'user_challenges')
ORDER BY table_name;

-- 2. Créer la table user_logins si elle n'existe pas
CREATE TABLE IF NOT EXISTS user_logins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    login_time BIGINT NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_login_time (user_id, login_time)
);

-- 3. Vérifier les données par défaut
SELECT 'Données par défaut:' as status;

SELECT 
    'Niveaux' as type,
    COUNT(*) as count
FROM levels

UNION ALL

SELECT 
    'Badges' as type,
    COUNT(*) as count
FROM badges

UNION ALL

SELECT 
    'Défis' as type,
    COUNT(*) as count
FROM challenges;

-- 4. Nettoyer les doublons user_xp
DELETE ux1 FROM user_xp ux1
INNER JOIN user_xp ux2 
WHERE ux1.id < ux2.id 
AND ux1.user_id = ux2.user_id;

-- 5. Créer les profils XP manquants
INSERT IGNORE INTO user_xp (user_id, total_xp, current_level, xp_to_next_level, last_updated)
SELECT u.id, 0, 1, 100, UNIX_TIMESTAMP() * 1000
FROM users u 
LEFT JOIN user_xp ux ON u.id = ux.user_id 
WHERE u.archived = false AND ux.id IS NULL;

SELECT 'Système prêt pour les tests !' as status;