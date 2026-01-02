-- Correction de la taille de la colonne challenge_type
USE 9awi_niveau;

-- Augmenter la taille de la colonne challenge_type
ALTER TABLE challenges MODIFY COLUMN challenge_type VARCHAR(100) NOT NULL;

-- Augmenter aussi la taille de criteria_type dans badges si nécessaire
ALTER TABLE badges MODIFY COLUMN criteria_type VARCHAR(100) NOT NULL;

-- Vérifier les modifications
DESCRIBE challenges;
DESCRIBE badges;