-- Script pour corriger et tester la progression des leçons
-- Ce script s'assure que les données de progression sont cohérentes

-- 1. Vérifier l'état actuel des enrollments et leçons complétées
SELECT 
    'État actuel des progressions' as section,
    e.id as enrollment_id,
    u.email,
    c.titre as cours,
    e.progress as progression_pourcent,
    COUNT(DISTINCT lc.id) as lecons_completees_reelles,
    (SELECT COUNT(*) FROM lecons l 
     JOIN modules m ON l.module_id = m.id 
     WHERE m.cours_id = c.id) as total_lecons_cours
FROM enrollments e
JOIN users u ON e.user_id = u.id
JOIN cours c ON e.cours_id = c.id
LEFT JOIN lecon_completions lc ON e.id = lc.enrollment_id
WHERE u.role = 'APPRENANT'
GROUP BY e.id, u.email, c.titre, e.progress
HAVING total_lecons_cours > 0
ORDER BY e.progress DESC
LIMIT 10;

-- 2. Identifier les incohérences (progression 100% mais pas toutes les leçons complétées)
SELECT 
    'Incohérences détectées' as section,
    e.id as enrollment_id,
    u.email,
    c.titre,
    e.progress as progression_affichee,
    COUNT(DISTINCT lc.id) as lecons_completees,
    (SELECT COUNT(*) FROM lecons l 
     JOIN modules m ON l.module_id = m.id 
     WHERE m.cours_id = c.id) as total_lecons,
    CASE 
        WHEN e.progress >= 100 AND COUNT(DISTINCT lc.id) < (SELECT COUNT(*) FROM lecons l JOIN modules m ON l.module_id = m.id WHERE m.cours_id = c.id)
        THEN 'INCOHÉRENCE: 100% mais leçons manquantes'
        WHEN e.progress < 100 AND COUNT(DISTINCT lc.id) = (SELECT COUNT(*) FROM lecons l JOIN modules m ON l.module_id = m.id WHERE m.cours_id = c.id)
        THEN 'INCOHÉRENCE: Toutes leçons complétées mais pas 100%'
        ELSE 'COHÉRENT'
    END as statut
FROM enrollments e
JOIN users u ON e.user_id = u.id
JOIN cours c ON e.cours_id = c.id
LEFT JOIN lecon_completions lc ON e.id = lc.enrollment_id
WHERE u.role = 'APPRENANT'
GROUP BY e.id, u.email, c.titre, e.progress
HAVING total_lecons > 0
ORDER BY 
    CASE WHEN statut LIKE 'INCOHÉRENCE%' THEN 1 ELSE 2 END,
    e.progress DESC;

-- 3. Corriger les progressions incohérentes
-- Mettre à jour la progression basée sur les leçons réellement complétées
UPDATE enrollments e
SET progress = (
    SELECT CASE 
        WHEN total_lecons.count = 0 THEN 0
        ELSE ROUND((completed_lecons.count * 100.0 / total_lecons.count), 2)
    END
    FROM (
        SELECT COUNT(*) as count
        FROM lecons l
        JOIN modules m ON l.module_id = m.id
        WHERE m.cours_id = e.cours_id
    ) as total_lecons
    CROSS JOIN (
        SELECT COUNT(*) as count
        FROM lecon_completions lc
        WHERE lc.enrollment_id = e.id
    ) as completed_lecons
)
WHERE EXISTS (
    SELECT 1 FROM users u WHERE u.id = e.user_id AND u.role = 'APPRENANT'
);

-- 4. Créer des données de test cohérentes si nécessaire
-- Trouver un cours avec des modules et leçons
SET @cours_test = (
    SELECT c.id 
    FROM cours c
    JOIN modules m ON c.id = m.cours_id
    JOIN lecons l ON m.id = l.module_id
    WHERE c.archived = FALSE
    GROUP BY c.id
    HAVING COUNT(DISTINCT l.id) >= 3
    ORDER BY COUNT(DISTINCT l.id) DESC
    LIMIT 1
);

-- Créer un utilisateur test s'il n'existe pas
INSERT IGNORE INTO users (email, password, first_name, last_name, role, created_at, updated_at) VALUES
('test.progression@example.com', '$2a$10$dummy', 'Test', 'Progression', 'APPRENANT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- Inscrire l'utilisateur au cours test
INSERT IGNORE INTO enrollments (user_id, cours_id, enrolled_at, progress, last_accessed_at)
SELECT 
    u.id,
    @cours_test,
    UNIX_TIMESTAMP() * 1000 - 86400000, -- Il y a 1 jour
    0, -- Commencer à 0%
    UNIX_TIMESTAMP() * 1000 - 3600000 -- Il y a 1 heure
FROM users u
WHERE u.email = 'test.progression@example.com'
AND @cours_test IS NOT NULL;

-- Compléter quelques leçons pour créer une progression réaliste
SET @enrollment_test = (
    SELECT e.id 
    FROM enrollments e
    JOIN users u ON e.user_id = u.id
    WHERE u.email = 'test.progression@example.com'
    AND e.cours_id = @cours_test
);

-- Compléter 60% des leçons
INSERT IGNORE INTO lecon_completions (enrollment_id, lecon_id, completed_at)
SELECT 
    @enrollment_test,
    l.id,
    UNIX_TIMESTAMP() * 1000 - (RAND() * 86400000) -- Complétée dans les dernières 24h
FROM lecons l
JOIN modules m ON l.module_id = m.id
WHERE m.cours_id = @cours_test
AND @enrollment_test IS NOT NULL
ORDER BY m.ordre, l.ordre
LIMIT (
    SELECT CEIL(COUNT(*) * 0.6) 
    FROM lecons l2 
    JOIN modules m2 ON l2.module_id = m2.id 
    WHERE m2.cours_id = @cours_test
);

-- Recalculer la progression pour cet enrollment
UPDATE enrollments e
SET progress = (
    SELECT CASE 
        WHEN total_lecons.count = 0 THEN 0
        ELSE ROUND((completed_lecons.count * 100.0 / total_lecons.count), 2)
    END
    FROM (
        SELECT COUNT(*) as count
        FROM lecons l
        JOIN modules m ON l.module_id = m.id
        WHERE m.cours_id = e.cours_id
    ) as total_lecons
    CROSS JOIN (
        SELECT COUNT(*) as count
        FROM lecon_completions lc
        WHERE lc.enrollment_id = e.id
    ) as completed_lecons
)
WHERE e.id = @enrollment_test;

-- 5. Vérifier les résultats après correction
SELECT 
    'Résultats après correction' as section,
    e.id as enrollment_id,
    u.email,
    c.titre,
    e.progress as progression_corrigee,
    COUNT(DISTINCT lc.id) as lecons_completees,
    (SELECT COUNT(*) FROM lecons l 
     JOIN modules m ON l.module_id = m.id 
     WHERE m.cours_id = c.id) as total_lecons,
    ROUND((COUNT(DISTINCT lc.id) * 100.0 / (SELECT COUNT(*) FROM lecons l JOIN modules m ON l.module_id = m.id WHERE m.cours_id = c.id)), 2) as progression_calculee
FROM enrollments e
JOIN users u ON e.user_id = u.id
JOIN cours c ON e.cours_id = c.id
LEFT JOIN lecon_completions lc ON e.id = lc.enrollment_id
WHERE u.role = 'APPRENANT'
AND (SELECT COUNT(*) FROM lecons l JOIN modules m ON l.module_id = m.id WHERE m.cours_id = c.id) > 0
GROUP BY e.id, u.email, c.titre, e.progress
ORDER BY e.progress DESC
LIMIT 10;

-- 6. Instructions de test
SELECT CONCAT('
🔧 CORRECTION DE LA PROGRESSION DES LEÇONS:

Cours de test: ID ', COALESCE(@cours_test, 'AUCUN'), '
Utilisateur test: test.progression@example.com

CORRECTIONS APPLIQUÉES:
✅ Recalcul de toutes les progressions basé sur les leçons réellement complétées
✅ Création d\'un utilisateur test avec progression réaliste
✅ Cohérence entre pourcentage et nombre de leçons

POUR TESTER:
1. Connectez-vous en tant que formateur
2. Allez sur les détails du cours ID ', COALESCE(@cours_test, 'X'), '
3. Cliquez sur "Apprenants inscrits"
4. Vérifiez que le nombre de leçons correspond au pourcentage

EXEMPLE ATTENDU:
- Si progression = 60%, alors leçons complétées = 60% du total
- Si progression = 100%, alors toutes les leçons sont complétées

Si le problème persiste:
- Redémarrez le backend pour appliquer les modifications du code
- Vérifiez les logs pour les erreurs de calcul
') as instructions;