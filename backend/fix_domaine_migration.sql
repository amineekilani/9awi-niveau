-- Script pour corriger le problème de domaine de spécialisation
-- Exécuter ce script dans votre base de données

-- 1. Vérifier si la colonne existe déjà
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'users' 
AND COLUMN_NAME = 'domaine_specialisation';

-- 2. Ajouter la colonne si elle n'existe pas
-- (Décommentez la ligne suivante si la colonne n'existe pas)
-- ALTER TABLE users ADD COLUMN domaine_specialisation VARCHAR(100) NULL;

-- 3. Mettre à jour les formateurs existants avec un domaine par défaut
UPDATE users 
SET domaine_specialisation = 'Développement Web' 
WHERE role = 'FORMATEUR' 
AND (domaine_specialisation IS NULL OR domaine_specialisation = '');

-- 4. Vérifier le résultat
SELECT id, email, first_name, last_name, role, domaine_specialisation 
FROM users 
ORDER BY role, id;