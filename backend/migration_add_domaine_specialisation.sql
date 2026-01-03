-- Migration pour ajouter le domaine de spécialisation aux formateurs
-- Ajouter la colonne domaine_specialisation à la table users

ALTER TABLE users ADD COLUMN domaine_specialisation VARCHAR(100) NULL;

-- Mettre à jour les formateurs existants avec des domaines par défaut
UPDATE users SET domaine_specialisation = 'Développement Web' 
WHERE role = 'FORMATEUR' AND domaine_specialisation IS NULL;

-- Vérifier les résultats
SELECT id, email, first_name, last_name, role, domaine_specialisation 
FROM users 
WHERE role = 'FORMATEUR';

-- Statistiques
SELECT 
    role,
    domaine_specialisation,
    COUNT(*) as nombre_utilisateurs
FROM users 
WHERE archived = false
GROUP BY role, domaine_specialisation
ORDER BY role, domaine_specialisation;