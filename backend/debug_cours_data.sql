-- Script de diagnostic pour identifier le problème des cours vides

-- 1. Vérifier la structure de la table users
DESCRIBE users;

-- 2. Vérifier tous les formateurs et leurs données
SELECT 
    id,
    email,
    first_name,
    last_name,
    domaine_specialisation,
    role,
    CONCAT(COALESCE(first_name, ''), ' ', COALESCE(last_name, '')) as nom_complet
FROM users 
WHERE role = 'FORMATEUR'
ORDER BY id;

-- 3. Vérifier la structure de la table cours
DESCRIBE cours;

-- 4. Vérifier tous les cours avec leurs formateurs
SELECT 
    c.id as cours_id,
    c.titre,
    c.description,
    c.formateur_id,
    u.email as formateur_email,
    u.first_name,
    u.last_name,
    u.domaine_specialisation,
    CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, '')) as nom_formateur
FROM cours c
LEFT JOIN users u ON c.formateur_id = u.id
WHERE c.archived = false
ORDER BY c.id;

-- 5. Identifier les cours avec des formateurs ayant des données manquantes
SELECT 
    c.id as cours_id,
    c.titre,
    u.email,
    u.first_name,
    u.last_name,
    u.domaine_specialisation,
    CASE 
        WHEN u.first_name IS NULL OR u.first_name = '' THEN 'PRENOM_MANQUANT'
        ELSE 'OK'
    END as status_prenom,
    CASE 
        WHEN u.last_name IS NULL OR u.last_name = '' THEN 'NOM_MANQUANT'
        ELSE 'OK'
    END as status_nom,
    CASE 
        WHEN u.domaine_specialisation IS NULL OR u.domaine_specialisation = '' THEN 'DOMAINE_MANQUANT'
        ELSE 'OK'
    END as status_domaine
FROM cours c
LEFT JOIN users u ON c.formateur_id = u.id
WHERE c.archived = false
ORDER BY c.id;