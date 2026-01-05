-- Test complet du système d'exercices interactifs

-- 1. Exécuter les migrations
SOURCE migration_add_exercices_interactifs.sql;
SOURCE migration_add_resultat_exercice.sql;

-- 2. Vérifier la structure des tables
SHOW TABLES LIKE '%exercice%';
DESCRIBE exercice;
DESCRIBE exercice_element;
DESCRIBE resultat_exercice;

-- 3. Test d'insertion d'un exercice de type FILL_BLANK
INSERT INTO exercice (titre, description, type_exercice, module_id, created_at, updated_at) 
VALUES ('Test Grammaire', 'Exercice de test pour les blancs', 'FILL_BLANK', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

SET @exercice_id = LAST_INSERT_ID();

-- Éléments pour texte à trous
INSERT INTO exercice_element (contenu, type_element, position_ordre, reponse_correcte, exercice_id, created_at) VALUES
('Le chat', 'TEXT', 1, NULL, @exercice_id, UNIX_TIMESTAMP() * 1000),
('', 'BLANK', 2, 'mange', @exercice_id, UNIX_TIMESTAMP() * 1000),
('sa nourriture dans', 'TEXT', 3, NULL, @exercice_id, UNIX_TIMESTAMP() * 1000),
('', 'BLANK', 4, 'la cuisine', @exercice_id, UNIX_TIMESTAMP() * 1000),
('.', 'TEXT', 5, NULL, @exercice_id, UNIX_TIMESTAMP() * 1000);

-- 4. Test d'insertion d'un exercice de type DRAG_DROP
INSERT INTO exercice (titre, description, type_exercice, module_id, created_at, updated_at) 
VALUES ('Classification Animaux', 'Glissez les animaux dans les bonnes catégories', 'DRAG_DROP', 2, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

SET @exercice_id2 = LAST_INSERT_ID();

INSERT INTO exercice_element (contenu, type_element, position_ordre, reponse_correcte, exercice_id, created_at) VALUES
('Chat', 'DRAGGABLE', 1, NULL, @exercice_id2, UNIX_TIMESTAMP() * 1000),
('Chien', 'DRAGGABLE', 2, NULL, @exercice_id2, UNIX_TIMESTAMP() * 1000),
('Aigle', 'DRAGGABLE', 3, NULL, @exercice_id2, UNIX_TIMESTAMP() * 1000),
('Poisson', 'DRAGGABLE', 4, NULL, @exercice_id2, UNIX_TIMESTAMP() * 1000),
('Mammifères:', 'DROP_ZONE', 5, 'Chat', @exercice_id2, UNIX_TIMESTAMP() * 1000),
('Mammifères:', 'DROP_ZONE', 6, 'Chien', @exercice_id2, UNIX_TIMESTAMP() * 1000),
('Oiseaux:', 'DROP_ZONE', 7, 'Aigle', @exercice_id2, UNIX_TIMESTAMP() * 1000),
('Poissons:', 'DROP_ZONE', 8, 'Poisson', @exercice_id2, UNIX_TIMESTAMP() * 1000);

-- 6. Vérifier les données insérées
SELECT 'EXERCICES CRÉÉS:' as info;
SELECT e.id, e.titre, e.type_exercice, COUNT(ee.id) as nb_elements 
FROM exercice e 
LEFT JOIN exercice_element ee ON e.id = ee.exercice_id 
GROUP BY e.id, e.titre, e.type_exercice;

SELECT 'ÉLÉMENTS PAR EXERCICE:' as info;
SELECT e.titre, ee.type_element, ee.contenu, ee.reponse_correcte, ee.position_ordre
FROM exercice e 
JOIN exercice_element ee ON e.id = ee.exercice_id 
ORDER BY e.id, ee.position_ordre;

-- 7. Test de requêtes typiques utilisées par l'API
SELECT 'REQUÊTE: Exercice par module' as info;
SELECT * FROM exercice WHERE module_id = 1;

SELECT 'REQUÊTE: Éléments d\'un exercice' as info;
SELECT * FROM exercice_element WHERE exercice_id = @exercice_id ORDER BY position_ordre;

-- 8. Test d'insertion d'un résultat
INSERT INTO resultat_exercice (user_id, exercice_id, score, date_passed, nombre_elements, reponses_correctes, temps_passe, reponses_details)
VALUES (1, @exercice_id, 85.5, UNIX_TIMESTAMP() * 1000, 2, 2, 120, '{"details": "test"}');

SELECT 'RÉSULTAT INSÉRÉ:' as info;
SELECT * FROM resultat_exercice WHERE exercice_id = @exercice_id;

SELECT 'TEST TERMINÉ AVEC SUCCÈS!' as status;