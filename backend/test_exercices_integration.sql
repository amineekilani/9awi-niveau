-- Test d'intégration des exercices interactifs

-- 1. Exécuter les migrations
SOURCE migration_add_exercices_interactifs.sql;
SOURCE migration_add_resultat_exercice.sql;

-- 2. Vérifier la structure des tables
DESCRIBE exercice;
DESCRIBE exercice_element;
DESCRIBE resultat_exercice;

-- 3. Insérer des données de test
-- Supposons qu'il existe un module avec ID 1
INSERT INTO exercice (titre, description, type_exercice, module_id, created_at, updated_at) 
VALUES ('Exercice de grammaire', 'Complétez les phrases avec les bons mots', 'FILL_BLANK', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

SET @exercice_id = LAST_INSERT_ID();

-- Éléments pour texte à trous
INSERT INTO exercice_element (contenu, type_element, position_ordre, reponse_correcte, exercice_id, created_at) VALUES
('Le chat', 'TEXT', 1, NULL, @exercice_id, UNIX_TIMESTAMP() * 1000),
('', 'BLANK', 2, 'mange', @exercice_id, UNIX_TIMESTAMP() * 1000),
('sa nourriture dans', 'TEXT', 3, NULL, @exercice_id, UNIX_TIMESTAMP() * 1000),
('', 'BLANK', 4, 'la cuisine', @exercice_id, UNIX_TIMESTAMP() * 1000),
('.', 'TEXT', 5, NULL, @exercice_id, UNIX_TIMESTAMP() * 1000);

-- Exercice drag and drop
INSERT INTO exercice (titre, description, type_exercice, module_id, created_at, updated_at) 
VALUES ('Classement des animaux', 'Glissez les animaux dans les bonnes catégories', 'DRAG_DROP', 2, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

SET @exercice_id2 = LAST_INSERT_ID();

INSERT INTO exercice_element (contenu, type_element, position_ordre, reponse_correcte, exercice_id, created_at) VALUES
('Chat', 'DRAGGABLE', 1, NULL, @exercice_id2, UNIX_TIMESTAMP() * 1000),
('Chien', 'DRAGGABLE', 2, NULL, @exercice_id2, UNIX_TIMESTAMP() * 1000),
('Aigle', 'DRAGGABLE', 3, NULL, @exercice_id2, UNIX_TIMESTAMP() * 1000),
('Mammifères:', 'DROP_ZONE', 4, 'Chat', @exercice_id2, UNIX_TIMESTAMP() * 1000),
('Oiseaux:', 'DROP_ZONE', 5, 'Aigle', @exercice_id2, UNIX_TIMESTAMP() * 1000);

-- Exercice d'appariement
INSERT INTO exercice (titre, description, type_exercice, module_id, created_at, updated_at) 
VALUES ('Capitales du monde', 'Associez chaque pays à sa capitale', 'MATCHING', 3, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

SET @exercice_id3 = LAST_INSERT_ID();

INSERT INTO exercice_element (contenu, type_element, position_ordre, reponse_correcte, options, exercice_id, created_at) VALUES
('France', 'MATCH_ITEM', 1, 'Paris', '["Paris", "Londres", "Berlin", "Madrid"]', @exercice_id3, UNIX_TIMESTAMP() * 1000),
('Allemagne', 'MATCH_ITEM', 2, 'Berlin', '["Paris", "Londres", "Berlin", "Madrid"]', @exercice_id3, UNIX_TIMESTAMP() * 1000),
('Espagne', 'MATCH_ITEM', 3, 'Madrid', '["Paris", "Londres", "Berlin", "Madrid"]', @exercice_id3, UNIX_TIMESTAMP() * 1000);

-- 4. Vérifier les données insérées
SELECT * FROM exercice;
SELECT * FROM exercice_element ORDER BY exercice_id, position_ordre;

-- 5. Test de requêtes typiques
SELECT e.*, COUNT(ee.id) as nb_elements 
FROM exercice e 
LEFT JOIN exercice_element ee ON e.id = ee.exercice_id 
GROUP BY e.id;

SELECT ee.* FROM exercice_element ee 
JOIN exercice e ON ee.exercice_id = e.id 
WHERE e.type_exercice = 'FILL_BLANK' 
ORDER BY ee.position_ordre;