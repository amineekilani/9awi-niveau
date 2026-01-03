-- Script de test pour la migration des niveaux de difficulté
-- Ce script vérifie que la migration s'est bien passée

-- 1. Vérifier que la colonne existe
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'cours' AND COLUMN_NAME = 'niveau_difficulte';

-- 2. Vérifier la contrainte
SELECT CONSTRAINT_NAME, CHECK_CLAUSE
FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS 
WHERE CONSTRAINT_NAME = 'chk_niveau_difficulte';

-- 3. Compter les cours par niveau
SELECT 
    niveau_difficulte,
    COUNT(*) as nombre_cours,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM cours), 2) as pourcentage
FROM cours 
GROUP BY niveau_difficulte
ORDER BY 
    CASE niveau_difficulte 
        WHEN 'DEBUTANT' THEN 1
        WHEN 'INTERMEDIAIRE' THEN 2
        WHEN 'AVANCE' THEN 3
        WHEN 'EXPERT' THEN 4
    END;

-- 4. Afficher quelques exemples de cours avec leur niveau
SELECT 
    id,
    titre,
    categorie,
    niveau_difficulte,
    keywords
FROM cours 
ORDER BY niveau_difficulte, titre
LIMIT 10;

-- 5. Vérifier qu'il n'y a pas de valeurs NULL
SELECT COUNT(*) as cours_sans_niveau
FROM cours 
WHERE niveau_difficulte IS NULL;

-- 6. Test d'insertion d'un nouveau cours avec niveau
INSERT INTO cours (titre, description, niveau_difficulte, formateur_id, created_at, updated_at)
VALUES ('Test Niveau Expert', 'Cours de test pour niveau expert', 'EXPERT', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- Vérifier l'insertion
SELECT * FROM cours WHERE titre = 'Test Niveau Expert';

-- Nettoyer le test
DELETE FROM cours WHERE titre = 'Test Niveau Expert';