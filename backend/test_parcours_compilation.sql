-- Script de test pour vérifier la compilation des parcours d'apprentissage
-- Ce script vérifie que toutes les tables nécessaires existent

-- 1. Vérifier l'existence des tables
SELECT 'Vérification des tables parcours' as test_name;

SELECT 
    TABLE_NAME, 
    TABLE_ROWS,
    CREATE_TIME
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME IN (
    'parcours_apprentissage',
    'parcours_etapes', 
    'parcours_conditions',
    'parcours_inscriptions'
)
ORDER BY TABLE_NAME;

-- 2. Vérifier la structure de la table principale
SELECT 'Structure table parcours_apprentissage' as test_name;
DESCRIBE parcours_apprentissage;

-- 3. Vérifier la structure de la table des étapes
SELECT 'Structure table parcours_etapes' as test_name;
DESCRIBE parcours_etapes;

-- 4. Vérifier les contraintes de clés étrangères
SELECT 'Contraintes de clés étrangères' as test_name;
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME LIKE 'parcours%'
AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, COLUMN_NAME;

-- 5. Vérifier les index
SELECT 'Index sur les tables parcours' as test_name;
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME LIKE 'parcours%'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;