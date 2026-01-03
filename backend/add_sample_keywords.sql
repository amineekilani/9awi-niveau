-- Script pour ajouter des mots-clés d'exemple aux cours existants
-- Exécuter ce script pour tester la fonctionnalité de recherche par mots-clés

-- Mise à jour des cours avec des mots-clés pertinents
UPDATE cours SET keywords = 'java,programmation,débutant,orienté objet' WHERE titre LIKE '%Java%' OR description LIKE '%Java%';
UPDATE cours SET keywords = 'javascript,web,frontend,développement' WHERE titre LIKE '%JavaScript%' OR description LIKE '%JavaScript%';
UPDATE cours SET keywords = 'python,programmation,data science,machine learning' WHERE titre LIKE '%Python%' OR description LIKE '%Python%';
UPDATE cours SET keywords = 'html,css,web,design,frontend' WHERE titre LIKE '%HTML%' OR titre LIKE '%CSS%' OR description LIKE '%HTML%' OR description LIKE '%CSS%';
UPDATE cours SET keywords = 'angular,typescript,framework,spa' WHERE titre LIKE '%Angular%' OR description LIKE '%Angular%';
UPDATE cours SET keywords = 'react,javascript,frontend,composants' WHERE titre LIKE '%React%' OR description LIKE '%React%';
UPDATE cours SET keywords = 'sql,base de données,mysql,requêtes' WHERE titre LIKE '%SQL%' OR titre LIKE '%base%' OR description LIKE '%SQL%' OR description LIKE '%base%';
UPDATE cours SET keywords = 'design,ui,ux,interface,utilisateur' WHERE titre LIKE '%Design%' OR titre LIKE '%UI%' OR titre LIKE '%UX%' OR description LIKE '%design%';
UPDATE cours SET keywords = 'marketing,digital,réseaux sociaux,publicité' WHERE titre LIKE '%Marketing%' OR description LIKE '%marketing%';
UPDATE cours SET keywords = 'business,entrepreneuriat,gestion,stratégie' WHERE titre LIKE '%Business%' OR titre LIKE '%Entreprise%' OR description LIKE '%business%';

-- Ajouter des mots-clés génériques pour les cours sans mots-clés spécifiques
UPDATE cours SET keywords = 'formation,apprentissage,cours en ligne' WHERE keywords IS NULL OR keywords = '';

-- Vérifier les résultats
SELECT id, titre, keywords FROM cours WHERE keywords IS NOT NULL AND keywords != '' LIMIT 10;

-- Statistiques
SELECT 
    COUNT(*) as total_cours,
    COUNT(CASE WHEN keywords IS NOT NULL AND keywords != '' THEN 1 END) as cours_avec_mots_cles,
    ROUND(COUNT(CASE WHEN keywords IS NOT NULL AND keywords != '' THEN 1 END) * 100.0 / COUNT(*), 2) as pourcentage
FROM cours;