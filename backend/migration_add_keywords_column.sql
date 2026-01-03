-- Migration pour ajouter la colonne keywords si elle n'existe pas déjà
-- Cette migration est idempotente (peut être exécutée plusieurs fois sans problème)

-- Vérifier si la colonne existe déjà
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'cours' 
  AND COLUMN_NAME = 'keywords' 
  AND TABLE_SCHEMA = DATABASE();

-- Ajouter la colonne keywords si elle n'existe pas
-- (Cette commande échouera silencieusement si la colonne existe déjà)
ALTER TABLE cours ADD COLUMN keywords VARCHAR(500) NULL;

-- Vérifier que la colonne a été ajoutée
DESCRIBE cours;