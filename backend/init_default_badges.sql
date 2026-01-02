-- Script pour mettre à jour les icônes des badges existants
-- Exécuter après avoir créé les images dans frontend/public/badges/

UPDATE badges SET icon_url = '/badges/first-course.svg' WHERE criteria_type = 'FIRST_COURSE';
UPDATE badges SET icon_url = '/badges/first-course.svg' WHERE criteria_type = 'COURS_COMPLETED' AND icon_url IS NULL;
UPDATE badges SET icon_url = '/badges/quiz-master.svg' WHERE criteria_type = 'QUIZ_PASSED';
UPDATE badges SET icon_url = '/badges/quiz-master.svg' WHERE criteria_type = 'FIRST_QUIZ';
UPDATE badges SET icon_url = '/badges/perfect-score.svg' WHERE criteria_type = 'PERFECT_SCORE';
UPDATE badges SET icon_url = '/badges/streak-master.svg' WHERE criteria_type = 'STREAK_DAYS';
UPDATE badges SET icon_url = '/badges/default-badge.svg' WHERE criteria_type = 'XP_EARNED' AND icon_url IS NULL;
UPDATE badges SET icon_url = '/badges/default-badge.svg' WHERE criteria_type = 'CHALLENGE_COMPLETED' AND icon_url IS NULL;
UPDATE badges SET icon_url = '/badges/default-badge.svg' WHERE criteria_type = 'LEVEL_REACHED' AND icon_url IS NULL;

-- Vérifier les mises à jour
SELECT name, criteria_type, icon_url FROM badges ORDER BY created_at;