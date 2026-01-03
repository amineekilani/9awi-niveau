-- Migration pour ajouter les mots clés aux cours
ALTER TABLE cours ADD COLUMN keywords VARCHAR(500);
