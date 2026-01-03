-- Migration pour ajouter le niveau de difficulté aux cours
-- Date: 2026-01-03

-- Ajouter la colonne niveau_difficulte à la table cours
ALTER TABLE cours 
ADD COLUMN niveau_difficulte VARCHAR(20) NOT NULL DEFAULT 'DEBUTANT';

-- Ajouter une contrainte pour valider les valeurs
ALTER TABLE cours 
ADD CONSTRAINT chk_niveau_difficulte 
CHECK (niveau_difficulte IN ('DEBUTANT', 'INTERMEDIAIRE', 'AVANCE', 'EXPERT'));

-- Mise à jour intelligente des cours existants basée sur les mots-clés
UPDATE cours 
SET niveau_difficulte = 'INTERMEDIAIRE' 
WHERE LOWER(keywords) LIKE '%intermédiaire%' 
   OR LOWER(keywords) LIKE '%intermediate%'
   OR LOWER(titre) LIKE '%intermédiaire%'
   OR LOWER(description) LIKE '%intermédiaire%';

UPDATE cours 
SET niveau_difficulte = 'AVANCE' 
WHERE LOWER(keywords) LIKE '%avancé%' 
   OR LOWER(keywords) LIKE '%advanced%'
   OR LOWER(keywords) LIKE '%expert%'
   OR LOWER(titre) LIKE '%avancé%'
   OR LOWER(description) LIKE '%avancé%';

UPDATE cours 
SET niveau_difficulte = 'EXPERT' 
WHERE LOWER(keywords) LIKE '%expert%' 
   OR LOWER(keywords) LIKE '%maître%'
   OR LOWER(keywords) LIKE '%master%'
   OR LOWER(titre) LIKE '%expert%'
   OR LOWER(description) LIKE '%expert%';

-- Mise à jour basée sur certaines catégories techniques
UPDATE cours 
SET niveau_difficulte = 'AVANCE' 
WHERE LOWER(categorie) IN ('architecture', 'sécurité', 'devops', 'machine learning');

UPDATE cours 
SET niveau_difficulte = 'INTERMEDIAIRE' 
WHERE LOWER(categorie) IN ('backend', 'frontend', 'base de données', 'api');

-- Afficher un résumé de la migration
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