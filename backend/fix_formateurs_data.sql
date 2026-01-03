-- Script pour corriger les données des formateurs
-- Vérifier les formateurs avec des données manquantes

-- 1. Vérifier les formateurs sans nom/prénom
SELECT id, email, first_name, last_name, domaine_specialisation, role
FROM users 
WHERE role = 'FORMATEUR' 
AND (first_name IS NULL OR last_name IS NULL OR first_name = '' OR last_name = '');

-- 2. Vérifier les formateurs sans domaine
SELECT id, email, first_name, last_name, domaine_specialisation, role
FROM users 
WHERE role = 'FORMATEUR' 
AND (domaine_specialisation IS NULL OR domaine_specialisation = '');

-- 3. Corriger les formateurs sans prénom/nom (utiliser l'email comme base)
UPDATE users 
SET first_name = SUBSTRING_INDEX(SUBSTRING_INDEX(email, '@', 1), '.', 1),
    last_name = SUBSTRING_INDEX(SUBSTRING_INDEX(email, '@', 1), '.', -1)
WHERE role = 'FORMATEUR' 
AND (first_name IS NULL OR last_name IS NULL OR first_name = '' OR last_name = '');

-- 4. Corriger les formateurs sans domaine
UPDATE users 
SET domaine_specialisation = 'Développement Web'
WHERE role = 'FORMATEUR' 
AND (domaine_specialisation IS NULL OR domaine_specialisation = '');

-- 5. Vérifier les résultats
SELECT id, email, first_name, last_name, domaine_specialisation, role
FROM users 
WHERE role = 'FORMATEUR'
ORDER BY id;