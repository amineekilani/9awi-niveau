-- Migration pour ajouter le champ thumbnail_url à la table cours
ALTER TABLE cours ADD COLUMN thumbnail_url VARCHAR(500);
