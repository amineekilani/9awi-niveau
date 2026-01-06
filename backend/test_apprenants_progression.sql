-- Test des données de progression des apprenants
-- Ce script vérifie que les données nécessaires existent pour l'affichage

-- 1. Vérifier qu'il y a des cours avec des apprenants inscrits
SELECT 
    'Cours avec apprenants inscrits' as section,
    c.id as cours_id,
    c.titre,
    COUNT(e.id) as apprenants_inscrits,
    AVG(e.progress) as progression_moyenne
FROM cours c
JOIN enrollments e ON c.id = e.cours_id
WHERE c.archived = FALSE
GROUP BY c.id, c.titre
HAVING COUNT(e.id) > 0
ORDER BY apprenants_inscrits DESC
LIMIT 5;

-- 2. Vérifier les données d'un cours spécifique
SET @cours_test = (
    SELECT c.id 
    FROM cours c
    JOIN enrollments e ON c.id = e.cours_id
    WHERE c.archived = FALSE
    GROUP BY c.id
    HAVING COUNT(e.id) > 0
    ORDER BY COUNT(e.id) DESC
    LIMIT 1
);

SELECT 
    'Détails du cours test' as section,
    c.id,
    c.titre,
    c.description,
    COUNT(DISTINCT e.id) as total_apprenants,
    COUNT(DISTINCT m.id) as total_modules,
    COUNT(DISTINCT l.id) as total_lecons
FROM cours c
LEFT JOIN enrollments e ON c.id = e.cours_id
LEFT JOIN modules m ON c.id = m.cours_id
LEFT JOIN lecons l ON m.id = l.module_id
WHERE c.id = @cours_test
GROUP BY c.id, c.titre, c.description;

-- 3. Vérifier les apprenants de ce cours
SELECT 
    'Apprenants du cours test' as section,
    u.id as user_id,
    u.first_name as prenom,
    u.last_name as nom,
    u.email,
    e.progress as progression_pourcent,
    FROM_UNIXTIME(e.enrolled_at/1000) as date_inscription,
    FROM_UNIXTIME(e.last_accessed_at/1000) as derniere_activite,
    COUNT(DISTINCT lc.id) as lecons_completees
FROM enrollments e
JOIN users u ON e.user_id = u.id
LEFT JOIN lecon_completions lc ON e.id = lc.enrollment_id
WHERE e.cours_id = @cours_test
GROUP BY u.id, u.first_name, u.last_name, u.email, e.progress, e.enrolled_at, e.last_accessed_at
ORDER BY e.progress DESC;

-- 4. Vérifier les modules et leçons du cours
SELECT 
    'Modules et leçons du cours test' as section,
    m.id as module_id,
    m.titre as module_titre,
    m.ordre as module_ordre,
    COUNT(l.id) as nombre_lecons
FROM modules m
LEFT JOIN lecons l ON m.id = l.module_id
WHERE m.cours_id = @cours_test
GROUP BY m.id, m.titre, m.ordre
ORDER BY m.ordre;

-- 5. Vérifier les quiz et résultats
SELECT 
    'Quiz et résultats du cours test' as section,
    m.titre as module_titre,
    q.id as quiz_id,
    q.titre as quiz_titre,
    COUNT(DISTINCT rq.id) as nombre_tentatives,
    AVG(rq.score) as score_moyen,
    MAX(rq.score) as meilleur_score
FROM modules m
LEFT JOIN quiz q ON m.id = q.module_id
LEFT JOIN resultat_quiz rq ON q.id = rq.quiz_id
WHERE m.cours_id = @cours_test
GROUP BY m.titre, q.id, q.titre
ORDER BY m.ordre;

-- 6. Créer des données de test si nécessaire
-- Créer un utilisateur test s'il n'existe pas
INSERT IGNORE INTO users (email, password, first_name, last_name, role, created_at, updated_at) VALUES
('apprenant.test@example.com', '$2a$10$dummy', 'Jean', 'Dupont', 'APPRENANT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- Inscrire l'utilisateur test au cours s'il ne l'est pas déjà
INSERT IGNORE INTO enrollments (user_id, cours_id, enrolled_at, progress, last_accessed_at)
SELECT 
    u.id,
    @cours_test,
    UNIX_TIMESTAMP() * 1000 - 86400000, -- Il y a 1 jour
    25.0, -- 25% de progression
    UNIX_TIMESTAMP() * 1000 - 3600000 -- Il y a 1 heure
FROM users u
WHERE u.email = 'apprenant.test@example.com'
AND @cours_test IS NOT NULL;

-- Marquer quelques leçons comme complétées
SET @enrollment_test = (
    SELECT e.id 
    FROM enrollments e
    JOIN users u ON e.user_id = u.id
    WHERE u.email = 'apprenant.test@example.com'
    AND e.cours_id = @cours_test
);

INSERT IGNORE INTO lecon_completions (enrollment_id, lecon_id, completed_at)
SELECT 
    @enrollment_test,
    l.id,
    UNIX_TIMESTAMP() * 1000 - (RAND() * 86400000) -- Complétée dans les dernières 24h
FROM lecons l
JOIN modules m ON l.module_id = m.id
WHERE m.cours_id = @cours_test
AND @enrollment_test IS NOT NULL
ORDER BY l.ordre
LIMIT 2; -- Compléter 2 leçons

-- 7. Vérifier le résultat final
SELECT 
    'Données finales pour test API' as section,
    u.first_name,
    u.last_name,
    u.email,
    e.progress,
    COUNT(DISTINCT lc.id) as lecons_completees_count,
    FROM_UNIXTIME(e.enrolled_at/1000) as inscription,
    FROM_UNIXTIME(e.last_accessed_at/1000) as derniere_activite
FROM enrollments e
JOIN users u ON e.user_id = u.id
LEFT JOIN lecon_completions lc ON e.id = lc.enrollment_id
WHERE e.cours_id = @cours_test
GROUP BY u.id, u.first_name, u.last_name, u.email, e.progress, e.enrolled_at, e.last_accessed_at
ORDER BY e.progress DESC;

-- 8. Instructions de test
SELECT CONCAT('
🧪 TEST DE LA PROGRESSION DES APPRENANTS:

Cours de test: ID ', COALESCE(@cours_test, 'AUCUN'), '

POUR TESTER:
1. Connectez-vous en tant que formateur
2. Allez sur les détails du cours ID ', COALESCE(@cours_test, 'AUCUN'), '
3. Cliquez sur "Apprenants inscrits"
4. Vérifiez que les données s\'affichent correctement:
   ✅ Pourcentage de progression
   ✅ Nombre de leçons complétées
   ✅ Date d\'inscription
   ✅ Dernière activité
   ✅ Détails par module

ENDPOINT À TESTER:
GET /api/enrollments/cours/', COALESCE(@cours_test, 'X'), '/apprenants

Si les données ne s\'affichent pas:
- Vérifiez les logs du backend
- Vérifiez que l\'endpoint retourne des données
- Vérifiez la console du navigateur pour les erreurs
') as instructions;