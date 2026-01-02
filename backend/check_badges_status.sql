-- Vérifier le statut des badges dans la base de données
USE 9awi_niveau;

-- Voir tous les badges et leur statut
SELECT id, name, is_active, created_at FROM badges ORDER BY id;

-- Compter les badges actifs/inactifs
SELECT 
    COUNT(*) as total_badges,
    SUM(is_active) as active_badges,
    COUNT(*) - SUM(is_active) as inactive_badges
FROM badges;

-- Mettre tous les badges à actif (solution temporaire)
-- UPDATE badges SET is_active = 1 WHERE is_active = 0;