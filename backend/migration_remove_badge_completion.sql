-- Migration pour supprimer le champ badge_completion des parcours
-- Date: 2026-01-05
-- Description: Suppression du système de badges de completion des parcours

-- Vérifier si la colonne existe avant de la supprimer
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'parcours_apprentissage'
    AND COLUMN_NAME = 'badge_completion'
);

-- Supprimer la colonne si elle existe
SET @sql = IF(@column_exists > 0,
    'ALTER TABLE parcours_apprentissage DROP COLUMN badge_completion;',
    'SELECT "Colonne badge_completion n\'existe pas" as message;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Vérification finale
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN 'SUCCESS: Colonne badge_completion supprimée avec succès'
        ELSE 'WARNING: Colonne badge_completion existe encore'
    END as status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'parcours_apprentissage'
AND COLUMN_NAME = 'badge_completion';