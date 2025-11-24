-- Migration pour mettre à jour la structure des questions
-- À exécuter si vous avez déjà créé les tables avec l'ancien script

-- Supprimer l'ancienne table question_options si elle existe
DROP TABLE IF EXISTS question_options;

-- Modifier la table question pour ajouter la colonne options si elle n'existe pas
ALTER TABLE question 
ADD COLUMN IF NOT EXISTS options TEXT NOT NULL AFTER question;

-- Si la table existe déjà et que vous voulez tout recommencer proprement :
-- DROP TABLE IF EXISTS question;
-- DROP TABLE IF EXISTS quiz;

-- Puis exécutez le script migration_add_quiz.sql
