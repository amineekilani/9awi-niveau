-- Vérifier si la colonne domaine_specialisation existe
DESCRIBE users;

-- Ou alternativement :
SHOW COLUMNS FROM users LIKE 'domaine_specialisation';

-- Vérifier les données existantes
SELECT id, email, role, domaine_specialisation 
FROM users 
LIMIT 10;